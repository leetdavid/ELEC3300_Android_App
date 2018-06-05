package paraverity.com.elec3300_bt;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;
import static paraverity.com.elec3300_bt.MainActivity.PREFS_NAME;
import static paraverity.com.elec3300_bt.NotificationListener.listenerServiceAction;

public class DeviceControlActivity extends AppCompatActivity {

	private ProgressDialog progress;
	BluetoothConnector btc = null;
	BluetoothAdapter btAdapter = null;
	BluetoothSocket btSocket = null;
	String address = null;
	private boolean isBtConnected = false;
	final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	TextView tv;
	private NotificationReceiver receiver;

	boolean alarmIsEnabled = false;

	Button btn_motor, btn_playpause, btn_synch, btn_alarm;
	char motorState = 'z'; //y = on
	char playState = 'p'; //q = resume

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_control);

		tv = (TextView) findViewById(R.id.tv);
		receiver = new NotificationReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(listenerServiceAction);
		registerReceiver(receiver,filter);

		btn_motor = (Button)findViewById(R.id.btn_motor);
		btn_playpause = (Button)findViewById(R.id.btn_playpause);
		btn_synch = (Button)findViewById(R.id.btn_synch_time);
		btn_alarm = (Button)findViewById(R.id.btn_alarm);

		Intent newint = getIntent();
		address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS);

		new ConnectBT().execute();

		btn_motor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(motorState == 'y'){
					motorState = 'z';
				} else {
					motorState = 'y';
				}
				btSend(motorState);
			}
		});

		btn_playpause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(motorState == 'q'){
					motorState = 'p';
					btn_playpause.setText(R.string.pause);
				} else {
					motorState = 'q';
					btn_playpause.setText(R.string.play);
				}
				btSend(playState);
			}
		});

		btn_synch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				btSendTime();
			}
		});

		btn_alarm.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if(alarmIsEnabled){

					btn_alarm.setText(R.string.alarm);
					btSend('d');
					alarmIsEnabled = false;

				} else {


					TimePickerDialog timePickerDialog = new TimePickerDialog(DeviceControlActivity.this, new TimePickerDialog.OnTimeSetListener() {
						public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

							int h = 0, m = 0;
							if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
								h = view.getHour();
								if(h > 12) h-=12;
								m = view.getMinute();
							}

							String builder = "a";

							if(h < 10) builder += "0" + h;
							else builder += h;
							if(m == 0) builder += "00";
							else if(m < 10) builder += "0" + m;
							else builder += m;

							btn_alarm.setText(builder.substring(1));
							Log.d(PREFS_NAME, builder);
							btSend(builder);
							alarmIsEnabled = true;
						}
					}, 12, 0, false);
					timePickerDialog.show();
				}
			}
		});

	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterReceiver(receiver);
		disconnect();
	}

	private void msg(String s){
		Toast.makeText(getApplicationContext(),s, Toast.LENGTH_SHORT).show();
	}

	private void disconnect(){
		if(btSocket != null){
			try{
				btSocket.close();
			} catch (Exception e){
				msg("ERROR");
			}
		}
		finish();
	}

	private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
	{
		private boolean ConnectSuccess = true; //if it's here, it's almost connected

		@Override
		protected void onPreExecute()
		{
			progress = ProgressDialog.show(DeviceControlActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
		}

		@Override
		protected Void doInBackground(Void... devices) {//while the progress dialog is shown, the connection is done in background
			try {
				if (btSocket == null || !isBtConnected)
				{
					btAdapter = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
					BluetoothDevice bluetoothDevice = btAdapter.getRemoteDevice(address);//connects to the device's address and checks if it's available
					btSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
					BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

					btc = new BluetoothConnector(bluetoothDevice, false, btAdapter, null);
					try{
						btc.connect();
						if(!btc.success){
							msg("Connection failed.");
							finish();
						}
					} catch(Exception e){
						e.printStackTrace();
					}

					//btSocket.connect();//start connection
				}
			}
			catch (IOException e){
				ConnectSuccess = false;//if the try failed, you can check the exception here
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result){
			//after the doInBackground, it checks if everything went fine
			super.onPostExecute(result);
			if (!ConnectSuccess){
				msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
				finish();
			}
			else{
				msg("Connected.");
				btSendTime();
				btSendTime();
				btSend('s');
				isBtConnected = true;
			}
			progress.dismiss();
		}
	}

	public void buttonClicked(View v){
		if(v.getId() == R.id.btnCreateNotify){
			NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Notification.Builder ncomp = new Notification.Builder(this);
			ncomp.setContentTitle("My Notification");
			ncomp.setContentText("Notification Listener Service Example");
			ncomp.setTicker("Notification Listener Service Example");
			ncomp.setSmallIcon(R.mipmap.ic_launcher);
			ncomp.setAutoCancel(true);
			nManager.notify((int)System.currentTimeMillis(),ncomp.build());
		}
		else if(v.getId() == R.id.btnListNotify) {
			Intent i = new Intent(listenerServiceAction);
			i.putExtra("command", "list");
			sendBroadcast(i);
		} else if(v.getId() == R.id.btn_like){
			btSend("i0;");
		} else if(v.getId() == R.id.btn_facebook) {
			btSend("i1;");
		} else if(v.getId() == R.id.btn_whatsapp){
			btSend("i2;");
		} else if(v.getId() == R.id.btn_gmail) {
			btSend("i3;");
		} else if(v.getId() == R.id.btn_heart){
			btSend("i4;");
		} else if(v.getId() == R.id.btn_clock){
			btSend("i5;");
		} else if(v.getId() == R.id.btn_replay){
			playState = 'q';
			btn_playpause.setText(R.string.pause);
			btSend('r');
		} else if(v.getId() == R.id.btn_stop){
			playState = 'p';
			btn_playpause.setText(R.string.play);
			btSend('s');
		} else if(v.getId() == R.id.btn_cant){
			playState = 'q';
			btn_playpause.setText(R.string.pause);
			btSend('u');
		}
	}

	public int getIcon(String pkgName){
		switch(pkgName){
			case "com.whatsapp":					//Whatsapp
			case "com.facebook.orca":				//Messenger
			case "org.telegram.messenger":			//Telegram
			case "paraverity.com.elec3300_bt":  	//Our App
				return 2;

			case "com.facebook.katana":				//Facebook
				return 1;

			case "com.google.android.apps.inbox":	//Inbox by Google
			case "com.google.android.gm":			//Gmail
				return 3;
		}
		return -1;
	}

	class NotificationReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent){

			//Execute this function when a new notification is received
			String temp = intent.getStringExtra("notification_event") + "\n" + tv.getText();
			tv.setText(temp);
			String notifPkg = intent.getStringExtra("notification_event");
			if(notifPkg == null) return;
			int icon = getIcon(notifPkg.substring(6));
			if(icon == -1) return;
			String send = "i" + icon + ";";
			Log.d(PREFS_NAME, "Notification Sent: " + notifPkg + " " + send);
			btSend(send);
		}
	}

	public void btSendTime(){
		DateFormat df = new java.text.SimpleDateFormat("hhmm");
		String str = df.format(new Date());
		btSend("t" + str + ";");
	}

	boolean btSend(String s){
		if(btSocket != null){
			try{
				btc.bluetoothSocket.getOutputStream().write(s.getBytes());
				return true;
			} catch(Exception e) {
				msg("Error sending bytes");
				e.printStackTrace();
			}
		}
		Log.d(PREFS_NAME, "BT Sent:" + s);
		return false;
	}

	boolean btSend(char c){
		return btSend("" + c);
	}
}
