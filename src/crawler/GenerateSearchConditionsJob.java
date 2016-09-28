package crawler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import tools.JobConfig;
import tools.MSSQL;
import tools.TimeUtils;


public class GenerateSearchConditionsJob {

	public String curUrl;
	public int startIdx=1;
	public int curCnt=0;
	
	public GenerateSearchConditionsJob() throws ClassNotFoundException, SQLException{}
	
	public void generateConditions() throws ClassNotFoundException, SQLException, UnsupportedEncodingException
	{
		generateConditions(TimeUtils.getYesterday(),TimeUtils.getToday(),"");
	}
	
	public void generateConditions(String startUploadDt,String stopUploadDt,String keyword) throws ClassNotFoundException, SQLException, UnsupportedEncodingException
	{
		String sql="insert into judgment.dbo.search_conditions "+
				"select (a.area+'|'+b.ajlx_id+'|'+b.ajlx+'|"+startUploadDt+"|"+stopUploadDt+"|1|||"+keyword+"'),-1,getDate(),1,0,0 "+
				"from "+
				"( "+
				"select distinct 'null|法院地域|'+area area from judgment.dbo.lu_court where area!='最高人民法院' "
				+ "union all "
				+ "select '最高法院|null|最高人民法院' area "+
				") a "+
				"cross join "+
				"lu_ajlx b";
		System.out.println(sql);
		MSSQL.executeUpdate(sql);
		MSSQL.commit();
	}
	

	
	public static String generateSearchParam(String condition) throws UnsupportedEncodingException
	{
		String[] args=condition.split("\\|",-1);
		String courtLevel=args[0];
		String areaLevel=args[1];
		String court=args[2];
		String ajlxId=args[3];
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
		/** 法院地域参数
		 *  法院层级：最高法院
		 *  法院地域：XX
		 * 		法院地域：XX && 法院层级：高级法院
		 * 		中级法院：XXXX
		 * 			中级法院：XXXX && 法院层级：中级法院
		 * 			基层法院：XXXXXX
		 */
		if(!courtLevel.equals("null"))
		{
			courtLevelCondition=getCondition(courtLevel, "法院层级");
		}
		if(!areaLevel.equals("null"))
		{
			areaLevelCondition+=getCondition(court, areaLevel);
		}
		//案件类型参数
		ajlxCondition=getAjlxCondition(ajlxId, ajlx);
		//上传日期参数
		uploadDtCondition=getUploadDtCondition(startUploadDt,stopUploadDt);
		//裁判日期参数
		if(!startJudgeDt.equals(""))
		{
			judgeDtCondition=getJudgeDtCondition(startJudgeDt, stopJudgeDt);
		}
		if(!keyword.equals(""))
		{
			
			keywordCondition=getKeywordCondition(keyword);
		}
		return courtLevelCondition+areaLevelCondition+ajlxCondition+uploadDtCondition+judgeDtCondition+keywordCondition;
	}
	
	public static String generateSearchLink(String condition) throws UnsupportedEncodingException
	{
		String urlPrefix="http://wenshu.court.gov.cn/List/List?sorttype=1";	
		return urlPrefix+generateSearchParam(condition);
	}

	public static String getUploadDtCondition(String startUploadDt,String stopUploadDt) throws UnsupportedEncodingException
	{
		String arg1=startUploadDt+"%20TO%20"+stopUploadDt;
		String arg2=URLEncoder.encode("上传日期", "UTF-8");
        return String.format("&conditions=searchWord+++%s+%s:%s", arg1,arg2,arg1);
	}
	
	public static String getJudgeDtCondition(String startJudgeDt,String stopJudgeDt) throws UnsupportedEncodingException
	{
		String arg=URLEncoder.encode("裁判日期", "UTF-8");
		return String.format("&conditions=searchWord++CPRQ++%s:%s TO %s",arg,startJudgeDt,stopJudgeDt);
	}
	
	public static String getKeywordCondition(String keyword) throws UnsupportedEncodingException
	{
		return String.format("&conditions=searchWord+QWJS+++%s:%s", 
				URLEncoder.encode("全文检索", "UTF-8"),
				URLEncoder.encode(keyword, "UTF-8"));
	}
	
	public static String getAjlxCondition(String ajlxId,String ajlx) throws UnsupportedEncodingException
	{
		return String.format("&conditions=searchWord+%s+AJLX++%s:%s", 
				ajlxId,
				URLEncoder.encode("案件类型", "UTF-8"),
				URLEncoder.encode(ajlx, "UTF-8"));
	}
	
	public static String getCondition(String arg1,String arg2) throws UnsupportedEncodingException
	{
		return String.format("&conditions=searchWord+%s+++%s:%s", 
				URLEncoder.encode(arg1, "UTF-8"),
				URLEncoder.encode(arg2, "UTF-8"),
				URLEncoder.encode(arg1, "UTF-8"));
	}

	public int run(JobConfig jobConf) throws ClassNotFoundException, UnsupportedEncodingException, SQLException
	{
		String startDt=TimeUtils.getYesterday();
//		String stopDt=TimeUtils.getToday();
		String stopDt=TimeUtils.getYesterday();
		if(jobConf.hasProperty("startDt"))
		{
			startDt=jobConf.getString("startDt");
		}
		if(jobConf.hasProperty("stopDt"))
		{
			stopDt=jobConf.getString("stopDt");
		}
		generateConditions(startDt,stopDt,"");

		return 1;
	}
	
	/**
	 * 拆分逻辑
	 * 法院地域+案件类型
	 * 拆分法院地域到中级法院
	 * 拆分中级法院到基层法院
	 * 拆分上传日期
	 * 拆分裁判日期
	 * @param mssql
	 * @param condition
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws ParseException
	 */
	
	public static int splitConditions(String condition) throws ClassNotFoundException, SQLException, ParseException
	{
		String[] args=condition.split("\\|",-1);
		String courtLevel=args[0];
		String areaLevel=args[1];
		String court=args[2];
		String ajlxId=args[3];
		String ajlx=args[4];
		String startUploadDt=args[5];
		String stopUploadDt=args[6];
		String splitId=args[7];
		String startJudgeDt=args[8];
		String stopJudgeDt=args[9];
		String keyword=args[10];
//		System.out.println(splitId);
		int splitRes=9;
		//拆区域到高级法院+中级法院
		if(splitId.equals("1"))
		{
			if(!courtLevel.equals("null"))
			{
				splitId="3";
			}
			else
			{
				String sql1=String.format("select distinct middle_court from lu_court where area='%s' and middle_court!='null'",court);
				System.out.println(sql1);
				ResultSet res1 = MSSQL.executeQuery(sql1);
				List<String> middleCourtList=new ArrayList<String>();
				while(res1.next())
				{
					String middleCourt=res1.getString(1);
					middleCourtList.add(middleCourt);
				}
				res1.close();
				if(middleCourtList.size()==0)
				{
					splitId="3";
				}
				else
				{
					middleCourtList.add(court);
					for(String middleCourt:middleCourtList)
					{
						if(middleCourt.equals(court))
						{
							courtLevel="高级法院";
							areaLevel="法院地域";
						}
						else
						{
							courtLevel="null";
							areaLevel="中级法院";
						}
						String sql2=String.format("insert into search_conditions values('%s|%s|%s|%s|%s|%s|%s|2|%s|%s|%s',-1,getDate(),1,0,0)", 
								courtLevel,areaLevel,middleCourt,ajlxId,ajlx,startUploadDt,stopUploadDt,startJudgeDt,stopJudgeDt,keyword);
//						System.out.println(sql2);
						MSSQL.executeUpdate(sql2);
//						
					}
				}
			}
			
		}
		//拆分中级法院到中级法院+基层法院
		if(splitId.equals("2"))
		{
			if(!courtLevel.equals("null"))
			{
				splitId="3";
			}
			else
			{
				String sql1="select base_court from lu_court where middle_court='"+court+"' and base_court!='null'";
				ResultSet res1 = MSSQL.executeQuery(sql1);
				List<String> baseCourtList=new ArrayList<String>();
				while(res1.next())
				{
					String baseCourt=res1.getString(1);
					baseCourtList.add(baseCourt);
				}
				res1.close();
				if(baseCourtList.size()==0)
				{
					splitId="3";
				}
				else
				{
					baseCourtList.add(court);
					for(String baseCourt:baseCourtList)
					{
						if(baseCourt.equals(court))
						{
							courtLevel="中级法院";
							areaLevel="中级法院";
						}
						else
						{
							courtLevel="null";
							areaLevel="基层法院";
						}
						String sql2=String.format("insert into search_conditions values('%s|%s|%s|%s|%s|%s|%s|3|%s|%s|%s',-1,getDate(),1,0,0)", 
								courtLevel,areaLevel,baseCourt,ajlxId,ajlx,startUploadDt,stopUploadDt,startJudgeDt,stopJudgeDt,keyword);
						MSSQL.executeUpdate(sql2);
//						System.out.println(sql2);
					}
				}
			}
		}
		//拆分上传日期
		if(splitId.equals("3"))
		{
			String[] newDt=TimeUtils.splitDateInterval(startUploadDt, stopUploadDt);
			
			if(newDt.length==4)
			{
				String condition1=args[0]+"|"+args[1]+"|"+
								  args[2]+"|"+args[3]+"|"+
								  args[4]+"|"+args[5]+"|"+
								  newDt[1]+"|3|"+
								  args[8]+"|"+args[9]+"|"+
								  keyword;
				
				String condition2=args[0]+"|"+args[1]+"|"+
						  		  args[2]+"|"+args[3]+"|"+
						  		  args[4]+"|"+newDt[2]+"|"+
						  		  args[6]+"|3|"+
						  		  args[8]+"|"+args[9]+"|"+
						  		  keyword;
				String sql1=String.format("insert into search_conditions values('%s',-1,getDate(),1,0,0)",condition1);
				String sql2=String.format("insert into search_conditions values('%s',-1,getDate(),1,0,0)",condition2);
//				System.out.println(sql1);
//				System.out.println(sql2);
				MSSQL.executeUpdate(sql1);
				MSSQL.executeUpdate(sql2);
			}
			else
			{
				splitId="4";
				startJudgeDt="2000-01-01";
				stopJudgeDt=stopUploadDt;
			}
		}
		if(splitId.equals("4"))
		{
			
			String[] newDt=TimeUtils.splitDateInterval(startJudgeDt, stopJudgeDt);
			if(newDt.length==2)
			{
				splitRes=8;
			}
			else
			{
				String condition1=args[0]+"|"+args[1]+"|"+
								  args[2]+"|"+args[3]+"|"+
								  args[4]+"|"+args[5]+"|"+
								  args[6]+"|4|"+
								  startJudgeDt+"|"+newDt[1]+"|"+keyword;
				
				String condition2=args[0]+"|"+args[1]+"|"+
						          args[2]+"|"+args[3]+"|"+
						          args[4]+"|"+args[5]+"|"+
						          args[6]+"|4|"+
						          newDt[2]+"|"+stopJudgeDt+"|"+keyword;
				
				String sql1=String.format("insert into search_conditions values('%s',-1,getDate(),1,0,0)",condition1);
				String sql2=String.format("insert into search_conditions values('%s',-1,getDate(),1,0,0)",condition2);
//				System.out.println(sql1);
//				System.out.println(sql2);
				MSSQL.executeUpdate(sql1);
				MSSQL.executeUpdate(sql2);
			}
		}
		
		String sql3=String.format("update search_conditions set update_status=%s where condition='%s'",splitRes,condition);
//		System.out.println(sql3);
		MSSQL.executeUpdate(sql3);
		return splitRes;
	}
	
	public void test() throws ClassNotFoundException, SQLException, UnsupportedEncodingException
	{
		ResultSet res=MSSQL.executeQuery("select * from search_conditions");
		while(res.next())
		{
			String condition=res.getString(1);
			System.out.println(condition);
			System.out.println(generateSearchLink(condition));
		}
	}
	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, InterruptedException
	{
		GenerateSearchConditionsJob job=new GenerateSearchConditionsJob();
		job.generateConditions("2019-09-11","2019-09-11","");
//		System.out.println(generateSearchLink("null|基层法院|长沙市芙蓉区人民法院|5|执行案件|2015-10-23|2015-11-15|3|||"));
	}
}
