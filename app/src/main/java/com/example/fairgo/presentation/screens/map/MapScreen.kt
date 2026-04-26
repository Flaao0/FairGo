package com.example.fairgo.presentation.screens.map

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.logo.HorizontalAlignment
import com.yandex.mapkit.logo.Padding
import com.yandex.mapkit.logo.VerticalAlignment
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onNavigateToAddressSelection: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateToPromoCode: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val recentAddresses by viewModel.recentAddresses.collectAsState()
    val routeGeometry by viewModel.routeGeometry.collectAsState()
    val orderState by viewModel.orderState.collectAsState()
    val tariffs by viewModel.tariffs.collectAsState()
    val routeEta by viewModel.routeEta.collectAsState()

    val hasLocationPermission = remember(context) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }

    var hasCenteredToUserLocation by remember { mutableStateOf(false) }

    // Спасаем линию от сборщика мусора
    var routePolylineMapObject by remember { mutableStateOf<PolylineMapObject?>(null) }

    val mapView = remember {
        val density = context.resources.displayMetrics.density
        val verticalPaddingPx = (130 * density).toInt()
        val horizontalPaddingPx = (16 * density).toInt()

        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            mapWindow.map.apply {
                isZoomGesturesEnabled = true
                isScrollGesturesEnabled = true
                isTiltGesturesEnabled = true
                isRotateGesturesEnabled = true

                logo.setAlignment(
                    com.yandex.mapkit.logo.Alignment(
                        HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM
                    )
                )
                logo.setPadding(Padding(horizontalPaddingPx, verticalPaddingPx))
            }
        }
    }

    // 1. Создаем ОТДЕЛЬНУЮ коллекцию только для маршрута, чтобы не ломать карту очисткой
    val routeCollection = remember(mapView) {
        mapView.mapWindow.map.mapObjects.addCollection()
    }

    val userLocationLayer = remember {
        MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow).apply {
            isHeadingEnabled = true
        }
    }

    DisposableEffect(userLocationLayer, hasLocationPermission) {
        userLocationLayer.isVisible = hasLocationPermission
        val listener = object : UserLocationObjectListener {

            private fun tryCenterMap(point: Point) {
                if (point.latitude == 0.0 && point.longitude == 0.0) return

                viewModel.updateUserLocation(point)

                if (!hasCenteredToUserLocation) {
                    mapView.mapWindow.map.move(
                        CameraPosition(point, 17.0f, 0f, 0f),
                        Animation(Animation.Type.SMOOTH, 0.5f),
                        null,
                    )
                    hasCenteredToUserLocation = true
                }
            }

            override fun onObjectAdded(userLocationView: UserLocationView) {
                tryCenterMap(userLocationView.pin.geometry)
            }

            override fun onObjectRemoved(userLocationView: UserLocationView) = Unit

            override fun onObjectUpdated(
                userLocationView: UserLocationView,
                objectEvent: ObjectEvent,
            ) {
                tryCenterMap(userLocationView.pin.geometry)
            }
        }
        userLocationLayer.setObjectListener(listener)
        onDispose {
            userLocationLayer.setObjectListener(null)
        }
    }

    LaunchedEffect(userLocationLayer, hasLocationPermission) {
        if (hasLocationPermission) {
            userLocationLayer.isVisible = true
            while (!hasCenteredToUserLocation) {
                val camera = userLocationLayer.cameraPosition()
                if (camera != null && camera.target.latitude != 0.0 && camera.target.longitude != 0.0) {
                    viewModel.updateUserLocation(camera.target)
                    mapView.mapWindow.map.move(
                        CameraPosition(camera.target, 17f, 0f, 0f),
                        Animation(Animation.Type.SMOOTH, 0.5f),
                        null,
                    )
                    hasCenteredToUserLocation = true
                } else {
                    kotlinx.coroutines.delay(500)
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    MapKitFactory.getInstance().onStart()
                    mapView.onStart()
                }

                Lifecycle.Event.ON_STOP -> {
                    mapView.onStop()
                    MapKitFactory.getInstance().onStop()
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // --- БРОНЕБОЙНАЯ ОТРИСОВКА МАРШРУТА ---
    LaunchedEffect(routeGeometry) {
        // Очищаем ТОЛЬКО коллекцию маршрутов, а не всю карту
        routeCollection.clear()

        val currentRoute = routeGeometry
        if (currentRoute != null) {
            try {
                Log.d("MAP_DEBUG", "Начинаем рисовать линию на карте...")

                // Добавляем полилинию в нашу отдельную коллекцию
                val polyline = routeCollection.addPolyline(currentRoute)

                // 2. Используем железобетонный android.graphics.Color
                polyline.setStrokeColor(android.graphics.Color.parseColor("#2C68FF"))
                polyline.setStrokeWidth(6f)
                polyline.setZIndex(100f)

                routePolylineMapObject = polyline
                Log.d("MAP_DEBUG", "Линия успешно добавлена в routeCollection!")

                // 3. Пытаемся подвинуть камеру
                val geometry = Geometry.fromPolyline(currentRoute)
                val cameraPosition = mapView.mapWindow.map.cameraPosition(geometry)

                mapView.mapWindow.map.move(
                    CameraPosition(cameraPosition.target, cameraPosition.zoom - 0.8f, 0f, 0f),
                    Animation(Animation.Type.SMOOTH, 1.2f),
                    null
                )
                Log.d("MAP_DEBUG", "Камера успешно передвинута к маршруту!")

            } catch (e: Exception) {
                Log.e("MAP_DEBUG", "КРАШ при отрисовке маршрута: ${e.message}")
            }
        }
    }
    // --------------------------------------------

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            DrawerMenuContent(
                onClose = { scope.launch { drawerState.close() } },
                onLogout = { /* TODO */ },
                onNavigateToPayment = onNavigateToPayment,
                onNavigateToPromoCode = onNavigateToPromoCode
            )
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { mapView },
            )

            FloatingCircleButton(
                icon = { Icon(Icons.Default.Menu, contentDescription = "Меню") },
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .systemBarsPadding()
                    .padding(start = 16.dp, top = 16.dp)
            )

            FloatingCircleButton(
                icon = { Icon(Icons.Default.GpsFixed, contentDescription = "Моя локация") },
                onClick = {
                    if (hasLocationPermission) {
                        val camera = userLocationLayer.cameraPosition()
                        if (camera != null) {
                            mapView.mapWindow.map.move(
                                CameraPosition(camera.target, 17f, 0f, 0f),
                                Animation(Animation.Type.SMOOTH, 0.5f),
                                null,
                            )
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 16.dp, bottom = 140.dp),
            )

            AnimatedContent(
                targetState = routeGeometry != null && orderState !is OrderState.Created && orderState !is OrderState.Accepted,
                transitionSpec = {
                    (fadeIn(tween(200)) + slideInVertically(tween(260)) { it / 4 })
                        .togetherWith(fadeOut(tween(160)) + slideOutVertically(tween(220)) { it / 6 })
                        .using(SizeTransform(clip = false))
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                label = "bottom_card",
            ) { showRideCard ->
                if (showRideCard) {
                    RideOptionsCard(
                        tariffs = tariffs,
                        routeEtaMinutes = routeEta,
                        orderState = orderState,
                        onSelectTariff = { viewModel.selectTariff(it) },
                        onOrderClick = {
                            val selected = tariffs.firstOrNull { it.isSelected }
                            viewModel.orderTaxi(selectedPrice = selected?.price)
                        },
                    )
                } else {
                    DefaultSearchCard(
                        recentAddresses = recentAddresses,
                        onNavigateToAddressSelection = onNavigateToAddressSelection,
                        onRecentClick = { viewModel.buildRouteToAddress(it) },
                    )
                }
            }

            if (orderState is OrderState.Created) {
                SearchingDriverOverlay(
                    onCancel = { viewModel.cancelOrder() },
                )
            }

            if (orderState is OrderState.Accepted) {
                DriverAcceptedCard(
                    onMessageDriver = { /* TODO: чат */ },
                    onCancelRide = { viewModel.cancelOrder() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun DriverAcceptedCard(
    onMessageDriver: () -> Unit,
    onCancelRide: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 34.dp, bottom = 18.dp)
                    .navigationBarsPadding(),
            ) {
                // Верхний "хэндл" как в дизайне
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 12.dp)
                        .size(width = 46.dp, height = 4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFD8DDE1)),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "Андрей",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF3D4754),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFF4C430),
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "4.8",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF97A8B7),
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = with(Modifier) { widthIn(min = 120.dp, max = 170.dp) },
                    ) {
                        Surface(
                            color = Color(0xFFF0F2F5),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = "У000РА 35",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF3D4754),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Volkswagen Jetta",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF97A8B7),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(
                        onClick = onMessageDriver,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7DB546)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                    ) {
                        Text(
                            text = "Написать водителю",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }

                    Surface(
                        onClick = onCancelRide,
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .size(56.dp)
                            .border(1.dp, Color(0xFFDCE2E8), RoundedCornerShape(16.dp)),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Отменить",
                                tint = Color(0xFF97A8B7),
                            )
                        }
                    }
                }
            }
        }

        // Выпирающая аватарка
        Surface(
            shape = CircleShape,
            color = Color(0xFFE9EEF2),
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 20.dp, y = (-30).dp)
                .size(64.dp)
                .clip(CircleShape)
                .border(4.dp, Color.White, CircleShape),
            shadowElevation = 8.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFF97A8B7),
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun DefaultSearchCard(
    recentAddresses: List<AddressItem>,
    onNavigateToAddressSelection: () -> Unit,
    onRecentClick: (AddressItem) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding(),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFD8DDE1)),
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable(onClick = onNavigateToAddressSelection),
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFFF7F7F7),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF7DB546),
                    )
                    Text(
                        text = "Куда едем?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFA0AAB4),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (recentAddresses.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                ) {
                    recentAddresses.take(2).forEach { address ->
                        AddressListItem(
                            address = address,
                            onClick = { onRecentClick(address) }
                        )
                        HorizontalDivider(
                            Modifier, DividerDefaults.Thickness, color = Color(0xFFDCE2E8)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    state: OrderState,
    onOrderClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding(),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFD8DDE1)),
            )

            Text(
                text = when (state) {
                    OrderState.Idle -> "Готовы заказать поездку?"
                    OrderState.Loading -> "Создаём заказ…"
                    is OrderState.Created -> "Поиск водителя..."
                    is OrderState.Error -> state.message
                    is OrderState.Accepted -> {""}
                },
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF3D4754),
                modifier = Modifier.padding(bottom = 12.dp),
            )

            val isLoading = state == OrderState.Loading
            val isCreated = state is OrderState.Created

            Button(
                onClick = onOrderClick,
                enabled = !isLoading && !isCreated,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7DB546)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(text = "Заказываем…", color = Color.White)
                } else {
                    Text(
                        text = if (isCreated) "Заказ создан" else "Заказать такси",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun RideOptionsCard(
    tariffs: List<TariffOption>,
    routeEtaMinutes: Int,
    orderState: OrderState,
    onSelectTariff: (String) -> Unit,
    onOrderClick: () -> Unit,
) {
    val isLoading = orderState == OrderState.Loading

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp)
                .navigationBarsPadding(),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 16.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFD8DDE1)),
            )

            TariffsCarousel(
                tariffs = tariffs,
                onSelect = onSelectTariff,
            )

            Spacer(Modifier.height(12.dp))

            BottomInfoRow(etaMinutes = routeEtaMinutes)

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = onOrderClick,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7DB546)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = Color.White,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(text = "Заказываем…", color = Color.White)
                } else {
                    Text(text = "Заказать такси", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun TariffsCarousel(
    tariffs: List<TariffOption>,
    onSelect: (String) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(tariffs, key = { it.id }) { item ->
            TariffItem(
                option = item,
                onClick = { onSelect(item.id) },
            )
        }
    }
}

@Composable
private fun TariffItem(
    option: TariffOption,
    onClick: () -> Unit,
) {
    val borderColor = if (option.isSelected) Color(0xFF7DB546) else Color.Transparent
    val container = Color.White
    val pickupBg = if (option.isSelected) Color(0xFF7DB546) else Color(0xFFE9EEF2)
    val pickupText = if (option.isSelected) Color.White else Color(0xFF7B8794)

    val icon = when (option.id) {
        "kids" -> Icons.Default.ChildCare
        else -> Icons.Default.DirectionsCar
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = container,
        shadowElevation = if (option.isSelected) 10.dp else 6.dp,
        tonalElevation = 0.dp,
        modifier = Modifier
            .width(118.dp)
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF3D4754),
                    modifier = Modifier.size(22.dp),
                )
                if (option.id == "lux") {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF3F9EC),
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = Color(0xFF7DB546),
                            modifier = Modifier.padding(4.dp).size(14.dp),
                        )
                    }
                }
            }

            Text(
                text = option.name,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF3D4754),
            )

            Text(
                text = "${option.price} ₽",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF3D4754),
            )

            Surface(
                color = pickupBg,
                shape = RoundedCornerShape(50),
            ) {
                Text(
                    text = "${option.pickupTime} мин",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = pickupText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun BottomInfoRow(etaMinutes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Примерное время в пути",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFA0AAB4),
            )
            Text(
                text = "$etaMinutes мин",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Color(0xFF7DB546),
            )
        }
        PaymentRow()
    }
}

@Composable
private fun PaymentRow() {
    Row(
        modifier = Modifier
            .clickable { }
            .padding(start = 8.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.AttachMoney,
            contentDescription = null,
            tint = Color(0xFF7DB546),
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Cash",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color(0xFF3D4754),
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFFA0AAB4),
        )
    }
}

@Composable
private fun SearchingDriverOverlay(
    onCancel: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB3000000)),
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.92f),
            modifier = Modifier
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(start = 16.dp, top = 12.dp)
                .size(40.dp)
                .align(Alignment.TopStart),
            onClick = onCancel,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Отмена",
                    tint = Color(0xFF3D4754),
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(260.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBFC6CC).copy(alpha = 0.30f))
            )
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBFC6CC).copy(alpha = 0.34f))
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFBFC6CC).copy(alpha = 0.38f))
            )
            Surface(
                shape = CircleShape,
                color = Color(0xFFF3F5F7),
                shadowElevation = 8.dp,
                modifier = Modifier.size(64.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Color(0xFF3D4754),
                        modifier = Modifier.size(30.dp),
                    )
                }
            }
        }

        Text(
            text = "Поиск такси",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .navigationBarsPadding(),
        )
    }
}

// ... Оставшийся код (DrawerMenuContent, AddressListItem и т.д.) без изменений ...
@Composable
fun DrawerMenuContent(
    onClose: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateToPromoCode: () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        modifier = Modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF7DB546))
                .systemBarsPadding()
                .padding(start = 24.dp, top = 24.dp, bottom = 32.dp, end = 24.dp)
        ) {
            Surface(
                modifier = Modifier.size(72.dp), shape = CircleShape, color = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Аватар",
                    tint = Color.LightGray,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Егор",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Udalovea@yandex.ru",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        DrawerMenuItem(text = "ИСТОРИЯ ПОЕЗДОК", onClick = { onClose() })
        DrawerMenuItem(
            text = "СПОСОБ ОПЛАТЫ", onClick = {
                onClose()
                onNavigateToPayment()
            }
        )
        DrawerMenuItem(text = "ПРОМОКОД", badge = "1", onClick = {
            onClose()
            onNavigateToPromoCode()
        })
        DrawerMenuItem(text = "ПОДДЕРЖКА", onClick = { onClose() })

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Выйти",
            color = Color(0xFF7DB546),
            textDecoration = TextDecoration.Underline,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(start = 24.dp, bottom = 48.dp)
                .navigationBarsPadding()
                .clickable {
                    onClose()
                    onLogout()
                }
        )
    }
}

@Composable
private fun FloatingCircleButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 6.dp,
        tonalElevation = 2.dp,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            icon()
        }
    }
}

@Composable
private fun AddressListItem(
    address: AddressItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFD8DEE4)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "•", color = Color.White)
        }
        Column {
            Text(
                text = address.street,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF3D4754)
            )
            Text(
                text = address.city,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF97A8B7)
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    text: String,
    badge: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF3D4754)
        )

        if (badge != null) {
            Spacer(modifier = Modifier.weight(1f))
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF3D4754)
                    )
                }
            }
        }
    }
}