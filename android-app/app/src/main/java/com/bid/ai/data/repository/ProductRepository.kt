
package com.bid.ai.data.repository

import com.bid.ai.data.api.RetrofitClient
import com.bid.ai.data.model.Product

class ProductRepository {

    suspend fun getProducts(): Result<List<Product>> {
        return try {
            val response = RetrofitClient.productApi.getProducts()

            if (response.isSuccessful) {
                Result.success(response.body()?.products.orEmpty())
            } else {
                Result.failure(
                    Exception("Failed to load products (${response.code()})")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
