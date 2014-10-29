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

package org.geometerplus.android.fbreader.preferences;

import java.util.Locale;

import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.zlibrary.core.options.ZLColorOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.ui.android.util.ZLAndroidColorUtil;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.fullreader.R;

class ZLColorPreference extends DialogPreference {
	private final ZLColorOption myOption;

	private SeekBar myRedSlider;
	private SeekBar myGreenSlider;
	private SeekBar myBlueSlider;
	private final GradientDrawable myPreviewDrawable = new GradientDrawable();
	private SharedPreferences sPref;
	ZLColorPreference(Context context, ZLResource resource, String resourceKey, ZLColorOption option) {
		super(context, null);
		myOption = option;
        final String title = resource.getResource(resourceKey).getValue();
		setTitle(title);
		
		/*if(Locale.getDefault().getDisplayLanguage().equals("русский") || Locale.getDefault().getDisplayLanguage().equals("українська")) {
			setTitle("Цвет фона");
		} else {
			setTitle("Background color"); 
		}
		if(loadCurrentLanguage().equals("ru")) {
			setTitle("Цвет фона");
		} 
		if(loadCurrentLanguage().equals("en")) {
			setTitle("Background color");
		} 
		if(loadCurrentLanguage().equals("de")) {
			setTitle("Hintergrundfarbe");
		} 
		if(loadCurrentLanguage().equals("fr")) {
			setTitle("couleur de fond");
		} 
		if(loadCurrentLanguage().equals("uk")) {
			setTitle("Колір фону");
		} */
		
		
		//setDialogTitle(title);
		
		
		setDialogLayoutResource(R.layout.color_dialog);

		
		
		
		//final ZLResource buttonResource = ZLResource.resource("dialog").getResource("button");
		//setPositiveButtonText(buttonResource.getResource("ok").getValue());
		//setNegativeButtonText(buttonResource.getResource("cancel").getValue());
	}
	
	String loadCurrentLanguage() {
	    sPref = getContext().getSharedPreferences("languagePrefs", getContext().MODE_PRIVATE);
	    return sPref.getString("curLanguage", "");
	}
	   /** Hide the cancel button */
    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setNegativeButton(null, null);
        builder.setPositiveButton(null, null);
    }
	private SeekBar createSlider(View view, int id, int value, String resourceKey) {
		final SeekBar slider = (SeekBar)view.findViewById(id);
		slider.setProgressDrawable(new SeekBarDrawable(
			slider.getProgressDrawable(),
			ZLResource.resource("color").getResource(resourceKey).getValue(),
			slider
		));
		slider.setProgress(value);
		return slider;
	}

	@Override
	protected void onBindDialogView(View view) {
		
		int theme = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);  
		LinearLayout body = (LinearLayout)view.findViewById(R.id.body);
	        switch(theme){	    
		        case IConstants.THEME_MYBLACK:
		        	body.setBackgroundResource(R.drawable.theme_black_bg);
		        	break;
		        case IConstants.THEME_LAMINAT:
		        	body.setBackgroundResource(R.drawable.theme_laminat_bg);
		        	break;
		        case IConstants.THEME_REDTREE:
		        	body.setBackgroundResource(R.drawable.theme_redtree_bg);
		        	break;
	        }
	        
			Button btnOk = (Button) view.findViewById(R.id.btn_ok);
			Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
			
			btnOk.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onDialogClosed(true);
					getDialog().dismiss();
				}
			});
			btnCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onDialogClosed(false);
					getDialog().dismiss();
				}
			});

	        btnOk.setText(ZLResource.resource("other").getResource("ok").getValue());
	        btnCancel.setText(ZLResource.resource("other").getResource("cancel").getValue());
		
		final ZLColor color = myOption.getValue();

		myRedSlider = createSlider(view, R.id.color_red, color.Red, "red");
		myGreenSlider = createSlider(view, R.id.color_green, color.Green, "green");
		myBlueSlider = createSlider(view, R.id.color_blue, color.Blue, "blue");

		final View colorBox = view.findViewById(R.id.color_box);
		colorBox.setBackgroundDrawable(myPreviewDrawable);
		myPreviewDrawable.setCornerRadius(7);
		myPreviewDrawable.setColor(ZLAndroidColorUtil.rgb(color));

		final SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				myPreviewDrawable.setColor(Color.rgb(
					myRedSlider.getProgress(),
					myGreenSlider.getProgress(),
					myBlueSlider.getProgress()
				));
				myPreviewDrawable.invalidateSelf();
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				myPreviewDrawable.setColor(Color.rgb(
					myRedSlider.getProgress(),
					myGreenSlider.getProgress(),
					myBlueSlider.getProgress()
				));
				myPreviewDrawable.invalidateSelf();
			}
		};
		myRedSlider.setOnSeekBarChangeListener(listener);
		myGreenSlider.setOnSeekBarChangeListener(listener);
		myBlueSlider.setOnSeekBarChangeListener(listener);

		super.onBindDialogView(view);
	}

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
		if (positiveResult) {
			myOption.setValue(new ZLColor(
				myRedSlider.getProgress(),
				myGreenSlider.getProgress(),
				myBlueSlider.getProgress()
			));
		}
	}

	/*
	@Override
	protected void onBindView(View view) {
		final ImageView colorView = (ImageView)view.findViewById(R.id.color_preference_color);
		//colorView.setImageResource(R.drawable.fbreader);
		final Drawable drawable = new ColorDrawable(0x00FF00);
		colorView.setImageDrawable(drawable);

		super.onBindView(view);
	}
	*/

	static class SeekBarDrawable extends Drawable {
		private final SeekBar mySlider;
		private final Drawable myBase;
		private final String myText;
		private final Paint myPaint;
		private final Paint myOutlinePaint;
		private boolean myLabelOnRight;

		public SeekBarDrawable(Drawable base, String text, SeekBar slider) {
			mySlider = slider;
			myBase = base;
			myText = text;

			myPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			myPaint.setTypeface(Typeface.DEFAULT_BOLD);
			myPaint.setColor(Color.BLACK);
			myPaint.setAlpha(255);

			myOutlinePaint = new Paint(myPaint);
			myOutlinePaint.setStyle(Paint.Style.STROKE);
			myOutlinePaint.setStrokeWidth(3);
			myOutlinePaint.setColor(0xFFAAAAAA);

			myLabelOnRight = mySlider.getProgress() < 128;
		}

		@Override
		protected void onBoundsChange(Rect bounds) {
			myBase.setBounds(bounds);
		}

		@Override
		protected boolean onStateChange(int[] state) {
			invalidateSelf();
			return false;
		}

		@Override
		public boolean isStateful() {
			return true;
		}

		@Override
		protected boolean onLevelChange(int level) {
			if (level < 4000) {
				myLabelOnRight = true;
			} else if (level > 6000) {
				myLabelOnRight = false;
			}
			return myBase.setLevel(level);
		}

		@Override
		public void draw(Canvas canvas) {
			myBase.draw(canvas);

			final Rect bounds = getBounds();
			final int textSize = bounds.height() * 2 / 3;
			myPaint.setTextSize(textSize);
			myOutlinePaint.setTextSize(textSize);
			final Rect textBounds = new Rect();
			myPaint.getTextBounds("a", 0, 1, textBounds);
			final String text = myText + ": " + mySlider.getProgress();
			final float textWidth = myOutlinePaint.measureText(text);
			final float x = myLabelOnRight ? bounds.width() - textWidth - 6 : 6;
			final float y = bounds.height() / 2 + textBounds.height();
			canvas.drawText(text, x, y, myOutlinePaint);
			canvas.drawText(text, x, y, myPaint);
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}

		@Override
		public void setAlpha(int alpha) {
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
		}
	}
}
