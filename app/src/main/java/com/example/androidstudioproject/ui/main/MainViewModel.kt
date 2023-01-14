package com.example.androidstudioproject.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainViewModel : ViewModel() {
    private var _count = MutableLiveData(0)
    var count: LiveData<Int> = _count
    private var _text = MutableLiveData("bruh")
    var text: LiveData<String> = _text
    private var _client = MutableLiveData<GoogleSignInClient>()
    var client: LiveData<GoogleSignInClient> = _client
    private var _auth = MutableLiveData<FirebaseAuth>()
    var auth: LiveData<FirebaseAuth> = _auth
    private var _user = MutableLiveData<FirebaseUser?>()
    var user: LiveData<FirebaseUser?> = _user

    fun setUser(user: FirebaseUser?) {
        _user.value = user
    }

    fun unsetUser() {
        _user.value = null
    }

    fun setAuth(auth: FirebaseAuth) {
        _auth.value = auth
    }

    fun setClient(client: GoogleSignInClient) {
        _client.value = client
    }

    fun updateText(text: String) {
        _text.value = text
    }

    fun updateCount() {
        _count.value = count.value?.plus(1)
    }

    fun checkUser(): Boolean {
        return user.value != null
    }
}