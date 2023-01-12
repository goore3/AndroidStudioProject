package com.example.androidstudioproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androidstudioproject.DeviceFragment
import com.example.androidstudioproject.databinding.FragmentItemBinding
import com.example.androidstudioproject.placeholder.PlaceholderContent
import com.example.androidstudioproject.placeholder.PlaceholderContent.PlaceholderItem


/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyDeviceRecyclerViewAdapter(
    private var values: List<PlaceholderContent.PlaceholderItem>,
    val listener: DeviceFragment.MyInterface
) : RecyclerView.Adapter<MyDeviceRecyclerViewAdapter.ViewHolder>() {





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

        val item = values[position]
        holder.idView.text = item.mac
        holder.contentView.text = item.content

        holder.itemView.setOnClickListener(View.OnClickListener {
            listener.onClick(item.mac)

        })
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val idView: TextView = binding.itemNumber
        val contentView: TextView = binding.content
        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }



}

