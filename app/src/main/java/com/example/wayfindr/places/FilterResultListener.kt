package com.example.wayfindr.places

import PlaceModel

interface FilterResultListener {
    fun onFilterResult(places:List<PlaceModel>)
}