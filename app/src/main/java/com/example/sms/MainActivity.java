package com.example.sms;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;



public class MainActivity extends AppCompatActivity {
    EditText editText;
    Button bota,disco,send,refresh;
    TextView label,tempMsg;
    ListView list;
    Multi multi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list = findViewById(R.id.listview);
        editText = (EditText) findViewById(R.id.editText);
        tempMsg = (TextView) findViewById(R.id.tempMsg);
        bota = (Button) findViewById(R.id.botao);
        refresh = (Button) findViewById(R.id.refresh);
        send = (Button) findViewById(R.id.send);
        disco =(Button) findViewById(R.id.discover);
        label = (TextView) findViewById(R.id.textView);

        multi = new Multi(getApplicationContext()) ;
        multi.InitialWork();
        exqListener();

        update();

    }

    private void exqListener() {

        {

            bota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                multi.WifiOnOff();

                update();

            }
        });
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    multi.ConnectIndexList(i);

                    update();
                }
            });
    }
        disco.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
               multi.DiscoverPeers();

                update();
            }

        });
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){

                String msg = editText.getText().toString();
                multi.SendMsg(msg);
                editText.setText("");

                update();
            }

        });
        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                update();

            }

        });
    }
    public void update(){
        label.setText(multi.label);
        tempMsg.setText(multi.tempMsg);
        list.setVisibility(multi.listVisibility);
        updateList();
    }
    public void updateList() {
        list.setAdapter(multi.adapter);
    }


    @Override
    public void onResume() {
        super.onResume();
        multi.onResume();
        update();
    }

    @Override
    public void onPause() {
        super.onPause();
        multi.onPause();
        update();
    }

}
