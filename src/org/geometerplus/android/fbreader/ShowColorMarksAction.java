/*
FullReader+ 
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com> 
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

import android.content.Intent;
import org.geometerplus.fbreader.fbreader.ReaderApp;

class ShowColorMarksAction extends FBAndroidAction {
	FullReaderActivity mActivity;
	ShowColorMarksAction(FullReaderActivity baseActivity, ReaderApp fbreader) {
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
		.putExtra("fromBook", true)
		.putExtra(com.webprestige.fr.citations.QuotesActivity.SHOW_COLOR_MARKS, true));
	}
}
