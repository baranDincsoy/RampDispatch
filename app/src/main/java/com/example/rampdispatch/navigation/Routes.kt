package com.example.rampdispatch.navigation

/**
 * Single place that defines every destination in the app.
 * Screens never hardcode route strings — they use these helpers,
 * so a typo in a route becomes a compile error, not a runtime crash.
 */
object Routes {
    const val BOARD = "board"
    const val ORDER_DETAIL = "order_detail/{orderId}"

    fun orderDetail(orderId: String) = "order_detail/$orderId"
}