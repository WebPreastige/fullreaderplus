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

package org.geometerplus.android.fbreader.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import org.geometerplus.android.fbreader.FullReaderActivity;
import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.formats.FormatPlugin;
import org.geometerplus.zlibrary.core.encodings.Encoding;
import org.geometerplus.zlibrary.core.language.Language;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;
import com.fullreader.R;

class BookTitlePreference extends ZLStringPreference {
	private final Book myBook;
	private EditText edtTitle;

	BookTitlePreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey);
		myBook = book;
		super.setValue(book.getTitle());
	}

	@Override
	protected void setValue(String value) {
		super.setValue(value);
		myBook.setTitle(value);
		((EditBookInfoActivity)getContext()).updateResult();
        ((EditBookInfoActivity)getContext()).saveBook();
	}
	

	/** Hide the cancel button */
	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);
		builder.setNegativeButton(null, null);
		builder.setPositiveButton(null, null);
	}
	
	@Override
	protected View onCreateDialogView() {
		ScrollView lay = (ScrollView)LayoutInflater.from(getContext()).inflate(R.layout.pref_edit_title, null);
        
		int theme1 = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
		final int color;
		final int colorText;
        switch(theme1){      
        case IConstants.THEME_MYBLACK:
        	color = Color.parseColor("#FFFFFF");
        	if(android.os.Build.VERSION.SDK_INT >= 9 &&  android.os.Build.VERSION.SDK_INT <14)colorText = Color.BLACK;
        	else colorText = Color.WHITE;
        	break;
        case IConstants.THEME_LAMINAT:
        	color = Color.parseColor("#f1dcc2");
        	colorText = Color.BLACK;
        	break;
        default:
        	color = Color.parseColor("#4d2114");
        	colorText = Color.WHITE;
        	break;
        }
        edtTitle = (EditText)lay.findViewById(R.id.message);
        edtTitle.setText(myBook.getTitle());
        
        edtTitle.setTextColor(colorText);
        
        LinearLayout body = (LinearLayout)lay.findViewById(R.id.lo_reminder_body);
        
    	int theme = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
        switch(theme){
        case IConstants.THEME_MYBLACK:
        	body.setBackgroundResource(R.drawable.theme_black_bg);
        	break;
        case IConstants.THEME_LAMINAT:
        	body.setBackgroundResource(R.drawable.theme_laminat_bg);
        	break;
        case IConstants.THEME_REDTREE:
        	body.setBackgroundResource(R.drawable.theme_redtree_bg);
        	break;
        }
        
		Button btnOk = (Button) lay.findViewById(R.id.btn_ok);
		Button btnCancel = (Button) lay.findViewById(R.id.btn_cancel);
		
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setValue(edtTitle.getText().toString());
				//onDialogClosed(true);
				getDialog().dismiss();
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//onDialogClosed(false);
				getDialog().dismiss();
			}
		});

        btnOk.setText(ZLResource.resource("other").getResource("ok").getValue());
        btnCancel.setText(ZLResource.resource("other").getResource("cancel").getValue());
        
        return lay;
	}
	
}

class BookLanguagePreference extends LanguagePreference {
	private final Book myBook;

	private static List<Language> languages() {
		final TreeSet<Language> set = new TreeSet<Language>();
		for (String code : ZLTextHyphenator.Instance().languageCodes()) {
			set.add(new Language(code));
		}
		set.add(new Language(Language.OTHER_CODE));
		return new ArrayList<Language>(set);
	}

	BookLanguagePreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey, languages());
		myBook = book;
		final String language = myBook.getLanguage();
		if (language == null || !setInitialValue(language)) {
			setInitialValue(Language.OTHER_CODE);
		}
	}

	@Override
	protected void init() {
	}

	@Override
	protected void setLanguage(String code) {
		myBook.setLanguage(code.length() > 0 ? code : null);
		((EditBookInfoActivity)getContext()).updateResult();
	}
}

class EncodingPreference extends ZLStringListPreference {
	private final Book myBook;

	EncodingPreference(Context context, ZLResource rootResource, String resourceKey, Book book) {
		super(context, rootResource, resourceKey);
		myBook = book;

		final FormatPlugin plugin;
		try {
			plugin = book.getPlugin();
		} catch (BookReadingException e) {
			return;
		}

		final List<Encoding> encodings =
			new ArrayList<Encoding>(plugin.supportedEncodings().encodings());
		Collections.sort(encodings, new Comparator<Encoding>() {
			public int compare(Encoding e1, Encoding e2) {
				return e1.DisplayName.compareTo(e2.DisplayName);
			}
		});
		final String[] codes = new String[encodings.size()];
		final String[] names = new String[encodings.size()];
		int index = 0;
		for (Encoding e : encodings) {
			//addItem(e.Family, e.Name, e.DisplayName);
			codes[index] = e.Name;
			names[index] = e.DisplayName;
			++index;
		}
		setLists(codes, names);
		if (encodings.size() == 1) {
			setInitialValue(codes[0]);
			setEnabled(false);
		} else {
			final String bookEncoding = book.getEncoding();
			if (bookEncoding != null) {
				setInitialValue(bookEncoding.toLowerCase());
			}
		}
	}

	@Override
	protected void onDialogClosed(boolean result) {
		super.onDialogClosed(result);
		if (result) {
			final String value = getValue();
			if (!value.equalsIgnoreCase(myBook.getEncoding())) {
				myBook.setEncoding(value);
				((EditBookInfoActivity)getContext()).updateResult();
			}
		}
	}
}

public class EditBookInfoActivity extends ZLPreferenceActivity {
	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private volatile boolean myInitialized;

	private Book myBook;

	public EditBookInfoActivity() {
		super("BookInfo");
	}
	
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	      case android.R.id.home:
	        finish();
	    }
		return true;
	  }

	void updateResult() {
		setResult(FullReaderActivity.RESULT_REPAINT, BookInfoActivity.intentByBook(myBook));
	}

    void saveBook() {
            myCollection.bindToService(this, new Runnable() {
                    public void run() {
                            myCollection.saveBook(myBook, false);
                    }
            });
    }
    
	@Override
	protected void init(Intent intent) {
	}

	@Override
	protected void onStart() {
		super.onStart();

		myBook = BookInfoActivity.bookByIntent(getIntent());

		if (myBook == null) {
			finish();
			return;
		}

		myCollection.bindToService(this, new Runnable() {
			public void run() {
				if (myInitialized) {
					return;
				}
				myInitialized = true;

				addPreference(new BookTitlePreference(EditBookInfoActivity.this, Resource, "title", myBook));
				//addPreference(new BookLanguagePreference(EditBookInfoActivity.this, Resource, "language", myBook));
				//addPreference(new EncodingPreference(EditBookInfoActivity.this, Resource, "encoding", myBook));
			}
		});
	}

	@Override
	protected void onStop() {
		myCollection.unbind();
		super.onStop();
	}
}
