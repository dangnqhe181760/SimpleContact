package com.example.simplecontact

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.simplecontact.Extension.makeResult
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class MainViewModel: ViewModel() {
    private var ioJob: Job = Job()
    val coroutineScope by lazy { CoroutineScope(ioJob + Dispatchers.IO + exceptionHandler) }
    private val exceptionHandler = CoroutineExceptionHandler { coroutineContext, error ->
        Log.d("exceptionHandler", "${error.message}")
//        val errorResult =
//            ErrorResult(errorMessage = error.message)
    }

    fun proceedContact() = coroutineScope.launch {
        val response = apiService.getContacts()
        if (!response.isNullOrEmpty()) {
            response?.let { contactValue.value = response }
        } else {
            contactValue.value = emptyList() // Set to an empty list if no data is returned
            errorResponse.emit("No contacts found.") // Emit an error message
        }
    }

    private val errorResponse = MutableStateFlow<String?>(null)

    private val contactValue = MutableStateFlow(emptyList<Contact>())

    val contactResponse: StateFlow<List<Contact>?> =
        contactValue.makeResult(coroutineScope)

    private val apiService = RetrofitInstance.api
//    val contacts: MutableState<List<Contact>> = mutableStateOf(emptyList())
//    suspend fun getContacts(): List<Contact> {
//        return try {
//            Log.d("get data", "Fetching data from API...")
//            val response = apiService.getContacts()
//            Log.d("Raw Response", response.toString())
//            if (response.isNotEmpty()) {
//                contacts.value = response // Update the state
//            }
//            response // Return the result of the API call
//        } catch (e: Exception) {
//            Log.e("API Error", e.message ?: "Unknown error")
//            emptyList() // Return an empty list in case of error
//        }
//    }
}