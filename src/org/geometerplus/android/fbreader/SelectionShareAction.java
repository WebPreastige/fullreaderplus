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

import org.geometerplus.fbreader.fbreader.ReaderApp;

import android.content.Intent;

public class SelectionShareAction extends FBAndroidAction {
	SelectionShareAction(FullReaderActivity baseActivity, ReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		String text = ReaderApp.getTextView().getSelectedText();
		ReaderApp.getTextView().clearSelection();

		try{
            text = "\""+text+"\""+" (c) "+ReaderApp.Model.Book.getTitle()+", "+ReaderApp.Model.Book.authors().get(0).DisplayName;
		}catch(IndexOutOfBoundsException e){
            text = "\""+text+"\""+" (c) "+ReaderApp.Model.Book.getTitle();
		}

		final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
		BaseActivity.startActivity(Intent.createChooser(intent, null));
	}
}
