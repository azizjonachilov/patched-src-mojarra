/*
 * $Id: MockServlet.java,v 1.2 2004/02/04 23:39:14 ofung Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package javax.faces.mock;


import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;


/**
 * <p>Mock <strong>Servlet</strong> for unit tests.</p>
 */

public class MockServlet implements Servlet {


    public MockServlet() {
    }


    public MockServlet(ServletConfig config) throws ServletException {
        init(config);
    }


    private ServletConfig config;


    public void destroy() {
    }


    public ServletConfig getServletConfig() {
        return (this.config);
    }


    public String getServletInfo() {
        return ("MockServlet");
    }


    public void init(ServletConfig config) throws ServletException {
        this.config = config;
    }



    public void service(ServletRequest request, ServletResponse response)
        throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }


}
