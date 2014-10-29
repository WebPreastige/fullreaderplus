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
import org.geometerplus.zlibrary.core.resources.ZLResource;

import com.fullreader.R;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import android.app.AlertDialog.Builder;

public class WallpaperAlignmentPreference extends ListPreference{

	public static String WALLPAPER_ALIGN_TILE = "wallpaper_tile";
	public static String WALLPAPER_ALIGN_CENTER = "wallpaper_center";
	public static String WALLPAPER_ALIGN_FILL = "wallpaper_fill";
	public static String WALLPAPER_ALIGN_ORIGINAL = "wallpaper_original";
	public static String WALLPAPER_ALIGN_ENABLED = "wallpaper_align_enabled";
	public static String WALLPAPER_ALIGN_KEY = "wallpaper_align_key";
	
	private SharedPreferences mPreferences;
	private Context mContext;
	private LayoutInflater mInflater;
    private CharSequence[] entries;
    private CharSequence[] entryValues;
    private SharedPreferences.Editor editor;
    private ListPreferenceAdapter mAdapter;
    
	public WallpaperAlignmentPreference(Context context) {
		super(context);
		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		mContext = context;
		mInflater = LayoutInflater.from(context);
	}
	
	public WallpaperAlignmentPreference(Context ctxt, AttributeSet attrs) {
		super(ctxt, attrs);
    }

	@Override
    protected void onPrepareDialogBuilder(Builder builder) {
        entries = getEntries();
        entryValues = getEntryValues();
        mAdapter = new ListPreferenceAdapter(mContext);
        builder.setAdapter(mAdapter, this);
        builder.setNegativeButton(null, null);
        super.onPrepareDialogBuilder(builder);
    }

	@Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        setSummary(getSummary());
    }
 
    @Override
    public CharSequence getSummary() {
    	try{
	        int pos = findIndexOfValue(getValue());
	        return getEntries()[pos];
    	}
    	catch(Exception e){return "";}
    }

	
	
	
	
	
    private class ListPreferenceAdapter extends BaseAdapter {        
        public ListPreferenceAdapter(Context context) {

        }

        public int getCount() {
            return entries.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {  
            View row = convertView;
            CustomHolder holder = null;
            TextView tView;
            if(row == null) {                                                                   
                row = mInflater.inflate(android.R.layout.simple_list_item_single_choice, parent, false);
                tView = (TextView)row.findViewById(android.R.id.text1);
                holder = new CustomHolder(row, position, tView);
                row.setTag(holder);
            }
            else  tView = (TextView)row.findViewById(android.R.id.text1);
            int theme = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);  
	        switch(theme){	    
		        case IConstants.THEME_MYBLACK:
		        	row.setBackgroundResource(R.drawable.theme_black_scroll_bg);
		        	tView.setTextColor(Color.WHITE);
	        	break;
		        case IConstants.THEME_LAMINAT:
		        	row.setBackgroundResource(R.drawable.theme_laminat_scroll_bg);
		        	tView.setTextColor(Color.BLACK);
	        	break;   	      
		        default:
		        	row.setBackgroundResource(R.drawable.theme_redtree_scroll_bg);
		        	tView.setTextColor(Color.WHITE);
	        	break;
	        }
	        tView.setText(entries[position]);
            return row;
        }

        class CustomHolder  {
            private TextView text = null;
            private RadioButton rButton = null;

            CustomHolder(View row, int position, TextView tView) {    
                text = tView;
                /*rButton = (RadioButton)row.findViewById(R.id.wallpaper_align_item_rb);
                rButton.setId(position);*/
            }
        }
    }
}
