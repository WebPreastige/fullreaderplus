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
package com.webprestige.fr.customlistview;

import java.io.File;
import java.util.ArrayList;

import org.geometerplus.android.fbreader.BaseActivity;
import org.geometerplus.android.fbreader.FullReaderActivity;
import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.library.BookInfoActivity;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.SerializerUtil;
import org.geometerplus.fbreader.bookmodel.BookReadingException;
import org.geometerplus.fbreader.library.LibraryTree;
import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.artifex.mupdf.MuPDFActivity;
import com.webprestige.fr.bookmarks.DatabaseHandler;
import com.fullreader.R;

public class AllFilesActivity extends BaseActivity{
	private ListView allFilesList;
	private AllFilesAdapter listAdapter;
	public static ArrayList<MyFile> files;
	private static final int BOOK_INFO_REQUEST = 1;
	public static Book book;
	private DatabaseHandler db;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.all_files_activity);
		db = new DatabaseHandler(this);
		initFilesArray();
		
		allFilesList = (ListView)findViewById(R.id.all_files_list);
		allFilesList.setCacheColorHint(Color.TRANSPARENT);
		initMyFilesList();
		allFilesList.setClickable(true);
		allFilesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Intent newIntent = new Intent();
				//final LibraryTree tree = (LibraryTree)listAdapter.getItem(position);
				if(files.get(position).getFileTitle().contains(".pdf") || files.get(position).getFileTitle().contains(".xps") || files.get(position).getFileTitle().contains(".cbz")) {
					Uri uri = Uri.parse(files.get(position).getFilePath());
					newIntent.setClass(AllFilesActivity.this, MuPDFActivity.class);
					newIntent.setAction(Intent.ACTION_VIEW);
	    			newIntent.setData(uri);	
	    			startActivity(newIntent);
				} else {
					FullReaderActivity.isCreateFromMyFilesBook = true;
					FullReaderActivity.myFileOpenedBookPath = files.get(position).getFilePath();
					ZLFile zlFile = ZLFile.createFileByPath(files.get(position).getFilePath());					
					try {
						AllFilesActivity.book = new Book(zlFile);
						book.setId(100);
						showBookInfo(book);
					} catch (BookReadingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
				}				
			}		
		});
	}
	
	private void showBookInfo(Book book) {
		String s = SerializerUtil.serialize(book);
		Log.d("serialized book: ", s);
		OrientationUtil.startActivityForResult(
			this,
			new Intent(getApplicationContext(), BookInfoActivity.class)
				.putExtra(FullReaderActivity.BOOK_KEY, SerializerUtil.serialize(book)), BOOK_INFO_REQUEST);
	}
	
	private void initMyFilesList() {
		listAdapter = new AllFilesAdapter(getApplicationContext(), files);
		allFilesList.setAdapter(listAdapter);	
	}
	
	private void initFilesArray() {
		files = new ArrayList<MyFile>();
		File file[] = Environment.getExternalStorageDirectory().listFiles();
		recursiveDisplayReadebleFiles(file);
		//files = db.getAllMyFiles();
		Log.d("files", String.valueOf(db.getAllMyFiles().size()));
	}
	
	public void recursiveDisplayReadebleFiles(File[] file1){
		int i = 0;
		String filePath="";
		String fileName="";
		if(file1!=null){
			while(i!=file1.length){
			    filePath = file1[i].getAbsolutePath();
			    fileName = file1[i].getName();
		        if(file1[i].isDirectory()){
		            File file[] = file1[i].listFiles();
		            recursiveDisplayReadebleFiles(file);
		        }
			    i++;
			    if(filePath.endsWith(".fb2")   ||
		    		filePath.endsWith(".epub") ||
		    		filePath.endsWith(".mobi") ||
		    		filePath.endsWith(".txt")  ||
		    		filePath.endsWith(".doc")  ||
		    		filePath.endsWith("djvu")  ||
		    		filePath.endsWith(".cbz")  ||
		    		filePath.endsWith(".pdf")  ||
		    		filePath.endsWith(".html")) {
			    	Log.d(i+"", filePath);
			    	
			    	files.add(new MyFile(fileName, filePath, 0));
			    	//db.addMyFile(new MyFile(fileName, filePath, i));
			    }
		    }
		}
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.book = null;
	}
}
