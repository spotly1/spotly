package com.example.spotly.ui.components

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.spotly.domain.repository.AddressRepository

// El host proporciona la implementación. Sin proveedor, previews muestran el texto alternativo.
val LocalAddressRepository = staticCompositionLocalOf<AddressRepository?> { null }
