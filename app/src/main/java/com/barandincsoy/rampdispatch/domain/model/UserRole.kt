package com.barandincsoy.rampdispatch.domain.model

enum class UserRole {
    TEAM_LEADER,   // plans work: assign / reassign / unassign, sees all orders
    FUELER         // does work: sees only own orders, runs the fueling flow
}