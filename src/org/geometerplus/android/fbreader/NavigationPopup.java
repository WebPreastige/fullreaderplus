/*
* FullReader+
   Copyright 2013-2014 Viktoriya Bilyk

   Original FBreader code 
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.ReaderApp;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fullreader.R;

final class NavigationPopup extends PopupPanel {
	final static String ID = "NavigationPopup";
	
	public int pagesCount;

	private volatile boolean myIsInProgress;
	private boolean isTextAdded = false;

	NavigationPopup(ReaderApp fbReader) {
		super(fbReader);
	}

	public void runNavigation() {
		if (myWindow == null || myWindow.getVisibility() == View.GONE) {
			myIsInProgress = false;
			initPosition();
			Application.showPopup(ID);
		}
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	protected void show_() {
		super.show_();
		if (myWindow != null) {
			setupNavigation(myWindow);
		}
	}

	@Override
	protected void update() {
		if (!myIsInProgress && myWindow != null) {
			setupNavigation(myWindow);
		}
	}

	@Override
	public void createControlPanel(FullReaderActivity activity, RelativeLayout root) {
		if (myWindow != null && activity == myWindow.getActivity()) {
			return;
		}

		myWindow = new PopupWindow(activity, root, PopupWindow.Location.Bottom, true);

		LinearLayout layout = null;
		int theme = PreferenceManager.getDefaultSharedPreferences(activity)
				.getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
        switch(theme){
        case IConstants.THEME_MYBLACK:
    		layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.navigate_theme_black, myWindow, false);
        	break;
        case IConstants.THEME_LAMINAT:
    		layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.navigate_theme_laminat, myWindow, false);
        	break;
        case IConstants.THEME_REDTREE:
    		layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.navigate_theme_redtree, myWindow, false);
        	break;
        }
        
		final SeekBar slider = (SeekBar)layout.findViewById(R.id.book_position_slider);
		final TextView text = (TextView)layout.findViewById(R.id.book_position_text);

		slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			private void gotoPage(int page) {
				final ZLTextView view = getReader().getTextView();
				if (page == 1) {
					view.gotoHome();
				} else {
					view.gotoPage(page);
				}
				getReader().getViewWidget().reset();
				getReader().getViewWidget().repaint();
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				myIsInProgress = false;
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
				myIsInProgress = true;
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					final int page = progress + 1;
					final int pagesNumber = seekBar.getMax() + 1;
					pagesCount = pagesNumber;
					gotoPage(page);
					text.setText(makeProgressText(page, pagesNumber));
				}
			}
		});
		
				
		final EditText pageEdit = (EditText)layout.findViewById(R.id.page_edit);
		Log.d("pages count", String.valueOf(pagesCount));
		final ZLTextView textView = getReader().getTextView();
		final ZLTextView.PagePosition pagePosition = textView.pagePosition();
		pageEdit.setFilters(new InputFilter[]{ new InputFilterMinMax(1, 99999)});
		
		pageEdit.addTextChangedListener(new TextWatcher() {
			int checker = 0;
			private void gotoPage(int page) {
				try {
					final ZLTextView view = getReader().getTextView();
					if (page == 1) {
						view.gotoHome();
					} else {
						view.gotoPage(page);
					}
					getReader().getViewWidget().reset();
					getReader().getViewWidget().repaint();
				} catch(NumberFormatException e) {
					return;
				}
			}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if(checker!=1){    
	               checker = 1;
	               try {
						final int page = Integer.parseInt(pageEdit.getEditableText().toString());
						final ZLTextView textView = getReader().getTextView();
						final ZLTextView.PagePosition pagePosition = textView.pagePosition();
						if(page >= pagePosition.Total) {
							Log.d("PAGE TOTAL: ", String.valueOf(pagePosition.Total));
								pageEdit.setText(String.valueOf(pagePosition.Total).trim());
								pageEdit.setSelection(pageEdit.getText().length());
								gotoPage(pagePosition.Total);
						} else {
							gotoPage(page);
						}
					} catch(NumberFormatException e) {
						return;
					} catch(StackOverflowError e) {
						return;
					}
	            }				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				 checker = 0;
				
			}
			
		});
		
		
		final Button btnOk = (Button)layout.findViewById(android.R.id.button3);
		final Button btnCancel = (Button)layout.findViewById(android.R.id.button1);
		View.OnClickListener listener = new View.OnClickListener() {
			public void onClick(View v) {
				final ZLTextWordCursor position = StartPosition;
				if (v == btnCancel && position != null) {
					getReader().getTextView().gotoPosition(position);
				} else if (v == btnOk) {
					storePosition();
				}
				StartPosition = null;
				Application.hideActivePopup();
				getReader().getViewWidget().reset();
				getReader().getViewWidget().repaint();
			}
		};
		btnOk.setOnClickListener(listener);
		btnCancel.setOnClickListener(listener);
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		btnOk.setText(buttonResource.getResource("ok").getValue());
		btnCancel.setText(buttonResource.getResource("cancel").getValue());
		
//		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//				LinearLayout.LayoutParams.MATCH_PARENT,
//				LinearLayout.LayoutParams.MATCH_PARENT
//			);
		
		LayoutParams params = myWindow.getLayoutParams();
		params.width = LayoutParams.MATCH_PARENT;
		
		myWindow.addView(layout);
		myWindow.makeTransparent();
	}
	
	private void setupNavigation(PopupWindow panel) {
		final SeekBar slider = (SeekBar)panel.findViewById(R.id.book_position_slider);
		final TextView text = (TextView)panel.findViewById(R.id.book_position_text);
		
		
		final ZLTextView textView = getReader().getTextView();
		final ZLTextView.PagePosition pagePosition = textView.pagePosition();

		if (slider.getMax() != pagePosition.Total - 1 || slider.getProgress() != pagePosition.Current - 1) {
			slider.setMax(pagePosition.Total - 1);
			slider.setProgress(pagePosition.Current - 1);
			text.setText(makeProgressText(pagePosition.Current, pagePosition.Total));
		}
	}

	public class InputFilterMinMax implements InputFilter {

	    private int min, max;

	    public InputFilterMinMax(int min, int max) {
	        this.min = min;
	        this.max = max;
	    }

	    public InputFilterMinMax(String min, String max) {
	        this.min = Integer.parseInt(min);
	        this.max = Integer.parseInt(max);
	    }

	    @Override
	    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {   
	        try {
	            int input = Integer.parseInt(dest.toString() + source.toString());
	            if (isInRange(min, max, input))
	                return null;
	        } catch (NumberFormatException nfe) { }     
	        return "";
	    }

	    private boolean isInRange(int a, int b, int c) {
	        return b > a ? c >= a && c <= b : c >= b && c <= a;
	    }
	}
		
	private String makeProgressText(int page, int pagesNumber) {
		final StringBuilder builder = new StringBuilder();
		builder.append(page);
		builder.append("/");
		builder.append(pagesNumber);
		final TOCTree tocElement = getReader().getCurrentTOCElement();
		if (tocElement != null) {
			builder.append("  ");
			builder.append(tocElement.getText());
		}
		return builder.toString();
	}
}
