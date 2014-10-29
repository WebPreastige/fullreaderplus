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

import org.vudroid.core.codec.CodecDocument;

public class DjvuDocument implements CodecDocument
{
    private long documentHandle;
    private final Object waitObject;

    private DjvuDocument(long documentHandle, Object waitObject)
    {
        this.documentHandle = documentHandle;
        this.waitObject = waitObject;
    }

    static DjvuDocument openDocument(String fileName, DjvuContext djvuContext, Object waitObject)
    {
        return new DjvuDocument(open(djvuContext.getContextHandle(), fileName), waitObject);
    }

    private native static long open(long contextHandle, String fileName);
    private native static long getPage(long docHandle, int pageNumber);
    private native static int getPageCount(long docHandle);
    private native static void free(long pageHandle);

    public DjvuPage getPage(int pageNumber)
    {
        return new DjvuPage(getPage(documentHandle, pageNumber), waitObject);
    }

    public int getPageCount()
    {
        return getPageCount(documentHandle);
    }

    @Override
    protected void finalize() throws Throwable
    {
        recycle();
        super.finalize();
    }

    public synchronized void recycle() {
        if (documentHandle == 0) {
            return;
        }
        free(documentHandle);
        documentHandle = 0;
    }
}
