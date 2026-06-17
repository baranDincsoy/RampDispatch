package com.example.rampdispatch.ui.fueling

/**
 * The ordered steps of the fueling process. Order matters: next()/previous()
 * move through this list by ordinal, so the sequence here IS the wizard flow.
 */
enum class FuelingStep {
    TAIL_VERIFY,    // confirm aircraft tail number matches the order
    EQUIPMENT,      // enter the cart / equipment number
    ARRIVAL,        // enter fuel already on board; app computes what's needed
    PUMPING,        // start the pump → order becomes IN_PROGRESS
    FINAL_READING,  // enter panel reading after fueling (target ± tolerance)
    CAP_CHECK,      // confirm the fuel cap is back on (physical safety step)
    TOTALIZER,      // enter cart totalizer; must reconcile within tolerance
    CLOSEOUT;       // enter employee id → order COMPLETED

    val isFirst: Boolean get() = ordinal == 0
    val isLast: Boolean get() = ordinal == entries.lastIndex

    fun next(): FuelingStep = if (isLast) this else entries[ordinal + 1]
    fun previous(): FuelingStep = if (isFirst) this else entries[ordinal - 1]
}