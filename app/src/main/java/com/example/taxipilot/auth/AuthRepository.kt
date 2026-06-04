package com.example.taxipilot.auth

// Repository d'authentification Firebase — gère toute la logique Auth et profil Firestore.
// Responsabilités :
//   - Connexion et inscription via Firebase Email/Password
//   - Lecture et écriture du profil utilisateur dans Firestore (collection "users")
//   - Liage Chauffeur ↔ Propriétaire via code à 6 chiffres
//   - Mise à jour du token FCM après connexion
//   - Traduction des codes d'erreur Firebase en messages français lisibles
//
// authStateFlow() : émet l'utilisateur Firebase courant en temps réel
//   → null quand déconnecté, FirebaseUser quand connecté
//   → utilisé par AuthViewModel.init pour détecter les changements d'état de connexion

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

    // Utilisateur Firebase actuellement connecté (null si déconnecté)
    val currentUser: FirebaseUser? get() = auth.currentUser

    // Flux temps réel de l'état de connexion Firebase
    // Émet null quand l'utilisateur se déconnecte, FirebaseUser quand il se connecte
    fun authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // Connecte un utilisateur existant par email/mot de passe
    // Retourne Result<UserProfile> : succès avec le profil, ou échec avec message d'erreur traduit
    suspend fun signIn(email: String, password: String): Result<UserProfile> = runCatching {
        val uid = auth.signInWithEmailAndPassword(email, password).await().user?.uid
            ?: error("Authentification échouée")
        getUserProfile(uid) ?: error("Profil introuvable. Veuillez vous réinscrire.")
    }.mapFailure()

    // Crée un nouveau compte Firebase et le profil Firestore correspondant
    // Les PROPRIETAIRE reçoivent un code unique à 6 chiffres à la création
    suspend fun register(
        email: String,
        password: String,
        nom: String,
        telephone: String,
        role: UserRole
    ): Result<UserProfile> = runCatching {
        val uid = auth.createUserWithEmailAndPassword(email, password).await().user?.uid
            ?: error("Création du compte échouée")

        // Génère un code unique pour les propriétaires (les chauffeurs et clients n'en ont pas)
        val codeProprietaire = if (role == UserRole.PROPRIETAIRE) generateUniqueCode() else null

        val profile = UserProfile(
            uid              = uid,
            email            = email,
            nom              = nom,
            telephone        = telephone,
            role             = role,
            codeProprietaire = codeProprietaire
        )
        users.document(uid).set(profile.toMap()).await() // écrit le profil dans Firestore
        profile
    }.mapFailure()

    // Lit le profil Firestore d'un utilisateur par son UID (retourne null si introuvable)
    suspend fun getUserProfile(uid: String): UserProfile? = runCatching {
        val doc = users.document(uid).get().await()
        if (!doc.exists()) return null
        doc.toUserProfile()
    }.getOrNull()

    // Met à jour le token FCM dans Firestore après connexion ou rotation de token
    suspend fun updateFcmToken(uid: String, token: String) {
        runCatching { users.document(uid).update("fcmToken", token).await() }
    }

    // ── Liage Chauffeur ↔ Propriétaire ───────────────────────────────────────

    // Cherche le propriétaire dont le codeProprietaire correspond à [code]
    // Retourne null si aucun propriétaire n'a ce code (ou en cas d'erreur réseau)
    suspend fun getProprietaireByCode(code: String): UserProfile? = runCatching {
        val query = users
            .whereEqualTo("codeProprietaire", code)
            .whereEqualTo("role", UserRole.PROPRIETAIRE.name)
            .limit(1)
            .get().await()
        query.documents.firstOrNull()?.toUserProfile()
    }.getOrNull()

    // Inscrit un chauffeur sous un propriétaire en écrivant son proprietaireId dans Firestore
    // Après ça, authStateFlow réémet et NavHost passe de NeedsProprietaireLink → Authenticated
    suspend fun linkChauffeurToProprietaire(chauffeurUid: String, proprietaireId: String) {
        users.document(chauffeurUid).update("proprietaireId", proprietaireId).await()
    }

    // Met à jour nom et téléphone dans Firestore (pas l'email — non modifiable)
    suspend fun updateProfile(uid: String, nom: String, telephone: String): Result<Unit> =
        runCatching {
            users.document(uid).update(mapOf("nom" to nom, "telephone" to telephone)).await()
            Unit
        }.mapFailure()

    // Change le mot de passe Firebase Auth de l'utilisateur connecté
    suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        auth.currentUser?.updatePassword(newPassword)?.await()
            ?: error("Utilisateur non connecté")
        Unit
    }.mapFailure()

    // Déconnecte l'utilisateur (efface la session Firebase Auth locale)
    fun signOut() = auth.signOut()

    // ── Fonctions internes ────────────────────────────────────────────────────

    // Génère un code numérique à 6 chiffres unique (pas déjà utilisé par un autre propriétaire)
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

    // Convertit un DocumentSnapshot Firestore en UserProfile (retourne null si le rôle est invalide)
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

    // Traduit les codes d'erreur Firebase en messages en français clairs pour l'utilisateur
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
