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
package org.vudroid.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public class ViewerPreferences
{
    private SharedPreferences sharedPreferences;
    private static final String FULL_SCREEN = "FullScreen";

    public ViewerPreferences(Context context)
    {
        sharedPreferences = context.getSharedPreferences("ViewerPreferences", 0);
    }

    public void setFullScreen(boolean fullscreen)
    {
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(FULL_SCREEN, fullscreen);
        editor.commit();
    }

    public boolean isFullScreen()
    {
        return sharedPreferences.getBoolean(FULL_SCREEN, false);
    }

    public void addRecent(Uri uri)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("recent:" + uri.toString(), uri.toString() + "\n" + System.currentTimeMillis());
        editor.commit();
    }

    public List<Uri> getRecent()
    {
        TreeMap<Long, Uri> treeMap = new TreeMap<Long, Uri>();
        for (String key : sharedPreferences.getAll().keySet())
        {
            if (key.startsWith("recent"))
            {
                String uriPlusDate = sharedPreferences.getString(key, null);
                String[] uriThenDate = uriPlusDate.split("\n");
                treeMap.put(Long.parseLong(uriThenDate.length > 1 ? uriThenDate[1] : "0"), Uri.parse(uriThenDate[0]));
            }
        }
        ArrayList<Uri> list = new ArrayList<Uri>(treeMap.values());
        Collections.reverse(list);
        return list;
    }
}
