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

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class OutlineActivity extends ListActivity {
	OutlineItem mItems[];
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    mItems = OutlineActivityData.get().items;
	    setListAdapter(new OutlineAdapter(getLayoutInflater(),mItems));
	    // Restore the position within the list from last viewing
	    getListView().setSelection(OutlineActivityData.get().position);
	    setResult(-1);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		OutlineActivityData.get().position = getListView().getFirstVisiblePosition();
		setResult(mItems[position].page);
		finish();
	}
}
