package com.example.wayfindr.home.models

import java.io.Serializable

data class EventModel(
    var eventId: String = "",
    var eventsName: String = "",
    var eventsLocation: String = "",
    var eventsImage: String = "",
    var eventsDetail: String = ""
) : Serializable

