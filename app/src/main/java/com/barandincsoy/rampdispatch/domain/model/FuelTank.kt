package com.barandincsoy.rampdispatch.domain.model

/**
 * The fuel tanks of an aircraft. Which tanks exist depends on the aircraft
 * type, so we derive the list from aircraftType rather than storing it.
 * Boeing narrowbodies have 3 tanks; Airbus narrowbodies add two ACTs
 * (additional center tanks).
 */
enum class FuelTank(val label: String) {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right"),
    ACT1("ACT 1"),
    ACT2("ACT 2");

    companion object {
        private val MAIN_TANKS = listOf(LEFT, CENTER, RIGHT)
        private val AIRBUS_TANKS = MAIN_TANKS + listOf(ACT1, ACT2)

        /**
         * Tank layout for a given aircraft type code (e.g. "B738", "A321").
         * Airbus (A3xx) gets the two additional center tanks; everything
         * else falls back to the three main tanks.
         */
        fun forAircraftType(aircraftType: String): List<FuelTank> =
            if (aircraftType.startsWith("A3", ignoreCase = true)) AIRBUS_TANKS
            else MAIN_TANKS
    }
}