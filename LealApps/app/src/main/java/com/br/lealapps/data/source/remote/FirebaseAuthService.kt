package com.br.lealapps.data.source.remote

import android.content.ContentValues.TAG
import android.util.Log
import com.br.lealapps.data.repository.AuthCallback
import com.br.lealapps.data.repository.AuthRepository
import com.br.lealapps.domain.model.AuthError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class FirebaseAuthService : AuthRepository {

    private val firebaseAuth = Firebase.auth

    override suspend fun signIn(email: String, password: String, callback: AuthCallback) {
        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            callback.onAuthSuccess(result.user!!)
            Log.d(TAG, "login:success:")
        } catch (e: FirebaseAuthException) {
            // Usuário não existe ou credenciais inválidas
            callback.onAuthFailed(AuthError.InvalidCredentials)
        } catch (e: Exception) {
            // Outro erro no login
            callback.onAuthFailed(AuthError.OtherError)
            Log.w(TAG,"login:error: $e")
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun createUser(email: String, password: String, callback: AuthCallback) {
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            callback.onAuthSuccess(result.user!!)
            Log.d(TAG, "createUser:success:")
        } catch (e: Exception) {
            callback.onAuthFailed(AuthError.CreateUserError)
        }
    }
}
