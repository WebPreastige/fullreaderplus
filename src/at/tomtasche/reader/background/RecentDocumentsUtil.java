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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.net.Uri;

// TODO: rewrite this class. it's a fucking mess!
public class RecentDocumentsUtil {

	private static final String FILENAME = "recent_documents";

	public static Map<String, String> getRecentDocuments(Context context)
			throws IOException {
		Map<String, String> result = new HashMap<String, String>();

		FileInputStream input = null;
		InputStreamReader reader = null;
		BufferedReader bufferedReader = null;
		try {
			input = context.openFileInput(FILENAME);

			reader = new InputStreamReader(input);
			bufferedReader = new BufferedReader(reader);
			for (String s = bufferedReader.readLine(); s != null; s = bufferedReader
					.readLine()) {
				String[] temp = s.split(";;;");
				if (temp.length == 2) {
					if (temp[0].length() == 0 || temp[1].length() == 0) {
						continue;
					}

					result.put(temp[0], temp[1]);
				}
			}
		} finally {
			if (input != null)
				input.close();
			if (reader != null)
				reader.close();
			if (bufferedReader != null)
				bufferedReader.close();
		}

		return result;
	}

	public static void addRecentDocument(Context context, String title, Uri uri)
			throws IOException {
		if (title == null)
			return;

		FileOutputStream output = null;
		OutputStreamWriter writer = null;
		try {
			output = context.openFileOutput(FILENAME, Context.MODE_APPEND);
			writer = new OutputStreamWriter(output);
			writer.append(System.getProperty("line.separator") + title + ";;;"
					+ uri.toString());
			writer.flush();
		} finally {
			if (output != null)
				output.close();
			if (writer != null)
				writer.close();
		}
	}

	public static void removeRecentDocument(Context context, String title)
			throws IOException {
		if (title == null)
			return;

		FileOutputStream output = null;
		OutputStreamWriter writer = null;

		InputStreamReader reader = null;
		BufferedReader bufferedReader = null;

		try {
			reader = new InputStreamReader(context.openFileInput(FILENAME));
			bufferedReader = new BufferedReader(reader);

			output = context.openFileOutput(FILENAME, 0);
			writer = new OutputStreamWriter(output);

			for (String s = bufferedReader.readLine(); s != null; s = bufferedReader
					.readLine()) {
				if (s.contains(title)) {
					continue;
				} else {
					writer.append(System.getProperty("line.separator") + s);
				}
			}
		} finally {
			if (reader != null)
				reader.close();
			if (bufferedReader != null)
				bufferedReader.close();

			if (writer != null)
				writer.close();
		}
	}
}
