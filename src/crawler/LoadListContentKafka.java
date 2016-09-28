package crawler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import parser.SifaParserKafka;
import tools.KafkaAPI;
import tools.MSSQL;
import tools.ParseCaseNo;

public class LoadListContentKafka {

	boolean getRelateFiles=true;
	boolean getSummary=true;
	static int timeout=10000;
	String dangShiRen=null;
	static String[] caseTypeArr={"刑事案件","民事案件","行政案件","赔偿案件","执行案件"};
	static boolean deleteOld=false;

	static HashMap<String,String> jsonDesc2Col=new HashMap<String,String>();
	static 
	{                                           
		jsonDesc2Col.put("文书ID", "f_name");      
		jsonDesc2Col.put("案件名称", "title");      
		jsonDesc2Col.put("裁判日期", "judgmenttime");
		jsonDesc2Col.put("案号", "docket");         
		jsonDesc2Col.put("法院名称", "courtname");
		jsonDesc2Col.put("案由","causeaction");
		jsonDesc2Col.put("案件类型","casetype");
    
	}
	
	static HashMap<String,String> documentPageDesc2Col=new HashMap<String,String>();
	static
	{
		documentPageDesc2Col.put("文书ID", "docId");
		documentPageDesc2Col.put("案件名称", "title");
		documentPageDesc2Col.put("裁判日期", "uploadDate");
		documentPageDesc2Col.put("案号", "code");
		documentPageDesc2Col.put("法院名称", "courtName");
		documentPageDesc2Col.put("审判程序", "proceedings");
		documentPageDesc2Col.put("案件类型","caseType");
	}

	public static void setDeleteOldToTrue()
	{
		deleteOld=true;
	}
	
	
	public static String getListJsonStr(Document doc)
	{
		String docText=doc.text();
		StringBuilder content = new StringBuilder(docText
				.replace("\\u003c", "<")
				.replace("\\u003e", ">")
				.replace("\\\\", "\\")
				.replace("\\\"", "\"")
				.replace("\\/", "/")
//				.replace("\\n", "\n")
				.replace("\\u0026nbsp;", " ")
				.replace("\\u0026ldquo", "“")
				.replace("\\u0026rdquo", "”")
				.replace("\\u0026", "&")
//				.replace("&amp;#xA;","\n")
				.replace("&amp;","&")
//				.replace("&#xA;", "\n")
				);
		if(content.charAt(0)=='"')
		{
			content.deleteCharAt(0);
		}
		if(content.charAt(content.length()-1)=='"')
		{
			content.deleteCharAt(content.length()-1);
		}
		
		String contentText=content.toString();
//		System.out.println(content);
		int idx1=0;
		int idx2_1=contentText.indexOf("书记员",idx1);
		int idx2_2=contentText.indexOf("书 记 员",idx1);
		int idx2=-1;
		if(idx2_1==-1 && idx2_2==-1)
		{
			idx2=-1;
		}
		else if(idx2_1==-1 && idx2_2!=-1)
		{
			idx2=idx2_2;
		}
		else if(idx2_1!=-1 && idx2_2==-1)
		{
			idx2=idx2_1;
		}
		else
		{
			idx2=Math.min(idx2_1, idx2_2);
		}
		
		while(idx2>0)
		{
			idx1=contentText.indexOf("\",\"", idx2);
			if(idx1==-1)
			{
				break;
			}
			int idx3=contentText.indexOf("&#xA", idx2);
			
			if(idx3>0 && idx3<idx1)
			{
				contentText=contentText.substring(0, idx3)+contentText.substring(idx1);
			}
			
			idx2_1=contentText.indexOf("书记员",idx1);
			idx2_2=contentText.indexOf("书 记 员",idx1);
			if(idx2_1==-1 && idx2_2==-1)
			{
				idx2=-1;
			}
			else if(idx2_1==-1 && idx2_2!=-1)
			{
				idx2=idx2_2;
			}
			else if(idx2_1!=-1 && idx2_2==-1)
			{
				idx2=idx2_1;
			}
			else
			{
				idx2=Math.min(idx2_1, idx2_2);
			}
		}
//		System.out.println("<\n"+content+"\n>");
		contentText=LoadDocContent.Html2Text(contentText);
//		System.out.println("<\n"+content+"\n>");
		return contentText;
	}
	
	public int splitCondition(String condition) throws ClassNotFoundException, SQLException, ParseException
	{
		int splitRes=9;
		try
		{
			splitRes=GenerateSearchConditionsJob.splitConditions(condition);
		}
		catch (Exception e)
		{
			if(!e.getMessage().contains("违反了 PRIMARY KEY 约束") && !e.getMessage().contains("Duplicate entry"))
			{
				throw e;
			}
		}
		return splitRes;
	}
	
	public static int parseDoc(KafkaAPI kafka,String taskId,String accountId,String keyword,double progress,JSONObject docJson) throws ClassNotFoundException, SQLException, IOException
	{
		int loadStatus=0;
		Iterator<Entry<String, Object>> iterator = docJson.entrySet().iterator();
		StringBuilder colBuilder=new StringBuilder("lastUpdateTime");
		StringBuilder valBuilder=new StringBuilder("getDate()");
		String docId=docJson.getString("文书ID");
		HashMap<String,String> jsonRes=new HashMap<String,String>();
		while(iterator.hasNext())
		{
			Entry<String, Object> entry = iterator.next();
			String desc=entry.getKey();
			String val=entry.getValue().toString();
			String col=documentPageDesc2Col.get(desc);
			if(col!=null)
			{
				if(col.equals("caseType"))
				{
					try
					{
						val=caseTypeArr[Integer.valueOf(val)-1];
					}
					catch (Exception e)
					{
						//案件类型不是1、2、3、4、5，而是实际类型字符串
					}
				}
//				else if(col.equals("title"))
//				{
//					System.out.println(val);
//				}
				if(val.contains("'"))
				{
					val=val.replace("'", "''");
				}
				colBuilder.append(","+col);
				valBuilder.append(",'"+val+"'");
			}
			if(jsonDesc2Col.containsKey(desc))
			{
				jsonRes.put(jsonDesc2Col.get(desc), val);
			}
			
//			System.out.println(entry);
		}
		String filePage="http://wenshu.court.gov.cn/content/content?DocID="+docId;
		colBuilder.append(",filePage");
		valBuilder.append(",'"+filePage+"'");
		if(deleteOld)
		{
			MSSQL.executeUpdate("delete from documentpage where docid='"+docId+"'");
			MSSQL.executeUpdate("delete from JudicialInfo where docid='"+docId+"'");
		}
	
		String sql1=String.format("select docid from JudicialInfo where docId='%s'",docId);
		ResultSet res = MSSQL.executeQuery(sql1);

		if(!res.next())
		{
			loadStatus=1;
			HashMap<String, String> summaryMap = GetSummary.getDocSummary(docId);
			
			for(String keyDesc:new String[]{"案件类型","案由","裁判日期"})
			{
				if(summaryMap.containsKey(keyDesc))
				{
					jsonRes.put(jsonDesc2Col.get(keyDesc),summaryMap.get(keyDesc));
				}
			}
			String dangShiRen=summaryMap.get("当事人");
			
			LoadRelateFiles.loadDoc(docId);
			String docText=docJson.getString("DocContent");
			int updateStatus=1;
//			System.out.println("docId:"+docId);
//			System.out.println("dangShiRen:"+dangShiRen);
			if(docText==null || !(docText.trim()).contains("\n"))
			{
				System.out.println("单独获取文书内容："+docId);
				docText=LoadDocContent.getDocContent(docId);
			}
			
			if(docText!=null)
			{
				if(docText.length()>0)
				{
					docText=docText.replace("\\n", "\n")
							.replace("&amp;#xA;","\n")
							.replace("&#xA;", "\n")
							.replace("'", "")
							.replaceAll("\n+", "\n");
					try
					{
						SifaParserKafka parser =new SifaParserKafka();
						JSONArray resArr=parser.parse(docText, docId, dangShiRen);
						if(resArr.size()==0)
						{
							JSONObject json=new JSONObject();
							json.put("progress", progress);
							json.put("taskId",taskId);
							json.put("accountId",accountId);
							json.put("inputCompanyName",keyword);
							json.put("CF", "judgidentifier");
							kafka.send(CrawlerByKeywordKafka.topic, json.toJSONString());
						}
						for(int i=0;i<resArr.size();i++)
						{
							String docket=resArr.getJSONObject(i).getString("docket");
							if(docket!=null)
							{
								docket=ParseCaseNo.parse(docket);
								resArr.getJSONObject(i).put("docket", "docket");
							}
							resArr.getJSONObject(i).putAll(jsonRes);
							resArr.getJSONObject(i).put("progress",progress);
							resArr.getJSONObject(i).put("CF","judgidentifier");
							String cause=resArr.getJSONObject(i).getString("causeaction");
							String causeLevel=getCauseLevel(cause);
							resArr.getJSONObject(i).put("causename",causeLevel);
							resArr.getJSONObject(i).put("taskId",taskId);
							resArr.getJSONObject(i).put("accountId",accountId);
							resArr.getJSONObject(i).put("inputCompanyName",keyword);
//							System.out.println(resArr.getJSONObject(i).toJSONString().replace("\\n", "<br>"));
							kafka.send(CrawlerByKeywordKafka.topic, resArr.getJSONObject(i).toJSONString().replace("\\n", "<br>"));
							
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
						updateStatus=3;
					}
//					SaveDoc.save(docText,docId);
				}
				else
				{
					System.out.println(docId+"内容加载失败1！");
					updateStatus=2;
				}
			}
			else
			{
				System.out.println(docId+"内容加载失败2！");
				updateStatus=0;
			}
			
			colBuilder.append(",UpdateStatus");
			valBuilder.append(","+updateStatus);
			String insertCmd=String.format("insert into documentPage (%s) values(%s)", colBuilder.toString(),valBuilder.toString());
//			System.out.println(insertCmd);
			try
			{
				MSSQL.executeUpdate(insertCmd);
			}
			catch (Exception e)
			{
				if(e.getMessage().contains("违反了 PRIMARY KEY 约束") || e.getMessage().contains("Duplicate entry"))
				{
					String updateCmd=String.format("update documentPage set lastUpdateTime=getDate() where docid='"+docId+"'");
//					System.out.println(updateCmd);
					MSSQL.executeUpdate(updateCmd);
				}
			}
			
		}
		return loadStatus;
	}
	
	public static String getCauseLevel(String cause) throws SQLException
	{
		String causeLevel=null;
		String sql="select cause_level from [lu_cause] where sub_cause='"+cause+"'";
		ResultSet res = MSSQL.executeQuery(sql);
		if(res.next())
		{
			causeLevel=res.getString("cause_level");
		}
		return causeLevel;
	}
	
	public String generateSearchParamForPost(String condition) throws UnsupportedEncodingException
	{
		String[] args=condition.split("\\|",-1);
		String courtLevel=args[0];
		String areaLevel=args[1];
		String court=args[2];
		String ajlx=args[4];
		String startUploadDt=args[5];
		String stopUploadDt=args[6];
		String startJudgeDt=args[8];
		String stopJudgeDt=args[9];
		String keyword=args[10];
		String courtLevelCondition="";
		String areaLevelCondition="";
		String ajlxCondition="";
		String uploadDtCondition="";
		String judgeDtCondition="";
		String keywordCondition="";
		if(!courtLevel.equals("null"))
		{
			courtLevelCondition=URLEncoder.encode(",法院层级:"+courtLevel, "UTF-8");
		}
		if(!areaLevel.equals("null"))
		{
			areaLevelCondition=URLEncoder.encode(","+areaLevel+":"+court, "UTF-8");
		}
		//案件类型参数
		ajlxCondition=URLEncoder.encode(",案件类型:"+ajlx, "UTF-8");
		//上传日期参数
		uploadDtCondition=URLEncoder.encode(",上传日期:","UTF-8")+startUploadDt+"+TO+"+stopUploadDt;
		//裁判日期参数
		if(!startJudgeDt.equals(""))
		{
			judgeDtCondition=URLEncoder.encode(",裁判日期:", "UTF-8")+startJudgeDt+"+TO+"+stopJudgeDt;
		}
		if(!keyword.equals(""))
		{
			keywordCondition=URLEncoder.encode(",全文检索:"+keyword, "UTF-8");;
		}
		String param=courtLevelCondition+areaLevelCondition+ajlxCondition+uploadDtCondition+judgeDtCondition+keywordCondition;
		while(param.startsWith("%2C"))
		{
			param=param.substring(3);
		}
		return param;
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, ParseException
	{
		
	}
	
}
