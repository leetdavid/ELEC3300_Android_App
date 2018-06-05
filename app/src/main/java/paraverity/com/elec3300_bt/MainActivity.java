package paraverity.com.elec3300_bt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

	public static final String PREFS_NAME = "ELEC3300_BT";

	Button btnPaired;
	ListView devicelist;

	private BluetoothAdapter bt = null;
	private Set<BluetoothDevice> pairedDevices;

	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
			= new BottomNavigationView.OnNavigationItemSelectedListener() {

		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			switch (item.getItemId()) {
				case R.id.navigation_home:
					//mTextMessage.setText(R.string.title_home);
					return true;
				case R.id.navigation_dashboard:
					//mTextMessage.setText(R.string.title_dashboard);
					return true;
				case R.id.navigation_notifications:
					//mTextMessage.setText(R.string.title_notifications);
					return true;
			}
			return false;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//Initalize Bluetooth
		bt = BluetoothAdapter.getDefaultAdapter();
		if(bt == null){
			Toast.makeText(getApplicationContext(), "Bluetooth device not available!", Toast.LENGTH_SHORT).show();
			finish();
		} else {
			if(bt.isEnabled()){

			} else {
				Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(turnBTon, 1);
			}
		}

		btnPaired = (Button)findViewById(R.id.button);
		devicelist = (ListView)findViewById(R.id.listView);

		btnPaired.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pairedDevicesList();
			}
		});

		//BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
		//navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
	}

	private void pairedDevicesList(){
		pairedDevices = bt.getBondedDevices();
		ArrayList<Object> list = new ArrayList<>();

		if(pairedDevices.size() > 0){
			for(BluetoothDevice bt : pairedDevices){
				list.add(bt.getName() + "\n" + bt.getAddress());
			}
		} else {
			Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices found!", Toast.LENGTH_SHORT).show();
		}

		final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
		devicelist.setAdapter(adapter);
		devicelist.setOnItemClickListener(myListClickListener);
	}

	private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
	{
		public void onItemClick (AdapterView av, View v, int arg2, long arg3)
		{
			// Get the device MAC address, the last 17 chars in the View
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);
			// Make an intent to start next activity.
			Intent i = new Intent(MainActivity.this, DeviceControlActivity.class);
			//Change the activity.
			i.putExtra(EXTRA_INFO, info);
			i.putExtra(EXTRA_ADDRESS, address); //this will be received at ledControl (class) Activity
			startActivity(i);
		}
	};

	public static final String
			EXTRA_ADDRESS = "extraadress",
			EXTRA_INFO = "extrainfo";


}
