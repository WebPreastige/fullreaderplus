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

import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipException;

import org.geometerplus.android.fbreader.IConstants;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import at.stefl.opendocument.java.odf.IllegalMimeTypeException;
import at.stefl.opendocument.java.odf.UnsupportedMimeTypeException;
import at.stefl.opendocument.java.odf.ZipEntryNotFoundException;
import at.tomtasche.reader.background.AndroidFileCache;
import at.tomtasche.reader.background.Document;
import at.tomtasche.reader.background.DocumentLoader;
import at.tomtasche.reader.background.DocumentLoader.EncryptedDocumentException;
import at.tomtasche.reader.background.FileLoader;
import at.tomtasche.reader.background.LoadingListener;
import at.tomtasche.reader.background.UpLoader;
import at.tomtasche.reader.ui.widget.PageFragment;
import at.tomtasche.reader.ui.widget.ProgressDialogFragment;

import com.devspark.appmsg.AppMsg;
import com.fullreader.R;
import com.webprestige.fr.bookmarks.DatabaseHandler;
import com.webprestige.fr.otherdocs.FrDocument;

@SuppressLint("NewApi")
public abstract class DocumentActivity extends FragmentActivity implements
		LoaderCallbacks<Document>, DocumentLoadingActivity {

	private static final String EXTRA_URI = "uri";
	private static final String EXTRA_LIMIT = "limit";
	private static final String EXTRA_PASSWORD = "password";
	private static final String EXTRA_TRANSLATABLE = "translatable";

	private ProgressDialogFragment progressDialog;
	private PageFragment pageFragment;

	private List<LoadingListener> loadingListeners;

	private Document document;

	private Handler handler;
	
	public FrDocument mFrDocument;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Проверка версии
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB){
			String msg="";
			if(loadCurrentLanguage().equals("en")){
				msg = "An ability to open odt files is  available only for devices with a version of Android 3.0 and above";
			}
			else if(loadCurrentLanguage().equals("de")){
				msg = "Die Fähigkeit, odt-Datei zu öffnen, ist nur für Geräte mit Android-Version 3.0 und höher verfügbar";
			}
			else if(loadCurrentLanguage().equals("fr")){
				msg = "Possibilité d'ouvrir odt n'est disponible que pour les appareils avec une version d'Android 3.0 et au-dessus";
			}
			else if(loadCurrentLanguage().equals("uk")){
				msg = "Можливість відкриття odt файлів доступна лише для пристроїв з версією Android 3.0 і вище";
			}
			else if(loadCurrentLanguage().equals("ru")){
				msg = "Возможность открытия odt файлов доступна только для устройств с версией Android 3.0 и выше";
			}
			else {
				if(Locale.getDefault().getDisplayLanguage().equals("русский")){
					msg = "Возможность открытия odt файлов доступна только для устройств с версией Android 3.0 и выше";
				}
				else if(Locale.getDefault().getDisplayLanguage().equals("українська")){
					msg = "Можливість відкриття odt файлів доступна лише для пристроїв з версією Android 3.0 і вище";
				}
				else{
					msg = "An ability to open odt files is  available only for devices with a version of Android 3.0 and above";
				}
			}
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
			finish();
		}
		else{
			setTitle("");
			setContentView(R.layout.open_document_main);

			handler = new Handler();
			loadingListeners = new LinkedList<LoadingListener>();

			getSupportLoaderManager().initLoader(0, null, this);
			getSupportLoaderManager().initLoader(1, null, this);

			pageFragment = (PageFragment) getSupportFragmentManager()
					.findFragmentByTag(PageFragment.FRAGMENT_TAG);
			if (pageFragment == null) {
				pageFragment = new PageFragment();
				getSupportFragmentManager()
						.beginTransaction()
						.add(R.id.document_container, pageFragment,
								PageFragment.FRAGMENT_TAG).commit();

				Uri uri = getIntent().getData();
				if (Intent.ACTION_VIEW.equals(getIntent().getAction())
						&& uri != null) {
						loadUri(uri);
				}
			}
			
			// Применение темы
			int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
			ActionBar bar = getActionBar();
			switch(theme){
			case IConstants.THEME_MYBLACK:
				bar.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar));
				break;
			case IConstants.THEME_LAMINAT:
				bar.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar ));
				break;
			case IConstants.THEME_REDTREE:
				bar.setBackgroundDrawable(getResources().getDrawable(com.fullreader.R.drawable.theme_redtree_action_bar ));
				break;
			}
		}
		
	}

	@Override
	public DocumentLoader loadUri(Uri uri) {
		return loadUri(uri, null, true, false);
	}

	public DocumentLoader loadUri(Uri uri, String password) {
		return loadUri(uri, password, true, false);
	}

	public DocumentLoader loadUri(Uri uri, boolean limit) {
		return loadUri(uri, null, limit, false);
	}

	public DocumentLoader loadUri(Uri uri, String password, boolean limit,
			boolean translatable) {
		Bundle bundle = new Bundle();
		bundle.putString(EXTRA_PASSWORD, password);
		bundle.putParcelable(EXTRA_URI, uri);
		bundle.putBoolean(EXTRA_LIMIT, limit);
		bundle.putBoolean(EXTRA_TRANSLATABLE, translatable);
		
		int lastIndex = uri.toString().lastIndexOf("/");
		String name = uri.toString().substring(lastIndex+1);
		mFrDocument = new FrDocument(-1, name, uri.toString(), FrDocument.DOCTYPE_ODT, FrDocument.getDate());
		mFrDocument.updateDate(FrDocument.getDate());
		DatabaseHandler handler = new DatabaseHandler(this);
		long id = handler.hasFrDocument(mFrDocument);
		if (id == -1){
			handler.addFrDocument(mFrDocument);
		}
		else{
			mFrDocument.updateId((int)id);
			handler.updateFrDocumentLastDate(mFrDocument);
		}
		return (DocumentLoader) getSupportLoaderManager().restartLoader(0,
				bundle, this);
	}

	public UpLoader uploadUri(Uri uri) {
		Bundle bundle = new Bundle();
		bundle.putParcelable(EXTRA_URI, uri);

		return (UpLoader) getSupportLoaderManager().restartLoader(1, bundle,
				this);
	}

	@Override
	public Loader<Document> onCreateLoader(int id, Bundle bundle) {
		boolean limit = true;
		boolean translatable = false;
		String password = null;
		Uri uri = DocumentLoader.URI_INTRO;
		if (bundle != null) {
			uri = bundle.getParcelable(EXTRA_URI);
			limit = bundle.getBoolean(EXTRA_LIMIT);
			translatable = bundle.getBoolean(EXTRA_TRANSLATABLE);
			password = bundle.getString(EXTRA_PASSWORD);
		}

		switch (id) {
		case 0:
			DocumentLoader documentLoader = new DocumentLoader(this, uri);
			documentLoader.setPassword(password);
			documentLoader.setLimit(limit);
			documentLoader.setTranslatable(translatable);

			showProgress(documentLoader, false);

			return documentLoader;

		case 1:
		default:
			UpLoader upLoader = new UpLoader(this, uri);

			showProgress(upLoader, true);

			return upLoader;
		}
	}

	@Override
	public void onLoadFinished(final Loader<Document> loader, Document document) {
		dismissProgress();

		FileLoader fileLoader = (FileLoader) loader;
		Throwable error = fileLoader.getLastError();
		final Uri uri = fileLoader.getLastUri();
		if (error != null) {
			handleError(error, uri);
		} else if (document != null) {
			this.document = document;

			// TODO: we should load the first page here already
			// DocumentActivity should - basically - work out-of-the-box
			// (without any further logic)!

			if (document.isLimited()) {
				showCrouton(R.string.toast_info_limited, new Runnable() {

					@Override
					public void run() {
						loadUri(uri, ((DocumentLoader) loader).getPassword(),
								false, false);
					}
				}, AppMsg.STYLE_INFO);
			}

			for (LoadingListener listener : loadingListeners) {
				listener.onSuccess(document, uri);
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Document> loader) {
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (intent == null)
			return;

		Uri uri = intent.getData();
		if (requestCode == 42 && resultCode == RESULT_OK && uri != null)
			loadUri(uri);
	}

	private void showProgress(final Loader<Document> loader,
			final boolean upload) {
		if (progressDialog != null)
			return;

		try {
			progressDialog = new ProgressDialogFragment(upload);

			FragmentTransaction transaction = getSupportFragmentManager()
					.beginTransaction();
			progressDialog.show(transaction,
					ProgressDialogFragment.FRAGMENT_TAG);

			if (!upload) {
				final FileLoader fileLoader = (FileLoader) loader;

				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						if (progressDialog == null)
							return;

						progressDialog.setProgress(fileLoader.getProgress());

						if (loader.isStarted())
							handler.postDelayed(this, 1000);
					}
				}, 1000);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();

			progressDialog = null;
		}
	}

	private void dismissProgress() {
		// dirty hack because committing isn't allowed right after
		// onLoadFinished:
		// "java.lang.IllegalStateException: Can not perform this action inside of onLoadFinished"
		if (progressDialog == null)
			progressDialog = (ProgressDialogFragment) getSupportFragmentManager()
					.findFragmentByTag(ProgressDialogFragment.FRAGMENT_TAG);

		if (progressDialog != null && progressDialog.getShowsDialog()
				&& progressDialog.isNotNull()) {
			try {
				progressDialog.dismissAllowingStateLoss();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

			progressDialog = null;
		}
	}

	public void handleError(Throwable error, final Uri uri) {
		Log.e("OpenDocument Reader", "Error opening file at " + uri.toString(),
				error);

		final Uri cacheUri = AndroidFileCache.getCacheFileUri();

		for (LoadingListener listener : loadingListeners) {
			listener.onError(error, uri);

			// TODO: return here, but only if the listener was registered by a
			// JUnit test
		}

		int errorDescription;
		if (error == null) {
			return;
		} else if (error instanceof EncryptedDocumentException) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.toast_error_password_protected);

			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			builder.setView(input);

			builder.setPositiveButton(getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							loadUri(cacheUri, input.getText().toString());

							dialog.dismiss();
						}
					});
			builder.setNegativeButton(getString(android.R.string.cancel), null);
			builder.show();

			return;
		} else if (error instanceof IllegalMimeTypeException
				|| error instanceof ZipException
				|| error instanceof ZipEntryNotFoundException
				|| error instanceof UnsupportedMimeTypeException) {
			/*AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.toast_error_illegal_file);
			builder.setMessage(R.string.dialog_upload_file);
			builder.setPositiveButton(getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int whichButton) {
							uploadUri(cacheUri);

							dialog.dismiss();
						}
					});
			builder.setNegativeButton(getString(android.R.string.cancel), null);
			builder.show();*/

			return;
		} else if (error instanceof FileNotFoundException) {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED_READ_ONLY)
					|| Environment.getExternalStorageState().equals(
							Environment.MEDIA_MOUNTED)) {
				errorDescription = R.string.toast_error_find_file;
			} else {
				errorDescription = R.string.toast_error_storage;
			}
		} else if (error instanceof IllegalArgumentException) {
			errorDescription = R.string.toast_error_illegal_file;
		} else if (error instanceof OutOfMemoryError) {
			errorDescription = R.string.toast_error_out_of_memory;
		} else {
			errorDescription = R.string.toast_error_generic;
		}

		showCrouton(errorDescription, null, AppMsg.STYLE_ALERT);

		
	}

	public void addLoadingListener(LoadingListener loadingListener) {
		loadingListeners.add(loadingListener);
	}

	public Document getDocument() {
		return document;
	}

	public PageFragment getPageFragment() {
		return pageFragment;
	}

	public void showToast(int resId) {
		showToast(getString(resId));
	}

	public void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	public void showCrouton(int resId, final Runnable callback,
			AppMsg.Style style) {
		showCrouton(getString(resId), callback, style);
	}

	public void showCrouton(final String message, final Runnable callback,
			final AppMsg.Style style) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				AppMsg crouton = AppMsg.makeText(DocumentActivity.this,
						message, style);
				crouton.setDuration(AppMsg.LENGTH_LONG);
				crouton.getView().setOnClickListener(
						new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								if (callback != null)
									callback.run();
							}
						});
				crouton.show();
			}
		});
	}
	
	String loadCurrentLanguage() {
	    SharedPreferences sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
	    return sPref.getString("curLanguage", "");
	}
	
	/*@Override 
	public void onDestroy(){
		super.onDestroy();
		if (mFrDocument != null){
			mFrDocument.updateDate(FrDocument.getDate());
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
	}*/
}
