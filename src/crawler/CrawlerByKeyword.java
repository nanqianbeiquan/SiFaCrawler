package crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map.Entry;

import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import tools.TimeUtils;

public class CrawlerByKeyword {

	HashMap<String,Integer> conditionMap=new HashMap<String,Integer>();
	String URL="http://wenshu.court.gov.cn/List/ListContent";

	public void crawl(String keyword,String startDt) throws UnsupportedEncodingException
	{
		
		String stopUploadDay=TimeUtils.getToday();
		String startUploadDay="2013-01-01";
		String startJudgeDay="";
		String stopJudgeDay="";
		if(startDt!=null)
		{
			startUploadDay=startDt;
		}
		
		conditionMap.put(keyword+"|"+startUploadDay+"|"+stopUploadDay+"|"+startJudgeDay+"|"+stopJudgeDay,1);

		while(conditionMap.size()>0)
		{
			Entry<String, Integer> entry = conditionMap.entrySet().iterator().next();
			String condition=entry.getKey();
			int pageIdx=entry.getValue();
			updateCondition(condition,pageIdx);
		}
	}
	
	/**
	 * 
	 * @param condition
	 * @param pageIdx
	 * @return {-1:更新失败,0:无结果,1:更新成功,9:结果太多，分裂条件}
	 * @throws UnsupportedEncodingException
	 */
	public int updateCondition(String condition,int pageIdx) throws UnsupportedEncodingException
	{
		String args="Param="+getParams(condition)
				+ "&Index="+pageIdx
				+ "&Page=20"
				+ "&Order=%E5%AE%A1%E5%88%A4%E7%A8%8B%E5%BA%8F%E4%BB%A3%E7%A0%81"
				+ "&Direction=asc";
		int itemNums=0;
		try
		{
			Document doc = MaYiDaiLi.post(URL,args);
			
			String contentText=LoadListContent.getListJsonStr(doc);
//			System.out.println(contentText);
			JSONArray jsonArr = JSONArray.parseArray(contentText);
			itemNums=jsonArr.getJSONObject(0).getIntValue("Count");
//			System.out.println("totalNums:"+itemNums);
			System.out.println("["+condition+"_"+pageIdx+"] -> "+itemNums);
			if(itemNums==0)
			{
				conditionMap.remove(condition);
				return 0;
			}
			else if(itemNums>500)
			{
				conditionMap.remove(condition);
				splitCondition(condition);
				return 9;
			}
			else
			{
				for(int i=1;i<jsonArr.size();i++)
				{
					JSONObject docJson = jsonArr.getJSONObject(i);
					LoadListContent.parseDoc(docJson);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}

		if((itemNums/20.0)<=pageIdx)
		{
			conditionMap.remove(condition);
			return 1;
		}
		else
		{
			conditionMap.put(condition, pageIdx+1);
//			return -1;
			return updateCondition(condition,pageIdx+1);
		}
	}
	
	public String getParams(String condition) throws UnsupportedEncodingException
	{
		String[] args=condition.split("\\|",-1);
		String keyword=args[0];
		String startUploadDt=args[1];
		String stopUploadDt=args[2];
		String startJudgeDt=args[3];
		String stopJudgeDt=args[4];
		
		String uploadDtCondition="";
		String judgeDtCondition="";
		String keywordCondition="";
		//上传日期参数
		uploadDtCondition=URLEncoder.encode("上传日期:","UTF-8")+startUploadDt+"+TO+"+stopUploadDt;
		//裁判日期参数
		if(!startJudgeDt.equals(""))
		{
			judgeDtCondition=URLEncoder.encode(",裁判日期:", "UTF-8")+startJudgeDt+"+TO+"+stopJudgeDt;
		}
		if(!keyword.equals(""))
		{
			keywordCondition=URLEncoder.encode(",全文检索:"+keyword, "UTF-8");;
		}
		return uploadDtCondition+judgeDtCondition+keywordCondition;
	}
	
	public void  splitCondition(String condition) throws Exception
	{
		String[] args=condition.split("\\|",-1);
		String keyword=args[0];
		String startUploadDt=args[1];
		String stopUploadDt=args[2];
		String startJudgeDt=args[3];
		String stopJudgeDt=args[4];
		String[] newDt=TimeUtils.splitDateInterval(startUploadDt, stopUploadDt);
		if(newDt.length==4)
		{
			String condition1=keyword+"|"+newDt[0]+"|"+newDt[1]+"|"+startJudgeDt+"|"+stopJudgeDt;
			String condition2=keyword+"|"+newDt[2]+"|"+newDt[3]+"|"+startJudgeDt+"|"+stopJudgeDt;
			conditionMap.remove(condition);
			conditionMap.put(condition1, 1);
			conditionMap.put(condition2, 1);
		}
		else
		{
			String[] newDt2=TimeUtils.splitDateInterval(startJudgeDt, stopJudgeDt);
			if(newDt2.length==4)
			{
				String condition1=keyword+"|"+startUploadDt+"|"+stopUploadDt+"|"+newDt2[0]+"|"+newDt2[1];
				String condition2=keyword+"|"+startUploadDt+"|"+stopUploadDt+"|"+newDt2[3]+"|"+newDt2[4];
				conditionMap.remove(condition);
				conditionMap.put(condition1, 1);
				conditionMap.put(condition2, 1);
			}
			else
			{
				throw new Exception("查询结果太大，请继续拆分！");
			}
		}
	}
	
	public void updateFromFile(String path,String startDt) throws UnsupportedEncodingException, IOException
	{
		File src=new File(path);
		InputStreamReader read = new InputStreamReader(new FileInputStream(src));
        BufferedReader bufferedReader = new BufferedReader(read);
        String lineText = null;
        while((lineText = bufferedReader.readLine()) != null)
        {
        	System.out.println(lineText);
        	crawl(lineText,startDt);
//        	break;
        }
        bufferedReader.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		MaYiDaiLi.turnOffProxy();
//		MaYiDaiLi.setSleepSeconds(2);
		LoadListContent.setDeleteOldToTrue();
		CrawlerByKeyword crawler= new CrawlerByKeyword();
//		crawler.crawl("上海大汉三通投资有限公司");
//		crawler.crawl("（2015）广大商初字第146-1号");
		crawler.updateFromFile("data/待更新公司名_2016-07-27.txt","2016-07-27");
	}
}
