/*******************************************************************************
 *  Copyright (c) 2005, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - Fix for viewer state save/restore [188704] 
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.model.provisional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A model delta. Used to create model deltas.
 * <p>
 * Clients may instantiate this class; not intended to be sub-classed.
 * </p>
 * @see IModelDelta
 * @since 3.2
 */
public class ModelDelta implements IModelDelta {

	private IModelDelta fParent;
	private Object fElement;
	private int fFlags;
	private ModelDelta[] fNodes = EMPTY_NODES;
	private List fNodesList = null;
	private Map fNodesMap;
	private Object fReplacement;
	private int fIndex = -1;
	private int fChildCount = -1;
	private static final ModelDelta[] EMPTY_NODES = new ModelDelta[0];

	/**
	 * Constructs a new delta for the given element.
	 * 
	 * @param element model element
	 * @param flags change flags
	 */
	public ModelDelta(Object element, int flags) {
		fElement = element;
		fFlags = flags;
	}

	/**
	 * Constructs a new delta for the given element to be replaced
	 * with the specified replacement element.
	 * 
	 * @param element model element
	 * @param replacement replacement element
	 * @param flags change flags
	 */
	public ModelDelta(Object element, Object replacement, int flags) {
        fElement = element;
        fReplacement = replacement;
        fFlags = flags;
    }

	/**
	 * Constructs a new delta for the given element to be inserted at
	 * the specified index.
	 * 
	 * @param element model element
	 * @param index insertion position
	 * @param flags change flags
	 */
    public ModelDelta(Object element, int index, int flags) {
        fElement = element;
        fIndex = index;
        fFlags = flags;
    }
    
	/**
	 * Constructs a new delta for the given element at the specified index
	 * relative to its parent with the given number of children.
	 * 
	 * @param element model element
	 * @param index insertion position
	 * @param flags change flags
	 * @param childCount number of children this node has
	 */
    public ModelDelta(Object element, int index, int flags, int childCount) {
        fElement = element;
        fIndex = index;
        fFlags = flags;
        fChildCount = childCount;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getElement()
     */
    public Object getElement() {
		return fElement;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getFlags()
	 */
	public int getFlags() {
		return fFlags;
	}

	/**
	 * Adds a child node to this delta with the given element and change flags,
	 * and returns the child delta.
	 * 
	 * @param element child element to add
	 * @param flags change flags for child
	 * @return newly created child delta
	 */
	public ModelDelta addNode(Object element, int flags) {
		ModelDelta node = new ModelDelta(element, flags);
		node.setParent(this);
		addDelta(node);
		return node;
	}
	
	/**
	 * Returns the child delta for the given element, or <code>null</code> if none.
	 * 
	 * @param element child element
	 * @return corresponding delta node, or <code>null</code>
	 */
	public ModelDelta getChildDelta(Object element) {
	    if (fNodesMap == null) {
	        mapNodes();
	    }
        Object nodeOrNodes = fNodesMap.get(element);
        if (nodeOrNodes instanceof ModelDelta) {
            return (ModelDelta)nodeOrNodes;
        } else if (nodeOrNodes instanceof ModelDelta[]) {
            return ((ModelDelta[])nodeOrNodes)[0];
        }
	    return null;
	}

	/**
     * Returns the child delta for the given element and index, or <code>null</code> if none.
     * 
     * @param element Element of the child delta to find
     * @param index Index of the child delta to find.
     * @return corresponding delta node, or <code>null</code>
     * 
     * @since 3.8
     */
    public ModelDelta getChildDelta(Object element, int index) {
        if (fNodesMap == null) {
            mapNodes();
        }
        Object nodeOrNodes = fNodesMap.get(element);
        if (nodeOrNodes instanceof ModelDelta) {
            ModelDelta node = (ModelDelta)nodeOrNodes;
            if (index == node.getIndex()) {
                return node;
            }
        } else if (nodeOrNodes instanceof ModelDelta[]) {
            ModelDelta[] nodes = (ModelDelta[])nodeOrNodes;
            for (int i = 0; i < nodes.length; i++) {
                if (index == nodes[i].getIndex()) {
                    return nodes[i];
                }
            }
        }
        return null;
    }

	private void mapNodes() {
	    if (fNodesList == null) {
	        fNodesMap = new HashMap(1);
	        return;
	    }
	    // Create a map with capacity for all child nodes.
	    fNodesMap = new HashMap(fNodesList.size()*4/3);
	    for (int i = 0; i < fNodesList.size(); i++) {
	        mapNode( (ModelDelta)fNodesList.get(i) );
	    }
	}
	
	private void mapNode(ModelDelta node) {
        Object oldValue = fNodesMap.put(node.getElement(), node);
        if (oldValue instanceof ModelDelta) {
            // Edge case: already a node for given element was added.
            ModelDelta[] nodes = new ModelDelta[] { (ModelDelta)oldValue, node };
            fNodesMap.put(node.getElement(), nodes);
        } else if (oldValue instanceof ModelDelta[]) {
            // Even more remote case: multiple delta nodes for the same element were already added
            ModelDelta[] oldNodes = (ModelDelta[])oldValue;
            ModelDelta[] newNodes = new ModelDelta[oldNodes.length + 1];
            System.arraycopy(oldNodes, 0, newNodes, 0, oldNodes.length);
            newNodes[newNodes.length - 1] = node;
            fNodesMap.put(node.getElement(), newNodes);
        }
	}
	

	/**
	 * Adds a child node to this delta to replace the given element with the
	 * specified replacement element and change flags, and returns the 
	 * newly created child delta.
	 * 
	 * @param element child element to add to this delta
	 * @param replacement replacement element for the child element
	 * @param flags change flags
	 * @return newly created child delta
	 */
    public ModelDelta addNode(Object element, Object replacement, int flags) {
        ModelDelta node = new ModelDelta(element, replacement, flags);
        node.setParent(this);
        addDelta(node);
        return node;
    }

    /**
     * Adds a child delta to this delta to insert the specified element at
     * the given index, and returns the newly created child delta.
     * 
     * @param element child element in insert
     * @param index index of insertion
     * @param flags change flags
     * @return newly created child delta
     */
    public ModelDelta addNode(Object element, int index, int flags) {
        ModelDelta node = new ModelDelta(element, index, flags);
        node.setParent(this);
        addDelta(node);
        return node;
    }
    
    /**
     * Adds a child delta to this delta at the specified index with the
     * given number of children, and returns the newly created child delta.
     * 
     * @param element child element in insert
     * @param index index of the element relative to parent
     * @param flags change flags
     * @param numChildren the number of children the element has
     * @return newly created child delta
     */
    public ModelDelta addNode(Object element, int index, int flags, int numChildren) {
        ModelDelta node = new ModelDelta(element, index, flags, numChildren);
        node.setParent(this);
        addDelta(node);
        return node;
    }
    
    /**
     * Sets the parent delta of this delta
     * 
     * @param node parent delta
     */
	void setParent(ModelDelta node) {
		fParent = node;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getParent()
	 */
	public IModelDelta getParentDelta() {
		return fParent;
	}

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getReplacementElement()
     */
    public Object getReplacementElement() {
        return fReplacement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getIndex()
     */
    public int getIndex() {
        return fIndex;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.IModelDelta#getNodes()
	 */
	public IModelDelta[] getChildDeltas() {
	    if (fNodes == null) {
	        fNodes = (ModelDelta[])fNodesList.toArray(new ModelDelta[fNodesList.size()]);
	    }
		return fNodes;
	}
	
	private void addDelta(ModelDelta delta) {
	    if (fNodesList == null) fNodesList = new ArrayList(4);
	    fNodesList.add(delta);
	    fNodes = null;
	    if (fNodesMap != null) {
	        mapNode(delta);
	    }
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Model Delta Start\n"); //$NON-NLS-1$
		appendDetail("  ", buf, this); //$NON-NLS-1$
		buf.append("Model Delta End\n"); //$NON-NLS-1$
		return buf.toString();
	}
	
	private void appendDetail(String indent, StringBuffer buf, IModelDelta delta) {
        buf.append(indent);
		buf.append("Element: "); //$NON-NLS-1$
		buf.append(delta.getElement());
		buf.append('\n');
        buf.append(indent);
		buf.append("    Flags: "); //$NON-NLS-1$
		int flags = delta.getFlags();
		if (flags == 0) {
			buf.append("NO_CHANGE"); //$NON-NLS-1$
		} else {
			if ((flags & IModelDelta.ADDED) > 0) {
				buf.append("ADDED | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.CONTENT) > 0) {
				buf.append("CONTENT | "); //$NON-NLS-1$
			}
            if ((flags & IModelDelta.COLLAPSE) > 0) {
                buf.append("COLLAPSE | "); //$NON-NLS-1$
            }
			if ((flags & IModelDelta.EXPAND) > 0) {
				buf.append("EXPAND | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.INSERTED) > 0) {
				buf.append("INSERTED | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.REMOVED) > 0) {
				buf.append("REMOVED | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.REPLACED) > 0) {
				buf.append("REPLACED | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.SELECT) > 0) {
				buf.append("SELECT | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.STATE) > 0) {
				buf.append("STATE | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.INSTALL) > 0) {
				buf.append("INSTALL | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.UNINSTALL) > 0) {
				buf.append("UNINSTALL | "); //$NON-NLS-1$
			}
			if ((flags & IModelDelta.REVEAL) > 0) {
                buf.append("REVEAL | "); //$NON-NLS-1$
            }
            if ((flags & IModelDelta.FORCE) > 0) {
                buf.append("FORCE | "); //$NON-NLS-1$
            }

		}
		buf.append('\n');
        buf.append(indent);
		buf.append("    Index: "); //$NON-NLS-1$
		buf.append(delta.getIndex());
		buf.append(" Child Count: "); //$NON-NLS-1$
		buf.append(delta.getChildCount());
		buf.append('\n');
		IModelDelta[] nodes = delta.getChildDeltas();
		for (int i = 0; i < nodes.length; i++) {
			appendDetail(indent + "  ", buf, nodes[i]); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta#getChildCount()
	 */
	public int getChildCount() {
		return fChildCount;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.IModelDelta#accept(org.eclipse.debug.internal.ui.viewers.provisional.IModelDeltaVisitor)
	 */
	public void accept(IModelDeltaVisitor visitor) {
		doAccept(visitor, 0);
	}
	
	protected void doAccept(IModelDeltaVisitor visitor, int depth) {
		if (visitor.visit(this, depth)) {
			IModelDelta[] childDeltas = getChildDeltas();
			for (int i = 0; i < childDeltas.length; i++) {
				((ModelDelta)childDeltas[i]).doAccept(visitor, depth+1);
			}
		}
	}
	
	/**
	 * Sets this delta's element
	 * 
	 * @param element element to set
	 */
	public void setElement(Object element) {
		fElement = element;
	}
	
	/**
	 * Sets this delta's flags.
	 * 
	 * @param flagsnew flags to set
	 */
	public void setFlags(int flags) {
		fFlags = flags;
	}

    /**
     * Sets this delta's index
     * 
     * @param index new index to set
     * @since 3.6
     */
    public void setIndex(int index) {
        fIndex = index;
    }
	
	/**
     * Sets this delta's child count.
     * 
     * @param count new child count to set
     */
    public void setChildCount(int count) {
        fChildCount = count;
    }

}
