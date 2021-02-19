
/*
 * Generated AccelerometerSubscription
 */

package ENACTConsumer.api;

import ENACTConsumer.model.smoolcore.impl.Accelerometer;

import org.smool.kpi.common.Logger;
import org.smool.kpi.model.smart.subscription.AbstractSubscription;
import org.smool.kpi.ssap.message.parameter.SSAPMessageRDFParameter.TypeAttribute;
import java.util.Observer;

public class AccelerometerSubscription extends AbstractSubscription<Accelerometer> {

	private Observer customObserver=null;
	
	public AccelerometerSubscription() {
		super(TypeAttribute.RDFM3);
	}
	
	
	public AccelerometerSubscription(Observer customObserver) {
		super(TypeAttribute.RDFM3);
		this.customObserver=customObserver;
	}

	public void conceptAdded(Accelerometer aoc) {
		// TODO Add code to handle new added concepts
		Logger.debug("New Concept: " + aoc);
		customNotify(aoc);
	}

	public void conceptRemoved(Accelerometer aoc) {
		// TODO Add code to handle removed concepts
		Logger.debug("Removed Concept: " + aoc);
		customNotify(aoc);
	}

	public void conceptUpdated(Accelerometer newConcept, Accelerometer obsoleteConcept) {
		// TODO Add code to handle updated concepts
		Logger.debug("Updated Concept:");
		Logger.debug("Previous: " + obsoleteConcept);
		Logger.debug("Current: " + newConcept);
		customNotify(newConcept);
	}
	
	private void customNotify(Accelerometer concept) {
	  SmoolKP.lastTimestamp = System.currentTimeMillis(); // update last time a message arrived
	  if(customObserver!=null) customObserver.update(null, concept);
	}

}

