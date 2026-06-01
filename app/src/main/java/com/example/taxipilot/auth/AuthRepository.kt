package com.example.taxipilot.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth  = FirebaseAuth.getInstance()
    private val users = FirebaseFirestore.getInstance().collection("users")

    val currentUser: FirebaseUser? get() = auth.currentUser

    /** Emits the current FirebaseUser (null = signed out). */
    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, password: String): Result<UserProfile> = runCatching {
        val uid = auth.signInWithEmailAndPassword(email, password).await().user?.uid
            ?: error("Authentification échouée")
        getUserProfile(uid) ?: error("Profil introuvable. Veuillez vous réinscrire.")
    }.mapFailure()

    suspend fun register(
        email: String,
        password: String,
        nom: String,
        telephone: String,
        role: UserRole
    ): Result<UserProfile> = runCatching {
        val uid = auth.createUserWithEmailAndPassword(email, password).await().user?.uid
            ?: error("Création du compte échouée")

        // Proprietaire gets a unique 6-digit code on registration
        val codeProprietaire = if (role == UserRole.PROPRIETAIRE) generateUniqueCode() else null

        val profile = UserProfile(
            uid              = uid,
            email            = email,
            nom              = nom,
            telephone        = telephone,
            role             = role,
            codeProprietaire = codeProprietaire
        )
        users.document(uid).set(profile.toMap()).await()
        profile
    }.mapFailure()

    suspend fun getUserProfile(uid: String): UserProfile? = runCatching {
        val doc = users.document(uid).get().await()
        if (!doc.exists()) return null
        doc.toUserProfile()
    }.getOrNull()

    suspend fun updateFcmToken(uid: String, token: String) {
        runCatching { users.document(uid).update("fcmToken", token).await() }
    }

    // ── Chauffeur ↔ Proprietaire linking ─────────────────────────────────────

    /**
     * Finds the proprietaire whose [codeProprietaire] matches [code].
     * Returns null if no match or network error.
     */
    suspend fun getProprietaireByCode(code: String): UserProfile? = runCatching {
        val query = users
            .whereEqualTo("codeProprietaire", code)
            .whereEqualTo("role", UserRole.PROPRIETAIRE.name)
            .limit(1)
            .get().await()
        query.documents.firstOrNull()?.toUserProfile()
    }.getOrNull()

    /**
     * Sets [proprietaireId] on the chauffeur's profile.
     * After this, authStateFlow will emit again and the NavHost transitions
     * from NeedsProprietaireLink → Authenticated.
     */
    suspend fun linkChauffeurToProprietaire(chauffeurUid: String, proprietaireId: String) {
        users.document(chauffeurUid).update("proprietaireId", proprietaireId).await()
    }

    /** Updates name and phone in Firestore. */
    suspend fun updateProfile(uid: String, nom: String, telephone: String): Result<Unit> =
        runCatching {
            users.document(uid).update(mapOf("nom" to nom, "telephone" to telephone)).await()
            Unit
        }.mapFailure()

    /** Changes Firebase Auth password. */
    suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        auth.currentUser?.updatePassword(newPassword)?.await()
            ?: error("Utilisateur non connecté")
        Unit
    }.mapFailure()

    fun signOut() = auth.signOut()

    // ── Internals ─────────────────────────────────────────────────────────────

    private suspend fun generateUniqueCode(): String {
        var code: String
        do {
            code = (100000..999999).random().toString()
            val existing = users
                .whereEqualTo("codeProprietaire", code)
                .limit(1).get().await()
        } while (!existing.isEmpty)
        return code
    }

    private fun DocumentSnapshot.toUserProfile(): UserProfile? {
        val roleStr = getString("role") ?: return null
        val role    = runCatching { UserRole.valueOf(roleStr) }.getOrNull() ?: return null
        return UserProfile(
            uid              = getString("uid")              ?: id,
            email            = getString("email")            ?: "",
            nom              = getString("nom")              ?: "",
            telephone        = getString("telephone")        ?: "",
            role             = role,
            fcmToken         = getString("fcmToken"),
            createdAt        = getLong("createdAt")          ?: 0L,
            codeProprietaire = getString("codeProprietaire"),
            proprietaireId   = getString("proprietaireId"),
            assignedTaxi     = getString("assignedTaxi"),
            statut           = getString("statut")           ?: "disponible"
        )
    }

    private fun <T> Result<T>.mapFailure(): Result<T> = this.recoverCatching { e ->
        val msg = e.message ?: ""
        throw Exception(
            when {
                "INVALID_EMAIL"             in msg -> "Email invalide"
                "WRONG_PASSWORD"            in msg ||
                "INVALID_LOGIN_CREDENTIALS" in msg -> "Email ou mot de passe incorrect"
                "USER_NOT_FOUND"            in msg -> "Aucun compte avec cet email"
                "EMAIL_ALREADY_IN_USE"      in msg -> "Cet email est déjà utilisé"
                "WEAK_PASSWORD"             in msg -> "Mot de passe trop faible (6 caractères min.)"
                "NETWORK_ERROR"             in msg -> "Vérifiez votre connexion internet"
                else                               -> e.message ?: "Une erreur est survenue"
            }
        )
    }
}
