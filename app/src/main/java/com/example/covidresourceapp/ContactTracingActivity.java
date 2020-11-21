// Himnish's code

package com.example.covidresourceapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class ContactTracingActivity extends AppCompatActivity {
    private static final String TAG = "MyActivity";
    int REQUEST_ENABLE_BT = 1;
    Intent enableBluetoothIntent;
    BluetoothAdapter myAdapter;
    BluetoothDevice[] btArray;
    SendReceive sendReceive;
    GoogleSignInAccount account = MainActivity.getAccount();
    String userEmail = account.getEmail();//account.getEmail();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    private static final String APP_NAME = "ContactKeys";
    private static final UUID MY_UUID=UUID.fromString("32b3a3b1-8776-43cd-8396-eccc62d06b73");

    Button buttonON, buttonOFF;
    Button simpleSwitch;
    TextView status, msg_box;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_tracing);

        Log.w(TAG, "account is" + MainActivity.getAccount());

        buttonON = (Button) findViewById(R.id.b1);
        buttonOFF = (Button) findViewById(R.id.b2);
        simpleSwitch = (Button) findViewById(R.id.simpleSwitch);
        status = (TextView) findViewById(R.id.textView3);
        msg_box = (TextView) findViewById(R.id.textView5);

        myAdapter = BluetoothAdapter.getDefaultAdapter();
        enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        bluetoothONMethod();
        bluetoothOFFMethod();
        implementListeners();
    }

    private void implementListeners() {
        simpleSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerClass serverClass = new ServerClass();
                serverClass.start();


                Set<BluetoothDevice> bt = myAdapter.getBondedDevices();
                Log.w("MyActivity", "Does bt exist: " + bt.size());
                for (BluetoothDevice device : bt)
                {
                    Log.w(TAG, "device is" + device.getName());
                    ClientClass clientClass = new ClientClass(device);
                    clientClass.start();


                    status.setText("Connecting");

                    //            try{
                    //                sendReceive.write(userEmail.getBytes());
                    //                Log.w(TAG, "send receive is " + sendReceive);
                    //            } catch (NullPointerException e) {
                    //                e.printStackTrace();
                    //            }


                }
                if (bt.toArray().length == 0) {
                    status.setText("None Available");
                }
            }

        });

    }


    //    private void implementListeners() {
//    }
    Handler handler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what)
            {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    //status.setText("Listening");
                    byte[] read_buffer = (byte[]) msg.obj;
                    String tempMag = new String(read_buffer, 0, msg.arg1);
                    if (tempMag.length() > 0) {
                        addCloseContact(tempMag);
                        msg_box.setText(tempMag);

                    } else {
                        msg_box.setText("Still waiting");
                    }


                    break;
            }
            return true;
        }
    });

    private void addCloseContact(String contactEmail) {
        // Since emails are unique, and all are google, extract string before @
        // Because file directories can't take those symbols
        String userToken = userEmail.substring(0, userEmail.indexOf('@')).replaceAll("[\\-\\+\\.\\^:,]","");
        String key = mDatabase.child("users").push().getKey();

        Map<String, Object> postValues = new HashMap<>();

        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        postValues.put("userID", userToken);
        postValues.put("closeContactEmail", contactEmail);
        postValues.put("timestamp", timeStamp);

        DatabaseReference ref= mDatabase.child("users");
        ref.orderByChild("users").equalTo(userToken).addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //create new user
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/users/" + key, postValues);
                mDatabase.updateChildren(childUpdates);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        });

    }

    private void bluetoothOFFMethod() {
        buttonOFF.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                myAdapter.disable();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth is enabled", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth Enabling Canceled", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void bluetoothONMethod() {
        buttonON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myAdapter == null) {
                    // does not support bluetooth
                    Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
                } else {
                    if (!myAdapter.isEnabled())
                    {
                        startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
                    }
                }
            }
        });
    }
    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;
        public ServerClass() {
            try {
                serverSocket = myAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void run() {
            BluetoothSocket socket = null;

            while (socket == null)
            {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket=serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if (socket != null)
                {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    //write code for send receive
                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                }
            }
        }
    }

    private class ClientClass extends Thread {
        private BluetoothSocket socket;
        private BluetoothDevice device;

        public ClientClass(BluetoothDevice device1) {
            device = device1;
            try {
                socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        public void run() {
            //myAdapter.cancelDiscovery();
            try {
                socket.connect();
                //send receive
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                Log.w(TAG, "send receive is initialized");
                sendReceive.start();
                sendReceive.write(userEmail.getBytes());

            } catch (IOException e) {
                e.printStackTrace();
                Log.w(TAG, "send receive is NOT INIT");
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }
    private class SendReceive extends Thread
    {
        private final BluetoothSocket bsocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive (BluetoothSocket socket) {
            bsocket=socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;
            try{
                tempIn = bsocket.getInputStream();
                tempOut = bsocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;

        }

        public void run() {
            byte[] buffer=new byte[1024];
            int bytes;

            while (true)
            {
                try{
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        public void write(byte[] bytes) {

            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}