package com.example.temixxdk;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import com.robotemi.sdk.Robot;
//import com.robotemi.sdk.TtsRequest;
//import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
//
//import org.jetbrains.annotations.NotNull;

public class MeasureActivity extends AppCompatActivity /*implements OnGoToLocationStatusChangedListener */{

    private final String connString = BuildConfig.DeviceConnectionString;
    IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

    static DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    static DatabaseReference conditionRef = mRootRef.child("text");
    ValueEventListener listener;

    RecyclerView recyclerView;

    ArrayList<MainModelRecyclerView> mainModelRecyclerViews;
    MainAdapterRecyclerView mainAdapterRecyclerView;

    Button btnGo;
    TextView showloca;
    String msg = "";
    String selectedLocation;
    TemiApplication mApp;

    private String txtStr;
    private Message sendMessage;
    private DeviceClient client;
    private int msgSentCount = 0;
    private Thread sendThread;
    private Thread sendtxtMessagesThread;
    private String lastException;

    private static final int METHOD_SUCCESS = 200;
    public static final int METHOD_THROWS = 403;
    private static final int METHOD_NOT_DEFINED = 404;
    private int sendMessagesInterval = 5000;
    public static Handler handler = new Handler();


//    private Robot robot;
    private String[] location= {"Homebase", "Pantry", "Temperature Taking Station", "Entrance", "IT Department", "Fullerton", "Testing"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure);

        btnGo = findViewById(R.id.btnGo);
        recyclerView = findViewById(R.id.recyclerView);
        showloca = findViewById(R.id.showlocationtxt);

//        robot = Robot.getInstance();
//        List<String> locationList = robot.getLocations();
//        location = new String[locationList.size()];
//        location = locationList.toArray(location);

        //make each recyclerview model according to the locations from Temi.
        mainModelRecyclerViews = new ArrayList<>();
        for (int i=0; i<location.length;i++){
            MainModelRecyclerView model = new MainModelRecyclerView(location[i]);
            mainModelRecyclerViews.add(model);
        }
        TemiApplication.bSize = mainModelRecyclerViews.size();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                MeasureActivity.this, LinearLayoutManager.HORIZONTAL, false
        );
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // put the recyclerview models in the recyclerview.
        mainAdapterRecyclerView = new MainAdapterRecyclerView(MeasureActivity.this, mainModelRecyclerViews);

        //리사이클러뷰에서 동작하는 모든 동작처리를 어답터에서 함.
        recyclerView.setAdapter(mainAdapterRecyclerView);
//        robot.addOnGoToLocationStatusChangedListener(this);


        mainAdapterRecyclerView.setOnItemClickListener(new MainAdapterRecyclerView.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int posision) {
                showloca.setText(TemiApplication.showlocations);
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        startSendingcConnection();

        listener = conditionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                msg = dataSnapshot.getValue(String.class);
                msg = msg.replace("{","");
                msg = msg.replace("}","");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    public void btnhomeOnClick(View view) {
        conditionRef.removeEventListener(listener);
//        robot.removeOnGoToLocationStatusChangedListener(this);
        stop();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void btnGoOnClick(View view) {
        temigoing();
        TemiApplication.showlocations = "";
        showloca.setText("");
    }

    public void temigoing () {

        for(int i = 0; i < TemiApplication.booleans.length; i++) {
            if (TemiApplication.booleans[i]) {
                selectedLocation = location[i];
                Log.e(this.getClass().getName(), "SELECTED ACtivity : " + String.valueOf(i));
                TemiApplication.booleans[i] = false;
//                robot.speak(TtsRequest.create("Going to the" + selectedLocation, true));
//                robot.goTo(selectedLocation.toLowerCase().trim());
                Log.e(this.getClass().getName(), "TEMI GOING TO " + selectedLocation);
                break;
            }
        }
    }

//    @Override
//    public void onGoToLocationStatusChanged(@NotNull String location, String status, int descriptionId, @NotNull String description) {
//        Log.d("GoToStatusChanged", "status=" + status + ", descriptionId=" + descriptionId + ", description=" + description);
////            robot.speak(TtsRequest.create(description, false));
//            switch (status) {
//                case "start":
////                    robot.speak(TtsRequest.create("Starting", false));
//                    break;
//
//                case "calculating":
////                    robot.speak(TtsRequest.create("Calculating", false));
//                    break;
//
//                case "going":
////                    robot.speak(TtsRequest.create("Going", false));
//                    break;
//
//                case "complete":
//                    robot.speak(TtsRequest.create("Start Temperature measuring at" + selectedLocation, true));
//                    sendtxtMessages();
//                    Log.e(this.getClass().getName(),"LISTENER");
//                    Log.e(this.getClass().getName(),"LISTENER2");
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    temigoing();
//                    break;
//
//                case "abort":
////                    robot.speak(TtsRequest.create("Cancelled", false));
//                    break;
//            }
//    }


    ///////////////////////////////////////////////////SEND/////////////////////////////////////////////////
    private void startSendingcConnection()
    {
        sendThread = new Thread(new Runnable() {
            public void run()
            {
                try
                {
                    initClient();
                } catch (Exception e)
                {
                    lastException = "Exception while opening IoTHub connection: " + e;
                    handler.post(exceptionRunnable);
                }
            }
        });
        sendThread.start();
    }

    private void stop()
    {
        new Thread(new Runnable() {
            public void run()
            {
                try
                {
                    sendThread.interrupt();
                    client.closeNow();
                    System.out.println("Shutting down...");
                }
                catch (Exception e)
                {
                    lastException = "Exception while closing IoTHub connection: " + e;
//                    handler.post(exceptionRunnable);
                }
            }
        }).start();
    }

    private void initClient() throws URISyntaxException, IOException {

        client = new DeviceClient(connString, protocol);

        try
        {
            client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());
            client.open();
            MessageCallback callback = new MessageCallback();
            client.setMessageCallback(callback, null);
            client.subscribeToDeviceMethod(new SampleDeviceMethodCallback(), getApplicationContext(), new DeviceMethodStatusCallBack(), null);
        }
        catch (Exception e)
        {
            System.err.println("Exception while opening IoTHub connection: " + e);
            try {
                client.closeNow();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("Shutting down...");
            Toast("IOT HUB SENDING CONNECTION LOST");
        }
    }

    private void sendtxtMessages() {


        sendtxtMessagesThread = new Thread(new Runnable(){
            public void run(){
                txtStr = "{\"LOCATION\" : " + "\""+selectedLocation+ "\""+", " + msg+"}";
                try
                {
                    sendMessage = new Message(txtStr);
                    sendMessage.setMessageId(java.util.UUID.randomUUID().toString());
                    System.out.println("Message Sent: " + txtStr);
                    EventCallback eventCallback = new EventCallback();
                    client.sendEventAsync(sendMessage, eventCallback, msgSentCount);
                    msgSentCount++;
//            handler.post(updateRunnable);
                }
                catch (Exception e)
                {
                    System.err.println("Exception while sending event: " + e);
                }

            }
        });
        sendtxtMessagesThread.start();
    }


    final Runnable exceptionRunnable = new Runnable() {
        public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MeasureActivity.this);
            builder.setMessage(lastException);
            builder.show();
            System.out.println(lastException);
//            btnStart.setEnabled(true);
        }
    };

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    class EventCallback implements IotHubEventCallback
    {

        public void execute(IotHubStatusCode status, Object context)
        {
            Integer i = context instanceof Integer ? (Integer) context : 0;
            System.out.println("IoT Hub responded to message " + i.toString()
                    + " with status " + status.name());

            if((status == IotHubStatusCode.OK) || (status == IotHubStatusCode.OK_EMPTY))
            {
//                TextView txtReceiptsConfirmedVal = findViewById(R.id.txtReceiptsConfirmedVal);
//                receiptsConfirmedCount++;
//                txtReceiptsConfirmedVal.setText(Integer.toString(receiptsConfirmedCount));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        txtMessage.setText(null);
//                        btnStart.setEnabled(true);
                        Log.e(this.getClass().getName(),"CONNECTION SUCCESSFUL LOGGGGGGG");

                    }
                });
            }
            else
            {
                Log.e(this.getClass().getName(),"CONNECTION FAILES.... LOGGGGGGG");
//                TextView txtSendFailuresVal = findViewById(R.id.txtSendFailuresVal);
//                sendFailuresCount++;
//                txtSendFailuresVal.setText(Integer.toString(sendFailuresCount));
            }
        }
    }

    class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            System.out.println(
                    "Received message with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
//            msgReceivedCount++;
//            txtLastMsgReceivedVal.setText("[" + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET) + "]");
            return IotHubMessageResult.COMPLETE;
        }
    }

    protected class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback
    {
        @Override
        public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
        {
            System.out.println();
            System.out.println("CONNECTION STATUS UPDATE: " + status);
            System.out.println("CONNECTION STATUS REASON: " + statusChangeReason);
            System.out.println("CONNECTION STATUS THROWABLE: " + (throwable == null ? "null" : throwable.getMessage()));
            System.out.println();

            if (throwable != null)
            {
                throwable.printStackTrace();
            }

            if (status == IotHubConnectionStatus.DISCONNECTED)
            {
                //connection was lost, and is not being re-established. Look at provided exception for
                // how to resolve this issue. Cannot send messages until this issue is resolved, and you manually
                // re-open the device client
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                //connection was lost, but is being re-established. Can still send messages, but they won't
                // be sent until the connection is re-established
            }
            else if (status == IotHubConnectionStatus.CONNECTED)
            {
                //Connection was successfully re-established. Can send messages.
            }
        }
    }

    private int method_setSendMessagesInterval(Object methodData) throws UnsupportedEncodingException, JSONException
    {
        String payload = new String((byte[])methodData, "UTF-8").replace("\"", "");
        JSONObject obj = new JSONObject(payload);
        sendMessagesInterval = obj.getInt("sendInterval");
//        handler.post(methodNotificationRunnable);
        return METHOD_SUCCESS;
    }

    private int method_default(Object data)
    {
        System.out.println("invoking default method for this device");
        // Insert device specific code here
        return METHOD_NOT_DEFINED;
    }

    protected class DeviceMethodStatusCallBack implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            System.out.println("IoT Hub responded to device method operation with status " + status.name());
        }
    }

    protected class SampleDeviceMethodCallback implements com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodCallback
    {
        @Override
        public DeviceMethodData call(String methodName, Object methodData, Object context)
        {
            DeviceMethodData deviceMethodData ;
            try {
                switch (methodName) {
                    case "setSendMessagesInterval": {
                        int status = method_setSendMessagesInterval(methodData);
                        deviceMethodData = new DeviceMethodData(status, "executed " + methodName);
                        break;
                    }
                    default: {
                        int status = method_default(methodData);
                        deviceMethodData = new DeviceMethodData(status, "executed " + methodName);
                    }
                }
            }
            catch (Exception e)
            {
                int status = METHOD_THROWS;
                deviceMethodData = new DeviceMethodData(status, "Method Throws " + methodName);
            }
            return deviceMethodData;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void Toast(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


}
