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
package com.webprestige.fr.bookmarks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import org.geometerplus.android.fbreader.BaseActivity;
import org.geometerplus.android.fbreader.FullReaderActivity;
import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.libraryService.SQLiteBooksDatabase;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BooksDatabase;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.fbreader.ReaderApp;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
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
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.webprestige.fr.dropbox.DropboxService;
import com.webprestige.fr.dropbox.IDropboxService;
import com.fullreader.R;

public class BookmarksActivity extends BaseActivity{
	
	private ListView bookmarksList;
	private BookmarksListAdapter listAdapter;
	private final BookCollectionShadow myCollection = new BookCollectionShadow();
	private BooksDatabase booksDB;
	private ArrayList<MyBookmark> bookmarks;
	private DatabaseHandler db;
	private boolean isRunFromBook = false;
	private boolean isFromBook = false;
	private boolean isFromStartScreen = false;
	
	private IDropboxService mDbxService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Запускаем службу для работы с Dropbox
		bindService(new Intent(this, DropboxService.class), mConnection, Context.BIND_AUTO_CREATE);
		
		try {
			this.isRunFromBook = getIntent().getExtras().getBoolean("isRunFromBook", false);
			this.isFromBook = getIntent().getExtras().getBoolean("fromBook", false);
			this.isFromStartScreen = getIntent().getExtras().getBoolean("fromStartScreen", false);
		} catch(NullPointerException e) {
			this.isRunFromBook = false;
			this.isFromBook = false;
			this.isFromStartScreen = false;
		}	
		setContentView(R.layout.bookmarks_activity);
		
		if(!checkAds()) {
			initAdMob();
		}
		
		if(isFromBook) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);		
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		db = new DatabaseHandler(this);
		bookmarks = db.getAllBookmarks();
	//	for(MyBookmark bookmark : bookmarks) {
		//	Log.d("BOOKMARK FROM DB: ", bookmark.getBookTitle() + book,);
		//}
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar bar = getSupportActionBar();
        if(loadCurrentLanguage().equals("en")){
			bar.setTitle("Bookmarks");
		} else if(loadCurrentLanguage().equals("de")){
			bar.setTitle("Bookmarks");
		} else if(loadCurrentLanguage().equals("fr")){
			bar.setTitle("Favoris");
		} else if(loadCurrentLanguage().equals("uk")){
			bar.setTitle("Закладки");
		} else if(loadCurrentLanguage().equals("ru")){
			bar.setTitle("Закладки");
		} else {
			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
				bar.setTitle("Закладки");
			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
				bar.setTitle("Закладки");
			} else {
				bar.setTitle("Bookmarks");
			}
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
		bookmarksList = (ListView)findViewById(R.id.bookmarks_list);
		bookmarksList.setCacheColorHint(Color.TRANSPARENT);
		registerForContextMenu(bookmarksList);
		initBookmarksList();
		bookmarksList.setClickable(true);
		bookmarksList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
					openBook(position);
			}		
		});
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
	
	private void initAdMob() {
		AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .addTestDevice("TEST_DEVICE_ID")
            .build();
        adView.loadAd(adRequest);
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		bookmarks.clear();
		bookmarks = db.getAllBookmarks();
		initBookmarksList();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	      super.onCreateContextMenu(menu, v, menuInfo);
	      if (v.getId()==R.id.bookmarks_list) {
	          MenuInflater inflater = getMenuInflater();
	        if(loadCurrentLanguage().equals("en")){
	        	 inflater.inflate(R.menu.menu_bookmarks_list_en, menu);
	  		} else if(loadCurrentLanguage().equals("de")){
	  			 inflater.inflate(R.menu.menu_bookmarks_list_de, menu);
	  		} else if(loadCurrentLanguage().equals("fr")){
	  			 inflater.inflate(R.menu.menu_bookmarks_list_fr, menu);
	  		} else if(loadCurrentLanguage().equals("uk")){
	  			 inflater.inflate(R.menu.menu_bookmarks_list_uk, menu);
	  		} else if(loadCurrentLanguage().equals("ru")){
	  			 inflater.inflate(R.menu.menu_bookmarks_list_ru, menu);
	  		} else {
	  			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
	  				 inflater.inflate(R.menu.menu_bookmarks_list_ru, menu);
	  			}else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
	  				inflater.inflate(R.menu.menu_bookmarks_list_uk, menu);
	  			}else {
	  				 inflater.inflate(R.menu.menu_bookmarks_list_en, menu);
	  			}
	  		}
	      }
	}
	
	 public String loadCurrentLanguage() {
		  SharedPreferences sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
		  return sPref.getString("curLanguage", "");
	 }
	 
	 private void openBook(int position) {
		// TODO Auto-generated method stub
			Log.d("LIST BOOKMARKS", "test on click");
			ReaderApp.pIndex = bookmarks.get(position).getParagraphIndex();
			ReaderApp.eIndex = bookmarks.get(position).getElementIndex();
			ReaderApp.cIndex = bookmarks.get(position).getCharIndex();
			ReaderApp.isOpenFromBookmark = true;
			Book book = booksDB.loadBook(bookmarks.get(position).getBookID());
			if(book == null) {
				Log.d("bookPATH: ", String.valueOf(bookmarks.get(position).getPathToBook()));
				Log.d("book is from file: ", String.valueOf(bookmarks.get(position).getIsFromMyFile()));
				if(bookmarks.get(position).getIsFromMyFile() == 1) {
					try {
						ZLFile zlFile = ZLFile.createFileByPath(bookmarks.get(position).getPathToBook());
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
				view.gotoPosition(bookmarks.get(position).getParagraphIndex(), 
								  bookmarks.get(position).getElementIndex(),
								  bookmarks.get(position).getCharIndex());
				isRunFromBook = false;
			}
			if(book != null) {
				Log.d("BOOK ", "not null");
				Log.d("BOOK ID ", String.valueOf(ReaderApp.bookID));
				try {
					if(book.File.exists()) {
						FullReaderActivity.openBookActivity(BookmarksActivity.this, book, null);
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
					
				}
			} else {
				Log.d("BOOK ", "NULL");
				Log.d("BOOK ID ", String.valueOf(ReaderApp.bookID));
			}
	 }
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	      final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
	      MyBookmark bookmark = (MyBookmark)bookmarksList.getAdapter().getItem(position);
	      switch(item.getItemId()) {
	          case R.id.open:
	        	  openBook(position);
	                return true;
	          case R.id.delete:
	        	   db.deleteBookmark(bookmark);
	        	   removeItemFromList(position);
	        	   try{
	        		   mDbxService.deleteBookmark(bookmark.getBookTitle(), bookmark.getParagraphIndex());
	        	   }
	        	   catch (Exception e){
	        		   Log.d("MyLog", "cant delete bookmark");
	        	   }
	               return true;
	          default:
	                return super.onContextItemSelected(item);
	      }
	}
	
	protected void removeItemFromList(int position) {
        final int deletePosition = position;
        bookmarks.remove(deletePosition);
        listAdapter.notifyDataSetChanged();
        listAdapter.notifyDataSetInvalidated();
    }
	
	private void initBookmarksList() {
		listAdapter = new BookmarksListAdapter(getApplicationContext(), bookmarks);
		bookmarksList.setAdapter(listAdapter);
				
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
