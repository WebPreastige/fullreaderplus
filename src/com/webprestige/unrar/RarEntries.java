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
package com.webprestige.unrar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.geometerplus.android.fbreader.ReaderApplication;
import org.geometerplus.android.fbreader.library.LibraryActivity;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.filesystem.ZLPhysicalFile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.github.junrar.Archive;
import com.github.junrar.exception.RarException;
import com.github.junrar.impl.FileVolumeManager;
import com.github.junrar.rarfile.FileHeader;

public class RarEntries {
	
	public static String RAR_EXTENSION = "rar";
	public static final int FILE_UNRAR_MSG = 2;
	public static final int FILE_UNRAR_SUCCES = 1;
	public static final int FILE_UNRAR_ERROR = 0;
	
	public RarEntries(){
		
	}
	
	// ------- Метод, который открывает архив, и получает из него названия файлов, которые в нем хранятся, не извлекая их на устройство -------
	public List<ZLFile> rarEntries(String path, Context ctx){
		ArrayList<ZLFile> entries = new ArrayList<ZLFile>();
		File f = new File(path);
		Archive a = null;
		try {
			a = new Archive(new FileVolumeManager(f));
		} catch (RarException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (a != null) {
			try{
				a.getMainHeader().print();
				FileHeader fh = a.nextFileHeader();
				while (fh != null) {
					try {
						File out = new File(f.getParent() + "/"
								+ fh.getFileNameString().trim());
						ZLPhysicalFile zFile = new ZLPhysicalFile(out);
						zFile.setupFileAsArchive(a, fh, f);
						entries.add(zFile);
					} 
					catch (Exception e){}
					fh = a.nextFileHeader();
				}
			}
			catch (Exception e){
				String msg = getFileUnrarErrorMsg(ctx);
				Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
			}
		}
		return entries;
	}
	
	
	private String getFileUnrarErrorMsg(Context ctx){
		String msg = "";
        if(loadCurrentLanguage(ctx).equals("en")){
        	msg = "The file is damaged";
		} else if(loadCurrentLanguage(ctx).equals("de")){
			msg = "Die Datei ist beschädigt";
		} else if(loadCurrentLanguage(ctx).equals("fr")){
			msg = "Le fichier est endommagé";
		} else if(loadCurrentLanguage(ctx).equals("uk")){
			msg = "Файл пошкоджено";
			
		} else if(loadCurrentLanguage(ctx).equals("ru")){
			msg = "Файл поврежден";
		} else {
			if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
				msg = "Файл поврежден";
			} else if(Locale.getDefault().getDisplayLanguage().equals("українська")){
				msg = "Файл поврежден";
			} else {
				msg = "The file is damaged";
			}
		}
    return msg;
	}
	
	
	public String loadCurrentLanguage(Context ctx) {
	    SharedPreferences sPref = ctx.getSharedPreferences("languagePrefs", ctx.MODE_PRIVATE);
	    return sPref.getString("curLanguage", "");
    }
	
	// ------- Метод, который распакоывает файл из архива на карту памяти -------
	public void unrarFile(ZLPhysicalFile zlFile, Handler handler, Context context){
		AsyncFileUnrar unrarThr = new AsyncFileUnrar(zlFile, handler, context);
		unrarThr.execute();
	}
	
	class AsyncFileUnrar extends AsyncTask <Void, Void, Integer>{
		private ZLPhysicalFile zlFile;
		private Handler mHandler;
		private ProgressDialog mDialog;
		private Message msg;
		private String unpackedFileName;
		private Context mCtx;
		
		public AsyncFileUnrar(ZLPhysicalFile file, Handler handler, Context context){
			zlFile = file;
			mHandler = handler;
			mCtx = context;
		}
		
		@Override
	    protected void onPreExecute() {
	      super.onPreExecute();
	      mDialog = new ProgressDialog(mCtx);
	      mDialog.setTitle(null);
	      mDialog.setMessage(getMsg());
	      mDialog.show();

	    }

	    @Override
	    protected Integer doInBackground(Void... params) {
	    	return unrar();
	    }

	    @Override
	    protected void onPostExecute(Integer result) {
	    	super.onPostExecute(result);
	      	mDialog.dismiss();
	      	msg = mHandler.obtainMessage(FILE_UNRAR_MSG, result, -1, unpackedFileName);
	      	mHandler.sendMessage(msg);
	    }
	    
	    private String getMsg(){
	    	 String msg = "";
		        if(loadCurrentLanguage().equals("en")){
		        	msg = "Wait please";
				} else if(loadCurrentLanguage().equals("de")){
					msg = "warten Sie mal";
				} else if(loadCurrentLanguage().equals("fr")){
					msg = "S'il vous plaît, attendez";
				} else if(loadCurrentLanguage().equals("uk")){
					msg = "Зачекайте будь ласка";
					
				} else if(loadCurrentLanguage().equals("ru")){
					msg = "Подождите пожалуйста";
				} else {
					if(Locale.getDefault().getDisplayLanguage().equals("русский")) {
						msg = "Подождите пожалуйста";
					} else if(Locale.getDefault().getDisplayLanguage().equals("українська")){
						msg = "Зачекайте будь ласка";
					} else {
						msg = "Wait please";
					}
				}
		    return msg;
	    }
	    
	    
	    private int unrar(){
	    	File f = zlFile.getArchiveFile();
	    	Archive a = null;
			try {
				a = new Archive(new FileVolumeManager(f));
			} catch (RarException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (a != null) {
				a.getMainHeader().print();
				FileHeader fh = a.nextFileHeader();
				while (fh != null) {
					try {
						if (fh.getFileNameString().trim().equals(zlFile.getRarFileHeader().getFileNameString().trim())){
							/*File out = new File(f.getParent() + "/"
									+ fh.getFileNameString().trim());*/
							File out = new File(mCtx.getCacheDir(),
									fh.getFileNameString().trim());
							unpackedFileName = out.getAbsolutePath();
							FileOutputStream os = new FileOutputStream(out);
							a.extractFile(fh, os);
							os.close();
							unpackedFileName = out.getAbsolutePath();
							return FILE_UNRAR_SUCCES;
						}
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return FILE_UNRAR_ERROR;
					} catch (RarException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return FILE_UNRAR_ERROR;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return FILE_UNRAR_ERROR;
					}
					catch (Exception e){}
					fh = a.nextFileHeader();
				}
			}
			return FILE_UNRAR_ERROR;
	    }
	    
	    public String loadCurrentLanguage() {
		    SharedPreferences sPref = mCtx.getSharedPreferences("languagePrefs", mCtx.MODE_PRIVATE);
		    return sPref.getString("curLanguage", "");
	    }
	  
	}
	
	
}
