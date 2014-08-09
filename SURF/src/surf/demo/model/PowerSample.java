package surf.demo.model;

public class PowerSample implements IPowerSample {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7139427425161909257L;
	/**
	 * 
	 */
	private double 	iRMS;
	private double	vRMS;
	private double 	realPower;
	private double 	reactivePower;
	private double 	apparentPower;
	private double 	powerFactor;
	private long 	timestamp;
	private long 	id = 0;
		
	public PowerSample(double iRMS, double vRMS, double realPower, double reactivePower, long timestamp, long id) {
		this.id = id;
		this.iRMS = iRMS;
		this.vRMS = vRMS;
		this.realPower = realPower;
		this.reactivePower = reactivePower;
		this.timestamp = timestamp;
		this.apparentPower = iRMS * vRMS;
		this.powerFactor = realPower/this.apparentPower;
		
		this.id = id;
	}
	
	public PowerSample(double iRMS, double vRMS, double realPower, double reactivePower, long timestamp) {
		this.iRMS = iRMS;
		this.vRMS = vRMS;
		this.realPower = realPower;
		this.reactivePower = reactivePower;
		this.timestamp = timestamp;
		this.apparentPower = iRMS * vRMS;
		this.powerFactor = realPower/this.apparentPower;
	}
	
	@Override
	public double getIRMS() {
		return this.iRMS;
	}

	@Override
	public double getVRMS() {
		return this.vRMS;
	}

	@Override
	public double getRealPower() {
		return this.realPower;
	}

	@Override
	public double getReactivePower() {
		return this.reactivePower;
	}

	@Override
	public double getApparentPower() {
		return this.apparentPower;
	}

	@Override
	public double getPowerFactor() {
		return this.powerFactor;
	}

	@Override
	public long getTimestamp() {
		return this.timestamp;
	}

	@Override
	public long getID() {
		return this.id;
	}

}
