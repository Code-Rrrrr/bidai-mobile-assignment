
package com.bid.ai

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bid.ai.data.SessionManager
import com.bid.ai.data.model.Product
import com.bid.ai.ui.home.HomeViewModel
import kotlinx.coroutines.launch
import java.util.Locale
import android.widget.FrameLayout

class HomeActivity : ComponentActivity() {

    private lateinit var viewModel: HomeViewModel

    private lateinit var carsContainer: LinearLayout
    private lateinit var bikesContainer: LinearLayout
    private lateinit var recommendationsContainer: LinearLayout
    private lateinit var productsProgress: ProgressBar
    private lateinit var productsMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager(this)

        if (!session.isLoggedIn()) {
            openLogin()
            return
        }

        setContentView(R.layout.activity_home)

        findViewById<TextView>(R.id.navSell).setOnClickListener {
            startActivity(Intent(this, SellActivity::class.java))
        }

        carsContainer = findViewById(R.id.carsContainer)
        bikesContainer = findViewById(R.id.bikesContainer)
        recommendationsContainer =
            findViewById(R.id.recommendationsContainer)

        // Keep the original product loading indicators.
        // These views are created programmatically because the feed now
        // has three independent product sections.
        productsProgress = ProgressBar(this)
        productsMessage = TextView(this)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    productsProgress.visibility =
                        if (state.loading) View.VISIBLE else View.GONE

                    carsContainer.removeAllViews()
                    bikesContainer.removeAllViews()
                    recommendationsContainer.removeAllViews()

                    when {
                        state.loading -> {
                            // Show loading feedback in the first section.
                            carsContainer.addView(
                                messageView("Loading products...")
                            )
                        }

                        state.error != null -> {
                            carsContainer.addView(
                                messageView(state.error)
                            )
                        }

                        state.products.isEmpty() -> {
                            carsContainer.addView(
                                messageView("No products available yet.")
                            )
                        }

                        else -> {
                            val products = state.products

                            val cars = products.filter {
                                val category = it.category.orEmpty()
                                    .lowercase(Locale.ROOT)
                                category.contains("car") ||
                                        category.contains("vehicle")
                            }

                            val bikes = products.filter {
                                val category = it.category.orEmpty()
                                    .lowercase(Locale.ROOT)
                                category.contains("bike") ||
                                        category.contains("motorcycle")
                            }

                            val otherProducts = products.filterNot {
                                it in cars || it in bikes
                            }

                            // Cars section: use matching products first.
                            addProductRows(
                                carsContainer,
                                if (cars.isNotEmpty()) cars
                                else products.take(4)
                            )

                            // Bikes section: use matching products first.
                            addProductRows(
                                bikesContainer,
                                if (bikes.isNotEmpty()) bikes
                                else products.take(4)
                            )

                            // Recommendations: show the remaining products,
                            // or the whole feed if there are only a few.
                            val recommendations =
                                if (otherProducts.isNotEmpty()) {
                                    otherProducts
                                } else {
                                    products
                                }

                            addProductRows(
                                recommendationsContainer,
                                recommendations
                            )
                        }
                    }
                }
            }
        }

        viewModel.loadProducts()
    }

    private fun addProductRows(
        container: LinearLayout,
        products: List<Product>
    ) {
        products.chunked(2).forEach { rowProducts ->

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.TOP
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            rowProducts.forEach { product ->
                val card = createProductCard(product)

                val params = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(dp(3), dp(5), dp(3), dp(5))
                }

                row.addView(card, params)
            }

            // Keep a blank second column if the final row has one card.
            if (rowProducts.size == 1) {
                row.addView(
                    View(this),
                    LinearLayout.LayoutParams(
                        0,
                        1,
                        1f
                    ).apply {
                        setMargins(dp(3), dp(5), dp(3), dp(5))
                    }
                )
            }

            container.addView(row)
        }
    }

    private fun createProductCard(product: Product): LinearLayout {

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(6), dp(6), dp(6), dp(7))
            background = roundedBackground(
                Color.WHITE,
                Color.rgb(220, 224, 231),
                10
            )
        }

        // Image placeholder: actual product photos can be added later.
        val imageArea = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(105)
            )
            background = roundedBackground(
                Color.rgb(242, 244, 248),
                Color.rgb(232, 234, 239),
                6
            )
        }

        val logo = TextView(this).apply {
            text = "BiD.ai"
            gravity = Gravity.CENTER
            textSize = 19f
            setTextColor(Color.rgb(8, 104, 232))
            setTypeface(null, Typeface.BOLD)
        }

        imageArea.addView(
            logo,
            android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        card.addView(imageArea)

        val verified = TextView(this).apply {
            text = "🔹 Verified Product"
            textSize = 9f
            setTextColor(Color.rgb(8, 104, 232))
            setPadding(0, dp(4), 0, dp(2))
        }

        val price = TextView(this).apply {
            text = "₹ ${product.price}"
            textSize = 12f
            setTextColor(Color.BLACK)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(2), 0, dp(2))
        }

        val title = TextView(this).apply {
            text = product.title
            textSize = 11f
            setTextColor(Color.rgb(35, 35, 35))
            maxLines = 2
        }

        val brand = TextView(this).apply {
            text = product.brand ?: product.category.orEmpty()
            textSize = 10f
            setTextColor(Color.DKGRAY)
            maxLines = 1
        }

        val location = TextView(this).apply {
            text = product.location
            textSize = 10f
            setTextColor(Color.DKGRAY)
            maxLines = 1
        }

        card.addView(verified)
        card.addView(price)
        card.addView(title)
        card.addView(brand)
        card.addView(location)

        return card
    }

    private fun messageView(message: String): TextView {
        return TextView(this).apply {
            text = message
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(Color.DKGRAY)
            setPadding(dp(8), dp(12), dp(8), dp(12))
        }
    }

    private fun roundedBackground(
        fillColor: Int,
        strokeColor: Int,
        cornerRadiusDp: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fillColor)
            setStroke(dp(1), strokeColor)
            cornerRadius = dp(cornerRadiusDp).toFloat()
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun openLogin() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }
}
