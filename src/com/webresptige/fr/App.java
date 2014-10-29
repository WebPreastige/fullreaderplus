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
package com.webresptige.fr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import dalvik.system.DexClassLoader;

public class App extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		dexTool();
	}
	
	/**
	* Copy the following code and call dexTool() after super.onCreate() in
	* Application.onCreate()
	* <p>
	* This method hacks the default PathClassLoader and load the secondary dex
	* file as it's parent.
	*/
	@SuppressLint("NewApi")
	private void dexTool() {
		
		File dexDir = new File(getFilesDir(), "dlibs");
		dexDir.mkdir();
		File dexFile = new File(dexDir, "libs.apk");
		File dexOpt = new File(dexDir, "opt");
		dexOpt.mkdir();
		try {
			InputStream ins = getAssets().open("libs.apk");
			if (dexFile.length() != ins.available()) {
				FileOutputStream fos = new FileOutputStream(dexFile);
				byte[] buf = new byte[4096];
				int l;
				while ((l = ins.read(buf)) != -1) {
					fos.write(buf, 0, l);
				}
				fos.close();
				}
				ins.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		ClassLoader cl = getClassLoader();
		ApplicationInfo ai = getApplicationInfo();
		String nativeLibraryDir = null;
		if (Build.VERSION.SDK_INT > 8) {
			nativeLibraryDir = ai.nativeLibraryDir;
		} else {
			nativeLibraryDir = "/data/data/" + ai.packageName + "/lib/";
		}
		DexClassLoader dcl = new DexClassLoader(dexFile.getAbsolutePath(),
		dexOpt.getAbsolutePath(), nativeLibraryDir, cl.getParent());
		
		try {
			Field f = ClassLoader.class.getDeclaredField("parent");
			f.setAccessible(true);
			f.set(cl, dcl);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
