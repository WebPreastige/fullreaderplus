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
package org.vudroid.core.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5StringUtil
{
    private static final MessageDigest digest;

    static
    {
        try
        {
            digest = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String md5StringFor(String s)
    {
        final byte[] hash = digest.digest(s.getBytes());
        final StringBuilder builder = new StringBuilder();
        for (byte b : hash)
        {
            builder.append(Integer.toString(b & 0xFF, 16));    
        }
        return builder.toString();
    }
}
