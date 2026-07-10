package com.delivce.kikapu.ui.util

import java.text.SimpleDateFormat
import java.util.Locale

fun formatKes(amount: Double): String = "KES ${String.format("%.0f", amount)}"

fun formatTime(millis: Long): String =
    SimpleDateFormat("h:mm a", Locale.getDefault()).format(millis)

fun formatDate(millis: Long): String =
    SimpleDateFormat("MMM d", Locale.getDefault()).format(millis)
