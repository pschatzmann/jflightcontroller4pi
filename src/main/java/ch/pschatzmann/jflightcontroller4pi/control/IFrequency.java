package ch.pschatzmann.jflightcontroller4pi.control;

public interface IFrequency {
	/**
	 * Defines the execution frequency in Hz
	 * @param tick
	 */
	void setFrequency(double frequency);
	
	/**
	 * Provides the requested execution frequency
	 */
	double getFrequency();
	
	

}
