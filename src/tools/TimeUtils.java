package tools;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeUtils {
	
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String getNow()
	{
		return sdf2.format(new Date());
	}
	
	public static String getToday()
	{
		return sdf.format(new Date());
	}
	
	public static String getYesterday()
	{
		Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE,-1);
		return sdf.format(calendar.getTime());
	}
	
	public static String getTomorrow()
	{
		Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DATE,1);
		return sdf.format(calendar.getTime());
	}
	
	public static int dateDiff(String startDt,String stopDt) throws ParseException
	{
		Date start = sdf.parse(startDt);
		Date stop=sdf.parse(stopDt);
		
		return (int) ((stop.getTime()-start.getTime())/(1000*3600*24));
	}
	
	public static String[] splitDateInterval(String startDt,String stopDt) throws ParseException
	{
		if(dateDiff(startDt,stopDt)<=1)
		{
			return new String[]{startDt,stopDt};
		}
//		if(startDt.equals(stopDt))
//		{
//			return new String[]{startDt,stopDt};
//		}
		int diff=dateDiff(startDt,stopDt);
		Date start = sdf.parse(startDt);
		Calendar calendar = new GregorianCalendar();
        calendar.setTime(start);
        calendar.add(Calendar.DATE, diff/2);
        String middleDt=sdf.format(calendar.getTime());
//        String newStopDt=sdf.format(calendar.getTime());
//        calendar.add(Calendar.DATE, 1);
//        String newStartDt=sdf.format(calendar.getTime());
//        return new String[]{startDt,newStopDt,newStartDt,stopDt};
        return new String[]{startDt,middleDt,middleDt,stopDt};
	}
	
	public static void main(String[] args) throws ParseException
	{
		System.out.println(getToday());
		System.out.println(getYesterday());
		System.out.println(getTomorrow());
		System.out.println(Arrays.toString(splitDateInterval("2015-05-21","2015-05-24")));
		System.out.println(getNow());
	}
	
}
