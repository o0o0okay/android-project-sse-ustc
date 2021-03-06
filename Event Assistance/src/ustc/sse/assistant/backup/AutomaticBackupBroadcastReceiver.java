/**
 * 
 */
package ustc.sse.assistant.backup;

import java.util.Calendar;

import ustc.sse.assistant.event.EventUtils;
import ustc.sse.assistant.event.provider.EventAssistant;
import ustc.sse.assistant.event.provider.EventAssistant.Event;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * @author 李健
 * 开机后检查上一次备份时间，如果已到设置的自动备份时间间隔，且自动备份功能
 * 已打开，则开启一个服务自动进行备份。
 * 如果没有上一次备份，则检查有没有事件，如果有立即备份。
 */
public class AutomaticBackupBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sf = context.getSharedPreferences(EventAssistant.TAG, Context.MODE_WORLD_WRITEABLE);
		long lastBackupDate = sf.getLong(BackupRestore.LAST_BACKUP_DATE, -1L);
		
		Calendar now = Calendar.getInstance();
		long actualInterval = now.getTimeInMillis() - lastBackupDate;
		SharedPreferences defaultSf = PreferenceManager.getDefaultSharedPreferences(context);
		boolean isBackupOn = defaultSf.getBoolean("backupRestore", false);
		String setBackupIntervalStr = defaultSf.getString("autoBackupInterval", "");
		long setBackupInterval = TextUtils.isEmpty(setBackupIntervalStr) ? 0 : EventUtils.dayToTimeInMillisecond(Integer.valueOf(setBackupIntervalStr));

		if (isBackupOn) {
			if (lastBackupDate == -1L) {
				//check whether there are events, true backup, false do nothing
				int count = haveEvent(context);
				if (count > 0) {
					//start a service to do backup
					WakeLockUtils.getWakeLock(context);
					startBackupNow(context);
				} 

			} else {
				//check the time interval
				
				//if backup is on, then we need to continue
				int count = haveEvent(context);
				if (!setBackupIntervalStr.equals("")
						&& actualInterval >= setBackupInterval && count > 0) {
					WakeLockUtils.getWakeLock(context);
					startBackupNow(context);

				} 
			}
						
		}
		
		doBackupInFuture(context, now.getTimeInMillis() + setBackupInterval);
	}

	/**
	 * @param context
	 */
	private void startBackupNow(Context context) {
		Intent service = new Intent(context, AutomaticBackupService.class);
		context.startService(service);
	}

	/**
	 * @param context
	 * @param lastBackupDate
	 * @param setBackupInterval
	 */
	private void doBackupInFuture(Context context, long triggerAtTime) {
		//set Alarm to do operation at future time
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent backupIntent = new Intent(context, this.getClass());
		backupIntent.setAction(BackupRestore.ACTION_AUTOMATIC_BACKUP);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, backupIntent, PendingIntent.FLAG_ONE_SHOT);
		
		am.set(AlarmManager.RTC_WAKEUP, triggerAtTime, pi);
	}
	
	

	/**
	 * @param context
	 * @return
	 */
	private int haveEvent(Context context) {
		Cursor c = context.getContentResolver().query(Event.CONTENT_URI, new String[]{Event._ID}, null, null, null);
		int count = c.getCount();
		c.close();
		return count;
	}

}
