package com.kashmir.bislei.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kashmir.bislei.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BisleiTopAppBar(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    showLogo: Boolean = false,
    actions: @Composable () -> Unit = {}
) {
    val hapticFeedback = LocalHapticFeedback.current

    val isDarkTheme = isSystemInDarkTheme()
    val logoTint = if (isDarkTheme) {
        MaterialTheme.colorScheme.onBackground
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .shadow(0.dp)
    ) {
        TopAppBar(
            title = {
                if (showLogo) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .wrapContentSize(align = Alignment.CenterStart)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.bislei_logo),
                                contentDescription = "App Logo",
                                tint = logoTint,
                                modifier = Modifier.size(70.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(0.dp)) // Adjust spacing if needed
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            navigationIcon = {
                if (onNavigateBack != null && !showLogo) {
                    IconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            actions = { actions() },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.fillMaxWidth()
        )

//        HorizontalDivider(
//            modifier = Modifier.fillMaxWidth(),
//            thickness = 0.5.dp,
//            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
//        )
    }
}

@Preview
@Composable
private fun TopAppBarPrev() {
    BisleiTopAppBar(title = "Bislei", onNavigateBack = {}, showLogo = true, actions = {})

}