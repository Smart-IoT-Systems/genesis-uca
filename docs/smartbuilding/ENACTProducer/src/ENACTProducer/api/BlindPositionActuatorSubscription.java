
/*
 * Generated BlindPositionActuatorSubscription
 */

package ENACTProducer.api;

import java.util.Observer;

import org.smool.kpi.common.Logger;
import org.smool.kpi.model.smart.subscription.AbstractSubscription;
import org.smool.kpi.ssap.message.parameter.SSAPMessageRDFParameter.TypeAttribute;

import ENACTProducer.model.smoolcore.impl.BlindPositionActuator;

public class BlindPositionActuatorSubscription extends AbstractSubscription<BlindPositionActuator> {

	private Observer customObserver = null;

	public BlindPositionActuatorSubscription() {
		super(TypeAttribute.RDFM3);
	}

	public BlindPositionActuatorSubscription(Observer customObserver) {
		super(TypeAttribute.RDFM3);
		this.customObserver = customObserver;
	}

	public void conceptAdded(BlindPositionActuator aoc) {
		// TODO Add code to handle new added concepts
		Logger.debug("New Concept: " + aoc);
		customNotify(aoc);
	}

	public void conceptRemoved(BlindPositionActuator aoc) {
		// TODO Add code to handle removed concepts
		Logger.debug("Removed Concept: " + aoc);
		customNotify(aoc);
	}

	public void conceptUpdated(BlindPositionActuator newConcept, BlindPositionActuator obsoleteConcept) {
		// TODO Add code to handle updated concepts
		Logger.debug("Updated Concept:");
		Logger.debug("Previous: " + obsoleteConcept);
		Logger.debug("Current: " + newConcept);
		customNotify(newConcept);
	}

	private void customNotify(BlindPositionActuator concept) {
		SmoolKP.lastTimestamp = System.currentTimeMillis(); // update last time a message arrived
		if (customObserver != null) {
			if (!SmoolKP.checkSecurity(concept))
				return; // do not invoke handler when core security is not valid
			customObserver.update(null, concept);
		}
	}

}
