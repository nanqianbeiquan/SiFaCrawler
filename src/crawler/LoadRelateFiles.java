package crawler;

import java.io.IOException;
import java.sql.SQLException;

import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import tools.MSSQL;

public class LoadRelateFiles {

	public static void loadDoc(String docId) throws ClassNotFoundException, IOException, SQLException
	{
		loadDoc(docId,0);
	}
	
	public static void loadDoc(String docId,int t) throws IOException, ClassNotFoundException, SQLException
	{
		System.out.println("加载关联文书");
		if(t==5)
		{
			return;
		}
		String URL="http://wenshu.court.gov.cn/Content/GetRelateFiles";
		String args="docId="+docId;
		try
		{
			Document doc = MaYiDaiLi.post(URL, args);
			JSONObject jsonObj=JSONObject.parseObject(doc.text());
			
			JSONArray relateFiles=(JSONArray) jsonObj.get("RelateFile");
			if(relateFiles.size()>0)
			{
				String deleteCmd="delete from TB_Related_Documents where docId='"+docId+"'";
				MSSQL.executeUpdate(deleteCmd);
				
				for(int i=0;i<relateFiles.size();i++)
				{
					
					JSONObject relateFile=(JSONObject) relateFiles.get(i);
					
					String relatedDocId=relateFile.getString("文书ID");
					String relatedDocket=relateFile.getString("案号");
					
					String insertCmd=String.format("insert into TB_Related_Documents values('%s','%s','%s')", docId,relatedDocket,relatedDocId);
					MSSQL.executeUpdate(insertCmd);
//					System.out.println(insertCmd);
				}
			}
		}
		catch (HttpStatusException e)
		{
//			e.printStackTrace();
			System.out.println("HttpStatusException -> "+e.getStatusCode());
			loadDoc(docId,t+1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			loadDoc(docId,t+1);
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException
	{
		loadDoc("89b02da9-e94b-4dd3-a231-9724ca9876fb");
	}
}
