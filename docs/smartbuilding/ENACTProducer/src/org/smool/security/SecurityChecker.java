package org.smool.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.smool.kpi.model.smart.AbstractOntConcept;
import org.smool.kpi.model.smart.slots.FunctionalSlot;

import ENACTProducer.model.smoolcore.IInformation;
import ENACTProducer.model.smoolcore.ISecurity;

/**
 * -----------------------------------------------------------------------------------------------------------------------------
 * Test if security constraints are met based on policies.
 * 
 * Note that the implementation of this class can be generated from external
 * tools (i.e:thingml)
 * -----------------------------------------------------------------------------------------------------------------------------
 */
public class SecurityChecker<T extends AbstractOntConcept> implements Predicate<T> {

	private Map<String, String> policies = new HashMap<>();

	public SecurityChecker() {
policies.put("BlindPositionActuator", "Authentication");

policies.put("BlindPositionActuator", "Authentication");

policies.put("", "");

policies.put("BlindPositionActuator", "Authentication");

policies.put("", "");

policies.put("", "");

policies.put("", "");

policies.put("", "");

policies.put("", "");



		// policies.put("BlindPositionActuator", "Authentication");
		// policies.put("BlindPositionActuator", "Authorization");
		// policies.put("BlindPositionActuator", "Confidentiality");
		// policies.put("BlindPositionActuator", "Integrity");
		// policies.put("BlindPositionActuator", "NonRepudiation");
	}

	/**
	 * Test if security policies are fulfilled.
	 * <p>
	 * If test is not passed, the message is discarded promptly, so the message is
	 * not arrived at the app "logic code"
	 * </p>
	 * <p>
	 * Note: This code can be replaced by custom security controls. Instead of
	 * checking policies as a HashMap, we could perform check operations on external
	 * IAM providers and so on.
	 * </p>
	 */
	public boolean test(T t) {
		try {
			if (policies.size() == 0)
				return true;
			final String name = t.getClass().getSimpleName();
			if (!policies.containsKey(name))
				return true; // there is no policy set for this element, so it is ok

			// get the slot with security data
			List<ISecurity> items = new ArrayList<>();
			t._listSlots().stream().filter(el -> el.isFunctionalProperty()).forEach(el -> {
				FunctionalSlot fSlot = ((FunctionalSlot) el);
				if (fSlot.getValue() == null)
					return;
				if ((fSlot.getValue() instanceof IInformation) == false)
					return;
				IInformation info = (IInformation) fSlot.getValue();
				ISecurity security = info.getSecurityData();
				if (security != null)
					items.add(security);
			});

			// check security elements (security objects have CLASS (Authentication
			// Authorization, etc) TYPE (KERBEROS,JWT...) and DATA(encripted payload,
			// token...
			boolean isCompliant = items.stream()
					.map(sec -> policies.get(name).equals(sec.getClass().getSimpleName().replace("Security", "")))
					.allMatch(el -> el == true);

			// return test
			System.out.println(">>>>>>>>>>Security checker is " + isCompliant + " for " + t._getIndividualID());
			return isCompliant ? true : false;

		} catch (Exception e) {
			e.printStackTrace();
			return false; // cannot verify if secure or not -> return false
		}
	}

}
