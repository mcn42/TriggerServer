package org.mtahq.pfc.turnstile;

import java.awt.Toolkit;

import java.io.File;
import java.io.IOException;

import java.io.PrintWriter;


import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PreDestroy;

import javax.print.attribute.standard.Media;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class TriggerServer {

    private static final int SERVER_PORT = 8080;
    private static final long LOCKOUT_PERIOD = 10000L;

    private PiPulser ppulse = new PiPulser();
    private Server server = null;

    private UUID currentUuid = UUID.randomUUID();
    private Set<UUID> uuidSet = new TreeSet<UUID>();
    private AtomicBoolean locked = new AtomicBoolean(false);
    private Timer lockTimer = new Timer();

    private String supervisorUrl = null;

    public TriggerServer() {
        super();
        setup();
    }

    private void setup() {
        server = new Server(SERVER_PORT);
        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[] { "home.html" });
        resource_handler.setResourceBase("src/main/webapp");

        ContextHandler context = new ContextHandler("/");
        context.setHandler(resource_handler);

        ContextHandler contextFR = new ContextHandler("/accept");
        contextFR.setHandler(new AcceptHandler());

        ContextHandler context3 = new ContextHandler("/uuid");
        context3.setHandler(new UUIDHandler());

        ContextHandler context4 = new ContextHandler("/poll");
        context4.setHandler(new PollingHandler());

        ContextHandler context5 = new ContextHandler("/subtrigger");
        context5.setHandler(new SubTriggerHandler());

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { context, contextFR, context3, context4, context5, new DefaultHandler() });

        server.setHandler(contexts);
    }

    public void start() {
        try {
            File f = new File(".");
            Utils.getLogger().info(String.format("Base directory '%s'", f.getAbsolutePath()));
            Utils.getLogger().info("Server is starting...");
            server.start();
            server.join();

            Utils.getLogger().info(String.format("Base directory '%s'", f.getAbsolutePath()));

            Utils.getLogger().info("Server has started");
        } catch (Exception e) {
            Utils.getLogger().log(Level.SEVERE, "A server startup error occurred", e);
            System.exit(1);
        }

    }

    public void setSupervisorUrl(String supervisorUrl) {
        this.supervisorUrl = supervisorUrl;
    }

    public String getSupervisorUrl() {
        return supervisorUrl;
    }

    public boolean isSupervisor() {
        return this.supervisorUrl == null;
    }

    public static void main(String[] args) {
        TriggerServer triggerServer = new TriggerServer();
        if (args.length > 0) {
            triggerServer.setSupervisorUrl(args[0]);
        }
        triggerServer.start();
    }

    private void changeUUID() {
        synchronized (this) {
            if (!this.uuidSet.remove(this.currentUuid)) {
                Utils.getLogger().warning("UUID not found for removal: " + this.currentUuid);
            }

            this.currentUuid = UUID.randomUUID();
            Utils.getLogger().info(String.format("Current UUID changed to %s", this.currentUuid));
            this.uuidSet.add(this.currentUuid);
        }
    }

    class AcceptHandler extends AbstractHandler {

        @Override
        public void handle(String string, Request request, HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse) throws IOException, ServletException {
            if (locked.get()) {
                Utils.getLogger().info("Locked, Accept request ignored");
                httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
                request.setHandled(true);
                return;
            }
            //      Accept only POSTs
            //            if (!request.getMethod().equalsIgnoreCase("POST")) {
            //                httpServletResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            //                return;
            //            }

            //  Accept only requests containig the UUID as a paramter
            //            String uuid = httpServletRequest.getParameter("UUID");
            //            if (uuid == null || !uuid.equals(currentUuid.toString())) {
            //                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            //                return;
            //            }
            lockTimer.schedule(new LockoutTask(), 0L);

            Utils.getLogger().info("Accept request received");
            ppulse.sendOneSequence();
            //Toolkit.getDefaultToolkit().beep();
            //playSound();

            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            request.setHandled(true);
        }
    }
    
    private void playSound() {
        try {
            Runtime.getRuntime().exec("afplay //Users//mnilsen//Downloads//Doorbell-SoundBible.com-516741062.wav");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class SubTriggerHandler extends AbstractHandler {

        @Override
        public void handle(String string, Request request, HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse) throws IOException, ServletException {
            changeUUID();
            Utils.getLogger().info("Subordinate Trigger notification received");
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            request.setHandled(true);
        }
    }

    class PollingHandler extends AbstractHandler {

        @Override
        public void handle(String string, Request request, HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse) throws IOException, ServletException {
            
            String[] uuids = httpServletRequest.getParameterValues("uuid");
            if (uuids == null || uuids.length == 0) {
                //  No UUID parameter, return error
                httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            //  If locked, wait
            if (locked.get()) {
                httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else
            //  If UUID is in uuidSet, the token is LIVE, so  we return a "No Content" code
            if (uuidSet.contains(uuids[0])) {
                httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } else
            //  UUID is not found, token expended, return OK
            {
                httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            }

            
        }
    }

    class RootHandler extends AbstractHandler {

        @Override
        public void handle(String string, Request request, HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse) throws IOException, ServletException {

            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType("text/html; charset=utf-8");

            PrintWriter out = httpServletResponse.getWriter();

            out.println("<h1>The Turnstile Trigger Server is Running...</h1>");

            request.setHandled(true);
        }
    }

    class UUIDHandler extends AbstractHandler {

        @Override
        public void handle(String string, Request request, HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse) throws IOException, ServletException {

            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType("text/plain; charset=utf-8");

            PrintWriter out = httpServletResponse.getWriter();

            out.println(currentUuid.toString());

            request.setHandled(true);
        }
    }

    class LockoutTask extends TimerTask {

        @Override
        public void run() {
            locked.set(true);
            try {
                Thread.sleep(LOCKOUT_PERIOD);
            } catch (InterruptedException e) {
            }
            locked.lazySet(false);
        }
    }
    
    @PreDestroy
    public void preDestroy() {
        if(this.ppulse != null) this.ppulse.preDestroy();
    }
}
