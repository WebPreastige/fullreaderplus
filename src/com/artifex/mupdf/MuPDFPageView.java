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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;

public class MuPDFPageView extends PageView {
	private final MuPDFCore mCore;

	public MuPDFPageView(Context c, MuPDFCore core, Point parentSize) {
		super(c, parentSize);
		mCore = core;
	}

	public int hitLinkPage(float x, float y) {
		// Since link highlighting was implemented, the super class
		// PageView has had sufficient information to be able to
		// perform this method directly. Making that change would
		// make MuPDFCore.hitLinkPage superfluous.
		float scale = mSourceScale*(float)getWidth()/(float)mSize.x;
		float docRelX = (x - getLeft())/scale;
		float docRelY = (y - getTop())/scale;

		return mCore.hitLinkPage(mPageNumber, docRelX, docRelY);
	}

	@Override
	protected void drawPage(Bitmap bm, int sizeX, int sizeY,
			int patchX, int patchY, int patchWidth, int patchHeight) {
		mCore.drawPage(mPageNumber, bm, sizeX, sizeY, patchX, patchY, patchWidth, patchHeight);
	}

	@Override
	protected LinkInfo[] getLinkInfo() {
		return mCore.getPageLinks(mPageNumber);
	}
}
