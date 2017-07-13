package tcp2btserial.zebrajaeger.de.tcp2btserial;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Lars Brandt on 10.07.2017.
 */
public class TcpBtServer {
    private final InetAddress inetAddress;
    private final int port;
    private final int backlog = 1;
    private BT bt;

    public TcpBtServer(BT bt, String ipAddress, final int port) throws UnknownHostException {
        this(bt, (ipAddress == null) ? null : InetAddress.getByName(ipAddress), port);
    }

    public TcpBtServer(BT bt, final InetAddress inetAddress, final int port) {
        this.bt = bt;
        this.inetAddress = inetAddress;
        this.port = port;

        Thread serverThread = new Thread("ServerThread") {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = (inetAddress == null) ? new ServerSocket(port) : new ServerSocket(port, backlog, inetAddress);
                    Socket s;
                    while ((s = serverSocket.accept()) != null) {
                        handleSocket(s);
                    }
                } catch (IOException e) {
                    Log.e("ServerThread", "Failed Server Thread", e);
                }
            }
        };
        serverThread.start();
    }

    private void handleSocket(final Socket s) {
        boolean debug = false;

        if (debug) {
            try {
            createEchoThread(s).join();
            } catch (InterruptedException e) {
                Log.e("ServerThread", "Failed join echo thread", e);
            }
        }else {
            final Thread receiver = cgreateReceiverThread(s);
            final Thread sender = createSenderThread(s);

            try {
                receiver.join();
                sender.join();
            } catch (InterruptedException e) {
                Log.e("ServerThread", "Failed join receiver/sender threads", e);
            }
        }
    }

    @NonNull
    private Thread createEchoThread(final Socket s) {
        final Thread sender = new Thread("BtEchoThread") {
            @Override
            public void run() {
                Log.i("BtEchoThread", "BtEchoThread started");
                try {
                    InputStream is = s.getInputStream();
                    OutputStream os = s.getOutputStream();
                    int i;
                    while ((i = is.read()) != -1) {
                        os.write(i);
                    }
                } catch (IOException e) {
                    Log.e("BtEchoThread", "Failed BtEchoThread Server Thread", e);
                }
                Log.i("BtEchoThread", "BtEchoThread stopped");
            }
        };
        sender.start();
        return sender;
    }

    @NonNull
    private Thread createSenderThread(final Socket s) {
        final Thread sender = new Thread("BtSenderThread") {
            @Override
            public void run() {
                Log.i("BtSenderThread", "BtSenderThread started");
                try {
                    InputStream is = s.getInputStream();
                    int l;
                    byte[] buffer = new byte[1024];
                    while ((l = is.read(buffer)) >0) {
                        bt.send(buffer,0,l);
                    }
                } catch (IOException e) {
                    Log.e("BtSenderThread", "Failed BtSenderThread Server Thread", e);
                }
                Log.i("BtSenderThread", "BtSenderThread stopped");
            }
        };
        sender.start();
        return sender;
    }

    @NonNull
    private Thread cgreateReceiverThread(final Socket s) {
        final Thread receiver = new Thread("BtReceiverThread") {
            @Override
            public void run() {
                Log.i("ServerThread", "BtReceiverThread started");
                try {
                    OutputStream os = s.getOutputStream();
                    int l;
                    byte[] buffer = new byte[1024];
                    while ((l = bt.read(buffer)) >0) {
                        os.write(buffer,0,l);
                    }
                } catch (IOException e) {
                    Log.e("BtReceiverThread", "Failed BtReceiverThread Server Thread", e);
                }
                Log.i("ServerThread", "BtReceiverThread stopped");
            }
        };
        receiver.start();
        return receiver;
    }
}
