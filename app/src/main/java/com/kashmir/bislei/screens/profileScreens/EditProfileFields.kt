package com.kashmir.bislei.screens.profileScreens

import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.kashmir.bislei.viewModels.ProfileViewModel
import com.kashmir.bislei.ui.theme.Dimensions
import com.kashmir.bislei.components.BisleiTopAppBar
import kotlinx.coroutines.launch
import android.graphics.ImageDecoder
import android.os.Build
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@Composable
fun EditProfileFieldsScreen(
    onProfileSaved: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val profile by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()

    val isFormValid =
        profile.name.isNotBlank() && profile.email.isNotBlank() && profile.phone.isNotBlank()

    var isLoading by remember { mutableStateOf(false) }
    var isFieldsInitialized by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(profile) {
        if (!isFieldsInitialized && profile.name.isNotEmpty()) {
            name = profile.name
            email = profile.email
            phone = profile.phone
            bio = profile.bio
            isFieldsInitialized = true
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        BisleiTopAppBar(
            title = "Edit Profile",
            onNavigateBack = onProfileSaved
        )

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(Dimensions.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            Spacer(modifier = Modifier.height(Dimensions.spaceL))

            // Profile Image Section with Pencil Icon
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                // Profile Image
                if (selectedImageUri != null) {
                    val bitmap = remember(selectedImageUri) {
                        val source = if (Build.VERSION.SDK_INT < 28) {
                            MediaStore.Images.Media.getBitmap(context.contentResolver, selectedImageUri)
                        } else {
                            val source = ImageDecoder.createSource(context.contentResolver, selectedImageUri!!)
                            ImageDecoder.decodeBitmap(source)
                        }
                        source
                    }
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable {
                                launcher.launch("image/*")
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(profile.profileImageUrl),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .border(
                                width = 3.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable {
                                launcher.launch("image/*")
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        contentScale = ContentScale.Crop
                    )
                }

                // Pencil Edit Icon
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clickable {
                            launcher.launch("image/*")
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit Profile Picture",
                        modifier = Modifier.padding(Dimensions.spaceS),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spaceS))

            // Form Fields
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation)
            ) {
                Column(
                    modifier = Modifier.padding(Dimensions.contentPadding),
                    verticalArrangement = Arrangement.spacedBy(Dimensions.spaceM)
                ) {
                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        shape = MaterialTheme.shapes.medium
                    )

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = MaterialTheme.shapes.medium
                    )

                    // Phone Field
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        shape = MaterialTheme.shapes.medium
                    )

                    // Bio Field
                    val maxLines = 5
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = maxLines,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            keyboardType = KeyboardType.Text,
                            imeAction = if (bio.count { it == '\n' } + 1 >= maxLines) ImeAction.Done else ImeAction.Default
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        shape = MaterialTheme.shapes.medium,
                        supportingText = {
                            Text(
                                text = "Tell others about yourself",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spaceXL))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceM)
            ) {
                OutlinedButton(
                    onClick = {
                        onProfileSaved()
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(vertical = Dimensions.spaceM)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.iconSizeSmall)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.spaceXS))
                    Text(
                        text = "Discard",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            val success = viewModel.updateUserProfile(
                                name = name,
                                bio = bio,
                                imageUri = selectedImageUri,
                                email = email,
                                phone = phone
                            )
                            isLoading = false

                            if (success) {
                                println("Profile successfully updated")
                                onProfileSaved()
                            } else {
                                println("Failed to update profile")
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    enabled = isFormValid && !isLoading,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = Dimensions.spaceM)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(Dimensions.iconSizeSmall)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Outlined.Save,
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.iconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(Dimensions.spaceXS))
                        Text(
                            text = "Save",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Bottom spacing for better scrolling
            Spacer(modifier = Modifier.height(Dimensions.spaceXL))
        }
    }
}