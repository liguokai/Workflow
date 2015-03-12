package com.biorad.wcms.utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Rick Poulin (Deloitte)
 * 
 *         Simple abstraction class to send both HTTP GET and HTTP POST requests
 *         to the same handler method.
 */
public abstract class ServletAccelerator extends HttpServlet {

    private static final long serialVersionUID = 6821107258100224090L;

    /**
     * Handle HTTP POST requests to this servlet
     * 
     * @param request
     *            the servlet request
     * @param response
     *            the servlet response
     * @throws ServletException
     *             if there's a problem handling the request
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    	dispatchRequest(request, response);
    }

    /**
     * Handle HTTP GET requests to this servlet
     * 
     * @param request
     *            the servlet request
     * @param response
     *            the servlet response
     * @throws ServletException
     *             if there's a problem handling the request
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    	dispatchRequest(request, response);
    }

    /**
     * Main method to dispatch all requests to this servlet
     * 
     * @param request
     *            the servlet request
     * @param response
     *            the servlet response
     * @throws ServletException
     *             if there's a problem handling the request
     */
    protected abstract void dispatchRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException;

}
