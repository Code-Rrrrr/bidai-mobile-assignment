
package com.bid.ai.data.model

data class Product(
    val id: String,
    val seller_type: String,
    val brand: String?,
    val category: String,
    val product_type: String?,
    val title: String,
    val description: String?,
    val price: String,
    val location: String,
    val images: List<String> = emptyList(),
    val approval_status: String? = null
)

data class ProductsResponse(
    val products: List<Product>
)
