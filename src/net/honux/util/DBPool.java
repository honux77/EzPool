package net.honux.util;

import java.util.*;
import java.sql.*;

public class DBPool {
	private final int INITCON = 16;
	
	private static DBPool mPool = new DBPool();	
	public static DBPool Instance() {return mPool;}
	
	public int maxConnection;
	
	private int numUsedConn = 0;
	
	private LinkedList <Connection> freeList = new LinkedList <Connection> ();
	private String url, user, password;	
	private Logger logger = Logger.Instance();
	
	public DBPool() {		
		Properties p = Environment.properties();		
		url = p.getProperty("url");
		user = p.getProperty("user");
		password = p.getProperty("password");		
		maxConnection = Integer.parseInt(p.getProperty("maxConnection"));		
		
		for(int i = 0; i < INITCON; i++) {
			boolean ret = addConnection();
			if(!ret) {
				String msg = "DBPool Init Fail!!";
				logger.log(msg);
				System.err.println(msg);
				System.exit(1);				
			}	
		}
	}	
	
	public synchronized Connection getConnection() {
		Connection conn = null;
		if (freeList.size() > 0 ) {
			conn = freeList.removeFirst();
			try {
				if(conn.isClosed()) {
					numUsedConn--;
					conn = getConnection();					
				}
				else {
					numUsedConn++;
					return conn;
				}
			} catch (SQLException e) {
				logger.log(e.getMessage());
				conn = getConnection();
			}
		}
		//no more connections in list
		if (freeList.isEmpty() && numUsedConn < maxConnection) {			
			String msg = String.format(
					"no more connections(current: %d max: %d). add 1.",
					numUsedConn, maxConnection);
			logger.log(msg);			
			addConnection();
			conn = getConnection();			
		}
		if (conn != null)
			return conn;
		else {
		//can't get connections
		String msg = String.format(
				"can't get connections (free: %d current: %d max: %d)",
				freeList.size(), numUsedConn, maxConnection);
		logger.log(msg);
		return null;
		}
	}				
	
	public synchronized void freeConnection(Connection conn) {
		numUsedConn--;
		try {
			if(!conn.isClosed())
				freeList.add(conn);
			else 
				addConnection();
			
		} catch (SQLException e) {
			//do nothing
		}		
		//for debug
		System.out.println(numUsedConn + ", " + freeList.size());
	}
	
	public synchronized void closeAll() {
		while(!freeList.isEmpty()) {			
			try {
				freeList.remove().close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		logger.close();
	}
		
	public boolean addConnection() {
		Connection c = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			logger.log("CREATE NEW CONNECTION FAIL!!");
			logger.log(e.getMessage());
			return false;
		}
		try {			
			c = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			logger.log("CREATE NEW CONNECTION FAIL!!");
			logger.log(e.getMessage());
			return false;
		}
		logger.log("generate connection from pool. freeList = " + freeList.size());
		freeList.add(c);
		return true;
	}
	
	public static void main(String[] args) {
		DBPool dbpool = DBPool.Instance();
		Connection conn = dbpool.getConnection();		
		dbpool.freeConnection(conn);		
		Connection[] carr = new Connection[100];
		for (int i =0; i < carr.length; i++)
			carr[i] = dbpool.getConnection();		
		for (int i =0; i < carr.length; i++)
			dbpool.freeConnection(carr[i]);		
		dbpool.closeAll();		
	}
}