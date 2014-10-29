/*
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

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

public class FileProvider extends ContentProvider {

	@Override
	public ParcelFileDescriptor openFile(final Uri uri, final String mode)
			throws FileNotFoundException {
		final File file = new File(
				AndroidFileCache.getCacheDirectory(getContext()),
				uri.getLastPathSegment());

		final ParcelFileDescriptor parcel = ParcelFileDescriptor.open(file,
				ParcelFileDescriptor.MODE_READ_ONLY);
		return parcel;
	}

	@Override
	public boolean onCreate() {
		return false;
	}

	@Override
	public Cursor query(final Uri uri, final String[] projection,
			final String selection, final String[] selectionArgs,
			final String sortOrder) {
		return null;
	}

	@Override
	public String getType(final Uri uri) {
		return null;
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		return null;
	}

	@Override
	public int delete(final Uri uri, final String selection,
			final String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(final Uri uri, final ContentValues values,
			final String selection, final String[] selectionArgs) {
		return 0;
	}
}
