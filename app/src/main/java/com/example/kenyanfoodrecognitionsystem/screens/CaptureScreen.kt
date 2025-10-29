package com.example.kenyanfoodrecognitionsystem.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kenyanfoodrecognitionsystem.data.AppDatabase
import com.example.kenyanfoodrecognitionsystem.data.Food
import com.example.kenyanfoodrecognitionsystem.utils.classifyFoodImageManual
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    initialSource: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State to track if the initial launcher has run
    var isInitialLaunchAttempted by remember { mutableStateOf(false) }

    // State for the selected or captured image
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // State for the prediction result
    var predictionResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var nutritionInfo by remember { mutableStateOf<Food?>(null) }

    // --- Activity Result Launchers ---

    // 1. Gallery Image Picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageBitmap = uri.toBitmap(context)
            predictionResult = null // Reset prediction when new image is selected
        } else if (!isInitialLaunchAttempted) {
            onNavigateBack()
        }
    }

    // 2. Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            imageBitmap = bitmap
            predictionResult = null // Reset prediction when new image is captured
        } else if (!isInitialLaunchAttempted) {
            onNavigateBack()
        }
    }

    // --- Launched Effect to handle initial navigation argument ---
    LaunchedEffect(initialSource) {
        if (!isInitialLaunchAttempted) {
            when (initialSource) {
                "camera" -> cameraLauncher.launch(null)
                "gallery" -> galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            isInitialLaunchAttempted = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Classify Food") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xAD10DAE9))
            )
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // --- Image Display Area ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    imageBitmap != null -> {
                        Image(
                            bitmap = imageBitmap!!.asImageBitmap(),
                            contentDescription = "Selected food image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    isLoading -> {
                        CircularProgressIndicator(color = Color(0xAD10DAE9))
                    }
                    else -> {
                        Text(
                            text = if (isInitialLaunchAttempted)
                                "No image selected.\nPlease select an image below."
                            else
                                "Initializing...",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- Classification Button (Moved to top) ---
            Button(
                onClick = {
                    if (imageBitmap != null && !isLoading) {
                        isLoading = true
                        predictionResult = null
                        nutritionInfo = null
                        coroutineScope.launch {
                            val result = withContext(Dispatchers.Default) {
                                try {
                                    classifyFoodImageManual(context, imageBitmap!!)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    "Error: ${e.message ?: "Unknown error"}"
                                }
                            }
                            predictionResult = result

                            // ...
                            // Fetch nutrition info from Room
                            if (!result.startsWith("Error")) {
                                val foodName = extractFoodName(result)
                                val db = AppDatabase.getDatabase(context)
                                val dao = db.foodDao()
                                val food = withContext(Dispatchers.IO) {
                                    // 2. Try EXACT match first:
                                    dao.getFoodByName(foodName)
                                    // 3. Fallback to PARTIAL match:
                                        ?: dao.getSimilarFoodByName(foodName)
                                }
                                nutritionInfo = food
                            }

                            isLoading = false
                        }
                    }
                },
                enabled = imageBitmap != null && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text("Classifying...", fontSize = 18.sp, color = Color.White)
                } else {
                    Text(
                        "Classify Food",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- Prediction Result Card ---
            if (predictionResult != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Prediction Result:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = predictionResult!!,
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (predictionResult!!.startsWith("Error"))
                                Color.Red
                            else
                                Color(0xFFFF8B40),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // --- Nutrition Info Card ---
            if (nutritionInfo != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF7)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Nutritional Information (per 100g)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Energy: ${nutritionInfo!!.energy ?: "-"} kcal")
                        Text("Protein: ${nutritionInfo!!.protein ?: "-"} g")
                        Text("Fat: ${nutritionInfo!!.fat ?: "-"} g")
                        Text("Carbohydrates: ${nutritionInfo!!.carbohydrates ?: "-"} g")
                        Text("Fibre: ${nutritionInfo!!.fibre ?: "-"} g")
                    }
                }
            } else if (predictionResult != null && !predictionResult!!.startsWith("Error")) {
                Text(
                    text = "⚠️ No nutrition data found for this food.",
                    color = Color.Gray,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Divider(
                thickness = 1.dp,
                color = Color.Gray.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // --- Change Image Section ---
            Text(
                text = "Change Image",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // --- Gallery and Camera Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Gallery Button
                OutlinedButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFF8B40)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoLibrary,
                        contentDescription = null,
                        Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Gallery", fontSize = 16.sp)
                }

                // Camera Button
                OutlinedButton(
                    onClick = { cameraLauncher.launch(null) },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF10DAE9)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Camera,
                        contentDescription = null,
                        Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Camera", fontSize = 16.sp)
                }
            }
        }
    }
}

/**
 * Utility function to safely convert a Content URI to a Bitmap.
 */
fun Uri.toBitmap(context: Context): Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, this))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, this)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun extractFoodName(rawResult: String): String {
    val trimmedResult = rawResult.trim()

    // Find the index of the last opening parenthesis
    val lastParenthesisIndex = trimmedResult.lastIndexOf('(')

    return if (lastParenthesisIndex > 0) {
        // Return the substring from the start up to the parenthesis, and trim any new whitespace
        trimmedResult.substring(0, lastParenthesisIndex).trim()
    } else {
        // If no parenthesis is found, return the trimmed result as-is
        trimmedResult
    }
}