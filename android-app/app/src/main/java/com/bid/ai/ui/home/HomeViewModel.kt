
package com.bid.ai.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bid.ai.data.model.Product
import com.bid.ai.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val products: List<Product> = emptyList(),
    val error: String? = null
)

class HomeViewModel : ViewModel() {

    private val repository = ProductRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(loading = true)

            repository.getProducts().fold(
                onSuccess = { products ->
                    _uiState.value = HomeUiState(
                        loading = false,
                        products = products
                    )
                },
                onFailure = {
                    _uiState.value = HomeUiState(
                        loading = false,
                        error = "Couldn't load products. Check your connection."
                    )
                }
            )
        }
    }
}
