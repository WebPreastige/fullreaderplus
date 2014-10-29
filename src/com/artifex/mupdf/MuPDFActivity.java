/*
  	MuPDF is Copyright 2006-2013 Artifex Software, Inc.
 	This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.artifex.mupdf;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Timer;

import org.geometerplus.android.fbreader.FullReaderActivity;
import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.android.fbreader.StartScreenActivity;
import org.geometerplus.fbreader.fbreader.ReaderApp;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.fullreader.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.plus.PlusShare;
import com.webprestige.fr.bookmarks.DatabaseHandler;
import com.webprestige.fr.otherdocs.FrDocument;

class SearchTaskResult {
	public final String txt;
	public final int   pageNumber;
	public final RectF searchBoxes[];
	static private SearchTaskResult singleton;

	SearchTaskResult(String _txt, int _pageNumber, RectF _searchBoxes[]) {
		txt = _txt;
		pageNumber = _pageNumber;
		searchBoxes = _searchBoxes;
	}

	static public SearchTaskResult get() {
		return singleton;
	}
	
	static public void set(SearchTaskResult r) {
		singleton = r;
	}
}

class ProgressDialogX extends ProgressDialog {
	public ProgressDialogX(Context context) {
		super(context);
	}
	
	private boolean mCancelled = false;

	public boolean isCancelled() {
		return mCancelled;
	}

	@Override
	public void cancel() {
		mCancelled = true;
		super.cancel();
	}
}

public class MuPDFActivity extends Activity
{
	/* The core rendering instance */
	private enum LinkState {DEFAULT, HIGHLIGHT, INHIBIT};
	private final int    TAP_PAGE_MARGIN = 5;
	private static final int    SEARCH_PROGRESS_DELAY = 200;
	private MuPDFCore    core;
	private String       mFileName;
	private ReaderView   mDocView;
	private View         mButtonsView;
	private boolean      mButtonsVisible;
	private EditText     mPasswordView;
	private TextView     mFilenameView;
	private SeekBar      mPageSlider;
	private int          mPageSliderRes;
	private TextView     mPageNumberView;
	private ImageView  mSearchButton;
	private ImageView  mHomeButton;
	private ImageView  mHomeArrow;
	private ImageView  mGooglePlusIcon;
	private ImageView mSettingsIcon;
	private ImageButton  mCancelButton;
	private ImageButton  mOutlineButton;
	private ViewSwitcher mTopBarSwitcher;
// XXX	private ImageButton  mLinkButton;
	private boolean      mTopBarIsSearch;
	private ImageButton  mSearchBack;
	private ImageButton  mSearchFwd;
	private EditText     mSearchText;
	private SafeAsyncTask<Void,Integer,SearchTaskResult> mSearchTask;
	//private SearchTaskResult mSearchTaskResult;
	private AlertDialog.Builder mAlertBuilder;
	private LinkState    mLinkState = LinkState.DEFAULT;
	private final Handler mHandler = new Handler();
	private SharedPreferences settings;
	
	private AdView mAdView;
	private LinearLayout mPdfBottomContainer;
	
	private RelativeLayout mLowerButtons;
	private int settingsIcon;
	private int googlePlusIcon;
	
	private boolean isCBZ = false;

	private Timer timerRemind;
	
	private AlertDialog dialogReminer;
	
	private RelativeLayout layout;
	
	private PowerManager.WakeLock wl;
	
	private FrDocument mFrDocument;
	
	private Handler mReadReminderHandler;
	private Runnable mReadReminderRunnable;

	private MuPDFCore openFile(String path)
	{
		int lastSlashPos = path.lastIndexOf('/');
		mFileName = new String(lastSlashPos == -1
					? path
					: path.substring(lastSlashPos+1));
		System.out.println("Trying to open "+path);
		
		try
		{
			core = new MuPDFCore(path);
			// New file: drop the old outline data
			OutlineActivityData.set(null);
			if(!ReaderApp.PDF_DJVU_BOOKS.contains(path)) {
				ReaderApp.PDF_DJVU_BOOKS.add(new PdfBook(path, mFileName));
				StartScreenActivity.PDF_BOOK_OPENED = true;
			}
			
			mFrDocument = new FrDocument(-1, mFileName, path, FrDocument.DOCTYPE_MUPDF, FrDocument.getDate());
			DatabaseHandler handler = new DatabaseHandler(this);
			long id = handler.hasFrDocument(mFrDocument);
			if (id == -1){
				handler.addFrDocument(mFrDocument);
			}
			else{
				mFrDocument.updateId((int)id);
				handler.updateFrDocumentLastDate(mFrDocument);
			}
			
		}
		catch (Exception e)
		{
			System.out.println(e);
			return null;
		}
		if (path.contains(".cbz")){
			core.setAsCBZ(true);
		}
		return core;
	}
	
	class MyTask extends AsyncTask<Void, Void, Void> {
		 private ProgressDialog dialog;
	   @Override
	   protected void onPreExecute() {
	     super.onPreExecute();

		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB){
			 dialog = new ProgressDialog(MuPDFActivity.this, AlertDialog.THEME_HOLO_DARK);
		}else{
		   dialog = new ProgressDialog(MuPDFActivity.this);
		}
	    
	    // dialog.getWindow().setGravity(Gravity.TOP);
	     dialog.getWindow().setGravity(Gravity.CENTER);
	     if(loadCurrentLanguage().equals("en")){
	   	  dialog.setMessage("Closing ...");
			} else if(loadCurrentLanguage().equals("de")){
				dialog.setMessage("Schließen...");
			} else if(loadCurrentLanguage().equals("fr")){
				dialog.setMessage("Fermeture ...");
			} else if(loadCurrentLanguage().equals("uk")){
				dialog.setMessage("закриття ...");
			} else if(loadCurrentLanguage().equals("ru")){
				dialog.setMessage("Закрывается ...");
			} else {
				if(Locale.getDefault().getDisplayLanguage().equals("русский") ) {
					dialog.setMessage("Закрывается ...");
				} else if( Locale.getDefault().getDisplayLanguage().equals("українська")){
					dialog.setMessage("закриття ...");
				} else {
					dialog.setMessage("Closing ...");
				}
			}
	     	
	     dialog.setCancelable(false);
	     dialog.show();
	    // dialog = ProgressDialog.show(MuPDFActivity.this, "Closing", "Please wait...", true);
	   }
	   
	   public String loadCurrentLanguage() {
		    SharedPreferences sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
		    return sPref.getString("curLanguage", "");
	   }
	   
	   @Override
	   protected Void doInBackground(Void... params) {
	   	 	onDestroy();
	   	 	return null;
	   }
	   
	   @Override
	   protected void onPostExecute(Void result) {
		   super.onPostExecute(result);
		   dialog.dismiss();
		   finish();
	   }
	}
	
	private void openSettings() {
		 Intent intentSettings = new Intent(MuPDFActivity.this, org.geometerplus.android.fbreader.preferences.PreferenceActivity.class);
		 org.geometerplus.android.fbreader.preferences.PreferenceActivity.isOpenFromPdfDjvu = true;
		 startActivityForResult(intentSettings, FullReaderActivity.REQUEST_PREFERENCES);
	}

	public String loadCurrentLanguage() {
	    SharedPreferences sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
	    return sPref.getString("curLanguage", "");
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		this.mReadReminderHandler = new Handler();
		this.mReadReminderRunnable = new Runnable(){
			public void run() { 
				showReminder();
				mReadReminderHandler.postDelayed(mReadReminderRunnable, settings.getLong("timeRemind", 60000));
			}
		};
		
		/*getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        ActionBar bar = getSupportActionBar();
        Drawable actionBarBackground = null;*/
      
       // bar.setBackgroundDrawable(actionBarBackground);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mAlertBuilder = new AlertDialog.Builder(this);

		if (core == null) {
			core = (MuPDFCore)getLastNonConfigurationInstance();
			
			if (savedInstanceState != null && savedInstanceState.containsKey("FileName")) {
				mFileName = savedInstanceState.getString("FileName");
			}
		}
		if (core == null) {
			Intent intent = getIntent();
			if (Intent.ACTION_VIEW.equals(intent.getAction())) {
				Uri uri = intent.getData();
				if (uri.toString().startsWith("content://media/external/file")) {
					// Handle view requests from the Transformer Prime's file manager
					// Hopefully other file managers will use this same scheme, if not
					// using explicit paths.
					Cursor cursor = getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
					if (cursor.moveToFirst()) {
						uri = Uri.parse(cursor.getString(0));
					}
				}
				core = openFile(Uri.decode(uri.getEncodedPath()));
				SearchTaskResult.set(null);
			}
			if (core != null && core.needsPassword()) {
				requestPassword(savedInstanceState);
				return;
			}
		}
		if (core == null)
		{
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle(R.string.open_failed);
			alert.setButton(AlertDialog.BUTTON_POSITIVE, "Dismiss",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			alert.show();
			return;
		}
		
		createUI(savedInstanceState);
		//initReminder();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if(loadCurrentLanguage().equals("en")){
			getMenuInflater().inflate(R.menu.mupdf_menu, menu);
		} else if(loadCurrentLanguage().equals("de")){
			getMenuInflater().inflate(R.menu.mupdf_menu, menu);
		} else if(loadCurrentLanguage().equals("fr")){
			getMenuInflater().inflate(R.menu.mupdf_menu, menu);
		} else if(loadCurrentLanguage().equals("uk")){			
			getMenuInflater().inflate(R.menu.mupdf_menu_ru, menu);
		} else if(loadCurrentLanguage().equals("ru")){
			getMenuInflater().inflate(R.menu.mupdf_menu_ru, menu);  
		} else {
			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
				getMenuInflater().inflate(R.menu.mupdf_menu_ru, menu);
			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
				getMenuInflater().inflate(R.menu.mupdf_menu_ru, menu);
			} else {
				getMenuInflater().inflate(R.menu.mupdf_menu, menu);					 	    
			}
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			openSettings();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);  
        addMenuItem("Google Plus", menu, 0, "Google Plus", R.drawable.google_plus_icon, true);
        if(loadCurrentLanguage().equals("en")){
   		 	addMenuItem("Settings", menu, 1, "Settings", R.drawable.settings_icon, true).setTitle("Settings");
		} else if(loadCurrentLanguage().equals("de")){
			addMenuItem("Settings", menu, 1, "Settings", R.drawable.settings_icon, true).setTitle("Einstellungen");
		} else if(loadCurrentLanguage().equals("fr")){
			addMenuItem("Settings", menu, 1, "Settings", R.drawable.settings_icon, true).setTitle("Paramètres"); 
		} else if(loadCurrentLanguage().equals("uk")){			
			addMenuItem("Settings", menu, 1, "Settings", R.drawable.settings_icon, true).setTitle("Настройки");
		} else if(loadCurrentLanguage().equals("ru")){
			addMenuItem("Settings", menu, 1, "Settings", R.drawable.settings_icon, true).setTitle("Настройки");  
		} else {
			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
				addMenuItem("Settings", menu, 1, "Settings", R.drawable.settings_icon, true).setTitle("Настройки"); 
			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
				addMenuItem("Settings", menu, 1, "Settings", R.drawable.settings_icon, true).setTitle("Настройки"); 
			} else {
				addMenuItem("Settings", menu, 1, "Settings", R.drawable.settings_icon, true).setTitle("Settings");					 	    
			}
		}
		return true;
	}*/
	
	/*protected MenuItem addMenuItem(String name, Menu menu, int id, String resourceKey, int iconId, boolean isVisible) {
		final String label = name;
		final MenuItem item = menu.add(0, id, Menu.NONE, label);
		item.setOnMenuItemClickListener(this);
		item.setIcon(iconId);
		if(isVisible)
		    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		else
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
		return item;
	}*/
	
	/*@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case 0:{
			try {
				Log.d("PDF", "Google play clicked icon!!!");
				Intent shareIntent = new PlusShare.Builder(MuPDFActivity.this)
	            .setType("text/plain")
	            .setText("The best reader for Android!")
	            .setContentUrl(Uri.parse("https://developers.google.com/+/"))
	            .getIntent();
				startActivityForResult(shareIntent, 0);
				MuPDFPageView pageView = (MuPDFPageView) mDocView.getDisplayedView();
				pageView.mPageNumber = 10;
				core.gotoPage(10);
			} catch(ActivityNotFoundException e) {
				//mDocView.setDisplayedViewIndex();
				Log.d("PDF", "return latest page!");
				MuPDFPageView pageView = (MuPDFPageView) mDocView.getDisplayedView();
				pageView.mPageNumber = 10;
				core.gotoPage(10);
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
			}
			
		case 1:
			openSettings();
			break;
		}
		return false;
	}*/
	
	public void requestPassword(final Bundle savedInstanceState) {
		mPasswordView = new EditText(this);
		mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
		mPasswordView.setTransformationMethod(new PasswordTransformationMethod());
		
		AlertDialog alert = mAlertBuilder.create();
		alert.setTitle(R.string.enter_password);
		alert.setView(mPasswordView);
		alert.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (core.authenticatePassword(mPasswordView.getText().toString())) {
					createUI(savedInstanceState);
				} else {
					requestPassword(savedInstanceState);
				}
			}
		});
		alert.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alert.show();
	}
	
	public void createUI(Bundle savedInstanceState) {
		if (core == null)
			return;
		// Now create the UI.
		// First create the document view making use of the ReaderView's internal
		// gesture recognition
		mDocView = new ReaderView(this) {
			private boolean showButtonsDisabled;

			public boolean onSingleTapUp(MotionEvent e) {
				if (e.getX() < super.getWidth()/TAP_PAGE_MARGIN) {
					super.moveToPrevious();
				} else if (e.getX() > super.getWidth()*(TAP_PAGE_MARGIN-1)/TAP_PAGE_MARGIN) {
					super.moveToNext();
				} else if (!showButtonsDisabled) {
					int linkPage = -1;
					if (mLinkState != LinkState.INHIBIT) {
						MuPDFPageView pageView = (MuPDFPageView) mDocView.getDisplayedView();
						if (pageView != null) {
// XXX							linkPage = pageView.hitLinkPage(e.getX(), e.getY());
						}
					}

					if (linkPage != -1) {
						mDocView.setDisplayedViewIndex(linkPage);
					} else {
						if (!mButtonsVisible) {
							showButtons();
						} else {
							hideButtons();
						}
					}
				}
				return super.onSingleTapUp(e);
			}

			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				if (!showButtonsDisabled)
					hideButtons();

				return super.onScroll(e1, e2, distanceX, distanceY);
			}

			public boolean onScaleBegin(ScaleGestureDetector d) {
				// Disabled showing the buttons until next touch.
				// Not sure why this is needed, but without it
				// pinch zoom can make the buttons appear
				showButtonsDisabled = true;
				return super.onScaleBegin(d);
			}

			public boolean onTouchEvent(MotionEvent event) {
				if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
					showButtonsDisabled = false;

				return super.onTouchEvent(event);
			}

			protected void onChildSetup(int i, View v) {
				if (SearchTaskResult.get() != null && SearchTaskResult.get().pageNumber == i)
					((PageView)v).setSearchBoxes(SearchTaskResult.get().searchBoxes);
				else
					((PageView)v).setSearchBoxes(null);

				((PageView)v).setLinkHighlighting(mLinkState == LinkState.HIGHLIGHT);
			}

			protected void onMoveToChild(int i) {
				if (core == null)
					return;
				mPageNumberView.setText(String.format("%d/%d", i+1, core.countPages()));
				mPageSlider.setMax((core.countPages()-1) * mPageSliderRes);
				mPageSlider.setProgress(i * mPageSliderRes);
				if (SearchTaskResult.get() != null && SearchTaskResult.get().pageNumber != i) {
					SearchTaskResult.set(null);
					mDocView.resetupChildren();
				}
			}

			protected void onSettle(View v) {
				// When the layout has settled ask the page to render
				// in HQ
				((PageView)v).addHq();
			}

			protected void onUnsettle(View v) {
				// When something changes making the previous settled view
				// no longer appropriate, tell the page to remove HQ
				((PageView)v).removeHq();
			}

			@Override
			protected void onNotInUse(View v) {
				((PageView)v).releaseResources();
			}
		};
		mDocView.setAdapter(new MuPDFPageAdapter(this, core));

		// Make the buttons overlay, and store all its
		// controls in variables
		makeButtonsView();

		// Set up the page slider
		int smax = Math.max(core.countPages()-1,1);
		mPageSliderRes = ((10 + smax - 1)/smax) * 2;

		// Set the file-name text
		mFilenameView.setText(mFileName);

		// Activate the seekbar
		mPageSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
				mDocView.setDisplayedViewIndex((seekBar.getProgress()+mPageSliderRes/2)/mPageSliderRes);
			}

			public void onStartTrackingTouch(SeekBar seekBar) {}

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				updatePageNumView((progress+mPageSliderRes/2)/mPageSliderRes);
			}
		});
		
		// Activate the search-preparing button
		mSearchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				searchModeOn();
			}
		});
		
		if (core.fileIsCBZ()) mSearchButton.setVisibility(View.GONE);
		
		mHomeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		mHomeArrow.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		mGooglePlusIcon.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mDocView.setDisplayedViewIndex(10);
				try {
					mDocView.setDisplayedViewIndex(10);
					Intent shareIntent = new PlusShare.Builder(MuPDFActivity.this)
		            .setType("text/plain")
		            .setText("The best reader for Android! http://play.google.com/store/apps/details?id=com.fullreader")
		            .setContentUrl(Uri.parse("https://developers.google.com/+/"))
		            .getIntent();
					startActivityForResult(shareIntent, 5);
					mDocView.setDisplayedViewIndex(10);
				} catch(ActivityNotFoundException e) {
					mDocView.setDisplayedViewIndex(10);
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
			}
			//}
		});
		
		mSettingsIcon.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				openSettings();
			}
			//}
		});
		
		mCancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				searchModeOff();
			}
		});
		
		// Search invoking buttons are disabled while there is no text specified
		mSearchBack.setEnabled(false);
		mSearchFwd.setEnabled(false);
		// React to interaction with the text widget
		mSearchText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				boolean haveText = s.toString().length() > 0;
				mSearchBack.setEnabled(haveText);
				mSearchFwd.setEnabled(haveText);

				// Remove any previous search results
				if (SearchTaskResult.get() != null && !mSearchText.getText().toString().equals(SearchTaskResult.get().txt)) {
					SearchTaskResult.set(null);
					mDocView.resetupChildren();
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
		});

		//React to Done button on keyboard
		mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE)
					search(1);
				return false;
			}
		});

		mSearchText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
					search(1);
				return false;
			}
		});
		
		// Activate search invoking buttons
		mSearchBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(-1);
			}
		});
		mSearchFwd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(1);
			}
		});

/* XXX
		mLinkButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				switch(mLinkState) {
				case DEFAULT:
					mLinkState = LinkState.HIGHLIGHT;
					mLinkButton.setImageResource(R.drawable.ic_hl_link);
					//Inform pages of the change.
					mDocView.resetupChildren();
					break;
				case HIGHLIGHT:
					mLinkState = LinkState.INHIBIT;
					mLinkButton.setImageResource(R.drawable.ic_nolink);
					//Inform pages of the change.
					mDocView.resetupChildren();
					break;
				case INHIBIT:
					mLinkState = LinkState.DEFAULT;
					mLinkButton.setImageResource(R.drawable.ic_link);
					break;
				}
			}
		});
*/

		// Reenstate last state if it was recorded
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		mDocView.setDisplayedViewIndex(prefs.getInt("page"+mFileName, 0));

		if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false))
			showButtons();

		if(savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false))
			searchModeOn();

		// Stick the document view and the buttons overlay into a parent view
		layout = new RelativeLayout(this);
		layout.addView(mDocView);
		if(!checkAds()) {
			//initAdMob(layout);
			//initAdMob(mLowerButtons);
			mAdView = (AdView) mButtonsView.findViewById(R.id.pdfAdView);
			AdRequest adRequest = new AdRequest.Builder().build();
			mAdView.loadAd(adRequest);
			mAdView.setAdListener(new AdListener() {
		        public void onAdLoaded() {
		        	mAdView.setVisibility(View.VISIBLE);
		        	ViewTreeObserver observer = mPdfBottomContainer.getViewTreeObserver();
		            observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
		                @Override
		                public void onGlobalLayout() {
		                	RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) mPdfBottomContainer.getLayoutParams();
		                   int height = mPdfBottomContainer.getHeight();
		                   mPdfBottomContainer.setMinimumHeight(height);
		                   int theme = PreferenceManager.getDefaultSharedPreferences(MuPDFActivity.this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
				            switch(theme){
				    	        case IConstants.THEME_MYBLACK:
				    	        	mPdfBottomContainer.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar ));
				    	         break;
				    	        case IConstants.THEME_LAMINAT:
				    	        	mPdfBottomContainer.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar ));
				    	         break;
				    	        case IConstants.THEME_REDTREE:
				    	        	mPdfBottomContainer.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_redtree_action_bar));
				    	         break;
				            }
				            lParams.height = height;
		                }
		            });
		        }
		    });
		}
		layout.addView(mButtonsView);
		
		//layout.setBackgroundResource(R.color.canvas);
		  int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
	        switch(theme){
		        case IConstants.THEME_MYBLACK:
		        // actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar );
		        	this.mPageSlider.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar ));
		        	this.
		        	mTopBarSwitcher.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar ));
		        	layout.setBackgroundResource(R.drawable.theme_black_shelf);
		        	mGooglePlusIcon.setImageResource(R.drawable.google_plus_marble);
		        	googlePlusIcon = R.drawable.google_plus_marble;
		        	//mSearchButton.setBackground(getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar ));
		         break;
		        case IConstants.THEME_LAMINAT:
		       //  actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar );
		        	this.mPageSlider.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar ));
		        	mTopBarSwitcher.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar ));
		        	layout.setBackgroundResource(R.drawable.theme_laminat_shelf);	
		        	settingsIcon = R.drawable.settings_icon;
		        	googlePlusIcon = R.drawable.google_plus_laminat;
		        	mGooglePlusIcon.setImageResource(R.drawable.google_plus_laminat);
		        	//mSearchButton.setBackground(getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar ));
		         break;
		        case IConstants.THEME_REDTREE:
		         //actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_redtree_action_bar );
		        	this.mPageSlider.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_redtree_action_bar));
		        	mTopBarSwitcher.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_redtree_action_bar ));
		        	layout.setBackgroundResource(R.drawable.theme_redtree_shelf);
		        	settingsIcon = R.drawable.settings_icon_red;
		        	googlePlusIcon = R.drawable.google_plus_red_tree;
		        	mGooglePlusIcon.setImageResource(R.drawable.google_plus_red_tree);
		        	//mSearchButton.setBackground(getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar ));
		         break;
	        }
	        setContentView(layout);   
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 5) {
			mDocView.setDisplayedViewIndex(9);
		}
		if (resultCode >= 0)
			mDocView.setDisplayedViewIndex(resultCode);
		super.onActivityResult(requestCode, resultCode, data);
	}

	public Object onRetainNonConfigurationInstance() {
		MuPDFCore mycore = core;
		core = null;
		return mycore;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mFileName != null && mDocView != null) {
			outState.putString("FileName", mFileName);

			// Store current page in the prefs against the file name,
			// so that we can pick it up each time the file is loaded
			// Other info is needed only for screen-orientation change,
			// so it can go in the bundle
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("page"+mFileName, mDocView.getDisplayedViewIndex());
			edit.commit();
		}

		if (!mButtonsVisible)
			outState.putBoolean("ButtonsHidden", true);
		
		if (mTopBarIsSearch)
			outState.putBoolean("SearchMode", true);
	}

	@Override
	protected void onPause() {
		super.onPause();

		stopReminder();
		
		killSearch();

		if (mFileName != null && mDocView != null) {
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("page"+mFileName, mDocView.getDisplayedViewIndex());
			edit.commit();
		}
	}
	
	@Override
	public void onDestroy() {
		if (core != null)
			core.onDestroy();
		core = null;
		super.onDestroy();
	}

	void showButtons() {
		if (core == null)
			return;
		if (!mButtonsVisible) {
			mButtonsVisible = true;
			// Update page number text and slider
			int index = mDocView.getDisplayedViewIndex();
			updatePageNumView(index);
			mPageSlider.setMax((core.countPages()-1)*mPageSliderRes);
			mPageSlider.setProgress(index*mPageSliderRes);
			if (mTopBarIsSearch) {
				mSearchText.requestFocus();
				showKeyboard();
			}

			Animation anim = new TranslateAnimation(0, 0, -mTopBarSwitcher.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mTopBarSwitcher.setVisibility(View.VISIBLE);
				}
				public void onAnimationRepeat(Animation animation) {}
				public void onAnimationEnd(Animation animation) {}
			});
			mTopBarSwitcher.startAnimation(anim);
			
			anim = new TranslateAnimation(0, 0, mPdfBottomContainer.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mPdfBottomContainer.setVisibility(View.VISIBLE);
				}
				public void onAnimationRepeat(Animation animation) {}
				public void onAnimationEnd(Animation animation) {
					mPageNumberView.setVisibility(View.VISIBLE);
				}
			});
			mPdfBottomContainer.startAnimation(anim);
		}
	}

	void hideButtons() {
		if (mButtonsVisible) {
			mButtonsVisible = false;
			hideKeyboard();
			
			Animation anim = new TranslateAnimation(0, 0, 0, -mTopBarSwitcher.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {}
				public void onAnimationRepeat(Animation animation) {}
				public void onAnimationEnd(Animation animation) {
					mTopBarSwitcher.setVisibility(View.INVISIBLE);
				}
			});
			mTopBarSwitcher.startAnimation(anim);
			
			anim = new TranslateAnimation(0, 0, 0, mPdfBottomContainer.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mPageNumberView.setVisibility(View.INVISIBLE);
				}
				public void onAnimationRepeat(Animation animation) {}
				public void onAnimationEnd(Animation animation) {
					mPdfBottomContainer.setVisibility(View.INVISIBLE);
				}
			});
			mPdfBottomContainer.startAnimation(anim);
		}
	}
	
	void searchModeOn() {
		if (!mTopBarIsSearch) {
			mTopBarIsSearch = true;
			//Focus on EditTextWidget
			mSearchText.requestFocus();
			showKeyboard();
			mTopBarSwitcher.showNext();
		}
	}

	void searchModeOff() {
		if (mTopBarIsSearch) {
			mTopBarIsSearch = false;
			hideKeyboard();
			mTopBarSwitcher.showPrevious();
			SearchTaskResult.set(null);
			// Make the ReaderView act on the change to mSearchTaskResult
			// via overridden onChildSetup method.
			mDocView.resetupChildren();
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		initReminder();
		int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
        switch(theme){
	        case IConstants.THEME_MYBLACK:
	        // actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar );
	        	this.mPageSlider.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar ));
	        	mTopBarSwitcher.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar ));
	        	layout.setBackgroundResource(R.drawable.theme_black_shelf);
	        	settingsIcon = R.drawable.settings_icon_black;
	        	googlePlusIcon = R.drawable.google_plus_marble;
	        	mGooglePlusIcon.setImageResource(R.drawable.google_plus_marble);
	        	mSettingsIcon.setImageResource(R.drawable.settings_icon_black);
	        	//mSearchButton.setBackground(getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar ));
	         break;
	        case IConstants.THEME_LAMINAT:
	       //  actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar );
	        	this.mPageSlider.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar ));
	        	mTopBarSwitcher.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar ));
	        	layout.setBackgroundResource(R.drawable.theme_laminat_shelf);
	        	settingsIcon = R.drawable.settings_icon;
	        	googlePlusIcon = R.drawable.google_plus_laminat;
	        	mGooglePlusIcon.setImageResource(R.drawable.google_plus_laminat);
	        	mSettingsIcon.setImageResource(R.drawable.settings_icon);
	        	//mSearchButton.setBackground(getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar ));
	         break;
	        case IConstants.THEME_REDTREE:
	         //actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_redtree_action_bar );
	        	this.mPageSlider.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_redtree_action_bar));
	        	mTopBarSwitcher.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_redtree_action_bar ));
	        	layout.setBackgroundResource(R.drawable.theme_redtree_shelf);
	        	settingsIcon = R.drawable.settings_icon_red;
	        	googlePlusIcon = R.drawable.google_plus_red_tree;
	        	mGooglePlusIcon.setImageResource(R.drawable.google_plus_red_tree);
	        	mSettingsIcon.setImageResource(R.drawable.settings_icon_red);
	        	//mSearchButton.setBackground(getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar ));
	         break;
        }
        setContentView(layout); 
	}

	void updatePageNumView(int index) {
		if (core == null)
			return;
		mPageNumberView.setText(String.format("%d/%d", index+1, core.countPages()));
	}

	void makeButtonsView() {
		mButtonsView = getLayoutInflater().inflate(R.layout.buttons,null);
		mLowerButtons = (RelativeLayout) mButtonsView.findViewById(R.id.lowerButtons);
		mFilenameView = (TextView)mButtonsView.findViewById(R.id.docNameText);
		mPageSlider = (SeekBar)mButtonsView.findViewById(R.id.pageSlider);
		mPageNumberView = (TextView)mButtonsView.findViewById(R.id.pageNumber);
		mSearchButton = (ImageView)mButtonsView.findViewById(R.id.searchButton);
		mHomeButton = (ImageView)mButtonsView.findViewById(R.id.homeButton);
		mHomeArrow = (ImageView)mButtonsView.findViewById(R.id.homeArrow);
		mCancelButton = (ImageButton)mButtonsView.findViewById(R.id.cancel);
		mOutlineButton = (ImageButton)mButtonsView.findViewById(R.id.outlineButton);
		mTopBarSwitcher = (ViewSwitcher)mButtonsView.findViewById(R.id.switcher);
		mSearchBack = (ImageButton)mButtonsView.findViewById(R.id.searchBack);
		mGooglePlusIcon = (ImageView)mButtonsView.findViewById(R.id.googlePlusButton);
		mSettingsIcon = (ImageView)mButtonsView.findViewById(R.id.settingsButton);
		mSearchFwd = (ImageButton)mButtonsView.findViewById(R.id.searchForward);
		mSearchText = (EditText)mButtonsView.findViewById(R.id.searchText);
		mPdfBottomContainer = (LinearLayout) mButtonsView.findViewById(R.id.pdf_bottom_container);
// XXX		mLinkButton = (ImageButton)mButtonsView.findViewById(R.id.linkButton);
		mTopBarSwitcher.setVisibility(View.INVISIBLE);
		mPageNumberView.setVisibility(View.INVISIBLE);
		mPageSlider.setVisibility(View.VISIBLE);
		//if (android.os.Build.VERSION.SDK_INT <= 11) {
			mSettingsIcon.setVisibility(View.GONE);
		//}
	}
	
	private void initAdMob(RelativeLayout layout) {
		AdView adView = new AdView(this);
		adView.setAdUnitId("use-your-own-id");
		adView.setAdSize(AdSize.BANNER);
		RelativeLayout.LayoutParams adViewParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		//the next line is the key to putting it on the bottom
		adViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		adViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);        
		AdRequest adRequest = new AdRequest.Builder().build();
	    adView.loadAd(adRequest);
	    adViewParams.addRule(RelativeLayout.BELOW, R.id.pageSlider);
		layout.addView(adView, adViewParams);
		//layout.addView(adView, 1);
	}

	void showKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.showSoftInput(mSearchText, 0);
	}
	
	private void initReminder() {
		if(settings.getBoolean("needToRemind", false)) {
			mReadReminderHandler.postDelayed(mReadReminderRunnable, settings.getLong("timeRemind", 60000));
		}
	}
	
	private void stopReminder(){
		mReadReminderHandler.removeCallbacks(mReadReminderRunnable);
	}
	
	private void showReminder() {
		try {
			if(dialogReminer == null){
	
				Builder builder = new AlertDialog.Builder(MuPDFActivity.this);
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
		} catch (BadTokenException e) {
			return;
		} catch(IllegalStateException e) {
			return;
		}
	}

	void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
	}

	void killSearch() {
		if (mSearchTask != null) {
			mSearchTask.cancel(true);
			mSearchTask = null;
		}
	}

	void search(int direction) {
		hideKeyboard();
		if (core == null)
			return;
		killSearch();

		final int increment = direction;
		final int startIndex = SearchTaskResult.get() == null ? mDocView.getDisplayedViewIndex() : SearchTaskResult.get().pageNumber + increment;

		final ProgressDialogX progressDialog = new ProgressDialogX(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setTitle(getString(R.string.searching_));
		progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				killSearch();
			}
		});
		progressDialog.setMax(core.countPages());

		mSearchTask = new SafeAsyncTask<Void,Integer,SearchTaskResult>() {
			@Override
			protected SearchTaskResult doInBackground(Void... params) {
				int index = startIndex;

				while (0 <= index && index < core.countPages() && !isCancelled()) {
					publishProgress(index);
					RectF searchHits[] = core.searchPage(index, mSearchText.getText().toString());

					if (searchHits != null && searchHits.length > 0)
						return new SearchTaskResult(mSearchText.getText().toString(), index, searchHits);

					index += increment;
				}
				return null;
			}

			@Override
			protected void onPostExecute(SearchTaskResult result) {
				progressDialog.cancel();
				if (result != null) {
					// Ask the ReaderView to move to the resulting page
					mDocView.setDisplayedViewIndex(result.pageNumber);
				    SearchTaskResult.set(result);
					// Make the ReaderView act on the change to mSearchTaskResult
					// via overridden onChildSetup method.
				    mDocView.resetupChildren();
				} else {
					mAlertBuilder.setTitle(SearchTaskResult.get() == null ? R.string.text_not_found : R.string.no_further_occurences_found);
					AlertDialog alert = mAlertBuilder.create();
					alert.setButton(AlertDialog.BUTTON_POSITIVE, "Dismiss",
							(DialogInterface.OnClickListener)null);
					alert.show();
				}
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				progressDialog.cancel();
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				super.onProgressUpdate(values);
				progressDialog.setProgress(values[0].intValue());
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mHandler.postDelayed(new Runnable() {
					public void run() {
						if (!progressDialog.isCancelled())
						{
							progressDialog.show();
							progressDialog.setProgress(startIndex);
						}
					}
				}, SEARCH_PROGRESS_DELAY);
			}
		};

		mSearchTask.safeExecute();
	}
	
	@Override
	public void onBackPressed() {
		MyTask finishTask = new MyTask();
		finishTask.execute();
	}
	
	@Override
	public boolean onSearchRequested() {
		if (mButtonsVisible && mTopBarIsSearch) {
			hideButtons();
		} else {
			showButtons();
			searchModeOn();
		}
		return super.onSearchRequested();
	}

/*	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mButtonsVisible && !mTopBarIsSearch) {
			hideButtons();
		} else {
			showButtons();
			searchModeOff();
		}
		return super.onPrepareOptionsMenu(menu);
	}*/
}