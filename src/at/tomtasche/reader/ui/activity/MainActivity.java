/*
	OpenOffice Document Reader is Android's first native ODF Viewer!
    Copyright (C) 2010  Thomas Taschauer - tomtasche@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package at.tomtasche.reader.ui.activity;

import java.io.File;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.Toast;
import at.tomtasche.reader.background.Document;
import at.tomtasche.reader.background.Document.Page;
import at.tomtasche.reader.background.DocumentLoader;
import at.tomtasche.reader.background.LoadingListener;

import com.devspark.appmsg.AppMsg;
import com.fullreader.R;
import com.webprestige.fr.bookmarks.DatabaseHandler;
import com.webprestige.fr.otherdocs.FrDocument;



public class MainActivity extends DocumentActivity implements
		 LoadingListener {


	private static final String EXTRA_TAB_POSITION = "tab_position";



	private int lastPosition;
	private boolean fullscreen;

	private Page currentPage;

	private long loadingStartTime;


	private Runnable saveCroutonRunnable;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Проверка версии
		if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB){
			if (savedInstanceState != null) {
				lastPosition = savedInstanceState.getInt(EXTRA_TAB_POSITION);
			} 

			addLoadingListener(this);
			Intent intent = getIntent();
			if (intent!=null){
				if (intent.getData() != null) {
					loadUri(intent.getData());
					
				}
			}
		}
		
	}



	
	@Override
	protected void onStart() {
		super.onStart();

		
	}

	@Override
	protected void onResume() {
		super.onResume();

		showSaveCrouton();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent.getData() != null) {
			loadUri(intent.getData());
		}
	}

	@Override
	public DocumentLoader loadUri(Uri uri, String password, boolean limit,
			boolean translatable) {
		loadingStartTime = System.currentTimeMillis();

		return super.loadUri(uri, password, limit, translatable);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		/*outState.putInt(EXTRA_TAB_POSITION, getSupportActionBar()
				.getSelectedNavigationIndex());*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		getMenuInflater().inflate(R.menu.menu_main, menu);

		

		return true;
	}



	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		super.onActivityResult(requestCode, resultCode, intent);
	}



	// TODO: that's super ugly - good job :)
	public void showSaveCroutonLater(final File modifiedFile, final Uri fileUri) {
		saveCroutonRunnable = new Runnable() {

			@Override
			public void run() {
				showCrouton(
						"Document successfully saved. You can find it on your sdcard: "
								+ modifiedFile.getName(), new Runnable() {

							@Override
							public void run() {
								share(fileUri);
							}
						}, AppMsg.STYLE_INFO);
			}
		};

		// also execute it immediately, for users who don't see ads
		saveCroutonRunnable.run();
	}

	private void showSaveCrouton() {
		if (saveCroutonRunnable != null) {
			saveCroutonRunnable.run();

			saveCroutonRunnable = null;
		}
	}



	public void share(Uri uri) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setData(uri);
		intent.setType("application/*");
		intent.putExtra(Intent.EXTRA_STREAM, uri);

		try {
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();

			showCrouton(R.string.crouton_error_open_app, null,
					AppMsg.STYLE_ALERT);
		}
	}




	private void leaveFullscreen() {
		//getSupportActionBar().show();

		getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		fullscreen = false;

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (fullscreen && keyCode == KeyEvent.KEYCODE_BACK) {
			leaveFullscreen();

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	/*@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		Page page = getDocument().getPageAt(tab.getPosition());
		showPage(page);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}*/

	private void showPage(Page page) {
		currentPage = page;

		getPageFragment().loadPage(page);
	}

	public void findDocument() {

		final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		// remove mime-type because most apps don't support ODF mime-types
		intent.setType("application/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);

		PackageManager pm = getPackageManager();
		final List<ResolveInfo> targets = pm.queryIntentActivities(intent, 0);
		int size = targets.size();
		String[] targetNames = new String[size];
		for (int i = 0; i < size; i++) {
			targetNames[i] = targets.get(i).loadLabel(pm).toString();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_choose_filemanager);
		builder.setItems(targetNames, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				ResolveInfo target = targets.get(which);
				if (target == null) {
					return;
				}

				intent.setComponent(new ComponentName(
						target.activityInfo.packageName,
						target.activityInfo.name));

				try {
					startActivityForResult(intent, 42);
				} catch (Exception e) {
					e.printStackTrace();

					showCrouton(R.string.crouton_error_open_app,
							new Runnable() {

								@Override
								public void run() {
									findDocument();
								}
							}, AppMsg.STYLE_ALERT);
				}

				dialog.dismiss();
			}
		});
		builder.show();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {




		try {
			// keeps throwing exceptions for some users:
			// Caused by: java.lang.NullPointerException
			// android.webkit.WebViewClassic.requestFocus(WebViewClassic.java:9898)
			// android.webkit.WebView.requestFocus(WebView.java:2133)
			// android.view.ViewGroup.onRequestFocusInDescendants(ViewGroup.java:2384)

			super.onDestroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getPublicKey() {
		return "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDsdGybFkj9/26Fpu2mNASpAC8xQDRYocvVkxbpN6mF8k4a9L5ocnyUAY7sfKb0wjEc5e+vxL21kFKvvW0zEZX8a5wSXUfD5oiaXaiMPrp7cC1YbPPAelZvFEAzriA6pyk7PPKuqtAN2tcTiJED+kpiVAyEVU42lDUqE70xlRE6dQIDAQAB";
	}

	@Override
	public void onSuccess(Document document, Uri uri) {
		showPage(document.getPageAt(0));
		if (loadingStartTime > 0) {
			loadingStartTime = 0;
		}
	}

	@Override
	public void onError(Throwable error, Uri uri) {

	}

	public Page getCurrentPage() {
		return currentPage;
	}
}
