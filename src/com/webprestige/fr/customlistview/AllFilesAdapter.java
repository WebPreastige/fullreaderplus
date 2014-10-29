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
package com.webprestige.fr.customlistview;

import java.util.ArrayList;

import com.webprestige.fr.bookmarks.MyBookmark;
import com.fullreader.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;

public class AllFilesAdapter extends BaseAdapter{
	Context ctx;
	LayoutInflater lInflater;
	ArrayList<MyFile> files;
	
	public AllFilesAdapter(Context context, ArrayList<MyFile> _files) {
		ctx = context;
		files = _files;
		lInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	 @Override
	  public int getCount() {
	    return files.size();
	  }

	  @Override
	  public Object getItem(int position) {
	    return files.get(position);
	  }

	  @Override
	  public long getItemId(int position) {
	    return position;
	  }

	  MyFile getFile(int position) {
	    return ((MyFile) getItem(position));
	  }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    View view = convertView;
	    if (view == null) {
	      view = lInflater.inflate(R.layout.all_files_list_item, parent, false);
	    }
	    MyFile file = getFile(position);
	    ((TextView) view.findViewById(R.id.file_title)).setText(file.getFileTitle());
	    if(file.getFileTitle().contains(".fb2")){
	    	 ((ImageView) view.findViewById(R.id.file_image)).setImageResource(R.drawable.fb2);
	    } else if(file.getFileTitle().contains(".epub")) {
	    	 ((ImageView) view.findViewById(R.id.file_image)).setImageResource(R.drawable.epub);
	    } else if(file.getFileTitle().contains(".doc")) {
	    	 ((ImageView) view.findViewById(R.id.file_image)).setImageResource(R.drawable.doc);
	    } else if(file.getFileTitle().contains(".txt")) {
	    	 ((ImageView) view.findViewById(R.id.file_image)).setImageResource(R.drawable.txt);
	    } else if(file.getFileTitle().contains(".mobi")) {
	    	 ((ImageView) view.findViewById(R.id.file_image)).setImageResource(R.drawable.mobi);
	    } else if(file.getFileTitle().contains(".rtf")) {
	    	 ((ImageView) view.findViewById(R.id.file_image)).setImageResource(R.drawable.rtf);
	    } else if(file.getFileTitle().contains(".html")) {
	    	 ((ImageView) view.findViewById(R.id.file_image)).setImageResource(R.drawable.html);
	    } else if(file.getFileTitle().contains(".pdf")) {
	    	 ((ImageView) view.findViewById(R.id.file_image)).setImageResource(R.drawable.ic_list_library_book_pdf);
	    } else if(file.getFileTitle().contains(".djvu")) {
	    	 ((ImageView) view.findViewById(R.id.file_image)).setImageResource(R.drawable.ic_list_library_book_djvu);
	    }
	   
	    return view;
	}

}
