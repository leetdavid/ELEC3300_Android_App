package paraverity.com.elec3300_bt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.TextView;

import static paraverity.com.elec3300_bt.MainActivity.PREFS_NAME;

/**
 * Created by David on 08-May-17.
 */

public class NotificationListener extends NotificationListenerService {

	public static final String listenerServiceAction = "paraverity.com.elec3300_bt.NOTIFICATION_LISTENER_SERVICE_EXAMPLE";

	private String TAG = this.getClass().getSimpleName();
	private NotifReceiver NotificationListenerreciver;
	@Override
	public void onCreate() {
		super.onCreate();
		NotificationListenerreciver = new NotifReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(listenerServiceAction);
		registerReceiver(NotificationListenerreciver,filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(NotificationListenerreciver);
	}

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {

		Log.i(TAG,"**********  onNotificationPosted");
		Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
		Intent i = new  Intent(listenerServiceAction);
		i.putExtra("notification_event","notif:" + sbn.getPackageName());
		sendBroadcast(i);

	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		Log.i(TAG,"********** onNOtificationRemoved");
		Log.i(TAG,"ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText +"\t" + sbn.getPackageName());
		Intent i = new  Intent(listenerServiceAction);
		i.putExtra("notification_event","rmved:" + sbn.getPackageName() + "\n");

		sendBroadcast(i);
	}

	class NotifReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getStringExtra("command") == null){
				//Log.d(PREFS_NAME, "Why :(");
			} else
			if(intent.getStringExtra("command").equals("clearall")){
				NotificationListener.this.cancelAllNotifications();
			}
			else if(intent.getStringExtra("command").equals("list")){
				Intent i1 = new  Intent(listenerServiceAction);
				i1.putExtra("notification_event","=====================");
				sendBroadcast(i1);
				int i=1;
				for (StatusBarNotification sbn : NotificationListener.this.getActiveNotifications()) {
					Intent i2 = new  Intent(listenerServiceAction);
					i2.putExtra("notification_event",i +" " + sbn.getPackageName() + "\n");
					sendBroadcast(i2);
					i++;
				}
				Intent i3 = new  Intent(listenerServiceAction);
				i3.putExtra("notification_event","===== Notification List ====");
				sendBroadcast(i3);
				Log.d(PREFS_NAME, "Notifications posted!");
			}

		}
	}

}