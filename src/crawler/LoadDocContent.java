package crawler;

import java.io.IOException;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;

public class LoadDocContent {
	
	public static String getDocContent(String docId) throws IOException
	{
		return getDocContent(docId,0);
	}
	
	public static String getDocContent(String docId,int t) throws IOException
	{
		try
		{
			Document doc=MaYiDaiLi.get("http://wenshu.court.gov.cn/CreateContentJS/CreateContentJS.aspx?DocID="+docId);
			StringBuilder b=new StringBuilder(doc.html());
//			System.out.println(b);
			if(b.indexOf("/Html_Pages/VisitRemind.html")>0)
			{
				throw new Exception("验证码页");
//				return "/Html_Pages/VisitRemind.html";
			}
			else if(b.indexOf("HtmlNotExist")>0)
			{
				throw new Exception("HtmlNotExist!");
			}
			int idx1=b.indexOf("$(function");
			int idx2=b.indexOf("<a type");
//			System.out.println(idx1+","+idx2);
			b.delete(idx1, idx2);
//			System.out.println(b);
			int idx3=b.lastIndexOf("</div>")+"</div>".length();
			int idx4=b.indexOf("</body>");
			b.delete(idx3,idx4);
//			System.out.println(Html2Text(b.toString()));	
			return Html2Text(b.toString());
		}
		catch (StringIndexOutOfBoundsException e1)
		{
			e1.printStackTrace();
			if(t<10)
			{
				return getDocContent(docId, t+1);
			}
			else
			{
				return "";
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
			if(t<10)
			{
				return getDocContent(docId, t+1);
			}
			else
			{
				return null;
			}
			
		}
	}
	
	public static String Html2Text(String inputString){
	     String htmlStr = inputString; //含html标签的字符串
	     String textStr ="";
	     java.util.regex.Pattern p_script;
	     java.util.regex.Matcher m_script;
	     java.util.regex.Pattern p_style;
	     java.util.regex.Matcher m_style;
	     java.util.regex.Pattern p_html;
	     java.util.regex.Matcher m_html;

	    try{
	          String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>"; //定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script> }
	          String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; //定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style> }
	          String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式

	          p_script = Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE);
	          m_script = p_script.matcher(htmlStr);
	          htmlStr = m_script.replaceAll(""); //过滤script标签

	          p_style = Pattern.compile(regEx_style,Pattern.CASE_INSENSITIVE);
	          m_style = p_style.matcher(htmlStr);
	          htmlStr = m_style.replaceAll(""); //过滤style标签

	          p_html = Pattern.compile(regEx_html,Pattern.CASE_INSENSITIVE);
	          m_html = p_html.matcher(htmlStr);
	          htmlStr = m_html.replaceAll(""); //过滤html标签
	          textStr = htmlStr.replaceAll("\n+", "\n");
	     }catch(Exception e){
	          e.printStackTrace();
	     }
	     return textStr;//返回文本字符串
	 }   
	public static void main(String[] args) throws IOException
	{
//		MaYiDaiLi.turnOffProxy();
		System.out.println(getDocContent("9846aec2-9881-4808-84e7-733e89de8159"));
	}

}
