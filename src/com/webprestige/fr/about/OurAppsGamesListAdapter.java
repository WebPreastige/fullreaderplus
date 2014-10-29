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
package com.webprestige.fr.about;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.fullreader.R;

public class OurAppsGamesListAdapter extends BaseAdapter{
	
	Context ctx;
	LayoutInflater lInflater;
	ArrayList<ListItem> objects;
	
	public OurAppsGamesListAdapter(Context context, ArrayList<ListItem> items) {
	    ctx = context;
	    objects = items;
	    lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		   return objects.size();
	}

	@Override
	public ListItem getItem(int position) {
		// TODO Auto-generated method stub
		 return objects.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		 return position;
	}
	
	ListItem getListItem(int position) {
	   return ((ListItem) getItem(position));
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = convertView;
	    if (view == null) {
	      view = lInflater.inflate(R.layout.our_apps_games_list_item, parent, false);
	    }

	    ListItem p = getListItem(position);
	    ((TextView) view.findViewById(R.id.title_tv)).setText(p.getTitle());
	    ((ImageView) view.findViewById(R.id.image_iv)).setImageBitmap(p.getImage());
	    
	    return view;
	}

}
