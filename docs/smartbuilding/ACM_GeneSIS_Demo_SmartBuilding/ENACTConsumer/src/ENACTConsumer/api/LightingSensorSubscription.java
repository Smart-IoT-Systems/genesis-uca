
/*
 * Generated LightingSensorSubscription
 */

package ENACTConsumer.api;

import java.util.Observer;

import org.smool.kpi.common.Logger;
import org.smool.kpi.model.smart.subscription.AbstractSubscription;
import org.smool.kpi.ssap.message.parameter.SSAPMessageRDFParameter.TypeAttribute;

import ENACTConsumer.model.smoolcore.impl.LightingSensor;

public class LightingSensorSubscription extends AbstractSubscription<LightingSensor> {

	private Observer customObserver = null;

	public LightingSensorSubscription() {
		super(TypeAttribute.RDFM3);
	}

	public LightingSensorSubscription(Observer customObserver) {
		super(TypeAttribute.RDFM3);
		this.customObserver = customObserver;
	}

	public void conceptAdded(LightingSensor aoc) {
		// TODO Add code to handle new added concepts
		Logger.debug("New Concept: " + aoc);
		customNotify(aoc);
	}

	public void conceptRemoved(LightingSensor aoc) {
		// TODO Add code to handle removed concepts
		Logger.debug("Removed Concept: " + aoc);
		customNotify(aoc);
	}

	public void conceptUpdated(LightingSensor newConcept, LightingSensor obsoleteConcept) {
		// TODO Add code to handle updated concepts
		Logger.debug("Updated Concept:");
		Logger.debug("Previous: " + obsoleteConcept);
		Logger.debug("Current: " + newConcept);
		customNotify(newConcept);
	}

	private void customNotify(LightingSensor concept) {
		SmoolKP.lastTimestamp = System.currentTimeMillis(); // update last time a message arrived
		if (customObserver != null)
			customObserver.update(null, concept);
	}

}
