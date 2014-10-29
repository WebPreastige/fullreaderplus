package com.webprestige.fr.dropbox;

import com.webprestige.fr.dropbox.SyncedBookInfo;
import com.webprestige.fr.dropbox.SyncedData;

interface IDropboxServiceCallback{
	void showDialog();
	void hideDialog();
	void showErrToast();
	void showSuccessToast();
	void showNoNetworkToast();
	void syncFinished(in SyncedData data);
	void uploadFinished(boolean res);
}