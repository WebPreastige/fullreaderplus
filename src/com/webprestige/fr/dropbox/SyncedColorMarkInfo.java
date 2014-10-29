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

public class SyncedColorMarkInfo implements Parcelable{
	
	private String mId;
	private String mBookTitle;
	private String mQuoteText;
	private int mParIndex;
	private int mElIndex;
	private int mChIndex;
	private String mDate;
	
	private int mStartParIndex;
	private int mStartElIndex;
	private int mStartChIndex;
	
	private int mEndParIndex;
	private int mEndElIndex;
	private int mEndChIndex;
	private int mColor;
	private String mHexColor;
	
	public SyncedColorMarkInfo(String id, String bookTitle, String quoteText, String date, int parIndex, int elIndex, int chIndex, 
			int startParIndex, int startElIndex, int startChIndex, int endParIndex, int endElIndex, int endChIndex, int color, String hexColor){
		mId	= id;
		mBookTitle = bookTitle;
		mQuoteText = quoteText;
		mParIndex = parIndex;
		mElIndex = elIndex;
		mChIndex = chIndex;
		mDate = date;
		mStartParIndex = startParIndex;
		mStartElIndex = startElIndex;
		mStartChIndex = startChIndex;
		mEndParIndex = endParIndex;
		mEndElIndex = endElIndex;
		mEndChIndex = endChIndex;
		mColor = color;
		mHexColor = hexColor;
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
	
	public int getStartParIndex(){
		return mStartParIndex;
	}
	
	public int getStartElIndex(){
		return mStartElIndex;
	}
	
	public int getStartChIndex(){
		return mStartChIndex;
	}
	
	public int getEndParIndex(){
		return mEndParIndex;
	}
	
	public int getEndElIndex(){
		return mEndElIndex;
	}
	
	public int getEndChIndex(){
		return mEndChIndex;
	}
	
	public int getColor(){
		return mColor;
	}
	
	public String getHexColor(){
		return mHexColor;
	}
	
	public static final Parcelable.Creator<SyncedColorMarkInfo> CREATOR = new
			 Parcelable.Creator<SyncedColorMarkInfo>() {
		         public SyncedColorMarkInfo createFromParcel(Parcel in) {
		             return new SyncedColorMarkInfo(in);
		         }

		         public SyncedColorMarkInfo[] newArray(int size) {
		             return new SyncedColorMarkInfo[size];
		         }
		};
		
		private SyncedColorMarkInfo(Parcel in) {
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
	        
	        mStartParIndex = in.readInt();
	        mStartElIndex = in.readInt();
	        mStartChIndex = in.readInt();
	        
	        mEndParIndex = in.readInt();
	        mEndElIndex = in.readInt();
	        mEndChIndex = in.readInt();
	        
	        mColor = in.readInt();
	        mHexColor = in.readString();
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
	        
	        out.writeInt(mStartParIndex);
	        out.writeInt(mStartElIndex);
	        out.writeInt(mStartChIndex);
	        
	        out.writeInt(mEndParIndex);
	        out.writeInt(mEndElIndex);
	        out.writeInt(mEndChIndex);
	        
	        out.writeInt(mColor);
	        out.writeString(mHexColor);
		}

}
