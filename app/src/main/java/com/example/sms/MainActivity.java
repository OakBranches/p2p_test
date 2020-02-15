package com.example.sms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager.Channel channel;
    WifiP2pManager manager;
    WifiManager wmanager;
    Button bota,disco;
    Receiver receiver;
    TextView label;
    ListView list;
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray ;
    WifiP2pDevice[] deviceArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = findViewById(R.id.listview);
        bota = (Button) findViewById(R.id.botao);
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
                if(wmanager.isWifiEnabled())
                    wmanager.setWifiEnabled(false);
                else{
                    wmanager.setWifiEnabled(true);
                }

            }
        });
    }
        disco.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        label.setText("Started Conection");
                    }

                    @Override
                    public void onFailure(int reasonCode) {

                        label.setText("Conection Failed");
                    }
                });
            }
        });
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
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNameArray);
                list.setAdapter(adapter);
            }else{
                Log.i("size:",Integer.toString( peerList.getDeviceList().size()));
            }
            if(peers.size()==0){
                Log.i("----------------->",peerList.toString()+"<<--");
                label.setText(peerList.toString());
                Toast.makeText(getApplicationContext(),"Nenhum peer encontrado",Toast.LENGTH_LONG).show();
                return;
            }else{
                label.setText(peerList.toString());
                Toast.makeText(getApplicationContext(),"Peer encontrado",Toast.LENGTH_LONG).show();
            }
        }
    } ;
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

}
