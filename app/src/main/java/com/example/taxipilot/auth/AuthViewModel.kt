package com.example.taxipilot.auth

// ViewModel d'authentification — fait le pont entre AuthRepository et les écrans UI.
// Gère trois machines à états :
//
//   AuthState : état global de connexion
//     Loading → (vérif Firebase) → Unauthenticated | NeedsProprietaireLink | Authenticated
//
//   LinkState : état du liage chauffeur ↔ propriétaire
//     Idle → Loading → Success | Error
//
//   ProfileUpdateState : état de la mise à jour du profil
//     Idle → Loading → Success | Error
//
// Cas spécial NeedsProprietaireLink : un chauffeur venant de s'inscrire n'a pas encore
// de proprietaireId → il est bloqué sur LinkCodeScreen jusqu'à saisir le bon code.
// resolveState() détermine si le profil doit aller vers NeedsProprietaireLink ou Authenticated.

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

    // ── Machine à états d'authentification ───────────────────────────────────
    sealed class AuthState {
        data object Loading : AuthState()           // vérification de la session en cours
        data object Unauthenticated : AuthState()   // pas de session active
        // Chauffeur inscrit mais pas encore lié à un propriétaire (bloqué sur LinkCodeScreen)
        data class NeedsProprietaireLink(val profile: UserProfile) : AuthState()
        data class Authenticated(val profile: UserProfile) : AuthState() // connecté et prêt
    }

    // ── Machine à états de liage propriétaire ─────────────────────────────────
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
        // Observe l'état Firebase Auth en temps réel — se déclenche à chaque connexion/déconnexion
        viewModelScope.launch {
            authRepository.authStateFlow().collect { firebaseUser ->
                if (firebaseUser == null) {
                    _authState.value = AuthState.Unauthenticated
                } else {
                    val profile = authRepository.getUserProfile(firebaseUser.uid)
                    if (profile != null) {
                        _authState.value = resolveState(profile)
                    } else {
                        // Firebase Auth existe mais pas de document Firestore → déconnexion forcée
                        authRepository.signOut()
                    }
                }
            }
        }
    }

    // Détermine l'état après connexion : chauffeur sans propriétaire → NeedsProprietaireLink
    private fun resolveState(profile: UserProfile): AuthState =
        if (profile.role == UserRole.CHAUFFEUR && profile.proprietaireId == null)
            AuthState.NeedsProprietaireLink(profile)
        else
            AuthState.Authenticated(profile)

    // Connecte un utilisateur existant → si succès, sauvegarde le token FCM
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

    // Crée un nouveau compte et le profil Firestore → si succès, sauvegarde le token FCM
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

    // Chauffeur entre le code à 6 chiffres de son propriétaire pour lier les deux comptes
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
                // authStateFlow ne se redéclenche pas sur les changements Firestore seuls
                // → on recharge manuellement le profil pour déclencher la transition d'état
                val updated = authRepository.getUserProfile(profile.uid)
                if (updated != null) _authState.value = resolveState(updated)
                _linkState.value = LinkState.Success
            } catch (e: Exception) {
                _linkState.value = LinkState.Error("Erreur réseau. Réessayez.")
            }
        }
    }

    // Remet l'état de liage à Idle (après affichage d'une erreur ou d'un succès)
    fun clearLinkState() { _linkState.value = LinkState.Idle }

    // ── Mise à jour du profil ─────────────────────────────────────────────────

    sealed class ProfileUpdateState {
        data object Idle    : ProfileUpdateState()
        data object Loading : ProfileUpdateState()
        data object Success : ProfileUpdateState()
        data class  Error(val message: String) : ProfileUpdateState()
    }

    private val _profileUpdateState = MutableStateFlow<ProfileUpdateState>(ProfileUpdateState.Idle)
    val profileUpdateState: StateFlow<ProfileUpdateState> = _profileUpdateState.asStateFlow()

    // Met à jour nom et téléphone dans Firestore
    fun updateProfile(nom: String, telephone: String) {
        val uid = authRepository.currentUser?.uid ?: return
        viewModelScope.launch {
            _profileUpdateState.value = ProfileUpdateState.Loading
            authRepository.updateProfile(uid, nom.trim(), telephone.trim())
                .onSuccess { _profileUpdateState.value = ProfileUpdateState.Success }
                .onFailure { _profileUpdateState.value = ProfileUpdateState.Error(it.message ?: "Erreur") }
        }
    }

    // Change le mot de passe Firebase Auth
    fun updatePassword(newPassword: String) {
        viewModelScope.launch {
            _profileUpdateState.value = ProfileUpdateState.Loading
            authRepository.updatePassword(newPassword)
                .onSuccess { _profileUpdateState.value = ProfileUpdateState.Success }
                .onFailure { _profileUpdateState.value = ProfileUpdateState.Error(it.message ?: "Erreur") }
        }
    }

    fun clearProfileUpdateState() { _profileUpdateState.value = ProfileUpdateState.Idle }

    // Déconnecte l'utilisateur → authStateFlow émet null → NavHost revient sur LoginScreen
    fun signOut() {
        authRepository.signOut()
    }

    fun clearError() { _error.value = null }

    // ── Token FCM ─────────────────────────────────────────────────────────────

    // Lit le token FCM actuel et le sauvegarde dans Firestore (appelé après connexion/inscription)
    private fun saveFcmToken(uid: String) {
        viewModelScope.launch {
            runCatching {
                val token = FirebaseMessaging.getInstance().token.await()
                authRepository.updateFcmToken(uid, token)
            }
        }
    }

    // Factory manuelle pour créer le ViewModel avec son AuthRepository (pas de Hilt)
    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(authRepository) as T
    }
}
