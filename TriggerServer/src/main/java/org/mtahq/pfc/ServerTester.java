package org.mtahq.pfc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;


public class ServerTester {
    private long pollingPeriodMillis = 1000L;
    private int maxPollCount = 100;
    private int minimumTriggerDelaySecs = 5;
    private int maximumTriggerVariationSecs = 7;
    private String host = "localhost";
    private String port = "8080";
    
    private Logger log = Logger.getLogger("org.mtahq.pfc");
    private HttpURLConnection conn = null;
    private String uuid = null;
    private Timer t = new Timer();
    private int pollCount = 0;
    private Random r = new Random();
    
    PollTask pt =  null;

    public ServerTester() {
        super();
    }

    public static void main(String[] args) {
        ServerTester serverTester = new ServerTester();
        serverTester.runOnce();
    }

    public void runOnce() {
        log.info("Starting test...");
        try {
            this.getUUID();
            if (this.uuid == null)
                return;
            this.poll();
        } catch (IOException e) {
            log.log(Level.SEVERE, String.format("IOException"), e);
        }
    }

    private void getUUID() throws MalformedURLException, IOException {
        URL url = buildUrl("uuid/");
        this.uuid = null;
        this.conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        log.info(String.format("Opened connection to %s, response was '%s'", url.toString(), conn.getResponseCode()));
        
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer response = new StringBuffer();
        String inputLine = in.readLine();
        while (inputLine != null) {
            response.append(inputLine);
            inputLine = in.readLine();
        }
        in.close();
        String resp = response.toString();
        if (resp == null || resp.length() == 0) {
            log.severe("The UUID was NULL or empty");
        } else {
            this.uuid = resp;
            log.info(String.format("UUID retrieved: '%s'", this.uuid));
        }
        this.conn.disconnect();
    }

    private void poll() {
        this.pollCount = 0;
        pt = new PollTask();
        this.t.schedule(pt, 0L, this.pollingPeriodMillis);
        int secs = this.minimumTriggerDelaySecs + this.r.nextInt(this.maximumTriggerVariationSecs);
        this.t.schedule(new TriggerTask(), secs * 1000L);
    }

    private void pollOnce() throws MalformedURLException, IOException {
        this.pollCount++;
        if(this.pollCount > this.maxPollCount) {
            log.info("Polling timed out.");
            this.finish();
            return;
        }
        URL url = buildUrl("poll?uuid=" + this.uuid);
        this.conn = (HttpURLConnection) url.openConnection();
        log.info(String.format("Polling response from %s was '%s'", url.toString(), conn.getResponseCode()));
        if(conn.getResponseCode() == HttpServletResponse.SC_OK) {
            log.info(String.format("Transaction %s completed successfully", this.uuid));
            this.finish();
        } 
        this.conn.disconnect();
    }

    private void sendAccept() throws MalformedURLException, IOException {
        URL url = buildUrl("accept/");
        this.uuid = null;
        this.conn = (HttpURLConnection) url.openConnection();
        log.info(String.format("Sentr trigger request to %s, response was '%s'", url.toString(), conn.getResponseCode()));
        this.conn.disconnect();
    }

    private void finish() {
        if(pt != null) pt.cancel();
        log.info("Test completed");
    }

    private URL buildUrl(String uri) throws MalformedURLException {
        return new URL(String.format("http://%s:%s/%s", this.host, this.port, uri));
    }

    private class PollTask extends TimerTask {

        @Override
        public void run() {
            try {
                pollOnce();
            } catch (IOException e) {
                log.log(Level.SEVERE, String.format("IOException"), e);
            }
        }
    }
    
    private class TriggerTask extends TimerTask {

        @Override
        public void run() {
            try {
                sendAccept();
            } catch (IOException e) {
                log.log(Level.SEVERE, String.format("IOException"), e);
            }
        }
    }
}
