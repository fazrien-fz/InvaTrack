package com.psm2.inva.presentation.features.stats

import com.psm2.inva.model.Sighting

data class StatsState(
    val isLoading: Boolean = true,
    val totalSightings: Int = 0,
    val invasiveCount: Int = 0,
    val nativeCount: Int = 0,
    val alienCount: Int = 0,
    val topInvasiveSpecies: List<Pair<String, Int>> = emptyList(), // Species Name -> Count
    val activeAnglersCount: Int = 0,
    val recentActivity: List<Sighting> = emptyList(),

    // Graph Data
    val invasiveSpeciesList: List<String> = emptyList(),
    val selectedSpecies: String = "",
    val trendData: List<Pair<String, Int>> = emptyList(), // Month Label -> Count

    // State Filter
    val stateList: List<String> = listOf("All", "Johor", "Kedah", "Kelantan", "Melaka", "Negeri Sembilan", "Pahang", "Perak", "Perlis", "Penang", "Sabah", "Sarawak", "Selangor", "Terengganu", "Kuala Lumpur", "Putrajaya", "Labuan"),
    val selectedState: String = "All"
)