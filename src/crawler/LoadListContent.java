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

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import parser.SifaParser;
import tools.MSSQL;

public class LoadListContent {

	boolean getRelateFiles=true;
	boolean getSummary=true;
	static int timeout=10000;
	String dangShiRen=null;
	static String[] caseTypeArr={"刑事案件","民事案件","行政案件","赔偿案件","执行案件"};
	static boolean deleteOld=false;
	
	
	
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
	
	static HashMap<String,String> tbBaseInfodesc2Col=new HashMap<String,String>();
	static 
	{
		tbBaseInfodesc2Col.put("公诉机关","ProsecutingOrgan");
		tbBaseInfodesc2Col.put("法院名称","CourtName");
		tbBaseInfodesc2Col.put("审理法院","CourtName");
		tbBaseInfodesc2Col.put("案件类型","CaseType");
		tbBaseInfodesc2Col.put("审理程序","Proceedings");
		tbBaseInfodesc2Col.put("判决时间","JudgmentTime");
		tbBaseInfodesc2Col.put("裁判日期","JudgmentTime");
		tbBaseInfodesc2Col.put("案由","CauseAction");
		tbBaseInfodesc2Col.put("当事人"	,"LegalParties");
		tbBaseInfodesc2Col.put("法律依据","LegalBasis");
		tbBaseInfodesc2Col.put("DocID","DocID");
		tbBaseInfodesc2Col.put("行政管理范围","AdministrationScope");
		tbBaseInfodesc2Col.put("行政行为种类","AdministrationActionType");
	}

	public static void setDeleteOldToTrue()
	{
		deleteOld=true;
	}
	
	public int[] loadCondition(String condition,int pageIdx,int realItemNums) throws IOException, ClassNotFoundException, SQLException, ParseException
	{
		System.out.println("加载第"+pageIdx+"页");
		int loadStatus=-1;
		int itemNums=0;
		int addCnt=0;
		String param=generateSearchParamForPost(condition);
		String args="Param="+param
				+ "&Index="+pageIdx
				+ "&Page=20"
				+ "&Order=%E5%AE%A1%E5%88%A4%E7%A8%8B%E5%BA%8F%E4%BB%A3%E7%A0%81"
				+ "&Direction=asc";
		
		String URL="http://wenshu.court.gov.cn/List/ListContent";
//		System.out.println(URL+"?"+args);
		try
		{
			Document doc = MaYiDaiLi.post(URL,args);
			String contentText=getListJsonStr(doc);
			JSONArray jsonArr = JSONArray.parseArray(contentText);
			itemNums=jsonArr.getJSONObject(0).getIntValue("Count");
			if(itemNums==0)
			{
				loadStatus=0;
				return new int[]{loadStatus,pageIdx,itemNums,realItemNums};
			}
			else if(itemNums>500)
			{
				loadStatus= splitCondition(condition);
				return new int[]{loadStatus,pageIdx,itemNums,realItemNums};
			}
			else
			{
				for(int i=1;i<jsonArr.size();i++)
				{
					JSONObject docJson = jsonArr.getJSONObject(i);
					addCnt+=(parseDoc(docJson));
					realItemNums++;
				}
			}
		}
		catch (HttpStatusException e)
		{
//			e.printStackTrace();
			System.out.println("HttpStatusException -> "+e.getStatusCode());
			loadStatus=3;
			return new int[]{loadStatus,pageIdx,itemNums,realItemNums};
		}
		catch (JSONException e)
		{
			e.printStackTrace();
			loadStatus=2;
			return new int[]{loadStatus,pageIdx,itemNums,realItemNums};
		}
		catch (Exception e)
		{
			e.printStackTrace();
			loadStatus=-1;
			return new int[]{loadStatus,pageIdx,itemNums,realItemNums};
		}
		System.out.println("新增数目:"+addCnt);
		if((itemNums/20.0)<=pageIdx)
		{
			loadStatus= 1;
			return new int[]{loadStatus,pageIdx,itemNums,realItemNums};
		}
		else
		{
			return loadCondition(condition,pageIdx+1,realItemNums);
		}
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
	
	public static int parseDoc(JSONObject docJson) throws ClassNotFoundException, SQLException, IOException
	{
		int loadStatus=0;
		Iterator<Entry<String, Object>> iterator = docJson.entrySet().iterator();
		StringBuilder colBuilder=new StringBuilder("lastUpdateTime");
		StringBuilder valBuilder=new StringBuilder("getDate()");
		String docId=docJson.getString("文书ID");
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
			String dangShiRen=GetSummary.getDocSummary(docId).get("当事人");
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
						SifaParser.parse(docText, docId, dangShiRen);
					}
					catch (Exception e)
					{
						e.printStackTrace();
						updateStatus=3;
					}
					SaveDoc.save(docText,docId);
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
		MaYiDaiLi.turnOffProxy();
		LoadListContent job=new LoadListContent();
//		[null|基层法院|瑞安市人民法院|2|民事案件|2015-12-31|2016-01-01|4|2012-01-01|2013-12-31|公司]。。。
		job.loadCondition("中级法院|中级法院|广东省广州市中级人民法院|2|民事案件|2015-07-21|2015-07-22|4|2014-07-31|2015-01-25|公司",9,0);
	}
	
}
