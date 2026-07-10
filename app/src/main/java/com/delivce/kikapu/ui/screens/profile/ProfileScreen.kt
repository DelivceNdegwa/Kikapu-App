package com.delivce.kikapu.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.delivce.kikapu.ui.foundation.RetroDefaults
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroFrame
import com.delivce.kikapu.ui.theme.AppColors

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {}
) {
    val user = viewModel.user
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val successRateTarget by viewModel.successRateTarget.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(RetroTheme.BackgroundColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = RetroTheme.TextColor
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .retroFrame(
                    borderColor = RetroTheme.BorderColor,
                    shadowColor = RetroTheme.ShadowColor
                )
                .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .retroFrame(
                        borderColor = RetroTheme.BorderColor,
                        shadowColor = RetroTheme.ShadowColor,
                        shape = CircleShape
                    )
                    .background(AppColors.Coral, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val name = user?.fullName
                if (name.isNullOrBlank()) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                } else {
                    Text(
                        text = initialsOf(name),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = (user?.fullName?.takeIf { it.isNotBlank() } ?: "KIKAPU USER").uppercase(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = RetroTheme.TextColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user?.email ?: "No email on file",
                style = MaterialTheme.typography.bodyMedium,
                color = RetroTheme.TextColor.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = RetroTheme.BorderColor.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStatChip(label = "UPCOMING", value = stats.upcomingTripsCount.toString())
                ProfileStatChip(label = "COMPLETED", value = stats.completedTripsCount.toString())
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor)
                .background(RetroTheme.SurfaceColor, RoundedCornerShape(RetroDefaults.CornerRadius))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "SUCCESS RATE GOAL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = RetroTheme.TextColor.copy(alpha = 0.6f)
            )
            Text(
                text = "The share of trips you want to stay within budget for",
                style = MaterialTheme.typography.labelSmall,
                color = RetroTheme.TextColor.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoalStepperButton(
                    label = "−",
                    onClick = { viewModel.setSuccessRateTarget(successRateTarget - 5) }
                )
                Text(
                    text = "$successRateTarget%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = AppColors.Coral
                )
                GoalStepperButton(
                    label = "+",
                    onClick = { viewModel.setSuccessRateTarget(successRateTarget + 5) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .retroFrame(
                    borderColor = RetroTheme.BorderColor,
                    shadowColor = RetroTheme.ShadowColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(AppColors.Coral, RoundedCornerShape(12.dp))
                .clickable { onLogout() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "LOG OUT",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun GoalStepperButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .retroFrame(borderColor = RetroTheme.BorderColor, shadowColor = RetroTheme.ShadowColor, shape = CircleShape)
            .background(RetroTheme.BackgroundColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = RetroTheme.TextColor
        )
    }
}

@Composable
private fun ProfileStatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = AppColors.Coral
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = RetroTheme.TextColor.copy(alpha = 0.5f)
        )
    }
}

private fun initialsOf(name: String): String =
    name.trim().split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
