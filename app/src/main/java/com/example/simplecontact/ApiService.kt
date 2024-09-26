package com.example.simplecontact

import retrofit2.http.GET

interface ApiService {
    @GET("sample_contacts_list.json")
    suspend fun getContacts(): List<Contact>
}