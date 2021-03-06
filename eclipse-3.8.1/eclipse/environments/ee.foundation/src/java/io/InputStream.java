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

package java.io;
public abstract class InputStream {
	public InputStream() { }
	public int available() throws java.io.IOException { return 0; }
	public void close() throws java.io.IOException { }
	public void mark(int var0) { }
	public boolean markSupported() { return false; }
	public abstract int read() throws java.io.IOException;
	public int read(byte[] var0) throws java.io.IOException { return 0; }
	public int read(byte[] var0, int var1, int var2) throws java.io.IOException { return 0; }
	public void reset() throws java.io.IOException { }
	public long skip(long var0) throws java.io.IOException { return 0l; }
}

