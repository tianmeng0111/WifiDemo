package com.tm.wifidemo.tcp;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {
    private static final String TAG = "TCP";
    private static ServerSocket serverSocket;
    private static Socket socket;

    public static void startServer(){
        if (serverSocket == null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        serverSocket = new ServerSocket(4444);
                        Log.e(TAG , "服务器等待连接中");
                        socket = serverSocket.accept();
                        Log.e(TAG , "客户端连接上来了");
                        InputStream inputStream = socket.getInputStream();
                        byte[] buffer = new byte[1024];
                        int len = -1;
                        while ((len = inputStream.read(buffer)) != -1) {
                            String data = new String(buffer, 0, len);
                            Log.e(TAG , "收到客户端的数据:" + data);
                            // todo 通讯到主ui
//                            EventBus.getDefault().post(new MessageServer(data));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();

                    }finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        socket = null;
                        serverSocket = null;
                    }
                }
            }).start();
        }
    }

    public static void sendTcpMessage(final String msg){
        if (socket != null && socket.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket.getOutputStream().write(msg.getBytes());
                        socket.getOutputStream().flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "run: " + e.getMessage());
                    }
                }
            }).start();
        }
    }
}
