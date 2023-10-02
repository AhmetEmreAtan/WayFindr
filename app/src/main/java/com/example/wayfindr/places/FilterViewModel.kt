package com.example.wayfindr.places

import androidx.lifecycle.ViewModel

class FilterViewModel : ViewModel() {
    var selectedDistance: Int = 0
    var isPaidSelected: Boolean = false
    var isFreeSelected: Boolean = false
    var selectedCategory: Int = -1
}