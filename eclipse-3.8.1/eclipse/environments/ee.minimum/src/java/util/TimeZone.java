/*
 * $Header: /cvshome/build/ee.minimum/src/java/util/TimeZone.java,v 1.8 2006/11/07 03:19:09 hargrave Exp $
 *
 * (C) Copyright 2001 Sun Microsystems, Inc.
 * Copyright (c) OSGi Alliance (2001, 2006). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.util;
public abstract class TimeZone implements java.io.Serializable, java.lang.Cloneable {
	public TimeZone() { }
	public java.lang.Object clone() { return null; }
	public static java.lang.String[] getAvailableIDs() { return null; }
	public static java.lang.String[] getAvailableIDs(int var0) { return null; }
	public static java.util.TimeZone getDefault() { return null; }
	public java.lang.String getID() { return null; }
	public abstract int getOffset(int var0, int var1, int var2, int var3, int var4, int var5);
	public abstract int getRawOffset();
	public static java.util.TimeZone getTimeZone(java.lang.String var0) { return null; }
	public abstract boolean inDaylightTime(java.util.Date var0);
	public static void setDefault(java.util.TimeZone var0) { }
	public void setID(java.lang.String var0) { }
	public abstract void setRawOffset(int var0);
	public abstract boolean useDaylightTime();
}

