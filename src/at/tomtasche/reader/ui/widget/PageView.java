/*
	OpenOffice Document Reader is Android's first native ODF Viewer!
    Copyright (C) 2010  Thomas Taschauer - tomtasche@gmail.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>
*/

package at.tomtasche.reader.ui.widget;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import at.tomtasche.reader.ui.ParagraphListener;

@SuppressLint("SetJavaScriptEnabled")
public class PageView extends WebView implements ParagraphListener {

	public static final String ENCODING = "UTF-8";

	private ParagraphListener paragraphListener;
	private boolean scrolled;

	private File writeTo;
	private Runnable writtenCallback;

	public PageView(Context context) {
		this(context, 0);
	}

	@SuppressLint("NewApi")
	public PageView(Context context, final int scroll) {
		super(context);

		WebSettings settings = getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setDisplayZoomControls(false);
		settings.setSupportZoom(true);
		settings.setDefaultTextEncodingName(ENCODING);
		settings.setJavaScriptEnabled(true);

		addJavascriptInterface(this, "paragraphListener");

		setKeepScreenOn(true);
		if (Build.VERSION.SDK_INT >= 14)
			try {
				Method method = context.getClass().getMethod(
						"setSystemUiVisibility", Integer.class);
				method.invoke(context, 1);
			} catch (Exception e) {
			}

		setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);

				if (!scrolled) {
					postDelayed(new Runnable() {

						@Override
						public void run() {
							scrollBy(0, scroll);
						}
					}, 250);

					scrolled = true;
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.startsWith("https://docs.google.com/viewer?embedded=true")) {
					return false;
				} else {
					try {
						getContext().startActivity(
								new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

						return true;
					} catch (Exception e) {
						e.printStackTrace();

						return false;
					}
				}
			}
		});
	}

	public void setParagraphListener(ParagraphListener paragraphListener) {
		this.paragraphListener = paragraphListener;
	}

	public void getParagraph(final int index) {
		post(new Runnable() {

			@Override
			public void run() {
				loadUrl("javascript:var children = document.body.childNodes; "
						// document.body.firstChild.childNodes in the desktop
						// version of Google Chrome
						+ "if (children.length <= " + index + ") { "
						+ "paragraphListener.end();" + " return; " + "}"
						+ "var child = children[" + index + "]; "
						+ "if (child) { "
						+ "paragraphListener.paragraph(child.innerText); "
						+ "} else { " + "paragraphListener.increaseIndex(); "
						+ "}");
			}
		});
	}

	public void requestHtml(File writeTo, Runnable callback) {
		this.writeTo = writeTo;
		this.writtenCallback = callback;

		loadUrl("javascript:window.paragraphListener.sendHtml('<html>' + document.getElementsByTagName('html')[0].innerHTML + '</html>');");
	}

	@JavascriptInterface
	public void sendHtml(String html) {
		FileWriter writer;
		try {
			writer = new FileWriter(writeTo);
			writer.write(html);
			writer.close();

			// TODO: oh. my. god. stop it!
			new Thread(writtenCallback).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void paragraph(String text) {
		paragraphListener.paragraph(text);
	}

	@Override
	public void increaseIndex() {
		paragraphListener.increaseIndex();
	}

	@Override
	public void end() {
		paragraphListener.end();
	}
}
