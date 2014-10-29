/*
Vudroid
Copyright 2010-2011 Pavel Tiunov

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
package org.vudroid.core.presentation;

import java.util.Collections;
import java.util.List;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fullreader.R;

public class UriBrowserAdapter extends BaseAdapter
{
    private List<Uri> uris = Collections.emptyList();

    public int getCount()
    {
        return uris.size();
    }

    public Uri getItem(int i)
    {
        return uris.get(i);
    }

    public long getItemId(int i)
    {
        return i; 
    }

    public View getView(int i, View view, ViewGroup viewGroup)
    {
        final View browserItem = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.browseritem, viewGroup, false);
        final ImageView imageView = (ImageView) browserItem.findViewById(R.id.browserItemIcon);
        final Uri uri = uris.get(i);
        final TextView textView = (TextView) browserItem.findViewById(R.id.browserItemText);
        textView.setText(uri.getLastPathSegment());
        imageView.setImageResource(R.drawable.book);
        return browserItem;
    }

    public void setUris(List<Uri> uris)
    {
        this.uris = uris;
        notifyDataSetInvalidated();
    }
}
