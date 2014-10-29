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

import org.vudroid.core.presentation.BrowserAdapter;
import org.vudroid.core.presentation.UriBrowserAdapter;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TabHost;

import com.fullreader.R;

public abstract class BaseBrowserActivity extends Activity
{
    private BrowserAdapter adapter;
    private static final String CURRENT_DIRECTORY = "currentDirectory";
    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener()
    {
        @SuppressWarnings({"unchecked"})
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            final File file = ((AdapterView<BrowserAdapter>)adapterView).getAdapter().getItem(i);
            if (file.isDirectory())
            {
                setCurrentDir(file);
            }
            else
            {
                showDocument(file);
            }
        }
    };
    private UriBrowserAdapter recentAdapter;
    private ViewerPreferences viewerPreferences;
    protected final FileFilter filter;

    public BaseBrowserActivity()
    {
        this.filter = createFileFilter();
    }

    protected abstract FileFilter createFileFilter();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser);
        viewerPreferences = new ViewerPreferences(this);
        final ListView browseList = initBrowserListView();
        final ListView recentListView = initRecentListView();
        TabHost tabHost = (TabHost) findViewById(R.id.browserTabHost);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("Browse").setIndicator("Browse").setContent(new TabHost.TabContentFactory()
        {
            public View createTabContent(String s)
            {
                return browseList;
            }
        }));
        tabHost.addTab(tabHost.newTabSpec("Recent").setIndicator("Recent").setContent(new TabHost.TabContentFactory()
        {
            public View createTabContent(String s)
            {
                return recentListView;
            }
        }));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        final File sdcardPath = new File("/sdcard");
        if (sdcardPath.exists())
        {
            setCurrentDir(sdcardPath);
        }
        else
        {
            setCurrentDir(new File("/"));
        }
        if (savedInstanceState != null)
        {
            final String absolutePath = savedInstanceState.getString(CURRENT_DIRECTORY);
            if (absolutePath != null)
            {
                setCurrentDir(new File(absolutePath));
            }
        }
    }

    private ListView initBrowserListView()
    {
        final ListView listView = new ListView(this);
        adapter = new BrowserAdapter(this, filter);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(onItemClickListener);
        listView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        return listView;
    }

    private ListView initRecentListView()
    {
        ListView listView = new ListView(this);
        recentAdapter = new UriBrowserAdapter();
        listView.setAdapter(recentAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @SuppressWarnings({"unchecked"})
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                showDocument(((AdapterView<UriBrowserAdapter>) adapterView).getAdapter().getItem(i));
            }
        });
        listView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        return listView;
    }

    private void showDocument(File file)
    {
        showDocument(Uri.fromFile(file));
    }

    protected abstract void showDocument(Uri uri);

    private void setCurrentDir(File newDir)
    {
        adapter.setCurrentDirectory(newDir);
        getWindow().setTitle(newDir.getAbsolutePath());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_DIRECTORY, adapter.getCurrentDirectory().getAbsolutePath());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        recentAdapter.setUris(viewerPreferences.getRecent());
    }
}
