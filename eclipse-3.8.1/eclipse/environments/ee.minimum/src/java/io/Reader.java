/*
 * $Header: /cvshome/build/ee.minimum/src/java/io/Reader.java,v 1.8 2006/11/07 03:19:10 hargrave Exp $
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

package java.io;
public abstract class Reader {
	protected Reader() { }
	protected Reader(java.lang.Object var0) { }
	public abstract void close() throws java.io.IOException;
	public void mark(int var0) throws java.io.IOException { }
	public boolean markSupported() { return false; }
	public int read() throws java.io.IOException { return 0; }
	public int read(char[] var0) throws java.io.IOException { return 0; }
	public abstract int read(char[] var0, int var1, int var2) throws java.io.IOException;
	public boolean ready() throws java.io.IOException { return false; }
	public void reset() throws java.io.IOException { }
	public long skip(long var0) throws java.io.IOException { return 0l; }
	protected java.lang.Object lock;
}

