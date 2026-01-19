package com.psm2.inva.di

import androidx.credentials.CredentialManager
import androidx.lifecycle.SavedStateHandle
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.psm2.inva.auth.GoogleAuthUiClient
import com.psm2.inva.data.*
import com.psm2.inva.presentation.features.add_blog.AddBlogViewModel
import com.psm2.inva.presentation.features.add_fish.AddFishViewModel
import com.psm2.inva.presentation.features.blog_detail.BlogDetailViewModel
import com.psm2.inva.presentation.features.catch_detail.CatchDetailViewModel
import com.psm2.inva.presentation.features.classification.ClassificationResultViewModel
import com.psm2.inva.presentation.features.edit_blog.EditBlogViewModel
import com.psm2.inva.presentation.features.edit_catch.EditCatchViewModel
import com.psm2.inva.presentation.features.edit_fish.EditFishViewModel
import com.psm2.inva.presentation.features.edit_profile.EditProfileViewModel
import com.psm2.inva.presentation.features.home.HomeViewModel
import com.psm2.inva.presentation.features.identify.IdentifyViewModel
import com.psm2.inva.presentation.features.map.MapViewModel
import com.psm2.inva.presentation.features.profile.ProfileViewModel
import com.psm2.inva.presentation.features.report.ReportViewModel
import com.psm2.inva.presentation.features.sign_in.SignInViewModel
import com.psm2.inva.presentation.features.species_details.SpeciesDetailViewModel
import com.psm2.inva.presentation.features.stats.StatsViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { CredentialManager.create(androidApplication().applicationContext) }
    single { LocationServices.getFusedLocationProviderClient(androidApplication()) }
    single { GoogleAuthUiClient(context = androidApplication().applicationContext, credentialManager = get()) }
    single { Firebase.auth }
    single { Firebase.firestore }
    single { Firebase.storage }

    // Updated AuthRepositoryImpl to take all necessary dependencies
    single<AuthRepository> {
        AuthRepositoryImpl(
            googleAuthUiClient = get(),
            auth = get(),
            firestore = get(),
            storage = get()
        )
    }

    //Data Repo
    single<FishRepository> { FishRepositoryImpl(firestore = get(), storage = get()) }
    single<SightingRepository> { SightingRepositoryImpl(firestore = get(), storage = get(), auth = get()) }
    single<BlogRepository> { BlogRepositoryImpl(firestore = get(), storage = get()) }

    //AI Classifier
    factory { FishClassifier(context = androidContext()) }

    //ViewModels
    viewModel { SignInViewModel(repository = get()) }

    // Simplified HomeViewModel (Only needs BlogRepository now)
    viewModel { HomeViewModel(blogRepository = get()) }

    viewModel { ProfileViewModel(authRepository = get(), sightingRepository = get()) }

    // New ViewModel for Edit Profile
    viewModel { EditProfileViewModel(authRepository = get()) }

    viewModel { IdentifyViewModel(repository = get(), authRepository = get()) }

    viewModel { MapViewModel(
        fusedLocationProviderClient = get(),
        sightingRepository = get(),
        authRepository = get()
    ) }

    viewModel { ReportViewModel(
        repository = get(),
        fishRepository = get(),
        fusedLocationClient = get(),
        authRepository = get()
    ) }

    // NEW: Stats ViewModel
    viewModel { StatsViewModel(repository = get()) }

    viewModel { AddFishViewModel(repository = get()) }

    viewModel { params -> AddBlogViewModel(
        categoryName = params.get(),
        blogRepository = get(),
        authRepository = get()
    )}

    viewModel { params -> EditBlogViewModel(
        blogId = params.get(),
        repository = get()
    )}

    viewModel { params -> BlogDetailViewModel(
        blogId = params.get(),
        repository = get(),
        authRepository = get()
    )}

    viewModel { params -> SpeciesDetailViewModel(
        fishId = params.get(),
        repository = get(),
        authRepository = get())
    }
    viewModel { params -> EditFishViewModel(
        fishId = params.get(),
        repository = get())
    }

    viewModel { (savedStateHandle: SavedStateHandle) -> ClassificationResultViewModel(
        savedStateHandle = savedStateHandle,
        classifier = get(),
        context = androidContext(),
        repository = get())
    }

    viewModel { params -> CatchDetailViewModel(
        sightingId = params.get(),
        repository = get(),
        authRepository = get()
    ) }

    viewModel { params -> EditCatchViewModel(
        sightingId = params.get(),
        repository = get(),
        fishRepository = get()
    ) }
}