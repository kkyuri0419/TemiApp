package com.example.temixxdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class SendActivity extends AppCompatActivity {
    Context mContext;
    RecyclerView recyclerView;
    ArrayList<MsgModelRecyclerView> msgModelRecyclerViews;
    MsgAdapterRecyclerView msgAdapterRecyclerView;

    private final String connString = BuildConfig.DeviceConnectionString;

    private String msgStr;
    private Message sendMessage;
    private String lastException;
    String msg_status;
    int id_status;
    boolean check;

    private DeviceClient client;

    IotHubClientProtocol protocol = IotHubClientProtocol.MQTT;

    Button btnStart;

    TextView txtMsgsSentVal;
    TextView txtLastMsgSentVal;
    EditText txtIput;

    private int msgSentCount = 0;
    private int receiptsConfirmedCount = 0;
    private int sendFailuresCount = 0;
    private int msgReceivedCount = 0;
    private int sendMessagesInterval = 5000;

    private final Handler handler = new Handler();
    private Thread sendThread;

    private static final int METHOD_SUCCESS = 200;
    public static final int METHOD_THROWS = 403;
    private static final int METHOD_NOT_DEFINED = 404;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        mContext = getApplicationContext();
        recyclerView = findViewById(R.id.recycleview);
        msgModelRecyclerViews = new ArrayList<>();

        btnStart = findViewById(R.id.btnStart);
//        txtMsgsSentVal = findViewById(R.id.txtMsgsSentVal);
//        txtLastMsgSentVal = findViewById(R.id.txtLastMsgSentVal);
        txtIput = findViewById(R.id.txtInput);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(
                SendActivity.this, LinearLayoutManager.VERTICAL, false
        );
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        msgAdapterRecyclerView = new MsgAdapterRecyclerView(SendActivity.this, msgModelRecyclerViews);
        recyclerView.setAdapter(msgAdapterRecyclerView);

        btnStart.setEnabled(false);
        start();
    }

    public void HomeBtnOnClick(View v){
        stop();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
                    finish();
                }
                catch (Exception e)
                {
                    lastException = "Exception while closing IoTHub connection: " + e;
                    handler.post(exceptionRunnable);
                }
            }
        }).start();
    }

    private void start()
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

    public void btnStartOnClick(View v)
    {
        sendMessages();
    }

    private void sendMessages()
    {

        msgStr = String.valueOf(txtIput.getText());
        try
        {
//            hideKeyboard(SendActivity.this);
            sendMessage = new Message(msgStr);
            sendMessage.setMessageId(java.util.UUID.randomUUID().toString());
            System.out.println("Message Sent: " + msgStr);
            txtIput.setText("");
            EventCallback eventCallback = new EventCallback();
            hideKeyboard(this);
//            recyclerView.smoothScrollToPosition(msgAdapterRecyclerView.getItemCount()-1);
            client.sendEventAsync(sendMessage, eventCallback, msgSentCount);
            msgSentCount++;
//            Runnable AddNewMsgRunnable = new AddNewMsgRunnable(id_status,msg_status,msgStr);
//            new Thread(AddNewMsgRunnable).start();
            Thread.sleep(500);
            if (!check){
                Log.e(this.getClass().getName(),"ㅇㅔ드하고 직후");
                addNewMsg(R.drawable.check_icon,"SENT SUCCESSFULLY : ",msgStr);

            }else{
                Log.e(this.getClass().getName(),"페ㅇㅔ드하고 직후");
                addNewMsg(R.drawable.x_icon,"SENT FAILED : ",msgStr);


            }
            Log.e(this.getClass().getName(),"마지");
            handler.post(updateRunnable);
        }
        catch (Exception e)
        {
            System.err.println("Exception while sending event: " + e);
        }
    }

    private void initClient() throws URISyntaxException, IOException
    {
        client = new DeviceClient(connString, protocol);

        try
        {
            client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());
            client.open();
//            MessageCallback callback = new MessageCallback();
//            client.setMessageCallback(callback, null);
            client.subscribeToDeviceMethod(new SampleDeviceMethodCallback(), getApplicationContext(), new DeviceMethodStatusCallBack(), null);
        }
        catch (Exception e)
        {
            System.err.println("Exception while opening IoTHub connection: " + e);
            client.closeNow();
            System.out.println("Shutting down...");
        }
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
                check = false;
                Log.e(this.getClass().getName(),"status : "+ status);
                Log.e(this.getClass().getName(),"check1 : "+ check);
//                TextView txtReceiptsConfirmedVal = findViewById(R.id.txtReceiptsConfirmedVal);
//                receiptsConfirmedCount++;
//                txtReceiptsConfirmedVal.setText(Integer.toString(receiptsConfirmedCount));
//                btnStart.setEnabled(true);
            }
            else
            {
                check = true;
                Log.e(this.getClass().getName(),"check2 : "+ check);

            }
        }
    }

    class MessageCallback implements com.microsoft.azure.sdk.iot.device.MessageCallback
    {
        public IotHubMessageResult execute(Message msg, Object context)
        {
            System.out.println(
                    "Received message with content: " + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
            msgReceivedCount++;
//            TextView txtMsgsReceivedVal = findViewById(R.id.txtMsgsReceivedVal);
//            txtMsgsReceivedVal.setText(Integer.toString(msgReceivedCount));
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnStart.setEnabled(true);

                    }
                });
            }
        }
    }

    public void addNewMsg(int id, String status, String msg){
        msgModelRecyclerViews.add(new MsgModelRecyclerView(id,status,msg));
        recyclerView.smoothScrollToPosition(msgAdapterRecyclerView.getItemCount()-1);
        id_status = Integer.parseInt(null);
        msg_status = null;
    }

    final Runnable updateRunnable = new Runnable() {
        public void run() {

//            txtMsgsSentVal.setText(Integer.toString(msgSentCount));
//            txtLastMsgSentVal.setText("[" + new String(sendMessage.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET) + "]");
        }
    };

    final Runnable exceptionRunnable = new Runnable() {
        public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(SendActivity.this);
            builder.setMessage(lastException);
            builder.show();
            System.out.println(lastException);
            btnStart.setEnabled(true);
        }
    };

    final Runnable methodNotificationRunnable = new Runnable() {
        public void run() {
            Context context = getApplicationContext();
            CharSequence text = "Set Send Messages Interval to " + sendMessagesInterval + "ms";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    };


    private int method_setSendMessagesInterval(Object methodData) throws UnsupportedEncodingException, JSONException
    {
        String payload = new String((byte[])methodData, "UTF-8").replace("\"", "");
        JSONObject obj = new JSONObject(payload);
        sendMessagesInterval = obj.getInt("sendInterval");
        handler.post(methodNotificationRunnable);
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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
