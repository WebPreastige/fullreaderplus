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
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.geometerplus.android.fbreader.SelectedMarkInfo;
import org.geometerplus.android.fbreader.libraryService.LibraryService;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import com.dropbox.sync.android.DbxException;
import com.webprestige.fr.citations.MyQuote;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

/*
 * Служба, которая в фоновом режиме будет синхронизироваться с Dropbox
 */

public class DropboxService extends Service {
	private static final String TAG = "dropbox_service";
	private DropboxHelper mHelper;
	private IDropboxServiceCallback mCallback;
	private SyncedData mSyncedData;
	public static boolean isRunning = false;

	public void onCreate(){
		super.onCreate();
		//Log.d(TAG, "Dropbox service created");
		mHelper = new DropboxHelper(getApplicationContext());
		mSyncedData = SyncedData.Instance();
		isRunning = true;
	}
	
	public void onDestroy(){
		super.onDestroy();
		Log.d(TAG, "Service is destroyed");
		mSyncedData.clearAll();
		isRunning = false;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		//Log.d(TAG, "Dropbox service on bind");
		return mDbxBinder;
	}
	
	public boolean onUnbind(Intent intent) {
	    //Log.d(TAG, "Dropbox servce on unbind");
		// stopSelf();
	    return super.onUnbind(intent);
	}
	
	
	
	// Интерфейс который позволяет Activity общатсья со службой
	private final IDropboxService.Stub mDbxBinder = new IDropboxService.Stub() {
		
		@Override
		public void deleteBookmark(String bookTitle, int parIndex){
			if (!DropboxHelper.hasConnection()) return;
			boolean result = false;
			ArrayList<SyncedBookmarkInfo> sBmkInfos = mSyncedData.getSBmkInfos();
			for (SyncedBookmarkInfo sBmkInfo : sBmkInfos){
				if (sBmkInfo.getTitle().equals(bookTitle) && sBmkInfo.getParIndex() == parIndex){
					result = mHelper.deleteBookmark(sBmkInfo.getId());
					break;
				}
			}
			if (result) Log.d(TAG, "Bookmark sucessully deleted from service");
			else Log.d(TAG, "Bookmark delete error from service");
		}
		
		@Override
		public void deleteQuote(String bookTitle, String quoteText, int parIndex){
			if (!DropboxHelper.hasConnection()) return;
			boolean result = false;
			ArrayList<SyncedQuoteInfo> sQtInfos = mSyncedData.getSQtInfos();
			for (SyncedQuoteInfo sQtInfo : sQtInfos){
				if (sQtInfo.getTitle().equals(bookTitle) 
						&& sQtInfo.getQuoteText().equals(quoteText)
						&& sQtInfo.getParIndex() == parIndex){
					result = mHelper.deleteQuote(sQtInfo.getId());
					break;
				}
			}
			if (result) Log.d(TAG, "Quote sucessully deleted from service");
			else Log.d(TAG, "Quote delete error from service");
			
		}
		
		@Override
		public void deleteColorMark(String bookTitle, String quoteText, int parIndex, int startParIndex, int endParIndex, int color){
			if (!DropboxHelper.hasConnection()) return;
			boolean result = false;
			ArrayList<SyncedColorMarkInfo> sCMInfos = mSyncedData.getSCMInfos();
			for (SyncedColorMarkInfo sCMInfo : sCMInfos){
				if (sCMInfo.getTitle().equals(bookTitle)
						&& sCMInfo.getQuoteText().equals(quoteText)
						&& sCMInfo.getParIndex() == parIndex
						&& sCMInfo.getStartParIndex() == startParIndex
						&& sCMInfo.getEndParIndex() == endParIndex
						&& sCMInfo.getColor() == color){
					result = mHelper.deleteColorMark(sCMInfo.getId());
					break;
				}
			}
			if (result) Log.d(TAG, "Colormark deleted from service");
			else Log.d(TAG, "Colormark delete error from service");
		}
		
		@Override
		public void addQuote(SyncedQuoteInfo sQtInfo) throws RemoteException {
			if (!DropboxHelper.hasConnection()) return;
			mSyncedData.addQuoteToSync(sQtInfo);
			mHelper.syncQuote(sQtInfo);
		}
		
		@Override
		public void addBookmark(SyncedBookmarkInfo sBmkInfo) throws RemoteException {
			if (!DropboxHelper.hasConnection()) return;
			mSyncedData.addBookmarkToSync(sBmkInfo);
			mHelper.syncBookmark(sBmkInfo);
		}
		
		@Override
		public void addColorMark(SyncedColorMarkInfo sCMInfo) throws RemoteException {
			if (!DropboxHelper.hasConnection()) return;
			mSyncedData.addColorMarkToSync(sCMInfo);
			mHelper.syncColorMark(sCMInfo);
		}
		
		@Override
		public void addBook(SyncedBookInfo sbInfo) throws RemoteException {
			mSyncedData.addBookToSync(sbInfo);
		}
		
		@Override
		public void syncAll(){
			Uploader uploader = new Uploader();
			uploader.execute();
		}

		@Override
		public void getSyncData() throws RemoteException {
			if (!DropboxHelper.hasConnection()){
				mCallback.showNoNetworkToast();
				return;
			}
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
			      public void run() {
			    	  Downloader downloader = new Downloader(DropboxService.this);
			    	  downloader.execute();
			      }
			});
			
			
			
		}

		@Override
		public void registerCallback(IDropboxServiceCallback callback){
			Log.d(TAG, "Callback registered");
			mCallback = callback;
		}
		
		@Override
		public void unregisterCallback(){
			mCallback = null;
		}
		
	};
	
	class Uploader extends AsyncTask <Void, Void, Void>{
		boolean res;
		@Override
		protected Void doInBackground(Void... arg0) {
			Log.d(TAG, "Uploader started");
			res = mHelper.syncBooks(mSyncedData);
			return null;
		}
		
		@Override
	    protected void onPostExecute(Void result) {
			 super.onPostExecute(result);
			 if (res)Log.d(TAG, "Save success");
			 else Log.d(TAG, "Save fail");
			 try {
				if (mCallback!=null)mCallback.uploadFinished(res);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
		}
		
	}
	
	
	class Downloader extends AsyncTask <Void, Void, Boolean>{
		ProgressDialog mDownloadProgress;
		Context ctx;
		DropboxHelper helper;
		SyncedData data;
		boolean result;
		
		Downloader(Context context){
			ctx = context;
			helper = DropboxHelper.Instance(ctx);
		}
		
		@Override
		protected void onPreExecute(){
			try {
				mCallback.showDialog();
				Log.d(TAG, "Downloading data started");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		
		@Override
		protected Boolean doInBackground (Void... args){
			data = helper.getSyncedData();
			if (data == null) return false;
			else return true;
			
		}
		
		@Override
	    protected void onPostExecute(Boolean result) {
			 super.onPostExecute(result);
			 try {
				if (data == null) Log.d(TAG, "Downloading fails");
				else Log.d(TAG, "Downloading success");
				mCallback.syncFinished(data);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

	}

}
