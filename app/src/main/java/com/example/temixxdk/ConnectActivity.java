package com.example.temixxdk;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

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
//import com.robotemi.sdk.Robot;
//import com.robotemi.sdk.TtsRequest;
//import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
//
//import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


public class ConnectActivity extends AppCompatActivity /*implements OnGoToLocationStatusChangedListener */{

    private final String connString = BuildConfig.DeviceConnectionString;
    IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

    static DatabaseReference mRootRef = FirebaseDatabase.getInstance().getReference();
    static DatabaseReference conditionRef = mRootRef.child("text");
    static DatabaseReference connectionRef = mRootRef.child("connection");
    static DatabaseReference locationRef = mRootRef.child("location");
    ValueEventListener connection_listener;
    ValueEventListener location_listener;
    ValueEventListener condition_listener;

    Switch switchview;
    TextView txtchanging;
    TextView onNoff;
    String locationStr="";
    String msg = "";
    String selectedLocation;
    private DeviceClient client;
    private int msgSentCount = 0;
    List<String > locationArray;
    private String txtStr;
    private Message sendMessage;

//    private Robot robot;
    private Thread sendThread;
    private Thread sendtxtMessagesThread;
    private String lastException;
    private static final int METHOD_SUCCESS = 200;
    public static final int METHOD_THROWS = 403;
    private static final int METHOD_NOT_DEFINED = 404;
    private int sendMessagesInterval = 5000;
    public static Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

//        robot = Robot.getInstance();
        switchview = findViewById(R.id.switchview);
        txtchanging = findViewById(R.id.txtchanging);
        onNoff = findViewById(R.id. onNoff);

        startSendingcConnection();
//        robot.addOnGoToLocationStatusChangedListener(this);

        condition_listener = conditionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                msg = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        switchview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    onNoff.setText("Phone Connection ON");
                    txtchanging.setText("Searching...");

                    connection_listener = connectionRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String connection_status = dataSnapshot.getValue(String.class);
                            if (connection_status.equals("connect")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtchanging.setText("Connected");
                                    }
                                });

                                location_listener = locationRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        locationStr = dataSnapshot.getValue(String.class);
                                        if (locationStr.contains(",")){
                                            locationArray = new LinkedList<>(Arrays.asList(locationStr.split(",")));
                                            goToLocation();
                                        }else if(!locationStr.equals("")){
                                            locationArray = new LinkedList<>();
                                            locationArray.add(locationStr);
                                            goToLocation();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



                }else{
                    onNoff.setText("Phone Connection OFF");
                    txtchanging.setText("");
                    connectionRef.removeEventListener(connection_listener);
                    locationRef.removeEventListener(location_listener);
                }
            }
        });

    }

    public void goHomebtn(View view) {
        if (condition_listener!=null){
            conditionRef.removeEventListener(condition_listener);
        }
        if(connection_listener!=null){
            conditionRef.removeEventListener(connection_listener);
        }
        if(location_listener!=null){
            conditionRef.removeEventListener(location_listener);
        }
//        robot.removeOnGoToLocationStatusChangedListener(this);
        stop();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void goToLocation(){
        if (locationArray != null){
            for (String location:locationArray){
//                for (String temlo:robot.getLocations()){
//                    if(location.equals(temlo)){
//                        selectedLocation = location;
//                        robot.speak(TtsRequest.create("Going to the " + selectedLocation, true));
//                        robot.goTo(location);
//                        locationArray.remove(selectedLocation);
//                        break;
//                    }
//                }
                break;
            }
        }
    }

//    @Override
//    public void onGoToLocationStatusChanged(@NotNull String location, String status, int descriptionId, @NotNull String description) {
//        Log.d("GoToStatusChanged", "status=" + status + ", descriptionId=" + descriptionId + ", description=" + description);
////            robot.speak(TtsRequest.create(description, false));
//        switch (status) {
//            case "start":
////                    robot.speak(TtsRequest.create("Starting", false));
//                break;
//
//            case "calculating":
////                    robot.speak(TtsRequest.create("Calculating", false));
//                break;
//
//            case "going":
////                    robot.speak(TtsRequest.create("Going", false));
//                break;
//
//            case "complete":
//                robot.speak(TtsRequest.create("Start Temperature measuring at" + selectedLocation, true));
//                sendtxtMessages();
//                Log.e(this.getClass().getName(),"LISTENER");
//                Log.e(this.getClass().getName(),"LISTENER2");
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                goToLocation();
//                break;
//
//            case "abort":
//                break;
//        }
//    }

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
            AlertDialog.Builder builder = new AlertDialog.Builder(ConnectActivity.this);
            builder.setMessage(lastException);
            builder.show();
            System.out.println(lastException);
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

            }
            else
            {
                Log.e(this.getClass().getName(),"CONNECTION FAILES.... LOGGGGGGG");

            }
        }
    }

    class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            System.out.println(
                    "Received message with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
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


}
