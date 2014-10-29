/*
Vudroid
Copyright 2010-2011 Pavel Tiunov

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.vudroid.core;

import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.app.Dialog;
import android.content.Context;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fullreader.R;

public class GoToPageDialog extends Dialog
{
	private final DocumentView documentView;
	private final DecodeService decodeService;

	public GoToPageDialog(final Context context, final DocumentView documentView, final DecodeService decodeService)
	{
		super(context);
		this.documentView = documentView;
		this.decodeService = decodeService;

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		int theme = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
		switch(theme){
		case IConstants.THEME_MYBLACK:
			setContentView(R.layout.gotopage_theme_black);
			break;
		case IConstants.THEME_LAMINAT:
			setContentView(R.layout.gotopage_theme_laminat);
			break;
		case IConstants.THEME_REDTREE:
			setContentView(R.layout.gotopage_theme_redtree);
			break;
		}

		getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);


		TextView title = (TextView)findViewById(R.id.tw_pref_orientation_title);
		title.setText(ZLResource.resource("vudroid").getResource("vu_goto").getValue());
		title.setPadding(10, 10, 10, 10);


		final Button button = (Button) findViewById(R.id.goToButton);
		button.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				goToPageAndDismiss();
			}
		});
		final Button buttonCancel = (Button) findViewById(R.id.cancelButton );
		buttonCancel.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				dismiss();
			}
		});
		final EditText editText = (EditText) findViewById(R.id.pageNumberTextEdit);
		editText.setOnEditorActionListener(new TextView.OnEditorActionListener()
		{
			public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
			{
				if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE)
				{
					goToPageAndDismiss();
					return true;
				}
				return false;
			}
		});

	}

	private void goToPageAndDismiss()
	{
		navigateToPage();
		dismiss();
	}

	private void navigateToPage()
	{
		final EditText text = (EditText) findViewById(R.id.pageNumberTextEdit);

		int pageNumber = 1;
		try{
			pageNumber = Integer.parseInt(text.getText().toString());
		}catch(NumberFormatException e){}
		if (pageNumber < 1 || pageNumber > decodeService.getPageCount())
		{
			Toast.makeText(getContext(), "Page number out of range. Valid range: 1-" + decodeService.getPageCount(), 2000).show();
			return;
		}
		documentView.goToPage(pageNumber-1);
	}
}
