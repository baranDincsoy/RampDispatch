package com.example.rampdispatch.ui.fueling

/**
 * Accumulates everything the fueler enters during the wizard.
 * Held in memory only; persisted to Room just once, at close-out.
 * Every field is nullable/blank initially and filled step by step.
 */
data class FuelingData(
    val enteredTail: String = "",
    val equipmentNumber: String = "",
    val arrivalLbs: Int? = null,
    val finalLbs: Int? = null,
    val capConfirmed: Boolean = false,
    val totalizerLbs: Int? = null,
    val employeeId: String = ""
)