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

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Quote;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.fbreader.ReaderApp;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MiscUtil;

import android.app.ActionBar;
import android.app.SearchManager;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.webprestige.fr.bookmarks.BookmarksActivity;
import com.fullreader.R;

public class QuotesActivity extends TabActivity {
	private static final int OPEN_ITEM_ID = 0;
	private static final int EDIT_ITEM_ID = 1;
	private static final int DELETE_ITEM_ID = 2;
    private static final int SHARE_ITEM_ID = 3;

	
    private AdView adView;
    
	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private volatile Book myBook;
	
	private final Comparator<Quote> myComparator = new Quote.ByTimeComparator();
	
	private volatile QuotesAdapter myThisBookAdapter;
	private volatile QuotesAdapter myAllBooksAdapter;
	private volatile QuotesAdapter mySearchResultsAdapter;
	
	private final ZLResource myResource = ZLResource.resource("quotesView");
	private final ZLStringOption myQuoteSearchPatternOption =
		new ZLStringOption("QuoteSearch", "Pattern", "");
	
	private ListView createTab(String tag, int id) {
		final TabHost host = getTabHost();
		final String label = myResource.getResource(tag).getValue();
		host.addTab(host.newTabSpec(tag).setIndicator(label).setContent(id));
		return (ListView)findViewById(id);
	}
	
	private void initAdMob() {
		AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .addTestDevice("TEST_DEVICE_ID")
            .build();
        adView.loadAd(adRequest);
        adView.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(null);
		
		final TabHost host = getTabHost();
		LayoutInflater.from(this).inflate(R.layout.quotes, host.getTabContentView(), true);
		Log.d("QUOTES!!!!!!!!!!!!!", "asd");
		
		myBook = SerializerUtil.deserializeBook(getIntent().getStringExtra(FullReaderActivity.BOOK_KEY));
		
		ActionBar bar = getActionBar();
        Drawable actionBarBackground = null;  
        int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
        switch(theme){
        case IConstants.THEME_MYBLACK:
            actionBarBackground = getResources().getDrawable( com.fullreader.R.drawable.theme_black_action_bar );
        	break;     
        }
        bar.setBackgroundDrawable(actionBarBackground);
        initAdMob();
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
	
	/*private void initAdMob() {
		adView = new AdView(this, AdSize.BANNER, "use-your-own-id");
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.quotes_activity_admob_place);
		RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, 
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

	    layout.addView(adView, adParams);
	    adView.loadAd(new AdRequest());
	}*/
	
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

				if (myBook != null) {
					myThisBookAdapter = new QuotesAdapter(
						createTab("thisBook", R.id.this_book), true
					);
				} else {
					findViewById(R.id.this_book).setVisibility(View.GONE);
				}
				myAllBooksAdapter = new QuotesAdapter(
					createTab("allBooks", R.id.all_books), false
				);
				final TabHost host = getTabHost();
				host.setOnTabChangedListener(new OnTabChangeListener() {

				    @Override
				    public void onTabChanged(String arg0) {

				        setTabColor(host);
				    }
				     });
				     setTabColor(host);
				findViewById(R.id.search_results).setVisibility(View.GONE);

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

	@Override
	public boolean onSearchRequested() {
		startSearch(myQuoteSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	void showSearchResultsTab(LinkedList<Quote> results) {
		if (mySearchResultsAdapter == null) {
			mySearchResultsAdapter = new QuotesAdapter(
				createTab("found", R.id.search_results), false
			);
		} else {
			mySearchResultsAdapter.clear();
		}
		mySearchResultsAdapter.addAll(results);
		getTabHost().setCurrentTabByTag("found");
	} 

	private void addQuote() {
		final Quote quote =
			(Quote) SerializerUtil.deserializeQuote(getIntent().getStringExtra(FullReaderActivity.BOOKMARK_KEY));
		if (quote != null) {
			myCollection.saveQuote(quote);
			myThisBookAdapter.add((Quote)quote);
			myAllBooksAdapter.add((Quote)quote);
		}
	}
	
	/*private void addQuote() {
		final Quote quote =
			(Quote) SerializerUtil.deserializeQuote(getIntent().getStringExtra(FullReaderActivity.BOOKMARK_KEY));
		if (quote != null) {
			myCollection.saveQuote(quote);
			myThisBookAdapter.add((Quote)quote);
			myAllBooksAdapter.add((Quote)quote);
		}
	}*/

	private void gotoQuote(Quote quote) {
		quote.markAsAccessed();
		myCollection.saveQuote(quote);
		final Book book = myCollection.getBookById(quote.getBookId());
		if (book != null) {
			try {
				FullReaderActivity.openBookActivity(this, book, quote);
			} catch (BookReadingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			UIUtil.showErrorMessage(this, "cannotOpenBook");
		}
	}

	private final class QuotesAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, OnCreateContextMenuListener{
		private final List<Quote> myQuotes =
			Collections.synchronizedList(new LinkedList<Quote>());
		private final boolean myShowAddQuoteItem;

		QuotesAdapter(ListView listView, boolean showAddQuoteItem) {
			myShowAddQuoteItem = showAddQuoteItem;
			listView.setAdapter(this);
			listView.setOnItemClickListener(this);
			listView.setOnCreateContextMenuListener(this);
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

		public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			if (getItem(position) != null) {
				menu.setHeaderTitle(getItem(position).getText());
				menu.add(0, OPEN_ITEM_ID, 0, myResource.getResource("open").getValue());
				//menu.add(0, EDIT_ITEM_ID, 0, myResource.getResource("edit").getValue());
				menu.add(0, SHARE_ITEM_ID, 0, myResource.getResource("share").getValue());
				menu.add(0, DELETE_ITEM_ID, 0, myResource.getResource("delete").getValue());
			}
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.quote_item, parent, false);
			final ImageView imageView = (ImageView)view.findViewById(R.id.quote_item_icon);
			final TextView textView = (TextView)view.findViewById(R.id.quote_item_text);
			final TextView bookTitleView = (TextView)view.findViewById(R.id.quote_item_booktitle);

			final Quote quote = getItem(position);
			if (quote == null) {
				imageView.setVisibility(View.VISIBLE);
				imageView.setImageResource(R.drawable.ic_list_plus);
				textView.setText(myResource.getResource("new").getValue());
				bookTitleView.setVisibility(View.GONE);
			} else {
				imageView.setVisibility(View.GONE);
				//textView.setText(quote.getText());
		        final Book book = myCollection.getBookById(quote.getBookId());
		        if(book.authors().get(0)!=null)
		        	textView.setText(quote.getText()+"\n"+quote.getBookTitle()+"("+book.authors().get(0).DisplayName+")");
		        else
		        	textView.setText(quote.getText()+"\n");
				if (myShowAddQuoteItem) {
					bookTitleView.setVisibility(View.GONE);
				} else {
					bookTitleView.setVisibility(View.VISIBLE);
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

		public final void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final Quote quote = getItem(position);
			Log.d("QUOTES", "quotes item clicked!");
			ReaderApp.quotesCIndex = quote.CharIndex;
			ReaderApp.quotesEIndex = quote.ElementIndex;
			ReaderApp.quotesPIndex = quote.ParagraphIndex;
			ReaderApp.isOpenFromQuotes = true;
			
			if (quote != null) {
				//gotoQuote(quote);
				//FullReaderActivity.openBookActivity(QuotesActivity.this, myBook, null);
			} else {
				addQuote();
			}
		}
	}

}
