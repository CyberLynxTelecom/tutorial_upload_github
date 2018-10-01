/*
 * $Header: /cvshome/build/ee.minimum/src/java/net/Authenticator.java,v 1.8 2006/11/07 03:19:11 hargrave Exp $
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
public abstract class Authenticator {
	public Authenticator() { }
	protected java.net.PasswordAuthentication getPasswordAuthentication() { return null; }
	protected final int getRequestingPort() { return 0; }
	protected final java.lang.String getRequestingPrompt() { return null; }
	protected final java.lang.String getRequestingProtocol() { return null; }
	protected final java.lang.String getRequestingScheme() { return null; }
	protected final java.net.InetAddress getRequestingSite() { return null; }
	public static java.net.PasswordAuthentication requestPasswordAuthentication(java.net.InetAddress var0, int var1, java.lang.String var2, java.lang.String var3, java.lang.String var4) { return null; }
	public static void setDefault(java.net.Authenticator var0) { }
}

