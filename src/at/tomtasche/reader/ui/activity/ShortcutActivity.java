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
package at.tomtasche.reader.ui.activity;

import com.fullreader.R;

import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import at.tomtasche.reader.background.DocumentLoader;
import at.tomtasche.reader.ui.widget.RecentDocumentDialogFragment;

public class ShortcutActivity extends FragmentActivity implements
		DocumentLoadingActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		transaction.add(new RecentDocumentDialogFragment(), "chooser");
		transaction.commit();
	}

	@Override
	public DocumentLoader loadUri(Uri uri) {
		ShortcutIconResource icon = Intent.ShortcutIconResource.fromContext(
				this, R.drawable.icon);

		Intent intent = new Intent();

		Intent launchIntent = new Intent(this, MainActivity.class);
		launchIntent.setAction(Intent.ACTION_VIEW);
		launchIntent.setData(uri);

		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, uri.getLastPathSegment());
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

		setResult(RESULT_OK, intent);

		finish();

		return null;
	}
}
