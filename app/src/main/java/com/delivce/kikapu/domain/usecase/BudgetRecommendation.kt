package com.delivce.kikapu.domain.usecase

import com.delivce.kikapu.domain.model.Item
import com.delivce.kikapu.domain.model.isDueForRestock
import com.delivce.kikapu.domain.model.overdueDays

enum class BudgetStrategy(val label: String) {
    PRIORITIZE_PRIORITY("PRIORITY"),
    PRIORITIZE_CHEAPNESS("AFFORDABILITY")
}

/**
 * Picks which catalog items to recommend for a trip's remaining budget.
 *
 * By default only items actually due for restock are candidates — an item nobody needs yet
 * shouldn't get bought just because it's cheap or high priority. Setting [includeNonDue] widens
 * the candidate pool to every catalog item; not-yet-due items score zero on urgency, so they only
 * get picked once genuinely due items are exhausted or budget is left over — they act as bonus
 * fill-ins, not replacements for what's actually needed.
 *
 * Candidates are ranked by a score blending priority and how overdue they are, then greedily
 * packed into the budget, skipping (not stopping at) anything that doesn't fit so smaller/cheaper
 * items further down the ranking still get a chance. That greedy-skip step is what keeps the
 * result to "many affordable items" rather than "one expensive item that eats the whole budget" —
 * the exact failure mode called out when this was speced.
 *
 * [BudgetStrategy.PRIORITIZE_CHEAPNESS] additionally ranks by score-per-currency-unit (value
 * density), which packs in more items for the same budget than ranking on score alone.
 *
 * Note: `estimatedPrice` is the total cost of an item's `quantity` (e.g. "5 for 300", not "60
 * each"), so it's used as-is here rather than multiplied by quantity.
 */
fun recommendItemsForBudget(
    catalogItems: List<Item>,
    budget: Double,
    strategy: BudgetStrategy,
    includeNonDue: Boolean = false,
    now: Long = System.currentTimeMillis()
): List<Item> {
    val due = catalogItems.filter {
        (includeNonDue || it.isDueForRestock(now)) && it.estimatedPrice > 0
    }
    if (due.isEmpty() || budget <= 0) return emptyList()

    val maxOverdueDays = due.maxOf { it.overdueDays(now) }.coerceAtLeast(1L).toDouble()
    val maxPrice = due.maxOf { it.estimatedPrice }.coerceAtLeast(1.0)

    fun score(item: Item): Double {
        val priorityScore = item.priorityIndex / 5.0
        val urgencyScore = item.overdueDays(now) / maxOverdueDays
        val baseValue = priorityScore * 0.65 + urgencyScore * 0.35
        return when (strategy) {
            BudgetStrategy.PRIORITIZE_PRIORITY -> baseValue
            BudgetStrategy.PRIORITIZE_CHEAPNESS -> {
                val normalizedPrice = (item.estimatedPrice / maxPrice).coerceIn(0.05, 1.0)
                baseValue / normalizedPrice
            }
        }
    }

    val ranked = due.sortedByDescending(::score)
    val selected = mutableListOf<Item>()
    var runningTotal = 0.0
    for (item in ranked) {
        if (runningTotal + item.estimatedPrice <= budget) {
            selected += item
            runningTotal += item.estimatedPrice
        }
    }
    return selected
}
