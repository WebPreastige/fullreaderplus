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
import java.net.URI;
import java.net.URISyntaxException;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import at.stefl.opendocument.java.translator.File2URITranslator;
import at.stefl.opendocument.java.util.DefaultFileCache;

public class AndroidFileCache extends DefaultFileCache {

	private static File cache;

	public static final File getCacheDirectory(Context context) {
		if (cache != null && testDirectory(cache)) {
			return cache;
		} else {
			File directory = context.getCacheDir();
			if (!testDirectory(directory)) {
				directory = context.getFilesDir();
				if (!testDirectory(directory)) {
					directory = new File(
							Environment.getExternalStorageDirectory(),
							".odf-reader");
					if (!testDirectory(directory)) {
						throw new IllegalStateException(
								"No writable cache available");
					}
				}
			}

			return cache = directory;
		}
	}

	private static final boolean testDirectory(File directory) {
		return directory != null && directory.canWrite() && directory.canRead();
	}

	private static final File2URITranslator URI_TRANSLATOR = new File2URITranslator() {
		@Override
		public URI translate(File file) {
			URI uri = file.toURI();

			File imageFile = new File(uri);
			String imageFileName = imageFile.getName();

			URI result = null;
			try {
				result = new URI(
				// use relative paths (important for chromecast-support)
				// "content://at.tomtasche.reader/" +
						Uri.encode(imageFileName));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

			return result;
		}
	};

	public AndroidFileCache(Context context) {
		super(getCacheDirectory(context), URI_TRANSLATOR);
	}

	public static Uri getCacheFileUri() {
		// hex hex!
		return Uri.parse("content://at.tomtasche.reader/document.odt");
	}

	public static Uri getHtmlCacheFileUri() {
		// hex hex!
		return Uri.parse("content://at.tomtasche.reader/content.html");
	}

	public static void cleanup(Context context) {
		File cache = getCacheDirectory(context);
		String[] files = cache.list();
		if (files == null)
			return;

		for (String s : files) {
			try {
				if (!s.equals("document.odt")) {
					new File(cache, s).delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
