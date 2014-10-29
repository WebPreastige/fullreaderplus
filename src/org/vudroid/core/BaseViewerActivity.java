/*
Vudroid
Copyright 2010-2011 Pavel Tiunov

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
package org.vudroid.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Locale;

import org.geometerplus.android.fbreader.FullReaderActivity;
import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.vudroid.core.events.CurrentPageListener;
import org.vudroid.core.events.DecodingProgressListener;
import org.vudroid.core.models.CurrentPageModel;
import org.vudroid.core.models.DecodingProgressModel;
import org.vudroid.core.models.ZoomModel;
import org.vudroid.core.views.PageViewZoomControls;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager.BadTokenException;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.fullreader.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.webprestige.fr.bookmarks.DatabaseHandler;
import com.webprestige.fr.otherdocs.FrDocument;

public abstract class BaseViewerActivity extends SherlockActivity implements DecodingProgressListener, CurrentPageListener, OnSharedPreferenceChangeListener, OnMenuItemClickListener
{
	private static final int MENU_EXIT = 0;
	private static final int MENU_GOTO = 1;
	private static final int MENU_FULL_SCREEN = 2;
	private static final int DIALOG_GOTO = 0;
	private static final String DOCUMENT_VIEW_STATE_PREFERENCES = "DjvuDocumentViewState";
	private DecodeService decodeService;
	private DocumentView documentView;
	private ViewerPreferences viewerPreferences;
	private Toast pageNumberToast;
	private CurrentPageModel currentPageModel;
	private ZLResource resource;
	private SharedPreferences settings;
	private AdView adView;
	private PageViewZoomControls pControls;
	
	private AlertDialog dialogReminer;
	
	private FrDocument mFrDocument;
	
	private Handler mReadReminderHandler;
	private Runnable mReadReminderRunnable;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		resource = ZLResource.resource("vudroid");
		
		this.mReadReminderHandler = new Handler();
		this.mReadReminderRunnable = new Runnable(){
			public void run() { 
				showReminder();
				mReadReminderHandler.postDelayed(mReadReminderRunnable, settings.getLong("timeRemind", 60000));
			}
		};
		

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
		
		initDecodeService();
		final ZoomModel zoomModel = new ZoomModel();
		final DecodingProgressModel progressModel = new DecodingProgressModel();
		progressModel.addEventListener(this);
		currentPageModel = new CurrentPageModel();
		currentPageModel.addEventListener(this);
		documentView = new DocumentView(this, zoomModel, progressModel, currentPageModel);
		zoomModel.addEventListener(documentView);
		documentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
		decodeService.setContentResolver(getContentResolver());
		decodeService.setContainerView(documentView);
		documentView.setDecodeService(decodeService);
		decodeService.open(getIntent().getData());
		
		String uri = getIntent().getData().toString();
		int lastIndex = uri.lastIndexOf("/");
		String name = uri.substring(lastIndex+1);
		try{
			name = URLDecoder.decode(name,"UTF-8");
			uri = URLDecoder.decode(uri,"UTF-8");
		}
		catch (Exception e){}
		
		mFrDocument = new FrDocument(-1, name, uri, FrDocument.DOCTYPE_DJVU, FrDocument.getDate());
		DatabaseHandler handler = new DatabaseHandler(this);
		long id = handler.hasFrDocument(mFrDocument);
		if (id == -1){
			handler.addFrDocument(mFrDocument);
		}
		else{
			mFrDocument.updateId((int)id);
			handler.updateFrDocumentLastDate(mFrDocument);
		}
		
		viewerPreferences = new ViewerPreferences(this);
		
		final FrameLayout frameLayout = createMainContainer();
		frameLayout.addView(documentView);
		pControls = createZoomControls(zoomModel);
		pControls.setActivity(this);
		frameLayout.addView(pControls);
		setFullScreen();
		setContentView(frameLayout);
		if(!checkAds()) {
			//initAdMob(frameLayout);
			initAdMob(pControls);
		}
		final SharedPreferences sharedPreferences = getSharedPreferences(DOCUMENT_VIEW_STATE_PREFERENCES, 0);
		documentView.goToPage(sharedPreferences.getInt(getIntent().getData().toString(), 0));
		documentView.showDocument();
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		viewerPreferences.addRecent(getIntent().getData());
		
        getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		//initReminder();
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
	
	/*private void initAdMob(FrameLayout layout) {
		
	    
	    AdView adView = new AdView(this);
	    adView.setAdUnitId("use-your-own-id");
	    adView.setAdSize(AdSize.BANNER);
	    adView.setTag("adView");
	    adView.refreshDrawableState();
	    adView.setVisibility(AdView.VISIBLE);
	             
	    AdRequest adRequest = new AdRequest.Builder()
        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        .addTestDevice("TEST_DEVICE_ID")
        .build();
    adView.loadAd(adRequest);
	             
	    FrameLayout.LayoutParams adParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT, 
				FrameLayout.LayoutParams.WRAP_CONTENT);
		adParams.gravity = Gravity.BOTTOM;
		adParams.gravity = Gravity.CENTER_HORIZONTAL;
	          
        layout.addView(adView, adParams);        
       
	}*/
	
	private void initAdMob(final LinearLayout layout) {
		/*adView = new AdView(this, AdSize.BANNER, "use-your-own-id");
		FrameLayout.LayoutParams adParams = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.WRAP_CONTENT, 
				FrameLayout.LayoutParams.WRAP_CONTENT);
		adParams.gravity = Gravity.BOTTOM;
		adParams.gravity = Gravity.CENTER_HORIZONTAL;
	    layout.addView(adView, adParams);
	    adView.loadAd(new AdRequest());*/
	    
	    adView = new AdView(this);
	    adView.setAdUnitId("use-your-own-id");
	    adView.setAdSize(AdSize.BANNER);
	    adView.setTag("adView");
	    
	             
	    AdRequest adRequest = new AdRequest.Builder()
        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        .addTestDevice("TEST_DEVICE_ID")
        .build();
	    adView.loadAd(adRequest);
	             
	    LinearLayout.LayoutParams adParams = new LinearLayout.LayoutParams(
	    		LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
       //layout.addView(adView, adParams);
	   adView.setVisibility(View.GONE);
	   layout.addView(adView);
	    
	    adView.setAdListener(new AdListener() {
	        public void onAdLoaded() {
	        	adView.refreshDrawableState();
	        	adView.setVisibility(View.VISIBLE);
	        	
	            ViewTreeObserver observer = adView.getViewTreeObserver();
	            observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
	                @Override
	                public void onGlobalLayout() {
	                   LinearLayout.LayoutParams lParams = (LayoutParams) adView.getLayoutParams();
	                   int height = adView.getHeight();
	                   adView.setMinimumHeight(height);
	                   int theme = PreferenceManager.getDefaultSharedPreferences(BaseViewerActivity.this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
		   	            switch(theme){
		   	    	        case IConstants.THEME_MYBLACK:
		   	    	        	adView.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar ));
		   	    	         break;
		   	    	        case IConstants.THEME_LAMINAT:
		   	    	        	adView.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar ));
		   	    	         break;
		   	    	        case IConstants.THEME_REDTREE:
		   	    	        	adView.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_redtree_action_bar));
		   	    	         break;
		   	            }
		   	            lParams.height = height;
	                }
	            });
         
	        }
	    });
	}
	
	public void decodingProgressChanged(final int currentlyDecoding)
	{
		runOnUiThread(new Runnable()
		{
			public void run()
			{
				// getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS, currentlyDecoding == 0 ? 10000 : currentlyDecoding);
			}
		});
	}

	public void currentPageChanged(int pageIndex)
	{
		final String pageText = (pageIndex + 1) + "/" + decodeService.getPageCount();
		if (pageNumberToast != null)
		{
			pageNumberToast.setText(pageText);
		}
		else
		{
			pageNumberToast = Toast.makeText(this, pageText, 300);
		}
		pageNumberToast.setGravity(Gravity.TOP | Gravity.LEFT,0,0);
		pageNumberToast.show();
		saveCurrentPage();
	}

	private void setWindowTitle()
	{
		final String name = getIntent().getData().getLastPathSegment();
		getWindow().setTitle(name);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		setWindowTitle();
	}
	
	@Override
	public void onBackPressed() {
		if (viewerPreferences.isFullScreen())
			{
			getSupportActionBar().show();
			viewerPreferences.setFullScreen(false);
			}
		else
			super.onBackPressed();
	}

	private void setFullScreen()
	{
		if (viewerPreferences.isFullScreen())
		{
			getSupportActionBar().hide();
			pControls.hide();
		}
		else
		{
			getSupportActionBar().show();
			pControls.show();
		}
	}

	private PageViewZoomControls createZoomControls(ZoomModel zoomModel)
	{
		final PageViewZoomControls controls = new PageViewZoomControls(this, zoomModel);
		controls.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
		zoomModel.addEventListener(controls);
		return controls;
	}

	private FrameLayout createMainContainer()
	{
		return new FrameLayout(this);
	}

	private void initDecodeService()
	{
		if (decodeService == null)
		{
			decodeService = createDecodeService();
		}
	}

	protected abstract DecodeService createDecodeService();

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		decodeService.recycle();
		decodeService = null;
		super.onDestroy();
		
	}

	private void saveCurrentPage()
	{
		final SharedPreferences sharedPreferences = getSharedPreferences(DOCUMENT_VIEW_STATE_PREFERENCES, 0);
		final SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(getIntent().getData().toString(), documentView.getCurrentPage());
		editor.commit();
	}
	
	protected MenuItem addMenuItem(String name, Menu menu, int id, String resourceKey, int iconId, boolean isVisible) {
		final String label = name;
		final MenuItem item = menu.add(0, id, Menu.NONE, label);
		item.setOnMenuItemClickListener(this);
		item.setIcon(iconId);
		/*if(isVisible)
		    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		else
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);*/
		return item;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		menu.clear();
		 if(loadCurrentLanguage().equals("en")) {
	   		 	addMenuItem("Settings", menu, 10, "Settings", 0, false).setTitle("Settings");
			} else if(loadCurrentLanguage().equals("de")){
				addMenuItem("Settings", menu, 10, "Settings", 0, false).setTitle("Einstellungen");
			} else if(loadCurrentLanguage().equals("fr")){
				addMenuItem("Settings", menu, 10, "Settings", 0, false).setTitle("Paramètres"); 
			} else if(loadCurrentLanguage().equals("uk")){			
				addMenuItem("Settings", menu, 10, "Settings", 0, false).setTitle("Настройки");
			} else if(loadCurrentLanguage().equals("ru")){
				addMenuItem("Settings", menu, 10, "Settings", 0, false).setTitle("Настройки");  
			} else {
				if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
					addMenuItem("Settings", menu, 10, "Settings", 0, false).setTitle("Настройки"); 
				} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
					addMenuItem("Settings", menu, 10, "Settings", 0, false).setTitle("Настройки"); 
				} else {
					addMenuItem("Settings", menu, 10, "Settings", 0, false).setTitle("Settings");					 	    
				}
			}
		menu.add(0, MENU_EXIT, 0, resource.getResource("vu_exit").getValue());
		menu.add(0, MENU_GOTO, 0, resource.getResource("vu_goto").getValue());
		final MenuItem menuItem = menu.add(0, MENU_FULL_SCREEN, 0, 
				resource.getResource("vu_fullscreen").getValue()).setCheckable(true).setChecked(viewerPreferences.isFullScreen());
		setFullScreenMenuItemText(menuItem);
		return true;
	}
	
	 public String loadCurrentLanguage() {
		    SharedPreferences sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
		    return sPref.getString("curLanguage", "");
  }

	private void setFullScreenMenuItemText(MenuItem menuItem)
	{
		menuItem.setTitle(menuItem.isChecked()?resource.getResource("vu_fullscreen_on").getValue():resource.getResource("vu_fullscreen_off").getValue());
	}

//	@Override
//	public boolean onPrepareOptionsMenu(Menu menu) {
//		
//		return super.onPrepareOptionsMenu(menu);
//	}
//	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
        case android.R.id.home:
        	onBackPressed();
            break;
		case MENU_EXIT:
			System.exit(0);
			return true;
		case MENU_GOTO:
			showDialog(DIALOG_GOTO);
			return true;
		case MENU_FULL_SCREEN:
			item.setChecked(!item.isChecked());
			setFullScreenMenuItemText(item);
			viewerPreferences.setFullScreen(item.isChecked());
			
			//finish();
			//startActivity(getIntent());
			setFullScreen();
			return true;
		case 10:
			openSettings();
			break;
		}
		return false;
	}
	
	private void openSettings() {
		 Intent intentSettings = new Intent(BaseViewerActivity.this, org.geometerplus.android.fbreader.preferences.PreferenceActivity.class);
		 org.geometerplus.android.fbreader.preferences.PreferenceActivity.isOpenFromPdfDjvu = true;
		 startActivityForResult(intentSettings, FullReaderActivity.REQUEST_PREFERENCES);
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{		
		int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
		int themeRes;
		switch(theme){
		case IConstants.THEME_MYBLACK:
			themeRes = R.style.Theme_myBlack;
			break;
		case IConstants.THEME_LAMINAT:
			themeRes = R.style.Theme_Laminat;
			break;
		case IConstants.THEME_REDTREE:
			themeRes = R.style.Theme_Redtree;
			break;
		}

		switch (id)
		{
		case DIALOG_GOTO:
			return new GoToPageDialog(this, documentView, decodeService);
		}
		return null;
	}

	@Override
	public void onResume() {
		super.onResume();
		initReminder();
		int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
		int themeRes;
		switch(theme) {
		case IConstants.THEME_MYBLACK:
			themeRes = R.style.Theme_myBlack;
			break;
		case IConstants.THEME_LAMINAT:
			themeRes = R.style.Theme_Laminat;
			break;
		case IConstants.THEME_REDTREE:
			themeRes = R.style.Theme_Redtree;
			break;
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d("onSharedPreferenceChanged", "onSharedPreferenceChanged");
		if(key.equals(IConstants.THEME_PREF)) {
			recreatethis();
		}     
		/*int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
		int themeRes;
		switch(theme) {
		case IConstants.THEME_MYBLACK:
			themeRes = R.style.Theme_myBlack;
			break;
		case IConstants.THEME_LAMINAT:
			themeRes = R.style.Theme_Laminat;
			break;
		case IConstants.THEME_REDTREE:
			themeRes = R.style.Theme_Redtree;
			break;
		}*/
	}

	private void initReminder() {
		if(settings.getBoolean("needToRemind", false)) {
			mReadReminderHandler.postDelayed(mReadReminderRunnable, settings.getLong("timeRemind", 60000));
		}
	}
	
	private void stopReminder(){
		mReadReminderHandler.removeCallbacks(mReadReminderRunnable);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		stopReminder();
	}
	
	
	private void showReminder() {
		try {
			if(dialogReminer == null){
	
				Builder builder = new AlertDialog.Builder(BaseViewerActivity.this);
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
}
