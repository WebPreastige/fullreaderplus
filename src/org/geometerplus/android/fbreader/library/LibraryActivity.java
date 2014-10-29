/*
* FullReader+
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.android.fbreader.library;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import net.robotmedia.acv.ui.ComicViewerActivity;

import org.geometerplus.android.fbreader.FBUtil;
import org.geometerplus.android.fbreader.FullReaderActivity;
import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.ReaderApplication;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.fbreader.libraryService.SQLiteBooksDatabase;
import org.geometerplus.android.fbreader.tree.TreeActivity;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.book.BooksDatabase;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.fbreader.ReaderApp;
import org.geometerplus.fbreader.library.FileTree;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.fbreader.library.RootTree;
import org.geometerplus.fbreader.library.SearchResultsTree;
import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;
import org.geometerplus.zlibrary.core.filetypes.FileTypeCollection;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.vudroid.djvudroid.DjvuViewerActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.artifex.mupdf.MuPDFActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.plus.PlusShare;
import com.fullreader.R;
import com.webprestige.fr.bookmarks.DatabaseHandler;
import com.webprestige.fr.otherdocs.FrDocument;
import com.webprestige.unrar.RarEntries;
//import com.google.ads.AdView;

public class LibraryActivity extends TreeActivity<LibraryTree> implements MenuItem.OnMenuItemClickListener, View.OnCreateContextMenuListener, IBookCollection.Listener,
OnSharedPreferenceChangeListener {
	static final String START_SEARCH_ACTION = "action.fbreader.library.start-search";

	private int googlePlusIcon = 0;
	public static final String OPEN_TREE_KEY = "opekeyentre";
	public static final int FILESYSTEM_TREE = 0;
	public static final int READ_TREE = 1;
	public static final int SEARCHREQUEST = 2;
	private boolean isDocxPluginInstalled = false;
	//private AdView adView;

	private volatile RootTree myRootTree;
	private Book mySelectedBook;
	private ActionBar bar;
	private Drawable actionBarBackground = null; 
	
	private EditText bookNameEdit;
	private Button renameOk;
	private Button renameCancel;
	
	private LinearLayout mFileInfoMain;
	
	private File sourceFile;
	private boolean copiedToClipboard = false;
	private Menu sherlockMenu;
	private static LibraryTree currentTree;
	
	private LibraryTree rarTree;
	
	public static Context ctx;
	
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		FullReaderActivity.addToActivityStack(this);
		if (myRootTree == null) {
			myRootTree = new RootTree(new BookCollectionShadow());
		}
		myRootTree.Collection.addListener(this);
		ctx = this;
		mySelectedBook =
				SerializerUtil.deserializeBook(getIntent().getStringExtra(FullReaderActivity.BOOK_KEY));

		bar = getSupportActionBar();
        
		int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
		int res = 0;
		switch(theme){
		case IConstants.THEME_MYBLACK:
			setContentView(R.layout.library_body_theme_black);
			res = R.layout.library_shelf_theme_black;
			actionBarBackground = getResources().getDrawable( com.fullreader.R.drawable.theme_black_action_bar );
			//Toast.makeText(getApplicationContext(), "THEME_BLACK", Toast.LENGTH_LONG).show();
			googlePlusIcon = R.drawable.google_plus_marble;
			break;
		case IConstants.THEME_LAMINAT:
			setContentView(R.layout.library_body_theme_laminat);
			res = R.layout.library_shelf_theme_laminat;
			googlePlusIcon = R.drawable.google_plus_laminat;
			actionBarBackground = getResources().getDrawable( com.fullreader.R.drawable.theme_laminat_action_bar );
			//Toast.makeText(getApplicationContext(), "THEME_LAMINAT", Toast.LENGTH_LONG).show();
			break;
		case IConstants.THEME_REDTREE:
			setContentView(R.layout.library_body_theme_redtree);
			res = R.layout.library_shelf_theme_redtree;
			googlePlusIcon = R.drawable.google_plus_red_tree;
			actionBarBackground = getResources().getDrawable( com.fullreader.R.drawable.theme_redtree_action_bar );
			//Toast.makeText(getApplicationContext(), "THEME_REDTREE", Toast.LENGTH_LONG).show();
			break;
		}
		bar.setBackgroundDrawable(actionBarBackground);
		new LibraryTreeAdapter(this, res);
		
		init(getIntent());
		
		getListView().setTextFilterEnabled(true);
		getListView().setOnCreateContextMenuListener(this);
		
		((BookCollectionShadow)myRootTree.Collection).bindToService(this, new Runnable() {
			public void run() {
				setProgressBarIndeterminateVisibility(!myRootTree.Collection.status().IsCompleted);

				switch(getIntent().getIntExtra(OPEN_TREE_KEY, -1)){
				case FILESYSTEM_TREE:
					myRootTree.setVisible(false);
					openTree(myRootTree.getSubTree(LibraryTree.ROOT_FILE_TREE));
					break;
				case READ_TREE:
					myRootTree.setVisible(false);
					final LibraryTree tree = (LibraryTree)myRootTree.getSubTree(LibraryTree.ROOT_RECENT);
					tree.waitForOpening();
					openTree(tree);
					break;
				case SEARCHREQUEST:
					onSearchRequested();
					break;
				}
			}
		});
		Log.d("checkADS: ", String.valueOf(checkAds()));
		if(!checkAds()) {
			initAdMob();
		}
		
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

	
	private void initAdMob() {
		AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .addTestDevice("TEST_DEVICE_ID")
            .build();
        adView.loadAd(adRequest);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (START_SEARCH_ACTION.equals(intent.getAction())) {
			final String pattern = intent.getStringExtra(SearchManager.QUERY);
			if (pattern != null && pattern.length() > 0) {
				startBookSearch(pattern);
			}
		} else {
			super.onNewIntent(intent);
		}
	}

	/*@Override
	public void onResume() {
		super.onResume();
	}*/

	@Override
	protected LibraryTree getTreeByKey(FBTree.Key key) {
		return key != null ? myRootTree.getLibraryTree(key) : myRootTree;
	}
	
	@Override
	protected void onDestroy() {
		myRootTree.Collection.removeListener(this);
		((BookCollectionShadow)myRootTree.Collection).unbind();
		super.onDestroy();
	}
	
	@Override
	public boolean isTreeSelected(FBTree tree) {
		final LibraryTree lTree = (LibraryTree)tree;
		return lTree.isSelectable() && lTree.containsBook(mySelectedBook);
	}
	
	@Override
	protected void onListItemClick(ListView listView, View view, int position, long rowId) {
		try {
			
			final LibraryTree tree = (LibraryTree)getListAdapter().getItem(position);
			Intent newIntent = new Intent();
			// Проверяем, вдруг этот файл находится в архиве
			if (FileTree.class.isInstance(tree)){
				ZLPhysicalFile zlPhFile =  ((FileTree)tree).getFile().getPhysicalFile();
				if (zlPhFile.isRarArchive()){
					RarEntries entries = new RarEntries();
					entries.unrarFile(zlPhFile, mUnrarHandler, this);
					rarTree = tree;
					return;
				}
			}
			int allItems = listView.getChildCount()-1;
			int curPos = position;
			Log.d("tree name", tree.getName());
			Log.d("all items", String.valueOf(allItems));
			/*if (tree.getName().equals("Все файлы")) {
				Log.d("last tree item", "last tree item");
				newIntent.setClass(LibraryActivity.this, AllFilesActivity.class);
				startActivity(newIntent);
			}*/
			//Toast.makeText(getApplicationContext(), tree.getFileName(), Toast.LENGTH_LONG).show();
				if(tree.getFileName().contains(".pdf") || tree.getFileName().contains(".djvu") || tree.getFileName().contains(".cbz") || tree.getFileName().contains(".docx") 
						|| tree.getFileName().contains(".xps") || tree.getFileName().contains(".odt") || tree.getFileName().contains("cbr")){
	
					//	MimeTypeMap myMime = MimeTypeMap.getSingleton();
						
					if(tree.getFileName().contains(".pdf") || tree.getFileName().contains(".xps") || tree.getFileName().contains(".cbz")) {
						Uri uri = Uri.parse(tree.getFileName());
						newIntent.setClass(this, MuPDFActivity.class);
						newIntent.setAction(Intent.ACTION_VIEW);
		    			newIntent.setData(uri);
		    			newIntent.setDataAndType(Uri.fromFile(new File(tree.getFileName())),"");
						startActivity(newIntent);
					}
					if(tree.getFileName().contains(".odt")) {
						Uri uri = Uri.parse("file://" + tree.getFileName());
						newIntent.setClass(this, at.tomtasche.reader.ui.activity.MainActivity.class);
						newIntent.setAction(Intent.ACTION_VIEW);
		    			newIntent.setData(uri);
		    			startActivity(newIntent);
					}
					if(tree.getFileName().contains(".cbr")) {
						Uri uri = Uri.parse(tree.getFileName());
						newIntent.setClass(this, ComicViewerActivity.class);
						newIntent.setAction(Intent.ACTION_VIEW);
		    			newIntent.setData(uri);
		    			startActivity(newIntent);
					}
					if(tree.getFileName().contains(".djvu")) {
						newIntent.setClass(this, DjvuViewerActivity.class);	
						newIntent.setDataAndType(Uri.fromFile(new File(tree.getFileName())),"");
						newIntent.setFlags(newIntent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(newIntent);
						//newIntent.setClass(LibraryActivity.this, AllFilesActivity.class);
					}
					if(tree.getFileName().contains(".docx")) {
						if(checkDocxInstalledOnDevice()) {
							Log.d("DOCX EXIST!!!", "DOCX EXIST!!!");
							if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
								
								int lastIndex = tree.getTreeTitle().lastIndexOf("/");
								String name = tree.getTreeTitle().substring(lastIndex+1);
								FrDocument frDocument = new FrDocument(-1, name, tree.getTreeTitle(), FrDocument.DOCTYPE_DOCX, FrDocument.getDate());
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
		 						 Log.d("pathToDOCXfile", tree.getTreeTitle());
		 						LaunchIntent.putExtra("pathToFile", tree.getTreeTitle());
		 						 startActivity(LaunchIntent);
							}
						} else {
							Log.d("DOCX NOT EXIST!!!", "DOCX NOT EXIST!!!");
							/*if(loadCurrentLanguage().equals("en")){
								Toast.makeText(getApplicationContext(), "You need activate RullReader if you want get possibility to open docx files.", Toast.LENGTH_LONG).show();
							} else if(loadCurrentLanguage().equals("de")){
								Toast.makeText(getApplicationContext(), "Sie müssen aktivieren RullReader wenn Sie die Möglichkeit, docx-Dateien öffnen möchten.", Toast.LENGTH_LONG).show();
							} else if(loadCurrentLanguage().equals("fr")){
								Toast.makeText(getApplicationContext(), "Vous devez activer RullReader si vous voulez obtenir la possibilité d'ouvrir les fichiers docx.", Toast.LENGTH_LONG).show();
							} else if(loadCurrentLanguage().equals("uk")){
								Toast.makeText(getApplicationContext(), "Вам потрібно активувати RullReader якщо ви хочете отримати можливість відкрити DOCX файли.", Toast.LENGTH_LONG).show();
							} else if(loadCurrentLanguage().equals("ru")){
								Toast.makeText(getApplicationContext(), "Активируйте FullReader чтобы получить возможность открывать docx файлы.", Toast.LENGTH_LONG).show();
							} else {
								if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
									Toast.makeText(getApplicationContext(), "Активируйте FullReader чтобы получить возможность открывать docx файлы.", Toast.LENGTH_LONG).show();
								} else if(Locale.getDefault().getDisplayLanguage().equals("українська")){
									Toast.makeText(getApplicationContext(), "Вам потрібно активувати RullReader якщо ви хочете отримати можливість відкрити DOCX файли.", Toast.LENGTH_LONG).show();
								} else {
									Toast.makeText(getApplicationContext(), "You need activate RullReader if you want get possibility to open docx files.", Toast.LENGTH_LONG).show();
								}
							}*/
							
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
						
						/*Log.d("checkAccessDocX: ", String.valueOf(checkAccessToDocxPlugin()));
						if(checkAccessToDocxPlugin() == true) {
							Log.d("dock plugin installed: ", String.valueOf(getDocxPlugin()));
							if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
								if(getDocxPlugin()) {
									 Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("org.plutext.DocxToHtml");
			 						 Log.d("pathToDOCXfile", tree.getTreeTitle());
			 						 LaunchIntent.putExtra("pathToFile", tree.getTreeTitle());
			 						 startActivity(LaunchIntent);
								} else {
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
						}
					}*/
					/*if() {
						newIntent.setClass(this, MainComicActivity.class);
						newIntent.putExtra("path", tree.getFileName());
						startActivity(newIntent);
					}*/
					//Intent newIntent = new Intent(Intent.ACTION_VIEW);
					//String mimeType = myMime.getMimeTypeFromExtension(fileExt(tree.getFileName().toString()).substring(1));
					
					
					//newIntent.setDataAndType(Uri.fromFile(new File(tree.getFileName())),"");
					//newIntent.setFlags(newIntent.FLAG_ACTIVITY_NEW_TASK);
					//startActivity(newIntent);
					}
					
				} else{
					final Book book = tree.getBook();
					if (book != null) {
						showBookInfo(book);
					} else {
						currentTree = tree;
						openTree(tree);						
					}
				}
		}catch(IndexOutOfBoundsException e){
			return;
		}
	}
	
	private boolean checkDocxInstalledOnDevice() {
		boolean docxExist = false;
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List pkgAppsList = getApplicationContext().getPackageManager().queryIntentActivities( mainIntent, 0);
		for(int i=0;i<pkgAppsList.size();i++) {
			Log.d("package: ", pkgAppsList.get(i).toString());
			/*if(pkgAppsList.get(i).toString().contains("org.plutext.DocxToHtml.AndroidDocxToHtmlActivity")) {
				docxExist = true;
			}*/
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
	
	private boolean checkAccessToDocxPlugin() {
		boolean access = false;
		try {
			File root = new File(Environment.getExternalStorageDirectory(), "FullReader Unlocker");
			File accessFile = new File(root, "access-file.txt");
			if(!accessFile.exists()) {
				if(loadCurrentLanguage().equals("en")){
					Toast.makeText(getApplicationContext(), "For reading .docx files you need to buy FR Unlocker.", Toast.LENGTH_LONG).show();
	    		} else if(loadCurrentLanguage().equals("de")){
	    			Toast.makeText(getApplicationContext(), "Um anzuzeigen .docx-Dateien benötigen Sie den FR Unlocker kaufen.", Toast.LENGTH_LONG).show();
	    		} else if(loadCurrentLanguage().equals("fr")){
	    			Toast.makeText(getApplicationContext(), "Pour voir Fichiers docx vous devez acheter FR Unlocker.", Toast.LENGTH_LONG).show();
	    		} else if(loadCurrentLanguage().equals("uk")){
	    			Toast.makeText(getApplicationContext(), "Щоб переглядати .docx файли Вам потрібно придбати FR Unlocker.", Toast.LENGTH_LONG).show();
	    		} else if(loadCurrentLanguage().equals("ru")){
	    			Toast.makeText(getApplicationContext(), "Чтобы просматривать .docx файлы Вам нужно приобрести FR Unlocker.", Toast.LENGTH_LONG).show();
	    		} else {
	    			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
	    				Toast.makeText(getApplicationContext(), "Чтобы просматривать .docx файлы Вам нужно приобрести FR Unlocker.", Toast.LENGTH_LONG).show();
	    			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
	    				Toast.makeText(getApplicationContext(), "Щоб переглядати .docx файли Вам потрібно придбати FR Unlocker.", Toast.LENGTH_LONG).show();
	    			} else {
	    				Toast.makeText(getApplicationContext(), "For reading .docx files you need to buy FR Unlocker.", Toast.LENGTH_LONG).show();
	    			}
	    		}
				access = false;
			} else {
				FileInputStream fin = new FileInputStream(accessFile);
				String fileString;
				fileString = convertStreamToString(fin);
			    //Make sure you close all streams.
			    fin.close(); 
			    String [] tmpStr = fileString.split("\\|");
			    String [] docxStr = tmpStr[0].split("\\:");
			    String [] adsStr = tmpStr[1].split("\\:");
			    Log.d("docx access: ", String.valueOf(Integer.parseInt(docxStr[1])));
			    if(Integer.parseInt(docxStr[1]) == 1) {
			    	access = true;
			    }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return access;
	}
	
	private boolean getDocxPlugin() {
		 SharedPreferences sPref = getSharedPreferences("docxPrefs", MODE_PRIVATE);
		    return sPref.getBoolean("isDocxInstalled", false);
	}
	
	//
	// show BookInfoActivity
	//
	private static final int BOOK_INFO_REQUEST = 1;

	private void showBookInfo(Book book) {
		OrientationUtil.startActivityForResult(
			this,
			new Intent(getApplicationContext(), BookInfoActivity.class)
				.putExtra(FullReaderActivity.BOOK_KEY, SerializerUtil.serialize(book)), BOOK_INFO_REQUEST);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int returnCode, Intent intent) {
		if (requestCode == BOOK_INFO_REQUEST) {
			final Book book = BookInfoActivity.bookByIntent(intent);
			((BookCollectionShadow)myRootTree.Collection).bindToService(this, new Runnable() {
				public void run() {
					if (book != null) {
						myRootTree.Collection.saveBook(book, true);
						if (getCurrentTree().onBookEvent(BookEvent.Updated, book)) {
							getListAdapter().replaceAll(getCurrentTree().subTrees(), true);
							//getListView().invalidateViews();
						}
					}
				}
			});
		} else {
			super.onActivityResult(requestCode, returnCode, intent);
		}
	}
	
	//
	// Search
	//
	private final ZLStringOption BookSearchPatternOption =
			new ZLStringOption("BookSearch", "Pattern", "");
	
	private void openSearchResults() {
		final LibraryTree tree = myRootTree.getSearchResultsTree();
		if (tree != null) {
			openTree(tree);
		}
	}
	
	@Override
	public boolean onSearchRequested() {
		startSearch(BookSearchPatternOption.getValue(), true, null, false);
		return true;
	}

	//
	// Context menu
	//
	private static final int OPEN_BOOK_ITEM_ID = 0;
	private static final int SHOW_BOOK_INFO_ITEM_ID = 1;
	private static final int SHARE_BOOK_ITEM_ID = 2;
	private static final int ADD_TO_FAVORITES_ITEM_ID = 3;
	private static final int REMOVE_FROM_FAVORITES_ITEM_ID = 4;
	private static final int DELETE_BOOK_ITEM_ID = 5;
	private static final int RENAME_BOOK_ITEM_ID = 6;
	private static final int COPY_BOOK_ITEM_ID = 7;
	private static final int PASTE_BOOK_ITEM_ID = 8;
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		try {
			final int pos = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			// Проверяем, вдруг этот файл находится в архиве, тогда для него контекстное меню делать не нужно
			LibraryTree tr = (LibraryTree)getListAdapter().getItem(pos);
			if (FileTree.class.isInstance(tr)){
				ZLPhysicalFile zlPhFile =  ((FileTree)tr).getFile().getPhysicalFile();
				if (zlPhFile.isRarArchive()) return;
			}
		
			final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
			final Book book = ((LibraryTree)getListAdapter().getItem(position)).getBook();
			if (book != null) {
				createBookContextMenu(menu, book, tr);
			}
			else{
				final LibraryTree tree = (LibraryTree)getListAdapter().getItem(position);
				if(tree.getFileName().contains(".pdf") || tree.getFileName().contains(".djvu") 
						|| tree.getFileName().contains(".cbz") || tree.getFileName().contains(".docx") 
							|| tree.getFileName().contains(".xps") || tree.getFileName().contains(".odt")|| tree.getFileName().contains(".cbr")
							|| tree.getFileName().contains(".rar")){
					createNonBookContextMenu(menu, tree);
				}
			}
		} catch(IndexOutOfBoundsException e) {
			return;
		}
	}
	
	// -------- Создание контекстного меню для всех остальных типов данных -------
	private void createNonBookContextMenu(ContextMenu menu, LibraryTree tree){
		final ZLResource resource = LibraryTree.resource();
		if (!tree.getFileName().contains(".rar")) menu.add(0, OPEN_BOOK_ITEM_ID, 0, resource.getResource("openBook").getValue());
		menu.add(0, SHOW_BOOK_INFO_ITEM_ID, 0, resource.getResource("showBookInfo").getValue());
		menu.add(0, SHARE_BOOK_ITEM_ID, 0, resource.getResource("shareBook").getValue());
		menu.add(0, RENAME_BOOK_ITEM_ID, 0, resource.getResource("renameBook").getValue());
		if (FileTree.class.isInstance(tree)) menu.add(0, COPY_BOOK_ITEM_ID, 0, resource.getResource("copyBook").getValue());
		menu.add(0, DELETE_BOOK_ITEM_ID, 0, resource.getResource("deleteBook").getValue());
	}
	
	// -------- Создание контекстного меню для файла книги -------
	private void createBookContextMenu(ContextMenu menu, Book book, LibraryTree tree) {
		final ZLResource resource = LibraryTree.resource();
		menu.setHeaderTitle(book.getTitle());
		menu.add(0, OPEN_BOOK_ITEM_ID, 0, resource.getResource("openBook").getValue());
		menu.add(0, SHOW_BOOK_INFO_ITEM_ID, 0, resource.getResource("showBookInfo").getValue());
		if (book.File.getPhysicalFile() != null) {
			menu.add(0, SHARE_BOOK_ITEM_ID, 0, resource.getResource("shareBook").getValue());
		}
		if (myRootTree.Collection.labels(book).contains(Book.FAVORITE_LABEL)) {
			menu.add(0, REMOVE_FROM_FAVORITES_ITEM_ID, 0, resource.getResource("removeFromFavorites").getValue());
		} 
//		else {
//			menu.add(0, ADD_TO_FAVORITES_ITEM_ID, 0, resource.getResource("addToFavorites").getValue());
//		}
		menu.add(0, RENAME_BOOK_ITEM_ID, 0, resource.getResource("renameBook").getValue());
		if (FileTree.class.isInstance(tree)) menu.add(0, COPY_BOOK_ITEM_ID, 0, resource.getResource("copyBook").getValue());
		if (BookUtil.canRemoveBookFile(book)) {
			menu.add(0, DELETE_BOOK_ITEM_ID, 0, resource.getResource("deleteBook").getValue());
		}
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final Book book = ((LibraryTree)getListAdapter().getItem(position)).getBook();
		final LibraryTree tree = (LibraryTree)getListAdapter().getItem(position);
		if (book != null) {
			return onContextItemSelected(item.getItemId(), book, tree, position);
		}
		else{
			if(tree.getFileName().contains(".pdf") || tree.getFileName().contains(".djvu") 
					|| tree.getFileName().contains(".cbz") || tree.getFileName().contains(".docx") 
						|| tree.getFileName().contains(".xps") || tree.getFileName().contains(".odt") || tree.getFileName().contains(".cbr")
						|| tree.getFileName().contains(".rar")){
				return onNonBookContextItemSelected(item.getItemId(), tree, position);
			}
		}
		return super.onContextItemSelected(item);
	}

	// -------- Обработка выбора в контекстном меню для книги -------
	private boolean onContextItemSelected(int itemId, Book book, LibraryTree tree, int position) {
		switch (itemId) {
		case OPEN_BOOK_ITEM_ID:
			try {
				FullReaderActivity.openBookActivity(this, book, null);
			} catch (BookReadingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		case SHOW_BOOK_INFO_ITEM_ID:
			showBookInfo(book);
			return true;
		case SHARE_BOOK_ITEM_ID:
			FBUtil.shareBook(this, book);
			return true;
		case ADD_TO_FAVORITES_ITEM_ID:
			myRootTree.Collection.setLabel(book, Book.FAVORITE_LABEL);
			return true;
		case REMOVE_FROM_FAVORITES_ITEM_ID:
			myRootTree.Collection.removeLabel(book, Book.FAVORITE_LABEL);
			if (getCurrentTree().onBookEvent(BookEvent.Updated, book)) {
				getListAdapter().replaceAll(getCurrentTree().subTrees());
				getListView().invalidateViews();
			}
			return true;
		case COPY_BOOK_ITEM_ID:
			copyFile(new File(book.File.getPath()));
		break;
		case RENAME_BOOK_ITEM_ID:
			showRenameBookDialog(book, tree, position);
		break;
		case DELETE_BOOK_ITEM_ID:
			tryToDeleteBook(book);
			return true;
		}
		return false;
	}

	// ------- Обработка выбора в контекстном меню для других файлов -------
	private boolean onNonBookContextItemSelected(int itemId, LibraryTree tree, int position ) {
		File file;
		ZLFile zlFile;
		switch (itemId) {
			case OPEN_BOOK_ITEM_ID:
				openNonBookFromContextMenu(tree);
			return true;
			case SHOW_BOOK_INFO_ITEM_ID:
				file = new File(tree.getFileName());
				showFileInfoDialog(file);
			return true;
			case SHARE_BOOK_ITEM_ID:
				zlFile = ZLFile.createFileByPath(tree.getFileName());
				shareNonBook(zlFile);
			return true;
			case COPY_BOOK_ITEM_ID:
				copyFile(new File(tree.getFileName()));
			break;
			case RENAME_BOOK_ITEM_ID:
				file = new File(tree.getFileName());
				showRenameFileDialog(file, tree, position);
			break;
			case DELETE_BOOK_ITEM_ID:
				file = new File(tree.getFileName());
				tryToDeleteFile(file, tree, position);
			return true;
		}
		return false;
	}
	
	//
	// Options menu
	//

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		addMenuItem(menu, 0, "pasteFile", R.drawable.ic_menu_paste, true);
		addMenuItem(menu, 1, "localSearch", R.drawable.ic_menu_search, true);
		addMenuItem(menu, 2, "rescan", R.drawable.ic_menu_refresh, true);
		addMenuItem(menu, 3, "Google Plus", googlePlusIcon, true).setTitle("Google+");
		sherlockMenu = menu;
		sherlockMenu.findItem(0).setVisible(false);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(2).setEnabled(myRootTree.Collection.status().IsCompleted);
		return true;
	}

	private MenuItem addMenuItem(Menu menu, int id, String resourceKey, int iconId, boolean isVisible) {
		final String label = LibraryTree.resource().getResource("menu").getResource(resourceKey).getValue();
		final MenuItem item = menu.add(0, id, Menu.NONE, label);
		item.setOnMenuItemClickListener(this);
		item.setIcon(iconId);
		if(isVisible)
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		else
			item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

		return item;
	}
	
	public String loadCurrentLanguage() {
	    SharedPreferences sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
	    return sPref.getString("curLanguage", "");
    }

	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			if (copiedToClipboard)paste();
		break;
		case 1:
			return onSearchRequested();
		case 2:

			switch(getIntent().getIntExtra(OPEN_TREE_KEY, -1)){
			case FILESYSTEM_TREE:
				if (myRootTree.Collection.status().IsCompleted) {
					((BookCollectionShadow)myRootTree.Collection).reset(true);
					//openTree(myRootTree.getSubTree(LibraryTree.ROOT_FILE_TREE));
					openTreeWithoutHistory(myRootTree.getSubTree(LibraryTree.ROOT_FILE_TREE));
				}
				break;
			default:
				if (myRootTree.Collection.status().IsCompleted) {
					((BookCollectionShadow)myRootTree.Collection).reset(true);
					openTree(myRootTree);
				}
				break;
			}
			return true;
		case 3:
			try {
				Intent shareIntent = new PlusShare.Builder(LibraryActivity.this)
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
			break;
		default:
			return true;
		}
		return true;
	}

	//
	// Book deletion
	//
	private class BookDeleter implements DialogInterface.OnClickListener {
		private final Book myBook;

		BookDeleter(Book book) {
			myBook = book;
		}

		public void onClick(DialogInterface dialog, int which) {
			if (getCurrentTree() instanceof FileTree) {
				getListAdapter().remove(new FileTree((FileTree)getCurrentTree(), myBook.File));
				getListView().invalidateViews();
			} else if (getCurrentTree().onBookEvent(BookEvent.Removed, myBook)) {
				getListAdapter().replaceAll(getCurrentTree().subTrees());
				getListView().invalidateViews();
			}

			myRootTree.Collection.removeBook(myBook, true);
			getListView().refreshDrawableState();
		}
	}

	private void tryToDeleteBook(Book book) {
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource.getResource("deleteBookBox");
		new AlertDialog.Builder(this)
		.setTitle(book.getTitle())
		.setMessage(boxResource.getResource("message").getValue())
		.setIcon(0)
		.setPositiveButton(buttonResource.getResource("yes").getValue(), new BookDeleter(book))
		.setNegativeButton(buttonResource.getResource("no").getValue(), null)
		.create().show();
	}

	private void startBookSearch(final String pattern) {
		BookSearchPatternOption.setValue(pattern);

		final Thread searcher = new Thread("Library.searchBooks") {
			public void run() {
				final SearchResultsTree oldSearchResults = myRootTree.getSearchResultsTree();

				if (oldSearchResults != null && pattern.equals(oldSearchResults.Pattern)) {
					onSearchEvent(true);
				} else if (myRootTree.Collection.hasBooksForPattern(pattern)) {
					if (oldSearchResults != null) {
						oldSearchResults.removeSelf();
					}
					myRootTree.createSearchResultsTree(pattern);
					onSearchEvent(true);
				} else {
					onSearchEvent(false);
				}
			}
		};
		searcher.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
		searcher.start();
	}

	private void onSearchEvent(final boolean found) {
		runOnUiThread(new Runnable() {
			public void run() {
				if (found) {
					openSearchResults();
				} else {
					UIUtil.showErrorMessage(LibraryActivity.this, "bookNotFound");
				}
			}
		});
	}

	public void onBookEvent(BookEvent event, Book book) {
		if (getCurrentTree().onBookEvent(event, book)) {
			getListAdapter().replaceAll(getCurrentTree().subTrees());
			getListView().invalidateViews();
		}
	}

	public void onBuildEvent(IBookCollection.Status status) {
		setProgressBarIndeterminateVisibility(!status.IsCompleted);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(IConstants.THEME_PREF)){
			recreatethis();
			/*int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
			int res = 0;
			switch(theme){
			case IConstants.THEME_MYBLACK:
				setContentView(R.layout.library_body_theme_black);
				res = R.layout.library_shelf_theme_black;
				actionBarBackground = getResources().getDrawable( com.fullreader.R.drawable.theme_black_action_bar );
				//Toast.makeText(getApplicationContext(), "THEME_BLACK", Toast.LENGTH_LONG).show();
				googlePlusIcon = R.drawable.google_plus_marble;
				break;
			case IConstants.THEME_LAMINAT:
				setContentView(R.layout.library_body_theme_laminat);
				res = R.layout.library_shelf_theme_laminat;
				googlePlusIcon = R.drawable.google_plus_laminat;
				actionBarBackground = getResources().getDrawable( com.fullreader.R.drawable.theme_laminat_action_bar );
				//Toast.makeText(getApplicationContext(), "THEME_LAMINAT", Toast.LENGTH_LONG).show();
				break;
			case IConstants.THEME_REDTREE:
				setContentView(R.layout.library_body_theme_redtree);
				res = R.layout.library_shelf_theme_redtree;
				googlePlusIcon = R.drawable.google_plus_red_tree;
				actionBarBackground = getResources().getDrawable( com.fullreader.R.drawable.theme_redtree_action_bar );
				//Toast.makeText(getApplicationContext(), "THEME_REDTREE", Toast.LENGTH_LONG).show();
				break;
			}
			bar.setBackgroundDrawable(actionBarBackground);*/
		}     
	}
	
	
	public void recreatethis() {
		if (android.os.Build.VERSION.SDK_INT >= 11){
			super.recreate();
		} else {
			startActivity(getIntent());
			finish();
		}
	}    
	
	/*@Override
	public void onBackPressed() {
		super.onBackPressed();
		//finish();
	}*/
	
//	public boolean onMenuItemSelected(int featureId, MenuItem item) {
//
//		int itemId = item.getItemId();
//		switch (itemId) {
//		case android.R.id.home:
////			startActivity(new Intent(this, StartScreenActivity.class));
////			finish();
//        	onBackPressed();
////        	onKeyDown(KeyEvent.KEYCODE_BACK, null);
//
//			break;
//		}
//		return true;
//	}
	
	// ------- Метод, который открывает диалоговое окно для переименования книги (для книг, которые читает FBReader) -------
	private void showRenameBookDialog(final Book book, final LibraryTree tree, final int position){
		final ZLResource resource = LibraryTree.resource();
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.rename_book_dialog);
		dialog.setTitle(book.getTitle());

		bookNameEdit = (EditText)dialog.findViewById(R.id.book_name);
		String splittedName = splitBookName(book.File.getPhysicalFile().getShortName(),book.File.getExtension() );
		bookNameEdit.setText(splittedName);
		
		renameOk = (Button) dialog.findViewById(R.id.rename_ok);
		renameOk.setText(resource.getResource("renameOk").getValue());
		
		renameCancel = (Button) dialog.findViewById(R.id.rename_cancel);
		renameCancel.setText(resource.getResource("renameCancel").getValue());
		
		renameOk.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				String name = bookNameEdit.getText().toString().trim();
				if (name.length() == 0){
					Toast.makeText(LibraryActivity.this, resource.getResource("renameTitle").getValue(), Toast.LENGTH_SHORT).show();
					return;
				}
				else{
					boolean res = renameBook(book, name, tree, position);
					if (res){
						Toast.makeText(LibraryActivity.this, resource.getResource("renameSuccess").getValue(), Toast.LENGTH_SHORT).show();
					}
					else{
						Toast.makeText(LibraryActivity.this, resource.getResource("renameError").getValue(), Toast.LENGTH_SHORT).show();
					}
					dialog.dismiss();
				}

			}
		});
		
		renameCancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				dialog.dismiss();
			}
		});
		
		dialog.show();
	}
	
	
	private boolean renameBook(Book book, String newName, LibraryTree tree, int position){
		File oldFile = new File(book.File.getPhysicalFile().getPath());
		String newPath = oldFile.getParentFile().getPath();
		newPath+="/" + newName;
		String oldLocation = oldFile.getPath();
		int extIndex = oldFile.getPath().lastIndexOf(book.File.getExtension());
		String extension = oldFile.getPath().substring(extIndex, oldFile.getPath().length());
		
		newPath+="."+extension;
		File newFile = new File(newPath);

		if (oldFile.renameTo(newFile)){
			
			ZLFile file = ZLFile.createFileByPath(newPath);
			FileTree fTree = new FileTree((FileTree)getCurrentTree(), file);
			
			getListAdapter().remove(tree);
			getListAdapter().add(position, fTree);
			getListAdapter().notifyDataSetChanged();
			getListView().refreshDrawableState();
			
			DatabaseHandler dbHandler = new DatabaseHandler(ReaderApplication.getContext());
			dbHandler.deleteFrDocumentAfterRename(oldLocation);
			return true;
		}
		else{
			return false;
		}
	}
	
	private String splitBookName(String name, String extension){
		int extIndex = name.lastIndexOf(extension);
		String splitName = name.substring(0, extIndex-1);
		return splitName;
	}
	
	private void openNonBookFromContextMenu(LibraryTree tree){
		Intent newIntent = new Intent();
		if(tree.getFileName().contains(".pdf") || tree.getFileName().contains(".xps") || tree.getFileName().contains(".cbz")) {
			Uri uri = Uri.parse(tree.getFileName());
			newIntent.setClass(this, MuPDFActivity.class);
			newIntent.setAction(Intent.ACTION_VIEW);
			newIntent.setData(uri);
			newIntent.setDataAndType(Uri.fromFile(new File(tree.getFileName())),"");
			startActivity(newIntent);
		}
		else
		if(tree.getFileName().contains(".djvu")) {
			newIntent.setClass(this, DjvuViewerActivity.class);	
			newIntent.setDataAndType(Uri.fromFile(new File(tree.getFileName())),"");
			newIntent.setFlags(newIntent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(newIntent);
		}
		if(tree.getFileName().contains(".docx")  || tree.getFileName().contains(".odt")) {
			if(checkDocxInstalledOnDevice()) {
				if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
					Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("org.plutext.DocxToHtml");
						 Log.d("pathToDOCXfile", tree.getTreeTitle());
						 LaunchIntent.putExtra("pathToFile", tree.getTreeTitle());
						 startActivity(LaunchIntent);
				}
			} else {
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
	}
	
	// ------ Поделиться файлом, который не является книгой -------
	private void shareNonBook(ZLFile file){
		try {
			if (file == null) {
				// That should be impossible
				return;
			}
			final CharSequence sharedFrom =	Html.fromHtml(ZLResource.resource("sharing").getResource("sharedFrom").getValue());
			this.startActivity(
				new Intent(Intent.ACTION_SEND)
					.setType(FileTypeCollection.Instance.simplifiedMimeType(file).Name)
					.putExtra(Intent.EXTRA_SUBJECT, file.getShortName())
					.putExtra(Intent.EXTRA_TEXT, sharedFrom)
					.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file.getPhysicalFile().javaFile()))
			);
		} catch (ActivityNotFoundException e) {
			// TODO: show toast
		}
	}
	
	// ------- Показать диалоговое окно с информацией о файле, который не является FBook -------
	private void showFileInfoDialog(File file){
		final ZLResource resource = LibraryTree.resource();
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.file_info_dialog);
		dialog.setTitle(resource.getResource("fileInfo").getValue());
		mFileInfoMain = (LinearLayout) dialog.findViewById(R.id.file_info_main);
		LayoutInflater lInflater = getLayoutInflater();
		
		TextView fileLabel;
		TextView fileInfoData;
		// Название файла
        View view = lInflater.inflate(R.layout.file_info_item, null, false);
        fileLabel = (TextView) view.findViewById(R.id.file_info_label);
        fileInfoData = (TextView) view.findViewById(R.id.file_info_data);
        
        fileLabel.setText(resource.getResource("fileName").getValue());
        fileInfoData.setText(file.getName());
        mFileInfoMain.addView(view);
        
        // Расположение файла
        view = lInflater.inflate(R.layout.file_info_item, null, false);
        fileLabel = (TextView) view.findViewById(R.id.file_info_label);
        fileInfoData = (TextView) view.findViewById(R.id.file_info_data);
        
        fileLabel.setText(resource.getResource("fileLocation").getValue());
        fileInfoData.setText(file.getPath());
        mFileInfoMain.addView(view);
        
        // Размер файла
        view = lInflater.inflate(R.layout.file_info_item, null, false);
        fileLabel = (TextView) view.findViewById(R.id.file_info_label);
        fileInfoData = (TextView) view.findViewById(R.id.file_info_data);
        
        fileLabel.setText(resource.getResource("fileSize").getValue());
        String fileSize = readableFileSize(file.length());
        fileInfoData.setText(fileSize);
        mFileInfoMain.addView(view);
        
        dialog.setCancelable(true);
        dialog.show();
        
	}
	
	// ------- Перевод длины файла в байтах в строку -------
	public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
	
	
	// ------- Показать диалоговое окно для переименования файла (который не читает FBReader --------
	private void showRenameFileDialog(final File file, final LibraryTree tree, final int position){
		final ZLResource resource = LibraryTree.resource();
		final Dialog dialog = new Dialog(this);
		dialog.setContentView(R.layout.rename_book_dialog);
		dialog.setTitle(file.getName());

		bookNameEdit = (EditText)dialog.findViewById(R.id.book_name);
		String splittedName = parseFileName(file);
		
		bookNameEdit.setText(splittedName);
		
		renameOk = (Button) dialog.findViewById(R.id.rename_ok);
		renameOk.setText(resource.getResource("renameOk").getValue());
		
		renameCancel = (Button) dialog.findViewById(R.id.rename_cancel);
		renameCancel.setText(resource.getResource("renameCancel").getValue());
		
		renameOk.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				String name = bookNameEdit.getText().toString().trim();
				if (name.length() == 0){
					Toast.makeText(LibraryActivity.this, resource.getResource("renameTitle").getValue(), Toast.LENGTH_SHORT).show();
					return;
				}
				else{
					boolean res = renameFile(file, name, tree, position);
					if (res){
						Toast.makeText(LibraryActivity.this, resource.getResource("renameSuccess").getValue(), Toast.LENGTH_SHORT).show();
					}
					else{
						Toast.makeText(LibraryActivity.this, resource.getResource("renameError").getValue(), Toast.LENGTH_SHORT).show();
					}
					dialog.dismiss();
				}

			}
		});
		
		renameCancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v){
				dialog.dismiss();
			}
		});
		
		dialog.show();
	}
	
	// ------- Метод, который парсит название файла и возвращает строку без разрешения -------
	private String parseFileName(File file){
		int dotIndex = file.getName().lastIndexOf(".");
		return file.getName().substring(0, dotIndex);
	}
	
	// ------- Метод, который переименовывает файл -------
	private boolean renameFile(File oldFile, String newName, LibraryTree tree, int position){
		String oldName = oldFile.getName();
		int dotIndex = oldName.lastIndexOf(".");
		String extension = oldName.substring(dotIndex, oldName.length());
		newName = oldFile.getParent()+ "/" + newName + extension;
		
		File newFile = new File(newName);
		if (oldFile.renameTo(newFile)){
			
			ZLFile file = ZLFile.createFileByPath(newFile.getPath());
			FileTree fTree = new FileTree((FileTree)getCurrentTree(), file);
			
			getListAdapter().remove(tree);
			getListAdapter().add(position, fTree);
			getListAdapter().notifyDataSetChanged();
			getListView().refreshDrawableState();
			return true;
		}
		else return false;
	}
	
	
	// ------ Метод, котоырй показывает диалоговое окно для подтверждения удаления файла -------
	private void tryToDeleteFile(File file, LibraryTree tree, int position) {
		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource libraryResource = ZLResource.resource("library");
		final ZLResource buttonResource = dialogResource.getResource("button");
		final ZLResource boxResource = dialogResource.getResource("deleteBookBox");
		new AlertDialog.Builder(this)
		.setTitle(libraryResource.getResource("deleteFileTitle").getValue())
		.setMessage(boxResource.getResource("message").getValue())
		.setIcon(0)
		.setPositiveButton(buttonResource.getResource("yes").getValue(), new FileDeleter(file, tree))
		.setNegativeButton(buttonResource.getResource("no").getValue(), null)
		.create().show();
	}
	
	// ------ Обработчик нажатия на кнопку удаления файла -------
	private class FileDeleter implements DialogInterface.OnClickListener {
		private final File myFile;
		LibraryTree myTree;
		FileDeleter(File file, LibraryTree tree) {
			myFile = file;
			myTree = tree;
		}

		public void onClick(DialogInterface dialog, int which) {
			if (myFile.delete()){
				getListAdapter().remove(myTree);
				getListAdapter().notifyDataSetChanged();
				getListView().refreshDrawableState();
			}
		}
	}

	// ------- Инициализируем файл, который нужно будет скопировать -------
	private void copyFile(File file){
		sourceFile = file;
		copiedToClipboard = true;
		sherlockMenu.findItem(0).setVisible(true);
	}
	
	// ------- Вставка файла -------
	private void paste(){
		File targetFile = new File(currentTree.getFileName()+"/"+sourceFile.getName());
		if (targetFile.getParentFile().isHidden() || targetFile.getParentFile().listFiles() == null)return;
		if (targetFile.getPath().equals(sourceFile.getPath()) || containsFile(targetFile)){
			copiedToClipboard = false;
			sherlockMenu.findItem(0).setVisible(false);
			return;
		}
		ZLResource libraryResource = ZLResource.resource("library");
		FileCopier copier = new FileCopier(libraryResource, targetFile, this);
		copier.execute();
	}
	
	private boolean containsFile(File targetFile){
		File parent = targetFile.getParentFile();
		String name = targetFile.getName();
		for (File file : parent.listFiles()){
			if (file.isDirectory())	continue;
			if (file.getName().equals(name)) return true;
		}
		return false;
	}
	
	@Override
	public void hideMenuIcon() {
		if (copiedToClipboard)	sherlockMenu.findItem(0).setVisible(false);
		
	}
	
	@Override
	public void showMenuIcon(){
		if (copiedToClipboard)	sherlockMenu.findItem(0).setVisible(true);
	}
	
	// ------- Класс, который в асинхронном порядке копирует файл из одной папки в другую -------
	private class FileCopier extends AsyncTask<Void, Void, Boolean>{
		private ZLResource mResource;
		private ProgressDialog mDialog;
		private File targetFile;
		private Context mContext;
		FileCopier(ZLResource resource, File file, Context context){
			mResource = resource;
			targetFile = file;
			mContext = context;
		}
		
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			mDialog = new ProgressDialog(mContext);
			mDialog.setMessage(mResource.getResource("fileCopying").getValue());
			mDialog.setCancelable(false);
			mDialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... params){
			return pasteFile(targetFile);
			
		}
		
		protected void onPostExecute(Boolean result){
			super.onPostExecute(result);
			mDialog.hide();
			if (result){
				Toast.makeText(mContext, mResource.getResource("copyFileSuccess").getValue(), Toast.LENGTH_SHORT).show();
				ZLFile file = ZLFile.createFileByPath(targetFile.getPath());
				FileTree fTree = new FileTree((FileTree)getCurrentTree(), file);
				getListAdapter().add(fTree);
				getListAdapter().notifyDataSetChanged();
				getListView().refreshDrawableState();
				sherlockMenu.findItem(0).setVisible(false);
				copiedToClipboard = false;
			}
			else{
				Toast.makeText(mContext, mResource.getResource("copyFileError").getValue(), Toast.LENGTH_SHORT).show();
			}
		}
		
		
		private boolean pasteFile(File targetFile){
	        try{
	        	InputStream in = new FileInputStream(sourceFile);
		        OutputStream out = new FileOutputStream(targetFile);
		        byte[] buf = new byte[1024];
			    int len;
		        while ((len = in.read(buf)) > 0) {
		            out.write(buf, 0, len);
		        }
		        in.close();
		        in = null;
		        
		        out.flush();
		        out.close();
		        out = null;
		        return true;
	        }
	        catch (Exception e){
	        	return false;
	        }
		}
	}

	// ------- Handler, который будет отлвливать сообщения насчет разархивирования rar файлов -------
	private Handler mUnrarHandler = new Handler(){
		public void handleMessage(Message msg) {
			String newFile = "";
	        switch (msg.what) {
	        	case RarEntries.FILE_UNRAR_MSG:
	        		if (msg.arg1 == RarEntries.FILE_UNRAR_SUCCES){
	        			newFile = (String)msg.obj;
	        			openFileFromRarArchive(newFile);
	        		}
	        		else
	        		if (msg.arg1 == RarEntries.FILE_UNRAR_ERROR){
	        			newFile = (String)msg.obj;
	        			Toast.makeText(LibraryActivity.this, getFileUnrarErrorMsg(), Toast.LENGTH_SHORT).show();
	        			File file = new File(newFile);
	        			if (file.exists()) file.delete();
	        		}
	        	break;
	        }
		}
	};
	
	
	private String getFileUnrarErrorMsg(){
		String msg = "";
        if(loadCurrentLanguage().equals("en")){
        	msg = "The file is damaged";
		} else if(loadCurrentLanguage().equals("de")){
			msg = "Die Datei ist beschädigt";
		} else if(loadCurrentLanguage().equals("fr")){
			msg = "Le fichier est endommagé";
		} else if(loadCurrentLanguage().equals("uk")){
			msg = "Файл пошкоджено";
			
		} else if(loadCurrentLanguage().equals("ru")){
			msg = "Файл поврежден";
		} else {
			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
				msg = "Файл поврежден";
			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")){
				msg = "Файл поврежден";
			} else {
				msg = "The file is damaged";
			}
		}
    return msg;
	}
	
	
	// ------- Открытие файла из rar архива -------
	private void openFileFromRarArchive(String path){
		FileTree fTree = (FileTree)rarTree;
		fTree = fTree.getParent().getParent();
		fTree.setupAsRarArchive();
		fTree.addFileFromArchive(new File(path));
		Intent newIntent = new Intent();
		if(path.contains(".pdf") || path.contains(".djvu") || path.contains(".cbz") || path.contains(".docx") 
				|| path.contains(".xps") || path.contains(".odt") || path.contains("cbr")){
				
			if(path.contains(".pdf") || path.contains(".xps") || path.contains(".cbz")) {
				Uri uri = Uri.parse(path);
				newIntent.setClass(this, MuPDFActivity.class);
				newIntent.setAction(Intent.ACTION_VIEW);
    			newIntent.setData(uri);
    			newIntent.setDataAndType(Uri.fromFile(new File(path)),"");
				startActivity(newIntent);
			}
			if(path.contains(".odt")) {
				Uri uri = Uri.parse("file://" + path);
				newIntent.setClass(this, at.tomtasche.reader.ui.activity.MainActivity.class);
				newIntent.setAction(Intent.ACTION_VIEW);
    			newIntent.setData(uri);
    			startActivity(newIntent);
			}
			if(path.contains(".cbr")) {
				Uri uri = Uri.parse(path);
				newIntent.setClass(this, ComicViewerActivity.class);
				newIntent.setAction(Intent.ACTION_VIEW);
    			newIntent.setData(uri);
    			startActivity(newIntent);
			}
			if(path.contains(".djvu")) {
				newIntent.setClass(this, DjvuViewerActivity.class);	
				newIntent.setDataAndType(Uri.fromFile(new File(path)),"");
				newIntent.setFlags(newIntent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(newIntent);
			}
			if(path.contains(".docx")) {
				if(checkDocxInstalledOnDevice()) {
					if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
						Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage("org.plutext.DocxToHtml");
 						LaunchIntent.putExtra("pathToFile", path);
 						startActivity(LaunchIntent);
					}
				} else {
				
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
			                         dialog.dismiss(); 
			                         
		                        }
		                    });
			            builder.setNegativeButton(noText,
			                    new DialogInterface.OnClickListener() {
			                        @Override
			                        public void onClick(DialogInterface dialog,
			                                int which) {
			                            dialog.dismiss();
			                        }
			                    });
				        AlertDialog alert = builder.create();
				        alert.getWindow().setLayout(600, 400);
				        alert.show();
				}
			}
			
		} else{
			// Открываем файл книги
			// и указываем, что она открывается из архива
			Uri uri = Uri.parse(path);
			startActivity(
					new Intent(this, FullReaderActivity.class)
					.setAction(Intent.ACTION_VIEW).setData(uri).putExtra(FullReaderActivity.FROM_RAR_ARCHIVE, true)
					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
					);
		}
	}
	
	public static void setCurrentTree(LibraryTree tree){
		currentTree = tree;
	}
}
