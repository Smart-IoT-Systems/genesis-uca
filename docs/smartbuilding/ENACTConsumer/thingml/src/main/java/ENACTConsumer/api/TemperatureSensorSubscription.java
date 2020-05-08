
/*
 * Generated TemperatureSensorSubscription
 */

package ENACTConsumer.api;

import java.util.Observer;

import org.smool.kpi.common.Logger;
import org.smool.kpi.model.smart.subscription.AbstractSubscription;
import org.smool.kpi.ssap.message.parameter.SSAPMessageRDFParameter.TypeAttribute;

import ENACTConsumer.model.smoolcore.impl.TemperatureSensor;

public class TemperatureSensorSubscription extends AbstractSubscription<TemperatureSensor> {

	private Observer customObserver = null;

	public TemperatureSensorSubscription() {
		super(TypeAttribute.RDFM3);
	}

	public TemperatureSensorSubscription(Observer customObserver) {
		super(TypeAttribute.RDFM3);
		this.customObserver = customObserver;
	}

	public void conceptAdded(TemperatureSensor aoc) {
		// TODO Add code to handle new added concepts
		Logger.debug("New Concept: " + aoc);
		customNotify(aoc);
	}

	public void conceptRemoved(TemperatureSensor aoc) {
		// TODO Add code to handle removed concepts
		Logger.debug("Removed Concept: " + aoc);
		customNotify(aoc);
	}

	public void conceptUpdated(TemperatureSensor newConcept, TemperatureSensor obsoleteConcept) {
		// TODO Add code to handle updated concepts
		Logger.debug("Updated Concept:");
		Logger.debug("Previous: " + obsoleteConcept);
		Logger.debug("Current: " + newConcept);
		customNotify(newConcept);
	}

	private void customNotify(TemperatureSensor concept) {
		SmoolKP.lastTimestamp = System.currentTimeMillis(); // update last time a message arrived
		if (customObserver != null) {
			if (!SmoolKP.checkSecurity(concept))
				return; // do not invoke handler when core security is not valid
			customObserver.update(null, concept);
		}
	}

}
