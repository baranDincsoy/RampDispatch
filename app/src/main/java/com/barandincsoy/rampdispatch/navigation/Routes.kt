package com.barandincsoy.rampdispatch.navigation

/**
 * Single place that defines every destination in the app.
 * Screens never hardcode route strings — they use these helpers,
 * so a typo in a route becomes a compile error, not a runtime crash.
 */
object Routes {
    const val BOARD = "board"
    const val STATS = "stats"
    const val ORDER_DETAIL = "order_detail/{orderId}"
    const val FUELING_WIZARD = "fueling_wizard/{orderId}"

    fun fuelingWizard(orderId: String) = "fueling_wizard/$orderId"

    fun orderDetail(orderId: String) = "order_detail/$orderId"
}