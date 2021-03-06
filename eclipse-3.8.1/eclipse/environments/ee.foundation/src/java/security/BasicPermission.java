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

package java.security;
public abstract class BasicPermission extends java.security.Permission implements java.io.Serializable {
	public BasicPermission(java.lang.String var0) { super((java.lang.String) null); }
	public BasicPermission(java.lang.String var0, java.lang.String var1) { super((java.lang.String) null); }
	public boolean equals(java.lang.Object var0) { return false; }
	public java.lang.String getActions() { return null; }
	public int hashCode() { return 0; }
	public boolean implies(java.security.Permission var0) { return false; }
}

