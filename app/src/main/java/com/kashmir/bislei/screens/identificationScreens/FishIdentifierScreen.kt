package com.kashmir.bislei.screens.identificationScreens


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.kashmir.bislei.viewModels.FishIdentifierViewModel

@Composable
fun FishIdentifierScreen(viewModel: FishIdentifierViewModel = viewModel()) {
    val context = LocalContext.current
    val prediction by viewModel.prediction.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
        uri?.let {
            viewModel.identifyFishFromUri(context, it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Select Image")
        }

        selectedImageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = null,
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
            )
        }

        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            prediction != null -> {
                Text("Prediction: $prediction", style = MaterialTheme.typography.titleMedium)
            }

            selectedImageUri != null && prediction == null -> {
                Text("No prediction found.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}