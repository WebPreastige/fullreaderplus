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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import org.geometerplus.android.fbreader.preferences.PreferenceActivity;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.plus.PlusShare;
import com.webprestige.fr.about.AboutActivity;
import com.webprestige.fr.networklibs.HowToAddOPDS;
import com.fullreader.R;
//import com.google.ads.AdView;

public class FaqActivity extends BaseActivity {

	private ListView listView;
	//private AdView adView;
	public static String curLanguage = "";
	private SharedPreferences sPref;
	private String aboutTitle;
	private String optionalFuncTitle;
	private String optionalFuncText = "";
	private String frSiteTitle;
	private String optionalfrSiteTitle = "";
	private int googlePlusIcon = 0;
	private int settingsIcon = 0;
	private String vkTitle;
	private String opdsTitle = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_activity);
		initActionBar();
        getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		listView = (ListView)findViewById(R.id.listView1);
		
		FaqAdapter adapter = new FaqAdapter(this);
		if(loadCurrentLanguage().equals("en")) {
			opdsTitle = "How to add network libraries";
			aboutTitle = "Remove ads";
			frSiteTitle = "Site FullReader+";
			vkTitle = "Our group on vk.com";
			optionalFuncTitle = "Additional functions";
			optionalFuncText = "-When reading the menu, you can choose to view the entire screen.\n"+
				"-Adding any TTF / OTF fonts (put them in a folder Fonts, / sdcard / Fonts, then just choose your desired settings font);\n"+
				"-When you select a set of buttons appears citations to save, send citations in the social. network, copy to clipboard and transfer through an interpreter.";
    	} else if(loadCurrentLanguage().equals("ru")) {
    		opdsTitle = "Как добавить сетевые библиотеки";
    		aboutTitle = "Убрать рекламу";
    		frSiteTitle = "Сайт FullReader+";
    		vkTitle = "Наша группа на vk.com";
    		optionalFuncText = "-При чтении в меню можно выбрать режим просмотра на весь экран.\n " +
					"-Добавление  любых TTF/OTF шрифтов (поместите их в папку Fonts, /sdcard/Fonts, затем просто в настройках выбирайте нужный вам шрифт);\n "+
					"При выделении цитаты появляется набор кнопок для сохранения, отправки цитаты в соц. сети, копирования в буфер обмена и перевода с помощью переводчика.";
    		optionalFuncTitle = "Дополнительные функции";
    	} else if(loadCurrentLanguage().equals("fr")) {
    		opdsTitle = "Comment ajouter des bibliothèques du réseau";
    		aboutTitle = "supprimer les annonces";
    		frSiteTitle = "Site";
    		vkTitle = "Notre groupe sur vk.com";
    		optionalFuncText = "-Lors de la lecture du menu, vous pouvez choisir d'afficher la totalité de l'écran.\n"+
    				"-Ajout de toutes les polices TTF / OTF (les mettre dans un dossier Fonts, / sdcard / Fonts, puis il suffit de choisir votre police de paramètres voulu);\n"+
    				"-Lorsque vous sélectionnez un ensemble de boutons apparaît citations à enregistrer, envoyer des citations dans le social. réseau, copiez au presse-papiers et de les transférer par le biais d'un interprète.";
    		optionalFuncTitle = "Fonctions supplémentaires";
    	} else if(loadCurrentLanguage().equals("de")) {
    		opdsTitle = "Wie Netzwerk-Bibliotheken hinzufügen";
    		aboutTitle = "Anzeigen zu entfernen";
    		vkTitle = "Unsere Gruppe auf vk.com";
    		frSiteTitle = "Standort FullReader+";
    		optionalFuncText = "Beim Lesen der Menü können Sie wählen, um den gesamten Bildschirm zu sehen.\n " +
					"-Hinzufügen keine TTF / OTF-Schriften (setzen Sie sie in einem Ordner Fonts, / sdcard / Fonts, dann nur die gewünschten Einstellungen Schriftart auswählen);\n "+
					"-Wenn Sie eine Reihe von Tasten wählen erscheint Zitate speichern, senden und Zitate in der sozialen. Netzwerk, in die Zwischenablage kopieren und übertragen durch einen Dolmetscher.";
    		optionalFuncTitle = "Zusatzfunktionen";
    	} else if(loadCurrentLanguage().equals("uk")) {
    		opdsTitle = "Як додати онлайн бібліотеки";
    		aboutTitle = "прибрати рекламу";
    		frSiteTitle = "Сайт FullReader+";
    		vkTitle = "Наша група на vk.com";
    		optionalFuncText = "-При читанні в меню можна вибрати режим перегляду на весь екран.\n " +
					"-Додавання будь-яких TTF / OTF шрифтів (помістіть їх в папку Fonts, / sdcard / Fonts, потім просто в настройках вибирайте потрібний вам шрифт);\n "+
					"При виділенні цитати з'являється набір кнопок для збереження, відправки цитати в соц. мережі, копіювання в буфер обміну і переказу за допомогою перекладача.";
    		optionalFuncTitle = "Додаткові функції";
    	} else {
    		if(Locale.getDefault().getDisplayLanguage().equals("русский") ) {
    			opdsTitle = "Как добавить сетевые библиотеки";
				aboutTitle = "Убрать рекламу";
				frSiteTitle = "Сайт FullReader+";
				vkTitle = "Наша группа на vk.com";
				optionalFuncText = "-При чтении в меню можно выбрать режим просмотра на весь экран.\n " +
						"-Добавление  любых TTF/OTF шрифтов (поместите их в папку Fonts, /sdcard/Fonts, затем просто в настройках выбирайте нужный вам шрифт);\n "+
						"При выделении цитаты появляется набор кнопок для сохранения, отправки цитаты в соц. сети, копирования в буфер обмена и перевода с помощью переводчика.";
				optionalFuncTitle = "Дополнительные функции";
			}else if(Locale.getDefault().getDisplayLanguage().equals("українська")){
				aboutTitle = "Прибрати рекламу";
				opdsTitle = "Як додати онлайн бібліотеки";
				vkTitle = "Наша група на vk.com";
				frSiteTitle = "Сайт FullReader+";
				optionalFuncText = "-При читанні в меню можна вибрати режим перегляду на весь екран.\n " +
						"-Додавання будь-яких TTF / OTF шрифтів (помістіть їх в папку Fonts, / sdcard / Fonts, потім просто в настройках вибирайте потрібний вам шрифт);\n "+
						"При виділенні цитати з'являється набір кнопок для збереження, відправки цитати в соц. мережі, копіювання в буфер обміну і переказу за допомогою перекладача.";
			} else {
				opdsTitle = "How to add network libraries";
				aboutTitle = "Remove ads"; 
				vkTitle = "Our group on vk.com";
				frSiteTitle = "Site FullReader+";
				optionalFuncTitle = "Additional functions";
			}
    	}
		for(int i=0; i<9; i++) {
			FaqItem item = new FaqItem();
			item.order = i+1;
			item.name = ZLResource.resource("faq").getResource("quest"+i).getValue();
			item.text = ZLResource.resource("faq").getResource("quest"+i+"_answer").getValue();
			item.clr = Color.BLACK;
			adapter.add(item);
		}
		
		/*FaqItem optionalFuncItem = new FaqItem();
		optionalFuncItem.order = 6;
		optionalFuncItem.name = optionalFuncTitle;
		optionalFuncItem.text = ZLResource.resource("faq").getResource("quest6_answer").getValue();
		optionalFuncItem.clr = Color.BLACK;
		adapter.add(optionalFuncItem);
		
		FaqItem aboutFRitem = new FaqItem();
		aboutFRitem.order = 7;
		aboutFRitem.name = aboutTitle;
		aboutFRitem.text = "FR";
		aboutFRitem.clr = Color.BLACK;
		adapter.add(aboutFRitem);*/
		
		
		FaqItem thirdPartyLibs = new FaqItem();
		thirdPartyLibs.order = 10;
		thirdPartyLibs.name = ZLResource.resource("faq").getResource("quest10").getValue();
		thirdPartyLibs.text = ZLResource.resource("faq").getResource("quest10_answer").getValue();
		thirdPartyLibs.clr = Color.BLACK;
		adapter.add(thirdPartyLibs);
		
		FaqItem vkItem = new FaqItem();
		vkItem.order = 11;
		vkItem.name = vkTitle;
		vkItem.text = "frVK";
		vkItem.clr = Color.BLUE;
		adapter.add(vkItem);
		
		FaqItem frSiteitem = new FaqItem();
		frSiteitem.order = 12;
		frSiteitem.name = frSiteTitle;
		frSiteitem.text = "frTitle";
		frSiteitem.clr = Color.BLUE;
		adapter.add(frSiteitem);
		
		

		
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(((FaqItem)(arg0.getItemAtPosition(arg2))).text.equals("FR")) {
					Intent intent = new Intent(FaqActivity.this, AboutActivity.class);
					startActivity(intent);
				} else if(((FaqItem)(arg0.getItemAtPosition(arg2))).text.equals("frVK")) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW,
							Uri.parse("http://vk.com/fullreader"));
					startActivity(browserIntent);
				} else if(((FaqItem)(arg0.getItemAtPosition(arg2))).text.equals("frTitle")) {
					String url = "http://www.fullreader.info";
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivity(i);
				} else if(((FaqItem)(arg0.getItemAtPosition(arg2))).text.equals("opdsTitle")) {
					Intent intent = new Intent(FaqActivity.this, HowToAddOPDS.class);
					startActivity(intent);
				} else {
					Intent intent = new Intent(FaqActivity.this, FaqAnswerActivity.class);
					intent.putExtra(FaqAnswerActivity.ANSWER_TEXT, ((FaqItem)(arg0.getItemAtPosition(arg2))).text);
					intent.putExtra(FaqAnswerActivity.ANSWER_TITLE, ((FaqItem)(arg0.getItemAtPosition(arg2))).name);
					startActivity(intent );
				} 				
			}
		});
		
		ImageView imgBackground = (ImageView)findViewById(R.id.img_faq_white_bg);
		switch(theme){
	        case IConstants.THEME_MYBLACK:
	    		imgBackground.setImageResource(R.drawable.bg_white);
	    		googlePlusIcon = R.drawable.google_plus_marble;
	    		settingsIcon = R.drawable.settings_icon_black;
	        	break;
	        case IConstants.THEME_LAMINAT:
	    		imgBackground.setImageResource(R.drawable.bg_white);
	    		googlePlusIcon = R.drawable.google_plus_laminat;
	    		settingsIcon = R.drawable.settings_icon;
	        	break;
	        case IConstants.THEME_REDTREE:
	    		imgBackground.setImageResource(R.drawable.bg_white);
	    		googlePlusIcon = R.drawable.google_plus_red_tree;
	    		settingsIcon = R.drawable.settings_icon_red;
	        	break;
		}
		//Log.d("checkADS: ", String.valueOf(checkAds()));
		/*if(!checkAds()) {
			initAdMob();
		}*/
	}
	
	public static String convertStreamToString(InputStream is) throws Exception {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	    StringBuilder sb = new StringBuilder();
	    String line = null;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line).append("\n");
	    }
	    reader.close();
	    return sb.toString();
	}
	
	private boolean checkAds() {
		boolean access = false;
		try {
			File root = new File(Environment.getExternalStorageDirectory(), "FullReader Unlocker");
			File accessFile = new File(root, "access-file.txt");
			if(!accessFile.exists()) {
				access = false;
			} else {
				FileInputStream fin = new FileInputStream(accessFile);
				String fileString;
				fileString = convertStreamToString(fin);
			    //Make sure you close all streams.
			    fin.close(); 
			    String [] tmpStr = fileString.split("\\|");
			    String [] adsStr = tmpStr[1].split("\\:");
			    Log.d("ads access: ", String.valueOf(Integer.parseInt(adsStr[1].trim())));
			    if(Integer.parseInt(adsStr[1].trim()) == 0) {
			    	access = true;
			    }
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return access;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);  
        addMenuItem(menu, 0, "Google Plus", googlePlusIcon, true).setTitle("Google+");
        addMenuItem(menu, 2, "VK", R.drawable.vk, true).setTitle("VK");
        if(loadCurrentLanguage().equals("en")){
        	addMenuItem(menu, 1, "Settings", settingsIcon, true).setTitle("Settings");
			//menu.add(0, MENU_QUOTES, 0, "Цитати");
		} else if(loadCurrentLanguage().equals("de")){
			addMenuItem(menu, 1, "Settings", settingsIcon, true).setTitle("Einstellungen");
			//menu.add(0, MENU_QUOTES, 0, "Цитати");
		} else if(loadCurrentLanguage().equals("fr")){
			addMenuItem(menu, 1, "Settings", settingsIcon, true).setTitle("Paramètres");
		} else if(loadCurrentLanguage().equals("uk")){
			//addMenuItem(menu, ActionCode.SHOW_QUOTES, resQuotes);
			addMenuItem(menu, 1, "Settings", settingsIcon, true).setTitle("Настройки");
		} else if(loadCurrentLanguage().equals("ru")){
			addMenuItem(menu, 1, "Settings", settingsIcon, true).setTitle("Настройки");
		} else {
			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
				addMenuItem(menu, 1, "Settings", settingsIcon, true).setTitle("Настройки");
			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
				addMenuItem(menu, 1, "Settings", settingsIcon, true).setTitle("Настройки");
			} else {
				addMenuItem(menu, 1, "Settings", settingsIcon, true).setTitle("Settings");
			}
		}
		return true;
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case 0:{
			try {
				Intent shareIntent = new PlusShare.Builder(FaqActivity.this)
	            .setType("text/plain")
	            .setText("The best reader for Android! http://play.google.com/store/apps/details?id=com.fullreader")
	            .setContentUrl(Uri.parse("https://developers.google.com/+/"))
	            .getIntent();
				
				startActivityForResult(shareIntent, 0);
			} catch(ActivityNotFoundException e) {
				if(loadCurrentLanguage().equals("en")){
					Toast.makeText(getBaseContext(), "Add Google account.", Toast.LENGTH_LONG).show();
	    		} else if(loadCurrentLanguage().equals("de")){
	    			Toast.makeText(getBaseContext(), "Google Konto hinzufügen.", Toast.LENGTH_LONG).show();
	    		} else if(loadCurrentLanguage().equals("fr")){
	    			Toast.makeText(getBaseContext(), "Ajouter un compte Google.", Toast.LENGTH_LONG).show();
	    		} else if(loadCurrentLanguage().equals("uk")){
	    			Toast.makeText(getBaseContext(), "Додайте аккаунт Google.", Toast.LENGTH_LONG).show();
	    		} else if(loadCurrentLanguage().equals("ru")){
	    			Toast.makeText(getBaseContext(), "Добавьте аккаунт Google.", Toast.LENGTH_LONG).show();
	    		} else {
	    			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
	    				Toast.makeText(getBaseContext(), "Добавьте аккаунт Google.", Toast.LENGTH_LONG).show();
	    			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")) {
	    				Toast.makeText(getBaseContext(), "Додайте аккаунт Google.", Toast.LENGTH_LONG).show();
	    			} else {
	    				Toast.makeText(getBaseContext(), "Add Google account.", Toast.LENGTH_LONG).show();
	    			}
	    		}
			}
			return true;
			}
		case 1: 
			Intent intentSettings = new Intent(this, PreferenceActivity.class);
		    startActivityForResult(intentSettings, FullReaderActivity.REQUEST_PREFERENCES);
			//startActivity(intent);
		    return true;
		case 2: 
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://vk.com/fullreader"));
			startActivity(browserIntent);
		    return true;
		}
		return false;
	}
	
	public String loadCurrentLanguage() {
	    sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
	    return sPref.getString("curLanguage", "");
    }
	
	 private void initActionBar() {
		 ActionBar bar = getSupportActionBar();
	     Drawable actionBarBackground = null;
	     int theme = PreferenceManager.getDefaultSharedPreferences(this).getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE);
	     switch(theme){
		        case IConstants.THEME_MYBLACK:
		         actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_black_action_bar );
		         break;
		        case IConstants.THEME_LAMINAT:
		         actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_laminat_action_bar );
		         break;
		        case IConstants.THEME_REDTREE:
		         actionBarBackground = getResources().getDrawable(com.fullreader.R.drawable.theme_redtree_action_bar );
		         break;
	     }      
	     bar.setBackgroundDrawable(actionBarBackground);
	 }
	
	/*private void initAdMob() {
		AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
            .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
            .addTestDevice("TEST_DEVICE_ID")
            .build();
        adView.loadAd(adRequest);
	}*/
	
	class FaqItem{
		int order;
		String name;
		String text;
		int clr;
	}
	
	class FaqAdapter extends ArrayAdapter<FaqItem>{

		public FaqAdapter(Context context) {
			super(context, 0);
			// TODO Auto-generated constructor stub
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout body = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.faq_list_item, null);
			
			TextView order = (TextView) body.findViewById(R.id.tw_order);
			TextView text = (TextView) body.findViewById(R.id.tw_text);
			
			order.setText(""+getItem(position).order);
			text.setText(getItem(position).name);

			order.setTextColor(getItem(position).clr);
			text.setTextColor(getItem(position).clr);
			
			return body;
		}
		
		

	}

}
