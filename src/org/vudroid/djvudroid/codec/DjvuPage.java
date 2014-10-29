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
package org.vudroid.djvudroid.codec;

import org.vudroid.core.codec.CodecPage;

import android.graphics.Bitmap;
import android.graphics.RectF;

public class DjvuPage implements CodecPage
{
    private long pageHandle;
    //TODO: remove all async operations
    private final Object waitObject;

    DjvuPage(long pageHandle, Object waitObject)
    {
        this.pageHandle = pageHandle;
        this.waitObject = waitObject;
    }

    public boolean isDecoding()
    {
        return false;
//        return !isDecodingDone(pageHandle);
    }

    private static native int getWidth(long pageHandle);

    private static native int getHeight(long pageHandle);

    private static native boolean isDecodingDone(long pageHandle);

    private static native boolean renderPage(long pageHandle, int targetWidth, int targetHeight, float pageSliceX,
                                    float pageSliceY,
                                    float pageSliceWidth,
                                    float pageSliceHeight, int[] buffer);

    private static native void free(long pageHandle);

    public void waitForDecode()
    {
    }

    public int getWidth()
    {
        return getWidth(pageHandle);
    }

    public int getHeight()
    {
        return getHeight(pageHandle);
    }

    public Bitmap renderBitmap(int width, int height, RectF pageSliceBounds)
    {
        final int[] buffer = new int[width * height];
        renderPage(pageHandle, width, height, pageSliceBounds.left, pageSliceBounds.top, pageSliceBounds.width(), pageSliceBounds.height(), buffer);
        return Bitmap.createBitmap(buffer, width, height, Bitmap.Config.RGB_565);
    }

    @Override
    protected void finalize() throws Throwable
    {
        recycle();
        super.finalize();
    }

    public synchronized void recycle() {
        if (pageHandle == 0) {
            return;
        }
        free(pageHandle);
        pageHandle = 0;
    }
}
