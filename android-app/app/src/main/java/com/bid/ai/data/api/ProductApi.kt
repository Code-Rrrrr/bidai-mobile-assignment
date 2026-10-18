
package com.bid.ai.data.api

import com.bid.ai.data.model.ProductsResponse
import retrofit2.Response
import retrofit2.http.GET

interface ProductApi {

    @GET("api/products")
    suspend fun getProducts(): Response<ProductsResponse>
}
