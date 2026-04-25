package com.example.fairgo.presentation.screens.map

import android.util.Log
import androidx.lifecycle.ViewModel
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AddressItem(
    val street: String,
    val city: String,
    val point: Point? = null,
)

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _fromAddress = MutableStateFlow("Брянск, текущее место")
    val fromAddress: StateFlow<String> = _fromAddress.asStateFlow()

    private val _toAddress = MutableStateFlow("")
    val toAddress: StateFlow<String> = _toAddress.asStateFlow()

    private val _addressSuggestions = MutableStateFlow<List<SuggestItem>>(emptyList())
    val addressSuggestions: StateFlow<List<SuggestItem>> = _addressSuggestions.asStateFlow()

    private var currentUserLocation: Point? = null

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

        Log.d("MAP_DEBUG", "Запрос маршрута: Start=$start, End=$end")

        if (end != null) {
            buildRoute(start, end)
        }
    }

    fun buildRoute(startPoint: Point, endPoint: Point) {
        _routeGeometry.value = null

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

    override fun onCleared() {
        super.onCleared()
        drivingSession?.cancel()
        suggestSession.reset()
        searchSession?.cancel()
    }

    private companion object {
        val WORLD_BOUNDING_BOX = BoundingBox(
            Point(-90.0, -180.0),
            Point(90.0, 180.0)
        )
    }
}