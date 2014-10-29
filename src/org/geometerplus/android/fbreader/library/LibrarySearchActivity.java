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

package org.geometerplus.android.fbreader.library;

import org.geometerplus.android.fbreader.SetScreenOrientationAction;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LibrarySearchActivity extends Activity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			Log.d("MyLog", "Start search action");
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			if (pattern != null && pattern.length() > 0) {
				Log.d("MyLog", "Start search action pattern - " + pattern);
				intent = new Intent(
					LibraryActivity.START_SEARCH_ACTION, null, this, LibraryActivity.class
				);
				intent.putExtra(SearchManager.QUERY, pattern);
				Log.d("MyLog", "Start library activity");
				startActivity(intent);
			}
		}
		finish();
	}
	
	@Override
	protected void onResume() {
        
		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
		SetScreenOrientationAction.setOrientation(this, zlibrary.OrientationOption.getValue());

		super.onResume();
	}
}
