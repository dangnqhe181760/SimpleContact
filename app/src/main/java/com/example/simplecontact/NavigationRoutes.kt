package com.example.simplecontact

sealed class NavigationRoutes(val route: String) {
    object MainContacts : NavigationRoutes("main_contacts")
    object ApiContacts : NavigationRoutes("api_contacts")

    // Route with a dynamic argument (like contactId)
    object ApiDetailContact : NavigationRoutes("api_detail_contact/{contactId}") {
        fun createRoute(contactId: String): String = "api_detail_contact/$contactId"
    }
}
