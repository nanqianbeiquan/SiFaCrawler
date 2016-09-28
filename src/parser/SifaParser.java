package parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.alibaba.fastjson.JSONObject;

import tools.MSSQL;
public class SifaParser {
	
	public static double maxmoney=0;
	public static String namestr=null;
	public static String personstr=null;
	public static String panjuestr=null;
	public static String content=null;
	public static String losingParty=null;
	public static String litigationType=null;
	public static String instrumentType=null;
	public static String documentType=null;
	public static String relatedAmountstr=null;
	public static String docId=null;
	public static String PersonName="";
	public static String PersonSex="";
	public static String PersonBirthday="";
	public static String PersonCard="";
	public static String PersonAddress="";
	public static String PersonIdentity="";
	public static String PersonOffice="";
	public static boolean normal=false;
	
	static Pattern pattern1 = Pattern.compile("公司（[\\s\\S]*）$");
	public static String[] shuxingArr={"案外人","被告人","被告","原告人","原告","赔偿申请人","起诉人",
			"被起诉人","上诉人","公诉机关","原公诉机关","一审被告","一审原告","原审被告",
			"原审原告","原审第三人","原审本诉原告","原审本诉被告","原审反诉原告","原审反诉被告",
			"原审申请人","原审被申请人","被申请人","再审申请人","申请再审人","再审执行人","申请人",
			"利害关系人","申请复议人","异议人","委托代理人","特别授权","负责人","诉讼代表人","监护人",
			"赔偿请求人","罪犯","申请执行人","原审自诉人","被执行人","被上诉人","抗诉机关","法定代表人",
			"法定代理人","代表人","代理人","辩护人","担保人","第三人","申报人","经营者","投资人",
			"赔偿义务机关","委托代理","申诉人","协助执行人","被罚款人","债权人",
			"第三人民法院","原审被告人"};
	
//	public static void abnormaldata(String doc) throws ClassNotFoundException, SQLException
//	{
//		int faburiqi=0;
//		faburiqi=doc.indexOf("发布日期");
//		if(faburiqi>-1)
//		{
//			doc=doc.substring(faburiqi, doc.length());
//		}
//		Pattern pat = Pattern.compile("([(（)(()(\\d+)(）)())]+)(.*)(号)");
//		Matcher ma=pat.matcher(doc);
//		
//		List<String> resultDocket=new ArrayList<String>();
//		while(ma.find())
//		{
//			resultDocket.add(ma.group());
//		}
//		String docket=resultDocket.get(0);
//
//		int gaiyao=0,anhao=0,fayuan=0;
//		
//		anhao=doc.indexOf(docket);
//		gaiyao=doc.indexOf("概要");
//		fayuan=doc.indexOf("法院");
////		System.out.println(docket);
////		String docu=doc.substring(fayuan+2, anhao);
////		docu=docu.replace(" ", "").trim();
//		if(anhao>gaiyao || docket.length()>50)
//		{
//			String insertabn=String.format("insert into [dbo].[Abnormal_Data] values('%s',getDate())",docId);		
//    		MSSQL.executeUpdate(insertabn);
//		}
//		else
//		{
//			normal=true;
//		}
//	}
//	
	/**
	 * 解析文书类型，民事裁定书 -> 民事+裁定书
	 * @param documentType 文书类型
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static void caseType(String documentType) throws IOException, ClassNotFoundException, SQLException
	{
		if(documentType.length()>0)
		{
			if(documentType.length()==5)
			{
				litigationType=documentType.substring(0, 2);
				instrumentType=documentType.substring(2, 5);			
			}
			else
			{
				litigationType=documentType.substring(0, documentType.length()-3);
			}			
		}

		
	}
	
	/**
	 * 解析当事人信息
	 * @param doc 文书全文
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static void litigant(String doc,String legalParties) throws IOException, ClassNotFoundException, SQLException
	{		
		int faburiqi=doc.indexOf("发布日期");
		if(faburiqi>0)
		{
			doc=doc.substring(faburiqi, doc.length());	
		}
		Pattern pat = Pattern.compile("([(（)(()(\\d+)(）)())]+)(.*)(号)");
//		Pattern pat = Pattern.compile("([(（)(()(\\d+)(）)())]+)([(\\u4e00-\\u9fa5)(\\d+)(－)(-)]+)(号)|([(（)(()(\\d+)(）)())]+)([(\\u4e00-\\u9fa5)(\\d+)(－)(-)]+)(号)|([(（)(()(\\d+)(）)())]+)([(\\u4e00-\\u9fa5)]+)(\\d+)([(\\u4e00-\\u9fa5)]+)(\\d+)[(－)(-)](\\d+)(号)");
		Matcher ma=pat.matcher(doc);
//		System.out.println(doc);
		List<String> resultDocket=new ArrayList<String>();
		while(ma.find())
		{
			resultDocket.add(ma.group());
		}
		String docket=resultDocket.get(0);
		int anhao=0,fayuan=0;
		anhao=doc.indexOf(docket);
//		int gaiyao=0;
//		gaiyao=doc.indexOf("概要");
		fayuan=doc.indexOf("法院");
//		System.out.println(anhao);
//		System.out.println("概要："+gaiyao);
//		System.out.println(docket);

		content=doc.substring(anhao);
//		content=doc;
		if(anhao>fayuan)
		{
			documentType=doc.substring(fayuan+2, anhao);
			documentType=documentType.replace(" ", "");
			documentType=documentType.trim();
			caseType(documentType);				
		}


		int mingzikaishi=0;
		mingzikaishi=content.indexOf(docket)+docket.length();		
			
		int yian=0,bufu=0,caiding=0,jiufen=0,shouli=0,	shenli=0,	zaishen=0,	zhixing=0,	shangsu=0,	zhifuling=0,	baoquan=0,	heyiting=0,	shencha=0,	chafeng=0,	shengxiao=0,	biwu=0,	cdrx=0,	qisushu=0,	benyuan=0,	benan=0,	anyou=0,	xieyi=0,zhixingwanbi=0;
		shouli=content.indexOf("受理");
		shenli=content.indexOf("审理");
		zaishen=content.indexOf("向本院申请再审");
		zhixing=content.indexOf("发生法律效力");
		shangsu=content.indexOf("提起上诉");
		zhifuling=content.indexOf("申请支付令");
		baoquan=content.indexOf("财产保全");
		heyiting=content.indexOf("合议庭");
		shencha=content.indexOf("审查");
		chafeng=content.indexOf("查封");
		shengxiao=content.indexOf("生效");
		biwu=content.indexOf("笔误");
		cdrx=content.indexOf("裁定如下");
		qisushu=content.indexOf("起诉书");
		benyuan=content.indexOf("本院");
		benan=content.indexOf("本案");
		anyou=content.indexOf("案由");
		xieyi=content.indexOf("协议");
		yian=content.indexOf("一案");
		bufu=content.indexOf("不服");
		caiding=content.indexOf("裁定");
		jiufen=content.indexOf("纠纷");
		zhixingwanbi=content.indexOf("执行完毕");
		int[] personOver=new int[]{shouli,shenli,zaishen,zhixing,shangsu,zhifuling,baoquan,heyiting,shencha,chafeng,shengxiao,biwu,cdrx,qisushu,benyuan,benan,anyou,xieyi,yian,bufu,jiufen,caiding,zhixingwanbi};
		java.util.Arrays.sort(personOver);
		int mingzijiewei=0,po=0;
		while(po<personOver.length)
		{
			if(personOver[po]>0)
			{
				mingzijiewei=personOver[po];
				break;
			}
			po++;	
		}
		if(mingzijiewei==-1)
		{
			mingzijiewei=content.length();
		}
//		System.out.println(anhao);
//		System.out.println(content);
//		System.out.println(mingzikaishi);
//		System.out.println(mingzijiewei);
		String[] personList=null;
		
		String dangshiren=content.substring(mingzikaishi, mingzijiewei+2);
		dangshiren=dangshiren.trim();
//		System.out.println("<"+dangshiren+">");
		String[] dangshirenList=dangshiren.split("\n");
		ArrayList<JSONObject> name=new ArrayList<JSONObject>();
		ArrayList<JSONObject> arr=new ArrayList<JSONObject>();
		String deleteId="delete from [dbo].[JudicialIndentity] where DocId='"+docId+"'";
		MSSQL.executeUpdate(deleteId);
		for(int i=0;i<dangshirenList.length-1;i++)
		{
			PersonName="";
			PersonSex="";
			PersonBirthday="";
			PersonCard="";
			PersonAddress="";
			PersonIdentity="";
			PersonOffice="";
				
			String dsrmz=dangshirenList[i];
			HashMap<String,Integer> shuxingMap=new HashMap<String,Integer>();
			for(String shuxing:shuxingArr)
			{
				shuxingMap.put(shuxing, dsrmz.indexOf(shuxing));
			}
			if(shuxingMap.get("第三人民法院")==shuxingMap.get("第三人"))
			{
					shuxingMap.put("第三人",-1);
			}
			if(shuxingMap.get("委托代理人")==shuxingMap.get("委托代理"))
			{
					shuxingMap.put("委托代理",-1);
			}
			Iterator<Entry<String, Integer>> iterator = shuxingMap.entrySet().iterator();
				
			int sxmin=Integer.MAX_VALUE,sxmax=-1;
			String xsminStr=null;

			boolean existsSxmax=false;
				
			while(iterator.hasNext())
			{
				Entry<String, Integer> entry = iterator.next();
				String key=entry.getKey();
				int val=entry.getValue();
					
				if(val>sxmax && val>=0)
				{
					existsSxmax=true;
					sxmax=val;
				}
				if(val<sxmin && val>=0)
				{
					xsminStr=key;
					sxmin=val;
				}
			}
			String shuxing = null;
			if(existsSxmax)
			{
				shuxing = xsminStr;
				PersonIdentity=shuxing;					
			}
			else
			{
				continue;
			}
//			System.out.println("++++++++++++++++++++");
//			System.out.println(dsrmz);
//			System.out.println("PersonIdentity:"+PersonIdentity);
			dsrmz=dsrmz.replace("：", ":");
			dsrmz=dsrmz.replace("，", ",");
			dsrmz=dsrmz.replace("。", ",");
			dsrmz=dsrmz.replace("（", "(");	
			dsrmz=dsrmz.replace("）", ")");
			boolean exitperson=false;
//			System.out.println("legalParties:"+legalParties);

			if (legalParties !=null)
			{			
				personList=legalParties.split(",");
				int xingming=-1;
				String pperson=null;
//				System.out.println(Arrays.toString(personList));
				for(String person:personList)
				{					
					xingming=dsrmz.indexOf(person);
//					System.out.println("__"+person);
					pperson=person;
					if(xingming>-1)
					{
						break;
					}
					
				}
				if(xingming>-1)
				{
					exitperson=true;
																	
				}
				else
				{
					int maohao=dsrmz.indexOf(":", sxmin);
					int douhao=dsrmz.indexOf(",", sxmin);
					int kuohao=dsrmz.indexOf("(", sxmin);
					int fankuohao=dsrmz.indexOf(")", sxmin);
					if(douhao==-1)
					{
						douhao=dsrmz.length();
					}
					if(kuohao==sxmin+xsminStr.length() && kuohao>-1)
					{
						dsrmz=dsrmz.substring(0, kuohao)+dsrmz.substring(fankuohao+1, dsrmz.length());
					}
					int mzqs=dsrmz.indexOf(":", sxmin);
					
					if (mzqs==-1 || maohao>douhao)
					{
						mzqs=sxmin+xsminStr.length();
					}
					else
					{
						mzqs=mzqs+1;
					}

					int mzjw=dsrmz.indexOf(",",mzqs);
					if(mzjw==-1)
					{
						mzjw=dsrmz.length();
					}
					if (mzqs>0 && mzjw>0)
					{
						
						pperson=dsrmz.substring(mzqs, mzjw);
						exitperson=true;
						
					}
				}
				
				if(exitperson)
				{
					PersonName=pperson;
//					System.out.println("*PersonName:"+PersonName);
//					break;
				}						
				
			}
			else
			{
				String person=null;
				int maohao=dsrmz.indexOf(":", sxmin);
				int douhao=dsrmz.indexOf(",", sxmin);
				int kuohao=dsrmz.indexOf("(", sxmin);
				int fankuohao=dsrmz.indexOf(")", sxmin);
				if(kuohao==sxmin+xsminStr.length())
				{
					dsrmz=dsrmz.substring(0, kuohao)+dsrmz.substring(fankuohao+1, dsrmz.length());
				}
				int mzqs=dsrmz.indexOf(":", sxmin);
				if(douhao==-1)
				{
					douhao=dsrmz.length();
				}
				
				if (mzqs==-1 || maohao>douhao)
				{
					mzqs=sxmin+xsminStr.length();
				}
				else
				{
					mzqs=mzqs+1;
				}

				int mzjw=dsrmz.indexOf(",",mzqs);
				if(mzjw==-1)
				{
					mzjw=dsrmz.length();
				}
				if (mzqs>0 && mzjw>0)
				{
					person=dsrmz.substring(mzqs, mzjw);
					exitperson=true;
					
				}
				
				if(exitperson)
				{
					PersonName=person;
				}
				
			}
			Sex(dsrmz);
			brith(dsrmz);
			number(dsrmz);
			address(dsrmz);
			office(dsrmz);
//			System.out.println("PersonName:"+PersonName);
			
			PersonName=PersonName.replace("(", "（").replace(")", "）");
			if(PersonName.contains("公司（") && !PersonName.endsWith("（有限合伙）"))
			{
				PersonName=PersonName.substring(0,PersonName.lastIndexOf("（"));
			}
			Matcher m1 = pattern1.matcher(PersonName);
			if(m1.find())
			{
				String s=m1.group();
				PersonName=PersonName.substring(0,PersonName.length()-s.length());
			}
			company(PersonName,PersonIdentity);
			
			JSONObject json=new JSONObject();
			
			json.put("PersonName", PersonName);
			json.put("PersonSex", PersonSex);
			json.put("PersonBirthday", PersonBirthday);
			json.put("PersonCard", PersonCard);
			json.put("PersonAddress", PersonAddress);
			json.put("PersonIdentity", PersonIdentity);
			json.put("PersonOffice", PersonOffice);
			arr.add(json);
			JSONObject js=new JSONObject();
			js.put("PersonName", PersonName);
			name.add(js);
		}			
//			System.out.println(arr);
			
			namestr=name.toString();
			personstr=arr.toString();

	}	
	
	/**
	 * 解析性别
	 * @param dsrmz
	 */
	public static void Sex(String dsrmz)
	{				
		int nan=0,nv=0,xingbie=0;
		nan=dsrmz.indexOf("男");
		nv=dsrmz.indexOf("女");
		xingbie=Math.max(nan, nv);
		if (xingbie>-1)
		{
			PersonSex=dsrmz.substring(xingbie, xingbie+1);
//			System.out.println("PersonSex:"+PersonSex);
		}		
	}
	
	/**
	 * 解析出生日期
	 * @param dsrmz
	 */
	public static void brith(String dsrmz)
	{
		Pattern patt = Pattern.compile("(\\d+)(年)(\\d+)(月)(\\d+)(日)");
		Matcher matt=patt.matcher(dsrmz);
		while(matt.find())
		{
			PersonBirthday=matt.group();
//			System.out.println("PersonBirthday:"+PersonBirthday);
			
		}	
	}
	
	/**
	 * 解析证件号码
	 * @param dsrmz
	 */
	public static void number(String dsrmz)
	{		
		int sfzhmqs=0,sfzhmjw=0;
		sfzhmqs=dsrmz.indexOf("身份证号");
		if(sfzhmqs>-1)
		{
			sfzhmjw=dsrmz.indexOf(',', sfzhmqs);
			if(sfzhmjw==-1)
			{
				sfzhmjw=dsrmz.length();
			}
			String identityCard=dsrmz.substring(sfzhmqs, sfzhmjw);
			Pattern patid = Pattern.compile("([(x)(X)(\\d+)(*)]+)");
			Matcher maid=patid.matcher(identityCard);
			while(maid.find())
			{
				PersonCard=maid.group();
//				System.out.println("PersonCard:"+PersonCard);
				
			}
		}		
	}
	
	/**
	 * 解析地址
	 * @param dsrmz
	 */
	public static void address(String dsrmz)
	{

		
		String[] zhuzhiArr={"住所地","所住地","居住地","住所地在"	,"住","住址","住所","住所在",
				"住所地为","住所地位于","现住","住房"	,"居住",	"户籍所在地"};					

		HashMap<String,Integer> zhuzhiMap=new HashMap<String,Integer>();
		for(String zhuzhi:zhuzhiArr)
		{
			zhuzhiMap.put(zhuzhi, dsrmz.indexOf(zhuzhi));
		}
		if(zhuzhiMap.get("住所地")==zhuzhiMap.get("住所地为"))
		{
			zhuzhiMap.put("住所地",-1);
		}
		if(zhuzhiMap.get("住所")==zhuzhiMap.get("住所地"))
		{
			zhuzhiMap.put("住所",-1);
		}
		if(zhuzhiMap.get("住")==zhuzhiMap.get("住所地"))
		{
			zhuzhiMap.put("住",-1);
		}
		if(zhuzhiMap.get("住所")==zhuzhiMap.get("住所地为"))
		{
			zhuzhiMap.put("住所",-1);
		}
		if(zhuzhiMap.get("住")==zhuzhiMap.get("住所地为"))
		{
			zhuzhiMap.put("住",-1);
		}
		if(zhuzhiMap.get("住所地")==zhuzhiMap.get("住所地位于"))
		{
			zhuzhiMap.put("住所地",-1);
		}
		if(zhuzhiMap.get("住所")==zhuzhiMap.get("住所地位于"))
		{
			zhuzhiMap.put("住所",-1);
		}
		if(zhuzhiMap.get("住")==zhuzhiMap.get("住所地位于"))
		{
			zhuzhiMap.put("住",-1);
		}
		if(zhuzhiMap.get("住所地")==zhuzhiMap.get("住所地在"))
		{
			zhuzhiMap.put("住所地",-1);
		}
		if(zhuzhiMap.get("住所")==zhuzhiMap.get("住所地在"))
		{
			zhuzhiMap.put("住所",-1);
		}
		if(zhuzhiMap.get("住所")==zhuzhiMap.get("住所地在"))
		{
			zhuzhiMap.put("住所",-1);
		}
		if(zhuzhiMap.get("住")==zhuzhiMap.get("住所地在"))
		{
			zhuzhiMap.put("住",-1);
		}
		if(zhuzhiMap.get("住所")==zhuzhiMap.get("住所在"))
		{
			zhuzhiMap.put("住所",-1);
		}
		if(zhuzhiMap.get("住")==zhuzhiMap.get("住所在"))
		{
			zhuzhiMap.put("住",-1);
		}
		if(zhuzhiMap.get("居住地")==zhuzhiMap.get("居住"))
		{
			zhuzhiMap.put("居住",-1);
		}
		if(zhuzhiMap.get("住址")==zhuzhiMap.get("住"))
		{
			zhuzhiMap.put("住",-1);
		}
		if(zhuzhiMap.get("住所")==zhuzhiMap.get("住"))
		{
			zhuzhiMap.put("住",-1);
		}
		if(zhuzhiMap.get("住房")==zhuzhiMap.get("住"))
		{
			zhuzhiMap.put("住",-1);
		}
		
		Iterator<Entry<String, Integer>> iter= zhuzhiMap.entrySet().iterator();
		
		int zZmax=-1;
		String zZmaxStr=null;
		
		boolean existszZmax=false;
		
		while(iter.hasNext())
		{
			Entry<String, Integer> ent = iter.next();
			String keyzZ=ent.getKey();
			int value=ent.getValue();
			
			if(value>zZmax && value>=0)
			{
				existszZmax=true;
				zZmaxStr=keyzZ;
				zZmax=value;
			}

		}
		
		int zzqs=0,zzjw=0;
		if(existszZmax)
		{
			zzqs=dsrmz.indexOf(":", zZmax);
			
			if(zzqs>0 && zzqs==zZmax+zZmaxStr.length())
			{
				zzqs=zzqs+1;
			}
			else
			{
				zzqs=zZmax+zZmaxStr.length();
			}
			zzjw=dsrmz.indexOf(",", zzqs);
			if(zzjw>0)
			{
				
			}
			else
			{
				zzjw=dsrmz.length();
			}

			PersonAddress=dsrmz.substring(zzqs, zzjw);
//			System.out.println("PersonAddress:"+PersonAddress);
			
		}

	}		

	/**
	 *  解析职务
	 * @param dsrmz
	 */
	public static void office(String dsrmz)
	{
		String[] zhiwuArr={"董事长",	"总经理"	,"副总经理",	"经理",	"律师",	"实习律师",	"律师事务所","无固定职业"	,"无职业",
				"公司负责人","法务","员工","职员","农民","退休干部","法律工作者",	"厂长","股东"};					

		HashMap<String,Integer> zhiwuMap=new HashMap<String,Integer>();
		for(String zhiwu:zhiwuArr)
		{
			zhiwuMap.put(zhiwu, dsrmz.indexOf(zhiwu));
		}	

		zhiwuMap.put("律师事务所",-1);
		Iterator<Entry<String, Integer>> iteratorZZ = zhiwuMap.entrySet().iterator();
		
		int zhiWUmin=Integer.MAX_VALUE;
		String zhiWUminStr=null;

		
		while(iteratorZZ.hasNext())
		{
			Entry<String, Integer> entryZZ = iteratorZZ.next();
			String keyZZ=entryZZ.getKey();
			int valZZ=entryZZ.getValue();
			
			if(valZZ<zhiWUmin && valZZ>=0)
			{
				zhiWUminStr=keyZZ;
				zhiWUmin=valZZ;
				PersonOffice=zhiWUminStr;
//				System.out.println("PersonOffice:"+PersonOffice);
			}
		}
	}
	/**
	 * 判断当事人是否为公司
	 * @param PersonName 当事人信息
	 * @param PersonIdentity 当事人属性（原告、被告...）
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static void company(String PersonName,String PersonIdentity)throws IOException,ClassNotFoundException,SQLException
	{
//		System.out.println("PersonName:"+PersonName);
		int jituan=0,gongsi = 0, Chang = 0, she = 0, zhongxin = 0, dian = 0, zhan = 0, suo = 0, binguan = 0, jia = 0, bu = 0,hang=0;
		jituan = PersonName.indexOf("集团");
		gongsi = PersonName.indexOf("公司");
        Chang = PersonName.indexOf("厂");
        she = PersonName.indexOf("社");
        zhongxin = PersonName.indexOf("中心");
        dian = PersonName.indexOf("店");
        zhan =PersonName.indexOf("站");
        suo = PersonName.indexOf("所");
        binguan = PersonName.indexOf("宾馆");
        jia = PersonName.indexOf("家");
        bu = PersonName.indexOf("部");
        hang = PersonName.indexOf("行");
        int[] cn = new int[] { jituan,gongsi, Chang, she, zhongxin, dian, zhan, suo, binguan, jia, bu ,hang};

        int temp = cn[0];
        for (int r = 0; r < cn.length; r++)
        {
            if (temp < cn[r])
            {
                temp = cn[r];
            }
        }
        
        if(temp>-1 && PersonName.length()<100)
        {
        	PersonName=PersonName.replace("(", "（").replace(")", "）");
    		String insertCom=String.format("insert into [dbo].[JudicialIndentity] values('%s','%s','%s',getDate())",docId,PersonName,PersonIdentity);		
//    		System.out.println(insertCom);
    		MSSQL.executeUpdate(insertCom);
        }
	}
	
	/**
	 * 判决结果
	 * @param doc 文书全文
	 */
	public static void judgecontent(String doc)
	{
//		int gaiyao=0;
		int panjueruxia=0,caidingruxia=0,pandingruxia=0,zhifuling=0,falvtiaowen = 0, falvtiaokuan = 0, panjueweizi=0, fufalvtiaowen = 0, xiangguanfalvtiaowen = 0,jisuanfangshi=0;		

		panjueruxia=doc.lastIndexOf("判决如下");
		caidingruxia=doc.lastIndexOf("裁定如下");
		pandingruxia=doc.lastIndexOf("判定如下");
		zhifuling=doc.lastIndexOf("如下支付令");
		falvtiaowen=doc.lastIndexOf("附相关法律法规条文");
		falvtiaokuan=doc.lastIndexOf("本案援引法律条款");
		fufalvtiaowen=doc.lastIndexOf("附：法律条文");
		xiangguanfalvtiaowen=doc.lastIndexOf("附：相关法律条文");
		jisuanfangshi=doc.lastIndexOf("附：本金利息计算方式");
//		gaiyao=doc.indexOf("概要");
		
//		判决起始位置
		int[] panjuekaishi={panjueruxia,caidingruxia,pandingruxia,zhifuling};
		int maxpjks=panjuekaishi[0];
		int pjks=0;
		for (pjks=0;pjks<panjuekaishi.length;pjks++)
		{
			if (maxpjks<panjuekaishi[pjks])
				maxpjks=panjuekaishi[pjks];
		}		
		int panjueqishi=maxpjks;
//		判决结束位置		
		int[] panjuejiewei=new int[]{falvtiaowen,falvtiaokuan,fufalvtiaowen,xiangguanfalvtiaowen,jisuanfangshi};
		int maxpjjw=panjuejiewei[0];
		int pjjw=0;
		for (pjjw=0;pjjw<panjuejiewei.length;pjjw++)
		{
			if (maxpjjw<panjuejiewei[pjjw])
				maxpjjw=panjuejiewei[pjjw];
		}
		if (maxpjjw>0)
		{
			panjueweizi=maxpjjw;
		}
		else
		{
			panjueweizi=doc.length();
		}
	
		 panjuestr="";
		if(panjueqishi>-1 && panjueqishi<panjueweizi)
			
		{
		panjuestr=doc.substring(panjueqishi,panjueweizi);
			
		}

		RelatedAmount(panjuestr);

	}

	/**
	 * 涉案金额
	 * @param panjuestr 判决结果
	 */
	public static void RelatedAmount(String panjuestr)
	{
//		System.out.("判决内容:"+panjuestr);
		double money=0;
		panjuestr=panjuestr.replace("，", ",");
		panjuestr=panjuestr.replace("..", ".");
		Pattern pat = Pattern.compile("([\\u4e00-\\u9fa5]+)([(\\d+)(壹贰叁肆伍陆柒捌玖零兆万仟佰拾元角分圆整正负圓萬千百一二三四五六七八九十〇)(.)(,)]+)(元)");
//		Pattern pat = Pattern.compile("([(\\u4e00-\\u9fa5)，,，、（）(\\d+)]+)(\\d+)(\\.\\d+)(元$)|([(\\u4e00-\\u9fa5)(，)(，)(,)(，)(，)(（)(）)(\\d+)]+)(元$)|([(\\u4e00-\\u9fa5)(，)(，)(,)(，)(，)(（)(）)(\\d+)]+)([万千美])(元)|([(\\u4e00-\\u9fa5)(，)(，)(,)(，)(、)(（)(）)(\\d+)]+)(\\d+)(\\.\\d+)([万千美])(元)|([(\\u4e00-\\u9fa5)(，)(，)(,)(，)(、)(（)(）)(\\d+)]+)([壹贰叁肆伍陆柒捌玖零兆万仟佰拾元角分圆整正负圓萬千百一二三四五六七八九十〇])(元)([壹贰叁肆伍陆柒捌玖零兆万仟佰拾元角分圆整正负圓萬千百一二三四五六七八九十〇])([角分])|([(\\u4e00-\\u9fa5)(，)(，)(,)(，)(、)(（)(）)(\\d+)]+)([壹贰叁肆伍陆柒捌玖零兆万仟佰拾元角分圆整正负圓萬千百一二三四五六七八九十〇])(元)|([(\\u4e00-\\u9fa5)(，)(，)(,)(，)(、)(（)(）)(\\d+)]+)([壹贰叁肆伍陆柒捌玖零亿兆万仟佰拾元角分圆整正负圓萬千百一二三四五六七八九十〇])([亿万千])(元)");
		Matcher mat=pat.matcher(panjuestr);
		List<String> resultAmount=new ArrayList<String>();
		List<Double> amount=new ArrayList<Double>();
		ArrayList<JSONObject> related=new ArrayList<JSONObject>();
		
		while(mat.find())
		{
			String feiyong=mat.group();
			
			int shoulifei=0,susongfei=0,jianban=0;
			shoulifei=feiyong.indexOf("受理费");
			susongfei=feiyong.indexOf("诉讼费");
			jianban=feiyong.indexOf("减半收取");
			if(shoulifei==-1 && susongfei==-1 && jianban==-1)
			{
				JSONObject am=new JSONObject();
				resultAmount.add(mat.group());
							
				am.put("AmountItem", mat.group());			
				related.add(am);				
			}

		}
		
		relatedAmountstr=related.toString();
//		System.out.println(resultAmount.size());
		for(String res:resultAmount)
		{
			money=GetAmountMoney(res);
			amount.add(money);
		}
		
//		System.out.println(amount);
		
		for (int m=0;m<amount.size();m++)
		{
			if(amount.get(m)>maxmoney)
			{
				maxmoney=amount.get(m);
			}
		}
		
	}
	
	/**
	 * 最大金额
	 * @param resultAmount 涉及到金额的字符串
	 * @return 解析出来的金额
	 */
	public static Double GetAmountMoney(String resultAmount)
	{
		double money=0;
//		System.out.println(resultAmount);
		resultAmount=resultAmount.replace(",", "");
		String[] moneyList=resultAmount.split("元");
		boolean isArabicNumber = true;
//		List<Double> moneyStack = new ArrayList<Double>();
			
		StringBuilder stack=new StringBuilder();
		for(String m:moneyList)
		{
			int s=0;

			s=m.lastIndexOf(".");

			if(s==m.length()-1 && s>0)
			{
				m=m.substring(0, m.length()-1);
			}

			for(int i=m.length()-1;i>=0;i--)
			{
				char c=m.charAt(i);
				if (CheckArabicNumber(c))
				{
					
				}
				else if(CheckChineseNumber(c))
				{
					isArabicNumber = false;
					
				}
				else
				{
					break;
				}
				
				stack.append(c);
			}
			stack=stack.reverse();
			
			int point=0;
			for(int j=0;j<stack.length()-1;j++)
			{
				char ch=stack.charAt(j);
				
				if(ch==46)
				{
					point++;
				}
			}
//			System.out.println(point);
			if(point>1 && point<3)
			{
				int dian=stack.indexOf(".");
				stack=stack.deleteCharAt(dian);
			}
//			System.out.println(stack);
			
			if(stack.length()>0 && point<3)
			{
				if (isArabicNumber)
				{
					money=Double.valueOf(stack.toString()).doubleValue();
				}
				else
				{
					money=DigitConvert.cnNumericToArabic(stack.toString(),true);
				}				
			}

		}	
		return money;
	}

	/**
	 * 判断是否为大写数字
	 * @param c
	 * @return
	 */
	public static boolean CheckChineseNumber(char c)
    {
        switch (c)
        {
            case '零':
            case '一':
            case '二':
            case '三':
            case '四':
            case '五':
            case '六':
            case '七':
            case '八':
            case '九':
            case '十':
            case '百':
            case '千':
            case '万':
            case '亿':
                return true;
            default:
                return false;
        }
    }
    
	/**
	 * 判断是否为阿拉伯数字
	 * @param c
	 * @return
	 */
    public static boolean CheckArabicNumber(char c)
    {
        if (c == 46 || (c >= 48 && c <= 57))
        {
            return true;
        }
        return false;
    }
    
    /**
     * 解析败诉方
     * @param panjuestr 判决结果
     * @param namestr 当事人列表
     */
    public static void losingParty(String panjuestr,String namestr)
    {
    	panjuestr=panjuestr.replace("承担", "负担");
    	int gongsujiguan=panjuestr.indexOf("公诉机关");
    	int mianxingshichufa=panjuestr.indexOf("免刑事处罚");
    	int wuzuidangtingshifang=panjuestr.indexOf("无罪当庭释放");
    	int fudan=panjuestr.lastIndexOf("负担");
    	int anjianshoulifei=panjuestr.indexOf("受理费");

    	String newpanjuestr=null;
    	if (gongsujiguan>=0)
    	{
    		if(mianxingshichufa==-1 && wuzuidangtingshifang==-1)
    		{
    			newpanjuestr=panjuestr;
    		}
    	}
//    	System.out.println(namestr);
    	if(anjianshoulifei>0 && fudan-anjianshoulifei>0 && namestr !=null)
    	{
    		newpanjuestr=panjuestr.substring(anjianshoulifei, fudan);
//    		System.out.println(namestr);
    		namestr=namestr.replace(":", "");
    		namestr=namestr.replace("[", "");
    		namestr=namestr.replace("]", "");
    		namestr=namestr.replace("{", "");
    		namestr=namestr.replace("}", "");
    		namestr=namestr.replace("\"", "");
    		namestr=namestr.replace(" ", "");
    		namestr=namestr.replace("PersonName", "");
    		String tempstr=null;
    		String[] nameList=namestr.split(",");
    		ArrayList<JSONObject> loseList=new ArrayList<JSONObject>();
			for(String personnal:nameList)
			{
				JSONObject lose=new JSONObject();
				int index=newpanjuestr.indexOf(personnal);
				
				if (index>-1)
				{
					tempstr=personnal;
					lose.put("loser", tempstr);
					loseList.add(lose);
				}
			}
//			System.out.println("loseList:"+loseList);
			losingParty=loseList.toString();
    	}
    	
    }

	public static void parse(String doc,String id,String dangshiren) throws ClassNotFoundException, IOException, SQLException
	{	
		maxmoney=0;
		namestr=null;
		personstr=null;
		panjuestr=null;
		content=null;
		losingParty=null;
		litigationType=null;
		instrumentType=null;
		documentType=null;
		relatedAmountstr=null;
		docId=id;
		PersonName="";
		PersonSex="";
		PersonBirthday="";
		PersonCard="";
		PersonAddress="";
		PersonIdentity="";
		PersonOffice="";
		normal=true;
		
//		maxmoney=0;           
//		namestr=null;         
//		personstr=null;       
//		panjuestr=null;       
//		content=null;         
//		losingParty=null;     
//		litigationType=null;  
//		instrumentType=null;  
//		documentType=null;    
//		relatedAmountstr=null;
//		normal=true;
//		docId=id;           
//		System.out.println(docId);
//		System.out.println(doc);
		if(doc.length()>0)
		{
//			String deleteabn="delete from [dbo].Abnormal_Data where DocId='"+docId+"'";
//			MSSQL.executeUpdate(deleteabn);
//			abnormaldata(doc);
			String deleteCmd1="delete from [dbo].[JudicialInfo] where DocId='"+docId+"'";
			MSSQL.executeUpdate(deleteCmd1);
			if(normal)
			{
				judgecontent(doc);
				litigant(doc,dangshiren);
				losingParty(panjuestr,namestr);
				String insertCmd2=String.format("insert into [dbo].[JudicialInfo] values"
						+ "('%s','%s',null,'%s','%s','%s','%s','%s','%s','%s','%s','%s',getDate())"
						,documentType,litigationType,instrumentType,maxmoney,personstr,
						relatedAmountstr,panjuestr,content,losingParty,namestr,docId);	
//				System.out.println(insertCmd2);
				MSSQL.executeUpdate(insertCmd2);
							
			}
			else
			{
				String insertCmd3=String.format("insert into [dbo].[JudicialInfo] values"
						+ "(null,null,null,null,null,null,null,null,null,null,'****','%s',getDate())"
						,docId);	
				MSSQL.executeUpdate(insertCmd3);
				
			}
			MSSQL.commit();			
		}

	}
	
	public static void test() throws IOException, ClassNotFoundException, SQLException
	{
		File src=new File("data");
		File[] file=src.listFiles();
		for(File f:file)
		{

			String docId=f.getName().replace(".txt", "");
//			System.out.println(docId);
			InputStreamReader reader = new InputStreamReader(new FileInputStream(f));
	        char[] content=new char[(int) f.length()];
	        reader.read(content);
	        reader.close();
	        String doc=new String(content);
	        parse(doc,docId,"");
		}
	}

	public static void main(String[] args) throws IOException,ClassNotFoundException,SQLException
	{
		File src=new File("data/test1");
		FileInputStream reader = new FileInputStream(src);
		int l=(int) src.length();
		byte[] content=new byte[l];
		reader.read(content);
		reader.close();
		String c=new String(content);
//		System.out.println(c);
		parse(c, "21f8de53-9f59-42c2-9bcb-a224bb7ac589", "山起重型机械股份公司,东营市东辰节能电力设备有限公司");
	}
}
