/*
* FullReader+
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.android.fbreader.network.action;

import org.geometerplus.android.fbreader.network.NetworkLibraryActivity;
import org.geometerplus.fbreader.network.NetworkCatalogItem;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.NetworkURLCatalogItem;
import org.geometerplus.fbreader.network.tree.NetworkCatalogTree;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;

import com.fullreader.R;

public class ReloadCatalogAction extends CatalogAction {
	public ReloadCatalogAction(NetworkLibraryActivity activity) {
		super(activity, ActionCode.RELOAD_CATALOG, "reload", R.drawable.ic_menu_refresh);
	}

	@Override
	public boolean isVisible(NetworkTree tree) {
		if (!super.isVisible(tree)) {
			return false;
		}
		final NetworkCatalogItem item = ((NetworkCatalogTree)tree).Item;
		if (!(item instanceof NetworkURLCatalogItem)) {
			return false;
		}
		return ((NetworkURLCatalogItem)item).getUrl(UrlInfo.Type.Catalog) != null;
	}

	@Override
	public boolean isEnabled(NetworkTree tree) {
		return NetworkLibrary.Instance().getStoredLoader(tree) == null;
	}

	@Override
	public void run(NetworkTree tree) {
		if (NetworkLibrary.Instance().getStoredLoader(tree) != null) {
			return;
		}
		((NetworkCatalogTree)tree).clearCatalog();
		((NetworkCatalogTree)tree).startItemsLoader(false, false);
	}
}
