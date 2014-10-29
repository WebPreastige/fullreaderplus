/*
FullReader+ 
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com> 
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


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.robotmedia.acv.ui.ComicViewerActivity;

import org.geometerplus.android.fbreader.api.TextPosition;
import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.network.NetworkLibraryPrimaryActivity;
import org.geometerplus.android.fbreader.network.SQLiteNetworkDatabase;
import org.geometerplus.android.fbreader.preferences.PreferenceActivity;
import org.geometerplus.fbreader.Paths;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.book.IBookCollection.Status;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.fbreader.ReaderApp;
import org.geometerplus.fbreader.network.NetworkDatabase;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.image.ZLLoadableImage;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageManager;
import org.vudroid.djvudroid.DjvuViewerActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.artifex.mupdf.MuPDFActivity;
import com.artifex.mupdf.PdfBook;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.plus.PlusShare;
import com.tapjoy.TapjoyConnect;
import com.tapjoy.TapjoyConnectFlag;
import com.tapjoy.TapjoyConnectNotifier;
import com.tapjoy.TapjoyEarnedPointsNotifier;
import com.webprestige.fr.bookmarks.DatabaseHandler;
import com.webprestige.fr.bookmarks.MyBookmark;
import com.webprestige.fr.citations.MyQuote;
import com.webprestige.fr.dropbox.DropboxHelper;
import com.webprestige.fr.dropbox.DropboxService;
import com.webprestige.fr.dropbox.IDropboxService;
import com.webprestige.fr.dropbox.IDropboxServiceCallback;
import com.webprestige.fr.dropbox.SyncedBookInfo;
import com.webprestige.fr.dropbox.SyncedBookmarkInfo;
import com.webprestige.fr.dropbox.SyncedColorMarkInfo;
import com.webprestige.fr.dropbox.SyncedData;
import com.webprestige.fr.dropbox.SyncedQuoteInfo;
import com.webprestige.fr.otherdocs.FrDocument;
import com.fullreader.R;
public class StartScreenActivity extends BaseActivity implements IBookCollection.Listener {

    private static final int MENU_INFO = 0;
	private static final int MENU_SEARCH = 3;
	private static final int MENU_BOOKMARK = 2;
	private static final int MENU_QUOTE = 1;
	private static final int MENU_PREFERENCES = 4;
	private static final int MENU_GOOGLE_PLUS = 5;
	private static final int MENU_SETTINGS_ICON = 6;
	private static final int MENU_BOOKMARKS2 = 7;
	private static final int MENU_COLOR_MARKS = 21;
	private String curTheme = "";
	private AlertDialog.Builder alertBuilder;
	
	private boolean needToShowStartDialog;
	boolean isNeedToDelete = false;
	
	public static String EXIT_ME = "exit_me";
	
	private static final String APP_ID = "bed4d992-e741-4496-9e58-6aa3dd6e19f1";
	private static final String SECRET_ID = "grxUy0BzZyD3zt650HGc";
	
	public static boolean PDF_BOOK_OPENED = false;
	
	
	private static final String SECONDARY_DEX_NAME = "secondary_dex.jar";
    
    // Buffer size for file copying.  While 8kb is used in this sample, you
    // may want to tweak it based on actual size of the secondary dex file involved.
	private static final int BUF_SIZE = 8 * 1024;
	
	private boolean isAppRated = false;
	
	private AdView adView;
	
	public static Activity mActivity;
	//handler events
	protected static final int ON_BIND_TO_SERVER = 0;
	private BookCollectionShadow collection;
	
	private IDropboxService mDbxService;
	
	private DropboxHelper mDbxHelper;
	SharedPreferences sPref;
	private boolean mWasUpdated = false;
	
	private ImageView twCurrent;	
	private LinearLayout body;
	private ImageView bootView;
	HorizontalScrollView recentBooksScrollView;
	
	private static final String WAS_UPDATED = "was_updated";
	
	  void saveData(boolean valueForSave) {
	    sPref = getPreferences(MODE_PRIVATE);
	    Editor ed = sPref.edit();
	    ed.putBoolean("rated", valueForSave);
	    ed.commit();
	  }
	  
	  boolean loadData() {
	    sPref = getPreferences(MODE_PRIVATE);
	    return sPref.getBoolean("rated", false);
	  }
	  
	  void saveStartDialogData(boolean valueForSave) {
		  sPref = getPreferences(MODE_PRIVATE);
		    Editor ed = sPref.edit();
		    ed.putBoolean("needStartDialog", valueForSave);
		    ed.commit();
	  }
	  
	  boolean loadStartDialogData() {
		  sPref = getPreferences(MODE_PRIVATE);
		    return sPref.getBoolean("needStartDialog", true);
	  }
	  
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getIntent().hasExtra(EXIT_ME)){
        	finish();
        	return;
        }
        
        
        FullReaderActivity.addToActivityStack(this);
        updateLocale();
        needToShowStartDialog = true;
        this.mActivity = this;
        final File dexInternalStoragePath = new File(getDir("dex", Context.MODE_PRIVATE),
                SECONDARY_DEX_NAME);
        ActionBar bar = getSupportActionBar();
        Drawable actionBarBackground = null;  
        switch(theme) {
        case IConstants.THEME_MYBLACK:
            setContentView(R.layout.reader_startup_theme_black_activity);
            actionBarBackground = getResources().getDrawable( com.fullreader.R.drawable.theme_black_action_bar );
            curTheme = "black";
        	break;
        case IConstants.THEME_LAMINAT:
            setContentView(R.layout.reader_startup_theme_laminat_activity);
            actionBarBackground = getResources().getDrawable( com.fullreader.R.drawable.theme_laminat_action_bar);
            curTheme = "laminat";
        	break;
        case IConstants.THEME_REDTREE:
            setContentView(R.layout.reader_startup_theme_redtree_activity);
            actionBarBackground = getResources().getDrawable( com.fullreader.R.drawable.theme_redtree_action_bar );
            curTheme = "redtree";
        	break;
        }
        bar.setBackgroundDrawable(actionBarBackground);
        setupUi();
		recentBooksScrollView = (HorizontalScrollView)findViewById(R.id.horizontalScroll);
		collection = new BookCollectionShadow();
		//collection.addListener(this);
		
		createFolderFonts();
		createFolderWallpapers();
		createBooksDirectory();
		SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(this);
		if(setting.getBoolean("first_start", true))
		{
			
			NetworkDatabase db = new SQLiteNetworkDatabase(getApplication());

			/*final UrlInfoCollection<UrlInfoWithDate> infos = new UrlInfoCollection<UrlInfoWithDate>();
			infos.addInfo(new UrlInfoWithDate(UrlInfo.Type.Catalog, "", MimeType.APP_ATOM_XML));
			
			ICustomNetworkLink myLink = new OPDSCustomNetworkLink(
					ICustomNetworkLink.INVALID_ID, INetworkLink.Type.Custom, "EpubBooks.ru", "EpubBooks.ru", null, null, infos);
			myLink.setUrl(UrlInfo.Type.Catalog, "http://www.epubbooks.ru/index.xml", MimeType.APP_ATOM_XML);
			NetworkDatabase.Instance().saveLink(myLink);
			
			//myLink = new OPDSCustomNetworkLink(
			//		ICustomNetworkLink.INVALID_ID, INetworkLink.Type.Custom, "Flibusta.net", "Flibusta.net", null, null, infos);
			//myLink.setUrl(UrlInfo.Type.Catalog, "http://flibusta.net/opds", MimeType.APP_ATOM_XML);
			//NetworkDatabase.Instance().saveLink(myLink);
			
			myLink = new OPDSCustomNetworkLink(
					ICustomNetworkLink.INVALID_ID, INetworkLink.Type.Custom, "lib.rus.ec", "lib.rus.ec", null, null, infos);
			myLink.setUrl(UrlInfo.Type.Catalog, "http://lib.rus.ec/opds", MimeType.APP_ATOM_XML);
			NetworkDatabase.Instance().saveLink(myLink);
			
			myLink = new OPDSCustomNetworkLink(
					ICustomNetworkLink.INVALID_ID, INetworkLink.Type.Custom, "http://coollib.net/opds", "http://coollib.net/opds", null, null, infos);
			myLink.setUrl(UrlInfo.Type.Catalog, "http://coollib.net/opds", MimeType.APP_ATOM_XML);
			NetworkDatabase.Instance().saveLink(myLink);
			
			myLink = new OPDSCustomNetworkLink(
					ICustomNetworkLink.INVALID_ID, INetworkLink.Type.Custom, "Zone4iPhone.ru", "Zone4iPhone.ru", null, null, infos);
			myLink.setUrl(UrlInfo.Type.Catalog, "http://www.zone4iphone.ru/catalog.php", MimeType.APP_ATOM_XML);
			NetworkDatabase.Instance().saveLink(myLink);*/
						
			Editor editeor = setting.edit();
			editeor.putBoolean("first_start", false);
			editeor.commit();
		}
		isAppRated = loadData();
		initTapjoy();
		//initAdMob();
		//Toast.makeText(getApplicationContext(), loadCurrentLanguage(), Toast.LENGTH_LONG).show();
		Log.d("SSActivity cur lang: ", loadCurrentLanguage());
		//showStartDialog();
		 
		// Запускаем службу для работы с Dropbox
		ZLBooleanOption DropboxSync = new ZLBooleanOption("Syncronization", "DropboxSync", false);
		if (DropboxSync.getValue()){
			Log.d("MyLog", "Dropbox is enabled");
			bindService(new Intent(this, DropboxService.class), mConnection, Context.BIND_AUTO_CREATE);
		}

		
		/*
		 * Открытие docx файла
		 * 
		 */
		try{
			String path = getIntent().getData().getPath();
			File f = new File(path);
			if (path!=null && path.length()>0){
				// Проверка версии
				if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB){
					// Если плагин уже установлен на устройстве
					if(checkDocxInstalledOnDevice()) {
						int lastIndex = path.lastIndexOf("/");
						String name = path.substring(lastIndex+1);
						FrDocument frDocument = new FrDocument(-1, name, path, FrDocument.DOCTYPE_DOCX, FrDocument.getDate());
						DatabaseHandler handler = new DatabaseHandler(this);
						long id = handler.hasFrDocument(frDocument);
						if (id == -1){
							handler.addFrDocument(frDocument);
						}
						else{
							frDocument.updateId((int)id);
							handler.updateFrDocumentLastDate(frDocument);
						}
						
						
						Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("org.plutext.DocxToHtml");
						LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
 						LaunchIntent.putExtra("pathToFile", path);
 						startActivity(LaunchIntent);
 						finish();
					}
					// Если нет - предлагаем пользователю его установить
					else{
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
				        builder.setCancelable(true);
				        String yesText = "";
				        String noText = "";
				        if(loadCurrentLanguage().equals("en")){
				        	builder.setMessage("For reading .docx files you need to install our docx plugin. Do it now?");
				        	yesText = "Yes";
				        	noText = "No";
						} else if(loadCurrentLanguage().equals("de")){
							builder.setMessage("Zum Lesen .docx-Dateien müssen Sie unsere docx-Plugin installieren. Tun Sie es jetzt?");
							yesText = "Ja";
				        	noText = "Nicht";
						} else if(loadCurrentLanguage().equals("fr")){
							builder.setMessage("Pour la lecture .docx vous devez installer notre plugin docx. Faites-le maintenant?");
							yesText = "Oui";
				        	noText = "Aucun";
						} else if(loadCurrentLanguage().equals("uk")){
							builder.setMessage("Для читання .docx файлів необхідно встановити наш плагін docx. Зробити це зараз?");
							yesText = "Так";
				        	noText = "Нi";
						} else if(loadCurrentLanguage().equals("ru")){
							builder.setMessage("Для чтения .docx файлов необходимо установить наш плагин docx. Сделайте это сейчас?");
							yesText = "Да";
				        	noText = "Нет";
						} else {
							if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
								builder.setMessage("Для чтения .docx файлов необходимо установить наш плагин docx. Сделать это сейчас?");
								yesText = "Да";
					        	noText = "Нет";
							} else if(Locale.getDefault().getDisplayLanguage().equals("українська")){
								builder.setMessage("Для читання .docx файлів необхідно встановити наш плагін docx. Зробити це зараз?");
								yesText = "Так";
					        	noText = "Нi";
							} else {
								builder.setMessage("For reading .docx files you need to install our docx plugin. Do it now?");
								yesText = "Yes";
					        	noText = "No";
							}
						}
				        builder.setInverseBackgroundForced(true);
			        	builder.setPositiveButton(yesText,
			                    new DialogInterface.OnClickListener() {
			                        @Override
			                        public void onClick(DialogInterface dialog,
			                                int which) {
			                        	AssetManager assetManager = getAssets();										
										InputStream in = null;
										OutputStream out = null;									
										try {
										    in = assetManager.open("docx.apk");
										    out = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath()+"/docx.apk");
										    byte[] buffer = new byte[1024];
										    
										    int read;
										    while((read = in.read(buffer)) != -1) {										
										        out.write(buffer, 0, read);
										    }
										    in.close();
										    in = null;	
										    out.flush();
										    out.close();
										    out = null;
										    Intent intent = new Intent(Intent.ACTION_VIEW);
										    intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/docx.apk")),
										        "application/vnd.android.package-archive");					
										    startActivity(intent);
										} catch(Exception e) {
											
										}
										
										saveDocxPlugin(true);
				                    	 Intent intent = new Intent(Intent.ACTION_VIEW)
				                    	 .setData(Uri.parse("file:///android_asset/docx.apk"))
				                    	 .setType("application/vnd.android.package-archive");
				                    	 startActivity(intent);
				                    	 
				 						// Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("org.plutext.DocxToHtml");
				 						// Log.d("pathToDOCXfile", tree.getTreeTitle());
				 						// LaunchIntent.putExtra("pathToFile", tree.getTreeTitle());
				 						// startActivity(LaunchIntent);
				                         dialog.dismiss(); 
				                         
			                        }
			                    });
				            builder.setNegativeButton(noText,
				                    new DialogInterface.OnClickListener() {
				                        @Override
				                        public void onClick(DialogInterface dialog,
				                                int which) {
				                            dialog.dismiss();
				                           // isActivityRunning(FullReaderActivity.class);
				                           // isActivityRunning(LibraryActivity.class);

				                        }
				                    });
					        AlertDialog alert = builder.create();
					        alert.getWindow().setLayout(600, 400);
					        alert.show();
					}
				}
				// Если же стоит версия Android меньше, чем 3
				else{
					String msg = "";
					if(loadCurrentLanguage().equals("en")){
						msg = "An ability to open docx files is  available only for devices with a version of Android 4.0 and above";
					}
					else if(loadCurrentLanguage().equals("de")){
						msg = "Die Fähigkeit, docx-Datei zu öffnen, ist nur für Geräte mit Android-Version 4.0 und höher verfügbar";
					}
					else if(loadCurrentLanguage().equals("fr")){
						msg = "Possibilité d'ouvrir docx n'est disponible que pour les appareils avec une version d'Android 4.0 et au-dessus";
					}
					else if(loadCurrentLanguage().equals("uk")){
						msg = "Можливість відкриття docx файлів доступна лише для пристроїв з версією Android 4.0 і вище";
					}
					else if(loadCurrentLanguage().equals("ru")){
						msg = "Возможность открытия docx файлов доступна только для устройств с версией Android 4.0 и выше";
					}
					else {
						if(Locale.getDefault().getDisplayLanguage().equals("русский")){
							msg = "Возможность открытия docx файлов доступна только для устройств с версией Android 4.0 и выше";
						}
						else if(Locale.getDefault().getDisplayLanguage().equals("українська")){
							msg = "Можливість відкриття docx файлів доступна лише для пристроїв з версією Android 4.0 і вище";
						}
						else{
							msg = "An ability to open docx files is  available only for devices with a version of Android 4.0 and above";
						}
					}
					Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
				}
			}
		
		}
		catch (Exception e){}
		
    }
	
	String loadCurrentLanguage() {
	    sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
	    return sPref.getString("curLanguage", "");
	}
	
	protected Boolean isActivityRunning(Class activityClass) {
	        ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
	        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
	        
	        for (ActivityManager.RunningTaskInfo task : tasks) {
	            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getClassName())) {
	            	try {
						((Activity) Class.forName(task.baseActivity.getClassName()).newInstance()).finish();
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	                return true;
	        }

	        return false;
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
	protected void onStart() {
    	super.onStart();
		
	}

    Handler handler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    		switch(msg.what){
    		case ON_BIND_TO_SERVER:
    			initRecent();
    			break;
    		}
    		super.handleMessage(msg);
    	}
    };
    
	private void setupUi() {
		
		((TextView)findViewById(R.id.fr_tw_current_label)).setText(ZLResource.resource("other").getResource("mainscr_book_current").getValue());
		((TextView)findViewById(R.id.fr_tw_lastopened_label)).setText(ZLResource.resource("other").getResource("mainscr_book_lastopened").getValue());
		((TextView)findViewById(R.id.fr_tw_network_label)).setText(ZLResource.resource("other").getResource("mainscr_libraries_netrwork").getValue());
		((TextView)findViewById(R.id.fr_tw_search_startscr)).setText(ZLResource.resource("other").getResource("mainscr_libraries_search").getValue());
		((TextView)findViewById(R.id.fr_tw_read_files_startscr)).setText(ZLResource.resource("library").getValue());
		((TextView)findViewById(R.id.fr_tw_files_on_device)).setText(ZLResource.resource("other").getResource("mainscr_libraries_filesys").getValue());
		
    	LinearLayout twLitRes = (LinearLayout)findViewById(R.id.fr_lo_litres_startscr);
    	LinearLayout twDeviceFiles = (LinearLayout)findViewById(R.id.fr_lo_files_on_device);
    	LinearLayout twLReadFiles = (LinearLayout)findViewById(R.id.fr_lo_read_files_startscr);
    	LinearLayout twSearch = (LinearLayout)findViewById(R.id.fr_lo_search_startscr);
    	
    	OnClickListener onClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
				case R.id.fr_lo_files_on_device: {
						Intent intent = new Intent(StartScreenActivity.this, LibraryActivity.class);
						intent.putExtra(LibraryActivity.OPEN_TREE_KEY, LibraryActivity.FILESYSTEM_TREE);
						startActivity(intent);
						break;
					}
				case R.id.fr_lo_search_startscr: {
					if(isNetworkConnected()) {
						Intent intent = new Intent(StartScreenActivity.this, NetworkLibraryPrimaryActivity.class);
						intent.putExtra(NetworkLibraryPrimaryActivity.START_REQUEST, NetworkLibraryPrimaryActivity.SEARCH_REQ);
						intent.putExtra("curTheme", curTheme);
						startActivity(intent);
						
					} else {
						if(loadCurrentLanguage().equals("ru")) {
							Toast.makeText(getApplicationContext(), "Нет активного подключения к интернету!", Toast.LENGTH_LONG).show();
						} else if(loadCurrentLanguage().equals("en")) {
							Toast.makeText(getApplicationContext(), "No active internet connection!", Toast.LENGTH_LONG).show();					
						} else if(loadCurrentLanguage().equals("de")) {
							Toast.makeText(getApplicationContext(), "Es gibt keine aktive Verbindung zum Internet!", Toast.LENGTH_LONG).show();
						} else if(loadCurrentLanguage().equals("fr")) {
							Toast.makeText(getApplicationContext(), "Il n'y a aucune connexion active à l'Internet!", Toast.LENGTH_LONG).show();
						} else if(loadCurrentLanguage().equals("uk")) {
							Toast.makeText(getApplicationContext(), "Немає активного підключення до інтернету!", Toast.LENGTH_LONG).show();
						} else {
							if(Locale.getDefault().getDisplayLanguage().equals("русский") || Locale.getDefault().getDisplayLanguage().equals("українська")) {
								Toast.makeText(getApplicationContext(), "Нет активного подключения к интернету!", Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(getApplicationContext(), "No active internet connection!", Toast.LENGTH_LONG).show();
							}	
						}
					}
					break;
	//					Intent intent = new Intent(StartScreenActivity.this, LibrarySearchActivity.class);
	//					intent.setAction(Intent.ACTION_SEARCH);
					}
				case R.id.fr_lo_read_files_startscr:{
						Intent intent = new Intent(StartScreenActivity.this, LibraryActivity.class);
						startActivity(intent);
						break;
					}
				}
			}
		};
		
		twLitRes.setOnClickListener(onClick);
		twDeviceFiles.setOnClickListener(onClick);
		twLReadFiles.setOnClickListener(onClick);
		twSearch.setOnClickListener(onClick);
    }
	
	private boolean isNetworkConnected() {
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		  NetworkInfo ni = cm.getActiveNetworkInfo();
		  if (ni == null) {
			  // There are no active networks.
			  return false;
		  } else
			  return true;
	}
    
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
	}
	
    @Override
	protected void onStop() {
    	super.onStop();
    	
	}
    
	@Override
    protected void onDestroy() {
		super.onDestroy();
		if (collection != null)	collection.removeListener(this);
    	if (DropboxService.isRunning && mConnection!=null){
    		Log.d("MyLog", "Unbinding service");
    		unbindService(mConnection);
    	}
    	
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int resInfo = 0, resQuote = 0, resBookmark = 0, resSettings = 0;
		super.onCreateOptionsMenu(menu);
	       switch(theme){
		        case IConstants.THEME_MYBLACK:
		        	resInfo = R.drawable.theme_black_but_info;
		        	resQuote = R.drawable.theme_black_but_citata;
		        	resBookmark = R.drawable.bookmarks_marble;
		        	resSettings = R.drawable.settings_icon_black;
		        	break;
		        case IConstants.THEME_LAMINAT:
		        	resInfo = R.drawable.theme_laminat_but_info;
		        	resQuote = R.drawable.theme_laminat_but_citata;
		        	resBookmark = R.drawable.bookmarks_laminat;
		        	resSettings = R.drawable.settings_icon;
		        	break;
		        case IConstants.THEME_REDTREE:
		        	resInfo = R.drawable.theme_redtree_but_info;
		        	resQuote = R.drawable.theme_redtree_but_citata;
		        	resBookmark = R.drawable.bookmarks_red;
		        	resSettings = R.drawable.settings_icon_red;
		        	break;
	        }
	       
	      
			//addMenuItem(menu, MENU_BOOKMARK, "bookmarks",
			//		resBookmark, false);
	        
	   		addMenuItem(menu, MENU_PREFERENCES, "preferences", 
	   				R.drawable.icon_settings, false);
	   		
	   		addMenuItem(menu, MENU_BOOKMARKS2, "bookmarks", 
	   				R.drawable.icon_mark, false);
	   		
	   		addMenuItem(menu, MENU_COLOR_MARKS, "menuColorMarks", 
	   				R.drawable.icon_clr_marks, false);
	   		
	   			if(loadCurrentLanguage().equals("en")){
	   		 		addMenuItem(menu, MENU_BOOKMARK, "Bookmarks", resBookmark, true).setTitle("Bookmarks");	
	   		 		addMenuItem(menu, MENU_QUOTE, "Quotes", resQuote, true).setTitle("Quotes");
		   		 	addMenuItem(menu, MENU_SETTINGS_ICON, "Settings", resSettings, true).setTitle("Settings");
		   			addMenuItem(menu, MENU_INFO, "menu_info", resInfo, true).setTitle("About FullReader");
		   			addMenuItem(menu, 20, "exit", R.drawable.exit, false).setTitle("Exit");
				} else if(loadCurrentLanguage().equals("de")){
					addMenuItem(menu, MENU_BOOKMARK, "Bookmarks", resBookmark, true).setTitle("Bookmarks");	
					addMenuItem(menu, MENU_QUOTE, "Zitate", resQuote, true).setTitle("Zitate");
					addMenuItem(menu, MENU_SETTINGS_ICON, "Settings", resSettings, true).setTitle("Einstellungen");
					addMenuItem(menu, MENU_INFO, "menu_info", resInfo, true).setTitle("Uber FullReader");
					addMenuItem(menu, 20, "exit", R.drawable.exit, false).setTitle("Ausweg");
				} else if(loadCurrentLanguage().equals("fr")){
					addMenuItem(menu, MENU_BOOKMARK, "Bookmarks", resBookmark, true).setTitle("Favoris");	
					addMenuItem(menu, MENU_QUOTE, "Citations", resQuote, true).setTitle("Citations");
					addMenuItem(menu, MENU_SETTINGS_ICON, "Settings", resSettings, true).setTitle("Paramètres");
					addMenuItem(menu, MENU_INFO, "menu_info", resInfo, true).setTitle("Sur FullReader");
					addMenuItem(menu, 20, "exit", R.drawable.exit, false).setTitle("Sortie");
				} else if(loadCurrentLanguage().equals("uk")){			
					addMenuItem(menu, MENU_BOOKMARK, "Закладки", resBookmark, true).setTitle("Закладки");
					addMenuItem(menu, MENU_QUOTE, "Citations", resQuote, true).setTitle("Цитати");
					addMenuItem(menu, MENU_SETTINGS_ICON, "Settings", resSettings, true).setTitle("Настройки");
					addMenuItem(menu, MENU_INFO, "menu_info", resInfo, true).setTitle("О нас");	 
					addMenuItem(menu, 20, "exit", R.drawable.exit, false).setTitle("Вихiд");
				} else if(loadCurrentLanguage().equals("ru")){
					addMenuItem(menu, MENU_BOOKMARK, "Bookmarks", resBookmark, true).setTitle("Закладки");
					addMenuItem(menu, MENU_QUOTE, "Citations", resQuote, true).setTitle("Цитаты");
					addMenuItem(menu, MENU_SETTINGS_ICON, "Settings", resSettings, true).setTitle("Настройки");
					addMenuItem(menu, MENU_INFO, "menu_info", resInfo, true).setTitle("О нас");
					addMenuItem(menu, 20, "exit", R.drawable.exit, false).setTitle("Выход");
				} else {
					if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
						addMenuItem(menu, MENU_BOOKMARK, "Bookmarks", resBookmark, true).setTitle("Закладки");
						addMenuItem(menu, MENU_QUOTE, "Цитаты", resQuote, true).setTitle("Цитаты");
						addMenuItem(menu, MENU_SETTINGS_ICON, "Settings", resSettings, true).setTitle("Настройки");
						addMenuItem(menu, MENU_INFO, "menu_info", resInfo, true).setTitle("О FullReader");  
						addMenuItem(menu, 20, "exit", R.drawable.exit, false).setTitle("Выход");
					} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
						addMenuItem(menu, MENU_INFO, "menu_info", resInfo, true).setTitle("О FullReader");	
						addMenuItem(menu, MENU_QUOTE, "Цитати", resQuote, true).setTitle("Вихiд");
						addMenuItem(menu, MENU_SETTINGS_ICON, "Settings", resSettings, true).setTitle("Настройки");
						addMenuItem(menu, MENU_BOOKMARK, "Bookmarks",resBookmark, true).setTitle("Закладки"); 
						addMenuItem(menu, 20, "exit", R.drawable.exit, false).setTitle("Вихiд");
					} else {
						addMenuItem(menu, MENU_BOOKMARK, "Bookmarks", resBookmark, true).setTitle("Bookmarks");
						addMenuItem(menu, MENU_QUOTE, "Quotes", resQuote, true).setTitle("Quotes");
						addMenuItem(menu, MENU_SETTINGS_ICON, "Settings", resSettings, true).setTitle("Settings");
						addMenuItem(menu, MENU_INFO, "menu_info", resInfo, true).setTitle("About FullReader");	
						addMenuItem(menu, 20, "exit", R.drawable.exit, false).setTitle("Exit");
					}
				}
	   	 switch(theme) {
	        case IConstants.THEME_MYBLACK:
	        	addMenuItem(menu, MENU_GOOGLE_PLUS, "Google+",
						R.drawable.google_plus_marble, true).setTitle("Google +");
	        	break;
	        case IConstants.THEME_LAMINAT:
	        	addMenuItem(menu, MENU_GOOGLE_PLUS, "Google+",
						R.drawable.google_plus_laminat, true).setTitle("Google +");
	        	break;
	        case IConstants.THEME_REDTREE:
	        	addMenuItem(menu, MENU_GOOGLE_PLUS, "Google+",
						R.drawable.google_plus_red_tree, true).setTitle("Google +");
	        	break;
	        }
	   	 
	   	 
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}
	
	@Override
	public void onBackPressed() {		
		if(!loadData()) {
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
	        builder.setCancelable(true);
	        String yesText = "";
	        String noText = "";
	        if(loadCurrentLanguage().equals("en")){
	        	builder.setMessage("Did you like our application? \n"+"Do you want rate app now?");
	        	yesText = "Yes";
	        	noText = "No";
			} else if(loadCurrentLanguage().equals("de")){
				builder.setMessage("Haben Sie unsere Anwendung gerne? \n"+"Haben Sie Rate App jetzt wollen?");
				yesText = "Ja";
	        	noText = "Nicht";
			} else if(loadCurrentLanguage().equals("fr")){
				builder.setMessage("Vous avez aimé notre demande? \n"+"Voulez-vous app taux maintenant?");
				yesText = "Oui";
	        	noText = "Aucun";
			} else if(loadCurrentLanguage().equals("uk")){
				builder.setMessage("Вам сподобалась наша програма?\n"+"Поставити оцінку зараз?");
				yesText = "Так";
	        	noText = "Нi";
			} else if(loadCurrentLanguage().equals("ru")){
				builder.setMessage("Вам понравилось наше приложение?\n"+"Поставить оценку сейчас?");
				yesText = "Да";
	        	noText = "Нет";
			} else {
				if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
					builder.setMessage("Вам понравилось наше приложение?\n"+"Поставить оценку сейчас?");
					yesText = "Да";
		        	noText = "Нет";
				} else if(Locale.getDefault().getDisplayLanguage().equals("українська")){
					builder.setMessage("Вам сподобалась наша програма?\n"+"Поставити оцінку зараз?");
					yesText = "Так";
		        	noText = "Нi";
				} else {
					builder.setMessage("Did you like our application? \n"+"Do you want rate app now?");
					yesText = "Yes";
		        	noText = "No";
				}
			}
	       
	        
	        /*if(Locale.getDefault().getDisplayLanguage().equals("русский") || Locale.getDefault().getDisplayLanguage().equals("українська")) {
	        	builder.setMessage("Вам понравилось наше приложение.\n"+"Поставить оценку сейчас?");
	        } else {
	        	builder.setMessage("Did you like our application? Do you want rate app now?");
	        }*/
	        builder.setInverseBackgroundForced(true);
	        if(Locale.getDefault().getDisplayLanguage().equals("русский") || Locale.getDefault().getDisplayLanguage().equals("українська")) {
	        builder.setPositiveButton(yesText,
	                new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog,
	                            int which) {
	                    	Uri uri = Uri.parse("market://details?id=com.fullreader");
	        				Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        				            try {
        				                startActivity(myAppLinkToMarket);
        				            } catch (ActivityNotFoundException e) {
    				            		
        				            }
        				            isAppRated = true;
        				            saveData(isAppRated);
	                        dialog.dismiss();
	                    }
	                });
	        builder.setNegativeButton(noText,
	                new DialogInterface.OnClickListener() {
	                    @Override
	                    public void onClick(DialogInterface dialog,
	                            int which) {
	                        dialog.dismiss();
	                        unbindService(mConnection);
	                        finish();
	                        android.os.Process.killProcess(android.os.Process.myPid());
	                    }
	                });
	        } else {
	        	builder.setPositiveButton("Yes",
	                    new DialogInterface.OnClickListener() {
	                        @Override
	                        public void onClick(DialogInterface dialog,
	                                int which) {
	                        	Uri uri = Uri.parse("market://details?id=com.fullreader");
	            				Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);	
	            				            try {
	            				                startActivity(myAppLinkToMarket);
	            				            } catch (ActivityNotFoundException e) {
	
	            				            }
	                            dialog.dismiss();
	                        }
	                    });
	            builder.setNegativeButton("No",
	                    new DialogInterface.OnClickListener() {
	                        @Override
	                        public void onClick(DialogInterface dialog,
	                                int which) {
	                            dialog.dismiss();
	                            unbindService(mConnection);
	                            finish();
	                            android.os.Process.killProcess(android.os.Process.myPid());
	                        }
	                    });
	        }
	        AlertDialog alert = builder.create();
	        alert.getWindow().setLayout(600, 400);
	        alert.show();
		} else {
			Log.d("MyLog", "Before updating");
          //  mDbxHelper.updateAll();
			finish();
		}
	}
	
	public void initTapjoy() {
		Hashtable<String, String> flags = new Hashtable<String, String>();
		flags.put(TapjoyConnectFlag.ENABLE_LOGGING, "true");

		TapjoyConnect.requestTapjoyConnect(getApplicationContext(), APP_ID, SECRET_ID, flags,
			new TapjoyConnectNotifier() {
				@Override
				public void connectSuccess() {
					onConnectSuccess();
				}

				@Override
				public void connectFail() {
					onConnectFail();
				}	
		});
		
		TapjoyConnect.getTapjoyConnectInstance().setEarnedPointsNotifier(
			new TapjoyEarnedPointsNotifier() {
				@Override
				public void earnedTapPoints(int count) {
					//Touch.game().tapPointsReceived(count);
				}
			});
	}

	public void onConnectSuccess() {
		Log.d("TAG", "Tapjoy connect successful.");
	}

	public void onConnectFail() {
		Log.e("TAG", "Tapjoy connect call failed.");
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SEARCH:{
			Intent intent = new Intent(StartScreenActivity.this, LibraryActivity.class);
			intent.putExtra(LibraryActivity.OPEN_TREE_KEY, LibraryActivity.SEARCHREQUEST);
			startActivity(intent);
			return true;
			}
		case MENU_QUOTE:
			//startActivity(new Intent(getApplicationContext(), QuotesFragmentActivity.class));
			startActivity(new Intent(getApplicationContext(), com.webprestige.fr.citations.QuotesActivity.class));
			return true;
		case MENU_COLOR_MARKS:
			mActivity.startActivity(new Intent(mActivity, com.webprestige.fr.citations.QuotesActivity.class)
			.putExtra("isRunFromBook", true)
			.putExtra("cIndex", ReaderApp.quotesCIndex)
			.putExtra("pIndex", ReaderApp.quotesPIndex)
			.putExtra("eIndex", ReaderApp.quotesEIndex)
			.putExtra("fromBook", true)
			.putExtra(com.webprestige.fr.citations.QuotesActivity.SHOW_COLOR_MARKS, true));
			return true;
		case MENU_BOOKMARK:
			//startActivity(new Intent(getApplicationContext(), BookmarkFragmentActivity.class));
			startActivity(new Intent(getApplicationContext(), com.webprestige.fr.bookmarks.BookmarksActivity.class).putExtra("fromStartScreen", true));
			return true;
		case MENU_INFO:
			startActivity(new Intent(getApplicationContext(), FaqActivity.class));
			return true;
		case MENU_SETTINGS_ICON:
		    Intent intentSettings = new Intent(this, PreferenceActivity.class);
		    startActivityForResult(intentSettings, FullReaderActivity.REQUEST_PREFERENCES);
		    org.geometerplus.android.fbreader.preferences.PreferenceActivity.isOpenFromPdfDjvu = false;
			//startActivity(intent);
		    return true;
		case 20:
			android.os.Process.killProcess(android.os.Process.myPid());
			return true;
		case MENU_GOOGLE_PLUS:
			try {
				Intent shareIntent = new PlusShare.Builder(StartScreenActivity.this)
	            .setType("text/plain")
	            .setText("The best reader for Android! http://play.google.com/store/apps/details?id=com.fullreader")
	            .setContentUrl(Uri.parse("https://developers.google.com/+/"))
	            .getIntent();
	
				startActivityForResult(shareIntent, 0);
			} catch(ActivityNotFoundException e) {
				if(loadCurrentLanguage().equals("en")){
					Toast.makeText(getBaseContext(), "Add Google account.", Toast.LENGTH_LONG).show();
	    		} else if(loadCurrentLanguage().equals("de")){
	    			Toast.makeText(getBaseContext(), "Google Konto hinzufügen.", Toast.LENGTH_LONG).show();
	    		} else if(loadCurrentLanguage().equals("fr")){
	    			Toast.makeText(getBaseContext(), "Ajouter un compte Google.", Toast.LENGTH_LONG).show();
	    		} else if(loadCurrentLanguage().equals("uk")){
	    			Toast.makeText(getBaseContext(), "Додайте аккаунт Google.", Toast.LENGTH_LONG).show();
	    		} else if(loadCurrentLanguage().equals("ru")){
	    			Toast.makeText(getBaseContext(), "Добавьте аккаунт Google.", Toast.LENGTH_LONG).show();
	    		} else {
	    			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
	    				Toast.makeText(getBaseContext(), "Добавьте аккаунт Google.", Toast.LENGTH_LONG).show();
	    			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
	    				Toast.makeText(getBaseContext(), "Додайте аккаунт Google.", Toast.LENGTH_LONG).show();
	    			} else {
	    				Toast.makeText(getBaseContext(), "Add Google account.", Toast.LENGTH_LONG).show();
	    			}
	    		}
			}
			return true;
			
		case MENU_PREFERENCES:{
		    Intent intent = new Intent(this, PreferenceActivity.class);
		    startActivityForResult(intent, FullReaderActivity.REQUEST_PREFERENCES);
		    org.geometerplus.android.fbreader.preferences.PreferenceActivity.isOpenFromPdfDjvu = false;
			//startActivity(intent);
		    return true;
			}
		case MENU_BOOKMARKS2:
			startActivity(new Intent(getApplicationContext(), com.webprestige.fr.bookmarks.BookmarksActivity.class));
			return true;
		default:
			return false;
		}
	}
	
	private void showStartDialog() {
		File root = new File(Environment.getExternalStorageDirectory(), "FullReader Unlocker");
		//File root = new File(Environment.getDataDirectory(), "FullReader Unlocker");
		Log.d("file_path", root.getAbsolutePath());
        if (root.exists()) {
        	 File accessFile = new File(root, "access-file.txt");
        	 if(!accessFile.exists() && needToShowStartDialog) {
        			AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	        builder.setCancelable(true);
        	        String yesText = "";
        	        String noText = "";
        	        String neutralText = "";
        	        builder.setInverseBackgroundForced(true);
        	        if(loadCurrentLanguage().equals("en")){
        	        	builder.setMessage("Remove ads and get access to view Docx file format can by purchasing addition FullReader Plus.(Docx plugin works only on Android 4+)");
        	        	yesText = "Buy";
        		        noText = "Do not remind";
        		        neutralText = "Remind later";
            		} else if(loadCurrentLanguage().equals("de")){
            			builder.setMessage("Anzeigen entfernen und erhalten Sie Zugang zu docx-Dateiformat kann durch den Kauf hinaus FullReader Plus-anzuzeigen.(Docx-Plugin funktioniert nur auf Android 4 +)");
            			yesText = "Kauf";
        		        noText = "erinnern Sie sich nicht";
        		        neutralText = "später erinnern";
            		} else if(loadCurrentLanguage().equals("fr")){
            			builder.setMessage("Retirer annonces et tu auras accès à voir docx format de fichier peut en achetant plus FullReader Plus.(Docx plugin ne fonctionne que sur Android 4 +)");
            			yesText = "Acheter";
        		        noText = "ne pas rappeler";
        		        neutralText = "Rappelez-moi plus tard";
            		} else if(loadCurrentLanguage().equals("uk")){
            			builder.setMessage("Прибрати рекламу і отримати доступ до перегляду файлів у форматі Docx можна пріобрев додаток FullReader Plus.(Docx плагін працює тільки на Android 4 +)");
            			yesText = "Придбати";
        		        noText = "Не нагадувати";
        		        neutralText = "Нагадати пізніше";
            		} else if(loadCurrentLanguage().equals("ru")){
            			builder.setMessage("Убрать рекламу и получить доступ к просмотру файлов в формате Docx можно приобрев дополнение FullReader Plus.(Docx плагин работает только на Android 4+)");
            			yesText = "Приобрести";
        		        noText = "Не напоминать";
        		        neutralText = "Напомнить позже";
            		} else {
            			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
            				builder.setMessage("Убрать рекламу и получить доступ к просмотру файлов в формате Docx можно приобрев дополнение FullReader Plus.(Docx плагин работает только на Android 4+)");
            				yesText = "Приобрести";
            		        noText = "Не напоминать";
            		        neutralText = "Напомнить позже";
            			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
            				builder.setMessage("Прибрати рекламу і отримати доступ до перегляду файлів у форматі Docx можна пріобрев додаток FullReader Plus.(Docx плагін працює тільки на Android 4 +)");
            				yesText = "Придбати";
            		        noText = "Не нагадувати";
            		        neutralText = "Нагадати пізніше";
            			} else {
            				builder.setMessage("Remove ads and get access to view Docx file format can by purchasing addition FullReader Plus.(Docx plugin works only on Android 4+)");
            				yesText = "Buy";
            		        noText = "Do not remind";
            		        neutralText = "Remind later";
            			}
            		}
        	        
        	        builder.setPositiveButton(yesText,
        	                new DialogInterface.OnClickListener() {
        	                    @Override
        	                    public void onClick(DialogInterface dialog,
        	                            int which) {
        	                    			needToShowStartDialog = false;
        	                    			Uri uri = Uri.parse("market://details?id=com.webprestige.frreaderunlocker");
        	                				Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
        	                			            try {
        	                			                startActivity(myAppLinkToMarket);
        	                			            } catch (ActivityNotFoundException e) {
        	                		            		
        	                			            }
        	    				            saveStartDialogData(false);
        	                        dialog.dismiss();
        	                    }
        	                });
        	        /*builder.setNeutralButton(neutralText,
        	                new DialogInterface.OnClickListener() {
        	                    @Override
        	                    public void onClick(DialogInterface dialog,
        	                            int which) {
        	                    	needToShowStartDialog = false;
        	                        dialog.dismiss();
        	                    }
        	                });*/
        	        builder.setNegativeButton(noText,
        	                new DialogInterface.OnClickListener() {
        	                    @Override
        	                    public void onClick(DialogInterface dialog,
        	                            int which) {
        				            saveStartDialogData(false);
        	                    }
        	                });
        	        AlertDialog alert = builder.create();
        	        alert.getWindow().setLayout(600, 400);
        	        alert.show();
        	 }
        }
		
	}
	
	private void alertDialogOnDeleteRecent(final FrDocument frDocument, final ImageView bootView) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        String yesText = "";
        String noText = "";
        String neutralText = "";
        builder.setInverseBackgroundForced(true);
    	builder.setMessage("Удаление данной книги приведет к ее удалению из списков текущих?");
    	yesText = "Yes";
        noText = "No";
        if(loadCurrentLanguage().equals("en")){
        	builder.setMessage("Removal of this book will lead to its removal from the list of current?");
        	yesText = "Yes";
	        noText = "No";
		} else if(loadCurrentLanguage().equals("de")){
			builder.setMessage("Die Entfernung dieses Buch wird auf seiner Entfernung aus der Liste der aktuellen zu führen?");
			yesText = "ja";
	        noText = "nicht";
		} else if(loadCurrentLanguage().equals("fr")){
			builder.setMessage("Enlèvement de ce livre permettra à son retrait de la liste des cours?");
			yesText = "oui";
	        noText = "aucun";
		} else if(loadCurrentLanguage().equals("uk")){
			builder.setMessage("Видалення даної книги призведе до її видалення зі списків поточних?");
			yesText = "Так";
	        noText = "Нi";
		} else if(loadCurrentLanguage().equals("ru")){
			builder.setMessage("Удаление данной книги приведет к ее удалению из списков текущих?");
			yesText = "Да";
	        noText = "Нет";
		} else {
			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
				builder.setMessage("Удаление данной книги приведет к ее удалению из списков текущих?");
				yesText = "Да";
		        noText = "Нет";
			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
				builder.setMessage("Видалення даної книги призведе до її видалення зі списків поточних?");
				yesText = "Так";
		        noText = "Hi";
			} else {
				builder.setMessage("Removal of this book will lead to its removal from the list of current?");
				yesText = "Yes";
		        noText = "No";
			}
		}
        builder.setPositiveButton(yesText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    	DatabaseHandler dbHandler = new DatabaseHandler(StartScreenActivity.this);
                    	dbHandler.deleteFrDocument(frDocument);
                    	if (frDocument.getDoctype() == FrDocument.DOCTYPE_FB)collection.removeBook(frDocument.getBook(), false);
                    	bootView.setVisibility(View.INVISIBLE);
                        dialog.dismiss();
                    	initRecent();
	                	
                    }
                });
        builder.setNegativeButton(noText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                            int which) {
                    	isNeedToDelete = false;
                    	 dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.getWindow().setLayout(600, 400);
        alert.setCanceledOnTouchOutside(true);
        alert.show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FullReaderActivity.REQUEST_PREFERENCES:			
			break;
		}
	}
	
	private Bitmap drawTextOnBitmap(Bitmap bmp, String text) {
		
		Typeface fontFace = Typeface.createFromAsset(getAssets(),"fonts/tahoma_bold.ttf"); 
		Typeface face = Typeface.create(fontFace, Typeface.BOLD);
		
		
		  Bitmap copyBmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
		  Canvas canvas = new Canvas(copyBmp);
		  // new antialised Paint
		  Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		  
		  paint.setTypeface(face);
		  // text color - #3D3D3D
		  paint.setColor(Color.BLACK);
		  // text size in pixels
		  paint.setTextSize((int) (16));
		  // text shadow
		  //paint.setShadowLayer(1f, 0f, 1f, Color.BLACK);
		  paint.setSubpixelText(true);
		 
		  // draw text to the Canvas center
		  Rect bounds = new Rect();
		  paint.getTextBounds(text, 0, text.length(), bounds);
		  //int x = (copyBmp.getWidth() - bounds.width()+40)/2;
		  int x = 0;
		  int y = (copyBmp.getHeight() + bounds.height()-6)/2;
		 
		  canvas.drawText(text, x+5, y, paint);
		  
		  return copyBmp;
	}
	
	private void setupCoverForFRDocuments(FrDocument document, ImageView twCurrent) {
		
		Drawable bg = getResources().getDrawable(R.drawable.shadow_for_book);	
		twCurrent.setVisibility(View.VISIBLE);		
		twCurrent.setTag(document);
		final DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		int COVER_HEIGHT = (int) ((float)136/146*bg.getIntrinsicHeight());
		int COVER_WIDTH = (int) ((float)98/150*bg.getIntrinsicHeight());
		
			Bitmap coverBitmap = null;
		//если у книги нету обложки, то выводим название книги
		if (coverBitmap == null) {
			//Toast.makeText(getApplicationContext(), book.getTitle(), Toast.LENGTH_LONG).show();
			//Bitmap coverBookImage = BitmapFactory.decodeResource(getResources(), R.drawable.book);
			Bitmap coverBookImage = null;
			if (document.getName().toLowerCase().contains("cbz") || document.getName().toLowerCase().contains("cbr")){
				coverBookImage = BitmapFactory.decodeResource(getResources(), R.drawable.icon_cbz_shadow);
			}
			else
			if (document.getName().toLowerCase().contains("odt") || document.getName().toLowerCase().contains("docx")
					|| document.getName().toLowerCase().contains("xps") || document.getName().toLowerCase().contains("txt")){
				coverBookImage = BitmapFactory.decodeResource(getResources(), R.drawable.book);
			}
			else
			if (document.getName().toLowerCase().contains("pdf") || document.getName().toLowerCase().contains("djvu")){
				coverBookImage = BitmapFactory.decodeResource(getResources(), R.drawable.icon_pdf_shadow);
			}
			
			coverBitmap = drawTextOnBitmap(coverBookImage, document.getName());
		}
		
		float scaleWidth = ((float) COVER_WIDTH) / coverBitmap.getWidth();
		float scaleHeight = ((float) COVER_HEIGHT) / coverBitmap.getHeight();
		
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		
		Bitmap resized = Bitmap.createBitmap(coverBitmap, 0, 0, coverBitmap.getWidth(), coverBitmap.getHeight(), matrix, false);
		
		Drawable top = new BitmapDrawable(getResources(), resized);
		
		LayerDrawable cover = new LayerDrawable(new Drawable[]{bg, top});
		cover.setLayerInset(1, 
				bg.getIntrinsicWidth()/2-top.getIntrinsicWidth()/2, 
				0, 
				bg.getIntrinsicWidth()/2-top.getIntrinsicWidth()/2, 
				bg.getIntrinsicHeight()-top.getIntrinsicHeight());
		
		twCurrent.setImageDrawable(cover);
	}

	private void setupCover(FrDocument document, ImageView twCurrent) {
		Book book = document.getBook();
		if(book == null){
			twCurrent.setVisibility(View.INVISIBLE);
			return;
		}
		Drawable bg = getResources().getDrawable(R.drawable.shadow_for_book);	
		twCurrent.setVisibility(View.VISIBLE);		
		twCurrent.setTag(document);
		
		final DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		
		
		int COVER_HEIGHT = (int) ((float)136/146*bg.getIntrinsicHeight());
		int COVER_WIDTH = (int) ((float)98/150*bg.getIntrinsicHeight());
		
		final ZLImage image = BookUtil.getCover(book);
		Bitmap coverBitmap = null;
		if (image != null) {
			if(image instanceof ZLLoadableImage) {
				final ZLLoadableImage loadableImage = (ZLLoadableImage)image;
				if (!loadableImage.isSynchronized()) {
					loadableImage.synchronize();
				}
			}
			final ZLAndroidImageData data = ((ZLAndroidImageManager)ZLAndroidImageManager.Instance()).getImageData(image);
			if(data == null) {
				return;
			}
			coverBitmap = data.getBitmap(COVER_WIDTH, COVER_HEIGHT); //(2 * maxWidth, 2 * maxHeight);
		}		
		
		//если у книги нету обложки, то выводим название книги
		if (coverBitmap == null) {
			//Toast.makeText(getApplicationContext(), book.getTitle(), Toast.LENGTH_LONG).show();
			Bitmap coverBookImage = BitmapFactory.decodeResource(getResources(), R.drawable.book);
			coverBitmap = drawTextOnBitmap(coverBookImage, book.getTitle());
		}
		
		float scaleWidth = ((float) COVER_WIDTH) / coverBitmap.getWidth();
		float scaleHeight = ((float) COVER_HEIGHT) / coverBitmap.getHeight();
		
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		
		Bitmap resized = Bitmap.createBitmap(coverBitmap, 0, 0, coverBitmap.getWidth(), coverBitmap.getHeight(), matrix, false);
		
		Drawable top = new BitmapDrawable(getResources(), resized);
		
		LayerDrawable cover = new LayerDrawable(new Drawable[]{bg, top});
		cover.setLayerInset(1, 
				bg.getIntrinsicWidth()/2-top.getIntrinsicWidth()/2, 
				0, 
				bg.getIntrinsicWidth()/2-top.getIntrinsicWidth()/2, 
				bg.getIntrinsicHeight()-top.getIntrinsicHeight());
		
		twCurrent.setImageDrawable(cover);
		//twCurrent.setText(book.getTitle());
	}
	
	@Override
	public void onBookEvent(BookEvent event, Book book) {
    	initRecent();
    	
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d("MyLog", "On pause");
		collection.unbind();
		TapjoyConnect.getTapjoyConnectInstance().appPause();
	}
	
	public boolean save() {
	    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(StartScreenActivity.this);
	    SharedPreferences.Editor mEdit1 = sp.edit();
	    mEdit1.putInt("Status_size", ReaderApp.PDF_DJVU_BOOKS.size()); /* sKey is an array */ 

	    for(int i=0;i<ReaderApp.PDF_DJVU_BOOKS.size();i++) {
	        mEdit1.remove("Status_" + i);
	        mEdit1.putString("Status_" + i, ReaderApp.PDF_DJVU_BOOKS.get(i).toString());  
	    }

	    return mEdit1.commit();     
	}
	
	public void loadArray(Context mContext) {  
	    SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(mContext);
	    ReaderApp.PDF_DJVU_BOOKS.clear();
	    int size = mSharedPreference1.getInt("Status_size", 0);  

	   // for(int i=0;i<size;i++) {
	    //	ReaderApp.PDF_DJVU_BOOKS.add(mSharedPreference1.getString("Status_" + i, null));
	   // }
	}

	@Override
	protected void onResume() {
		super.onResume();
		collection.bindToService(this, new Runnable() {
			public void run() {
				new Thread() {
					public void run() {
						Log.d("MyLog", "Sending message");
						handler.sendEmptyMessage(ON_BIND_TO_SERVER);
					}
				}.start();
			}
		});
		//initRecent();
		/*if(PDF_BOOK_OPENED) {
			
			LinearLayout body = (LinearLayout) findViewById(R.id.fr_ll_lastopened_body_start_scr);
			body.removeAllViews();
			//HorizontalScrollView recentBooksScrollView = (HorizontalScrollView)findViewById(R.id.scroll_view_recent_books);
			Log.d("recent books collection size: ", String.valueOf(collection.recentBooks().size()));
			
			for (final PdfBook book : ReaderApp.PDF_DJVU_BOOKS) {
				final ImageView bootView = new ImageView(this);
				//TextView bootView = (TextView)LayoutInflater.from(this).inflate(R.layout.book_label, null);
				bootView.setId(1);
				
				OnClickListener onClick = new OnClickListener() {
					@Override
					public void onClick(View v) {
						if(v.getId() == 0) {
							try {
								FullReaderActivity.openBookActivity(StartScreenActivity.this, (Book) v.getTag(), null);
							} catch (BookReadingException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							Uri uri = Uri.parse(book.getPath());
							Intent newIntent = new Intent(StartScreenActivity.this, MuPDFActivity.class);
							newIntent.setAction(Intent.ACTION_VIEW);
			    			newIntent.setData(uri);
			    			newIntent.setDataAndType(Uri.fromFile(new File(book.getPath())),"");
			    			
							startActivity(newIntent);
							Toast.makeText(getBaseContext(), "pdf book", Toast.LENGTH_LONG).show();
						}
					}};
				bootView.setOnClickListener(onClick);
				setupCoverForPDFBook(book.getTitle(), bootView);
				
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
					
					layoutParams.setMargins(0, 0, 0, 5);
					
					body.addView(bootView, layoutParams);
					
					
			}
			
			PDF_BOOK_OPENED = false;
		}*/
		
		/*if(!ReaderApp.PDF_DJVU_BOOKS.isEmpty()) {
			for(PdfBook pdfBook : ReaderApp.PDF_DJVU_BOOKS) {
				Log.d("PDF BOOK: ", pdfBook);
			}
		}*/
		TapjoyConnect.getTapjoyConnectInstance().appResume();
	}
	
	@Override
	public void onBuildEvent(Status status) {
    	initRecent();
	}
	
	private void createFolderFonts() {
		File folder = new File(Environment.getExternalStorageDirectory() + "/Fonts");
		if(!folder.exists()) {
			folder.mkdir();
		}
	}
	
	private void createFolderWallpapers() {
		File folder = new File(Environment.getExternalStorageDirectory() + "/Wallpapers");
		if(!folder.exists()) {
			folder.mkdir();
		}
	}
	
	private void createBooksDirectory(){
		File folder = new File(Paths.cardDirectory() + "/Books");
		if (!folder.exists()){
			folder.mkdir();
		}
	}

	private void initRecent() {
		Log.d("MyLog", "Init recent");
		DatabaseHandler dbHandler = new DatabaseHandler(this);
		ArrayList<FrDocument> frDocuments = dbHandler.getAllFrDocuments();
			
		twCurrent = (ImageView) findViewById(R.id.fr_tw_current_startscr);	
		body = (LinearLayout) findViewById(R.id.fr_ll_lastopened_body_start_scr);
		
		updateFrDocumentsCollection(frDocuments);
		
		if (frDocuments.size() == 0) {
			twCurrent.setVisibility(View.INVISIBLE);
			View view = body.getChildAt(0);
			view.setVisibility(View.INVISIBLE);
			return;
		}
		twCurrent.setVisibility(View.VISIBLE);
		FrDocument frDocument = frDocuments.get(0);
		
		// ------- Текущий документ
		if (frDocument.getDoctype() == FrDocument.DOCTYPE_FB){
			setupCover(frDocument, twCurrent);
			twCurrent.setOnClickListener(onClick);
			twCurrent.setOnLongClickListener(onLongClickListener);
		}
		else{
			setupCoverForFRDocuments(frDocuments.get(0), twCurrent);
			twCurrent.setOnClickListener(onClick);
			twCurrent.setOnLongClickListener(onLongClickListener);
		}
		
		// ------- Последние просмотренные документы
		body.removeAllViews();
		for (int i = 0; i<frDocuments.size(); i++) {
			frDocument = frDocuments.get(i);
			bootView = new ImageView(this);
			bootView.setId(i);
			bootView.setOnClickListener(onClick);
			bootView.setOnLongClickListener(onRecentLongClick);
			if (frDocument.getDoctype() == FrDocument.DOCTYPE_FB){
				setupCover(frDocument, bootView);
			}
			else{
				setupCoverForFRDocuments(frDocument, bootView);
			}
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(0, 0, 0, 5);
			body.addView(bootView, layoutParams);
		}
		if (recentBooksScrollView != null){
			recentBooksScrollView.fullScroll(ScrollView.FOCUS_LEFT);
		}
		
	}
    
	private void updateLocale() {
        String localeName = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getString(IConstants.PREF_LOCALE, "ru");
        
        Resources res = this.getResources();
        Configuration conf = res.getConfiguration();
        DisplayMetrics dm = res.getDisplayMetrics();
        conf.locale = new Locale(localeName.toLowerCase());
        res.updateConfiguration(conf, dm);
	}
	
	// File I/O code to copy the secondary dex file from asset resource to internal storage.
    private boolean prepareDex(File dexInternalStoragePath) {
        BufferedInputStream bis = null;
        OutputStream dexWriter = null;
        
        try {
            bis = new BufferedInputStream(getAssets().open(SECONDARY_DEX_NAME));
            dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath));
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while((len = bis.read(buf, 0, BUF_SIZE)) > 0) {
                dexWriter.write(buf, 0, len);
            }
            dexWriter.close();
            bis.close();
            return true;
        } catch (IOException e) {
            if (dexWriter != null) {
                try {
                    dexWriter.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            return false;
        }
    }
    
    private class PrepareDexTask extends AsyncTask<File, Void, Boolean> {
    	
        @Override
        protected void onCancelled() {
            super.onCancelled();
           
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        
        }

        @Override
        protected Boolean doInBackground(File... dexInternalStoragePaths) {
            prepareDex(dexInternalStoragePaths[0]);
            return null;
        }
    }
    
    private boolean checkDocxInstalledOnDevice() {
		boolean docxExist = false;
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List pkgAppsList = getApplicationContext().getPackageManager().queryIntentActivities( mainIntent, 0);
		for(int i=0;i<pkgAppsList.size();i++) {
			if(pkgAppsList.get(i).toString().contains("org.plutext.DocxToHtml")) {
				docxExist = true;
			}
		}
		return docxExist;
	}
    
    private void saveDocxPlugin(boolean docxInstalled) {
		 SharedPreferences sPref = getBaseContext().getSharedPreferences("docxPrefs", MODE_PRIVATE);
		 Editor ed = sPref.edit();
		 ed.putBoolean("isDocxInstalled", docxInstalled);
		 ed.commit();
	}
    
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(WAS_UPDATED, mWasUpdated);
    }
    
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mWasUpdated = savedInstanceState.getBoolean(WAS_UPDATED);
    }
    
    
    private ServiceConnection mConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mDbxService = IDropboxService.Stub.asInterface(service);
			try {
				mDbxService.registerCallback(mCallback);
				if (!mWasUpdated)mDbxService.getSyncData();
				//DropboxHelper.Instance(mActivity).deleteDatastore();
			} catch (RemoteException e) {
				Log.d("MyLog", "Can't register callback");
			}
			Log.d("MyLog", "On Service connected");
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			Log.d("MyLog", "On Service disconnected");
			
		}
    	
    };
    
    /*
     *  Callback для асинхронного общения со службой загрузки синхронизированных данных из Dropbox
     */
    private IDropboxServiceCallback mCallback = new IDropboxServiceCallback.Stub(){

    	ProgressDialog mDownloadProgress;
		@Override
		public void syncFinished(SyncedData data) throws RemoteException {
			if (data == null) hideDialog();
			Message msg = mDbxServiceHandler.obtainMessage(BOOKS_FINISHED, 0, 0, data);
			mDbxServiceHandler.sendMessage(msg);
		}

		@Override
		public void uploadFinished(boolean res){
			Message msg = mDbxServiceHandler.obtainMessage(UPLOAD_FINISHED, 0, 0, res);
			mDbxServiceHandler.sendMessage(msg);
		}
		
		@Override
		public void showDialog() throws RemoteException {
			runOnUiThread(new Runnable() {
			    @Override
			    public void run() {
			    	mDownloadProgress = new ProgressDialog(StartScreenActivity.this);
					mDownloadProgress.setTitle(ZLResource.resource("dropbox_service").getResource("downloader_title").getValue());
					mDownloadProgress.setMessage(ZLResource.resource("dropbox_service").getResource("downloader_message").getValue());
					mDownloadProgress.show();
			    }
			});
		}
		
		@Override
		public void showNoNetworkToast(){
			StartScreenActivity.this.runOnUiThread(new Runnable() {
			    public void run() {
			    	Toast.makeText(StartScreenActivity.this, ZLResource.resource("dropbox_service").getResource("sync_dbx_no_network").getValue(), Toast.LENGTH_SHORT).show();
			    }
			});
		}

		@Override
		public void hideDialog() throws RemoteException {
			runOnUiThread(new Runnable() {
			    @Override
			    public void run() {
			    	mDownloadProgress.dismiss();
			    	mWasUpdated = true;
			    }
			});
			
		}
		
		@Override
		public void showErrToast(){
			StartScreenActivity.this.runOnUiThread(new Runnable() {
			    public void run() {
			    	Toast.makeText(StartScreenActivity.this, ZLResource.resource("dropbox_service").getResource("sync_dbx_fail").getValue(), Toast.LENGTH_SHORT).show();
			    }
			});
			
		}
		
		@Override
		public void showSuccessToast(){
			StartScreenActivity.this.runOnUiThread(new Runnable() {
			    public void run() {
			    	Toast.makeText(StartScreenActivity.this, ZLResource.resource("dropbox_service").getResource("sync_dbx_succes").getValue(), Toast.LENGTH_SHORT).show();
			    }
			});
			
		}
		
    };
    
    
    /*
     * Handler получения сообщений от DropboxServiceCallback
     * 
     * 
     */
    
    private static final int BOOKS_FINISHED = 1;
    private static final int UPLOAD_FINISHED = 2;
    
    private Handler mDbxServiceHandler = new Handler(Looper.getMainLooper()){
    	@Override 
    	public void handleMessage(Message msg){
    		switch(msg.what){
	    		case BOOKS_FINISHED:
	    			SyncedData data = (SyncedData)msg.obj;
	    			if (data == null){
		   				 try {
							mCallback.showErrToast();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    			}
	    			else{
	   					try {
							mCallback.showSuccessToast();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	    				Updater updater = new Updater(data, StartScreenActivity.this);
		    			updater.execute();
	    			}
	    		break;
	    		case UPLOAD_FINISHED:
	    			boolean res = (Boolean)msg.obj;
	    			try{
	    				if (res)mCallback.showSuccessToast();
	    				else mCallback.showErrToast();
	    			}
	    			catch (Exception ex){}
	    			
	    		break;
    		}
    	}
    };
    
    
    /*
     * Класс, который во вторичном потоке обрабатывает синхронизированные с Dropbox данные
     * и скачивает с него данные
     * 
     */
    
    class Updater extends AsyncTask<Void, Void, Void>{

    	SyncedData mData;
    	DatabaseHandler mDbHandler;
    	Context ctx;
    	
    	public Updater(SyncedData syncedData, Context context){
    		mData = syncedData;
    		ctx = context;
    		mDbHandler = new DatabaseHandler(ctx);
    	}

    	@Override
		protected Void doInBackground(Void... arg0) {
			updateBooks();
			updateBookmarks();
			updateQuotes();
			updateColorMarks();
			return null;
		}
		
		private void updateBooks(){
			ArrayList<SyncedBookInfo> infos = mData.getSBInfos();
			List<Book> booksOnDevice;
			ZLTextFixedPosition position;
			for (SyncedBookInfo info : infos){
				booksOnDevice = collection.getBooksByTitle(info.getBookTitle());
				if ( booksOnDevice!=null && booksOnDevice.size()>0 ){
					
					Book bookOnDevice = booksOnDevice.get(0);
					/*Log.d("MyLog", "Got book on device - " + bookOnDevice.getTitle());
					Log.d("MyLog", "Update par index - " + String.valueOf(info.getParIndex())
							+ " Update el index - " + String.valueOf(info.getElIndex()) 
							+ " Update ch index - " + String.valueOf(info.getChIndex()));*/
					position = new ZLTextFixedPosition(info.getParIndex(), info.getElIndex(), info.getChIndex());
					collection.storePosition(bookOnDevice.getId(), position);
					collection.addBookToRecentList(bookOnDevice);
				}
			}
		}
		
		private void updateBookmarks(){
			ArrayList<SyncedBookmarkInfo> sBmkInfos = mData.getSBmkInfos();
			MyBookmark myBmk;
			List<Book> booksOnDevice;
			Book book;
			boolean present;
			for (SyncedBookmarkInfo sBmkInfo : sBmkInfos){
				booksOnDevice = collection.getBooksByTitle(sBmkInfo.getTitle());
				if (booksOnDevice.size() == 0) continue;
				book = booksOnDevice.get(0);
				present = false;
				myBmk = new MyBookmark(sBmkInfo.getParIndex(), sBmkInfo.getElIndex(), sBmkInfo.getChIndex(), book.getTitle(), getAuthor(book),
						book.getId(), sBmkInfo.getDate(), "", 0);
				// Проверка, нет ли такой закладки в базе данных
				for(MyBookmark bookmark : mDbHandler.getAllBookmarks()) {
					if(bookmark.getParagraphIndex() == myBmk.getParagraphIndex() && bookmark.getBookTitle().equals(myBmk.getBookTitle())) {
						present = true;
						break;
					}	
				}
				if (present) continue;
				mDbHandler.addBookmark(myBmk);
			}
		}
		
		
		private void updateQuotes(){
			ArrayList<SyncedQuoteInfo> sQtInfos = mData.getSQtInfos();
			MyQuote quote;
			List<Book> booksOnDevice;
			Book book;
			boolean present;
			for (SyncedQuoteInfo sQtInfo : sQtInfos){
				booksOnDevice = collection.getBooksByTitle(sQtInfo.getTitle());
				if (booksOnDevice.size() == 0) continue;
				book = booksOnDevice.get(0);
				quote = new MyQuote(sQtInfo.getQuoteText(), sQtInfo.getParIndex(), sQtInfo.getElIndex(), sQtInfo.getChIndex(),
						sQtInfo.getTitle(), getAuthor(book), book.getId(), sQtInfo.getDate(), "", 0,"-1");
				ArrayList<MyQuote> quotes = mDbHandler.getAllQuotes();
				present = false;
				for (MyQuote myQuote : quotes){
					if (myQuote.getBookTitle().equals(sQtInfo.getTitle()) && myQuote.getText().equals(sQtInfo.getQuoteText()) 
							&& myQuote.getParagraphIndex() == sQtInfo.getParIndex() && myQuote.getColor().equals(String.valueOf(quote.getColor()))){
						present = true;
						break;
					}
				}
				if (present) continue;
				mDbHandler.addQuote(quote);
			}
		}
		
		private void updateColorMarks(){
			ArrayList<SyncedColorMarkInfo> sCMInfos = mData.getSCMInfos();
			SelectedMarkInfo SMInfo;
			List<Book> booksOnDevice;
			Book book;
			MyQuote quote;
			boolean present;
			ZLTextPosition startCur;
			ZLTextPosition endCur;
			ZLColor color;
			for (SyncedColorMarkInfo sCMInfo : sCMInfos){
				booksOnDevice = collection.getBooksByTitle(sCMInfo.getTitle());
				if (booksOnDevice.size() == 0) continue;
				book = booksOnDevice.get(0);
				quote = new MyQuote(sCMInfo.getQuoteText(), sCMInfo.getParIndex(), sCMInfo.getElIndex(), sCMInfo.getChIndex(),
						sCMInfo.getTitle(), getAuthor(book), book.getId(), sCMInfo.getDate(), "", 0, String.valueOf(sCMInfo.getColor()));
				ArrayList<MyQuote> quotes = mDbHandler.getAllQuotes();
				present = false;
				for (MyQuote myQuote : quotes){
					// Делаем проверку не только на совпадение параграфов но и на совпадение цветов
					String hexStr = sCMInfo.getHexColor();
					if (myQuote.getBookTitle().equals(quote.getBookTitle()) && myQuote.getText().equals(quote.getText()) 
							&& myQuote.getParagraphIndex() == quote.getParagraphIndex() && myQuote.getColor().equals(hexStr)){
						present = true;
						break;
					}
				}
				if (present) continue;
				long quoteId = mDbHandler.addQuote(quote);
				startCur = new ZLTextFixedPosition(sCMInfo.getStartParIndex(), sCMInfo.getStartElIndex(), sCMInfo.getStartChIndex());
				endCur = new ZLTextFixedPosition(sCMInfo.getEndParIndex(), sCMInfo.getEndElIndex(), sCMInfo.getEndChIndex());
				color = new ZLColor(sCMInfo.getColor());
				SMInfo = new  SelectedMarkInfo(-1, color, startCur, endCur);
				SMInfo.updateQuoteId(quoteId);
				mDbHandler.addColorMark(SMInfo, book.getId());
			}
		}
		
		private String getAuthor(Book myBook){
			if(myBook.authors().size() > 0) {
				return myBook.authors().get(0).DisplayName;
			}  else if(myBook.authors().isEmpty()){
				if(loadCurrentLanguage().equals("en")){
					return "Author unknown";
				} else if(loadCurrentLanguage().equals("de")){
					return  "Autor unbekannt";
				} else if(loadCurrentLanguage().equals("fr")){
					return "Auteur inconnu";
				} else if(loadCurrentLanguage().equals("uk")){
					return "Автор невiдомий";
				} else if(loadCurrentLanguage().equals("ru")){
					return "Автор неизвестен";
				} else {
					if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
						return  "Автор неизвестен";
					} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
						return  "Автор невiдомий";
					} else {
						return "Author unknown";
					}
				}
			}
			return ""; 
		}
		
		@Override
	    protected void onPostExecute(Void result) {
			 super.onPostExecute(result);
			 initRecent();
			 try {
				mCallback.hideDialog();
				Log.d("MyLog", "Update finished");
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
    OnClickListener onClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			FrDocument document = (FrDocument)v.getTag();
			openFrDocument(document);
		}};
		
		void openFrDocument(FrDocument frDocument){
			Intent newIntent = new Intent();
			Uri uri;
			switch (frDocument.getDoctype()){
				case FrDocument.DOCTYPE_CBR:
					uri = Uri.parse(frDocument.getLocation());
					newIntent.setClass(StartScreenActivity.this, ComicViewerActivity.class);
					newIntent.setAction(Intent.ACTION_VIEW);
	    			newIntent.setData(uri);
	    			startActivity(newIntent);
				break;
				case FrDocument.DOCTYPE_DJVU:
					uri = Uri.parse(frDocument.getLocation());
					newIntent.setClass(StartScreenActivity.this, DjvuViewerActivity.class);	
					//newIntent.setDataAndType(Uri.fromFile(new File(frDocument.getLocation())),"");
					newIntent.setData(uri);
					newIntent.setFlags(newIntent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(newIntent);
				break;
				case FrDocument.DOCTYPE_DOCX:
					DatabaseHandler handler = new DatabaseHandler(this);
					handler.updateFrDocumentLastDate(frDocument);
					Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("org.plutext.DocxToHtml");
					LaunchIntent.putExtra("pathToFile", frDocument.getLocation());
					startActivity(LaunchIntent);
				break;
				case FrDocument.DOCTYPE_MUPDF:
					uri = Uri.parse(frDocument.getLocation());
					newIntent.setClass(StartScreenActivity.this, MuPDFActivity.class);
					newIntent.setAction(Intent.ACTION_VIEW);
	    			newIntent.setData(uri);
	    			newIntent.setDataAndType(Uri.fromFile(new File(frDocument.getLocation())),"");
					startActivity(newIntent);
				break;
				case FrDocument.DOCTYPE_ODT:
					uri = Uri.parse(frDocument.getLocation());
					newIntent.setClass(StartScreenActivity.this, at.tomtasche.reader.ui.activity.MainActivity.class);
					newIntent.setAction(Intent.ACTION_VIEW);
	    			newIntent.setData(uri);
	    			startActivity(newIntent);
				break;
				case FrDocument.DOCTYPE_FB:
					try {
						FullReaderActivity.openBookActivity(StartScreenActivity.this, frDocument.getBook(), null);
					} catch (BookReadingException e) {
						e.printStackTrace();
					}
				break;
			}
		}
		
		private void updateFrDocumentsCollection(ArrayList<FrDocument> documents){
			Book book;
			FrDocument document;
			Iterator<FrDocument> iterator = documents.iterator();
			DatabaseHandler db = new DatabaseHandler(this);
			while (iterator.hasNext()){
				document = iterator.next();
				if (document.getDoctype() == FrDocument.DOCTYPE_FB){
					book = collection.getRecentBookByTitleAndLocation(document.getName(), document.getLocation());
					if (book == null) continue;
					if (book.getTitle().equals("About FBReaderJ") || !book.File.exists()){
						collection.removeRecentBook(book);
						db.deleteFrDocument(document);
						iterator.remove();
					}
					else{
						document.setupBook(book);
					}
				}
				else{
					// Если такого документа нету или если это был документ из Rar-архива (они распаковываются в директорю приложения)
					if (!document.getFile().exists() || document.getFile().getParent().equals(this.getCacheDir().getPath())){
						db.deleteFrDocument(document);
						iterator.remove();
						if (document.getFile().getParent().equals(this.getCacheDir().getPath())){
							document.getFile().delete();
						}
					}
				}
			}
		}
		
		
		OnLongClickListener onLongClickListener = new OnLongClickListener() {
			@Override
			public boolean onLongClick(final View v) {
				String delText = "";
				String openText = "";
				if(loadCurrentLanguage().equals("en")){
					openText = "Open";
					delText = "Delete";
	    		} else if(loadCurrentLanguage().equals("de")){
	    			openText = "öffnen";
					delText = "löschen";
	    		} else if(loadCurrentLanguage().equals("fr")){
	    			openText = "Ouvrir";
					delText = "effacer";
	    		} else if(loadCurrentLanguage().equals("uk")){
	    			openText = "Відкрити";
					delText = "Видалити";
	    		} else if(loadCurrentLanguage().equals("ru")){
	    			openText = "Удалить";
					delText = "Открыть";
	    		} else {
	    			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
	    				openText = "Открыть";
						delText = "Удалить";
	    			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
	    				openText = "Вiдкрити";
						delText = "Видалити";
	    			} else {
	    				openText = "Open";
						delText = "Delete";
	    			}
	    		}
				// TODO Auto-generated method stub
				 final String[] mDialogItems ={openText, delText};

				 	AlertDialog.Builder aBuilder = new AlertDialog.Builder(StartScreenActivity.this);
				 	aBuilder.setCancelable(true);
				 	aBuilder.setTitle("FullReader"); // заголовок для диалога

				 	aBuilder.setItems(mDialogItems, new DialogInterface.OnClickListener() {
			            @Override
			            public void onClick(DialogInterface dialog, int item) {
			            	FrDocument frDocument = (FrDocument)v.getTag();
			            	DatabaseHandler handler = new DatabaseHandler(StartScreenActivity.this);
			            	if(item == 0) {
			                	openFrDocument(frDocument);
			                }
			                if(item == 1) {
			                	handler.deleteFrDocument(frDocument);
			                	if (frDocument.getDoctype() == FrDocument.DOCTYPE_FB) collection.removeRecentBook(frDocument.getBook());
			                	twCurrent.setVisibility(View.INVISIBLE);
			                	if (handler.getAllFrDocuments().size() != 0){
			                		body.removeViewAt(0);
			                		initRecent();
			                	}
			                	else{
			                		View view = body.getChildAt(0);
			                		view.setVisibility(View.INVISIBLE);
			                	}
			                }
			            }
			        });
			        AlertDialog alert = aBuilder.create();
			        alert.getWindow().setLayout(600, 400);
			        alert.setCanceledOnTouchOutside(true);
			        alert.show();
				return false;
			}
		}; 
		
		
		
		
		
		
		private OnLongClickListener onRecentLongClick = new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(final View v) {
				String delText = "";
				String openText = "";
				if(loadCurrentLanguage().equals("en")){
					openText = "Open";
					delText = "Delete";
	    		} else if(loadCurrentLanguage().equals("de")){
	    			openText = "öffnen";
					delText = "löschen";
	    		} else if(loadCurrentLanguage().equals("fr")){
	    			openText = "Ouvrir";
					delText = "effacer";
	    		} else if(loadCurrentLanguage().equals("uk")){
	    			openText = "Вiдкрити";
					delText = "Видалити";
	    		} else if(loadCurrentLanguage().equals("ru")){
	    			openText = "Открыть";
					delText = "Удалить";
	    		} else {
	    			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
	    				openText = "Открыть";
						delText = "Удалить";
	    			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
	    				openText = "Вiдкрити";
						delText = "Видалити";
	    			} else {
	    				openText = "Open";
						delText = "Delete";
	    			}
	    		}
				// TODO Auto-generated method stub
				 final String[] mDialogItems ={ openText, delText};
				 	
				 	AlertDialog.Builder aBuilder = new AlertDialog.Builder(StartScreenActivity.this);
				 	aBuilder.setTitle("FullReader"); // заголовок для диалога
			 		
				 	aBuilder.setItems(mDialogItems, new DialogInterface.OnClickListener() {
			            @Override
			            public void onClick(DialogInterface dialog, int item) {
			            	DatabaseHandler dbHandler = new DatabaseHandler(StartScreenActivity.this);
			            	FrDocument frDocument = (FrDocument) v.getTag();
			            	if (item == 0){
			            		openFrDocument(frDocument);
			            	}
			            	if (item == 1){
			            		FrDocument current = dbHandler.getAllFrDocuments().get(0);
			            		if (current.getLocation().equals(frDocument.getLocation()) && current.getName().equals(frDocument.getName())){
			            			alertDialogOnDeleteRecent(frDocument, bootView);
			            		}
			            		else{
			            			dbHandler.deleteFrDocument(frDocument);
			            			if (frDocument.getDoctype() == FrDocument.DOCTYPE_FB){
			            				collection.removeBook(frDocument.getBook(), false);
			            			}
			            			bootView.setVisibility(View.INVISIBLE);
			            			initRecent();
			            		}
			            	}
			            }
			        });
				 	aBuilder.setCancelable(true);
			        AlertDialog alert = aBuilder.create();
			        alert.getWindow().setLayout(600, 400);
			        alert.setCanceledOnTouchOutside(true);
			        alert.show();
				return false;
			}
		}; 
    
}
