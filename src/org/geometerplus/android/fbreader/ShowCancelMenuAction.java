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

import java.util.List;

import org.geometerplus.fbreader.fbreader.ReaderApp;

class ShowCancelMenuAction extends FBAndroidAction {
	ShowCancelMenuAction(FullReaderActivity baseActivity, ReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	protected void run(Object ... params) {
		if (!ReaderApp.jumpBack()) {
			final List<ReaderApp.CancelActionDescription> descriptionList =
				ReaderApp.getCancelActionsList();
//			if (descriptionList.size() == 1) {
				ReaderApp.closeWindow();
//			} else {
//				final Intent intent = new Intent();
//				intent.setClass(BaseActivity, CancelActivity.class);
//				intent.putExtra(CancelActivity.LIST_SIZE, descriptionList.size());
//				int index = 0;
//				for (ReaderApp.CancelActionDescription description : descriptionList) {
//					intent.putExtra(CancelActivity.ITEM_TITLE + index, description.Title);
//					intent.putExtra(CancelActivity.ITEM_SUMMARY + index, description.Summary);
//					++index;
//				}
//				BaseActivity.startActivityForResult(intent, Reader.REQUEST_CANCEL_MENU);
//			}
		}
	}
}
