package com.example.kenyanfoodrecognitionsystem.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kenyanfoodrecognitionsystem.data.AppDatabase
import com.example.kenyanfoodrecognitionsystem.data.Food
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TextInputFoodItem(
    val name: String,
    val weight: Float,
    val nutrition: CompositeNutrition?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State for the current food search
    var searchQuery by remember { mutableStateOf("") }
    var searchSuggestions by remember { mutableStateOf<List<Food>>(emptyList()) }
    var showDropdown by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    // State for weight input
    var weightText by remember { mutableStateOf("100") }

    // List of added food items with their nutrition
    var addedFoodItems by remember { mutableStateOf(listOf<TextInputFoodItem>()) }

    // Total nutrition (only when 2+ items)
    var totalNutrition by remember { mutableStateOf<CompositeNutrition?>(null) }

    // Feedback messages
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    // Debounced search
    LaunchedEffect(searchQuery) {
        searchJob?.cancel()
        if (searchQuery.isNotBlank() && searchQuery.length >= 2) {
            searchJob = coroutineScope.launch {
                delay(300) // Debounce delay
                val results = withContext(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(context)
                    db.foodDao().searchFoodsByName(searchQuery)
                }
                searchSuggestions = results
                showDropdown = results.isNotEmpty()
            }
        } else {
            searchSuggestions = emptyList()
            showDropdown = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover Food") },
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
            // Header Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF10DAE9),
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Search Food Database",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Enter food names to get nutritional information",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Search Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Add Food Item",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    // Search TextField with suggestions below
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { newValue ->
                            searchQuery = newValue
                            feedbackMessage = null
                        },
                        label = { Text("Food Name") },
                        placeholder = { Text("Start typing to search...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    // Suggestions displayed below the text field
                    if (searchSuggestions.isNotEmpty() && searchQuery.isNotBlank()) {
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
                                                searchQuery = food.food_name ?: ""
                                                searchSuggestions = emptyList()
                                                feedbackMessage = "✓ Food found in database"
                                                isError = false
                                            }
                                            .padding(horizontal = 8.dp, vertical = 8.dp)
                                    )
                                    if (food != searchSuggestions.take(5).last()) {
                                        HorizontalDivider(
                                            color = Color.LightGray,
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Feedback message
                    if (feedbackMessage != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = feedbackMessage!!,
                            color = if (isError) Color.Red else Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Show warning if no results and query is long enough
                    if (searchQuery.length >= 3 && searchSuggestions.isEmpty() && searchJob?.isActive == false) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "⚠️ No matching foods found in database",
                            color = Color(0xFFFF9800),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Weight Input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { newValue ->
                                if (newValue.all { it.isDigit() || it == '.' }) {
                                    weightText = newValue
                                }
                            },
                            label = { Text("Grams") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Button(
                            onClick = {
                                val weight = weightText.toFloatOrNull()
                                if (searchQuery.isNotBlank() && weight != null && weight > 0) {
                                    coroutineScope.launch {
                                        // Check if food exists in database
                                        val nutrition = fetchNutritionForFood(
                                            context = context,
                                            foodName = searchQuery,
                                            weightGrams = weight
                                        )

                                        if (nutrition != null) {
                                            // Add to list
                                            val newItem = TextInputFoodItem(
                                                name = searchQuery,
                                                weight = weight,
                                                nutrition = nutrition
                                            )
                                            addedFoodItems = addedFoodItems + newItem

                                            // Recalculate total
                                            recalculateTotalNutritionForTextInput(
                                                items = addedFoodItems,
                                                onTotalCalculated = { total -> totalNutrition = total }
                                            )

                                            // Clear inputs
                                            searchQuery = ""
                                            weightText = "100"
                                            feedbackMessage = null
                                        } else {
                                            feedbackMessage = "❌ Food not found in database"
                                            isError = true
                                        }
                                    }
                                }
                            },
                            enabled = searchQuery.isNotBlank() && weightText.toFloatOrNull() != null,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8B40)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("ADD")
                        }
                    }
                }
            }

            // Results Section
            if (addedFoodItems.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))

                // Show Total Nutrition Card ONLY if there are 2+ items
                if (addedFoodItems.size >= 2) {
                    TotalNutritionCard(totalNutrition)
                    Spacer(Modifier.height(16.dp))
                }

                // Individual Food Item Cards
                addedFoodItems.forEachIndexed { index, item ->
                    if (index > 0) Spacer(Modifier.height(16.dp))

                    TextInputFoodItemCard(
                        foodName = item.name,
                        weight = item.weight,
                        nutrition = item.nutrition,
                        onRemove = {
                            addedFoodItems = addedFoodItems.filterIndexed { i, _ -> i != index }
                            recalculateTotalNutritionForTextInput(
                                items = addedFoodItems,
                                onTotalCalculated = { total -> totalNutrition = total }
                            )
                        }
                    )
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun TextInputFoodItemCard(
    foodName: String,
    weight: Float,
    nutrition: CompositeNutrition?,
    onRemove: () -> Unit
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = foodName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF8B40)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Weight: ${weight.toInt()}g",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

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

fun recalculateTotalNutritionForTextInput(
    items: List<TextInputFoodItem>,
    onTotalCalculated: (CompositeNutrition?) -> Unit
) {
    if (items.size < 2) {
        onTotalCalculated(null)
        return
    }

    var totalEnergy = 0f
    var totalProtein = 0f
    var totalFat = 0f
    var totalCarbs = 0f
    var totalFibre = 0f
    var hasAnyData = false

    for (item in items) {
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