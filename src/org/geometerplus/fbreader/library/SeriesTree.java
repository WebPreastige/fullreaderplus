/*
* FullReader+
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.fbreader.library;

import java.util.Collections;

import org.geometerplus.fbreader.book.Author;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookEvent;
import org.geometerplus.fbreader.book.IBookCollection;
import org.geometerplus.fbreader.book.Series;
import org.geometerplus.fbreader.book.SeriesInfo;
import org.geometerplus.zlibrary.core.util.MiscUtil;

public final class SeriesTree extends LibraryTree {
	public final Series Series;
	public final Author Author;

	SeriesTree(IBookCollection collection, Series series, Author author) {
		super(collection);
		Series = series;
		Author = author;
	}

	SeriesTree(LibraryTree parent, Series series, Author author, int position) {
		super(parent, position);
		Series = series;
		Author = author;
	}

	@Override
	public String getName() {
		return Series.getTitle();
	}

	@Override
	public String getSummary() {
		if (Author != null) {
			return MiscUtil.join(Collection.titlesForSeriesAndAuthor(Series.getTitle(), Author, 5), ", ");
		} else {
			return MiscUtil.join(Collection.titlesForSeries(Series.getTitle(), 5), ", ");
		}
	}

	@Override
	protected String getStringId() {
		return "@SeriesTree " + getName();
	}

	@Override
	public boolean containsBook(Book book) {
		if (book == null) {
			return false;
		}
		final SeriesInfo info = book.getSeriesInfo();
		return info != null && Series.equals(info.Series);
	}

	@Override
	protected String getSortKey() {
		return Series.getSortKey();
	}

	@Override
	public Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public void waitForOpening() {
		clear();
		if (Author != null) {
			for (Book book : Collection.booksForSeriesAndAuthor(Series.getTitle(), Author)) {
				createBookInSeriesSubTree(book);
			}
		} else {
			for (Book book : Collection.booksForSeries(Series.getTitle())) {
				createBookInSeriesSubTree(book);
			}
		}
	}

	@Override
	public boolean onBookEvent(BookEvent event, Book book) {
		switch (event) {
			case Added:
				return containsBook(book) && createBookInSeriesSubTree(book);
			case Updated:
			{
				boolean changed = removeBook(book);
				changed |= containsBook(book) && createBookInSeriesSubTree(book);
				return changed;
			}
			case Removed:
			default:
				return super.onBookEvent(event, book);
		}
	}

	boolean createBookInSeriesSubTree(Book book) {
		final BookInSeriesTree temp = new BookInSeriesTree(Collection, book);
		int position = Collections.binarySearch(subTrees(), temp);
		if (position >= 0) {
			return false;
		} else {
			new BookInSeriesTree(this, book, - position - 1);
			return true;
		}
	}
}
