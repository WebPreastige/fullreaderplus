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

package org.geometerplus.android.fbreader.fragments;

import java.util.Locale;

import org.geometerplus.android.fbreader.BookmarkFragmentActivity.BookmarksAdapter;
import org.geometerplus.fbreader.book.Bookmark;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.fullreader.R;


public final class FragmentBookmarks extends SherlockListFragment{
	private static final int OPEN_ITEM_ID = 0;
	private static final int DELETE_ITEM_ID = 1;
	
	public interface OnBookmarkListClick{
		void addBookmark();
		void gotoBookmark(Bookmark quote);
		
		void deleteBookmark(Bookmark quote);
	}
	
	OnBookmarkListClick listClicker;
	protected Object mActionMode;
	//protected int choosenPos;
	
	@Override
	public void onAttach(Activity activity) {
		listClicker = (OnBookmarkListClick) activity;
		super.onAttach(activity);
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { 
        View view = inflater.inflate(R.layout.list_fragment, null);
        AdView adView = (AdView)view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .addTestDevice("TEST_DEVICE_ID")
            .build();
        adView.loadAd(adRequest);
		return view;
    }
    
	public void setAdapter(ListAdapter myAllBooksAdapter){
		setListAdapter(myAllBooksAdapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Bookmark quote = (Bookmark) getListView().getAdapter().getItem(position);
		if (quote != null) {
			listClicker.gotoBookmark(quote);
		} else {
			listClicker.addBookmark();
		}
		super.onListItemClick(l, v, position, id);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    getListView().setItemsCanFocus(false);
	    registerForContextMenu(getListView());
	    /*getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
		          
				return true;
			}
		}); */
	}
	
	public String loadCurrentLanguage() {
	    SharedPreferences sPref = getActivity().getSharedPreferences("languagePrefs", getActivity().MODE_PRIVATE);
	    return sPref.getString("curLanguage", "");
	}
	
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v,
        ContextMenuInfo menuInfo) {
		if(loadCurrentLanguage().equals("en")){
			menu.add(0, OPEN_ITEM_ID, 0, "Open bookmark");
	      	menu.add(0, DELETE_ITEM_ID, 0, "Delete bookmark");
		} else if(loadCurrentLanguage().equals("de")){
			menu.add(0, OPEN_ITEM_ID, 0, "Lesezeichen öffnen");
	      	menu.add(0, DELETE_ITEM_ID, 0, "Lesezeichen löschen");
		} else if(loadCurrentLanguage().equals("fr")){
			menu.add(0, OPEN_ITEM_ID, 0, "Ouvrir un signet");
	      	menu.add(0, DELETE_ITEM_ID, 0, "Supprimer le favori");
		} else if(loadCurrentLanguage().equals("uk")){
			menu.add(0, OPEN_ITEM_ID, 0, "Відкрити закладку");
	      	menu.add(0, DELETE_ITEM_ID, 0, "Видалити закладку");
		} else if(loadCurrentLanguage().equals("ru")){
			menu.add(0, OPEN_ITEM_ID, 0, "Открыть закладку");
	      	menu.add(0, DELETE_ITEM_ID, 0, "Удалить закладку");
		} else {
			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
				menu.add(0, OPEN_ITEM_ID, 0, "Открыть закладку");
		      	menu.add(0, DELETE_ITEM_ID, 0, "Удалить закладку");
			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
				menu.add(0, OPEN_ITEM_ID, 0, "Відкрити закладку");
		      	menu.add(0, DELETE_ITEM_ID, 0, "Видалити закладку");
			}else {
				menu.add(0, OPEN_ITEM_ID, 0, "Open bookmark");
		      	menu.add(0, DELETE_ITEM_ID, 0, "Delete bookmark");
			}
		}
      
    }
	
	 public void registerForContextMenu(View view) {
	    view.setOnCreateContextMenuListener(this);
	 }
	 
	 
	 @Override
	  public boolean onContextItemSelected(MenuItem item) {
	    // TODO Auto-generated method stub
		final int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
		Log.d("CHOOSEN POS: ", String.valueOf(position));
		final Bookmark bookmark = (Bookmark) (getListView().getAdapter()).getItem(position);
	    switch (item.getItemId()) {
	    case OPEN_ITEM_ID:
	    	listClicker.gotoBookmark(bookmark);
	      break;
	    case DELETE_ITEM_ID:
	    	listClicker.deleteBookmark(bookmark);
			//((BookmarksAdapter)(getListView().getAdapter())).remove(bookmark);
	      break;
	    }
	    return true;
	  }
	 
	/*private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			//menu.setHeaderTitle(getItem(position).getText());
	    	final MenuItem item = menu.add(0, OPEN_ITEM_ID, 0, ZLResource.resource("bookmarksView").getResource("open").getValue());
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
			//menu.add(0, EDIT_ITEM_ID, 0, myResource.getResource("edit").getValue());
            final MenuItem item2 = menu.add(0, DELETE_ITEM_ID, 0, ZLResource.resource("bookmarksView").getResource("delete").getValue());
            item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
	        return true;
	    }

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			final int position = choosenPos;//((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
			final Bookmark quote = (Bookmark) (getListView().getAdapter()).getItem(position);
			switch (item.getItemId()) {
				case OPEN_ITEM_ID:
					listClicker.gotoBookmark(quote);
					return true;
				case DELETE_ITEM_ID:
					listClicker.deleteBookmark(quote);
					((BookmarksAdapter)(getListView().getAdapter())).remove(quote);
					return true;
				}
			
		return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			
		}
	};*/

}
