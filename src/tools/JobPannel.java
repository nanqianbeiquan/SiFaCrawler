package tools;

import crawler.GenerateSearchConditionsJob;
import crawler.MaYiDaiLi;
import crawler.SearchConditionsErrorUpdateJob;
import crawler.SearchConditionsUpdateJob;
import crawler.UpdateDocURL;

public class JobPannel {
	
	public static boolean outer=false;
	
	public static void run(String[] args) throws Exception
	{
		
		JobConfig jobConf=new JobConfig(args);
		if(jobConf.hasProperty("useProxy"))
		{
			boolean useProxy=jobConf.getBoolean("useProxy");
			if(useProxy)
			{
				MaYiDaiLi.turnOnProxy();
			}
			else
			{
				MaYiDaiLi.turnOffProxy();
			}
		}
		if(jobConf.hasProperty("sleepSeconds"))
		{
			MaYiDaiLi.setSleepSeconds(jobConf.getInteger("sleepSeconds"));
		}
		if(jobConf.hasProperty("outer"))
		{
			outer=jobConf.getBoolean("outer");
		}
		
		else if(jobConf.jobName.equals("SearchConditionsUpdateJob"))
		{
			new SearchConditionsUpdateJob(jobConf).execUpdateProc();;
		}

		else if(jobConf.jobName.equals("SearchConditionsErrorUpdateJob"))
		{
			new SearchConditionsErrorUpdateJob(jobConf).execUpdateProc();;
		}
		else if(jobConf.jobName.equals("GenerateSearchConditionsJob"))
		{
			new GenerateSearchConditionsJob().run(jobConf);
		}
		else if(jobConf.jobName.equals("UpdateDocURL"))
		{
			new UpdateDocURL().execUpdateProc();
		}
		else
		{
			System.out.println("Please input right jobName!");
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		run(args);
	}
}
