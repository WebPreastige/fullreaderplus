/*
* FullReader+
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
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
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.graphics.Color;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fullreader.R;

abstract class ZLStringListPreference extends ListPreference {
	private final ZLResource myResource;

	ZLStringListPreference(Context context, ZLResource rootResource, String resourceKey) {
		super(context);
		
		myResource = rootResource.getResource(resourceKey);
		setTitle(myResource.getValue());
	}

	protected final void setList(String[] values) {
		String[] texts = new String[values.length];
		for (int i = 0; i < values.length; ++i) {
			final ZLResource resource = myResource.getResource(values[i]);
			texts[i] = resource.hasValue() ? resource.getValue() : values[i];
		}
		setLists(values, texts);
	}

	protected final void setLists(String[] values, String[] texts) {
		assert(values.length == texts.length);
		setEntries(texts);
		setEntryValues(values);
		
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
    
	
	
	
	
    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
    	
		int theme = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
		final int color;
		final int colorText;
        switch(theme){
        case IConstants.THEME_MYBLACK:
        	color = Color.parseColor("#555555");
        	colorText = Color.WHITE;
        	break;
        case IConstants.THEME_LAMINAT:
        	color = Color.parseColor("#f1dcc2");
        	colorText = Color.BLACK;
        	break;
        default:
        	color = Color.parseColor("#4d2114");
        	colorText = Color.WHITE;
        	break;
        }
    	
    	ListAdapter listAdapter = new ArrayAdapter<CharSequence>(getContext(), android.R.layout.simple_list_item_single_choice, 
    			this.getEntries()){
    		@Override
    		public View getView(int position, View convertView, ViewGroup parent) {
    			View v = super.getView(position, convertView, parent);
    			int theme = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
    			switch(theme){
    	        case IConstants.THEME_MYBLACK:
    	        	v.setBackgroundResource(R.drawable.theme_black_scroll_bg);
    	        	break;
    	        case IConstants.THEME_LAMINAT:
    	        	v.setBackgroundResource(R.drawable.theme_laminat_scroll_bg);
    	        	break;   	      
    	        default:
    	        	v.setBackgroundResource(R.drawable.theme_redtree_scroll_bg);
    	        	break;
    	        }
    			((TextView) v.findViewById(android.R.id.text1)).setTextColor(colorText);
    			return v;
    		}
    	};
    	
		builder.setAdapter(listAdapter, this);
		builder.setNegativeButton(null, null);

		super.onPrepareDialogBuilder(builder);
    }
    
	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		if (result) {
			setSummary(getEntry());
		}
	}
	
}
