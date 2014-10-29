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

package org.geometerplus.fbreader.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geometerplus.fbreader.network.urlInfo.UrlInfo;
import org.geometerplus.fbreader.network.urlInfo.UrlInfoCollection;
import org.geometerplus.zlibrary.core.options.ZLStringListOption;

public abstract class BasketItem extends NetworkCatalogItem {
	private long myGeneration = 0;

	private final ZLStringListOption myBooksInBasketOption;
	private final Map<String,NetworkBookItem> myBooks =
		Collections.synchronizedMap(new HashMap<String,NetworkBookItem>());

	protected BasketItem(INetworkLink link) {
		super(
			link,
			NetworkLibrary.resource().getResource("basket").getValue(),
			NetworkLibrary.resource().getResource("basketSummaryEmpty").getValue(),
			new UrlInfoCollection<UrlInfo>(),
			Accessibility.ALWAYS,
			FLAGS_DEFAULT & ~FLAGS_GROUP
		);
		myBooksInBasketOption = new ZLStringListOption(Link.getSiteName(), "Basket", Collections.<String>emptyList(), ",");
	}

	public void addItem(NetworkBookItem book) {
		myBooks.put(book.Id, book);
	}

	@Override
	public CharSequence getSummary() {
		final int size = bookIds().size();
		if (size == 0) {
			return super.getSummary();
		} else {
			//final Money basketCost = cost();
				return NetworkLibrary.resource().getResource("basketSummaryCountOnly").getValue(size)
					.replace("%0", String.valueOf(size));
		}
	}

	@Override
	public boolean canBeOpened() {
		return !bookIds().isEmpty();
	}

	@Override
	public String getStringId() {
		return "@Basket:" + Link.getSiteName();
	}

	public long getGeneration() {
		return myGeneration;
	}

	public final void add(NetworkBookItem book) {
		List<String> ids = bookIds();
		if (!ids.contains(book.Id)) {
			ids = new ArrayList<String>(ids);
			ids.add(book.Id);
			myBooksInBasketOption.setValue(ids);
			addItem(book);
			++myGeneration;
			NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
		}
	}

	public final void remove(NetworkBookItem book) {
		List<String> ids = bookIds();
		if (ids.contains(book.Id)) {
			ids = new ArrayList<String>(ids);
			ids.remove(book.Id);
			myBooksInBasketOption.setValue(ids);
			myBooks.remove(book);
			++myGeneration;
			NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
		}
	}

	public final void clear() {
		myBooksInBasketOption.setValue(null);
		myBooks.clear();
		++myGeneration;
		NetworkLibrary.Instance().fireModelChangedEvent(NetworkLibrary.ChangeListener.Code.SomeCode);
	}

	public final boolean contains(NetworkBookItem book) {
		return bookIds().contains(book.Id);
	}

	public List<String> bookIds() {
		return myBooksInBasketOption.getValue();
	}

	public NetworkBookItem getBook(String id) {
		return myBooks.get(id);
	}

	protected boolean isFullyLoaded() {
		synchronized (myBooks) {
			for (String id : bookIds()) {
				final NetworkBookItem b = myBooks.get(id);
				if (b == null) {
					return false;
				}
			}
		}
		return true;
	}
}
