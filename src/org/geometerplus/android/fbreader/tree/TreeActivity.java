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

package org.geometerplus.android.fbreader.tree;

import java.util.ArrayList;

import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.SetScreenOrientationAction;
import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.fbreader.ReaderApp;
import org.geometerplus.fbreader.library.FileTree;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;
import com.fullreader.R;

public abstract class TreeActivity<T extends FBTree> extends SherlockListActivity implements OnSharedPreferenceChangeListener {
	private static final String OPEN_TREE_ACTION = "android.reader.action.OPEN_TREE";

	public static final String TREE_KEY_KEY = "TreeKey";
	public static final String SELECTED_TREE_KEY_KEY = "SelectedTreeKey";
	public static final String HISTORY_KEY = "HistoryKey";
	private T myCurrentTree;
	// we store the key separately because
	// it will be changed in case of myCurrentTree.removeSelf() call
	private FBTree.Key myCurrentKey;
	private ArrayList<FBTree.Key> myHistory;

	@Override
	public void onCreate(Bundle icicle) {
		Thread.setDefaultUncaughtExceptionHandler(new org.geometerplus.zlibrary.ui.android.library.UncaughtExceptionHandler(this));
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
        switch(theme){
        case IConstants.THEME_MYBLACK:
        	setTheme(R.style.Theme_myBlack);
        	getWindow().setBackgroundDrawableResource(R.drawable.theme_black_shelf);
        case IConstants.THEME_LAMINAT:
            setTheme(R.style.Theme_Laminat);
            getWindow().setBackgroundDrawableResource(R.drawable.theme_laminat_shelf);
        	break;
        case IConstants.THEME_REDTREE:
        	setTheme(R.style.Theme_Redtree);
        	getWindow().setBackgroundDrawableResource(R.drawable.theme_redtree_shelf);
        	break;
        }
        getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		super.onCreate(icicle);
	}

	@Override
	protected void onStart() {
		super.onStart();
		OrientationUtil.setOrientation(this, getIntent());
	}
	
	@Override
	protected void onResume() {
        
		final ZLAndroidLibrary zlibrary = (ZLAndroidLibrary)ZLibrary.Instance();
		SetScreenOrientationAction.setOrientation(this, zlibrary.OrientationOption.getValue());
		super.onResume();
	}

	@Override
	public TreeAdapter getListAdapter() {
		return (TreeAdapter)super.getListAdapter();
	}

	protected T getCurrentTree() {
		return myCurrentTree;
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		OrientationUtil.setOrientation(this, intent);
		if (OPEN_TREE_ACTION.equals(intent.getAction())) {
			runOnUiThread(new Runnable() {
				public void run() {
					init(intent);
				}
			});
		} else {
			super.onNewIntent(intent);
		}
	}

	protected abstract T getTreeByKey(FBTree.Key key);
	public abstract boolean isTreeSelected(FBTree tree);

	protected boolean isTreeInvisible(FBTree tree) {
		return !tree.isVisible();
	}

	@Override
	public void onBackPressed() {
		FBTree parent = null;
		while (parent == null && !myHistory.isEmpty()) {
			parent = getTreeByKey(myHistory.remove(myHistory.size() - 1));
		}
		if (parent == null) {
			parent = myCurrentTree.Parent;
		}
		if (parent != null && !isTreeInvisible(parent)) {
			// В зависимости от уровня на котором находится пользователь
			// скрываем или показываем иконку для значка вставить
			if (parent.Level<=1){
				hideMenuIcon();
			}
			else showMenuIcon();
			// Перед тем, как вернуться на один уровень вверх
			// нужно сделать проверку, не распаковывались ли книги
			// в эту папку и при необходимости - удалить их
			if (FileTree.class.isInstance(parent)){
				FileTree fTree = (FileTree)parent;
				if (fTree.isRarArchive()){
					fTree.deleteUnrarFiles();
				}
			}
			LibraryActivity.setCurrentTree((LibraryTree)parent);
			openTree(parent, myCurrentTree, false);
			return;
		}
		super.onBackPressed();
	}

	// Абстрактные методы, которые будут скрывать или показывать кинку "Вставить"
	public abstract void hideMenuIcon();
	public abstract void showMenuIcon();
	
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == KeyEvent.KEYCODE_BACK) {
//			FBTree parent = null;
//			while (parent == null && !myHistory.isEmpty()) {
//				parent = getTreeByKey(myHistory.remove(myHistory.size() - 1));
//			}
//			if (parent == null) {
//				parent = myCurrentTree.Parent;
//			}
//			if (parent != null && !isTreeInvisible(parent)) {
//				openTree(parent, myCurrentTree, false);
//				return true;
//			}
//		}
//
//		return super.onKeyDown(keyCode, event);
//	}

	// TODO: change to protected
	public void openTree(final FBTree tree) {
		openTree(tree, null, true);
	}
	
	public void openTreeWithoutHistory(final FBTree tree) {
		myHistory.clear();
		openTree(tree, null, false);
	}

	protected void onCurrentTreeChanged() {
	}

	private void openTree(final FBTree tree, final FBTree treeToSelect, final boolean storeInHistory) {
		// Если на данный момент пользователь находится в разделе, который не имеет отношения к файловой систему
		// то нужно скрыть иконку "Вставить"
		if (tree.Level<=1){
			hideMenuIcon();
		}
		else showMenuIcon();
		
		switch (tree.getOpeningStatus()) {
			case WAIT_FOR_OPEN:
			case ALWAYS_RELOAD_BEFORE_OPENING:
				final String messageKey = tree.getOpeningStatusMessage();
				if (messageKey != null) {
					UIUtil.runWithMessage(
						TreeActivity.this, messageKey,
						new Runnable() {
							public void run() {
								tree.waitForOpening();
							}
						},
						new Runnable() {
							public void run() {
								openTreeInternal(tree, treeToSelect, storeInHistory);
							}
						},
						true
					);
				} else {
					tree.waitForOpening();
					openTreeInternal(tree, treeToSelect, storeInHistory);
				}
				break;
			default:
				openTreeInternal(tree, treeToSelect, storeInHistory);
				break;
		}
	}

	protected void init(Intent intent) {
		final FBTree.Key key = (FBTree.Key)intent.getSerializableExtra(TREE_KEY_KEY);
		final FBTree.Key selectedKey = (FBTree.Key)intent.getSerializableExtra(SELECTED_TREE_KEY_KEY);
		myCurrentTree = getTreeByKey(key);
		// not myCurrentKey = key
		// because key might be null
		myCurrentKey = myCurrentTree.getUniqueKey();
		final TreeAdapter adapter = getListAdapter();
		adapter.replaceAll(myCurrentTree.subTrees());
		setTitle(myCurrentTree.getTreeTitle());
		final FBTree selectedTree =
			selectedKey != null ? getTreeByKey(selectedKey) : adapter.getFirstSelectedItem();
		final int index = adapter.getIndex(selectedTree);
		if (index != -1) {
			setSelection(index);
			getListView().post(new Runnable() {
				public void run() {
					setSelection(index);
				}
			});
		}else{
			setSelection(0);
		}
		
		getListView().setCacheColorHint(Color.TRANSPARENT);

		myHistory = (ArrayList<FBTree.Key>)intent.getSerializableExtra(HISTORY_KEY);
		if (myHistory == null) {
			myHistory = new ArrayList<FBTree.Key>();
		}
		onCurrentTreeChanged();
	}

	private void openTreeInternal(FBTree tree, FBTree treeToSelect, boolean storeInHistory) {
		switch (tree.getOpeningStatus()) {
			case READY_TO_OPEN:
			case ALWAYS_RELOAD_BEFORE_OPENING:
				if (storeInHistory && !myCurrentKey.equals(tree.getUniqueKey())) {
					myHistory.add(myCurrentKey);
				}
				onNewIntent(new Intent(this, getClass())
					.setAction(OPEN_TREE_ACTION)
					.putExtra(TREE_KEY_KEY, tree.getUniqueKey())
					.putExtra(
						SELECTED_TREE_KEY_KEY,
						treeToSelect != null ? treeToSelect.getUniqueKey() : null
					)
					.putExtra(HISTORY_KEY, myHistory)
				);
				break;
			case CANNOT_OPEN:
				UIUtil.showErrorMessage(TreeActivity.this, tree.getOpeningStatusMessage());
				break;
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(IConstants.THEME_PREF)){
			//recreatethis();
		}     

	}
	
    /*public void recreatethis()
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
    }*/
    
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
        case android.R.id.home:
        	onBackPressed();
        	//onKeyDown(KeyEvent.KEYCODE_BACK, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
//        	startActivity(new Intent(this, StartScreenActivity.class));
//        	finish();
            break;
        }
        return true;
    }
}
