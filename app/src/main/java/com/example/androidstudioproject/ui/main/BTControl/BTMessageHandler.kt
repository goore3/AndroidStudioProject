package com.example.androidstudioproject.BTControl

import android.os.Build.DEVICE
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Toast
import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import com.example.androidstudioproject.DeviceFragment
import com.example.androidstudioproject.FITActivities.FitActivityFragment
import com.example.androidstudioproject.MainActivity
import com.google.android.gms.tasks.Tasks.call

class BTMessageHandler  {

    companion object
    {
        // Message types sent from the BluetoothChatService Handler
        const val MESSAGE_STATE_CHANGE = 1
        const val MESSAGE_READ = 2
        const val MESSAGE_WRITE = 3
        const val MESSAGE_DEVICE_CONNECTED = 4
        const val MESSAGE_TOAST = 5
        const val TOAST = "toast"

        val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                var writeMessage = ""

                if(msg.obj != null)
                {
                    val writeBuf = msg.obj as ByteArray
                    writeMessage = String(writeBuf)
                }


                if (msg.what == MESSAGE_DEVICE_CONNECTED)
                {

                    Log.d(DEVICE, "Device connected")
                    DeviceFragment.mHandler.obtainMessage(DeviceFragment.MESSAGE_DEVICE_CONNECTED, -1, -1, writeMessage)
                        .sendToTarget()

                }
                else if (msg.what == MESSAGE_READ)
                {

//                    Log.d(DEVICE, "Message received: " + writeMessage)
                    FitActivityFragment.mHandler.obtainMessage(FitActivityFragment.MESSAGE_READ, msg.arg1, -1, writeMessage)
                        .sendToTarget()
                }else if (msg.what == MESSAGE_WRITE)
                {
                    Log.d(DEVICE, "Message write: " + msg.obj)
//                    FitActivityFragment.mHandler.obtainMessage(FitActivityFragment.MESSAGE_READ, -1, -1, msg.obj)
//                        .sendToTarget()
                }

            }
        }

    }

}