package com.example.androidstudioproject.FITActivities

import android.annotation.SuppressLint
import android.os.Build.DEVICE
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.androidstudioproject.FITActivities.FitActivityFragment
import com.example.androidstudioproject.DeviceFragment
import com.example.androidstudioproject.databinding.FragmentItemBinding
import java.io.File
import java.nio.file.Files

class FitActivityRecyclerViewAdapter
    (
    val listener: FitActivityFragment.MyInterface

            ): RecyclerView.Adapter<FitActivityRecyclerViewAdapter.ViewHolder>(){
    private val allFiles = mutableListOf<String>()

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content
        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = allFiles[position]
        holder.idView.text = item
        holder.itemView.setOnClickListener(View.OnClickListener {
            listener.onClick(item)
        })
}

    override fun getItemCount(): Int {
        return allFiles.size
    }


    @SuppressLint("NotifyDataSetChanged")
    fun update_list(newData: MutableLiveData<ArrayList<String>>)
    {
        allFiles.clear()
        allFiles.addAll(newData.value!!)
        notifyDataSetChanged()
    }
}