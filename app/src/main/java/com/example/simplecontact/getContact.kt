//package com.example.simplecontact
//
//import android.annotation.SuppressLint
//import android.app.Application
//import android.content.ContentResolver
//import android.os.AsyncTask
//import android.os.Build
//import android.os.Bundle
//import android.provider.ContactsContract
//import android.text.TextUtils
//import android.util.Log
//import android.util.Patterns
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.enableEdgeToEdge
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.tooling.preview.Preview
//import com.example.simplecontact.ui.theme.SimpleContactTheme
//
//class getContact : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContent {
//            SimpleContactTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
//        val myList = getNamePhoneDetails()
//        for (i in myList) {
//            Log.d("name", i.toString())
//        }
//    }
//}
//
//    @SuppressLint("Range")
//    fun getNamePhoneDetails(): List<Contact2> {
//        val names = ArrayList<Contact2>()
//        val mApplication = Application()
//        val cr = mApplication.contentResolver
//        val cur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
//            null, null, null)
//        if (cur!!.count > 0) {
//            while (cur.moveToNext()) {
//                val id = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NAME_RAW_CONTACT_ID))
//                val name = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
//                val number = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//                val email = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
//                names.add(Contact2(id, name, number,email))
//            }
//        }
//        return names
//    }
//
//
////private fun getPhoneContacts(): ArrayList<Contact2> {
////    val contactsList = ArrayList<Contact2>()
////    val mApplication = Application()
////    val contactsCursor = mApplication.contentResolver?.query(
////        ContactsContract.Contacts.CONTENT_URI,
////        null,
////        null,
////        null,
////        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")
////    if (contactsCursor != null && contactsCursor.count > 0) {
////        val idIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts._ID)
////        val nameIndex = contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
////        while (contactsCursor.moveToNext()) {
////            val id = contactsCursor.getString(idIndex)
////            val name = contactsCursor.getString(nameIndex)
////            if (name != null) {
////                contactsList.add(Contact2(id, name))
////            }
////        }
////        contactsCursor.close()
////    }
////    return contactsList
////}
////
////
////private fun getContactNumbers(): HashMap<String, ArrayList<String>> {
////    val contactsNumberMap = HashMap<String, ArrayList<String>>()
////    val mApplication = Application()
////    val phoneCursor: Cursor? = mApplication.contentResolver.query(
////        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
////        null,
////        null,
////        null,
////        null
////    )
////    if (phoneCursor != null && phoneCursor.count > 0) {
////        val contactIdIndex = phoneCursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
////        val numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
////        while (phoneCursor.moveToNext()) {
////            val contactId = phoneCursor.getString(contactIdIndex)
////            val number: String = phoneCursor.getString(numberIndex)
////            //check if the map contains key or not, if not then create a new array list with number
////            if (contactsNumberMap.containsKey(contactId)) {
////                contactsNumberMap[contactId]?.add(number)
////            } else {
////                contactsNumberMap[contactId] = arrayListOf(number)
////            }
////        }
////        //contact contains all the number of a particular contact
////        phoneCursor.close()
////    }
////    return contactsNumberMap
////}
////
////private fun getContactEmails(): HashMap<String, ArrayList<String>> {
////    val contactsEmailMap = HashMap<String, ArrayList<String>>()
////    val mApplication = Application()
////    val emailCursor = mApplication.contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
////        null,
////        null,
////        null,
////        null)
////    if (emailCursor != null && emailCursor.count > 0) {
////        val contactIdIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
////        val emailIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
////        while (emailCursor.moveToNext()) {
////            val contactId = emailCursor.getString(contactIdIndex)
////            val email = emailCursor.getString(emailIndex)
////            //check if the map contains key or not, if not then create a new array list with email
////            if (contactsEmailMap.containsKey(contactId)) {
////                contactsEmailMap[contactId]?.add(email)
////            } else {
////                contactsEmailMap[contactId] = arrayListOf(email)
////            }
////        }
////        //contact contains all the emails of a particular contact
////        emailCursor.close()
////    }
////    return contactsEmailMap
////}
////
////suspend fun getContacts(): List<Contact2> = coroutineScope {
////    val names = async(Dispatchers.IO) { getPhoneContacts() }.await()
////    val numbers = async(Dispatchers.IO) { getContactNumbers() }.await()
////    val emails = async(Dispatchers.IO) { getContactEmails() }.await()
////
////    names.map { contact ->
////        val contactNumbers = numbers[contact.id].orEmpty()
////        val contactEmails = emails[contact.id].orEmpty()
////        Contact2(contact.id, contact.name, contactNumbers, contactEmails)
////    }
////}
//
//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    Text(
//        text = "Hello $name!",
//        modifier = modifier
//    )
//}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    SimpleContactTheme {
//        Greeting("Android")
//    }
//}