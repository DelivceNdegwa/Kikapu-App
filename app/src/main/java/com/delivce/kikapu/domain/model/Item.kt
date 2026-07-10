package com.delivce.kikapu.domain.model

data class Item(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val estimatedPrice: Double = 0.0,
    val priorityIndex: Int = 3, // 1-5, higher = more important
    val durationDays: Int = 30,
    val lastShoppedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

private const val DAY_MILLIS = 24L * 60 * 60 * 1000

/** Null until the item has been shopped at least once. */
fun Item.nextRestockAt(): Long? = lastShoppedAt?.plus(durationDays * DAY_MILLIS)

/** Only ever true once a restock cycle has actually elapsed — a never-shopped item isn't "due". */
fun Item.isDueForRestock(now: Long = System.currentTimeMillis()): Boolean =
    nextRestockAt()?.let { now >= it } ?: false

/** 0f..1f progress through the current restock cycle; null if never shopped. */
fun Item.restockProgress(now: Long = System.currentTimeMillis()): Float? {
    val last = lastShoppedAt ?: return null
    val cycle = durationDays * DAY_MILLIS
    if (cycle <= 0) return 1f
    return ((now - last).toFloat() / cycle).coerceIn(0f, 1f)
}

/** Whole days past the restock deadline; 0 if not yet due or never shopped. */
fun Item.overdueDays(now: Long = System.currentTimeMillis()): Long {
    val next = nextRestockAt() ?: return 0L
    return ((now - next) / DAY_MILLIS).coerceAtLeast(0L)
}
