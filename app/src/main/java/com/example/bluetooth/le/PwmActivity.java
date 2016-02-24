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
import android.widget.EditText;

@SuppressLint("NewApi")
public class PwmActivity extends Activity {

	private BluetoothLeService mBluetoothLeService;
	 private BluetoothGattService pwmService;
	 private BluetoothGattCharacteristic pwm1Characteristic;
	 private BluetoothGattCharacteristic pwm2Characteristic;
	 private BluetoothGattCharacteristic pwm3Characteristic;
	 private BluetoothGattCharacteristic pwmConfigCharacteristic;
	 
	private Button mBnPwm1;
	private Button mBnPwm2;
	private Button mBnPwm3;
	private Button mBnFreq;
	
	private EditText mEdPwm1Per;
	private EditText mEdPwm2Per;
	private EditText mEdPwm3Per;
	private EditText mEdFreq;
	
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
            pwmService = returnOKservice(UUID.fromString(SampleGattAttributes.PWM_SERVICE));
            pwmConfigCharacteristic = pwmService.getCharacteristics().get(0);
            pwm1Characteristic = pwmService.getCharacteristics().get(1);
            pwm2Characteristic = pwmService.getCharacteristics().get(2);
            pwm3Characteristic = pwmService.getCharacteristics().get(3);
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
            
        }
    };
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pwm);
		
		mBnFreq = (Button)findViewById(R.id.fSet);
		mBnPwm1 = (Button)findViewById(R.id.pwm1);
		mBnPwm2 = (Button)findViewById(R.id.pwm2);
		mBnPwm3 = (Button)findViewById(R.id.pwm3);
		mEdFreq = (EditText)findViewById(R.id.editText4);
		mEdPwm1Per = (EditText)findViewById(R.id.editText1);
		mEdPwm2Per = (EditText)findViewById(R.id.editText2);
		mEdPwm3Per = (EditText)findViewById(R.id.editText3);
		
		mBnFreq.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int f;
				String eFreqString = mEdFreq.getText().toString();
				if(eFreqString.length() == 0){
					f = 0;
				}else{
					f = Integer.valueOf(eFreqString).intValue();
				}
				byte freq[] = int2bytes(f);
				pwmConfigCharacteristic.setValue(freq);
				pwmConfigCharacteristic.setWriteType(pwmConfigCharacteristic.getWriteType());
            	mBluetoothLeService.writeCharacteristic(pwmConfigCharacteristic);
			}
		});
		
		mBnPwm1.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int p;
				String ePerString = mEdPwm1Per.getText().toString();
				if(ePerString.length() == 0){
					p = 0;
				}else{
					p = Integer.valueOf(ePerString).intValue();
				}
				Log.i("jinxin","pwm1 per= "+p);
				byte per[] = int2byteU8(p);
				pwm1Characteristic.setValue(per);
				Log.i("jinxin","pwm1 uuid= "+pwm1Characteristic.getUuid());
				pwm1Characteristic.setWriteType(pwm1Characteristic.getWriteType());
            	mBluetoothLeService.writeCharacteristic(pwm1Characteristic);
			}
		});
		
		mBnPwm2.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int p;
				String ePerString = mEdPwm2Per.getText().toString();
				if(ePerString.length() == 0){
					p = 0;
				}else{
					p = Integer.valueOf(ePerString).intValue();
				}
				byte per[] = int2byteU8(p);
				pwm2Characteristic.setValue(per);
				pwm2Characteristic.setWriteType(pwm2Characteristic.getWriteType());
            	mBluetoothLeService.writeCharacteristic(pwm2Characteristic);
			}
		});
		
		mBnPwm3.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int p;
				String ePerString = mEdPwm3Per.getText().toString();
				if(ePerString.length() == 0){
					p = 0;
				}else{
					p = Integer.valueOf(ePerString).intValue();
				}
				byte per[] = int2byteU8(p);
				pwm3Characteristic.setValue(per);
				pwm3Characteristic.setWriteType(pwm3Characteristic.getWriteType());
            	mBluetoothLeService.writeCharacteristic(pwm3Characteristic);
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
        pwmService = null;
        pwmConfigCharacteristic = null;
        pwm1Characteristic = null;
        pwm2Characteristic = null;
        pwm3Characteristic = null;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pwm, menu);
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
	
	private static byte[] int2bytes(int num)
    {
           byte[] b=new byte[2];
           int mask=0xff;
           for(int i=0;i<2;i++){
                b[i]=(byte)(num>>>(8-i*8));
           }
          return b;
    }
	
	private static byte[] int2byteU8(int num)
    {
           byte[] b=new byte[2];
           int mask=0xff;
           for(int i=0;i<2;i++){
                b[1-i]=(byte)(num>>>(8-i*8));
           }
          return b;
    }
	
}
