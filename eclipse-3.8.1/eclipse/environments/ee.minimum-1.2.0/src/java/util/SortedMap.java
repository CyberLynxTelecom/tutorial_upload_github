/*
 * $Revision: 6107 $
 *
 * (C) Copyright 2001 Sun Microsystems, Inc.
 * Copyright (c) OSGi Alliance (2001, 2008). All Rights Reserved.
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
public abstract interface SortedMap extends java.util.Map {
	public abstract java.util.Comparator comparator();
	public abstract java.lang.Object firstKey();
	public abstract java.util.SortedMap headMap(java.lang.Object var0);
	public abstract java.lang.Object lastKey();
	public abstract java.util.SortedMap subMap(java.lang.Object var0, java.lang.Object var1);
	public abstract java.util.SortedMap tailMap(java.lang.Object var0);
}

