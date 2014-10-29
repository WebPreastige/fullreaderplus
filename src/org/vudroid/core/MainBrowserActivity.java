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

import java.io.File;
import java.io.FileFilter;
import java.util.HashMap;

import org.vudroid.djvudroid.DjvuViewerActivity;
import org.vudroid.pdfdroid.PdfViewerActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class MainBrowserActivity extends BaseBrowserActivity
{
    private final static HashMap<String, Class<? extends Activity>> extensionToActivity = new HashMap<String, Class<? extends Activity>>();

    static
    {
        extensionToActivity.put("pdf", PdfViewerActivity.class);
        extensionToActivity.put("djvu", DjvuViewerActivity.class);
        extensionToActivity.put("djv", DjvuViewerActivity.class);
    }

    @Override
    protected FileFilter createFileFilter()
    {
        return new FileFilter()
        {
            public boolean accept(File pathname)
            {
                for (String s : extensionToActivity.keySet())
                {
                    if (pathname.getName().endsWith("." + s)) return true;
                }
                return pathname.isDirectory();
            }
        };
    }

    @Override
    protected void showDocument(Uri uri)
    {
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        String uriString = uri.toString();
        String extension = uriString.substring(uriString.lastIndexOf('.') + 1);
        intent.setClass(this, extensionToActivity.get(extension));
        startActivity(intent);
    }
}
