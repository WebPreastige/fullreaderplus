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
import java.util.concurrent.TimeUnit;

import org.geometerplus.android.fbreader.BaseActivity;
import org.geometerplus.android.fbreader.FullReaderActivity;
import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import ru.androidteam.listviewnumberpicker.NumberPicker;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fullreader.R;

public class ReminderChoosePreference extends DialogPreference{
  
    private SharedPreferences settings;
    private NumberPicker hours;
    private NumberPicker minutes;


    public ReminderChoosePreference(Context ctxt) {
        this(ctxt, null);
        
        setTitle(ZLResource.resource("other").getResource("reminder_pref").getValue());
        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(!loadCurrentRemindnerTime().equals("")) {
        	setSummary(loadCurrentRemindnerTime());
        }
    }
    
    public ReminderChoosePreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, 0);
    }

    public ReminderChoosePreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);

        setPositiveButtonText(ZLResource.resource("other").getResource("ok").getValue());
        setNegativeButtonText(ZLResource.resource("other").getResource("cancel").getValue());
    }

    /** Hide the cancel button */
    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setNegativeButton(null, null);
        builder.setPositiveButton(null, null);
    }

    @Override
    protected View onCreateDialogView() {
        LinearLayout lay = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.reminder_dialog, null);
        
        ((TextView)(lay.findViewById(R.id.tw_pref_time_first))).setText(ZLResource.resource("other").getResource("hours").getValue());
        ((TextView)(lay.findViewById(R.id.tw_pref_time_second))).setText(ZLResource.resource("other").getResource("minutes").getValue());
        
        hours = (NumberPicker)lay.findViewById(R.id.numberPickerHours);
        hours.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        hours.setRange(0, 24);
        
        minutes = (NumberPicker)lay.findViewById(R.id.numberPickerMinutes);
        minutes.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        minutes.setRange(0, 59);
        if(!loadCurrentRemindnerTime().equals("")) {
        	String[] curTime = loadCurrentRemindnerTime().split("\\:");
            hours.setCurrent(Integer.parseInt(curTime[0]));
        	minutes.setCurrent(Integer.parseInt(curTime[1]));
        }
        
    	int theme = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
        LinearLayout body = (LinearLayout)lay.findViewById(R.id.lo_reminder_body);
        switch(theme) {
	        case IConstants.THEME_MYBLACK:
	        	body.setBackgroundResource(R.drawable.theme_black_bg);
	        	break;
	        case IConstants.THEME_LAMINAT:
	        	body.setBackgroundResource(R.drawable.theme_laminat_bg);
	        	break;
	        case IConstants.THEME_REDTREE:
	        	body.setBackgroundResource(R.drawable.theme_redtree_bg);
	        	break;
        }
        
		Button btnOk = (Button) lay.findViewById(R.id.btn_ok);
		Button btnCancel = (Button) lay.findViewById(R.id.btn_cancel);
		
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
        
        return lay;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        long start = settings.getLong("reminder", 969670800000l);
        
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(start);
        
        if(cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.HOUR) == 0) {
        	minutes.setCurrent(1);
        	hours.setCurrent(0);
        } else {
        	minutes.setCurrent(minutes.getCurrent());
        	hours.setCurrent(hours.getCurrent());
        }

        //if(< 1) {
        //	hours.setCurrent(1);
        //} else {
        //	hours.setCurrent(cal.get(Calendar.HOUR));
        //}
        
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
       /* if (positiveResult) {
            Calendar calDay = Calendar.getInstance();
           // calDay.set(Calendar.HOUR_OF_DAY, hours.getCurrent());
            //calDay.set(Calendar.MINUTE, minutes.getCurrent());
            if(hours.getCurrent() == 0 && minutes.getCurrent() == 0) {
            	calDay.set(Calendar.MINUTE, 1);
            	calDay.set(Calendar.HOUR_OF_DAY, 0);
            } else {
            	calDay.set(Calendar.MINUTE, minutes.getCurrent());
            	calDay.set(Calendar.HOUR_OF_DAY, hours.getCurrent());
            }
            String time=null;
            
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            
            time = sdf.format(calDay.getTime());

            if (callChangeListener(time)) {
                persistString(time);
            }
            
            setSummary(time);
            
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("reminder", calDay.getTimeInMillis());
            editor.putString("currentRemindnerTime", time);
            editor.commit();
            
           // int hours = calDay.getTime().getHours();
            int minutes = calDay.getTime().getMinutes();
          //  minutes *=1000;
           // if(hours > 0) {
           // 	hours *= 60000;
           // }
            
            editor.putLong("reminder", TimeUnit.MINUTES.toMillis(minutes));
            FullReaderActivity.reminderTime = Long.valueOf(TimeUnit.MINUTES.toMillis(minutes));
            FullReaderActivity.reminderTimer = true;
            Log.d("seconds: ", String.valueOf(FullReaderActivity.reminderTime));
            editor.putString("currentRemindnerTimeAutopage", time);
            editor.apply();
        }*/
        
        if (positiveResult) {
            Calendar calDay = Calendar.getInstance();
           // calDay.set(Calendar.HOUR_OF_DAY, hours.getCurrent());
            //calDay.set(Calendar.MINUTE, minutes.getCurrent());
            if(hours.getCurrent() == 0 && minutes.getCurrent() == 0) {
            	calDay.set(Calendar.MINUTE, 1);
            	calDay.set(Calendar.HOUR_OF_DAY, 0);
            } else {
            	calDay.set(Calendar.MINUTE, minutes.getCurrent());
            	calDay.set(Calendar.HOUR_OF_DAY, hours.getCurrent());
            }
            String time=null;
            
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.ENGLISH);

            time = sdf.format(calDay.getTime());

            if (callChangeListener(time)) {
                persistString(time);
            }
            long timeToRemind = TimeUnit.MINUTES.toMillis(minutes.getCurrent());
            setSummary(time);
            
            SharedPreferences.Editor editor = settings.edit();
           
            editor.putLong("reminder", calDay.getTimeInMillis());
            editor.putLong("timeRemind", timeToRemind);
            saveTimeRemind(timeToRemind);
            editor.putString("currentRemindnerTime", time);
            editor.commit();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            Log.d("REMIND: ", String.valueOf(prefs.getLong("timeRemind", 1)));
        }
    }
    
    void saveTimeRemind(long value) {
	    SharedPreferences sPref = getContext().getSharedPreferences("remindPrefs", getContext().MODE_PRIVATE);
	    Editor ed = sPref.edit();
	    ed.putLong("timeRemind", value);
	    ed.commit();
  }
    
    public String loadCurrentRemindnerTime() {
	    return settings.getString("currentRemindnerTime", "");
 }

}
