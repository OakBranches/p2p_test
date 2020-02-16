package com.example.sms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.IntentFilter;
import android.net.InetAddresses;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static android.os.Looper.getMainLooper;

public class Multi {
    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel channel;
    WifiP2pManager manager;
    WifiManager wmanager;
    ArrayAdapter<String> arrayAdapter1;
    Receiver receiver;
    Context applicationContext;
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray ;
    WifiP2pDevice[] deviceArray;
    static final int MESSAGE_READ = 1;
    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;
    String tempMsg,label,toast;
    int listVisibility;
    ArrayAdapter<String> adapter;

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                label="VOCE É O HOST";
                serverClass = new ServerClass();
                serverClass.start();
                listVisibility=View.INVISIBLE;
            }else if(wifiP2pInfo.groupFormed){
                label="VOCE É O CLIENTE";
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
                listVisibility=View.INVISIBLE;
            }
        }
    };
    Multi(Context appCont){
        this.applicationContext = appCont;
    }
    public void setWifiOn(){
        wmanager.setWifiEnabled(true);
    }
    public void setWifiOff(){
        wmanager.setWifiEnabled(false);
    }
    public void ConnectIndexList(int i){
        final WifiP2pDevice device =deviceArray[i];
        WifiP2pConfig config= new WifiP2pConfig();
        config.deviceAddress=device.deviceAddress;
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(applicationContext,"celular conectado a :"+device.deviceName,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(applicationContext,"celular não pode se conectar ",Toast.LENGTH_LONG).show();
                return;
            }
        });
    }
    public void DiscoverPeers(){
        manager.discoverPeers(channel,new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                label="Started Conection";

                listVisibility=View.VISIBLE;

            }

            @Override
            public void onFailure(int reasonCode) {

                label="Conection Failed";
            }
        });
    }
    public void SendMsg(final String msg){
        new Thread(new Runnable() {
            @Override
            public void run() {

                    sendReceive.write(msg.getBytes());

            }
        }).start();
        ;
    }
    public void WifiOnOff(){
        if (wmanager.isWifiEnabled())
            wmanager.setWifiEnabled(false);
        else {
            wmanager.setWifiEnabled(true);
        }
    }
    public void InitialWork(){

        wmanager = (WifiManager)  applicationContext.getSystemService(Context.WIFI_SERVICE);
        listVisibility = 0;
        label="Hello World";
        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) applicationContext.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(applicationContext, getMainLooper(), null);

    }


    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener(){
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList){
            if(!peerList.getDeviceList().equals(peers)){
                peers.clear();
                Log.i("size:",Integer.toString( peerList.getDeviceList().size()));
                peers.addAll(peerList.getDeviceList());
                deviceNameArray=new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index = 0;
                for (WifiP2pDevice device : peerList.getDeviceList()){
                    Log.i("i:",Integer.toString( index));
                    deviceNameArray[index]=device.deviceName;
                    deviceArray[index]=device;
                    index++;

                }
                adapter = new ArrayAdapter<String>(applicationContext,android.R.layout.simple_list_item_1,deviceNameArray);

            }else{
                Log.i("size:",Integer.toString( peerList.getDeviceList().size()));
            }
            if(peers.size()==0){
                Log.i("----------------->",peerList.toString()+"<<--");

                Toast.makeText(applicationContext,"Nenhum peer encontrado",Toast.LENGTH_LONG).show();
                return;
            }else{


            }
        }
    } ;
    public void onResume() {
        receiver = new Receiver(manager, channel, this);
        applicationContext.registerReceiver(receiver, intentFilter);
    }
    public void onPause() {
        applicationContext.unregisterReceiver(receiver);
    }
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch(message.what){
                case MESSAGE_READ:
                    byte[] readBuff= (byte[]) message.obj;
                    String Msg = new String (readBuff,0,message.arg1);
                    tempMsg=Msg;
                    break;
            }
            return true;
        }
    });
    private class SendReceive extends  Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while(socket!=null){
                try{
                    bytes=inputStream.read(buffer);
                    if(bytes>0){
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

        }

        public SendReceive(Socket skt){
            socket =skt;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        public void write(final byte[] bytes){
            if(socket!=null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.i("INSIDE WRITE", "HELLO");
                            outputStream.write(bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }
    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
    public class ClientClass extends  Thread{
        Socket socket;
        String hostAdd;
        public ClientClass(InetAddress HostAddress){
            hostAdd=HostAddress.getHostAddress();
            socket = new Socket();
        }

        @Override
        public void run() {
            try{
                socket.connect(new InetSocketAddress(hostAdd,8888),500);
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
