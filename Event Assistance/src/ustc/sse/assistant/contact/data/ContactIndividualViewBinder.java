package ustc.sse.assistant.contact.data;

import ustc.sse.assistant.R;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

/**
 * 
 * @author 李健
 *This class was used when binding data from list of contact to individual view
 */
public class ContactIndividualViewBinder implements ViewBinder {

	public boolean setViewValue(View view, Object data, String textRepresentation) {
		if (view instanceof ImageView) {
			ImageView iv = (ImageView) view;
			
			byte[] photo = (byte[]) data;
			if (photo != null) {
				Bitmap bm = BitmapFactory.decodeByteArray(photo, 0, photo.length);
				iv.setImageBitmap(bm);
			} else {
				iv.setImageResource(R.drawable.default_contact_image);
			}
			
		} else if (view instanceof TextView) {
			TextView tv = (TextView) view;
			if (tv.getId() == R.id.contactIndividualName) {
				tv.setText((String) data);
			} else {
				//hiddenIndividualId
				tv.setText(data.toString());
			}
			
		}
		
		
		return true;
	}

}
