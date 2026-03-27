package com.example.fairgo.presentation.screens.map

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onNavigateToAddressSelection: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val recentAddresses by viewModel.recentAddresses.collectAsState()

    val hasLocationPermission = remember(context) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }

    var hasCenteredToUserLocation by rememberSaveable { mutableStateOf(false) }
    var lastUserPoint by remember { mutableStateOf<Point?>(null) }

    val mapView = remember {
        MapView(context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            mapWindow.map.apply {
                isZoomGesturesEnabled = true
                isScrollGesturesEnabled = true
                isTiltGesturesEnabled = true
                isRotateGesturesEnabled = true
            }
        }
    }

    val userLocationLayer = remember {
        MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow).apply {
            isVisible = hasLocationPermission
        }
    }

    DisposableEffect(userLocationLayer) {
        userLocationLayer.isVisible = hasLocationPermission
        val listener = object : UserLocationObjectListener {

            // Вынесли центрирование в отдельную функцию
            private fun tryCenterMap(point: Point) {
                if (point.latitude == 0.0 && point.longitude == 0.0) return // Ждем нормальные координаты

                lastUserPoint = point
                if (!hasCenteredToUserLocation) {
                    mapView.mapWindow.map.move(
                        CameraPosition(point, 16.0f, 0f, 0f),
                        Animation(Animation.Type.SMOOTH, 1.5f),
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
                // Теперь карта проверяет координаты постоянно, пока не прилетит к тебе
                tryCenterMap(userLocationView.pin.geometry)
            }
        }
        userLocationLayer.setObjectListener(listener)
        onDispose {
            userLocationLayer.setObjectListener(null)
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

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Жесты меню работают ТОЛЬКО когда оно открыто. Закрытой карте они больше не мешают!
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Меню (заглушка)",
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
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
                        userLocationLayer.isVisible = true
                        // Надежный способ: берем координаты напрямую из слоя Яндекса
                        val target = userLocationLayer.cameraPosition()?.target ?: lastUserPoint

                        if (target != null && target.latitude != 0.0) {
                            mapView.mapWindow.map.move(
                                CameraPosition(target, 16f, 0f, 0f),
                                Animation(Animation.Type.SMOOTH, 1.0f),
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

            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
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
                                AddressListItem(address)
                                HorizontalDivider(
                                    Modifier,
                                    DividerDefaults.Thickness,
                                    color = Color(0xFFDCE2E8)
                                )
                            }
                        }
                    }
                }
            }
        }
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
private fun AddressListItem(address: AddressItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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