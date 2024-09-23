package com.example.simplecontact

import retrofit2.http.GET

interface ApiService {
    @GET("/contacts")
    suspend fun getContacts(): List<Contact>
}