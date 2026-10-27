package com.lumatech.voltup
data class HealthUpUser(
    val userId: String = "",
    val email: String = "",
    val userName: String = "",
    val avatarId: Int = 1,
    val voltPoints: Int = 0,
    val rankingPoints: Int = 0,
    val totalEnergyWh: Double = 0.0,
    val co2SavedKg: Double = 0.0,
    val userLevel: Int = 0,
    val streakDays: Int = 1,
    val lastActiveDate: String = "",
    val createdAt: Long = 0L
) {
    val healthPoints: Int get() = voltPoints

    fun toMap(): Map<String, Any> = mapOf(
        "user_id" to userId,
        "email" to email,
        "user_name" to userName,
        "avatar_id" to avatarId,
        "volt_points" to voltPoints,
        "health_points" to voltPoints,
        "ranking_points" to rankingPoints,
        "total_energy_wh" to totalEnergyWh,
        "co2_saved_kg" to co2SavedKg,
        "user_level" to userLevel,
        "streak_days" to streakDays,
        "last_active_date" to lastActiveDate,
        "created_at" to createdAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): HealthUpUser {
            val voltPts = (map["health_points"] as? Number ?: map["volt_points"] as? Number ?: map["points"] as? Number)?.toInt() ?: 0
            val rankPts = (map["ranking_points"] as? Number)?.toInt() ?: 0
            return HealthUpUser(
                userId = (map["user_id"] as? String ?: map["id"] as? String).orEmpty(),
                email = (map["email"] as? String ?: map["mail"] as? String).orEmpty(),
                userName = (map["user_name"] as? String ?: map["name"] as? String ?: map["nazwa"] as? String).orEmpty().ifBlank { "Użytkownik HealthUp" },
                avatarId = (map["avatar_id"] as? Number ?: map["avatar"] as? Number)?.toInt() ?: 1,
                voltPoints = voltPts,
                rankingPoints = rankPts,
                totalEnergyWh = (map["total_energy_wh"] as? Number ?: map["energy_wh"] as? Number)?.toDouble() ?: 0.0,
                co2SavedKg = (map["co2_saved_kg"] as? Number ?: map["co2"] as? Number)?.toDouble() ?: 0.0,
                userLevel = (map["user_level"] as? Number ?: map["level"] as? Number)?.toInt() ?: 0,
                streakDays = (map["streak_days"] as? Number ?: map["streak"] as? Number)?.toInt() ?: 1,
                lastActiveDate = (map["last_active_date"] as? String).orEmpty(),
                createdAt = (map["created_at"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}

typealias VoltUpUser = HealthUpUser
data class WorkoutSessionDto(
    val sessionId: String = "",
    val userId: String = "",
    val stationId: String = "",
    val stationName: String = "",
    val stationLocation: String = "",
    val durationSeconds: Int = 0,
    val generatedWh: Double = 0.0,
    val earnedPoints: Int = 0,
    val co2SavedKg: Double = 0.0,
    val caloriesKcal: Int = 0,
    val createdAt: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "session_id" to sessionId,
        "user_id" to userId,
        "station_id" to stationId,
        "station_name" to stationName,
        "station_location" to stationLocation,
        "duration_seconds" to durationSeconds,
        "generated_wh" to generatedWh,
        "earned_points" to earnedPoints,
        "co2_saved_kg" to co2SavedKg,
        "calories_kcal" to caloriesKcal,
        "created_at" to createdAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): WorkoutSessionDto {
            return WorkoutSessionDto(
                sessionId = (map["session_id"] as? String).orEmpty(),
                userId = (map["user_id"] as? String).orEmpty(),
                stationId = (map["station_id"] as? String).orEmpty(),
                stationName = (map["station_name"] as? String).orEmpty(),
                stationLocation = (map["station_location"] as? String).orEmpty(),
                durationSeconds = (map["duration_seconds"] as? Number)?.toInt() ?: 0,
                generatedWh = (map["generated_wh"] as? Number)?.toDouble() ?: 0.0,
                earnedPoints = (map["earned_points"] as? Number)?.toInt() ?: 0,
                co2SavedKg = (map["co2_saved_kg"] as? Number)?.toDouble() ?: 0.0,
                caloriesKcal = (map["calories_kcal"] as? Number)?.toInt() ?: 0,
                createdAt = (map["created_at"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}
data class UserAchievementDto(
    val userId: String = "",
    val achievementId: String = "",
    val currentLevel: Int = 0,
    val currentProgress: Double = 0.0,
    val updatedAt: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "user_id" to userId,
        "achievement_id" to achievementId,
        "current_level" to currentLevel,
        "current_progress" to currentProgress,
        "updated_at" to updatedAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>, docId: String = ""): UserAchievementDto {
            val rawId = (map["achievement_id"] as? String).orEmpty()
            val achId = if (rawId.isNotBlank()) rawId else docId
            return UserAchievementDto(
                userId = (map["user_id"] as? String).orEmpty(),
                achievementId = achId,
                currentLevel = (map["current_level"] as? Number)?.toInt() ?: 0,
                currentProgress = (map["current_progress"] as? Number)?.toDouble() ?: 0.0,
                updatedAt = (map["updated_at"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}
data class UserChallengeDto(
    val id: String = "",
    val userId: String = "",
    val challengeId: String = "",
    val weekCode: String = "",
    val currentProgress: Float = 0.0f,
    val currentValue: Double = 0.0,
    val targetValue: Double = 100.0,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "user_id" to userId,
        "challenge_id" to challengeId,
        "week_code" to weekCode,
        "current_progress" to currentProgress,
        "current_value" to currentValue,
        "target_value" to targetValue,
        "is_completed" to isCompleted,
        "is_claimed" to isClaimed
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): UserChallengeDto {
            return UserChallengeDto(
                id = (map["id"] as? String).orEmpty(),
                userId = (map["user_id"] as? String).orEmpty(),
                challengeId = (map["challenge_id"] as? String).orEmpty(),
                weekCode = (map["week_code"] as? String).orEmpty(),
                currentProgress = (map["current_progress"] as? Number)?.toFloat() ?: 0f,
                currentValue = (map["current_value"] as? Number)?.toDouble() ?: 0.0,
                targetValue = (map["target_value"] as? Number)?.toDouble() ?: 100.0,
                isCompleted = (map["is_completed"] as? Boolean) ?: false,
                isClaimed = (map["is_claimed"] as? Boolean) ?: false
            )
        }
    }
}
data class UserCouponDto(
    val couponId: String = "",
    val userId: String = "",
    val rewardId: String = "",
    val discountCode: String = "",
    val costPoints: Int = 0,
    val validityMinutes: Int = 0,
    val isReadyToScan: Boolean = false,
    val isActivated: Boolean = false,
    val activatedAt: Long? = null,
    val redeemedAt: Long = 0L
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "coupon_id" to couponId,
        "user_id" to userId,
        "reward_id" to rewardId,
        "discount_code" to discountCode,
        "cost_points" to costPoints,
        "validity_minutes" to validityMinutes,
        "is_ready_to_scan" to isReadyToScan,
        "is_activated" to isActivated,
        "activated_at" to activatedAt,
        "redeemed_at" to redeemedAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): UserCouponDto {
            return UserCouponDto(
                couponId = (map["coupon_id"] as? String).orEmpty(),
                userId = (map["user_id"] as? String).orEmpty(),
                rewardId = (map["reward_id"] as? String).orEmpty(),
                discountCode = (map["discount_code"] as? String).orEmpty(),
                costPoints = (map["cost_points"] as? Number)?.toInt() ?: 0,
                validityMinutes = (map["validity_minutes"] as? Number)?.toInt() ?: 0,
                isReadyToScan = (map["is_ready_to_scan"] as? Boolean) ?: false,
                isActivated = (map["is_activated"] as? Boolean) ?: false,
                activatedAt = (map["activated_at"] as? Number)?.toLong(),
                redeemedAt = (map["redeemed_at"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}
data class UserSettingsDto(
    val userId: String = "",
    val themeMode: String = "SYSTEM",
    val notificationsEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true
) {
    fun toMap(): Map<String, Any> = mapOf(
        "user_id" to userId,
        "theme_mode" to themeMode,
        "notifications_enabled" to notificationsEnabled,
        "sound_effects_enabled" to soundEffectsEnabled,
        "haptic_feedback_enabled" to hapticFeedbackEnabled
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): UserSettingsDto {
            return UserSettingsDto(
                userId = (map["user_id"] as? String).orEmpty(),
                themeMode = (map["theme_mode"] as? String) ?: "SYSTEM",
                notificationsEnabled = (map["notifications_enabled"] as? Boolean) ?: true,
                soundEffectsEnabled = (map["sound_effects_enabled"] as? Boolean) ?: true,
                hapticFeedbackEnabled = (map["haptic_feedback_enabled"] as? Boolean) ?: true
            )
        }
    }
}
data class KineticDeviceDto(
    val deviceId: String = "",
    val name: String = "",
    val locationName: String = "",
    val district: String = "",
    val status: String = "online", // "online" lub "offline"
    val maxPowerW: Int = 150,
    val latitude: Double = 51.7592,
    val longitude: Double = 19.4560,
    val deviceType: String = "BIKE",
    val qrCodeId: String = "",
    val currentEnergyRateW: Int = 0,
    val activeUsersCount: Int = 0,
    val mapCoordinatesX: Float = 0.5f,
    val mapCoordinatesY: Float = 0.5f,
) {
    fun toMap(): Map<String, Any> = mapOf(
        "device_id" to deviceId,
        "name" to name,
        "location_name" to locationName,
        "district" to district,
        "status" to status,
        "max_power_w" to maxPowerW,
        "latitude" to latitude,
        "longitude" to longitude,
        "device_type" to deviceType,
        "qr_code_id" to qrCodeId,
        "current_energy_rate_w" to currentEnergyRateW,
        "active_users_count" to activeUsersCount,
        "map_coordinates_x" to mapCoordinatesX,
        "map_coordinates_y" to mapCoordinatesY
    )

    fun toKineticStation(): KineticStation {
        val parsedType = try {
            DeviceType.valueOf(deviceType)
        } catch (_: Exception) {
            DeviceType.BIKE
        }
        return KineticStation(
            id = deviceId,
            name = name,
            location = locationName,
            type = parsedType,
            currentEnergyRateW = currentEnergyRateW,
            distance = "500 m",
            activeUsersCount = activeUsersCount,
            qrCodeId = qrCodeId.ifEmpty { "LDZ-$deviceId" },
            isFavorite = false,
            district = district,
            latitude = latitude,
            longitude = longitude,
            mapCoordinatesX = mapCoordinatesX,
            mapCoordinatesY = mapCoordinatesY,
            isOnline = status.equals("online", ignoreCase = true) || status.equals("aktywna", ignoreCase = true),
            maxPowerW = maxPowerW
        )
    }

    companion object {
        fun fromStation(station: KineticStation): KineticDeviceDto = KineticDeviceDto(
            deviceId = station.id,
            name = station.name,
            locationName = station.location,
            district = station.district,
            status = if (station.isOnline) "online" else "offline",
            maxPowerW = station.maxPowerW,
            latitude = station.latitude,
            longitude = station.longitude,
            deviceType = station.type.name,
            qrCodeId = station.qrCodeId,
            currentEnergyRateW = station.currentEnergyRateW,
            activeUsersCount = station.activeUsersCount,
            mapCoordinatesX = station.mapCoordinatesX,
            mapCoordinatesY = station.mapCoordinatesY
        )

        fun fromMap(map: Map<String, Any?>, fallbackId: String = ""): KineticDeviceDto {
            val devId = (map["device_id"] as? String
                ?: map["deviceId"] as? String
                ?: map["id"] as? String
                ?: map["station_id"] as? String
                ?: map["stationId"] as? String
                ?: fallbackId).orEmpty().ifBlank { fallbackId }

            val qrId = (map["qr_code_id"] as? String
                ?: map["qrCodeId"] as? String
                ?: map["qr_code"] as? String
                ?: map["qrCode"] as? String
                ?: "").orEmpty().ifBlank { "LDZ-$devId" }

            val rawName = (map["name"] as? String
                ?: map["nazwa"] as? String
                ?: map["atrakcja"] as? String
                ?: map["attraction"] as? String
                ?: map["title"] as? String
                ?: map["tytul"] as? String
                ?: map["device"] as? String
                ?: map["urzadzenie"] as? String
                ?: "").trim()

            val rawLocation = (map["location_name"] as? String
                ?: map["locationName"] as? String
                ?: map["location"] as? String
                ?: map["lokalizacja"] as? String
                ?: map["miejsce"] as? String
                ?: map["place"] as? String
                ?: map["rynek"] as? String
                ?: map["address"] as? String
                ?: map["adres"] as? String
                ?: "").trim()

            val rawType = (map["device_type"] as? String
                ?: map["deviceType"] as? String
                ?: map["type"] as? String
                ?: map["typ"] as? String
                ?: map["kategoria"] as? String
                ?: "").trim().uppercase()

            val resolvedType = when {
                rawType.contains("SWING") || rawType.contains("HUST") || rawType.contains("HUŚT") -> "SWING"
                rawType.contains("CROSS") || rawType.contains("ORBI") -> "CROSS_TRAINER"
                rawType.contains("STEP") -> "STEPPER"
                rawType.contains("SOLAR") -> "SOLAR_BENCH"
                rawType.contains("BENCH") || rawType.contains("LAWK") || rawType.contains("ŁAWK") -> "BENCH"
                rawType.contains("TREAD") || rawType.contains("BIEG") || rawType.contains("BIEŻ") -> "TREADMILL"
                rawType.contains("ROWER") && (rawType.contains("WIOS") || rawType.contains("WIOŚ")) -> "ROWER"
                rawType.contains("BIKE") || rawType.contains("ROWER") -> "BIKE"
                rawName.contains("huśt", ignoreCase = true) || rawName.contains("hust", ignoreCase = true) -> "SWING"
                rawName.contains("orbit", ignoreCase = true) -> "CROSS_TRAINER"
                rawName.contains("step", ignoreCase = true) -> "STEPPER"
                rawName.contains("ławk", ignoreCase = true) || rawName.contains("lawk", ignoreCase = true) -> "BENCH"
                rawName.contains("bież", ignoreCase = true) || rawName.contains("biez", ignoreCase = true) -> "TREADMILL"
                rawName.contains("wiośl", ignoreCase = true) || rawName.contains("wiosl", ignoreCase = true) -> "ROWER"
                else -> if (rawType.isNotBlank()) rawType else "BIKE"
            }

            val finalName = rawName.ifBlank {
                when (resolvedType) {
                    "SWING" -> "Huśtawka Dynamiczna"
                    "CROSS_TRAINER" -> "Orbitrek Kinetyczny"
                    "STEPPER" -> "Stepper Miejski"
                    "SOLAR_BENCH" -> "Ławka Solarna"
                    "BENCH" -> "Ławka Kinetyczna"
                    "TREADMILL" -> "Bieżnia Kinetyczna"
                    "ROWER" -> "Wioślarz Miejski"
                    else -> "Rower Kinetyczny"
                }
            }

            fun parseCoord(v: Any?): Double? {
                return when (v) {
                    is Number -> v.toDouble()
                    is String -> v.replace(',', '.').trim().toDoubleOrNull()
                    else -> null
                }
            }

            var parsedLat: Double? = parseCoord(map["latitude"])
                ?: parseCoord(map["lat"])
                ?: parseCoord(map["szerokosc"])
                ?: parseCoord(map["szerokosc_geograficzna"])
                ?: parseCoord(map["geo_lat"])
                ?: parseCoord(map["geolat"])

            var parsedLng: Double? = parseCoord(map["longitude"])
                ?: parseCoord(map["lng"])
                ?: parseCoord(map["lon"])
                ?: parseCoord(map["long"])
                ?: parseCoord(map["dlugosc"])
                ?: parseCoord(map["dlugosc_geograficzna"])
                ?: parseCoord(map["geo_lng"])
                ?: parseCoord(map["geolng"])

            val geoObj = map["coordinates"] ?: map["position"] ?: map["geopoint"] ?: map["geo"] ?: map["geo_point"] ?: map["coords"]
            if (geoObj is Map<*, *>) {
                if (parsedLat == null) parsedLat = parseCoord(geoObj["latitude"] ?: geoObj["lat"] ?: geoObj["szerokosc"])
                if (parsedLng == null) parsedLng = parseCoord(geoObj["longitude"] ?: geoObj["lng"] ?: geoObj["lon"] ?: geoObj["dlugosc"])
            }

            val combinedSearch = "$rawLocation $finalName $devId".lowercase()
            val knownLocationCoords = when {
                combinedSearch.contains("manufaktur") || combinedSearch.contains("rynek") || devId.equals("MF-1", ignoreCase = true) -> Pair(51.7797, 19.4496)
                combinedSearch.contains("poniatowsk") || devId.equals("PP-1", ignoreCase = true) -> Pair(51.7538, 19.4445)
                combinedSearch.contains("zdrowi") || devId.equals("PZ-1", ignoreCase = true) -> Pair(51.7656, 19.4182)
                combinedSearch.contains("źródlisk") || combinedSearch.contains("zrodlisk") || devId.equals("PZ-2", ignoreCase = true) -> Pair(51.7575, 19.4754)
                combinedSearch.contains("piotrkowsk") || devId.equals("PC-1", ignoreCase = true) -> Pair(51.7594, 19.4568)
                combinedSearch.contains("stefańsk") || combinedSearch.contains("stefansk") || devId.equals("SS-1", ignoreCase = true) -> Pair(51.7042, 19.4492)
                combinedSearch.contains("ec1") || devId.equals("EC-1", ignoreCase = true) -> Pair(51.7672, 19.4718)
                combinedSearch.contains("matejk") || devId.equals("PM-1", ignoreCase = true) -> Pair(51.7765, 19.4880)
                combinedSearch.contains("monopolis") -> Pair(51.7617, 19.4883)
                combinedSearch.contains("fuzja") -> Pair(51.7483, 19.4627)
                combinedSearch.contains("off") -> Pair(51.7610, 19.4585)
                else -> null
            }

            val finalLat: Double
            val finalLng: Double
            val pLat = parsedLat
            val pLng = parsedLng
            if (knownLocationCoords != null) {
                val isDefaultCoord = (pLat == null || pLng == null) ||
                        (kotlin.math.abs(pLat - 51.7592) < 0.0001 && kotlin.math.abs(pLng - 19.4560) < 0.0001)
                val isManufakturaMismatched = pLat != null && (combinedSearch.contains("manufaktur") || combinedSearch.contains("rynek")) &&
                        kotlin.math.abs(pLat - 51.7538) < 0.005
                val isPoniatowskiMismatched = pLat != null && combinedSearch.contains("poniatowsk") &&
                        kotlin.math.abs(pLat - 51.7797) < 0.005

                if (isDefaultCoord || isManufakturaMismatched || isPoniatowskiMismatched) {
                    finalLat = knownLocationCoords.first
                    finalLng = knownLocationCoords.second
                } else {
                    finalLat = pLat
                    finalLng = pLng
                }
            } else {
                finalLat = pLat ?: 51.7592
                finalLng = pLng ?: 19.4560
            }

            val finalLocation = rawLocation.ifBlank {
                when {
                    combinedSearch.contains("manufaktur") || combinedSearch.contains("rynek") -> "Rynek w Manufakturze"
                    combinedSearch.contains("poniatowsk") -> "Park Poniatowskiego"
                    combinedSearch.contains("zdrowi") -> "Park na Zdrowiu"
                    combinedSearch.contains("źródlisk") || combinedSearch.contains("zrodlisk") -> "Park Źródliska"
                    combinedSearch.contains("piotrkowsk") -> "Piotrkowska Centrum"
                    combinedSearch.contains("stefańsk") || combinedSearch.contains("stefansk") -> "Stawy Stefańskiego"
                    combinedSearch.contains("ec1") -> "EC1 Łódź"
                    combinedSearch.contains("matejk") -> "Park Matejki"
                    else -> "Łódź Centrum"
                }
            }

            val parsedDistrict = (map["district"] as? String ?: map["dzielnica"] as? String).orEmpty().trim().ifBlank {
                when {
                    combinedSearch.contains("manufaktur") || combinedSearch.contains("bałut") || combinedSearch.contains("balut") -> "Bałuty"
                    combinedSearch.contains("poniatowsk") || combinedSearch.contains("zdrowi") || combinedSearch.contains("poles") -> "Polesie"
                    combinedSearch.contains("źródlisk") || combinedSearch.contains("zrodlisk") || combinedSearch.contains("widzew") -> "Widzew"
                    combinedSearch.contains("piotrkowsk") || combinedSearch.contains("ec1") || combinedSearch.contains("matejk") || combinedSearch.contains("śródmieś") || combinedSearch.contains("srodmies") -> "Śródmieście"
                    combinedSearch.contains("stefańsk") || combinedSearch.contains("stefansk") || combinedSearch.contains("górn") || combinedSearch.contains("gorn") -> "Górna"
                    else -> "Polesie"
                }
            }

            val normX = ((finalLng - 19.38) / (19.54 - 19.38)).toFloat()
            val normY = ((51.82 - finalLat) / (51.82 - 51.70)).toFloat()
            val computedMapX = (0.10f + normX.coerceIn(0f, 1f) * 0.80f)
            val computedMapY = (0.10f + normY.coerceIn(0f, 1f) * 0.80f)

            val explicitMapX = parseCoord(map["map_coordinates_x"] ?: map["mapCoordinatesX"] ?: map["x"])?.toFloat()
            val explicitMapY = parseCoord(map["map_coordinates_y"] ?: map["mapCoordinatesY"] ?: map["y"])?.toFloat()

            val finalMapX = if (explicitMapX != null && explicitMapX != 0.5f) explicitMapX else computedMapX
            val finalMapY = if (explicitMapY != null && explicitMapY != 0.5f) explicitMapY else computedMapY

            val maxPower = (map["max_power_w"] as? Number
                ?: map["maxPowerW"] as? Number
                ?: map["max_power"] as? Number
                ?: map["maxPower"] as? Number
                ?: map["moc_max"] as? Number
                ?: map["moc"] as? Number)?.toInt() ?: 150

            val statusVal = (map["status"] as? String ?: map["stan"] as? String) ?: "online"

            return KineticDeviceDto(
                deviceId = devId,
                name = finalName,
                locationName = finalLocation,
                district = parsedDistrict,
                status = statusVal,
                maxPowerW = maxPower,
                latitude = finalLat,
                longitude = finalLng,
                deviceType = resolvedType,
                qrCodeId = qrId,
                currentEnergyRateW = (map["current_energy_rate_w"] as? Number ?: map["currentEnergyRateW"] as? Number)?.toInt() ?: 0,
                activeUsersCount = (map["active_users_count"] as? Number ?: map["activeUsersCount"] as? Number)?.toInt() ?: 0,
                mapCoordinatesX = finalMapX,
                mapCoordinatesY = finalMapY
            )
        }
    }
}
