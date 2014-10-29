/*
* FullReader+
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.ui.android.view;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.geometerplus.android.fbreader.ReaderApplication;
import org.geometerplus.android.fbreader.preferences.WallpaperAlignmentPreference;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.image.ZLImageData;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;
import org.geometerplus.zlibrary.ui.android.image.ZLAndroidImageData;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.EmbossMaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public final class ZLAndroidPaintContext extends ZLPaintContext {
	public static ZLBooleanOption AntiAliasOption = new ZLBooleanOption("Fonts", "AntiAlias", true);
	public static ZLBooleanOption DeviceKerningOption = new ZLBooleanOption("Fonts", "DeviceKerning", false);
	public static ZLBooleanOption DitheringOption = new ZLBooleanOption("Fonts", "Dithering", false);
	public static ZLBooleanOption HintingOption = new ZLBooleanOption("Fonts", "Hinting", false);
	public static ZLBooleanOption SubpixelOption = new ZLBooleanOption("Fonts", "Subpixel", false);

	private static Boolean ourUsesHintingOption;
	public static boolean usesHintingOption() {
		if (ourUsesHintingOption == null) {
			try {
				ourUsesHintingOption = Paint.class.getMethod("setHinting", int.class) != null;
			} catch (NoSuchMethodException e) {
				ourUsesHintingOption = false;
			}
		}
		return ourUsesHintingOption;
	}

	private final Canvas myCanvas;
	private final Paint myTextPaint = new Paint();
	private final Paint myLinePaint = new Paint();
	private final Paint myFillPaint = new Paint();
	private final Paint myOutlinePaint = new Paint();

	private final int myWidth;
	private final int myHeight;
	private final int myScrollbarWidth;

	private ZLColor myBackgroundColor = new ZLColor(0, 0, 0);
	
	private SharedPreferences preferences;
	private int mScreenWidth;
	private int mScreenHeight;
	
	private float mScaleWidth;
	private float mScaleHeight;
	
	private int mBitmapWidth;
	private int mBitmapHeight;
	
	private String title;
	private DisplayMetrics displaymetrics;
	WindowManager manager;
	ZLAndroidPaintContext(Canvas canvas, int width, int height, int scrollbarWidth) {
		myCanvas = canvas;
		myWidth = width - scrollbarWidth;
		myHeight = height;
		myScrollbarWidth = scrollbarWidth;

		myTextPaint.setLinearText(false);
		myTextPaint.setAntiAlias(AntiAliasOption.getValue());
		if (DeviceKerningOption.getValue()) {
			myTextPaint.setFlags(myTextPaint.getFlags() | Paint.DEV_KERN_TEXT_FLAG);
		} else {
			myTextPaint.setFlags(myTextPaint.getFlags() & ~Paint.DEV_KERN_TEXT_FLAG);
		}
		myTextPaint.setDither(DitheringOption.getValue());
//		if (usesHintingOption()) {
//			myTextPaint.setHinting(HintingOption.getValue() ? Paint.HINTING_ON : Paint.HINTING_OFF);
//		}
		myTextPaint.setSubpixelText(SubpixelOption.getValue());

		myLinePaint.setStyle(Paint.Style.STROKE);

		myOutlinePaint.setColor(Color.rgb(255, 127, 0));
		myOutlinePaint.setAntiAlias(true);
		myOutlinePaint.setDither(true);
		myOutlinePaint.setStrokeWidth(4);
		myOutlinePaint.setStyle(Paint.Style.STROKE);
		myOutlinePaint.setPathEffect(new CornerPathEffect(5));
		myOutlinePaint.setMaskFilter(new EmbossMaskFilter(new float[] {1, 1, 1}, .4f, 6f, 3.5f));
		
		displaymetrics = new DisplayMetrics();
		manager = (WindowManager) ReaderApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
		manager.getDefaultDisplay().getMetrics(displaymetrics);
		/*mScreenHeight = displaymetrics.heightPixels;
		mScreenWidth = displaymetrics.widthPixels;*/
		mScreenHeight = height;
		mScreenWidth = width;
	}

	private static ZLFile ourWallpaperFile;
	private static Bitmap ourWallpaper;
	@Override
	public void clear(ZLFile wallpaperFile, WallpaperMode mode) {
		preferences = PreferenceManager.getDefaultSharedPreferences(ReaderApplication.getContext());
		String wallpaperAlignment = preferences.getString(WallpaperAlignmentPreference.WALLPAPER_ALIGN_KEY, "");
		boolean canAlign = preferences.getBoolean(WallpaperAlignmentPreference.WALLPAPER_ALIGN_ENABLED, false);
		// В случае, если фон может выравниваться - устанавливаем режим выравнивания
		if (canAlign){
			if (wallpaperAlignment.equals("wallpaper_center")) mode = WallpaperMode.CENTER;
			else if (wallpaperAlignment.equals("wallpaper_fill")) mode = WallpaperMode.FILL;
			else if (wallpaperAlignment.equals("wallpaper_original")) mode = WallpaperMode.ORIGINAL;
			//else if (wallpaperAlignment.equals("wallpaper_tile")||wallpaperAlignment.length() == 0) mode = WallpaperMode.TILE_MIRROR;
			else if (wallpaperAlignment.equals("wallpaper_tile")||wallpaperAlignment.length() == 0) mode = WallpaperMode.TILE;
		}
		if (!wallpaperFile.equals(ourWallpaperFile)) {
			ourWallpaperFile = wallpaperFile;
			ourWallpaper = null;
			try {
				 Bitmap fileBitmap = null;
				// В зависимости от того, может ли изображение выравниваться или нет - декодируем изображение по разному
				if (!canAlign) fileBitmap = BitmapFactory.decodeStream(wallpaperFile.getInputStream());
				else fileBitmap = BitmapFactory.decodeFile(wallpaperFile.getPhysicalFile().getPath());
				// Fix for tile mode 
				// there was no string
				mode = WallpaperMode.TILE;
				switch (mode) {
					
					case TILE_MIRROR:
					{
						final int w = fileBitmap.getWidth();
						final int h = fileBitmap.getHeight();
						final Bitmap wallpaper = Bitmap.createBitmap(2 * w, 2 * h, fileBitmap.getConfig());
						final Canvas wallpaperCanvas = new Canvas(wallpaper);
						final Paint wallpaperPaint = new Paint();
						
						Matrix m = new Matrix();
						wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
						m.preScale(-1, 1);
						m.postTranslate(2 * w, 0);
						wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
						m.preScale(1, -1);
						m.postTranslate(0, 2 * h);
						wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
						m.preScale(-1, 1);
						m.postTranslate(- 2 * w, 0);
						wallpaperCanvas.drawBitmap(fileBitmap, m, wallpaperPaint);
						ourWallpaper = wallpaper;
						break;
					}
					case TILE:
					case CENTER:
					case FILL:
					case ORIGINAL:
						ourWallpaper = fileBitmap;
						break;
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		if (ourWallpaper != null) {
			myBackgroundColor = ZLAndroidColorUtil.getAverageColor(ourWallpaper);
			myCanvas.drawColor(0, Mode.CLEAR);
			switch (mode){
				case TILE:
				case TILE_MIRROR:
					
					final int w = ourWallpaper.getWidth();
					final int h = ourWallpaper.getHeight();
					for (int cw = 0, iw = 1; cw < myWidth; cw += w, ++iw) {
						for (int ch = 0, ih = 1; ch < myHeight; ch += h, ++ih) {
							myCanvas.drawBitmap(ourWallpaper, cw, ch, myFillPaint);
						}
					}
				break;
				case FILL:
					myCanvas.drawBitmap(getFilledBitmap(ourWallpaper), 0, 0, null);
				break;
				case ORIGINAL:
					myCanvas.drawBitmap(ourWallpaper, 0, 0, null);
				break;
				case CENTER:
					drawCenteredBitmap(ourWallpaper);
				break;
			}
		} else {
			clear(new ZLColor(128, 128, 128));
		}
	}

	@Override
	public void clear(ZLColor color) {
		myBackgroundColor = color;
		myFillPaint.setColor(ZLAndroidColorUtil.rgb(color));
		myCanvas.drawRect(0, 0, myWidth + myScrollbarWidth, myHeight, myFillPaint);
	}

	@Override
	public ZLColor getBackgroundColor() {
		return myBackgroundColor;
	}

	public void fillPolygon(int[] xs, int ys[]) {
		final Path path = new Path();
		final int last = xs.length - 1;
		path.moveTo(xs[last], ys[last]);
		for (int i = 0; i <= last; ++i) {
			path.lineTo(xs[i], ys[i]);
		}
		myCanvas.drawPath(path, myFillPaint);
	}

	public void drawPolygonalLine(int[] xs, int ys[]) {
		final Path path = new Path();
		final int last = xs.length - 1;
		path.moveTo(xs[last], ys[last]);
		for (int i = 0; i <= last; ++i) {
			path.lineTo(xs[i], ys[i]);
		}
		myCanvas.drawPath(path, myLinePaint);
	}

	public void drawOutline(int[] xs, int ys[]) {
		final int last = xs.length - 1;
		int xStart = (xs[0] + xs[last]) / 2;
		int yStart = (ys[0] + ys[last]) / 2;
		int xEnd = xStart;
		int yEnd = yStart;
		if (xs[0] != xs[last]) {
			if (xs[0] > xs[last]) {
				xStart -= 5;
				xEnd += 5;
			} else {
				xStart += 5;
				xEnd -= 5;
			}
		} else {
			if (ys[0] > ys[last]) {
				yStart -= 5;
				yEnd += 5;
			} else {
				yStart += 5;
				yEnd -= 5;
			}
		}

		final Path path = new Path();
		path.moveTo(xStart, yStart);
		for (int i = 0; i <= last; ++i) {
			path.lineTo(xs[i], ys[i]);
		}
		path.lineTo(xEnd, yEnd);
		myCanvas.drawPath(path, myOutlinePaint);
	}

	@Override
	protected void setFontInternal(String family, int size, boolean bold, boolean italic, boolean underline, boolean strikeThrought) {
		myTextPaint.setTypeface(AndroidFontUtil.typeface(family, bold, italic));
		myTextPaint.setTextSize(size);
		myTextPaint.setUnderlineText(underline);
		myTextPaint.setStrikeThruText(strikeThrought);
	}

	@Override
	public void setTextColor(ZLColor color) {
		myTextPaint.setColor(ZLAndroidColorUtil.rgb(color));
	}

	@Override
	public void setLineColor(ZLColor color) {
		myLinePaint.setColor(ZLAndroidColorUtil.rgb(color));
	}
	@Override
	public void setLineWidth(int width) {
		myLinePaint.setStrokeWidth(width);
	}

	@Override
	public void setFillColor(ZLColor color, int alpha) {
		myFillPaint.setColor(ZLAndroidColorUtil.rgba(color, alpha));
	}

	public int getWidth() {
		return myWidth;
	}
	public int getHeight() {
		return myHeight;
	}

	@Override
	public int getStringWidth(char[] string, int offset, int length) {
		boolean containsSoftHyphen = false;
		for (int i = offset; i < offset + length; ++i) {
			if (string[i] == (char)0xAD) {
				containsSoftHyphen = true;
				break;
			}
		}
		if (!containsSoftHyphen) {
			return (int)(myTextPaint.measureText(new String(string, offset, length)) + 0.5f);
		} else {
			final char[] corrected = new char[length];
			int len = 0;
			for (int o = offset; o < offset + length; ++o) {
				final char chr = string[o];
				if (chr != (char)0xAD) {
					corrected[len++] = chr;
				}
			}
			return (int)(myTextPaint.measureText(corrected, 0, len) + 0.5f);
		}
	}
	@Override
	protected int getSpaceWidthInternal() {
		return (int)(myTextPaint.measureText(" ", 0, 1) + 0.5f);
	}
	@Override
	protected int getStringHeightInternal() {
		return (int)(myTextPaint.getTextSize() + 0.5f);
	}
	@Override
	protected int getDescentInternal() {
		return (int)(myTextPaint.descent() + 0.5f);
	}
	@Override
	public void drawString(int x, int y, char[] string, int offset, int length) {
		boolean containsSoftHyphen = false;
		for (int i = offset; i < offset + length; ++i) {
			if (string[i] == (char)0xAD) {
				containsSoftHyphen = true;
				break;
			}
		}
		if (!containsSoftHyphen) {
			myCanvas.drawText(new String(string, offset, length), x, y, myTextPaint);
		} else {
			final char[] corrected = new char[length];
			int len = 0;
			for (int o = offset; o < offset + length; ++o) {
				final char chr = string[o];
				if (chr != (char)0xAD) {
					corrected[len++] = chr;
				}
			}
			myCanvas.drawText(new String(corrected, 0, len), x, y, myTextPaint);
		}
	}

	@Override
	public Size imageSize(ZLImageData imageData, Size maxSize, ScalingType scaling) {
		final Bitmap bitmap = ((ZLAndroidImageData)imageData).getBitmap(maxSize, scaling);
		return (bitmap != null && !bitmap.isRecycled())
			? new Size(bitmap.getWidth(), bitmap.getHeight()) : null;
	}

	@Override
	public void drawImage(int x, int y, ZLImageData imageData, Size maxSize, ScalingType scaling) {
		final Bitmap bitmap = ((ZLAndroidImageData)imageData).getBitmap(maxSize, scaling);
		if (bitmap != null && !bitmap.isRecycled()) {
			myCanvas.drawBitmap(bitmap, x, y - bitmap.getHeight(), myFillPaint);
		}
	}
	
	@Override
	public void drawImage(int x, int y, Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			myCanvas.drawBitmap(bitmap, x, y - bitmap.getHeight(), myFillPaint);
		}
	}
	
	@Override
	public void drawLine(int x0, int y0, int x1, int y1) {
		final Canvas canvas = myCanvas;
		final Paint paint = myLinePaint;
		paint.setAntiAlias(false);
		canvas.drawLine(x0, y0, x1, y1, paint);
		canvas.drawPoint(x0, y0, paint);
		canvas.drawPoint(x1, y1, paint);
		paint.setAntiAlias(true);
	}

	@Override
	public void fillRectangle(int x0, int y0, int x1, int y1) {
		if (x1 < x0) {
			int swap = x1;
			x1 = x0;
			x0 = swap;
		}
		if (y1 < y0) {
			int swap = y1;
			y1 = y0;
			y0 = swap;
		}
		myCanvas.drawRect(x0, y0, x1 + 1, y1 + 1, myFillPaint);
	}
	
	
	
	
	// ------- Метод, который будет создавать картинку, которая будет заполнять весь экран -------
	private Bitmap getFilledBitmap(Bitmap bitmap){
		/*manager.getDefaultDisplay().getMetrics(displaymetrics);
		mScreenHeight = displaymetrics.heightPixels;
		mScreenWidth = displaymetrics.widthPixels;*/
		
		mBitmapHeight = bitmap.getHeight();
		mBitmapWidth = bitmap.getWidth();
		if (mBitmapWidth == mScreenWidth && mBitmapHeight == mScreenHeight) return bitmap;
		mScaleWidth = ((float) mScreenWidth) / mBitmapWidth;
		mScaleHeight = ((float) mScreenHeight) / mBitmapHeight;

		Matrix matrix = new Matrix();
		matrix.postScale(mScaleWidth, mScaleHeight);
		Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, mBitmapWidth, mBitmapHeight, matrix, false);
		return resized;
	}
	
	// ------- Метод, который будет рисовать центрированную картинку -------
	private void drawCenteredBitmap(Bitmap bitmap){
		mBitmapHeight = bitmap.getHeight();
		mBitmapWidth = bitmap.getWidth();
		// Если изображение меньше, чем размер экрана
		if (mBitmapWidth < mScreenWidth && mBitmapHeight < mScreenHeight){
			int cx = (mScreenWidth- mBitmapWidth) / 2;
		    int cy = (mScreenHeight - mBitmapHeight) / 2;
		    myCanvas.drawBitmap(bitmap, cx, cy, null);
		}
		// В остальных случаях - скейлим до середины
		else{
			/*
			 * Если изображение с одноий из сторон меньше чем размер экрана - 
			 * нужно будет рассчитать x-y точки для его отрисовки
			 */
			int xPoint = 0;
			int yPoint = 0;
			if (mBitmapHeight<mScreenHeight){
				yPoint = (mScreenHeight - mBitmapHeight) / 2;
			}
			else
			if (mBitmapWidth<mScreenWidth){
				xPoint = (mScreenWidth- mBitmapWidth) / 2;
			}
			myCanvas.drawBitmap(getCroppedBitmap(bitmap), xPoint, yPoint, null);
		}
	}
	
	private Bitmap getCroppedBitmap(Bitmap srcBmp){
		Bitmap dstBmp;
		if (srcBmp.getWidth() >= srcBmp.getHeight()){
			dstBmp = Bitmap.createBitmap( srcBmp, srcBmp.getWidth()/2 - srcBmp.getHeight()/2, 0,srcBmp.getHeight(), srcBmp.getHeight());
		}
		else{
			dstBmp = Bitmap.createBitmap(srcBmp,0, srcBmp.getHeight()/2 - srcBmp.getWidth()/2, srcBmp.getWidth(), srcBmp.getWidth());
		}
		return dstBmp;
	}
}
