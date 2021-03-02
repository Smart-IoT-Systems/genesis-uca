
/*
 * Generated LightRangeActuatorSubscription
 */

package ENACTProducer.api;

import ENACTProducer.model.smoolcore.impl.LightRangeActuator;

import org.smool.kpi.common.Logger;
import org.smool.kpi.model.smart.subscription.AbstractSubscription;
import org.smool.kpi.ssap.message.parameter.SSAPMessageRDFParameter.TypeAttribute;
import java.util.Observer;

public class LightRangeActuatorSubscription extends AbstractSubscription<LightRangeActuator> {

	private Observer customObserver=null;
	
	public LightRangeActuatorSubscription() {
		super(TypeAttribute.RDFM3);
	}
	
	
	public LightRangeActuatorSubscription(Observer customObserver) {
		super(TypeAttribute.RDFM3);
		this.customObserver=customObserver;
	}

	public void conceptAdded(LightRangeActuator aoc) {
		// TODO Add code to handle new added concepts
		Logger.debug("New Concept: " + aoc);
		customNotify(aoc);
	}

	public void conceptRemoved(LightRangeActuator aoc) {
		// TODO Add code to handle removed concepts
		Logger.debug("Removed Concept: " + aoc);
		customNotify(aoc);
	}

	public void conceptUpdated(LightRangeActuator newConcept, LightRangeActuator obsoleteConcept) {
		// TODO Add code to handle updated concepts
		Logger.debug("Updated Concept:");
		Logger.debug("Previous: " + obsoleteConcept);
		Logger.debug("Current: " + newConcept);
		customNotify(newConcept);
	}
	
	private void customNotify(LightRangeActuator concept) {
	  SmoolKP.lastTimestamp = System.currentTimeMillis(); // update last time a message arrived
	  if(customObserver!=null) customObserver.update(null, concept);
	}

}

