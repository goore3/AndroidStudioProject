package com.example.androidstudioproject.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.androidstudioproject.R
import com.example.androidstudioproject.databinding.FragmentMainBinding
import com.google.firebase.auth.FirebaseUser

var debugTag = "DEBUG"

class MainFragment : Fragment(R.layout.fragment_main) {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val viewModel: MainViewModel by viewModels(
        ownerProducer = { requireActivity() }
    )
    private lateinit var binding: FragmentMainBinding
    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        viewModel.user.observe(viewLifecycleOwner, Observer{
            if(viewModel.checkUser()) {
                binding.labelUsername.text = "Welcome user " + viewModel.user.value!!.email.toString()
            } else {
                binding.labelUsername.text = ""
            }
        })
        binding.button.setOnClickListener {
            Log.d(debugTag,"Button Pressed")
            val action = MainFragmentDirections.actionMainFragmentToTestFragment()
            findNavController().navigate(action)
        }
        val view = binding.root
        return view
    }
}