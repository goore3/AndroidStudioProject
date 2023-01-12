package com.example.androidstudioproject

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.*
import android.os.Build.DEVICE
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidstudioproject.BTControl.BTMessageHandler
import com.example.androidstudioproject.BTControl.BluetoothChatService
import com.example.androidstudioproject.MainActivity
import com.example.androidstudioproject.databinding.FragmentItemListBinding
import com.example.androidstudioproject.MyDeviceRecyclerViewAdapter
import com.example.androidstudioproject.placeholder.PlaceholderContent
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


/**
 * A fragment representing a list of Items.
 */
class DeviceFragment : Fragment() {
    private var columnCount = 1
    lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter
    private var m_adapter: MyDeviceRecyclerViewAdapter?= null
    private val DEVICE: String = DeviceFragment::class.java.simpleName
    private var _binding: FragmentItemListBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    // Keeps track of which LayoutManager is in use for the [RecyclerView]

    // Name for the SDP record when creating server socket
    private val NAME = "BluetoothChatActivity"

    // Unique UUID for this application
    private val MY_UUID = UUID.fromString("fa98c1d1-afac-22de-9a49-0900200c9a77")

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //granted
        }else{
            //deny
        }
    }

    public interface MyInterface {
        fun onClick(mac :String)
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action

            when(action) {
                BluetoothDevice.ACTION_FOUND -> {

                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (ActivityCompat.checkSelfPermission(
                            context.applicationContext,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
//                        return
                    }
                    val deviceName = device?.name
                    val deviceHardwareAddress = device?.address // MAC address
                    if(device?.name != null){
                        val deviceName = device.name
                        if(device?.address != null)
                        {
                            val deviceHardwareAddress = device.address // MAC address
                            PlaceholderContent.addItem(PlaceholderContent.createPlaceholderItem(deviceHardwareAddress, deviceName))
                            m_adapter?.notifyDataSetChanged()
                            Log.d(DEVICE    , "Device name: " + deviceName+ "Device MAC" + deviceHardwareAddress + " UUID: " + device.uuids )

                        }
                    }
                }
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }

        //Ask for permission
        if (!bluetoothAdapter.isEnabled) {
//            bluetoothAdapter.enable()
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
           }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        m_adapter = MyDeviceRecyclerViewAdapter(PlaceholderContent.ITEMS, object: MyInterface {
            override fun onClick(mac :String) {
                Log.d(DEVICE, mac)
                val device = bluetoothAdapter.getRemoteDevice(mac)
                mChatService!!.connect(device)
            }
        })
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = m_adapter

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Retrieve and inflate the layout for this fragment
        _binding = FragmentItemListBinding.inflate(inflater, container, false)
        val view = binding.root
        val scan_button = view.findViewById<Button>(R.id.Scan_button)

        scan_button.setOnClickListener()
        {
            var test:Boolean = false

            //Start discovery
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            test = bluetoothAdapter.startDiscovery()
            Log.d(DEVICE, "SCAN STATUS: " + test )

            if(test == false)
            {
                Toast.makeText(context, "Wait", Toast.LENGTH_SHORT).show()


            }else if(test == true)
            {
                PlaceholderContent.clear(PlaceholderContent.ITEMS)
                m_adapter?.notifyDataSetChanged()
                // Register for broadcasts when a device is discovered.
                val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                context?.registerReceiver(receiver, filter);

                arguments?.let {
                    columnCount = it.getInt(ARG_COLUMN_COUNT)
                }
                // Initialize the BluetoothChatService to perform bluetooth connections
                mChatService = BluetoothChatService(requireContext().applicationContext, BTMessageHandler.mHandler)
                Toast.makeText(context, "Scan started", Toast.LENGTH_SHORT).show()
            }
        }
        val activity_button = view.findViewById<Button>(R.id.Activity_button)
        activity_button.setOnClickListener()
        {
            val action = DeviceFragmentDirections.actionDeviceFragmentToFitActivityFragment()
            view.findNavController().navigate(action)
        }
        return view

    }


    override fun onDestroy() {

        super.onDestroy()
        try
        {
            context?.unregisterReceiver(receiver)

        }catch (e: Exception)
        {

        }


    }
    companion object {
        const val MESSAGE_DEVICE_CONNECTED = 1
        var mChatService: BluetoothChatService? = null
        private var buffer = byteArrayOf()
        val mHandler = object : Handler(Looper.getMainLooper())
        {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DEVICE_CONNECTED)
                {
                    Toast.makeText(MainActivity.appContext, "Device connected", Toast.LENGTH_SHORT).show()
                    /*Request all files*/
                    buffer = "R1\n".toByteArray(Charsets.UTF_8)
                    mChatService?.write(buffer)

                }

            }
        }

        val EXTRA_ADDRESS: String = "Device_address"

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            DeviceFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}