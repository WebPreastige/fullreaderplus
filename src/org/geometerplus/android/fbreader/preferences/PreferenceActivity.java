/*
* FullReader+
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
 * Copyright (C) 2009-2013 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.android.fbreader.DictionaryUtil;
import org.geometerplus.android.fbreader.FullReaderActivity;
import org.geometerplus.android.fbreader.IConstants;
import org.geometerplus.android.fbreader.ReaderApplication;
import org.geometerplus.android.fbreader.StartScreenActivity;
import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.android.fbreader.libraryService.BookCollectionShadow;
import org.geometerplus.android.util.UIUtil;
import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.fbreader.fbreader.ActionCode;
import org.geometerplus.fbreader.fbreader.ColorProfile;
import org.geometerplus.fbreader.fbreader.ReaderApp;
import org.geometerplus.fbreader.fbreader.ScrollingPreferences;
import org.geometerplus.fbreader.fbreader.TapZoneMap;
import org.geometerplus.zlibrary.core.application.ZLKeyBindings;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.options.ZLIntegerOption;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.text.view.style.ZLTextBaseStyle;
import org.geometerplus.zlibrary.text.view.style.ZLTextFullStyleDecoration;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleDecoration;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;
import org.geometerplus.zlibrary.ui.android.view.ZLAndroidPaintContext;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.ads.AdView;
import com.webprestige.fr.dropbox.DropboxHelper;
import com.fullreader.R;

public class PreferenceActivity extends ZLPreferenceActivity  implements  OnSharedPreferenceChangeListener{
	private static String NEEDS_RESTART = "needs_restart";
	private boolean mFromTheme = false;
	
	private BookCollectionShadow myCollection = new BookCollectionShadow();
	private TimeSwitchPreference timePref;
	private TimePreference dayNight;
	private ReminderChoosePreference remindPref;
	private boolean runFromBook = false;
	
	public static boolean isOpenFromPdfDjvu = false;
	private SharedPreferences mPreferences;
	private AdView adView;

	// Массивы со значениями для выравнивания фона
	private String [] russianValues = new String[] {"Плитка", "По центру", "Заполнение", "Оригинал"};
	private String [] ukrainianValues = new String[] {"Плитка", "По центру", "Заповнення", "Оригінал"};
	private String [] englishValues = new String [] {"Tile", "Center" , "Fill", "Original"};
    private String [] germanValues = new String [] {"Fliese", "Zentriert", "Füllung", "Original"};
    private String [] frenchValues = new String [] { "Carreau", "Centré", "Remplissage", "Original"};
	private String russianTitle = "Выравнивание фона";
	private String ukrainianTitle = "Вирівнювання фону";
	private String englishTitle = "Background alignment";
	private String germanTitle = "intergrund Ausrichtung";
	private String frenchTitle = "Alignez fond";
	
	private String HOR_TAPZONE_CAT = "hor_tapzone_cat";
	private String VER_TAPZONE_CAT = "ver_tapzone_cat";
	
	TapzonesListPreference tapzonesListPrefs;
	private PreferenceCategory horTapzoneCat;
	private PreferenceCategory verTapzoneCat;
	private Screen tapScreen; 
	
	TapzoneOptionsPreference horTopPref;
	TapzoneOptionsPreference horCenterPref;
	TapzoneOptionsPreference horBottomPref;
	
	TapzoneOptionsPreference verLeftPref;
	TapzoneOptionsPreference verCenterPref;
	TapzoneOptionsPreference verRightPref;
	
	CheckBoxPreference tapDefault;
	
	private static String tapDefaultKey = "tapDefaultKey";
	private static String TAPZONE_DEFAULT = "left_to_right";
	
	private String [] TAP_ACTIONS = new String []{
			ActionCode.TURN_PAGE_FORWARD,
			ActionCode.TURN_PAGE_BACK,
			ActionCode.SWITCH_TO_DAY_PROFILE,
			ActionCode.SWITCH_TO_NIGHT_PROFILE,
			ActionCode.SHOW_BOOK_INFO,
			ActionCode.SHOW_BOOKMARKS,
			ActionCode.SHOW_QUOTES,
			ActionCode.SHOW_PREFERENCES,
			ActionCode.INCREASE_FONT,
			ActionCode.DECREASE_FONT,
			ActionCode.SHOW_TOC, 
			ActionCode.SEARCH,
			ActionCode.SHOW_NAVIGATION,
			ActionCode.EXIT,
			ActionCode.SHOW_LIBRARY,
			ActionCode.SHOW_MENU,
			ActionCode.SHOW_COLOR_PREFERENCES,
			ActionCode.NEXT_BOOK,
			ActionCode.PREVIOUS_BOOK,
			ActionCode.FULLSCREEN_MODE
	};
	
	private ZLBooleanPreference mDbxBoolPref;
	private boolean showColorsScreen = false;
	public static String OPEN_COLOR_PREFERENCES = "open_color_preferences";
	private Preference tapzoneDoubleTapPref;
	
	public PreferenceActivity() {
		super("Preferences");
	}

	@Override
	protected void onStart() {
		super.onStart();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.registerOnSharedPreferenceChangeListener(this);
		myCollection.bindToService(this, null);
	}

	@Override
	protected void onStop() {
		myCollection.unbind();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.unregisterOnSharedPreferenceChangeListener(this);

		super.onStop();
	}

	@Override
	protected void init(Intent intent) {
		try {
			this.runFromBook = getIntent().getExtras().getBoolean("fromBook", false);
		} catch(NullPointerException e) {
			this.runFromBook = false;
		}
		if (getIntent()!=null){
			Intent myIntent = getIntent();
			if (myIntent.hasExtra(PreferenceActivity.OPEN_COLOR_PREFERENCES)){
				showColorsScreen = true;
			}
		}
		setResult(FullReaderActivity.RESULT_REPAINT);

		ReaderApp myReaderApp = (ReaderApp)ReaderApp.Instance();
		if (myReaderApp == null) {
			myReaderApp = new ReaderApp(new BookCollectionShadow());
		}
		final ZLAndroidLibrary androidLibrary = (ZLAndroidLibrary)ZLAndroidLibrary.Instance();
		final ColorProfile profile = myReaderApp.getColorProfile();
		final String profileName = myReaderApp.getColorProfileName();
		
		//androidLibrary.setActivity(Reader.getInstance());
		//androidLibrary.setActivity(intent.getC);

//		final ZLAndroidApplication androidApplication = (ZLAndroidApplication)getApplication();
//		if (androidApplication.myMainWindow == null) {
//			androidApplication.myMainWindow = new ZLAndroidApplicationWindow(myReaderApp);
//			myReaderApp.initWindow();
//		}

		//GENERAL
		Screen appearanceScreen = null;
		Screen GeneralScreen = createPreferenceScreen("general");
		if(!isOpenFromPdfDjvu) {
			appearanceScreen = createPreferenceScreen("appearance");
		}
		Log.d("runFromBookPREFS: ", String.valueOf(runFromBook));
		if (runFromBook) {
			if(android.os.Build.VERSION.SDK_INT >= 11 ) {
				GeneralScreen.addPreference(new LanguagePreference(
						this, appearanceScreen.Resource, "language", ZLResource.languages()
						) {
					
					@Override
					protected void init() {
						setInitialValue(ZLResource.LanguageOption.getValue());
					}
					
					@Override
					protected void setLanguage(String code) {
						if (!code.equals(ZLResource.LanguageOption.getValue())) {
							ZLResource.LanguageOption.setValue(code);
		//					startActivity(new Intent(
		//							Intent.ACTION_VIEW, Uri.parse("reader-action:preferences#appearance")
		//							));
								Editor editor = PreferenceManager.getDefaultSharedPreferences(PreferenceActivity.this).edit();
								editor.putString(IConstants.PREF_LOCALE, code);
								editor.commit();
								recreatethis();
						}
					}
				});
				
				appearanceScreen.addPreference(new ThemeListPreference(this){

					@Override
					public void updatePref() {
//						startActivity(new Intent(
//								Intent.ACTION_VIEW, Uri.parse("reader-action:preferences#general")
//								));
//						finish();
						mFromTheme = true;
						recreatethis();
					}});
			}
		} else {
			if(isOpenFromPdfDjvu) {
				
			}
			if(!isOpenFromPdfDjvu) {
			GeneralScreen.addPreference(new LanguagePreference(
					this, appearanceScreen.Resource, "language", ZLResource.languages()
					) {
				
				@Override
				protected void init() {
					setInitialValue(ZLResource.LanguageOption.getValue());
				}
	
				@Override
				protected void setLanguage(String code) {
					if (!code.equals(ZLResource.LanguageOption.getValue())) {
						ZLResource.LanguageOption.setValue(code);
	//					startActivity(new Intent(
	//							Intent.ACTION_VIEW, Uri.parse("reader-action:preferences#appearance")
	//							));
							Editor editor = PreferenceManager.getDefaultSharedPreferences(PreferenceActivity.this).edit();
							editor.putString(IConstants.PREF_LOCALE, code);
							editor.commit();
							recreatethis();
					}
				}
			});
			
				appearanceScreen.addPreference(new ThemeListPreference(this){
	
					@Override
					public void updatePref() {
	//					startActivity(new Intent(
	//							Intent.ACTION_VIEW, Uri.parse("reader-action:preferences#general")
	//							));
	//					finish();
						mFromTheme = true;
						recreatethis();
					}});
			}
		}
		//REMINDER
		GeneralScreen.addPreference(new ZLBooleanPreference(
				this,
				myReaderApp.ReaderOption,
				GeneralScreen.Resource,
				"reminder"
				) {
			@Override
			protected void onClick() {
				super.onClick();
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("needToRemind", isChecked());
				editor.commit();
			}
		});

		remindPref = new ReminderChoosePreference(this);
		remindPref.setEnabled(PreferenceManager.getDefaultSharedPreferences(getBaseContext())
				.getBoolean("needToRemind", false));
		
		GeneralScreen.addPreference(remindPref);
		if(!isOpenFromPdfDjvu) {
			GeneralScreen.addPreference(new OrientationChangePreference(
					this, GeneralScreen.Resource, "screenOrientation",
					androidLibrary.OrientationOption, androidLibrary.allOrientations()
					));
	
			/*		
			final Screen directoriesScreen = createPreferenceScreen("directories");
			directoriesScreen.addPreference(new ZLStringOptionPreference(
				this, Paths.BooksDirectoryOption(), directoriesScreen.Resource, "books"
			) {
				protected void setValue(String value) {
					super.setValue(value);
					myCollection.reset(false);
				}
			});
			directoriesScreen.addOption(Paths.FontsDirectoryOption(), "fonts");
			directoriesScreen.addOption(Paths.WallpapersDirectoryOption(), "wallpapers");
			 */
	
			//		appearanceScreen.addPreference(new ZLStringChoicePreference(
			//				this, appearanceScreen.Resource, "screenOrientation",
			//				androidLibrary.OrientationOption, androidLibrary.allOrientations()
			//			));
				
			appearanceScreen.addPreference(new ZLBooleanPreference(
					this,
					myReaderApp.AllowScreenBrightnessAdjustmentOption,
					appearanceScreen.Resource,
					"allowScreenBrightnessAdjustment"
					) {
				private final int myLevel = androidLibrary.ScreenBrightnessLevelOption.getValue();
				
				@Override
				protected void onClick() {
					super.onClick();
					androidLibrary.ScreenBrightnessLevelOption.setValue(isChecked() ? myLevel : 0);
				}
			});
			
			/*appearanceScreen.addPreference(new BatteryLevelToTurnScreenOffPreference(
				this,
				androidLibrary.BatteryLevelToTurnScreenOffOption,
				appearanceScreen.Resource,
				"dontTurnScreenOff"
			));
			
			appearanceScreen.addPreference(new ZLBooleanPreference(
				this,
				androidLibrary.DontTurnScreenOffDuringChargingOption,
				appearanceScreen.Resource,
				"dontTurnScreenOffDuringCharging"
			));
			
			appearanceScreen.addOption(androidLibrary.ShowStatusBarOption, "showStatusBar");
			appearanceScreen.addOption(androidLibrary.DisableButtonLightsOption, "disableButtonLights");
			
			 */
			
			final Screen textScreen = createPreferenceScreen("text");
			final Screen fontPropertiesScreen = textScreen.createPreferenceScreen("fontProperties");
			fontPropertiesScreen.addOption(ZLAndroidPaintContext.AntiAliasOption, "antiAlias");
			fontPropertiesScreen.addOption(ZLAndroidPaintContext.DeviceKerningOption, "deviceKerning");
			fontPropertiesScreen.addOption(ZLAndroidPaintContext.DitheringOption, "dithering");
			fontPropertiesScreen.addOption(ZLAndroidPaintContext.SubpixelOption, "subpixel");
			
			final ZLTextStyleCollection collection = ZLTextStyleCollection.Instance();
			final ZLTextBaseStyle baseStyle = collection.getBaseStyle();
			
			textScreen.addPreference(new FontOption(
					this, textScreen.Resource, "font",
					baseStyle.FontFamilyOption, false
					));
			textScreen.addPreference(new ZLIntegerRangePreference(
					this, textScreen.Resource.getResource("fontSize"),
					baseStyle.FontSizeOption
					));
			textScreen.addPreference(new FontStylePreference(
					this, textScreen.Resource, "fontStyle",
					baseStyle.BoldOption, baseStyle.ItalicOption
					));
			final ZLIntegerRangeOption spaceOption = baseStyle.LineSpaceOption;
			final String[] spacings = new String[spaceOption.MaxValue - spaceOption.MinValue + 1];
			for (int i = 0; i < spacings.length; ++i) {
				final int val = spaceOption.MinValue + i;
				spacings[i] = (char)(val / 10 + '0') + "." + (char)(val % 10 + '0');
			}
			textScreen.addPreference(new ZLChoicePreference(
					this, textScreen.Resource, "lineSpacing",
					spaceOption, spacings
					));
			final String[] alignments = { "left", "right", "center", "justify" };
			textScreen.addPreference(new ZLChoicePreference(
					this, textScreen.Resource, "alignment",
					baseStyle.AlignmentOption, alignments
					));
			textScreen.addOption(baseStyle.AutoHyphenationOption, "autoHyphenations");
	
			final Screen moreStylesScreen = textScreen.createPreferenceScreen("more");
			
			byte styles[] = {
					FBTextKind.REGULAR,
					FBTextKind.TITLE,
					FBTextKind.SECTION_TITLE,
					FBTextKind.SUBTITLE,
					FBTextKind.H1,
					FBTextKind.H2,
					FBTextKind.H3,
					FBTextKind.H4,
					FBTextKind.H5,
					FBTextKind.H6,
					FBTextKind.ANNOTATION,
					FBTextKind.EPIGRAPH,
					FBTextKind.AUTHOR,
					FBTextKind.POEM_TITLE,
					FBTextKind.STANZA,
					FBTextKind.VERSE,
					FBTextKind.CITE,
					FBTextKind.INTERNAL_HYPERLINK,
					FBTextKind.EXTERNAL_HYPERLINK,
					FBTextKind.FOOTNOTE,
					FBTextKind.ITALIC,
					FBTextKind.EMPHASIS,
					FBTextKind.BOLD,
					FBTextKind.STRONG,
					FBTextKind.DEFINITION,
					FBTextKind.DEFINITION_DESCRIPTION,
					FBTextKind.PREFORMATTED,
					FBTextKind.CODE
			};
			for (int i = 0; i < styles.length; ++i) {
				final ZLTextStyleDecoration decoration = collection.getDecoration(styles[i]);
				if (decoration == null) {
					continue;
				}
				ZLTextFullStyleDecoration fullDecoration =
						decoration instanceof ZLTextFullStyleDecoration ?
								(ZLTextFullStyleDecoration)decoration : null;
	
								final Screen formatScreen = moreStylesScreen.createPreferenceScreen(decoration.getName());
								formatScreen.addPreference(new FontOption(
										this, textScreen.Resource, "font",
										decoration.FontFamilyOption, true
										));
								formatScreen.addPreference(new ZLIntegerRangePreference(
										this, textScreen.Resource.getResource("fontSizeDifference"),
										decoration.FontSizeDeltaOption
										));
								formatScreen.addPreference(new ZLBoolean3Preference(
										this, textScreen.Resource, "bold",
										decoration.BoldOption
										));
								formatScreen.addPreference(new ZLBoolean3Preference(
										this, textScreen.Resource, "italic",
										decoration.ItalicOption
										));
								formatScreen.addPreference(new ZLBoolean3Preference(
										this, textScreen.Resource, "underlined",
										decoration.UnderlineOption
										));
								formatScreen.addPreference(new ZLBoolean3Preference(
										this, textScreen.Resource, "strikedThrough",
										decoration.StrikeThroughOption
										));
								if (fullDecoration != null) {
									final String[] allAlignments = { "unchanged", "left", "right", "center", "justify" };
									formatScreen.addPreference(new ZLChoicePreference(
											this, textScreen.Resource, "alignment",
											fullDecoration.AlignmentOption, allAlignments
											));
								}
								formatScreen.addPreference(new ZLBoolean3Preference(
										this, textScreen.Resource, "allowHyphenations",
										decoration.AllowHyphenationsOption
										));
								if (fullDecoration != null) {
									formatScreen.addPreference(new ZLIntegerRangePreference(
											this, textScreen.Resource.getResource("spaceBefore"),
											fullDecoration.SpaceBeforeOption
											));
									formatScreen.addPreference(new ZLIntegerRangePreference(
											this, textScreen.Resource.getResource("spaceAfter"),
											fullDecoration.SpaceAfterOption
											));
									formatScreen.addPreference(new ZLIntegerRangePreference(
											this, textScreen.Resource.getResource("leftIndent"),
											fullDecoration.LeftIndentOption
											));
									formatScreen.addPreference(new ZLIntegerRangePreference(
											this, textScreen.Resource.getResource("rightIndent"),
											fullDecoration.RightIndentOption
											));
									formatScreen.addPreference(new ZLIntegerRangePreference(
											this, textScreen.Resource.getResource("firstLineIndent"),
											fullDecoration.FirstLineIndentDeltaOption
											));
									final ZLIntegerOption spacePercentOption = fullDecoration.LineSpacePercentOption;
									final int[] spacingValues = new int[17];
									final String[] spacingKeys = new String[17];
									spacingValues[0] = -1;
									spacingKeys[0] = "unchanged";
									for (int j = 1; j < spacingValues.length; ++j) {
										final int val = 4 + j;
										spacingValues[j] = 10 * val;
										spacingKeys[j] = (char)(val / 10 + '0') + "." + (char)(val % 10 + '0');
									}
									formatScreen.addPreference(new ZLIntegerChoicePreference(
											this, textScreen.Resource, "lineSpacing",
											spacePercentOption, spacingValues, spacingKeys
											));
								}
			}
			/*final ZLPreferenceSet bgPreferences = new ZLPreferenceSet();
			
	
			if(profileName.equals(ColorProfile.DAY)){
			final Screen colorsScreen = createPreferenceScreen("colors");
				colorsScreen.addPreference(new WallpaperPreference(
						this, profile, colorsScreen.Resource, "background"
						) {
					@Override
					protected void onDialogClosed(boolean result) {
						super.onDialogClosed(result);
						bgPreferences.setEnabled("".equals(getValue()));
					}
				});
				bgPreferences.add(
						colorsScreen.addOption(profile.BackgroundOption, "#FFFFFF")
					); 
				bgPreferences.setEnabled("".equals(profile.WallpaperOption.getValue()));
			}*/
			final Screen colorsScreen = createPreferenceScreen("colors");
			if(profileName.equals(ColorProfile.DAY)){
				final ZLPreferenceSet bgPreferences = new ZLPreferenceSet();
				final WallpaperPreference wallpaperPreference = new WallpaperPreference(
					this, profile, colorsScreen.Resource, "background"
				) {
					@Override
					protected void onDialogClosed(boolean result) {
						super.onDialogClosed(result);
						bgPreferences.setEnabled("".equals(getValue()));
					}
				};
				colorsScreen.addPreference(wallpaperPreference);
	
				/*
				 * Настройки для выравнивания кастомного фона
				 */
				WallpaperAlignmentPreference wallpaperAlignment = new WallpaperAlignmentPreference(this);
				wallpaperAlignment.setKey(WallpaperAlignmentPreference.WALLPAPER_ALIGN_KEY);
				
				String currentLanguage = loadCurrentLanguage();
				if (currentLanguage.equals("ru")){
					wallpaperAlignment.setTitle(russianTitle);
					wallpaperAlignment.setEntries(russianValues);
				}
				else
				if (currentLanguage.equals("uk")){
					wallpaperAlignment.setTitle(ukrainianTitle);
					wallpaperAlignment.setEntries(ukrainianValues);
				}
				else
				if (currentLanguage.equals("en")){
					wallpaperAlignment.setTitle(englishTitle);
					wallpaperAlignment.setEntries(englishValues);
				}
				else
				if (currentLanguage.equals("de")){
					wallpaperAlignment.setTitle(germanTitle);
					wallpaperAlignment.setEntries(germanValues);
				}
				else
				if (currentLanguage.equals("fr")){
					wallpaperAlignment.setTitle(frenchTitle);
					wallpaperAlignment.setEntries(frenchValues);
				}
				else {
					if(Locale.getDefault().getDisplayLanguage().equals("русский")){
						wallpaperAlignment.setTitle(russianTitle);
						wallpaperAlignment.setEntries(russianValues);
					}
					else if(Locale.getDefault().getDisplayLanguage().equals("українська")){
						wallpaperAlignment.setTitle(ukrainianTitle);
						wallpaperAlignment.setEntries(ukrainianValues);
					}
					else{
						wallpaperAlignment.setTitle(englishTitle);
						wallpaperAlignment.setEntries(englishValues);
					}
				}
				wallpaperAlignment.setEntryValues(R.array.wallpaper_alignment_entry_values);
				// Делаем проверку, может ли опция для заполнения фона быть доступной пользователю
				SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(PreferenceActivity.this);
				boolean isEnabled = prefs.getBoolean(WallpaperAlignmentPreference.WALLPAPER_ALIGN_ENABLED, false);
				wallpaperAlignment.setEnabled(isEnabled);
				wallpaperPreference.setWallpaperAlignmentPreference(wallpaperAlignment);
				colorsScreen.addPreference(wallpaperAlignment);
				/*
				 * 
				 */
				/*bgPreferences.add(
					colorsScreen.addOption(profile.BackgroundOption, "backgroundColor")
				);*/
				bgPreferences.setEnabled("".equals(profile.WallpaperOption.getValue()));
				
				Preference bcgColorPref = colorsScreen.addOption(profile.BackgroundOption, "backgroundColor");
				wallpaperPreference.setBcgColorPreference(bcgColorPref);
				if (wallpaperPreference.getValue().length() != 0) bcgColorPref.setEnabled(false);
				
				colorsScreen.addOption(profile.HighlightingOption, "highlighting");
				colorsScreen.addOption(profile.RegularTextOption, "text");
				colorsScreen.addOption(profile.HyperlinkTextOption, "hyperlink");
				colorsScreen.addOption(profile.VisitedHyperlinkTextOption, "hyperlinkVisited");
				//colorsScreen.addOption(profile.FooterFillOption, "footer");
				colorsScreen.addOption(profile.SelectionBackgroundOption, "selectionBackground");
				colorsScreen.addOption(profile.SelectionForegroundOption, "selectionForeground");
				// Colors SCREEN
				/*colorsScreen.addOption(profile.HighlightingOption, "highlighting");
				colorsScreen.addOption(profile.RegularTextOption, "text");
				colorsScreen.addOption(profile.HyperlinkTextOption, "hyperlink");
				colorsScreen.addOption(profile.VisitedHyperlinkTextOption, "hyperlinkVisited");
				colorsScreen.addOption(profile.FooterFillOption, "footer");
				colorsScreen.addOption(profile.SelectionBackgroundOption, "selectionBackground");
				colorsScreen.addOption(profile.SelectionForegroundOption, "selectionForeground");*/
				//profile
				
				/*	
				colorsScreen.addOption(profile.SelectionBackgroundOption, "selectionBackground");
				colorsScreen.addOption(profile.HighlightingOption, "highlighting");
				colorsScreen.addOption(profile.HyperlinkTextOption, "hyperlink");
				colorsScreen.addOption(profile.VisitedHyperlinkTextOption, "hyperlinkVisited");
				colorsScreen.addOption(profile.FooterFillOption, "footer");
				colorsScreen.addOption(profile.SelectionBackgroundOption, "selectionBackground");
				colorsScreen.addOption(profile.SelectionForegroundOption, "selectionForeground");
		
				final Screen marginsScreen = createPreferenceScreen("margins");
				marginsScreen.addPreference(new ZLIntegerRangePreference(
					this, marginsScreen.Resource.getResource("left"),
					fbReader.LeftMarginOption
				));
				marginsScreen.addPreference(new ZLIntegerRangePreference(
					this, marginsScreen.Resource.getResource("right"),
					fbReader.RightMarginOption
				));
				marginsScreen.addPreference(new ZLIntegerRangePreference(
					this, marginsScreen.Resource.getResource("top"),
					fbReader.TopMarginOption
				));
				marginsScreen.addPreference(new ZLIntegerRangePreference(
					this, marginsScreen.Resource.getResource("bottom"),
					fbReader.BottomMarginOption
				));
		
				final Screen statusLineScreen = createPreferenceScreen("scrollBar");
		
				final String[] scrollBarTypes = {"hide", "show", "showAsProgress", "showAsFooter"};
				statusLineScreen.addPreference(new ZLChoicePreference(
					this, statusLineScreen.Resource, "scrollbarType",
					fbReader.ScrollbarTypeOption, scrollBarTypes
				) {
					@Override
					protected void onDialogClosed(boolean result) {
						super.onDialogClosed(result);
						footerPreferences.setEnabled(
							findIndexOfValue(getValue()) == FBView.SCROLLBAR_SHOW_AS_FOOTER
						);
					}
				});
		
				footerPreferences.add(statusLineScreen.addPreference(new ZLIntegerRangePreference(
					this, statusLineScreen.Resource.getResource("footerHeight"),
					fbReader.FooterHeightOption
				)));
				footerPreferences.add(statusLineScreen.addOption(profile.FooterFillOption, "footerColor"));
				footerPreferences.add(statusLineScreen.addOption(fbReader.FooterShowTOCMarksOption, "tocMarks"));
		
				footerPreferences.add(statusLineScreen.addOption(fbReader.FooterShowClockOption, "showClock"));
				footerPreferences.add(statusLineScreen.addOption(fbReader.FooterShowBatteryOption, "showBattery"));
				footerPreferences.add(statusLineScreen.addOption(fbReader.FooterShowProgressOption, "showProgress"));
				footerPreferences.add(statusLineScreen.addPreference(new FontOption(
					this, statusLineScreen.Resource, "font",
					fbReader.FooterFontOption, false
				)));
				footerPreferences.setEnabled(
					fbReader.ScrollbarTypeOption.getValue() == FBView.SCROLLBAR_SHOW_AS_FOOTER
				);
		
				final Screen colorProfileScreen = createPreferenceScreen("colorProfile");
				final ZLResource resource = colorProfileScreen.Resource;
				colorProfileScreen.setSummary(ColorProfilePreference.createTitle(resource, fbreader.getColorProfileName()));
				for (String key : ColorProfile.names()) {
					colorProfileScreen.addPreference(new ColorProfilePreference(
						this, fbreader, colorProfileScreen, key, ColorProfilePreference.createTitle(resource, key)
					));
				}
				 */
				
				
				
		
				/*		final Screen imagesScreen = createPreferenceScreen("images");
				imagesScreen.addOption(fbReader.ImageTappingActionOption, "tappingAction");
				imagesScreen.addOption(fbReader.FitImagesToScreenOption, "fitImagesToScreen");
				imagesScreen.addOption(fbReader.ImageViewBackgroundOption, "backgroundColor");
		
				final Screen cancelMenuScreen = createPreferenceScreen("cancelMenu");
				cancelMenuScreen.addOption(fbReader.ShowLibraryInCancelMenuOption, "library");
				cancelMenuScreen.addOption(fbReader.ShowNetworkLibraryInCancelMenuOption, "networkLibrary");
				cancelMenuScreen.addOption(fbReader.ShowPreviousBookInCancelMenuOption, "previousBook");
				cancelMenuScreen.addOption(fbReader.ShowPositionsInCancelMenuOption, "positions");
				final String[] backKeyActions =
					{ ActionCode.EXIT, ActionCode.SHOW_CANCEL_MENU };
				cancelMenuScreen.addPreference(new ZLStringChoicePreference(
					this, cancelMenuScreen.Resource, "backKeyAction",
					keyBindings.getOption(KeyEvent.KEYCODE_BACK, false), backKeyActions
				));
				final String[] backKeyLongPressActions =
					{ ActionCode.EXIT, ActionCode.SHOW_CANCEL_MENU, ReaderApp.NoAction };
				cancelMenuScreen.addPreference(new ZLStringChoicePreference(
					this, cancelMenuScreen.Resource, "backKeyLongPressAction",
					keyBindings.getOption(KeyEvent.KEYCODE_BACK, true), backKeyLongPressActions
				));
		
				final Screen tipsScreen = createPreferenceScreen("tips");
				tipsScreen.addOption(TipsManager.Instance().ShowTipsOption, "showTips");
		
				final Screen aboutScreen = createPreferenceScreen("about");
				aboutScreen.addPreference(new InfoPreference(
					this,
					aboutScreen.Resource.getResource("version").getValue(),
					androidLibrary.getFullVersionName()
				));
				aboutScreen.addPreference(new UrlPreference(this, aboutScreen.Resource, "site"));
				aboutScreen.addPreference(new UrlPreference(this, aboutScreen.Resource, "email"));
				aboutScreen.addPreference(new UrlPreference(this, aboutScreen.Resource, "twitter"));
				 */
			}
			else{
				myScreen.removePreference(colorsScreen.myScreen);
			}
			
			//GENERAL
			Screen otherScreen = createPreferenceScreen("other");
			
			otherScreen.addPreference(new ZLBooleanPreference(
					this,
					myReaderApp.DayNight,
					otherScreen.Resource,
					"daynightenable"
					) {
				@Override
				protected void onClick() {
					super.onClick();
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean("daynightenable", isChecked());
					editor.commit();
				}
			});
			
			dayNight = new TimePreference(this);
			dayNight.setEnabled(PreferenceManager.getDefaultSharedPreferences(getBaseContext())
					.getBoolean("daynightenable", false));
			otherScreen.addPreference(dayNight);
			//otherScreen.addOption(myReaderApp.EnableDoubleTapOption, "enableDoubleTapDetection");
			
			
			final ScrollingPreferences scrollingPreferences = ScrollingPreferences.Instance();
			final Screen scrollingScreen = createPreferenceScreenForScreen("scrolling", otherScreen);
			
			//AUTOPAGGINg
			scrollingScreen.addPreference(new ZLBooleanPreference(
					this,
					myReaderApp.AllowAutopaggingOption,
					GeneralScreen.Resource,
					"autoscrolling"
					) {
				@Override
				protected void onClick() {
					super.onClick();
					SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
					SharedPreferences.Editor editor = settings.edit();
					if(isChecked()){
						FullReaderActivity.autopagingTimer = true;
					} else {
						FullReaderActivity.autopagingTimer = false;
					}
					editor.putBoolean("needToAutopaging", isChecked());
					editor.commit();
				}
			});
			
			timePref = new TimeSwitchPreference(this);
			timePref.setEnabled(PreferenceManager.getDefaultSharedPreferences(getBaseContext())
					.getBoolean("needToAutopaging", false));
			
			scrollingScreen.addPreference(timePref);
			scrollingScreen.addOption(scrollingPreferences.FingerScrollingOption, "fingerScrolling");
			
			/*
			final ZLPreferenceSet volumeKeysPreferences = new ZLPreferenceSet();
			scrollingScreen.addPreference(new ZLCheckBoxPreference(
				this, scrollingScreen.Resource, "volumeKeys"
			) {
				{
					setChecked(fbReader.hasActionForKey(KeyEvent.KEYCODE_VOLUME_UP, false));
				}
	
				@Override
				protected void onClick() {
					super.onClick();
					if (isChecked()) {
						keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_DOWN, false, ActionCode.VOLUME_KEY_SCROLL_FORWARD);
						keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_UP, false, ActionCode.VOLUME_KEY_SCROLL_BACK);
					} else {
						keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_DOWN, false, ReaderApp.NoAction);
						keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_UP, false, ReaderApp.NoAction);
					}
					volumeKeysPreferences.setEnabled(isChecked());
				}
			});
			volumeKeysPreferences.add(scrollingScreen.addPreference(new ZLCheckBoxPreference(
				this, scrollingScreen.Resource, "invertVolumeKeys"
			) {
				{
					setChecked(ActionCode.VOLUME_KEY_SCROLL_FORWARD.equals(
						keyBindings.getBinding(KeyEvent.KEYCODE_VOLUME_UP, false)
					));
				}
	
				@Override
				protected void onClick() {
					super.onClick();
					if (isChecked()) {
						keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_DOWN, false, ActionCode.VOLUME_KEY_SCROLL_BACK);
						keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_UP, false, ActionCode.VOLUME_KEY_SCROLL_FORWARD);
					} else {
						keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_DOWN, false, ActionCode.VOLUME_KEY_SCROLL_FORWARD);
						keyBindings.bindKey(KeyEvent.KEYCODE_VOLUME_UP, false, ActionCode.VOLUME_KEY_SCROLL_BACK);
					}
				}
			}));
			volumeKeysPreferences.setEnabled(fbReader.hasActionForKey(KeyEvent.KEYCODE_VOLUME_UP, false));
			 */
			
			
			scrollingScreen.addOption(scrollingPreferences.AnimationOption, "animation");
			/*scrollingScreen.addPreference(new AnimationSpeedPreference(
					this,
					scrollingScreen.Resource,
					"animationSpeed",
					scrollingPreferences.AnimationSpeedOption
					));*/
			
			scrollingScreen.addOption(scrollingPreferences.HorizontalOption, "horizontal");
			
			
			
			final Screen dictionaryScreen = createPreferenceScreenForScreen("dictionary", otherScreen);
			try {
				dictionaryScreen.addPreference(new DictionaryPreference(
						this,
						dictionaryScreen.Resource,
						"dictionary",
						DictionaryUtil.singleWordTranslatorOption(),
						DictionaryUtil.dictionaryInfos(this, true)
						));
				dictionaryScreen.addPreference(new DictionaryPreference(
						this,
						dictionaryScreen.Resource,
						"translator",
						DictionaryUtil.multiWordTranslatorOption(),
						DictionaryUtil.dictionaryInfos(this, false)
						));
			} catch (Exception e) {
				// ignore: dictionary lists are not initialized yet
			}
			dictionaryScreen.addPreference(new ZLBooleanPreference(
					this,
					myReaderApp.NavigateAllWordsOption,
					dictionaryScreen.Resource,
					"navigateOverAllWords"
					));
			//dictionaryScreen.addOption(myReaderApp.WordTappingActionOption, "tappingAction");
			
			
			//initAdMob();
			
			// Настройки синхронизации
			final Screen syncScreen = createPreferenceScreen("synchronization");
			ZLBooleanOption DropboxSync = new ZLBooleanOption("Syncronization", "DropboxSync", false);
			mDbxBoolPref = new ZLBooleanPreference(this, DropboxSync, otherScreen.Resource, "synchronizationDropbox");
			mDbxBoolPref.setupForDropbox(this);
			syncScreen.addPreference(mDbxBoolPref);
			
			/*
			 * 
			 *  Вкладка для настроек тап-зон
			 *  
			 */
			tapScreen = createPreferenceScreen("tapzones");
			ZLResource tapRes = ZLResource.resource("tapzones");
			tapzonesListPrefs = new TapzonesListPreference(this);
			tapzonesListPrefs.setKey(TapzonesListPreference.TAPZONES_LIST_KEY);
			String [] entries = new String[] {
					tapRes.getResource("tapzone_horizontal").getValue(),
					tapRes.getResource("tapzone_vertical").getValue(),
			};
			String [] entryValues = new String[] {TapzonesListPreference.TAPZONE_HORIZONTAL, TapzonesListPreference.TAPZONE_VERTICAL};
			tapzonesListPrefs.setTitle(tapRes.getResource("tapzone_choose").getValue());
			tapzonesListPrefs.setEntries(entries);
			tapzonesListPrefs.setEntryValues(entryValues);
			tapzonesListPrefs.setOnPreferenceChangeListener(tapListPrefChangeListener);
			tapScreen.addPreference(tapzonesListPrefs);
			
			// Список c настройками для горизонтальных тап зон
			horTapzoneCat = new PreferenceCategory(this);
			horTapzoneCat.setKey(HOR_TAPZONE_CAT);
			horTapzoneCat.setTitle(tapRes.getResource("tapzone_horisontal_category").getValue());
			tapScreen.addPreference(horTapzoneCat);
		    
			String [] tapzoneOptEntries = new String[]{
		    		tapRes.getResource("tapzone_next_page").getValue(),
		    		tapRes.getResource("tapzone_prev_page").getValue(),
		    		tapRes.getResource("tapzone_day_mode").getValue(),
		    		tapRes.getResource("tapzone_night_mode").getValue(),
		    		tapRes.getResource("tapzone_book_info").getValue(),
		    		tapRes.getResource("tapzone_bookmarks").getValue(),
		    		tapRes.getResource("tapzone_quotes").getValue(),
		    		tapRes.getResource("tapzone_preferences").getValue(),
		    		tapRes.getResource("tapzone_increase_font").getValue(),
		    		tapRes.getResource("tapzone_decrease_font").getValue(),
		    		tapRes.getResource("tapzone_contents").getValue(),
		    		tapRes.getResource("tapzone_search").getValue(),
		    		tapRes.getResource("tapzone_navigate").getValue(),
		    		tapRes.getResource("tapzone_close").getValue(),
		    		tapRes.getResource("tapzone_library").getValue(),
		    		tapRes.getResource("tapzone_menu").getValue(),
		    		tapRes.getResource("tapzone_colors").getValue(),
		    		tapRes.getResource("tapzone_next_book").getValue(),
		    		tapRes.getResource("tapzone_previous_book").getValue(),
		    		tapRes.getResource("tapzone_fullscreen").getValue()
		    };
		   
		    horTopPref = new TapzoneOptionsPreference(this);
		    horTopPref.setKey(TapzoneOptionsPreference.TAPZONE_HOR_TOP_KEY);
		    horTopPref.setTitle(tapRes.getResource("tapzone_top_touch").getValue());
		    horTopPref.setEntries(tapzoneOptEntries);
		    horTopPref.setEntryValues(TAP_ACTIONS);
		    horTopPref.setOnPreferenceChangeListener(tapOptionPrefChangeListener);
		    horTapzoneCat.addPreference(horTopPref);

		    horCenterPref = new TapzoneOptionsPreference(this);
		    horCenterPref.setKey(TapzoneOptionsPreference.TAPZONE_HOR_CENTER_KEY);
		    horCenterPref.setTitle(tapRes.getResource("tapzone_center_touch").getValue());
		    horCenterPref.setEntries(tapzoneOptEntries);
		    horCenterPref.setEntryValues(TAP_ACTIONS);
		    horCenterPref.setOnPreferenceChangeListener(tapOptionPrefChangeListener);
		    horTapzoneCat.addPreference(horCenterPref);
		    
		    horBottomPref = new TapzoneOptionsPreference(this);
		    horBottomPref.setKey(TapzoneOptionsPreference.TAPZONE_HOR_BOTTOM_KEY);
		    horBottomPref.setTitle(tapRes.getResource("tapzone_bottom_touch").getValue());
		    horBottomPref.setEntries(tapzoneOptEntries);
		    horBottomPref.setEntryValues(TAP_ACTIONS);
		    horBottomPref.setOnPreferenceChangeListener(tapOptionPrefChangeListener);
		    horTapzoneCat.addPreference(horBottomPref);
	    
		    // Список c настройками для вертикальных тап зон
 			verTapzoneCat = new PreferenceCategory(this);
 			verTapzoneCat.setKey(VER_TAPZONE_CAT);
 			verTapzoneCat.setTitle(tapRes.getResource("tapzone_vertical_category").getValue());
 		    tapScreen.addPreference(verTapzoneCat);
 		    
 		    verLeftPref = new TapzoneOptionsPreference(this);
 		    verLeftPref.setKey(TapzoneOptionsPreference.TAPZONE_VER_LEFT_KEY);
 		    verLeftPref.setTitle(tapRes.getResource("tapzone_left_touch").getValue());
 		    verLeftPref.setEntries(tapzoneOptEntries);
 		    verLeftPref.setEntryValues(TAP_ACTIONS);
 		    verLeftPref.setOnPreferenceChangeListener(tapOptionPrefChangeListener);
		    verTapzoneCat.addPreference(verLeftPref);
		  
		    verCenterPref = new TapzoneOptionsPreference(this);
		    verCenterPref.setKey(TapzoneOptionsPreference.TAPZONE_VER_CENTER_KEY);
		    verCenterPref.setTitle(tapRes.getResource("tapzone_center_touch").getValue());
		    verCenterPref.setEntries(tapzoneOptEntries);
		    verCenterPref.setEntryValues(TAP_ACTIONS);
		    verCenterPref.setOnPreferenceChangeListener(tapOptionPrefChangeListener);
		    verTapzoneCat.addPreference(verCenterPref);
		    
		    verRightPref = new TapzoneOptionsPreference(this);
		    verRightPref.setKey(TapzoneOptionsPreference.TAPZONE_VER_RIGHT_KEY);
		    verRightPref.setTitle(tapRes.getResource("tapzone_right_touch").getValue());
		    verRightPref.setEntries(tapzoneOptEntries);
		    verRightPref.setEntryValues(TAP_ACTIONS);
		    verRightPref.setOnPreferenceChangeListener(tapOptionPrefChangeListener);
		    verTapzoneCat.addPreference(verRightPref);
 		    
		    tapzoneDoubleTapPref = tapScreen.addOption(myReaderApp.EnableDoubleTapOption, "tapzoneDoubleTapDetection");
		    
		    tapDefault = new CheckBoxPreference(this);
			tapDefault.setKey(tapDefaultKey);
			tapDefault.setSummary(tapRes.getResource("tapzone_default").getValue());
			tapDefault.setOnPreferenceChangeListener(mTapDefaultChangeListener);
			tapDefault.setChecked(false);
			tapScreen.addPreference(tapDefault);
			
		    // Проверка, какая опция была выбрана в списке с видами тап-зон
		    // в зависимости от того, какая тап зона выбрана скрываем, ту которая неактивна
		    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			String selectedValue = settings.getString(TapzonesListPreference.TAPZONES_LIST_KEY, "");
			if (selectedValue.equals(TapzonesListPreference.TAPZONE_HORIZONTAL)){
				tapScreen.myScreen.removePreference(verTapzoneCat);
				initTapPrefs(selectedValue);
			}
			else if (selectedValue.equals(TapzonesListPreference.TAPZONE_VERTICAL)){
				tapScreen.myScreen.removePreference(horTapzoneCat);
				initTapPrefs(selectedValue);
			}
			else{
				tapScreen.myScreen.removePreference(horTapzoneCat);
				tapScreen.myScreen.removePreference(verTapzoneCat);
				tapScreen.myScreen.removePreference(tapzoneDoubleTapPref);
				tapScreen.myScreen.removePreference(tapDefault);
			}
			
			
			// Если нужно - делаем переход на экран с настройками цвета
			if (showColorsScreen && profileName.equals(ColorProfile.DAY)){
				runOnUiThread(new Runnable() {
					public void run() {
						PreferenceActivity.this.setPreferenceScreen((PreferenceScreen)colorsScreen.myScreen);
					}
				});
			}
		}
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	}
	
	
	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		if (getIntent()!=null){
			Intent intent = getIntent();
			if (intent.hasExtra(NEEDS_RESTART)){
				showAppThemeRestartDialog();
			}
		}
	}
	
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String key) {
		if(key.equals("needToAutopaging")){
			timePref.setEnabled(PreferenceManager.getDefaultSharedPreferences(getBaseContext())
					.getBoolean("needToAutopaging", false));
		}
		if(key.equals("daynightenable")){
			dayNight.setEnabled(PreferenceManager.getDefaultSharedPreferences(getBaseContext())
					.getBoolean("daynightenable", false));
		}
		if(key.equals("needToRemind")){
			remindPref.setEnabled(PreferenceManager.getDefaultSharedPreferences(getBaseContext())
					.getBoolean("needToRemind", false));
		}
	}

	@Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	      case android.R.id.home:
	        finish();
	    }
		return true;
	  }
	
	/*private void initAdMob() {
		
		  RelativeLayout.LayoutParams leftMarginParams = new RelativeLayout.LayoutParams(200, 50);
		  RelativeLayout relativeLayout = new RelativeLayout(getApplicationContext());

	        // Defining the RelativeLayout layout parameters.
	        // In this case I want to fill its parent

		adView = new AdView(this, AdSize.BANNER, "use-your-own-id");
	
		leftMarginParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		leftMarginParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

		relativeLayout.addView(adView, leftMarginParams);
		AdRequest adRequest = new AdRequest.Builder().build();
	    adView.loadAd(adRequest);
	    
	    addContentView(relativeLayout, leftMarginParams);
	}*/
	
	
	
	 public void recreatethis()
	    {
	        if (android.os.Build.VERSION.SDK_INT >= 11) {
	            super.recreate();
	        } else {
	        	final Handler handler = new Handler();
	        	handler.postDelayed(new Runnable() {
	        	  @Override
	        	  public void run() {
	                  finish();
	                  Intent intent = getIntent();
	                  if (mFromTheme) intent.putExtra(NEEDS_RESTART, true);
	                  startActivity(intent);
	        	  }
	        	}, 500);
	        	
	        	
	        	/*final Handler handler = new Handler();
	        	handler.postDelayed(new Runnable() {
	        	  @Override
	        	  public void run() {
	                  finish();
	                  Intent intent = getIntent();
	                  intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
	                  startActivity(intent);
	        	  }
	        	}, 500);*/
	        	
	        }
	    }
	 
	 String loadCurrentLanguage() {
	    SharedPreferences sPref = getSharedPreferences("languagePrefs", MODE_PRIVATE);
	    return sPref.getString("curLanguage", "");
	}
	 
	 
	 @Override
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
	     // Обработка подключения аккаунта Dropbox
		 if (requestCode == DropboxHelper.REQUEST_LINK_TO_DBX) {
	         if (resultCode == Activity.RESULT_OK) {
	        	 DropboxHelper.Instance(ReaderApplication.getContext()).linkAccount();
	        	 showDbxRestartDialog();
	         } else {
	             mDbxBoolPref.setChecked(false);
	             ZLBooleanOption DropboxSync = new ZLBooleanOption("Syncronization", "DropboxSync", false);
	             DropboxSync.setValue(false);
	         }
	     } else {
	         super.onActivityResult(requestCode, resultCode, data);
	     }
	 }
	 
	 
	 // ------- Метод, который показывает диалоговое окно для перезапуска приложения после подключения аккаунта Dropbox -------
	 public void showDbxRestartDialog(){
		ZLResource dbxDilogRes = ZLResource.resource("dropbox_service");
	    AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle(dbxDilogRes.getResource("dbx_dialog_title").getValue()); 
        ad.setMessage(dbxDilogRes.getResource("dbx_dialog_message").getValue()); 
        ad.setPositiveButton(dbxDilogRes.getResource("dbx_dialog_ok_text").getValue(), new OnClickListener() {
			@Override
			public void onClick(DialogInterface arg, int arg1) {
				Intent i = new Intent(getBaseContext(), StartScreenActivity.class);
	            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(i);
				finish();
			}
        });
        ad.setNegativeButton(dbxDilogRes.getResource("dbx_dialog_no_text").getValue(), new OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
            	mDbxBoolPref.setChecked(false);
            	ZLBooleanOption DropboxSync = new ZLBooleanOption("Syncronization", "DropboxSync", false);
	            DropboxSync.setValue(false);
	            DropboxHelper.Instance(ReaderApplication.getContext()).unlinkAccount();
            }
        });
        ad.setCancelable(false);
        ad.show();
	 }
	 
	 // ------- Метод, который скрывает или показывает опции настроек для определенной тап-зоны -------
	 public void enableTapPrefCategory(String category){
		 tapScreen.myScreen.removePreference(tapzoneDoubleTapPref);
		 tapScreen.myScreen.removePreference(tapDefault);
		 if (category.equals(TapzonesListPreference.TAPZONE_HORIZONTAL)){
			 tapScreen.myScreen.removePreference(verTapzoneCat);
			 tapScreen.addPreference(horTapzoneCat);
		 }
		 else
		 if (category.equals(TapzonesListPreference.TAPZONE_VERTICAL)){
			 tapScreen.myScreen.removePreference(horTapzoneCat);
			 tapScreen.addPreference(verTapzoneCat);
		 }
		 tapScreen.myScreen.addPreference(tapzoneDoubleTapPref);
		 tapDefault.setChecked(false);
		 tapScreen.myScreen.addPreference(tapDefault);
		// Правка бага с темами
		 int theme = mPreferences.getInt(IConstants.THEME_PREF, IConstants.THEME_REDTREE); 
		 //tapzonesListPrefs.getEditor().clear().commit();
		 //tapzonesListPrefs.setDefaultValue(null);
		 mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		 Editor editor = mPreferences.edit();
		 editor.putInt(IConstants.THEME_PREF, theme);
		 //editor.commit();
	 }
	 
	 // -------- Метод, который устанавливает значения по умолчанию для опций тап-зон, если у них нет никаких сохраненных данных ------
	 private void initTapPrefs(String tapzone){
		 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(PreferenceActivity.this);
		 String action = "";
		 if (tapzone.equals(TapzonesListPreference.TAPZONE_HORIZONTAL)){
			 action = settings.getString(TapzoneOptionsPreference.TAPZONE_HOR_TOP_KEY, "");
			 if (action.length() == 0){
				 horTopPref.setValue(TAP_ACTIONS[0]);
			 }
			 action = settings.getString(TapzoneOptionsPreference.TAPZONE_HOR_CENTER_KEY, "");
			 if (action.length() == 0){
				 horCenterPref.setValue(TAP_ACTIONS[7]);
			 }
			 action = settings.getString(TapzoneOptionsPreference.TAPZONE_HOR_BOTTOM_KEY, "");
			 if (action.length() == 0){
				 horBottomPref.setValue(TAP_ACTIONS[1]);
			 }
		 }
		 else
		 if (tapzone.equals(TapzonesListPreference.TAPZONE_VERTICAL)){
			 action = settings.getString(TapzoneOptionsPreference.TAPZONE_VER_LEFT_KEY, "");
			 if (action.length() == 0){
				 verLeftPref.setValue(TAP_ACTIONS[1]);
			 }
			 action = settings.getString(TapzoneOptionsPreference.TAPZONE_VER_CENTER_KEY, "");
			 if (action.length() == 0){
				 verCenterPref.setValue(TAP_ACTIONS[7]);
			 }
			 action = settings.getString(TapzoneOptionsPreference.TAPZONE_VER_RIGHT_KEY, "");
			 if (action.length() == 0){
				 verRightPref.setValue(TAP_ACTIONS[0]);
			 }
		 }
	 }
	 
	 /*
	  * Обработчик выбора тап зоны в списке с тап-зонами
	  */
	 OnPreferenceChangeListener tapListPrefChangeListener = new OnPreferenceChangeListener(){

		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			enableTapPrefCategory(newValue.toString());
			final ScrollingPreferences prefs = ScrollingPreferences.Instance();
			prefs.TapZoneMapOption.setValue(newValue.toString());
			initTapPrefs(newValue.toString());
			return true;
		}
	 };
	 
	 /*
	  * Обработчик выбора действия для определенной тап-зоны 
	  */
	 
	 OnPreferenceChangeListener tapOptionPrefChangeListener = new OnPreferenceChangeListener(){
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue){
			final ScrollingPreferences prefs = ScrollingPreferences.Instance();
			String id = prefs.TapZoneMapOption.getValue();
			if (id.equals(TapzonesListPreference.TAPZONE_VERTICAL)){
				TapZoneMap verTapzone = TapZoneMap.zoneMap(id);
				
				if (preference.getTitle().equals(verLeftPref.getTitle())){
					verTapzone.setActionForZone(0, 0, true, newValue.toString());
					verTapzone.setActionForZone(0, 1, true, newValue.toString());
					verTapzone.setActionForZone(0, 2, true, newValue.toString());
				}
				else
				if (preference.getTitle().equals(verCenterPref.getTitle())){
					verTapzone.setActionForZone(1, 0, true, newValue.toString());
					verTapzone.setActionForZone(1, 1, true, newValue.toString());
					verTapzone.setActionForZone(1, 2, true, newValue.toString());
				}
				else
				if (preference.getTitle().equals(verRightPref.getTitle())){
					verTapzone.setActionForZone(2, 0, true, newValue.toString());
					verTapzone.setActionForZone(2, 1, true, newValue.toString());
					verTapzone.setActionForZone(2, 2, true, newValue.toString());
				}
			}
			else
			if (id.equals(TapzonesListPreference.TAPZONE_HORIZONTAL)){
				TapZoneMap horTapzone = TapZoneMap.zoneMap(id);
				if (preference.getTitle().equals(horTopPref.getTitle())){
					horTapzone.setActionForZone(0, 0, true, newValue.toString());
					horTapzone.setActionForZone(1, 0, true, newValue.toString());
					horTapzone.setActionForZone(2, 0, true, newValue.toString());
				}
				else
				if (preference.getTitle().equals(horCenterPref.getTitle())){
					horTapzone.setActionForZone(0, 1, true, newValue.toString());
					horTapzone.setActionForZone(1, 1, true, newValue.toString());
					horTapzone.setActionForZone(2, 1, true, newValue.toString());
				}
				else
				if (preference.getTitle().equals(horBottomPref.getTitle())){
					horTapzone.setActionForZone(0, 2, true, newValue.toString());
					horTapzone.setActionForZone(1, 2, true, newValue.toString());
					horTapzone.setActionForZone(2, 2, true, newValue.toString());
				}

			}
			return true;
		}
	 };
	 
	 // ------- Метод, который обрабатывает смену выбора в чекбоксе для сброса настроек тап-зон -------
	 private OnPreferenceChangeListener mTapDefaultChangeListener = new OnPreferenceChangeListener(){
		@Override
		public boolean onPreferenceChange(Preference pref, Object value) {
			if (value instanceof Boolean){
				if ((Boolean) value == true){
					 tapScreen.myScreen.removePreference(tapzoneDoubleTapPref);
					 tapScreen.myScreen.removePreference(verTapzoneCat);
					 tapScreen.myScreen.removePreference(horTapzoneCat);
					 tapScreen.myScreen.removePreference(tapDefault);
					 
					 SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(PreferenceActivity.this);
					 Editor editor = settings.edit();
					 editor.putString(TapzonesListPreference.TAPZONES_LIST_KEY, "");
					 editor.commit();
					 
					 final ScrollingPreferences prefs = ScrollingPreferences.Instance();
					 prefs.TapZoneMapOption.setValue(TAPZONE_DEFAULT);
					 //tapzonesListPrefs.getEditor().clear().commit();
					 tapzonesListPrefs.setValue("");
				}
			}
			return true;
		}
	 };
	 
	 public void showAppThemeRestartDialog(){
			ZLResource dbxDilogRes = ZLResource.resource("theme_restart_dialog");
		    AlertDialog.Builder ad = new AlertDialog.Builder(this);
	        ad.setTitle(dbxDilogRes.getResource("theme_restart_dialog_title").getValue()); 
	        ad.setMessage(dbxDilogRes.getResource("theme_restart_dialog_message").getValue()); 
	        ad.setPositiveButton(dbxDilogRes.getResource("theme_restart_dialog_ok").getValue(), new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg, int arg1) {
					/*Intent i = new Intent(getBaseContext(), StartScreenActivity.class);
		            finish();
		            startActivity(i);*/
					
					
					/*Intent newIntent = IntentCompat.makeRestartActivityTask(new ComponentName(getBaseContext(), StartScreenActivity.class));
					startActivity(newIntent);*/
					finish();
					startActivity(new Intent(ReaderApplication.getContext(), StartScreenActivity.class));
				}
	        });
	        ad.setCancelable(false);
	        ad.show();
		 }
}
