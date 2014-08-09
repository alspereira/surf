package surf.demo;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import javax.swing.JFileChooser;

import surf.demo.util.PowerCalculator;
import surf.demo.util.SURFPowerReader;
import surf.demo.view.PowerChart;
import surf.file.Annotation;
import surf.file.Info;
import surf.file.Marker;
import surf.file.Region;
import surf.file.SURFFile;
import surf.file.SURFFileDescr;

public class SURFRead {

	static String			file_IN_path_A = "assets/BLUED_PhaseA_DayOne_surf.wav";
	static String			file_IN_path_B = "assets/BLUED_PhaseB_DayOne_surf.wav";
	
	static File				file_IN = new File(file_IN_path_A);
	
	static SURFFileDescr 	SURF_descr_IN;
	static SURFFile 		SURF_file_IN;
	
	static void printMenu(String file_path) {
		String menu = "\n\tSelect an option \n\n"
					+ "\tA - Appliance Activities (aka Labels) \n"
					+ "\tU - User Activities (aka Regions) \n"
					+ "\tN - Localized Metadata (aka Notes) \n"
					+ "\tC - Comments \n"
					+ "\tM - Metadata \n"
					+ "\tI - Info \n"
					+ "\tF - Config \n"
					+ "\tP - Power \n"
		+ "\tQ - Quit";
		System.out.println( "\nBrowsing: " + file_path + "\n" + menu );
		System.out.print("->");
	}
	
	static ArrayList<Marker> getLabels() {
		ArrayList<Marker> labels = (ArrayList<Marker>) SURF_descr_IN.getProperty(SURFFileDescr.KEY_LABELS);
		return labels;
	}
	
	static ArrayList<Marker> getNotes() {
		ArrayList<Marker> notes = (ArrayList<Marker>) SURF_descr_IN.getProperty(SURFFileDescr.KEY_NOTES);
		return notes;
	}
	
	static ArrayList<Region> getRegions() {
		ArrayList<Region> regions = (ArrayList<Region>) SURF_descr_IN.getProperty(SURFFileDescr.KEY_REGIONS);
		return regions;
	}
	
	static ArrayList<Annotation> getComments() {
		ArrayList<Annotation> comments = (ArrayList<Annotation>) SURF_descr_IN.getProperty(SURFFileDescr.KEY_COMMENTS);
		return comments;
	}
	
	static ArrayList<Annotation> getMetadata() {
		ArrayList<Annotation> metadata = (ArrayList<Annotation>) SURF_descr_IN.getProperty(SURFFileDescr.KEY_METADATA);
		return metadata;
	}
	
	static void printInfoFields() {
		Info inf = (Info) SURF_descr_IN.getProperty(SURFFileDescr.KEY_INFO);
		System.out.println(inf.toString());
	}
	
	static void printConfigFields() {
		String config = "RIFF DEFAULT" + 
						"\n-Channels: " + SURF_descr_IN.channels +
						"\n-Rate: " + SURF_descr_IN.rate +
						"\n-BitsPerSample: " + SURF_descr_IN.bitsPerSample +
						"\nSURF SPECIFIC" +
						"\n-InitialTimestamp: " + SURF_descr_IN.SURF_initial_timestamp +
						"\n-Timezone: " + SURF_descr_IN.SURF_timezone +
						"\n-Rate: " + SURF_descr_IN.SURF_sample_rate;
		System.out.println(config);
	}
	
	static void print(ArrayList<?> list) {
		if(list == null)
			System.out.println("None available");
		else {
			Iterator<?> it = list.iterator();
			while(it.hasNext()) {
				Object obj = it.next();
				if( obj instanceof Marker ) // Labels and notes
					System.out.println( ((Marker) obj).name );
				else if( obj instanceof Region ) // Regions
					System.out.println( ((Region) obj).name );
				else if( obj instanceof Annotation ) // Comments and metadata
					System.out.println( ((Annotation) obj).content + "\n");
			}
		}
	}
	
	public static void main(String[] args) {
		
		if(args.length > 0) {
			if(args[0] == "A")
				file_IN = new File(file_IN_path_A);
			else if(args[0] == "B")
				file_IN = new File(file_IN_path_B);
		}
		
		try {
			SURF_file_IN 	= SURFFile.openAsRead(file_IN);
			SURF_descr_IN 	= SURF_file_IN.getDescr();	
			SURF_file_IN.readMarkers();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		String file_path	= file_IN.getPath();
		
		boolean mustQuit 	= false;
		Scanner textScanner = new Scanner(System.in);
		String 	inputString = "";
		
		printMenu(file_path);
		
		while (mustQuit == false) {
		
			inputString = textScanner.nextLine();
			
			switch( inputString.toUpperCase() ) {
			case "A":
				print(getLabels());
				printMenu(file_path);
				break;
			case "U":
				print(getRegions());
				printMenu(file_path);
				break;
			case "N":
				print(getNotes());
				printMenu(file_path);
				break;
			case "C":
				print(getComments());
				printMenu(file_path);
				break;
			case "M":
				print(getMetadata());
				printMenu(file_path);
				break;
			case "F":
				printConfigFields();
				printMenu(file_path);
				break;
			case "I":
				printInfoFields();
				printMenu(file_path);
				break;
			case "P":
				new ReadPowerAux();
				System.out.println("->");
				break;
			case "Q":
				mustQuit = true;
				break;
			default:
				break;
			}
				
		}
		System.out.println("Goodbye");
		textScanner.close();
		System.exit(0);

     }
	
	static class ReadPowerAux {
		private SURFPowerReader powerReader;
		private PowerCalculator powerCalculator;
		private PowerChart		powerChart;
		
		private long			timestamp;
		private String			datetime;
		
		private  SimpleDateFormat dateFormat 	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		
		private float[] calibrationConstants;
		
		public ReadPowerAux() {
			
			try {
				powerReader = new SURFPowerReader(file_IN);		
				
				datetime = SURF_file_IN.getDescr().SURF_initial_timestamp;
				calibrationConstants = SURF_file_IN.getDescr().SURF_channel_calibration;
				timestamp = dateFormat.parse(datetime).getTime();
				
				powerCalculator = new PowerCalculator(timestamp);
				powerCalculator.setCalibatrionConstants(calibrationConstants);
				powerCalculator.setChannelSamples(powerReader.getAudioDataQueue());
				
				powerChart = new PowerChart(5000);
				powerChart.setPowerSamplesQueue(powerCalculator.getPowerSamplesQueue());
				
				// start all this crap
				
				new Thread(powerChart).start();
				new Thread(powerCalculator).start();
				new Thread(powerReader).start();	
				
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			
		}
	}
}

