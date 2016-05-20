package com.example.bluetoothclient;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.io.IOException;

import com.example.bluetoothclient.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

public class MainActivity extends Activity {
	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothAdapter btAdapter = null;
	private BluetoothSocket btSocket = null;
	private OutputStream outputStream = null;
	private InputStream inputStream = null;
	BluetoothDevice btDevice = null;

	private static final UUID SPP_UUID = 
			UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	private static String address = "00:14:01:02:10:60";
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBtState();      
        btDevice = btAdapter.getRemoteDevice(address);
    }
	
    private void checkBtState() {
        if (btAdapter == null) {
        	popupInfo("Error", "Bluetooth is not supported by this hardware platform");
        	finish();
        }
        if (! btAdapter.isEnabled()) {
        	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);       	
        	startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == RESULT_CANCELED)
    		finish();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	if (! btAdapter.isEnabled()) 
    		return;
    	try {
    		btSocket = btDevice.createRfcommSocketToServiceRecord(SPP_UUID);
    		btAdapter.cancelDiscovery();  // Because discovery is resource intensive.
    		if (! btSocket.isConnected()) {
    			popupInfo("Connecting","wait..");
    			btSocket.connect();		  // Connecting takes long time...
    			inputStream = btSocket.getInputStream();
    			outputStream = btSocket.getOutputStream();
    		}
    	} catch (Exception e) {
    		popupInfo("Exception", e.getMessage());
    		e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
    	super.onPause();
    	if (! btAdapter.isEnabled()) 
    		return;
    	try {
    		inputStream.close();
    		outputStream.flush();
    		outputStream.close();
    		//btSocket.close();
    		btSocket.close();
    	} catch (IOException e) {
    		popupInfo("Exception", e.getMessage());
    	}
    }
    
    private void popupInfo(String title, String message) {
    	Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
    }
    
    // TODO merge these 3 methods
    public void onTempButtonClick(View view) {
    	try {
    		byte rq = 0x01;
			int b = sendReceive(rq);
	    	TextView valTextView = (TextView)findViewById(R.id.valTextView);
	    	valTextView.setText(Integer.toString(b));
		} catch (Exception e) {
			e.printStackTrace();
			popupInfo("Exception",e.getMessage());
		}
    }

    public void onHumButtonClick(View view) {
    	try {
    		byte rq = 0x02;
			int b = sendReceive(rq);
	    	TextView valTextView = (TextView)findViewById(R.id.valTextView);
	    	valTextView.setText(Integer.toString(b));
		} catch (Exception e) {
			e.printStackTrace();
			popupInfo("Exception",e.getMessage());
		}
    }
    
    public void onIllumButtonClick(View view) {
    	try {
    		byte rq = 0x03;
			int b = sendReceive(rq);
	    	TextView valTextView = (TextView)findViewById(R.id.valTextView);
	    	valTextView.setText(Integer.toString(b));
		} catch (Exception e) {
			e.printStackTrace();
			popupInfo("Exception",e.getMessage());
		}
    }
    
    public void onLcdBacklightButtonClick(View view) {
    	try {
    		byte rq = 0x0A;
			sendReceive(rq);
		} catch (Exception e) {
			e.printStackTrace();
			popupInfo("Exception",e.getMessage());
		}
    }
    
	private int sendReceive(byte rq) throws IOException {
		outputStream.write(rq);
		outputStream.flush();
		if (rq == 0x0A) return 0;
		int b = inputStream.read();
		byte[] buffer = new byte[4];
		inputStream.read(buffer, 0, 2);
		b = java.nio.ByteBuffer.wrap(buffer).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
		return b;
	} 
}