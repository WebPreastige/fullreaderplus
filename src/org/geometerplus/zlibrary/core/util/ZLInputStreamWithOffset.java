/*
* FullReader+
Copyright 2013-2014 Viktoriya Bilyk

Original FBreader code 
 * Copyright (C) 2007-2013 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.core.util;

import java.io.IOException;
import java.io.InputStream;

public class ZLInputStreamWithOffset extends InputStream {
	private final InputStream myDecoratedStream;
	private int myOffset = 0;

	public ZLInputStreamWithOffset(InputStream stream) {
		myDecoratedStream = stream;
	}

	@Override
	public int available() throws IOException {
		return myDecoratedStream.available();
	}

	@Override
	public long skip(long n) throws IOException {
		long shift = myDecoratedStream.skip(n);
		if (shift > 0) {
			myOffset += (int)shift;
		}
		while ((shift < n) && (read() != -1)) {
			++shift;
		}
		return shift;
	}

	@Override
	public int read() throws IOException {
		int result = myDecoratedStream.read();
		if (result != -1) {
			++myOffset;
		}
		return result;
	}

	@Override
	public void close() throws IOException {
		myOffset = 0;
		myDecoratedStream.close();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		final int shift = myDecoratedStream.read(b, off, len);
		if (shift > 0) {
			myOffset += shift;
		}
		return shift;
	}

	@Override
	public void reset() throws IOException {
		myOffset = 0;
		myDecoratedStream.reset();
	}

	public int offset() {
		return myOffset;
	}
}
