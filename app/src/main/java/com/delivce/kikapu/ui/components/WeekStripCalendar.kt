package com.delivce.kikapu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val DAY_MILLIS = 24L * 60 * 60 * 1000

private fun startOfWeek(dateMillis: Long): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = dateMillis
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val firstDayOffset = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday-first week
    cal.add(Calendar.DAY_OF_YEAR, -firstDayOffset)
    return cal.timeInMillis
}

/** A horizontal week strip date picker, styled after the calendar-reference design. */
@Composable
fun WeekStripCalendar(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var weekStart by rememberSaveable { mutableLongStateOf(startOfWeek(selectedDate)) }
    val headerFormatter = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()) }
    val dayNameFormatter = remember { SimpleDateFormat("EEE", Locale.getDefault()) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = headerFormatter.format(Date(selectedDate)).uppercase(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = RetroTheme.TextColor
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable { weekStart -= 7 * DAY_MILLIS },
                contentAlignment = Alignment.Center
            ) {
                Text("‹", style = MaterialTheme.typography.titleLarge, color = RetroTheme.TextColor.copy(alpha = 0.5f))
            }

            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayIndex in 0..6) {
                    val dayMillis = weekStart + dayIndex * DAY_MILLIS
                    val isSelected = isSameDay(dayMillis, selectedDate)
                    val cal = Calendar.getInstance().apply { timeInMillis = dayMillis }
                    DayCell(
                        dayName = dayNameFormatter.format(Date(dayMillis)).take(3).uppercase(),
                        dayNumber = cal.get(Calendar.DAY_OF_MONTH).toString(),
                        isSelected = isSelected,
                        onClick = { onDateSelected(mergeDateKeepingTime(dayMillis, selectedDate)) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clickable { weekStart += 7 * DAY_MILLIS },
                contentAlignment = Alignment.Center
            ) {
                Text("›", style = MaterialTheme.typography.titleLarge, color = RetroTheme.TextColor.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun DayCell(dayName: String, dayNumber: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelSmall,
            color = RetroTheme.TextColor.copy(alpha = 0.45f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(if (isSelected) AppColors.Coral else Color.Transparent, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayNumber,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else RetroTheme.TextColor
            )
        }
    }
}

private fun isSameDay(a: Long, b: Long): Boolean {
    val calA = Calendar.getInstance().apply { timeInMillis = a }
    val calB = Calendar.getInstance().apply { timeInMillis = b }
    return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
        calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
}

private fun mergeDateKeepingTime(newDayMillis: Long, existingMillis: Long): Long {
    val newDay = Calendar.getInstance().apply { timeInMillis = newDayMillis }
    val existing = Calendar.getInstance().apply { timeInMillis = existingMillis }
    newDay.set(Calendar.HOUR_OF_DAY, existing.get(Calendar.HOUR_OF_DAY))
    newDay.set(Calendar.MINUTE, existing.get(Calendar.MINUTE))
    newDay.set(Calendar.SECOND, existing.get(Calendar.SECOND))
    return newDay.timeInMillis
}
