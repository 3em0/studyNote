/**
 * License Agreement.
 *
 *  JBoss RichFaces - Ajax4jsf Component Library
 *
 * Copyright (C) 2007  Exadel, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 */

package org.richfaces.demo.tree;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.ajax4jsf.context.AjaxContext;
import org.richfaces.component.UIDragSupport;
import org.richfaces.component.UITree;
import org.richfaces.component.UITreeNode;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.DropEvent;
import org.richfaces.event.NodeSelectedEvent;
import org.richfaces.model.TreeNode;
import org.richfaces.model.TreeRowKey;

public class SimpleTreeDndBean {

    private TreeNode rootNode = null;

    private List<TreeNode<String>> selectedNodeChildren = new ArrayList<TreeNode<String>>();

    private String nodeTitle;

    private static final String ICONS_PATH = "/images/tree/dnd/";
    
    private static final String DATA_PATH = "/richfaces/tree/examples/simple-tree-data-dnd.properties";

    private void addNodes(String path, TreeNode node, Properties properties) {
	boolean end = false;
	int counter = 1;

	while (!end) {
	    String key = path != null ? path + '.' + counter : String.valueOf(counter);

	    String value = properties.getProperty(key);
	    if (value != null) {
		DemoTreeNodeImpl nodeImpl = new DemoTreeNodeImpl();
		String[] splittedValue = value.split(" - ");
		if (splittedValue.length > 1) {
		    nodeImpl.setData(splittedValue[0]);
		    nodeImpl.setType(splittedValue[1]);
		    if (splittedValue.length>2){
		    	nodeImpl.setIcon(ICONS_PATH+splittedValue[2]);
		    	nodeImpl.setLeafIcon(ICONS_PATH+splittedValue[2]);
		    }
		    if (splittedValue.length>3){
		    	nodeImpl.setLeafIcon(ICONS_PATH+splittedValue[3]);
		    }
		    node.addChild(new Integer(counter), nodeImpl);
		}
		addNodes(key, nodeImpl, properties);

		counter++;
	    } else {
		end = true;
	    }
	}
    }

    private void loadTree() {
	FacesContext facesContext = FacesContext.getCurrentInstance();
	ExternalContext externalContext = facesContext.getExternalContext();
	InputStream dataStream = externalContext.getResourceAsStream(DATA_PATH);
	try {
	    Properties properties = new Properties();
	    properties.load(dataStream);

	    rootNode = new DemoTreeNodeImpl();
	    addNodes(null, rootNode, properties);

	} catch (IOException e) {
	    throw new FacesException(e.getMessage(), e);
	} finally {
	    if (dataStream != null) {
		try {
		    dataStream.close();
		} catch (IOException e) {
		    externalContext.log(e.getMessage(), e);
		}
	    }
	}
    }

    public void processSelection(NodeSelectedEvent event) {
	HtmlTree tree = (HtmlTree) event.getComponent();
	nodeTitle = (String) tree.getRowData();
	selectedNodeChildren.clear();
	TreeNode<String> currentNode = tree.getModelTreeNode(tree.getRowKey());
	DemoTreeNodeImpl<String> demoCurrentNodeImpl = currentNode instanceof DemoTreeNodeImpl ? (DemoTreeNodeImpl) currentNode : null;
	if (currentNode.isLeaf() && (demoCurrentNodeImpl != null && demoCurrentNodeImpl.getType().toLowerCase().equals("leaf") || demoCurrentNodeImpl == null )) {	    
	    selectedNodeChildren.add(currentNode);
	} else {
	    Iterator<Map.Entry<Object, TreeNode<String>>> it = currentNode.getChildren();
	    while (it != null && it.hasNext()) {
		Map.Entry<Object, TreeNode<String>> entry = it.next();
		selectedNodeChildren.add(entry.getValue());
	    }
	}
    }

    private Object getNewId(TreeNode parentNode) {
	Map<Object, TreeNode> childs = new HashMap<Object, TreeNode>();
	Iterator<Map.Entry<Object, TreeNode>> iter = parentNode.getChildren();
	while (iter != null && iter.hasNext()) {
	    Map.Entry<Object, TreeNode> entry = iter.next();
	    childs.put(entry.getKey(), entry.getValue());
	}

	Integer index = 1;
	while (childs.containsKey(index)) {
	    index++;
	}
	return index;
    }

    public void dropListener(DropEvent dropEvent) {

	// resolve drag destination attributes
	UITreeNode destNode = (dropEvent.getSource() instanceof UITreeNode) ? (UITreeNode) dropEvent.getSource() : null;
	UITree destTree = destNode != null ? destNode.getUITree() : null;
	TreeRowKey dropNodeKey = (dropEvent.getDropValue() instanceof TreeRowKey) ? (TreeRowKey) dropEvent.getDropValue() : null;
	TreeNode droppedInNode = dropNodeKey != null ? destTree.getTreeNode(dropNodeKey) : null;

	// resolve drag source attributes
	UITreeNode srcNode = (dropEvent.getDraggableSource() instanceof UITreeNode) ? (UITreeNode) dropEvent.getDraggableSource() : null;
	UITree srcTree = srcNode != null ? srcNode.getUITree() : null;
	TreeRowKey dragNodeKey = (dropEvent.getDragValue() instanceof TreeRowKey) ? (TreeRowKey) dropEvent.getDragValue() : null;
	TreeNode draggedNode = dragNodeKey != null ? srcTree.getTreeNode(dragNodeKey) : null;
	if (dropEvent.getDraggableSource() instanceof UIDragSupport && srcTree == null && draggedNode == null && dropEvent.getDragValue() instanceof TreeNode) {	    
	    srcTree = destTree;
	    draggedNode = (TreeNode) dropEvent.getDragValue();
	    dragNodeKey = srcTree.getTreeNodeRowKey(draggedNode) instanceof TreeRowKey ? (TreeRowKey) srcTree.getTreeNodeRowKey(draggedNode) : null;
	}

	// Note: check if we dropped node on to itself or to item instead of
	// folder here
	if (droppedInNode != null && (droppedInNode.equals(draggedNode) || droppedInNode.getParent().getParent() != null || draggedNode.getParent().getParent() == null)) {
	    return;
	}

	FacesContext context = FacesContext.getCurrentInstance();

	if (dropNodeKey != null) {
	    // add destination node for rerender
	    destTree.addRequestKey(dropNodeKey);

	    Object state = null;
	    if (dragNodeKey != null) { // Drag from this or other tree
		TreeNode parentNode = draggedNode.getParent();
		// 1. remove node from tree
		state = srcTree.removeNode(dragNodeKey);
		// 2. add parent for rerender
		Object rowKey = srcTree.getTreeNodeRowKey(parentNode);
		srcTree.addRequestKey(rowKey);
		if (dropEvent.getDraggableSource() instanceof UIDragSupport) {
		    selectedNodeChildren.remove(draggedNode);
		    // if node was gragged in it's parent place dragged node to
		    // the end of selected nodes in grid
		    if (droppedInNode.equals(parentNode)) {
			selectedNodeChildren.add(draggedNode);
		    }
		}
	    } else if (dropEvent.getDragValue() != null) { // Drag from some
							    // drag source
		draggedNode = new DemoTreeNodeImpl<String>();
		draggedNode.setData(dropEvent.getDragValue().toString());
	    }

	    // generate new node id
	    Object id = getNewId(destTree.getTreeNode(dropNodeKey));
	    destTree.addNode(dropNodeKey, draggedNode, id, state);
	}

	AjaxContext ac = AjaxContext.getCurrentInstance();
	// Add destination tree to reRender
	try {
	    ac.addComponentToAjaxRender(destTree);
	} catch (Exception e) {
	    System.err.print(e.getMessage());
	}

    }

    public List<TreeNode<String>> getSelectedNodeChildren() {
	return selectedNodeChildren;
    }

    public void setSelectedNodeChildren(List<TreeNode<String>> selectedNodeChildren) {
	this.selectedNodeChildren = selectedNodeChildren;
    }

    public TreeNode getTreeNode() {
	if (rootNode == null) {
	    loadTree();
	}
	return rootNode;
    }

    public String getNodeTitle() {
	return nodeTitle;
    }

    public void setNodeTitle(String nodeTitle) {
	this.nodeTitle = nodeTitle;
    }

}
