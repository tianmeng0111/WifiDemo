package com.tm.wifidemo.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.tm.wifidemo.PermissionUtil
import com.tm.wifidemo.R
import com.tm.wifidemo.ScanDeviceTool
import com.tm.wifidemo.SocketManager
import com.tm.wifidemo.wifi_probe.WifiProbeManager
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private val PORT = 8003

    private lateinit var mProbe: WifiProbeManager

    private lateinit var mWifi_list: ListView
    private lateinit var mAdapter: ArrayAdapter<String>

    private lateinit var tv: EditText

    private lateinit var mServerSocket: ServerSocket
    private lateinit var mSocket: Socket

    private lateinit var mOutStream: OutputStream
    private lateinit var mInputStream: InputStream

    private var mIp: String = ""

    private lateinit var mSocketManager: SocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        PermissionUtil.verifyStoragePermissions(this)
        initView()

        val scanDeviceTool = ScanDeviceTool()
        scanDeviceTool.setOnScanListener { ipList, devAddress ->
            initViewAfterData(ipList)

            findViewById<TextView>(R.id.tv_local).text = "本机ip： $devAddress"
        }
        val ipList = scanDeviceTool.scan()

        try {
            // 创建socket实例
            mServerSocket = ServerSocket(PORT)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        /**
         * 收数据
         */
        val socketAcceptThread = SocketAcceptThread()
        socketAcceptThread.start()

        val btn = findViewById<View>(R.id.btn_socket)
        btn.setOnClickListener(View.OnClickListener {
            sendUpd(mIp, "" + findViewById<EditText>(R.id.et).text)
        })

    }

    private fun initView() {
        mWifi_list = findViewById<ListView>(R.id.list_wifi)
        tv = findViewById<EditText>(R.id.tv)
    }

    private fun initViewAfterData(ipList: List<String>) {
        mAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ipList)
        mWifi_list.adapter = mAdapter
        mWifi_list.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                mIp = ipList[position]
                SocketConnectThread(mIp).start()
            }
    }

    inner class SocketConnectThread(val ip: String) : Thread(){
        override fun run(){
            Log.e("info", "run: ============线程启动");
            try {
                //指定ip地址和端口号
                mSocket = Socket(ip, PORT)
                if(mSocket != null){
                    //获取输出流、输入流
                    mOutStream = mSocket.getOutputStream();
                    mInputStream = mSocket.getInputStream();
                }else {
                    Log.e("info", "run: =========scoket==null");
                }
            } catch (e: Exception) {
                e.printStackTrace();
                return;
            }
            Log.e("info", "connect success========================================");
//            startReader(mSocket);
            startReader1(mSocket)
        }

    }

    private fun sendUpd(ip: String, str: String) {
        Thread(object : Runnable {
            override fun run() {
                try {
                    // socket.getInputStream()
                    mOutStream = mSocket.getOutputStream()
                    var writer = DataOutputStream(mOutStream)
                    writer.writeUTF(str); // 写一个UTF-8的信息
                    Log.d(TAG, "发送消息:" + str)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }).start();
    }

    /**
     * 从参数的Socket里获取最新的消息
     */
    public fun startReader(socket: Socket) {
        Thread(object : Runnable {

            override fun run() {
                var reader: DataInputStream
                try {
                    // 获取读取流
                    reader = DataInputStream(socket.getInputStream());
                    while (true) {
                        val msg: String = reader.readUTF()
                        Log.e(TAG, "获取到客户端的信息：=" + msg)

                        //告知客户端消息收到
//                        val writer = DataOutputStream(mSocket.getOutputStream());
//                        writer.writeUTF("收到：" + msg); // 写一个UTF-8的信息

                        //发消息更新UI
                        Message.obtain(handler, 1, msg).sendToTarget()
                    }
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
        }).start()
    }

    public fun startReader1(socket: Socket) {
        Thread(object : Runnable {

            override fun run() {
                var reader: DataInputStream
                try {
                    // 获取读取流
                    reader = DataInputStream(socket.getInputStream());
                    while (true) {
                        // 读取数据
                        val msg: String = reader.readUTF()
                        Log.e(TAG, "获取到客户端的信息：=" + msg)
                        Message.obtain(handler, 1, msg).sendToTarget()
                    }
                } catch (e: IOException) {
                    e.printStackTrace();
                }
            }
        }).start()
    }

    @SuppressLint("HandlerLeak")
    public var handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg);
            // 取时间戳
            val format = SimpleDateFormat("hh:mm:ss")
            if (msg.what == 0) {//文件
                tv.append("[${format.format(Date())}]${msg.obj}\n")
            } else if (msg.what == 1) {
                val obj = msg.obj as String
                tv.append("[${format.format(Date())}]$obj\n")
                if(!TextUtils.isEmpty(obj) && obj.contains("链接上了")) {
                    mAdapter.clear()
                }
            }
        }
    }

    inner class SocketAcceptThread: Thread(){
        override fun run() {
            try {
                //等待客户端的连接，Accept会阻塞，直到建立连接，
                //所以需要放在子线程中运行。
                mSocket = mServerSocket.accept()
            } catch (e: IOException) {
                e.printStackTrace();
                Log.e("info", "run: ==============" + "accept error");
                return;
            }


            Log.e("info", "accept success==================")
            sendUpd(mIp, "链接上了！！！！！！！！！！")
            Message.obtain(handler, 1, "链接上了！！！！！！！！！！").sendToTarget()

            //启动消息接收线程
            startReader(mSocket)

            mSocketManager = SocketManager(mServerSocket)
            while (true) { //接收文件
                val response: String = mSocketManager.ReceiveFile()
                Message.obtain(handler, 0, response).sendToTarget()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //选择了文件发送
        if (resultCode == RESULT_OK) {
            val fileName = data!!.getStringExtra("FileName")
            val path = data!!.getStringExtra("FilePath")
            Message.obtain(handler, 0, "$fileName 正在发送至$mIp:$PORT").sendToTarget()
            mSocketManager = SocketManager(mServerSocket)
            val sendThread = Thread {
                val response: String = mSocketManager.SendFile(fileName, path, mIp, PORT)
                Message.obtain(handler, 0, response).sendToTarget()
            }
            sendThread.start()
        }
    }
    override fun onDestroy() {
        try {
            if (mServerSocket != null) {
                mServerSocket.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_to_activity2) {
            startActivity(Intent(this, MainActivity2::class.java))
            return true
        } else if (item.itemId == R.id.menu_to_file) {
            startActivityForResult(Intent(this, FileSelectActivity::class.java), 1)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}