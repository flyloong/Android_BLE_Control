package com.example.bluetooth.le;

import java.util.List;
import java.util.UUID;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

public class TioActivity extends Activity {

	private BluetoothLeService mBluetoothLeService;
	private BluetoothGattService tioService;
	private BluetoothGattCharacteristic tio1aCharacteristic;
	private BluetoothGattCharacteristic tio1bCharacteristic;
	private BluetoothGattCharacteristic tio2aCharacteristic;
	private BluetoothGattCharacteristic tio2bCharacteristic;
	
	private Button mBnStart1;
	private Button mBnStart2;
	private CheckBox mCheck1;
	private CheckBox mCheck2;
	private EditText mEdTio1A;
	private EditText mEdTio1B;
	private EditText mEdTio2A;
	private EditText mEdTio2B;
	
	
	
	private final ServiceConnection mServiceConnection = new ServiceConnection() {

        private Object pwmConfigCharacteristic;

		@Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            //Log.i("ble","onServiceConnected");
            if (!mBluetoothLeService.initialize()) {
                Log.e("jinxin", "Unable to initialize Bluetooth");
               // finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
            tioService = returnOKservice(UUID.fromString(SampleGattAttributes.TIO_SERVICE));
            tio1aCharacteristic = tioService.getCharacteristics().get(0);
            tio1bCharacteristic = tioService.getCharacteristics().get(1);
            tio2aCharacteristic = tioService.getCharacteristics().get(2);
            tio2bCharacteristic = tioService.getCharacteristics().get(3);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            
        }
    };
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tio);
		mBnStart1 = (Button)findViewById(R.id.TIO1_start);
		mBnStart2 = (Button)findViewById(R.id.TIO2_start);
		mCheck1 = (CheckBox)findViewById(R.id.checkBoxHL1);
		mCheck2 = (CheckBox)findViewById(R.id.checkBoxHL2);
		mEdTio1A = (EditText)findViewById(R.id.editTextTio1A);
		mEdTio1B = (EditText)findViewById(R.id.editTextTio1B);
		mEdTio2A = (EditText)findViewById(R.id.editTextTio2A);
		mEdTio2B = (EditText)findViewById(R.id.editTextTio2B);
		
		
		mBnStart1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int t2;
				
				String tio1b = mEdTio1B.getText().toString();
				
				if(tio1b.length() == 0){
					t2 = 0;
				}else{
					t2 = Integer.valueOf(tio1b).intValue();
				}
				byte[] tiob = int2bytes(t2);
				tio1bCharacteristic.setValue(tiob);
				tio1bCharacteristic.setWriteType(tio1bCharacteristic.getWriteType());
            	mBluetoothLeService.writeCharacteristic(tio1bCharacteristic);
            	
            	
            	
            	
            	Log.i("jinxin", "UUID1 = "+tio1aCharacteristic.getUuid()+"UUID2 = "+tio1bCharacteristic.getUuid());
			}
		});
		
		mCheck1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				byte[] tioa = new byte[6];
				int t1;
				String tio1a = mEdTio1A.getText().toString();
				if(tio1a.length() == 0){
					t1 = 0;
				}else{
					t1 = Integer.valueOf(tio1a).intValue();
				}
				
            	byte[] tioa1 = int2bytes(t1);
            	System.arraycopy(tioa1, 0, tioa, 0, 4);            	
            	
				if(isChecked){
					tioa[5] = 1;
				}else {
					tioa[5] = 0;
				}
				
				tio1aCharacteristic.setValue(tioa);
				tio1aCharacteristic.setWriteType(tio1aCharacteristic.getWriteType());
            	mBluetoothLeService.writeCharacteristic(tio1aCharacteristic);
			}
		});
		
		mBnStart2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int t2;
				String tio2b = mEdTio2B.getText().toString();

				if(tio2b.length() == 0){
					t2 = 0;
				}else{
					t2 = Integer.valueOf(tio2b).intValue();
				}
				byte[] tiob = int2bytes(t2);
				tio2bCharacteristic.setValue(tiob);
				tio2bCharacteristic.setWriteType(tio2bCharacteristic.getWriteType());
            	mBluetoothLeService.writeCharacteristic(tio2bCharacteristic);
            	Log.i("jinxin", "UUID1 = "+tio2aCharacteristic.getUuid()+"UUID2 = "+tio2bCharacteristic.getUuid());
			}
		});
		
		mCheck2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				byte[] tioa = new byte[6];
				int t1;
				String tio2a = mEdTio2A.getText().toString();
				if(tio2a.length() == 0){
					t1 = 0;
				}else{
					t1 = Integer.valueOf(tio2a).intValue();
				}
				
            	byte[] tioa1 = int2bytes(t1);
            	System.arraycopy(tioa1, 0, tioa, 0, 4);            	
            	
				if(isChecked){
					tioa[5] = 1;
				}else {
					tioa[5] = 0;
				}
				
				tio2aCharacteristic.setValue(tioa);
				tio2aCharacteristic.setWriteType(tio2aCharacteristic.getWriteType());
            	mBluetoothLeService.writeCharacteristic(tio2aCharacteristic);
			}
		});
		
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);

        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
	}

	
	@Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateAdcIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        tioService = null;
        tio1aCharacteristic = null;
        tio1bCharacteristic = null;
        tio2aCharacteristic = null;
        tio2bCharacteristic = null;
    }
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tio, menu);
		return true;
	}

	private static IntentFilter makeGattUpdateAdcIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(BluetoothLeService.ACTION_ADC1);
        //intentFilter.addAction(BluetoothLeService.ACTION_ADC2);
        
        return intentFilter;
    }
	
	private BluetoothGattService returnOKservice(UUID uuid){
    	int i =0;
    	List<BluetoothGattService> services = mBluetoothLeService.getSupportedGattServices();
    	BluetoothGattService cService;
    	
		for (BluetoothGattService gattService : services){
			if(uuid.equals(gattService.getUuid())){
				return gattService;
			}
		}
		return null;
	}
	
	 private static byte[] hexStringToByte(String hexString) {    
			if (hexString == null || hexString.equals("")) {    
				return null;    
			}    
			int[] time = new int[2];
			hexString = hexString.toUpperCase();    
			int length = hexString.length() / 2;    
			char[] hexChars = hexString.toCharArray();
			byte[] d = new byte[length];    
			for (int i = 0; i < length; i++) {    
				int pos = i * 2;    
				d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));    
			}    
			return d;    
		}  
	    
	    private static String hexStringToIntString(String hexString) {    
			if (hexString == null || hexString.equals("")) {    
				return null;    
			}    
			Log.i("jinxin","hexString = "+hexString);
			hexString = hexString.toUpperCase();    
			int length = hexString.length() / 2;    
			char[] hexChars = hexString.toCharArray();
			byte[] d = new byte[length];   
			Log.i("jinxin", "lenth = "+length);
			for (int i = 0; i < length; i++) {    
				int pos = i * 2;    
				d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));    
			}    
			int in = byteArrayToInt(d);
			Log.i("jinxin", "in = "+in);
			return int2OctStr(in);  
		}  
	    
	    public static int byteArrayToInt(byte[] b) {
	        int value = 0;
	        for (int i = 0; i < 2; i++) {
	            int shift = (2 - 1 - i) * 8;
	            value += (b[i] & 0x000000FF) << shift;
	        }
	        return value;
	    }
	    
	    private static byte[] int2bytes(int num)
	    {
	           byte[] b=new byte[4];
	           int mask=0xff;
	           for(int i=0;i<4;i++){
	                b[i]=(byte)(num>>>(24-i*8));
	           }
	          return b;
	    }

	    
	    public static int byteArrayToU32(byte[] b) {
	        int value = 0;
	        for (int i = 0; i < 4; i++) {
	            int shift = (4 - 1 - i) * 8;
	            value += (b[i] & 0x000000FF) << shift;
	        }
	        return value;
	    }
	    
	    public static int[] byteArrayToIntArray(byte[] b, int len) {
	        int v[] = new int[10];
	        for(int j=0; j<len; j++){
		        for (int i = 0; i < 2; i++) {
		            int shift = (2 - 1 - i) * 8;
		            v[j] += (b[i+j*2] & 0x000000FF) << shift;
		        }
	        }
	        return v;
	    }
	    
	    private static byte charToByte(char c) {    
			return (byte) "0123456789ABCDEF".indexOf(c);    
		}
	    
	    private static final String[] hexArr = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
	    
	    public static String byte2HexStr(byte[] byt){
			StringBuffer strRet = new StringBuffer(); 
			for(int i=0;i<byt.length;i++){ 
				strRet.append(hexArr[(byt[i] & 0xf0)/16]); 
				strRet.append(hexArr[byt[i] & 0x0f]); 
				//strRet.append(" ");
			} 
			return strRet.toString(); 
		}
	    
	    public static String int2OctStr(int adc){
	    	int tmp = adc;
	    	
			StringBuffer strRet = new StringBuffer(); 
				if(tmp/1000 > 0)
					strRet.append(hexArr[tmp/1000]); 
				tmp %= 1000;
				strRet.append(hexArr[tmp/100]); 
				tmp %= 100;
				strRet.append(hexArr[tmp/10]); 
				strRet.append(hexArr[tmp%10]); 
				strRet.append(" mV");

			return strRet.toString(); 
		}
	
}
