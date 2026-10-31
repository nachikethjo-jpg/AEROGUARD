package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.local.PollutionReportEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class UserProfile(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false
)

class FirebaseService(private val context: Context) {

    private val isFirebaseAvailable: Boolean by lazy {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            true
        } catch (e: Exception) {
            Log.w("FirebaseService", "FirebaseApp init skipped: ${e.message}")
            false
        }
    }

    private val auth: FirebaseAuth? by lazy {
        if (isFirebaseAvailable) {
            try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
        } else null
    }

    private val firestore: FirebaseFirestore? by lazy {
        if (isFirebaseAvailable) {
            try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
        } else null
    }

    fun getCurrentUser(): UserProfile? {
        val user = auth?.currentUser
        return if (user != null) {
            UserProfile(
                uid = user.uid,
                displayName = user.displayName ?: if (user.isAnonymous) "Guest Citizen" else "AeroGuard User",
                email = user.email ?: "guest@aeroguard.app",
                photoUrl = user.photoUrl?.toString(),
                isAnonymous = user.isAnonymous
            )
        } else null
    }

    suspend fun signInAnonymously(): Result<UserProfile> {
        val authInstance = auth ?: return Result.success(
            UserProfile(
                uid = "local_guest_uid",
                displayName = "Durg Citizen",
                email = "citizen@durg.gov.in",
                isAnonymous = true
            )
        )

        return try {
            val result = authInstance.signInAnonymously().await()
            val user = result.user
            if (user != null) {
                Result.success(
                    UserProfile(
                        uid = user.uid,
                        displayName = "Verified Citizen",
                        email = "anonymous_${user.uid.take(5)}@aeroguard.app",
                        isAnonymous = true
                    )
                )
            } else {
                Result.failure(Exception("Failed to acquire anonymous user"))
            }
        } catch (e: Exception) {
            // Graceful fallback for environments without live Google Play Services
            Result.success(
                UserProfile(
                    uid = "local_dev_user",
                    displayName = "Durg Resident (Offline)",
                    email = "nachiketh.JO@gmail.com",
                    isAnonymous = false
                )
            )
        }
    }

    suspend fun signInWithDemoGoogleAccount(email: String, displayName: String): Result<UserProfile> {
        // Authenticate or set user profile
        return Result.success(
            UserProfile(
                uid = "google_user_${email.hashCode()}",
                displayName = displayName,
                email = email,
                isAnonymous = false
            )
        )
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.w("FirebaseService", "Sign out note: ${e.message}")
        }
    }

    suspend fun syncReportToFirestore(report: PollutionReportEntity): Boolean {
        val db = firestore ?: return false
        return try {
            val reportMap = hashMapOf(
                "sourceType" to report.sourceType,
                "location" to report.location,
                "description" to report.description,
                "severity" to report.severity,
                "timestamp" to report.timestamp,
                "upvotes" to report.upvotes,
                "status" to report.status,
                "authorName" to report.authorName,
                "imageUri" to (report.imageUri ?: "")
            )
            db.collection("pollution_reports")
                .document("report_${report.id}")
                .set(reportMap)
                .await()
            true
        } catch (e: Exception) {
            Log.w("FirebaseService", "Firestore sync note: ${e.message}")
            false
        }
    }

    suspend fun upvoteReportInFirestore(reportId: Long): Boolean {
        val db = firestore ?: return false
        return try {
            val docRef = db.collection("pollution_reports").document("report_$reportId")
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentVotes = snapshot.getLong("upvotes") ?: 0
                transaction.update(docRef, "upvotes", currentVotes + 1)
            }.await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
