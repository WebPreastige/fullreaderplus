/*
FullReader+
Copyright 2013-2014 Viktoriya Bilyk

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
package com.webprestige.fr.bookmarks;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fullreader.R;

public class BookmarksListAdapter extends BaseAdapter{
	  Context ctx;
	  LayoutInflater lInflater;
	  ArrayList<MyBookmark> bookmarks;

	  public BookmarksListAdapter(Context context, ArrayList<MyBookmark> _bookmarks) {
	    ctx = context;
	    bookmarks = _bookmarks;
	    lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	  }

	  @Override
	  public int getCount() {
	    return bookmarks.size();
	  }

	  @Override
	  public Object getItem(int position) {
	    return bookmarks.get(position);
	  }

	  @Override
	  public long getItemId(int position) {
	    return position;
	  }

	  MyBookmark getBookmark(int position) {
	    return ((MyBookmark) getItem(position));
	  }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    View view = convertView;
	    if (view == null) {
	      view = lInflater.inflate(R.layout.bookmark_list_item, parent, false);
	    }

	    MyBookmark b = getBookmark(position);

	    ((TextView) view.findViewById(R.id.bookAuthorAndTitle)).setText(b.getBookTitle()+" ("+b.getBookAuthror()+")");
	    ((TextView) view.findViewById(R.id.bookmarkCreationDate)).setText(b.getCreationTime());
	    return view;
	}
}
