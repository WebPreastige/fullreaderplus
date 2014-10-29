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
package com.webprestige.fr.bookmarks;

import java.util.ArrayList;
import java.util.List;

import org.geometerplus.android.fbreader.SelectedMarkInfo;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.text.view.ZLTextFixedPosition;
import org.geometerplus.zlibrary.text.view.ZLTextPosition;

import com.webprestige.fr.citations.MyQuote;
import com.webprestige.fr.customlistview.MyFile;
import com.webprestige.fr.otherdocs.FrDocument;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper{

	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 3;
 
    // Database Name
    private static final String DATABASE_NAME = "BookmarksDB";
 
    // Contacts table name
    private static final String TABLE_BOOKMARKS = "bookmarks";
    private static final String TABLE_QUOTES = "quotes";
    private static final String TABLE_MYFILES = "myfiles";
    private static final String TABLE_COLOR_MARKS = "color_marks";
    private static final String TABLE_FR_DOCUMENTS = "fr_documents";
    // Contacts Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_BOOK_ID = "bookID";
    private static final String KEY_P_INDEX = "pIndex";
    private static final String KEY_E_INDEX = "eIndex";
    private static final String KEY_C_INDEX = "cIndex";
    private static final String KEY_CREATION_DATE = "creationDate";
    private static final String KEY_TITLE = "title";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_MYFILE_PATH = "path";
    private static final String KEY_ISFROMMYFILE = "isfrommyfile";
    
    private static final String Q_KEY_ID = "id";
    private static final String Q_KEY_BOOK_ID = "bookID";
    private static final String Q_KEY_P_INDEX = "pIndex";
    private static final String Q_KEY_E_INDEX = "eIndex";
    private static final String Q_KEY_C_INDEX = "cIndex";
    private static final String Q_KEY_CREATION_DATE = "creationDate";
    private static final String Q_KEY_TITLE = "title";
    private static final String Q_KEY_AUTHOR = "author";
    private static final String Q_KEY_QUOTE_TEXT = "quoteText";
    private static final String Q_KEY_MYFILE_PATH = "path";
    private static final String Q_KEY_ISFROMMYFILE = "isfrommyfile";
    private static final String Q_KEY_COLOR = "quoteColor";
    
    private static final String M_COLOR_MARK_ID = "_id";
    private static final String M_COLOR_MARK_QUOTE_ID = "color_mark_quote_id";
    private static final String M_COLOR_MARK_BOOK_ID = "color_mark_cook_id";
    private static final String M_COLOR_MARK_START_P_INDEX = "color_mark_start_p_index";
    private static final String M_COLOR_MARK_START_E_INDEX = "color_mark_start_e_index";
    private static final String M_COLOR_MARK_START_C_INDEX = "color_mark_start_c_index";
    private static final String M_COLOR_MARK_END_P_INDEX = "color_mark_end_p_index";
    private static final String M_COLOR_MARK_END_E_INDEX = "color_mark_end_e_index";
    private static final String M_COLOR_MARK_END_C_INDEX = "color_mark_end_c_index";
    private static final String M_COLOR_MARK_KEY_COLOR = "color_mark_color";
    
    private static final String MYFILE_ID = "id";
    private static final String MYFILE_PATH = "myfilePath";
    private static final String MYFILE_NAME = "myfileName";
    
    private static final String FR_DOCUMENT_ID = "_id";
    private static final String FR_DOCUMENT_NAME = "fr_document_name";
    private static final String FR_DOCUMENT_LOCATION = "fr_document_location";
    private static final String FR_DOCUMENT_DOCTYPE = "fr_document_doctype";
    private static final String FR_DOCUMENT_LASTDATE = "fr_document_lastdate";
    
    private static final int FR_DOCUMENT_MAX_COUNT = 16;
    
    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
 
    //Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BOOKMARKS_TABLE = "CREATE TABLE " + TABLE_BOOKMARKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," 
                + KEY_BOOK_ID + " INTEGER,"
        		+ KEY_P_INDEX + " INTEGER,"
                + KEY_E_INDEX + " INTEGER," 
                + KEY_C_INDEX + " INTEGER,"
                + KEY_CREATION_DATE + " TEXT,"
                + KEY_TITLE + " TEXT,"
                + KEY_AUTHOR + " TEXT,"
                + KEY_MYFILE_PATH + " TEXT,"
                + KEY_ISFROMMYFILE + " INTEGER"
                +")";
        db.execSQL(CREATE_BOOKMARKS_TABLE);
        
        String CREATE_QUOTES_TABLE = "CREATE TABLE " + TABLE_QUOTES + "("
                + Q_KEY_ID + " INTEGER PRIMARY KEY," 
                + Q_KEY_BOOK_ID + " INTEGER,"
        		+ Q_KEY_P_INDEX + " INTEGER,"
                + Q_KEY_E_INDEX + " INTEGER," 
                + Q_KEY_C_INDEX + " INTEGER,"
                + Q_KEY_CREATION_DATE + " TEXT,"
                + Q_KEY_TITLE + " TEXT,"
                + Q_KEY_AUTHOR + " TEXT,"
                + Q_KEY_QUOTE_TEXT + " TEXT,"
                + Q_KEY_MYFILE_PATH + " TEXT,"
                + Q_KEY_ISFROMMYFILE + " INTEGER,"
                + Q_KEY_COLOR + " TEXT"
                +")";
        db.execSQL(CREATE_QUOTES_TABLE);
        
        String CREATE_COLOR_MARKS_TABLE = "CREATE TABLE " + TABLE_COLOR_MARKS + "("
        		+ M_COLOR_MARK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        		+ M_COLOR_MARK_QUOTE_ID + " INTEGER, "
        		+ M_COLOR_MARK_BOOK_ID + " INTEGER, "
        		+ M_COLOR_MARK_START_P_INDEX + " INTEGER, "
        		+ M_COLOR_MARK_START_E_INDEX + " INTEGER, "
        		+ M_COLOR_MARK_START_C_INDEX + " INTEGER, "
        		+ M_COLOR_MARK_END_P_INDEX + " INTEGER, "
        		+ M_COLOR_MARK_END_E_INDEX + " INTEGER, "
        		+ M_COLOR_MARK_END_C_INDEX + " INTEGER, "
        		+ M_COLOR_MARK_KEY_COLOR + " TEXT"
        		+ ")";
        db.execSQL(CREATE_COLOR_MARKS_TABLE);
        
        String CREATE_FR_DOCUMENTS_TABLE = "CREATE TABLE " + TABLE_FR_DOCUMENTS + "("
        		+ FR_DOCUMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        		+ FR_DOCUMENT_NAME + " TEXT, "
        		+ FR_DOCUMENT_LOCATION + " TEXT, "
        		+ FR_DOCUMENT_DOCTYPE + " INTEGER, "
        		+ FR_DOCUMENT_LASTDATE + " TEXT"
        		+ ")";
        db.execSQL(CREATE_FR_DOCUMENTS_TABLE);
        
        String CREATE_MYFILES_TABLE = "CREATE TABLE " + TABLE_MYFILES + "("
                + MYFILE_ID + " INTEGER PRIMARY KEY," 
                + MYFILE_PATH + " TEXT,"
                + MYFILE_NAME + " TEXT"
                +")";
        db.execSQL(CREATE_MYFILES_TABLE);
    }
    
    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKMARKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MYFILES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLOR_MARKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FR_DOCUMENTS);
        // Create tables again
        onCreate(db);
    }

    // Adding new bookmark
    public void addBookmark(MyBookmark bookmark) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	 
        ContentValues values = new ContentValues();
        values.put(KEY_BOOK_ID, bookmark.getBookID());
        values.put(KEY_P_INDEX, bookmark.getParagraphIndex());
        values.put(KEY_E_INDEX, bookmark.getElementIndex());
        values.put(KEY_C_INDEX, bookmark.getCharIndex());
        values.put(KEY_CREATION_DATE, bookmark.getCreationTime());
       // values.put(KEY_TITLE, bookmark.getBookTitle()+bookmark.getBookAuthror());
        values.put(KEY_TITLE, bookmark.getBookTitle());
        values.put(KEY_AUTHOR, bookmark.getBookAuthror());
        values.put(KEY_MYFILE_PATH, bookmark.getPathToBook());
        values.put(KEY_ISFROMMYFILE, bookmark.getIsFromMyFile());
        // Inserting Row
        db.insert(TABLE_BOOKMARKS, null, values);
        db.close(); 
    }
    
 // Adding new bookmark
    public long addQuote(MyQuote quote) {
    	SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Q_KEY_BOOK_ID, quote.getBookID());
        values.put(Q_KEY_P_INDEX, quote.getParagraphIndex());
        values.put(Q_KEY_E_INDEX, quote.getElementIndex());
        values.put(Q_KEY_C_INDEX, quote.getCharIndex());
        values.put(Q_KEY_CREATION_DATE, quote.getCreationTime());
       // values.put(KEY_TITLE, bookmark.getBookTitle()+bookmark.getBookAuthror());
        values.put(Q_KEY_TITLE, quote.getBookTitle());
        values.put(Q_KEY_AUTHOR, quote.getBookAuthror());
        values.put(Q_KEY_QUOTE_TEXT, quote.getText());
        values.put(Q_KEY_MYFILE_PATH, quote.getPathToBook());
        values.put(Q_KEY_ISFROMMYFILE, quote.getIsFromMyFile());
        if (!quote.getColor().equals("-1")){
        	int color = Integer.parseInt(quote.getColor());
	        //String hexStr = Integer.toHexString(color);
        	String hexStr = String.format("#%06X", (0xFFFFFF & color));
	        values.put(Q_KEY_COLOR, hexStr);
        }
        else values.put(Q_KEY_COLOR, quote.getColor());
        // Inserting Row
        long quoteId = db.insert(TABLE_QUOTES, null, values);
        db.close();
        return quoteId;
    }
    
    // Добавление инфы о пометке
    public void addColorMark(SelectedMarkInfo info, long bookId){
    	SQLiteDatabase db = this.getWritableDatabase();
    	ContentValues cv = new ContentValues();
    	cv.put(M_COLOR_MARK_QUOTE_ID, info.quoteId);
    	cv.put(M_COLOR_MARK_BOOK_ID, bookId);
    	cv.put(M_COLOR_MARK_START_P_INDEX, info.startCursor.getParagraphIndex());
    	cv.put(M_COLOR_MARK_START_E_INDEX, info.startCursor.getElementIndex());
    	cv.put(M_COLOR_MARK_START_C_INDEX, info.startCursor.getCharIndex());
    	cv.put(M_COLOR_MARK_END_P_INDEX, info.endCursor.getParagraphIndex());
    	cv.put(M_COLOR_MARK_END_E_INDEX, info.endCursor.getElementIndex());
    	cv.put(M_COLOR_MARK_END_C_INDEX, info.endCursor.getCharIndex());
        cv.put(M_COLOR_MARK_KEY_COLOR, String.valueOf(info.color.getIntValue()));
        db.insert(TABLE_COLOR_MARKS, null, cv);
    }
    
    public long addFrDocument(FrDocument document){
    	removeLastFrDocument();
    	SQLiteDatabase db = this.getWritableDatabase();
    	ContentValues cv = new ContentValues();
    	cv.put(FR_DOCUMENT_NAME, document.getName());
    	cv.put(FR_DOCUMENT_LOCATION, document.getLocation());
    	cv.put(FR_DOCUMENT_DOCTYPE, document.getDoctype());
    	cv.put(FR_DOCUMENT_LASTDATE, document.getLastDate());
    	Log.d("MyLog", "Adding document - " + document.getName() + " Doc location - " 
    			+	document.getLocation());
    	return db.insert(TABLE_FR_DOCUMENTS, null, cv);
    }
    
    private void removeLastFrDocument(){
    	String query = "SELECT * FROM " + TABLE_FR_DOCUMENTS;
    	SQLiteDatabase db = this.getWritableDatabase();
    	Cursor cursor = db.rawQuery(query, null);
    	if (cursor.getColumnCount() >= FR_DOCUMENT_MAX_COUNT){
    		int idIndex = cursor.getColumnIndex(FR_DOCUMENT_ID);
    		cursor.moveToLast();
    		long id = cursor.getLong(idIndex);
    		String whereClause = FR_DOCUMENT_ID + " =?";
    		String [] whereArgs = new String []{String.valueOf(id)};
    		db.delete(TABLE_FR_DOCUMENTS, whereClause, whereArgs);
    	}
    }
    
    public void deleteFrDocument(FrDocument document){
    	String where = FR_DOCUMENT_ID + " =?";
    	String [] whereArgs = new String [] {String.valueOf(document.getId())};
    	SQLiteDatabase db = this.getWritableDatabase();
    	db.delete(TABLE_FR_DOCUMENTS, where, whereArgs);
    }
    
    public void addMyFile(MyFile file) {
    	SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MYFILE_ID, file.getFileId());
        values.put(MYFILE_PATH, file.getFilePath());
        values.put(MYFILE_NAME, file.getFileTitle());
        db.insert(TABLE_MYFILES, null, values);
        db.close(); 
    }
    
    public MyFile getMyFile(int id) {
    	SQLiteDatabase db = this.getReadableDatabase();
   	 
        Cursor cursor = db.query(TABLE_MYFILES, new String[] { 
        		MYFILE_ID,
        		MYFILE_PATH,
        		MYFILE_NAME
                }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        MyFile fileToReturn = new MyFile(cursor.getString(2), cursor.getString(1), cursor.getInt(0));
        return fileToReturn;
    }
     
    // Getting single bookmark
    public MyBookmark getBookmark(int id) {
    	SQLiteDatabase db = this.getReadableDatabase();
    	 
        Cursor cursor = db.query(TABLE_BOOKMARKS, new String[] { 
        		KEY_ID,
        		KEY_BOOK_ID,
                KEY_P_INDEX,
                KEY_E_INDEX,
                KEY_C_INDEX,
                KEY_CREATION_DATE,
                KEY_TITLE,
                KEY_AUTHOR,
                KEY_MYFILE_PATH,
                KEY_ISFROMMYFILE
                }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
     
        MyBookmark bookmarkToReturn = new MyBookmark(
        								cursor.getInt(0),
						        		cursor.getInt(2),
						        		cursor.getInt(3), 
						        		cursor.getInt(4), 
						        		cursor.getString(6), 
						        		cursor.getString(7), 
						        		cursor.getInt(1), 
						        		cursor.getString(5),
						        		cursor.getString(8),
						        		cursor.getInt(9));
        return bookmarkToReturn;
    }
    
    
    // Getting single bookmark
    public MyQuote getQuote(int id) {
    	SQLiteDatabase db = this.getReadableDatabase();
    	 
        Cursor cursor = db.query(TABLE_QUOTES, new String[] { 
        		Q_KEY_ID,
        		Q_KEY_BOOK_ID,
                Q_KEY_P_INDEX,
                Q_KEY_E_INDEX,
                Q_KEY_C_INDEX,
                Q_KEY_CREATION_DATE,
                Q_KEY_TITLE,
                Q_KEY_AUTHOR,
                Q_KEY_QUOTE_TEXT,
                Q_KEY_MYFILE_PATH,
                Q_KEY_ISFROMMYFILE,
                Q_KEY_COLOR
                }, KEY_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        
        MyQuote quoteToReturn = new MyQuote(
        								cursor.getString(8),
        								cursor.getInt(0),
						        		cursor.getInt(2),
						        		cursor.getInt(3), 
						        		cursor.getInt(4), 
						        		cursor.getString(6), 
						        		cursor.getString(7), 
						        		cursor.getInt(1), 
						        		cursor.getString(5),
						        		cursor.getString(cursor.getColumnIndex(Q_KEY_MYFILE_PATH)), 
						        		cursor.getInt(cursor.getColumnIndex(Q_KEY_ISFROMMYFILE)),
						        		cursor.getString(cursor.getColumnIndex(Q_KEY_ISFROMMYFILE)));
        return quoteToReturn;
    }
    
     
    // Getting all bookmarks
    public ArrayList<MyBookmark> getAllBookmarks() {
    	 ArrayList<MyBookmark> bookmarksList = new ArrayList<MyBookmark>();
    	    String selectQuery = "SELECT  * FROM " + TABLE_BOOKMARKS;
    	    SQLiteDatabase db = this.getWritableDatabase();
    	    Cursor cursor = db.rawQuery(selectQuery, null);   	 
    	    if (cursor.moveToFirst()) {
    	        do {   	   
    	            MyBookmark bookmark = new MyBookmark(cursor.getInt(0), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(6),  cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)), cursor.getInt(1), cursor.getString(5), cursor.getString(cursor.getColumnIndex(KEY_MYFILE_PATH)), cursor.getInt(cursor.getColumnIndex(KEY_ISFROMMYFILE)));
    	            bookmarksList.add(bookmark);
    	        } while (cursor.moveToNext());
    	    }
    	    // return bookmarks list
    	    return bookmarksList;
    }
    
    // Getting all quotes
    public ArrayList<MyQuote> getAllQuotes() {
	 	ArrayList<MyQuote> quotesList = new ArrayList<MyQuote>();
	    String selectQuery = "SELECT  * FROM " + TABLE_QUOTES;
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);   	 
	    if (cursor.moveToFirst()) {
	        do {   	   
	        	MyQuote quote = new MyQuote(cursor.getString(8), cursor.getInt(0), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(6),  cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)), cursor.getInt(1), cursor.getString(5), cursor.getString(cursor.getColumnIndex(KEY_MYFILE_PATH)), cursor.getInt(cursor.getColumnIndex(KEY_ISFROMMYFILE)),cursor.getString(cursor.getColumnIndex(Q_KEY_COLOR)));
	        	quotesList.add(quote);
	        } while (cursor.moveToNext());
	    }
	    // return quotes list
	    return quotesList;
    }
    
    public ArrayList<MyQuote> getAllQuotesWithFilter(boolean showColorMarks) {
      	 ArrayList<MyQuote> quotesList = new ArrayList<MyQuote>();
      	    String selectQuery = "SELECT  * FROM " + TABLE_QUOTES;
      	    SQLiteDatabase db = this.getWritableDatabase();
      	    Cursor cursor = db.rawQuery(selectQuery, null);   	 
      	    if (cursor.moveToFirst()) {
      	        do {   	   
      	        	MyQuote quote = new MyQuote(cursor.getString(8), cursor.getInt(0), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(6),  cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)), cursor.getInt(1), cursor.getString(5), cursor.getString(cursor.getColumnIndex(KEY_MYFILE_PATH)), cursor.getInt(cursor.getColumnIndex(KEY_ISFROMMYFILE)),cursor.getString(cursor.getColumnIndex(Q_KEY_COLOR)));
      	        	if (showColorMarks && !quote.getColor().equals("-1")){
      	        		quotesList.add(quote);
      	        	}
      	        	else if (!showColorMarks && quote.getColor().equals("-1")) {
      	        		quotesList.add(quote);
      	        	}
      	        } while (cursor.moveToNext());
      	    }
      	    for (MyQuote qt : quotesList){
      	    	Log.d("MyLog", "Quote text - " + qt.getText());
      	    }
      	    // return quotes list
      	    return quotesList;
      }
    
    public long hasFrDocument(FrDocument frDocument){
    	String where = FR_DOCUMENT_NAME + " =? AND " + FR_DOCUMENT_LOCATION + " =? ";
    	String [] whereArgs = new String[] {frDocument.getName(), frDocument.getLocation()};
    	SQLiteDatabase db = this.getWritableDatabase();
    	Cursor cursor = db.query(TABLE_FR_DOCUMENTS, null, where, whereArgs, null, null, null);
    	if (cursor.getCount()>0){
    		int idIndex = cursor.getColumnIndex(FR_DOCUMENT_ID);
    		cursor.moveToFirst();
    		return cursor.getLong(idIndex);
    	}
    	else return -1;
    }
    
    public void updateFrDocumentLastDate(FrDocument document){
    	String where = FR_DOCUMENT_ID + " =?";
    	String [] whereArgs = new String[] {String.valueOf(document.getId())};
    	SQLiteDatabase db = this.getWritableDatabase();
    	ContentValues cv = new ContentValues();
    	cv.put(FR_DOCUMENT_LASTDATE, document.getLastDate());
    	db.update(TABLE_FR_DOCUMENTS, cv, where, whereArgs);
    	Log.d("MyLog", "Updating date - " + document.getName());
    }
    
    public void deleteFrDocumentAfterRename(String oldLocation){
    	SQLiteDatabase db = this.getWritableDatabase();
    	String whereClause = FR_DOCUMENT_LOCATION + " =?";
    	String [] whereArgs = new String [] {oldLocation};
    	int res = db.delete(TABLE_FR_DOCUMENTS, whereClause, whereArgs);
    }
    
    public ArrayList<FrDocument> getAllFrDocuments(){
    	ArrayList<FrDocument> frDocsList = new ArrayList<FrDocument>();
    	String selectQuery = "SELECT * FROM " + TABLE_FR_DOCUMENTS + " ORDER BY " + FR_DOCUMENT_LASTDATE + " DESC";
    	SQLiteDatabase db = this.getWritableDatabase();
    	Cursor cursor = db.rawQuery(selectQuery, null);
    	if (cursor.getCount()>0){
    		int idIndex = cursor.getColumnIndex(FR_DOCUMENT_ID);
    		int nmIndex = cursor.getColumnIndex(FR_DOCUMENT_NAME);
    		int locIndex = cursor.getColumnIndex(FR_DOCUMENT_LOCATION);
    		int doctypeIndex = cursor.getColumnIndex(FR_DOCUMENT_DOCTYPE);
    		int lDateIndex = cursor.getColumnIndex(FR_DOCUMENT_LASTDATE);
    		cursor.moveToFirst();
    		do{
    			frDocsList.add(new FrDocument(cursor.getInt(idIndex),
    					cursor.getString(nmIndex),
    					cursor.getString(locIndex),
    					cursor.getInt(doctypeIndex),
    					cursor.getString(lDateIndex)));
    		}
    		while(cursor.moveToNext());
    	}
    	return frDocsList;
    }
    
    // Получить список выделенных цветом цитат для определенной книги
    public ArrayList<SelectedMarkInfo> getColorMarksForBook(long bookId){
    	String where = M_COLOR_MARK_BOOK_ID + " =?";
    	String [] whereArgs = new String[]{String.valueOf(bookId)};
    	SQLiteDatabase db = this.getWritableDatabase();
    	Cursor cursor = db.query(TABLE_COLOR_MARKS, null, where, whereArgs, null, null, null);
    	if(cursor.getCount()>0){
    		ArrayList<SelectedMarkInfo> infoList = new ArrayList<SelectedMarkInfo>();
    		ZLColor color;
    		ZLTextPosition startPosition;
    		ZLTextPosition endPosition;
    		long quoteId;
    		SelectedMarkInfo info;
    		cursor.moveToFirst();
    		int qIDIndex = cursor.getColumnIndex(M_COLOR_MARK_QUOTE_ID);
    		int sPIndex = cursor.getColumnIndex(M_COLOR_MARK_START_P_INDEX);
			int sEIndex = cursor.getColumnIndex(M_COLOR_MARK_START_E_INDEX);
			int sCIndex = cursor.getColumnIndex(M_COLOR_MARK_START_C_INDEX);
			int ePIndex = cursor.getColumnIndex(M_COLOR_MARK_END_P_INDEX);
			int eEIndex = cursor.getColumnIndex(M_COLOR_MARK_END_E_INDEX);
			int eCIndex = cursor.getColumnIndex(M_COLOR_MARK_END_C_INDEX);
			int zCIndex = cursor.getColumnIndex(M_COLOR_MARK_KEY_COLOR);
			do{
				quoteId = cursor.getLong(qIDIndex);
				startPosition = new ZLTextFixedPosition(cursor.getInt(sPIndex), cursor.getInt(sEIndex), cursor.getInt(sCIndex));
				endPosition = new ZLTextFixedPosition(cursor.getInt(ePIndex), cursor.getInt(eEIndex), cursor.getInt(eCIndex));
				color = new ZLColor(Integer.parseInt(cursor.getString(zCIndex)));
				info = new SelectedMarkInfo(quoteId, color, startPosition, endPosition);
				infoList.add(info);
			}
			while(cursor.moveToNext());
    		return infoList;
    	}
    	return null;
    }
    
 // Получить список выделенных цветом цитат для определенной книги
    public SelectedMarkInfo getColorMarkForQuote(long qId){
    	Log.d("MyLog", "Get color mark for quote id - " + String.valueOf(qId));
    	String where = M_COLOR_MARK_QUOTE_ID + " =?";
    	String [] whereArgs = new String[]{String.valueOf(qId)};
    	SQLiteDatabase db = this.getWritableDatabase();
    	Cursor cursor = db.query(TABLE_COLOR_MARKS, null, where, whereArgs, null, null, null);
    	
    	if(cursor.getCount()>0){
    		Log.d("MyLog", "Cursor get count - " + String.valueOf(cursor.getCount()));
    		ZLColor color;
    		ZLTextPosition startPosition;
    		ZLTextPosition endPosition;
    		long quoteId;
    		SelectedMarkInfo info;
    		cursor.moveToFirst();
    		int qIDIndex = cursor.getColumnIndex(M_COLOR_MARK_QUOTE_ID);
    		int sPIndex = cursor.getColumnIndex(M_COLOR_MARK_START_P_INDEX);
			int sEIndex = cursor.getColumnIndex(M_COLOR_MARK_START_E_INDEX);
			int sCIndex = cursor.getColumnIndex(M_COLOR_MARK_START_C_INDEX);
			int ePIndex = cursor.getColumnIndex(M_COLOR_MARK_END_P_INDEX);
			int eEIndex = cursor.getColumnIndex(M_COLOR_MARK_END_E_INDEX);
			int eCIndex = cursor.getColumnIndex(M_COLOR_MARK_END_C_INDEX);
			int zCIndex = cursor.getColumnIndex(M_COLOR_MARK_KEY_COLOR);

				quoteId = cursor.getLong(qIDIndex);
				startPosition = new ZLTextFixedPosition(cursor.getInt(sPIndex), cursor.getInt(sEIndex), cursor.getInt(sCIndex));
				endPosition = new ZLTextFixedPosition(cursor.getInt(ePIndex), cursor.getInt(eEIndex), cursor.getInt(eCIndex));
				color = new ZLColor(Integer.parseInt(cursor.getString(zCIndex)));
				info = new SelectedMarkInfo(quoteId, color, startPosition, endPosition);
				return info;
    	}
    	return null;
    }
    
    public ArrayList<MyFile> getAllMyFiles() {
   	 ArrayList<MyFile> myFilesList = new ArrayList<MyFile>();
   	    String selectQuery = "SELECT  * FROM " + TABLE_MYFILES;
   	    SQLiteDatabase db = this.getWritableDatabase();
   	    Cursor cursor = db.rawQuery(selectQuery, null);   	 
   	    if (cursor.moveToFirst()) {
   	        do {   	   
   	        	//MyQuote quote = new MyQuote(cursor.getString(8), cursor.getInt(0), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(6),  cursor.getString(cursor.getColumnIndex(KEY_AUTHOR)), cursor.getInt(1), cursor.getString(5));
   	        	//quotesList.add(quote);
   	        	MyFile file = new MyFile(cursor.getString(2), cursor.getString(1), cursor.getInt(0));
   	        	myFilesList.add(file);
   	        } while (cursor.moveToNext());
   	    }
   	    // return quotes list
   	    return myFilesList;
   }
    
    public int getBookmarksCount() {
    	 String countQuery = "SELECT  * FROM " + TABLE_BOOKMARKS;
         SQLiteDatabase db = this.getReadableDatabase();
         Cursor cursor = db.rawQuery(countQuery, null);
         cursor.close();
         // return count
         return cursor.getCount();
    }
    
    public int getQuoteCount() {
   	 String countQuery = "SELECT  * FROM " + TABLE_QUOTES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();
        // return count
        return cursor.getCount();
   }
    
    // Updating single bookmark
    public int updateBookmark(MyBookmark bookmark) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	 
        ContentValues values = new ContentValues();
        values.put(KEY_BOOK_ID, bookmark.getBookID());
        values.put(KEY_P_INDEX, bookmark.getParagraphIndex());
        values.put(KEY_E_INDEX, bookmark.getElementIndex());
        values.put(KEY_C_INDEX, bookmark.getCharIndex());
        values.put(KEY_CREATION_DATE, bookmark.getCreationTime());
       // values.put(KEY_TITLE, bookmark.getBookTitle()+bookmark.getBookAuthror());
        values.put(KEY_TITLE, bookmark.getBookTitle());
        values.put(KEY_AUTHOR, bookmark.getBookAuthror());
        // updating row
        return db.update(TABLE_BOOKMARKS, values, KEY_ID + " = ?",
                new String[] { String.valueOf(bookmark.getID()) });
    }
    
 // Updating single quote
    public int updateQuote(MyQuote quote) {
    	SQLiteDatabase db = this.getWritableDatabase();
    	 
        ContentValues values = new ContentValues();
        values.put(Q_KEY_BOOK_ID, quote.getBookID());
        values.put(Q_KEY_P_INDEX, quote.getParagraphIndex());
        values.put(Q_KEY_E_INDEX, quote.getElementIndex());
        values.put(Q_KEY_C_INDEX, quote.getCharIndex());
        values.put(Q_KEY_CREATION_DATE, quote.getCreationTime());
       // values.put(KEY_TITLE, bookmark.getBookTitle()+bookmark.getBookAuthror());
        values.put(Q_KEY_TITLE, quote.getBookTitle());
        values.put(Q_KEY_AUTHOR, quote.getBookAuthror());
        values.put(Q_KEY_QUOTE_TEXT, quote.getText());
        // updating row
        return db.update(TABLE_QUOTES, values, KEY_ID + " = ?",
                new String[] { String.valueOf(quote.getID()) });
    }
    
     
    // Deleting single bookmark
    public void deleteBookmark(MyBookmark bookmark) {
    	SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BOOKMARKS, KEY_ID + " = ?",
                new String[] { String.valueOf(bookmark.getID()) });
        db.close();
    }
    
 // Deleting single quote
    public void deleteQuote(MyQuote quote) {
    	SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_QUOTES, KEY_ID + " = ?",
                new String[] { String.valueOf(quote.getID()) });
        db.close();
    }
    
    public void deleteColoredMark(int quoteId){
    	String whereClause = M_COLOR_MARK_QUOTE_ID + " =?";
    	String [] whereArgs = new String[] {String.valueOf(quoteId)};
    	SQLiteDatabase db = getWritableDatabase();
    	db.delete(TABLE_COLOR_MARKS, whereClause, whereArgs);
    	db.close();
    }
}
