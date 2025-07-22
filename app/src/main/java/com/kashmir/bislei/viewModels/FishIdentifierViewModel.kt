package com.kashmir.bislei.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Bitmap.Config
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.DataType
import java.io.File

class FishIdentifierViewModel : ViewModel() {

    private val _prediction = MutableStateFlow<String?>(null)
    val prediction = _prediction.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    companion object {
        private const val TAG = "FishIdentifierVM"
    }

    fun identifyFishFromUri(context: Context, uri: Uri) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting fish identification for URI: $uri")
                val bitmap = getBitmapFromUri(context, uri)
                if (bitmap != null) {
                    Log.d(TAG, "Bitmap loaded successfully.")
                    val processedTensorImage = preprocessImage(bitmap)
                    Log.d(TAG, "Image preprocessed into TensorImage.")
                    val modelFile = downloadModel()
                    if (modelFile != null) {
                        Log.d(TAG, "Model downloaded: ${modelFile.absolutePath}")
                        runModelOnTensorImage(processedTensorImage, modelFile)
                    } else {
                        Log.e(TAG, "Model download failed: modelFile is null")
                        _prediction.value = "Model download failed"
                        _isLoading.value = false
                    }
                } else {
                    Log.e(TAG, "Failed to load bitmap from URI.")
                    _prediction.value = "Failed to load image"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in identifyFishFromUri", e)
                _prediction.value = "Error: ${e.localizedMessage}"
                _isLoading.value = false
            }
        }
    }

    private suspend fun downloadModel(): File? {
        return try {
            Log.d(TAG, "Downloading custom model: fish_identifier")
            val conditions = CustomModelDownloadConditions.Builder().build()
            val model: CustomModel = FirebaseModelDownloader.getInstance()
                .getModel("fish_model", DownloadType.LOCAL_MODEL, conditions)
                .await()
            Log.d(TAG, "Model download successful. Path: ${model.file?.absolutePath}")
            model.file
        } catch (e: Exception) {
            Log.e(TAG, "Model download error", e)
            null
        }
    }

    private fun runModelOnTensorImage(tensorImage: TensorImage, modelFile: File) {
        try {
            Log.d(TAG, "Running model on TensorImage.")
            val localModel = LocalModel.Builder()
                .setAbsoluteFilePath(modelFile.absolutePath)
                .build()

            val options = CustomImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(0.5f)
                .build()

            val image = InputImage.fromBitmap(tensorImage.bitmap, 0)
            val labeler = ImageLabeling.getClient(options)

            labeler.process(image)
                .addOnSuccessListener { labels ->
                    Log.d(TAG, "Model inference successful: ${labels.joinToString { it.text }}")
                    _prediction.value = labels.joinToString(", ") { it.text }
                    _isLoading.value = false
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Model inference failed", e)
                    _prediction.value = "Prediction failed: ${e.localizedMessage}"
                    _isLoading.value = false
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while running model", e)
            _prediction.value = "Error: ${e.localizedMessage}"
            _isLoading.value = false
        }
    }

    private fun preprocessImage(bitmap: Bitmap): TensorImage {
        Log.d(TAG, "Preprocessing image.")
        val argbBitmap = if (bitmap.config != Config.ARGB_8888) {
            bitmap.copy(Config.ARGB_8888, true)
        } else {
            bitmap
        }

        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(argbBitmap)

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(256, 256, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f)) // Normalize pixel values from [0, 255] -> [0, 1]
            .build()

        val processed = imageProcessor.process(tensorImage)
        Log.d(TAG, "Image preprocessing complete.")
        return processed
    }

    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT < 28) {
                Log.d(TAG, "Decoding bitmap with MediaStore (API < 28)")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                Log.d(TAG, "Decoding bitmap with ImageDecoder (API >= 28)")
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode bitmap from URI", e)
            null
        }
    }
}
