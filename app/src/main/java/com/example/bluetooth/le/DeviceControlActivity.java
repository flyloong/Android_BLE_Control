/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluetooth.le;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
@SuppressLint("NewApi")
public class DeviceControlActivity extends Activity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private View mfrangment_rocker;
    private View mfrangment_seekbar;
    private TextView mConnectionState;
    private TextView mDataSend;
    private TextView mDataDisplay;
    private ScrollView bScroll = null;
    private String mDeviceName;
    private String mDeviceAddress;
    private Button mBnSend;
    private Button mBnClear;
    private ToggleButton togglebutton;
    private ToggleButton togglebutton1;
    private ToggleButton togglebutton2;
    private Button mBnNotify;
    private SeekBar seekBar1;
    private SeekBar seekBar2;
    private SeekBar seekBar3;
    private SeekBar seekBar4;
    private boolean bFlagNotifyOn = true;
    private boolean firstSendFlag = true;
    private boolean Car_cmd2_New_Flag=false;
    private RockerView rockerView1;
    private RockerView rockerView2;
    private MenuItem RockerBtn;
    //private int MYSendValue[]=new int[5];
    private int MYSendValue[]={0,50,50,50,50};


    boolean runFlag = false;
    boolean SendOver = true;
    String sendBufferString;
    int sendCntStart = 0;
    int sendCntEnd = 0;
    
    private Button mBnADC;
    private Button mBnPWM;
    private Button mBnGPIO;
    private Button mBnTIO;
    byte[] Car_cmd2 = new byte[1];
    //byte[]Msg_EOF= new byte[] {0xF0};
    private Button.OnClickListener Bthandler;
    private List<BluetoothGattService> mGattServices;
    
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothGattCharacteristic MYcharacteristic;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";


    public final static UUID UUID_ADC       = UUID.fromString(SampleGattAttributes.ADC_SERVICE);
    public final static UUID UUID_PWM       = UUID.fromString(SampleGattAttributes.PWM_SERVICE);
    public final static UUID UUID_GPIO       = UUID.fromString(SampleGattAttributes.GPIO_SERVICE);
    public final static UUID UUID_TIO       = UUID.fromString(SampleGattAttributes.TIO_SERVICE);


    class MYThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);//貌似一秒10个数据是极限了
                    //System.out.println("send...");
                    byte[] tx = new byte[8];
                    tx[0]='s';

                    tx[1]='t';
                    tx[2]=(byte)MYSendValue[0];
                    tx[3]=(byte)MYSendValue[1];
                    tx[4]=(byte)MYSendValue[2];
                    tx[5]=(byte)MYSendValue[3];
                    tx[6]=(byte)MYSendValue[4];
                    tx[7]=Car_cmd2[0];
                    /*
                    int[] temp={MYSendValue[1],MYSendValue[2],MYSendValue[3],MYSendValue[4]};
                    for(i=0;i<4;i++){//将整数型数据变成字符型
                        if(temp[i]<0)
                            temp[i]=65536+temp[i];
                        tx[2*i+2]=(byte)(temp[i]%256);
                        tx[2*i+3]=(byte)(temp[i]/256);
                    }
                    for(i = 0; i < 9; i++) tx[10] += tx[i];
                    */
                    //SendData(String.valueOf(MYSendValue[0]));

                    SendDataByte(tx);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("thread error...");
                }
            }
        }
    }
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.i("ble", "onServiceConnected");
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            Log.i("ble", "onServiceDisconnected--4");
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Log.i("ble", "connected--2");
                //updateConnectionState(R.string.connected);
                //getActionBar().setTitle(mDeviceName + " " + mDeviceAddress + " " + "connected");
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                getActionBar().setTitle("Disconnect");
                //updateConnectionState(R.string.disconnected);
                //getActionBar().setTitle(mDeviceName + " " + mDeviceAddress + " " + "disconnected");
                invalidateOptionsMenu();
                Log.i("ble","disconnected--2");
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            	mGattServices = mBluetoothLeService.getSupportedGattServices();
                StartReceiver(bFlagNotifyOn);
                new Thread(new MYThread()).start();
            	displayGattServiceSOC(mBluetoothLeService.getSupportedGattServices());
                byte[] tx = new byte[8];
                tx[0]='s';
                tx[1]='t';
                tx[2]=(byte)MYSendValue[0];
                tx[3]=(byte)MYSendValue[1];
                tx[4]=(byte)MYSendValue[2];
                tx[5]=(byte)MYSendValue[3];
                tx[6]=(byte)MYSendValue[4];
                tx[7]='c';
                SendDataByte(tx);
                Log.i("ble","displayserice--2");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                Log.i("Gatt","display data--2");
            }
            else if (BluetoothLeService.ACTION_DATA_RSSI.equals(action)) {
                //mDataDisplay.setText("RSSI"+intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                getActionBar().setTitle("RSSI"+intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                Log.i("Gatt", "display RSSI--2");
            }else if(BluetoothLeService.ACTION_DATA_WRITE.equals(action)){
            	Log.i("jinxin","write callback data--2");
                SendOver = true;
                if(Car_cmd2[0]!='t'&&!Car_cmd2_New_Flag){
                    Car_cmd2_New_Flag=true;
                }
                else if(Car_cmd2_New_Flag){
                    Car_cmd2[0]='t';
                    Car_cmd2_New_Flag=false;
                }
                byte[] tx = new byte[8];
                tx[0]='s';
                tx[1]='t';
                tx[2]=(byte)MYSendValue[0];
                tx[3]=(byte)MYSendValue[1];
                tx[4]=(byte)MYSendValue[2];
                tx[5]=(byte)MYSendValue[3];
                tx[6]=(byte)MYSendValue[4];
                tx[7]=Car_cmd2[0];
              //  tx[8]=(byte)240;
              //  SendDataByte(tx);
            }
            
        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @SuppressLint("NewApi")
				@Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                	Log.i("ble","OnChildClick=G="+groupPosition+"child="+childPosition);
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        final UUID uuid = characteristic.getUuid();
                        //Log.i("ble","charaprop===== "+charaProp+"  ,uuid="+uuid);
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
 
                            // If there is an active  notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            Log.i("Gatt", "read characteristic : "+uuid);
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                            Log.i("Gatt", "enable characteristic nofity : "+uuid);
                        }
                        
                        if((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                        	byte[] t = {0x02,0x00};
                        	characteristic.setValue(t);
                            characteristic.setWriteType(characteristic.getWriteType());
                        	Log.i("Gatt", "write--- characteristic value ");
                        	mBluetoothLeService.writeCharacteristic(characteristic);
                        }
                        
                        return true;
                    }
                    return false;
                }
    };

    private void clearUI() {
       
        mDataDisplay.setText("");
    }

    
    Runnable sendHandler = new Runnable() {  
    	@Override  
    	public void run() {       
    		
			while(runFlag){
    			
    			try {
    				String sendTmpString;
    				if(SendOver){
    					SendOver = false;
    					if(sendCntEnd-sendCntStart > 20){
    						sendTmpString = sendBufferString.substring(sendCntStart, 
									sendCntStart + 20);
                            sendCntStart += 20;
                        } else {
                            sendTmpString = sendBufferString.substring(sendCntStart,
    								sendCntEnd);
    						
    						sendCntStart = 0;
    						sendCntEnd = 0;
    						runFlag = false;
						}
    					
    					SendData(sendTmpString);
    				}
    				
    				Thread.sleep(0);//50
    			} catch (InterruptedException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}
    		
    	}  
    };  
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        //((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);

       // mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataSend = (EditText) findViewById(R.id.SendText);
        mDataDisplay =(TextView)findViewById(R.id.textView1);
        mDataDisplay.setMovementMethod(new ScrollingMovementMethod());
        mDataDisplay.setText("");
        rockerView1 = (RockerView)findViewById(R.id.rockerView1);
        rockerView2 = (RockerView)findViewById(R.id.rockerView2);
        rockerView1.setRockerChangeListener(new RockerView.RockerChangeListener() {
            @Override
            public void report(float x, float y) {
                // TODO Auto-generated method stub
                // doLog(x + "/" + y);
                // setLayout(rockerView2, (int)x, (int)y);
                //  setLayout(rockerView2, (int)x, (int)y);
                MYSendValue[1]=(int)(y/rockerView1.getR()*50+50);
                MYSendValue[2]=(int)(x/rockerView1.getR()*30+50);

            }
        });
        rockerView2.setRockerChangeListener(new RockerView.RockerChangeListener() {
            @Override
            public void report(float x, float y) {
                // TODO Auto-generated method stub
                // doLog(x + "/" + y);
                // setLayout(rockerView2, (int)x, (int)y);
                //  setLayout(rockerView2, (int)x, (int)y);
                MYSendValue[4]=(int)(y/rockerView1.getR()*30+50);
                MYSendValue[3]=(int)(-x/rockerView1.getR()*30+50);

            }
        });


        togglebutton = (ToggleButton) findViewById(R.id.bAuto);
        togglebutton1 = (ToggleButton) findViewById(R.id.bt_Warn);
        togglebutton2 = (ToggleButton) findViewById(R.id.bt_FoMe);
        togglebutton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (togglebutton.isChecked()) {//自动模式
                    MYSendValue[0] = 1;
                    seekBar3.setProgress(50);
                    seekBar4.setProgress(50);
                } else {//手动模式
                    MYSendValue[0] = 0;
                }
            }
        });
        togglebutton1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (togglebutton1.isChecked()) {//预警模式
                    MYSendValue[0] = 2;
                    seekBar3.setProgress(50);
                    seekBar4.setProgress(50);
                } else {//
                    MYSendValue[0] = 0;
                }
            }
        });
        togglebutton2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (togglebutton2.isChecked()) {//预警模式
                    MYSendValue[0] = 4;
                    seekBar3.setProgress(50);
                    seekBar4.setProgress(50);
                } else {//
                    MYSendValue[0] = 0;
                }
            }
        });
            seekBar1=(SeekBar)findViewById(R.id.seekBar1);
        seekBar2=(SeekBar)findViewById(R.id.seekBar2);
        seekBar3=(SeekBar)findViewById(R.id.seekBar3);
        seekBar4=(SeekBar)findViewById(R.id.seekBar4);
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
             //      mDataDisplay.setText(String.valueOf(progress));
                MYSendValue[1]=progress;
            }
        });
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekBar2.setProgress(50);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //    mDataDisplay.setText(String.valueOf(progress));
                MYSendValue[2]=progress;
            }
        });
        seekBar3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
             //   mDataDisplay.setText(String.valueOf(progress));
                MYSendValue[3]=progress;
            }
        });
        seekBar4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
             //   mDataDisplay.setText(String.valueOf(progress));
                MYSendValue[4]=progress;
            }
        });


        Bthandler = new Button.OnClickListener() {
    		
    		@Override
    		public void onClick(View v) {
    			// TODO Auto-generated method stub
    			switch(v.getId()){
    			case R.id.bSend:
                    /*
                    byte[] tx = new byte[9];
                    tx[0]='s';
                    tx[1]='t';
                    tx[2]=(byte)MYSendValue[0];
                    tx[3]=(byte)MYSendValue[1];
                    tx[4]=(byte)MYSendValue[2];
                    tx[5]=(byte)MYSendValue[3];
                    tx[6]=(byte)MYSendValue[4];
                    tx[7]=Car_cmd2[0];
                    tx[8]=(byte)240;
                      SendDataByte(tx);
                      */

    				String sendString;
                    sendString = mDataSend.getText().toString();
    				sendCntStart = 0;
    				sendCntEnd = sendString.length();
    				if(sendString.length() > 20){
    					runFlag = true;
    					sendBufferString = sendString;
    					new Thread(sendHandler).start();
    				}else {
						SendData(sendString);


					}

    				//Log.i("jinxin", "button send press! text ="+sendString);
    				break;
    			case R.id.bClear:
    				Log.i("jinxin", "button clear press!");

                    mDataDisplay.setText("");
    				break;
                    case R.id.L_Reset:
                        seekBar1.setProgress(50);
                        break;
                    case R.id.B_1:
                        Car_cmd2[0]='x';
                        break;
                    case R.id.B_2:
                        Car_cmd2[0]='l';
                        break;
                    case R.id.B_3:
                        Car_cmd2[0]='r';
                        break;
                    case R.id.B_4:
                        Car_cmd2[0]='w';
                        break;
                    case R.id.B_5:
                        Car_cmd2[0]='v';
                        break;
                    case R.id.B_6:
                        Car_cmd2[0]='a';
                        break;
                    case R.id.B_7:
                        Car_cmd2[0]='i';
                        break;
                    case R.id.B_Hume:
                        Car_cmd2[0]='H';
                        break;
                    case R.id.B_Pres:
                        Car_cmd2[0]='P';
                        break;
                    case R.id.B_Temp:
                        Car_cmd2[0]='T';
                        break;
                    case R.id.B_Dist:
                        Car_cmd2[0]='D';
                        break;
                    case R.id.B_Dist2:
                        Car_cmd2[0]='U';
                        break;
                    case R.id.B_Yse:
                        Car_cmd2[0]='Y';
                        break;
                    case R.id.B_N0:
                        Car_cmd2[0]='N';
                        break;
                    case R.id.B_Laug:
                        Car_cmd2[0]='L';
                        break;
                    case R.id.B_Then:
                        Car_cmd2[0]='R';
                        break;
                    case R.id.B_OK:
                        Car_cmd2[0]='O';
                        break;
                    case R.id.B_Shake:
                        Car_cmd2[0]='S';
                        break;
                    case R.id.B_Read:
                        Car_cmd2[0]='C';
                        break;
                    case R.id.B_Rocker:
                        if(mfrangment_rocker.getVisibility()==View.GONE) {
                            mfrangment_rocker.setVisibility(View.VISIBLE);
                            mfrangment_seekbar.setVisibility(View.GONE);

                        }
                        else {
                            mfrangment_rocker.setVisibility(View.GONE);
                            mfrangment_seekbar.setVisibility(View.VISIBLE);
                        }
                        break;
    			}
    		}
    	};
        //mBnSend = (Button)findViewById(R.id.bSend);
        //mBnClear = (Button)findViewById(R.id.bClear);
        findViewById(R.id.bSend).setOnClickListener(Bthandler);
        findViewById(R.id.bClear).setOnClickListener(Bthandler);
        findViewById(R.id.L_Reset).setOnClickListener(Bthandler);
        findViewById(R.id.B_1).setOnClickListener(Bthandler);
        findViewById(R.id.B_2).setOnClickListener(Bthandler);
        findViewById(R.id.B_3).setOnClickListener(Bthandler);
        findViewById(R.id.B_4).setOnClickListener(Bthandler);
        findViewById(R.id.B_5).setOnClickListener(Bthandler);
        findViewById(R.id.B_6).setOnClickListener(Bthandler);
        findViewById(R.id.B_7).setOnClickListener(Bthandler);
        findViewById(R.id.B_Hume).setOnClickListener(Bthandler);
        findViewById(R.id.B_Pres).setOnClickListener(Bthandler);
        findViewById(R.id.B_Temp).setOnClickListener(Bthandler);
        findViewById(R.id.B_Dist).setOnClickListener(Bthandler);
        findViewById(R.id.B_Dist2).setOnClickListener(Bthandler);
        findViewById(R.id.B_Yse).setOnClickListener(Bthandler);
        findViewById(R.id.B_N0).setOnClickListener(Bthandler);
        findViewById(R.id.B_Laug).setOnClickListener(Bthandler);
        findViewById(R.id.B_Then).setOnClickListener(Bthandler);
        findViewById(R.id.B_OK).setOnClickListener(Bthandler);
        findViewById(R.id.B_Shake).setOnClickListener(Bthandler);
        findViewById(R.id.B_Read).setOnClickListener(Bthandler);
        findViewById(R.id.B_Rocker).setOnClickListener(Bthandler);
        mfrangment_rocker=findViewById(R.id.fragment_rocker);
        mfrangment_seekbar=findViewById(R.id.fragment_seekbar);
        /////////////soc activity


        

       // getActionBar().setTitle(mDeviceName);
        //getActionBar().setTitle(mDeviceName+" "+mDeviceAddress);
        getActionBar().setDisplayHomeAsUpEnabled(true);		///enable return the last activity
        getActionBar().setTitle("");
        initView();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);

        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    private void StartReceiver(boolean on){
    	BluetoothGattService cService;
		List<BluetoothGattCharacteristic> cGattCharacteristics;
		cService = mGattServices.get(1);
		cGattCharacteristics = cService.getCharacteristics();
		

        BluetoothGattCharacteristic characteristic =
        		cGattCharacteristics.get(1);
        final int charaProp = characteristic.getProperties();
        final UUID uuid = characteristic.getUuid();
        Log.i("jinxin","receiver uuid = "+uuid);
        if((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
        	//String t = "abcdefghijklmnop";
        	//char[] s = t.toCharArray();
        	if(on){
	        	mBluetoothLeService.setCharacteristicNotification(
	                    characteristic, true);
        	}
        	else {
        		mBluetoothLeService.setCharacteristicNotification(
                        characteristic, false);
			}
        }
    }
    
    private void SendData(String sendString){

	//	Log.i("jinxin", "send = "+sendString+"lenth = "+sendString.length());
		if(sendString.length()< 1){
			Toast.makeText(this, "can't send nothing!", Toast.LENGTH_SHORT).show();
			return;
		}
if(firstSendFlag) {
    firstSendFlag=false;
    BluetoothGattService cService;
    List<BluetoothGattCharacteristic> cGattCharacteristics;
    cService = mGattServices.get(1);
    cGattCharacteristics = cService.getCharacteristics();


    MYcharacteristic = cGattCharacteristics.get(0);
    final int charaProp = MYcharacteristic.getProperties();
    final UUID uuid = MYcharacteristic.getUuid();
    Log.i("jinxin","receiver uuid = "+uuid);
}
		


            //String t = "abcdefghijklmnop";
        	//char[] s = t.toCharArray();
            MYcharacteristic.setValue(sendString);
            MYcharacteristic.setWriteType(MYcharacteristic.getWriteType());
            Log.i("Gatt", "write--- characteristic value ");
        	//mBluetoothLeService.writeCharacteristic(characteristic);
            mBluetoothLeService.writeCharacteristic(MYcharacteristic);

        
    }

    private void SendDataByte( byte[]  sendString){

        //	Log.i("jinxin", "send = "+sendString+"lenth = "+sendString.length());
      //  if(sendString.length()< 1){
      //      Toast.makeText(this, "can't send nothing!", Toast.LENGTH_SHORT).show();
      //      return;
      //  }
        if(firstSendFlag) {
            firstSendFlag=false;
            BluetoothGattService cService;
            List<BluetoothGattCharacteristic> cGattCharacteristics;
            cService = mGattServices.get(1);
            cGattCharacteristics = cService.getCharacteristics();


            MYcharacteristic = cGattCharacteristics.get(0);
            final int charaProp = MYcharacteristic.getProperties();
            final UUID uuid = MYcharacteristic.getUuid();
            Log.i("jinxin","receiver uuid = "+uuid);
        }



        //String t = "abcdefghijklmnop";
        //char[] s = t.toCharArray();
        MYcharacteristic.setValue(sendString);
           MYcharacteristic.setWriteType(MYcharacteristic.getWriteType());
        Log.i("Gatt", "write--- characteristic value ");
        //mBluetoothLeService.writeCharacteristic(characteristic);
        mBluetoothLeService.writeCharacteristic(MYcharacteristic);


    }


    @Override
    protected void onResume() {
      //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.i(TAG, "Connect request result=" + result);
        }

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
    }

   

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        RockerBtn= menu.findItem(R.id.menu_Rocker);
        if(mfrangment_rocker.getVisibility()==View.GONE) {
            RockerBtn.setTitle("Rocker");
        }
        else {
            RockerBtn.setTitle("SeekBar");
        }
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);

        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case R.id.menu_Rocker:
                if(mfrangment_rocker.getVisibility()==View.GONE) {
                    mfrangment_rocker.setVisibility(View.VISIBLE);
                    mfrangment_seekbar.setVisibility(View.GONE);
                    RockerBtn.setTitle("SeekBar");
                }
                else {
                    mfrangment_rocker.setVisibility(View.GONE);
                    mfrangment_seekbar.setVisibility(View.VISIBLE);
                    RockerBtn.setTitle("Rocker");
                }
                return true;
            case R.id.menu_ID:
                Car_cmd2[0]='i';
                return true;
            case R.id.menu_Tem:
                Car_cmd2[0]='T';
                return true;
            case android.R.id.home:
            	Log.i("ble", "home select");
                onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
             //   mConnectionState.setText(resourceId);
              //  getActionBar().setTitle(mDeviceName + " " + mDeviceAddress +" "+ resourceId);
            }
        });
    }

    int i = 0;
    private void displayData(String data) {
        if (data != null) {
            mDataDisplay.append(data);
        }
    }

    /////SOC SERVICES DISPLAY
    private void displayGattServiceSOC(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        //String uuid = null;

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
     
            //uuid = gattService.getUuid().toString();

        }
    }
    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        //mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_WRITE);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_RSSI);
        return intentFilter;
    }
    private void initView(){
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        //????????????б??????????
        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.action_list,
                android.R.layout.simple_spinner_dropdown_item);

        actionBar.setListNavigationCallbacks(mSpinnerAdapter, new ActionBar.OnNavigationListener() {
            @Override
            public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                switch (itemPosition) {
                    case 0:
                       //手动模式
                            MYSendValue[0] = 0;

                    return true;
                    case 1:
                        //自动模式
                        MYSendValue[0] = 1;
                        seekBar3.setProgress(50);
                        seekBar4.setProgress(50);
                        return true;
                }
                return true;
            }
        });

    }
}
