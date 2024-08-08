package com.example.wayfindr.home.models

import java.io.Serializable

class AllEvents (
    var eventsId: String="",
    val eventsCategory: String = "",
    val eventsCity: String = "",
    val eventsDetail: String = "",
    val eventsImage: String = "",
    val eventsLocation: String? = "",
    val eventsName: String = ""
):Serializable