package com.example.bluetooth.le;

import java.util.List;
import java.util.UUID;

import android.os.Bundle;
import android.os.IBinder;
import android.annotation.SuppressLint;
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
import android.widget.RadioButton;
import android.widget.TextView;

@SuppressLint("NewApi")
public class AdcActivity extends Activity {

	 private BluetoothLeService mBluetoothLeService;
	 private BluetoothGattService adcService;
	 private BluetoothGattCharacteristic adcSwithCharacteristic;
	 private BluetoothGattCharacteristic adcTimeCharacteristic;
	 private BluetoothGattCharacteristic adc1Characteristic;
	 private BluetoothGattCharacteristic adc2Characteristic;
	 private Button mBnRead1;
	 private Button mBnRead2;
	 private Button mBnConfig;
	 private EditText mAdc1Time;
	 private EditText mAdc2Time;
	 private CheckBox mAdc1On;
	 private CheckBox mAdc2On;
	 private TextView mAdc1Out;
	 private TextView mAdc2Out;
	 
	// Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

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
            adcService = returnOKservice(UUID.fromString(SampleGattAttributes.ADC_SERVICE));
            adcSwithCharacteristic = adcService.getCharacteristics().get(0);
            adcTimeCharacteristic = adcService.getCharacteristics().get(1);
            adc1Characteristic = adcService.getCharacteristics().get(2);
            adc2Characteristic = adcService.getCharacteristics().get(3);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.i("jinxin","onServiceDisconnected--4");
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            
            if (BluetoothLeService.ACTION_ADC1.equals(action)) {
            	String getString = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
            	Log.i("jinxin","getstring= "+getString);
            	String outString = hexStringToIntString(getString);
            	mAdc1Out.setText(outString);
            }else if(BluetoothLeService.ACTION_ADC2.equals(action)){
            	String getString = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
            	String outString = hexStringToIntString(getString);
            	mAdc2Out.setText(outString);
            }
            
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adc);
		
		mBnRead1 = (Button)findViewById(R.id.button1);
		mBnRead2 = (Button)findViewById(R.id.button2);
		mBnConfig = (Button)findViewById(R.id.button3);
		mAdc1Time = (EditText)findViewById(R.id.editText1);
		mAdc2Time = (EditText)findViewById(R.id.editText2);
		mAdc1On = (CheckBox)findViewById(R.id.checkBox1);
		mAdc2On = (CheckBox)findViewById(R.id.checkBox2);
		mAdc1Out = (TextView)findViewById(R.id.textView3);
		mAdc2Out = (TextView)findViewById(R.id.textView4);
		
		
		mBnRead1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
		        final int charaProp = adc1Characteristic.getProperties();
		        final UUID uuid = adc1Characteristic.getUuid();
		        Log.i("jinxin","adc uuid= "+uuid);
		        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
		        	 
                    // If there is an active notification on a characteristic, clear
                    mBluetoothLeService.readCharacteristic(adc1Characteristic);
                }
		        
			}
		});
		
		mBnRead2.setOnClickListener(new View.OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				final int charaProp = adc2Characteristic.getProperties();
		        final UUID uuid = adc2Characteristic.getUuid();
		        Log.i("jinxin","adc uuid= "+uuid);
		        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
		        	 
                    // If there is an active notification on a characteristic, clear
                    mBluetoothLeService.readCharacteristic(adc2Characteristic);
                }
		        
			}
		});
		mBnConfig.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				final int charaProp = adcTimeCharacteristic.getProperties();
		        final UUID uuid = adcTimeCharacteristic.getUuid();
                
                String cTime1String = mAdc1Time.getText().toString();
                String cTime2String = mAdc2Time.getText().toString();
            	
            	int[] ddd = new int[2];
            	if(cTime1String.length() == 0)
            		ddd[0] = 0;
            	else
            		ddd[0] = Integer.valueOf(cTime1String).intValue();
            	
            	if(cTime2String.length() == 0)
            		ddd[1] = 0;
            	else 
            		ddd[1] = Integer.valueOf(cTime2String).intValue();
				
            	
            	byte tim1[] = int2bytes(ddd[0]);
                byte tim2[] = int2bytes(ddd[1]);
                
            	byte[] tim = new byte[8];
            	System.arraycopy(tim1, 0, tim, 0, 4);
            	System.arraycopy(tim2, 0, tim, 4, 4);
            	Log.i("jinxin","time = "+ddd[0]+"  tim2 = "+ddd[1]);
            	adcTimeCharacteristic.setValue(tim);
            	adcTimeCharacteristic.setWriteType(adcTimeCharacteristic.getWriteType());
            	mBluetoothLeService.writeCharacteristic(adcTimeCharacteristic);
			}
		});
		
		mAdc1On.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
                final int charaProp = adcSwithCharacteristic.getProperties();
                final int charPr1 = adc1Characteristic.getProperties();
                //Log.i("jinxin", "adc swith uuid= "+uuid+"t3= "+uuid3);
				if(isChecked){
					if ((charPr1 & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
						mBluetoothLeService.setCharacteristicNotification(
								adc1Characteristic, true);
						Log.i("jinxin", "start adc1 sample");
					}
					if((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
						byte[] t = {0x01,0x00};
						adcSwithCharacteristic.setValue(t);
						adcSwithCharacteristic.setWriteType(adcSwithCharacteristic.getWriteType());
	                	mBluetoothLeService.writeCharacteristic(adcSwithCharacteristic);
					}
				}else{
					mBluetoothLeService.setCharacteristicNotification(
							adc1Characteristic, false);
					Log.i("jinxin", "stop adc1 sample");
					
					byte[] t = {0x00,0x00};
					adcSwithCharacteristic.setValue(t);
					adcSwithCharacteristic.setWriteType(adcSwithCharacteristic.getWriteType());
                	mBluetoothLeService.writeCharacteristic(adcSwithCharacteristic);
                	
				}
			}
		});
		
		mAdc2On.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
                final int charaProp = adcSwithCharacteristic.getProperties();
                final int charPr2 = adc2Characteristic.getProperties();
                //Log.i("jinxin", "adc swith uuid= "+uuid+"t3= "+uuid3);
				if(isChecked){
					if ((charPr2 & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
						mBluetoothLeService.setCharacteristicNotification(
								adc2Characteristic, true);
						Log.i("jinxin", "start adc2 sample");
					}
					if((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
						byte[] t = {0x02,0x00};
						adcSwithCharacteristic.setValue(t);
						adcSwithCharacteristic.setWriteType(adcSwithCharacteristic.getWriteType());
	                	mBluetoothLeService.writeCharacteristic(adcSwithCharacteristic);
					}
				}else{
					mBluetoothLeService.setCharacteristicNotification(
							adc2Characteristic, false);
					Log.i("jinxin", "stop adc2 sample");
					
					byte[] t = {0x00,0x00};
					adcSwithCharacteristic.setValue(t);
					adcSwithCharacteristic.setWriteType(adcSwithCharacteristic.getWriteType());
                	mBluetoothLeService.writeCharacteristic(adcSwithCharacteristic);
                	
				}
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
        adcService = null;
        adcSwithCharacteristic = null;
        adcTimeCharacteristic = null;
        adc1Characteristic = null;
        adc2Characteristic = null;
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.adc, menu);
		return true;
	}
	
	private static IntentFilter makeGattUpdateAdcIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_ADC1);
        intentFilter.addAction(BluetoothLeService.ACTION_ADC2);
        
        return intentFilter;
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
}
