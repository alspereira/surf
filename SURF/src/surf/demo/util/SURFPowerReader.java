package surf.demo.util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import surf.file.SURFFile;

public class SURFPowerReader implements Runnable {
	
	private ArrayBlockingQueue<float[][]> powerData;
	
	private int bufferSize = 60;
	private int channels = 2;
	
	private File file;
	
	public SURFPowerReader(File file) {
		powerData = new ArrayBlockingQueue<float[][]>(10);
		this.file = file;
	}
	
	public ArrayBlockingQueue<float[][]> getAudioDataQueue() {
		return powerData;
	}

	@Override
	public void run() {
		
		int samplesLength = 0;
		int numBuffers = 0;
		int samplesRemaining = 0;

		try {
			SURFFile surfFile = SURFFile.openAsRead(file);
			samplesLength = (int) surfFile.getFrameNum();
			
			
			if((samplesLength % bufferSize) == 0) {
				numBuffers = samplesLength / bufferSize;
			}
			else {
				numBuffers = (int) Math.ceil(samplesLength / bufferSize);
				samplesRemaining = samplesLength - (numBuffers * bufferSize);
			}
			
			float[][] frames = new float[channels][bufferSize];
			while(numBuffers > 0) {
				frames = new float[channels][bufferSize];
				surfFile.readFrames(frames, 0, bufferSize);
				numBuffers --;
				powerData.put(frames);
			}
			if(samplesRemaining > 0) {
				frames = new float[channels][samplesRemaining];
				surfFile.readFrames(frames, 0, samplesRemaining);
				powerData.put(frames);
			}
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			System.exit(-1);
		}	
	}
}
