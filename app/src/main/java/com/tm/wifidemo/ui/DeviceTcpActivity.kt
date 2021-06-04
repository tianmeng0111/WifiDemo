package com.tm.wifidemo.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.tm.wifidemo.R
import com.tm.wifidemo.tcp.TcpClient
import com.tm.wifidemo.tcp.TcpServer
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DeviceTcpActivity : AppCompatActivity() {

    private val TAG = "DeviceTcpActivity"

    private lateinit var tv: EditText

    private val PORT = 4444

    private lateinit var mSocket: Socket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_tcp)

        val ips: ArrayList<String>? = intent?.getStringArrayListExtra("ips")

        val btn = findViewById<Button>(R.id.btn_connect)
        val btnSend = findViewById<Button>(R.id.btn_send_msg)
        tv = findViewById<EditText>(R.id.et_msg)
        btn.text = "connect to " + ips?.size + "个ip"
        btn.setOnClickListener {
            //开启服务器
            if (ips == null || ips.size == 0) {
                Toast.makeText(this, "null ips", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            TcpClient.startClient(ips, PORT, handler)
            btn.isEnabled = false
        }
        btnSend.setOnClickListener {
            //发送数据给服务器
            TcpClient.sendTcpMessage("iSM", handler)
        }
    }

    @SuppressLint("HandlerLeak")
    public val handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg);
            // 取时间戳
            val format = SimpleDateFormat("hh:mm:ss")
            if (msg.what == 0) {//文件
                tv.append("[${format.format(Date())}]${msg.obj}\n")
            } else if (msg.what == 1) {
                val obj = msg.obj as String
                tv.append("[${format.format(Date())}]$obj\n")
            }
        }
    }

}