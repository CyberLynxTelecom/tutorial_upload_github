/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import org.eclipse.jface.window.Window;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.operations.ITagOperation;
import org.eclipse.team.internal.ccvs.ui.tags.TagSelectionDialog;

public class MoveRemoteTagAction extends TagInRepositoryAction {

	/**
	 * @see TagRemoteAction#promptForTag(ICVSFolder[])
	 */
	protected ITagOperation configureOperation() {
		// Allow the user to select a tag
		ITagOperation operation = createTagOperation();
		TagSelectionDialog dialog = new TagSelectionDialog(getShell(), operation.getTagSource(), 
			CVSUIMessages.MoveTagAction_title, 
			CVSUIMessages.MoveTagAction_message, 
			TagSelectionDialog.INCLUDE_BRANCHES | TagSelectionDialog.INCLUDE_VERSIONS, 
			isFolderSelected(), /* show recurse */
			IHelpContextIds.TAG_REMOTE_WITH_EXISTING_DIALOG);
		dialog.setBlockOnOpen(true);
		if (dialog.open() == Window.CANCEL) {
			return null;
		}
		CVSTag tag = dialog.getResult();
		if (tag == null) return null;
		operation.setTag(tag);
		operation.moveTag();
		boolean recursive = dialog.getRecursive();
		if (!recursive)  {
			operation.doNotRecurse();
		}
		return operation;
	}

    private boolean isFolderSelected() {
        ICVSResource[] resources = getSelectedCVSResources();
        for (int i = 0; i < resources.length; i++) {
            ICVSResource resource = resources[i];
            if (resource.isFolder()) 
                return true;
        }
        return false;
    }
}
