package com.example.kenyanfoodrecognitionsystem.authentication

import com.google.firebase.auth.FirebaseAuth

object AuthManager {
    val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()
}