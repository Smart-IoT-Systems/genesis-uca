
/*
 * Generated HVACActuatorSubscription
 */

package ENACTProducer.api;

import ENACTProducer.model.smoolcore.impl.HVACActuator;

import org.smool.kpi.common.Logger;
import org.smool.kpi.model.smart.subscription.AbstractSubscription;
import org.smool.kpi.ssap.message.parameter.SSAPMessageRDFParameter.TypeAttribute;
import java.util.Observer;

public class HVACActuatorSubscription extends AbstractSubscription<HVACActuator> {

	private Observer customObserver=null;
	
	public HVACActuatorSubscription() {
		super(TypeAttribute.RDFM3);
	}
	
	
	public HVACActuatorSubscription(Observer customObserver) {
		super(TypeAttribute.RDFM3);
		this.customObserver=customObserver;
	}

	public void conceptAdded(HVACActuator aoc) {
		// TODO Add code to handle new added concepts
		Logger.debug("New Concept: " + aoc);
		customNotify(aoc);
	}

	public void conceptRemoved(HVACActuator aoc) {
		// TODO Add code to handle removed concepts
		Logger.debug("Removed Concept: " + aoc);
		customNotify(aoc);
	}

	public void conceptUpdated(HVACActuator newConcept, HVACActuator obsoleteConcept) {
		// TODO Add code to handle updated concepts
		Logger.debug("Updated Concept:");
		Logger.debug("Previous: " + obsoleteConcept);
		Logger.debug("Current: " + newConcept);
		customNotify(newConcept);
	}
	
	private void customNotify(HVACActuator concept) {
	  SmoolKP.lastTimestamp = System.currentTimeMillis(); // update last time a message arrived
	  if(customObserver!=null) customObserver.update(null, concept);
	}

}

