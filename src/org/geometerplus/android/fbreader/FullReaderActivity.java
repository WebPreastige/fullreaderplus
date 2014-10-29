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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.geometerplus.android.fbreader.api.ApiListener;
import org.geometerplus.android.fbreader.api.ApiServerImplementation;
import org.geometerplus.android.fbreader.api.PluginApi;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.book.Author;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.ITextMarker;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.bookmodel.BookModel;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.ColorProfile;
import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.fbreader.fbreader.ReaderApp;
import org.geometerplus.fbreader.fbreader.ScrollingPreferences;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.text.hyphenation.ZLTextHyphenator;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;
import org.geometerplus.zlibrary.text.view.ZLTextView;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;
import org.geometerplus.zlibrary.ui.android.application.ZLAndroidApplicationWindow;
import org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidApplication;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.AndroidFontUtil;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidWidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.plus.PlusShare;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import com.webprestige.fr.bookmarks.DatabaseHandler;
import com.webprestige.fr.bookmarks.MyBookmark;
import com.webprestige.fr.citations.MyQuote;
import com.webprestige.fr.dropbox.DropboxHelper;
import com.webprestige.fr.dropbox.DropboxService;
import com.webprestige.fr.dropbox.IDropboxService;
import com.webprestige.fr.dropbox.SyncedBookInfo;
import com.webprestige.fr.dropbox.SyncedBookmarkInfo;
import com.webprestige.fr.dropbox.SyncedColorMarkInfo;
import com.webprestige.fr.dropbox.SyncedQuoteInfo;
import com.fullreader.R;
//import com.google.ads.AdView;

public final class FullReaderActivity extends BaseActivity implements OnSharedPreferenceChangeListener{

	public static final String FROM_RAR_ARCHIVE = "from_rar_archive";
	
	public final static String TAG = "Reader";
	private SharedPreferences sPref;
	private boolean isFullScreen = false;
	private AdView adView;
	
	public static String CURRENT_PROFILE = "day";
	
	private Handler customHandler = new Handler();
	
	//private AdView adView;
	
	private ReaderApp myFBReaderApp;
	public static boolean hyperlinkPressed = false;
	
	public static int CURRENT_COLOR_THEME = 0; //0 - day, 1 - night
	
	BroadcastReceiver _broadcastReceiver;
	private final SimpleDateFormat _sdfWatchTime = new SimpleDateFormat("HH:mm");
	
	private IDropboxService mDbxService;
	
	
	public static TextView batteryTV;
	public static TextView timeTV;
	//private ImageView batteryIV;
	
	private int googlePlusIcon;
	
	public static ArrayList<com.webprestige.fr.bookmarks.MyBookmark> myBoomarks = new ArrayList<com.webprestige.fr.bookmarks.MyBookmark>();
	private DatabaseHandler db;
	private Menu optionsMenu;
	private LayoutParams mainViewLayoutParams;
	
	public static final String ACTION_OPEN_BOOK = "android.reader.action.VIEW";
	public static final String BOOK_KEY = "reader.book";
	public static final String BOOKMARK_KEY = "reader.bookmark";
	
	public static final int REQUEST_PREFERENCES = 1;
	public static final int REQUEST_BOOK_INFO = 2;
	public static final int REQUEST_CANCEL_MENU = 3;
	
	public static final String TITLE_BOOK = "title_book";
	public static final int UPDATE_TITLE_BAR = 1;
	
	public static final int MENU_BOOKMARK = 32749823;
	public static final int RESULT_DO_NOTHING = RESULT_FIRST_USER;
	public static final int RESULT_REPAINT = RESULT_FIRST_USER + 1;
	
	private static final int MENU_GOOGLE_PLUS = 4;
	private static final int MENU_QUOTES = 5;
	private static final int MENU_SETTINGS = 6;
	private static final int MENU_EXIT = 20;
	public static boolean autopagingTimer = false;
	public static long autopagingTime = 0;
	public static boolean reminderTimer = false;
	public static long reminderTime;
	
	public static boolean isCreateFromMyFilesBook = false;
	public static String myFileOpenedBookPath = "";
	
	private static FullReaderActivity readerInstance;
	
	private LayoutParams batteryBarLp;
	
	private LinearLayout batteryBar;
	private LayoutParams timeBarLp;
	private LayoutParams batteryBarTVLp;
	
	private String curThemeProfile;
	public static ArrayList<WeakReference<Activity>> activity_stack=new ArrayList<WeakReference<Activity>>();
	
	
	private ImageView bCell1;
	private ImageView bCell2;
	private ImageView bCell3;
	private ImageView bCell4;
	private ImageView [] mBatteryCells;
	
	private boolean mBookFromRarArchive = false;
	
	private List<Book> booksByLocation;
	private long currentBookId;
	
	private Handler mReadReminderHandler;
	private Runnable mReadReminderRunnable;
	
	public static void openBookActivity(Context context, Book book, ITextMarker bookmark) throws BookReadingException {
		Log.d("book title 123", book.getTitle());
		context.startActivity(
				new Intent(context, FullReaderActivity.class)
				.setAction(ACTION_OPEN_BOOK)
				.putExtra(BOOK_KEY, SerializerUtil.serialize(book))
				.putExtra(BOOKMARK_KEY, SerializerUtil.serialize(bookmark))
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
				);
	}
	
	public Handler myHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(loadCurrentLanguage().equals("en")){
				 Toast.makeText(ReaderApplication.getContext(), "File is corrupt!", Toast.LENGTH_LONG).show();
	  		} else if(loadCurrentLanguage().equals("de")){
	  			Toast.makeText(ReaderApplication.getContext(), "Die Datei ist korrupt!", Toast.LENGTH_LONG).show();
	  		} else if(loadCurrentLanguage().equals("fr")){
	  			Toast.makeText(ReaderApplication.getContext(), "Le fichier est corrompu!", Toast.LENGTH_LONG).show();
	  		} else if(loadCurrentLanguage().equals("uk")){
	  			Toast.makeText(ReaderApplication.getContext(), "Файл пошкоджений!", Toast.LENGTH_LONG).show();
	  		} else if(loadCurrentLanguage().equals("ru")){
	  			Toast.makeText(ReaderApplication.getContext(), "Файл поврежден!", Toast.LENGTH_LONG).show();
	  		} else {
	  			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
	  				Toast.makeText(ReaderApplication.getContext(), "Файл поврежден!", Toast.LENGTH_LONG).show();
	  			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
	  				Toast.makeText(ReaderApplication.getContext(), "Файл пошкоджений!", Toast.LENGTH_LONG).show();
	  			} else {
	  				Toast.makeText(ReaderApplication.getContext(), "File is corrupt!", Toast.LENGTH_LONG).show();
	  			}
	  		}
		}
	};
	
	public static FullReaderActivity getInstance(){
		if(readerInstance==null)
			readerInstance = new FullReaderActivity();
		return readerInstance;
	}
	
	private static ZLAndroidLibrary getZLibrary() {
		return (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
	}
	
	private ReaderApp myReaderApp;
	private volatile Book myBook;
	
	private RelativeLayout myRootView;
	private ZLAndroidWidget myMainView;
	
	private int myFullScreenFlag;
	
	private MenuItem item = null;

	private static final String PLUGIN_ACTION_PREFIX = "___";
	private final List<PluginApi.ActionInfo> myPluginActions =
			new LinkedList<PluginApi.ActionInfo>();
	private final BroadcastReceiver myPluginInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final ArrayList<PluginApi.ActionInfo> actions = getResultExtras(true).<PluginApi.ActionInfo>getParcelableArrayList(PluginApi.PluginInfo.KEY);
			if (actions != null) {
				synchronized (myPluginActions) {
					int index = 0;
					while (index < myPluginActions.size()) {
						myReaderApp.removeAction(PLUGIN_ACTION_PREFIX + index++);
					}
					myPluginActions.addAll(actions);
					index = 0;
					for (PluginApi.ActionInfo info : myPluginActions) {
						myReaderApp.addAction(
								PLUGIN_ACTION_PREFIX + index++,
								new RunPluginAction(FullReaderActivity.this, myReaderApp, info.getId())
								);
					}
				}
			}
		}
	};

	private SharedPreferences settings;

	private Timer myTimer;
	private Timer timerRemind;
	private Timer timerDay;
	private Timer timerNight;

	//private boolean startedFromBookmark;

	private synchronized void openBook(Intent intent, Runnable action, boolean force) {
		
		if (!force && myBook != null) {
			return;
		}
		myBook = SerializerUtil.deserializeBook(intent.getStringExtra(BOOK_KEY));
		final Bookmark bookmark =
				(Bookmark) SerializerUtil.deserializeBookmark(intent.getStringExtra(BOOKMARK_KEY));
		if (myBook == null) {
			final Uri data = intent.getData();
			if (data != null) {
				myBook = createBookForFile(ZLFile.createFileByPath(data.getPath()));

			}
		}
		if(bookmark != null)
		{
			//	startedFromBookmark = true;
		}
		myReaderApp.openBook(myBook, bookmark, action);
		booksByLocation = myReaderApp.Collection.booksForLocation(myBook.File.getParent().getPath());
		currentBookId = myBook.getId();
	}
	
	public static int convertPixelsToDp(float px, Context context){
		DisplayMetrics displaymetrics = new DisplayMetrics();
		int dp = (int) (50 * displaymetrics.density + 0.5f);
	    return dp;
	}
	
	private float dpFromPx(float px)
	{
	    return px / this.getBaseContext().getResources().getDisplayMetrics().density;
	}
	
	public static void addToActivityStack(Activity act)
	{
	    WeakReference<Activity> ref = new WeakReference<Activity>(act);
	    activity_stack.add(ref);

	}

	private Book createBookForFile(ZLFile file) {
		if (file == null) {
			return null;
		}
		Book book = myReaderApp.Collection.getBookByFile(file);
		if (book != null) {
			return book;
		}
		if (file.isArchive()) {
			for (ZLFile child : file.children()) {
				book = myReaderApp.Collection.getBookByFile(child);
				if (book != null) {
					return book;
				}
			}
		}
		return null;
	}

	private Runnable getPostponedInitAction() {
		return new Runnable() {
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						//disable tips
						//new TipRunner().start();
						DictionaryUtil.init(FullReaderActivity.this);
					}
				});
			}
		};
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// Запускаем службу для работы с Dropbox
		bindService(new Intent(this, DropboxService.class), mConnection, Context.BIND_AUTO_CREATE);
		readerInstance = this;
		setVolumeControlStream(AudioManager.STREAM_ALARM);
		/*AudioManager aManager=(AudioManager)getSystemService(AUDIO_SERVICE);
	    aManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
	    aManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
	    aManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);*/

		this.mReadReminderHandler = new Handler();
		this.mReadReminderRunnable = new Runnable(){
			public void run() { 
				showReminder();
				mReadReminderHandler.postDelayed(mReadReminderRunnable, settings.getLong("timeRemind", 60000));
			}
		};
		
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(this));
		initActionBar();
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		myRootView = (RelativeLayout)findViewById(R.id.root_view);
		myMainView = (ZLAndroidWidget)findViewById(R.id.main_view);
		
		mainViewLayoutParams = myMainView.getLayoutParams();
		myMainView.setFocusableInTouchMode(false);
		myMainView.getLayoutParams();
		myRootView.setFocusableInTouchMode(false);
		batteryTV = (TextView)findViewById(R.id.battery_tv);
		timeTV = (TextView)findViewById(R.id.time_tv);
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		//timeTV.setText(today.hour+":"+today.minute);
		batteryBar = (LinearLayout)findViewById(R.id.batteryBar);
	//	batteryBar = (RelativeLayout)findViewById(R.id.battery_bar);
		batteryBarLp = batteryBar.getLayoutParams();
		batteryBarTVLp = batteryTV.getLayoutParams();
		timeBarLp = timeTV.getLayoutParams();
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		getZLibrary().setActivity(this);

		bCell1 = (ImageView)findViewById(R.id.cellItem1);
    	bCell2 = (ImageView)findViewById(R.id.cellItem2);
    	bCell3 = (ImageView)findViewById(R.id.cellItem3);
    	bCell4 = (ImageView)findViewById(R.id.cellItem4);
		mBatteryCells = new ImageView[]{bCell1, bCell2, bCell3, bCell4};
		
		myReaderApp = (ReaderApp)ReaderApp.Instance();
		//if (myReaderApp == null) 
		{
			myReaderApp = new ReaderApp(new BookCollectionShadow());
		}
		
		// Передаем ссылку на текстовые поля с батарейкой и временем для смены их цвета при смене режимов день-ночь
			myReaderApp.linkBatteryAndTimeViews(bCell1, bCell2, bCell3, bCell4, this, timeTV, batteryTV, batteryBar);
		//
		
		
		getCollection().bindToService(this, null);
		myBook = null;
		
		final ZLAndroidApplication androidApplication = (ZLAndroidApplication)getApplication();
		//if (androidApplication.myMainWindow == null) 
		{
			androidApplication.myMainWindow = new ZLAndroidApplicationWindow(myReaderApp);
			myReaderApp.initWindow();
		}
		
		// Делаем проверку, не открыта ли книга из rar-архива
		if (getIntent()!=null){
			if(getIntent().hasExtra(FROM_RAR_ARCHIVE)){
				mBookFromRarArchive = true;
			}
		}
		
		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
		myFullScreenFlag =
				zlibrary.ShowStatusBarOption.getValue() ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN, myFullScreenFlag
				);

		if (myReaderApp.getPopupById(TextSearchPopup.ID) == null) {
			new TextSearchPopup(myReaderApp);
		}
		if (myReaderApp.getPopupById(NavigationPopup.ID) == null) {
			new NavigationPopup(myReaderApp);
		}
		if (myReaderApp.getPopupById(SelectionPopup.ID) == null) {
			new SelectionPopup(myReaderApp, mBookFromRarArchive);
		}

		myReaderApp.addAction(ActionCode.SHOW_LIBRARY, new ShowLibraryAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SHOW_PREFERENCES, new ShowPreferencesAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SHOW_COLOR_PREFERENCES, new ShowColorPreferencesAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SHOW_BOOK_INFO, new ShowBookInfoAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SHOW_TOC, new ShowTOCAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SHOW_BOOKMARKS, new ShowBookmarksAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SHOW_QUOTES, new ShowQuotesAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SHOW_COLOR_MARKS, new ShowColorMarksAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SHOW_NETWORK_LIBRARY, new ShowNetworkLibraryAction(this, myReaderApp));
		
		myReaderApp.addAction(ActionCode.NEXT_BOOK, new OpenNextBookAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.PREVIOUS_BOOK, new OpenPrevBookAction(this, myReaderApp));
		
		myReaderApp.addAction(ActionCode.FULLSCREEN_MODE, new FullScreenAction(this, myReaderApp));

		myReaderApp.addAction(ActionCode.SHOW_MENU, new ShowMenuAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SHOW_NAVIGATION, new ShowNavigationAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SEARCH, new SearchAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SHARE_BOOK, new ShareBookAction(this, myReaderApp));

		myReaderApp.addAction(ActionCode.SELECTION_SHOW_PANEL, new SelectionShowPanelAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SELECTION_HIDE_PANEL, new SelectionHidePanelAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SELECTION_COPY_TO_CLIPBOARD, new SelectionCopyAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SELECTION_SHARE, new SelectionShareAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SELECTION_TRANSLATE, new SelectionTranslateAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SELECTION_BOOKMARK, new SelectionFbShareAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SELECTION_QUOTES, new SelectionQuoteAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SELECTION_DICTIONARY, new SelectionDictionaryAction(this, myReaderApp));
		myReaderApp.addAction(ActionCode.SELECTION_MARK_COLOR, new SelectionMarkColorAction(this, myReaderApp));
		
		myReaderApp.addAction(ActionCode.PROCESS_HYPERLINK, new ProcessHyperlinkAction(this, myReaderApp));

		myReaderApp.addAction(ActionCode.SHOW_CANCEL_MENU, new ShowCancelMenuAction(this, myReaderApp));

		myReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SYSTEM, new SetScreenOrientationAction(this, myReaderApp, ZLibrary.SCREEN_ORIENTATION_SYSTEM));
		myReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_SENSOR, new SetScreenOrientationAction(this, myReaderApp, ZLibrary.SCREEN_ORIENTATION_SENSOR));
		myReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT, new SetScreenOrientationAction(this, myReaderApp, ZLibrary.SCREEN_ORIENTATION_PORTRAIT));
		myReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE, new SetScreenOrientationAction(this, myReaderApp, ZLibrary.SCREEN_ORIENTATION_LANDSCAPE));
		if (ZLibrary.Instance().supportsAllOrientations()) {
			myReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT, new SetScreenOrientationAction(this, myReaderApp, ZLibrary.SCREEN_ORIENTATION_REVERSE_PORTRAIT));
			myReaderApp.addAction(ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE, new SetScreenOrientationAction(this, myReaderApp, ZLibrary.SCREEN_ORIENTATION_REVERSE_LANDSCAPE));
		}

		settings = PreferenceManager.getDefaultSharedPreferences(this);
		//settings.registerOnSharedPreferenceChangeListener(this);
		
		//timerRemind = new Timer();

		checkAutoPaging();

		checkDayNightPrefs();
		
		Log.d("checkADS: ", String.valueOf(checkAds()));
		if(!checkAds()) {
			initAdMob();
		}
		if(myReaderApp.getColorProfileName().equals("defaultDark")) {
			curThemeProfile = "";
			curThemeProfile = "dark";
		} else if(myReaderApp.getColorProfileName().equals("defaultLight")) {
			curThemeProfile = "";
			curThemeProfile = "light";
		}
		timeTV.setText(_sdfWatchTime.format(new Date()));
		initSharedPreference();
		initBatteryAndTimeBar();
		
		
		_broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent)
            {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                	//timeTV.setText("");
                    timeTV.setText(_sdfWatchTime.format(new Date()));
                    checkDayNightPrefs();
            }
        }
    };

    registerReceiver(_broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
		
	//	fr.keyBindings().bindKey(KeyEvent.KEYCODE_BACK, false, ZLApplication.NoAction);
	}
	
	private long loadTimeRemind() {
	    sPref = getSharedPreferences("remindPrefs", MODE_PRIVATE);
	    return sPref.getLong("timeRemind", 1);
	}
	
	/*@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
	    boolean result;
	     switch( event.getKeyCode() ) {
	        case KeyEvent.KEYCODE_VOLUME_UP:
	        	prevPage();
	        case KeyEvent.KEYCODE_VOLUME_DOWN:
	        	nextPage();
	            result = true;
	            break;

	         default:
	            result= super.dispatchKeyEvent(event);
	            break;
	     }

	     return result;
	}*/
	
	public void nextPage(){
		myReaderApp.runAction(ActionCode.TURN_PAGE_FORWARD, true);
	}
	
	public void prevPage(){
		myReaderApp.runAction(ActionCode.TURN_PAGE_BACK, false);
	}
	
	public class RemoteControlReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
	            KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
	            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
	                // Handle key press.
	            }
	        }
	    }
	}
	
	private Runnable updateTimerThread = new Runnable() {
		
		public void run() {
			checkDayNightPrefs();
			ImageView bCell1 = (ImageView)findViewById(R.id.cellItem1);
	    	ImageView bCell2 = (ImageView)findViewById(R.id.cellItem2);
	    	ImageView bCell3 = (ImageView)findViewById(R.id.cellItem3);
	    	ImageView bCell4 = (ImageView)findViewById(R.id.cellItem4);
			if(CURRENT_PROFILE.equals("night")) {
				timeTV.setTextColor(Color.WHITE);
				batteryTV.setTextColor(Color.WHITE);
				bCell1.setImageResource(R.drawable.cell_item_dark);
				bCell2.setImageResource(R.drawable.cell_item_dark);
				bCell3.setImageResource(R.drawable.cell_item_dark);
				bCell4.setImageResource(R.drawable.cell_item_dark);
				batteryBar.setBackgroundResource(R.drawable.battery_dark);
			}
			if(CURRENT_PROFILE.equals("day")){
				timeTV.setTextColor(Color.BLACK);
				batteryTV.setTextColor(Color.BLACK);
				bCell1.setImageResource(R.drawable.cell_item);
				bCell2.setImageResource(R.drawable.cell_item);
				bCell3.setImageResource(R.drawable.cell_item);
				bCell4.setImageResource(R.drawable.cell_item);
				batteryBar.setBackgroundResource(R.drawable.battery);
			}
		}

	};
	
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
	
	 private void initActionBar() {
		 ActionBar bar = getSupportActionBar();
	     Drawable actionBarBackground = null;
	     int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
	     switch(theme) {
		        case IConstants.THEME_MYBLACK:
		         actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar );
		         break;
		        case IConstants.THEME_LAMINAT:
		         actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar );
		         break;
		        case IConstants.THEME_REDTREE:
		         actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_redtree_action_bar );
		         break;
	     }      
	     bar.setBackgroundDrawable(actionBarBackground);
	 }
	
	private void initSharedPreference() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		if(settings.getBoolean("first_start", true)){
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("daynightenable", myReaderApp.DayNight.getValue());
			editor.putBoolean("needToRemind", myReaderApp.ReaderOption.getValue());
			editor.putBoolean("needToAutopaging", myReaderApp.AllowAutopaggingOption.getValue());

			editor.putBoolean("first_start", false);
			editor.commit();
		}
	}
	
	Handler handler = new Handler(){
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case UPDATE_TITLE_BAR: {
				FullReaderActivity.this.getSupportActionBar().setTitle((String)msg.getData().get(TITLE_BOOK));
				break;
			}
			default:
				showReminder();
				break;
			}
		}
		
	};

	/*private void initReminder() {
		try {
			if(settings.getBoolean("needToRemind", false)){
				timerRemind = new Timer();
				//ReminderTask reminderTask = new ReminderTask();
				//reminderTask.execute();
				//if(reminderTime == -1)
				//	return;
				TimerTask task = new TimerTask() { // ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½
					@Override
					public void run() {
						handler.sendEmptyMessage(0);
					}
				};
				final long rTime = settings.getLong("reminder", 60000);
				//Calendar cal = Calendar.getInstance();
				//cal.setTimeInMillis(autopage);
				
				timerRemind.schedule(task, rTime, rTime);
				Log.d("reminder", "REMINDER ON");
				Log.d("reminder time: ", String.valueOf(reminderTime));
			} else {
				Log.d("reminder", "REMINDER OFF");
				//new TurnPageAction(myReaderApp, false).checkAndRun();
			}
		} catch(IllegalArgumentException e) {
			return;
		}
	}*/
	
	private void initReminder() {
		if(settings.getBoolean("needToRemind", false)) {
			this.mReadReminderHandler.postDelayed(mReadReminderRunnable, settings.getLong("timeRemind", 60000));
		}
	}
	
	private void stopReminder(){
		this.mReadReminderHandler.removeCallbacks(mReadReminderRunnable);
	}
	
	private void scrollNextPage() {
		final ScrollingPreferences preferences = ScrollingPreferences.Instance();
		myReaderApp.getViewWidget().startAnimatedScrolling(FBView.PageIndex.next,
				preferences.HorizontalOption.getValue()
					? FBView.Direction.rightToLeft : FBView.Direction.up,
				preferences.AnimationSpeedOption.getValue()
			);
	}
	
	private void checkAutoPaging() {
		Log.d("checkAutopaging", "checkAutopaging");
		if(settings.getBoolean("needToAutopaging", false)){
			myTimer = new Timer();
			Log.d("autopagign", "AUTOPAGE TRUE");
			/*TimerTask task = new TimerTask() { // ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½
				@Override
				public void run() {
					Log.d("TurPageAction","TurPageAction");
					//new TurnPageAction(myReaderApp, true).checkAndRun();
					if(!autopagingTimer) {
						return;
					} else {
						scrollNextPage();
					}
				}
			};*/
			
			/*Thread myThread = new Thread(new Runnable() {
			    @Override
			    public void run() {
			    	try {
			    		 while (autopagingTimer){
			    			 Thread.sleep(autopagingTime); 
			    			 scrollNextPage();
			    			 Log.d("page slided", "page slided");
			    			 Log.d("time slide: ", String.valueOf(autopagingTime));
		    			 }
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    }
			});

			myThread.start();*/
			
			AutopagingTask autopagingTask = new AutopagingTask();
			autopagingTask.execute();
			
			//long autopage = settings.getLong("autopage", -1);
		//	Log.d("autopage seconds: ", String.valueOf(autopage));
			//Calendar cal = Calendar.getInstance();
			//cal.setTimeInMillis(autopage);
			
			//myTimer.schedule(task,cal.get(Calendar.MINUTE)*60000+cal.get(Calendar.SECOND)*1000,
			//		cal.get(Calendar.MINUTE)*60000+cal.get(Calendar.SECOND)*1000); 
			if(autopagingTime == -1)
				return;
			Log.d("CUR SECONDS: ", String.valueOf(autopagingTime));
			//myTimer.schedule(task, Long.valueOf(autopagingTime), Long.valueOf(autopagingTime));
		} else {
			Log.d("autopagign", "AUTOPAGE FALSE");
			//new TurnPageAction(myReaderApp, false).checkAndRun();
		}
	}
	
	class AutopagingTask extends AsyncTask<Void, Void, Void> {

	    @Override
	    protected void onPreExecute() {
	    	super.onPreExecute();
	    }

	    @Override
	    protected Void doInBackground(Void... params) {
	    	try {
	    		 while (autopagingTimer){
	    			 Thread.sleep(autopagingTime); 
	    			 scrollNextPage();
	    			 Log.d("page slided", "page slided");
	    			 Log.d("time slide: ", String.valueOf(autopagingTime));
    			 }
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return null;
	    }

	    @Override
	    protected void onPostExecute(Void result) {
	    	super.onPostExecute(result);
	    }
	  }
	
	class ReminderTask extends AsyncTask<Void, Void, Void> {
		private long timeRemind;
		public ReminderTask(long timeToRemind) {
			timeRemind = timeToRemind;
		}
	    @Override
	    protected void onPreExecute() {
	    	super.onPreExecute();
	    }

	    @Override
	    protected Void doInBackground(Void... params) {
	    	try {
	    		 while (true){
	    			 Log.d("time reminder: ", String.valueOf(loadTimeRemind()));
	    			 Thread.sleep(loadTimeRemind()); 
	    			 handler.sendEmptyMessage(0);
	    			
    			 }
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return null;
	    }

	    @Override
	    protected void onPostExecute(Void result) {
	    	super.onPostExecute(result);
	    }
	  }
	
	/*private void initAdMob() {
		adView = new AdView(this, AdSize.BANNER, "use-your-own-id");
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.root_view);
		RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, 
				RelativeLayout.LayoutParams.WRAP_CONTENT);

		adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		adParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

	    layout.addView(adView, adParams);
	    adView.loadAd(new AdRequest());
	}*/

	
	/*private void checkDayNightPref() {

		Log.d("DAYNIGHTPREFS: ", "DAYNIGHTPREFS");
		if(settings.getBoolean("daynightenable", false)) {
			
			//timerDay = new Timer();
			//TimerTask task = new TimerTask() { // ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½ ï¿½ï¿½ï¿½ï¿½ï¿½ï¿½
			//	@Override
			//	public void run() {
					final long start = settings.getLong("dayStart", 969688800000l);
					final long end = settings.getLong("dayEnd", 969735600000l);
					Calendar calC = Calendar.getInstance();
					Calendar calS = Calendar.getInstance();
					calS.setTimeInMillis(start);
					Calendar calE = Calendar.getInstance();
					calE.setTimeInMillis(end);
					if(
							//startMin<=curMin && endMin >=curMin
							                calS.get(Calendar.HOUR_OF_DAY)<=calC.get(Calendar.HOUR_OF_DAY)&&
							                calS.get(Calendar.MINUTE)<=calC.get(Calendar.MINUTE)&&
							                calE.get(Calendar.HOUR_OF_DAY)>=calC.get(Calendar.HOUR_OF_DAY)&&
							                calE.get(Calendar.MINUTE)>=calC.get(Calendar.MINUTE)
							){
						myReaderApp.setColorProfileName(ColorProfile.DAY);
						myReaderApp.getViewWidget().reset();
						myReaderApp.getViewWidget().repaint();
						//myMainView.reset();
						//myMainView.repaint();
					}else{
						myReaderApp.setColorProfileName(ColorProfile.NIGHT);
						myReaderApp.getViewWidget().reset();
						myReaderApp.getViewWidget().repaint();
						//myMainView.reset();
						//myMainView.repaint();
					}
				}
			//};
			
			//int startMin = calS.get(Calendar.HOUR_OF_DAY)*60+calS.get(Calendar.MINUTE);
			//int endMin = calE.get(Calendar.HOUR_OF_DAY)*60+calE.get(Calendar.MINUTE);
			//int curMin = calC.get(Calendar.HOUR_OF_DAY)*60+calC.get(Calendar.MINUTE);
			
			//timerDay.schedule(task, 1,
			//		1);
		//}
	}*/
	
	public void checkDayNightPrefs() {
		if(settings.getBoolean("daynightenable", false)) {
			int dayStartHour = settings.getInt("dayStartHour", 0);
			int dayStartMinute = settings.getInt("dayStartMinute", 0);
			int nightStartHour = settings.getInt("nightStartHour", 0);
			int nightStartMinute = settings.getInt("nightStartMinute", 0);
			
			int currentHour = 0;
			int currentMinute = 0;
			
			Time today = new Time(Time.getCurrentTimezone());
			today.setToNow();
			if(today.minute < 10) {
				currentMinute = today.minute;	
				currentHour = today.hour;
			} else {
				currentMinute = today.minute;	
				currentHour = today.hour;
			}
			
			if(currentHour == dayStartHour && currentMinute == dayStartMinute) {
				//day
				myReaderApp.setColorProfileName(ColorProfile.DAY);
				myReaderApp.getViewWidget().reset();
				myReaderApp.getViewWidget().repaint();
			}
			if(currentHour == nightStartHour && currentMinute == nightStartMinute) {
				//night
				myReaderApp.setColorProfileName(ColorProfile.NIGHT);
				myReaderApp.getViewWidget().reset();
				myReaderApp.getViewWidget().repaint();
			}
			
			
			if(CURRENT_PROFILE.equals("night")) {
				timeTV.setTextColor(Color.WHITE);
				batteryTV.setTextColor(Color.WHITE);
				bCell1.setImageResource(R.drawable.cell_item_dark);
				bCell2.setImageResource(R.drawable.cell_item_dark);
				bCell3.setImageResource(R.drawable.cell_item_dark);
				bCell4.setImageResource(R.drawable.cell_item_dark);
				batteryBar.setBackgroundResource(R.drawable.battery_dark);
			}
			if(CURRENT_PROFILE.equals("day")){
				timeTV.setTextColor(Color.BLACK);
				batteryTV.setTextColor(Color.BLACK);
				bCell1.setImageResource(R.drawable.cell_item);
				bCell2.setImageResource(R.drawable.cell_item);
				bCell3.setImageResource(R.drawable.cell_item);
				bCell4.setImageResource(R.drawable.cell_item);
				batteryBar.setBackgroundResource(R.drawable.battery);
			}
			
		}
	}

	// 	@Override
	//	public boolean onPrepareOptionsMenu(Menu menu) {
	//		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
	//		if (!zlibrary.isKindleFire() && !zlibrary.ShowStatusBarOption.getValue()) {
	//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	//		}
	//		return super.onPrepareOptionsMenu(menu);
	//	}
	//
	//	@Override
	//	public void onOptionsMenuClosed(Menu menu) {
	//		super.onOptionsMenuClosed(menu);
	//		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
	//		if (!zlibrary.isKindleFire() && !zlibrary.ShowStatusBarOption.getValue()) {
	//			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	//		}
	//	}
	//
	//	@Override
	//	public boolean onOptionsItemSelected(MenuItem item) {
	//		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
	//		if (!zlibrary.isKindleFire() && !zlibrary.ShowStatusBarOption.getValue()) {
	//			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
	//		}
	//		return super.onOptionsItemSelected(item);
	//	}
	
	public ZLAndroidWidget getMainView() {
		return myMainView;
	}
	
	private void initAdMob() {
		adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .addTestDevice("TEST_DEVICE_ID")
            .build();
        adView.loadAd(adRequest);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
          switch (item.getItemId()) {
          case android.R.id.home:
             Toast.makeText(getApplicationContext(),"Back button clicked", Toast.LENGTH_SHORT).show(); 
          }
          return true;
    }
	
	@Override
	protected void onNewIntent(final Intent intent) {
		Log.d("MyLog", "OnNew intent");
		final String action = intent.getAction();
		final Uri data = intent.getData();
		
		if ((intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) != 0) {
			super.onNewIntent(intent);
		} else if (Intent.ACTION_VIEW.equals(action)
				&& data != null && "reader-action".equals(data.getScheme())) {
			myReaderApp.runAction(data.getEncodedSchemeSpecificPart(), data.getFragment());
		} else if (Intent.ACTION_VIEW.equals(action) || ACTION_OPEN_BOOK.equals(action)) {
			getCollection().bindToService(this, new Runnable() {
				public void run() {
					// Делаем, проверку на то, не открыта ли книга из rar-архива
					if (intent.hasExtra(FROM_RAR_ARCHIVE)){
						mBookFromRarArchive = true;
					}
					else{
						mBookFromRarArchive = false;
					}
					openBook(intent, null, true);
					String title = null;
					//title = (myBook.authors()!=null)?
							//(myBook.getTitle()+"("+myBook.authors().get(0).DisplayName+")")
							//:myBook.getTitle();
							
					if(myBook.authors() != null && !myBook.authors().isEmpty()) {
						title = myBook.getTitle();
					} else {
						title = "no title";
					}
					FullReaderActivity.this.getSupportActionBar().setTitle(title);
				}
			});
		} else if (Intent.ACTION_SEARCH.equals(action)) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			final Runnable runnable = new Runnable() {
				public void run() {
					final TextSearchPopup popup = (TextSearchPopup)myReaderApp.getPopupById(TextSearchPopup.ID);
					popup.initPosition();
					myReaderApp.TextSearchPatternOption.setValue(pattern);
					if (myReaderApp.getTextView().search(pattern, true, false, false, false) != 0) {
						runOnUiThread(new Runnable() {
							public void run() {
								myReaderApp.showPopup(popup.getId());
							}
						});
					} else {
						runOnUiThread(new Runnable() {
							public void run() {
								UIUtil.showErrorMessage(FullReaderActivity.this, "textNotFound");
								popup.StartPosition = null;
							}
						});
					}
				}
			};
			UIUtil.wait("search", runnable, this);
		} else {
			super.onNewIntent(intent);
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		getCollection().bindToService(this, new Runnable() {
			public void run() {
				new Thread() {

					public void run() {
						openBook(getIntent(), getPostponedInitAction(), false);
						String title;
						
						try {
							title = myBook.getTitle()+"("+myBook.authors().get(0).DisplayName+")";
						}catch(Exception e) {
							try{
								title = myBook.getTitle();
							}catch(NullPointerException e1){
								title = null;
							}
						}
						if(title!=null) {							
							Message msg = new Message();
							Bundle data = new Bundle();
							data.putString(TITLE_BOOK, title);
							msg.what = UPDATE_TITLE_BAR;
							handler.sendMessage(msg);
						}
						
						myReaderApp.getViewWidget().repaint();
					}
				}.start();
				
				myReaderApp.getViewWidget().repaint();
			}
		});

		initPluginActions();
		
		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();

		final int fullScreenFlag =
				zlibrary.ShowStatusBarOption.getValue() ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
		if (fullScreenFlag != myFullScreenFlag) {
			finish();
			startActivity(new Intent(this, getClass()));
		}
   
		SetScreenOrientationAction.setOrientation(this, zlibrary.OrientationOption.getValue());

		((PopupPanel)myReaderApp.getPopupById(TextSearchPopup.ID)).setPanelInfo(this, myRootView);
		((PopupPanel)myReaderApp.getPopupById(NavigationPopup.ID)).setPanelInfo(this, myRootView);
		((PopupPanel)myReaderApp.getPopupById(SelectionPopup.ID)).setPanelInfo(this, myRootView);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		switchWakeLock(hasFocus &&
				getZLibrary().BatteryLevelToTurnScreenOffOption.getValue() <
				myReaderApp.getBatteryLevel()
				);
	}
	
	private void initPluginActions() {
		synchronized (myPluginActions) {
			int index = 0;
			while (index < myPluginActions.size()) {
				myReaderApp.removeAction(PLUGIN_ACTION_PREFIX + index++);
			}
			myPluginActions.clear();
		}

		sendOrderedBroadcast(
				new Intent(PluginApi.ACTION_REGISTER),
				null,
				myPluginInfoReceiver,
				null,
				RESULT_OK,
				null,
				null
				);
	}
	
	
	
	// -------- reciever для контроля расхода батареи -------
	BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context ctxt, Intent intent) {
			  int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			  if (level!=-1){
				  batteryTV.setText(String.valueOf(level) + "%");
				  batteryTV.setVisibility(View.VISIBLE);
				  if (level>75){
					  for (int i=0; i<mBatteryCells.length; i++){
						  mBatteryCells[i].setVisibility(View.VISIBLE);
					  }
				  }
				  else
				  if (level>50){
					  for (int i=0; i<mBatteryCells.length; i++){
						  if (i<1)mBatteryCells[i].setVisibility(View.INVISIBLE);
						  else mBatteryCells[i].setVisibility(View.VISIBLE);
					  }
				  }
				 else
				 if (level>25){
					 for (int i=0; i<mBatteryCells.length; i++){
						  if (i<2)mBatteryCells[i].setVisibility(View.INVISIBLE);
						  else mBatteryCells[i].setVisibility(View.VISIBLE);
					  }
				 }
				 else
				 if (level<=25){
					 for (int i=0; i<mBatteryCells.length; i++){
						  if (i<3)mBatteryCells[i].setVisibility(View.INVISIBLE);
						  else mBatteryCells[i].setVisibility(View.VISIBLE);
					  }
				 }
			  }
		}
    };
	
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		initReminder();
		
		
		/*AudioManager aManager=(AudioManager)getSystemService(AUDIO_SERVICE);
	    aManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
	    aManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
	    aManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);*/
		registerReceiver(_broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
		
		batteryTV.setVisibility(View.VISIBLE);
		// Регистрируем Reciever который отлавливает изменения заряда батареи
		registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		//Time today = new Time(Time.getCurrentTimezone());
		//today.setToNow();
		//timeTV.setText("");
		//timeTV.setText(today.hour+":"+today.minute);
		customHandler.postDelayed(updateTimerThread, 0);
		checkDayNightPrefs();
		Log.d("on resume", "on resume!");
		if(checkAds()) {
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			layoutParams.setMargins(0, 0, 0, 0);
			myMainView.setLayoutParams(layoutParams);
			
			 RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams
	    	            (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    	    lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	    	    Resources r = getBaseContext().getResources();
	    	    
	    	    int px1 = (int) TypedValue.applyDimension(
	    	            TypedValue.COMPLEX_UNIT_DIP,
	    	            87, 
	    	            r.getDisplayMetrics()
	    	    );
	    	    
	    	    int px2 = (int) TypedValue.applyDimension(
	    	            TypedValue.COMPLEX_UNIT_DIP,
	    	            57, 
	    	            r.getDisplayMetrics()
	    	    );
	    	    
	    	    int px3 = (int) TypedValue.applyDimension(
	    	            TypedValue.COMPLEX_UNIT_DIP,
	    	            1, 
	    	            r.getDisplayMetrics()
	    	    );
	    	    
	    	    
	    	    lp.setMargins(px2, 0, 0, px3);
	    	    
	    	    RelativeLayout.LayoutParams batteryTvLp = new RelativeLayout.LayoutParams
	    	            (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    	    batteryTvLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	    	    batteryTvLp.setMargins(px1, 0, 0, px3);
	    	    
	    	    RelativeLayout.LayoutParams lpTimeTv = new RelativeLayout.LayoutParams
	    	            (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    	    lpTimeTv.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	    	    lpTimeTv.setMargins(0, 0, 0, px3);
	    	    lpTimeTv.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
	    	    
	    	    batteryBar.setLayoutParams(lp);
	    	    timeTV.setLayoutParams(lpTimeTv);
	    	    batteryTV.setLayoutParams(batteryTvLp);
		}
		initBatteryAndTimeBar();
		checkAutoPaging();
		if(!settings.getBoolean("needToRemind", false)) {
			Log.d("NO NEED TO REMIND", "NO NEED TO REMIND");
			if(timerRemind != null) {
				timerRemind.cancel();
				timerRemind = null;
			}
		}
		Log.d("COLOR PROFILE: ", myReaderApp.getColorProfile().toString());
		/*if(myReaderApp.getColorProfile().equals(ColorProfile.NIGHT)) {
			timeTV.setTextColor(Color.WHITE);
			Log.d("COLOR PROFILE: ", "DAY");
		}*/
		myStartTimer = true;
		final int brightnessLevel =
				getZLibrary().ScreenBrightnessLevelOption.getValue();
		if (brightnessLevel != 0) {
			setScreenBrightness(brightnessLevel);
		} else {
			setScreenBrightnessAuto();
		}
		if (getZLibrary().DisableButtonLightsOption.getValue()) {
			setButtonLight(false);
		}
		
		//registerReceiver(myBatteryInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		
		PopupPanel.restoreVisibilities(myReaderApp);
		ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_OPENED);

		getCollection().bindToService(this, new Runnable() {
			public void run() {
				final BookModel model = myReaderApp.Model;
				if (model == null || model.Book == null) {
					return;
				}
				onPreferencesUpdate(myReaderApp.Collection.getBookById(model.Book.getId()));
			}
		});
	}
	
	@Override
	protected void onPause() {
		stopReminder();
		try {
			//unregisterReceiver(myBatteryInfoReceiver);
			unregisterReceiver(mBatInfoReceiver);
		} catch (IllegalArgumentException e) {
			// do nothing, this exception means myBatteryInfoReceiver was not registered
		}
		myReaderApp.stopTimer();
		if (getZLibrary().DisableButtonLightsOption.getValue()) {
			setButtonLight(true);
		}
			myReaderApp.onWindowClosing();
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		ApiServerImplementation.sendEvent(this, ApiListener.EVENT_READ_MODE_CLOSED);
		PopupPanel.removeAllWindows(myReaderApp, this);
		super.onStop();
		if(myTimer != null) {
			myTimer.cancel();
			myTimer = null;
			//new TurnPageAction(myReaderApp, false);
		}
		if(timerRemind != null) {
			timerRemind.cancel();
			timerRemind = null;
		}
		
		if(timerDay != null) {
			timerDay.cancel();
			timerDay = null;
		}
		
		if(timerNight != null) {
			timerNight.cancel();
			timerNight = null;
		}
		
		if (_broadcastReceiver != null)
	        unregisterReceiver(_broadcastReceiver);
	}

	@Override
	protected void onDestroy() {
		
		getCollection().unbind();
		unbindService(mConnection);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onDestroy();
	}

	@Override
	public boolean onSearchRequested() {
		final ReaderApp.PopupPanel popup = myReaderApp.getActivePopup();
		//myReaderApp.hideActivePopup();
		final SearchManager manager = (SearchManager)getSystemService(SEARCH_SERVICE);
		manager.setOnCancelListener(new SearchManager.OnCancelListener() {
			public void onCancel() {
				if (popup != null) {
					myReaderApp.showPopup(popup.getId());
				}
				manager.setOnCancelListener(null);
			}
		});
		startSearch(myReaderApp.TextSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	@Override
	public void onLowMemory() {
		myReaderApp.onWindowClosing();
		super.onLowMemory();
	}

	public void showSelectionPanel() {
		final ZLTextView view = myReaderApp.getTextView();
		((SelectionPopup)myReaderApp.getPopupById(SelectionPopup.ID))
		.move(view.getSelectionStartY(), view.getSelectionEndY());
		myReaderApp.showPopup(SelectionPopup.ID);
	}

	public void hideSelectionPanel() {
		final ReaderApp.PopupPanel popup = myReaderApp.getActivePopup();
		if (popup != null && popup.getId() == SelectionPopup.ID) {
			myReaderApp.hidePopup2();
		}
	}
	
	private void onPreferencesUpdate(Book book) {
		AndroidFontUtil.clearFontCache();
		
		final BookModel model = myReaderApp.Model;
		if (book == null || model == null || model.Book == null) {
			//Log.d("log", "book == null || model == null || model.Book == null");
			return;
		}

		final String newEncoding = book.getEncodingNoDetection();
		final String oldEncoding = model.Book.getEncodingNoDetection();

		model.Book.updateFrom(book);

		if (newEncoding != null && !newEncoding.equals(oldEncoding)) {
			Log.d("log", "reload book");
			myReaderApp.reloadBook();
		} else { 
			Log.d("log", "no reload book");
			ZLTextHyphenator.Instance().load(model.Book.getLanguage());
			myReaderApp.clearTextCaches();
			myReaderApp.getViewWidget().repaint();

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_PREFERENCES:
			ReaderApp fullReader = (ReaderApp)ReaderApp.Instance();
			fullReader.showBookTextView();
			break;
		case REQUEST_BOOK_INFO:
			if (resultCode != RESULT_DO_NOTHING) {
				final Book book = BookInfoActivity.bookByIntent(data);
				if (book != null) {
					getCollection().bindToService(this, new Runnable() {
						public void run() {
							myReaderApp.Collection.saveBook(book, true);
							onPreferencesUpdate(book);
						}
					});
				}
			}
			break;
		case REQUEST_CANCEL_MENU:
			//myReaderApp.runCancelAction(resultCode - 1);
			break;
		}
		try {
			facebook.authorizeCallback(requestCode, resultCode, data);
		} catch(NullPointerException e){}
	}

	public void navigate() {
		((NavigationPopup)myReaderApp.getPopupById(NavigationPopup.ID)).runNavigation();
	}

	private Menu addSubMenu(Menu menu, String id) {
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		return application.myMainWindow.addSubMenu(menu, id);
	}

	private void addMenuItem(Menu menu, String actionId, int icon, boolean isAction) {
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		application.myMainWindow.addMenuItem(menu, actionId, icon, null, isAction);
	}
	
	private void addMenuItem(Menu menu, String actionId, String name) {
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		application.myMainWindow.addMenuItem(menu, actionId, null, name);
	}
	
	private void addMenuItem(Menu menu, String actionId, int iconId) {
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		application.myMainWindow.addMenuItem(menu, actionId, iconId, null);
	}
	
	private void addMenuItem(Menu menu, String actionId) {
		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		application.myMainWindow.addMenuItem(menu, actionId, null, null);
	}
	
	private void initBatteryAndTimeBar() {		
			//batteryIV.setVisibility(View.VISIBLE);
			
		}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		

		int resAdd, resLib, resNetLib, resToc, resBookmark, resQuotes = 0, resNight, resDay, resSearch,resSet = 0, resFontInc = 0, resFontDec = 0;
		int resBookmarks = 0;

		int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
		int settingsIcon = 0;
		switch(theme) {
			case IConstants.THEME_MYBLACK: {
				resFontInc =  R.drawable.theme_black_ic_zoom_p;
				resFontDec = R.drawable.theme_black_ic_zoom_m;
				resSet = R.drawable.icon_settings;
				settingsIcon = R.drawable.settings_icon_black;
				resQuotes = R.drawable.theme_black_but_citata;
				resBookmarks = R.drawable.bookmarks_marble;
				resToc = R.drawable.theme_black_ic_menu_toc;
				googlePlusIcon = R.drawable.google_plus_marble;
				break;
			}
			case IConstants.THEME_LAMINAT: {
				resFontInc =  R.drawable.theme_laminat_ic_zoom_p;
				googlePlusIcon = R.drawable.google_plus_laminat;
				resFontDec = R.drawable.theme_laminat_ic_zoom_m;
				resSet = R.drawable.icon_settings;
				settingsIcon = R.drawable.settings_icon;
				resQuotes = R.drawable.theme_laminat_but_citata;
				resBookmarks = R.drawable.bookmarks_laminat;
				resToc = R.drawable.theme_laminat_ic_menu_toc;
				break;
			}
			case IConstants.THEME_REDTREE: {
				resFontInc =  R.drawable.theme_redtree_ic_zoom_p;
				googlePlusIcon = R.drawable.google_plus_red_tree;
				resFontDec = R.drawable.theme_redtree_ic_zoom_m;
				resSet = R.drawable.icon_settings;
				settingsIcon = R.drawable.settings_icon_red;
				resQuotes = R.drawable.theme_redtree_but_citata;
				resBookmarks = R.drawable.bookmarks_red;
				resToc = R.drawable.theme_redtree_ic_menu_toc;
				break;
			}
		}
		
		resAdd =  R.drawable.ic_menu_addbookmark;
		resLib = R.drawable.icon_lib;
		resNetLib = R.drawable.icon_netlib;
		resToc = R.drawable.ic_menu_toc;
		resBookmark = R.drawable.icon_mark;
		
		resNight = R.drawable.icon_night;
		resDay = R.drawable.icon_day;
		resSearch = R.drawable.icon_search;
		
		//        	break;}
		//        case IConstants.THEME_MYBLACK:{
		//        	resAdd =  R.drawable.ic_menu_addbookmark;
		//        	resLib = R.drawable.theme_black_ic_menu_library;
		//        	resNetLib = R.drawable.theme_black_ic_menu_networklibrary;
		        	
		//        	resBookmark = R.drawable.theme_black_ic_menu_bookmarks;
		//        	resQuotes = R.drawable.theme_black_ic_menu_quotes;
		//        	resNight = R.drawable.theme_black_ic_menu_night;
		//        	resDay = R.drawable.theme_black_ic_menu_day;
		//        	resSearch = R.drawable.theme_black_ic_menu_search;
		//        	break;}
		//        case IConstants.THEME_LAMINAT:{
		//        	resAdd =  R.drawable.ic_menu_addbookmark;
		//        	resLib = R.drawable.theme_laminat_ic_menu_library;
		//        	resNetLib = R.drawable.theme_laminat_ic_menu_networklibrary;
		//        	resToc = R.drawable.theme_laminat_ic_menu_toc;
		//        	resBookmark = R.drawable.theme_laminat_ic_menu_bookmarks;
		//        	resQuotes = R.drawable.theme_laminat_ic_menu_quotes;
		//        	resNight = R.drawable.theme_laminat_ic_menu_night;
		//        	resDay = R.drawable.theme_laminat_ic_menu_day;
		//        	resSearch = R.drawable.theme_laminat_ic_menu_search;
		//        	break;}
		//        case IConstants.THEME_REDTREE:{
		//        	resAdd =  R.drawable.ic_menu_addbookmark;
		//        	resLib = R.drawable.theme_redtree_ic_menu_library;
		//        	resNetLib = R.drawable.theme_redtree_ic_menu_networklibrary;
		       	
		//        	resBookmark = R.drawable.theme_redtree_ic_menu_bookmarks;
		//        	resQuotes = R.drawable.theme_redtree_ic_menu_quotes;
		//        	resNight = R.drawable.theme_redtree_ic_menu_night;
		//        	resDay = R.drawable.theme_redtree_ic_menu_day;
		//        	resSearch = R.drawable.theme_redtree_ic_menu_search;
		//        	break;}
		//        case IConstants.THEME_STELLAJ:{
		//        	resAdd =  R.drawable.ic_menu_addbookmark;
		//        	resLib = R.drawable.theme_stellaj_ic_menu_library;
		//        	resNetLib = R.drawable.theme_stellaj_ic_menu_networklibrary;
		 //       	resToc = R.drawable.theme_stellaj_ic_menu_toc;
		//        	resBookmark = R.drawable.theme_stellaj_ic_menu_bookmarks;
		//        	resQuotes = R.drawable.theme_stellaj_ic_menu_quotes;
		//        	resNight = R.drawable.theme_stellaj_ic_menu_night;
		//        	resDay = R.drawable.theme_stellaj_ic_menu_day;
		//        	resSearch = R.drawable.theme_stellaj_ic_menu_search;
		//        	break;}
		//        }
		if(loadCurrentLanguage().equals("en")){
			item = menu.add ("Fullscreen mode");
			item.setIcon(R.drawable.full_screen_icon);
		} else if(loadCurrentLanguage().equals("de")){
			item = menu.add ("Vollbild");
			item.setIcon(R.drawable.full_screen_icon);
		} else if(loadCurrentLanguage().equals("fr")){
			item = menu.add ("Plein écran");
			item.setIcon(R.drawable.full_screen_icon);
		} else if(loadCurrentLanguage().equals("uk")){
			item = menu.add ("Повноекранний режим");
			item.setIcon(R.drawable.full_screen_icon);
		} else if(loadCurrentLanguage().equals("ru")){
			item = menu.add ("Полноэкранный режим");
			item.setIcon(R.drawable.full_screen_icon);
		} else {
			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
				item = menu.add ("Полноэкранный режим");
				item.setIcon(R.drawable.full_screen_icon);
			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
				item = menu.add ("Звичайний режим");
				item.setIcon(R.drawable.full_screen_icon);
			} else {
				item = menu.add ("Fullscreen mode");
				item.setIcon(R.drawable.full_screen_icon);
			}
		}
		item.setOnMenuItemClickListener (new OnMenuItemClickListener(){
		    @Override
		    public boolean onMenuItemClick (MenuItem item){
		    	switchToFullScreen();
		      return true;
		    }
		  });
		
		if (!mBookFromRarArchive){
				if(loadCurrentLanguage().equals("en")){
				addMenuItem(menu, MENU_BOOKMARK, "Bookmarks",R.drawable.ic_menu_addbookmark, true).setTitle("Bookmarks");
			} else if(loadCurrentLanguage().equals("de")){
				addMenuItem(menu, MENU_BOOKMARK, "Bookmarks",R.drawable.ic_menu_addbookmark, true).setTitle("Bookmarks");
			} else if(loadCurrentLanguage().equals("fr")){
				addMenuItem(menu, MENU_BOOKMARK, "Bookmarks",R.drawable.ic_menu_addbookmark, true).setTitle("Favoris");
			} else if(loadCurrentLanguage().equals("uk")){
				addMenuItem(menu, MENU_BOOKMARK, "Bookmarks",R.drawable.ic_menu_addbookmark, true).setTitle("Закладки");
			} else if(loadCurrentLanguage().equals("ru")){
				addMenuItem(menu, MENU_BOOKMARK, "Bookmarks",R.drawable.ic_menu_addbookmark, true).setTitle("Закладки");
			} else {
				if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
					addMenuItem(menu, MENU_BOOKMARK, "Bookmarks",R.drawable.ic_menu_addbookmark, true).setTitle("Закладки");
				} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
					addMenuItem(menu, MENU_BOOKMARK, "Bookmarks",R.drawable.ic_menu_addbookmark, true).setTitle("Закладки");
				} else {
					addMenuItem(menu, MENU_BOOKMARK, "Bookmarks",R.drawable.ic_menu_addbookmark, true).setTitle("Bookmarks");
				}
			}		
		}
		
		//addMenuItem(menu, MENU_BOOKMARK, getResources().getString(resQuotes), 
		//		R.drawable.ic_menu_addbookmark, true);
		
		
		
		
		//		menu.findItem(MENU_BOOKMARK).setEnabled(!startedFromBookmark);
		//		addMenuItem(menu, ActionCode.SHARE_BOOK, R.drawable.ic_menu_search);
		//		final Menu subMenu = addSubMenu(menu, "screenOrientation");
		//		addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_SYSTEM);
		//		addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_SENSOR);
		//		addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_PORTRAIT);
		//		addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_LANDSCAPE);
		//		if (ZLibrary.Instance().supportsAllOrientations()) {
		//			addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_REVERSE_PORTRAIT);
		//			addMenuItem(subMenu, ActionCode.SET_SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
		//		}
		addMenuItem(menu, ActionCode.SWITCH_TO_NIGHT_PROFILE, resNight);
		addMenuItem(menu, ActionCode.SWITCH_TO_DAY_PROFILE, resDay);
		addMenuItem(menu, ActionCode.SEARCH, resSearch);
		
		addMenuItem(menu, ActionCode.SHOW_PREFERENCES,resSet);
		
		addMenuItem(menu, ActionCode.SHOW_BOOKMARKS, resBookmark);
		addMenuItem(menu, ActionCode.SHOW_COLOR_MARKS);
	//	addMenuItem(menu, ActionCode.SHOW_BOOKMARKS, "Google Plus", 
		//	resBookmark, true).setTitle("Google+");
		//addMenuItem(menu, ActionCode.SHOW_LIBRARY, resLib);
		addMenuItem(menu, ActionCode.SHOW_NETWORK_LIBRARY, resNetLib);
		//addMenuItem(menu, MENU_QUOTES, "Quotes", R.drawable.theme_black_ic_menu_quotes, true);
		
		addMenuItem(menu, ActionCode.INCREASE_FONT, resFontInc, true);
		addMenuItem(menu, ActionCode.DECREASE_FONT, resFontDec, true);
		addMenuItem(menu, ActionCode.SHOW_NAVIGATION);
		addMenuItem(menu, ActionCode.SHOW_TOC, resToc);
		addMenuItem(menu, ActionCode.SHOW_BOOK_INFO);
		addMenuItem(menu, MENU_GOOGLE_PLUS, "Google Plus", 
   				googlePlusIcon, true).setTitle("Google+");
		if (android.os.Build.VERSION.SDK_INT <= 11) {
			
			//addMenuItem(menu, MENU_SETTINGS, "Settings", settingsIcon, true).setTitle("Settings");
			if(loadCurrentLanguage().equals("en")){
	   			addMenuItem(menu, MENU_SETTINGS, "Settings", settingsIcon, true).setTitle("Settings");
	   		
				//menu.add(0, MENU_QUOTES, 0, "Цитати");
			} else if(loadCurrentLanguage().equals("de")){
				addMenuItem(menu, MENU_SETTINGS, "Settings", settingsIcon, true).setTitle("Einstellungen");
			
				//menu.add(0, MENU_QUOTES, 0, "Цитати");
			} else if(loadCurrentLanguage().equals("fr")){
				addMenuItem(menu, MENU_SETTINGS, "Settings", settingsIcon, true).setTitle("Paramètres");
				
			} else if(loadCurrentLanguage().equals("uk")){
				//addMenuItem(menu, ActionCode.SHOW_QUOTES, resQuotes);
				addMenuItem(menu, MENU_SETTINGS, "Settings", settingsIcon, true).setTitle("Настройки");
				
			} else if(loadCurrentLanguage().equals("ru")){
				addMenuItem(menu, MENU_SETTINGS, "Settings", settingsIcon, true).setTitle("Настройки");
			} else {
				if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
					addMenuItem(menu, MENU_SETTINGS, "Settings", settingsIcon, true).setTitle("Настройки");
				} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
					addMenuItem(menu, MENU_SETTINGS, "Settings", settingsIcon, true).setTitle("Настройки");
				} else {
					addMenuItem(menu, MENU_SETTINGS, "Settings", settingsIcon, true).setTitle("Settings");
				}
			}
		}
		
		synchronized (myPluginActions) {
			int index = 0;
			for (PluginApi.ActionInfo info : myPluginActions) {
				if (info instanceof PluginApi.MenuActionInfo) {
					addMenuItem(
							menu,
							PLUGIN_ACTION_PREFIX + index++,
							((PluginApi.MenuActionInfo)info).MenuItemName
							);
				}
			}
		}
		
		if(loadCurrentLanguage().equals("en")){
			addMenuItem(menu, MENU_QUOTES, "Quotes", resQuotes, true).setTitle("Quotes");
			addMenuItem(menu, MENU_EXIT, "Exit", R.drawable.exit, false).setTitle("Exit");
			//menu.add(0, MENU_QUOTES, 0, "Цитати");
		} else if(loadCurrentLanguage().equals("de")){
			addMenuItem(menu, MENU_QUOTES, "Zitate", resQuotes, true).setTitle("Zitate");
			addMenuItem(menu, MENU_EXIT, "Exit", R.drawable.exit, false).setTitle("Ausgang");
			//menu.add(0, MENU_QUOTES, 0, "Цитати");
		} else if(loadCurrentLanguage().equals("fr")){
			addMenuItem(menu, MENU_QUOTES, "Citations", resQuotes, true).setTitle("Citations");
			addMenuItem(menu, MENU_EXIT, "Exit", R.drawable.exit, false).setTitle("Quitter");
		} else if(loadCurrentLanguage().equals("uk")){
			//addMenuItem(menu, ActionCode.SHOW_QUOTES, resQuotes);
			addMenuItem(menu, MENU_QUOTES, "Citations", resQuotes, true).setTitle("Цитати");
			addMenuItem(menu, MENU_EXIT, "Exit", R.drawable.exit, false).setTitle("Вихiд");
		} else if(loadCurrentLanguage().equals("ru")){
			addMenuItem(menu, MENU_QUOTES, "Citations", resQuotes, true).setTitle("Цитаты");
			addMenuItem(menu, MENU_EXIT, "Exit", R.drawable.exit, false).setTitle("Выход");
		} else {
			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
				addMenuItem(menu, MENU_QUOTES, "Цитаты", resQuotes, true).setTitle("Цитаты");
				addMenuItem(menu, MENU_EXIT, "Exit", R.drawable.exit, false).setTitle("Выход");
			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
				addMenuItem(menu, MENU_QUOTES, "Цитати", resQuotes, true).setTitle("Цитати");
				addMenuItem(menu, MENU_EXIT, "Exit", R.drawable.exit, false).setTitle("Вихiд");
			} else {
				//addMenuItem(menu, ActionCode.SHOW_QUOTES, "Цитати", resQuotes, true);
				addMenuItem(menu, ActionCode.SHOW_QUOTES, resQuotes);
				addMenuItem(menu, MENU_EXIT, "Exit", R.drawable.exit, false).setTitle("Exit");
			}
		}

		final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
		application.myMainWindow.refresh();
		super.onCreateOptionsMenu(menu);
		optionsMenu = menu;
		return true;
	}
	
	void addSelectionBookmark() {
		addSelectionBookmark(null);
	}
	
	void addSelectionBookmark(String firstWord) {
		Log.d("MyLog", "Add selection bookmark");
		final FBView fbView = myReaderApp.getTextView();

		String text = null;
		if(firstWord == null){
			fbView.onFingerLongPress(60, 60);
		}
		
		text = fbView.getSelectedText();	
		
		final Bookmark bookmark = new Bookmark(
				myReaderApp.Model.Book,
				fbView.getModel().getId(),
				(text.isEmpty()?fbView.getFirstWordStart():fbView.getSelectionStartPosition()),
				text,
				true
				);
		myReaderApp.Collection.saveBookmark(bookmark);
		fbView.clearSelection();

		UIUtil.showMessageText(
				this,
				ZLResource.resource("selection").getResource("bookmarkCreated").getValue()
				);
	}

	void addSelectionQuotes() {
		Log.d("MyLog", "Add selection quote");
		final FBView fbView = myReaderApp.getTextView();
		final String text = fbView.getSelectedText();
		/*final FBView fbView = myReaderApp.getTextView();
		final String text = fbView.getSelectedText();

		final Quote quote = new Quote(
				
				myReaderApp.Model.Book,
				fbView.getModel().getId(),
				fbView.getSelectionStartPosition(),
				text,
				true
				);
		myReaderApp.Collection.saveQuote(quote);
		fbView.clearSelection();

		UIUtil.showMessageText(
				this,
				ZLResource.resource("selection").getResource("quoteCreated").getValue().replace("%s", text)
				);*/
		
		//saveBookmark(myBook.getTitle(), myBook.authors().get(0).DisplayName, myBook.getId());
		/*if(myBook.authors().size() > 0) {
			saveMyQuote(text, myBook.getTitle(), myBook.authors().get(0).DisplayName, myBook.getId());
		} else {
			saveMyQuote(text, myBook.getTitle(), "Author unknown", myBook.getId());
		}*/
		
		if(myBook.authors().size() > 0) {
			saveMyQuote(text, myBook.getTitle(), myBook.authors().get(0).DisplayName, myBook.getId());
			ReaderApp.bookAuthors.put(String.valueOf(myBook.getId()), myBook.authors().get(0).DisplayName);
		} else if(myBook.authors().isEmpty()){
			if(loadCurrentLanguage().equals("en")){
				saveMyQuote(text,myBook.getTitle(), "Author unknown", myBook.getId());
			} else if(loadCurrentLanguage().equals("de")){
				saveMyQuote(text,myBook.getTitle(), "Autor unbekannt", myBook.getId());
			} else if(loadCurrentLanguage().equals("fr")){
				saveMyQuote(text,myBook.getTitle(), "Auteur inconnu", myBook.getId());
			} else if(loadCurrentLanguage().equals("uk")){
				saveMyQuote(text,myBook.getTitle(), "Автор невiдомий", myBook.getId());
			} else if(loadCurrentLanguage().equals("ru")){
				saveMyQuote(text,myBook.getTitle(), "Автор неизвестен", myBook.getId());
			} else {
				if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
					saveMyQuote(text, myBook.getTitle(), "Автор неизвестен", myBook.getId());
				} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
					saveMyQuote(text,myBook.getTitle(), "Автор невiдомий", myBook.getId());
				} else {
					saveMyQuote(text,myBook.getTitle(), "Author unknown", myBook.getId());
				}
			}				
		} else {
			saveMyQuote(text, myBook.getTitle(), ReaderApp.bookAuthors.get(String.valueOf(myBook.getId())), myBook.getId());
		}
	}
	
	
	void addSelectionQuotes(SelectedMarkInfo info, FBView fbview) {
		final FBView fbView = myReaderApp.getTextView();
		final String text = fbView.getSelectedText();

		if(myBook.authors().size() > 0) {
			saveMyQuote(text, myBook.getTitle(), myBook.authors().get(0).DisplayName, myBook.getId(), info, fbview);
			ReaderApp.bookAuthors.put(String.valueOf(myBook.getId()), myBook.authors().get(0).DisplayName);
		} else if(myBook.authors().isEmpty()){
			if(loadCurrentLanguage().equals("en")){
				saveMyQuote(text,myBook.getTitle(), "Author unknown", myBook.getId(), info, fbview);
			} else if(loadCurrentLanguage().equals("de")){
				saveMyQuote(text,myBook.getTitle(), "Autor unbekannt", myBook.getId(), info, fbview);
			} else if(loadCurrentLanguage().equals("fr")){
				saveMyQuote(text,myBook.getTitle(), "Auteur inconnu", myBook.getId(), info, fbview);
			} else if(loadCurrentLanguage().equals("uk")){
				saveMyQuote(text,myBook.getTitle(), "Автор невiдомий", myBook.getId(), info, fbview);
			} else if(loadCurrentLanguage().equals("ru")){
				saveMyQuote(text,myBook.getTitle(), "Автор неизвестен", myBook.getId(), info, fbview);
			} else {
				if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
					saveMyQuote(text, myBook.getTitle(), "Автор неизвестен", myBook.getId(), info, fbview);
				} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
					saveMyQuote(text,myBook.getTitle(), "Автор невiдомий", myBook.getId(), info, fbview);
				} else {
					saveMyQuote(text,myBook.getTitle(), "Author unknown", myBook.getId(), info, fbview);
				}
			}				
		} else {
			saveMyQuote(text, myBook.getTitle(), ReaderApp.bookAuthors.get(String.valueOf(myBook.getId())), myBook.getId(), info, fbview);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		ReaderApp fr = (ReaderApp)ReaderApp.Instance();
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if(hyperlinkPressed) {
				if(fr != null) {
					fr.showBookTextView();
				}
				hyperlinkPressed = false;
			} 
			else if (fr.refClicked()){
				fr.goBackToClicked();
			}
			else if(isFullScreen) {
				try {
		    		isFullScreen = false;
		    		getSherlock().getActionBar().show();
		    		if(loadCurrentLanguage().equals("en")){
		    			item.setTitle("Fullscreen mode");
		    		} else if(loadCurrentLanguage().equals("de")){
		    			item.setTitle("Vollbild");
		    		} else if(loadCurrentLanguage().equals("fr")){
		    			item.setTitle("Plein écran");
		    		} else if(loadCurrentLanguage().equals("uk")){
		    			item.setTitle("Повноекранний режим");
		    		} else if(loadCurrentLanguage().equals("ru")){
		    			item.setTitle("Полноэкранный режим");
		    		} else {
		    			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
		    				item.setTitle("Полноэкранный режим");
		    			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
		    				item.setTitle("Повноекранний режим");
		    			} else {
		    				//addMenuItem(menu, ActionCode.SHOW_QUOTES, "Цитати", resQuotes, true);
		    				item.setTitle("Normal mode");
		    			}
		    		}
		    		
		    		if(checkAds()) {
		    			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
		    				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		    			layoutParams.setMargins(0, 0, 0, 0);
		    			myMainView.setLayoutParams(layoutParams);
		    					    			
		    		}
		    		
		    		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
		    				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    				
		    		layoutParams.setMargins(0, 0, 0, convertPixelsToDp(160, FullReaderActivity.this));
		    		
		    	    if(!checkAds()) {
		    	    	adView.setVisibility(View.VISIBLE);
		    	    }
		    	    if(!checkAds()) {
		    	    	myMainView.setLayoutParams(mainViewLayoutParams);
		    	    	batteryBar.setLayoutParams(batteryBarLp);
			    	    timeTV.setLayoutParams(timeBarLp);
			    	    batteryTV.setLayoutParams(batteryBarTVLp);
		    	    }
	    		} catch(NullPointerException e) {
	    			
	    		}
	         } else {
	        	// Если читалась книга из архива - ее нужно удалить из базы
	      		if (mBookFromRarArchive){
	      			Book recent = getCollection().getRecentBook(0);
	      			Log.d("MyLog", "Delete recent book - " + recent.getTitle());
	      			getCollection().removeBook(recent, false);
	      		}
	      		
	      		// Добавляем инфо о прочитанной книге в службу
	     		try {
	     			ZLBooleanOption DropboxSync = new ZLBooleanOption("Syncronization", "DropboxSync", false);
	     			if (DropboxSync.getValue()){
	     				if (mDbxService == null) Log.d("MyLog", "Dbx service is null");
	     				mDbxService.addBook(prepareBookToSync());
	     				mDbxService.syncAll();
	     			}
	     		} catch (RemoteException e) {
	     			// TODO Auto-generated catch block
	     			e.printStackTrace();
	     			Log.d("MyLog", "remote sync exception");
	     		}
	        	 setVolumeControlStream(AudioManager.STREAM_RING);
	        	 finish();
	        	 Log.d("FINISHING BOOK ACTIVITY","FINISHING BOOK ACTIVITY");
	         }
	        return true;
	    }else{
	    	return (myMainView != null && myMainView.onKeyDown(keyCode, event)) || super.onKeyDown(keyCode, event);
	    }
	}
	
/*	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return (myMainView != null && myMainView.onKeyUp(keyCode, event)) || super.onKeyUp(keyCode, event);
	}*/
	
	private void setButtonLight(boolean enabled) {
		try {
			final WindowManager.LayoutParams attrs = getWindow().getAttributes();
			final Class<?> cls = attrs.getClass();
			final Field fld = cls.getField("buttonBrightness");
			if (fld != null && "float".equals(fld.getType().toString())) {
				fld.setFloat(attrs, enabled ? -1.0f : 0.0f);
				getWindow().setAttributes(attrs);
			}
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		}
	}
	
	private PowerManager.WakeLock myWakeLock;
	private boolean myWakeLockToCreate;
	private boolean myStartTimer;
	
	public final void createWakeLock() {
		if (myWakeLockToCreate) {
			synchronized (this) {
				if (myWakeLockToCreate) {
					myWakeLockToCreate = false;
					myWakeLock =
							((PowerManager)getSystemService(POWER_SERVICE)).
							newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Reader");
					myWakeLock.acquire();
				}
			}
		}
		if (myStartTimer) {
			myReaderApp.startTimer();
			myStartTimer = false;
		}
	}

	private final void switchWakeLock(boolean on) {
		if (on) {
			if (myWakeLock == null) {
				myWakeLockToCreate = true;
			}
		} else {
			if (myWakeLock != null) {
				synchronized (this) {
					if (myWakeLock != null) {
						myWakeLock.release();
						myWakeLock = null;
					}
				}
			}
		}
	}

	private BroadcastReceiver myBatteryInfoReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			final int level = intent.getIntExtra("level", 100);
			final ZLAndroidApplication application = (ZLAndroidApplication)getApplication();
			application.myMainWindow.setBatteryLevel(level);
			switchWakeLock(
				hasWindowFocus() &&
				getZLibrary().BatteryLevelToTurnScreenOffOption.getValue() < level
				);
		}
	};

	private AlertDialog dialogReminer;
	protected Facebook facebook;

	private void setScreenBrightnessAuto() {
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = -1.0f;
		getWindow().setAttributes(attrs);
	}

	public void setScreenBrightness(int percent) {
		if (percent < 10) {
			percent = 10;
		} else if (percent > 100) {
			percent = 100;
		}
		final WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.screenBrightness = percent / 100.0f;
		getWindow().setAttributes(attrs);
		getZLibrary().ScreenBrightnessLevelOption.setValue(percent);
	}

	public int getScreenBrightness() {
		final int level = (int)(100 * getWindow().getAttributes().screenBrightness);
		return (level >= 0) ? level : 50;
	}

	private BookCollectionShadow getCollection() {
		return (BookCollectionShadow)myReaderApp.Collection;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.w(TAG, "Preference: "+sharedPreferences.getAll()+", key: "+key);
    
			if(key.equals("daynightenable")|| key.equals("dayStart")|| key.equals("dayEnd")){
				checkDayNightPrefs();
			} else
				if(key.equals("needToRemind") || key.equals("reminders")){
				}
				else
					super.onSharedPreferenceChanged(sharedPreferences, key);
	}
	
	SharedPreferences.OnSharedPreferenceChangeListener prefListener = 
	        new SharedPreferences.OnSharedPreferenceChangeListener() {
	    public void onSharedPreferenceChanged(SharedPreferences prefs,
	            String key) {
	    	Log.d("SP listener", "SP listener");
	        if (key.equals("time")) {
	        }
	    }
	};

	public  void saveBookmark(String bookTitle, String bookAuthror, long bookID) {
		Log.d("MyLog", "Save bookmark");
		ZLTextView view = (ZLTextView)ReaderApp.Instance().getCurrentView();
		ZLTextWordCursor myCursor = view.getStartCursor();
		Log.d("BOOK CURSOR: ", myCursor.toString());
		ReaderApp.bookID = bookID;
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		if(db == null) {
			db = new DatabaseHandler(this);
		}
		boolean isMatch = false;
		Calendar c = Calendar.getInstance();
		System.out.println("Current time => " + c.getTime());

		SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
		String formattedDate = df.format(c.getTime());
		/*if(isCreateFromMyFilesBook) {
			Log.d("myFileOpenedBookPath", myFileOpenedBookPath);
			for(MyBookmark bookmark : db.getAllBookmarks()) {
				Log.d("bookmark p index: " ,String.valueOf(bookmark.getParagraphIndex()));
				if(bookmark.getParagraphIndex() == myCursor.getParagraphIndex()) {
					Toast.makeText(getApplicationContext(), "Sorry, this page already added to your bookmarks list.", Toast.LENGTH_LONG).show();
				} else {
					db.addBookmark(new MyBookmark(myCursor.getParagraphIndex(), myCursor.getElementIndex(),
							myCursor.getCharIndex(), bookTitle, bookAuthror, bookID, formattedDate+" "+today.hour+":"+today.minute, myFileOpenedBookPath, 1));
					myFileOpenedBookPath = "";
					isCreateFromMyFilesBook = false;
				}
			}
			
		} else {*/
			for(MyBookmark bookmark : db.getAllBookmarks()) {
				if(bookmark.getParagraphIndex() == myCursor.getParagraphIndex()) {
					isMatch = true;
				}	
			}
			if(!isMatch) {
				db.addBookmark(new MyBookmark(myCursor.getParagraphIndex(), myCursor.getElementIndex(),
						myCursor.getCharIndex(), bookTitle, bookAuthror, bookID, formattedDate+" "+today.hour+":"+today.minute, "", 0));
				// Добавляем закладку в службу Dropbox
				ZLBooleanOption DropboxSync = new ZLBooleanOption("Syncronization", "DropboxSync", false);
				if (DropboxSync.getValue()){
					SyncedBookmarkInfo sBmkInfo = new SyncedBookmarkInfo(DropboxHelper.NO_ID, bookTitle,
					myCursor.getParagraphIndex(), myCursor.getElementIndex(), myCursor.getCharIndex(), formattedDate+" "+today.hour+":"+today.minute);
					try{
						mDbxService.addBookmark(sBmkInfo);
					}
					catch (Exception e){
						Log.d("MyLog", "Exception on adding bookmark to service");
					}
				}
				if(loadCurrentLanguage().equals("en")){
					Toast.makeText(getApplicationContext(), "Bookmark created", Toast.LENGTH_LONG).show();
				} else if(loadCurrentLanguage().equals("de")){
					Toast.makeText(getApplicationContext(), "Bookmark erstellt", Toast.LENGTH_LONG).show();
				} else if(loadCurrentLanguage().equals("fr")){
					Toast.makeText(getApplicationContext(), "Bookmark créé", Toast.LENGTH_LONG).show();
				} else if(loadCurrentLanguage().equals("uk")){
					Toast.makeText(getApplicationContext(), "Закладка створена", Toast.LENGTH_LONG).show();
				} else if(loadCurrentLanguage().equals("ru")){
					Toast.makeText(getApplicationContext(), "Закладка создана", Toast.LENGTH_LONG).show();
				} else {
					if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
						Toast.makeText(getApplicationContext(), "Закладка создана", Toast.LENGTH_LONG).show();
					} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
						Toast.makeText(getApplicationContext(), "Закладка створена", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(getApplicationContext(), "Bookmark created", Toast.LENGTH_LONG).show();
					}
				}
			} else {
				if(loadCurrentLanguage().equals("en")){
					Toast.makeText(getApplicationContext(), "Bookmark this page has already been created.", Toast.LENGTH_LONG).show();
				} else if(loadCurrentLanguage().equals("de")){
					Toast.makeText(getApplicationContext(), "Bookmark diese Seite wurde bereits erstellt.", Toast.LENGTH_LONG).show();
				} else if(loadCurrentLanguage().equals("fr")){
					Toast.makeText(getApplicationContext(), "Marquer cette page a déjà été créé.", Toast.LENGTH_LONG).show();
				} else if(loadCurrentLanguage().equals("uk")){
					Toast.makeText(getApplicationContext(), "Закладка на цій сторінці вже створена.", Toast.LENGTH_LONG).show();
				} else if(loadCurrentLanguage().equals("ru")){
					Toast.makeText(getApplicationContext(), "Закладка на этой странице уже создана.", Toast.LENGTH_LONG).show();
				} else {
					if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
						Toast.makeText(getApplicationContext(), "Закладка на этой странице уже создана.", Toast.LENGTH_LONG).show();
					} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
						Toast.makeText(getApplicationContext(), "Закладка на цій сторінці вже створена.", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(getApplicationContext(), "Bookmark this page has already been created.", Toast.LENGTH_LONG).show();
					}
				}
				
			}
		//}
		//myCursor.getCharIndex(), bookTitle,bookAuthror, bookID, String.valueOf(day+"."+month+"."+year+" "+today.hour+":"+today.minute)));
		Log.d("PARAGRAPH INDEX", String.valueOf(myCursor.getParagraphIndex()));
		Log.d("CHAR INDEX", String.valueOf(myCursor.getCharIndex()));
		Log.d("ELEMENT INDEX", String.valueOf(myCursor.getElementIndex()));
		
	}
	
	public void saveMyQuote(String quoteText, String bookTitle, String bookAuthor, long bookID) {
		Log.d("MyLog", "Save my quote");
		ZLTextView view = (ZLTextView)ReaderApp.Instance().getCurrentView();
		ZLTextWordCursor myCursor = view.getStartCursor();
		Log.d("BOOK CURSOR: ", myCursor.toString());
		ReaderApp.quoteBookID = bookID;
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		if(db == null) {
			db = new DatabaseHandler(this);
		}
		Calendar c = Calendar.getInstance();
		System.out.println("Current time => " + c.getTime());

		SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		String formattedDate = df.format(c.getTime());
		if(isCreateFromMyFilesBook) {
			Log.d("myFileOpenedBookPath", myFileOpenedBookPath);
			db.addQuote(new MyQuote(quoteText, myCursor.getParagraphIndex(), myCursor.getElementIndex(),
					myCursor.getCharIndex(), bookTitle, bookAuthor, bookID, formattedDate, myFileOpenedBookPath, 1,"-1"));
			myFileOpenedBookPath = "";
			isCreateFromMyFilesBook = false;
		} else {
			Log.d("MyLog", " Create quote not from my files ");
			db.addQuote(new MyQuote(quoteText, myCursor.getParagraphIndex(), myCursor.getElementIndex(),
					myCursor.getCharIndex(), bookTitle, bookAuthor, bookID, formattedDate, "", 0,"-1"));
		}
		// Сохраняем цитату в Dropbox
		ZLBooleanOption DropboxSync = new ZLBooleanOption("Syncronization", "DropboxSync", false);
		if (DropboxSync.getValue()){
			SyncedQuoteInfo sQtInfo = new SyncedQuoteInfo(DropboxHelper.NO_ID,  bookTitle,
				quoteText, myCursor.getParagraphIndex(), myCursor.getElementIndex(), myCursor.getCharIndex(),
				formattedDate+" "+today.hour+":"+today.minute);
			try{
				mDbxService.addQuote(sQtInfo);
			}
			catch (Exception e){}
		}
		//myCursor.getCharIndex(), bookTitle,bookAuthror, bookID, String.valueOf(day+"."+month+"."+year+" "+today.hour+":"+today.minute)));
		Log.d("PARAGRAPH INDEX", String.valueOf(myCursor.getParagraphIndex()));
		Log.d("CHAR INDEX", String.valueOf(myCursor.getCharIndex()));
		Log.d("ELEMENT INDEX", String.valueOf(myCursor.getElementIndex()));
		if(loadCurrentLanguage().equals("en")){
			Toast.makeText(getApplicationContext(), "Quote created", Toast.LENGTH_LONG).show();
		} else if(loadCurrentLanguage().equals("de")){
			Toast.makeText(getApplicationContext(), "Zitat erstellt", Toast.LENGTH_LONG).show();
		} else if(loadCurrentLanguage().equals("fr")){
			Toast.makeText(getApplicationContext(), "Citer créé", Toast.LENGTH_LONG).show();
		} else if(loadCurrentLanguage().equals("uk")){
			Toast.makeText(getApplicationContext(), "Цитата створена", Toast.LENGTH_LONG).show();
		} else if(loadCurrentLanguage().equals("ru")){
			Toast.makeText(getApplicationContext(), "Цитата создана", Toast.LENGTH_LONG).show();
		} else {
			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
				Toast.makeText(getApplicationContext(), "Цитата создана", Toast.LENGTH_LONG).show();
			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
				Toast.makeText(getApplicationContext(), "Цитата створена", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), "Quote created", Toast.LENGTH_LONG).show();
			}
		}
	}

	
	public void saveMyQuote(String quoteText, String bookTitle, String bookAuthor, long bookID, SelectedMarkInfo info, FBView fbview) {
		long quoteId;
		ZLTextView view = (ZLTextView)ReaderApp.Instance().getCurrentView();
		ZLTextWordCursor myCursor = view.getStartCursor();
		ReaderApp.quoteBookID = bookID;
		Time today = new Time(Time.getCurrentTimezone());
		today.setToNow();
		if(db == null) {
			db = new DatabaseHandler(this);
		}
		Calendar c = Calendar.getInstance();
		System.out.println("Current time => " + c.getTime());

		SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		String formattedDate = df.format(c.getTime());
		if(isCreateFromMyFilesBook) {
			quoteId = db.addQuote(new MyQuote(quoteText, myCursor.getParagraphIndex(), myCursor.getElementIndex(),
					myCursor.getCharIndex(), bookTitle, bookAuthor, bookID, formattedDate, myFileOpenedBookPath, 1, String.valueOf(info.color.getIntValue())));
			//+" "+today.hour+":"+today.minute
			myFileOpenedBookPath = "";
			isCreateFromMyFilesBook = false;
		} else {
			quoteId = db.addQuote(new MyQuote(quoteText, myCursor.getParagraphIndex(), myCursor.getElementIndex(),
					myCursor.getCharIndex(), bookTitle, bookAuthor, bookID, formattedDate, "", 0, String.valueOf(info.color.getIntValue())));
		}
		// Сохраняем инфо о цветной пометке
		info.updateQuoteId(quoteId);
		db.addColorMark(info, bookID);
		// Добавляем цитату в текущий список цитат для отрисовки
		fbview.addColorMark(info);
		// Сохраняем выделенную цитату в Dropbox
		ZLBooleanOption DropboxSync = new ZLBooleanOption("Syncronization", "DropboxSync", false);
		if (DropboxSync.getValue()){
        	String hexStr = String.format("#%06X", (0xFFFFFF & info.color.getIntValue()));
			SyncedColorMarkInfo sCMInfo = new SyncedColorMarkInfo(DropboxHelper.NO_ID, bookTitle,
					quoteText, formattedDate+" "+today.hour+":"+today.minute, myCursor.getParagraphIndex(), myCursor.getElementIndex(), myCursor.getCharIndex(),
					info.startCursor.getParagraphIndex(), info.startCursor.getElementIndex(), info.startCursor.getCharIndex(),
					info.endCursor.getParagraphIndex(), info.endCursor.getElementIndex(), info.endCursor.getCharIndex(),
					info.color.getIntValue(), hexStr);
			try{
				mDbxService.addColorMark(sCMInfo);
			}
			catch (Exception e){}
		}
		
		
		if(loadCurrentLanguage().equals("en")){
			Toast.makeText(getApplicationContext(), "Mark created", Toast.LENGTH_LONG).show();
		} else if(loadCurrentLanguage().equals("de")){
			Toast.makeText(getApplicationContext(), "Erstellt Mark", Toast.LENGTH_LONG).show();
		} else if(loadCurrentLanguage().equals("fr")){
			Toast.makeText(getApplicationContext(), "Mark créé", Toast.LENGTH_LONG).show();
		} else if(loadCurrentLanguage().equals("uk")){
			Toast.makeText(getApplicationContext(), "Помітка створена", Toast.LENGTH_LONG).show();
		} else if(loadCurrentLanguage().equals("ru")){
			Toast.makeText(getApplicationContext(), "Пометка создана", Toast.LENGTH_LONG).show();
		} else {
			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
				Toast.makeText(getApplicationContext(), "Пометка создана", Toast.LENGTH_LONG).show();
			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
				Toast.makeText(getApplicationContext(), "Помітка створена", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getApplicationContext(), "Mark created", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_BOOKMARK:
			/*try {
				addSelectionBookmark();
			} catch(NullPointerException e){
				
			}*/
			for(Author a : myBook.authors()) {
				Log.d("LOG", a.DisplayName);
			}
			//try{
			
				if(myBook.authors().size() > 0) {
					saveBookmark(myBook.getTitle(), myBook.authors().get(0).DisplayName, myBook.getId());
					ReaderApp.bookAuthors.put(String.valueOf(myBook.getId()), myBook.authors().get(0).DisplayName);
				}  else if(myBook.authors().isEmpty()){
					if(loadCurrentLanguage().equals("en")){
						saveBookmark(myBook.getTitle(), "Author unknown", myBook.getId());
					} else if(loadCurrentLanguage().equals("de")){
						saveBookmark(myBook.getTitle(), "Autor unbekannt", myBook.getId());
					} else if(loadCurrentLanguage().equals("fr")){
						saveBookmark(myBook.getTitle(), "Auteur inconnu", myBook.getId());
					} else if(loadCurrentLanguage().equals("uk")){
						saveBookmark(myBook.getTitle(), "Автор невiдомий", myBook.getId());
					} else if(loadCurrentLanguage().equals("ru")){
						saveBookmark(myBook.getTitle(), "Автор неизвестен", myBook.getId());
					} else {
						if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
							saveBookmark(myBook.getTitle(), "Автор неизвестен", myBook.getId());
						} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
							saveBookmark(myBook.getTitle(), "Автор невiдомий", myBook.getId());
						} else {
							saveBookmark(myBook.getTitle(), "Author unknown", myBook.getId());
						}
					}
						
				} else {
					Log.d("BOOK ID ", String.valueOf(myBook.getId()));
					saveBookmark(myBook.getTitle(), ReaderApp.bookAuthors.get(String.valueOf(myBook.getId())), myBook.getId());			
				}
		//	} catch(NullPointerException e) {
			//		saveBookmark(myBook.getTitle(), "Author unknown", myBook.getId());
		//	}
			
			return true;
		case MENU_QUOTES:
			startActivity(new Intent(getApplicationContext(), com.webprestige.fr.citations.QuotesActivity.class)
			.putExtra("isRunFromBook", true)
			.putExtra("cIndex", ReaderApp.quotesCIndex)
			.putExtra("pIndex", ReaderApp.quotesPIndex)
			.putExtra("eIndex", ReaderApp.quotesEIndex)
			.putExtra("fromBook", true));
			//finish();		
			return true;
		case MENU_GOOGLE_PLUS:
			try {
				Intent shareIntent = new PlusShare.Builder(FullReaderActivity.this)
	            .setType("text/plain")
	            .setText("I read " + '"'+myBook.getTitle()+'"'+" using Android app 'FullReader+'. http://play.google.com/store/apps/details?id=com.fullreader")
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
				case MENU_SETTINGS:
					openOptionsMenu();
		    return true;
				case MENU_EXIT:
					/*killAllExcept(this);
					onDestroy();
					System.exit(0);
					android.os.Process.killProcess(android.os.Process.myPid());*/
					Intent intent = new Intent(this, StartScreenActivity.class);
				    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				    intent.putExtra(StartScreenActivity.EXIT_ME, true);
				    startActivity(intent);
				    finish();
		    return true;
		default:
		}

		return false;
	}
	
	public static void killAllExcept(Activity act)
	{
	    for(WeakReference<Activity> ref:activity_stack)
	    {
	        if(ref != null && ref.get() != null)
	        {
	            if(act != null && ref.get().equals(act)) 
	            {
	                continue;//dont finish this up.
	            }
	            ref.get().finish();
	        }
	    }
	    activity_stack.clear();//but clear all the activity references
	}

	private void showReminder() {
		try {
			if(dialogReminer == null){
	
				Builder builder = new AlertDialog.Builder(FullReaderActivity.this);
				builder.setPositiveButton(ZLResource.resource("other").getResource("ok").getValue(), new OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}});
				TextView text = new TextView(this);
				String msg = ZLResource.resource("other").getResource("reminder_msg").getValue();
				msg = msg.replace("\\n", "\n"); 
				text.setText(msg);  
				text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 
						LinearLayout.LayoutParams.WRAP_CONTENT));
				text.setTextSize(16);        
				text.setGravity(Gravity.CENTER);
				text.setPadding(15, 15, 15, 15);
				builder.setView(text);
				dialogReminer = builder.create();
			}
			dialogReminer.show();
		} catch(BadTokenException e) {
			return;
		} catch(IllegalStateException e) {
			return;
		}
	}

	@SuppressWarnings("deprecation")
	public void shareFbQuote(final String text) {
		facebook = new Facebook(IConstants.FACEBOOK_APP_ID);
		final String[] permissions = {"publish_stream"};
		facebook.authorize(FullReaderActivity.this, permissions, new DialogListener () {
			@Override
			public void onComplete(Bundle values) {
				Toast.makeText(FullReaderActivity.this, "Authorization successful", Toast.LENGTH_SHORT).show();
				new Thread(){ public void run() {
					postMassage(text);
				}}.start();		
			}
			
			@Override
			public void onFacebookError(FacebookError e) {
				Toast.makeText(FullReaderActivity.this, "Facebook error, try again later", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onError(DialogError e) {
				Toast.makeText(FullReaderActivity.this, "Error, try again later", Toast.LENGTH_SHORT).show();
			}
			
			@Override
			public void onCancel() {
				Toast.makeText(FullReaderActivity.this, "Authorization canceled", Toast.LENGTH_SHORT).show();
			}

		});
	}
	
	//public void closePopupWindow() {
		//myReaderApp.hideActivePopup();
	//	myReaderApp.hidePopup();
	//}
	
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int itemId = item.getItemId();
		ReaderApp fr = (ReaderApp)ReaderApp.Instance();
		switch (itemId) {
		case android.R.id.home:
			if(hyperlinkPressed) {
				if(fr != null) {
					fr.showBookTextView();
				}
				hyperlinkPressed = false;
			} 
			else if (fr.refClicked()){
				fr.goBackToClicked();
			}
			else finish();
			break;
		}
		return true;
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

	public void showTranslatemenu(final String text){

		Builder builder = new AlertDialog.Builder(FullReaderActivity.this);
		builder.setPositiveButton(ZLResource.resource("other").getResource("ok").getValue(), new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		LinearLayout lay = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.translate_layout, null);
		final EditText tw = (EditText)lay.findViewById(R.id.tw_translation);
		final Spinner spinner = (Spinner) lay.findViewById(R.id.spinner);
		final Spinner spinnerFrom = (Spinner) lay.findViewById(R.id.spinner_from);
		final ProgressBar pb = (ProgressBar) lay.findViewById(R.id.progressBar);

		tw.setText(text);
		tw.setGravity(Gravity.CENTER);

		List<org.geometerplus.zlibrary.core.language.Language> listLang = ZLResource.languages();
		int index = 0;
		String[] names = new String[listLang.size()-1];
		final String[] codes = new String[listLang.size()-1];
		for (org.geometerplus.zlibrary.core.language.Language l : listLang) {
			if(l.Code.equals("system")){
				continue;
			}
			codes[index] = l.Code;
			names[index] = l.Name;
			++index;
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, names);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner.setAdapter(adapter);
		spinner.setPrompt("Title");//[Deutsch, English, FranÃ§ais, Ð ÑƒÑ�Ñ�ÐºÐ¸Ð¹, Ð£ÐºÑ€Ð°Ñ—Ð½Ñ�ÑŒÐºÐ°]
		spinner.setSelection(1);

		spinnerFrom.setAdapter(adapter);
		spinnerFrom.setPrompt("Title");
//		int i=-1;
//		for(String code: codes){
//			if(ZLResource.LanguageOption.getValue().equals(code))
//				break;
//			i++;
//		}
		spinnerFrom.setSelection(3);

		OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
				pb.setVisibility(View.VISIBLE);
				new AsyncTask<Void, Void, Boolean>() {
					String translatedText;
					@Override
					protected Boolean doInBackground(Void... params) {
						Translate.setClientId("FullReader");
						Translate.setClientSecret("J8+1uDnJWrndoKVLpbVV4a1PmzEE0BPazAEVF9ca9Zk=");
						
						try {
							translatedText = Translate.execute(text, 
									getLanguageFromStringCode(codes[spinnerFrom.getSelectedItemPosition()]),//ZLResource.LanguageOption.getValue()), 
									getLanguageFromStringCode(codes[spinner.getSelectedItemPosition()]));
						} catch (Exception e) {
							return false;
						}

						return true;
					}
					@Override
					protected void onPostExecute(Boolean result) {
						if(result){
							tw.setText(translatedText);
							pb.setVisibility(View.GONE);
						}else{
							final ZLResource dialogResource = ZLResource.resource("dialog");
							final ZLResource buttonResource = dialogResource.getResource("button");
							final ZLResource boxResource = dialogResource.getResource("networkError");
							new AlertDialog.Builder(FullReaderActivity.this)
							.setTitle(boxResource.getResource("internalError").getValue())
							.setMessage(boxResource.getResource("couldntConnectToNetworkMessage").getValue())
							.setIcon(0)
							.setPositiveButton(buttonResource.getResource("ok").getValue(), null)
							.create().show();
						}
						super.onPostExecute(result);
					}
				}.execute();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		};

		spinner.setOnItemSelectedListener(onItemSelectedListener);
		spinnerFrom.setOnItemSelectedListener(onItemSelectedListener);

		lay.setPadding(15, 15, 15, 15);

		builder.setView(lay);
		builder.create().show();
	}
	
	@Override
	public void onBackPressed() {
		if(hyperlinkPressed) {
			ReaderApp fr = (ReaderApp)ReaderApp.Instance();
			if(fr != null) {
				fr.showBookTextView();
			}
			hyperlinkPressed = false;
		 } else if(isFullScreen) {
        	 isFullScreen = false;
         }
		 else{
			 Log.d("MyLog", "Finishing reader activity");
		 }
    }  
	
	public String loadCurrentLanguage() {
	    sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
	    return sPref.getString("curLanguage", "");
	}

	private Language getLanguageFromStringCode(String code){
		if(code.equalsIgnoreCase("en"))
			return Language.ENGLISH;
		else if(code.equalsIgnoreCase("ru"))
			return Language.RUSSIAN;
		else if(code.equalsIgnoreCase("fr"))
			return Language.FRENCH;
		else if(code.equalsIgnoreCase("de"))
			return Language.GERMAN;
		else if(code.equalsIgnoreCase("uk"))
			return Language.UKRAINIAN;
		else if(code.equalsIgnoreCase("uk"))
			return getLanguageFromStringCode(Locale.getDefault().getCountry());
		return Language.ENGLISH;
	}
	
	public List<Book> getBooksInFolder(){
		for (Book book : booksByLocation){
			Log.d("MyLog", "Get book - " + book.getTitle());
		}
		return booksByLocation;
	}
	
	public long getCurrentBookId(){
		return currentBookId;
	}
	
	public void setCurrentBookId(long id){
		currentBookId = id;
	}
	
	 private ServiceConnection mConnection = new ServiceConnection(){

			@Override
			public void onServiceConnected(ComponentName className, IBinder service) {
				mDbxService = IDropboxService.Stub.asInterface(service);
			}

			@Override
			public void onServiceDisconnected(ComponentName className) {
				
			}
	    	
	    };
	   
	private SyncedBookInfo prepareBookToSync(){
		Book toUpdate = getCollection().getRecentBook(0);
		myFBReaderApp = (ReaderApp) ReaderApp.Instance();
		ZLTextWordCursor cur = myFBReaderApp.BookTextView.getStartCursor();
		ZLTextPosition position = new ZLTextFixedPosition(cur.getParagraphIndex(), cur.getElementIndex(), cur.getElementIndex());
 		/*ZLTextPosition position = getCollection().getStoredPosition(toUpdate.getId());
 		if (position == null){
 			myFBReaderApp = (ReaderApp) ReaderApp.Instance();
 			ZLTextWordCursor cur = myFBReaderApp.BookTextView.getStartCursor();
 			position = new ZLTextFixedPosition(cur.getParagraphIndex(), cur.getElementIndex(), cur.getElementIndex());
 		}*/
 		return new SyncedBookInfo(DropboxHelper.NO_ID, toUpdate.getTitle(), position.getParagraphIndex(), position.getElementIndex(), position.getCharIndex(), true);
	}

	
	// ------- Переключение в полноэкранный режим или наоборот
	public void switchToFullScreen (){
		if(!isFullScreen) {
    		isFullScreen = true;
    		if(loadCurrentLanguage().equals("en")){
    			item.setTitle("Normal mode");
    		} else if(loadCurrentLanguage().equals("de")){
    			item.setTitle("Common Mode");
    		} else if(loadCurrentLanguage().equals("fr")){
    			item.setTitle("mode commun");
    		} else if(loadCurrentLanguage().equals("uk")){
    			item.setTitle("Звичайний режим");
    		} else if(loadCurrentLanguage().equals("ru")){
    			item.setTitle("Обычный режим");
    		} else {
    			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
    				item.setTitle("Обычный режим");
    			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
    				item.setTitle("Повноекранний режим");
    			} else {
    				//addMenuItem(menu, ActionCode.SHOW_QUOTES, "Цитати", resQuotes, true);
    				item.setTitle("Normal mode");
    			}
    		}
    		item.setIcon(R.drawable.full_screen_icon);
    		getSupportActionBar().hide();
    		try {
	    		if(!checkAds()) {
	    			adView.setVisibility(View.INVISIBLE);
	    		}
    		} catch(NullPointerException e) {
    			
    		}
    		
    	    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams
    	            (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	    lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    	    Resources r = getBaseContext().getResources();
    	    
    	    int px1 = (int) TypedValue.applyDimension(
    	            TypedValue.COMPLEX_UNIT_DIP,
    	            87, 
    	            r.getDisplayMetrics()
    	    );
    	    
    	    int px2 = (int) TypedValue.applyDimension(
    	            TypedValue.COMPLEX_UNIT_DIP,
    	            57, 
    	            r.getDisplayMetrics()
    	    );
    	    
    	    int px3 = (int) TypedValue.applyDimension(
    	            TypedValue.COMPLEX_UNIT_DIP,
    	            3, 
    	            r.getDisplayMetrics()
    	    );
    	    
    	    int px4 = (int) TypedValue.applyDimension(
    	            TypedValue.COMPLEX_UNIT_DIP,
    	            20, 
    	            r.getDisplayMetrics()
    	    );
    	    
    	    
    	    lp.setMargins(px2, 0, 0, px3);
    	    
    	    RelativeLayout.LayoutParams batteryTvLp = new RelativeLayout.LayoutParams
    	            (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	    batteryTvLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    	    batteryTvLp.setMargins(px1, 0, 0, 0);
    	    
    	    RelativeLayout.LayoutParams lpTimeTv = new RelativeLayout.LayoutParams
    	            (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	    lpTimeTv.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
    	    lpTimeTv.setMargins(px4, 0, 0, 0);
    	    lpTimeTv.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    	    
    	    batteryBar.setLayoutParams(lp);
    	    timeTV.setLayoutParams(lpTimeTv);
    	    batteryTV.setLayoutParams(batteryTvLp);
    	    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
    				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    			layoutParams.setMargins(0, 0, 0, 0);
    			myMainView.setLayoutParams(layoutParams);
    			
    	} else {
    		try {
	    		isFullScreen = false;
	    		getSherlock().getActionBar().show();
	    		if(loadCurrentLanguage().equals("en")){
	    			item.setTitle("Fullscreen mode");
	    		} else if(loadCurrentLanguage().equals("de")){
	    			item.setTitle("Vollbild");
	    		} else if(loadCurrentLanguage().equals("fr")){
	    			item.setTitle("Plein écran");
	    		} else if(loadCurrentLanguage().equals("uk")){
	    			item.setTitle("Повноекранний режим");
	    		} else if(loadCurrentLanguage().equals("ru")){
	    			item.setTitle("Полноэкранный режим");
	    		} else {
	    			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
	    				item.setTitle("Полноэкранный режим");
	    			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
	    				item.setTitle("Повноекранний режим");
	    			} else {
	    				//addMenuItem(menu, ActionCode.SHOW_QUOTES, "Цитати", resQuotes, true);
	    				item.setTitle("Normal mode");
	    			}
	    		}
	    		
	    		if(checkAds()) {
	    			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
	    				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	    			layoutParams.setMargins(0, 0, 0, 0);
	    			myMainView.setLayoutParams(layoutParams);
	    			
	    		}
	    		
	    		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
	    				RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	    		
	    		layoutParams.setMargins(0, 0, 0, convertPixelsToDp(160, FullReaderActivity.this));
	    		
	    	    if(!checkAds()) {
	    	    	adView.setVisibility(View.VISIBLE);
	    	    }
	    	    if(!checkAds()) {
	    	    	myMainView.setLayoutParams(mainViewLayoutParams);
	    	    	batteryBar.setLayoutParams(batteryBarLp);
		    	    timeTV.setLayoutParams(timeBarLp);
		    	    batteryTV.setLayoutParams(batteryBarTVLp);
	    	    }
    		} catch(NullPointerException e) {
    			
    		}
    	}
	}
	
}
