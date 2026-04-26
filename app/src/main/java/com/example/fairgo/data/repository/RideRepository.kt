package com.example.fairgo.data.repository

import com.example.fairgo.data.network.FairGoApi
import com.example.fairgo.data.network.models.CreateRideRequest
import com.example.fairgo.data.network.models.RideResponse
import com.yandex.mapkit.geometry.Point
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject
import retrofit2.HttpException

@Singleton
class RideRepository @Inject constructor(
    private val api: FairGoApi,
) {
    suspend fun createRide(start: Point, finish: Point): Result<RideResponse> {
        return try {
            val request = CreateRideRequest(
                startLat = start.latitude,
                startLon = start.longitude,
                finishLat = finish.latitude,
                finishLon = finish.longitude,
            )
            Result.success(api.createRide(request))
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseHttpError(exception: HttpException): String {
        val fallbackMessage = "Ошибка запроса: ${exception.code()}"
        val errorBody = exception.response()?.errorBody()?.string().orEmpty()
        if (errorBody.isBlank()) return fallbackMessage

        return try {
            val json = JSONObject(errorBody)
            if (json.has("detail")) {
                json.optString("detail", fallbackMessage)
            } else {
                fallbackMessage
            }
        } catch (_: Exception) {
            fallbackMessage
        }
    }
}

