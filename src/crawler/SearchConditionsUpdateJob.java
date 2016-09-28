package crawler;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;

import tools.JobConfig;
import tools.Logger;
import tools.MSSQL;

public class SearchConditionsUpdateJob {

	private Logger logger;
	LoadListContent loader= new LoadListContent();
	
	public SearchConditionsUpdateJob(JobConfig jobConf) throws ClassNotFoundException, SQLException, IOException
	{
		if(jobConf.hasProperty("useProxy"))
		{
			jobConf.getBoolean("useProxy");
		}
		String runtimeMXBeanName = ManagementFactory.getRuntimeMXBean().getName();
		String pid=runtimeMXBeanName.split("@")[0];
		logger=new Logger("SearchConditionsUpdateJob_"+pid);
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
			if(!e.getMessage().contains("违反了 PRIMARY KEY 约束"))
			{
				throw e;
			}
		}
		return splitRes;
	}

	public void execUpdateProc() throws Exception
	{
		while(true)
		{
			String sql1="select top 1 * from "
					+ "("
						+ "SELECT TOP 30 * "
						+ "FROM search_conditions with(nolock) where update_status=-1 order by last_update_time desc"
					+ ") a "
					+ "order by NEWID()";
			ResultSet res1 = MSSQL.executeQuery(sql1);
			if(res1.next())
			{
				String condition=res1.getString(1);
				int pageIdx=res1.getInt(4);
				int realItemNums=res1.getInt(6);
				String sql2="update search_conditions set update_status=-2,last_update_time=getDate() where condition='"+condition+"'";
//				System.out.println(sql2);
				MSSQL.executeUpdate(sql2);
				System.out.println("更新 ["+condition+"]。。。");
//				mssql.commit();
				int[] updateRes=loader.loadCondition(condition, pageIdx, realItemNums);
				System.out.println("更新结果: "+Arrays.toString(updateRes));
				if(updateRes[0]==2 || updateRes[0]==3)
				{
					String sql4=String.format("insert into search_conditions_error"
							+ "(condition,page_idx,update_status,last_update_time) values('%s',%d,2,getDate())", 
							condition,updateRes[1]);
//					System.out.println(sql4);
					try
					{
						MSSQL.executeUpdate(sql4);
					}
					catch (Exception e){}
					if(updateRes[1]<25)
					{
						updateRes[1]++;
						updateRes[0]=-1;
					}
					else
					{
						updateRes[0]=5;
					}
//					updateRes[1]++;
//					updateRes[0]=-1;
					System.out.println("更新结果: "+Arrays.toString(updateRes));
				}
				String sql3="update search_conditions set update_status="+updateRes[0]+",last_update_time=getDate(),page_idx="+updateRes[1] 
						+",item_nums="+updateRes[2]+",real_item_nums="+updateRes[3]
						+ " where condition='"+condition+"'";
//				System.out.println(sql3);
				MSSQL.executeUpdate(sql3);
//				mssql.commit();
			}
			else
			{
				String sql4="update search_conditions set update_status=-1 where update_status=-2";
				MSSQL.executeUpdate(sql4);
				ResultSet res4 = MSSQL.executeQuery("select @@ROWCOUNT");
//				mssql.commit();
				if(res4.next())
				{
					if(res4.getInt(1)>0)
					{
						continue;
					}
				}
				logger.info("更新完毕！");
				break;
			}
		}
	}
	
	public static void main(String[] args) throws Exception
	{
//		MaYiDaiLi.turnOffProxy();
//		MaYiDaiLi.setSleepSeconds(2);
//		JobPannel.outer=true;
		String[] newArgs=new String[]{"--useProxy=false"};
//		MaYiDaiLi.turnOffProxy();
		
		JobConfig jobConf=new JobConfig(newArgs);
		SearchConditionsUpdateJob job=new SearchConditionsUpdateJob(jobConf);
		job.execUpdateProc();
//		job.updateCondition("null|法院地域|云南省|一级案由|刑事案由|2016-06-03|2016-06-04|1|||", 1, 0);
	}
}
