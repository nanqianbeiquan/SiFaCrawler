package crawler;

import java.io.IOException;
import java.sql.ResultSet;
import parser.SifaParser;
import tools.Logger;
import tools.MSSQL;

public class UpdateDocURL {

	/*
	 * 更新状态：
	 * 未更新:0
	 * 更新中:-1
	 * 已更新:1
	 * 加载失败：2
	 * 解析失败：3
	 * 老数据已更新：5
	 */
	public Logger logger=new Logger("UpdateDocURL");
	public UpdateDocURL() throws IOException {}
	public void execUpdateProc() throws Exception {
		int totalUpdateCnt=0;
		logger.info("totalUpdateCnt:"+totalUpdateCnt);
		while(true)
		{
			String sql0="select top 1 docId from "
					+ "("
						+ "SELECT TOP 30 docId "
						+ "FROM Judgment.dbo.DocumentPage with(nolock) where updateStatus=0"
					+ ") a "
					+ "order by NEWID()";
			ResultSet res0 = MSSQL.executeQuery(sql0);
			String docId=null;
			if(res0.next())
			{
				docId=res0.getString(1);
				logger.info("获取"+docId);
			}
			res0.close();
			if(docId!=null)
			{
//				docId="3f117560-973b-49a2-997c-62bc22f1ddd9";
				String sql1=String.format("update DocumentPage set updateStatus=-1 where docId='%s'",docId);
				MSSQL.executeUpdate(sql1);
				logger.info("更新状态标记为-1");
				String docContent=LoadDocContent.getDocContent(docId);
				int updateStatus=0;
				if(docContent!=null)
				{
					if(docContent.equals(""))
					{
						updateStatus=2;
					}
					else
					{
						String dangShiRen=GetSummary.getDocSummary(docId).get("当事人");
						LoadRelateFiles.loadDoc(docId);
						try
						{
							SifaParser.parse(docContent, docId, dangShiRen);
							updateStatus=1;
						}
						catch (Exception e)
						{
							updateStatus=3;
						}
						SaveDoc.save(docContent, docId);
					}
				}
				
				MSSQL.executeUpdate("update DocumentPage set updateStatus="+updateStatus+" where docId='"+docId+"'");			
				logger.info("更新状态标记为"+updateStatus);
				totalUpdateCnt+=1;
				logger.info("totalUpdateCnt:"+totalUpdateCnt);
			}
			else
			{
				String sql2="update DocumentPage set updateStatus=0 where updateStatus=-1";
				MSSQL.executeUpdate(sql2);
				System.out.println("sql2执行成功");
				ResultSet rowCountRes = MSSQL.executeQuery("select @@ROWCOUNT");
				int rowCount=0;
				rowCountRes.next();
				rowCount=rowCountRes.getInt(1);
				if(rowCount==0)
				{
					logger.info("更新完毕！");
					break;
				}
				else
				{
					continue;
				}
			}
		}
		logger.info("Mession complete!");
		logger.close();
	}

	public static void main(String[] args) throws Exception
	{
		MaYiDaiLi.turnOffProxy();
		MaYiDaiLi.setSleepSeconds(1);
		UpdateDocURL job=new UpdateDocURL();
		job.execUpdateProc();
	}
}
