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
import java.util.ArrayList;

import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.fbreader.fbreader.ReaderApp;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import afzkl.development.colorpickerview.dialog.ColorPickerDialog;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;


public class SelectionMarkColorAction extends FBAndroidAction {
	private int initColor;
	private int selectedColor;
	private Activity mActivity;
	private ArrayList<SelectedMarkInfo> mInfoList;
	
	SelectionMarkColorAction(FullReaderActivity baseActivity, ReaderApp fbreader) {
		super(baseActivity, fbreader);
		initColor = Color.parseColor("#0099FF");
		mActivity = baseActivity;
		mInfoList = new ArrayList<SelectedMarkInfo>();
	}

	@Override
	protected void run(Object ... params) {
		final FBView fbview = ReaderApp.getTextView();
		final ColorPickerDialog colorDialog = new ColorPickerDialog(mActivity, initColor);
		colorDialog.setAlphaSliderVisible(true);
		colorDialog.setTitle(ZLResource.resource("library").getResource("selectMarker").getValue());
		colorDialog.setAlphaSliderVisible(false);
		colorDialog.setButton(DialogInterface.BUTTON_POSITIVE, ZLResource.resource("library").getResource("saveMarker").getValue(), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				selectedColor = colorDialog.getColor();
				ZLColor color = new ZLColor(selectedColor);
				ZLTextPosition startCur = fbview.getSelectionStartPosition();
				ZLTextPosition endCur = fbview.getSelectionEndPosition();
				SelectedMarkInfo info = new SelectedMarkInfo(-1, color, startCur, endCur);
				// Сохраняем цитату в базе данных
				BaseActivity.addSelectionQuotes(info, fbview);
				fbview.clearSelection();
			}
		});
		colorDialog.setButton(DialogInterface.BUTTON_NEGATIVE, ZLResource.resource("library").getResource("renameCancel").getValue(), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				fbview.clearSelection();
			}
		});
		colorDialog.show();
	}
	
	
	
}