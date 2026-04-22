package com.example.fairgo.data.repository

import com.example.fairgo.data.local.TokenManager
import com.example.fairgo.data.network.FairGoApi
import com.example.fairgo.data.network.models.AuthRequest
import com.example.fairgo.data.network.models.RegisterRequest
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: FairGoApi,
    private val tokenManager: TokenManager
) {

    suspend fun login(request: AuthRequest): Result<Unit> {
        return try {
            val response = api.login(request)
            tokenManager.saveTokens(response.access, response.refresh)
            Result.success(Unit)
        } catch (e: HttpException) {
            Result.failure(Exception(parseHttpError(e)))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(request: RegisterRequest): Result<Unit> {
        return try {
            api.register(request)
            Result.success(Unit)
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
                return json.optString("detail", fallbackMessage)
            }

            val messages = mutableListOf<String>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = json.opt(key) ?: continue
                when (value) {
                    is org.json.JSONArray -> {
                        for (i in 0 until value.length()) {
                            val item = value.optString(i).trim()
                            if (item.isNotBlank()) {
                                messages.add(item)
                            }
                        }
                    }
                    is String -> {
                        if (value.isNotBlank()) {
                            messages.add(value)
                        }
                    }
                    else -> {
                        val text = value.toString().trim()
                        if (text.isNotBlank()) {
                            messages.add(text)
                        }
                    }
                }
            }

            messages.firstOrNull() ?: fallbackMessage
        } catch (_: Exception) {
            fallbackMessage
        }
    }
}
