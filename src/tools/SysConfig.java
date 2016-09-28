package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
public class SysConfig {

	public static String workDir=System.getProperty("user.dir");
	
	public static String LOG_FILE_SAVE_PATH=workDir+"/logs/"; //日志文件保存路径
	
	public static String FIREFOX_PROFILE=workDir+"/conf/FirefoxProfile";

	public static String MSSQL_HOST="172.16.0.26"; //主机
	
	public static int mysqlPort=3306;
	public static String mysqlHost="172.16.0.20";
	public static String mysqlUser="root";
	public static String mysqlPwd="LENGjing1@34";
	public static String mysqlDB="job_info";
	
	public static int MAX_TRY_TIMES=5;//Integer.MAX_VALUE;
	public static int WAIT_IN_SECONDS=10;
//	public static int SLEEP_IN_MILLIS=500; //default value is 500
	
	public static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static void setMSSQL_HOST(String ip)
	{
		MSSQL_HOST=ip;
	}
	
	public static String getError(Exception e)
	{
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw, true));
		return sw.toString();
	}
	
	public static String getCurTime()
	{
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
	}
	
	public static String getLocalHost() throws IOException
	{
		String localhost=ProcessInfo.getHostName();
		File file=new File("D:\\PublishAgent\\LocalIp.txt");
		if(file.exists())
		{
			InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"gbk");
		    BufferedReader bufferedReader = new BufferedReader(reader);
		    localhost=bufferedReader.readLine().trim();
		    bufferedReader.close();
		    reader.close();
		}
	    return localhost;
	}
	
	public static String getMysqlConnection()
	{
		return String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s&useUnicode=true&characterEncoding=utf-8&useSSL=false"
				,mysqlHost,mysqlPort,mysqlDB,mysqlUser,mysqlPwd);
	}
	
	
	public static void main(String[] args) throws IOException
	{
		System.out.println(getLocalHost());
	}

}
