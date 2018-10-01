/*******************************************************************************
 *  Copyright (c) 2005, 2008 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.pde.internal.core.text.bundle;

import org.eclipse.osgi.service.resolver.VersionRange;
import org.eclipse.pde.internal.core.ibundle.IBundle;

public class BundleVersionHeader extends SingleManifestHeader {

	private static final long serialVersionUID = 1L;

	public BundleVersionHeader(String name, String value, IBundle bundle, String lineDelimiter) {
		super(name, value, bundle, lineDelimiter);
	}

	public void setVersionRange(String range) {
		setMainComponent(range);
	}

	public VersionRange getVersionRange() {
		return new VersionRange(getMainComponent());
	}

}
