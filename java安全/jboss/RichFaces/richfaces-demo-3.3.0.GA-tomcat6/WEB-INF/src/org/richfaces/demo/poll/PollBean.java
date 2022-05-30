/**
 * 
 */
package org.richfaces.demo.poll;

import java.util.Date;

/**
 * @author Ilya Shaikovsky
 *
 */
public class PollBean {
	
	private Date pollStartTime;
	private boolean pollEnabled;

	
	public PollBean() {
		pollEnabled=true;
	}
	public Date getDate() {
		Date date = new Date();
		if (null==pollStartTime){
			pollStartTime = new Date();
			return date;
		}
		if ((date.getTime()-pollStartTime.getTime())>=60000) setPollEnabled(false);
		return date;
	}

	public boolean getPollEnabled() {
		return pollEnabled;
	}

	public void setPollEnabled(boolean pollEnabled) {
		if (pollEnabled) setPollStartTime(null);
		this.pollEnabled = pollEnabled;
	}

	public Date getPollStartTime() {
		return pollStartTime;
	}

	public void setPollStartTime(Date pollStartTime) {
		this.pollStartTime = pollStartTime;
	}
	
}
