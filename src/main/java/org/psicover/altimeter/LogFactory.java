package org.psicover.altimeter;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public abstract class LogFactory {
	static {
		
		String logClass = System.getProperty("java.util.logging.config.class");
		String logFile = System.getProperty("java.util.logging.config.file");
		if(null == logClass && logFile == null) {
			loadLogConfig();
		}
	}
	
	private static void loadLogConfig() {
		try (InputStream ins = LogFactory.class.getResourceAsStream("/logging.properties")){
			LogManager.getLogManager().readConfiguration(ins);
			getLogger(LogFactory.class).fine("Log config loaded");
		} catch (Exception e) {
			getLogger(null).log(Level.SEVERE, "Could not load config loaded", e);
		}
	}
	
	
	
	public static Logger getLogger(Class<?> c) {
		if(null == c) return Logger.getAnonymousLogger();
		return Logger.getLogger(c.getName());
	}

	private LogFactory() {}
}
