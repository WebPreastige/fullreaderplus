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
package com.webprestige.fr.citations;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fullreader.R;

public class QuotesListAdapter extends BaseAdapter{
	 Context ctx;
	  LayoutInflater lInflater;
	  ArrayList<MyQuote> quotes;

	  public QuotesListAdapter(Context context, ArrayList<MyQuote> _quotes) {
	    ctx = context;
	    quotes = _quotes;
	    lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	  }

	  @Override
	  public int getCount() {
	    return quotes.size();
	  }

	  @Override
	  public Object getItem(int position) {
	    return quotes.get(position);
	  }

	  @Override
	  public long getItemId(int position) {
	    return position;
	  }

	  MyQuote getQuote(int position) {
	    return ((MyQuote) getItem(position));
	  }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    View view = convertView;
	    if (view == null) {
	      view = lInflater.inflate(R.layout.quote_list_item, parent, false);
	    }

	    MyQuote q = getQuote(position);

	    ((TextView) view.findViewById(R.id.quoteText)).setText(q.getText()+", "+q.getBookTitle()+" ("+q.getBookAuthror()+")");
	    ((TextView) view.findViewById(R.id.quoteCreationDate)).setText(q.getCreationTime());
	    LinearLayout textContainer = (LinearLayout) view.findViewById(R.id.quote_text_container);
	    
	    if (!q.getColor().equals("-1")){
	    	textContainer.setBackgroundColor(Color.parseColor(q.getColor()));
	    }
	    else{
	    	textContainer.setBackgroundColor(Color.TRANSPARENT);
	    }
	    return view;
	}
}
