package com.delivce.kikapu.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.delivce.kikapu.domain.model.TripStatus
import com.delivce.kikapu.ui.components.EmptyStateIllustration
import com.delivce.kikapu.ui.components.RetroSavingsCard
import com.delivce.kikapu.ui.components.RetroStatCard
import com.delivce.kikapu.ui.components.TripRowCard
import com.delivce.kikapu.ui.components.SuccessGreen
import com.delivce.kikapu.ui.components.toColor
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.theme.AppColors

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onViewAllTrips: () -> Unit = {},
    onTripClick: (String) -> Unit = {}
) {
    val stats by viewModel.summaryStats.collectAsStateWithLifecycle()
    val recentTrips by viewModel.recentTrips.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(RetroTheme.BackgroundColor),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Hi ${viewModel.userName}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = RetroTheme.TextColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ready to shop?",
                    style = MaterialTheme.typography.labelMedium,
                    color = RetroTheme.TextColor.copy(alpha = 0.5f)
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RetroStatCard(
                        label = "UPCOMING",
                        value = stats.upcomingTripsCount.toString(),
                        icon = Icons.Filled.Schedule,
                        backgroundColor = TripStatus.UPCOMING.toColor(),
                        modifier = Modifier.weight(1f)
                    )
                    RetroStatCard(
                        label = "COMPLETED",
                        value = stats.completedTripsCount.toString(),
                        icon = Icons.Filled.TaskAlt,
                        backgroundColor = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RetroStatCard(
                        label = "ITEMS DUE",
                        value = stats.itemsDueCount.toString(),
                        icon = Icons.Filled.Inventory2,
                        backgroundColor = AppColors.Amber,
                        modifier = Modifier.weight(1f)
                    )
                    RetroSavingsCard(
                        amount = stats.totalSavedOrOverspent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT TRIPS",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RetroTheme.TextColor
                )
                Text(
                    text = "VIEW ALL →",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Coral,
                    modifier = Modifier.clickable { onViewAllTrips() }
                )
            }
        }

        if (recentTrips.isEmpty()) {
            item {
                EmptyStateIllustration(caption = "[ NO TRIPS YET — PLAN YOUR FIRST ONE ]")
            }
        } else {
            items(recentTrips, key = { it.id }) { trip ->
                TripRowCard(trip = trip, onClick = { onTripClick(trip.id) })
            }
        }
    }
}
