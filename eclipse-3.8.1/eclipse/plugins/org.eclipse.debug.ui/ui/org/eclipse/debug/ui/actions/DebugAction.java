/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui.actions;


import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.actions.ExecutionAction;
import org.eclipse.debug.ui.IDebugUIConstants;

/**
 * Action to launch the last launch configuration that was successfully
 * launched, in debug mode. If no configurations have been launched, the launch
 * configuration dialog is opened.
 * <p>
 * This class may be instantiated.
 * </p>
 * @since 2.0
 */
public final class DebugAction extends ExecutionAction {
	
	public DebugAction() {
		super(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
	}

	/**
	 * @see ExecutionAction#getMode()
	 */
	protected String getMode() {
		return ILaunchManager.DEBUG_MODE;
	}
	
}
