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

public class MyBookmark {
	
	private int paragraphIndex;
	private int elementIndex;
	private int charIndex;
	private String creationTime;
	private String pathToBook;
	private int isFromMyFile;
	
	public String getPathToBook() {
		return pathToBook;
	}

	public void setPathToBook(String pathToBook) {
		this.pathToBook = pathToBook;
	}

	public int getIsFromMyFile() {
		return isFromMyFile;
	}

	public void setIsFromMyFile(int isFromMyFile) {
		this.isFromMyFile = isFromMyFile;
	}

	private long bookID;
	private int ID;
	
	private String bookTitle;
	private String bookAuthror;
	
	public String getBookTitle() {
		return bookTitle;
	}

	public String getBookAuthror() {
		return bookAuthror;
	}

	public long getBookID() {
		return bookID;
	}

	public MyBookmark(int id, int pIndex, int eIndex, int cIndex, String bTitle, String bAuthor, long _bookID, String cTime, String bookPath, int isfromfile) {
		paragraphIndex = pIndex;
		elementIndex = eIndex;
		charIndex = cIndex;
		bookTitle = bTitle;
		bookAuthror = bAuthor;
		bookID = _bookID;
		creationTime = cTime;
		ID = id;
		pathToBook = bookPath;
		isFromMyFile = isfromfile;
	}
	
	public MyBookmark(int pIndex, int eIndex, int cIndex, String bTitle, String bAuthor, long _bookID, String cTime, String bookPath, int isfromfile) {
		paragraphIndex = pIndex;
		elementIndex = eIndex;
		charIndex = cIndex;
		bookTitle = bTitle;
		bookAuthror = bAuthor;
		bookID = _bookID;
		creationTime = cTime;
		pathToBook = bookPath;
		isFromMyFile = isfromfile;
	}

	public int getID() {
		return ID;
	}

	public String getCreationTime() {
		return creationTime;
	}

	public int getParagraphIndex() {
		return paragraphIndex;
	}

	public int getElementIndex() {
		return elementIndex;
	}

	public int getCharIndex() {
		return charIndex;
	}
}
