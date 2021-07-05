package com.example.trinkgeldprototype;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    boolean isConnected=false;
    TextView incomingMessages;
    StringBuilder message;
    BluetoothAdapter myAdapter;
    private TextView mBluetoothStatus;
    private TextView discoverDisplay;
    private TextView mBondStateView;
    private TextView mBondStateView2;
    private TextView mConnectionState;
    Button btnSend;

    Set<BluetoothDevice> s;

    BluetoothConnectionService mBluetoothConnection;
    EditText etSend;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    BluetoothDevice mBTDevice;


    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView IvNewDevices;
    TextView deviceView;


    private final BroadcastReceiver mBroadcastReciver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(myAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR);
                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        mBluetoothStatus.setText("Bluetooth Off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        mBluetoothStatus.setText("Bluetooth turning off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        mBluetoothStatus.setText("Bluetooth ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        mBluetoothStatus.setText("Bluetooth turning ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReciver2 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(myAdapter.ACTION_SCAN_MODE_CHANGED)) {
                final int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE,BluetoothAdapter.ERROR);
                switch (mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        discoverDisplay.setText("discoverability Enabled");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        discoverDisplay.setText("Discoverability Disabled. Able to receive Connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        discoverDisplay.setText("Discoverability Disabled. not Able to receive Connections");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        discoverDisplay.setText("Connecting");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        discoverDisplay.setText("Connected");
                        break;
                }
            }
        }
    };


    private BroadcastReceiver mBroadcastReciver3=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(BluetoothDevice.ACTION_FOUND)){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (!(device.getName()==null)){
                    mBTDevices.add(device);}
                    deviceView.setText("" + device.getName()+ ": " + device.getAddress());

                    mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                    IvNewDevices.setAdapter(mDeviceListAdapter);
                }
        }
    };
    private final BroadcastReceiver mBroadcastReciver4= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action= intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice =intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mDevice.getBondState()==BluetoothDevice.BOND_BONDED){
                    mBondStateView.setText("BOND_BONDED");
                }
                if (mDevice.getBondState()==BluetoothDevice.BOND_BONDING){
                    mBondStateView.setText("BOND_BONDING");
                }
                if (mDevice.getBondState()==BluetoothDevice.BOND_NONE){
                    mBondStateView.setText("BOND_NONE");
                }
            }
        }
    };
    private final BroadcastReceiver mBroadcastReciver5= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action= intent.getAction();

            if (action.equals(myAdapter.EXTRA_CONNECTION_STATE)){
                BluetoothDevice mDevice =intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (mDevice.getBondState()==myAdapter.STATE_CONNECTING){
                    mConnectionState.setText("Connecting");
                }
                if (mDevice.getBondState()==myAdapter.STATE_CONNECTED){
                    mConnectionState.setText("Connected");
                }
            }
        }
    };
    private final BroadcastReceiver mReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");

            message.append(text+ "\n");
            incomingMessages.setText(message);
        }
    };

    //Check Connection Receiver
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Device found
                mConnectionState.setText("Device Found");
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //Device is now connected
                mConnectionState.setText("Connected");
                isConnected=true;
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Done searching
                mConnectionState.setText("Done Searching");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
                mConnectionState.setText("about to Disconnected");
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected
                mConnectionState.setText("Disconnected");
                isConnected=false;
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReciver);
        unregisterReceiver(mBroadcastReciver2);
        unregisterReceiver(mBroadcastReciver3);
        unregisterReceiver(mBroadcastReciver4);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter filter=new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReciver4,filter);
        mBondStateView = (TextView) findViewById(R.id.bondState);
        mBondStateView2 = (TextView) findViewById(R.id.bondState2);
        myAdapter=BluetoothAdapter.getDefaultAdapter();
        IvNewDevices =(ListView) findViewById(R.id.IvNewDevices);
        deviceView =(TextView) findViewById(R.id.deviceView);
        mBTDevices = new ArrayList<>();
        mBluetoothStatus = (TextView)findViewById(R.id.StateView);
        discoverDisplay = (TextView)findViewById(R.id.discoverablty);
        IvNewDevices.setOnItemClickListener(MainActivity.this);

        mConnectionState= findViewById(R.id.connectionState);

        s=myAdapter.getBondedDevices();
        incomingMessages = (TextView) findViewById(R.id.incomingMassages);
        message = new StringBuilder();

        LocalBroadcastManager.getInstance(this).registerReceiver(mReciver, new IntentFilter("incomingMessage"));

        btnSend = (Button) findViewById(R.id.btnSend);
        etSend = (EditText) findViewById(R.id.editText);

        //BroadcastReceiver to check Connection
        IntentFilter filter2 = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mReceiver, filter2);
        //discoverDisplay=(findViewById(R.id.textView2));
        IntentFilter filter3 = new IntentFilter(myAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReciver2,filter2);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                try {
                    mBluetoothConnection.write(bytes);
                }catch (NullPointerException e){
                    Toast.makeText(MainActivity.this,"Not Connected",Toast.LENGTH_SHORT).show();
                }
                etSend.setText("");
            }
        });
    }

    public void nextActivity(View v){
        Intent intent=new Intent(this, Beginning.class);
        startActivity(intent);
    }

    public void startConnection (BluetoothDevice device){
        startBTConnection(device,MY_UUID_INSECURE);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        mBondStateView.setText("Initializing RFCOM Connection");
        mBluetoothConnection.startClient(device,uuid);
    }

    public void btnDiscover(View v){
        if (!myAdapter.isEnabled()){
            myAdapter.enable();

            IntentFilter BTintent=new IntentFilter(myAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReciver, BTintent);
            Toast.makeText(MainActivity.this,"Enabled",Toast.LENGTH_SHORT).show();
        }
        mBTDevices.clear();
        if (myAdapter.isDiscovering()){
            myAdapter.cancelDiscovery();
            checkPermission();
            myAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReciver3, discoverDevicesIntent);
        }
        if (!myAdapter.isDiscovering()) {
            checkPermission();
            myAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReciver3, discoverDevicesIntent);
        }
    }

    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_BACKGROUND_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 1001); //Any number
            }
        }
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
        } else {
            checkPermission();
        }
    }

    public void bluetoothOnOff(View v){
        mBTDevices.clear();
        if (myAdapter==null){
            Toast.makeText(MainActivity.this,"not supported",Toast.LENGTH_SHORT).show();
        }
        if (!myAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);

            IntentFilter BTintent=new IntentFilter(myAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReciver, BTintent);
            Toast.makeText(MainActivity.this,"Enabled",Toast.LENGTH_SHORT).show();
        }
        if (myAdapter.isEnabled()){
            myAdapter.disable();
            IntentFilter BTintent=new IntentFilter(myAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReciver, BTintent);
            Toast.makeText(MainActivity.this,"Disabled",Toast.LENGTH_SHORT).show();
        }
        IntentFilter filter = new IntentFilter(myAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReciver2,filter);
    }

    public void enableDisableDiscover(View V){
        if (!myAdapter.isEnabled()){
            myAdapter.enable();

            IntentFilter BTintent=new IntentFilter(myAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReciver, BTintent);
            Toast.makeText(MainActivity.this,"Enabled",Toast.LENGTH_SHORT).show();
        }
        mBTDevices.clear();
        Toast.makeText(MainActivity.this,"Making Device discoverable for 300 Seconds",Toast.LENGTH_SHORT).show();
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(myAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        startActivity(discoverableIntent);
        mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
        IntentFilter filter = new IntentFilter(myAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReciver2,filter);
    }

    public void getPaired(View v){
        ArrayList<BluetoothDevice> pairedDevices = new ArrayList<>();
        for (BluetoothDevice dv : s){
            pairedDevices.add(dv);
        }
        mDeviceListAdapter = new DeviceListAdapter(this, R.layout.device_adapter_view, pairedDevices);
        IvNewDevices.setAdapter(mDeviceListAdapter);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        myAdapter.cancelDiscovery();
        String deviceName=mBTDevices.get(position).getName();
        String deviceAddress=mBTDevices.get(position).getAddress();
        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.JELLY_BEAN_MR2){
            mBondStateView2.setText("Bonding to "+deviceName+": "+deviceAddress);
            if (s.contains(mBTDevices.get(position))){
                mBondStateView2.setText("Already Bonded");
            }else{
                mBTDevices.get(position).createBond();
            }
            mBTDevice=mBTDevices.get(position);
            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
            startConnection(mBTDevice);
        }
        IntentFilter filter = new IntentFilter(myAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReciver2,filter);
        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mBroadcastReciver5,filter2);
    }
}