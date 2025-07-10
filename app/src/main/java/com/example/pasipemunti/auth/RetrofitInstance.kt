package com.example.pasipemunti.auth

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// singleton
// by lazy, adica se creeaza doar cand e prima data folosita
object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://3d6aa37fac35.ngrok-free.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
}
