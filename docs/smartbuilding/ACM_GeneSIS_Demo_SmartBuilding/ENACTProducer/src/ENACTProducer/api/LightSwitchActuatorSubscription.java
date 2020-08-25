
/*
 * Generated LightSwitchActuatorSubscription
 */

package ENACTProducer.api;

import ENACTProducer.model.smoolcore.impl.LightSwitchActuator;

import org.smool.kpi.common.Logger;
import org.smool.kpi.model.smart.subscription.AbstractSubscription;
import org.smool.kpi.ssap.message.parameter.SSAPMessageRDFParameter.TypeAttribute;
import java.util.Observer;

public class LightSwitchActuatorSubscription extends AbstractSubscription<LightSwitchActuator> {

	private Observer customObserver=null;
	
	public LightSwitchActuatorSubscription() {
		super(TypeAttribute.RDFM3);
	}
	
	
	public LightSwitchActuatorSubscription(Observer customObserver) {
		super(TypeAttribute.RDFM3);
		this.customObserver=customObserver;
	}

	public void conceptAdded(LightSwitchActuator aoc) {
		// TODO Add code to handle new added concepts
		Logger.debug("New Concept: " + aoc);
		customNotify(aoc);
	}

	public void conceptRemoved(LightSwitchActuator aoc) {
		// TODO Add code to handle removed concepts
		Logger.debug("Removed Concept: " + aoc);
		customNotify(aoc);
	}

	public void conceptUpdated(LightSwitchActuator newConcept, LightSwitchActuator obsoleteConcept) {
		// TODO Add code to handle updated concepts
		Logger.debug("Updated Concept:");
		Logger.debug("Previous: " + obsoleteConcept);
		Logger.debug("Current: " + newConcept);
		customNotify(newConcept);
	}
	
	private void customNotify(LightSwitchActuator concept) {
	  SmoolKP.lastTimestamp = System.currentTimeMillis(); // update last time a message arrived
	  if(customObserver!=null) customObserver.update(null, concept);
	}

}

