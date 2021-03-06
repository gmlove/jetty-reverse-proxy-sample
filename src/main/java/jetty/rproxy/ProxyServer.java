//
//  ========================================================================
//  Copyright (c) 1995-2015 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package jetty.rproxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.ConnectHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.ProxyServlet;

@SpringBootApplication
public class ProxyServer implements CommandLineRunner
{
  public static void reverseProxy(int port, String url) throws Exception{
    Server server = new Server();

    SocketConnector connector = new SocketConnector();
    connector.setHost("0.0.0.0");
    connector.setPort(port);
    connector.setRequestHeaderSize(20 * 1024 * 1024);

    server.setConnectors(new Connector[]{connector});

    // Setup proxy handler to handle CONNECT methods
    ConnectHandler proxy = new ConnectHandler();
    server.setHandler(proxy);

    // Setup proxy servlet
    ServletContextHandler context = new ServletContextHandler(proxy, "/", ServletContextHandler.SESSIONS);
    ServletHolder proxyServlet = new ServletHolder(ProxyServlet.Transparent.class);
    proxyServlet.setInitParameter("ProxyTo", url);
    proxyServlet.setInitParameter("Prefix", "/");
    context.addServlet(proxyServlet, "/*");

    server.start();
  }

  @Override
  public void run(String... args)  throws Exception {
    reverseProxy(Integer.parseInt(args[0]), args[1]);
  }

  public static void main(String[] args) throws Exception {
    SpringApplication application = new SpringApplication(ProxyServer.class);
    application.setApplicationContextClass(AnnotationConfigApplicationContext.class);
    SpringApplication.run(ProxyServer.class, args);
  }

}
