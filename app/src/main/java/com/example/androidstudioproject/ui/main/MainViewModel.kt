package com.example.androidstudioproject.ui.main

import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var count = 0

    fun updateCount() {
        ++count
    }
}