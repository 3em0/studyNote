/*
 * Facet.java		Date created: 10.12.2007
 * Last modified by: $Author$
 * $Revision$	$Date$
 */

package org.richfaces.datatablescroller;

/**
 * TODO Class description goes here.
 * @author "Andrey Markavtsov"
 *
 */
public class Facet {
	private String header;
	private String footer;
	
	/**
	 * TODO Description goes here.
	 * @param header
	 * @param footer
	 */
	public Facet(String header, String footer) {
	    super();
	    this.header = header;
	    this.footer = footer;
	}
	/**
	 * @return the header
	 */
	public String getHeader() {
	    return header;
	}
	/**
	 * @param header the header to set
	 */
	public void setHeader(String header) {
	    this.header = header;
	}
	/**
	 * @return the footer
	 */
	public String getFooter() {
	    return footer;
	}
	/**
	 * @param footer the footer to set
	 */
	public void setFooter(String footer) {
	    this.footer = footer;
	}
	
}
