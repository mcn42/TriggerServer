package org.mtahq.pfc.turnstile;

import java.io.IOException;

import java.io.PrintWriter;


import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class TriggerServer {

    private static final int SERVER_PORT = 8080;

    private PiPulser ppulse = new PiPulser();
    private Server server = null;
    
    private UUID currentUuid = UUID.randomUUID();

    public TriggerServer() {
        super();
        setup();
    }

    private void setup() {
        server = new Server(SERVER_PORT);

        ContextHandler context = new ContextHandler("/");
        context.setContextPath("/");
        context.setHandler(new RootHandler());

        ContextHandler contextFR = new ContextHandler("/accept");
        contextFR.setHandler(new AcceptHandler());
        
        ContextHandler context3 = new ContextHandler("/uuid");
        context3.setHandler(new UUIDHandler());

        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.setHandlers(new Handler[] { context, contextFR,context3 });

        server.setHandler(contexts);
    }

    public void start() {
        try {
            Utils.getLogger().info("Server is starting...");
            server.start();
            server.join();
            Utils.getLogger().info("Server has started");
        } catch (Exception e) {
            Utils.getLogger().log(Level.SEVERE, "A server startup error occurred", e);
            System.exit(1);
        }

    }

    public static void main(String[] args) {
        TriggerServer triggerServer = new TriggerServer();
        triggerServer.start();
    }

    class AcceptHandler extends AbstractHandler {

        @Override
        public void handle(String string, Request request, HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse) throws IOException, ServletException {
            //  Accept only POSTs
            //            if(!request.getMethod().equals("POST")) {
            //                httpServletResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            //                return;
            //            }
            
            //  Accept only requests containig the UUID as a paramter
            String uuid = httpServletRequest.getParameter("UUID");
            if(uuid == null || !uuid.equals(currentUuid.toString())) {
                httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            currentUuid = UUID.randomUUID();
            Utils.getLogger().info("Accept request received");
            ppulse.sendOneSequence();
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            request.setHandled(true);
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
}
