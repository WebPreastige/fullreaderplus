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

import org.geometerplus.android.fbreader.FullReaderActivity;
import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import ru.androidteam.listviewnumberpicker.NumberPicker;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
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

public class TimeSwitchPreference extends DialogPreference{
  
    private SharedPreferences settings;
    private NumberPicker minutes;
    private NumberPicker seconds;


    public TimeSwitchPreference(Context ctxt) {
        this(ctxt, null);
        
        setTitle(ZLResource.resource("other").getResource("autopagging_pref").getValue());
        settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        if(!loadCurrentRemindnerTime().equals("")) {
        	setSummary(loadCurrentRemindnerTime());
        }
    }

    public TimeSwitchPreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, 0);
    }

    public TimeSwitchPreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);
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
        minutes = (NumberPicker)lay.findViewById(R.id.numberPickerHours);
        minutes.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        minutes.setRange(0, 59); 
        
        seconds = (NumberPicker)lay.findViewById(R.id.numberPickerMinutes);
        seconds.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        seconds.setRange(0, 59);
        
        if(!loadCurrentRemindnerTime().equals("")) {
        	String[] curTime = loadCurrentRemindnerTime().split("\\:");
            minutes.setCurrent(Integer.parseInt(curTime[0]));
            seconds.setCurrent(Integer.parseInt(curTime[1]));
        }
        
        TextView first = (TextView) lay.findViewById(R.id.tw_pref_time_first);
        TextView second = (TextView) lay.findViewById(R.id.tw_pref_time_second);
        first.setText(ZLResource.resource("other").getResource("minutes").getValue());
        second.setText(ZLResource.resource("other").getResource("seconds").getValue());
        
        LinearLayout body = (LinearLayout)lay.findViewById(R.id.lo_reminder_body);

    	int theme = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
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

        long start = settings.getLong("autopage", 1367222460000l);
        
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(start);
        
     /*   if(cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0) {
        	seconds.setCurrent(1);
        	minutes.setCurrent(0);
        } else {
        	minutes.setCurrent(cal.get(Calendar.MINUTE));
        	seconds.setCurrent(cal.get(Calendar.SECOND));
        }*/
        minutes.setCurrent(cal.get(Calendar.MINUTE));
    	seconds.setCurrent(cal.get(Calendar.SECOND));
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            Calendar  calDay = Calendar.getInstance();
            if(minutes.getCurrent() == 0 && seconds.getCurrent() == 0) {
            	calDay.set(Calendar.SECOND, 1);
            	calDay.set(Calendar.MINUTE, 0);
            } else {
            	calDay.set(Calendar.MINUTE, minutes.getCurrent());
            	calDay.set(Calendar.SECOND, seconds.getCurrent());
            }
            //calDay.set(Calendar.MINUTE, minutes.getCurrent());
        	//calDay.set(Calendar.SECOND, seconds.getCurrent());
            String time=null;
            
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss", Locale.ENGLISH);
            
            time = sdf.format(calDay.getTime());
            
            if (callChangeListener(time)) {
                persistString(time);
            }
            
            setSummary(time);
            
            SharedPreferences.Editor editor = settings.edit();
            
            int seconds = calDay.getTime().getSeconds();
            int minutes = calDay.getTime().getMinutes();
            seconds *=1000;
            if(minutes > 0) {
            	minutes *= 60000;
            }
            editor.putLong("autopage", Long.valueOf(seconds));
            FullReaderActivity.autopagingTime = Long.valueOf(seconds);
            Log.d("seconds and minutes: ", String.valueOf(seconds+minutes));
            editor.putString("currentRemindnerTimeAutopage", time);
            editor.apply();
        }
    }
    
    public String loadCurrentRemindnerTime() {
	    return settings.getString("currentRemindnerTimeAutopage", "");
    }

}
