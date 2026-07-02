package com.delivce.kikapu.domain.model

data class ShoppingItem(
    val id: String = "",
    val tripId: String = "",
    val name: String = "",
    val estimatedPrice: Double = 0.0,
    val actualPrice: Double = 0.0,
    val quantity: Int = 1,
    val priorityIndex: Int = 3, // 1-5, higher = more important
    val isChecked: Boolean = false,
    val isOutOfBudget: Boolean = false,
    val category: String? = null,
    val isCustom: Boolean = false,
    val catalogItemId: String? = null
)
