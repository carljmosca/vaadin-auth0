/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.vaadin.oauth;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import org.vaadin.viewportservlet.ViewPortCDIServlet;

/**
 *
 * @author moscac
 */


@WebServlet(urlPatterns = {"/MAIN/*", "/main/*", "/VAADIN/*", "/vaadin/*"}, initParams = { @WebInitParam(name = "ui", value = "com.github.vaadin.oauth.MyUI")})
public class Servlet extends ViewPortCDIServlet implements Filter {

    @Override
    public void init(FilterConfig fc) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc) throws IOException, ServletException {
    }


}
