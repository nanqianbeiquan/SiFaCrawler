package tools;

public class ParseCaseNo  {

	public static String parse(String caseNo)
	{
		if(caseNo!=null)
		{
			caseNo=caseNo.replace(" ","");
			caseNo=caseNo.replace(']','）');
			caseNo=caseNo.replace('(','（');
			caseNo=caseNo.replace('[','（');
			caseNo=caseNo.replace(')','）');
			caseNo=caseNo.replace("（（","（");
			caseNo=caseNo.replace("））","）");
			caseNo=caseNo.replace("？","");
			caseNo=caseNo.replace("？","");
			if(caseNo.endsWith("）"))
			{
				caseNo=caseNo.substring(0, caseNo.length()-1);
			}
		}
		return caseNo;
	}
	public static void main(String[] args)
	{
		
	}
}
