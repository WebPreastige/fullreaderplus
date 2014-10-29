/*
Vudroid
Copyright 2010-2011 Pavel Tiunov

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
package org.vudroid.core;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.net.Uri;
import android.view.View;

public interface DecodeService
{
    void setContentResolver(ContentResolver contentResolver);

    void setContainerView(View containerView);

    void open(Uri fileUri);

    void decodePage(Object decodeKey, int pageNum, DecodeCallback decodeCallback, float zoom, RectF pageSliceBounds);

    void stopDecoding(Object decodeKey);

    int getEffectivePagesWidth();

    int getEffectivePagesHeight();

    int getPageCount();

    int getPageWidth(int pageIndex);

    int getPageHeight(int pageIndex);

    void recycle();

    public interface DecodeCallback
    {
        void decodeComplete(Bitmap bitmap);
    }
}
