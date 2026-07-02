package com.delivce.kikapu.domain.model

data class Item(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val quantity: Int = 1,
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
