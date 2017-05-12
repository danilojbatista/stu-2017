package com.ibm.br.ltc.rte.utils;

import javax.websocket.Session;

public class WebSocketUtil {

	/**
	 * Send the message to all the WebSocket clients.
	 * 
	 * @param msg
	 *            Message to be delivered
	 */
	public static void sendMessages(String msg, Session session) {
		if (session != null) {
			// Iterate all the websockets to send the message
			for (Session sess : session.getOpenSessions()) {

				try {
					if (sess.isOpen()) {
						sess.getBasicRemote().sendText(msg);
					}
				} catch (Exception e) {
					e.printStackTrace();
					sess = null;
				}

			}

		}
	}
}
