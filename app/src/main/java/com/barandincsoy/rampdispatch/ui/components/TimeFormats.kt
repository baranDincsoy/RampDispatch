package com.barandincsoy.rampdispatch.ui.components

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val hhmm = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

/** Instant (absolute UTC time) -> local wall-clock text, e.g. "18:45". */
fun Instant.asClockText(): String = hhmm.format(this)

/** Duration -> compact countdown, e.g. "32m", "1h 05m", "-12m" when past due. */
fun Duration.asCountdownText(): String {
    val totalMin = toMinutes()
    val sign = if (totalMin < 0) "-" else ""
    val abs = kotlin.math.abs(totalMin)
    val h = abs / 60
    val m = abs % 60
    return if (h > 0) "$sign${h}h %02dm".format(m) else "$sign${m}m"
}