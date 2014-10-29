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
package com.webprestige.fr.citations;

public class MyQuote {

	private String text;
	private int paragraphIndex;
	private int elementIndex;
	private int charIndex;
	private String creationTime;
	private long bookID;
	private int ID;
	private String bookTitle;
	private String bookAuthror;
	private String pathToBook;
	private int isFromMyFile;
	private String color;
	
	public String getPathToBook() {
		return pathToBook;
	}
	public int getIsFromMyFile() {
		return isFromMyFile;
	}
	public void setPathToBook(String pathToBook) {
		this.pathToBook = pathToBook;
	}
	public void setIsFromMyFile(int isFromMyFile) {
		this.isFromMyFile = isFromMyFile;
	}
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public String getText() {
		return text;
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
	public String getCreationTime() {
		return creationTime;
	}
	public long getBookID() {
		return bookID;
	}
	public String getBookTitle() {
		return bookTitle;
	}
	public String getBookAuthror() {
		return bookAuthror;
	}
	public String getColor(){
		return color;
	}
	
	public MyQuote(String quoteText, int id, int pIndex, int eIndex, int cIndex, String bTitle, String bAuthor, long _bookID, String cTime, String bookPath, int isfromfile, String clr) {
		text = quoteText;
		paragraphIndex = pIndex;
		elementIndex = eIndex;
		charIndex = cIndex;
		creationTime = cTime;
		bookID = _bookID;
		ID = id;
		bookTitle = bTitle;
		bookAuthror = bAuthor;
		pathToBook = bookPath;
		isFromMyFile = isfromfile;
		color = clr;
	}
	
	public MyQuote(String quoteText, int pIndex, int eIndex, int cIndex, String bTitle, String bAuthor, long _bookID, String cTime, String bookPath, int isfromfile, String clr) {
		text = quoteText;
		paragraphIndex = pIndex;
		elementIndex = eIndex;
		charIndex = cIndex;
		creationTime = cTime;
		bookID = _bookID;
		bookTitle = bTitle;
		bookAuthror = bAuthor;
		pathToBook = bookPath;
		isFromMyFile = isfromfile;
		color = clr;
	}
}
