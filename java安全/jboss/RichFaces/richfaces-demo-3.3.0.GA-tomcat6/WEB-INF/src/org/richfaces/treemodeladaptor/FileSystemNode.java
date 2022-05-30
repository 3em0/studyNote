package org.richfaces.treemodeladaptor;

import java.util.Set;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

public class FileSystemNode {
	private String path;

	private static FileSystemNode[] CHILDREN_ABSENT = new FileSystemNode[0];
	
	private FileSystemNode[] children;

	private String shortPath;
	
	public FileSystemNode(String path) {
		this.path = path;
		int idx = path.lastIndexOf('/');
		if (idx != -1) {
			shortPath = path.substring(idx + 1);
		} else {
			shortPath = path;
		}
	}

	public synchronized FileSystemNode[] getNodes() {
		if (children == null) {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ExternalContext externalContext = facesContext.getExternalContext();
			Set resourcePaths = externalContext.getResourcePaths(this.path);
			if (resourcePaths != null) {
				Object[] nodes = (Object[]) resourcePaths.toArray();
				children = new FileSystemNode[nodes.length];
				
				for (int i = 0; i < nodes.length; i++) {
					String nodePath = nodes[i].toString();
					if (nodePath.endsWith("/")) {
						nodePath = nodePath.substring(0, nodePath.length() - 1);
					}
					children[i] = new FileSystemNode(nodePath);
				}
			} else {
				children = CHILDREN_ABSENT;
			}
		}

		return children;
	}
	
	public String toString() {
		return shortPath;
	}

}