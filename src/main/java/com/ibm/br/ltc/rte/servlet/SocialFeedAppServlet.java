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

import facebook4j.Facebook;

@WebServlet(name = "initApp", urlPatterns = { "/feed" })
public class SocialFeedAppServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String FEED_PAGE = "/start/bts.jsp";

	private static final String ERROR_PAGE = "/start/authError.jsp";

	private static final String CALLBACK_SERVLET_URL = "/facebookCallback";

	@Inject
	private FacebookSingleton facebookSingleton;

	@Inject
	private Logger logger;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if (this.facebookSingleton.hasNotBeingInitialized() || this.facebookSingleton.isAuthTokenExpired()) {
			logger.info("Facebook does not have a valid access token.\nInitializing Authentication process");
			this.signin(req, resp);
		} else {
			String pageStr;

			if (this.facebookSingleton.isAuthenticated()) {
				pageStr = FEED_PAGE;
			} else {
				pageStr = ERROR_PAGE;
			}

			resp.sendRedirect(req.getContextPath() + pageStr);
		}

	}

	private void signin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String callbackURL = this.getCallbackURL(request);
		Facebook facebook4j = facebookSingleton.getFacebook4j();

		this.logger
				.info("Request received to Facebook sig in. Redirecting to facebook with the following callback url: "
						+ callbackURL);
		this.facebookSingleton.setAuthStatusToRequestSent();
		this.redirectToFacebookAuthentication(response, callbackURL, facebook4j);
	}

	private void redirectToFacebookAuthentication(HttpServletResponse response, String callbackURL, Facebook facebook4j)
			throws IOException {
		response.sendRedirect(facebook4j.getOAuthAuthorizationURL(callbackURL));
	}

	private String getCallbackURL(HttpServletRequest request) {
		StringBuffer requestURL = request.getRequestURL();
		int index = requestURL.lastIndexOf("/");
		return requestURL.replace(index, requestURL.length(), "").append(CALLBACK_SERVLET_URL).toString();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doGet(req, resp);
	}

}
