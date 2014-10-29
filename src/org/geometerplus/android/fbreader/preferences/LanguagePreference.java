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

import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.zlibrary.core.language.Language;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.Toast;

abstract class LanguagePreference extends ZLStringListPreference {
	SharedPreferences sPref;
	LanguagePreference(
		Context context, ZLResource rootResource, String resourceKey, List<Language> languages
	) {
		super(context, rootResource, resourceKey);
		
		final int size = languages.size();
		String[] codes = new String[size];
		String[] names = new String[size];
		int index = 0;
		for (Language l : languages) {
			codes[index] = l.Code;
			names[index] = l.Name;
			++index;
		}
		setLists(codes, names);
		init();
		
		setNegativeButtonText(ZLResource.resource("other").getResource("cancel").getValue());
	}
	
	 void saveCurrentLanguage(String language) {
		    sPref = getContext().getSharedPreferences("languagePrefs", getContext().MODE_PRIVATE);
		    Editor ed = sPref.edit();
		    ed.putString("curLanguage", language);
		    ed.commit();
	  }

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		if (result) {
			setLanguage(getValue());
			//Toast.makeText(getContext(), getValue(), Toast.LENGTH_LONG).show();
			saveCurrentLanguage(getValue());
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        	SharedPreferences.Editor editor = settings.edit();
            int k = settings.getInt(IConstants.LANG_PREF, 0);
            k++;
			editor.putInt(IConstants.LANG_PREF, k );
            editor.commit();
		}
	}

	protected abstract void init();
	protected abstract void setLanguage(String code);
}
