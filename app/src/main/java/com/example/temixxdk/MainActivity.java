package com.example.temixxdk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    CardView sendBtn;
    CardView readBtn;
    CardView measureBtn;
    CardView connectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendBtn = findViewById(R.id.sendBtn);
        readBtn = findViewById(R.id.readBtn);
        measureBtn = findViewById(R.id.measureBtn);
        connectBtn = findViewById(R.id.connectBtn);


        sendBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.e(this.getClass().getName(),"Clicked");
                Toast();
                SendActivity();
            }
        });

        readBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.e(this.getClass().getName(),"Clicked");
                Toast();
                ReadActivity();
            }
        });

        measureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(this.getClass().getName(),"Clicked");
                Toast();
                MeasureActivity();
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectActivity();
            }
        });
    }

    private void SendActivity() {
        Intent intent = new Intent(this, SendActivity.class);
        startActivity(intent);
    }

    private void ReadActivity() {
        Intent intent = new Intent(this, ReadActivity.class);
        startActivity(intent);
    }

    private void MeasureActivity() {
        Intent intent = new Intent(this, MeasureActivity.class);
        startActivity(intent);
    }

    private void ConnectActivity() {
        Intent intent = new Intent(this, ConnectActivity.class);
        startActivity(intent);
    }


    private void Toast(){
        android.widget.Toast.makeText(this, "Clicked", android.widget.Toast.LENGTH_SHORT).show();
    }
}
