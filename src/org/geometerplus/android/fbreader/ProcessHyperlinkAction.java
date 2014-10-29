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

package org.geometerplus.android.fbreader;

import org.geometerplus.android.fbreader.image.ImageViewActivity;
import org.geometerplus.android.fbreader.network.BookDownloader;
import org.geometerplus.android.fbreader.network.BookDownloaderService;
import org.geometerplus.fbreader.bookmodel.FBHyperlinkType;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.ReaderApp;
import org.geometerplus.fbreader.network.NetworkLibrary;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlink;
import org.geometerplus.zlibrary.text.view.ZLTextHyperlinkRegionSoul;
import org.geometerplus.zlibrary.text.view.ZLTextImageRegionSoul;
import org.geometerplus.zlibrary.text.view.ZLTextRegion;
import org.geometerplus.zlibrary.text.view.ZLTextWordRegionSoul;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;

class ProcessHyperlinkAction extends FBAndroidAction {
	ProcessHyperlinkAction(FullReaderActivity baseActivity, ReaderApp fbreader) {
		super(baseActivity, fbreader);
	}

	@Override
	public boolean isEnabled() {
		return ReaderApp.getTextView().getSelectedRegion() != null;
	}

	@Override
	protected void run(Object ... params) {
		ReaderApp readerApp = (ReaderApp)ReaderApp.Instance();
		ReaderApp.addAction(ActionCode.PROCESS_HYPERLINK, new ProcessHyperlinkAction(BaseActivity, readerApp));
		final ZLTextRegion region = ReaderApp.getTextView().getSelectedRegion();
		if (region == null) {
			return;
		}
		
		final ZLTextRegion.Soul soul = region.getSoul();
		if (soul instanceof ZLTextHyperlinkRegionSoul) {
			ReaderApp.getTextView().hideSelectedRegionBorder();
			ReaderApp.getViewWidget().repaint();
			final ZLTextHyperlink hyperlink = ((ZLTextHyperlinkRegionSoul)soul).Hyperlink;
			switch (hyperlink.Type) {
				case FBHyperlinkType.EXTERNAL:
					openInBrowser(hyperlink.Id);
					break;
				case FBHyperlinkType.INTERNAL:
					ReaderApp.Collection.markHyperlinkAsVisited(ReaderApp.Model.Book, hyperlink.Id);
					ReaderApp.tryOpenFootnote(hyperlink.Id);
					break;
			}
		} else if (soul instanceof ZLTextImageRegionSoul) {
			ReaderApp.getTextView().hideSelectedRegionBorder();
			ReaderApp.getViewWidget().repaint();
			final String url = ((ZLTextImageRegionSoul)soul).ImageElement.URL;
			if (url != null) {
				try {
					final Intent intent = new Intent();
					intent.setClass(BaseActivity, ImageViewActivity.class);
					intent.setData(Uri.parse(url));
					//intent.putExtra(
					//	ImageViewActivity.BACKGROUND_COLOR_KEY,
					//	ReaderApp.ImageViewBackgroundOption.getValue().getIntValue()
					//);
					OrientationUtil.startActivity(BaseActivity, intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (soul instanceof ZLTextWordRegionSoul) {
			DictionaryUtil.openWordInDictionary(
				BaseActivity, ((ZLTextWordRegionSoul)soul).Word, region
			);
		}
	}

	private void openInBrowser(final String url) {
		final Intent intent = new Intent(Intent.ACTION_VIEW);
		final boolean externalUrl;
		if (BookDownloader.acceptsUri(Uri.parse(url))) {
			intent.setClass(BaseActivity, BookDownloader.class);
			intent.putExtra(BookDownloaderService.SHOW_NOTIFICATIONS_KEY, BookDownloaderService.Notifications.ALL);
			externalUrl = false;
		} else {
			externalUrl = true;
		}
		final NetworkLibrary nLibrary = NetworkLibrary.Instance();
		new Thread(new Runnable() {
			public void run() {
				if (!url.startsWith("reader-action:")) {
					nLibrary.initialize();
				}
				intent.setData(Uri.parse(nLibrary.rewriteUrl(url, externalUrl)));
				BaseActivity.runOnUiThread(new Runnable() {
					public void run() {
						try {
							OrientationUtil.startActivity(BaseActivity, intent);
						} catch (ActivityNotFoundException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}).start();
	}
}
