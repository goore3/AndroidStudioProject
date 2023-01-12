package com.example.androidstudioproject.FITActivities

import android.os.*
import android.os.Build.DEVICE
import androidx.lifecycle.ViewModelProvider
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidstudioproject.DeviceFragment
import com.example.androidstudioproject.DeviceFragmentDirections


import com.example.androidstudioproject.MainActivity
import com.example.androidstudioproject.MainActivity.Companion.viewModel
import com.example.androidstudioproject.R
import com.example.androidstudioproject.databinding.FragmentFitActivityBinding
import java.io.File

class FitActivityFragment : Fragment() {
    private var m_adapter: FitActivityRecyclerViewAdapter?= null

    companion object {
        // Message types sent from the BluetoothChatService Handler
        const val MESSAGE_READ = 1
        private var buffer = byteArrayOf()
        private var file_package: Boolean = false
        private var sum_bytes = 0
        private var packets_received = 0
        private var init_packets_received =0
        var missing_files = mutableListOf<String>()

        fun newInstance(columnCount: Int) =
            FitActivityFragment().apply {
                arguments = Bundle().apply {
                    putInt(DeviceFragment.ARG_COLUMN_COUNT, columnCount)
                }
            }
        val mHandler = object : Handler(Looper.getMainLooper())
        {
            override fun handleMessage(msg: Message) {

                if (msg.what == MESSAGE_READ)
                {
                    var message_data = msg.obj as String
                    if(message_data[0] == 'R' && message_data[1] == '1')
                    {
                        var startIndex = 2

                        while(true)
                        {
                            val index = message_data.indexOf(".fit", startIndex)
                            if(index == -1)
                            {
                                break
                            }
                            Log.d(DEVICE, "File name: " + message_data.substring(startIndex, index+4))
                            if(viewModel.check_existing(message_data.substring(startIndex, index+4)) == false)
                            {
                                missing_files.add(message_data.substring(startIndex, index+4))
                            }

                            startIndex = index + 4
                        }
                        Log.d(DEVICE, "Missing files: " +  missing_files)

                        request_file(0)

                        Log.d(DEVICE, "All files: " + viewModel.FIT_files.value)

                        file_package = true


                    }
                    else if(message_data[0] == 'R' && message_data[1] == '2') //if first file data received
                    {
                        /*Set flag we are waiting for more data*/
                        file_package = true

                        /*Sum of data received*/
                        sum_bytes += msg.arg1
                        packets_received++

                        /*Remove first 2 bytes*/
//                        Log.d(DEVICE, "Message: " + message_data)

                        message_data = message_data.substring(2,msg.arg1)
//                        Log.d(DEVICE, "!!!!!!!! PACKET NR: " + packets_received)
//                        Log.d(DEVICE, "PACKET BYTES: " + msg.arg1)
//
//                        Log.d(DEVICE, "TOTAL BYTES: " + sum_bytes)

                        /*Create file and add data to the file*/
                        add_data_to_file(missing_files[0], message_data, msg.arg1)


                        /*Send request for more data*/
                        request_file(packets_received)
                    }
                    else if(message_data[0] == 'R' && message_data[1] == '3') //if all file is received
                    {
                        /*End of file*/
                        file_package = false

                        Log.d(DEVICE, "TOTAL AMOUNT OF RECEIVED BYTES: " + sum_bytes)
                        Log.d(DEVICE, "TOTAL AMOUNT OF RECEIVED PACKETS: " + packets_received)

                        sum_bytes = 0
                        packets_received = 0


                        /*Close file*/

                        /*Delete missing file from the list of missing files*/
                        viewModel.insertFile(missing_files[0])
                        missing_files.removeFirst()


                        /*File received message*/
                        buffer = ("R3\n").toByteArray(Charsets.UTF_8)
                        DeviceFragment.mChatService?.write(buffer)

                        /*Request more files if needed*/
                        request_file(0)
                    }else if(file_package == true) //if message received and flag is set then it is file package data
                    {
                        /*Sum of data received*/
                        sum_bytes += msg.arg1

                        /*Remove empty bytes*/
                        message_data = message_data.substring(0, msg.arg1)

//                        Log.d(DEVICE, "######### SPLITED PACKET OF NR: " + packets_received)
//                        Log.d(DEVICE, "PACKET BYTES: " + msg.arg1)
//                        Log.d(DEVICE, "TOTAL BYTES: " + sum_bytes)

                        /*Append data*/
                        add_data_to_file(missing_files[0], message_data, msg.arg1)

                    }

                }

            }

            private fun request_file(packet: Int)
            {
                /*There are missing files*/
                if(missing_files.size != 0)
                {
                    /*Send request to receive specific file*/
                    buffer = ("R2"+packet.toString()+"!"+missing_files[0] +"\n").toByteArray(Charsets.UTF_8)
                    DeviceFragment.mChatService?.write(buffer)
                }

            }
            private fun add_data_to_file(filename: String, data: String, bytes: Int)
            {
                // Create file object
//                Log.d(DEVICE, "File name: " + filename)

                val file = File(
                    MainActivity.appContext.filesDir
                    , filename)
                // Create file
                val isCreated = file.createNewFile()
                if (isCreated)
                {
                    Log.d(DEVICE, "File is created")

                }
                else
                {
//                    Log.d(DEVICE, " File is not created")
                }
                file.appendBytes(data.toByteArray())

//                Log.d(DEVICE, "Bytes wrritten: ")
//                viewModel.insertFile(file.name)
//                file.delete()
            }
        }
    }
    public interface MyInterface {
        fun onClick(mac :String)
    }
    private var _binding: FragmentFitActivityBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        m_adapter = FitActivityRecyclerViewAdapter( object: MyInterface {
            override fun onClick(file :String) {
                Log.d(DEVICE, file)
//MOVE TO THE FILE PARSING PROCESS AND DISPLAYING//
            }
        })
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = m_adapter
        viewModel.FIT_files.observe(viewLifecycleOwner, Observer {
            m_adapter?.update_list(viewModel.FIT_files)

        })
        m_adapter?.update_list(viewModel.FIT_files)
        // TODO: Use the ViewModel
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Retrieve and inflate the layout for this fragment
        _binding = FragmentFitActivityBinding.inflate(inflater, container, false)
        val view = binding.root
        val scan_button = view.findViewById<Button>(R.id.Scan_button)
        (activity as AppCompatActivity).supportActionBar!!.setDisplayHomeAsUpEnabled(false)

        return view
    }
}