package crawler;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.google.common.base.Joiner;

public class MaYiDaiLi {

	// 定义申请获得的appKey和appSecret
	static String appkey = "83247795";
	static String secret = "e7674ea5c301f682f4715cc11149da6c";
	static String proxyHost="123.56.238.200";
	static int proxyPort=8123;
	static Map<String, String> paramMap = new TreeMap<String, String>();
	static DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static int timeout=10000;
	static int sleepSeconds=0;
	static boolean useProxy=true;
	static
	{
		paramMap.put("app_key", appkey);
		paramMap.put("enable-simulate", "true");
		paramMap.put("random-useragent", "pc");
		format.setTimeZone(TimeZone.getTimeZone("GMT+8"));//使用中国时间，以免时区不同导致认证错误
	}
	
	public static void setSleepSeconds(int t)
	{
		sleepSeconds=t;
	}
	
	public static void turnOffProxy()
	{
		System.out.println("设置关闭代理");
		useProxy=false;
	}
	
	public static void turnOnProxy()
	{
		System.out.println("设置开启代理");
		useProxy=true;
	}
	
	public static void sleep() throws InterruptedException
	{
//		System.out.println("sleepSeconds:"+sleepSeconds);
		if(sleepSeconds!=0)
		{
//			System.out.println("休眠"+sleepSeconds+"秒");
			TimeUnit.SECONDS.sleep(sleepSeconds);
		}
	}
	
	public static Document post(String URL,String args) throws IOException, InterruptedException
	{
		return post(URL,args,0);
	}
	public static Document post(String URL,String args,int t) throws IOException, InterruptedException
	{
		
		sleep();
		try
		{
			Connection conn = Jsoup.connect(URL+"?"+args)
//					.proxy(MaYiDaiLi.proxyHost, MaYiDaiLi.proxyPort, null)
//					.header("Proxy-Authorization", MaYiDaiLi.getAuthHeader())
					.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:46.0) Gecko/20100101 Firefox/46.0")
					.header("host", "wenshu.court.gov.cn")
					.header("Accept","*/*")
					.header("Accept-Encoding","gzip, deflate")
					.header("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
					.header("Connection","keep-alive")
					.header("Content-type","application/json")
					.ignoreContentType(true)
					.timeout(timeout);
			if(useProxy)
			{
				return conn.proxy(MaYiDaiLi.proxyHost, MaYiDaiLi.proxyPort, null)
				.header("Proxy-Authorization", MaYiDaiLi.getAuthHeader())
				.post();
			}
			else
			{
				return conn.post();
			}
		}
		catch (Exception e)
		{
			if(t==5)
			{
				throw e;
			}
			else
			{
				return post(URL,args,t+1);
			}
		}
	}
	
	public static Document get(String URL) throws IOException, InterruptedException
	{
		sleep();
		Connection conn = Jsoup.connect(URL)
//				.proxy(MaYiDaiLi.proxyHost, MaYiDaiLi.proxyPort, null)
//				.header("Proxy-Authorization", MaYiDaiLi.getAuthHeader())
				.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:46.0) Gecko/20100101 Firefox/46.0")
				.header("host", "wenshu.court.gov.cn")
				.header("Accept","*/*")
				.header("Accept-Encoding","gzip, deflate")
				.header("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
				.header("Connection","keep-alive")
				.header("Content-type","application/json")
				.ignoreContentType(true)
				.timeout(timeout);
		if(useProxy)
		{
			return conn
				.proxy(MaYiDaiLi.proxyHost, MaYiDaiLi.proxyPort, null)
				.header("Proxy-Authorization", MaYiDaiLi.getAuthHeader())
				.get();
		}
		else
		{
			return conn.get();
		}
	}

	public static String getAuthHeader()
	{
		paramMap.put("timestamp", format.format(new Date()));
		// 对参数名进行排序
		// 拼接有序的参数名-值串
		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append(secret);
		for(Entry<String, String> entry : paramMap.entrySet())
		{
		    stringBuilder.append(entry.getKey()).append(entry.getValue());
		}	     
		stringBuilder.append(secret);
		String codes = stringBuilder.toString();
		// MD5编码并转为大写， 这里使用的是Apache codec
		String sign = org.apache.commons.codec.digest.DigestUtils.md5Hex(codes).toUpperCase();
		paramMap.put("sign", sign);
		// 拼装请求头Proxy-Authorization的值，这里使用 guava 进行map的拼接
		String authHeader = "MYH-AUTH-MD5 " + Joiner.on('&').withKeyValueSeparator("=").join(paramMap);
		paramMap.remove("sign");
//		System.out.println(authHeader);
		return authHeader;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException
	{
//		turnOffProxy();
//		System.out.println(post("http://wenshu.court.gov.cn/List/ListContent","Direction=asc&Index=1&Order=%E6%B3%95%E9%99%A2%E5%B1%82%E7%BA%A7&Page=5&Param="));
		
//		System.out.println(
//				get("http://1212.ip138.com/ic.asp")
//				);
		
		System.out.println(post("http://gsxt.xjaic.gov.cn:7001/ztxy.do","djjg=&maent.entbigtype=11&maent.pripid=6531240000011713&method=qyInfo&random=1469683573610"));
	}
}
