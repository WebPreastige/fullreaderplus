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

package org.geometerplus.android.fbreader.network;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.android.fbreader.network.action.Action;
import org.geometerplus.android.fbreader.network.action.ActionCode;
import org.geometerplus.android.fbreader.network.action.AddCustomCatalogAction;
import org.geometerplus.android.fbreader.network.action.BuyBasketBooksAction;
import org.geometerplus.android.fbreader.network.action.ClearBasketAction;
import org.geometerplus.android.fbreader.network.action.EditCustomCatalogAction;
import org.geometerplus.android.fbreader.network.action.LanguageFilterAction;
import org.geometerplus.android.fbreader.network.action.NetworkBookActions;
import org.geometerplus.android.fbreader.network.action.OpenCatalogAction;
import org.geometerplus.android.fbreader.network.action.OpenInBrowserAction;
import org.geometerplus.android.fbreader.network.action.RefreshRootCatalogAction;
import org.geometerplus.android.fbreader.network.action.ReloadCatalogAction;
import org.geometerplus.android.fbreader.network.action.RemoveCustomCatalogAction;
import org.geometerplus.android.fbreader.network.action.RunSearchAction;
import org.geometerplus.android.fbreader.network.action.ShowBookInfoAction;
import org.geometerplus.android.fbreader.network.action.SignInAction;
import org.geometerplus.android.fbreader.network.action.SignOutAction;
import org.geometerplus.android.fbreader.network.action.SignUpAction;
import org.geometerplus.android.fbreader.network.action.TopupAction;
import org.geometerplus.android.fbreader.tree.TreeActivity;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.tree.NetworkAuthorTree;
import org.geometerplus.fbreader.network.tree.NetworkBookTree;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.tree.NetworkItemsLoader;
import org.geometerplus.fbreader.network.tree.NetworkSeriesTree;
import org.geometerplus.fbreader.network.tree.RootTree;
import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLBoolean3;
import org.geometerplus.zlibrary.ui.android.network.SQLiteCookieDatabase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import com.fullreader.R;

public abstract class NetworkLibraryActivity extends TreeActivity<NetworkTree> implements ListView.OnScrollListener, NetworkLibrary.ChangeListener{
	public static final String OPEN_CATALOG_ACTION = "android.reader.action.OPEN_NETWORK_CATALOG";

	public static final String START_REQUEST = "startreq";
	public static final int SEARCH_REQ = 0;
	public static final int LITRES_REQ = 0;
	private int theme = 0;
	final BookDownloaderServiceConnection Connection = new BookDownloaderServiceConnection();
	
	final List<Action> myOptionsMenuActions = new ArrayList<Action>();
	final List<Action> myContextMenuActions = new ArrayList<Action>();
	final List<Action> myListClickActions = new ArrayList<Action>();
	private Intent myDeferredIntent;
	private boolean mySingleCatalog;
	
	@Override
	public void onCreate(Bundle icicle) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);  
		super.onCreate(icicle);
		
		ActionBar bar = getSupportActionBar();
        Drawable actionBarBackground = null;  
        theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
       String myTheme = getIntent().getExtras().getString("curTheme");
       if(myTheme.equals("black")) {
        	setTheme(R.style.Theme_myBlack);
        	getWindow().setBackgroundDrawableResource(R.drawable.theme_black_shelf);
        	//Toast.makeText(getApplicationContext(), "THEME_BLACK", Toast.LENGTH_LONG).show();
       } else if(myTheme.equals("laminat")){
            setTheme(R.style.Theme_Laminat);
            getWindow().setBackgroundDrawableResource(R.drawable.theme_laminat_shelf);
           // Toast.makeText(getApplicationContext(), "THEME_LAMINAT", Toast.LENGTH_LONG).show();
       } else {
    	    setTheme(R.style.Theme_Redtree);
	        getWindow().setBackgroundDrawableResource(R.drawable.theme_redtree_shelf);
	       // Toast.makeText(getApplicationContext(), "THEME_REDTREE", Toast.LENGTH_LONG).show();
       }
       /*switch(theme){
       case IConstants.THEME_MYBLACK:
	       	setTheme(R.style.Theme_myBlack);
		    actionBarBackground = getResources().getDrawable( com.fullreader.R.drawable.theme_black_action_bar );
	       	getWindow().setBackgroundDrawableResource(R.drawable.theme_black_shelf);
       case IConstants.THEME_LAMINAT:
           setTheme(R.style.Theme_Laminat);
           getWindow().setBackgroundDrawableResource(R.drawable.theme_laminat_shelf);
       	break;
       case IConstants.THEME_REDTREE:
       	setTheme(R.style.Theme_Redtree);
       	getWindow().setBackgroundDrawableResource(R.drawable.theme_redtree_shelf);
       	break;
       }*/
        bar.setBackgroundDrawable(actionBarBackground);
        
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		SQLiteCookieDatabase.init(this);
		
		setListAdapter(new NetworkLibraryAdapter(this));
		final Intent intent = getIntent();
		init(intent);
		
		myDeferredIntent = null;		
		
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
		
		if (getCurrentTree() instanceof RootTree) {
			mySingleCatalog = intent.getBooleanExtra("SingleCatalog", false);
			if (!NetworkLibrary.Instance().isInitialized()) {
				Util.initLibrary(this);					
				myDeferredIntent = intent;
			} else {
				NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
				openTreeByIntent(intent);
			}
		}
		
		getListView().setOnScrollListener(this);
		
		switch(intent.getIntExtra(START_REQUEST, -1)){
		case SEARCH_REQ:
			onSearchRequested();
			break;
		}	
	}

	@Override
	protected NetworkTree getTreeByKey(FBTree.Key key) {
		final NetworkLibrary library = NetworkLibrary.Instance();
		final NetworkTree tree = library.getTreeByKey(key);
		return tree != null ? tree : library.getRootTree();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Connection.bindToService(this, null);
		NetworkLibrary.Instance().addChangeListener(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		getListView().setOnCreateContextMenuListener(this);
		NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
	}
	
	@Override
	protected void onStop() {
		NetworkLibrary.Instance().removeChangeListener(this);		
		Connection.unbind(this);
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	
	private boolean openTreeByIntent(Intent intent) {
		if (OPEN_CATALOG_ACTION.equals(intent.getAction())) {
			final Uri uri = intent.getData();
			if (uri != null) {
				final NetworkTree tree =
					NetworkLibrary.Instance().getCatalogTreeByUrl(uri.toString());
				if (tree != null) {
					checkAndRun(new OpenCatalogAction(this), tree);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		if (!openTreeByIntent(intent)) {
			super.onNewIntent(intent);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Connection.bindToService(this, new Runnable() {
			public void run() {
				getListView().invalidateViews();
			}
		});
	}
	
	@Override
	public boolean onSearchRequested() {
		final NetworkTree tree = getCurrentTree();
		final RunSearchAction action = new RunSearchAction(this, false);
		if (action.isVisible(tree) && action.isEnabled(tree)) {
			action.run(tree);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean isTreeSelected(FBTree tree) {
		return false;
	}
	
	@Override
	protected boolean isTreeInvisible(FBTree tree) {
		return tree instanceof RootTree && (mySingleCatalog || ((RootTree)tree).IsFake);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			final NetworkItemsLoader loader =
				NetworkLibrary.Instance().getStoredLoader(getCurrentTree());
			if (loader != null) {
				loader.interrupt();
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void fillOptionsMenuList() {
		myOptionsMenuActions.add(new AddCustomCatalogAction(this));
		myOptionsMenuActions.add(new LanguageFilterAction(this));
	}

	private void fillContextMenuList() {
		myContextMenuActions.add(new OpenCatalogAction(this));
		myContextMenuActions.add(new OpenInBrowserAction(this));
		//myContextMenuActions.add(new RunSearchAction(this, true));
		myContextMenuActions.add(new AddCustomCatalogAction(this));
		myContextMenuActions.add(new EditCustomCatalogAction(this));
		myContextMenuActions.add(new RemoveCustomCatalogAction(this));
	}

	private void fillListClickList() {
		myListClickActions.add(new OpenCatalogAction(this));
		myListClickActions.add(new OpenInBrowserAction(this));
		//myListClickActions.add(new RunSearchAction(this, true));
		myListClickActions.add(new AddCustomCatalogAction(this));
		myListClickActions.add(new ShowBookInfoAction(this));
	}

	private List<? extends Action> getContextMenuActions(NetworkTree tree) {
		return tree instanceof NetworkBookTree
			? NetworkBookActions.getContextMenuActions(this, (NetworkBookTree)tree, Connection)
			: myContextMenuActions;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
		if (myContextMenuActions.isEmpty()) {
			fillContextMenuList();
		}

		final int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
		final NetworkTree tree = (NetworkTree)getListAdapter().getItem(position);
		if (tree != null) {
			menu.setHeaderTitle(tree.getName());
			for (Action a : getContextMenuActions(tree)) {
				if (a.isVisible(tree) && a.isEnabled(tree)) {
					menu.add(0, a.Code, 0, a.getContextLabel(tree));
				}
			}
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		final NetworkTree tree = (NetworkTree)getListAdapter().getItem(position);
		if (tree != null) {
			for (Action a : getContextMenuActions(tree)) {
				if (a.Code == item.getItemId()) {
					checkAndRun(a, tree);
					return true;
				}
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position, long rowId) {
		if (myListClickActions.isEmpty()) {
			fillListClickList();
			//Toast.makeText(getApplicationContext(), "on click lib!", Toast.LENGTH_LONG).show();
		}

		final NetworkTree tree = (NetworkTree)getListAdapter().getItem(position);
		for (Action a : myListClickActions) {
			if (a.isVisible(tree) && a.isEnabled(tree)) {
				checkAndRun(a, tree);
				return;
			}
		}

		listView.showContextMenuForChild(view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		if (myOptionsMenuActions.isEmpty()) {
			fillOptionsMenuList();
		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		final NetworkTree tree = getCurrentTree();
		for (Action a : myOptionsMenuActions) {
			final MenuItem item = menu.findItem(a.Code);
			if (a.isVisible(tree)) {
				item.setVisible(true);
				item.setEnabled(a.isEnabled(tree));
				item.setTitle(a.getOptionsLabel(tree));
			} else {
				item.setVisible(false);
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final NetworkTree tree = getCurrentTree();
		for (Action a : myOptionsMenuActions) {
			if (a.Code == item.getItemId()) {
				checkAndRun(a, tree);
				break;
			}
		}
		return true;
	}

	private void updateLoadingProgress() {
		final NetworkLibrary library = NetworkLibrary.Instance();
		final NetworkTree tree = getCurrentTree();
		final NetworkTree lTree = getLoadableNetworkTree(tree);
		//final NetworkTree sTree = RunSearchAction.getSearchTree(tree);
		getSherlock().setProgressBarIndeterminateVisibility(
			library.isUpdateInProgress() ||
			library.isLoadingInProgress(lTree) 
			//||
			//library.isLoadingInProgress(sTree)
		);
	}

	// method from NetworkLibrary.ChangeListener
	public void onLibraryChanged(final NetworkLibrary.ChangeListener.Code code, final Object[] params) {
		runOnUiThread(new Runnable() {
			public void run() {
				switch (code) {
					default:
						updateLoadingProgress();
						getListAdapter().replaceAll(getCurrentTree().subTrees());
						//getListView().invalidateViews();
						break;
					case InitializationFailed:
						showInitLibraryDialog((String)params[0]);
						break;
					case InitializationFinished:
						NetworkLibrary.Instance().runBackgroundUpdate(false);
						if (myDeferredIntent != null) {
							openTreeByIntent(myDeferredIntent);
							myDeferredIntent = null;
						}
						break;
					case Found:
						openTree((NetworkTree)params[0]);
						break;
					case NotFound:
						UIUtil.showErrorMessage(NetworkLibraryActivity.this, "emptyNetworkSearchResults");
						getListView().invalidateViews();
						break;
					case EmptyCatalog:
						UIUtil.showErrorMessage(NetworkLibraryActivity.this, "emptyCatalog");
						break;
					case NetworkError:
						UIUtil.showMessageText(NetworkLibraryActivity.this, (String)params[0]);
						break;
				}
			}
		});
	}

	private static NetworkTree getLoadableNetworkTree(NetworkTree tree) {
		while (tree instanceof NetworkAuthorTree || tree instanceof NetworkSeriesTree) {
			if (tree.Parent instanceof NetworkTree) {
				tree = (NetworkTree)tree.Parent;
			} else {
				return null;
			}
		}
		return tree;
	}

	@Override
	protected void onCurrentTreeChanged() {
		NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
	}

	private void showInitLibraryDialog(String error) {
		final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					Util.initLibrary(NetworkLibraryActivity.this);
				} else {
					finish();
				}
			}
		};

		final ZLResource dialogResource = ZLResource.resource("dialog");
		final ZLResource boxResource = dialogResource.getResource("networkError");
		final ZLResource buttonResource = dialogResource.getResource("button");
		new AlertDialog.Builder(this)
			.setTitle(boxResource.getResource("title").getValue())
			.setMessage(error)
			.setIcon(0)
			.setPositiveButton(buttonResource.getResource("tryAgain").getValue(), listener)
			.setNegativeButton(buttonResource.getResource("cancel").getValue(), listener)
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					listener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
				}
			})
			.create().show();
	}

	private void checkAndRun(final Action action, final NetworkTree tree) {
		if (tree instanceof NetworkCatalogTree) {
			final NetworkCatalogTree catalogTree = (NetworkCatalogTree)tree;
			switch (catalogTree.getVisibility()) {
				case B3_FALSE:
					break;
				case B3_TRUE:
					action.run(tree);
					break;
				case B3_UNDEFINED:
					Util.runAuthenticationDialog(this, tree.getLink(), new Runnable() {
						public void run() {
							if (catalogTree.getVisibility() != ZLBoolean3.B3_TRUE) {
								return;
							}
							if (action.Code != ActionCode.SIGNIN) {
								action.run(tree);
							}
						}
					});
					break;
			}
		} else {
			action.run(tree);
		}
	}

	public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {
		if (firstVisible + visibleCount + 1 >= totalCount) {
			final NetworkTree tree = getCurrentTree();
			if (tree instanceof NetworkCatalogTree) {
				((NetworkCatalogTree)tree).loadMoreChildren(totalCount);
			}
		}
	}

	public void onScrollStateChanged(AbsListView view, int state) {
	}
	

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(IConstants.THEME_PREF)){
			theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
		       String myTheme = getIntent().getExtras().getString("curTheme");
		       if(myTheme.equals("black")) {
		        	setTheme(R.style.Theme_myBlack);
		        	getWindow().setBackgroundDrawableResource(R.drawable.theme_black_shelf);
		        	//Toast.makeText(getApplicationContext(), "THEME_BLACK", Toast.LENGTH_LONG).show();
		       } else if(myTheme.equals("laminat")){
		            setTheme(R.style.Theme_Laminat);
		            getWindow().setBackgroundDrawableResource(R.drawable.theme_laminat_shelf);
		           // Toast.makeText(getApplicationContext(), "THEME_LAMINAT", Toast.LENGTH_LONG).show();
		       } else {
		    	    setTheme(R.style.Theme_Redtree);
			        getWindow().setBackgroundDrawableResource(R.drawable.theme_redtree_shelf);
			       // Toast.makeText(getApplicationContext(), "THEME_REDTREE", Toast.LENGTH_LONG).show();
		       }
			recreatethis();
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
    
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
        case android.R.id.home:
        	onBackPressed();
//        	onKeyDown(KeyEvent.KEYCODE_BACK, null);
//        	startActivity(new Intent(this, StartScreenActivity.class));
//        	finish();
            break;
           default:
        	   return onOptionsItemSelected(item);
        }
        return true;
    }

//	@Override
//	public boolean onMenuItemClick(MenuItem item) {
//		// TODO Auto-generated method stub
//		return false;
//	}
}