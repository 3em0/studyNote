package org.richfaces.treemodeladaptor;


public class FileSystemBean {
	private static String SRC_PATH = "/WEB-INF/src";
	
	private FileSystemNode[] srcRoots;
	
	public synchronized FileSystemNode[] getSourceRoots() {
		if (srcRoots == null) {
			srcRoots = new FileSystemNode(SRC_PATH).getNodes();
		}
		
		return srcRoots;
	}
}
