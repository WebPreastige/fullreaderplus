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
package com.webprestige.fr.otherdocs;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.geometerplus.fbreader.book.Book;

public class FrDocument {
	
	public static final int DOCTYPE_DJVU = 1;
	public static final int DOCTYPE_MUPDF = 2;
	public static final int DOCTYPE_CBR = 3;
	public static final int DOCTYPE_DOCX = 4;
	public static final int DOCTYPE_ODT = 5;
	public static final int DOCTYPE_FB = 6;
	
	private int mId;
	private String mName;
	private String mLocation;
	private int mDoctype;
	private String mLastDate;
	private Book mBook;
	private File mFile;
	
	public FrDocument(int id, String name, String location, int doctype, String lastDate){
		mId = id;
		mName = name;
		mLocation = location;
		mDoctype = doctype;
		mLastDate = lastDate;
		if (location.contains("file://")){
			String newLoc = location.substring(7, location.length());
			mFile = new File(newLoc);
		}
		else mFile = new File(location);
	}
	
	public int getId(){
		return mId;
	}
	
	public String getName(){
		return mName;
	}
	
	public String getLocation(){
		return mLocation;
	}
	
	public int getDoctype(){
		return mDoctype;
	}
	
	public String getLastDate(){
		return mLastDate;
	}
	
	public void updateDate(String newDate){
		mLastDate = newDate;
	}
	
	public void updateId(int id){
		mId = id;
	}
	
	public static String getDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss", Locale.US);
		return sdf.format(new Date());
	}
	
	public void setupBook(Book book){
		mBook = book;
	}
	
	public Book getBook(){
		return mBook;
	}
	
	public File getFile(){
		return mFile;
	}
}
