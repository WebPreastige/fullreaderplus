/*
FullReader+
Copyright 2013-2014 Viktoriya Bilyk

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
package com.webprestige.fr.citations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Locale;

import org.geometerplus.android.fbreader.BaseActivity;
import org.geometerplus.android.fbreader.FullReaderActivity;
import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.android.fbreader.SelectedMarkInfo;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.libraryService.SQLiteBooksDatabase;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BooksDatabase;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.fbreader.fbreader.ReaderApp;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.ZLTextView;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.webprestige.fr.bookmarks.DatabaseHandler;
import com.webprestige.fr.dropbox.DropboxService;
import com.webprestige.fr.dropbox.IDropboxService;
import com.fullreader.R;

public class QuotesActivity extends BaseActivity{
	private ListView quotesList;
	private QuotesListAdapter listAdapter;
	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private BooksDatabase booksDB;
	private ArrayList<MyQuote> quotes;
	private DatabaseHandler db;
	private Facebook facebook;
	private boolean isRunFromBook = false;
	private boolean isFromBook = false;
	private int cIndex, eIndex, pIndex;
	boolean showColorMarks = false;
	private IDropboxService mDbxService;
	public static String SHOW_COLOR_MARKS = "show_color_marks";
	private String title = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("MyLog", "Quotes on create");
		Intent intent = getIntent();
		if (intent!=null){
			if (intent.hasExtra(SHOW_COLOR_MARKS)){
				Log.d("MyLog", "Has show color marks extra");
				showColorMarks = intent.getBooleanExtra(SHOW_COLOR_MARKS, false);
				ZLResource res = ZLResource.resource("menu");
				title = res.getResource("menuColorMarks").getValue();
				Log.d("MyLog", "Title - " + title);
			}
		}
		// Запускаем службу для работы с Dropbox
		bindService(new Intent(this, DropboxService.class), mConnection, Context.BIND_AUTO_CREATE);
		//if() {
		try {
			this.isRunFromBook = getIntent().getExtras().getBoolean("isRunFromBook", false);
			this.isFromBook = getIntent().getExtras().getBoolean("fromBook", false);
		} catch(NullPointerException e) {
			this.isRunFromBook = false;
		}
		//}
		setContentView(R.layout.quotes_activity);
		Log.d("checkADS: ", String.valueOf(checkAds()));
		if(!checkAds()) {
			initAdMob();
		}
		if(isFromBook) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);		
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		db = new DatabaseHandler(this);
		quotes = db.getAllQuotesWithFilter(showColorMarks);
		Log.d("MyLog", "Quotes count - !!!!!" + String.valueOf(quotes.size()));
	//	for(MyBookmark bookmark : bookmarks) {
		//	Log.d("BOOKMARK FROM DB: ", bookmark.getBookTitle() + book,);
		//}
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar bar = getSupportActionBar();
        if (!showColorMarks){
        	 if(loadCurrentLanguage().equals("en")){
     			bar.setTitle("Quotes");
     		} else if(loadCurrentLanguage().equals("de")){
     			bar.setTitle("Zitate");
     		} else if(loadCurrentLanguage().equals("fr")){
     			bar.setTitle("Citations");
     		} else if(loadCurrentLanguage().equals("uk")){
     			bar.setTitle("Цитати");
     		} else if(loadCurrentLanguage().equals("ru")){
     			bar.setTitle("Цитаты");
     		} else {
     			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
     				bar.setTitle("Цитаты");
     			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
     				bar.setTitle("Цитати");
     			} else {
     				bar.setTitle("Quotes");
     			}
     		}
        }
        else{
        	bar.setTitle(title);
        }
        Drawable actionBarBackground = null;
        int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
        switch(theme){
	        case IConstants.THEME_MYBLACK:
	         actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar );
	         getWindow().setBackgroundDrawableResource(R.drawable.theme_black_shelf);
	         break;
	        case IConstants.THEME_LAMINAT:
	         actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar );
	         getWindow().setBackgroundDrawableResource(R.drawable.theme_laminat_shelf);
	         break;
	        case IConstants.THEME_REDTREE:
	         actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_redtree_action_bar );
	         getWindow().setBackgroundDrawableResource(R.drawable.theme_redtree_shelf);
	         break;
        }      
        bar.setBackgroundDrawable(actionBarBackground);
		
		booksDB = SQLiteBooksDatabase.Instance(getBaseContext());
		quotesList = (ListView)findViewById(R.id.quotes_list);
		quotesList.setCacheColorHint(Color.TRANSPARENT);
		registerForContextMenu(quotesList);
		initQuotesList();
		quotesList.setClickable(true);
		quotesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				Log.d("LIST QUOTES", "test on click");
				ReaderApp.quotesPIndex = quotes.get(position).getParagraphIndex();
				ReaderApp.quotesEIndex = quotes.get(position).getElementIndex();
				ReaderApp.quotesCIndex = quotes.get(position).getCharIndex();
				ReaderApp.isOpenFromQuotes = true;
				Book book = booksDB.loadBook(quotes.get(position).getBookID());
				if(book == null) {
					Log.d("bookPATH: ", String.valueOf(quotes.get(position).getPathToBook()));
					Log.d("book is from file: ", String.valueOf(quotes.get(position).getIsFromMyFile()));
					if(quotes.get(position).getIsFromMyFile() == 1) {
						try {
							ZLFile zlFile = ZLFile.createFileByPath(quotes.get(position).getPathToBook());
							book = new Book(zlFile);
							book.setId(100);
						} catch (BookReadingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}		
				}
				if(isRunFromBook) {
					ZLTextView view = (ZLTextView)ReaderApp.Instance().getCurrentView();
					view.gotoPosition(quotes.get(position).getParagraphIndex(), 
									quotes.get(position).getElementIndex(),
									quotes.get(position).getCharIndex());
					isRunFromBook = false;
				}
				if(book != null) {
					Log.d("QUOTE BOOK ", "not null");
					Log.d("QUOTE BOOK ID ", String.valueOf(ReaderApp.quoteBookID));
					try {
						if(book.File.exists()) {
							FullReaderActivity.openBookActivity(QuotesActivity.this, book, null);
						} else {
							
							if(loadCurrentLanguage().equals("en")){
								 Toast.makeText(getApplicationContext(), "This book unavailable or deleted!", Toast.LENGTH_LONG).show();
					  		} else if(loadCurrentLanguage().equals("de")){
					  			Toast.makeText(getApplicationContext(), "Dieses Buch verfügbar oder gelöscht!", Toast.LENGTH_LONG).show();
					  		} else if(loadCurrentLanguage().equals("fr")){
					  			Toast.makeText(getApplicationContext(), "Ce livre indisponible ou supprimé!", Toast.LENGTH_LONG).show();
					  		} else if(loadCurrentLanguage().equals("uk")){
					  			Toast.makeText(getApplicationContext(), "Ця книга недоступна або видалена!", Toast.LENGTH_LONG).show();
					  		} else if(loadCurrentLanguage().equals("ru")){
					  			Toast.makeText(getApplicationContext(), "Эта книга недоступна или удалена!", Toast.LENGTH_LONG).show();
					  		} else {
					  			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
					  				Toast.makeText(getApplicationContext(), "Эта книга недоступна или удалена!", Toast.LENGTH_LONG).show();
					  			}else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
					  				Toast.makeText(getApplicationContext(), "Ця книга недоступна або видалена!", Toast.LENGTH_LONG).show();
					  			}else {
					  				Toast.makeText(getApplicationContext(), "This book unavailable or deleted!", Toast.LENGTH_LONG).show();
					  			}
					  		}
						}
					} catch (BookReadingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					Log.d("QUOTE BOOK ", "NULL");
					Log.d("QUOTE BOOK ID ", String.valueOf(ReaderApp.quoteBookID));
				}
			}		
		});
	}
	
	private void initAdMob() {
		AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .addTestDevice("TEST_DEVICE_ID")
            .build();
        adView.loadAd(adRequest);
	}
	
	protected void onDestroy(){
		super.onDestroy();
		unbindService(mConnection);
	}
	
	
	private boolean checkAds() {
		boolean access = false;
		try {
			File root = new File(Environment.getExternalStorageDirectory(), "FullReader Unlocker");
			File accessFile = new File(root, "access-file.txt");
			if(!accessFile.exists()) {
				access = false;
			} else {
				FileInputStream fin = new FileInputStream(accessFile);
				String fileString;
				fileString = convertStreamToString(fin);
			    //Make sure you close all streams.
			    fin.close(); 
			    String [] tmpStr = fileString.split("\\|");
			    String [] adsStr = tmpStr[1].split("\\:");
			    Log.d("ads access: ", String.valueOf(Integer.parseInt(adsStr[1].trim())));
			    if(Integer.parseInt(adsStr[1].trim()) == 0) {
			    	access = true;
			    }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return access;
	}
	public static String convertStreamToString(InputStream is) throws Exception {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line).append("\n");
	    }
	    reader.close();
	    return sb.toString();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		quotes.clear();
		quotes = db.getAllQuotesWithFilter(showColorMarks);
		initQuotesList();
	}
		
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	      super.onCreateContextMenu(menu, v, menuInfo);
	      if (v.getId()==R.id.quotes_list) {
	          MenuInflater inflater = getMenuInflater();
	        if(loadCurrentLanguage().equals("en")){
	        	 inflater.inflate(R.menu.menu_quotes_list_en, menu);
	  		} else if(loadCurrentLanguage().equals("de")){
	  			 inflater.inflate(R.menu.menu_quotes_list_de, menu);
	  		} else if(loadCurrentLanguage().equals("fr")){
	  			 inflater.inflate(R.menu.menu_quotes_list_fr, menu);
	  		} else if(loadCurrentLanguage().equals("uk")){
	  			 inflater.inflate(R.menu.menu_quotes_list_uk, menu);
	  		} else if(loadCurrentLanguage().equals("ru")){
	  			 inflater.inflate(R.menu.menu_quotes_list_ru, menu);
	  		} else {
	  			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
	  				 inflater.inflate(R.menu.menu_quotes_list_ru, menu);
	  			}else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
	  				inflater.inflate(R.menu.menu_quotes_list_uk, menu);
	  			}else {
	  				 inflater.inflate(R.menu.menu_quotes_list_en, menu);
	  			}
	  		}
      }
	}
	
	 public String loadCurrentLanguage() {
		  SharedPreferences sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
		  return sPref.getString("curLanguage", "");
	 }
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	      final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
	      MyQuote quote = (MyQuote)quotesList.getAdapter().getItem(position);
	      switch(item.getItemId()) {
	          
	          case R.id.open:
	        	  	Log.d("LIST QUOTES", "test on click");
					ReaderApp.quotesPIndex = quotes.get(position).getParagraphIndex();
					ReaderApp.quotesEIndex = quotes.get(position).getElementIndex();
					ReaderApp.quotesCIndex = quotes.get(position).getCharIndex();
					ReaderApp.isOpenFromQuotes = true;
					Book book = booksDB.loadBook(quotes.get(position).getBookID());
					if(book == null) {
						Log.d("bookPATH: ", String.valueOf(quotes.get(position).getPathToBook()));
						Log.d("book is from file: ", String.valueOf(quotes.get(position).getIsFromMyFile()));
						if(quotes.get(position).getIsFromMyFile() == 1) {
							try {
								ZLFile zlFile = ZLFile.createFileByPath(quotes.get(position).getPathToBook());
								book = new Book(zlFile);
								book.setId(100);
							} catch (BookReadingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}		
					}
					if(book != null) {
						Log.d("QUOTE BOOK ", "not null");
						Log.d("QUOTE BOOK ID ", String.valueOf(ReaderApp.quoteBookID));
						try {
							if(book.File.exists()) {
								FullReaderActivity.openBookActivity(QuotesActivity.this, book, null);
							} else {
								
								if(loadCurrentLanguage().equals("en")){
									 Toast.makeText(getApplicationContext(), "This book unavailable or deleted!", Toast.LENGTH_LONG).show();
						  		} else if(loadCurrentLanguage().equals("de")){
						  			Toast.makeText(getApplicationContext(), "Dieses Buch verfügbar oder gelöscht!", Toast.LENGTH_LONG).show();
						  		} else if(loadCurrentLanguage().equals("fr")){
						  			Toast.makeText(getApplicationContext(), "Ce livre indisponible ou supprimé!", Toast.LENGTH_LONG).show();
						  		} else if(loadCurrentLanguage().equals("uk")){
						  			Toast.makeText(getApplicationContext(), "Ця книга недоступна або видалена!", Toast.LENGTH_LONG).show();
						  		} else if(loadCurrentLanguage().equals("ru")){
						  			Toast.makeText(getApplicationContext(), "Эта книга недоступна или удалена!", Toast.LENGTH_LONG).show();
						  		} else {
						  			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
						  				Toast.makeText(getApplicationContext(), "Эта книга недоступна или удалена!", Toast.LENGTH_LONG).show();
						  			}else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
						  				Toast.makeText(getApplicationContext(), "Ця книга недоступна або видалена!", Toast.LENGTH_LONG).show();
						  			}else {
						  				Toast.makeText(getApplicationContext(), "This book unavailable or deleted!", Toast.LENGTH_LONG).show();
						  			}
						  		}
							}
						} catch (BookReadingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						Log.d("QUOTE BOOK ", "NULL");
						Log.d("QUOTE BOOK ID ", String.valueOf(ReaderApp.quoteBookID));
					}
	                return true;
	          case R.id.delete:
	        	  try{
	        		  if (quote.getColor().equals("-1")){
	        			  mDbxService.deleteQuote(quote.getBookTitle(), quote.getText(), quote.getParagraphIndex());
	        		  }
	        		  else{
	        			  Log.d("MyLog", "Deleting colormark");
	        			 SelectedMarkInfo SMInfo = db.getColorMarkForQuote(quote.getID());
	        			 mDbxService.deleteColorMark(quote.getBookTitle(), quote.getText(), quote.getParagraphIndex(), SMInfo.startCursor.getParagraphIndex(), 
	        					 SMInfo.endCursor.getParagraphIndex(), SMInfo.color.getIntValue());
	        		  }
	        	  }
	        	  catch (Exception e){
	        		  Log.d("MyLog", "Deleting quote exception");
	        		  e.printStackTrace();
	        	  }
	        	  db.deleteQuote(quote);
	        	  db.deleteColoredMark(quote.getID());
	        	  
	        	  try{
	        		  final FBView fbview = (FBView)ReaderApp.Instance().getCurrentView();
	        		  fbview.deleteColorMark(quote.getID());
	        	  }
	        	  catch (Exception e){}
	        	  removeItemFromList(position);
	              return true;
	          case R.id.fb_share:
	        	  shareFbQuote(quote.getText());
	          	return true;
	          case R.id.share:
	        	  shareQuote(quote);
	        	  return true;
	          default:
	                return super.onContextItemSelected(item);
	      }
	}
	
	public void shareQuote(MyQuote quote) {
		final Intent intentShare = new Intent(android.content.Intent.ACTION_SEND);
		intentShare.setType("text/plain");
		//intentShare.putExtra(android.content.Intent.EXTRA_SUBJECT,);
		final Book book = booksDB.loadBook(quote.getBookID());
		String text;
		try {
			//if(book.authors().get(0)!=null && book.authors().get(0).DisplayName != null)
			text = "\""+quote.getText()+"\""+" (c) "+quote.getBookTitle()+", "+book.authors().get(0).DisplayName;
		} catch(IndexOutOfBoundsException e){
			///else
			text = "\""+quote.getText()+"\""+" (c) "+quote.getBookTitle();
		}
		intentShare.putExtra(android.content.Intent.EXTRA_TEXT, text);
		startActivity(Intent.createChooser(intentShare, null));
	}
	
	public void shareFbQuote(final String text) {
		facebook = new Facebook(IConstants.FACEBOOK_APP_ID);
		final String[] permissions = {"publish_stream"};
		facebook.authorize(QuotesActivity.this, permissions, new DialogListener () {
			@Override
			public void onComplete(Bundle values) {
				Toast.makeText(QuotesActivity.this, "Authorization successful", Toast.LENGTH_SHORT).show();
				new Thread(){ public void run() {
					postMassage(text);
				}}.start();		
			}

			@Override
			public void onFacebookError(FacebookError e) {
				Toast.makeText(QuotesActivity.this, "Facebook error, try again later", Toast.LENGTH_SHORT).show();
				Log.d("FACEBOOK", e.getMessage());
				e.printStackTrace();
			}

			@Override
			public void onError(DialogError e) {
				Toast.makeText(QuotesActivity.this, "Error, try again later", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onCancel() {
				Toast.makeText(QuotesActivity.this, "Authorization canceled", Toast.LENGTH_SHORT).show();
			}

		});
	}
	
	private void postMassage(String text) {
		Bundle parameters = new Bundle();
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
	
	protected void removeItemFromList(int position) {
        final int deletePosition = position;
        quotes.remove(deletePosition);
        listAdapter.notifyDataSetChanged();
        listAdapter.notifyDataSetInvalidated();
    }
	
	private void initQuotesList() {
		listAdapter = new QuotesListAdapter(getApplicationContext(), quotes);
		quotesList.setAdapter(listAdapter);
				
	}
	
	private ServiceConnection mConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mDbxService = IDropboxService.Stub.asInterface(service);
			Log.d("MyLog", "On Service connected");
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			Log.d("MyLog", "On Service disconnected");
			
		}
    	
    };
}
