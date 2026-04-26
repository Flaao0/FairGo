package com.example.fairgo.data.network

import android.util.Log
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

@Singleton
class RideStatusSocket @Inject constructor(
    private val okHttpClient: OkHttpClient,
) {
    private val gson = Gson()
    private var socket: WebSocket? = null

    fun connect(
        rideId: Int,
        onEvent: (Event) -> Unit,
    ) {
        close()

        val request = Request.Builder()
            .url("ws://10.0.2.2:8000/ws/rides/$rideId/")
            .build()

        socket = okHttpClient.newWebSocket(
            request,
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    Log.d("WS_CONN", "Соединение установлено по адресу: ${webSocket.request().url}")
                    onEvent(Event.Opened)
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    Log.d("WS_RAW", "Пришло сырое сообщение: $text")
                    val parsed = runCatching { gson.fromJson(text, RideStatusMessage::class.java) }.getOrNull()
                    if (parsed != null) {
                        onEvent(Event.Message(parsed))
                    }
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    // ignore
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    onEvent(Event.Closing(code, reason))
                    webSocket.close(code, reason)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    Log.d("WS_CONN", "Соединение закрыто. code=$code reason=$reason")
                    onEvent(Event.Closed(code, reason))
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.e("WS_ERROR", "Ошибка сокета: ", t)
                    onEvent(Event.Failure(t))
                }
            }
        )
    }

    fun close() {
        socket?.close(1000, "closed")
        socket = null
    }

    sealed class Event {
        data object Opened : Event()
        data class Message(val message: RideStatusMessage) : Event()
        data class Closing(val code: Int, val reason: String) : Event()
        data class Closed(val code: Int, val reason: String) : Event()
        data class Failure(val throwable: Throwable) : Event()
    }
}

