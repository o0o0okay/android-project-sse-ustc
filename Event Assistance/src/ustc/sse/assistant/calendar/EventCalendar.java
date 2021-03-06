package ustc.sse.assistant.calendar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import ustc.sse.assistant.R;
import ustc.sse.assistant.calendar.utils.GregorianDate;
import ustc.sse.assistant.calendar.utils.MyCalendar;
import ustc.sse.assistant.calendar.utils.SmartDate;
import ustc.sse.assistant.event.EventAdd;
import ustc.sse.assistant.event.EventList;
import ustc.sse.assistant.event.provider.EventAssistant.Event;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * @author 李健、宋安琪
 *
 */
public class EventCalendar extends Activity implements OnGesturePerformedListener {
	private static final double CALENDAR_ROW_NUMBER = 6.0;

	public static final String SMART_DATE = "smart_date";
	public static final int NEW_EVENT_ID = Menu.FIRST;
	public static final int EVENT_LIST_ID = NEW_EVENT_ID + 1;
	
	public static final int DATE_PICKER_ID = 100;
	
	private TextView preMonthTextView;
	private TextView curMonthTextView;
	private TextView nextMonthTextView;
	
	private GridView calendarGridView;
	private Calendar preCalendar;
	private Calendar curCalendar;
	private Calendar nextCalendar;
	
	public static final String DATE_FORMAT_YEAR_MONTH = "yyyy年MM月";
	public static final String DATE_FORMAT_MONTH = "MM月";
	
	private GestureLibrary gestureLibrary;
	private Animation slideLeft;
	private Animation slideRight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.calendar);	
		initiateCalendars();
		initiateWidget();
		setAllTabText();
		delayCalendarViewInitiation();
		
		gestureLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
		if (gestureLibrary.load())
		{
			GestureOverlayView gestureOverlayView = (GestureOverlayView) findViewById(R.id.calendar_gesture_view);
			gestureOverlayView.addOnGesturePerformedListener(this);
		}
		
		slideLeft = AnimationUtils.loadAnimation(this, R.anim.slide_left);
		slideRight = AnimationUtils.loadAnimation(this, R.anim.slide_right);
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		//refresh the calendar
		initiateCalendarGridView(curCalendar, null);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.event_menu, menu);
		
		Intent eventIntent = new Intent(this, EventAdd.class);
		Intent eventListIntent = new Intent(this, EventList.class);
		
		menu.findItem(R.id.new_event).setIntent(eventIntent);
		menu.findItem(R.id.event_list).setIntent(eventListIntent);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		switch (item.getItemId()) {
		case R.id.event_jump_to_date :
			showDialog(DATE_PICKER_ID);
			return true;
		case R.id.search_event :
			onSearchRequested();
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		//set the proper calendar to MenuItem's intent extra
		MenuItem eventListMenuItem = menu.findItem(R.id.event_list);
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(curCalendar.getTimeInMillis());
		Calendar fromCalendar = Calendar.getInstance();
		fromCalendar.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1, 0, 0, 0);
		Calendar toCalendar = Calendar.getInstance();
		toCalendar.setTimeInMillis(fromCalendar.getTimeInMillis());
		toCalendar.add(Calendar.MONTH, 1);

		eventListMenuItem.getIntent().putExtra(EventList.FROM_CALENDAR, fromCalendar);
		eventListMenuItem.getIntent().putExtra(EventList.TO_CALENDAR, toCalendar);
		
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (DATE_PICKER_ID == id) {
			OnDateSetListener callBack = new OnDateSetListener() {
				
				public void onDateSet(DatePicker view, int year, int monthOfYear,
						int dayOfMonth) {
					curCalendar.set(year, monthOfYear, dayOfMonth);
					preCalendar.set(year, monthOfYear - 1, dayOfMonth);
					nextCalendar.set(year, monthOfYear + 1, dayOfMonth);
					
					initiateCalendarGridView(curCalendar, null);
					setAllTabText();
					
				}
			};
			DatePickerDialog dialog = new DatePickerDialog(this, 
													callBack, 
													curCalendar.get(Calendar.YEAR), 
													curCalendar.get(Calendar.MONTH), 
													curCalendar.get(Calendar.DAY_OF_MONTH));
		
			return dialog;
		}
		return super.onCreateDialog(id);
	}
	
	private void delayCalendarViewInitiation() {
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			
			public void run() {
				initiateCalendarGridView(curCalendar, null);
				
			}
		}, 100);
	}

	/**
	 * initiate all calendars to original state
	 */
	private void initiateCalendars() {
		preCalendar = Calendar.getInstance();
		curCalendar = Calendar.getInstance();
		nextCalendar =  Calendar.getInstance();
		
		preCalendar.roll(Calendar.MONTH, false);
		nextCalendar.roll(Calendar.MONTH, true);
		
	}

	private void initiateWidget() {
		preMonthTextView = (TextView) findViewById(R.id.calendar_left_tab);
		curMonthTextView = (TextView) findViewById(R.id.calendar_center_tab);
		nextMonthTextView = (TextView) findViewById(R.id.calendar_right_tab);	
		calendarGridView = (GridView) findViewById(R.id.calendar_gridView);
		
		calendarGridView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				SmartDate smartDate = (SmartDate) view.getTag();
				Intent intent = new Intent(EventCalendar.this, EventList.class);
				Calendar fromCalendar = Calendar.getInstance();
				fromCalendar.set(smartDate.getGregorianYear(), 
									smartDate.getGregorianMonth() - 1, 
									smartDate.getGregorianDay(), 
									0, 
									0, 
									0);
				Calendar toCalendar = Calendar.getInstance();
				toCalendar.setTimeInMillis(fromCalendar.getTimeInMillis());
				toCalendar.add(Calendar.DAY_OF_MONTH, 1);
				intent.putExtra(EventList.FROM_CALENDAR, fromCalendar);
				intent.putExtra(EventList.TO_CALENDAR, toCalendar);
				startActivity(intent);
				
			}
		});
		//set onClick listener for tabs
		initiateTabListener();
		
	}


	private void initiateTabListener() {
		OnClickListener leftTabListener = new OnClickListener() {
			
			public void onClick(View v) {
				rollCalendarsMonth(false);
				initiateCalendarGridView(curCalendar, slideRight);
				setAllTabText();
			}
		}; 
		preMonthTextView.setOnClickListener(leftTabListener);
		
		OnClickListener centerTabListener = new OnClickListener() {
			
			public void onClick(View v) {
				Calendar now = Calendar.getInstance();
				if (!(curCalendar.get(Calendar.MONTH) == now.get(Calendar.MONTH)) || 
						!(curCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR))) {
					initiateCalendars();
					initiateCalendarGridView(curCalendar, null);
					
					setAllTabText();
				}
				
			}
		};
		curMonthTextView.setOnClickListener(centerTabListener);
	
		OnClickListener rightTabListener = new OnClickListener() {
			
			public void onClick(View v) {
				rollCalendarsMonth(true);
				initiateCalendarGridView(curCalendar, slideLeft);
				setAllTabText();
			}
		};
		nextMonthTextView.setOnClickListener(rightTabListener);
	}

	/**
	 * using currentCalendar to initiate the gridview
	 * @param currentCalendar
	 */
	private void initiateCalendarGridView(Calendar currentCalendar, Animation animation) {

		int calendarHeight = calendarGridView.getHeight();
		double cellHeight = (calendarHeight - 5) / CALENDAR_ROW_NUMBER;
	
		MyCalendar myCalendar = new MyCalendar(currentCalendar);
	    SmartDate[] data = myCalendar.getCurrentMonthCalendar();
		
		ListAdapter adapter = new EventCalendarGridViewAdapter(this, data, (int) cellHeight);

		
		if (animation != null) {
			calendarGridView.startAnimation(animation);
		}
		calendarGridView.setAdapter(adapter);
		
		
	}
	
	/**
	 * direction true : increment all calendar's month by one
	 * direction false : decrease all calendar's month by one
	 * @param direction
	 */
	private void rollCalendarsMonth(boolean direction) {
			int amount = 0;
			if (direction){
				amount = 1;
			} else {
				amount = -1;
			}
			preCalendar.add(Calendar.MONTH, amount);
			curCalendar.add(Calendar.MONTH, amount);
			nextCalendar.add(Calendar.MONTH, amount);				
	}

	private void setAllTabText() {
		preMonthTextView.setText(DateFormat.format(DATE_FORMAT_MONTH, preCalendar));
		curMonthTextView.setText(DateFormat.format(DATE_FORMAT_YEAR_MONTH, curCalendar));
		nextMonthTextView.setText(DateFormat.format(DATE_FORMAT_MONTH, nextCalendar));
	}

	
	private static class EventCalendarGridViewAdapter extends BaseAdapter {

		private Context context;
		private SmartDate[] data;
		private int height;
		private HashMap<GregorianDate, Boolean> hm = new HashMap<GregorianDate, Boolean>();
		
		/**
		 * 
		 * @param ctx
		 * @param data list of map of SmartDate, a map contain a gregorian date and a lunar date
		 * @param height the height of each cell
		 */
		public EventCalendarGridViewAdapter(Context ctx, SmartDate[] data, int height) {
			this.context = ctx;
			this.data = data;
			this.height = height;

			initialEventDate();
		}
		
		private void initialEventDate() {
			int fromYear = data[0].getGregorianYear();
			int fromMonth = data[0].getGregorianMonth();
			int fromDay = data[0].getGregorianDay();
			
			int toYear = data[data.length-1].getGregorianYear();
			int toMonth = data[data.length-1].getGregorianMonth();
			int toDay = data[data.length-1].getGregorianDay();
			
			Calendar fromCalendar = Calendar.getInstance();
			fromCalendar.set(fromYear, fromMonth - 1, fromDay, 0, 0, 0);
			Calendar toCalendar = Calendar.getInstance();
			toCalendar.set(toYear, toMonth - 1, toDay, 23, 59, 59);
			toCalendar.add(Calendar.DAY_OF_YEAR, 1);

			ContentResolver cr = context.getContentResolver();
			String[] projection = { Event.BEGIN_TIME, Event.END_TIME };
			String selection = Event.BEGIN_TIME + " >=  ? AND "
					+ Event.BEGIN_TIME + " <= ? ";
			String[] selectionArgs = {
					String.valueOf(fromCalendar.getTimeInMillis()),
					String.valueOf(toCalendar.getTimeInMillis()) };
			Cursor cursor = cr.query(Event.CONTENT_URI, projection, selection,
					selectionArgs, null);
			if (cursor.moveToFirst()) {
				do {
					String beginTime = cursor.getString(cursor
							.getColumnIndex(Event.BEGIN_TIME));
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(Long.valueOf(beginTime));
					GregorianDate eventDate = new GregorianDate(
							calendar.get(Calendar.YEAR),
							calendar.get(Calendar.MONTH) + 1,
							calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
					hm.put(eventDate, true);
				} while (cursor.moveToNext());
			}
			cursor.close();		
		}

		public int getCount() {
			return data.length;
		}

		public Object getItem(int position) {
			return data[position];
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			
			LinearLayout linearLayout = null;
			if (convertView != null && convertView instanceof LinearLayout) {
				linearLayout = (LinearLayout) convertView;
			} else {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				linearLayout = (LinearLayout) inflater.inflate(R.layout.calendar_cell, parent, false);

			}
			linearLayout.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT, height));
			
			SmartDate smartDate = data[position];
			Calendar now = Calendar.getInstance();
			if (smartDate.getGregorianYear() == now.get(Calendar.YEAR) 
					&& smartDate.getGregorianMonth() == (now.get(Calendar.MONTH) + 1)
					&& smartDate.getGregorianDay() == now.get(Calendar.DAY_OF_MONTH)) {
				linearLayout.setBackgroundResource(R.drawable.calendar_today_highlight);
				
			}
		    
			// check if there are events on this day
			GregorianDate gregorianDate = new GregorianDate(
					smartDate.getGregorianYear(), smartDate.getGregorianMonth(),
					smartDate.getGregorianDay(), 0, 0, 0);
			if (hm.get(gregorianDate) != null) {
				linearLayout.setBackgroundResource(R.drawable.calendar_event_background);
			}
						
			TextView firstTv = (TextView) linearLayout.findViewById(R.id.calendar_gridview_textview1);
			TextView secondTv = (TextView) linearLayout.findViewById(R.id.calendar_gridview_textview2);
			
			//set different color and font, or other attributes using information from SmartDate
			firstTv.setText(smartDate.getGregorianDay().toString());
			secondTv.setText(smartDate.getDisplayText());
			firstTv.setTextColor(context.getApplicationContext().getResources().getColor(smartDate.getGregorianColorResId()));
			secondTv.setTextColor(context.getApplicationContext().getResources().getColor(smartDate.getLunarColorResId()));

			linearLayout.setTag(smartDate);
			return linearLayout;			
		}
		
	}
	
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture)
	{
		ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);

		if (predictions.size() > 0)
		{
			for (int i = 0; i < predictions.size(); i++)
			{
				Prediction prediction = predictions.get(i);
				if (prediction.score > 1.0)
				{
					if ("prevMonthGesture".equals(prediction.name))
					{
						rollCalendarsMonth(false);
						initiateCalendarGridView(curCalendar, slideRight);
						setAllTabText();
					} 
					
					if ("nextMonthGesture".equals(prediction.name))
					{
						rollCalendarsMonth(true);
						initiateCalendarGridView(curCalendar, slideLeft);
						setAllTabText();
					} 
				}
			}

		}

	}

}
