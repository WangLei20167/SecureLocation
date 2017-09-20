package connect;

import android.os.Handler;
import android.os.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import msg.MsgValue;

/**
 * 用于与TCPClient创建连接
 * Created by Administrator on 2017/9/20 0020.
 */

public class TCPServer {
    private List<Socket> socketList = new ArrayList<Socket>();

    private ExecutorService mExecutorService = null;   //线程池
    //创建一个SocketServer服务线程
    private ServerThread serverThread = new ServerThread();
    //主活动的handler
    private Handler handler = null;

    public TCPServer(Handler handler) {
        this.handler = handler;
    }

    public void startTCPServer() {
        serverThread.start();
    }

    //关闭所有的socket
    public void stopTCPServer() {
        for (int i = 0; i < socketList.size(); ++i) {
            Socket s = socketList.get(i);

            socketList.remove(i);
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            break;
        }
    }

    //SocketServer服务线程 等待client连接
    class ServerThread extends Thread {
        ServerSocket serverSocket = null;

        public ServerThread() {
            this.setName("SocketServer等待连接线程");
        }

        @Override
        public void run() {
            //创建一个SocketServer服务
            try {
                serverSocket = new ServerSocket(Constant.TCP_ServerPORT);
                serverSocket.setReuseAddress(true);   //设置上一个关闭的超时状态下可连接
            } catch (IOException e) {
                //失败
                SendMessage(MsgValue.TELL_ME_SOME_INFOR, 0, 0,
                        "绑定端口" + Constant.TCP_ServerPORT + "失败"
                );
                e.printStackTrace();
            }
            //成功
            SendMessage(MsgValue.TELL_ME_SOME_INFOR, 0, 0,
                    "SocketServer开启成功，等待连接"
            );
            //创建一个线程池，用来处理client
            mExecutorService = Executors.newCachedThreadPool();
            Socket client = null;
            //等待client连接
            while (true) {
                try {
                    //阻塞等待连接
                    client = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                String client_ip = client.getInetAddress().toString();
                for (int i = 0; i < socketList.size(); ++i) {
                    Socket s = socketList.get(i);
                    if (s.getInetAddress().toString().equals(client_ip)) {
                        socketList.remove(i);
                        try {
                            s.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                socketList.add(client);
                //启动一个线程处理与client的对话
                try {
                    mExecutorService.execute(new ClientThread(client)); //启动一个新的线程来处理连接
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    class ClientThread extends Thread {
        private Socket socket;
        private String client_ip;

        private DataInputStream in = null;   //接收
        private DataOutputStream out = null; //发送

        public ClientThread(Socket socket) {
            this.socket = socket;

            try {
                this.socket.setTcpNoDelay(true); //设置直接发送
                in = new DataInputStream(socket.getInputStream());     //接收
                out = new DataOutputStream(socket.getOutputStream());//发送
            } catch (IOException e) {
                e.printStackTrace();
            }
            client_ip = socket.getInetAddress().toString();
            this.setName(client_ip + " 服务线程");
            System.out.println(this.getName() + "已启动");
        }

        @Override
        public void run() {
            byte[] bytes = new byte[1024];
            int msgLen = -1;
            while (true) {
                if (socket.isConnected() && !socket.isInputShutdown()) {
                    try {
                        //获取信息的字节数
                        if ((msgLen = in.read(bytes, 0, 1024)) > -1) {
                            //
                            String msg = new String(bytes, 0, msgLen);
                            //显示接收到的信息
                            SendMessage(MsgValue.TELL_ME_SOME_INFOR, 0, 0, msg);
                            out.write(msg.getBytes());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
        }
    }

    //发送给UI进程
    void SendMessage(int what, int arg1, int arg2, Object obj) {
        if (handler != null) {
            Message.obtain(handler, what, arg1, arg2, obj).sendToTarget();
        }
    }
}
