package com.anjan.log4j;

import java.io.File;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class LoggerFile {
	
	static Logger logger = Logger.getLogger(LoggerFile.class);
	
	public static void setUpLogger(String logPath){

		String log4jConfigFile = "log4j.properties";
                
		ClassLoader loader = LoggerFile.class.getClassLoader();
		URL url = loader.getResource(log4jConfigFile);
		
        PropertyConfigurator.configure(url);
        
        logger.info("Log4j appender configuration is Successful!!!");
        logger.info("Log File Path - "+logPath+File.separator+"sftplog.txt");
        
	}

}
