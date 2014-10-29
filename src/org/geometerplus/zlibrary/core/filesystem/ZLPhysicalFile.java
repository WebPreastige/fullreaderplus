/*
* FullReader+
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.util.Log;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

public final class ZLPhysicalFile extends ZLFile {
	private final File myFile;

	private Archive mArchive;
	private boolean mIsRarArchive = false;
	private FileHeader mHeader;
	private File mArchiveFile;
	
	ZLPhysicalFile(String path) {
		this(new File(path));
	}

	public ZLPhysicalFile(File file) {
		myFile = file;
		init();
	}

	@Override
	public boolean exists() {
		return myFile.exists();
	}

	@Override
	public long size() {
		return myFile.length();
	}

	@Override
	public boolean isDirectory() {
		return myFile.isDirectory();
	}

	@Override
	public boolean isReadable() {
		return myFile.canRead();
	}

	public boolean delete() {
		return myFile.delete();
	}

	public File javaFile() {
		return myFile;
	}

	@Override
	public String getPath() {
		try {
			return myFile.getCanonicalPath();
		} catch (Exception e) {
			// should be never thrown
		  	return myFile.getPath();
		}
	}

	@Override
	public String getLongName() {
		return isDirectory() ? getPath() : myFile.getName();
	}

	@Override
	public ZLFile getParent() {
		return isDirectory() ? null : new ZLPhysicalFile(myFile.getParent());
	}

	@Override
	public ZLPhysicalFile getPhysicalFile() {
		return this;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(myFile);
	}

	protected List<ZLFile> directoryEntries() {
		File[] subFiles = myFile.listFiles();
		if ((subFiles == null) || (subFiles.length == 0)) {
			return Collections.emptyList();
		}

		ArrayList<ZLFile> entries = new ArrayList<ZLFile>(subFiles.length);
		for (File f : subFiles) {
			if (!f.getName().startsWith(".")) {
				entries.add(new ZLPhysicalFile(f));
			}
		}
		return entries;
	}
	
	
	// ------- Метод, который указвает, что эьтот файл является rar архив -------
	public void setupFileAsArchive(Archive archive, FileHeader header, File archFile){
		mArchive = archive;
		mHeader = header;
		mArchiveFile = archFile;
		mIsRarArchive = true;
	}
	
	public boolean isRarArchive(){
		return mIsRarArchive;
	}
	
	public Archive getRarAcrivhe(){
		return mArchive;
	}
	
	public FileHeader getRarFileHeader(){
		return mHeader;
	}
	
	public File getArchiveFile(){
		return mArchiveFile;
	}
}
