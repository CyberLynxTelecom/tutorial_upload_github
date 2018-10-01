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
public abstract interface DataOutput {
	public abstract void write(int var0) throws java.io.IOException;
	public abstract void write(byte[] var0) throws java.io.IOException;
	public abstract void write(byte[] var0, int var1, int var2) throws java.io.IOException;
	public abstract void writeBoolean(boolean var0) throws java.io.IOException;
	public abstract void writeByte(int var0) throws java.io.IOException;
	public abstract void writeBytes(java.lang.String var0) throws java.io.IOException;
	public abstract void writeChar(int var0) throws java.io.IOException;
	public abstract void writeChars(java.lang.String var0) throws java.io.IOException;
	public abstract void writeDouble(double var0) throws java.io.IOException;
	public abstract void writeFloat(float var0) throws java.io.IOException;
	public abstract void writeInt(int var0) throws java.io.IOException;
	public abstract void writeLong(long var0) throws java.io.IOException;
	public abstract void writeShort(int var0) throws java.io.IOException;
	public abstract void writeUTF(java.lang.String var0) throws java.io.IOException;
}

