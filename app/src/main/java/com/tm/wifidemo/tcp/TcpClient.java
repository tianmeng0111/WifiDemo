package com.tm.wifidemo.tcp;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class TcpClient {

    public static Socket socket;

    public static void startClient(final ArrayList<String> ips , final int port, final Handler handler){
        if (ips == null){
            return;
        }
//        if (socket == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < ips.size(); i++) {
                        String ip = ips.get(i);
//                        Socket socket = null;
                        try {
                            Log.i("tcp", "启动客户端" + ip);
                            if (handler != null) {
                                Message.obtain(handler, 1,
                                        "启动客户端:" + ip)
                                        .sendToTarget();
                            }
                            socket = new Socket(ip, port);
                            Log.i("tcp", "客户端连接成功");
                            if (handler != null) {
                                Message.obtain(handler, 1,
                                        "客户端连接成功" + ip)
                                        .sendToTarget();
                            }
                            PrintWriter pw = new PrintWriter(socket.getOutputStream());
                            InputStream inputStream = socket.getInputStream();

                            byte[] buffer = new byte[1024];
                            int len = -1;
                            while ((len = inputStream.read(buffer)) != -1) {
                                String data = new String(buffer, 0, len);
                                Log.i("tcp", "收到服务器的数据:" + data);
                                // todo 通讯到主ui
                                if (handler != null) {
                                    Message.obtain(handler, 1,
                                            "收到服务器的数据:" + data)
                                            .sendToTarget();
                                }
                            }
                            Log.e("tcp", "客户端断开连接");
                            pw.close();
                            // 走到这就是链接成功，后面的不用试着链接了。
                            break;
                        } catch (Exception EE) {
                            EE.printStackTrace();
                            Log.e("tcp", "客户端无法连接服务器:" + ip);
                            // todo 通讯到主ui
                            if (handler != null) {
                                Message.obtain(handler, 1,
                                        "客户端无法连接服务器:" + ip)
                                        .sendToTarget();
                            }

                        } finally {
                            try {
                                if (socket != null) {
                                    socket.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
//                            socket = null;
                        }
                    }
                }
            }).start();
//        }
    }

    public static void sendTcpMessage(final String msg, final Handler handler){
        if (socket != null && socket.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket.getOutputStream().write(msg.getBytes());
                        socket.getOutputStream().flush();
                        Log.e("tcp", "send msg " + msg);
                        if (handler != null) {
                            Message.obtain(handler, 1,
                                    "发送:" + msg)
                                    .sendToTarget();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}

