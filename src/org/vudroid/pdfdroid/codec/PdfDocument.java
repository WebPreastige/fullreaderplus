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
package org.vudroid.pdfdroid.codec;

import org.vudroid.core.codec.CodecDocument;
import org.vudroid.core.codec.CodecPage;

public class PdfDocument implements CodecDocument
{
    private long docHandle;
    private static final int FITZMEMORY = 512 * 1024;

    private PdfDocument(long docHandle)
    {
        this.docHandle = docHandle;
    }

    public CodecPage getPage(int pageNumber)
    {
        return PdfPage.createPage(docHandle, pageNumber + 1);
    }

    public int getPageCount()
    {
        return getPageCount(docHandle);
    }

    static PdfDocument openDocument(String fname, String pwd)
    {
        return new PdfDocument(open(FITZMEMORY, fname, pwd));
    }

    private static native long open(int fitzmemory, String fname, String pwd);

    private static native void free(long handle);

    private static native int getPageCount(long handle);

    @Override
    protected void finalize() throws Throwable
    {
        recycle();
        super.finalize();
    }

    public synchronized void recycle() {
        if (docHandle != 0) {
            free(docHandle);
            docHandle = 0;
        }
    }
}
