package com.delivce.kikapu.ui.screens.trips

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.delivce.kikapu.domain.model.TripStatus
import com.delivce.kikapu.ui.components.EmptyStateIllustration
import com.delivce.kikapu.ui.components.TripRowCard
import com.delivce.kikapu.ui.foundation.RetroDefaults
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroFrame
import com.delivce.kikapu.ui.theme.AppColors

@Composable
fun TripPlanScreen(
    modifier: Modifier = Modifier,
    viewModel: TripsViewModel = hiltViewModel(),
    onCreateTrip: () -> Unit = {},
    onTripClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.onEvent(TripsEvent.ClearError)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(RetroTheme.BackgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Trips",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = RetroTheme.TextColor
                )
                Text(
                    text = "${uiState.filteredTrips.size} RECORDS",
                    style = MaterialTheme.typography.labelMedium,
                    color = RetroTheme.TextColor.copy(alpha = 0.5f)
                )
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        label = "ALL",
                        selected = uiState.filterStatus == null,
                        onClick = { viewModel.onEvent(TripsEvent.FilterByStatus(null)) }
                    )
                }
                items(TripStatus.entries.toList()) { status ->
                    FilterChip(
                        label = status.name,
                        selected = uiState.filterStatus == status,
                        onClick = { viewModel.onEvent(TripsEvent.FilterByStatus(status)) }
                    )
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        label = "DATE ↓",
                        selected = uiState.sortOrder == SortOrder.DATE_DESC,
                        onClick = { viewModel.onEvent(TripsEvent.SortBy(SortOrder.DATE_DESC)) }
                    )
                }
                item {
                    FilterChip(
                        label = "DATE ↑",
                        selected = uiState.sortOrder == SortOrder.DATE_ASC,
                        onClick = { viewModel.onEvent(TripsEvent.SortBy(SortOrder.DATE_ASC)) }
                    )
                }
                item {
                    FilterChip(
                        label = "BUDGET ↓",
                        selected = uiState.sortOrder == SortOrder.BUDGET_HIGH,
                        onClick = { viewModel.onEvent(TripsEvent.SortBy(SortOrder.BUDGET_HIGH)) }
                    )
                }
                item {
                    FilterChip(
                        label = "BUDGET ↑",
                        selected = uiState.sortOrder == SortOrder.BUDGET_LOW,
                        onClick = { viewModel.onEvent(TripsEvent.SortBy(SortOrder.BUDGET_LOW)) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.Coral)
                    }
                }
                uiState.filteredTrips.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyStateIllustration(caption = "[ NO TRIPS FOUND ]")
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.filteredTrips, key = { it.id }) { trip ->
                            TripRowCard(trip = trip, onClick = { onTripClick(trip.id) })
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(56.dp)
                .retroFrame(
                    borderColor = RetroTheme.BorderColor,
                    shadowColor = RetroTheme.ShadowColor,
                    shape = RoundedCornerShape(28.dp)
                )
                .background(AppColors.Coral, RoundedCornerShape(28.dp))
                .clickable { onCreateTrip() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .retroFrame(
                borderColor = RetroTheme.BorderColor,
                shadowColor = RetroTheme.ShadowColor,
                shape = RoundedCornerShape(10.dp),
                thickness = 1.5.dp
            )
            .background(
                if (selected) AppColors.Coral else RetroTheme.SurfaceColor,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else RetroTheme.TextColor
        )
    }
}
