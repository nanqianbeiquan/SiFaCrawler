package tools;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;

public class Test {

	public static void main(String[] args) throws IOException
	{
		Response resultImageResponse = Jsoup.connect("http://zhixing.court.gov.cn/search/security/jcaptcha.jpg")
				.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:47.0) Gecko/20100101 Firefox/47.0")
				.header("Host", "zhixing.court.gov.cn")
				.header("Accept","*/*")
				.header("Accept-Encoding","gzip, deflate")
				.header("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
				.header("Connection","keep-alive")
				.header("Content-type","application/json")
				.header("Referer", "http://zhixing.court.gov.cn/search/")
				
//				.header("Cookie","route=3d79dd694962dc238a7c6689f2fc2ec6; JSESSIONID=748CB285B156B71C1E448329F05449E9; yunsuo_session_verify=cc15f66a3635859c985ab94974aaa90a; _gscu_1049835508=64686625vzp4wb38; Hm_lvt_9e03c161142422698f5b0d82bf699727=1467093136,1467363632,1469092724,1469150050; _gscbrs_1049835508=1; Hm_lpvt_9e03c161142422698f5b0d82bf699727=1469150050")
				.ignoreContentType(true).execute();
		Map<String, String> cookies = resultImageResponse.cookies();
		FileOutputStream out = (new FileOutputStream(new java.io.File("test.jpg")));
		out.write(resultImageResponse.bodyAsBytes());           
		// resultImageResponse.body() is where the image's contents are.
		out.close();
		
		Scanner sc = new Scanner(System.in); 
        System.out.println("请输入验证码："); 
        String yzm = sc.nextLine(); 
        System.out.println("yzm:"+yzm);
		String url1="http://zhixing.court.gov.cn/search/newsearch"
				+ "?cardNum=&j_captcha="
				+yzm
				+"&pname=%E6%9D%8E%E5%87%AF"
				+ "&searchCourtName=%E5%85%A8%E5%9B%BD%E6%B3%95%E9%99%A2%EF%BC%88%E5%8C%85%E5%90%AB%E5%9C%B0%E6%96%B9%E5%90%84%E7%BA%A7%E6%B3%95%E9%99%A2%EF%BC%89"
				+ "&selectCourtArrange=1"
				+ "&selectCourtId=1"
				;
		String url2="http://zhixing.court.gov.cn/search/newdetail?id=111238590&j_captcha="+yzm;
		
//		System.out.println(url2);
		
		Connection conn2 = Jsoup.connect(url1)
//				.proxy(MaYiDaiLi.proxyHost, MaYiDaiLi.proxyPort, null)
//				.header("Proxy-Authorization", MaYiDaiLi.getAuthHeader())
				.header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:47.0) Gecko/20100101 Firefox/47.0")
//				.header("Host", "zhixing.court.gov.cn")
				.header("Accept","*/*")
				.header("Accept-Encoding","gzip, deflate")
				.header("Accept-Language","zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
//				.header("Connection","keep-alive")
				.header("Content-type","application/json")
				.header("Referer", "http://zhixing.court.gov.cn/search/")
//				.data("cardNum","")
//				.data("j_captcha",yzm)
//				.data("pname","%E6%9D%8E%E5%87%AF")
//				.data("searchCourtName","%E5%85%A8%E5%9B%BD%E6%B3%95%E9%99%A2%EF%BC%88%E5%8C%85%E5%90%AB%E5%9C%B0%E6%96%B9%E5%90%84%E7%BA%A7%E6%B3%95%E9%99%A2%EF%BC%89")
//				.data("selectCourtArrange","1")
//				.data("selectCourtId","1")
				;
		System.out.println(cookies);
		conn2.cookies(cookies);
		System.out.println(conn2.request().cookies());
		Response res = conn2.method(Method.POST).execute();
//		res = conn2.method(Method.POST).execute();
		System.out.println(res.headers());
		System.out.println(res.url());
		System.out.println(res.parse());
//		System.out.println(conn2.post());
		sc.close();
	}
}
