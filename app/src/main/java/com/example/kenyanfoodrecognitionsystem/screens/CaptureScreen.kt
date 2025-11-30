package com.example.kenyanfoodrecognitionsystem.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kenyanfoodrecognitionsystem.data.AppDatabase
import com.example.kenyanfoodrecognitionsystem.data.Food
import com.example.kenyanfoodrecognitionsystem.data.MealHistory
import com.example.kenyanfoodrecognitionsystem.utils.ImageUtils
import com.example.kenyanfoodrecognitionsystem.utils.classifyFoodImageManual
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AddedFood(
    val name: String,
    val weightGrams: Float
)

data class CompositeNutrition(
    val energy: Float? = null,
    val protein: Float? = null,
    val fat: Float? = null,
    val carbohydrates: Float? = null,
    val fibre: Float? = null
)

data class FoodItemNutrition(
    val name: String,
    val weight: Float,
    val nutrition: CompositeNutrition?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScreen(
    initialSource: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isInitialLaunchAttempted by remember { mutableStateOf(false) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var predictionResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // State for editable food name
    var editableFoodName by remember { mutableStateOf("") }
    var classifiedWeight by remember { mutableStateOf(100f) }
    var classifiedWeightText by remember { mutableStateOf("100") }

    // Individual nutrition for classified item
    var classifiedNutrition by remember { mutableStateOf<CompositeNutrition?>(null) }

    // List of added food items with their individual nutrition
    var addedFoodItems by remember { mutableStateOf(listOf<FoodItemNutrition>()) }

    // Total nutrition (only when 2+ items)
    var totalNutrition by remember { mutableStateOf<CompositeNutrition?>(null) }

    // Input fields for adding new items
    var newItemName by remember { mutableStateOf("") }
    var newItemWeight by remember { mutableStateOf("") }

    // New state for search functionality
    var searchSuggestions by remember { mutableStateOf<List<Food>>(emptyList()) }
    var showDropdown by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // Feedback messages for save operation
    var saveMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Activity Result Launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageBitmap = uri.toBitmap(context)
            predictionResult = null
            editableFoodName = ""
            classifiedNutrition = null
            addedFoodItems = emptyList()
            totalNutrition = null
            classifiedWeightText = "100"
            classifiedWeight = 100f
            newItemName = ""
            newItemWeight = ""
        } else if (!isInitialLaunchAttempted) {
            onNavigateBack()
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            imageBitmap = bitmap
            predictionResult = null
            editableFoodName = ""
            classifiedNutrition = null
            addedFoodItems = emptyList()
            totalNutrition = null
            classifiedWeightText = "100"
            classifiedWeight = 100f
            newItemName = ""
            newItemWeight = ""
        } else if (!isInitialLaunchAttempted) {
            onNavigateBack()
        }
    }

    LaunchedEffect(newItemName) {
        searchJob?.cancel()
        if (newItemName.isNotBlank() && newItemName.length >= 2) {
            searchJob = coroutineScope.launch {
                delay(300) // Debounce delay
                val results = withContext(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(context)
                    // Assuming you have a Food data class and FoodDao is accessible
                    db.foodDao().searchFoodsByName(newItemName)
                }
                searchSuggestions = results
                showDropdown = results.isNotEmpty()
            }
        } else {
            searchSuggestions = emptyList()
            showDropdown = false
        }
    }

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
                }
            )
        },
        containerColor = Color(0xFFF0F0F0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Image Display Area
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

            // Classification Button
            Button(
                onClick = {
                    if (imageBitmap != null && !isLoading) {
                        isLoading = true
                        predictionResult = null
                        editableFoodName = ""
                        classifiedNutrition = null
                        addedFoodItems = emptyList()
                        totalNutrition = null

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

                            if (!result.startsWith("Error")) {
                                val foodName = extractFoodName(result)
                                editableFoodName = foodName

                                // Fetch nutrition for classified item only
                                val nutrition = fetchNutritionForFood(context, foodName, classifiedWeight)
                                classifiedNutrition = nutrition

                                // No total nutrition yet (only 1 item)
                                totalNutrition = null
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

            // Change Image Section
            Text(
                text = "Change Image",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
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

            // Results Section (comes AFTER buttons)
            if (predictionResult != null && !predictionResult!!.startsWith("Error")) {
                Spacer(Modifier.height(24.dp))

                // Show Total Nutrition Card ONLY if there are 2+ items
                val totalItemCount = 1 + addedFoodItems.size
                if (totalItemCount >= 2) {
                    TotalNutritionCard(totalNutrition)
                    Spacer(Modifier.height(16.dp))
                }

                // Classified Food Item Card
                FoodItemCard(
                    foodName = editableFoodName,
                    weight = classifiedWeight,
                    weightText = classifiedWeightText,
                    nutrition = classifiedNutrition,
                    onFoodNameChange = { newName ->
                        editableFoodName = newName
                        // Recalculate nutrition when name changes
                        coroutineScope.launch {
                            val nutrition = fetchNutritionForFood(context, newName, classifiedWeight)
                            classifiedNutrition = nutrition
                            recalculateTotalNutrition(
                                classifiedNutrition = classifiedNutrition,
                                addedItems = addedFoodItems,
                                onTotalCalculated = { total -> totalNutrition = total }
                            )
                        }
                    },
                    onWeightChange = { newWeightText ->
                        classifiedWeightText = newWeightText
                        val newWeight = newWeightText.toFloatOrNull() ?: 100f
                        classifiedWeight = newWeight
                        // Recalculate nutrition when weight changes
                        coroutineScope.launch {
                            val nutrition = fetchNutritionForFood(context, editableFoodName, classifiedWeight)
                            classifiedNutrition = nutrition
                            recalculateTotalNutrition(
                                classifiedNutrition = classifiedNutrition,
                                addedItems = addedFoodItems,
                                onTotalCalculated = { total -> totalNutrition = total }
                            )
                        }
                    },
                    onRemove = null // No remove button for classified item
                )

                // Added Food Items Cards
                addedFoodItems.forEachIndexed { index, item ->
                    Spacer(Modifier.height(16.dp))
                    FoodItemCard(
                        foodName = item.name,
                        weight = item.weight,
                        weightText = item.weight.toString(),
                        nutrition = item.nutrition,
                        isEditable = false,
                        onFoodNameChange = {},
                        onWeightChange = {},
                        onRemove = {
                            // Remove this item and recalculate
                            addedFoodItems = addedFoodItems.filterIndexed { i, _ -> i != index }
                            recalculateTotalNutrition(
                                classifiedNutrition = classifiedNutrition,
                                addedItems = addedFoodItems,
                                onTotalCalculated = { total -> totalNutrition = total }
                            )
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Add Unclassified Items Section
                val bringIntoViewRequester = remember { BringIntoViewRequester() }
                val focusCoroutineScope = rememberCoroutineScope()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(bringIntoViewRequester),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Add Unclassified Food Items",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = newItemName,
                            onValueChange = { newItemName = it },
                            label = { Text("Food Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusEvent { focusState ->
                                    if (focusState.isFocused) {
                                        focusCoroutineScope.launch {
                                            bringIntoViewRequester.bringIntoView()
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(8.dp)
                        )

                        if (searchSuggestions.isNotEmpty() && newItemName.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = "Suggestions:",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                    searchSuggestions.take(5).forEach { food ->
                                        Text(
                                            text = food.food_name ?: "Unknown",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    newItemName = food.food_name ?: "" // Select suggestion
                                                    searchSuggestions = emptyList()     // Clear suggestions
                                                }
                                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                        )
                                        if (food != searchSuggestions.take(5).last()) {
                                            androidx.compose.material3.HorizontalDivider(
                                                color = Color.LightGray,
                                                thickness = 0.5.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (newItemName.length >= 3 && searchSuggestions.isEmpty() && searchJob?.isActive == false) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "⚠️ No matching foods found in database",
                                color = Color(0xFFFF9800),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newItemWeight,
                                onValueChange = { newValue ->
                                    if (newValue.all { it.isDigit() || it == '.' }) {
                                        newItemWeight = newValue
                                    }
                                },
                                label = { Text("Grams") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .onFocusEvent { focusState ->
                                        if (focusState.isFocused) {
                                            focusCoroutineScope.launch {
                                                bringIntoViewRequester.bringIntoView()
                                            }
                                        }
                                    },
                                shape = RoundedCornerShape(8.dp)
                            )

                            Button(
                                onClick = {
                                    val weight = newItemWeight.toFloatOrNull()
                                    if (newItemName.isNotBlank() && weight != null && weight > 0) {
                                        coroutineScope.launch {
                                            // Fetch nutrition for this new item
                                            val nutrition = fetchNutritionForFood(context, newItemName, weight)
                                            val newItem = FoodItemNutrition(
                                                name = newItemName,
                                                weight = weight,
                                                nutrition = nutrition
                                            )
                                            addedFoodItems = addedFoodItems + newItem

                                            // Recalculate total
                                            recalculateTotalNutrition(
                                                classifiedNutrition = classifiedNutrition,
                                                addedItems = addedFoodItems,
                                                onTotalCalculated = { total -> totalNutrition = total }
                                            )

                                            // Clear inputs
                                            newItemName = ""
                                            newItemWeight = ""
                                        }
                                    }
                                },
                                enabled = newItemName.isNotBlank() && newItemWeight.toFloatOrNull() != null,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8B40)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text("ADD")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Save Meal Button - Only show if there's nutrition data
                val canSaveMeal = classifiedNutrition != null || addedFoodItems.any { it.nutrition != null }
                if (canSaveMeal) {
                    Button(
                        onClick = {
                            if (imageBitmap != null && !isSaving) {
                                isSaving = true
                                saveMessage = null
                                coroutineScope.launch {
                                    try {
                                        val result = saveMealToDatabase(
                                            context = context,
                                            bitmap = imageBitmap!!,
                                            classifiedFoodName = editableFoodName,
                                            classifiedWeight = classifiedWeight,
                                            addedItems = addedFoodItems,
                                            totalNutrition = totalNutrition ?: classifiedNutrition
                                        )

                                        if (result) {
                                            saveMessage = "✓ Meal saved successfully!"
                                        } else {
                                            saveMessage = "❌ Failed to save meal"
                                        }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        saveMessage = "❌ Error: ${e.message}"
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            }
                        },
                        enabled = !isSaving && imageBitmap != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50),
                            disabledContainerColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text("Saving...", fontSize = 18.sp, color = Color.White)
                        } else {
                            Text(
                                "Save Meal to History",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Save feedback message
                    if (saveMessage != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = saveMessage!!,
                            color = if (saveMessage!!.startsWith("✓")) Color(0xFF4CAF50) else Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else if (predictionResult?.startsWith("Error") == true) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = predictionResult!!,
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun TotalNutritionCard(totalNutrition: CompositeNutrition?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(2.dp, Color(0xFF2196F3))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Total Nutritional Information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )

            if (totalNutrition != null) {
                Spacer(Modifier.height(16.dp))

                // Calories
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${totalNutrition.energy.toFormattedString()} kCal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5722)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Macros Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CompactMacroItem("Carbs", totalNutrition.carbohydrates.toFormattedString(), Color(0xFF64B5F6))
                    CompactMacroItem("Proteins", totalNutrition.protein.toFormattedString(), Color(0xFFFFF176))
                    CompactMacroItem("Fats", totalNutrition.fat.toFormattedString(), Color(0xFF81C784))
                    CompactMacroItem("Fiber", totalNutrition.fibre.toFormattedString(), Color(0xFFFFB74D))
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Calculating total nutrition...",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun FoodItemCard(
    foodName: String,
    weight: Float,
    weightText: String,
    nutrition: CompositeNutrition?,
    isEditable: Boolean = true,
    onFoodNameChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onRemove: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Prediction Result:",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )

                // Show X button only for removable items (added items)
                if (onRemove != null) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove item",
                            tint = Color.Red
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Editable Food Name
            if (isEditable) {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = onFoodNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "💡 You can edit the food name if it's not correct",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            } else {
                Text(
                    text = foodName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF8B40)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Weight Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditable) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() || it == '.' }) {
                                onWeightChange(newValue)
                            }
                        },
                        label = { Text("Grams") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.width(120.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Text(
                        text = "Weight: ${weight.toInt()}g",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Nutritional Information (for ${weight.toInt()}g)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (nutrition != null) {
                Spacer(Modifier.height(12.dp))

                // Calories Display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF8E1), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Calories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${nutrition.energy.toFormattedString()} kCal",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5722)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Macronutrients Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SmallMacroCard("Carbs", nutrition.carbohydrates.toFormattedString(), Color(0xFF64B5F6))
                    SmallMacroCard("Proteins", nutrition.protein.toFormattedString(), Color(0xFFFFF176))
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SmallMacroCard("Fats", nutrition.fat.toFormattedString(), Color(0xFF81C784))
                    SmallMacroCard("Fiber", nutrition.fibre.toFormattedString(), Color(0xFFFFB74D))
                }
            } else {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "⚠️ No nutrition data found for this food.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun RowScope.SmallMacroCard(label: String, value: String, backgroundColor: Color) {
    Card(
        modifier = Modifier
            .weight(1f)
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$value g",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun CompactMacroItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(color, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$value\ng",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

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
    val lastParenthesisIndex = trimmedResult.lastIndexOf('(')
    return if (lastParenthesisIndex > 0) {
        trimmedResult.substring(0, lastParenthesisIndex).trim()
    } else {
        trimmedResult
    }
}

fun Float?.toFormattedString(defaultValue: String = "-"): String {
    return this?.let { String.format("%.1f", it) } ?: defaultValue
}

suspend fun fetchNutritionForFood(
    context: Context,
    foodName: String,
    weightGrams: Float
): CompositeNutrition? {
    val db = AppDatabase.getDatabase(context)
    val dao = db.foodDao()

    val food = withContext(Dispatchers.IO) {
        dao.getFoodByName(foodName) ?: dao.getSimilarFoodByName(foodName)
    }

    return if (food != null) {
        val multiplier = weightGrams / 100.0f
        CompositeNutrition(
            energy = food.energy?.times(multiplier),
            protein = food.protein?.times(multiplier),
            fat = food.fat?.times(multiplier),
            carbohydrates = food.carbohydrates?.times(multiplier),
            fibre = food.fibre?.times(multiplier)
        )
    } else {
        null
    }
}

fun recalculateTotalNutrition(
    classifiedNutrition: CompositeNutrition?,
    addedItems: List<FoodItemNutrition>,
    onTotalCalculated: (CompositeNutrition?) -> Unit
) {
    // Only calculate total if there are 2+ items
    val totalItemCount = 1 + addedItems.size
    if (totalItemCount < 2) {
        onTotalCalculated(null)
        return
    }

    var totalEnergy = 0f
    var totalProtein = 0f
    var totalFat = 0f
    var totalCarbs = 0f
    var totalFibre = 0f
    var hasAnyData = false

    // Add classified item
    if (classifiedNutrition != null) {
        totalEnergy += classifiedNutrition.energy ?: 0f
        totalProtein += classifiedNutrition.protein ?: 0f
        totalFat += classifiedNutrition.fat ?: 0f
        totalCarbs += classifiedNutrition.carbohydrates ?: 0f
        totalFibre += classifiedNutrition.fibre ?: 0f
        hasAnyData = true
    }

    // Add all manually added items
    for (item in addedItems) {
        if (item.nutrition != null) {
            totalEnergy += item.nutrition.energy ?: 0f
            totalProtein += item.nutrition.protein ?: 0f
            totalFat += item.nutrition.fat ?: 0f
            totalCarbs += item.nutrition.carbohydrates ?: 0f
            totalFibre += item.nutrition.fibre ?: 0f
            hasAnyData = true
        }
    }

    if (hasAnyData) {
        onTotalCalculated(
            CompositeNutrition(
                energy = totalEnergy,
                protein = totalProtein,
                fat = totalFat,
                carbohydrates = totalCarbs,
                fibre = totalFibre
            )
        )
    } else {
        onTotalCalculated(null)
    }
}

/**
 * Save meal to database
 */
suspend fun saveMealToDatabase(
    context: Context,
    bitmap: Bitmap,
    classifiedFoodName: String,
    classifiedWeight: Float,
    addedItems: List<FoodItemNutrition>,
    totalNutrition: CompositeNutrition?
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val dao = db.mealHistoryDao()

            // Resize and save image
            val resizedBitmap = ImageUtils.resizeBitmap(bitmap, 800, 800)
            val imagePath = ImageUtils.saveBitmapToFile(context, resizedBitmap)
                ?: return@withContext false

            // Build food items string
            val foodItemsBuilder = StringBuilder()
            foodItemsBuilder.append("$classifiedFoodName (${classifiedWeight.toInt()}g)")

            addedItems.forEach { item ->
                foodItemsBuilder.append(", ${item.name} (${item.weight.toInt()}g)")
            }

            // Create meal history record
            val mealHistory = MealHistory(
                imageData = imagePath,
                timestamp = ImageUtils.getCurrentTimestamp(),
                dateString = ImageUtils.getCurrentDateString(),
                classifiedFoodName = classifiedFoodName,
                foodItems = foodItemsBuilder.toString(),
                totalEnergy = totalNutrition?.energy,
                totalProtein = totalNutrition?.protein,
                totalFat = totalNutrition?.fat,
                totalCarbohydrates = totalNutrition?.carbohydrates,
                totalFibre = totalNutrition?.fibre,
                mealType = null,
                notes = null
            )

            // Insert into database
            val id = dao.insertMeal(mealHistory)
            Log.d("MealHistory", "Meal Saved! ID: $id")
            id > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
