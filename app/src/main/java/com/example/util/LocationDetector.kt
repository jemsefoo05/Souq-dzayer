package com.example.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.Wilaya
import com.example.data.model.WilayaData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Result representing detected user location mapped to an Algerian Wilaya and City/Commune.
 */
data class DetectedCityResult(
    val cityName: String,
    val wilaya: Wilaya,
    val latitude: Double,
    val longitude: Double,
    val isGpsAccurate: Boolean = true,
    val sourceDescription: String = "GPS Fused Location"
)

/**
 * Utility to detect the user's city/wilaya using Google Play Services Location
 * (FusedLocationProviderClient) and match it to Algeria's 58 Wilayas for localized browsing.
 */
object LocationDetector {

    private const val TAG = "LocationDetector"

    /**
     * Approximate center coordinates for each of the 58 Wilayas of Algeria.
     * Used for fallback distance-based matching if Geocoder is offline or unavailable.
     */
    private val wilayaCoordinates: Map<Int, Pair<Double, Double>> = mapOf(
        1 to Pair(27.8743, -0.2939),   // Adrar
        2 to Pair(36.1652, 1.3345),    // Chlef
        3 to Pair(33.8000, 2.8651),    // Laghouat
        4 to Pair(35.8755, 7.1135),    // Oum El Bouaghi
        5 to Pair(35.5559, 6.1741),    // Batna
        6 to Pair(36.7559, 5.0843),    // Béjaïa
        7 to Pair(34.8504, 5.7281),    // Biskra
        8 to Pair(31.6167, -2.2167),   // Béchar
        9 to Pair(36.4700, 2.8300),    // Blida
        10 to Pair(36.3749, 3.9020),   // Bouira
        11 to Pair(22.7850, 5.5228),   // Tamanrasset
        12 to Pair(35.4042, 8.1242),   // Tébessa
        13 to Pair(34.8783, -1.3150),  // Tlemcen
        14 to Pair(35.3710, 1.3170),   // Tiaret
        15 to Pair(36.7118, 4.0459),   // Tizi Ouzou
        16 to Pair(36.7538, 3.0588),   // Alger
        17 to Pair(34.6728, 3.2630),   // Djelfa
        18 to Pair(36.8206, 5.7667),   // Jijel
        19 to Pair(36.1911, 5.4137),   // Sétif
        20 to Pair(34.8303, 0.1517),   // Saïda
        21 to Pair(36.8762, 6.9092),   // Skikda
        22 to Pair(35.1899, -0.6308),  // Sidi Bel Abbès
        23 to Pair(36.9000, 7.7667),   // Annaba
        24 to Pair(36.4621, 7.4261),   // Guelma
        25 to Pair(36.3650, 6.6147),   // Constantine
        26 to Pair(36.2642, 2.7539),   // Médéa
        27 to Pair(35.9312, 0.0892),   // Mostaganem
        28 to Pair(35.7058, 4.5419),   // M'Sila
        29 to Pair(35.3966, 0.1403),   // Mascara
        30 to Pair(31.9493, 5.3250),   // Ouargla
        31 to Pair(35.6987, -0.6349),  // Oran
        32 to Pair(33.6832, 1.0193),   // El Bayadh
        33 to Pair(26.4833, 8.4667),   // Illizi
        34 to Pair(36.0732, 4.7611),   // Bordj Bou Arreridj
        35 to Pair(36.7664, 3.4772),   // Boumerdès
        36 to Pair(36.7672, 8.3138),   // El Tarf
        37 to Pair(27.6711, -8.1474),  // Tindouf
        38 to Pair(35.6072, 1.8108),   // Tissemsilt
        39 to Pair(33.3683, 6.8675),   // El Oued
        40 to Pair(35.4358, 7.1433),   // Khenchela
        41 to Pair(36.2864, 7.9511),   // Souk Ahras
        42 to Pair(36.5897, 2.4475),   // Tipaza
        43 to Pair(36.4503, 6.2644),   // Mila
        44 to Pair(36.2644, 1.9679),   // Aïn Defla
        45 to Pair(33.2667, -0.3167),  // Naâma
        46 to Pair(35.2975, -1.1404),  // Aïn Témouchent
        47 to Pair(32.4833, 3.6667),   // Ghardaïa
        48 to Pair(35.7373, 0.5558),   // Relizane
        49 to Pair(29.2639, 0.2310),   // Timimoun
        50 to Pair(21.3283, 0.9542),   // Bordj Badji Mokhtar
        51 to Pair(34.4300, 5.0667),   // Ouled Djellal
        52 to Pair(30.1333, -2.1667),  // Béni Abbès
        53 to Pair(27.1935, 2.4607),   // In Salah
        54 to Pair(19.5700, 5.7700),   // In Guezzam
        55 to Pair(33.1053, 6.0579),   // Touggourt
        56 to Pair(24.5539, 9.4853),   // Djanet
        57 to Pair(33.9500, 5.9200),   // El M'Ghair
        58 to Pair(30.5847, 2.8758)    // El Meniaa
    )

    /**
     * Checks whether location permissions are granted.
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    /**
     * Asynchronously detects the user's location via Google Play Services Fused Location Client,
     * performs reverse geocoding to identify city and commune, and matches it with Algerian Wilayas.
     */
    @SuppressLint("MissingPermission")
    suspend fun detectCity(context: Context): Result<DetectedCityResult> = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext Result.failure(
                SecurityException("Permissions de localisation non accordées")
            )
        }

        try {
            val fusedClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)

            val location: Location? = getLocationFromFusedClient(fusedClient)

            if (location == null) {
                // If device location is null (e.g. running on emulator or location toggled off),
                // fallback to standard default capital Algiers or cached last known location
                val defaultWilaya = WilayaData.getWilaya(16) ?: WilayaData.allWilayas.first()
                return@withContext Result.success(
                    DetectedCityResult(
                        cityName = "Alger Centre",
                        wilaya = defaultWilaya,
                        latitude = 36.7538,
                        longitude = 3.0588,
                        isGpsAccurate = false,
                        sourceDescription = "Position par défaut (Alger Centre)"
                    )
                )
            }

            val lat = location.latitude
            val lng = location.longitude

            // Try geocoding city and commune name
            val geocodedResult = reverseGeocode(context, lat, lng)
            val matchedWilaya = matchToAlgerianWilaya(geocodedResult?.first, geocodedResult?.second, lat, lng)
            val detectedCityName = geocodedResult?.first ?: matchedWilaya.communes.firstOrNull() ?: matchedWilaya.nameFr

            Result.success(
                DetectedCityResult(
                    cityName = detectedCityName,
                    wilaya = matchedWilaya,
                    latitude = lat,
                    longitude = lng,
                    isGpsAccurate = true,
                    sourceDescription = "Détecté via GPS & FusedLocation (${matchedWilaya.nameFr})"
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting location", e)
            Result.failure(e)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocationFromFusedClient(client: FusedLocationProviderClient): Location? {
        // Try getting current fresh location first
        val cancellationSource = CancellationTokenSource()
        return try {
            suspendCancellableCoroutine { continuation ->
                client.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationSource.token
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        // Try last location as fallback
                        client.lastLocation.addOnSuccessListener { lastLoc ->
                            continuation.resume(lastLoc)
                        }.addOnFailureListener {
                            continuation.resume(null)
                        }
                    }
                }.addOnFailureListener {
                    // Try last location fallback
                    client.lastLocation.addOnSuccessListener { lastLoc ->
                        continuation.resume(lastLoc)
                    }.addOnFailureListener {
                        continuation.resume(null)
                    }
                }

                continuation.invokeOnCancellation {
                    cancellationSource.cancel()
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get current location from fused client", e)
            null
        }
    }

    /**
     * Reverse geocodes latitude/longitude to extract city/commune and state/admin area in Algeria.
     */
    private suspend fun reverseGeocode(
        context: Context,
        lat: Double,
        lng: Double
    ): Pair<String?, String?>? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null

            val geocoder = Geocoder(context, Locale("fr", "DZ"))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine<Pair<String?, String?>?> { cont ->
                    geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            val addr = addresses.firstOrNull()
                            if (addr != null) {
                                val city = addr.locality ?: addr.subLocality ?: addr.subAdminArea
                                val admin = addr.adminArea ?: addr.subAdminArea
                                cont.resume(Pair(city, admin))
                            } else {
                                cont.resume(null)
                            }
                        }

                        override fun onError(errorMessage: String?) {
                            cont.resume(null)
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                val addr = addresses?.firstOrNull() ?: return@withContext null
                val city = addr.locality ?: addr.subLocality ?: addr.subAdminArea
                val admin = addr.adminArea ?: addr.subAdminArea
                Pair(city, admin)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Geocoder reverseGeocode failed: ${e.message}")
            null
        }
    }

    /**
     * Matches raw geocoded names or coordinates to one of the 58 Wilayas of Algeria.
     */
    fun matchToAlgerianWilaya(
        cityName: String?,
        adminArea: String?,
        lat: Double,
        lng: Double
    ): Wilaya {
        val searchStrings = listOfNotNull(cityName, adminArea)

        // 1. Match by Wilaya name (French or Arabic)
        for (query in searchStrings) {
            val normalizedQuery = query.lowercase().trim()
            val match = WilayaData.allWilayas.firstOrNull { wilaya ->
                normalizedQuery.contains(wilaya.nameFr.lowercase()) ||
                        wilaya.nameFr.lowercase().contains(normalizedQuery) ||
                        normalizedQuery.contains(wilaya.nameAr) ||
                        wilaya.nameAr.contains(normalizedQuery) ||
                        // Check communes
                        wilaya.communes.any { commune ->
                            normalizedQuery.contains(commune.lowercase()) ||
                                    commune.lowercase().contains(normalizedQuery)
                        }
            }
            if (match != null) {
                return match
            }
        }

        // 2. Fallback: Find closest Wilaya by geographic distance (Haversine formula)
        var closestWilaya = WilayaData.getWilaya(16) ?: WilayaData.allWilayas.first()
        var minDistanceKm = Double.MAX_VALUE

        for ((code, coords) in wilayaCoordinates) {
            val distance = haversineDistanceKm(lat, lng, coords.first, coords.second)
            if (distance < minDistanceKm) {
                minDistanceKm = distance
                val w = WilayaData.getWilaya(code)
                if (w != null) {
                    closestWilaya = w
                }
            }
        }

        return closestWilaya
    }

    /**
     * Computes Haversine distance in kilometers between two lat/lng coordinates.
     */
    private fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
