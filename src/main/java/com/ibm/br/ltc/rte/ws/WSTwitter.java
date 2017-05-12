package com.ibm.br.ltc.rte.ws;

import java.io.IOException;

import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.ibm.br.ltc.rte.bean.TwitterSingleton;

/**
 * 
 * @author <a href="mailto:diogo.fabrile@sprint.com">Diogo Favero Fabrile</a>
 *
 *         WebSocket endpoint that send all messages.
 */
@ServerEndpoint("/websocket")
public class WSTwitter {

	@Inject
	private TwitterSingleton tsingleton;

	@Inject
	private Logger logger;

	private Gson gson = new Gson();

	@OnMessage
	public void onMessage(String message, Session session) throws IOException,
			InterruptedException {

		if (message.equals("Pong")) {
			logger.debug("Received Pong from client " + session.getId());
		} else if (message.equals("Received")) {
			logger.debug("Tweet received from client " + session.getId());
		}

	}

	@OnOpen
	public void onOpen(Session session) {
		tsingleton.setSession(session);
		try {
			session.getBasicRemote().sendText(
					gson.toJson(tsingleton.getCacheTweets()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnClose
	public void onClose(Session session) {
		System.out.println("Connection closed");
	}

}
