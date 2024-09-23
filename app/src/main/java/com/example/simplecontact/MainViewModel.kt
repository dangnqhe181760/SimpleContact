package com.example.simplecontact

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val apiService = RetrofitInstance.api
    val contacts: MutableState<List<Contact>> = mutableStateOf(emptyList())
    fun getContacts() {
        viewModelScope.launch {
            try {
                Log.d("get data", "get data from api")
                val response = apiService.getContacts()
                if (response.isNotEmpty()) {
                    contacts.value = response
                }
            } catch (e: Exception) {
                // Handle errors here
            } finally {
                Log.d("result: ", contacts.value.toString())
            }
        }
    }
}