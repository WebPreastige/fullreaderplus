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

import android.os.Parcel;
import android.os.Parcelable;

public class SyncedBookmarkInfo implements Parcelable{
	
	private String mId;
	private String mBookTitle;
	private int mParIndex;
	private int mElIndex;
	private int mChIndex;
	private String mDate;
	
	public SyncedBookmarkInfo(String id, String bookTitle, int parIndex, int elIndex, int chIndex, String date){
		mId	= id;
		mBookTitle = bookTitle;
		mParIndex = parIndex;
		mElIndex = elIndex;
		mChIndex = chIndex;
		mDate = date;
	}
	
	public String getId(){
		return mId;
	}
	
	public String getTitle(){
		return mBookTitle;
	}
	
	public int getParIndex(){
		return mParIndex;
	}
	
	public int getElIndex(){
		return mElIndex;
	}
	
	public int getChIndex(){
		return mChIndex;
	}
	
	public void updateId(String id){
		mId = id;
	}
	
	public String getDate(){
		return mDate;
	}
	
	public static final Parcelable.Creator<SyncedBookmarkInfo> CREATOR = new
			 Parcelable.Creator<SyncedBookmarkInfo>() {
		         public SyncedBookmarkInfo createFromParcel(Parcel in) {
		             return new SyncedBookmarkInfo(in);
		         }

		         public SyncedBookmarkInfo[] newArray(int size) {
		             return new SyncedBookmarkInfo[size];
		         }
		};
		
		private SyncedBookmarkInfo(Parcel in) {
	        readFromParcel(in);
	    }


	    public void readFromParcel(Parcel in) {
	    	mId = in.readString();
	        mBookTitle = in.readString();
	        mParIndex = in.readInt();
	        mElIndex = in.readInt();
	        mChIndex = in.readInt();
	        mDate = in.readString();
	    }

		@Override
		public int describeContents() {
			return 0;
		}

		@Override
		public void writeToParcel(Parcel out, int arg1) {
			out.writeString(mId);
			out.writeString(mBookTitle);
	        out.writeInt(mParIndex);
	        out.writeInt(mElIndex);
	        out.writeInt(mChIndex);
			out.writeString(mDate);
		}
}
