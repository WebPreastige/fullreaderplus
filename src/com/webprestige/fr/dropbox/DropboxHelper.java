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
package com.webprestige.fr.dropbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geometerplus.android.fbreader.ReaderApplication;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxDatastoreManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFields;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileStatus;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;


/*
 * Класс, который будет работать с Dropbox API
 */
public class DropboxHelper {
	private String APP_KEY = "jua11jfxwedmd07";
	private String APP_SECRET = "8pww0cq6ld7sxxh";
	
	private static String TAG = "dropbox_helper";
	
	public static final String NO_ID = "no_id";
	
	private static final String BOOKS_TABLE_ID = "synced_books";
	private static final String BOOKMARKS_TABLE_ID = "synced_bookmarks";
	private static final String QUOTES_TABLE_ID = "synced_quotes";
	private static final String COLOR_MARKS_TABLE_ID = "synced_color_marks";
	
	private static final String BOOK_TITLE = "book";
	private static final String BOOK_PAR_INDEX = "book_par_index";
	private static final String BOOK_EL_INDEX = "book_el_index";
	private static final String BOOK_CH_INDEX = "book_ch_index";
	private static final String BOOK_DBX_USER_ID = "book_dbx_user_id";
	
	private static final String BOOKMARK_BOOK_TITLE = "bmk_book_title";
	private static final String BOOKMARK_BOOK_PAR_INDEX = "bmk_book_par_index";
	private static final String BOOKMARK_BOOK_EL_INDEX = "bmk_book_el_index";
	private static final String BOOKMARK_BOOK_CH_INDEX = "bmk_book_ch_index";
	private static final String BOOKMARK_DATE = "bookmark_date";
	private static final String BOOKMARK_DBX_USER_ID = "bookmark_dbx_user_id";
	
	private static final String QUOTE_BOOK_TITLE = "qt_book_title";
	private static final String QUOTE_TEXT = "qt_text";
	private static final String QUOTE_BOOK_PAR_INDEX = "qt_book_par_index";
	private static final String QUOTE_BOOK_EL_INDEX = "qt_book_el_index";
	private static final String QUOTE_BOOK_CH_INDEX = "qt_book_ch_index";
	private static final String QUOTE_DATE = "qt_date";
	private static final String QUOTE_DBX_USER_ID = "qt_dbx_user_id";
	
	private static final String CM_BOOK_TITLE = "cm_book_title";
	private static final String CM_TEXT  = "cm_text";
	private static final String CM_BOOK_PAR_INDEX = "cm_book_par_index";
	private static final String CM_BOOK_EL_INDEX = "cm_book_el_index";
	private static final String CM_BOOK_CH_INDEX = "cm_book_ch_index";
	private static final String CM_BOOK_START_PAR_INDEX = "cm_book_start_par_index";
	private static final String CM_BOOK_START_EL_INDEX = "cm_book_start_el_index";
	private static final String CM_BOOK_START_CH_INDEX = "cm_book_start_ch_index";
	private static final String CM_BOOK_END_PAR_INDEX = "cm_book_end_par_index";
	private static final String CM_BOOK_END_EL_INDEX = "cm_book_end_el_index";
	private static final String CM_BOOK_END_CH_INDEX = "cm_book_end_ch_index";
	private static final String CM_DATE = "cm_date";
	private static final String CM_COLOR = "cm_color";
	private static final String CM_HEX_COLOR = "cm_hex_color";
	private static final String CM_DBX_USER_ID = "cm_dbx_user_id";
			
	
	public static final int REQUEST_LINK_TO_DBX = 1;
	
	private int RESULT_OK = 1;
	private int RESULT_ERR = 0;
	
	private static DropboxHelper instance;
	private DbxAccountManager mAccountManager;
	private DbxAccount mAccount;
	
	private Context mContext;
	DbxFileSystem mDbxFs;
	DbxDatastore mStore;
	
	DbxTable mTable;
	
	DropboxHelper (Context context){
		mContext = context;
		mAccountManager =  DbxAccountManager.getInstance(ReaderApplication.getContext(), APP_KEY, APP_SECRET);
		if (mAccountManager.hasLinkedAccount()) {
			mAccount = mAccountManager.getLinkedAccount();
			try{
				mStore = DbxDatastore.openDefault(mAccount);
				mStore.sync();
				mStore.close();
			}
			catch (Exception e){
				if (mStore!=null && mStore.isOpen()) mStore.close();
				e.printStackTrace();
			}
		}
	}
		
	
	public static DropboxHelper Instance(Context context){
		if (instance == null){
			instance = new DropboxHelper(context);
		}
		return instance;
	} 
	
	public boolean hasLinkedAccount(){
		return mAccountManager.hasLinkedAccount();
	}
	
	public void startLink(Activity from){
		  mAccountManager.startLink(from, REQUEST_LINK_TO_DBX);
	}
	
	
	public void linkAccount(){
		mAccount = mAccountManager.getLinkedAccount();
		if (mAccount == null) Log.d("MyLog", "Account on link is null!!!!!");
		try{
			mStore = DbxDatastore.openDefault(mAccount);
			mStore.sync();
			mStore.close();
			if (mAccount.isLinked())Log.d("MyLog", "Dropbox account is linked");
		}
		catch (Exception e){
			e.printStackTrace();
			if (mStore!=null && mStore.isOpen()) mStore.close();
		}
		
	}
	
	
	public void unlinkAccount(){
		mAccount = mAccountManager.getLinkedAccount();
		mAccount.unlink();
	}

	public boolean syncBooks(SyncedData data){
		try{
			mStore = DbxDatastore.openDefault(mAccount);
			String id;
			if (data.hasBooks()){
				DbxTable books = mStore.getTable(BOOKS_TABLE_ID);
				ArrayList<SyncedBookInfo> infos = data.getSBInfos();
				Log.d(TAG, "Synced infos size - " + String.valueOf(infos.size()));
				for (SyncedBookInfo info : infos){
					if (!info.needsUpdate()) continue;
					if (info.getId().equals(NO_ID)){
						// Делаем проверку, нет ли уже такой засинхронизированной книги
						// для этого пользователя
						DbxFields queryParams = new DbxFields().set(BOOK_TITLE, info.getBookTitle())
								.set(BOOK_DBX_USER_ID, mAccount.getUserId());
						DbxTable.QueryResult results = books.query(queryParams);
						// Если такая книга уже есть в базе Dropbox - обновляем ее
						if (results.count()>0){
							Log.d(TAG, "Book found");
							DbxRecord record = results.iterator().next();
							record.set(BOOK_PAR_INDEX, info.getParIndex())
							.set(BOOK_EL_INDEX, info.getElIndex())
							.set(BOOK_CH_INDEX, info.getChIndex());
							info.updateId(record.getId());
						}
						// Если такой записи нету - добавляем ее
						else{
							DbxRecord record = books.insert().set(BOOK_TITLE, info.getBookTitle())
										.set(BOOK_PAR_INDEX, info.getParIndex())
										.set(BOOK_EL_INDEX, info.getElIndex())
										.set(BOOK_CH_INDEX, info.getChIndex())
										.set(BOOK_DBX_USER_ID, mAccount.getUserId());
							info.updateId(record.getId());
						}
						
					}
					else{
						id = info.getId();
						DbxRecord record = books.get(id);
						record.set(BOOK_PAR_INDEX, info.getParIndex())
								.set(BOOK_EL_INDEX, info.getElIndex())
								.set(BOOK_CH_INDEX, info.getChIndex());
					}
					info.noNeedsUpdate();
				}
			}
			mStore.sync();
			mStore.sync();
			mStore.close();
		}
		catch (Exception e){
			Log.d(TAG, "Error on data saving to Dropbox");
			if (mStore!=null && mStore.isOpen()) mStore.close();
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	public boolean syncBookmark(SyncedBookmarkInfo sBmkInfo){
		try{
			mStore = DbxDatastore.openDefault(mAccount);
			DbxTable bookmarks = mStore.getTable(BOOKMARKS_TABLE_ID);
			DbxRecord record = bookmarks.insert().set(BOOKMARK_BOOK_TITLE, sBmkInfo.getTitle())
					.set(BOOKMARK_BOOK_PAR_INDEX, sBmkInfo.getParIndex())
					.set(BOOKMARK_BOOK_EL_INDEX, sBmkInfo.getElIndex())
					.set(BOOKMARK_BOOK_CH_INDEX, sBmkInfo.getChIndex())
					.set(BOOKMARK_DATE, sBmkInfo.getDate())
					.set(BOOKMARK_DBX_USER_ID, mAccount.getUserId());
			sBmkInfo.updateId(record.getId());
			mStore.sync();
			mStore.close();
			return true;
		}
		catch (Exception e){
			e.printStackTrace();
			if (mStore!=null && mStore.isOpen()) mStore.close();
			return false;
		}
	}
	
	
	public boolean syncQuote(SyncedQuoteInfo sQtInfo){
		try{
			mStore = DbxDatastore.openDefault(mAccount);
			DbxTable quotes = mStore.getTable(QUOTES_TABLE_ID);
			DbxRecord record = quotes.insert().set(QUOTE_BOOK_TITLE, sQtInfo.getTitle())
					.set(QUOTE_TEXT, sQtInfo.getQuoteText())
					.set(QUOTE_BOOK_PAR_INDEX, sQtInfo.getParIndex())
					.set(QUOTE_BOOK_EL_INDEX, sQtInfo.getElIndex())
					.set(QUOTE_BOOK_CH_INDEX, sQtInfo.getChIndex())
					.set(QUOTE_DATE, sQtInfo.getDate())
					.set(QUOTE_DBX_USER_ID, mAccount.getUserId());
			sQtInfo.updateId(record.getId());
			mStore.sync();
			mStore.close();
			return true;
		}
		catch (Exception e){
			e.printStackTrace();
			if (mStore!=null && mStore.isOpen()) mStore.close();
			return false;
		}
	}
	
	public boolean syncColorMark(SyncedColorMarkInfo sCMInfo){
		try{
			mStore = DbxDatastore.openDefault(mAccount);
			DbxTable colorMarks = mStore.getTable(COLOR_MARKS_TABLE_ID);
			DbxRecord record = colorMarks.insert().set(CM_BOOK_TITLE, sCMInfo.getTitle())
				.set(CM_TEXT, sCMInfo.getQuoteText())
				.set(CM_BOOK_PAR_INDEX, sCMInfo.getParIndex())
				.set(CM_BOOK_EL_INDEX, sCMInfo.getParIndex())
				.set(CM_BOOK_CH_INDEX, sCMInfo.getChIndex())
				.set(CM_BOOK_START_PAR_INDEX, sCMInfo.getStartParIndex())
				.set(CM_BOOK_START_EL_INDEX, sCMInfo.getStartElIndex())
				.set(CM_BOOK_START_CH_INDEX, sCMInfo.getStartChIndex())
				.set(CM_BOOK_END_PAR_INDEX, sCMInfo.getEndParIndex())
				.set(CM_BOOK_END_EL_INDEX, sCMInfo.getEndElIndex())
				.set(CM_BOOK_END_CH_INDEX, sCMInfo.getEndChIndex())
				.set(CM_DATE, sCMInfo.getDate())
				.set(CM_COLOR, sCMInfo.getColor())
				.set(CM_HEX_COLOR, sCMInfo.getHexColor())
				.set(CM_DBX_USER_ID, mAccount.getUserId());
				sCMInfo.updateId(record.getId());
			mStore.sync();
			mStore.close();
			return true;
		}
		catch (Exception e){
			e.printStackTrace();
			if (mStore!=null && mStore.isOpen()) mStore.close();
			return false;
		}
	}
	
	
	public void deleteDatastore(){
		try{
			if (mStore.isOpen()) mStore.close();
			DbxDatastoreManager.forAccount(mAccount).deleteDatastore("default");
			Log.d(TAG, "Record deleted");
		}
		catch (Exception e){
			Log.d(TAG, "DELETe datastore exception");
			e.printStackTrace();
			if (mStore!=null && mStore.isOpen()) mStore.close();
		}
	}
	
	public SyncedData getSyncedData(){
		try{
			mAccountManager =  DbxAccountManager.getInstance(ReaderApplication.getContext(), APP_KEY, APP_SECRET);
			mAccount = mAccountManager.getLinkedAccount();
			if (!mAccountManager.hasLinkedAccount())Log.d("MyLog", "Acc manager has no account");
			if (mAccount == null) Log.d("MyLog", "mAccount is null");
			if (mAccountManager == null) Log.d("MyLog", "mAccount manager");
			DbxDatastoreManager mng = DbxDatastoreManager.forAccount(mAccount);
			mStore = mng.openDefaultDatastore();
			mStore.sync();
			mStore.sync();
			DbxTable books= mStore.getTable(BOOKS_TABLE_ID);	
			DbxFields queryParams = new DbxFields().set(BOOK_DBX_USER_ID, mAccount.getUserId());
			DbxTable.QueryResult results = books.query(queryParams);
			DbxRecord record; 
			SyncedData data = SyncedData.Instance();
			Iterator<?> iter = results.iterator();
			while(iter.hasNext()) {
				record = (DbxRecord) iter.next();
				Log.d(TAG, "Got book - " + record.getString(BOOK_TITLE));
				data.addBookFromDbx(new SyncedBookInfo(record.getId(), record.getString(BOOK_TITLE),
						(int)record.getLong(BOOK_PAR_INDEX), (int)record.getLong(BOOK_EL_INDEX), (int)record.getLong(BOOK_CH_INDEX), false));
			}
			// Bookmarks
			DbxTable bookmarks = mStore.getTable(BOOKMARKS_TABLE_ID);
			queryParams = new DbxFields().set(BOOKMARK_DBX_USER_ID, mAccount.getUserId());
			results = bookmarks.query(queryParams);
			iter = results.iterator();
			while (iter.hasNext()){
				record = (DbxRecord) iter.next();
				data.addBookmarkToSync(new SyncedBookmarkInfo(record.getId(), 
						record.getString(BOOKMARK_BOOK_TITLE),
						(int)record.getLong(BOOKMARK_BOOK_PAR_INDEX),
						(int)record.getLong(BOOKMARK_BOOK_EL_INDEX),
						(int)record.getLong(BOOKMARK_BOOK_CH_INDEX),
						record.getString(BOOKMARK_DATE)));
			}
			// Quotes 
			DbxTable quotes = mStore.getTable(QUOTES_TABLE_ID);
			queryParams = new DbxFields().set(QUOTE_DBX_USER_ID, mAccount.getUserId());
			results = quotes.query(queryParams);
			iter = results.iterator();
			while (iter.hasNext()){
				record = (DbxRecord) iter.next();
				data.addQuoteToSync(new SyncedQuoteInfo(record.getId(),
						record.getString(QUOTE_BOOK_TITLE),
						record.getString(QUOTE_TEXT),
						(int)record.getLong(QUOTE_BOOK_PAR_INDEX),
						(int)record.getLong(QUOTE_BOOK_EL_INDEX),
						(int)record.getLong(QUOTE_BOOK_CH_INDEX),
						record.getString(QUOTE_DATE)));
			}
			// Color marks 
			DbxTable colorMarks = mStore.getTable(COLOR_MARKS_TABLE_ID);
			queryParams = new DbxFields().set(CM_DBX_USER_ID, mAccount.getUserId());
			results = colorMarks.query(queryParams);
			iter = results.iterator();
			while (iter.hasNext()){
				record = (DbxRecord) iter.next();
				data.addColorMarkToSync(new SyncedColorMarkInfo(record.getId(), record.getString(CM_BOOK_TITLE),
						record.getString(CM_TEXT),
						record.getString(CM_DATE),
						(int)record.getLong(CM_BOOK_PAR_INDEX),
						(int)record.getLong(CM_BOOK_EL_INDEX),
						(int)record.getLong(CM_BOOK_CH_INDEX),
						(int)record.getLong(CM_BOOK_START_PAR_INDEX),
						(int)record.getLong(CM_BOOK_START_EL_INDEX),
						(int)record.getLong(CM_BOOK_START_CH_INDEX),
						(int)record.getLong(CM_BOOK_END_PAR_INDEX),
						(int)record.getLong(CM_BOOK_END_EL_INDEX),
						(int)record.getLong(CM_BOOK_END_CH_INDEX),
						(int)record.getLong(CM_COLOR),
						record.getString(CM_HEX_COLOR)));
			}
			mStore.close();
			return data;
		}
		catch (Exception ex){
			Log.d(TAG, "Error on get synced data");
			ex.printStackTrace();
			if (mStore!=null && mStore.isOpen()) mStore.close();
			return null;
		}
	}
	
	public boolean deleteBookmark(String id){
		try{
			mStore = DbxDatastore.openDefault(mAccount);
			DbxTable bookmarks = mStore.getTable(BOOKMARKS_TABLE_ID);
			DbxRecord record = bookmarks.get(id);
			record.deleteRecord();
			mStore.sync();
			mStore.close();
			return true;
		}
		catch (Exception e){
			e.printStackTrace();
			if (mStore!=null && mStore.isOpen()) mStore.close();
			return false;
		}
	}
	
	public boolean deleteQuote(String id){
		try{
			mStore = DbxDatastore.openDefault(mAccount);
			DbxTable quotes = mStore.getTable(QUOTES_TABLE_ID);
			DbxRecord record = quotes.get(id);
			record.deleteRecord();
			mStore.sync();
			mStore.close();
			return true;
		}
		catch (Exception e){
			e.printStackTrace();
			if (mStore!=null && mStore.isOpen()) mStore.close();
			return false;
		}
	}

	public boolean deleteColorMark(String id){
		try{
			mStore = DbxDatastore.openDefault(mAccount);
			DbxTable colorMarks = mStore.getTable(COLOR_MARKS_TABLE_ID);
			DbxRecord record = colorMarks.get(id);
			record.deleteRecord();
			mStore.sync();
			mStore.close();
			return true;
		}
		catch (Exception e){
			e.printStackTrace();
			if (mStore!=null && mStore.isOpen()) mStore.close();
			return false;
		}
	}
	
	 public static boolean hasConnection() {
	    ConnectivityManager cm = (ConnectivityManager)ReaderApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

	    NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    if (wifiNetwork != null && wifiNetwork.isConnected()) {
	      return true;
	    }

	    NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    if (mobileNetwork != null && mobileNetwork.isConnected()) {
	      return true;
	    }

	    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
	    if (activeNetwork != null && activeNetwork.isConnected()) {
	      return true;
	    }

	    return false;
	 }
}
