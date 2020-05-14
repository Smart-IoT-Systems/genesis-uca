package ENACTConsumer.logic;

import ENACTConsumer.api.SmoolKP;
import ENACTConsumer.model.smoolcore.impl.BlindPositionActuator;
import ENACTConsumer.model.smoolcore.impl.ContinuousInformation;
import ENACTConsumer.model.smoolcore.impl.SecurityAuthorization;

/**
 * Example of sending back to SMOOL am actuation message with security data.
 * 
 * <p>
 * Once a temperature value is reached, the temperature Observer calls this
 * class. Then, a new Actuation message on blinds is sent. To prevent other
 * clients to send actuation orders, this message is sent with security content.
 * </P>
 */
public class CustomActuation {
	private boolean isFirstActuation = true;
	private String kpName;
	private String name;
	private double val = 0;
	private BlindPositionActuator actuator;
	private ContinuousInformation blindPos;
	private SecurityAuthorization sec;

	public CustomActuation(String kpName) {
		this.kpName = kpName;
		this.name = kpName + "_blindActuator";
		actuator = new BlindPositionActuator(name + "_actuator");
		blindPos = new ContinuousInformation(name + "_blindPosition");
		sec = new SecurityAuthorization(name + "_security");

	}

	public synchronized void run(double temp) {
		System.out.println("Sending ACTUATION order because temp is " + temp);
		run();
	}
	
	public synchronized void run() {
		try {
			if (isFirstActuation) {
				// TODO this security payload will be provided either by the Tecnalia AC agent
				// or by the thingml features for SMOOL

				// if started as insecure, do not send credentials (>java -Dinsecure app.jar)
				if (System.getProperty("insecure") != null) {
					// IMPORTANT, send data="" instead of null since the global list pf props must
					// be sent and if old BlindSobscriptions where created with that triple and then
					// with new insecure requests the data triple is not sent, the subscriptors
					// crash and cannot reconnect until smool server is restarted.
					sec.setData("");
				} else {
					sec.setData(
							"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkludmVzdGlnYWRvciBDb2xhYm9yYWRvciIsImlhdCI6MTUxNjIzOTAyMn0.OBdiCZDhegbD8liXQ5WqFk2tZQGw0pLXTYm61GoNWgM");
				}

				blindPos.setValue(val).setSecurityData(sec);
				actuator.setBlindPos(blindPos);

				SmoolKP.getProducer().createBlindPositionActuator(name, kpName, "TECNALIA", null, blindPos, null);
				isFirstActuation = false;

			} else {
				val = val + 1; // WARNING: if val is the same as latest val, the mesage is not sent
				blindPos.setValue(val); // we can also send temp, so the SCADA will contain the temp-to-blind rule
				SmoolKP.getProducer().updateBlindPositionActuator(name, kpName, "TECNALIA", null, blindPos, null);
			}
		} catch (Exception e) {
			System.out.println("Error: the actuation order cannot be sent. " + e.getMessage());
		}
	}
}