package com.example.temixxdk;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.log4j.BasicConfigurator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import pl.pawelkleczkowski.customgauge.CustomGauge;

public class ReadActivity extends AppCompatActivity {

    static DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    static DatabaseReference conditionRef = mRootRef.child("text");
    ValueEventListener listener;

    TextView txtview;
    TextView tempShowtxt;
    TextView humShowtxt;
    TextView luxShowtxt;
    Context context;
    CustomGauge gauge1;
    CustomGauge gauge2;
    CustomGauge gauge3;
    CardView home;
    CardView start;
    CardView stop;

    boolean isFistVal = true;

    int lasttemp;
    int lasthum;
    int lastlux;
    int inttemp;
    int inthum;
    int intlux;
    int difftemp=0;
    int diffhum=0;
    int difflux=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);
        context = this;

        txtview = findViewById(R.id.txtview);
        tempShowtxt = findViewById(R.id.tempShowtxt);
        humShowtxt = findViewById(R.id.humShowtxt);
        luxShowtxt = findViewById(R.id.luxShowtxt);
        gauge1 = findViewById(R.id.gauge1);
        gauge2 = findViewById(R.id.gauge2);
        gauge3 = findViewById(R.id.gauge3);
        home = findViewById(R.id.home);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);
        BasicConfigurator.configure();
    }

    public void startOnClick(View view) {
        start.setCardBackgroundColor(Color.parseColor("#BAC7DA"));
        stop.setCardBackgroundColor(Color.parseColor("#FFE3ECFA"));
        listener = conditionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String msg = dataSnapshot.getValue(String.class);
                txtview.setText(msg);
                processData(msg);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void btnStpOnClick(View view) {
        stop.setCardBackgroundColor(Color.parseColor("#BAC7DA"));
        start.setCardBackgroundColor(Color.parseColor("#FFE3ECFA"));
        conditionRef.removeEventListener(listener);
    }

    protected void processData(String msg){
        Log.e(ReadActivity.this.getClass().getName(), msg + "TTTTTTTTTTTTTTTTT");

        int i = msg.indexOf("p");
        int y = msg.indexOf("y");
        int x = msg.indexOf("x");
        String tempval = msg.substring(i + 4, i + 9);
        String humval = msg.substring(y + 4, y + 6);
        String luxval = msg.substring(x + 4, x + 7);

        if (luxval.contains("\"")) {
            //맨뒤에껄 지우기
            luxval = luxval.substring(0, luxval.length() - 1);
            Log.e(ReadActivity.this.getClass().getName(), "11111111111111111");
            //다시체크
            if (luxval.contains("\"")) {
                Log.e(ReadActivity.this.getClass().getName(), "222222222222222");
                luxval = luxval.substring(0, luxval.length() - 1);
                if (luxval.contains("\"")) {
                    Log.e(ReadActivity.this.getClass().getName(), "333333333333333333");
                    luxval = luxval.substring(0, luxval.length() - 1);
                    Log.e(ReadActivity.this.getClass().getName(), "4444444444444444444");
                }
            }
        }

        Log.e(ReadActivity.this.getClass().getName(), tempval + "QQQQQQQQQQQQQQQ");
        Log.e(ReadActivity.this.getClass().getName(), humval + "RRRRRRRRRRRRRR");
        Log.e(ReadActivity.this.getClass().getName(), luxval + "CCCCCCCCCCCCCC");

        tempShowtxt.setText(tempval);
        humShowtxt.setText(humval);
        luxShowtxt.setText(luxval);

        inttemp = Double.valueOf(tempval).intValue();
        inthum = (Double.valueOf(humval).intValue());
        intlux = (Double.valueOf(luxval).intValue());


        int temM = 28;
        int humM = 50;
        int luxM = 500;

        Log.e(ReadActivity.this.getClass().getName(), "TRUE");
        if (inttemp > temM) {
            Log.e(ReadActivity.this.getClass().getName(), "??");
//            ReadActivity.this.runOnUiThread(new Runnable() {
//                                                public void run() {
//                                                    Toast toast = Toast.makeText(context, "Alert : Temperature Too High", Toast.LENGTH_SHORT);
//                                                    toast.setGravity(Gravity.NO_GRAVITY | Gravity.LEFT, 280, -100);
//                                                    toast.show();
//                                                }
//                                            }
//            );
        }
        if (inthum > humM) {
            Log.e(ReadActivity.this.getClass().getName(), "??");
//            ReadActivity.this.runOnUiThread(new Runnable() {
//                                                public void run() {
//                                                    Toast toast = Toast.makeText(context, "Alert : Humidity Too High", Toast.LENGTH_SHORT);
//                                                    toast.setGravity(Gravity.CENTER, 20, -100);
//                                                    toast.show();
//                                                }
//                                            }
//            );
        }
        if (intlux > luxM) {
            Log.e(ReadActivity.this.getClass().getName(), "??");
//            ReadActivity.this.runOnUiThread(new Runnable() {
//                                                public void run() {
//                                                    Toast toast = Toast.makeText(context, "Alert : Light Intensity Too High", Toast.LENGTH_SHORT);
//                                                    toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT, 0, -100);
//                                                    toast.show();
//                                                }
//                                            }
//            );
        }

        if (isFistVal) {
            CustomGauge gauge1 = findViewById(R.id.gauge1);
            CustomGauge gauge2 = findViewById(R.id.gauge2);
            CustomGauge gauge3 = findViewById(R.id.gauge3);
            gauge1.setValue(inttemp);
            gauge2.setValue(inthum);
            gauge3.setValue(intlux);
            Log.e(ReadActivity.this.getClass().getName(), "true");
            isFistVal = false;
            lasthum = inthum;
            lasttemp = inttemp;
            lastlux = intlux;
        } else {
            difftemp = inttemp - lasttemp;
            Log.e(ReadActivity.this.getClass().getName(), "lasttemp : " + (String.valueOf(lasttemp)));
            Log.e(ReadActivity.this.getClass().getName(), "difftemp : " + (String.valueOf(difftemp)));
            diffhum = inthum - lasthum;
            Log.e(ReadActivity.this.getClass().getName(), "lasthum : " + (String.valueOf(lasthum)));
            Log.e(ReadActivity.this.getClass().getName(), "diffhum : " + (String.valueOf(diffhum)));
            difflux = intlux - lastlux;
            Log.e(ReadActivity.this.getClass().getName(), "lastlux : " + (String.valueOf(lastlux)));
            Log.e(ReadActivity.this.getClass().getName(), "difflux : " + (String.valueOf(difflux)));

            if (difflux < 0) {
                difflux *= -1;
                Runnable LuxDrunnable = new LuxDecRunnable(inthum, lasthum, diffhum);
                new Thread(LuxDrunnable).start();

            } else if (difflux > 0) {
                Runnable LuxIncRunnable = new LuxIncRunnable(inttemp, lasttemp, difftemp);
                new Thread(LuxIncRunnable).start();

            } else if (difflux == 0) {
                lastlux = intlux;
                Log.e(ReadActivity.this.getClass().getName(), "SAME LUX");
            }

            if (difftemp < 0) {
                difftemp *= -1;
                Runnable tempDrunnable = new TempDecRunnable(inthum, lasthum, diffhum);
                new Thread(tempDrunnable).start();

            } else if (difftemp > 0) {
                Runnable tempIrunnable = new TempIncRunnable(inttemp, lasttemp, difftemp);
                new Thread(tempIrunnable).start();

            } else if (difftemp == 0) {
                lasttemp = inttemp;
                Log.e(ReadActivity.this.getClass().getName(), "SAME TEMP");
            }

            if (diffhum < 0) {
                diffhum *= -1;
                Runnable humDrunnable = new HumDecRunnable(inthum, lasthum, diffhum);
                new Thread(humDrunnable).start();

            } else if (diffhum > 0) {
                Runnable HumIncRunnable = new HumIncRunnable(inttemp, lasttemp, difftemp);
                new Thread(HumIncRunnable).start();

            } else if (diffhum == 0) {
                lasthum = inthum;
                Log.e(ReadActivity.this.getClass().getName(), "SAME HUM");
            }
        }

    }

    public void HomebtnOnClick(View view) {
        if (listener != null){
            conditionRef.removeEventListener(listener);
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }



    public class TempIncRunnable implements Runnable {

        public TempIncRunnable(int pres, int last, int diff){
        }
        @Override
        public void run() {

            for (int i = 1; i <= difftemp; i++){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gauge1.setValue(lasttemp+=1);
                    }
                });
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Log.e(this.getClass().getName(),"Thread Success11111");
            }
            lasttemp = inttemp;
        }
    }

    public class TempDecRunnable implements Runnable {

        public TempDecRunnable(int pres, int last, int diff){

        }
        @Override
        public void run() {

            for (int i = 1; i <= difftemp; i++){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gauge1.setValue(lasttemp-=1);
                    }
                });
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Log.e(this.getClass().getName(),"Thread Success22222");
            }
            lasttemp = inttemp;
        }
    }

    public class HumIncRunnable implements Runnable {

        public HumIncRunnable(int pres, int last, int diff){

        }
        @Override
        public void run() {

            for (int i = 1; i <= diffhum; i++) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gauge2.setValue(lasthum+=1);
                    }
                });
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Log.e(this.getClass().getName(),"Thread Success33333");
                lasthum = inthum;
            }

        }
    }

    public class HumDecRunnable implements Runnable {

        public HumDecRunnable(int pres, int last, int diff){

        }
        @Override
        public void run() {

            for (int i = 1; i <= diffhum; i++){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gauge2.setValue(lasthum-=1);
                    }
                });
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Log.e(this.getClass().getName(),"Thread Success444444");
            }
            lasthum = inthum;
        }
    }

    public class LuxIncRunnable implements Runnable {

        public LuxIncRunnable(int pres, int last, int diff){

        }
        @Override
        public void run() {

            for (int i = 1; i <= difflux; i++) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gauge3.setValue(lastlux+=1);
                    }
                });
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Log.e(this.getClass().getName(),"Thread Success33333");
                lastlux = intlux;
            }

        }
    }

    public class LuxDecRunnable implements Runnable {

        public LuxDecRunnable(int pres, int last, int diff){

        }
        @Override
        public void run() {

            for (int i = 1; i <= difflux; i++){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gauge3.setValue(lastlux-=1);
                    }
                });
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Log.e(this.getClass().getName(),"Thread Success444444");
            }
            lastlux = intlux;
        }
    }
}

