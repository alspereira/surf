import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JFileChooser;

import surf.file.Annotation;
import surf.file.Info;
import surf.file.Marker;
import surf.file.Region;
import surf.file.SURFFile;
import surf.file.SURFFileDescr;
import surf.file.Span;

public class scissDemo {

	static String fileName = "";
	
	static SURFFileDescr descriptor;
	
	static Marker myMarker;
	
	static List<Marker> fileMarkers;
	static List<Region> fileRegions;
	static List<Marker> fileNotes;
	
	static List<Annotation> fileMetadata;
	static List<Annotation> fileComments;

	static Info info = new Info();
	
	static SURFFile myAudioFile;
	static SURFFile myWritableAudioFile;
	
	public static void main(String[] args) {
		
		// Setup the file descriptor
		descriptor = new SURFFileDescr();
		descriptor.type = SURFFileDescr.TYPE_WAVE;
		descriptor.channels = 2;
		descriptor.rate = 60;
		descriptor.bitsPerSample = 16;
		descriptor.sampleFormat = SURFFileDescr.FORMAT_INT;
		
		descriptor.SURF_initial_timestamp = "2011-10-20 11:58:32.623_";
		descriptor.SURF_timezone = "EST"; // timezone in Pittsburgh when the dataset was collected
		descriptor.SURF_sample_rate = 60f; // this was suposed to be only used when the actual rate is lower than 1!
		descriptor.SURF_channel_calibration = new float[]{19200, 19200};
		
		
		info.archival_location = "locatio_";
		info.file_creator = "Amâncio Lucas (lucas@m-iti.org)";
		info.commissioner = "Mario Berges (marioberges@cmu.edu)";
		info.comments = "Isto é um comentário";
		info.copyright = "copyrig_______";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		info.creation_date = df.format(Calendar.getInstance().getTime());
		info.creation_date = "date__________";
		info.keywords = "NILM, event-based";
		info.name = "name__________";
		info.product = "product__";
		info.subject = "subject_______";
		info.software = "softwar_______";
		info.source = "source________";
		info.source_form = ".mat (matlab)";
		
		
		// Create a markers list and add this marker there
		fileMarkers = new ArrayList<Marker>();
		fileMarkers.add(new Marker(1,"{this is a label"));	
		fileMarkers.add(new Marker(50, "{this is another label}"));
		
		fileRegions = new ArrayList<Region>();
		fileRegions.add(new Region(new Span(1, 20), "my region"));
		fileRegions.add(new Region(new Span(21, 200), "my other region"));
		
		fileNotes = new ArrayList<Marker>();
		fileNotes.add(new Marker(200,"this is a note"));
		fileNotes.add(new Marker(400, "this is another note"));
		
		fileMetadata = new ArrayList<Annotation>();
		fileMetadata.add(new Annotation("metadata Amâncio Lucas de Sousa Pereira ; €@^^^ ^^^^"));
		fileMetadata.add(new Annotation("this is my second metadata"));
		fileMetadata.add(new Annotation("more metadata"));
		
		fileComments = new ArrayList<Annotation>();
		fileComments.add(new Annotation("comment_"));
		fileComments.add(new Annotation("this is my second comment"));
		
		File file = new File(System.getProperty("user.dir"));
        JFileChooser fc = new JFileChooser(file);
        //fc.setMultiSelectionEnabled(true);
        fc.setFileFilter(new javax.swing.filechooser.FileFilter () {
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String name = f.getName();
                fileName = name;
                if (name.endsWith(".au") || name.endsWith(".wav") || name.endsWith(".aiff") || name.endsWith(".aif")) {
                    return true;
                }
                return false;
            }
            public String getDescription() {
                return ".au, .wav, .aif";
            }
        });
        
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        	File waveFile = new File("/Users/lucaspereira/Desktop/" + Calendar.getInstance().getTimeInMillis() + ".wav");
        	float[][] frames = new float[2][8000];
        	
        	descriptor.file = waveFile;
        	// open a riff stream
        	try {
        		       		
        		descriptor.setProperty(SURFFileDescr.KEY_LABELS, fileMarkers);        		
	        	descriptor.setProperty(SURFFileDescr.KEY_REGIONS, fileRegions);
	        	descriptor.setProperty(SURFFileDescr.KEY_NOTES, fileNotes);
	        	descriptor.setProperty(SURFFileDescr.KEY_COMMENTS, fileComments);
	        	descriptor.setProperty(SURFFileDescr.KEY_METADATA, fileMetadata);
	        	descriptor.setProperty(SURFFileDescr.KEY_INFO, info);
	        
        		myWritableAudioFile = SURFFile.openAsWrite(descriptor);
				
	        	myAudioFile = SURFFile.openAsRead(fc.getSelectedFile());
	        	
	        	boolean eof = false;
	        	int samplesCount = 0;
	        	int step = 0;
	        	int totalSteps = (int) (myAudioFile.getFrameNum() / 8000);
	        	
	        	totalSteps = 100;
	    
        		while(step < totalSteps) {
        			myAudioFile.readFrames(frames, 0, 8000);
        			myWritableAudioFile.writeFrames(frames, 0, 8000);
        			step ++;
        		}
	        	
	        	
	        	//myWritableAudioFile.writeFrames(frames, 0, 8000);
	        	
	        	myAudioFile.cleanUp();
	        	myWritableAudioFile.cleanUp();
	        	
	        	
				
				
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
        	
        	
        }
	}

}
