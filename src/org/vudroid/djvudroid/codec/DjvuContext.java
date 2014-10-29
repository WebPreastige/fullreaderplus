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

import java.util.concurrent.Semaphore;

import org.vudroid.core.VuDroidLibraryLoader;
import org.vudroid.core.codec.CodecContext;

import android.content.ContentResolver;
import android.util.Log;

public class DjvuContext implements Runnable, CodecContext
{
    static
    {
        VuDroidLibraryLoader.load();        
    }

    private long contextHandle;
    private static final String DJVU_DROID_CODEC_LIBRARY = "DjvuDroidCodecLibrary";
    private final Object waitObject = new Object();
    private final Semaphore docSemaphore = new Semaphore(0);

    public DjvuContext()
    {
        this.contextHandle = create();
        new Thread(this).start();
    }

    public DjvuDocument  openDocument(String fileName)
    {
        final DjvuDocument djvuDocument = DjvuDocument.openDocument(fileName, this, waitObject);
        try
        {
            docSemaphore.acquire();
        } catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        return djvuDocument;
    }

    long getContextHandle()
    {
        return contextHandle;
    }

    public void run()
    {
        for(;;)
        {
            try
            {
                synchronized (this) {
                    if (isRecycled()) return;
                    handleMessage(contextHandle);
                    wait(200);
                }
                synchronized (waitObject)
                {
                    waitObject.notifyAll();
                }
            }
            catch (Exception e)
            {
                Log.e(DJVU_DROID_CODEC_LIBRARY, "Codec error", e);
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void handleDocInfo()
    {
        docSemaphore.release();
    }

    public void setContentResolver(ContentResolver contentResolver)
    {
    }

    @Override
    protected void finalize() throws Throwable
    {
        recycle();
        super.finalize();
    }

    public synchronized void recycle() {
        if (isRecycled()) {
            return;
        }
        free(contextHandle);
        contextHandle = 0;
    }

    private boolean isRecycled() {
        return contextHandle == 0;
    }

    private static native long create();
    private static native void free(long contextHandle);
    private native void handleMessage(long contextHandle);
}
