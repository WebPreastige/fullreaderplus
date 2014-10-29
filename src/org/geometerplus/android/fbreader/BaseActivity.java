/*
 * 	FullReader+
 *	Copyright 2013-2014 Viktoriya Bilyk
 *
 * 	Original FBreader code 
 *	copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com> 
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
 * 
 */


package org.geometerplus.android.fbreader;

import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.fullreader.R;

public abstract class BaseActivity extends SherlockFragmentActivity implements MenuItem.OnMenuItemClickListener,
OnSharedPreferenceChangeListener{

	protected int theme;
    
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	      case android.R.id.home:
	        finish();
	    }
		return true;
	  }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));
		
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

		theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
        switch(theme){
        case IConstants.THEME_MYBLACK:
        	setTheme(R.style.Theme_myBlack);
        	break;
        case IConstants.THEME_LAMINAT:
            setTheme(R.style.Theme_Laminat);
        	break;
        case IConstants.THEME_REDTREE:
        	setTheme(R.style.Theme_Redtree);
        	break;
        }

        getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onDestroy() {
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
        
		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
		SetScreenOrientationAction.setOrientation(this, zlibrary.OrientationOption.getValue());
		super.onResume();
	}

	protected MenuItem addMenuItem(Menu menu, int id, String resourceKey, int iconId, boolean isVisible) {
		final String label = ZLResource.resource("menu").getResource(resourceKey).getValue();
		final MenuItem item = menu.add(0, id, Menu.NONE, label);
		item.setOnMenuItemClickListener(this);
		item.setIcon(iconId);
		if(isVisible)
		    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		else
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		return item;
	}
	
	protected MenuItem addStringMenuItem(Menu menu, int id, String title, int iconId, boolean isVisible) {
		final String label = title;
		final MenuItem item = menu.add(0, id, Menu.NONE, label);
		item.setOnMenuItemClickListener(this);
		item.setIcon(iconId);
		if(isVisible)
		    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		else
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

		return item;
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(IConstants.THEME_PREF) || key.equals(IConstants.LANG_PREF)){
			recreatethis();
		}     
	}
	
    public void recreatethis()
    {
        if (android.os.Build.VERSION.SDK_INT >= 11)
        {
            super.recreate();
        }
        else
        {
            finish();
            startActivity(getIntent());
        }
    }
    
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
        case android.R.id.home:
        	onBackPressed();
//        	onKeyDown(KeyEvent.KEYCODE_BACK, null);
//        	startActivity(new Intent(this, StartScreenActivity.class));
//        	finish();
            break;
        }
        return true;
    }
}
