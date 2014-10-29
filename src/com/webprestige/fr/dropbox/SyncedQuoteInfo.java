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

public class SyncedQuoteInfo implements Parcelable{
	
	private String mId;
	private String mBookTitle;
	private String mQuoteText;
	private int mParIndex;
	private int mElIndex;
	private int mChIndex;
	private String mDate;
	
	public SyncedQuoteInfo(String id, String bookTitle, String quoteText, int parIndex, int elIndex, int chIndex, String date){
		mId	= id;
		mBookTitle = bookTitle;
		mQuoteText = quoteText;
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
	
	public String getQuoteText(){
		return mQuoteText;
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
	
	public static final Parcelable.Creator<SyncedQuoteInfo> CREATOR = new
			 Parcelable.Creator<SyncedQuoteInfo>() {
		         public SyncedQuoteInfo createFromParcel(Parcel in) {
		             return new SyncedQuoteInfo(in);
		         }

		         public SyncedQuoteInfo[] newArray(int size) {
		             return new SyncedQuoteInfo[size];
		         }
		};
		
		private SyncedQuoteInfo(Parcel in) {
	        readFromParcel(in);
	    }


	    public void readFromParcel(Parcel in) {
	    	mId = in.readString();
	        mBookTitle = in.readString();
	        mQuoteText = in.readString();
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
			out.writeString(mQuoteText);
	        out.writeInt(mParIndex);
	        out.writeInt(mElIndex);
	        out.writeInt(mChIndex);
	        out.writeString(mDate);
		}
}
