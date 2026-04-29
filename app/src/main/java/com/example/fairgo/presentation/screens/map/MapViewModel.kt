package com.example.fairgo.presentation.screens.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fairgo.data.network.RideStatusSocket
import com.example.fairgo.data.network.models.RideResponse
import com.example.fairgo.data.repository.RideRepository
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouter
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.search.Response
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.search.SearchManager
import com.yandex.mapkit.search.SearchManagerType
import com.yandex.mapkit.search.SearchOptions
import com.yandex.mapkit.search.Session
import com.yandex.mapkit.search.SuggestItem
import com.yandex.mapkit.search.SuggestOptions
import com.yandex.mapkit.search.SuggestResponse
import com.yandex.mapkit.search.SuggestSession
import com.yandex.runtime.Error
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddressItem(
    val street: String,
    val city: String,
    val point: Point? = null,
)

data class TariffOption(
    val id: String,
    val name: String,
    val price: Int,
    val pickupTime: Int,
    val isSelected: Boolean,
)

sealed class OrderState {
    data object Idle : OrderState()
    data object Loading : OrderState()
    data class Created(val ride: RideResponse) : OrderState()
    data class Accepted(val rideId: Int) : OrderState()
    data class Finished(val rideId: Int) : OrderState()
    data class Error(val message: String) : OrderState()
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val rideRepository: RideRepository,
    private val rideStatusSocket: RideStatusSocket,
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _fromAddress = MutableStateFlow("Брянск, текущее место")
    val fromAddress: StateFlow<String> = _fromAddress.asStateFlow()

    private val _toAddress = MutableStateFlow("")
    val toAddress: StateFlow<String> = _toAddress.asStateFlow()

    private val _addressSuggestions = MutableStateFlow<List<SuggestItem>>(emptyList())
    val addressSuggestions: StateFlow<List<SuggestItem>> = _addressSuggestions.asStateFlow()

    private var currentUserLocation: Point? = null
    private var lastRouteFinishPoint: Point? = null

    val orderState = MutableStateFlow<OrderState>(OrderState.Idle)
    val tariffs = MutableStateFlow<List<TariffOption>>(emptyList())
    val routeEta = MutableStateFlow(0)
    val startAddressText = MutableStateFlow("Мое местоположение")
    val endAddressText = MutableStateFlow("")

    private var connectedRideId: Int? = null
    private var reconnectAttempt: Int = 0
    private var reconnectJob: Job? = null

    fun updateUserLocation(point: Point) {
        currentUserLocation = point
    }

    private val _recentAddresses = MutableStateFlow<List<AddressItem>>(
        listOf(
            AddressItem("ТРЦ Аэропарк", "Брянск", Point(53.298282, 34.305602)),
            AddressItem("Курган Бессмертия", "Брянск", Point(53.272186, 34.346808)),
            AddressItem("Площадь Партизан", "Брянск", Point(53.242131, 34.363935)),
            AddressItem("Набережная", "Брянск", Point(53.243562, 34.372500)),
        )
    )
    val recentAddresses: StateFlow<List<AddressItem>> = _recentAddresses.asStateFlow()

    private val drivingRouter: DrivingRouter =
        DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)
    private var drivingSession: DrivingSession? = null
    private val searchManager: SearchManager =
        SearchFactory.getInstance().createSearchManager(SearchManagerType.COMBINED)
    private val suggestSession: SuggestSession = searchManager.createSuggestSession()
    private var searchSession: Session? = null

    private val _routeGeometry = MutableStateFlow<Polyline?>(null)
    val routeGeometry: StateFlow<Polyline?> = _routeGeometry.asStateFlow()

    fun buildRouteToAddress(item: AddressItem) {
        // Если ты в Брянске, старт будет там. Если GPS не поймал - берем Набережную как дефолт
        val start = currentUserLocation ?: Point(53.243562, 34.372500)
        val end = item.point
        lastRouteFinishPoint = end
        orderState.value = OrderState.Idle
        tariffs.value = emptyList()
        routeEta.value = 0
        // здесь старт - текущее местоположение
        startAddressText.value = "Мое местоположение"
        endAddressText.value = "${item.street}${if (item.city.isNotBlank()) ", ${item.city}" else ""}"

        Log.d("MAP_DEBUG", "Запрос маршрута: Start=$start, End=$end")

        if (end != null) {
            buildRoute(start, end)
        }
    }

    fun buildRoute(startPoint: Point, endPoint: Point) {
        _routeGeometry.value = null
        lastRouteFinishPoint = endPoint
        orderState.value = OrderState.Idle
        tariffs.value = emptyList()
        routeEta.value = 0
        startAddressText.value = "Мое местоположение"

        val requestPoints = listOf(
            RequestPoint(startPoint, RequestPointType.WAYPOINT, null, null),
            RequestPoint(endPoint, RequestPointType.WAYPOINT, null, null)
        )

        drivingSession = drivingRouter.requestRoutes(
            requestPoints,
            DrivingOptions(),
            VehicleOptions(),
            object : DrivingSession.DrivingRouteListener {
                override fun onDrivingRoutes(routes: MutableList<DrivingRoute>) {
                    if (routes.isNotEmpty()) {
                        Log.d(
                            "MAP_DEBUG",
                            "Успех! Маршрут построен. Точек в линии: ${routes.first().geometry.points.size}"
                        )
                        _routeGeometry.value = routes.first().geometry

                        // Позже привяжем к данным Яндекса, пока — динамика "на лету"
                        routeEta.value = (10..20).random()
                        calculateTariffs(basePrice = (150..250).random())
                    } else {
                        Log.e("MAP_DEBUG", "Яндекс вернул пустой список маршрутов")
                    }
                }

                override fun onDrivingRoutesError(error: com.yandex.runtime.Error) {
                    Log.e("MAP_DEBUG", "Ошибка Яндекса при построении: $error")
                }
            }
        )
    }

    fun onFromAddressChanged(value: String) {
        _fromAddress.value = value
    }

    fun onToAddressChanged(value: String) {
        _toAddress.value = value
        suggestAddress(value)
    }

    fun onSearchQueryChanged(value: String) {
        _searchQuery.value = value
    }

    fun suggestAddress(query: String) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) {
            _addressSuggestions.value = emptyList()
            suggestSession.reset()
            return
        }

        suggestSession.suggest(
            normalizedQuery,
            WORLD_BOUNDING_BOX,
            SuggestOptions(),
            object : SuggestSession.SuggestListener {
                override fun onResponse(response: SuggestResponse) {
                    _addressSuggestions.value = response.items
                }

                override fun onError(error: Error) {
                    Log.e("MAP_DEBUG", "Ошибка подсказок: $error")
                    _addressSuggestions.value = emptyList()
                }
            }
        )
    }

    fun onAddressSelected(suggestItem: SuggestItem) {
        val queryText = suggestItem.displayText?.takeIf { it.isNotBlank() }
            ?: suggestItem.title.text.takeIf { it.isNotBlank() }
            ?: return

        endAddressText.value = queryText

        _toAddress.value = queryText
        _addressSuggestions.value = emptyList()

        val start = currentUserLocation ?: Point(53.243562, 34.372500)
        searchSession?.cancel()
        searchSession = searchManager.submit(
            queryText,
            Geometry.fromBoundingBox(WORLD_BOUNDING_BOX),
            SearchOptions(),
            object : Session.SearchListener {
                override fun onSearchResponse(response: Response) {
                    val endPoint = response.collection.children.firstNotNullOfOrNull { child ->
                        child.obj?.geometry?.firstOrNull()?.point
                    }

                    if (endPoint != null) {
                        lastRouteFinishPoint = endPoint
                        orderState.value = OrderState.Idle
                        tariffs.value = emptyList()
                        routeEta.value = 0
                        startAddressText.value = "Мое местоположение"

                        val newAddress = AddressItem(
                            street = suggestItem.title.text,
                            city = suggestItem.subtitle?.text.orEmpty(),
                            point = endPoint
                        )
                        _recentAddresses.update { addresses ->
                            listOf(newAddress) + addresses.filterNot {
                                it.street == newAddress.street && it.city == newAddress.city
                            }
                        }
                        buildRoute(start, endPoint)
                    } else {
                        Log.e("MAP_DEBUG", "Не удалось получить координаты из подсказки")
                    }
                }

                override fun onSearchError(error: Error) {
                    Log.e("MAP_DEBUG", "Ошибка поиска координат: $error")
                }
            }
        )
    }

    fun calculateTariffs(basePrice: Int) {
        val standard = TariffOption(
            id = "standard",
            name = "Стандарт",
            price = basePrice,
            pickupTime = (2..5).random(),
            isSelected = false,
        )
        val lux = TariffOption(
            id = "lux",
            name = "Люкс",
            price = basePrice + (50..200).random(),
            pickupTime = (8..15).random(),
            isSelected = true,
        )
        val kids = TariffOption(
            id = "kids",
            name = "Детское",
            price = basePrice + (100..150).random(),
            pickupTime = (4..10).random(),
            isSelected = false,
        )

        tariffs.value = listOf(standard, lux, kids)
    }

    fun selectTariff(id: String) {
        tariffs.value = tariffs.value.map { it.copy(isSelected = it.id == id) }
    }

    fun orderTaxi(selectedPrice: Int? = null) {
        val start = currentUserLocation
        val finish = lastRouteFinishPoint

        if (start == null || finish == null) {
            orderState.value = OrderState.Error("Не удалось определить точки маршрута")
            return
        }

        viewModelScope.launch {
            orderState.value = OrderState.Loading
            val result = rideRepository.createRide(start = start, finish = finish)
            orderState.value = result.fold(
                onSuccess = { ride ->
                    connectToRideSocket(ride.id)
                    OrderState.Created(ride)
                },
                onFailure = { e -> OrderState.Error(e.message ?: "Не удалось создать заказ") }
            )
        }
    }

    fun connectToRideSocket(rideId: Int) {
        if (connectedRideId == rideId) return

        reconnectJob?.cancel()
        connectedRideId = rideId
        reconnectAttempt = 0

        rideStatusSocket.connect(rideId) { event ->
            when (event) {
                is RideStatusSocket.Event.Message -> {
                    val status = event.message.status
                    if (status.equals("ACCEPTED", ignoreCase = true)) {
                        orderState.value = OrderState.Accepted(event.message.rideId)
                        // можно закрыть сокет, но оставим подключение для будущих статусов
                    } else if (status.equals("FINISHED", ignoreCase = true)) {
                        orderState.value = OrderState.Finished(event.message.rideId)
                        reconnectJob?.cancel()
                        reconnectJob = null
                        rideStatusSocket.close()
                    }
                }
                is RideStatusSocket.Event.Failure,
                is RideStatusSocket.Event.Closed -> {
                    scheduleReconnect()
                }
                else -> Unit
            }
        }
    }

    private fun scheduleReconnect() {
        val rideId = connectedRideId ?: return
        if (orderState.value is OrderState.Accepted || orderState.value is OrderState.Idle) return

        reconnectJob?.cancel()
        reconnectJob = viewModelScope.launch {
            val delayMs = (1000L shl reconnectAttempt.coerceAtMost(5)).coerceAtMost(30_000L)
            reconnectAttempt = (reconnectAttempt + 1).coerceAtMost(10)
            kotlinx.coroutines.delay(delayMs)
            if (connectedRideId == rideId && orderState.value is OrderState.Created) {
                rideStatusSocket.connect(rideId) { event ->
                    when (event) {
                        is RideStatusSocket.Event.Message -> {
                            val status = event.message.status
                            if (status.equals("ACCEPTED", ignoreCase = true)) {
                                orderState.value = OrderState.Accepted(event.message.rideId)
                            } else if (status.equals("FINISHED", ignoreCase = true)) {
                                orderState.value = OrderState.Finished(event.message.rideId)
                                reconnectJob?.cancel()
                                reconnectJob = null
                                rideStatusSocket.close()
                            }
                        }
                        is RideStatusSocket.Event.Failure,
                        is RideStatusSocket.Event.Closed -> scheduleReconnect()
                        else -> Unit
                    }
                }
            }
        }
    }

    fun cancelOrder() {
        orderState.value = OrderState.Idle
        connectedRideId = null
        reconnectJob?.cancel()
        reconnectJob = null
        rideStatusSocket.close()
    }

    fun resetOrder() {
        _routeGeometry.value = null
        tariffs.value = emptyList()
        routeEta.value = 0
        _toAddress.value = ""
        _addressSuggestions.value = emptyList()
        startAddressText.value = "Мое местоположение"
        endAddressText.value = ""

        connectedRideId = null
        reconnectJob?.cancel()
        reconnectJob = null
        rideStatusSocket.close()

        orderState.value = OrderState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        drivingSession?.cancel()
        suggestSession.reset()
        searchSession?.cancel()
        reconnectJob?.cancel()
        rideStatusSocket.close()
    }

    private companion object {
        val WORLD_BOUNDING_BOX = BoundingBox(
            Point(-90.0, -180.0),
            Point(90.0, 180.0)
        )
    }
}