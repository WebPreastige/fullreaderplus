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

package at.tomtasche.reader.ui.widget;

import com.fullreader.R;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


@SuppressLint("ValidFragment")
public class ProgressDialogFragment extends DialogFragment {

	public static final String FRAGMENT_TAG = "progress_dialog";

	private ProgressDialog progressDialog;
	private boolean upload;

	public ProgressDialogFragment() {
		this(false);
	}

	public ProgressDialogFragment(boolean upload) {
		this.upload = upload;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		progressDialog = new ProgressDialog(getActivity());

		int title;
		if (upload) {
			//title = R.string.dialog_uploading_title;
		} else {
			//title = R.string.dialog_loading_title;
		}
		//progressDialog.setTitle(getString("Loading"));
		progressDialog.setTitle("Loading");
		//progressDialog.setMessage(getString(R.string.dialog_loading_message));
		progressDialog.setCancelable(false);
		progressDialog.setIndeterminate(upload);
		if (!upload) {
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(100);
			progressDialog.setProgress(0);
		}

		setCancelable(false);

		return progressDialog;
	}

	public void setProgress(double progress) {
		if (progressDialog != null)
			progressDialog.setProgress(((int) (progress * 100)));
	}

	// another dirty hack for a nullpointerexception thrown sometimes on dismiss()
	public boolean isNotNull() {
		return progressDialog != null;
	}
}
