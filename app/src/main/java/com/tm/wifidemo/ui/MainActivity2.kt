package com.tm.wifidemo.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.tm.wifidemo.R
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket


class MainActivity2 : AppCompatActivity() , Runnable {

    private lateinit var ds: MulticastSocket
    var multicastHost = "224.0.0.1"
    var receiveAddress: InetAddress? = null

    lateinit var packet: DatagramPacket

    private lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        initView()
        try {
            ds = MulticastSocket(8003)
            val buffer = ByteArray(1024)
            packet = DatagramPacket(buffer, buffer.size)
            receiveAddress = InetAddress.getByName(multicastHost)
            ds.joinGroup(receiveAddress)
            Thread(this).start()
        } catch (e1: Exception) {
            e1.printStackTrace()
        }
    }

    private fun initView() {
        tv = findViewById<TextView>(R.id.tv)
    }

    override fun run() {
        val buf = ByteArray(1024)
        val dp = DatagramPacket(buf, 1024)
        while (true) {
            try {
                ds.receive(dp)
                val s = String(packet.getData(), 0, packet.getLength())
                //Toast.makeText(this, new String(buf, 0, dp.getLength()), Toast.LENGTH_LONG);
                println("client ip : " + String(buf, 0, dp.getLength()))
                println("client ip : " + s)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

}