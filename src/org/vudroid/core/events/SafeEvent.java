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
package org.vudroid.core.events;

import java.lang.reflect.Method;

public abstract class SafeEvent<T> implements Event<T>
{
    private final Class<?> listenerType;

    protected SafeEvent()
    {
        listenerType = getListenerType();
    }

    private Class<?> getListenerType()
    {
        for (Method method : getClass().getMethods())
        {
            if ("dispatchSafely".equals(method.getName()) && !method.isSynthetic())
            {
                return method.getParameterTypes()[0];
            }
        }
        throw new RuntimeException("Couldn't find dispatchSafely method");
    }

    @SuppressWarnings({"unchecked"})
    public final void dispatchOn(Object listener)
    {
        if (listenerType.isAssignableFrom(listener.getClass()))
        {
            dispatchSafely((T) listener);
        }
    }

    public abstract void dispatchSafely(T listener);
}
