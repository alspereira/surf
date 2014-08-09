package surf.demo;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

import surf.demo.service.LoadBLUEDGroundTruthLabelsJob;
import surf.demo.util.NILMMetadata_BLUED;
import surf.file.Annotation;
import surf.file.Info;
import surf.file.Marker;
import surf.file.Region;
import surf.file.SURFFile;
import surf.file.SURFFileDescr;

import com.almworks.sqlite4java.SQLiteQueue;

public class SURFTag {

	private static File database_file = new File("assets/BLUED.db");
	private static SQLiteQueue sqliteQueue = new SQLiteQueue(database_file);
	
	private static File file_IN = new File("/users/lucaspereira/desktop/BLUED_PhaseA_wave.wav");
	private static File file_OUT;
	
	private static SURFFile SURF_file_IN;
	private static SURFFile SURF_file_OUT;
	
	private static SURFFileDescr SURF_descr_IN;
	private static SURFFileDescr SURF_descr_OUT;
	
	private static List<Marker> labels 			= new ArrayList<Marker>();
	private static List<Marker> notes 			= new ArrayList<Marker>();
	private static List<Region> regions			= new ArrayList<Region>();
	
	private static List<Annotation> metadata 	= new ArrayList<Annotation>();
	private static List<Annotation> comments	= new ArrayList<Annotation>();
	
	private static Info	SURF_info		= new Info();
	
	private static DecimalFormat df 			= new DecimalFormat("#.###");
	private static SimpleDateFormat dateFormat 	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	// ------ Methods -----
	
	private static String Label2JSON(surf.demo.model.BLUEDGroundTruthLabelDTO label) {
		Map<String, Object> obj = new LinkedHashMap<String, Object>();
		obj.put("SURF_ID", label.id);
		obj.put("App_ID", new Integer(label.appliance_id));
		obj.put("App_Label", label.appliance_label);
		obj.put("Position", new Long(label.position));
		obj.put("Timestamp", label.timestamp);
		obj.put("Delta_P", df.format(new Double(label.delta_P)));
		obj.put("Delta_Q", df.format(new Double(label.delta_Q)));
		obj.put("Type", new Integer((label.delta_P > 0)? 1 : -1));
		return JSONValue.toJSONString(obj);
	}
	
	public static void main(String[] args) {
		
		file_OUT = new File( "/users/lucaspereira/desktop/" + Calendar.getInstance().getTimeInMillis() + "_" + file_IN.getName() );
		
		// Start the sqlite jobs execution queue
		sqliteQueue.start();
		
		// Load all the labels
		surf.demo.model.BLUEDGroundTruthLabelDTO[] BLUED_labels = 
				sqliteQueue.execute(new LoadBLUEDGroundTruthLabelsJob<surf.demo.model.BLUEDGroundTruthLabelDTO[]>
				("phase = 'A'")).complete();
		
		// For each label create a marker
		for(int i = 0; i < BLUED_labels.length; i++)
			Marker.add(labels, new Marker( BLUED_labels[i].position, Label2JSON( BLUED_labels[i] ) ) );
		
		try {
			SURF_file_IN 	= SURFFile.openAsRead(file_IN);
			SURF_descr_IN 	= SURF_file_IN.getDescr();
			
			SURF_descr_OUT 	= new SURFFileDescr();
			
			SURF_descr_OUT.type 		= SURF_descr_IN.type;
			SURF_descr_OUT.sampleFormat = SURF_descr_IN.sampleFormat;
			SURF_descr_OUT.channels 	= SURF_descr_IN.channels;
			SURF_descr_OUT.rate 		= SURF_descr_IN.rate;
			SURF_descr_OUT.bitsPerSample= SURF_descr_IN.bitsPerSample;
			SURF_descr_OUT.file 		= file_OUT;
			
			// SURF mandatory configuration fields
			SURF_descr_OUT.SURF_initial_timestamp 	= "2011-10-20 11:58:32.623";
			SURF_descr_OUT.SURF_timezone 			= "EST";
			SURF_descr_OUT.SURF_sample_rate 		= 60f;
			SURF_descr_OUT.SURF_channel_calibration = new float[]{19200, 19200};
			
			// SURF (optional) info chunk
			SURF_info.archival_location = "http://nilm.cmubi.org";
			SURF_info.comments 			= "This is the SURF version of the BLUED dataset phase A at 60 Hz";
			SURF_info.commissioner 		= "Mario Bergés (marioberges@cmu.edu)";
			SURF_info.copyright 		= "Inherited from the original source";
			SURF_info.creation_date 	= dateFormat.format( Calendar.getInstance().getTime() );
			SURF_info.file_creator		= "Lucas Pereira (lucas@m-iti.org)";
			SURF_info.keywords 			= "NILM, dataset, event-based";
			SURF_info.name 				= "SURF-BLUED: Phase A at 60 Hz";
			SURF_info.product 			= "Event-based NILM performance evaluation";
			SURF_info.software 			= "SURF and SURF-PI: Java v0.1";
			SURF_info.subject 			= "Phase A: Real and Reactive power at 60 Hz";
			SURF_info.source 			= "BLUED: Building-Level fUlly labeled Electricity Disaggregation Dataset";
			SURF_info.source_form 		= "Matlab (.mat)";
			
			// Add the labels to the descriptor of the SURF_file_OUT
			SURF_descr_OUT.setProperty(SURFFileDescr.KEY_LABELS, labels);
			
			// Add the info to the descriptor of the SURF_file_OUT
			SURF_descr_OUT.setProperty(SURFFileDescr.KEY_INFO, SURF_info);
			
			// Add metadata from the NILM Metadata specification (https://github.com/nilmtk/nilm_metadata)
			metadata.add( new Annotation( NILMMetadata_BLUED.getRawDatasetMetadataFromFile() ));
			metadata.add( new Annotation( NILMMetadata_BLUED.getRawMeterDevicesMetadataFromFile() ));
			metadata.add( new Annotation( NILMMetadata_BLUED.getRawBuildingMetadataFromFile() ));

			// Add the metadata to the descriptor of the SURF_file_OUT
			SURF_descr_OUT.setProperty(SURFFileDescr.KEY_METADATA, metadata);
			
			// Add some comments
			comments.add(new Annotation("The metadata schema being used is the one from the NILM metadata project"
					+ " (see: https://github.com/nilmtk/nilm_metadata)"));
			comments.add( new Annotation("The original metadata files can be found in the assets folder of the"
					+ " source code (see: http://github.com/alspereira/surf)") );
			
			// Add the comment to the descriptor of the SURF_file_OUT
			SURF_descr_OUT.setProperty(SURFFileDescr.KEY_COMMENTS, comments);
			
			SURF_file_OUT = SURFFile.openAsWrite(SURF_descr_OUT);
		
			// Copy the waveform data
			SURF_file_IN.copyFrames( SURF_file_OUT, SURF_file_IN.getFrameNum() );
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			sqliteQueue.stop(true);
		}
	}
}
