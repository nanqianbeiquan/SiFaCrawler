package crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveDoc {

	public static void save(String docContent,String docId) throws IOException
	{
		String today=new SimpleDateFormat("yyyyMMdd").format(new Date());
		File parentDir=new File("data/"+today);
		if(!parentDir.exists())
		{
			parentDir.mkdirs();
		}
		FileWriter fw=new FileWriter(parentDir+"/"+docId+".txt");
//		System.out.println(docText);
		fw.write(docContent);
		fw.close();
	}
}
