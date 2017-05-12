package com.ibm.br.ltc.rte.producers;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import facebook4j.Facebook;
import facebook4j.FacebookFactory;

public class Facebook4jProducer {

	@Produces
	public Facebook facebook4jProducer(InjectionPoint injectionPoint){
		Facebook facebook4j = new FacebookFactory().getInstance();
		facebook4j.setOAuthAppId("1657491477898275", "4b1b5ad8e7787eb51af0e0d92eec2ac9");
		facebook4j.setOAuthPermissions("email,manage_pages");
		
		return facebook4j;
	}
}
