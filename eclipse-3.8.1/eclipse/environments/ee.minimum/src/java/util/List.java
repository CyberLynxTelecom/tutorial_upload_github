/*
 * $Header: /cvshome/build/ee.minimum/src/java/util/List.java,v 1.9 2006/11/07 06:07:58 hargrave Exp $
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
public abstract interface List extends java.util.Collection {
	public abstract void add(int var0, java.lang.Object var1);
	public abstract boolean addAll(int var0, java.util.Collection var1);
	public abstract boolean equals(java.lang.Object var0);
	public abstract java.lang.Object get(int var0);
	public abstract int hashCode();
	public abstract int indexOf(java.lang.Object var0);
	public abstract int lastIndexOf(java.lang.Object var0);
	public abstract java.util.ListIterator listIterator();
	public abstract java.util.ListIterator listIterator(int var0);
	public abstract java.lang.Object remove(int var0);
	public abstract java.lang.Object set(int var0, java.lang.Object var1);
	public abstract java.util.List subList(int var0, int var1);
}

