package be.spacebel.ese.downloadmanager.plugin.http;


import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.conn.HttpClientConnectionManager;


/**
 *
 * @author ane
 */
public class IdleConnectionMonitor implements Runnable {

    private final HttpClientConnectionManager connMgr;
    private final long schedule;
    private volatile AtomicBoolean stop;


    /**
     *
     * @param connMgr the connection manager used by HttpClientController.
     * @param schedule in milliseconds.
     */
    public IdleConnectionMonitor(HttpClientConnectionManager connMgr, long schedule) {
        this.connMgr = connMgr;
        stop = new AtomicBoolean(false);
        this.schedule = schedule;
    }


    public void shutdown() {
        stop.set(true);
        synchronized (this) {
            notifyAll();
            //System.out.println("[IdleConnectionMonitor] [" + System.currentTimeMillis() + "] shutting down monitor...");
        }
    }


    @Override
    public void run() {
        try {
            while (stop.get() == false) {
                synchronized (this) {
                    wait(schedule);
                    //System.out.println("[IdleConnectionMonitor] [" + System.currentTimeMillis() + "] Closing expired & idle connections.");
                    this.connMgr.closeExpiredConnections();
                    this.connMgr.closeIdleConnections(10, TimeUnit.SECONDS);
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(IdleConnectionMonitor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
