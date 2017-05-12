package com.ibm.br.ltc.rte.webservice;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

public abstract class AbstractWebServiceCaller {
	
	@Inject
	private Logger logger;
	
	protected Response doGetAPICall(WebTarget webTarget){
		return this.doGetAPICall(webTarget, MediaType.APPLICATION_JSON);
	}
	
	protected Response doGetAPICall(WebTarget webTarget, String mediaType){
		if(webTarget == null ){
			IllegalStateException ex = new IllegalStateException("webTarget CANNOT be null");
			logger.error("AbstractWebServiceCaller.doGetAPICall: webTarget cannot be NULL", ex);
			throw ex;
		}
		
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
		return invocationBuilder.get();
	}
	
	protected Client getClient(){
		return ClientBuilder.newClient();
	}

}
