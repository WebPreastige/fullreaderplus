/*
Original OpenOffice Document Reader code 

OpenOffice Document Reader is Android's first native ODF Viewer!
    Copyright (C) 2010  Thomas Taschauer - tomtasche@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package at.tomtasche.reader.background;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import at.stefl.opendocument.java.odf.OpenDocument;

public class Document {

	private final OpenDocument origin;
	private final List<Page> pages;
	private boolean limited;

	public Document(OpenDocument origin) {
		this.origin = origin;

		pages = new ArrayList<Page>();
	}

	public Document(OpenDocument origin, List<Page> pages) {
		this.origin = origin;
		this.pages = pages;
	}

	public OpenDocument getOrigin() {
		return origin;
	}

	public List<Page> getPages() {
		return pages;
	}

	public void addPage(Page page) {
		pages.add(page);
	}

	public Page getPageAt(int index) {
		return pages.get(index);
	}

	public boolean isLimited() {
		return limited;
	}

	public void setLimited(boolean limited) {
		this.limited = limited;
	}

	public static class Page {
		private final String name;
		private final String url;
		private final int index;

		public Page(String name, URI url, int index) {
			this.name = name;
			this.url = url.toString();
			this.index = index;
		}

		public String getName() {
			return name;
		}

		public String getUrl() {
			return url;
		}

		public Uri getUri() {
			return Uri.parse(url);
		}

		public int getIndex() {
			return index;
		}
	}
}
