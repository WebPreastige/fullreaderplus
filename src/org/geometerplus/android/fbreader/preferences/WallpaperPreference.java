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

import java.util.List;

import org.geometerplus.fbreader.fbreader.ColorProfile;
import org.geometerplus.fbreader.fbreader.WallpapersUtil;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

class WallpaperPreference extends ZLStringListPreference {
	private final ZLStringOption myOption;
	private Context mContext;
	private WallpaperAlignmentPreference mPreference;
	private Preference mBcgColorPreference;
	
	WallpaperPreference(Context context, ColorProfile profile, ZLResource resource, String resourceKey) {
		super(context, resource, resourceKey);
		mContext = context;
		myOption = profile.WallpaperOption;
		final List<ZLFile> predefined = WallpapersUtil.predefinedWallpaperFiles();
		final List<ZLFile> external = WallpapersUtil.externalWallpaperFiles();

		final int size = 1 + predefined.size() + external.size();
		final String[] values = new String[size];
		final String[] texts = new String[size];

		final ZLResource optionResource = resource.getResource(resourceKey);
		values[0] = "";
		texts[0] = optionResource.getResource("solidColor").getValue();
		int index = 1;
		for (ZLFile f : predefined) {
			values[index] = f.getPath();
			final String name = f.getShortName();
			texts[index] = optionResource.getResource(
				name.substring(0, name.indexOf("."))
			).getValue();
			++index;
		}
		for (ZLFile f : external) {
			values[index] = f.getPath();
			texts[index] = f.getShortName();
			++index;
		}
		
		for(ZLFile f : external) {
			Log.d("EXTERNAL WALLPAPERS: ", f.getPath());
		}
		for(ZLFile f : predefined) {
			Log.d("PREDEFINED WALLPAPERS: ", f.getPath());
		}
		setLists(values, texts);

		setInitialValue(myOption.getValue());
		// Делаем проверку - может ли фон выравниваться
		wallpaperCanBeAligned(myOption.getValue());
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		myOption.setValue(getValue());
		wallpaperCanBeAligned(getValue());
		bcgPrefCanBeEnabled(getValue());
	}

	private void bcgPrefCanBeEnabled(String name){
		if (name.length() == 0) mBcgColorPreference.setEnabled(true);
		else mBcgColorPreference.setEnabled(false);
	}
	
	// ------- Метод, по которому в настройки будет сохраняться опция что фон можно выравнивать или нет -------
	public void wallpaperCanBeAligned(String wallpaperName){
		boolean isEnabled = false;
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		if (wallpaperName.length()!=0 && wallpaperName!= null ){
			String [] splitted = wallpaperName.split("/");
			if (!splitted[0].equals("wallpapers")) isEnabled = true;
			
		}
		Editor editor = preferences.edit();
		editor.putBoolean(WallpaperAlignmentPreference.WALLPAPER_ALIGN_ENABLED, isEnabled);
		editor.commit();
		if (mPreference!=null)mPreference.setEnabled(isEnabled);
	}
	
	public void setWallpaperAlignmentPreference(WallpaperAlignmentPreference preference){
		mPreference = preference;
	}
	
	public void setBcgColorPreference(Preference bcgColorPref){
		mBcgColorPreference = bcgColorPref;
	}
	
}
