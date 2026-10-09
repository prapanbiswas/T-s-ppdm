package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("পোদ্দারপাড়া দুর্গা মন্দির", appName)
  }

  @Test
  fun `verify order deserialization from Firebase Realtime Database map`() {
    val map = mapOf<String, Any?>(
      "orderId" to "7K2B9X4",
      "customerName" to "সুজন মণ্ডল",
      "mobileNumber" to "01711000000",
      "unitPrice" to 250.0,
      "totalQuantity" to 3,
      "totalAmount" to 750.0,
      "status" to "UNDER_PROCESSING",
      "smsStatus" to "SENT",
      "trackingUrl" to "https://podderpara.shop/?id=7K2B9X4",
      "qtyChild12" to 1,
      "qtyS" to 0,
      "qtyM" to 1,
      "qtyL" to 1,
      "qtyXL" to 0,
      "qtyXXL" to 0,
      "qtyXXXL" to 0
    )

    val order = com.example.data.model.TShirtOrder.fromMap(map)
    org.junit.Assert.assertNotNull(order)
    assertEquals("7K2B9X4", order?.orderId)
    assertEquals("সুজন মণ্ডল", order?.customerName)
    assertEquals(3, order?.totalQuantity)
    assertEquals(750.0, order?.totalAmount ?: 0.0, 0.01)
    assertEquals(com.example.data.model.SyncStatus.SYNCED, order?.syncStatus)
  }

  @Test
  fun `verify orderId checksum generation and validation`() {
    val generatedId = com.example.data.model.TShirtOrder.generateOrderId()
    assertEquals(7, generatedId.length)
    org.junit.Assert.assertTrue(com.example.data.model.TShirtOrder.isValidOrderId(generatedId))
  }
}
