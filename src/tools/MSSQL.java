package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MSSQL {

	public static Connection conn;
	public static String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	public static String connectionURL="jdbc:sqlserver://%s:1433;DatabaseName=judgment";
	public static String user="likai";
	public static String pwd="d!{<kN5K38u-";
	public static boolean autoCommit=true;
	public static Statement statement;
	public static String host =JobPannel.outer?"210.16.191.146":"172.16.0.26";
	static
	{
		buildConnection();
	}
	
	public static void buildConnection()
	{
		try
		{
			Class.forName(driverName);
			conn = DriverManager.getConnection(String.format(connectionURL, host), user, pwd);
			conn.setAutoCommit(autoCommit);
			statement=conn.createStatement();
		} 
		catch (ClassNotFoundException | SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static int executeUpdate(String sql) throws SQLException, ClassNotFoundException
	{
//		System.out.println(sql);
		return executeUpdate(sql,0);
	}
	
	public static int executeUpdate(String sql,int t) throws SQLException, ClassNotFoundException
	{
		try
		{
			return statement.executeUpdate(sql);
		}
		catch (Exception e)
		{
			if(t==10) throw e;
//			e.printStackTrace();
			if(e.getMessage().contains("违反了 PRIMARY KEY 约束"))
			{
				throw e;
			}
			else
			{
				buildConnection();
				return executeUpdate(sql,t+1);
			}
		}
	}
	
	public static ResultSet executeQuery(String sql)
	{
		try
		{
//			System.out.println(sql);
			return statement.executeQuery(sql);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			buildConnection();
			return executeQuery(sql);
		}
	}
	
	public void close() throws SQLException
	{
		statement.close();
		conn.close();
	}
	
	public static void commit() throws SQLException
	{
		conn.commit();
	}
	
	public static void main(String[] args)
	{
		executeQuery("select top 100 * from documentpage");
	}
}
