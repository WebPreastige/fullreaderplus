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

package at.tomtasche.reader.ui.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fullreader.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import at.tomtasche.reader.background.RecentDocumentsUtil;
import at.tomtasche.reader.ui.activity.DocumentLoadingActivity;

public class RecentDocumentDialogFragment extends DialogFragment implements
		OnItemClickListener, OnItemLongClickListener {

	public static final String FRAGMENT_TAG = "document_chooser";

	private Map<String, String> items;
	private ListAdapter adapter;
	private ListView listView;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setTitle(R.string.dialog_recent_title);
		builder.setCancelable(true);

		TextView emptyView = new TextView(getActivity());
		emptyView.setText(R.string.dialog_loading_title);

		listView = new ListView(getActivity());
		listView.setEmptyView(emptyView);
		listView.setOnItemClickListener(this);
		listView.setOnItemLongClickListener(this);

		adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, new String[0]);
		listView.setAdapter(adapter);

		builder.setView(listView);

		setCancelable(true);

		items = new HashMap<String, String>();

		loadRecentDocuments();

		listView.setEmptyView(emptyView);

		return builder.create();
	}

	private void loadRecentDocuments() {
		items.clear();
		try {
			items.putAll(RecentDocumentsUtil.getRecentDocuments(getActivity()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (items.size() == 0) {
			items = new HashMap<String, String>();
			items.put(
					getActivity().getString(
							R.string.dialog_list_no_documents_found), null);
		}

		adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, new ArrayList<String>(
						items.keySet()));

		listView.setAdapter(adapter);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (items == null)
			return;

		String key = (String) adapter.getItem(arg2);
		if (key == null)
			return;

		String uri = items.get(key);
		if (uri == null)
			return;

		dismiss();

		DocumentLoadingActivity activity = ((DocumentLoadingActivity) getActivity());
		activity.loadUri(Uri.parse(uri));
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		if (items == null)
			return false;

		final String key = (String) adapter.getItem(arg2);
		if (key == null)
			return false;

		try {
			RecentDocumentsUtil.removeRecentDocument(getActivity(), key);
		} catch (IOException e) {
			e.printStackTrace();
		}

		loadRecentDocuments();

		return true;
	}
}
