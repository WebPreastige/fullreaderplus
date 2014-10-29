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
package com.webprestige.fr.networklibs;

import java.util.Locale;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fullreader.R;

public class HowToAddOPDS extends Activity{

	private TextView populatOpdsTV;
	private ListView opdsList;
	private ArrayAdapter<String> listAdapter;
	String[] opdsLibsArray = {  
			"http://flibusta.net/opds",
			"http://lib.rus.ec/opds",
			"http://www.e-reading.ws/opds)",
			"http://coollib.net/opds",
			"http://maxima-library.org/opds",
			"http://www.zone4iphone.ru/catalog.php",
			"http://www.epubbooks.ru/index.xml",
			"http://dimonvideo.ru/lib.xml",
			"http://www.feedbooks.com/catalog.atom",
			"http://lib.rus.ec/opds",
			"http://books.vnuki.org/opds.xml"			
	};
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.how_to_add_opds);
		
		
		initOpdsList();
		
	}
	
	private void initTitle() {
		populatOpdsTV = (TextView)findViewById(R.id.popular_opds_tv);
		if(Locale.getDefault().getDisplayLanguage().equals("русский") || Locale.getDefault().getDisplayLanguage().equals("українська")) {
			populatOpdsTV.setText("Популярные онлайн библиотеки");
		}  else if(loadCurrentLanguage().equals("uk")){
			populatOpdsTV.setText("Популярные онлайн библиотеки");
		} else if(loadCurrentLanguage().equals("ru")){
			populatOpdsTV.setText("Популярные онлайн библиотеки");
		} else if(loadCurrentLanguage().equals("en")){
			populatOpdsTV.setText("Popular online libraries");
		} else {
			populatOpdsTV.setText("Popular online libraries");
		}
	}
	
	private void initOpdsList() {
		opdsList = (ListView)findViewById(R.id.opds_list);
		listAdapter = new ArrayAdapter<String>(this,
		        android.R.layout.simple_list_item_1, opdsLibsArray);
		opdsList.setAdapter(listAdapter);
	}
	
	public String loadCurrentLanguage() {
	    SharedPreferences sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
	    return sPref.getString("curLanguage", "");
 }
	@Override
	public void onResume() {
		super.onResume();
		initTitle();
	}
}
