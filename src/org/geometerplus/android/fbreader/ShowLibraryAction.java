/*
* FullReader+
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.fbreader.ReaderApp;

import android.content.ActivityNotFoundException;
import android.content.Intent;

class ShowLibraryAction extends FBAndroidAction {
	ShowLibraryAction(FullReaderActivity baseActivity, ReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		try {
			startLibraryActivity(
				new Intent("android.reader.action.EXTERNAL_LIBRARY")
			);
		} catch (ActivityNotFoundException e) {
			Intent intent = new Intent(BaseActivity.getApplicationContext(), LibraryActivity.class);
			///intent.putExtra(LibraryActivity.OPEN_TREE_KEY, LibraryActivity.READ_TREE);
			startLibraryActivity(intent);
		}
	}

	private void startLibraryActivity(Intent intent) {
		if (ReaderApp.Model != null) {
			intent.putExtra(FullReaderActivity.BOOK_KEY, SerializerUtil.serialize(ReaderApp.Model.Book));
		}
		OrientationUtil.startActivity(BaseActivity, intent);
	}
}
