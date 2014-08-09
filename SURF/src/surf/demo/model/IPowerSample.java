package surf.demo.model;

import java.io.Serializable;


public interface IPowerSample extends Serializable {
	public double getIRMS();
	public double getVRMS();
	public double getRealPower();
	public double getReactivePower();
	public double getApparentPower();
	public double getPowerFactor();
	public long getTimestamp();
	public long getID();
}
