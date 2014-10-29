package com.webprestige.fr.dropbox;

import com.webprestige.fr.dropbox.IDropboxServiceCallback;
import com.webprestige.fr.dropbox.SyncedBookInfo;
import com.webprestige.fr.dropbox.SyncedBookmarkInfo;
import com.webprestige.fr.dropbox.SyncedQuoteInfo;
import com.webprestige.fr.dropbox.SyncedColorMarkInfo;

interface IDropboxService {
	void addBook(in SyncedBookInfo sbInfo);
	void addBookmark(in SyncedBookmarkInfo sBmkInfo);
	void addQuote(in SyncedQuoteInfo sQtInfo);
	void addColorMark(in SyncedColorMarkInfo sCMInfo);
	void getSyncData();
	void syncAll();
	void registerCallback(IDropboxServiceCallback callback);
	void unregisterCallback();
	void deleteBookmark(String bookTitle, int parIndex);
	void deleteQuote(String bookTitle, String quoteText, int parIndex);
	void deleteColorMark(String bookTitle, String quoteText, int parIndex, int startParIndex, int endParIndex, int color);
}