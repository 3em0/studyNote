package org.richfaces.demo.queue;


import java.util.Random;

import javax.faces.event.ActionEvent;

/**
 * <p>This simple bean conains one <code>ActionEvent</code>
 * handler to simulate random processing times for 
 * requests on the server.
 * <p/>
 *
 */
public class MojarraBean {

    public void process(ActionEvent ae) {
        Random generator2 = new Random(System.currentTimeMillis());
        int delay = generator2.nextInt(6001) + 1;
        try {
            Thread.sleep(delay);
        } catch (Exception e) {
        }
    }
}
