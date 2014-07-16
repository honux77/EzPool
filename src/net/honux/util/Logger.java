package net.honux.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

public class Logger {	
	FileWriter fw;
	
	private static Logger mlog = new Logger();
	private boolean isInit = false;
	public static Logger Instance() {
		return mlog;
	}
	private Logger() {
		if(!isInit)
			init();			
	} 
	
	public void init() {
		String logfile =Environment.properties().getProperty("logfile"); 
		try {
			fw = new FileWriter(logfile, true);
		} catch (IOException e) {
			System.err.println("Logger init error");
			e.printStackTrace();
			System.exit(1);		
		}		
		isInit = true;
	}
	
	public void close()  {
		try {
			fw.close();
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	public void log(String msg) {		
		String str = String.format("[%s] %s\n", Calendar.getInstance().getTime().toString(),
				msg);
		try {
			fw.write(str);
			fw.flush();
		} catch (IOException e) {			
			e.printStackTrace();
		}		
	}
	
	public void error(String msg) {
		System.err.printf("[%s] %s", Calendar.getInstance().getTime().toString(),
				msg);
	}
	
	public static void main(String[] args) {
		Logger l = Logger.Instance();
		l.log("Hello");
		l.log("This is log");
		l.close();
	}
}
