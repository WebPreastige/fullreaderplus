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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.geometerplus.android.fbreader.fragments.FragmentQuotesBooks;
import org.geometerplus.android.fbreader.fragments.FragmentQuotesBooks.OnQuoteListClick;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.DateType;
import org.geometerplus.fbreader.book.ITextMarker;
import org.geometerplus.fbreader.book.Quote;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.fbreader.ReaderApp;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.fullreader.R;

public class QuotesFragmentActivity extends SherlockFragmentActivity implements OnQuoteListClick,OnSharedPreferenceChangeListener{//MenuItem.OnMenuItemClickListener {
	private static final int OPEN_ITEM_ID = 0;
	private static final int EDIT_ITEM_ID = 1;
	private static final int DELETE_ITEM_ID = 2;
	private static final int SHARE_ITEM_ID = 3;
	public static final int FBSHARE_ITEM_ID = 4;

	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private volatile Book myBook;

	private final Comparator<Quote> myComparator = new Quote.ByTimeComparator();

	private volatile QuotesAdapter myThisBookAdapter;
	private volatile QuotesAdapter myAllBooksAdapter;
	private volatile QuotesAdapter mySearchResultsAdapter;

	private final ZLResource myResource = ZLResource.resource("quotesView");
	private final ZLStringOption myQuoteSearchPatternOption =
			new ZLStringOption("QuoteSearch", "Pattern", "");
	private MyTabListener myTabListener;
	public FragmentQuotesBooks fragAll;
	public FragmentQuotesBooks fragThis;
	private Facebook facebook;
	
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

		//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		//
		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(null);
		//
		//		final TabHost host = getTabHost();
		//		LayoutInflater.from(this).inflate(R.layout.quotes, host.getTabContentView(), true);


		myBook = SerializerUtil.deserializeBook(getIntent().getStringExtra(FullReaderActivity.BOOK_KEY));

		myTabListener = new MyTabListener();
        getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if(loadCurrentLanguage().equals("en")) {
			getSupportActionBar().setSubtitle("Quotes");
    	} else if(loadCurrentLanguage().equals("ru")) {
    		getSupportActionBar().setSubtitle("Цитаты");
    	} else if(loadCurrentLanguage().equals("fr")) {
    		getSupportActionBar().setSubtitle("Citations");
    	} else if(loadCurrentLanguage().equals("de")) {
    		getSupportActionBar().setSubtitle("Zitate");
    	} else if(loadCurrentLanguage().equals("uk")) {
    		getSupportActionBar().setSubtitle("Цитати");
    	} else {
    		if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
    			getSupportActionBar().setSubtitle("Цитаты");
			} else  if(Locale.getDefault().getDisplayLanguage().equals("українська")){
    			getSupportActionBar().setSubtitle("Цитати");
			} else {
				getSupportActionBar().setSubtitle("Quotes");
			}
    	}
		
	}
	
	public String loadCurrentLanguage() {
	    SharedPreferences sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
	    return sPref.getString("curLanguage", "");
    }
	
	//Change The Backgournd Color of Tabs
		public void setTabColor(TabHost tabhost) {

		    for(int i=0;i<tabhost.getTabWidget().getChildCount();i++)
		        tabhost.getTabWidget().getChildAt(i).setBackgroundColor(Color.CYAN); //unselected

		    if(tabhost.getCurrentTab()==0)
		           tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundColor(Color.RED); //1st tab selected
		    else
		           tabhost.getTabWidget().getChildAt(tabhost.getCurrentTab()).setBackgroundColor(Color.BLUE); //2nd tab selected
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
					final List<Quote> thisBookQuotes =
							myCollection.quotesForBook(myBook, id, 20);
					if (thisBookQuotes.isEmpty()) {
						break;
					} else {
						id = thisBookQuotes.get(thisBookQuotes.size() - 1).getId() + 1;
					}
					myThisBookAdapter.addAll(thisBookQuotes);
					myAllBooksAdapter.addAll(thisBookQuotes);
				}
			}
			id = 0;
			while (true) {
				final List<Quote> allQuotes = myCollection.quotes(id, 20);
				if (allQuotes.isEmpty()) {
					break;
				} else {
					id = allQuotes.get(allQuotes.size() - 1).getId() + 1;
				}
				myAllBooksAdapter.addAll(allQuotes);
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
				myAllBooksAdapter = new QuotesAdapter(false);
				fragAll = new FragmentQuotesBooks();
				fragAll.setAdapter(myAllBooksAdapter);

				if (myBook != null) {
					myThisBookAdapter = new QuotesAdapter(false);
					setTab("thisBook");
					fragThis = new FragmentQuotesBooks();
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
		myQuoteSearchPatternOption.setValue(pattern);

		final LinkedList<Quote> quotes = new LinkedList<Quote>();
		pattern = pattern.toLowerCase();
		for (Quote b : myAllBooksAdapter.quotes()) {
			if (MiscUtil.matchesIgnoreCase(b.getText(), pattern)) {
				quotes.add(b);
			}
		}
		if (!quotes.isEmpty()) {
			showSearchResultsTab(quotes);
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
		startSearch(myQuoteSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	void showSearchResultsTab(LinkedList<Quote> results) {
		if (mySearchResultsAdapter == null) {
			mySearchResultsAdapter = new QuotesAdapter( false);
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

	//	@Override
	//	public boolean onContextItemSelected(MenuItem item) {
	//		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
	//		final ListView view = null;// = (ListView)getTabHost().getCurrentView();
	//		final Quote quote = ((QuotesAdapter)view.getAdapter()).getItem(position);
	//		switch (item.getItemId()) {
	//		case OPEN_ITEM_ID:
	//			gotoQuote(quote);
	//			return true;
	//		case EDIT_ITEM_ID:
	//			final Intent intent = new Intent(this, QuoteEditActivity.class);
	//			OrientationUtil.startActivityForResult(this, intent, 1);
	//			// TODO: implement
	//			return true;
	//		}
	//		}
	//		return super.onContextItemSelected(item);
	//	}

	public void addQuote() {
		final Quote quote =
				(Quote) SerializerUtil.deserializeQuote(getIntent().getStringExtra(FullReaderActivity.BOOKMARK_KEY));
		if (quote != null) {
			myCollection.saveQuote(quote);
			myThisBookAdapter.add((Quote)quote);
			myAllBooksAdapter.add((Quote)quote);
		}
	}

	public void gotoQuote(Quote quote) {
		quote.markAsAccessed();
		myCollection.saveQuote(quote);
		final Book book = myCollection.getBookById(quote.getBookId());
		if (book != null) {
			//FullReaderActivity.openBookActivity(this, book, quote);
			ReaderApp.quotesCIndex = quote.CharIndex;
			ReaderApp.quotesEIndex = quote.ElementIndex;
			ReaderApp.quotesPIndex = quote.ParagraphIndex;
			ReaderApp.isOpenFromQuotes = true;
			try {
				FullReaderActivity.openBookActivity(this, myBook, null);
			} catch (BookReadingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			UIUtil.showErrorMessage(this, "cannotOpenBook");
		}
	}
	
	public final class QuotesAdapter extends BaseAdapter{// implements View.OnCreateContextMenuListener {
		private final List<Quote> myQuotes =
				Collections.synchronizedList(new LinkedList<Quote>());
		private final boolean myShowAddQuoteItem;

		QuotesAdapter(boolean showAddQuoteItem) {
			myShowAddQuoteItem = showAddQuoteItem;
		}

		public List<Quote> quotes() {
			return Collections.unmodifiableList(myQuotes);
		}

		public void addAll(final List<Quote> quotes) {
			runOnUiThread(new Runnable() {
				public void run() {
					synchronized (myQuotes) {
						for (Quote b : quotes) {
							final int position = Collections.binarySearch(myQuotes, b, myComparator);
							if (position < 0) {
								myQuotes.add(- position - 1, b);
							}
						}
					}
					notifyDataSetChanged();
				}
			});
		}

		public void add(final Quote b) {
			runOnUiThread(new Runnable() {
				public void run() {
					synchronized (myQuotes) {
						final int position = Collections.binarySearch(myQuotes, b, myComparator);
						if (position < 0) {
							myQuotes.add(- position - 1, b);
						}
					}
					notifyDataSetChanged();
				}
			});
		}

		public void remove(final Quote b) {
			runOnUiThread(new Runnable() {
				public void run() {
					myQuotes.remove(b);
					notifyDataSetChanged();
				}
			});
		}

		public void clear() {
			runOnUiThread(new Runnable() {
				public void run() {
					myQuotes.clear();
					notifyDataSetChanged();
				}
			});
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.bookmark_item, parent, false);
			final TextView textView = (TextView)view.findViewById(R.id.bookmark_item_text);
			final TextView bookTitleView = (TextView)view.findViewById(R.id.bookmark_item_booktitle);
			LinearLayout layout = (LinearLayout)findViewById(R.layout.bookmark_item);
			textView.setTextColor(Color.BLACK);
			bookTitleView.setTextColor(Color.argb(60, 0, 0, 0));
			
			final ITextMarker quote = getItem(position);
			if (quote == null) {
				textView.setText(myResource.getResource("new").getValue());
				bookTitleView.setVisibility(View.GONE);
			} else {
				textView.setText(quote.getText());
				try{
					final Book book = myCollection.getBookById(quote.getBookId());
					if(book.authors().size()>0)
						textView.setText(quote.getText()+"\n"+quote.getBookTitle()+"("+book.authors().get(0).DisplayName+")");
					else
						textView.setText(quote.getText()+"\n");
					bookTitleView.setText(new SimpleDateFormat("dd.MM.yy").format(quote.getDate(DateType.Creation)));
				} catch(NullPointerException e) {
					textView.setText(quote.getText()+"\n");
				}
			}		
		    
			return view;
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

		public final Quote getItem(int position) {
			if (myShowAddQuoteItem) {
				--position;
			}
			return (position >= 0) ? myQuotes.get(position) : null;
		}

		public final int getCount() {
			return myShowAddQuoteItem ? myQuotes.size() + 1 : myQuotes.size();
		}

	}

	private class MyTabListener implements ActionBar.TabListener
	{

		@Override
		public void onTabSelected(Tab tab,
				android.support.v4.app.FragmentTransaction ft) {
			if(tab.getPosition()==0)
			{
				if(fragThis!=null) {
					ft.replace(android.R.id.content, fragThis);					
					Log.d("TAB SELECTION QUOTES: ", "tab selected!!!!!");
				}
					
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

	@Override
	public void shareQuote(Quote quote) {
		final Intent intentShare = new Intent(android.content.Intent.ACTION_SEND);
		intentShare.setType("text/plain");
		//intentShare.putExtra(android.content.Intent.EXTRA_SUBJECT,);
		final Book book = myCollection.getBookById(quote.getBookId());
		String text;
		try{
			//if(book.authors().get(0)!=null && book.authors().get(0).DisplayName != null)
			text = "\""+quote.getText()+"\""+" (c) "+quote.getBookTitle()+", "+book.authors().get(0).DisplayName;
		}catch(IndexOutOfBoundsException e){
			///else
			text = "\""+quote.getText()+"\""+" (c) "+quote.getBookTitle();
		}
		intentShare.putExtra(android.content.Intent.EXTRA_TEXT, text);
		startActivity(Intent.createChooser(intentShare, null));
	}

	@Override
	public void deleteQuote(Quote quote) {
		myCollection.deleteTextMarker(quote); 
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

	@Override
	public void shareFbQuote(final Quote quote) {
		facebook = new Facebook(IConstants.FACEBOOK_APP_ID);
		final String[] permissions = {"publish_stream"};
		facebook.authorize(QuotesFragmentActivity.this, permissions, new DialogListener () {
			@Override
			public void onComplete(Bundle values) {
				Toast.makeText(QuotesFragmentActivity.this, "Authorization successful", Toast.LENGTH_SHORT).show();
				new Thread(){ public void run() {
					postMassage(quote);
				}}.start();		
			}

			@Override
			public void onFacebookError(FacebookError e) {
				Toast.makeText(QuotesFragmentActivity.this, "Facebook error, try again later", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onError(DialogError e) {
				Toast.makeText(QuotesFragmentActivity.this, "Error, try again later", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCancel() {
				//���� ������� ������� �� �����������, ��������, ������ � SDK
				Toast.makeText(QuotesFragmentActivity.this, "Authorization canceled", Toast.LENGTH_SHORT).show();
			}

		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}


	private void postMassage(Quote quote) {
		Bundle parameters = new Bundle();
		final Book book = myCollection.getBookById(quote.getBookId());
		String text;
		try{
			//if(book.authors().get(0)!=null && book.authors().get(0).DisplayName != null)
			text = "\""+quote.getText()+"\""+" (c) "+quote.getBookTitle()+", "+book.authors().get(0).DisplayName;
		}catch(IndexOutOfBoundsException e){
			///else
			text = "\""+quote.getText()+"\""+" (c) "+quote.getBookTitle();
		}
		parameters.putString("message", text);
		try {
			facebook.request("me/feed", parameters, "POST");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
