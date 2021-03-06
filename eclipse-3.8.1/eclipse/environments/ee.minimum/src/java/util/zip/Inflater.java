/*
 * $Header: /cvshome/build/ee.minimum/src/java/util/zip/Inflater.java,v 1.8 2006/11/07 03:19:12 hargrave Exp $
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

package java.util.zip;
public class Inflater {
	public Inflater() { }
	public Inflater(boolean var0) { }
	public void end() { }
	protected void finalize() { }
	public boolean finished() { return false; }
	public int getAdler() { return 0; }
	public int getRemaining() { return 0; }
	public int getTotalIn() { return 0; }
	public int getTotalOut() { return 0; }
	public int inflate(byte[] var0) throws java.util.zip.DataFormatException { return 0; }
	public int inflate(byte[] var0, int var1, int var2) throws java.util.zip.DataFormatException { return 0; }
	public boolean needsDictionary() { return false; }
	public boolean needsInput() { return false; }
	public void reset() { }
	public void setDictionary(byte[] var0) { }
	public void setDictionary(byte[] var0, int var1, int var2) { }
	public void setInput(byte[] var0) { }
	public void setInput(byte[] var0, int var1, int var2) { }
}

