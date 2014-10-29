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

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fullreader.R;

public class BrowserAdapter extends BaseAdapter
{
    private final Context context;
    private File currentDirectory;
    private List<File> files = Collections.emptyList();
    private final FileFilter filter;

    public BrowserAdapter(Context context, FileFilter filter)
    {
        this.context = context;
        this.filter = filter;
    }

    public int getCount()
    {
        return files.size();
    }

    public File getItem(int i)
    {
        return files.get(i);
    }

    public long getItemId(int i)
    {
        return i;
    }

    public View getView(int i, View view, ViewGroup viewGroup)
    {
        final View browserItem = LayoutInflater.from(context).inflate(R.layout.browseritem, viewGroup, false);
        final ImageView imageView = (ImageView) browserItem.findViewById(R.id.browserItemIcon);
        final File file = files.get(i);
        final TextView textView = (TextView) browserItem.findViewById(R.id.browserItemText);
        textView.setText(file.getName());
        if (file.equals(currentDirectory.getParentFile()))
        {
            imageView.setImageResource(R.drawable.arrowup);
            textView.setText(file.getAbsolutePath());
        }
        else if (file.isDirectory())
        {
            imageView.setImageResource(R.drawable.folderopen);
        }
        else
        {
            imageView.setImageResource(R.drawable.book);
        }
        return browserItem;
    }

    public void setCurrentDirectory(File currentDirectory)
    {
        final File[] fileArray = currentDirectory.listFiles(filter);
        ArrayList<File> files = new ArrayList<File>(fileArray != null ? Arrays.asList(fileArray) : Collections.<File>emptyList());
        this.currentDirectory = currentDirectory;
        Collections.sort(files, new Comparator<File>()
        {
            public int compare(File o1, File o2)
            {
                if (o1.isDirectory() && o2.isFile()) return -1;
                if (o1.isFile() && o2.isDirectory()) return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
        if (currentDirectory.getParentFile() != null)
        {
            files.add(0, currentDirectory.getParentFile());
        }
        setFiles(files);
    }

    public void setFiles(List<File> files)
    {
        this.files = files;
        notifyDataSetInvalidated();
    }

    public File getCurrentDirectory()
    {
        return currentDirectory;
    }
}
