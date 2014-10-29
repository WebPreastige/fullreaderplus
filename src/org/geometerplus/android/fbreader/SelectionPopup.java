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

import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.ReaderApp;

import android.view.View;
import android.widget.RelativeLayout;

import com.fullreader.R;

class SelectionPopup extends ButtonsPopupPanel {
	final static String ID = "SelectionPopup";
	// Переменная, по которой мы будем ориентироваться, какие иконки показывать, а какие - нет
	private boolean isFromRar = false;
	
	SelectionPopup(ReaderApp fbReader, boolean fromRar) {
		super(fbReader);
		isFromRar = fromRar;
	}
	
	@Override
	public String getId() {
		return ID;
	}
	
	@Override
	public void createControlPanel(FullReaderActivity activity, RelativeLayout root) {
		if (myWindow != null && activity == myWindow.getActivity()) {
			return;
		}
		
		myWindow = new PopupWindow(activity, root, PopupWindow.Location.Floating, false);
		if (!isFromRar) addButton(ActionCode.SELECTION_MARK_COLOR, true, R.drawable.color_marker);
		addButton(ActionCode.SELECTION_DICTIONARY, true, R.drawable.dictionary);
		addButton(ActionCode.SELECTION_TRANSLATE, true, R.drawable.translate);
		addButton(ActionCode.SELECTION_SHARE, true, R.drawable.share);
		if (!isFromRar) addButton(ActionCode.SELECTION_BOOKMARK, true, R.drawable.fb_share_from_read);
		if (!isFromRar) addButton(ActionCode.SELECTION_QUOTES, true, R.drawable.save);
		addButton(ActionCode.SELECTION_COPY_TO_CLIPBOARD, true, R.drawable.copy);
		addButton(ActionCode.SELECTION_CLEAR, true, R.drawable.close);
	}
	
	public void move(int selectionStartY, int selectionEndY) {
		if(myWindow == null) {
			return;
		}
		
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
			RelativeLayout.LayoutParams.WRAP_CONTENT,
			RelativeLayout.LayoutParams.WRAP_CONTENT
		);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

		final int verticalPosition;
		final int screenHeight = ((View)myWindow.getParent()).getHeight();
		final int diffTop = screenHeight - selectionEndY;
		final int diffBottom = selectionStartY;
		if (diffTop > diffBottom) {
			verticalPosition = diffTop > myWindow.getHeight() + 20
				? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.CENTER_VERTICAL;
		} else {
			verticalPosition = diffBottom > myWindow.getHeight() + 20
				? RelativeLayout.ALIGN_PARENT_TOP : RelativeLayout.CENTER_VERTICAL;
		}

		layoutParams.addRule(verticalPosition);
		myWindow.setLayoutParams(layoutParams);
	}
}
