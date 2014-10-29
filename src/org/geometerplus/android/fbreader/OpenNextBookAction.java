/*
FullReader+ 
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
copyright (coffee) 2009-2013 Geometer Plus <contact@geometerplus.com> 
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

import java.util.List;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.fbreader.ReaderApp;

import android.util.Log;



class OpenNextBookAction extends FBAndroidAction {
	
	private FullReaderActivity mActivity;
	private ReaderApp mReaderApp;
	private long current;
	
	OpenNextBookAction(FullReaderActivity baseActivity, ReaderApp fbreader) {
		super(baseActivity, fbreader);
		mActivity = baseActivity;
		mReaderApp = fbreader;
	}

	@Override
	protected void run(Object ... params) {
		List<Book> booksInFolder = mActivity.getBooksInFolder();
		if (booksInFolder.size() == 0 || booksInFolder == null) return;
		current = mActivity.getCurrentBookId();
		int pos = 0;
		for (Book book : booksInFolder){
			if (current == book.getId()){
				break;
			}
			pos++;
		}
		if (pos != booksInFolder.size()-1){
			pos++;
			Book book = booksInFolder.get(pos);
			current = book.getId();
			mReaderApp.openBook(book, null, null);
			mActivity.setCurrentBookId(current);
		}
	}
}
