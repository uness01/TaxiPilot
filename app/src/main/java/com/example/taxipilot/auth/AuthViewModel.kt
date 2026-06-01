package com.example.taxipilot.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    sealed class AuthState {
        data object Loading : AuthState()
        data object Unauthenticated : AuthState()
        /** Chauffeur registered but not yet linked to a proprietaire. */
        data class NeedsProprietaireLink(val profile: UserProfile) : AuthState()
        data class Authenticated(val profile: UserProfile) : AuthState()
    }

    sealed class LinkState {
        data object Idle    : LinkState()
        data object Loading : LinkState()
        data object Success : LinkState()
        data class  Error(val message: String) : LinkState()
    }

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _linkState = MutableStateFlow<LinkState>(LinkState.Idle)
    val linkState: StateFlow<LinkState> = _linkState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.authStateFlow().collect { firebaseUser ->
                if (firebaseUser == null) {
                    _authState.value = AuthState.Unauthenticated
                } else {
                    val profile = authRepository.getUserProfile(firebaseUser.uid)
                    if (profile != null) {
                        _authState.value = resolveState(profile)
                    } else {
                        // Firebase auth exists but no Firestore profile — force sign-out
                        authRepository.signOut()
                    }
                }
            }
        }
    }

    private fun resolveState(profile: UserProfile): AuthState =
        if (profile.role == UserRole.CHAUFFEUR && profile.proprietaireId == null)
            AuthState.NeedsProprietaireLink(profile)
        else
            AuthState.Authenticated(profile)

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.signIn(email, password)
                .onSuccess { profile -> saveFcmToken(profile.uid) }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    fun register(
        email: String,
        password: String,
        nom: String,
        telephone: String,
        role: UserRole
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            authRepository.register(email, password, nom, telephone, role)
                .onSuccess { profile -> saveFcmToken(profile.uid) }
                .onFailure { _error.value = it.message }
            _isLoading.value = false
        }
    }

    /** Chauffeur enters their proprietaire's 6-digit code to link accounts. */
    fun linkToProprietaire(code: String) {
        val profile = (_authState.value as? AuthState.NeedsProprietaireLink)?.profile ?: return
        viewModelScope.launch {
            _linkState.value = LinkState.Loading
            val proprietaire = authRepository.getProprietaireByCode(code.trim())
            if (proprietaire == null) {
                _linkState.value = LinkState.Error("Code invalide. Vérifiez avec votre Propriétaire.")
                return@launch
            }
            try {
                authRepository.linkChauffeurToProprietaire(profile.uid, proprietaire.uid)
                // authStateFlow only fires on sign-in/out, not Firestore changes —
                // manually reload the profile so the state transitions immediately.
                val updated = authRepository.getUserProfile(profile.uid)
                if (updated != null) _authState.value = resolveState(updated)
                _linkState.value = LinkState.Success
            } catch (e: Exception) {
                _linkState.value = LinkState.Error("Erreur réseau. Réessayez.")
            }
        }
    }

    fun clearLinkState() { _linkState.value = LinkState.Idle }

    // ── Profile update ────────────────────────────────────────────────────────

    sealed class ProfileUpdateState {
        data object Idle    : ProfileUpdateState()
        data object Loading : ProfileUpdateState()
        data object Success : ProfileUpdateState()
        data class  Error(val message: String) : ProfileUpdateState()
    }

    private val _profileUpdateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val profileUpdateState: StateFlow<ProfileUpdateState> = _profileUpdateState.asStateFlow()

    fun updateProfile(nom: String, telephone: String) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _profileUpdateState.value = ProfileUpdateState.Loading
            authRepository.updateProfile(uid, nom.trim(), telephone.trim())
                .onSuccess { _profileUpdateState.value = ProfileUpdateState.Success }
                .onFailure { _profileUpdateState.value = ProfileUpdateState.Error(it.message ?: "Erreur") }
        }
    }

    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _profileUpdateState.value = ProfileUpdateState.Loading
            authRepository.updatePassword(newPassword)
                .onSuccess { _profileUpdateState.value = ProfileUpdateState.Success }
                .onFailure { _profileUpdateState.value = ProfileUpdateState.Error(it.message ?: "Erreur") }
        }
    }

    fun clearProfileUpdateState() { _profileUpdateState.value = ProfileUpdateState.Idle }

    fun signOut() {
        authRepository.signOut()
    }

    fun clearError() { _error.value = null }

    // ── FCM token ─────────────────────────────────────────────────────────────

    private fun saveFcmToken(uid: String) {
        viewModelScope.launch {
            runCatching {
                val token = FirebaseMessaging.getInstance().token.await()
                authRepository.updateFcmToken(uid, token)
            }
        }
    }

    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(authRepository) as T
    }
}
