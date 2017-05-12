package com.ibm.br.ltc.rte.servlet;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.ibm.br.ltc.rte.bean.FacebookSingleton;

@WebServlet(name = "facebookCallback", urlPatterns={"/facebookCallback"})
public class FacebookCallbackServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String AUTHENTICATION_REDIRECT_URL = "/feed";
	
	@Inject
	private FacebookSingleton facebookSingleton;
	
	@Inject
	private Logger logger;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.info("Received request from Facebook. request = " + request);
		String oAuthCode = request.getParameter("code");
		
		logger.debug("oauthcode returned by facebook: " + oAuthCode);
		
		facebookSingleton.setAccessToken(oAuthCode);

		if (facebookSingleton.isAuthenticated()) {
			logger.info("Authentication succeed.");
		} else {
			logger.info("Authentication failed.");
		}
		
		response.sendRedirect(request.getContextPath() + AUTHENTICATION_REDIRECT_URL);

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
	}
	
}
