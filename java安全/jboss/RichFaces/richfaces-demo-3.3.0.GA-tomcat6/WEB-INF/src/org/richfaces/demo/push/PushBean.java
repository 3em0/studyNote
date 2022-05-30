/**
 * 
 */
package org.richfaces.demo.push;

import java.util.Date;
import java.util.EventListener;
import java.util.EventObject;
import java.util.UUID;

import org.ajax4jsf.event.PushEventListener;

/**
 * @author Ilya Shaikovsky
 * 
 */

public class PushBean implements Runnable {

	private String uuid = "";
	
	private boolean enabled = false;
	
	private Date startDate; 
	
	PushEventListener listener;

	private Thread thread;

	// private int eventsFired counter;

	public void addListener(EventListener listener) {
		synchronized (listener) {
			if (this.listener != listener) {
				this.listener = (PushEventListener) listener;
			}
		}
	}

	public void run() {
		while (thread != null) {
			try {
				if (((new Date()).getTime()-startDate.getTime())>=60000) {
					stop();
				}
				uuid = UUID.randomUUID().toString();
				listener.onEvent(new EventObject(this));
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}
	}

	public String getUuid() {
		return uuid;
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.setDaemon(true);
			thread.start();
			setStartDate(new Date());
			setEnabled(true);
		}
	}

	public void stop() {
		if (thread != null) {
			//thread.stop();
			setStartDate(null);
			setEnabled(false);
			thread = null;
		}
	}

	public Thread getThread() {
		return thread;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
}
