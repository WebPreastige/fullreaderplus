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

public interface IConstants{

	String THEME_PREF = "theme_name";
	String LANG_PREF = "lang_name";
	String CURRENT_LANGUAGE = "";

	int THEME_MYBLACK = 0;
	int THEME_LAMINAT = 1;
	int THEME_REDTREE = 2;

	String THEME_MYBLACK_VALUE = "0";
	String THEME_LAMINAT_VALUE = "1";
	String THEME_REDTREE_VALUE = "2";

	long REMINDER_DEFAULT = 0;
	long DAY_START_DEFAULT = 0;
	long NIGHT_START_DEFAULT = 0;
	long AUTOPAGE_DEFAULT = 0;

	//String FACEBOOK_APP_ID = "504996749585628";
	//String FACEBOOK_APP_ID = "363318213799858";
	String FACEBOOK_APP_ID = "913376495359149";
	String PREF_LOCALE = "pref_locale";
}
