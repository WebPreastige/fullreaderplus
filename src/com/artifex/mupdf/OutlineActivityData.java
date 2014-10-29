/*
  	MuPDF is Copyright 2006-2013 Artifex Software, Inc.
 	This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.artifex.mupdf;

public class OutlineActivityData {
	public OutlineItem items[];
	public int         position;
	static private OutlineActivityData singleton;

	static public void set(OutlineActivityData d) {
		singleton = d;
	}

	static public OutlineActivityData get() {
		if (singleton == null)
			singleton = new OutlineActivityData();
		return singleton;
	}
}
