package com.psm2.inva.presentation.features.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psm2.inva.data.SightingRepository
import com.psm2.inva.model.Sighting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StatsViewModel(
    private val repository: SightingRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatsState())
    val state = _state.asStateFlow()

    // Cache all sightings to avoid re-fetching for filtering
    private var allSightings: List<Sighting> = emptyList()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            repository.getAllSightings().collect { sightings ->
                allSightings = sightings

                // 1. Calculate Counts by Category
                val invasive = sightings.count { it.category.equals("Invasive", ignoreCase = true) }
                val native = sightings.count { it.category.equals("Native", ignoreCase = true) }
                val alien = sightings.count { it.category.equals("Alien", ignoreCase = true) }

                // 2. Find Active Anglers (Unique User IDs)
                val uniqueAnglers = sightings.map { it.userId }.distinct().count()

                // 3. Find Top Invasive Species
                val invasiveSightings = sightings.filter { it.category.equals("Invasive", ignoreCase = true) }

                val topInvasive = invasiveSightings
                    .groupingBy { it.speciesName }
                    .eachCount()
                    .toList()
                    .sortedByDescending { it.second }
                    .take(5)

                // 4. Prepare Species List for Dropdown
                val speciesList = invasiveSightings
                    .map { it.speciesName }
                    .distinct()
                    .sorted()

                // Default selection logic
                val currentSpecies = _state.value.selectedSpecies
                val defaultSelection = if (currentSpecies.isNotEmpty() && speciesList.contains(currentSpecies)) {
                    currentSpecies
                } else if (topInvasive.isNotEmpty()) {
                    topInvasive.first().first
                } else if (speciesList.isNotEmpty()) {
                    speciesList.first()
                } else {
                    ""
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        totalSightings = sightings.size,
                        invasiveCount = invasive,
                        nativeCount = native,
                        alienCount = alien,
                        activeAnglersCount = uniqueAnglers,
                        topInvasiveSpecies = topInvasive,
                        recentActivity = sightings.sortedByDescending { s -> s.date }.take(3),
                        invasiveSpeciesList = speciesList,
                        selectedSpecies = defaultSelection
                    )
                }

                // Calculate trend for initial selection
                if (defaultSelection.isNotEmpty()) {
                    calculateTrendData(defaultSelection, _state.value.selectedState)
                }
            }
        }
    }

    fun onSpeciesSelected(species: String) {
        _state.update { it.copy(selectedSpecies = species) }
        calculateTrendData(species, _state.value.selectedState)
    }

    fun onStateSelected(stateName: String) {
        _state.update { it.copy(selectedState = stateName) }
        calculateTrendData(_state.value.selectedSpecies, stateName)
    }

    private fun calculateTrendData(speciesName: String, stateName: String) {
        if (speciesName.isBlank()) return

        // Get last 6 months
        val calendar = Calendar.getInstance()
        val months = mutableListOf<String>()
        val monthKeys = mutableListOf<String>() // For grouping key "yyyy-MM"

        // Prepare keys for the last 6 months (including current)
        for (i in 5 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -i)
            val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
            val key = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)
            months.add(monthName)
            monthKeys.add(key)
        }

        // Filter sightings for this species AND state
        val filteredSightings = allSightings.filter { sighting ->
            val speciesMatch = sighting.speciesName.equals(speciesName, ignoreCase = true)

            // Check state match.
            // Using ONLY the new dedicated locationState field for robust filtering as requested.
            // Removed fallback to locationDescription.
            val stateMatch = if (stateName == "All") {
                true
            } else {
                sighting.locationState.contains(stateName, ignoreCase = true)
            }

            speciesMatch && stateMatch
        }

        // Group by month
        val countsByMonth = filteredSightings.groupingBy {
            val date = it.date.toDate()
            SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date)
        }.fold(0) { acc, sighting -> acc + sighting.quantity } // Sum quantities

        // Map to the last 6 months list, defaulting to 0 if no data
        val trendData = monthKeys.mapIndexed { index, key ->
            months[index] to (countsByMonth[key] ?: 0)
        }

        _state.update { it.copy(trendData = trendData) }
    }
}