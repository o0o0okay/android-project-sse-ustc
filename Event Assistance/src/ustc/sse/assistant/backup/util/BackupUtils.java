/**
 * 
 */
package ustc.sse.assistant.backup.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;

import ustc.sse.assistant.backup.BackupRestore;

import android.os.Environment;
import android.util.Log;

/**
 * @author 李健
 *
 */
public class BackupUtils {
	public static final String TAG = "BackupUtils";
	public static boolean writeToBackupFile(StringWriter writer) {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File root = Environment.getExternalStorageDirectory();
			File backupDir = new File(root, BackupRestore.BACKUP_RESTORE_DIR);
			
			if (!backupDir.exists()) {
				backupDir.mkdirs();
			}
			Calendar now = Calendar.getInstance();
			String fileName = BackupRestore.FILE_PREFIX + now.get(Calendar.YEAR) + now.get(Calendar.MONTH)
								+ now.get(Calendar.DAY_OF_MONTH) + now.get(Calendar.HOUR_OF_DAY)
								+ now.get(Calendar.MINUTE) + now.get(Calendar.SECOND) + ".xml";
			
			File backupFile = new File(backupDir, fileName);
			try {
				BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(backupFile));
				bo.write(writer.toString().getBytes());
				bo.flush();
				bo.close();
				Log.i(TAG, "backup write success");
				return true;
				
			} catch (FileNotFoundException e) {
				Log.i(TAG, "backup write fail");
				return false;
			} catch (IOException e) {
				Log.i(TAG, "backup write fail");
				return false;
			}
		}
		 
		return false;
	}
}
