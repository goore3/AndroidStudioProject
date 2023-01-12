package com.example.androidstudioproject.FITActivities

import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidstudioproject.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class FitActivityViewModel : ViewModel() {

    val FIT_files: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val file = File( "TEST")
    init {
        //read all file names
        val files: Array<String> = MainActivity.appContext.fileList()

        FIT_files.value = ArrayList()
        (FIT_files.value as ArrayList<String>).addAll(files)
    }
    fun insertFile(data: String) = viewModelScope.launch(Dispatchers.IO) {
        FIT_files.value?.add(data)
        FIT_files.postValue(FIT_files.value)
    }
    fun check_existing(data: String): Boolean?
    {
        return FIT_files.value?.contains(data)
    }
}