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
import android.util.Log;

public class SyncedBookInfo implements Parcelable {
	
	private String mId;
	private String mBookTitle;
	private int mParIndex;
	private int mElIndex;
	private int mChIndex;
	private boolean mNeedsUpdate;
	
	public SyncedBookInfo(String id, String bookTitle, int parIndex, int elIndex, int chIndex, boolean needsUpdate){
		mId	= id;
		mBookTitle = bookTitle;
		mParIndex = parIndex;
		mElIndex = elIndex;
		mChIndex = chIndex;
		mNeedsUpdate = needsUpdate;
	}
	
	public String getId(){
		return mId;
	}
	
	public String getBookTitle(){
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
	
	public boolean needsUpdate(){
		return mNeedsUpdate;
	}
	
	 public static final Parcelable.Creator<SyncedBookInfo> CREATOR = new
		 Parcelable.Creator<SyncedBookInfo>() {
	         public SyncedBookInfo createFromParcel(Parcel in) {
	             return new SyncedBookInfo(in);
	         }

	         public SyncedBookInfo[] newArray(int size) {
	             return new SyncedBookInfo[size];
	         }
	};
	
	private SyncedBookInfo(Parcel in) {
        readFromParcel(in);
    }


    public void readFromParcel(Parcel in) {
    	mId = in.readString();
        mBookTitle = in.readString();
        mParIndex = in.readInt();
        mElIndex = in.readInt();
        mChIndex = in.readInt();
        boolean [] boolArr =new boolean [1];
        in.readBooleanArray(boolArr);
        mNeedsUpdate = boolArr[0];
        
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
		boolean [] boolArr = new boolean[]{mNeedsUpdate};
		out.writeBooleanArray(boolArr);
	}
	
	public void updatePosition(int parIndex, int elIndex, int chIndex){
		mParIndex = parIndex;
		mElIndex = elIndex;
		mChIndex = chIndex;
		mNeedsUpdate = true;
	}
	
	public void noNeedsUpdate(){
		mNeedsUpdate = false;
	}
	
	public void updateId(String id){
		mId = id;
	}
}


