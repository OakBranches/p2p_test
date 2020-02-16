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


public class MainActivity extends AppCompatActivity {
    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel channel;
    WifiP2pManager manager;
    WifiManager wmanager;
    Button bota,disco,send;
    ArrayAdapter<String> arrayAdapter1;
    Receiver receiver;
    TextView label,tempMsg;
    ListView list;
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray ;
    WifiP2pDevice[] deviceArray;
    static final int MESSAGE_READ = 1;
    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;
    EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = findViewById(R.id.listview);
        editText = (EditText) findViewById(R.id.editText);
        tempMsg = (TextView) findViewById(R.id.tempMsg);
        bota = (Button) findViewById(R.id.botao);
        send = (Button) findViewById(R.id.send);
        disco =(Button) findViewById(R.id.discover);
        label = (TextView) findViewById(R.id.textView);
        wmanager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        exqListener();
    }

    private void exqListener() {
        {

            bota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (wmanager.isWifiEnabled())
                    wmanager.setWifiEnabled(false);
                else {
                    wmanager.setWifiEnabled(true);
                }

            }
        });
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    final WifiP2pDevice device =deviceArray[i];
                    WifiP2pConfig config= new WifiP2pConfig();
                    config.deviceAddress=device.deviceAddress;
                    manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(),"celular conectado a :"+device.deviceName,Toast.LENGTH_LONG).show();
                            //label.setText("celular conectado a :"+device.deviceName);
                        }

                        @Override
                        public void onFailure(int i) {
                            Toast.makeText(getApplicationContext(),"celular não pode se conectar ",Toast.LENGTH_LONG).show();
                            return;
                        }
                    });
                }
            });
    }
        disco.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                manager.discoverPeers(channel,new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        label.setText("Started Conection");

                        list.setVisibility(View.VISIBLE);

                    }

                    @Override
                    public void onFailure(int reasonCode) {

                        label.setText("Conection Failed");
                    }
                });
            }

        });
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){

                    String msg = editText.getText().toString();
                    Log.i("mensagem ->>>>",msg);
                    sendReceive.write(msg.getBytes());

            }

        });
    }
    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                label.setText("VOCE É O HOST");
                serverClass = new ServerClass();
                serverClass.start();
                list.setVisibility(View.INVISIBLE);
            }else if(wifiP2pInfo.groupFormed){
                label.setText("VOCE É O CLIENTE");
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
                list.setVisibility(View.INVISIBLE);
            }
        }
    };

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
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNameArray);
                list.setAdapter(adapter);

            }else{
                Log.i("size:",Integer.toString( peerList.getDeviceList().size()));
            }
            if(peers.size()==0){
                Log.i("----------------->",peerList.toString()+"<<--");

                Toast.makeText(getApplicationContext(),"Nenhum peer encontrado",Toast.LENGTH_LONG).show();
                return;
            }else{


            }
        }
    } ;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch(message.what){
                case MESSAGE_READ:
                    byte[] readBuff= (byte[]) message.obj;
                    String Msg = new String (readBuff,0,message.arg1);
                    tempMsg.setText(Msg);
                    break;
            }
            return true;
        }
    });
    @Override
    public void onResume() {
        super.onResume();
        receiver = new Receiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
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
            Log.i("SEND RECEIVE","<<<<<FOI CRIADO>>>>>");
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
