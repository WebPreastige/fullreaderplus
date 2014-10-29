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

package org.geometerplus.fbreader.network.tree;

import org.geometerplus.fbreader.network.NetworkTree;
import org.geometerplus.fbreader.network.TopUpItem;
import org.geometerplus.fbreader.network.authentication.NetworkAuthenticationManager;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.network.ZLNetworkException;

public class TopUpTree extends NetworkTree {
	public final TopUpItem Item;

	TopUpTree(NetworkCatalogTree parentTree, TopUpItem item) {
		super(parentTree);
		Item = item;
	}

	@Override
	public String getName() {
		return Item.Title.toString();
	}

	@Override
	public String getSummary() {
		final NetworkAuthenticationManager mgr = getLink().authenticationManager();
		try {
			if (mgr != null && mgr.isAuthorised(false)) {
				final CharSequence summary = Item.getSummary();
			}
		} catch (ZLNetworkException e) {
		}
		return null;
	}

	@Override
	protected ZLImage createCover() {
		return createCover(Item);
	}

	@Override
	protected String getStringId() {
		return "@TopUp Account";
	}
}
