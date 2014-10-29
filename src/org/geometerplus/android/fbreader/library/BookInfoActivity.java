/*
* FullReader+
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.library;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.geometerplus.android.fbreader.BaseActivity;
import org.geometerplus.android.fbreader.FullReaderActivity;
import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.preferences.EditBookInfoActivity;
import org.geometerplus.fbreader.book.Author;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.book.SeriesInfo;
import org.geometerplus.fbreader.book.Tag;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.network.HtmlUtil;
import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.language.Language;
import org.geometerplus.zlibrary.core.language.ZLLanguageUtil;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.webprestige.fr.customlistview.AllFilesActivity;
import com.fullreader.R;

public class BookInfoActivity extends BaseActivity {
	private static final boolean ENABLE_EXTENDED_FILE_INFO = false;

	public static final String FROM_READING_MODE_KEY = "fbreader.from.reading.mode";

	private final ZLResource myResource = ZLResource.resource("bookInfo");
	private Book myBook;
	private int myResult;
	private boolean myDontReloadBook;

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Thread.setDefaultUncaughtExceptionHandler(
			new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this)
		);
		
		myDontReloadBook = getIntent().getBooleanExtra(FROM_READING_MODE_KEY, false);
		myBook = bookByIntent(getIntent());	
		//Log.d("myBookTitle", myBook.getTitle());
		setContentView(R.layout.book_info);

		myResult = FullReaderActivity.RESULT_DO_NOTHING;
		setResult(myResult, getIntent());
	}

	@Override
	protected void onStart() {
		super.onStart();

		OrientationUtil.setOrientation(this, getIntent());
		if (myBook != null) {
			// we do force language & encoding detection
			myBook.getEncoding();

			setupCover(myBook);
			setupBookInfo(myBook);
			setupAnnotation(myBook);
			setupFileInfo(myBook);
		}

		setupButton(R.id.book_info_button_open, "openBook", new View.OnClickListener() {
			public void onClick(View view) {
				if (myDontReloadBook) {
					finish();
				} else {
					try {
						FullReaderActivity.openBookActivity(BookInfoActivity.this, myBook, null);
					} catch (BookReadingException e) {
						Toast.makeText(getApplicationContext(), "CAN'T OPEN THIS BOOK!!!", Toast.LENGTH_LONG).show();
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		setupButton(R.id.book_info_button_edit, "editInfo", new View.OnClickListener() {
			public void onClick(View view) {
				OrientationUtil.startActivityForResult(
					BookInfoActivity.this,
					new Intent(getApplicationContext(), EditBookInfoActivity.class)
						.putExtra(FullReaderActivity.BOOK_KEY, SerializerUtil.serialize(myBook)),
					1
				);
			}
		});
		findViewById(R.id.book_info_button_reload).setVisibility(View.GONE);
//		setupButton(R.id.book_info_button_reload, "reloadInfo", new View.OnClickListener() {
//			public void onClick(View view) {
//				if (myBook != null) {
//					myBook.reloadInfoFromFile();
//					setupBookInfo(myBook);
//					myDontReloadBook = false;
//					myResult = FullReaderActivity.RESULT_REPAINT;
//					setResult(myResult, intentByBook(myBook));
//				}
//			}
//		});

		final View root = findViewById(R.id.book_info_root);
		root.invalidate();
		root.requestLayout();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		OrientationUtil.setOrientation(this, intent);
	}
	
	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	      case android.R.id.home:
	        finish();
	    }
		return true;
	  }

	public static Intent intentByBook(Book book) {
		return new Intent().putExtra(FullReaderActivity.BOOK_KEY, SerializerUtil.serialize(book));
	}

	public static Book bookByIntent(Intent intent) {
		if(intent == null) {
			Log.d("intent is null", "intent is null");
		}
		return intent != null ?
			SerializerUtil.deserializeBook(intent.getStringExtra(FullReaderActivity.BOOK_KEY)) : null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		final Book book = bookByIntent(data);
		if (book != null) {
			myBook = book;
			setupBookInfo(book);
			myDontReloadBook = false;
		}

		myResult = FullReaderActivity.RESULT_REPAINT;
		setResult(myResult, data);
	}

	private Button findButton(int buttonId) {
		return (Button)findViewById(buttonId);
	}

	private void setupButton(int buttonId, String resourceKey, View.OnClickListener listener) {
		final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		final Button button = findButton(buttonId);
		int theme = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
		final int color;
		final int colorText;
        switch(theme){
        case IConstants.THEME_MYBLACK:
        	button.setBackgroundResource(R.drawable.theme_black_bg_for_ok_cancel);
        	break;
        case IConstants.THEME_LAMINAT:
        	button.setBackgroundResource(R.drawable.theme_laminat_bg_for_ok_cancel);
        	break;
        default:
        	button.setBackgroundResource(R.drawable.theme_redtree_bg_for_ok_cancel);
        	break;
        }
		button.setText(buttonResource.getResource(resourceKey).getValue());
		button.setOnClickListener(listener);
	}

	private void setupInfoPair(int id, String key, CharSequence value) {
		setupInfoPair(id, key, value, 0);
	}

	private void setupInfoPair(int id, String key, CharSequence value, int param) {
		final LinearLayout layout = (LinearLayout)findViewById(id);
		if (value == null || value.length() == 0) {
			layout.setVisibility(View.GONE);
			return;
		}
		layout.setVisibility(View.VISIBLE);
		((TextView)layout.findViewById(R.id.book_info_key)).setText(myResource.getResource(key).getValue(param));
		((TextView)layout.findViewById(R.id.book_info_value)).setText(value);
	}

	private void setupCover(Book book) {
		final ImageView coverView = (ImageView)findViewById(R.id.book_cover);

		final DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		final int maxHeight = metrics.heightPixels * 2 / 3;
		final int maxWidth = maxHeight * 2 / 3;
		
		coverView.setVisibility(View.GONE);
		coverView.setImageDrawable(null);
		
		final ZLImage image = BookUtil.getCover(book);
		
		if (image == null) {
			return;
		}

		if (image instanceof ZLLoadableImage) {
			final ZLLoadableImage loadableImage = (ZLLoadableImage)image;
			if (!loadableImage.isSynchronized()) {
				loadableImage.synchronize();
			}
		}
		final ZLAndroidImageData data =
			((ZLAndroidImageManager)ZLAndroidImageManager.Instance()).getImageData(image);
		if (data == null) {
			return;
		}

		final Bitmap coverBitmap = data.getBitmap(2 * maxWidth, 2 * maxHeight);
		if (coverBitmap == null) {
			return;
		}

		coverView.setVisibility(View.VISIBLE);
		coverView.getLayoutParams().width = maxWidth;
		coverView.getLayoutParams().height = maxHeight;
		coverView.setImageBitmap(coverBitmap);
	}

	private void setupBookInfo(Book book) {
		((TextView)findViewById(R.id.book_info_title)).setText(myResource.getResource("bookInfo").getValue());
		
		setupInfoPair(R.id.book_title, "title", book.getTitle());
		
		final StringBuilder buffer = new StringBuilder();
		final List<Author> authors = book.authors();
		for (Author a : authors) {
			if (buffer.length() > 0) {
				buffer.append(", ");
			}
			buffer.append(a.DisplayName);
		}
		setupInfoPair(R.id.book_authors, "authors", buffer, authors.size());
		
		final SeriesInfo series = book.getSeriesInfo();
		setupInfoPair(R.id.book_series, "series", series == null ? null : series.Series.getTitle());
		String seriesIndexString = null;
		if (series != null && series.Index != null) {
			seriesIndexString = series.Index.toString();
		}
		setupInfoPair(R.id.book_series_index, "indexInSeries", seriesIndexString);
		
		buffer.delete(0, buffer.length());
		final HashSet<String> tagNames = new HashSet<String>();
		for (Tag tag : book.tags()) {
			if (!tagNames.contains(tag.Name)) {
				if (buffer.length() > 0) {
					buffer.append(", ");
				}
				buffer.append(tag.Name);
				tagNames.add(tag.Name);
			}
		}
		setupInfoPair(R.id.book_tags, "tags", buffer, tagNames.size());
		String language = book.getLanguage();
		if (!ZLLanguageUtil.languageCodes().contains(language)) {
			language = Language.OTHER_CODE;
		}
		setupInfoPair(R.id.book_language, "language", new Language(language).Name);
	}

	private void setupAnnotation(Book book) {
		final TextView titleView = (TextView)findViewById(R.id.book_info_annotation_title);
		final TextView bodyView = (TextView)findViewById(R.id.book_info_annotation_body);
		final String annotation = BookUtil.getAnnotation(book);
		if (annotation == null) {
			titleView.setVisibility(View.GONE);
			bodyView.setVisibility(View.GONE);
		} else {
			titleView.setText(myResource.getResource("annotation").getValue());
			bodyView.setText(HtmlUtil.getHtmlText(annotation));
			bodyView.setMovementMethod(new LinkMovementMethod());
			bodyView.setTextColor(ColorStateList.valueOf(bodyView.getTextColors().getDefaultColor()));
		}
	}

	private void setupFileInfo(Book book) {
		((TextView)findViewById(R.id.file_info_title)).setText(myResource.getResource("fileInfo").getValue());

		setupInfoPair(R.id.file_name, "name", book.File.getPath());
		if (ENABLE_EXTENDED_FILE_INFO) {
			setupInfoPair(R.id.file_type, "type", book.File.getExtension());

			final ZLPhysicalFile physFile = book.File.getPhysicalFile();
			final File file = physFile == null ? null : physFile.javaFile();
			if (file != null && file.exists() && file.isFile()) {
				setupInfoPair(R.id.file_size, "size", formatSize(file.length()));
				setupInfoPair(R.id.file_time, "time", formatDate(file.lastModified()));
			} else {
				setupInfoPair(R.id.file_size, "size", null);
				setupInfoPair(R.id.file_time, "time", null);
			}
		} else {
			setupInfoPair(R.id.file_type, "type", null);
			setupInfoPair(R.id.file_size, "size", null);
			setupInfoPair(R.id.file_time, "time", null);
		}
	}

	private String formatSize(long size) {
		if (size <= 0) {
			return null;
		}
		final int kilo = 1024;
		if (size < kilo) { // less than 1 kilobyte
			return myResource.getResource("sizeInBytes").getValue((int)size).replaceAll("%s", String.valueOf(size));
		}
		final String value;
		if (size < kilo * kilo) { // less than 1 megabyte
			value = String.format("%.2f", ((float)size) / kilo);
		} else {
			value = String.valueOf(size / kilo);
		}
		return myResource.getResource("sizeInKiloBytes").getValue().replaceAll("%s", value);
	}

	private String formatDate(long date) {
		if (date == 0) {
			return null;
		}
		return DateFormat.getDateTimeInstance().format(new Date(date));
	}
}
