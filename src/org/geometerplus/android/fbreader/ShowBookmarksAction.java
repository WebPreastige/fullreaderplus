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

import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.fbreader.ReaderApp;

import com.webprestige.fr.bookmarks.BookmarksActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;

class ShowBookmarksAction extends FBAndroidAction {
	ShowBookmarksAction(FullReaderActivity baseActivity, ReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	public boolean isVisible() {
		return ReaderApp.Model != null;
	}

	@Override
	protected void run(Object ... params) {
		//try {
		//	startBookmarksActivity(
		//		new Intent("android.reader.action.EXTERNAL_BOOKMARKS")
		//	);
	//	} catch (ActivityNotFoundException e) {
		//	startBookmarksActivity(new Intent(BaseActivity.getApplicationContext(), BookmarksActivity.class));
	//	}
		Intent i = new Intent(BaseActivity.getApplicationContext(), BookmarksActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.putExtra("isRunFromBook", true);
		i.putExtra("fromBook", true);
		BaseActivity.getApplicationContext().startActivity(i);
	//	startActivity(new Intent(getApplicationContext(), com.webprestige.fr.citations.QuotesActivity.class));
	}

/*	private void startBookmarksActivity(Intent intent) {
		intent.putExtra(
			FullReaderActivity.BOOK_KEY, SerializerUtil.serialize(ReaderApp.Model.Book)
		);
		intent.putExtra(
			FullReaderActivity.BOOKMARK_KEY, SerializerUtil.serialize(ReaderApp.createBookmark(20, true))
		);
		OrientationUtil.startActivity(BaseActivity, intent);
	}*/
}
