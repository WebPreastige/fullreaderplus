/*
FullReader+
Copyright 2013-2014 Viktoriya Bilyk

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.webprestige.fr.formats.docx;

import java.io.File;
import java.util.ArrayList;


import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class DocViewActivity extends Activity {
/*implements CommentListener, NoteListener, HyperlinkListener, ProgressListener{

	OliveWordOperator viu;

	EditText searchEditText;
	ArrayList<String> bookmarks;
	Handler handler;
	String path;

	protected void onCreate(Bundle savedInstanceState) {
		
		//Log.d("LOG", path);
		viu = new OliveWordOperator(this, this);
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		   setProgressBarVisibility(true);
		   getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		setContentView(com.fullreader.R.layout.document_view_activity);
		OliveWordView view = (OliveWordView) findViewById(com.fullreader.R.id.test_view);

		try {			
			this.path = getIntent().getStringExtra(path);
			
			if(path != null) {
				Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();
				Log.d("PATH",path);
				viu.init(view, Uri.fromFile(new File(path)));
			} else {
				//viu.init(view, Uri.fromFile(new File("mnt/sdcard/test.docx")));
				Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();
				Log.d("PATH",path);
			}
			viu.start(viu.isEncrypted(), "111");
		} catch (Exception e) {
			e.printStackTrace();
		}
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
	            setProgress(msg.what * 10);
	            super.handleMessage(msg);
			}			
		};
	}

	@Override
	protected void onDestroy() {
		viu.release();
		super.onDestroy();
	}

	@Override
	public void getComment(ArrayList<String[]> comments) {
		for (int i = 0; i < comments.size(); i++) {
			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle(comments.get(i)[0]).setMessage(comments.get(i)[1])
					.show();
		}
	}

	@Override
	public void getHyperlink(String hyperlink) {
		if (Uri.parse(hyperlink).getScheme().contains("mailto")) {
			try {
				startActivity(new Intent(Intent.ACTION_SENDTO,
						Uri.parse(hyperlink)));
			} catch (ActivityNotFoundException anfe) {
				Toast.makeText(this, "can't found email application",
						Toast.LENGTH_SHORT).show();
			}
		} else {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(hyperlink)));
		}
	}

	@Override
	public void getNote(SparseArray<String> notes) {
		for (int i = 0; i < notes.size(); i++) {
			AlertDialog.Builder builder = new Builder(this);
			if (notes.keyAt(i) == NoteListener.FOOTNOTE) {
				builder.setTitle("footnote").setMessage(notes.valueAt(i))
						.show();
			} else if (notes.keyAt(i) == NoteListener.ENDNOTE) {
				builder.setTitle("endnote").setMessage(notes.valueAt(i)).show();
			}
		}
	}
	
	public void goToBookmarks(String name) {
		viu.goToBookmark(name);
	}

	public void listBookmarks() {
		this.bookmarks = viu.listBookmarks();
	}
	
	@Override
	public void notifyProgress(int progress) {
		handler.sendEmptyMessage(progress);
	}*/
}
