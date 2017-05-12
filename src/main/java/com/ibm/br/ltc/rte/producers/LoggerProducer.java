package com.ibm.br.ltc.rte.producers;

import org.apache.log4j.Logger;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * 
 * @author <a href="mailto:diogo.fabrile@sprint.com">Diogo Favero Fabrile</a>
 *
 *         Producer for the log4j object
 */
public class LoggerProducer {

	@Produces
	public Logger produceLogger(InjectionPoint injectionPoint) {
		return Logger.getLogger(injectionPoint.getMember().getDeclaringClass()
				.getName());
	}

}
