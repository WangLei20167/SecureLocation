package connect;

import android.os.Handler;
import android.os.Message;
import android.provider.Settings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import appData.GlobalVar;
import msg.MsgValue;

/**
 * 用于连接TCPServer
 * Created by Administrator on 2017/9/20 0020.
 */

public class TCPClient {
    private Socket socket = null;

    private DataInputStream in = null;   //接收
    private DataOutputStream out = null; //发送
    private Handler handler = null;

    private RevThread revThread;//接收线程
    public TCPClient(Handler handler) {
        this.handler = handler;
    }

    public void connectServer(final String TCP_ServerIP) {
        Thread conThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //连接服务器
                //实现一个socket创建3秒延迟的作用
                long startTime = System.currentTimeMillis();
                try {
                    while (true) {
                        try {
                            socket = new Socket(TCP_ServerIP, Constant.TCP_ServerPORT);
                            break;
                        } catch (IOException e) {
                            e.printStackTrace();
                            long endTime = System.currentTimeMillis();
                            if ((endTime - startTime) > 3000) {
                                //连接超时
                                throw new IOException();
                            } else {
                                //等待0.1秒重连
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                    SendMessage(MsgValue.TELL_ME_SOME_INFOR, 0, 0, "连接Socket成功");
                    socket.setTcpNoDelay(true);
                    //输出输出流
                    try {
                        in = new DataInputStream(socket.getInputStream());     //接收
                        out = new DataOutputStream(socket.getOutputStream());//发送
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //开启接收和发送线程
                    send2Server();
                    //开机接收线程
                    RevThread revThread=new RevThread();
                    revThread.start();
                    revThread.setName("接收线程已经开启");
                    System.out.println(revThread.getName() + " 已启动");
                } catch (IOException e) {
                    e.printStackTrace();

                    SendMessage(MsgValue.TELL_ME_SOME_INFOR, 0, 0, "连接Socket失败");
                }
            }
        });
        conThread.setName("连接wifi并建立socket线程");
        conThread.start();
        System.out.println(conThread.getName() + " 已启动");
    }

    //关闭连接
    public void disconnectServer(){
        try {
            //关闭流
            out.close();
            in.close();
            //关闭Socket
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send2Server(){
        //发送给父节点自己的经纬度
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    out.write(GlobalVar.LonAndLat.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //
    class RevThread extends Thread{

        //接收
        @Override
        public void run(){
            byte[] bytes=new byte[1024];
            int msgLen=-1;
            while(true){
                if(socket.isConnected()&&!socket.isInputShutdown()){
                    try {
                        //获取信息的字节数
                        if((msgLen=in.read(bytes,0,1024))>-1){
                            //
                            String msg=new String(bytes,0,msgLen);
                            //显示接收到的信息
                            SendMessage(MsgValue.TELL_ME_SOME_INFOR,0,0,msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    break;
                }
            }
        }
    }

    void SendMessage(int what, int arg1, int arg2, Object obj) {
        if (handler != null) {
            Message.obtain(handler, what, arg1, arg2, obj).sendToTarget();
        }
    }
}
