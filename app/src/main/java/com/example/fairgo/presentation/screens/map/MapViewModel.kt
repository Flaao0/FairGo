package com.example.fairgo.presentation.screens.map

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AddressItem(
    val street: String,
    val city: String,
)

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _fromAddress = MutableStateFlow("ул. Таубе, 15")
    val fromAddress: StateFlow<String> = _fromAddress.asStateFlow()

    private val _toAddress = MutableStateFlow("")
    val toAddress: StateFlow<String> = _toAddress.asStateFlow()

    private val _recentAddresses = MutableStateFlow(
        listOf(
            AddressItem("ул. Таубе, 15", "Омск"),
            AddressItem("ул. Старозагородная Роща, д. 8", "Омск"),
            AddressItem("1-й Самарский переулок, д. 18", "Омск"),
            AddressItem("ул. Кирова, д. 20", "Омск"),
        ),
    )
    val recentAddresses: StateFlow<List<AddressItem>> = _recentAddresses.asStateFlow()

    fun onSearchQueryChanged(value: String) {
        _searchQuery.value = value
    }

    fun onFromAddressChanged(value: String) {
        _fromAddress.value = value
    }

    fun onToAddressChanged(value: String) {
        _toAddress.value = value
    }
}

