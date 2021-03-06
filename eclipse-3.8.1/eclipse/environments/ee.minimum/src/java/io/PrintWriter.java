/*
 * $Header: /cvshome/build/ee.minimum/src/java/io/PrintWriter.java,v 1.8 2006/11/07 03:19:10 hargrave Exp $
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
public class PrintWriter extends java.io.Writer {
	public PrintWriter(java.io.OutputStream var0) { }
	public PrintWriter(java.io.OutputStream var0, boolean var1) { }
	public PrintWriter(java.io.Writer var0) { }
	public PrintWriter(java.io.Writer var0, boolean var1) { }
	public boolean checkError() { return false; }
	public void close() { }
	public void flush() { }
	public void print(char var0) { }
	public void print(double var0) { }
	public void print(float var0) { }
	public void print(int var0) { }
	public void print(long var0) { }
	public void print(java.lang.Object var0) { }
	public void print(java.lang.String var0) { }
	public void print(boolean var0) { }
	public void print(char[] var0) { }
	public void println() { }
	public void println(char var0) { }
	public void println(double var0) { }
	public void println(float var0) { }
	public void println(int var0) { }
	public void println(long var0) { }
	public void println(java.lang.Object var0) { }
	public void println(java.lang.String var0) { }
	public void println(boolean var0) { }
	public void println(char[] var0) { }
	protected void setError() { }
	public void write(int var0) { }
	public void write(java.lang.String var0) { }
	public void write(java.lang.String var0, int var1, int var2) { }
	public void write(char[] var0) { }
	public void write(char[] var0, int var1, int var2) { }
	protected java.io.Writer out;
}

