package com.kashmir.bislei.screens.profileScreens

import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.kashmir.bislei.viewModels.ProfileViewModel
import com.kashmir.bislei.ui.theme.Dimensions
import com.kashmir.bislei.components.BisleiTopAppBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.graphics.ImageDecoder
import android.os.Build
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileFieldsScreen(
    onNavigateBack: () -> Unit,
    onProfileSaved: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    val profile by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var isLoading by remember { mutableStateOf(false) }
    var isFieldsInitialized by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val isFormValid = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank()

    // Initialize fields when profile loads
    LaunchedEffect(profile) {
        if (!isFieldsInitialized && profile.name.isNotEmpty()) {
            name = profile.name
            email = profile.email
            phone = profile.phone
            bio = profile.bio ?: ""
            isFieldsInitialized = true
        }
    }

    // Auto-clear messages after delay
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            delay(5000)
            errorMessage = ""
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage.isNotEmpty()) {
            delay(3000)
            successMessage = ""
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar
            BisleiTopAppBar(
                title = "Edit Profile",
                onNavigateBack = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateBack()
                }
            )

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(Dimensions.contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Dimensions.spaceL))

                // Header Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.spaceL),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(Dimensions.iconSize),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(Dimensions.spaceS))
                        Text(
                            text = "Update Your Profile",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Keep your information up to date",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spaceL))

                // Profile Image Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = Dimensions.cardElevation
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.spaceXL),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Section Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = null,
                                modifier = Modifier.size(Dimensions.iconSize),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(Dimensions.spaceS))
                            Text(
                                text = "Profile Picture",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimensions.spaceL))

                        // Profile Image with Edit Overlay
                        Box(
                            modifier = Modifier.size(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = MaterialTheme.shapes.extraLarge,
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 8.dp
                                )
                            ) {
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
                                            .size(120.dp)
                                            .clip(MaterialTheme.shapes.extraLarge)
                                            .border(
                                                width = 4.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = MaterialTheme.shapes.extraLarge
                                            )
                                            .clickable {
                                                launcher.launch("image/*")
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Image(
                                        painter = rememberAsyncImagePainter(
                                            profile.profileImageUrl.ifEmpty {
                                                "https://dummyimage.com/120x120/cccccc/ffffff&text=No+Img"
                                            }
                                        ),
                                        contentDescription = "Profile Image",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(MaterialTheme.shapes.extraLarge)
                                            .border(
                                                width = 4.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = MaterialTheme.shapes.extraLarge
                                            )
                                            .clickable {
                                                launcher.launch("image/*")
                                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }

                            // Edit Icon Overlay
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
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 6.dp
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Edit,
                                    contentDescription = "Edit Profile Picture",
                                    modifier = Modifier.padding(Dimensions.spaceM),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimensions.spaceS))

                        Text(
                            text = "Tap to change photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spaceL))

                // Form Fields Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = MaterialTheme.shapes.extraLarge,
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = Dimensions.cardElevation
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Dimensions.spaceXL),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spaceL)
                    ) {
                        // Section Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = null,
                                modifier = Modifier.size(Dimensions.iconSize),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(Dimensions.spaceS))
                            Text(
                                text = "Personal Information",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Name Field
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                errorMessage = ""
                            },
                            label = {
                                Text(
                                    "Full Name",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                                capitalization = KeyboardCapitalization.Words
                            ),
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )

                        // Email Field (Read-only)
                        OutlinedTextField(
                            value = email,
                            onValueChange = { }, // No-op since it's read-only
                            label = {
                                Text(
                                    "Email Address",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Read-only field",
                                    modifier = Modifier.size(Dimensions.iconSizeSmall),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = false, // Makes it read-only
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            ),
                            supportingText = {
                                Text(
                                    "Email cannot be changed as it's your unique identifier",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        )

                        // Phone Field
                        OutlinedTextField(
                            value = phone,
                            onValueChange = {
                                if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                                    phone = it
                                    errorMessage = ""
                                }
                            },
                            label = {
                                Text(
                                    "Phone Number",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            supportingText = {
                                Text(
                                    "${phone.length}/10",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (phone.length == 10)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )

                        // Bio Field
                        OutlinedTextField(
                            value = bio,
                            onValueChange = {
                                if (it.length <= 500) {
                                    bio = it
                                    errorMessage = ""
                                }
                            },
                            label = {
                                Text(
                                    "Bio",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            placeholder = {
                                Text(
                                    "Tell others about yourself...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done,
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            supportingText = {
                                Text(
                                    "${bio.length}/500 characters",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (bio.length > 450)
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spaceL))

                // Error/Success Messages
                AnimatedVisibility(
                    visible = errorMessage.isNotEmpty(),
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.spaceM),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Error,
                                contentDescription = null,
                                modifier = Modifier.size(Dimensions.iconSize),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(Dimensions.spaceS))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = successMessage.isNotEmpty(),
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.spaceM),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(Dimensions.iconSize),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(Dimensions.spaceS))
                            Text(
                                text = successMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spaceL))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.spaceM)
                ) {
                    OutlinedButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigateBack()
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
                        Spacer(modifier = Modifier.width(Dimensions.spaceS))
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = ""
                                successMessage = ""
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                                val success = viewModel.updateUserProfile(
                                    name = name.trim(),
                                    bio = bio.trim(),
                                    imageUri = selectedImageUri,
                                    email = email,
                                    phone = phone.trim()
                                )
                                isLoading = false

                                if (success) {
                                    successMessage = "Profile updated successfully!"
                                    delay(1500)
                                    onProfileSaved()
                                } else {
                                    errorMessage = "Failed to update profile. Please try again."
                                }
                            }
                        },
                        shape = MaterialTheme.shapes.medium,
                        enabled = isFormValid && !isLoading,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = Dimensions.spaceM),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(Dimensions.iconSizeSmall)
                            )
                            Spacer(modifier = Modifier.width(Dimensions.spaceS))
                            Text(
                                text = "Saving...",
                                style = MaterialTheme.typography.labelLarge
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = null,
                                modifier = Modifier.size(Dimensions.iconSizeSmall)
                            )
                            Spacer(modifier = Modifier.width(Dimensions.spaceS))
                            Text(
                                text = "Save Changes",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                // Bottom spacing for better scrolling
                Spacer(modifier = Modifier.height(Dimensions.spaceXXL))
            }
        }
    }
}