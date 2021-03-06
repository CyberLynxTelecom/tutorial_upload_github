/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.net.proxy;

/**
 * A listener that, when registered with the {@link IProxyService}, gets notified when the
 * proxy information changes.
 * <p>
 * This interface may be implemented by clients
 * @since 1.0
 */
public interface IProxyChangeListener {

	/**
	 * Callback that occurs when information related to proxies has changed.
	 * @param event the event that describes the change
	 */
	void proxyInfoChanged(IProxyChangeEvent event);
}
