package com.dusht.calstuff.ui.screens.navscreen.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dusht.calstuff.ui.theme.calStuffColors
import com.dusht.calstuff.ui.theme.FontSize
import com.dusht.calstuff.vm.ThemeViewModel
import com.dusht.shared.session.ThemeMode

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToLogin: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                ProfileEffect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.calStuffColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ProfileHeader(
                userName = state.userName,
                memberSinceText = state.memberSinceText,
                onBack = onBack,
            )

            Spacer(modifier = Modifier.height(20.dp))

            ThemeSectionCard(
                themeMode = themeMode,
                onThemeModeSelected = themeViewModel::setThemeMode,
            )

            Spacer(modifier = Modifier.height(16.dp))

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

/** First letters of up to the first two words in [name], e.g. "Ada Lovelace" -> "AL". */
private fun initialsFor(name: String): String {
    val words = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        words.size >= 2 -> "${words[0].first()}${words[1].first()}".uppercase()
        words.size == 1 -> words[0].take(1).uppercase()
        else -> ""
    }
}

@Composable
private fun ProfileHeader(
    userName: String,
    memberSinceText: String,
    onBack: () -> Unit,
) {
    val colors = MaterialTheme.calStuffColors
    val headerShape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(headerShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(colors.gradientStart, colors.gradientEnd),
                    start = Offset.Zero,
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                ),
            ),
    ) {
        // Decorative blurred color blobs. Wrapped in matchParentSize() so their explicit
        // size/offset never grows the header itself — a plain child Box sized e.g. 180.dp
        // forces its Box parent to be at least that tall, even if later offset elsewhere.
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-50).dp)
                    .background(colors.onGradient.copy(alpha = 0.35f), CircleShape)
                    .blur(radius = 60.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-40).dp, y = 40.dp)
                    .background(colors.onGradient.copy(alpha = 0.2f), CircleShape)
                    .blur(radius = 45.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded),
            )
        }

        // Frosted glass veil: a translucent white wash + edge highlight over the color and
        // blobs above — this milky layer is what actually reads as "glass" rather than a
        // flat color card with some blurry shapes on it.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(colors.onGradient.copy(alpha = 0.14f))
                .border(width = 1.dp, color = colors.onGradient.copy(alpha = 0.45f), shape = headerShape),
        )

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.onGradient,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                        color = colors.onGradient,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Member since $memberSinceText",
                        fontSize = FontSize.small,
                        fontWeight = FontWeight.Medium,
                        color = colors.onGradientMuted,
                    )
                }

                // Right: circular avatar with initials (falls back to a person icon)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(colors.accentSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    val initials = initialsFor(userName)
                    if (initials.isNotEmpty()) {
                        Text(
                            text = initials,
                            fontSize = FontSize.large,
                            fontWeight = FontWeight.Bold,
                            color = colors.onAccentSoft,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = colors.onAccentSoft,
                            modifier = Modifier.size(32.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSectionCard(
    themeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
) {
    val colors = MaterialTheme.calStuffColors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        ProfileThemeSelectorItem(
            icon = Icons.Default.DarkMode,
            title = "Theme",
            themeMode = themeMode,
            iconTint = colors.accent,
            onThemeModeSelected = onThemeModeSelected,
        )
    }
}

@Composable
private fun ProfileSectionsCard(
    onLogoutClick: () -> Unit,
) {
    val colors = MaterialTheme.calStuffColors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            ProfileSectionItem(
                icon = Icons.Default.Person,
                title = "Personal Info",
                subtitle = "Name, age, goals",
                iconTint = colors.accent,
                onClick = { /* TODO: navigate to personal info */ },
            )

            SectionDivider()

            ProfileSectionItem(
                icon = Icons.Default.Info,
                title = "About CalStuff",
                subtitle = "Version, licenses, credits",
                iconTint = colors.accent,
                onClick = { /* TODO: navigate to about */ },
            )

            SectionDivider()

            ProfileSectionItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = "Log Out",
                subtitle = "Sign out of your account",
                iconTint = colors.error,
                titleColor = colors.error,
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
    titleColor: Color = MaterialTheme.calStuffColors.textPrimary,
    showChevron: Boolean = true,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.calStuffColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SectionIconChip(icon = icon, tint = iconTint)

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
                color = colors.textSecondary,
            )
        }

        if (showChevron) {
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun ThemeMode.displayLabel(): String = when (this) {
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
    ThemeMode.SYSTEM -> "System"
}

@Composable
private fun ProfileThemeSelectorItem(
    icon: ImageVector,
    title: String,
    themeMode: ThemeMode,
    iconTint: Color,
    onThemeModeSelected: (ThemeMode) -> Unit,
) {
    val colors = MaterialTheme.calStuffColors
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionIconChip(icon = icon, tint = iconTint)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = FontSize.body,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary,
                )
                Text(
                    text = themeMode.displayLabel(),
                    fontSize = FontSize.xSmall,
                    color = colors.textSecondary,
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
            ) {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = mode == themeMode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.accentSoft else Color.Transparent)
                            .clickable {
                                onThemeModeSelected(mode)
                                expanded = false
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = mode.displayLabel(),
                            modifier = Modifier.weight(1f),
                            fontSize = FontSize.small,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) colors.onAccentSoft else colors.textSecondary,
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = colors.onAccentSoft,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionIconChip(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.calStuffColors.divider,
    )
}

@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.calStuffColors
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = colors.surface,
        title = {
            Text(
                text = "Log Out",
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
            )
        },
        text = {
            Text(
                text = "Are you sure you want to log out of CalStuff?",
                fontSize = FontSize.medium,
                color = colors.textSecondary,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Yes, Log Out",
                    color = colors.error,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = colors.textPrimary,
                )
            }
        },
    )
}
