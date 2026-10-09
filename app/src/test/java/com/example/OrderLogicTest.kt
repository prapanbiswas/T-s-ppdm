package com.example

import com.example.data.model.OrderStatus
import com.example.data.model.TShirtOrder
import com.example.util.CurrencyUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderLogicTest {

    @Test
    fun testAlphanumericOrderId() {
        val randomId = TShirtOrder.generateOrderId()
        assertEquals(7, randomId.length)
        assertTrue(TShirtOrder.isValidOrderId(randomId))

        val order1 = TShirtOrder(
            orderId = randomId,
            customerName = "Rahim",
            mobileNumber = "01712345678",
            unitPrice = 350.0
        )
        assertEquals(randomId, order1.orderId)
        assertEquals("#$randomId", order1.formattedSerial())
        assertTrue(order1.trackingUrl.contains(randomId))
    }

    @Test
    fun testMathematicalChecksumValidation() {
        // Generate multiple valid IDs and verify
        for (i in 0 until 20) {
            val id = TShirtOrder.generateOrderId()
            assertEquals(7, id.length)
            assertTrue("Generated ID $id should be valid", TShirtOrder.isValidOrderId(id))

            // Tamper with data character
            val tamperedData = if (id[0] == '2') '3' else '2'
            val tamperedId = tamperedData + id.substring(1)
            assertTrue("Tampered ID $tamperedId must be rejected", !TShirtOrder.isValidOrderId(tamperedId))

            // Tamper with check character
            val tamperedCheck = if (id[6] == 'Z') 'A' else 'Z'
            val tamperedCheckId = id.substring(0, 6) + tamperedCheck
            assertTrue("Tampered check digit $tamperedCheckId must be rejected", !TShirtOrder.isValidOrderId(tamperedCheckId))
        }

        // Invalid lengths or characters
        assertTrue(!TShirtOrder.isValidOrderId("7K2B9"))
        assertTrue(!TShirtOrder.isValidOrderId("7K2B9X45"))
        assertTrue(!TShirtOrder.isValidOrderId("1K2B9X0")) // Contains excluded '1' and '0'
    }

    @Test
    fun testQuantityAndAmountCalculation() {
        val order = TShirtOrder(
            orderId = "M49W2L",
            customerName = "Karim",
            mobileNumber = "01712345678",
            unitPrice = 350.0,
            qtyChild12 = 2,
            qtyS = 3,
            qtyM = 5,
            qtyL = 10,
            qtyXL = 4,
            qtyXXL = 1,
            qtyXXXL = 1
        )
        // 2 + 3 + 5 + 10 + 4 + 1 + 1 = 26
        assertEquals(26, order.totalQuantity)
        // 26 * 350.0 = 9100.0
        assertEquals(9100.0, order.totalAmount, 0.001)
    }

    @Test
    fun testBdtFormatting() {
        val formatted = CurrencyUtils.formatTaka(350.0)
        assertEquals("৳ 350", formatted)

        val formattedLarge = CurrencyUtils.formatTaka(9100.0)
        assertEquals("৳ 9,100", formattedLarge)

        val pdfFormat = CurrencyUtils.formatTakaPdf(350.0)
        assertEquals("Tk 350", pdfFormat)
    }

    @Test
    fun testTempleSmsAndTrackingUrl() {
        val id = TShirtOrder.generateOrderId()
        val order = TShirtOrder(
            orderId = id,
            customerName = "প্রীতম",
            mobileNumber = "01700000000",
            unitPrice = 350.0,
            qtyM = 2,
            trackingUrl = "https://podderpara.shop/?id=$id"
        )
        val sms = com.example.sms.SmsGatewayManager.generateBengaliSmsMessage(order)
        assertTrue(sms.contains("পোদ্দারপাড়া সর্বজনীন দুর্গা মন্দির"))
        assertTrue(sms.contains("https://podderpara.shop/?id=$id"))
        assertTrue(sms.contains("শুভ শারদীয়া!"))
    }

    @Test
    fun testManufacturingTotalsAndPdfBreakdown() {
        val order1 = TShirtOrder(
            orderId = "ABC1234",
            customerName = "Prapan",
            mobileNumber = "01711223344",
            unitPrice = 350.0,
            qtyL = 100,
            qtyM = 100,
            qtyXL = 50,
            status = OrderStatus.UNDER_PROCESSING
        )
        val order2 = TShirtOrder(
            orderId = "XYZ5678",
            customerName = "Sujoy",
            mobileNumber = "01899887766",
            unitPrice = 350.0,
            qtyXL = 50,
            qtyS = 30,
            qtyChild12 = 20,
            status = OrderStatus.PAID
        )
        val cancelledOrder = TShirtOrder(
            orderId = "CAN9999",
            customerName = "Cancelled Customer",
            mobileNumber = "01900112233",
            unitPrice = 350.0,
            qtyL = 50,
            qtyM = 50,
            status = OrderStatus.CANCELLED
        )

        val orders = listOf(order1, order2, cancelledOrder)

        // Calculate manufacturing production totals (excluding cancelled)
        var totalChild12 = 0
        var totalS = 0
        var totalM = 0
        var totalL = 0
        var totalXL = 0
        var totalXXL = 0
        var totalXXXL = 0
        var totalGarments = 0

        for (order in orders) {
            if (order.status != OrderStatus.CANCELLED) {
                totalChild12 += order.qtyChild12
                totalS += order.qtyS
                totalM += order.qtyM
                totalL += order.qtyL
                totalXL += order.qtyXL
                totalXXL += order.qtyXXL
                totalXXXL += order.qtyXXXL
                totalGarments += order.totalQuantity
            }
        }

        // Verify exact combined pieces needed for factory manufacturing
        assertEquals(100, totalL) // 100 pieces L size
        assertEquals(100, totalM) // 100 pieces M size
        assertEquals(100, totalXL) // 50 + 50 = 100 pieces XL size
        assertEquals(30, totalS)  // 30 pieces S size
        assertEquals(20, totalChild12) // 20 pieces Child size
        assertEquals(0, totalXXL)
        assertEquals(0, totalXXXL)
        assertEquals(350, totalGarments) // 100 + 100 + 100 + 30 + 20 = 350 total pieces to manufacture

        // Verify unmasked mobile number preserved for report
        assertEquals("01711223344", order1.mobileNumber)
        assertEquals("01899887766", order2.mobileNumber)
    }

    @Test
    fun testTenOrdersSubtotalManufacturingScenario() {
        // User scenario: 10 orders combining to 9 pieces L, 6 pieces M, etc.
        val orders = mutableListOf<TShirtOrder>()

        // 10 orders with individual sizes
        orders.add(TShirtOrder(orderId = "ORD001", customerName = "A", mobileNumber = "01710000001", unitPrice = 350.0, qtyL = 2, qtyM = 1, status = OrderStatus.PAID))
        orders.add(TShirtOrder(orderId = "ORD002", customerName = "B", mobileNumber = "01710000002", unitPrice = 350.0, qtyL = 1, qtyM = 2, status = OrderStatus.UNDER_PROCESSING))
        orders.add(TShirtOrder(orderId = "ORD003", customerName = "C", mobileNumber = "01710000003", unitPrice = 350.0, qtyL = 3, qtyM = 0, status = OrderStatus.PAID))
        orders.add(TShirtOrder(orderId = "ORD004", customerName = "D", mobileNumber = "01710000004", unitPrice = 350.0, qtyL = 0, qtyM = 1, status = OrderStatus.PAID))
        orders.add(TShirtOrder(orderId = "ORD005", customerName = "E", mobileNumber = "01710000005", unitPrice = 350.0, qtyL = 1, qtyM = 1, qtyXL = 2, status = OrderStatus.UNDER_PROCESSING))
        orders.add(TShirtOrder(orderId = "ORD006", customerName = "F", mobileNumber = "01710000006", unitPrice = 350.0, qtyL = 2, qtyM = 0, qtyS = 3, status = OrderStatus.PAID))
        orders.add(TShirtOrder(orderId = "ORD007", customerName = "G", mobileNumber = "01710000007", unitPrice = 350.0, qtyM = 1, qtyChild12 = 4, status = OrderStatus.PAID))
        orders.add(TShirtOrder(orderId = "ORD008", customerName = "H", mobileNumber = "01710000008", unitPrice = 350.0, qtyXL = 3, qtyXXL = 1, status = OrderStatus.DELIVERED))
        orders.add(TShirtOrder(orderId = "ORD009", customerName = "I", mobileNumber = "01710000009", unitPrice = 350.0, qtyXXXL = 2, status = OrderStatus.PAID))
        orders.add(TShirtOrder(orderId = "ORD010", customerName = "J", mobileNumber = "01710000010", unitPrice = 350.0, qtyS = 2, status = OrderStatus.UNDER_PROCESSING))

        assertEquals(10, orders.size)

        val totalL = orders.sumOf { it.qtyL }
        val totalM = orders.sumOf { it.qtyM }
        val totalXL = orders.sumOf { it.qtyXL }
        val totalS = orders.sumOf { it.qtyS }
        val totalXXL = orders.sumOf { it.qtyXXL }
        val totalXXXL = orders.sumOf { it.qtyXXXL }
        val totalChild12 = orders.sumOf { it.qtyChild12 }
        val grandTotalPieces = orders.sumOf { it.totalQuantity }

        // Exact counts as requested: L size was 9 piece, M size was 6 piece
        assertEquals(9, totalL) // 9 pieces L size
        assertEquals(6, totalM) // 6 pieces M size
        assertEquals(5, totalXL)
        assertEquals(5, totalS)
        assertEquals(1, totalXXL)
        assertEquals(2, totalXXXL)
        assertEquals(4, totalChild12)
        assertEquals(32, grandTotalPieces)

        // Verify individual order single row numbers
        val ord1 = orders[0]
        assertEquals("ORD001", ord1.orderId)
        assertEquals("A", ord1.customerName)
        assertEquals("01710000001", ord1.mobileNumber)
        assertEquals(2, ord1.qtyL)
        assertEquals(1, ord1.qtyM)
        assertEquals(0, ord1.qtyS)
        assertEquals(3, ord1.totalQuantity)
        assertEquals(OrderStatus.PAID, ord1.status)
    }
}

