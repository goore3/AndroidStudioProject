package com.example.androidstudioproject.BTControl

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.androidstudioproject.BTControl.BTMessageHandler
import com.example.androidstudioproject.DeviceFragment
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class BluetoothChatService (private val context: Context, private val mHandler: Handler){
    private val DEVICE: String = DeviceFragment::class.java.simpleName
    lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothAdapter: BluetoothAdapter

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
    // Give the new state to the Handler so the UI Activity can update
    var state: Int
        @Synchronized get() = mState
        @Synchronized private set(state) {
            mState = state
            mHandler.obtainMessage(BTMessageHandler.MESSAGE_STATE_CHANGE, state, -1).sendToTarget()
        }

    private var mConnectThread:  ConnectThread? = null
    private var mConnectedThread:  ConnectedThread? = null

    private var mState: Int = 0
    // Creates a single instance of the Object. Similar to 'Static'
    companion object {
        // Constants that indicate the current connection state
        const val STATE_NONE = 0       // we're doing nothing
        const val STATE_CONNECTING = 1 // now initiating an outgoing connection
        const val STATE_CONNECTED = 2  // now connected to a remote device
    }

    /**
     * Start the ConnectThread to begin connection with device
     *
     * @param device The BluetoothDevice that has been connected
     */
    fun connect(device: BluetoothDevice) {

        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread!!.cancel()
                mConnectThread = null
                Log.d(DEVICE, "THREAD CONNECT DELETED")
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel()
            mConnectedThread = null
        }

        // Start the thread to connect with the given device
        mConnectThread = ConnectThread(device)
        mConnectThread!!.start()

        state = STATE_CONNECTING
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    fun connected(socket: BluetoothSocket, device: BluetoothDevice) {
        // Cancel the thread that completed the connection
//        if (mConnectThread != null) {
//            mConnectThread!!.cancel();
//            mConnectThread = null;
//        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread!!.cancel();
            mConnectedThread = null;
        }


        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = ConnectedThread(socket, DEVICE)
        mConnectedThread!!.start()

        // Send the name of the connected device back to the UI Activity
        val msg = mHandler.obtainMessage(BTMessageHandler.MESSAGE_DEVICE_CONNECTED)
        val bundle = Bundle()
        if (ActivityCompat.checkSelfPermission(
                context,
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
        }
        bundle.putString(null, device.name)
        msg.data = bundle
        mHandler.sendMessage(msg)

        state = STATE_CONNECTED

    }
    fun write(buffer: ByteArray)
    {
        mConnectedThread!!.write(buffer)
    }
    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private var mmSocket: BluetoothSocket? = null
        init {
            var tmp: BluetoothSocket? = null
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (ActivityCompat.checkSelfPermission(
                        context!!.applicationContext,
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

                }
                val temp: Boolean = device.fetchUuidsWithSdp()
                var uuid: UUID? = null
                uuid = device.getUuids()[0].getUuid();

                tmp = device.createRfcommSocketToServiceRecord(uuid)
            } catch (e: IOException) {

            }

            mmSocket = tmp
        }


        public override fun run() {
            // Cancel discovery because it otherwise slows down the connection.
            if (ActivityCompat.checkSelfPermission(
                    context,
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
            bluetoothAdapter?.cancelDiscovery()

            mmSocket?.let { mmSocket ->
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect()
                Log.d(DEVICE, "The connection attempt succeeded")
                connected(mmSocket, device)

            }
        }


        // Closes the client socket and causes the thread to finish.
        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
                Log.e(DEVICE, "Could not close the client socket", e)
            }
        }
    }
    private inner class ConnectedThread(socket: BluetoothSocket, socketType: String) : Thread() {
        private var mmSocket: BluetoothSocket? = null
        private var mmInStream: InputStream? = null
        private var mmOutStream: OutputStream? = null

        init{
            Log.d(DEVICE, "create ConnectedThread: $socketType")
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                Log.e(DEVICE, "temp sockets not created", e)
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
            mState = STATE_CONNECTED
        }
        override fun run() {
            Log.i(DEVICE, "BEGIN mConnectedThread")
            var bytes: Int
            var buffer = ByteArray(5024)

            // Keep listening to the InputStream while connected
            while (mState === STATE_CONNECTED) {
                try {

                    // Read from the InputStream
                    bytes = mmInStream!!.read(buffer)

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(BTMessageHandler.MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget()
                    buffer = ByteArray(5024)


                } catch (e: IOException) {
                    Log.d(DEVICE, "disconnected", e)
//                    connectionLost()
                    break
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        fun write(buffer: ByteArray?) {
            try {
                mmOutStream!!.write(buffer)

                // Share the sent message back to the UI Activity
//                mHandler.obtainMessage(BTMessageHandler.MESSAGE_WRITE, -1, -1, buffer)
//                    .sendToTarget()
            } catch (e: IOException) {
                Log.d(DEVICE, "Exception during write", e)
            }
        }

        fun cancel() {
            try {
                mmSocket?.close()
            } catch (e: IOException) {
            }

        }
    }
}