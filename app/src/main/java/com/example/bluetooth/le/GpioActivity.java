package com.example.bluetooth.le;

import java.util.ArrayList;
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
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class GpioActivity extends Activity {

	private BluetoothLeService mBluetoothLeService;
	private BluetoothGattService gpioService;
	private BluetoothGattCharacteristic gpio1Characteristic;
	private BluetoothGattCharacteristic gpio2Characteristic;
	private BluetoothGattCharacteristic gpio3Characteristic;
	
	private Button mBngpio0;
	private Button mBngpio1;
	private Button mBngpio2;
	private Button mBngpio9;
	private Button mBngpio10;
	private Button mBnCfg;
	private Button mBnRead;
	private Button mBnNotify;
	private Boolean bFlagNotity = false;
	
	private Spinner mSpinGpio;
	private Spinner mSpinDir;
	private Spinner mSpinFun;
	
	private List<String> list1 = new ArrayList<String>();
	private List<String> list2 = new ArrayList<String>();
	private List<String> list3 = new ArrayList<String>();
	private ArrayAdapter<String> adapter1;
	private ArrayAdapter<String> adapter2;
	private ArrayAdapter<String> adapter3;
	
	
	
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
            gpioService = returnOKservice(UUID.fromString(SampleGattAttributes.GPIO_SERVICE));
            gpio1Characteristic = gpioService.getCharacteristics().get(0);
            gpio2Characteristic = gpioService.getCharacteristics().get(1);
            gpio3Characteristic = gpioService.getCharacteristics().get(2);
  
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
            if(BluetoothLeService.ACTION_GPIO.equals(action)){

            	String getString = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
            	Log.i("jinxin","getstring= "+getString);
            	if(getString.length() < 2)
            		return;
            	
            	byte[] aaa= hexStringToByte(getString);
            	for(int i=0; i<(aaa.length/2); i++){
            		if(aaa[2*i] == 9){
            			if(aaa[2*i+1] == 1){
            				mBngpio9.setBackgroundColor(Color.rgb(123,0,0));
            			}else {
            				mBngpio9.setBackgroundColor(Color.rgb(123,123,123));
						}
            		}
            		if(aaa[2*i] == 10){
            			if(aaa[2*i+1] == 1){
            				mBngpio10.setBackgroundColor(Color.rgb(123,0,0));
            			}else {
            				mBngpio10.setBackgroundColor(Color.rgb(123,123,123));
						}
            		}
            		if(aaa[2*i] == 0){
            			if(aaa[2*i+1] == 1){
            				mBngpio0.setBackgroundColor(Color.rgb(123,0,0));
            			}else {
            				mBngpio0.setBackgroundColor(Color.rgb(123,123,123));
						}
            		}
            		if(aaa[2*i] == 1){
            			if(aaa[2*i+1] == 1){
            				mBngpio1.setBackgroundColor(Color.rgb(123,0,0));
            			}else {
            				mBngpio1.setBackgroundColor(Color.rgb(123,123,123));
						}
            		}
            		if(aaa[2*i] == 2){
            			if(aaa[2*i+1] == 1){
            				mBngpio2.setBackgroundColor(Color.rgb(123,0,0));
            			}else {
            				mBngpio2.setBackgroundColor(Color.rgb(123,123,123));
						}
            		}
            	}
            }
        }
    };
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gpio);
		mBnCfg = (Button)findViewById(R.id.gpioCfg);
		mBnRead = (Button)findViewById(R.id.gpioRead);
		mBnNotify = (Button)findViewById(R.id.gpioNotify);
		mBngpio0 = (Button)findViewById(R.id.bgpio0);
		mBngpio1 = (Button)findViewById(R.id.bgpio1);
		mBngpio2 = (Button)findViewById(R.id.bgpio2);
		mBngpio9 = (Button)findViewById(R.id.bgpio9);
		mBngpio10 = (Button)findViewById(R.id.bgpio10);
		
		mSpinGpio = (Spinner)findViewById(R.id.spinner1);
		mSpinDir = (Spinner)findViewById(R.id.spinner2);
		mSpinFun = (Spinner)findViewById(R.id.spinner3);
		
		mBngpio0.setBackgroundColor(Color.rgb(123,0,0));
		list1.add("GPIO0");
		list1.add("GPIO1");
		list1.add("GPIO2");
		list1.add("GPIO9");
		list1.add("GPIO10");
		
		list2.add("input");
		list2.add("output");
		
		list3.add("3-state/low");
		list3.add("pullup/high");
		list3.add("pulldown");
		
		adapter1 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list1);       
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);     
        mSpinGpio.setAdapter(adapter1); 
        
        adapter2 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list2);      
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);      
        mSpinDir.setAdapter(adapter2); 
        
        adapter3 = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, list3);          
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);     
        mSpinFun.setAdapter(adapter3); 
		
        
        
        mBnCfg.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				byte[] GpioTable =new byte[] {0,1,2,9,10};
				int a;
				int b;
				int c;
				a = mSpinGpio.getSelectedItemPosition();
				b = mSpinDir.getSelectedItemPosition();
				c = mSpinFun.getSelectedItemPosition();
				
				
				byte[] da = new byte[6];
				da[0] = GpioTable[a];
				da[1] = (byte)b;
				da[2] = (byte)c;
				Log.i("jinxin","a= "+da[0]+"  b= "+da[1]+"  c= "+da[2]);
				gpio1Characteristic.setValue(da);
				Log.i("jinxin","gpio uuid= "+gpio1Characteristic.getUuid());
				gpio1Characteristic.setWriteType(gpio1Characteristic.getWriteType());
            	mBluetoothLeService.writeCharacteristic(gpio1Characteristic);
				
			}
		});
        
        mBnRead.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mBluetoothLeService.readCharacteristic(gpio3Characteristic);
				
			}
		});
        
        mBnNotify.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!bFlagNotity){
				mBluetoothLeService.setCharacteristicNotification(
						gpio3Characteristic, true);
				mBnNotify.setText("ON");
				}else {
					mBluetoothLeService.setCharacteristicNotification(
							gpio3Characteristic, false);
					mBnNotify.setText("OFF");
				}
				bFlagNotity = !bFlagNotity;
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
        gpioService = null;
        gpio1Characteristic = null;
        gpio2Characteristic = null;
        gpio3Characteristic = null;
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.gpio, menu);
		return true;
	}
	
	private static IntentFilter makeGattUpdateAdcIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GPIO);
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
	private static byte charToByte(char c) {    
		return (byte) "0123456789ABCDEF".indexOf(c);    
	}

}
