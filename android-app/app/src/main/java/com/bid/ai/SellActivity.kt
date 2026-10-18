
package com.bid.ai

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bid.ai.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class SellActivity : ComponentActivity() {

    private lateinit var categoryGrid: GridLayout
    private lateinit var photosContainer: LinearLayout
    private lateinit var photoCount: TextView
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var uploadButton: Button

    private lateinit var brandInput: EditText
    private lateinit var productTypeInput: EditText
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var priceInput: EditText
    private lateinit var locationInput: EditText
    private lateinit var sellerTypeGroup: RadioGroup

    private var selectedCategory: String? = null
    private val selectedPhotos = mutableListOf<Uri>()

    private val categories = listOf(
        "Cars" to "🚙",
        "Real Estate" to "🏠",
        "Mobiles" to "📱",
        "Jobs" to "💼",
        "Bikes" to "🏍️",
        "Electronics" to "🖥️",
        "Home & Garden" to "🪑",
        "Beauty" to "👜",
        "Clothing" to "👕",
        "Books" to "📚",
        "Arts and Crafts" to "🎨",
        "Services" to "🛠️",
        "General Service" to "⚙️",
        "Yodha" to "🌾",
        "Agriculture" to "🚜"
    )

    private val photoPicker =
        registerForActivityResult(
            ActivityResultContracts.GetMultipleContents()
        ) { uris ->
            selectedPhotos.clear()
            selectedPhotos.addAll(uris.take(5))
            renderPhotos()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell)

        categoryGrid = findViewById(R.id.sellCategoryGrid)
        photosContainer = findViewById(R.id.selectedPhotosContainer)
        photoCount = findViewById(R.id.photoCount)
        statusText = findViewById(R.id.sellStatus)
        progressBar = findViewById(R.id.sellProgress)
        uploadButton = findViewById(R.id.uploadProductButton)

        brandInput = findViewById(R.id.brandInput)
        productTypeInput = findViewById(R.id.productTypeInput)
        titleInput = findViewById(R.id.titleInput)
        descriptionInput = findViewById(R.id.descriptionInput)
        priceInput = findViewById(R.id.priceInput)
        locationInput = findViewById(R.id.locationInput)
        sellerTypeGroup = findViewById(R.id.sellerTypeGroup)

        buildCategories()

        findViewById<TextView>(R.id.backButton).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.selectPhotosButton)
            .setOnClickListener {
                photoPicker.launch("image/*")
            }

        uploadButton.setOnClickListener {
            submitProduct()
        }

        findViewById<TextView>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun buildCategories() {
        categoryGrid.removeAllViews()
        categoryGrid.columnCount = 4

        categories.forEach { (name, emoji) ->
            val item = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(2), dp(2), dp(2), dp(2))
            }

            val circle = FrameLayout(this).apply {
                background = categoryBackground(false)
            }

            val icon = TextView(this).apply {
                text = emoji
                gravity = Gravity.CENTER
                textSize = 29f
            }

            circle.addView(
                icon,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
            )

            val label = TextView(this).apply {
                text = name
                gravity = Gravity.CENTER
                textSize = 9f
                setTextColor(Color.BLACK)
                maxLines = 2
            }

            item.addView(circle, LinearLayout.LayoutParams(dp(54), dp(54)))
            item.addView(
                label,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(28)
                )
            )

            item.setOnClickListener {
                selectedCategory = name
                updateCategorySelection()
            }

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = dp(82)
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(2), dp(1), dp(2), dp(1))
            }

            categoryGrid.addView(item, params)
        }
    }

    private fun updateCategorySelection() {
        for (i in 0 until categoryGrid.childCount) {
            val item = categoryGrid.getChildAt(i) as LinearLayout
            val name = categories[i].first
            item.getChildAt(0).background =
                categoryBackground(name == selectedCategory)
        }
    }

    private fun categoryBackground(selected: Boolean) =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.rgb(242, 242, 242))
            setStroke(
                dp(if (selected) 1 else 1),
                if (selected) Color.rgb(8, 104, 232)
                else Color.rgb(235, 235, 235)
            )
        }

    private fun renderPhotos() {
        photosContainer.removeAllViews()

        selectedPhotos.forEach { uri ->
            val image = ImageView(this).apply {
                setImageURI(uri)
                scaleType = ImageView.ScaleType.CENTER_CROP
                background = categoryBackground(false)
                clipToOutline = true
            }

            val params = LinearLayout.LayoutParams(dp(48), dp(48)).apply {
                setMargins(dp(3), dp(2), dp(3), dp(2))
            }

            photosContainer.addView(image, params)
        }

        photoCount.text =
            "${selectedPhotos.size}/5 photos selected (minimum 2 recommended)"
    }

    private fun submitProduct() {
        val category = selectedCategory
        val brand = brandInput.text.toString().trim()
        val productType = productTypeInput.text.toString().trim()
        val title = titleInput.text.toString().trim()
        val description = descriptionInput.text.toString().trim()
        val priceText = priceInput.text.toString().trim()
        val location = locationInput.text.toString().trim()

        val price = priceText.toDoubleOrNull()

        val error = when {
            category == null -> "Please select a category."
            brand.isBlank() -> "Please enter the brand."
            productType.isBlank() -> "Please enter the product type."
            title.isBlank() -> "Please enter an ad title."
            description.isBlank() -> "Please enter a description."
            price == null || price <= 0 -> "Please enter a valid price."
            location.isBlank() -> "Please enter a location."
            else -> null
        }

        if (error != null) {
            showStatus(error)
            return
        }

        val token = SessionManager(this).getToken()
        if (token.isNullOrBlank()) {
            showStatus("Your session has expired. Please log in again.")
            return
        }

        val sellerType = if (
            sellerTypeGroup.checkedRadioButtonId == R.id.individualRadio
        ) {
            "individual"
        } else {
            "vendor"
        }

        val payload = JSONObject().apply {
            put("seller_type", sellerType)
            put("brand", brand)
            put("category", category)
            put("product_type", productType)
            put("title", title)
            put("description", description)
            put("price", price)
            put("location", location)
            // The selected images are local device URIs. They need an
            // image-upload service before they can be stored as public URLs.
            put("images", JSONArray())
        }

        setLoading(true)
        statusText.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    postProduct(payload, token)
                }

                if (result.success) {
                    val approved = result.approvalStatus == "approved"
                    showResultDialog(approved)
                } else {
                    showStatus(result.message)
                }
            } catch (e: Exception) {
                showStatus(
                    "Could not connect to the server. Check that your backend is running."
                )
            } finally {
                setLoading(false)
            }
        }
    }

    private data class SubmitResult(
        val success: Boolean,
        val message: String,
        val approvalStatus: String?
    )

    private fun postProduct(
        payload: JSONObject,
        token: String
    ): SubmitResult {
        val url = URL("${BuildConfig.API_BASE_URL}api/products")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 15000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        return try {
            connection.outputStream.use { output ->
                output.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val status = connection.responseCode
            val stream = if (status in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = try {
                JSONObject(body)
            } catch (_: Exception) {
                JSONObject()
            }

            val product = json.optJSONObject("product")
                ?: json.optJSONObject("data")
                ?: json

            val approval = product.optString(
                "approval_status",
                json.optString("approval_status", "")
            ).takeIf { it.isNotBlank() }

            if (status in 200..299) {
                SubmitResult(
                    success = true,
                    message = json.optString(
                        "message",
                        "Product submitted successfully."
                    ),
                    approvalStatus = approval
                )
            } else {
                SubmitResult(
                    success = false,
                    message = json.optString(
                        "message",
                        "Product upload failed (HTTP $status)."
                    ),
                    approvalStatus = approval
                )
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun showResultDialog(approved: Boolean) {
        val title = if (approved) "Congratulations!" else "Product submitted"
        val message = if (approved) {
            "Your product is approved and live now."
        } else {
            "Your product was submitted successfully and is awaiting approval."
        }

        AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_info)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(true)
            .show()
    }

    private fun showStatus(message: String) {
        statusText.text = message
        statusText.setTextColor(Color.rgb(190, 40, 40))
        statusText.visibility = View.VISIBLE
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        uploadButton.isEnabled = !loading
        uploadButton.text = if (loading) "Uploading..." else "Upload"
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()
}
