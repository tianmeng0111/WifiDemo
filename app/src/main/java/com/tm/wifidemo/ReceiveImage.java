package com.tm.wifidemo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class ReceiveImage {
    private static int IMAGE_PORT = 9000;
    private DatagramPacket datagramPacket;
    private DatagramSocket datagramSocket;

    byte b[] = new byte[8192];
    public byte[] receive() throws SocketException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if(datagramSocket == null){
            datagramSocket = new DatagramSocket(null);
            datagramSocket.setReuseAddress(true);
            datagramSocket.bind(new InetSocketAddress(IMAGE_PORT));
        }else
            try {
                datagramSocket = new DatagramSocket(IMAGE_PORT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
        while(true){
            datagramPacket = new DatagramPacket(b, b.length);
            try {
                datagramSocket.receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String msg  = new String(datagramPacket.getData(),0,datagramPacket.getLength());
            if(msg.startsWith(";!")){
                System.out.println("-->接收到所有数据");
                break;
            }
            baos.write(datagramPacket.getData(),0,datagramPacket.getLength());
            //System.out.println("-->正在接收数据:"+datagramPacket.getData());
        }
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();


    }

}