package com.guitarshack

import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.*

class StockMonitor(private val alert: Alert) {

    private val client = OkHttpClient()
    private val gson = Gson()

    fun productSold(productId: Int, quantity: Int) {
        val productUrl = buildUrl(
            "https://6hr1390c1j.execute-api.us-east-2.amazonaws.com/default/product",
            mapOf("id" to productId)
        )

        val productJson = getJson(productUrl)
        val product = gson.fromJson(productJson, Product::class.java)

        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        calendar.add(Calendar.DATE, -30)
        val startDate = calendar.time

        val format = SimpleDateFormat("M/d/yyyy")
        val salesUrl = buildUrl(
            "https://gjtvhjg8e9.execute-api.us-east-2.amazonaws.com/default/sales",
            mapOf(
                "productId" to product.id,
                "startDate" to format.format(startDate),
                "endDate" to format.format(endDate),
                "action" to "total"
            )
        )

        val salesJson = getJson(salesUrl)
        val total = gson.fromJson(salesJson, SalesTotal::class.java)

        val dailyAverage = total.total / 30.0
        val reorderLevel = (dailyAverage * product.leadTime).toInt()

        if (product.stock - quantity <= reorderLevel) {
            alert.send(product)
        }
    }

    private fun getJson(url: String): String {
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Unexpected code $response")
            }
            return response.body?.string() ?: ""
        }
    }

    private fun buildUrl(base: String, params: Map<String, Any>): String {
        val paramString = params.entries.joinToString("&") { "${it.key}=${it.value}" }
        return "$base?$paramString"
    }
}
