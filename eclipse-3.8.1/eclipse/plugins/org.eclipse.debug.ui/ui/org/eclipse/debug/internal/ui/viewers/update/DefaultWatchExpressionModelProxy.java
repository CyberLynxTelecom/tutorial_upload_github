/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Pawel Piech - Bug 210023: Another NPE in DefaultWatchExpressionModelProxy
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.contexts.IDebugContextService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

/**
 * Model proxy for the expressions view
 * 
 * @see org.eclipse.debug.internal.ui.views.expression.ExpressionView
 * @see org.eclipse.debug.internal.ui.model.elements.ExpressionContentProvider
 * @see org.eclipse.debug.internal.ui.model.elements.ExpressionsViewMementoProvider
 * 
 * @since 3.2
 */
public class DefaultWatchExpressionModelProxy extends DefaultExpressionModelProxy implements IDebugContextListener {
	
	private IWorkbenchWindow fWindow;
	
	public DefaultWatchExpressionModelProxy(IWatchExpression expression) {
		super(expression);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#installed(org.eclipse.jface.viewers.Viewer)
	 */
	public void installed(final Viewer viewer) {
		super.installed(viewer);
		UIJob job = new UIJob("install watch expression model proxy") { //$NON-NLS-1$
			public IStatus runInUIThread(IProgressMonitor monitor) {
			    if (!isDisposed()) {
    				IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
    				for (int i = 0; i < workbenchWindows.length; i++) {
    					IWorkbenchWindow window = workbenchWindows[i];
    					// Virtual viewer may have a null control.
    					Control control = viewer.getControl(); 
    					if (control != null && control.getShell().equals(window.getShell())) {
    						fWindow = window;
    						break;
    					}
    				}
    				if (fWindow == null) {
    					fWindow = DebugUIPlugin.getActiveWorkbenchWindow();
    				}
    				IDebugContextService contextService = DebugUITools.getDebugContextManager().getContextService(fWindow);
    				contextService.addDebugContextListener(DefaultWatchExpressionModelProxy.this);
    				ISelection activeContext = contextService.getActiveContext();
    				if (activeContext != null) {
    					contextActivated(activeContext);
    				}
			    }
				return Status.OK_STATUS;
			}
		
		};
		job.setSystem(true);
		job.schedule();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.DefaultExpressionModelProxy#dispose()
	 */
	public synchronized void dispose() {
		super.dispose();
		if (fWindow != null) {
            DebugUITools.getDebugContextManager().getContextService(fWindow).removeDebugContextListener(this);
    		fWindow = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.update.EventHandlerModelProxy#createEventHandlers()
	 */
	protected DebugEventHandler[] createEventHandlers() {
		return new DebugEventHandler[]{new ExpressionEventHandler(this)};
	}

	/**
	 * Handles the activation of the specified debug context (i.e. the selection)
	 * @param selection the specified context to 'activate'
	 */
	protected void contextActivated(ISelection selection) {
		if (fWindow != null) {
			if (selection instanceof IStructuredSelection) {
				IDebugElement context = null;
				IStructuredSelection ss = (IStructuredSelection)selection;
				if (ss.size() < 2) {
					Object object = ss.getFirstElement();
					if (object instanceof IDebugElement) {
						context= (IDebugElement) object;
					} else if (object instanceof ILaunch) {
						context= ((ILaunch) object).getDebugTarget();
					}
				}
				IWatchExpression expression = (IWatchExpression)getExpression();
				if (expression != null){
					expression.setExpressionContext(context);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextListener#debugContextChanged(org.eclipse.debug.ui.contexts.DebugContextEvent)
	 */
	public void debugContextChanged(DebugContextEvent event) {
		if ((event.getFlags() & DebugContextEvent.ACTIVATED) > 0) {
			contextActivated(event.getContext());
		}
	}
	
}
