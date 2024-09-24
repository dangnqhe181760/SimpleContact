package com.example.simplecontact

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainViewModel: ViewModel() {
    private val apiService = RetrofitInstance.api
    val contacts: MutableState<List<Contact>> = mutableStateOf(emptyList())
    suspend fun getContacts(): List<Contact> {
        return try {
            Log.d("get data", "Fetching data from API...")
            val response = apiService.getContacts()
            Log.d("Raw Response", response.toString())
            if (response.isNotEmpty()) {
                contacts.value = response // Update the state
            }
            response // Return the result of the API call
        } catch (e: Exception) {
            Log.e("API Error", e.message ?: "Unknown error")
            emptyList() // Return an empty list in case of error
        }
    }
}