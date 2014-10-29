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
package com.webprestige.fr.dropbox;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class SyncedData implements Parcelable {
	private ArrayList<SyncedBookInfo> sbInfos;
	private ArrayList<SyncedBookmarkInfo> sBmkInfos;
	private ArrayList<SyncedQuoteInfo> sQtInfos;
	private ArrayList<SyncedColorMarkInfo> sCMInfos;
	
	private static SyncedData mData;
	private SyncedData (){
		sbInfos = new ArrayList<SyncedBookInfo>();
		sBmkInfos = new ArrayList<SyncedBookmarkInfo>();
		sQtInfos = new ArrayList<SyncedQuoteInfo>();
		sCMInfos = new ArrayList<SyncedColorMarkInfo>();
	}
	
	public static SyncedData Instance(){
		if (mData == null) mData = new SyncedData();
		return mData;
	}
	
	public void clearAll(){
		sbInfos.clear();
		sBmkInfos.clear();
		sQtInfos.clear();
		sCMInfos.clear();
	}
	
	public void addBookFromDbx(SyncedBookInfo info){
		sbInfos.add(info);
	}
	
	
	public void addBookToSync(SyncedBookInfo info){
		SyncedBookInfo sbInfo = hasBook(info.getBookTitle());
		if (sbInfo == null){
			sbInfos.add(info);
		}
		else{
			sbInfo.updatePosition(info.getParIndex(), info.getElIndex(), info.getChIndex());
		}
	}
	
	
	public void addBookmarkToSync(SyncedBookmarkInfo sBmkInfo){
		sBmkInfos.add(sBmkInfo);
	}
	
	public void addQuoteToSync(SyncedQuoteInfo sQtInfo){
		sQtInfos.add(sQtInfo);
	}
	
	public void addColorMarkToSync(SyncedColorMarkInfo sCMInfo){
		sCMInfos.add(sCMInfo);
	}
	
	public ArrayList<SyncedBookInfo> getSBInfos(){
		return sbInfos;
	}
	
	public ArrayList<SyncedBookmarkInfo> getSBmkInfos(){
		return sBmkInfos;
	}
	
	public ArrayList<SyncedQuoteInfo> getSQtInfos(){
		return sQtInfos;
	}
	
	public ArrayList<SyncedColorMarkInfo> getSCMInfos(){
		return sCMInfos;
	}
	
	public boolean hasBooks(){
		if (sbInfos.size()>0) return true;
		else return false;
	}
	
	public boolean hasBookmarks(){
		if (sBmkInfos.size()>0) return true;
		else return false;
	}
	
	public boolean hasQuotes(){
		if (sQtInfos.size()>0) return true;
		else return false;
	}
	
	public boolean hasColorMarks(){
		if (sCMInfos.size()>0) return true;
		else return false;
	}
	
	
	private SyncedBookInfo hasBook(String bookTitle){
		for (SyncedBookInfo info : sbInfos){
			if (info.getBookTitle().contains(bookTitle)){
				return info;
			}
		}
		return null;
	}

	
	public static final Parcelable.Creator<SyncedData> CREATOR = new
			 Parcelable.Creator<SyncedData>() {
		         public SyncedData createFromParcel(Parcel in) {
		             return new SyncedData(in);
		         }

		         public SyncedData[] newArray(int size) {
		             return new SyncedData[size];
		         }
		};
		
		private SyncedData(Parcel in) {
	        readFromParcel(in);
	    }


	    public void readFromParcel(Parcel in) {
	    	sbInfos = new ArrayList<SyncedBookInfo>();
	    	sBmkInfos = new ArrayList<SyncedBookmarkInfo>();
			sQtInfos = new ArrayList<SyncedQuoteInfo>();
			sCMInfos = new ArrayList<SyncedColorMarkInfo>();
			
	        in.readTypedList(sbInfos, SyncedBookInfo.CREATOR);
	        in.readTypedList(sBmkInfos, SyncedBookmarkInfo.CREATOR);
	        in.readTypedList(sQtInfos, SyncedQuoteInfo.CREATOR);
	        in.readTypedList(sCMInfos, SyncedColorMarkInfo.CREATOR);
	    }

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel out, int arg1) {
			out.writeTypedList(sbInfos);
			out.writeTypedList(sBmkInfos);
			out.writeTypedList(sQtInfos);
			out.writeTypedList(sCMInfos);
		}
}
