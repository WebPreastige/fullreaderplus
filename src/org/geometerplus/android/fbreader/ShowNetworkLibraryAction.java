/*
* FullReader+
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader;

import java.util.Locale;

import org.geometerplus.android.fbreader.network.NetworkLibraryPrimaryActivity;
import org.geometerplus.fbreader.fbreader.ReaderApp;

import com.webprestige.fr.bookmarks.BookmarksActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

class ShowNetworkLibraryAction extends FBAndroidAction {
	
	private FullReaderActivity mActivity;
	ShowNetworkLibraryAction(FullReaderActivity baseActivity, ReaderApp fbreader) {
		super(baseActivity, fbreader);
		mActivity = baseActivity;
	}

	@Override
	protected void run(Object ... params) {
		/*OrientationUtil.startActivity(
			BaseActivity, new Intent(
				BaseActivity.getApplicationContext(),
				NetworkLibraryPrimaryActivity.class
			)
		);*/
		if(isNetworkConnected()) {
			Intent intent = new Intent(BaseActivity.getApplicationContext(), NetworkLibraryPrimaryActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(NetworkLibraryPrimaryActivity.START_REQUEST, NetworkLibraryPrimaryActivity.SEARCH_REQ);
			BaseActivity.getApplicationContext().startActivity(intent);
			
		} else {
			if(loadCurrentLanguage().equals("ru")) {
				Toast.makeText(mActivity, "Нет активного подключения к интернету!", Toast.LENGTH_LONG).show();
			} else if(loadCurrentLanguage().equals("en")) {
				Toast.makeText(mActivity, "No active internet connection!", Toast.LENGTH_LONG).show();					
			} else if(mActivity.equals("de")) {
				Toast.makeText(mActivity, "Es gibt keine aktive Verbindung zum Internet!", Toast.LENGTH_LONG).show();
			} else if(loadCurrentLanguage().equals("fr")) {
				Toast.makeText(mActivity, "Il n'y a aucune connexion active à l'Internet!", Toast.LENGTH_LONG).show();
			} else if(loadCurrentLanguage().equals("uk")) {
				Toast.makeText(mActivity, "Немає активного підключення до інтернету!", Toast.LENGTH_LONG).show();
			} else {
				if(Locale.getDefault().getDisplayLanguage().equals("русский") || Locale.getDefault().getDisplayLanguage().equals("українська")) {
					Toast.makeText(mActivity, "Нет активного подключения к интернету!", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(mActivity, "No active internet connection!", Toast.LENGTH_LONG).show();
				}	
			}
		}
	}
	
	
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
		  NetworkInfo ni = cm.getActiveNetworkInfo();
		  if (ni == null) {
			  // There are no active networks.
			  return false;
		  } else
			  return true;
	}
	
	String loadCurrentLanguage() {
	    SharedPreferences sPref = mActivity.getSharedPreferences("languagePrefs", mActivity.MODE_PRIVATE);
	    return sPref.getString("curLanguage", "");
	}
}
