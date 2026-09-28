package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

sealed class GoogleAuthResult {
    data class Success(
        val email: String,
        val displayName: String,
        val photoUrl: String?,
        val idToken: String?,
        val firebaseUid: String?
    ) : GoogleAuthResult()

    data object Cancelled : GoogleAuthResult()
    data class Error(val message: String, val canFallback: Boolean = false) : GoogleAuthResult()
}

class GoogleAuthManager(private val context: Context) {
    companion object {
        private const val TAG = "GoogleAuthManager"

        fun getWebClientId(context: Context): String {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) {
                val found = context.getString(resId)
                if (found.isNotBlank()) return found
            }
            return "807434162865-mobikhata.apps.googleusercontent.com"
        }
    }

    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(
        context: Context,
        customServerClientId: String? = null
    ): GoogleAuthResult {
        val serverClientId = customServerClientId ?: getWebClientId(context)
        return try {
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val email = googleIdTokenCredential.id
                val displayName = googleIdTokenCredential.displayName ?: email.substringBefore("@")
                val photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                var firebaseUid: String? = null

                try {
                    if (FirebaseApp.getApps(context).isNotEmpty()) {
                        val auth = FirebaseAuth.getInstance()
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult = auth.signInWithCredential(firebaseCredential).await()
                        firebaseUid = authResult.user?.uid
                        Log.d(TAG, "Firebase Auth sign-in successful: uid=$firebaseUid")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Firebase Auth sign-in note: ${e.message}")
                }

                GoogleAuthResult.Success(
                    email = email,
                    displayName = displayName,
                    photoUrl = photoUrl,
                    idToken = idToken,
                    firebaseUid = firebaseUid
                )
            } else {
                GoogleAuthResult.Error(
                    message = "Unexpected credential type: ${credential.type}",
                    canFallback = true
                )
            }
        } catch (e: GetCredentialCancellationException) {
            Log.i(TAG, "User cancelled Google Sign-In via Credential Manager")
            GoogleAuthResult.Cancelled
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No Google credentials available on device: ${e.message}")
            GoogleAuthResult.Error(
                message = "No Google account found on device (${e.message})",
                canFallback = true
            )
        } catch (e: GoogleIdTokenParsingException) {
            Log.e(TAG, "Failed to parse Google ID token: ${e.message}", e)
            GoogleAuthResult.Error(
                message = "Token parsing error: ${e.message}",
                canFallback = true
            )
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Credential Manager error: ${e.message}")
            GoogleAuthResult.Error(
                message = "Google Sign-In: ${e.message}",
                canFallback = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during Google Sign-In", e)
            GoogleAuthResult.Error(
                message = e.localizedMessage ?: "Sign-in failed",
                canFallback = true
            )
        }
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.w(TAG, "Error clearing credential state: ${e.message}")
        }
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseAuth.getInstance().signOut()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error signing out of Firebase Auth: ${e.message}")
        }
    }

    fun getCurrentFirebaseUser() = try {
        if (FirebaseApp.getApps(context).isNotEmpty()) {
            FirebaseAuth.getInstance().currentUser
        } else null
    } catch (e: Exception) {
        null
    }
}
