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
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    }

    fun onSearchQueryChanged(value: String) {
        _searchQuery.value = value
    }

    override fun onCleared() {
        super.onCleared()
        drivingSession?.cancel()
    }
}