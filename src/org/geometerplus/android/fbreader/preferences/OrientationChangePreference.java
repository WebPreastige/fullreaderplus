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

import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.fullreader.R;

public class OrientationChangePreference  extends DialogPreference{

	private final ZLStringOption myOption;
	private ZLResource myResource;
	private RadioButton rbSystemOrient;
	private RadioButton rbHorzOrient;
	private RadioButton rbVertOrient;
	private int choosenIndex;
	
	private String[] values;
	private String[] texts;


	public OrientationChangePreference(Context context, ZLResource rootResource, String resourceKey, ZLStringOption option, String[] values) {
		super(context, null);
		myResource = rootResource.getResource(resourceKey);
		setTitle(myResource.getValue());

		myOption = option;
		
		this.values = values;
		texts = new String[values.length];
		for (int i = 0; i < values.length; ++i) {
			final ZLResource resource = myResource.getResource(values[i]);
			texts[i] = resource.hasValue() ? resource.getValue() : values[i];
		}

		setInitialValue(option.getValue());
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		if (result) {
			setSummary(getEntry());
		}
		myOption.setValue(getValue());
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
    	LinearLayout lay = null;
    	int theme = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
        switch(theme) {
	        case IConstants.THEME_MYBLACK:
	        	lay = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.pref_orientation_dialog_black, null);
	        	break;
	        case IConstants.THEME_LAMINAT:
	        	lay = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.pref_orientation_dialog_laminat, null);
	        	break;
	        case IConstants.THEME_REDTREE:
	        	lay = (LinearLayout)LayoutInflater.from(getContext()).inflate(R.layout.pref_orientation_dialog_redtree, null);
	        	break;
        }
        
        TextView title = (TextView)lay.findViewById(R.id.tw_pref_orientation_title);
        title.setText(getTitle());
        		
        title.setPadding(10, 10, 10, 10);
        lay.setPadding(10, 10, 10, 10);
        
        rbSystemOrient =  (RadioButton)lay.findViewById(R.id.rb_orientation_system);
        rbHorzOrient =  (RadioButton)lay.findViewById(R.id.rb_orientation_horiz);
        rbVertOrient =  (RadioButton)lay.findViewById(R.id.rb_orientation_vert);
        
        rbVertOrient.setClickable(false);
        rbHorzOrient.setClickable(false);
        rbSystemOrient.setClickable(false);
        
        switch(choosenIndex){
        case 0:
        	rbSystemOrient.setChecked(true);
        	break;
        case 3:
        	rbHorzOrient.setChecked(true);
        	break;
        case 2:
        	rbVertOrient.setChecked(true);
        	break;
        }
        
        View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				switch(v.getId()){
				case R.id.lo_body_sys:
					rbHorzOrient.setChecked(false);
					rbVertOrient.setChecked(false);
					rbSystemOrient.setChecked(true);
					choosenIndex =0;
					break;
				case R.id.lo_body_horz:
					rbHorzOrient.setChecked(true);
					rbVertOrient.setChecked(false);
					rbSystemOrient.setChecked(false);
					choosenIndex =3;
					break;
				case R.id.lo_body_vert:
					rbHorzOrient.setChecked(false);
					rbVertOrient.setChecked(true);
					rbSystemOrient.setChecked(false);
					choosenIndex =2;
					break;
				}
				
			}
		};
		lay.findViewById(R.id.lo_body_sys).setOnClickListener(listener );
		lay.findViewById(R.id.lo_body_vert).setOnClickListener(listener );
		lay.findViewById(R.id.lo_body_horz).setOnClickListener(listener );
		
        
		Button btnOk = (Button) lay.findViewById(R.id.btn_ok);
		Button btnCancel = (Button) lay.findViewById(R.id.btn_cancel);
		btnCancel.setVisibility(View.GONE);
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
        
    }	

	protected final boolean setInitialValue(String value) {
		int index = 0;
		boolean found = false;;
		final CharSequence[] entryValues = getEntryValues();
		if (value != null) {
			for (int i = 0; i < entryValues.length; ++i) {
				if (value.equals(entryValues[i])) {
					index = i;
					found = true;
					break;
				}
			}
		}
		setValueIndex(index);
		setSummary(getEntry());
		return found;
	}

	private CharSequence getEntry() {
		return texts[choosenIndex];
	}

	private void setValueIndex(int index) {
		choosenIndex = index;
	}

	private CharSequence[] getEntryValues() {
		return values;
	}

	private String getValue() {
		byte pos = 0;
		if(rbHorzOrient.isChecked())
			pos = 3;
		else
			if(rbVertOrient.isChecked())
				pos = 2;
		return values[pos];
	}
	
}
