package surf.demo.util;

import java.util.concurrent.ArrayBlockingQueue;

import surf.demo.model.IPowerSample;
import surf.demo.model.PowerSample;


public class PowerCalculator implements Runnable {
	private ArrayBlockingQueue<IPowerSample> powerSamplesQueue;
	private ArrayBlockingQueue<float[][]> channelSamples;
	
	private long sampleTimestamp = 0;
	private float[] calibrationConstants = {1,1};
	private double r = (double)1/60;
	private double timestampStep = r * 1000; // file is 60 Hz

	private long sampleCount = 1;	
	
	public PowerCalculator (long initialTimestamp) {
		this.sampleTimestamp = initialTimestamp;
		this.powerSamplesQueue = new ArrayBlockingQueue<IPowerSample>(10000);
	}
	
	public void setCalibatrionConstants(float[] calibrationConstants) {
		this.calibrationConstants = calibrationConstants;
	}
	
	public ArrayBlockingQueue<float[][]> getChannelSamplesQueue() {
		return this.channelSamples;
	}
	
	public ArrayBlockingQueue<IPowerSample> getPowerSamplesQueue() {
		return this.powerSamplesQueue;
	}
	
	public void setChannelSamples(ArrayBlockingQueue<float[][]> channelSamples) {
		this.channelSamples = channelSamples;
	}
	
	public void setPowerSamplesQueue(ArrayBlockingQueue<IPowerSample> powerSamplesQueue) {
		this.powerSamplesQueue = powerSamplesQueue;
	}

	@Override
	public void run() {
		float[][] cs;
		PowerSample ps;
		while(true) {
			
			try {
				cs = channelSamples.take();
				for(int i = 0; i < cs[0].length; i++) {
					ps = new PowerSample(0,0, cs[0][i] * this.calibrationConstants[0], 
											cs[1][i] * this.calibrationConstants[1], sampleTimestamp, sampleCount);
					sampleTimestamp += timestampStep;
					sampleCount ++;
					this.powerSamplesQueue.put(ps);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
	}
}
