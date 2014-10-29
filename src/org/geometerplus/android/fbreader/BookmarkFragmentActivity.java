/*
 * 	FullReader+
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

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.geometerplus.android.fbreader.fragments.FragmentBookmarks;
import org.geometerplus.android.fbreader.fragments.FragmentBookmarks.OnBookmarkListClick;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.DateType;
import org.geometerplus.fbreader.book.ITextMarker;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MiscUtil;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.MenuItem;
import com.fullreader.R;

public class BookmarkFragmentActivity extends BaseActivity implements OnBookmarkListClick, OnSharedPreferenceChangeListener{//MenuItem.OnMenuItemClickListener {
	private static final int OPEN_ITEM_ID = 0;
	private static final int EDIT_ITEM_ID = 1;
	private static final int DELETE_ITEM_ID = 2;
	private static final int SHARE_ITEM_ID = 3;
	private SharedPreferences sPref;
	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private volatile Book myBook;
	
	private final Comparator<Bookmark> myComparator = new Bookmark.ByTimeComparator();
	
	private volatile BookmarksAdapter myThisBookAdapter;
	private volatile BookmarksAdapter myAllBooksAdapter;
	private volatile BookmarksAdapter mySearchResultsAdapter;
	
	private final ZLResource myResource = ZLResource.resource("bookmarksView");
	private final ZLStringOption myBookmarkSearchPatternOption =
			new ZLStringOption("Bookmarksearch", "Pattern", "");
	private MyTabListener myTabListener;
	public FragmentBookmarks fragAll;
	public FragmentBookmarks fragThis;

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));

		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

		int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
		switch(theme){
		case IConstants.THEME_MYBLACK:
			setTheme(R.style.Theme_myBlack);
			break;
		case IConstants.THEME_LAMINAT:
			setTheme(R.style.Theme_Laminat);
			break;
		case IConstants.THEME_REDTREE:
			setTheme(R.style.Theme_Redtree);
			break;

		}
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(null);

		myBook = SerializerUtil.deserializeBook(getIntent().getStringExtra(FullReaderActivity.BOOK_KEY));

		myTabListener = new MyTabListener();
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setSubtitle(ZLResource.resource("menu").getResource("bookmarks").getValue());

	}

	private void setTab(String tag) {
		ActionBar bar =  getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		ActionBar.Tab tab1 = bar.newTab();
		final String label = myResource.getResource(tag).getValue();
		tab1.setText(label);
		tab1.setTabListener(myTabListener);
		bar.addTab(tab1);
	}

	private class Initializer implements Runnable {
		public void run() {
			long id = 0;
			if (myBook != null) {
				while (true) {
					final List<Bookmark> thisBookBookmarks =
							myCollection.bookmarksForBook(myBook, id, 20);
					if (thisBookBookmarks.isEmpty()) {
						break;
					} else {
						id = thisBookBookmarks.get(thisBookBookmarks.size() - 1).getId() + 1;
					}
					myThisBookAdapter.addAll(thisBookBookmarks);
					myAllBooksAdapter.addAll(thisBookBookmarks);
				}
			}
			id = 0;
			while (true) {
				final List<Bookmark> allBookmarks = myCollection.bookmarks(id, 20);
				if (allBookmarks.isEmpty()) {
					break;
				} else {
					id = allBookmarks.get(allBookmarks.size() - 1).getId() + 1;
				}
				myAllBooksAdapter.addAll(allBookmarks);
			}
			runOnUiThread(new Runnable() {
				public void run() {
					setProgressBarIndeterminateVisibility(false);
				}
			});
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		runOnUiThread(new Runnable() {
			public void run() {
				setProgressBarIndeterminateVisibility(true);
			}
		});

		myCollection.bindToService(this, new Runnable() {
			public void run() {
				if (myAllBooksAdapter != null) {
					return;
				}
				myAllBooksAdapter = new BookmarksAdapter(false);
				fragAll = new FragmentBookmarks();
				fragAll.setAdapter(myAllBooksAdapter);

				if (myBook != null) {
					myThisBookAdapter = new BookmarksAdapter(true);
					setTab("thisBook");
					fragThis = new FragmentBookmarks();
					fragThis.setAdapter(myThisBookAdapter);

					android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					ft.add(android.R.id.content,  fragThis);
					ft.commit();
				}else{
					android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					ft.add(android.R.id.content,  fragAll);
					ft.commit();
				}

				setTab("allBooks");

				//getSupportActionBar().selectTab(getSupportActionBar().getTabAt(1));
				//getSupportActionBar().setSelectedNavigationItem(1);
				
				new Thread(new Initializer()).start();
			}
		});

		OrientationUtil.setOrientation(this, getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) {
		OrientationUtil.setOrientation(this, intent);

		if (!Intent.ACTION_SEARCH.equals(intent.getAction())) {
			return;
		}
		String pattern = intent.getStringExtra(SearchManager.QUERY);
		myBookmarkSearchPatternOption.setValue(pattern);

		final LinkedList<Bookmark> bookmarks = new LinkedList<Bookmark>();
		pattern = pattern.toLowerCase();
		for (Bookmark b : myAllBooksAdapter.bookmarks()) {
			if (MiscUtil.matchesIgnoreCase(b.getText(), pattern)) {
				bookmarks.add(b);
			}
		}
		if (!bookmarks.isEmpty()) {
			showSearchResultsTab(bookmarks);
		} else {
			UIUtil.showErrorMessage(this, "quoteNotFound");
		}
	}

	@Override
	protected void onStop() {
		myCollection.unbind();
		super.onStop();
	}

	//	@Override
	//	public boolean onCreateOptionsMenu(Menu menu) {
	//		super.onCreateOptionsMenu(menu);
	//		final MenuItem item = menu.add(
	//				0, 1, Menu.NONE,
	//				myResource.getResource("menu").getResource("search").getValue()
	//				);
	//		item.setOnMenuItemClickListener(this);
	//		item.setIcon(R.drawable.ic_menu_search);
	//		return true;
	//	}

	@Override
	public boolean onSearchRequested() {
		startSearch(myBookmarkSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	void showSearchResultsTab(LinkedList<Bookmark> results) {
		if (mySearchResultsAdapter == null) {
			mySearchResultsAdapter = new BookmarksAdapter( false);
		} else {
			mySearchResultsAdapter.clear();
		}
		mySearchResultsAdapter.addAll(results);
		//getTabHost().setCurrentTabByTag("found");
	}

	//	public boolean onMenuItemClick(MenuItem item) {
	//		switch (item.getItemId()) {
	//		case 1:
	//			return onSearchRequested();
	//		default:
	//			return true;
	//		}
	//	}


	public void addBookmark() {
		final Bookmark bookmark =
				SerializerUtil.deserializeBookmark(getIntent().getStringExtra(FullReaderActivity.BOOKMARK_KEY));
		if (bookmark != null) {
			myCollection.saveBookmark(bookmark);
			myThisBookAdapter.add((Bookmark)bookmark);
			myAllBooksAdapter.add((Bookmark)bookmark);
		}
	}

	public void gotoBookmark(Bookmark bookmark) {
		bookmark.markAsAccessed();
		myCollection.saveBookmark(bookmark);
		final Book book = myCollection.getBookById(bookmark.getBookId());
		if (book != null) {
			try {
				FullReaderActivity.openBookActivity(this, book, bookmark);
			} catch (BookReadingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			UIUtil.showErrorMessage(this, "cannotOpenBook");
		}
	}

	public final class BookmarksAdapter extends BaseAdapter  {
		private final List<Bookmark> myBookmarks =
				Collections.synchronizedList(new LinkedList<Bookmark>());
		private final boolean myShowAddBookmarkItem;

		BookmarksAdapter( boolean showAddBookmarkItem) {
			myShowAddBookmarkItem = showAddBookmarkItem;
		}

		public List<Bookmark> bookmarks() {
			return Collections.unmodifiableList(myBookmarks);
		}

		public void addAll(final List<Bookmark> bookmarks) {
			runOnUiThread(new Runnable() {
				public void run() {
					synchronized (myBookmarks) {
						for (Bookmark b : bookmarks) {
							final int position = Collections.binarySearch(myBookmarks, b, myComparator);
							if (position < 0) {
								myBookmarks.add(- position - 1, b);
							}
						}
					}
					notifyDataSetChanged();
				}
			});
		}

		public void add(final Bookmark b) {
			runOnUiThread(new Runnable() {
				public void run() {
					synchronized (myBookmarks) {
						final int position = Collections.binarySearch(myBookmarks, b, myComparator);
						if (position < 0) {
							myBookmarks.add(- position - 1, b);
						}
					}
					notifyDataSetChanged();
				}
			});
		}

		public void remove(final Bookmark b) {
			runOnUiThread(new Runnable() {
				public void run() {
					myBookmarks.remove(b);
					notifyDataSetChanged();
				}
			});
		}

		public void clear() {
			runOnUiThread(new Runnable() {
				public void run() {
					myBookmarks.clear();
					notifyDataSetChanged();
				}
			});
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			TextView textView = null;
			try {
				view = (convertView != null) ? convertView :
					LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
				textView = (TextView)view.findViewById(R.id.bookmark_item_text);
				final TextView bookTitleView = (TextView)view.findViewById(R.id.bookmark_item_booktitle);
	
				textView.setTextColor(Color.BLACK);
				bookTitleView.setTextColor(Color.argb(60, 0, 0, 0));
	
				final ITextMarker bookmark = getItem(position);
				if (bookmark == null) {
					textView.setText(myResource.getResource("new").getValue());
	//				bookTitleView.setVisibility(View.GONE);
				} else {
					//textView.setText(bookmark.getText());
	//				if (myShowAddBookmarkItem) {
	//					bookTitleView.setVisibility(View.GONE);
	//				} else {
						//bookTitleView.setVisibility(View.VISIBLE);
						final Book book = myCollection.getBookById(bookmark.getBookId());
						if(book.authors().size()>0)
							textView.setText(bookmark.getBookTitle()+"("+book.authors().get(0).DisplayName+")");
						else
							textView.setText(bookmark.getBookTitle());
	//				}
						bookTitleView.setText(new SimpleDateFormat("dd.MM.yy").format(bookmark.getDate(DateType.Creation)));
				}
			} catch(NullPointerException e) {
				if(loadCurrentLanguage().equals("system")) {
					if(Locale.getDefault().getDisplayLanguage().equals("русский") || Locale.getDefault().getDisplayLanguage().equals("українська")) {
						textView.setText("Книга удалена");
					} else {
						textView.setText("This book was removed.");
					}
				}
				if(loadCurrentLanguage().equals("en")){
					textView.setText("This book was removed.");
				}
				if(loadCurrentLanguage().equals("de")){
					textView.setText("Buch gelöscht.");
				}
				if(loadCurrentLanguage().equals("fr")){
					textView.setText("Réserver supprimés.");
				}
				if(loadCurrentLanguage().equals("uk")){
					textView.setText("Книга видалена.");
				}
				if(loadCurrentLanguage().equals("ru")){
					textView.setText("Книга удалена");
				}
			}

			return view;
		}
			
		public String loadCurrentLanguage() {
			   sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
			   return sPref.getString("curLanguage", "");
	    }

		public final boolean areAllItemsEnabled() {
			return true;
		}

		public final boolean isEnabled(int position) {
			return true;
		}

		public final long getItemId(int position) {
			return position;
		}

		public final Bookmark getItem(int position) {
			if (myShowAddBookmarkItem) {
				--position;
			}
			return (position >= 0) ? myBookmarks.get(position) : null;
		}

		public final int getCount() {
			return myShowAddBookmarkItem ? myBookmarks.size() + 1 : myBookmarks.size();
		}

	}



	private class MyTabListener implements ActionBar.TabListener
	{

		@Override
		public void onTabSelected(Tab tab,
				android.support.v4.app.FragmentTransaction ft) {
			if(tab.getPosition()==0)
			{
				if(fragThis!=null)
					ft.replace(android.R.id.content, fragThis);
			}
			else
			{
				if(fragAll!=null)
					ft.replace(android.R.id.content, fragAll);
			}
		}
		@Override
		public void onTabUnselected(Tab tab,
				android.support.v4.app.FragmentTransaction ft) {

		}
		@Override
		public void onTabReselected(Tab tab,
				android.support.v4.app.FragmentTransaction ft) {
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(IConstants.THEME_PREF)){
			recreatethis();
		}     

	}

	public void recreatethis()
	{
		if (android.os.Build.VERSION.SDK_INT >= 11)
		{
			super.recreate();
		}
		else
		{
			startActivity(getIntent());
			finish();
		}
	}
	
	public String loadCurrentLanguage() {
	    sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
	    return sPref.getString("curLanguage", "");
	}

	@Override
	public void deleteBookmark(Bookmark quote) {
		Log.d("myCollection size", String.valueOf(myCollection.size()));
		if(loadCurrentLanguage().equals("en")){
			Toast.makeText(getApplicationContext(), "Bookmark deleted.", Toast.LENGTH_LONG).show();
		} else if(loadCurrentLanguage().equals("de")){
			Toast.makeText(getApplicationContext(), "Bookmark gelöscht.", Toast.LENGTH_LONG).show();
		} else if(loadCurrentLanguage().equals("fr")){
			Toast.makeText(getApplicationContext(), "Bookmark supprimé.", Toast.LENGTH_LONG).show();
		} else if(loadCurrentLanguage().equals("uk")){
			Toast.makeText(getApplicationContext(), "Закладка вилучена.", Toast.LENGTH_LONG).show();
		} else if(loadCurrentLanguage().equals("ru")){
			Toast.makeText(getApplicationContext(), "Закладка удалена.", Toast.LENGTH_LONG).show();
		} else {
			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
				Toast.makeText(getApplicationContext(), "Закладка удалена.", Toast.LENGTH_LONG).show();
			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
				Toast.makeText(getApplicationContext(), "Закладка вилучена.", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), "Bookmark deleted.", Toast.LENGTH_LONG).show();
			}
		}
		
		myCollection.deleteTextMarker(quote);
		Log.d("myCollection size", String.valueOf(myCollection.size()));
		if (myThisBookAdapter != null) {
			myThisBookAdapter.remove(quote);
		}
		if (myAllBooksAdapter != null) {
			myAllBooksAdapter.remove(quote);
		}
		if (mySearchResultsAdapter != null) {
			mySearchResultsAdapter.remove(quote);
		}
	}
    
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
        case android.R.id.home:
        	onBackPressed();
//        	onKeyDown(KeyEvent.KEYCODE_BACK, null);
//        	startActivity(new Intent(this, StartScreenActivity.class));
//        	finish();
            break;
        }
        return true;
    }
}
