package com.easytoday.guidegroup.data.location

import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.easytoday.guidegroup.domain.repository.LocationClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultLocationClientTest : BehaviorSpec({

    beforeSpec {
        if (Timber.treeCount == 0) {
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {}
            })
        }
    }

    val mockContext = mockk<Context>(relaxed = true)
    val mockFusedLocationProviderClient = mockk<FusedLocationProviderClient>()
    val mockLocationManager = mockk<LocationManager>()

    val defaultLocationClient = DefaultLocationClient(mockContext, mockFusedLocationProviderClient)

    beforeEach {
        clearAllMocks()
        every { mockContext.packageManager } returns mockk()
        every { mockContext.getSystemService(Context.LOCATION_SERVICE) } returns mockLocationManager
        every { mockFusedLocationProviderClient.requestLocationUpdates(any<LocationRequest>(), any<LocationCallback>(), any()) } returns mockk()
        // CORRECTION : Utiliser `just Awaits` pour une fonction suspendue
        coEvery { mockFusedLocationProviderClient.removeLocationUpdates(any<LocationCallback>()) } just Awaits
    }

    Given("DefaultLocationClient") {

        When("getLocationUpdates est appelé sans permissions de localisation") {
            every { mockContext.hasLocationPermission() } returns false

            Then("il devrait lancer une LocationException") {
                runTest {
                    val exception = shouldThrow<LocationClient.LocationException> {
                        defaultLocationClient.getLocationUpdates(1000L).first()
                    }
                    exception.message shouldBe "Missing location permission."
                }
            }
        }

        When("getLocationUpdates est appelé sans GPS ou réseau activé") {
            every { mockContext.hasLocationPermission() } returns true
            every { mockLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } returns false
            every { mockLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } returns false

            Then("il devrait lancer une LocationException") {
                runTest {
                    val exception = shouldThrow<LocationClient.LocationException> {
                        defaultLocationClient.getLocationUpdates(1000L).first()
                    }
                    exception.message shouldBe "GPS or network is not enabled."
                }
            }
        }
    }
})