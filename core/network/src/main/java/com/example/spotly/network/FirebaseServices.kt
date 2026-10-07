package com.example.spotly.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/** Clientes externos; este módulo no conoce dominio, datos ni pantallas. */
class FirebaseServices(
    val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
)
