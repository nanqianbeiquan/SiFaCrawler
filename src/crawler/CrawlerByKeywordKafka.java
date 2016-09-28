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

import tools.KafkaAPI;
import tools.TimeUtils;

public class CrawlerByKeywordKafka {

	public final static String topic="SifaCrawlerTest";
	HashMap<String,Integer> conditionMap=new HashMap<String,Integer>();
	String URL="http://wenshu.court.gov.cn/List/ListContent";
	
	KafkaAPI kafka=new KafkaAPI();
	JSONArray resJsonArr=new JSONArray();
	int totalNums=0;
	int curNums=0;
	String taskId;
	String accountId;
	String keyword;
	public CrawlerByKeywordKafka(String taskId,String accountId) throws IOException
	{
		this.taskId=taskId;
		this.accountId=accountId;
	}
	
	public void crawl(String keyword,String startDt) throws UnsupportedEncodingException,IOException
	{
		this.keyword=keyword;
		String startUploadDay="2016-01-01";
		String startJudgeDay="2016-01-01";
		String stopUploadDay=TimeUtils.getToday();
		String stopJudgeDay=TimeUtils.getToday();
		if(startDt!=null && startDt.compareTo(startUploadDay)>0)
		{
			startUploadDay=startDt;
			startJudgeDay=startDt;
		}

		conditionMap.put(keyword+"|"+startUploadDay+"|"+stopUploadDay+"|"+startJudgeDay+"|"+stopJudgeDay,1);

		while(conditionMap.size()>0)
		{
			Entry<String, Integer> entry = conditionMap.entrySet().iterator().next();
			String condition=entry.getKey();
			int pageIdx=entry.getValue();
			updateCondition(condition,pageIdx);
		}
		
		JSONObject res=new JSONObject();
		res.put("dataCnt", resJsonArr.size());
		res.put("data", resJsonArr);
//		kafka.send("SifaCrawlerTest", res.toJSONString().replace("\\n", "<br>"));
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
		
		try
		{
			Document doc = MaYiDaiLi.post(URL,args);
			
			String contentText=LoadListContent.getListJsonStr(doc);
//			System.out.println(contentText);
			JSONArray jsonArr = JSONArray.parseArray(contentText);
			if(totalNums==0)
			{
				totalNums=jsonArr.getJSONObject(0).getIntValue("Count");
				if(totalNums==0)
				{
					JSONObject json=new JSONObject();
					json.put("progress", 1.0);
					json.put("taskId",taskId);
					json.put("accountId",accountId);
					json.put("inputCompanyName",keyword);
					json.put("CF", "judgidentifier");
					
					kafka.send(topic, json.toJSONString());
				}
			}
			int itemNums=jsonArr.getJSONObject(0).getIntValue("Count");
//			System.out.println("totalNums:"+itemNums);
			System.out.println("["+condition+"_"+pageIdx+"] -> "+totalNums);
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
					curNums++;
					JSONObject docJson = jsonArr.getJSONObject(i);
					double progress=Double.valueOf(curNums)/totalNums;
//					System.out.println(progress);
					LoadListContentKafka.parseDoc(kafka,taskId,accountId,keyword,progress,docJson);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		}

		if((totalNums/20.0)<=pageIdx)
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
	
	public void updateFromFile(String path) throws UnsupportedEncodingException, IOException
	{
		File src=new File(path);
		InputStreamReader read = new InputStreamReader(new FileInputStream(src));
        BufferedReader bufferedReader = new BufferedReader(read);
        String lineText = null;
        while((lineText = bufferedReader.readLine()) != null)
        {
        	System.out.println(lineText);
        	crawl(lineText,null);
//        	break;
        }
        bufferedReader.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		MaYiDaiLi.turnOffProxy();
		LoadListContentKafka.setDeleteOldToTrue();
		CrawlerByKeywordKafka crawler= new CrawlerByKeywordKafka("123","456");
		crawler.crawl("天翼视讯传媒有限公司",null);
//		crawler.crawl("（2015）广大商初字第146-1号");
//		crawler.updateFromFile("data/待更新公司名_2016-06-16.txt");
	}
}
