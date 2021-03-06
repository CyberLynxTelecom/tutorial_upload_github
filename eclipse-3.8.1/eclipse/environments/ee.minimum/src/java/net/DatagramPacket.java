/*
 * $Header: /cvshome/build/ee.minimum/src/java/net/DatagramPacket.java,v 1.8 2006/11/07 03:19:11 hargrave Exp $
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

package java.net;
public final class DatagramPacket {
	public DatagramPacket(byte[] var0, int var1) { }
	public DatagramPacket(byte[] var0, int var1, int var2) { }
	public DatagramPacket(byte[] var0, int var1, int var2, java.net.InetAddress var3, int var4) { }
	public DatagramPacket(byte[] var0, int var1, java.net.InetAddress var2, int var3) { }
	public java.net.InetAddress getAddress() { return null; }
	public byte[] getData() { return null; }
	public int getLength() { return 0; }
	public int getOffset() { return 0; }
	public int getPort() { return 0; }
	public void setAddress(java.net.InetAddress var0) { }
	public void setData(byte[] var0) { }
	public void setData(byte[] var0, int var1, int var2) { }
	public void setLength(int var0) { }
	public void setPort(int var0) { }
}

