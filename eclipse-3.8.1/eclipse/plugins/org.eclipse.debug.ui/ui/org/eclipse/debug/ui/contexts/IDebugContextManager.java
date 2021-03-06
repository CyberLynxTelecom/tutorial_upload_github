/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.contexts;

import org.eclipse.ui.IWorkbenchWindow;

/**
 * Manages debug context services. There is a debug context service
 * for each workbench window. Clients interested in context change
 * notification for all windows can register with the manager. Clients
 * interested in context change notification for a specific window
 * can register with the context service specific to that window.
 * <p>
 * Clients register debug context providers with a context service - i.e.
 * for a specific window.
 * </p> 
 * @see IDebugContextProvider
 * @see IDebugContextListener
 * @see IDebugContextService
 * @since 3.3
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IDebugContextManager {		
	
	/**
	 * Registers the given listener for debug context notification in all windows.
	 * 
	 * @param listener debug context listener
	 */	
	public void addDebugContextListener(IDebugContextListener listener);
	
	/**
	 * Unregisters the given listener for context notification in all windows.
	 * 
	 * @param listener debug context listener
	 */	
	public void removeDebugContextListener(IDebugContextListener listener);
	
	/**
	 * Returns the debug context service for the specified window.
	 * 
	 * @param window workbench window
	 * @return debug context service
	 */
	public IDebugContextService getContextService(IWorkbenchWindow window);
}
