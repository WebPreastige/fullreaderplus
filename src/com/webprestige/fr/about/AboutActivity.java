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
package com.webprestige.fr.about;

import java.util.Locale;

import org.geometerplus.android.fbreader.BaseActivity;
import org.geometerplus.android.fbreader.IConstants;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.fullreader.R;

public class AboutActivity extends BaseActivity{
	private Button siteBtn;
	private TextView developerTV;
	//private AdView adView;
	private SharedPreferences sPref;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar bar = getSupportActionBar();
        Drawable actionBarBackground = null;
        siteBtn = (Button) findViewById(R.id.buy_btn);
        int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
        switch(theme){
	        case IConstants.THEME_MYBLACK:
	         actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar );
	         getWindow().setBackgroundDrawableResource(R.drawable.theme_black_shelf);
	         break;
	        case IConstants.THEME_LAMINAT:
	         actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar );
	         getWindow().setBackgroundDrawableResource(R.drawable.theme_laminat_shelf);
	         break;
	        case IConstants.THEME_REDTREE:
	         actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_redtree_action_bar );
	         getWindow().setBackgroundDrawableResource(R.drawable.theme_redtree_shelf);
	         break;
        }      
        bar.setBackgroundDrawable(actionBarBackground);
        //Typeface tf = Typeface.createFromAsset(getAssets(),
        //        "fonts/Arial.ttf");
		developerTV = (TextView)findViewById(R.id.desc_tv);
		//developerTV.setTypeface(tf);
		if(loadCurrentLanguage().equals("en")){
			developerTV.setText("If you want disable ads you need to buy extension 'FullReader Plus'. After activation this extension in FullReader will disabled ads and will be added possibility to view docx files(Docx plugin works only for Android 4+)");
			siteBtn.setText("Buy");
		} else if(loadCurrentLanguage().equals("de")){
			developerTV.setText("Wenn Sie deaktivieren möchten, dass Sie Anzeigen Erweiterung 'FullReader Plus' zu kaufen. Nach der Aktivierung dieser Erweiterung in FullReader deaktiviert Anzeigen und Möglichkeit, docx-Dateien anzuzeigen hinzugefügt werden (docx-Plugin funktioniert nur für Android-4 +)");
			siteBtn.setText("Kaufen");
		} else if(loadCurrentLanguage().equals("fr")){
			developerTV.setText("Afin de supprimer les annonces doivent acheter add-«FullReader plus». Lorsque vous activez cette publicité FullReader add-in sera supprimé et ajouté la possibilité d'afficher les fichiers dans DOCX (docx aperçu est disponible avec la version Android 4 +)");
			siteBtn.setText("Acheter");
		} else if(loadCurrentLanguage().equals("uk")){
			developerTV.setText("Для того щоб прибрати рекламу потрібно придбати додаток «FullReader Plus». При активації даного доповнення в FullReader буде прибрана реклама і додана можливість переглядати файли у форматі DOCX (перегляд docx доступний з версії Android 4 +)");
			siteBtn.setText("Придбати");
		} else if(loadCurrentLanguage().equals("ru")){
			developerTV.setText("Для того чтобы убрать рекламу нужно приобрести дополнение «FullReader Plus». При активации данного дополнения в FullReader будет убрана реклама и добавлена возможность просматривать файлы в формате DOCX (просмотр docx доступен с версии Android 4+)");
			siteBtn.setText("Купить");
		} else {
			if(Locale.getDefault().getDisplayLanguage().equals("русский") || Locale.getDefault().getDisplayLanguage().equals("українська")) {
				developerTV.setText("Для того чтобы убрать рекламу нужно приобрести дополнение «FullReader Plus». При активации данного дополнения в FullReader будет убрана реклама и добавлена возможность просматривать файлы в формате DOCX (просмотр docx доступен с версии Android 4+)");
				siteBtn.setText("Приобрести");
			} else {
				developerTV.setText("If you want disable ads you need to buy extension 'FullReader Plus'. After activation this extension in FullReader will disabled ads and will be added possibility to view docx files(Docx plugin works only for Android 4+)");
				siteBtn.setText("Buy");
			}
		}
		OnClickSiteBtn();
		
		//initListData();
		//listAdapter = new OurAppsGamesListAdapter(getApplicationContext(), listItems);
		//this.ourAppsGamesList.setAdapter(listAdapter);
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .addTestDevice("TEST_DEVICE_ID")
            .build();
        adView.loadAd(adRequest);
	 }
	 	
	 public String loadCurrentLanguage() {
		    sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
		    return sPref.getString("curLanguage", "");
     }
	 
	@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
				finish();
			}
			return false;
	}
	private void OnClickSiteBtn() {
		// TODO Auto-generated method stub
		
		siteBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Uri uri = Uri.parse("market://details?id=com.webprestige.fullreaderplus");
				Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
			            try {
			                startActivity(myAppLinkToMarket);
			            } catch (ActivityNotFoundException e) {
		            		
			            }
			}
		});
	}
}
