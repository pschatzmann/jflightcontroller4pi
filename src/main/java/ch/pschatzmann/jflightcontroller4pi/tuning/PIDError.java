package ch.pschatzmann.jflightcontroller4pi.tuning;

import ch.pschatzmann.jflightcontroller4pi.FlightController;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Calculation of the Fitness (= 1 / Total Error Rate)  of the actual settings.
 * 
 * @author pschatzmann
 *
 */
public class PIDError {
	private double pitchError;
	private double rollError;
	private int pitchCount;
	private int rollCount;
	
	
	PIDError(FlightController ctl){
		ctl.getParameterStore().register(ParametersEnum.SENSORPITCH, p -> {
			pitchError += Math.abs(p.value);
			pitchCount++;				
		});
		
		ctl.getParameterStore().register(ParametersEnum.SENSORROLL, p -> {
			rollError += Math.abs(p.value);
			rollCount++;
		});

	}
	
	void reset(){
		pitchError = 0;
		pitchCount = 0; 
		rollError = 0;
		rollCount = 0;
	}
	
	double getError() {
		return (pitchError / pitchCount) + (rollError / rollCount);		
	}

	double getError(ParametersEnum par) {
		if (par == ParametersEnum.PITCH)
			return (pitchError / pitchCount);		
		if (par == ParametersEnum.ROLL)
			return (rollError / rollCount);	
		return (pitchError / pitchCount) + (rollError / rollCount);		
	}

	double getFitness() {
		return 1.0 / getError();
	}

}
