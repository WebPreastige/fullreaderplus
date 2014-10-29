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

package org.geometerplus.fbreader.library;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;

import org.geometerplus.fbreader.Paths;
import org.geometerplus.zlibrary.core.filesystem.ZLFile;
import org.geometerplus.zlibrary.core.resources.ZLResource;

import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class FileFirstLevelTree extends FirstLevelTree {
	FileFirstLevelTree(RootTree root) {
		super(root, ROOT_FILE_TREE);
		addChild(Paths.BooksDirectoryOption().getValue(), "fileTreeLibrary");
		addChild("/", "fileTreeRoot");
		addChild(Paths.cardDirectory(), "fileTreeCard");
		
		ArrayList<File> directories = new ArrayList<File>();
		for(int i=0;i<getStorageDirectories().length;i++) {
			directories.add(new File(getStorageDirectories()[i]));
		}
		
		Log.d("DEFAULT SDCARD: ", Paths.cardDirectory());
		for(int j=0;j<directories.size();j++) {
			Log.d("ALL SDCARDS: ", directories.get(j).getPath());
			if(!directories.get(j).getAbsolutePath().equals(Paths.cardDirectory())) {
				File f = new File(directories.get(j).getAbsolutePath());
				//File[] contents = directories.get(j).listFiles();
				//if (contents != null && contents.length > 0) {
				if (android.os.Build.VERSION.SDK_INT >= 19) {
					addChildWithoutResourceKey("/storage/sdcard1", "SD card 2", "SD card 2");
				} else {
					addChildWithoutResourceKey(directories.get(j).getAbsolutePath(), "SD card 2", "SD card 2");
				}
				//}
			}
		}
	}
	
	private void createAllFilesFolder() {
		
	}
	
	private void addChild(String path, String resourceKey) {
		final ZLFile file = ZLFile.createFileByPath(path);
		
		if (file != null) {
			final ZLResource resource = resource().getResource(resourceKey);
			new FileTree(
				this,
				file,
				resource.getValue(),
				resource.getResource("summary").getValue()
			);
		}
	}
	
	private String getPathToSDCard() {
		File file = new File("/system/etc/vold.fstab");
	    FileReader fr = null;
	    BufferedReader br = null;
	    String path = "";
	    try {
	        fr = new FileReader(file);
	    } catch (FileNotFoundException e) {
	        return null;
	    } 
	   
	    try {
	        if (fr != null) {
	            br = new BufferedReader(fr);
	            String s = br.readLine();
	            while (s != null) {
	                if (s.startsWith("dev_mount")) {
	                    String[] tokens = s.split("\\s");
	                    path = tokens[2]; //mount_point
	                    if (!Environment.getExternalStorageDirectory().getAbsolutePath().equals(path)) {
	                        break;
	                    }
	                }
	                s = br.readLine();
	            }
	        }            
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (fr != null) {
	                fr.close();
	            }            
	            if (br != null) {
	                br.close();
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
		return path;
	}
	
	public static String[] getStorageDirectories() {
        String[] dirs = null;
        BufferedReader bufReader = null;
        try {
            bufReader = new BufferedReader(new FileReader("/proc/mounts"));
            ArrayList<String> list = new ArrayList<String>();
            list.add(Environment.getExternalStorageDirectory().getPath());
            String line;
            while((line = bufReader.readLine()) != null) {
                if(line.contains("vfat") || line.contains("exfat") ||
                   line.contains("/mnt") || line.contains("/Removable")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String s = tokens.nextToken();
                    s = tokens.nextToken(); // Take the second token, i.e. mount point

                    if (list.contains(s))
                        continue;

                    if (line.contains("/dev/block/vold")) {
                        if (!line.startsWith("tmpfs") &&
                            !line.startsWith("/dev/mapper") &&
                            !s.startsWith("/mnt/secure") &&
                            !s.startsWith("/mnt/shell") &&
                            !s.startsWith("/mnt/asec") &&
                            !s.startsWith("/mnt/obb")
                            ) {
                            list.add(s);
                        }
                    }
                }
            }

            dirs = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                dirs[i] = list.get(i);
            }
        }
        catch (FileNotFoundException e) {}
        catch (IOException e) {}
        finally {
            if (bufReader != null) {
                try {
                    bufReader.close();
                }
                catch (IOException e) {}
            }
        }
        return dirs;
    }
	
	private void addChildWithoutResourceKey(String path, String name, String summary) {
		final ZLFile file = ZLFile.createFileByPath(path);
		if (file != null) {
			new FileTree(
				this,
				file,
				name,
				summary
			);
		}
	}
	
	private void addAllFiles(String path, String name, String summary) {
		final ZLFile file = ZLFile.createFileByPath(path);
		if (file != null) {
			new FileTree(
				this,
				file,
				name,
				summary
			);
		}
	}

	@Override
	public String getTreeTitle() {
		return getName();
	}

	@Override
	public Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}
}
