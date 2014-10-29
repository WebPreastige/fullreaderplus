/*
FullReader+ 
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
copyright (coffee) 2009-2013 Geometer Plus <contact@geometerplus.com> 
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

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.plus.PlusShare;
import com.fullreader.R;

public class FaqAnswerActivity extends BaseActivity {

	
	
	public static final String ANSWER_TITLE = "title";
	public static final String ANSWER_TEXT = "text";

	private int googlePlusIcon = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.faq_answer_activity);
		
		TextView title = (TextView)findViewById(R.id.tw_faq_answer_title);
		TextView text = (TextView)findViewById(R.id.tw_faq_answer_text);
		
		title.setTextColor(Color.BLACK);
		text.setTextColor(Color.BLACK);
		
		title.setText(getIntent().getStringExtra(ANSWER_TITLE));
		text.setText(Html.fromHtml(getIntent().getStringExtra(ANSWER_TEXT)));
		
		ImageView imgBackground = (ImageView)findViewById(R.id.img_faq_white_bg);
		switch(theme) {
	        case IConstants.THEME_MYBLACK:
	    		imgBackground.setImageResource(R.drawable.bg_white);
	    		title.setTextColor(Color.parseColor("#555555"));
	    		text.setTextColor(Color.parseColor("#555555"));
	    		googlePlusIcon = R.drawable.google_plus_marble;
	        	break;
	        case IConstants.THEME_LAMINAT:
	    		imgBackground.setImageResource(R.drawable.bg_white);
	    		title.setTextColor(Color.parseColor("#6b4017"));
	    		text.setTextColor(Color.parseColor("#ad7e52"));
	    		googlePlusIcon = R.drawable.google_plus_laminat;
	        	break;
	        case IConstants.THEME_REDTREE:
	    		imgBackground.setImageResource(R.drawable.bg_white);
	    		title.setTextColor(Color.parseColor("#555555"));
	    		text.setTextColor(Color.parseColor("#555555"));
	    		googlePlusIcon = R.drawable.google_plus_red_tree;
	        	break;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);  
        addMenuItem(menu, 0, "Google Plus", googlePlusIcon, true);
		return true;
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case 0:{
			Intent shareIntent = new PlusShare.Builder(FaqAnswerActivity.this)
            .setType("text/plain")
            .setText("Best reader for Android!")
            .setContentUrl(Uri.parse("https://developers.google.com/+/"))
            .getIntent();
			startActivityForResult(shareIntent, 0);
			return true;
			}
		}
		return false;
	}
}
 
