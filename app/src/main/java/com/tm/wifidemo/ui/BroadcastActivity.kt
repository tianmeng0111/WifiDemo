package com.tm.wifidemo.ui

import android.annotation.SuppressLint
import android.os.*
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.tm.wifidemo.DeviceSearcher
import com.tm.wifidemo.DeviceSearcher.DeviceBean
import com.tm.wifidemo.DeviceWaitingSearch
import com.tm.wifidemo.R
import java.net.InetSocketAddress

class BroadcastActivity : AppCompatActivity() {

    private val TAG = "BroadcastActivity"

    private val mDeviceList: MutableList<DeviceBean> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        initView()
    }

    private fun initView() {
        val btnSearch = findViewById<Button>(R.id.btn_search)
        val btnReceive = findViewById<Button>(R.id.btn_receive)
        btnSearch.setOnClickListener {
            searchDevices_broadcast()
        }
        btnReceive.setOnClickListener {
            initData()
        }
    }

     @SuppressLint("HandlerLeak")
     private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
        }
    }

    private fun searchDevices_broadcast() {

        object : DeviceSearcher() {

            override fun onSearchStart() {
//                startSearch() // 主要用于在UI上展示正在搜索
            }

            override fun onSearchFinish(deviceSet: Set<DeviceBean>) {
//                endSearch() // 结束UI上的正在搜索
                mDeviceList.clear()
                mDeviceList.addAll(deviceSet)
                for (device in mDeviceList) {
                    Log.e(TAG, "name:" + device.name + ";ip:" + device.ip + ";room" + device.room)
                }
                mHandler.sendEmptyMessage(0) // 在UI上更新设备列表
            }
        }.start()

    }

    private fun initData() {
        object : DeviceWaitingSearch(this, "日灯光", "客厅") {
            override fun onDeviceSearched(socketAddr: InetSocketAddress) {
                pushMsgToMain("已上线，搜索主机：" + socketAddr.getAddress().getHostAddress().toString() + ":" + socketAddr.getPort())
                Log.e(TAG, "已上线，搜索主机：" + socketAddr.getAddress().getHostAddress().toString() + ":" + socketAddr.getPort())
            }
        }.start()
    }

    /**
     * 建立单独链接
     */
    private fun pushMsgToMain(msg : String) {

    }

}