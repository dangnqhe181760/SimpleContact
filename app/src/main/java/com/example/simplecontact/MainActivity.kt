package com.example.simplecontact

import android.R
import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog


data class Contact2(
    val id: String = "",
    var name: String = "",
    var numbers: String = "",
    val emails: String = ""
)


class MainActivity : ComponentActivity() {
    val contacts: MutableState<List<Contact2>> = mutableStateOf(emptyList())
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
                    // Call HomePage which reacts to contact changes
                    HomePage()
                }
            }

            // Initialize contacts asynchronously outside the composable

            contacts.value = getNamePhoneDetails()

        }
    }

    @Composable
    @SuppressLint("Range")
    fun getNamePhoneDetails(): List<Contact2> {
        val names = ArrayList<Contact2>()
        val mApplication = LocalContext.current
        val cur = mApplication.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI, null,
            null, null, null
        )

        if (cur != null && cur.count > 0) {
            while (cur.moveToNext()) {
                val id =
                    cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.NAME_RAW_CONTACT_ID))
                Log.d("id", id)

                var number = ""
                val cur2 = mApplication.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(id), null
                )

                if (cur2 != null && cur2.moveToFirst()) {
                    number =
                        cur2.getString(cur2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    cur2.close()
                }

                val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                var email = ""
                val cur3 = mApplication.contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    arrayOf(id), null
                )

                if (cur3 != null && cur3.moveToFirst()) {
                    email =
                        cur3.getString(cur3.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
                    cur3.close()
                }

                names.add(Contact2(id, name, number, email))
            }
            cur.close()
        }
        return names
    }

    @Composable
    fun HomePage() {
        val contactList by remember { contacts } // Observe the state for changes
        var contactToRemove by remember { mutableStateOf<Contact2?>(null) }
        var contactToUpdate by remember { mutableStateOf<Contact2?>(null) }
        val showRemoveContactDialog = remember { mutableStateOf(false) }
        val showUpdateContactDialog = remember { mutableStateOf(false) }
        val showLogoutDialog = remember { mutableStateOf(false) }
        if (showLogoutDialog.value) {
            CustomDialog(
                onAddValueSuccess = { addValue ->
                    showLogoutDialog.value = false
                    val updatedContacts = contacts.value.toMutableList()
                    updatedContacts.add(addValue)
                    contacts.value = updatedContacts
                    val cpo = ArrayList<ContentProviderOperation>()
                    val rawContactId = cpo.size
                    cpo.add(
                        ContentProviderOperation.newInsert(
                            ContactsContract.RawContacts.CONTENT_URI
                        )
                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                            .build()
                    )
                    cpo.add(
                        ContentProviderOperation.newInsert(
                            ContactsContract.Data.CONTENT_URI
                        )
                            .withValueBackReference(
                                ContactsContract.RawContacts.Data.RAW_CONTACT_ID,
                                rawContactId
                            )
                            .withValue(
                                ContactsContract.RawContacts.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                            )
                            .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                                addValue.name
                            )
                            .withValue(
                                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                                ""
                            )
                            .build()
                    )
                    cpo.add(
                        ContentProviderOperation.newInsert(
                            ContactsContract.Data.CONTENT_URI
                        )
                            .withValueBackReference(
                                ContactsContract.RawContacts.Data.RAW_CONTACT_ID,
                                rawContactId
                            )
                            .withValue(
                                ContactsContract.RawContacts.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                            )
                            .withValue(
                                ContactsContract.CommonDataKinds.Phone.NUMBER,
                                addValue.numbers
                            )
                            .withValue(
                                ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                            )
                            .build()
                    )
                    cpo.add(
                        ContentProviderOperation.newInsert(
                            ContactsContract.Data.CONTENT_URI
                        )
                            .withValueBackReference(
                                ContactsContract.RawContacts.Data.RAW_CONTACT_ID,
                                rawContactId
                            )
                            .withValue(
                                ContactsContract.RawContacts.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                            )
                            .withValue(ContactsContract.CommonDataKinds.Email.DATA, addValue.emails)
                            .withValue(
                                ContactsContract.CommonDataKinds.Email.TYPE,
                                ContactsContract.CommonDataKinds.Email.TYPE_WORK
                            )
                            .build()
                    )
                    contentResolver.applyBatch(ContactsContract.AUTHORITY, cpo)

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

            contactToRemove?.let { contact2 ->
                DialogExample(
                    showDialog = showRemoveContactDialog,
                    onConfirm = {
                        val updatedContacts = contacts.value.toMutableList()
                        val cpo = ArrayList<ContentProviderOperation>()
                        updatedContacts.remove(contact2)
                        Log.d("deleting id", contact2.id)
                        cpo.add(
                            ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                                .withSelection(
                                    ContactsContract.RawContacts.CONTACT_ID + "=?",
                                    arrayOf(contact2.id)
                                ).build()
                        )
                        contentResolver.applyBatch(ContactsContract.AUTHORITY, cpo)
                        contacts.value = updatedContacts.toList()
                    },
                    contactName = contact2.name
                )
            }

            contactToUpdate?.let { contact3 ->
                UpdateDialog(
                    showDialog = showUpdateContactDialog,
                    onAddValueSuccess = { addValue ->
                        showUpdateContactDialog.value = false
                        val updatedContacts = contacts.value.toMutableList()
                        Log.d("updating id", contact3.id)
                        contact3.numbers = addValue.numbers
                        contact3.name = addValue.name
                        updatedContacts[updatedContacts.indexOf(contact3)] = contact3
                        contacts.value = updatedContacts.toList()
                        val cpo = ArrayList<ContentProviderOperation>()
                        Log.d("name", addValue.name)
                        Log.d("number", addValue.numbers)
                        cpo.add(
                            ContentProviderOperation
                                .newUpdate(ContactsContract.Data.CONTENT_URI)
                                .withSelection(
                                    ContactsContract.Data.RAW_CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?",
                                    arrayOf(
                                        contact3.id.toString(),
                                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                                    )
                                )
                                .withValue(
                                    ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                                    null
                                ) // Set to null if you want to clear
                                .withValue(
                                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                                    addValue.name
                                )
                                .build()
                        )

                        cpo.add(
                            ContentProviderOperation
                                .newUpdate(ContactsContract.Data.CONTENT_URI)
                                .withSelection(
                                    ContactsContract.Data.RAW_CONTACT_ID + " = ? AND "
                                            + ContactsContract.Data.MIMETYPE + " = ?",
                                    arrayOf(
                                        contact3.id.toString(),
                                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                                    )
                                )
                                .withValue(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    addValue.numbers
                                ) // Access the first number in the list
                                .build()
                        )
                        try {
                            contentResolver.applyBatch(ContactsContract.AUTHORITY, cpo)
                            Log.d("done updating id", contact3.id)
                        } catch (e: Exception) {
                            Log.e("applyBatch", "Error updating contact", e)
                        }
                        contactToUpdate = null
                    },
                )
            }

            // Pass the updated contact list
            DisplayList(
                contacts = contactList,
                onClick = { contact2 ->
                    contactToRemove = contact2
                    showRemoveContactDialog.value = true
                    Log.d("click", contact2.name)
                },
                onLongClickLabel = { contact3 ->
                    contactToUpdate = contact3
                    showUpdateContactDialog.value = true
                    Log.d("long click", contact3.name)
                },
            )
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

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun DisplayList(
        contacts: List<Contact2>,
        onClick: (Contact2) -> Unit,
        onLongClickLabel: (Contact2) -> Unit
    ) {
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

            LazyColumn {
                items(contacts.size) { index ->
                    Text(
                        text = contacts[index].name + " - " + contacts[index].numbers + " - " + contacts[index].emails,
                        modifier = Modifier
                            .padding(16.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        // perform some action here..
                                        onLongClickLabel.invoke(contacts[index])
                                    },
                                    onTap = {
                                        // perform some action here..
                                        onClick.invoke(contacts[index])
                                    }
                                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDialog(onAddValueSuccess: (Contact2) -> Unit, showDialog: MutableState<Boolean>) {
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
                                onAddValueSuccess.invoke(
                                    Contact2(
                                        "",
                                        name.value,
                                        phoneNumber.value,
                                        ""
                                    )
                                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDialog(onAddValueSuccess: (Contact2) -> Unit, showDialog: MutableState<Boolean>) {
    val txtFieldError = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val phoneNumber = remember { mutableStateOf("") }
    if (showDialog.value) {
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
                                text = "Update contact",
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
                                    onAddValueSuccess.invoke(
                                        Contact2(
                                            "",
                                            name.value,
                                            phoneNumber.value,
                                            ""
                                        )
                                    )
                                    Log.d("name from dialog", name.value)
                                    Log.d("number from dialog", phoneNumber.value)
                                    showDialog.value = false
                                },
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(text = "Done")
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(modifier = Modifier.padding(40.dp, 0.dp, 40.dp, 0.dp)) {
                            Button(
                                onClick = {
                                    showDialog.value = false
                                },
                                shape = RoundedCornerShape(50.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(text = "Cancel")
                            }
                        }
                    }
                }
            }
        }
    }

}
