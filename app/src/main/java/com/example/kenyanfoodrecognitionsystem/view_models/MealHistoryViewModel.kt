package com.example.kenyanfoodrecognitionsystem.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kenyanfoodrecognitionsystem.data.MealHistory
import com.example.kenyanfoodrecognitionsystem.data.MealHistoryDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MealHistoryViewModel(private val dao: MealHistoryDao) : ViewModel() {

    /**
     * Exposes the complete list of all meal history records as a StateFlow.
     * Room's Flow automatically handles background threading and updates whenever
     * a change occurs in the 'meal_history' table (e.g., a new meal is inserted).
     */
    val mealHistory: StateFlow<List<MealHistory>> = dao.getAllMeals()
        .stateIn(
            scope = viewModelScope,
            // Start collecting the Flow eagerly and keep it active
            // as long as there is at least one collector.
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}