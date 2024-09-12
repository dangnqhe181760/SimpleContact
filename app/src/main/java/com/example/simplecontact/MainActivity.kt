package com.example.simplecontact

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import android.R
import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.provider.ContactsContract
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.DialogProperties

data class Contact(var name: String, var phoneNumber: String)

data class Contact2(
    val id: String = "",
    val name: String = "",
    val numbers: String = "",
    val emails: String = ""
)


class MainActivity<ImageVector> : ComponentActivity() {
    private var contacts = mutableStateOf(
        listOf(
            Contact("John Doe", "123-456-7890"),
            Contact("Jane Smith", "987-654-3210"),
            Contact("Bob Johnson", "555-555-5555")
        )
    )

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(
                modifier = Modifier.semantics {
                    testTagsAsResourceId = true
                },
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
            ) { padding ->
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .navigationBarsPadding()
                        .consumeWindowInsets(padding)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Horizontal,
                            ),
                        ),
                ) {
                    HomePage()
                }
            }
            val myList = getNamePhoneDetails()
            for (i in myList) {
                Log.d("name", i.toString())
            }
        }
    }

    @Composable
    @SuppressLint("Range")
    fun getNamePhoneDetails(): List<Contact2> {
        val names = ArrayList<Contact2>()
        val context = LocalContext.current
        val cur = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
            null, null, null)
        if (cur!!.count > 0) {
            while (cur.moveToNext()) {
                val id = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NAME_RAW_CONTACT_ID))
                val name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val email = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
                names.add(Contact2(id, name, number, email))
            }
        }
        return names
    }

    @Composable
    fun HomePage() {
        var contactToRemove by remember { mutableStateOf<Contact?>(null) }
        val showRemoveContactDialog = remember { mutableStateOf(false) }
        val showLogoutDialog = remember { mutableStateOf(false) }
        if (showLogoutDialog.value) {
            CustomDialog(
                onAddValueSuccess = { addValue ->
                    showLogoutDialog.value = false
                    val updatedContacts = contacts.value.toMutableList()
                    updatedContacts.add(addValue)
                    contacts.value = updatedContacts
                },
                showDialog = showLogoutDialog
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                showLogoutDialog.value = true
            }) {
                Text(text = "Add new contact")
            }

            contactToRemove?.let { contact ->
                DialogExample(
                    showDialog = showRemoveContactDialog,
                    onConfirm = {
                        val updatedContacts = contacts.value.toMutableList()
                        updatedContacts.remove(contact)
                        contacts.value = updatedContacts
                    },
                    contactName = contact.name
                )
            }

            DisplayList(contacts = contacts.value, onContactClick = { contact ->
                contactToRemove = contact
                showRemoveContactDialog.value = true
            })
        }
    }

    @Composable
    fun DialogExample(
        showDialog: MutableState<Boolean>,
        onConfirm: () -> Unit,
        contactName: String
    ) {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text("Confirm Removal") },
                text = { Text("Are you sure you want to remove $contactName?") },
                confirmButton = {
                    Button(onClick = {
                        onConfirm()
                        showDialog.value = false
                    }) {
                        Text("Remove")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }



    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CustomDialog(onAddValueSuccess: (Contact) -> Unit, showDialog: MutableState<Boolean>) {
        val txtFieldError = remember { mutableStateOf("") }
        val name = remember { mutableStateOf("") }
        val phoneNumber = remember { mutableStateOf("") }
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Add new contact",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "",
                                tint = colorResource(android.R.color.darker_gray),
                                modifier = Modifier
                                    .width(30.dp)
                                    .height(30.dp)
                                    .clickable {
                                        showDialog.value = false
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        //TextField(value = name.value, onValueChange = { name.value = it })
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(
                                        width = 2.dp,
                                        color = colorResource(id = if (txtFieldError.value.isEmpty()) R.color.holo_green_light else R.color.holo_red_dark)
                                    ),
                                    shape = RoundedCornerShape(50)
                                ),
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            placeholder = { Text(text = "Enter name") },
                            value = name.value,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            onValueChange = {
                                name.value = it.take(10)
                            })

                        Spacer(modifier = Modifier.height(20.dp))
                        //TextField(value = phoneNumber.value, onValueChange = { phoneNumber.value = it })
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(
                                        width = 2.dp,
                                        color = colorResource(id = if (txtFieldError.value.isEmpty()) R.color.holo_green_light else R.color.holo_red_dark)
                                    ),
                                    shape = RoundedCornerShape(50)
                                ),
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            placeholder = { Text(text = "Enter phone number") },
                            value = phoneNumber.value,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            onValueChange = {
                                phoneNumber.value = it.take(10)
                            })

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
                            Button(
                                onClick = {
                                    if (name.value.isEmpty()) {
                                        txtFieldError.value = "Field can not be empty"
                                        return@Button
                                    }
                                    //chỗ này trả data ngược ra ngoài đọc thêm về high order fucntion
                                    onAddValueSuccess.invoke(Contact(name.value, phoneNumber.value))
                                },
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(text = "Done")
                            }
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun DisplayList(contacts: List<Contact>, onContactClick: (Contact) -> Unit) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Contacts",
                modifier = Modifier.padding(16.dp),
                style = TextStyle(
                    color = Color.Black,
                    fontSize = TextUnit(value = 20.0F, type = TextUnitType.Sp)
                ),
                fontWeight = FontWeight.Black
            )
            LazyColumn() {
                items(contacts.size) { index ->
                    Text(
                        text = contacts[index].name + " - " + contacts[index].phoneNumber,
                        modifier = Modifier
                            .padding(16.dp)
                            .clickable {
                                Log.d("click", "click")
                                onContactClick(contacts[index])
                            },
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = TextUnit(value = 20.0F, type = TextUnitType.Sp)
                        ),
                        fontWeight = FontWeight.Black
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}