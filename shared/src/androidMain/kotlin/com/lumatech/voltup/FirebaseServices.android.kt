package com.lumatech.voltup

import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private const val TAG = "VoltUpFirebase"

fun initVoltUpFirebase(context: android.content.Context) {
    try {
        voltUpAppContext = context.applicationContext
        var apiKey = "AIzaSyC1sit0tMF-_QBowwG3yt505xoRnItx4Uo"
        var applicationId = "1:344024062833:android:voltupapp"
        var projectId = "voltup-21c34"
        var storageBucket = "voltup-21c34.firebasestorage.app"

        try {
            val jsonString = context.assets.open("google-services.json").bufferedReader().use { it.readText() }
            val root = org.json.JSONObject(jsonString)
            val projectInfo = root.optJSONObject("project_info")
            if (projectInfo != null) {
                projectId = projectInfo.optString("project_id", projectId)
                storageBucket = projectInfo.optString("storage_bucket", storageBucket)
            }
            val clients = root.optJSONArray("client")
            if (clients != null && clients.length() > 0) {
                val client = clients.getJSONObject(0)
                val clientInfo = client.optJSONObject("client_info")
                if (clientInfo != null) {
                    applicationId = clientInfo.optString("mobilesdk_app_id", applicationId)
                }
                val apiKeys = client.optJSONArray("api_key")
                if (apiKeys != null && apiKeys.length() > 0) {
                    apiKey = apiKeys.getJSONObject(0).optString("current_key", apiKey)
                }
            }
        } catch (_: Exception) {}

        val existingApps = com.google.firebase.FirebaseApp.getApps(context)
        if (existingApps.isNotEmpty()) {
            val currentApp = com.google.firebase.FirebaseApp.getInstance()
            if (currentApp.options.apiKey != apiKey || currentApp.options.projectId != projectId) {
                currentApp.delete()
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId(applicationId)
                    .setApiKey(apiKey)
                    .setProjectId(projectId)
                    .setStorageBucket(storageBucket)
                    .build()
                com.google.firebase.FirebaseApp.initializeApp(context, options)
            }
        } else {
            val options = com.google.firebase.FirebaseOptions.Builder()
                .setApplicationId(applicationId)
                .setApiKey(apiKey)
                .setProjectId(projectId)
                .setStorageBucket(storageBucket)
                .build()
            com.google.firebase.FirebaseApp.initializeApp(context, options)
        }

        try {
            val appCheck = FirebaseAppCheck.getInstance()
            val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
            if (isDebug) {
                appCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
                Log.d(TAG, "Firebase App Check zainicjalizowany z DebugAppCheckProviderFactory")
            } else {
                appCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
                Log.d(TAG, "Firebase App Check zainicjalizowany z PlayIntegrityAppCheckProviderFactory")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Błąd inicjalizacji Firebase App Check: ${e.message}", e)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Błąd inicjalizacji Firebase: ${e.message}", e)
        try {
            com.google.firebase.FirebaseApp.initializeApp(context)
        } catch (_: Exception) {}
    }
}

class AndroidFirebaseAuthService : VoltUpAuthService {
    private val authScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _currentUser = MutableStateFlow<VoltUpUser?>(null)
    override val currentUser: StateFlow<VoltUpUser?> = _currentUser.asStateFlow()

    private val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val dbService: VoltUpDatabaseService by lazy {
        getDatabaseService()
    }

    init {
        try {
            auth.addAuthStateListener { firebaseAuth ->
                val fbUser = firebaseAuth.currentUser
                if (fbUser != null) {
                    authScope.launch {
                        val dbUser = dbService.getUser(fbUser.uid)
                        if (dbUser != null) {
                            _currentUser.value = dbUser
                        } else {
                            val fallback = VoltUpUser(
                                userId = fbUser.uid,
                                email = fbUser.email.orEmpty(),
                                userName = fbUser.displayName?.ifBlank { "Użytkownik VoltUp" } ?: "Użytkownik VoltUp",
                                avatarId = 1,
                                voltPoints = 0,
                                totalEnergyWh = 0.0,
                                co2SavedKg = 0.0,
                                userLevel = 0,
                                streakDays = 1,
                                lastActiveDate = getTodayString(),
                                createdAt = System.currentTimeMillis()
                            )
                            dbService.saveUser(fallback)
                            _currentUser.value = fallback
                        }
                    }
                } else {
                    _currentUser.value = null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Błąd inicjalizacji nasłuchiwania Auth: ${e.message}", e)
        }
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        val cleanEmail = email.trim()
        val cleanPass = password.trim()
        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            return AuthResult(false, null, "Wypełnij wszystkie pola.")
        }

        return try {
            val result = auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
            val fbUser = result.user ?: return AuthResult(false, null, "Nie udało się pobrać konta.")

            var voltUpUser = dbService.getUser(fbUser.uid)
            if (voltUpUser == null) {
                voltUpUser = VoltUpUser(
                    userId = fbUser.uid,
                    email = fbUser.email ?: cleanEmail,
                    userName = fbUser.displayName?.ifBlank { "Użytkownik VoltUp" } ?: "Użytkownik VoltUp",
                    avatarId = 1,
                    voltPoints = 0,
                    totalEnergyWh = 0.0,
                    co2SavedKg = 0.0,
                    userLevel = 0,
                    streakDays = 1,
                    lastActiveDate = getTodayString(),
                    createdAt = System.currentTimeMillis()
                )
                dbService.saveUser(voltUpUser)
            }

            _currentUser.value = voltUpUser
            AuthResult(isSuccess = true, user = voltUpUser)
        } catch (e: Exception) {
            val msg = e.message.orEmpty()
            Log.e(TAG, "Błąd logowania: $msg", e)

            if (e is FirebaseAuthException) {
                AuthResult(isSuccess = false, errorMessage = mapFirebaseError(e))
            } else {
                AuthResult(isSuccess = false, errorMessage = mapGenericError(e))
            }
        }
    }

    override suspend fun signUp(email: String, password: String, userName: String, avatarId: Int): AuthResult {
        val cleanEmail = email.trim()
        val cleanPass = password.trim()
        val cleanName = userName.trim()

        if (cleanEmail.isBlank() || cleanPass.isBlank() || cleanName.isBlank()) {
            return AuthResult(false, null, "Uzupełnij wszystkie wymagane dane.")
        }
        if (cleanPass.length < 6) {
            return AuthResult(false, null, "Hasło musi mieć co najmniej 6 znaków.")
        }

        return try {
            val result = auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
            val fbUser = result.user ?: return AuthResult(false, null, "Błąd tworzenia konta.")

            val newUser = VoltUpUser(
                userId = fbUser.uid,
                email = cleanEmail,
                userName = cleanName,
                avatarId = avatarId,
                voltPoints = 0,
                totalEnergyWh = 0.0,
                co2SavedKg = 0.0,
                userLevel = 0,
                streakDays = 1,
                lastActiveDate = getTodayString(),
                createdAt = System.currentTimeMillis()
            )

            dbService.saveUser(newUser)

            dbService.saveUserSettings(
                UserSettingsDto(
                    userId = fbUser.uid,
                    themeMode = "SYSTEM",
                    notificationsEnabled = true,
                    soundEffectsEnabled = true,
                    hapticFeedbackEnabled = true
                )
            )

            initializeDefaultAchievements(fbUser.uid, dbService)

            initializeDefaultChallenges(fbUser.uid, dbService)

            _currentUser.value = newUser
            AuthResult(isSuccess = true, user = newUser)
        } catch (e: Exception) {
            val msg = e.message.orEmpty()
            Log.e(TAG, "Błąd rejestracji: $msg", e)

            if (e is FirebaseAuthException) {
                AuthResult(isSuccess = false, errorMessage = mapFirebaseError(e))
            } else {
                AuthResult(isSuccess = false, errorMessage = mapGenericError(e))
            }
        }
    }

    override suspend fun sendPasswordReset(email: String): AuthResult {
        return try {
            val cleanEmail = email.trim()
            if (cleanEmail.isBlank()) {
                return AuthResult(false, null, "Wprowadź adres e-mail.")
            }
            try {
                val savedLang = VoltUpStorage.getString(VoltUpPreferenceKeys.KEY_SAVED_LANGUAGE, "pl")
                auth.setLanguageCode(if (savedLang == "en") "en" else "pl")
            } catch (_: Exception) {}
            auth.sendPasswordResetEmail(cleanEmail).await()
            AuthResult(isSuccess = true)
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Błąd resetu hasła: ${e.errorCode}")
            AuthResult(isSuccess = false, errorMessage = mapFirebaseError(e))
        } catch (e: Exception) {
            Log.e(TAG, "Błąd ogólny resetu hasła: ${e.message}")
            AuthResult(isSuccess = false, errorMessage = mapGenericError(e))
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): AuthResult {
        return try {
            val cleanOld = oldPassword.trim()
            val cleanNew = newPassword.trim()
            if (cleanOld.isBlank()) {
                return AuthResult(false, null, "Wprowadź swoje dotychczasowe hasło.")
            }
            if (cleanNew.length < 6) {
                return AuthResult(false, null, "Nowe hasło musi zawierać co najmniej 6 znaków.")
            }
            val user = auth.currentUser ?: return AuthResult(false, null, "Brak zalogowanego użytkownika.")
            val email = user.email ?: return AuthResult(false, null, "Brak przypisanego adresu e-mail.")

            try {
                val credential = EmailAuthProvider.getCredential(email, cleanOld)
                user.reauthenticate(credential).await()
            } catch (e: Exception) {
                Log.w(TAG, "Błędne dotychczasowe hasło: ${e.message}")
                return AuthResult(false, null, "Aktualne hasło jest nieprawidłowe.")
            }

            user.updatePassword(cleanNew).await()
            AuthResult(isSuccess = true)
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Błąd zmiany hasła: ${e.errorCode}")
            AuthResult(isSuccess = false, errorMessage = mapFirebaseError(e))
        } catch (e: Exception) {
            Log.e(TAG, "Błąd ogólny zmiany hasła: ${e.message}")
            AuthResult(isSuccess = false, errorMessage = mapGenericError(e))
        }
    }

    override suspend fun updateProfile(userName: String, avatarId: Int): AuthResult {
        val current = _currentUser.value ?: return AuthResult(false, null, "Brak aktywnej sesji.")
        val updated = current.copy(userName = userName.trim(), avatarId = avatarId)
        val success = dbService.saveUser(updated)
        if (success) {
            _currentUser.value = updated
            return AuthResult(true, updated)
        }
        return AuthResult(false, null, "Nie udało się zaktualizować profilu.")
    }

    override suspend fun deleteAccount(): AuthResult {
        return try {
            val user = auth.currentUser ?: return AuthResult(false, null, "Brak aktywnej sesji użytkownika.")
            val uid = user.uid

            // 1. Najpierw usuwamy dane użytkownika z Firestore, póki sesja jest aktywna i posiada uprawnienia
            val dbDeleted = dbService.deleteUser(uid)
            if (!dbDeleted) {
                Log.w(TAG, "Ostrzeżenie: Nie wszystkie dane Firestore zostały usunięte dla $uid")
            }

            // 2. Następnie usuwamy konto z Firebase Authentication
            user.delete().await()

            _currentUser.value = null
            dbService.clearCache()
            AuthResult(isSuccess = true)
        } catch (e: FirebaseAuthException) {
            Log.e(TAG, "Błąd usuwania konta: ${e.errorCode}")
            if (e.errorCode == "ERROR_REQUIRES_RECENT_LOGIN" || e.message?.contains("requires-recent-login", ignoreCase = true) == true) {
                AuthResult(isSuccess = false, errorMessage = "Operacja wymaga ponownego zalogowania ze względów bezpieczeństwa. Wyloguj się i zaloguj ponownie przed usunięciem konta.")
            } else {
                AuthResult(isSuccess = false, errorMessage = mapFirebaseError(e))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Błąd usuwania konta: ${e.message}")
            AuthResult(isSuccess = false, errorMessage = mapGenericError(e))
        }
    }

    override suspend fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Błąd signOut: ${e.message}")
        }
        _currentUser.value = null
        dbService.clearCache()
    }

    private fun mapFirebaseError(e: FirebaseAuthException): String {
        val msg = e.message.orEmpty()
        if (msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) || msg.contains("invalid-credential", ignoreCase = true)) {
            return "Nieprawidłowy e-mail lub hasło (konto nie istnieje)."
        }
        if (msg.contains("has not been used", ignoreCase = true) || msg.contains("identitytoolkit", ignoreCase = true)) {
            return "Włącz usługę: W konsoli Firebase przejdź do Authentication i kliknij 'Rozpocznij' oraz włącz 'Adres e-mail/hasło'."
        }
        if (msg.contains("blocked", ignoreCase = true) || msg.contains("API_KEY_SERVICE_BLOCKED", ignoreCase = true)) {
            return "Klucz API w Google Cloud blokuje Identity Toolkit. Odblokuj 'Identity Toolkit API' w Google Cloud Console dla klucza API projektu voltup-21c34."
        }
        return when (e.errorCode) {
            "ERROR_INVALID_EMAIL", "invalid-email" -> "Niepoprawny format adresu e-mail."
            "ERROR_WRONG_PASSWORD", "wrong-password" -> "Nieprawidłowe hasło."
            "ERROR_USER_NOT_FOUND", "user-not-found" -> "Konto z tym adresem e-mail nie istnieje."
            "ERROR_INVALID_CREDENTIAL", "invalid-credential", "INVALID_LOGIN_CREDENTIALS" -> "Nieprawidłowy e-mail lub hasło (konto nie istnieje)."
            "ERROR_USER_DISABLED", "user-disabled" -> "To konto zostało zablokowane."
            "ERROR_TOO_MANY_REQUESTS", "too-many-requests" -> "Zbyt wiele prób. Spróbuj ponownie za chwilę."
            "ERROR_EMAIL_ALREADY_IN_USE", "email-already-in-use" -> "Konto z tym adresem e-mail już istnieje. Zaloguj się."
            "ERROR_WEAK_PASSWORD", "weak-password" -> "Hasło jest za słabe. Wymagane min. 6 znaków."
            "ERROR_OPERATION_NOT_ALLOWED", "operation-not-allowed" -> "Logowanie e-mail/hasło nie jest włączone w Firebase Console."
            else -> {
                if (e.errorCode.contains("INVALID_CREDENTIAL", ignoreCase = true)) {
                    "Nieprawidłowy e-mail lub hasło (konto nie istnieje)."
                } else {
                    e.localizedMessage ?: "Wystąpił błąd autoryzacji (${e.errorCode})."
                }
            }
        }
    }

    private fun mapGenericError(e: Exception): String {
        val msg = e.message.orEmpty()
        return when {
            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) || msg.contains("invalid-credential", ignoreCase = true) ->
                "Nieprawidłowy e-mail lub hasło (konto nie istnieje)."
            msg.contains("USER_NOT_FOUND", ignoreCase = true) || msg.contains("user-not-found", ignoreCase = true) ->
                "Konto z tym adresem e-mail nie istnieje."
            msg.contains("EMAIL_ALREADY_IN_USE", ignoreCase = true) || msg.contains("email-already-in-use", ignoreCase = true) ->
                "Konto z tym adresem e-mail już istnieje. Zaloguj się."
            msg.contains("has not been used", ignoreCase = true) || msg.contains("identitytoolkit", ignoreCase = true) ->
                "Włącz usługę: W konsoli Firebase przejdź do Authentication i kliknij 'Rozpocznij' oraz włącz 'Adres e-mail/hasło'."
            msg.contains("API_KEY_SERVICE_BLOCKED", ignoreCase = true) || msg.contains("blocked", ignoreCase = true) ->
                "Klucz API w Google Cloud blokuje Identity Toolkit. Odblokuj klucz w Google Cloud Console."
            msg.contains("PERMISSION_DENIED", ignoreCase = true) ->
                "Brak uprawnień bazy danych. Sprawdź reguły Firestore w konsoli Firebase."
            msg.contains("network", ignoreCase = true) || msg.contains("timeout", ignoreCase = true) ->
                "Błąd połączenia z siecią. Sprawdź dostęp do internetu."
            else -> msg.ifBlank { "Wystąpił nieoczekiwany błąd. Spróbuj ponownie." }
        }
    }

    private fun getTodayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private suspend fun initializeDefaultAchievements(userId: String, db: VoltUpDatabaseService) {
        val defaults = DefaultAchievements.initialAchievements.map { ach ->
            UserAchievementDto(
                userId = userId,
                achievementId = ach.id,
                currentLevel = 0,
                currentProgress = 0.0,
                updatedAt = System.currentTimeMillis()
            )
        }
        for (ach in defaults) {
            db.saveAchievement(ach)
        }
    }

    private suspend fun initializeDefaultChallenges(userId: String, db: VoltUpDatabaseService) {
        val currentWeek = SimpleDateFormat("yyyy-'W'ww", Locale.getDefault()).format(Date())
        val defaultChallenges = MockData.sampleChallenges.map { ch ->
            UserChallengeDto(
                id = "${userId}_${ch.id}",
                userId = userId,
                challengeId = ch.id,
                weekCode = currentWeek,
                currentProgress = 0.0f,
                currentValue = 0.0,
                targetValue = ch.targetValue,
                isCompleted = false,
                isClaimed = false
            )
        }
        for (ch in defaultChallenges) {
            db.saveChallenge(ch)
        }
    }
}

class AndroidFirestoreService : VoltUpDatabaseService {
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val memoryUsers = mutableMapOf<String, VoltUpUser>()
    private val memorySettings = mutableMapOf<String, UserSettingsDto>()
    private val memoryWorkouts = mutableListOf<WorkoutSessionDto>()
    private val memoryAchievements = mutableMapOf<String, UserAchievementDto>()
    private val memoryChallenges = mutableMapOf<String, UserChallengeDto>()
    private val memoryCoupons = mutableListOf<UserCouponDto>()

    override suspend fun saveUser(user: VoltUpUser): Boolean {
        memoryUsers[user.userId] = user
        return try {
            firestore.collection("users")
                .document(user.userId)
                .set(user.toMap(), SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Zapis users w chmurze nie powiódł się (zapisano lokalnie): ${e.message}")
            true
        }
    }

    override suspend fun getUser(userId: String): VoltUpUser? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            val data = doc.data
            if (doc.exists() && data != null) {
                val user = VoltUpUser.fromMap(data)
                memoryUsers[userId] = user
                user
            } else {
                memoryUsers[userId]
            }
        } catch (e: Exception) {
            Log.w(TAG, "Pobieranie users z chmury nie powiodło się: ${e.message}")
            memoryUsers[userId]
        }
    }

    override suspend fun getUserByEmail(email: String): VoltUpUser? {
        val clean = email.trim()
        val local = memoryUsers.values.firstOrNull { it.email.equals(clean, ignoreCase = true) }
        if (local != null) return local

        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("email", clean)
                .limit(1)
                .get()
                .await()
            val doc = snapshot.documents.firstOrNull()
            val data = doc?.data
            if (doc != null && data != null) {
                val user = VoltUpUser.fromMap(data)
                memoryUsers[user.userId] = user
                user
            } else null
        } catch (e: Exception) {
            Log.w(TAG, "Pobieranie users po email: ${e.message}")
            null
        }
    }

    override suspend fun updateUserStats(
        userId: String,
        pointsDelta: Int,
        whDelta: Double,
        co2Delta: Double,
        newLevel: Int,
        rankingPointsDelta: Int
    ): Boolean {
        val current = memoryUsers[userId]
        if (current != null) {
            val updated = current.copy(
                voltPoints = current.voltPoints + pointsDelta,
                rankingPoints = current.rankingPoints + rankingPointsDelta,
                totalEnergyWh = current.totalEnergyWh + whDelta,
                co2SavedKg = current.co2SavedKg + co2Delta,
                userLevel = newLevel,
                lastActiveDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            memoryUsers[userId] = updated
        }

        return try {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val updateMap = mutableMapOf<String, Any>(
                "volt_points" to FieldValue.increment(pointsDelta.toLong()),
                "total_energy_wh" to FieldValue.increment(whDelta),
                "co2_saved_kg" to FieldValue.increment(co2Delta),
                "user_level" to newLevel,
                "last_active_date" to todayStr
            )
            if (rankingPointsDelta != 0) {
                updateMap["ranking_points"] = FieldValue.increment(rankingPointsDelta.toLong())
            }
            firestore.collection("users").document(userId).update(updateMap).await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Aktualizacja statystyk w chmurze nie powiodła się: ${e.message}")
            true
        }
    }

    override suspend fun saveWorkoutSession(session: WorkoutSessionDto): Boolean {
        val realSessionId = if (session.sessionId.isNotBlank()) session.sessionId else UUID.randomUUID().toString()
        val toSave = session.copy(sessionId = realSessionId)
        memoryWorkouts.add(0, toSave)

        return try {
            firestore.collection("users")
                .document(toSave.userId)
                .collection("workout_sessions")
                .document(realSessionId)
                .set(toSave.toMap())
                .await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Zapis sesji treningowej w chmurze nie powiódł się: ${e.message}")
            true
        }
    }

    override suspend fun getWorkoutSessions(userId: String): List<WorkoutSessionDto> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("workout_sessions")
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { WorkoutSessionDto.fromMap(it) }
            }
            if (list.isNotEmpty()) {
                list
            } else {
                memoryWorkouts.filter { it.userId == userId }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Pobieranie sesji z chmury nie powiodło się: ${e.message}")
            memoryWorkouts.filter { it.userId == userId }
        }
    }

    override suspend fun saveAchievement(achievement: UserAchievementDto): Boolean {
        val key = "${achievement.userId}_${achievement.achievementId}"
        memoryAchievements[key] = achievement
        return try {
            firestore.collection("users")
                .document(achievement.userId)
                .collection("user_achievements")
                .document(achievement.achievementId)
                .set(achievement.toMap())
                .await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Zapis osiągnięcia w chmurze: ${e.message}")
            true
        }
    }

    override suspend fun getUserAchievements(userId: String): List<UserAchievementDto> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("user_achievements")
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { UserAchievementDto.fromMap(it, doc.id) }
            }
            if (list.isNotEmpty()) {
                list.forEach { memoryAchievements["${it.userId}_${it.achievementId}"] = it }
                list
            } else {
                memoryAchievements.values.filter { it.userId == userId }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Pobieranie osiągnięć: ${e.message}")
            memoryAchievements.values.filter { it.userId == userId }
        }
    }

    override suspend fun saveChallenge(challenge: UserChallengeDto): Boolean {
        val cleanChallengeId = challenge.challengeId.removePrefix("${challenge.userId}_")
        val id = if (challenge.id.isNotBlank()) challenge.id else cleanChallengeId
        val toSave = challenge.copy(id = id, challengeId = cleanChallengeId)
        val key = "${challenge.userId}_$cleanChallengeId"
        memoryChallenges[key] = toSave

        return try {
            firestore.collection("users")
                .document(challenge.userId)
                .collection("user_challenges")
                .document(cleanChallengeId)
                .set(toSave.toMap())
                .await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Zapis wyzwania w chmurze: ${e.message}")
            true
        }
    }

    override suspend fun getUserChallenges(userId: String): List<UserChallengeDto> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("user_challenges")
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { UserChallengeDto.fromMap(it) }
            }
            if (list.isNotEmpty()) {
                list.forEach { 
                    val cleanId = it.challengeId.removePrefix("${it.userId}_")
                    memoryChallenges["${it.userId}_$cleanId"] = it 
                }
                list
            } else {
                memoryChallenges.values.filter { it.userId == userId }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Pobieranie wyzwań: ${e.message}")
            memoryChallenges.values.filter { it.userId == userId }
        }
    }

    override suspend fun claimChallenge(userId: String, challengeId: String): Boolean {
        val cleanChallengeId = challengeId.removePrefix("${userId}_")
        val key = "${userId}_$cleanChallengeId"
        val existing = memoryChallenges[key]
        if (existing != null) {
            memoryChallenges[key] = existing.copy(isClaimed = true, isCompleted = true)
        }

        return try {
            firestore.collection("users")
                .document(userId)
                .collection("user_challenges")
                .document(cleanChallengeId)
                .update(
                    mapOf(
                        "is_claimed" to true,
                        "is_completed" to true
                    )
                ).await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Odbieranie wyzwania w chmurze: ${e.message}")
            true
        }
    }

    override suspend fun saveCoupon(coupon: UserCouponDto): Boolean {
        val realId = if (coupon.couponId.isNotBlank()) coupon.couponId else UUID.randomUUID().toString()
        val toSave = coupon.copy(couponId = realId)
        memoryCoupons.add(0, toSave)

        return try {
            firestore.collection("users")
                .document(coupon.userId)
                .collection("user_coupons")
                .document(realId)
                .set(toSave.toMap())
                .await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Zapis kuponu: ${e.message}")
            true
        }
    }

    override suspend fun getUserCoupons(userId: String): List<UserCouponDto> {
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000L
        val cutoff = currentTimeMillis() - thirtyDaysMs
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("user_coupons")
                .orderBy("redeemed_at", Query.Direction.DESCENDING)
                .get()
                .await()

            val validList = mutableListOf<UserCouponDto>()
            for (doc in snapshot.documents) {
                val dto = doc.data?.let { UserCouponDto.fromMap(it) } ?: continue
                if (dto.redeemedAt in 1..cutoff) {
                    try { doc.reference.delete() } catch (_: Exception) {}
                } else {
                    validList.add(dto)
                }
            }
            if (validList.isNotEmpty()) {
                validList
            } else {
                memoryCoupons.filter { it.userId == userId && (it.redeemedAt == 0L || it.redeemedAt >= cutoff) }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Pobieranie kuponów: ${e.message}")
            memoryCoupons.filter { it.userId == userId && (it.redeemedAt == 0L || it.redeemedAt >= cutoff) }
        }
    }

    override suspend fun updateCouponActivation(couponId: String, isActivated: Boolean, activatedAtMs: Long?): Boolean {
        val idx = memoryCoupons.indexOfFirst { it.couponId == couponId || it.discountCode == couponId }
        val userId = if (idx != -1) {
            memoryCoupons[idx] = memoryCoupons[idx].copy(isActivated = isActivated, activatedAt = activatedAtMs)
            memoryCoupons[idx].userId
        } else ""

        return try {
            if (userId.isNotBlank()) {
                val realCouponId = if (idx != -1) memoryCoupons[idx].couponId else couponId
                firestore.collection("users")
                    .document(userId)
                    .collection("user_coupons")
                    .document(realCouponId)
                    .update(
                        mapOf(
                            "is_activated" to isActivated,
                            "activated_at" to activatedAtMs
                        )
                    ).await()
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "Aktualizacja aktywacji kuponu: ${e.message}")
            true
        }
    }

    override suspend fun deleteCoupon(userId: String, couponId: String): Boolean {
        memoryCoupons.removeAll { it.couponId == couponId || (it.userId == userId && it.discountCode == couponId) }
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("user_coupons")
                .document(couponId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Usunięcie kuponu: ${e.message}")
            true
        }
    }

    override suspend fun saveUserSettings(settings: UserSettingsDto): Boolean {
        memorySettings[settings.userId] = settings
        return try {
            firestore.collection("users")
                .document(settings.userId)
                .set(
                    mapOf(
                        "theme_mode" to settings.themeMode,
                        "notifications_enabled" to settings.notificationsEnabled,
                        "sound_effects_enabled" to settings.soundEffectsEnabled,
                        "haptic_feedback_enabled" to settings.hapticFeedbackEnabled
                    ),
                    SetOptions.merge()
                )
                .await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Zapis ustawień w dokumencie użytkownika: ${e.message}")
            true
        }
    }

    override suspend fun getUserSettings(userId: String): UserSettingsDto? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            val data = doc.data
            if (doc.exists() && data != null) {
                val s = UserSettingsDto(
                    userId = userId,
                    themeMode = (data["theme_mode"] as? String) ?: "SYSTEM",
                    notificationsEnabled = (data["notifications_enabled"] as? Boolean) ?: true,
                    soundEffectsEnabled = (data["sound_effects_enabled"] as? Boolean) ?: true,
                    hapticFeedbackEnabled = (data["haptic_feedback_enabled"] as? Boolean) ?: true
                )
                memorySettings[userId] = s
                s
            } else {
                memorySettings[userId]
            }
        } catch (e: Exception) {
            Log.w(TAG, "Pobieranie ustawień z users/$userId: ${e.message}")
            memorySettings[userId]
        }
    }

    override suspend fun getLeaderboard(limit: Int): List<LeaderboardUser> {
        val currentUid = try { FirebaseAuth.getInstance().currentUser?.uid } catch (_: Exception) { null }

        return try {
            val snapshot = firestore.collection("users").get().await()
            val firestoreUsers = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { VoltUpUser.fromMap(it) }
            }

            val allUsersMap = (memoryUsers.values + firestoreUsers).associateBy { it.userId }
            val realLeaderboardUsers = allUsersMap.values.map { u ->
                val userPts = u.rankingPoints
                LeaderboardUser(
                    id = u.userId,
                    name = u.userName.ifBlank { u.email.substringBefore("@") },
                    points = userPts,
                    level = u.userLevel,
                    isCurrentUser = (u.userId == currentUid)
                )
            }.sortedByDescending { it.points }

            realLeaderboardUsers.take(limit)
        } catch (e: Exception) {
            Log.w(TAG, "Błąd pobierania rankingu: ${e.message}")
            val localUsers = memoryUsers.values.map { u ->
                val userPts = u.rankingPoints
                LeaderboardUser(
                    id = u.userId,
                    name = u.userName.ifBlank { u.email.substringBefore("@") },
                    points = userPts,
                    level = u.userLevel,
                    isCurrentUser = (u.userId == currentUid)
                )
            }.sortedByDescending { it.points }
            localUsers.take(limit)
        }
    }

    override suspend fun deleteUser(userId: String): Boolean {
        memoryUsers.remove(userId)
        memorySettings.remove(userId)
        memoryWorkouts.removeAll { it.userId == userId }
        memoryAchievements.keys.filter { it.startsWith("${userId}_") }.forEach { memoryAchievements.remove(it) }
        memoryChallenges.keys.filter { it.startsWith("${userId}_") }.forEach { memoryChallenges.remove(it) }
        memoryCoupons.removeAll { it.userId == userId }

        return try {
            val userDoc = firestore.collection("users").document(userId)
            val subcollections = listOf("workout_sessions", "user_achievements", "user_challenges", "user_coupons")
            for (sub in subcollections) {
                try {
                    val snap = userDoc.collection(sub).get().await()
                    for (d in snap.documents) {
                        d.reference.delete().await()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Błąd usuwania podkolekcji $sub dla $userId: ${e.message}")
                }
            }

            userDoc.delete().await()
            Log.d(TAG, "Pomyślnie usunięto dokument użytkownika $userId z Firestore.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Błąd usuwania użytkownika z chmury: ${e.message}", e)
            false
        }
    }

    private val defaultDevices = listOf(
        KineticStation(
            id = "PC-1",
            name = "Rower Kinetyczny",
            location = "Piotrkowska Centrum",
            district = "Śródmieście",
            type = DeviceType.BIKE,
            distance = "450 m",
            activeUsersCount = 3,
            qrCodeId = "LDZ-PC-1",
            mapCoordinatesX = 0.52f,
            mapCoordinatesY = 0.48f,
            latitude = 51.7594,
            longitude = 19.4568,
            isOnline = true,
            maxPowerW = 250
        ),
        KineticStation(
            id = "PP-1",
            name = "Rower Kinetyczny",
            location = "Park Poniatowskiego",
            district = "Polesie",
            type = DeviceType.BIKE,
            distance = "600 m",
            activeUsersCount = 2,
            qrCodeId = "LDZ-PP-1",
            mapCoordinatesX = 0.38f,
            mapCoordinatesY = 0.52f,
            latitude = 51.7538,
            longitude = 19.4445,
            isOnline = true,
            maxPowerW = 180
        ),
        KineticStation(
            id = "PZ-1",
            name = "Orbitrek Parkowy",
            location = "Park na Zdrowiu",
            district = "Polesie",
            type = DeviceType.CROSS_TRAINER,
            distance = "1.1 km",
            activeUsersCount = 4,
            qrCodeId = "LDZ-PZ-1",
            mapCoordinatesX = 0.16f,
            mapCoordinatesY = 0.38f,
            latitude = 51.7656,
            longitude = 19.4182,
            isOnline = true,
            maxPowerW = 220
        ),
        KineticStation(
            id = "MF-1",
            name = "Huśtawka Dynamiczna",
            location = "Manufaktura",
            district = "Bałuty",
            type = DeviceType.SWING,
            distance = "1.8 km",
            activeUsersCount = 2,
            qrCodeId = "LDZ-MF-1",
            mapCoordinatesX = 0.48f,
            mapCoordinatesY = 0.18f,
            latitude = 51.7797,
            longitude = 19.4496,
            isOnline = true,
            maxPowerW = 120
        ),
        KineticStation(
            id = "PZ-2",
            name = "Stepper Miejski",
            location = "Park Źródliska",
            district = "Widzew",
            type = DeviceType.STEPPER,
            distance = "2.3 km",
            activeUsersCount = 1,
            qrCodeId = "LDZ-PZ-2",
            mapCoordinatesX = 0.74f,
            mapCoordinatesY = 0.52f,
            latitude = 51.7575,
            longitude = 19.4754,
            isOnline = true,
            maxPowerW = 160
        ),
        KineticStation(
            id = "SS-1",
            name = "Orbitrek Nadwodny",
            location = "Stawy Stefańskiego",
            district = "Górna",
            type = DeviceType.CROSS_TRAINER,
            distance = "3.8 km",
            activeUsersCount = 2,
            qrCodeId = "LDZ-SS-1",
            mapCoordinatesX = 0.46f,
            mapCoordinatesY = 0.88f,
            latitude = 51.7042,
            longitude = 19.4492,
            isOnline = true,
            maxPowerW = 200
        ),
        KineticStation(
            id = "EC-1",
            name = "Rower Generator",
            location = "EC1 Łódź",
            district = "Śródmieście",
            type = DeviceType.BIKE,
            distance = "1.2 km",
            activeUsersCount = 3,
            qrCodeId = "LDZ-EC-1",
            mapCoordinatesX = 0.70f,
            mapCoordinatesY = 0.36f,
            latitude = 51.7672,
            longitude = 19.4718,
            isOnline = true,
            maxPowerW = 250
        ),
        KineticStation(
            id = "PM-1",
            name = "Stepper Parkowy",
            location = "Park Matejki",
            district = "Śródmieście",
            type = DeviceType.STEPPER,
            distance = "2.0 km",
            activeUsersCount = 1,
            qrCodeId = "LDZ-PM-1",
            mapCoordinatesX = 0.79f,
            mapCoordinatesY = 0.22f,
            latitude = 51.7765,
            longitude = 19.4880,
            isOnline = true,
            maxPowerW = 150
        )
    )

    private val memoryDevices = mutableMapOf<String, KineticStation>()

    private fun cleanStation(st: KineticStation): KineticStation {
        val cleanName = st.name.replace(Regex("""\s*#\d+"""), "").trim()
        val cleanLocation = st.location.replace(Regex("""\s*\(.*?\)"""), "").trim()
        return st.copy(name = cleanName, location = cleanLocation)
    }

    override suspend fun getDevices(): List<KineticStation> {
        return try {
            val snapshot = firestore.collection("devices").get().await()
            val existingList = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    KineticDeviceDto.fromMap(data, doc.id).toKineticStation()
                }
            }

            if (existingList.isNotEmpty()) {
                existingList.forEach { memoryDevices[it.id] = it }
                existingList
            } else {
                for (defaultDev in defaultDevices) {
                    try {
                        val dto = KineticDeviceDto.fromStation(defaultDev)
                        firestore.collection("devices").document(defaultDev.id).set(dto.toMap()).await()
                    } catch (e: Exception) {
                        Log.w(TAG, "Błąd inicjalizacji stacji ${defaultDev.id}: ${e.message}")
                    }
                }
                defaultDevices.forEach { memoryDevices[it.id] = it }
                defaultDevices
            }
        } catch (e: Exception) {
            Log.w(TAG, "Pobieranie urządzeń z Firestore: ${e.message}")
            val fallback = if (memoryDevices.isNotEmpty()) memoryDevices.values.toList() else defaultDevices
            fallback
        }
    }

    override suspend fun saveDevice(device: KineticStation): Boolean {
        memoryDevices[device.id] = device
        return try {
            val dto = KineticDeviceDto.fromStation(device)
            firestore.collection("devices").document(device.id).set(dto.toMap()).await()
            true
        } catch (e: Exception) {
            Log.w(TAG, "Zapis urządzenia w Firestore: ${e.message}")
            true
        }
    }

    override fun clearCache() {
        memoryUsers.clear()
        memorySettings.clear()
        memoryWorkouts.clear()
        memoryAchievements.clear()
        memoryChallenges.clear()
        memoryCoupons.clear()
    }
}

private val androidAuthService by lazy { AndroidFirebaseAuthService() }
private val androidDatabaseService by lazy { AndroidFirestoreService() }

actual fun getAuthService(): VoltUpAuthService = androidAuthService
actual fun getDatabaseService(): VoltUpDatabaseService = androidDatabaseService
