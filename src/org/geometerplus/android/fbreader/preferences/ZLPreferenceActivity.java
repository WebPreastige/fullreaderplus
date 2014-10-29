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

import java.io.IOException;
import java.util.HashMap;

import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.SetScreenOrientationAction;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.options.ZLColorOption;
import org.geometerplus.zlibrary.core.options.ZLEnumOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.fullreader.R;

abstract class ZLPreferenceActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener{
	public static String SCREEN_KEY = "screen";
	private int theme = 0;
	
	private static final HashMap<String,Screen> myScreenMap = new HashMap<String,Screen>();

	protected class Screen {
		public final ZLResource Resource;
		public final PreferenceScreen myScreen;

		private Screen(ZLResource root, String resourceKey) {
			Resource = root.getResource(resourceKey);
			myScreen = getPreferenceManager().createPreferenceScreen(ZLPreferenceActivity.this);
			myScreen.setTitle(Resource.getValue());
			myScreen.setSummary(Resource.getResource("summary").getValue());
			getListView().setCacheColorHint(Color.TRANSPARENT);
		}

		public void setSummary(CharSequence summary) {
			myScreen.setSummary(summary);
		}

		public Screen createPreferenceScreen(String resourceKey) {
			Screen screen = new Screen(Resource, resourceKey);
			myScreen.addPreference(screen.myScreen);
			return screen;
		}

		public Preference addPreference(Preference preference) {
			myScreen.addPreference(preference);
			getListView().setCacheColorHint(Color.TRANSPARENT);
			return preference;
		}

		public Preference addOption(ZLBooleanOption option, String resourceKey) {
			return addPreference(
				new ZLBooleanPreference(ZLPreferenceActivity.this, option, Resource, resourceKey)
				
			);
		}

		public Preference addOption(ZLStringOption option, String resourceKey) {
			return addPreference(
				new ZLStringOptionPreference(ZLPreferenceActivity.this, option, Resource, resourceKey)
			);
		}

		public Preference addOption(ZLColorOption option, String resourceKey) {
			return addPreference(
				new ZLColorPreference(ZLPreferenceActivity.this, Resource, resourceKey, option)
			);
		}

		public <T extends Enum<T>> Preference addOption(ZLEnumOption<T> option, String resourceKey) {
			return addPreference(
				new ZLEnumPreference<T>(ZLPreferenceActivity.this, option, Resource, resourceKey)
			);
		}
	}

	protected PreferenceScreen myScreen;
	final ZLResource Resource;

	ZLPreferenceActivity(String resourceKey) {
		Resource = ZLResource.resource(resourceKey);
	}

	Screen createPreferenceScreen(String resourceKey) {
		final Screen screen = new Screen(Resource, resourceKey);
		myScreenMap.put(resourceKey, screen);
		myScreen.addPreference(screen.myScreen);
		getListView().setCacheColorHint(Color.TRANSPARENT);
		return screen;
	}
	
	Screen createPreferenceScreenForScreen(String resourceKey, Screen parent) {
		final Screen screen = new Screen(Resource, resourceKey);
		myScreenMap.put(resourceKey, screen);
		parent.addPreference(screen.myScreen);
		getListView().setCacheColorHint(Color.TRANSPARENT);
		return screen;
	}
	

	public Preference addPreference(Preference preference) {
		myScreen.addPreference((Preference)preference);
		getListView().setCacheColorHint(Color.TRANSPARENT);
		return preference;
	}

	public Preference addOption(ZLBooleanOption option, String resourceKey) {
		ZLBooleanPreference preference =
			new ZLBooleanPreference(ZLPreferenceActivity.this, option, Resource, resourceKey);
		myScreen.addPreference(preference);
		getListView().setCacheColorHint(Color.TRANSPARENT);
		return preference;
	}

	/*
	protected Category createCategory() {
		return new CategoryImpl(myScreen, Resource);
	}
	*/

	protected abstract void init(Intent intent);

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		//Toast.makeText(getApplicationContext(), "setting abc", Toast.LENGTH_LONG).show();
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
		setContentView(R.layout.reader_preferences);
        switch(theme){
        case IConstants.THEME_MYBLACK:
        	setTheme(R.style.Theme_myBlack);
        	getWindow().setBackgroundDrawableResource(R.drawable.theme_black_shelf);
        	getListView().setBackgroundResource(R.drawable.theme_black_shelf);
        	getListView().setCacheColorHint(Color.TRANSPARENT);
        	if (android.os.Build.VERSION.SDK_INT >= 10 && android.os.Build.VERSION.SDK_INT < 14 ){
	        	final ActionBar ab = getSupportActionBar();        
	        	ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.theme_black_shelf));
        	}
        	break;
        case IConstants.THEME_LAMINAT:
        	Log.d("MyLog", "Laminat thme");
        	setTheme(R.style.Theme_Laminat);
        	getWindow().setBackgroundDrawableResource(R.drawable.theme_laminat_shelf);
        	getListView().setBackgroundResource(R.drawable.theme_laminat_shelf);       	
        	getListView().setCacheColorHint(Color.TRANSPARENT);
        	if (android.os.Build.VERSION.SDK_INT >= 10 && android.os.Build.VERSION.SDK_INT < 14 ){
	        	final ActionBar ab = getSupportActionBar();        
	        	ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.theme_laminat_shelf));
        	}
        	break;
        case IConstants.THEME_REDTREE:
        	setTheme(R.style.Theme_Redtree);
        	getWindow().setBackgroundDrawableResource(R.drawable.theme_redtree_shelf);
        	getListView().setBackgroundResource(R.drawable.theme_redtree_shelf);        
        	getListView().setCacheColorHint(Color.TRANSPARENT);
        	if (android.os.Build.VERSION.SDK_INT >= 10 && android.os.Build.VERSION.SDK_INT < 14 ){
	        	final ActionBar ab = getSupportActionBar();        
	        	ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.theme_redtree_shelf));
        	}
        	break;
        }
		
		
		
		final ProgressDialog pd = ProgressDialog.show(this, "", ZLResource.resource("dialog").getResource("waitMessage")
				.getResource("loadInfo").getValue());
	
		if (android.os.Build.VERSION.SDK_INT >= 11){
//			getActionBar().hide();
			getActionBar().setHomeButtonEnabled(true);
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
        
		myScreen = getPreferenceManager().createPreferenceScreen(this);
		
		final Intent intent = getIntent();
		
		new Thread(){
			public void run() {
				init(intent);
				final Screen screen = myScreenMap.get(intent.getStringExtra(SCREEN_KEY));
				setPreferenceScreen(screen != null ? screen.myScreen : myScreen);
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						pd.dismiss();
					}
				});
			};
		}.start();
		/*switch(theme){
        case IConstants.THEME_MYBLACK:
        	getListView().setBackgroundResource(R.drawable.theme_black_shelf);
        	getListView().setCacheColorHint(Color.TRANSPARENT);
        	break;
        case IConstants.THEME_LAMINAT:
        	getListView().setBackgroundResource(R.drawable.theme_laminat_shelf);   
        	getListView().setCacheColorHint(Color.TRANSPARENT);
        	break;
        case IConstants.THEME_REDTREE:
        	getListView().setBackgroundResource(R.drawable.theme_redtree_shelf);   
        	getListView().setCacheColorHint(Color.TRANSPARENT);
        	break;
        }*/
	}

	@Override
	protected void onStart() {
		super.onStart();
		OrientationUtil.setOrientation(this, getIntent());

	}
	
	@Override
	protected void onResume() {
        
		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
		SetScreenOrientationAction.setOrientation(this, zlibrary.OrientationOption.getValue());
		
		super.onResume();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		OrientationUtil.setOrientation(this, intent);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(IConstants.THEME_PREF)){
			recreatethis();
		}     

	}
	
    @SuppressLint("NewApi")
	public void recreatethis()
    {
        if (android.os.Build.VERSION.SDK_INT >= 11)
        {
            super.recreate();
        }
        else
        {
            startActivity(getIntent());
            finish();
        }
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
    {
    	super.onPreferenceTreeClick(preferenceScreen, preference);
    	if (preference!=null)
	    	if (preference instanceof PreferenceScreen){
	        	if (((PreferenceScreen)preference).getDialog()!=null){
	        		((PreferenceScreen)preference).getDialog().getWindow().getDecorView()
	        		.setBackgroundDrawable(this.getWindow().getDecorView().getBackground().getConstantState().newDrawable());
	        	}
	        	
//	        	String key = "";
//	        	Iterator<Entry<String, Screen>> it = myScreenMap.entrySet().iterator();
//	        	while (it.hasNext()) {
//	                Map.Entry pairs = (Map.Entry)it.next();
//	                if(((Screen)pairs.getValue()).myScreen.equals(preferenceScreen)){
//	                	key = pairs.getKey().toString();
//	                	break;
//	                }
//	                it.remove(); // avoids a ConcurrentModificationException
//	            }
//	        	
//	        	Intent intent = new Intent(this, PreferenceActivity.class);
//	        	intent.putExtra(SCREEN_KEY, key);
//				startActivity(intent);
//	        	
	        }
    	return false;
    }
}
