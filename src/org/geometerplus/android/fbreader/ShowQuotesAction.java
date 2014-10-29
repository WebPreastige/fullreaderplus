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
import com.webprestige.fr.citations.QuotesActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;

class ShowQuotesAction extends FBAndroidAction {
	FullReaderActivity mActivity;
	ShowQuotesAction(FullReaderActivity baseActivity, ReaderApp fbreader) {
		super(baseActivity, fbreader);
		mActivity = baseActivity;
	}

	@Override
	public boolean isVisible() {
		return ReaderApp.Model != null;
	}

	@Override
	protected void run(Object ... params) {
		mActivity.startActivity(new Intent(mActivity, com.webprestige.fr.citations.QuotesActivity.class)
		.putExtra("isRunFromBook", true)
		.putExtra("cIndex", ReaderApp.quotesCIndex)
		.putExtra("pIndex", ReaderApp.quotesPIndex)
		.putExtra("eIndex", ReaderApp.quotesEIndex)
		.putExtra("fromBook", true));
		/*try {
			startBookmarksActivity(
				new Intent("android.reader.action.EXTERNAL_BOOKMARKS")
			);
		} catch (ActivityNotFoundException e) {
			startBookmarksActivity(
				new Intent(BaseActivity.getApplicationContext(), QuotesFragmentActivity.class)
			);
		}*/
		/*Intent i = new Intent(BaseActivity.getApplicationContext(), QuotesActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		BaseActivity.getApplicationContext().startActivity(i);*/
	}

	/*private void startBookmarksActivity(Intent intent) {
		intent.putExtra(
			FullReaderActivity.BOOK_KEY, SerializerUtil.serialize(ReaderApp.Model.Book)
		);
		intent.putExtra(
			FullReaderActivity.BOOKMARK_KEY, SerializerUtil.serialize(ReaderApp.createBookmark(20, true))
		);
		OrientationUtil.startActivity(BaseActivity, intent);
	}*/
}
