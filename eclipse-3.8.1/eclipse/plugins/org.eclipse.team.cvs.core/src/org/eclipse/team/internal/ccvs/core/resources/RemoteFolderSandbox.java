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
package org.eclipse.team.internal.ccvs.core.resources;

import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;

/**
 * This specialized remote folder supports the creation of a cached sandbox.
 */
public class RemoteFolderSandbox extends RemoteFolder {

	public RemoteFolderSandbox(RemoteFolder parent, ICVSRepositoryLocation repository, String repositoryRelativePath, CVSTag tag) {
		super(parent, repository, repositoryRelativePath, tag);
		setChildren(new ICVSRemoteResource[0]);
	}

	public RemoteFolderSandbox(RemoteFolder parent, String name, CVSRepositoryLocation repository, String repositoryRelativePath, CVSEntryLineTag tag, boolean isStatic) {
		super(parent, name, repository, repositoryRelativePath, tag, isStatic);
		setChildren(new ICVSRemoteResource[0]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFolder#getFile(java.lang.String)
	 */
	public ICVSFile getFile(String name) throws CVSException {
		try {
			return super.getFile(name);
		} catch (CVSException e) {
			if (e.getStatus().getCode() == CHILD_DOES_NOT_EXIST) {
				IPath path = new Path(null, name);
				String fileName = path.lastSegment();
				RemoteFolderSandbox parent = getParentFolder(path);
				RemoteFile file = new RemoteFile(parent, Update.STATE_NONE, fileName, null, null, getTag());
				parent.addChild(file);
				return file;
			}
			throw e;
		}
	}
	
	private void addChild(RemoteResource resource) {
		ICVSRemoteResource[] children = getChildren();
		ICVSRemoteResource[] newChildren = new ICVSRemoteResource[children.length + 1];
		System.arraycopy(children, 0, newChildren, 0, children.length);
		newChildren[children.length] = resource;
		setChildren(newChildren);
	}

	private RemoteFolderSandbox getParentFolder(IPath path) throws CVSException {
		IPath parentPath = path.removeLastSegments(1);
		String parentString;
		if (parentPath.isEmpty()) {
			parentString = Session.CURRENT_LOCAL_FOLDER;
		} else {
			parentString = path.removeLastSegments(1).toString();
		}
		RemoteFolderSandbox parent = (RemoteFolderSandbox)getFolder(parentString);
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFolder#getFolder(java.lang.String)
	 */
	public ICVSFolder getFolder(String name) throws CVSException {
		try {
			return super.getFolder(name);
		} catch (CVSException e) {
			if (e.getStatus().getCode() == CHILD_DOES_NOT_EXIST) {
				IPath path = new Path(null, name);
				RemoteFolderSandbox parent = getParentFolder(path);
				String repoPath = new Path(null, parent.getRepositoryRelativePath()).append(path.lastSegment()).removeTrailingSeparator().toString();
				RemoteFolderSandbox folder = new RemoteFolderSandbox(parent, getRepository(), repoPath, getTag());
				parent.addChild(folder);
				return folder;
			}
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.resources.RemoteFolder#getMembers(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public ICVSRemoteResource[] getMembers(IProgressMonitor monitor) throws TeamException {
		return getChildren();
	}

	/*
	 * @see ICVSFolder#acceptChildren(ICVSResourceVisitor)
	 */
	public void acceptChildren(ICVSResourceVisitor visitor) throws CVSException {
		ICVSRemoteResource[] children = getChildren();
		if (children == null) return;
		for (int i=0; i<children.length; i++) {
			((ICVSResource)children[i]).accept(visitor);
		}
	}

	public void remove(RemoteFile file) {
		ICVSRemoteResource[] children = getChildren();
		ArrayList results = new ArrayList();
		for (int i = 0; i < children.length; i++) {
			if (children[i] != file){
				results.add(children[i]);
			}
		}
		setChildren((ICVSRemoteResource[]) results.toArray(new ICVSRemoteResource[results.size()]));		
	}
}
