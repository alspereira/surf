/*
 *  Config.java
 *  
 *  MANDATORY
 *  
 *
 */

package surf.file;

public class Config {
	
	public final long initial_timestamp;			// 'ITSP' unix timestamp
	public final float sampling_rate;				// 'SPRT' sampling rate - to be used when lower than 1 Hz in the original format chunk
	public final int num_channels;					// from the original format chunk
	public final int sample_size ;					// from the original format chunk
	public final float[] channel_callibration; 		// 'CHCC' callibration constant -> one per channel
	
	public Config(long initial_timestamp, float sampling_rate, 
			int num_channels, int sample_size, float[] channel_callibration) {
		this.initial_timestamp = initial_timestamp;
		this.sampling_rate = sampling_rate;
		this.num_channels = num_channels;
		this.sample_size = sample_size;
		if(channel_callibration.length == this.num_channels)
			this.channel_callibration = channel_callibration;
		else {
			this.channel_callibration = new float[num_channels];
			// default to 1. should throw a warning or exception
			for(int i = 0; i < num_channels; i++)
				this.channel_callibration[i] = 1;
		}
	}
} 