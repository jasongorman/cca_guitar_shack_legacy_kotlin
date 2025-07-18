package com.guitarshack

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class StockMonitorTest {

    @Test
    fun `alert is sent when product hits restock level`() {
        val alert = mock<Alert>()
        StockMonitor(alert).productSold(811, 30)
        verify(alert).send(any())
    }

    @Test
    fun `no alert is sent when product stays above restock level`() {
        val alert = mock<Alert>()
        StockMonitor(alert).productSold(811, 20)
        verify(alert, never()).send(any())
    }
}
