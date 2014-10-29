/*
* FullReader+
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
 * Copyright (C) 2010-2013 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.library;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.book.BookUtil;
import org.geometerplus.fbreader.formats.PluginCollection;
import org.geometerplus.fbreader.tree.FBTree;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImage;

import android.util.Log;

public class FileTree extends LibraryTree {
	private ZLFile myFile;
	private final String myName;
	private final String mySummary;
	private final boolean myIsSelectable;
	private boolean isRarArchive;
	private ArrayList<File> filesFromArchive = new ArrayList<File>();
	private FileTree mParent;
	FileTree(LibraryTree parent, ZLFile file, String name, String summary) {
		super(parent);
		myFile = file;
		myName = name;
		mySummary = summary;
		myIsSelectable = false;
	}

	public FileTree(FileTree parent, ZLFile file) {
		super(parent);
		myFile = file;
		myName = null;
		mySummary = null;
		myIsSelectable = true;
		mParent = parent;
	}

	@Override
	public String getName() {
		return myName != null ? myName : myFile.getShortName();
	}

	@Override
	public String getTreeTitle() {
		return myFile.getPath();
	}

	@Override
	protected String getStringId() {
		return myFile.getShortName();
	}

	@Override
	public String getSummary() {
		if (mySummary != null) {
			return mySummary;
		}

		final Book book = getBook();
		if (book != null) {
			return book.getTitle();
		}

		return null;
	}

	@Override
	public boolean isSelectable() {
		return myIsSelectable;
	}

	@Override
	public ZLImage createCover() {
		return BookUtil.getCover(getBook());
	}

	public ZLFile getFile() {
		return myFile;
	}

    
    public String getFileName(){
        return myFile.getPath();
    }

	
	private Object myBook;
	private static final Object NULL_BOOK = new Object();

	@Override
	public Book getBook() {
		if (myBook == null) {
			myBook = Collection.getBookByFile(myFile);
			if (myBook == null) {
				myBook = NULL_BOOK;
			}
		}
		return myBook instanceof Book ? (Book)myBook : null;
	}

	@Override
	public boolean containsBook(Book book) {
		if (book == null) {
			return false;
		}
		if (myFile.isDirectory()) {
			String prefix = myFile.getPath();
			if (!prefix.endsWith("/")) {
				prefix += "/";
			}
			return book.File.getPath().startsWith(prefix);
		} else if (myFile.isArchive()) {
			return book.File.getPath().startsWith(myFile.getPath() + ":");
		} else {
			return book.equals(getBook());
		}
	}

	@Override
	public Status getOpeningStatus() {
		//if (!myFile.isReadable()) {
		//	return Status.CANNOT_OPEN;
		//}
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public String getOpeningStatusMessage() {
		return getOpeningStatus() == Status.CANNOT_OPEN ? "permissionDenied" : null;
	}

	@Override
	public void waitForOpening() {
		if (getBook() != null) {
			return;
		}
		final TreeSet<ZLFile> set = new TreeSet<ZLFile>(ourFileComparator);
		for (ZLFile file : myFile.children()) {
			if(android.os.Build.VERSION.SDK_INT >= 11 ) {
				if (file.isDirectory() || file.isArchive() ||
						PluginCollection.Instance().getPlugin(file) != null 
						|| file.getExtension().equals("pdf") || file.getExtension().equals("djvu")
						|| file.getExtension().equals("cbz") || file.getExtension().equals("xps")
						|| file.getExtension().equals("docx") || file.getExtension().equals("odt")
						|| file.getExtension().endsWith("cbr") || file.getExtension().endsWith("rar")) {
						set.add(file);
					}
			} else {
				if (file.isDirectory() || file.isArchive() ||
						PluginCollection.Instance().getPlugin(file) != null 
						|| file.getExtension().equals("pdf") || file.getExtension().equals("djvu")
						|| file.getExtension().equals("cbz") || file.getExtension().equals("xps")
						||file.getExtension().endsWith("cbr") || file.getExtension().endsWith("rar")) {
						set.add(file);
					}
			}
			
		}
		clear();
		for (ZLFile file : set) {
			new FileTree(this, file);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof FileTree)) {
			return true;
		}
		return myFile.equals(((FileTree)o).myFile);
	}

	private static final Comparator<ZLFile> ourFileComparator = new Comparator<ZLFile>() {
		public int compare(ZLFile file0, ZLFile file1) {
			final boolean isDir = file0.isDirectory();
			if (isDir != file1.isDirectory()) {
				return isDir ? -1 : 1;
			}
			return file0.getShortName().compareToIgnoreCase(file1.getShortName());
		}
	};

	@Override
	public int compareTo(FBTree tree) {
		return ourFileComparator.compare(myFile, ((FileTree)tree).myFile);
	}
	
	
	public FileTree getParent(){
		return mParent;
	}
	
	public void setupAsRarArchive(){
		isRarArchive = true;
	}
	
	public boolean isRarArchive(){
		return isRarArchive;
	}
	
	public void addFileFromArchive(File file){
		filesFromArchive.add(file);
	}
	
	public void deleteUnrarFiles(){
		for (File f : filesFromArchive){
			try{
				boolean res = f.delete();
			}
			catch (Exception e){}
		}
		filesFromArchive.clear();
		isRarArchive = false;
	}
}
