package ustc.sse.assistant.event;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ustc.sse.assistant.R;
import ustc.sse.assistant.contact.ContactSelection;
import ustc.sse.assistant.event.data.EventEntity;
import ustc.sse.assistant.event.provider.EventAssistant;
import ustc.sse.assistant.event.provider.EventAssistant.Event;
import ustc.sse.assistant.event.provider.EventAssistant.EventContact;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.format.DateFormat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

/**
 * 
 * @author 李健
 *
 */
public class EventAdd extends Activity{
	/** Called when the activity is first created. */
	public static final int CONTACT_REQUEST_CODE = 100;
	
	private Button cancelButton;
	private Button saveButton;
	private Button beginDateButton;
	private Button endDateButton;
	private Button beginTimeButton;
	private Button endTimeButton;
	private ImageView contactImageButton;
	
	private Spinner prioriAlarmDaySpinner;
	private Spinner prioriAlarmRepeatSpinner;
	private Spinner alarmTypeSpinner;
	
	private EditText contentEditText;
	private EditText locationEditText;
	private EditText contactEditText;
	private EditText noteEditText;
	
	private Calendar beginCalendar = Calendar.getInstance();
	private Calendar endCalendar = Calendar.getInstance();
	
	private int prioriAlarmDay = 0;
	private int prioriAlarmRepeat = 0;
	private int alarmType = 0;
	private String location;
	private String note;
	private String content;
	private Map<Long, String> contactData = new HashMap<Long,String>();
	
	public static final String DATE_FORMAT = "yyyy年MM月dd日 EE";
	public static final String TIME_FORMAT = "h:mmaa";
	
	public static final int BEGIN_DATE_DIALOG = 0;
	public static final int BEGIN_TIME_DIALOG = 1;
	public static final int END_DATE_DIALOG = 2;
	public static final int END_TIME_DIALOG = 3;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_add);
        
        initiateWidgets();
        
    }

	private void initiateWidgets() {
		contactImageButton = (ImageView) findViewById(R.id.event_add_contact_add_imageView);
		cancelButton = (Button) findViewById(R.id.event_add_cancel_button);
		saveButton = (Button) findViewById(R.id.event_add_save_button);
		beginDateButton = (Button) findViewById(R.id.event_add_beginTime_date_button);
		endDateButton = (Button) findViewById(R.id.event_add_endTime_date_button);
		beginTimeButton = (Button) findViewById(R.id.event_add_beginTime_time_button);
		endTimeButton = (Button) findViewById(R.id.event_add_endTime_time_button);
		
		prioriAlarmDaySpinner = (Spinner) findViewById(R.id.event_add_priori_alarm_day_spinner);
		prioriAlarmRepeatSpinner = (Spinner) findViewById(R.id.event_add_priori_alarm_repeat_spinner);
		alarmTypeSpinner = (Spinner) findViewById(R.id.event_add_alarm_type_spinner);
		
		contentEditText = (EditText) findViewById(R.id.event_add_content_editText);
		locationEditText = (EditText) findViewById(R.id.event_add_location_editText);
		contactEditText = (EditText) findViewById(R.id.event_add_contact_editText);
		noteEditText = (EditText) findViewById(R.id.event_add_note_editText);
		
		initiateContactImageView();
		initiateButtons();
		
		//initiate spinners
		initiateSpinner();

		
	}

	private void initiateContactImageView() {
		contactImageButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EventAdd.this, ContactSelection.class);
				startActivityForResult(intent, CONTACT_REQUEST_CODE);
			}
		});
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CONTACT_REQUEST_CODE && data != null) {
			StringBuilder sb = new StringBuilder();
			long[] contactIds = data.getLongArrayExtra(ContactSelection.SELECTED_CONTACT_IDS);
			String[] names = data.getStringArrayExtra(ContactSelection.SELECTED_CONTACT_DISPLAY_NAME);
		
			if (contactIds != null && names != null) {
				for (int i = 0; i < contactIds.length; i++) {
					contactData.put(contactIds[i], names[i]);
					sb.append(names[i] + " ");							
				}	
				contactEditText.setText(sb.toString());
			}
		}
	}

	private void initiateSpinner() {
		String[] prioriDayStringArray = getApplicationContext().getResources().getStringArray(R.array.entries_list_priori_day);
		int[] prioriDayIntArray = getApplicationContext().getResources().getIntArray(R.array.entriesvalue_list_priori_day);
		
		List<Map<String, Integer>> prioriAlarmDayData = new ArrayList<Map<String,Integer>>();
		for (int i = 0; i < prioriDayStringArray.length; i++) {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("name", prioriDayIntArray[i]);
			map.put("value", prioriDayIntArray[i]);
			prioriAlarmDayData.add(map);
		}
		SpinnerAdapter prioriAlarmDayAdapter = new SimpleAdapter(this, 
														prioriAlarmDayData, 
														R.layout.event_add_spinner_item,
														new String[]{"name", "value"}, 
														new int[]{R.id.event_add_spinner_textview1, R.id.event_add_spinner_textview2});
		prioriAlarmDaySpinner.setAdapter(prioriAlarmDayAdapter);
		prioriAlarmDaySpinner.setPromptId(R.string.event_priorAlarm);
		prioriAlarmDaySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				TextView tv = (TextView) view.findViewById(R.id.event_add_spinner_textview2);
				prioriAlarmDay = Integer.valueOf(tv.getText().toString());
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		//--------------------------------------------------------------------------
		String[] prioriRepeatStringArray = getApplicationContext().getResources().getStringArray(R.array.entries_list_priori_repeat);
		int[] prioriRepeatIntArray = getApplicationContext().getResources().getIntArray(R.array.entriesvalue_list_priori_repeat);
		List<Map<String, Integer>> prioriAlarmRepeatData = new ArrayList<Map<String,Integer>>();
		for (int i = 0; i < prioriRepeatStringArray.length; i++) {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("name", prioriRepeatIntArray[i]);
			map.put("value", prioriRepeatIntArray[i]);
			prioriAlarmRepeatData.add(map);
		}
		SpinnerAdapter prioriAlarmRepeatAdapter = new SimpleAdapter(this, 
				prioriAlarmRepeatData, 
				R.layout.event_add_spinner_item,
				new String[]{"name", "value"}, 
				new int[]{R.id.event_add_spinner_textview1, R.id.event_add_spinner_textview2});
		prioriAlarmRepeatSpinner.setAdapter(prioriAlarmRepeatAdapter);
		prioriAlarmRepeatSpinner.setPromptId(R.string.event_repeat);
		prioriAlarmRepeatSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				TextView tv = (TextView) view.findViewById(R.id.event_add_spinner_textview2);
				prioriAlarmRepeat = Integer.valueOf(tv.getText().toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		
		});
		//---------------------------------------------------------------------------------
		String[] alarmTypeStringArray = getApplicationContext().getResources().getStringArray(R.array.entries_list_alarm_type);
		int[] alarmTypeIntArray = getApplicationContext().getResources().getIntArray(R.array.entriesvalue_list_alarm_type);
		List<Map<String, Object>> alarmTypeData = new ArrayList<Map<String,Object>>();
		for (int i = 0; i < alarmTypeStringArray.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("name", alarmTypeStringArray[i]);
			map.put("value", alarmTypeIntArray[i]);
			alarmTypeData.add(map);
		}
		SpinnerAdapter alarmTypeAdapter = new SimpleAdapter(this, 
				alarmTypeData, 
				R.layout.event_add_spinner_item,
				new String[]{"name", "value"}, 
				new int[]{R.id.event_add_spinner_textview1, R.id.event_add_spinner_textview2});

		alarmTypeSpinner.setAdapter(alarmTypeAdapter);
		alarmTypeSpinner.setPromptId(R.string.event_alarmType);
		alarmTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				TextView tv = (TextView) view.findViewById(R.id.event_add_spinner_textview2);
				alarmType = Integer.valueOf(tv.getText().toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}

	private void initiateButtons() {
		//initiate buttons
		Calendar calendar = Calendar.getInstance();
		CharSequence currentDate = DateFormat.format(DATE_FORMAT, calendar);
		beginDateButton.setText(currentDate);
		endDateButton.setText(currentDate);
		CharSequence currentTime = DateFormat.format(TIME_FORMAT, calendar);
		beginTimeButton.setText(currentTime);
		endTimeButton.setText(currentTime);
		
		beginDateButton.setOnClickListener(beginDateListener);
		beginTimeButton.setOnClickListener(beginTimeListener);
		endDateButton.setOnClickListener(endDateListener);
		endTimeButton.setOnClickListener(endTimeListener);
		
		cancelButton.setOnClickListener(cancelButtonListener);
		saveButton.setOnClickListener(saveButtonListener);
	}
	
	private OnClickListener cancelButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			EventAdd.this.finish();
		}
	};
	private OnClickListener saveButtonListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			//save all the data and store into database
			//if necessary, start the alarm services
			ContentResolver cr = getContentResolver();
			//store event
			content = contentEditText.getText().toString();
			location = locationEditText.getText().toString();
			note = noteEditText.getText().toString();
			Calendar now = Calendar.getInstance();
			ContentValues contentValues = EventEntity.eventToContentValues(
																	content,
																	String.valueOf(beginCalendar.getTimeInMillis()), 
																	String.valueOf(alarmType), 
																	String.valueOf(beginCalendar.getTimeInMillis()), 
																	String.valueOf(endCalendar.getTimeInMillis()), 
																	String.valueOf(now.getTimeInMillis()), 
																	String.valueOf(now.getTimeInMillis()), 
																	location, 
																	note, 
																	prioriAlarmDay, 
																	prioriAlarmRepeat);
			Uri newUri = cr.insert(Event.CONTENT_URI, contentValues);
			//store eventcontact
			Long eventId = Long.valueOf(newUri.getPathSegments().get(1));
			ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
			Iterator<Entry<Long, String>> iter = contactData.entrySet().iterator();
			
			while (iter.hasNext()) {
				Entry<Long, String> entry = iter.next();
				ContentProviderOperation op = ContentProviderOperation
														.newInsert(EventContact.CONTENT_URI)
														.withValue(EventContact.EVENT_ID, eventId)
														.withValue(EventContact.CONTACT_ID, entry.getKey())
														.withValue(EventContact.DISPLAY_NAME, entry.getValue())
														.build();
				ops.add(op);
			}
						
			try {
				cr.applyBatch(EventAssistant.EVENT_CONTACT_AUTHORITY, ops);
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (OperationApplicationException e) {
				e.printStackTrace();
			}				
			//start alarm service here
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			
			EventAdd.this.finish();
		}
	};
	
	private OnClickListener beginDateListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			showDialog(BEGIN_DATE_DIALOG);
		}
	};
	private OnClickListener beginTimeListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			showDialog(BEGIN_TIME_DIALOG);
		}
	};
	private OnClickListener endDateListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			showDialog(END_DATE_DIALOG);			
		}
	};
	private OnClickListener endTimeListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			showDialog(END_TIME_DIALOG);
		}
	};
	
	protected Dialog onCreateDialog(int id) {
		Calendar today = Calendar.getInstance();
		OnDateSetListener beginDateCallBack = new OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				beginCalendar.set(year, monthOfYear, dayOfMonth);
				beginDateButton.setText(DateFormat.format(DATE_FORMAT, beginCalendar));
				
			}
		};
		
		OnTimeSetListener beginTimeCallBack = new OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				beginCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				beginCalendar.set(Calendar.MINUTE, minute);
				beginTimeButton.setText(DateFormat.format(TIME_FORMAT, beginCalendar));
			}
		};
		
		OnDateSetListener endDateCallBack = new OnDateSetListener() {
			
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				Calendar calendar = Calendar.getInstance();
				calendar.set(year, monthOfYear, dayOfMonth);
				calendar.set(Calendar.HOUR_OF_DAY, endCalendar.get(Calendar.HOUR_OF_DAY));
				calendar.set(Calendar.MINUTE, endCalendar.get(Calendar.MINUTE));
				if (calendar.after(beginCalendar)) {
					endCalendar.set(year, monthOfYear, dayOfMonth);
					endDateButton.setText(DateFormat.format(DATE_FORMAT, endCalendar));				
				}							
			}
		};
		
		OnTimeSetListener endTimeCallback = new OnTimeSetListener() {
			
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(endCalendar.getTimeInMillis());
				calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				calendar.set(Calendar.MINUTE, minute);
				
				if (calendar.after(beginCalendar)) {
					endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
					endCalendar.set(Calendar.MINUTE, minute);
					endTimeButton.setText(DateFormat.format(TIME_FORMAT, endCalendar));

				}							
			}
		};
		switch (id) {
		case BEGIN_DATE_DIALOG :
			DatePickerDialog datePickerDialog = new DatePickerDialog(this,
														beginDateCallBack, 
														today.get(Calendar.YEAR), 
														today.get(Calendar.MONTH), 
														today.get(Calendar.DAY_OF_MONTH));
			return datePickerDialog;
		case BEGIN_TIME_DIALOG :
			TimePickerDialog timePickerDialog = new TimePickerDialog(this, 
															beginTimeCallBack, 
															today.get(Calendar.HOUR_OF_DAY), 
															today.get(Calendar.MINUTE), 
															true);
			return timePickerDialog;
		case END_DATE_DIALOG :
			DatePickerDialog datePickerDialog2 = new DatePickerDialog(this, 
															endDateCallBack, 
															today.get(Calendar.YEAR), 
															today.get(Calendar.MONTH), 
															today.get(Calendar.DAY_OF_MONTH));
			return datePickerDialog2;
		case END_TIME_DIALOG :
			TimePickerDialog timePickerDialog2 = new TimePickerDialog(this, 
															endTimeCallback, 
															today.get(Calendar.HOUR_OF_DAY), 
															today.get(Calendar.MINUTE), 
															true);
			return timePickerDialog2;
			
		}
		
		return null;
	}

}