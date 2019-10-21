package ch.pschatzmann.jflightcontroller4pi.tests;

import org.junit.Assert;
import org.junit.Test;

import ch.pschatzmann.jflightcontroller4pi.parameters.ParameterStore;
import ch.pschatzmann.jflightcontroller4pi.parameters.ParametersEnum;

/**
 * Checks for the storing and retrieving of parameter values
 * 
 * @author pschatzmann
 *
 */
public class TestParameters {
	boolean isLambdaExecuted = false;
	
	@Test 
	public void testSimple() {
		ParameterStore store = new ParameterStore(2);
		
		Assert.assertTrue(store.getValue(ParametersEnum.THROTTLE).isEmpty());
		
		store.setValue(ParametersEnum.THROTTLE, 0.1);		
		Assert.assertEquals(0.1, store.getValue(ParametersEnum.THROTTLE).value,0.001);
		Assert.assertFalse(store.getValue(ParametersEnum.THROTTLE).isEmpty());

		store.setValue(ParametersEnum.THROTTLE, 0.2);
		Assert.assertEquals(0.2, store.getValue(ParametersEnum.THROTTLE).value,0.001);
		Assert.assertFalse(store.getValue(ParametersEnum.THROTTLE).isEmpty());

		store.setValue(ParametersEnum.THROTTLE, 0.3);
		Assert.assertEquals(0.3, store.getValue(ParametersEnum.THROTTLE).value,0.001);
		Assert.assertEquals(0.2, store.getPriorValue(ParametersEnum.THROTTLE).value,0.001);

	}
	
	@Test 
	public void testHistory() {
		ParameterStore store = new ParameterStore(2);
		
		store.setValue(ParametersEnum.THROTTLE, 0.1);		
		Assert.assertEquals(1, store.getHistory(ParametersEnum.THROTTLE).length);
		Assert.assertEquals(0.1, store.getHistory(ParametersEnum.THROTTLE)[0].value,0.001);
		Assert.assertFalse(store.getHistory(ParametersEnum.THROTTLE)[0].isEmpty());

		store.setValue(ParametersEnum.THROTTLE, 0.2);
		Assert.assertEquals(2, store.getHistory(ParametersEnum.THROTTLE).length);
		Assert.assertEquals(0.1, store.getHistory(ParametersEnum.THROTTLE)[0].value,0.001);
		Assert.assertEquals(0.2, store.getHistory(ParametersEnum.THROTTLE)[1].value,0.001);

		store.setValue(ParametersEnum.THROTTLE, 0.3);
		Assert.assertEquals(2, store.getHistory(ParametersEnum.THROTTLE).length);
		Assert.assertEquals(0.2, store.getHistory(ParametersEnum.THROTTLE)[0].value,0.001);
		Assert.assertEquals(0.3, store.getHistory(ParametersEnum.THROTTLE)[1].value,0.001);

	}
	
	@Test 
	public void testLambda() {
		ParameterStore store = new ParameterStore(2);
	
		isLambdaExecuted = false;
		store.register(ParametersEnum.THROTTLE, c -> { 
			isLambdaExecuted = true;
		});
		
		Assert.assertFalse(isLambdaExecuted);
		store.setValue(ParametersEnum.THROTTLE, 0.1);		
		Assert.assertTrue(isLambdaExecuted);
		
	}


	

}
