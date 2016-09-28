package crawler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import tools.MSSQL;

public class GetSummary {

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
	
	public static HashMap<String, String> getDocSummary(String docId) throws ClassNotFoundException, IOException, SQLException
	{
		return getDocSummary(docId,0);
	}
	
	public static HashMap<String, String> getDocSummary(String docId,int t) throws IOException, ClassNotFoundException, SQLException
	{
		System.out.println("加载概要信息");
		HashMap<String,String> resMap=new HashMap<String,String>();
//		String dangShiRen=null;
		if(t==5)
		{
			return resMap;
		}
		String URL="http://wenshu.court.gov.cn/Content/GetSummary";
		String args="docId="+docId;
		
		try
		{	
			Document doc = MaYiDaiLi.post(URL, args);
//			System.out.println(doc.text());
			String content = doc.text()
					.replace("\\\"", "\"")
					.replace("&amp;","&")
					.replace("RelateInfo", "\"RelateInfo\"")
					.replace("name", "\"name\"")
					.replace("key", "\"key\"")
					.replace("value", "\"value\"")
					.replace("'", "")
					;
			
			int idx1=content.indexOf("[");
			int idx2=content.indexOf("]",idx1);
			
			String RelateInfoStr=content.substring(idx1,idx2+1);
			JSONArray relateInfoArr = JSONArray.parseArray(RelateInfoStr);
			
			HashMap<String,String> basicInfo=new HashMap<String,String>();
			basicInfo.put("docId", docId);
			
			StringBuilder colBuilder=new StringBuilder("docId");
			StringBuilder valBuilder=new StringBuilder("'"+docId+"'");
			
			for(Object relateInfo:relateInfoArr)
			{
				String desc=((JSONObject)relateInfo).getString("name");
				String val=((JSONObject)relateInfo).getString("value");
				String col=tbBaseInfodesc2Col.get(desc);
				resMap.put(desc, val);
//				if(desc.equals("当事人"))
//				{
//					dangShiRen=val;
//				}
				if(col==null)
				{
					String sql=String.format("insert into pachong.dbo.UnknownColumn values('裁判文书','%s','%s',getDate(),-1)",basicInfo.get("DocID"),desc);
					MSSQL.executeUpdate(sql);
					throw new IllegalArgumentException("未知列名："+desc);
				}
				colBuilder.append(","+col);
				valBuilder.append(",'"+val+"'");
				
			}
			
			if(content.contains("LegalBase"))
			{
				int idx3=content.indexOf("LegalBase:")+"LegalBase:".length();
				int idx4=content.lastIndexOf("]");
				
				colBuilder.append(",LegalBasis");
				String LegalBase=content.substring(idx3,idx4+1);
				valBuilder.append(",'"+LegalBase+"'");
			}
			String deleteCmd="delete from TB_Base_Info where docId='"+docId+"'";
			MSSQL.executeUpdate(deleteCmd);
			String insertCmd=String.format("insert into TB_Base_Info(%s,LastUpdateTime) values (%s,getDate())", colBuilder.toString(),valBuilder.toString());
//			System.out.println(insertCmd);
			MSSQL.executeUpdate(insertCmd);
			return resMap;
			
		}
		catch (HttpStatusException e)
		{
//			e.printStackTrace();
			System.out.println("HttpStatusException -> "+e.getStatusCode());
			return getDocSummary(docId,t+1);
		}
		catch (Exception e)
		{
			return getDocSummary(docId,t+1);
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException
	{
		System.out.println(getDocSummary("47b9425e-8174-4428-88fd-675537f2b9f1"));
	}
	
}
