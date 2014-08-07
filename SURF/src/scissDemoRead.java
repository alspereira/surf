import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JFileChooser;

import surf.file.Annotation;
import surf.file.Info;
import surf.file.SURFFile;
import surf.file.SURFFileDescr;



public class scissDemoRead {

	static String fileName = "";
	
	static SURFFileDescr descriptor;
	
	
	static SURFFile myAudioFile;
	
	public static void main(String[] args) {
		
		// Setup the file descriptor
		descriptor = new SURFFileDescr();
		descriptor.type = SURFFileDescr.TYPE_WAVE;
		descriptor.channels = 2;
		descriptor.rate = 8000;
		descriptor.bitsPerSample = 16;
		descriptor.sampleFormat = SURFFileDescr.FORMAT_INT;
		
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
        	int bufferSize = 16000;// 1 second * 2 channels

        	
        	// open a riff strem
        	try {
			        	
	        	myAudioFile = SURFFile.openAsRead(fc.getSelectedFile());

	        	myAudioFile.readMarkers();

	        	Object markers = myAudioFile.getDescr().getProperty(SURFFileDescr.KEY_LABELS);
	        	Object regions = myAudioFile.getDescr().getProperty(SURFFileDescr.KEY_REGIONS);
	        	Object notes   = myAudioFile.getDescr().getProperty(SURFFileDescr.KEY_NOTES);
	        	Object comments = myAudioFile.getDescr().getProperty(SURFFileDescr.KEY_COMMENTS);
	        	Object metadata = myAudioFile.getDescr().getProperty(SURFFileDescr.KEY_METADATA);
	        	Object info = myAudioFile.getDescr().getProperty(SURFFileDescr.KEY_INFO);
	        	
	        	ArrayList<Annotation> metadataList = (ArrayList<Annotation>) metadata;
	        	
	        	System.out.println(metadataList.get(1).content);
	        	
	        	@SuppressWarnings("unchecked")
				//ArrayList<Marker> m = (ArrayList<Marker>) markers;
	        	
	        	//Collections.sort(m, new CustomComparator());
	        	SURFFileDescr descr = myAudioFile.getDescr();
	        	System.out.println("initial timestamp: " + descr.SURF_initial_timestamp);
	        	System.out.println("timezone: " + descr.SURF_timezone);
	        	System.out.println("sampling rate: " + descr.SURF_sample_rate);
	        	System.out.println("calibration: " + Arrays.toString(descr.SURF_channel_calibration));
	        	System.out.println("end");
	        	
	        	
	        	myAudioFile.cleanUp();
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
        }
	}
}
