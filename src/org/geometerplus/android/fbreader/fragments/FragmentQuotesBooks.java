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
import java.util.zip.Inflater;

import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.android.fbreader.BookmarkFragmentActivity.BookmarksAdapter;
import org.geometerplus.android.fbreader.QuotesFragmentActivity.QuotesAdapter;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.fbreader.book.Bookmark;
import org.geometerplus.fbreader.book.Quote;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.fullreader.R;

public final class FragmentQuotesBooks extends SherlockListFragment {
	private static final int OPEN_ITEM_ID = 0;
	private static final int DELETE_ITEM_ID = 2;
	private static final int SHARE_ITEM_ID = 3;
	protected static final int FBSHARE_ITEM_ID = 4;

	BookCollectionShadow myCollection = new BookCollectionShadow();

	public interface OnQuoteListClick {
		void addQuote();

		void gotoQuote(Quote quote);

		void shareQuote(Quote quote);

		void deleteQuote(Quote quote);

		void shareFbQuote(Quote quote);
	}

	protected Object mActionMode;

	OnQuoteListClick listClicker;
	protected int choosenPos;
	
	@Override
	public void onAttach(Activity activity) {
		listClicker = (OnQuoteListClick) activity;
		super.onAttach(activity);
	}

	public void setAdapter(ListAdapter myAllBooksAdapter) {
		setListAdapter(myAllBooksAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.list_fragment, null);
		AdView adView = (AdView) view.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("TEST_DEVICE_ID").build();
		adView.loadAd(adRequest);
		return view;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Quote quote = (Quote) getListView().getAdapter()
				.getItem(position);
		if (quote != null) {
			listClicker.gotoQuote(quote);
		} else {
			listClicker.addQuote();
		}
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setItemsCanFocus(false);
		registerForContextMenu(getListView());

	}

	public String loadCurrentLanguage() {
		SharedPreferences sPref = getActivity().getSharedPreferences(
				"languagePrefs", getActivity().MODE_PRIVATE);
		return sPref.getString("curLanguage", "");
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (loadCurrentLanguage().equals("en")) {
			menu.add(0, OPEN_ITEM_ID, 0, "Open quote");
			menu.add(0, DELETE_ITEM_ID, 0, "Delete quote");
			menu.add(0, SHARE_ITEM_ID, 0, "Share quote");
			menu.add(0, FBSHARE_ITEM_ID, 0, "FB share quote");
		} else if (loadCurrentLanguage().equals("de")) {
			menu.add(0, OPEN_ITEM_ID, 0, "Offene Zitat");
			menu.add(0, DELETE_ITEM_ID, 0, "Zitat löschen");
			menu.add(0, SHARE_ITEM_ID, 0, "Teilen Zitat");
			menu.add(0, FBSHARE_ITEM_ID, 0, "FB teilen zitat");
		} else if (loadCurrentLanguage().equals("fr")) {
			menu.add(0, OPEN_ITEM_ID, 0, "Open quote");
			menu.add(0, DELETE_ITEM_ID, 0, "Supprimer citation");
			menu.add(0, SHARE_ITEM_ID, 0, "Part citation");
			menu.add(0, FBSHARE_ITEM_ID, 0, "Part de FB citation");
		} else if (loadCurrentLanguage().equals("uk")) {
			menu.add(0, OPEN_ITEM_ID, 0, "Відкрити цитату");
			menu.add(0, DELETE_ITEM_ID, 0, "Видалити цитату");
			menu.add(0, SHARE_ITEM_ID, 0, "поділитися цитатою");
			menu.add(0, FBSHARE_ITEM_ID, 0, "поділитися цитатою у FB");
		} else if (loadCurrentLanguage().equals("ru")) {
			menu.add(0, OPEN_ITEM_ID, 0, "Открыть цитату");
			menu.add(0, DELETE_ITEM_ID, 0, "Удалить цитату");
			menu.add(0, SHARE_ITEM_ID, 0, "Поделиться цитатой");
			menu.add(0, FBSHARE_ITEM_ID, 0, "Поделиться цитатой в FB");
		} else {
			if (Locale.getDefault().getDisplayLanguage().equals("русский")) {
				menu.add(0, OPEN_ITEM_ID, 0, "Открыть цитату");
				menu.add(0, DELETE_ITEM_ID, 0, "Удалить цитату");
				menu.add(0, SHARE_ITEM_ID, 0, "Поделиться цитатой");
				menu.add(0, FBSHARE_ITEM_ID, 0, "Поделиться цитатой в FB");
			} else if (Locale.getDefault().getDisplayLanguage()
					.equals("українська")) {
				menu.add(0, OPEN_ITEM_ID, 0, "Open bookmark");
				menu.add(0, DELETE_ITEM_ID, 0, "Delete bookmark");
				menu.add(0, SHARE_ITEM_ID, 0, "Share bookmark");
				menu.add(0, FBSHARE_ITEM_ID, 0, "FB share bookmark");
			} else {
				menu.add(0, OPEN_ITEM_ID, 0, "Відкрити цитату");
				menu.add(0, DELETE_ITEM_ID, 0, "Видалити цитату");
				menu.add(0, SHARE_ITEM_ID, 0, "поділитися цитатою");
				menu.add(0, FBSHARE_ITEM_ID, 0, "поділитися цитатою у FB");
			}
		}

	}

	public void registerForContextMenu(View view) {
		view.setOnCreateContextMenuListener(this);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		final int position = ((AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo()).position;
		Log.d("CHOOSEN POS: ", String.valueOf(position));
		final Quote quote = (Quote) (getListView().getAdapter())
				.getItem(position);
		switch (item.getItemId()) {
			case OPEN_ITEM_ID:
				listClicker.gotoQuote(quote);
				return true;
			case DELETE_ITEM_ID:
				listClicker.deleteQuote(quote);
				((QuotesAdapter) (getListView().getAdapter())).remove(quote);
				return true;
			case SHARE_ITEM_ID: {
				listClicker.shareQuote(quote);
				break;
			}
			case FBSHARE_ITEM_ID: {
				listClicker.shareFbQuote(quote);
				break;
			}
			default:
				return super.onContextItemSelected(item);
		}
		return true;
	}

	/*
	 * private ActionMode.Callback mActionModeCallback = new
	 * ActionMode.Callback() {
	 * 
	 * @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	 * final MenuItem item = menu.add(0, OPEN_ITEM_ID, 0,
	 * ZLResource.resource("quotesView").getResource("open").getValue());
	 * item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER); //menu.add(0,
	 * EDIT_ITEM_ID, 0, myResource.getResource("edit").getValue()); final
	 * MenuItem item2 = menu.add(0, SHARE_ITEM_ID, 0,
	 * ZLResource.resource("quotesView").getResource("share").getValue());
	 * item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS); final MenuItem
	 * item4 = menu.add(0, FBSHARE_ITEM_ID, 0,
	 * ZLResource.resource("quotesView").getResource("fbshare").getValue());
	 * item4.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS); int theme =
	 * PreferenceManager
	 * .getDefaultSharedPreferences(getSherlockActivity()).getInt
	 * (IConstants.THEME_PREF, IConstants.THEME_REDTREE); switch(theme){
	 * default: item2.setIcon(R.drawable.but_share);
	 * item4.setIcon(R.drawable.fb_share); break; case IConstants.THEME_MYBLACK:
	 * item2.setIcon(R.drawable.theme_black_but_share);
	 * item4.setIcon(R.drawable.theme_black_fb_share); break; case
	 * IConstants.THEME_LAMINAT:
	 * item2.setIcon(R.drawable.theme_laminat_but_share);
	 * item4.setIcon(R.drawable.theme_laminat_fb_share); break; case
	 * IConstants.THEME_REDTREE:
	 * item2.setIcon(R.drawable.theme_redtree_but_share);
	 * item4.setIcon(R.drawable.theme_redtree_fb_share); break; } final MenuItem
	 * item3 = menu.add(0, DELETE_ITEM_ID, 0,
	 * ZLResource.resource("quotesView").getResource("delete").getValue());
	 * item3.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER); //menu.add(0,
	 * EDIT_ITEM_ID, 0, myResource.getResource("edit").getValue()); return true;
	 * }
	 * 
	 * @Override public boolean onPrepareActionMode(ActionMode mode, Menu menu)
	 * { // TODO Auto-generated method stub return false; }
	 * 
	 * @Override public boolean onActionItemClicked(ActionMode mode, MenuItem
	 * item) { final int position =
	 * choosenPos;//((AdapterView.AdapterContextMenuInfo
	 * )item.getMenuInfo()).position; final Quote quote = (Quote)
	 * (getListView().getAdapter()).getItem(position); switch (item.getItemId())
	 * { case OPEN_ITEM_ID: listClicker.gotoQuote(quote); return true; case
	 * DELETE_ITEM_ID: listClicker.deleteQuote(quote);
	 * ((QuotesAdapter)(getListView().getAdapter())).remove(quote); return true;
	 * case SHARE_ITEM_ID:{ listClicker.shareQuote(quote); break; } case
	 * FBSHARE_ITEM_ID:{ listClicker.shareFbQuote(quote); break; } } return
	 * false; }
	 * 
	 * @Override public void onDestroyActionMode(ActionMode mode) { // TODO
	 * Auto-generated method stub
	 * 
	 * } }
	 */
}
