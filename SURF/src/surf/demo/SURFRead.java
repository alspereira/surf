package surf.demo;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Scanner;

import javax.swing.JFileChooser;

import surf.file.Annotation;
import surf.file.Info;
import surf.file.Marker;
import surf.file.Region;
import surf.file.SURFFile;
import surf.file.SURFFileDescr;

public class SURFRead {

	//static File				file_IN = new File("assets/BLUED_PhaseA_DayONE_wave.wav");
	static File				file_IN = new File("assets/BLUED_PhaseA_surf.wav");
	static SURFFileDescr 	SURF_descr_IN;
	static SURFFile 		SURF_file_IN;
	
	static void printMenu() {
		String menu = "\nSelect an option \n\n"
					+ "A - Appliance Activities (aka Labels) \n"
					+ "U - User Activities (aka Regions) \n"
					+ "N - Localized Metadata (aka Notes) \n"
					+ "C - Comments \n"
					+ "M - Metadata \n"
					+ "I - Info \n"
					+ "F - Config \n"
					+ "P - Power \n"
					+ "Q - Quit";
		System.out.println( menu );
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
		
		try {
			SURF_file_IN 	= SURFFile.openAsRead(file_IN);
			SURF_descr_IN 	= SURF_file_IN.getDescr();	
			SURF_file_IN.readMarkers();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		boolean mustQuit 	= false;
		Scanner textScanner = new Scanner(System.in);
		String 	inputString = "";
		
		printMenu();
		while (mustQuit == false) {
		
			inputString = textScanner.nextLine();
			
			switch( inputString.toUpperCase() ) {
			case "A":
				print(getLabels());
				printMenu();
				break;
			case "U":
				print(getRegions());
				printMenu();
				break;
			case "N":
				print(getNotes());
				printMenu();
				break;
			case "C":
				print(getComments());
				printMenu();
				break;
			case "M":
				print(getMetadata());
				printMenu();
				break;
			case "F":
				printConfigFields();
				printMenu();
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
}