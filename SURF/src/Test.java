import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import surf.demo.NILMMetadata_BLUED;


public class Test {

	public static void main(String[] args) {
		
		try {
			System.out.println(NILMMetadata_BLUED.getRawDataFromFile("assets/dataset.yaml"));
			System.out.println(NILMMetadata_BLUED.getRawDataFromFile("assets/building1.yaml"));
			
			
			/*System.out.println(NILMMetadata_BLUED.getDatasetMetadataFromFile());
			System.out.println("");
			System.out.println(NILMMetadata_BLUED.getBuildingMetadataFromFile());
			System.out.println("");
			System.out.println(NILMMetadata_BLUED.getMeterDevicesMetadataFromFile());*/
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// TODO Auto-generated method stub
		
		long ts = 1387985892286L;
		
		long ts_blued = 1319111912623L;
		
		long ts_blued2 = 1319108312623L;
		
		long ts_blued3 = ts_blued2 + 5*60*60*1000;
		
		long ts_blued4 = 1319108312623L;
		
		long ts_blued5 = 1319111912623L;
		
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(ts);
		
		System.out.println(c.getTime());
		
		
		c.setTimeInMillis(ts_blued);
		
		System.out.println(c.getTime());
		
		c.setTimeInMillis(ts_blued2);
		
		System.out.println(c.getTime());
		
		c.setTimeInMillis(ts_blued3);
		
		System.out.println(c.getTime());
		
		c.setTimeInMillis(ts_blued4);
		
		System.out.println(c.getTime());
		
		c.setTimeInMillis(ts_blued5);
		
		System.out.println(c.getTime());
		/*
		System.out.println(Calendar.getInstance().getTimeInMillis());
		System.out.println(Calendar.getInstance().getTime().toString());
		System.out.println(Calendar.getInstance().getTimeZone().getID());
		
		TimeZone timeZone1 = TimeZone.getTimeZone("America/Los_Angeles");
		Calendar cal = Calendar.getInstance(timeZone1);
		System.out.println(cal.getTimeInMillis());
		System.out.println(cal.getTime().toString());
		System.out.println("hour     = " + cal.get(Calendar.HOUR_OF_DAY));
		*/
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		System.out.println(df.format(Calendar.getInstance().getTime()));
		
		System.out.println(Calendar.getInstance().getTimeZone().getID());
		
	}

}
