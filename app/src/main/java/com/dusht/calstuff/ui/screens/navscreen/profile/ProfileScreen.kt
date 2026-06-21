package com.dusht.calstuff.ui.screens.navscreen.profile

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dusht.calstuff.ui.theme.FontSize

/** Warm amber-orange palette for the header */
private val HeaderGradientStart = Color(0xFFFF9A56)
private val HeaderGradientEnd = Color(0xFFFF6F3C)

private val AvatarBackground = Color(0xFFFFE0CC)
private val AvatarForeground = Color(0xFFFF6F3C)

private val SectionIconTint = Color(0xFFFF8C50)
private val LogoutRed = Color(0xFFF85B4E)
private val SubtextColor = Color(0xFF9E9E9E)
private val DarkText = Color(0xFF1C1B1F)

/** Avatars: simple emoji faces for a random pick */
private val avatarEmojis = listOf(
    "\uD83D\uDE0A", // smiling
    "\uD83D\uDE0E", // sunglasses
    "\uD83E\uDDD1\u200D\uD83D\uDCBB", // technologist
    "\uD83D\uDE04", // grinning
    "\uD83E\uDD29", // star-struck
    "\uD83E\uDDD1\u200D\uD83C\uDF73", // cook
)

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val randomEmoji = remember { avatarEmojis.random() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ProfileHeader(
                memberSinceText = state.memberSinceText,
                avatarEmoji = randomEmoji,
            )

            Spacer(modifier = Modifier.height(20.dp))

            ProfileSectionsCard(
                onLogoutClick = { viewModel.handleEvent(ProfileEvent.LogoutClicked) },
            )

            // Bottom spacing for nav bar
            Spacer(modifier = Modifier.height(120.dp))
        }

        if (state.showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = { viewModel.handleEvent(ProfileEvent.LogoutConfirmed) },
                onDismiss = { viewModel.handleEvent(ProfileEvent.LogoutDismissed) },
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    memberSinceText: String,
    avatarEmoji: String,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(HeaderGradientStart, HeaderGradientEnd),
                    start = Offset.Zero,
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                ),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
            )
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        // Subtle decorative circle in background
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = size.minDimension * 0.55f,
                center = Offset(size.width * 0.85f, size.height * 0.2f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: title + subtitle
            Column {
                Text(
                    text = "Profile",
                    fontSize = FontSize.heading,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Member since $memberSinceText",
                    fontSize = FontSize.small,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }

            // Right: circular avatar
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AvatarBackground),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = avatarEmoji,
                    fontSize = FontSize.heading,
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionsCard(
    onLogoutClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            ProfileSectionItem(
                icon = Icons.Default.Person,
                title = "Personal Info",
                subtitle = "Name, age, goals",
                iconTint = SectionIconTint,
                onClick = { /* TODO: navigate to personal info */ },
            )

            SectionDivider()

            ProfileSectionItem(
                icon = Icons.Default.Info,
                title = "About CalStuff",
                subtitle = "Version, licenses, credits",
                iconTint = SectionIconTint,
                onClick = { /* TODO: navigate to about */ },
            )

            SectionDivider()

            ProfileSectionItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Log Out",
                subtitle = "Sign out of your account",
                iconTint = LogoutRed,
                titleColor = LogoutRed,
                showChevron = false,
                onClick = onLogoutClick,
            )
        }
    }
}

@Composable
private fun ProfileSectionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    titleColor: Color = DarkText,
    showChevron: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = FontSize.body,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
            )
            Text(
                text = subtitle,
                fontSize = FontSize.xSmall,
                color = SubtextColor,
            )
        }

        if (showChevron) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = SubtextColor,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = 0.5.dp,
        color = Color(0xFFEEEEEE),
    )
}

@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Log Out",
                fontWeight = FontWeight.Bold,
                color = DarkText,
            )
        },
        text = {
            Text(
                text = "Are you sure you want to log out of CalStuff?",
                fontSize = FontSize.medium,
                color = SubtextColor,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Yes, Log Out",
                    color = LogoutRed,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = DarkText,
                )
            }
        },
    )
}
