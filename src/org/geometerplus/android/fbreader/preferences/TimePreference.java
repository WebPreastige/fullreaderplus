/*
FullReader+ 
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com> 
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 
 * 02110-1301, USA.
*/

package org.geometerplus.android.fbreader.preferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.fullreader.R;

public class TimePreference extends DialogPreference {

	private SharedPreferences settings;
	private EditText edtStartHour;
	private EditText edtStartMin;
	private EditText edtEndHour;
	private EditText edtEndMin;


	public TimePreference(Context ctxt) {
		this(ctxt, null);
		
		setTitle(ZLResource.resource("other").getResource("day_night_pref").getValue());
		settings = PreferenceManager.getDefaultSharedPreferences(getContext());
		if(edtStartHour != null && edtStartMin != null && edtEndHour != null && edtEndMin != null) {
			initCurrentDayNightPrefs();
		}
	}

	public TimePreference(Context ctxt, AttributeSet attrs) {
		this(ctxt, attrs, 0);
	}

	public TimePreference(Context ctxt, AttributeSet attrs, int defStyle) {
		super(ctxt, attrs, defStyle);
	}

	/** Hide the cancel button */
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);
		builder.setNegativeButton(null, null);
		builder.setPositiveButton(null, null);
	}
	
	private void initCurrentDayNightPrefs() {
		edtStartHour.setText(String.valueOf(settings.getInt("dayStartHour", 1)));
		edtStartMin.setText(String.valueOf(settings.getInt("dayStartMinute", 1)));
		edtEndHour.setText(String.valueOf(settings.getInt("nightStartHour", 1)));
		edtEndMin.setText(String.valueOf(settings.getInt("nightStartMinute", 1)));
	}

	@Override
	protected View onCreateDialogView() {
		ScrollView scrView = new ScrollView(getContext());

		LinearLayout view;

		int theme = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
		switch(theme){
		default:
			view = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.pref_day_night_theme_black, null);
			break;
		case IConstants.THEME_MYBLACK:
			view = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.pref_day_night_theme_black, null);
			break;
		case IConstants.THEME_LAMINAT:
			view = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.pref_day_night_theme_laminat, null);
			break;
		case IConstants.THEME_REDTREE:
			view = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.pref_day_night_theme_redtree, null);
			break;
		}

		((TextView)view.findViewById(R.id.tw_order)).setText(ZLResource.resource("other").getResource("pref_start_day_mode").getValue());
		((TextView)view.findViewById(R.id.tw_text)).setText(ZLResource.resource("other").getResource("pref_end_day_mode").getValue());
		
		//view.setOrientation(LinearLayout.VERTICAL);
		TextView title = (TextView)view.findViewById(R.id.tw_pref_orientation_title);
		title.setText(getTitle());
		
		edtStartHour = (EditText) view.findViewById(R.id.edt_start_hour);
		edtStartMin = (EditText) view.findViewById(R.id.edt_start_min);
		edtEndHour = (EditText) view.findViewById(R.id.edt_end_hour);
		edtEndMin = (EditText) view.findViewById(R.id.edt_end_min);
		
		if(android.os.Build.VERSION.SDK_INT >= 9 &&  android.os.Build.VERSION.SDK_INT <14){
			int colorText = Color.WHITE;
			edtStartHour.setTextColor(colorText);
			edtStartMin.setTextColor(colorText);
			edtEndHour.setTextColor(colorText);
			edtEndMin.setTextColor(colorText);
		}
		
		
		TextWatcher hoursWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}

			@Override
			public void afterTextChanged(Editable s) {
				try{
					int hour = Integer.valueOf(s.toString());
					/*if(hour<0){
						s.replace(0, s.length(),"0");
					}else */if (hour>23){
						s.replace(0, s.length(),"23");
						//				}else{
						//					s.clear();
						//					s.append(""+hour);
					}
				}catch(NumberFormatException ee){
					//s.replace(0, s.length(),"0");
				}
			}
		};

		edtStartHour.addTextChangedListener(hoursWatcher);
		edtEndHour.addTextChangedListener(hoursWatcher);

		TextWatcher minsWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				try{
					int minute = Integer.valueOf(s.toString());
					/*if(minute<1){
						s.replace(0, s.length(),"0");
					}else*/ if (minute>59){
						s.replace(0, s.length(),"59");
						//				}else{
						//					s.clear();
						//					s.append(""+minute);
					}
				}catch(NumberFormatException ee){
					//s.replace(0, s.length(),"0");
				}
			}

		};

		edtStartMin.addTextChangedListener(minsWatcher);
		edtEndMin.addTextChangedListener(minsWatcher);

		Button btnOk = (Button) view.findViewById(R.id.btn_ok);
		Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);

		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (edtStartHour.getText().length() == 0) edtStartHour.setText("0");
				if (edtStartMin.getText().length() == 0) edtStartMin.setText("0");
				if (edtEndHour.getText().length() == 0) edtEndHour.setText("0");
				if (edtEndMin.getText().length() == 0) edtEndMin.setText("0");
				onDialogClosed(true);
				getDialog().dismiss();
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onDialogClosed(false);
				getDialog().dismiss();
			}
		});

		btnOk.setText(ZLResource.resource("other").getResource("ok").getValue());
		btnCancel.setText(ZLResource.resource("other").getResource("cancel").getValue());


		scrView.addView(view);

		return scrView;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);

		long start = settings.getLong("dayStart", 969688800000l);
		long end = settings.getLong("dayEnd", 969735600000l);

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(start);
		
		/*edtStartHour.setText(""+cal.get(Calendar.HOUR_OF_DAY));
		edtStartMin.setText(""+cal.get(Calendar.MINUTE));

		cal.setTimeInMillis(end);
		edtEndHour.setText(""+cal.get(Calendar.HOUR_OF_DAY));
		edtEndMin.setText(""+cal.get(Calendar.MINUTE));*/
		initCurrentDayNightPrefs();
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			Calendar  calDay = Calendar.getInstance();
			Calendar  calNight = Calendar.getInstance();
			if(edtStartHour.equals("0") && edtStartMin.equals("0")) {
				calDay.set(Calendar.HOUR_OF_DAY, 0);
				calDay.set(Calendar.MINUTE, 1);
            } 
			if(edtEndHour.equals("0") && edtStartMin.equals("0")) {
				calNight.set(Calendar.HOUR_OF_DAY, 0);
				calNight.set(Calendar.MINUTE, 1);
			}
			try{
				//if(!edtStartHour.equals("0") && !edtStartMin.equals("0")) {
					calDay.set(Calendar.HOUR_OF_DAY, Integer.valueOf(edtStartHour.getText().toString()));
					calDay.set(Calendar.MINUTE, Integer.valueOf(edtStartMin.getText().toString()));
				//}
				//if(!edtEndHour.equals("0") && !edtStartMin.equals("0")) {
					calNight.set(Calendar.HOUR_OF_DAY, Integer.valueOf(edtEndHour.getText().toString()));
					calNight.set(Calendar.MINUTE, Integer.valueOf(edtEndMin.getText().toString()));
				//}
			}catch(NumberFormatException e){
				calDay.set(Calendar.HOUR_OF_DAY, 0);
				calDay.set(Calendar.MINUTE, 0);

				calNight.set(Calendar.HOUR_OF_DAY, 0);
				calNight.set(Calendar.MINUTE, 0);
			}
			String time=null;

			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

			time = sdf.format(calDay.getTime()) + " / "+ sdf.format(calNight.getTime());

			//            if (callChangeListener(time)) {
			//                persistString(time);
			//            }

			setSummary(time);


			SharedPreferences.Editor editor = settings.edit();
			//editor.putLong("dayStart", calDay.getTimeInMillis());
			//editor.putLong("dayEnd", calNight.getTimeInMillis());
			
			editor.putInt("dayStartHour", Integer.valueOf(edtStartHour.getText().toString()));
			editor.putInt("dayStartMinute", Integer.valueOf(edtStartMin.getText().toString()));
			editor.putInt("nightStartHour", Integer.valueOf(edtEndHour.getText().toString()));
			editor.putInt("nightStartMinute", Integer.valueOf(edtEndMin.getText().toString()));
			editor.commit();
		}
	}

}
