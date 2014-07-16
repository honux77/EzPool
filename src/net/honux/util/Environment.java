package net.honux.util;

import java.io.*;
import java.util.Properties;

public class Environment {
	private final String XMLFILE = "env.xml";	
	private final String COMMENT = "MySQL Connection Pool Setting File";

	//singleton
	private static Environment mEnvironment = new Environment();
	private Environment() {
		mProp = new Properties();
		init();
	}
	public static Environment Instance() { return mEnvironment;	}	

	private static Properties mProp;	
	private boolean isLoaded = false;
	public static Properties properties() {return mProp;}
	
	public void init() {
		if(!isLoaded)
			load();
	}
	
	private void load() {		
		FileInputStream fin;
		try {
			fin = new FileInputStream(XMLFILE);
			mProp.loadFromXML(fin);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		//mProp.list(System.out);
		isLoaded = true;

	}	
	
	public void generateXML() throws IOException {		
		mProp.setProperty("url", "db url here");		
		mProp.setProperty("user", "user id here");
		mProp.setProperty("password", "password here");
		File f = new File(XMLFILE);
		f.createNewFile();
		System.out.println("PATH of env file: " + f.getAbsolutePath());
		FileOutputStream fos = new FileOutputStream(f);
		mProp.storeToXML(fos, COMMENT);
		fos.close();		
	}	
	
	//test
	public static void main(String[] args) throws IOException {
		Environment e = Environment.Instance();		
		//e.generateXML();
		e.init();
	}
}
