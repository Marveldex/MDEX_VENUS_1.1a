
/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mdex.venusAlpha01;


/*
*   TODO
*   send button
*   re-connection 안정화...
*   protocol 분석
*
*
* test git
*
* */

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.os.SystemClock.elapsedRealtime;

/**
 *
 * @mainpage SCMS( Sitting Cushion Management System) - Marveldex
 * @brief 스마트 방석을 이용한 Application :
 * @details 방석의 각 압력 센서의 값을 VENUS 보드를 통해 전달 받아 데이터를 처리하여 Application을 통해 여러 형태의 값으로 보여주는 기능을 하는 Source
 *
 * @brief 2번째 설명 :
 * @details 방석의 각 압력 센서의 값을 VENUS 보드를 통해 전달 받아 데이터를 처리하여 Application을 통해 여러 형태의 값으로 보여주는 기능을 하는 Source
 *
 */

/**
 *
 * @file MainActivity.java
 * @brief 메인 기능을 수행하는 파일
 *
 */

/**
 *
 * @brief this is main function for run this app
 * @details show values of sence and save to CSV File
 * @author Marveldex
 * @date 2017-03-17
 * @version 0.0.1
 * @li list1
 * @li list2
 *
 */

public class   MainActivity extends Activity implements RadioGroup.OnCheckedChangeListener {
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int UART_PROFILE_READY = 10;
    public static final String TAG = "nRFUART";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int STATE_OFF = 10;

    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private int mState = UART_PROFILE_DISCONNECTED;
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;
    private ListView messageListView;
    private ArrayAdapter<String> listAdapter;
    private Button btnConnectDisconnect,btnSend;

    private boolean mSave_Flag = false;
    private int mChmode = 0;

    Button mSave_Start;
    Button mSave_Stop;
    Button mLeft_Btn;
    Button mRight_Btn;
    SharedPreferences pref;

    private String mPosition_Csv = null;
    Map<String, Object> hash_map = null;
    ArrayList<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
    TextView mPointTxt;

    ImageButton mMdexHome;
    int cell_index = 0;

    long now = System.currentTimeMillis();
    Date date = new Date(now);
    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMdd_HHmmss");
    String formatDate = sdfNow.format(date);

    private int toast_flag = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        pref = getSharedPreferences("MacAddr", Activity.MODE_PRIVATE);

        messageListView = (ListView) findViewById(R.id.listMessage);
        listAdapter = new ArrayAdapter<String>(this, R.layout.message_detail);
        messageListView.setAdapter(listAdapter);
        messageListView.setDivider(null);
        btnConnectDisconnect=(Button) findViewById(R.id.btn_select);
        btnSend=(Button) findViewById(R.id.sendButton);
        edtMessage = (EditText) findViewById(R.id.sendText);
        mSave_Start = (Button)findViewById(R.id.Save_Start);
        mSave_Stop = (Button)findViewById(R.id.Save_Stop);
        mPointTxt = (TextView)findViewById(R.id.point);


        mMdexHome = (ImageButton)findViewById(R.id.MdexHome);

        mLeft_Btn = (Button)findViewById(R.id.Left_On_Off);
        mRight_Btn = (Button)findViewById(R.id.Right_On_Off);

        UI_onCreate();

        service_init();
       
        // Handle Disconnect & Connect button
        /**
         *
         * @brief Disconnect Ble with Venus Board
         * @details if you click this Button, Ble Connection is Stopped
         * @param
         * @return
         * @throws
         */
        btnConnectDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mBtAdapter.isEnabled()) {
                    Log.i(TAG, "onClick - BT not enabled yet");
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                	if (btnConnectDisconnect.getText().equals("Connect")){
                		
                		//Connect button pressed, open DeviceListActivity class, with popup windows that scan for devices
                		
            			Intent newIntent = new Intent(MainActivity.this, com.mdex.venusAlpha01.DeviceListActivity.class);
            			startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
        			} else {
        				//Disconnect button pressed
        				if (mDevice!=null)
        				{
        					mService.disconnect();
        					
        				}
        			}
                }

                toast_flag = 0;
            }
        });

        mMdexHome.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.mdex.co.kr"));
                startActivity(browserIntent);
            }
        });

        // Handle Send button
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	EditText editText = (EditText) findViewById(R.id.sendText);
            	String message = editText.getText().toString();
            	byte[] value;
				try {
					//send data to service
					value = message.getBytes("UTF-8");
					mService.writeRXCharacteristic(value);
					//Update the log with time stamp
					String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
					listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
               	 	messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
               	 	edtMessage.setText("");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });
     
        // Set initial UI state
        mLeft_Btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                String message;

                if(mLeft_Btn.getText().toString().equals("Left On")){

                    mLeft_Btn.setText("Left Off");

                    message = "turn on left";
                    //message = "turn left on";

                }else{
                    mLeft_Btn.setText("Left On");

                    message = "turn off left";
                    //message = "turn left off";
                }

                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });

        mRight_Btn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                String message;

                if(mRight_Btn.getText().toString().equals("Right On")){

                    mRight_Btn.setText("Right Off");

                    message = "turn on right";
                    //message = "turn right on";

                }else{
                    mRight_Btn.setText("Right On");

                    message = "turn off right";
                    //message = "turn right off";
                }

                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    mService.writeRXCharacteristic(value);
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    listAdapter.add("["+currentDateTimeString+"] TX: "+ message);
                    messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                    edtMessage.setText("");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     *
     * @brief UART Service Connected/ DIsConnected
     * @details Call the Service from UartService.java and check state of connection
     * @param ComponentName , IBinder
     * @return
     * @throws
     */

    //UART service connected/disconnected
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            mService = ((com.mdex.venusAlpha01.UartService.LocalBinder) rawBinder).getService();
            Log.d(TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            if(mService != null) {
                //mService.disconnect(mDevice);
                mService.disconnect();
            }
            //mService = null;
        }
    };

    /**
     *
     * @brief To Save Sitting Data, Make CSV File
     * @details IF you Click the Button, Check the Button String. if the String is'Start Save' then this App Start saving Date and when you click the Button again, Make CSV File
     * @param
     * @return
     * @throws
     */

    public void onClick_Save(View v){
        switch (v.getId()) {
//            case R.id.Save_Start:
            case R.id.Save_Start:

                if(mSave_Flag==false){
                    mSave_Flag = true;
                    mSave_Start.setEnabled(true);
                    mSave_Start.setText("Save Stop");
                }else{
                    mSave_Flag = false;
                    //파일 생성
                    createToFilecsv();
                    mSave_Start.setEnabled(true);
                    mSave_Start.setText("Save Start");
                }
                break;

            case R.id.Save_Stop:

                mSave_Flag = false;
                //파일 생성
                createToFilecsv();
                mSave_Start.setEnabled(true);
                mSave_Stop.setEnabled(false);
                break;
        }

    }

    /**
     *
     * @brief Create CSV File
     * @details When you Click 'Save Stop' Button, this Function count the now date and create CSV File. the File name is TODAY.csv
     * @param
     * @return
     * @throws
     */

    private void createToFilecsv(){

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String formatDate = sdfNow.format(date);

        try{
            File file = new File("/mnt/sdcard/Download/" + formatDate + ".csv");
            if(!file.exists()){
                file = new File("/mnt/sdcard/Download/" + formatDate + ".csv");
                file.createNewFile();
            }

            PrintWriter csvWriter;
            csvWriter = new  PrintWriter(new FileWriter(file,true));

            csvWriter.print(mPosition_Csv);
            //csvWriter.print("\r\n");
            csvWriter.close();


            mPointTxt.setText("파일의 저장 경로  : /mnt/sdcard/Download/" + formatDate + ".csv ");

            Toast toast = Toast.makeText(getApplicationContext(), "Saved successfully", Toast.LENGTH_LONG);
            toast.show();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     *
     * @brief To Handler
     * @details  To Create Handler
     * @param
     * @return
     * @throws
     */
    private Handler mHandler = new Handler() {
        @Override
        
        //Handler events that received from UART service 
        public void handleMessage(Message msg) {
            Log.i(TAG, "Uart service handleMessage message= " + msg);
        }
    };

    /**
     *
     * @brief Receive Broadcast and Check and Action each Function
     * @details  It receives data from the UarService.java file as a broadcast and performs its function through its value. Manage Bluetooth and device connection status.
     * @param
     * @return
     * @throws
     */
    private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            final Intent mIntent = intent;
           //*********************//
            if (action.equals(com.mdex.venusAlpha01.UartService.ACTION_GATT_CONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                         String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                         Log.d(TAG, "UART_CONNECT_MSG");
                         btnConnectDisconnect.setText("Disconnect");
                         edtMessage.setEnabled(true);
                         btnSend.setEnabled(true);
                         ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connected");
                         listAdapter.add("["+currentDateTimeString+"] Connected to: "+ mDevice.getName());
                         messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);
                         //연결 완료  - 맥어드레스 저장
                         //업데이트
                         SharedPreferences.Editor editor = pref.edit();
                         editor.putString("MacAddr",mDevice.getAddress());
                         editor.commit();
                         mState = UART_PROFILE_CONNECTED;
                     }
            	 });
            }
           
          //*********************//
            if (action.equals(com.mdex.venusAlpha01.UartService.ACTION_GATT_DISCONNECTED)) {
            	 runOnUiThread(new Runnable() {
                     public void run() {
                    	 	 String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             Log.d(TAG, "UART_DISCONNECT_MSG");
                             btnConnectDisconnect.setText("Connect");
                             edtMessage.setEnabled(false);
                             btnSend.setEnabled(false);
                             ((TextView) findViewById(R.id.deviceName)).setText("Not Connected");
                             listAdapter.add("["+currentDateTimeString+"] Disconnected to: "+ mDevice.getName());
                             mState = UART_PROFILE_DISCONNECTED;
                             mService.close();
                            //setUiState();
                         
                     }
                 });
            }


          //*********************//
            if (action.equals(com.mdex.venusAlpha01.UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
             	 mService.enableTXNotification();
                Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
            }
          //*********************//
            if (action.equals(com.mdex.venusAlpha01.UartService.ACTION_DATA_AVAILABLE)) {
                final byte[] packetVenus2Phone = intent.getByteArrayExtra(com.mdex.venusAlpha01.UartService.EXTRA_DATA);

                runOnUiThread(new Runnable() {
                     public void run() {
                         try {
                         	String text = new String(packetVenus2Phone, "UTF-8");
                         	String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                             listAdapter.add("["+currentDateTimeString+"] RX: "+text);
                             messageListView.smoothScrollToPosition(listAdapter.getCount() - 1);

                         } catch (Exception e) {
                             Log.e(TAG, e.toString());
                         }

                         //-------------------<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
                         if(packetVenus2Phone.length < 20){
                             Log.e(TAG, packetVenus2Phone.toString());
                             return;
                         }

                         m_PacketParser.ParseOnePacket(packetVenus2Phone);
                         UI_showParsedData();
                         UI_updateComputedData();

                     }

                 });
             }
           //*********************//
            if (action.equals(com.mdex.venusAlpha01.UartService.DEVICE_DOES_NOT_SUPPORT_UART)){
            	showMessage("Device doesn't support UART. Disconnecting");
            	mService.disconnect();
            }
        }
    };

    /**
     *
     * @brief Service Init
     * @details
     * @param
     * @return
     * @throws
     */
    private void service_init() {
        Intent bindIntent = new Intent(this, com.mdex.venusAlpha01.UartService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
  
        LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
    }

    /**
     *
     * @brief intenrFilter를 생성하고 값을 설정한는 함수
     * @details intentFilter를 생성하고 UarService.java에서 각 value를 불러와 Setting 한다.
     * @param
     * @return intentFilter
     * @throws
     */
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(com.mdex.venusAlpha01.UartService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(com.mdex.venusAlpha01.UartService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(com.mdex.venusAlpha01.UartService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(com.mdex.venusAlpha01.UartService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(com.mdex.venusAlpha01.UartService.DEVICE_DOES_NOT_SUPPORT_UART);
        //      gap messages
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        //intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);

        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        return intentFilter;
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     *
     * @brief 진행중인 서비스 STOP
     * @details
     * @param
     * @return
     * @throws
     */

    @Override
    public void onDestroy() {
    	 super.onDestroy();
        Log.d(TAG, "onDestroy()");
        
        try {
        	LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        } 
        unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!mBtAdapter.isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
 
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     *
     * @brief requestCode에 따른 결과 값 도출
     * @details REQUEST_SELECT_DEVICE -  DeviceListActivity에서 선택된 device의 주소 넘겨 받아 device Connection을 한다.
     * @param   requestCode, int resultCode, Intent data
     * @return
     * @throws
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

        case REQUEST_SELECT_DEVICE:
        	//When the DeviceListActivity return, with the selected device address
            if (resultCode == Activity.RESULT_OK && data != null) {
                String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
               
                Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
                ((TextView) findViewById(R.id.deviceName)).setText(mDevice.getName()+ " - connecting");
                btnConnectDisconnect.setText("Connecting...");

                mService.connect(deviceAddress);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                finish();
            }
            break;
        default:
            Log.e(TAG, "wrong request code");
            break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
    }

    private void showMessage(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    /**
     *
     * @brief
     * @details
     * @param
     * @return
     * @throws
     */
    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            showMessage("nRFUART's running in background.\n             Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.popup_title)
            .setMessage(R.string.popup_message)
            .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
   	                finish();
                }
            })
            .setNegativeButton(R.string.popup_no, null)
            .show();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  USER VARIABLES
    //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private EditText edtMessage;
    private TextView edtBatteryLevel;
    private TextView edtLastPacketTime;
    private TextView tvADC_HexaMain;
    private TextView tvADC_HexaShield;
    private int mBoardSelect = 1; // 0 : Main board, 1 : Shield board

    private TextView [] atvChairCells_Row0 = new TextView[PacketParser.def_CELL_COUNT_ROW0];
    private TextView [] atvChairCells_Row1 = new TextView[PacketParser.def_CELL_COUNT_ROW1];
    private TextView [] atvChairCells_Row2 = new TextView[PacketParser.def_CELL_COUNT_ROW2];

    private TextView edtCOMLog;
    private TextView mtv_BlindState, mtv_BlindStartTime, mtv_BlindElapsedTime;
    PacketParser m_PacketParser = new PacketParser();

    int def_UI_SERO_START_X = 92;
    int def_UI_CELL_WIDTH = 63;
    int def_UI_CELL_HEIGHT = 40;

    Space sp_COM_sero;
    Space sp_COC_left;
    Space sp_COC_right;

    enum POSTURE_tag{
        POSTURE_NO_LOG,
        POSTURE_LEFT_BAD,
        POSTURE_LEFT,
        POSTURE_CENTER,
        POSTURE_RIGHT,
        POSTURE_RIGHT_BAD
    }

    POSTURE_tag m_PostureState;
    long m_PostureOriginTimeMS = elapsedRealtime();

    MediaPlayer m_Player;
    private String m_Mode_Info;
    private TextView m_Mode_TxtView;

    protected void onRadioClicked(View view){
        switch (view.getId()){
            case R.id.RB_MAIN:
                mBoardSelect = 0; // Main board
                break;
            case R.id.RB_SHIELD:
                mBoardSelect = 1; // Shield board
                break;
        }
    }

    /**
     *
     * @brief App의 화면의 컨트롤들 선언
     * @details App의 화면을 구성하는 컨트롤들을 불러와 각 변수에 선언하여 사용할수 있게 셌팅한다.
     * @param
     * @return
     * @throws
     */

    private void UI_onCreate(){

        edtBatteryLevel = (TextView) findViewById(R.id.TV_BatteryLevel);
        edtLastPacketTime = (TextView)findViewById(R.id.TV_CurTime);
        tvADC_HexaMain = (TextView) findViewById(R.id.PacketHexaMain);
        tvADC_HexaShield = (TextView) findViewById(R.id.PacketHexaShield);

        mtv_BlindState = (TextView)findViewById(R.id.TV_BLIND_STATE);
        mtv_BlindStartTime = (TextView)findViewById(R.id.TV_BLIND_TIME_START);
        mtv_BlindElapsedTime = (TextView)findViewById(R.id.TV_BLIND_TIME_ELAPSED);

        cell_index = 0;
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_0);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_1);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_2);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_3);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_4);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_5);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_6);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_7);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_8);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_9);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_10);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_11);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_12);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_13);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_14);
        atvChairCells_Row0[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_0_15);


        cell_index = 0;
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_0);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_1);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_2);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_3);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_4);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_5);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_6);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_7);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_8);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_9);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_10);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_11);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_12);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_13);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_14);
        atvChairCells_Row1[cell_index++] =(TextView)findViewById(R.id.TV_CHAIR_1_15);


        edtBatteryLevel.setText(String.format("Battery level : [%2d%%]", 0));

        edtCOMLog = (TextView)findViewById(R.id.TV_COM_LOG );

        sp_COM_sero = (Space)findViewById(R.id.space_COM);
        sp_COC_left = (Space)findViewById(R.id.space_COC_LEFT);
        sp_COC_right = (Space)findViewById(R.id.space_COC_RIGHT);


        m_PostureState = POSTURE_tag.POSTURE_NO_LOG;

        //creating media player
        m_Player = MediaPlayer.create(this, R.raw.alarm_flipover);
        m_Player.setLooping(false);
        //m_Player.start();

        m_Mode_TxtView = (TextView)findViewById(R.id.MODE_INFO);


        m_ConnectionMonitorTimer.schedule(new com.mdex.venusAlpha01.MainActivity.TimerTask_ConnectionMonitor(), 500,TIMER_PERIOD_MONITOR);
    }

    private Timer m_ConnectionMonitorTimer  = new Timer();
    private final int TIMER_PERIOD_MONITOR  = 1000; // 1000 : 1 sec
    int m_Blind_ElapsedTime = 0;
    int m_Blind_ElapsedTime_Last= 0;
    private static final int THRESHOLD_BLIND_SEC = 3;  // THRESHOLD_BLIND_SEC : threshold to decide blind state
    private static final int CONN_STATE_DISCONNECT = 0;
    private static final int CONN_STATE_CONNECT_OK = 1;
    private static final int CONN_STATE_CONNECT_BLIND = 2;
    int m_AutoConn_State = CONN_STATE_DISCONNECT;

    private class TimerTask_ConnectionMonitor extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mState == UART_PROFILE_DISCONNECTED) {
                        m_AutoConn_State = CONN_STATE_DISCONNECT;
                        if(com.mdex.venusAlpha01.UartService.getIsDisconnIntentional() == false) {
                            //  This disconnect message is...
                            //  not occurred by device disconnect request. (Device disconnect doesn't make disconnect message in phone)
                            //  not occurred by phone disconnect request. (Phone disconnect request make UartService.m_is_Disconnect_Intentional flag be true)
                            //  occurred only when this application is just launched. So reconnection needs device mac info to connect.
                        }
                        return;
                    }

                    //  case (m_State == UART_PROFILE_CONNECTED)
                    m_Blind_ElapsedTime++;
                    if(THRESHOLD_BLIND_SEC <= m_Blind_ElapsedTime) {
                        m_Blind_ElapsedTime_Last = m_Blind_ElapsedTime;
                        //  first time
                        if(m_Blind_ElapsedTime_Last == THRESHOLD_BLIND_SEC) {
                            m_AutoConn_State = CONN_STATE_CONNECT_BLIND;

                            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
                            Calendar cal = Calendar.getInstance();
                            String time_str = dateFormat.format(cal.getTime());
                            mtv_BlindStartTime.setText("Last blind : " + time_str);

                            mtv_BlindState.setText("State : Blind");
                        }
                        mtv_BlindElapsedTime.setText(m_Blind_ElapsedTime_Last + " sec");
                    }
                    else {
                        return;
                    }
                }
            });
        }
    }
    /**
     *
     * @brief 변수에 value setting
     * @details 각 변수에 전달 받은 압력값을 Setting 하고 CSV FIle저장 유무에 따라 데이터를 따르 저장한다.
     * @param
     * @return
     * @throws
     */
    private void UI_showParsedData() {
        // last time packet received
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            String time_str = dateFormat.format(cal.getTime());
            edtLastPacketTime.setText("Last packet : " + time_str);
        }

        if (m_PacketParser.isPacketCompleted() == false)
            return;


        // last time packet received
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            String time_str = dateFormat.format(cal.getTime());
            edtLastPacketTime.setText(getString(R.string.fmt_time_last_packet) + time_str);

            m_AutoConn_State = CONN_STATE_CONNECT_OK;
            m_Blind_ElapsedTime = 0;
            mtv_BlindState.setText("State : Receiving");
        }

        //  UI - battery level
        edtBatteryLevel.setText(String.format("Battery level : [%2d%%]", PacketParser.getBatteryLevel()));

        //Mode Check
        m_Mode_Info = PacketParser.Mode_Info;
        Toast toast = Toast.makeText(getApplicationContext(), "딥스위치 B를 OFF 하세요", Toast.LENGTH_LONG);

        if (m_Mode_Info == "M" || m_Mode_Info == "S") {

            m_Mode_TxtView.setText("");
            toast.cancel();

        } else if (m_Mode_Info == "L" || m_Mode_Info == "R") {

            m_Mode_TxtView.setText("본 어플 실행시 보드의 딥스위치B를 OFF하세요");
            //toast 보여주기

            if (toast_flag == 0) {
                toast.show();
                toast_flag = toast_flag + 1;
            }
        }

        //  UI - cell data and color
        {
            int sensor_value = 0;
            int cell_index = 0;
            int row_index = 0;

            hash_map = new HashMap<String, Object>();

            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String formatDate = sdfNow.format(date);


            for (cell_index = 0; cell_index < PacketParser.def_CELL_COUNT_ROW0; cell_index++) {
                sensor_value = m_PacketParser.getSensorDataByCoord(row_index, cell_index);
                atvChairCells_Row0[cell_index].setText(String.format("%d", sensor_value));
                atvChairCells_Row0[cell_index].setBackgroundColor(0x00FF0000 | (sensor_value << 24));


                // save as csv file / row 0...
                if (mSave_Flag == true) {

                    String nPoint = atvChairCells_Row0[cell_index].getText().toString();

                    if (mPosition_Csv == null) {
                        mPosition_Csv = formatDate + "," + nPoint + ",";

                    } else {

                        if (cell_index == 0) {
                            mPosition_Csv = mPosition_Csv + formatDate + "," + nPoint + ",";

                        } else {
                            mPosition_Csv = mPosition_Csv + nPoint + ",";
                        }
                    }
                }

            }

            row_index = 1;
            for (cell_index = 0; cell_index < PacketParser.def_CELL_COUNT_ROW1; cell_index++) {
                sensor_value = m_PacketParser.getSensorDataByCoord(row_index, cell_index);
                atvChairCells_Row1[cell_index].setText(String.format("%d", sensor_value));
                atvChairCells_Row1[cell_index].setBackgroundColor(0x00FF0000 | (sensor_value << 24));

                String nPoint1 = atvChairCells_Row1[cell_index].getText().toString();

                // save as csv file / row 1...
                if (mSave_Flag == true) {

                    if (cell_index < 15) {
                        mPosition_Csv = mPosition_Csv + nPoint1 + ",";

                    } else {
                        mPosition_Csv = mPosition_Csv + nPoint1 + "\r\n";
                    }

                }
            }

        }

        // UI LOG : adc data
        tvADC_HexaMain.setText(m_PacketParser.textHexaMain);
        tvADC_HexaShield.setText(m_PacketParser.textHexaShield);



        boolean m_isAlarm_2000MS = false;
        long m_PostureOriginTimeMS = elapsedRealtime();
    }

    private void setPostureState(POSTURE_tag posture_state){
        if(m_PostureState != posture_state) {
            //m_isAlarm_2000MS = false;
            //m_PostureOriginTimeMS = elapsedRealtime();
        }

        //  it's not necessary
        m_PostureState = posture_state;
    }

    private POSTURE_tag getPostureState(){
        return m_PostureState;
    }

    private boolean makePostureAlarm_2000MS() {
       /* if(m_isAlarm_2000MS == true)
            return false;

        //m_Player.start();
        m_isAlarm_2000MS = true;*/
        return true;
    }

    private long getPostureElapsedTime(){
        return (elapsedRealtime() - m_PostureOriginTimeMS);
    }

    /**
     *
     * @brief App의 화면에서 보여지는 UI부분에 해당 값을 출력하는 함수
     * @details 센서로 부터 압력 값을 전달받는다. 전달 받은 값이 빈 값이면 비어있는 시간과 함께 출력해주고 센서에 값이 들어오면 해당 값과 이미지를 출력해준다.
     * @param
     * @return
     * @throws
     */
    private void UI_updateComputedData(){
        if(m_PacketParser.isPostureValid_Row1() == false){
            setPostureState(POSTURE_tag.POSTURE_NO_LOG);
            edtCOMLog.setText(String.format("EMPTY, %d sec", getPostureElapsedTime() / 1000));

            return;
        }

        //  UI draw COM - sero
        {
            float com_coord_x = m_PacketParser.getCOM_x_Row1();

            //  calculate COM - lateral
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)sp_COM_sero.getLayoutParams();
            params.setMargins(0, 0, (int)(def_UI_CELL_WIDTH * com_coord_x) + def_UI_SERO_START_X, 0); //substitute parameters for left, top, right, bottom
            sp_COM_sero.setLayoutParams(params);

            float com_coord_x_central = com_coord_x - 7.0F;
            if(com_coord_x < 6.3F){
                edtCOMLog.setText(String.format("LEFT - BAD (%1.3f), %d sec", com_coord_x_central, getPostureElapsedTime() / 1000));
                setPostureState(POSTURE_tag.POSTURE_LEFT_BAD);
            }
            else if(com_coord_x < 6.7F){
                edtCOMLog.setText(String.format("LEFT (%1.3f), %d sec", com_coord_x_central, getPostureElapsedTime() / 1000));
                setPostureState(POSTURE_tag.POSTURE_LEFT);
            }
            else if(com_coord_x < 7.3F){
                edtCOMLog.setText(String.format("CENTER (%1.3f), %d sec", com_coord_x_central, getPostureElapsedTime() / 1000));
                setPostureState(POSTURE_tag.POSTURE_CENTER);
            }
            else if(com_coord_x < 7.7F){
                edtCOMLog.setText(String.format("RIGHT (%1.3f), %d sec", com_coord_x_central, getPostureElapsedTime() / 1000));
                setPostureState(POSTURE_tag.POSTURE_RIGHT);
            }
            else {
                edtCOMLog.setText(String.format("RIGHT - BAD (%1.3f), %d sec", com_coord_x_central, getPostureElapsedTime() / 1000));
                setPostureState(POSTURE_tag.POSTURE_RIGHT_BAD);
            }
        }

        //  make alarm of bad posture
        if(2000 < getPostureElapsedTime()) {
            switch (getPostureState()){
                case POSTURE_LEFT_BAD:
                case POSTURE_LEFT:
                case POSTURE_RIGHT:
                case POSTURE_RIGHT_BAD:
                    makePostureAlarm_2000MS();
                    break;
            }
        }

        //  UI draw COC
        {
            float coc_coord_left = m_PacketParser.getCOC_left_Row1();
            //  calculate COC - left
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) sp_COC_left.getLayoutParams();
            params.setMargins(0, 0, (int) (def_UI_CELL_WIDTH * coc_coord_left) + def_UI_SERO_START_X, 0); //substitute parameters for left, top, right, bottom
            sp_COC_left.setLayoutParams(params);
        }

        {
            float coc_coord_right = m_PacketParser.getCOC_right_Row1();
            //  calculate COC - right
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)sp_COC_right.getLayoutParams();
            params.setMargins(0, 0, (int)(def_UI_CELL_WIDTH * coc_coord_right) + def_UI_SERO_START_X, 0); //substitute parameters for left, top, right, bottom
            sp_COC_right.setLayoutParams(params);
        }
    }

}