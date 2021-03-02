package ENACTConsumer.logic;

import java.io.IOException;
import java.util.Observer;

import org.smool.kpi.model.exception.KPIModelException;

import ENACTConsumer.api.Consumer;
import ENACTConsumer.api.HumiditySensorSubscription;
import ENACTConsumer.api.LightingSensorSubscription;
import ENACTConsumer.api.PresenceSensorSubscription;
import ENACTConsumer.api.SmoolKP;
import ENACTConsumer.api.TemperatureSensorSubscription;
import ENACTConsumer.model.smoolcore.impl.HumiditySensor;
import ENACTConsumer.model.smoolcore.impl.LightingSensor;
import ENACTConsumer.model.smoolcore.impl.PresenceSensor;
import ENACTConsumer.model.smoolcore.impl.TemperatureSensor;

/**
 * Subscribe to data generated from KUBIK smart building.
 * <p>
 * This class should be the skeleton for the IoT application in the SB use case
 * </p>
 * <p>
 * The app is retrieving temperature and other data. When temp is higher or
 * lower than comfort, an actuation order is sent back to the Smart Building to
 * turn the temperature back to normal.
 * </p>
 *
 */
public class ConsumerMain {
	//For testing purpose, just hard code the name of the SmartEnergyApp here
	public static final String name = "SmartEnergyApp_ENACT";//"EnactConsumer" + System.currentTimeMillis() % 10000;
	public static CustomActuation actuation;

	public ConsumerMain(String sib, String addr, int port) throws Exception {

		SmoolKP.setKPName(name);
		System.out.println("*** " + name + " ***");
		// ------------PREPARE ACTUATION OBJECT--------------------
		actuation = new CustomActuation(name);

		// ---------------------------CONNECT TO SMOOL---------------------
		// SmoolKP.connect();
		// SmoolKP.connect("sib1", "192.168.1.128", 23000);
		SmoolKP.connect(sib, addr, port);

		// ---------------------------SUBSCRIBE TO DATA----------------------
		Consumer consumer = SmoolKP.getConsumer();

		consumer.subscribeToTemperatureSensor(new TemperatureSensorSubscription(createTemperatureObserver()), null);

		consumer.subscribeToPresenceSensor(new PresenceSensorSubscription(createPresenceObserver()), null);

		consumer.subscribeToLightingSensor(new LightingSensorSubscription(createLightingObserver()), null);

		consumer.subscribeToHumiditySensor(new HumiditySensorSubscription(createHumidityObserver()), null);

		// consumer.subscribeToSmokeSensor(new
		// SmokeSensorSubscription(createSmokeObserver()), null);

		//
		// GasSensorSubscription gasSubscription = new
		// GasSensorSubscription(createGasObserver());
		// consumer.subscribeToGasSensor(gasSubscription, null);

		// -----------ATTACH WATCHDOG instead of SLEEP-------
		// Thread.sleep(Long.MAX_VALUE); // keep application alive.
		SmoolKP.watchdog(3600); // maximum interval that at least one message should arrive

	}

	private Observer createTemperatureObserver() {
		return (o, concept) -> {
			TemperatureSensor sensor = (TemperatureSensor) concept;
			double temp = sensor.getTemperature().getValue();
			System.out.println("temp  from " + sensor._getIndividualID() + ": " + temp);
			if (temp > 24) {
				// launch an actuation order to modify the blinds position
				new Thread(() -> ConsumerMain.actuation.run(temp)).start(); // ConsumerMain.actuation.run();
			}
		};
	}

	// private Observer createSmokeObserver() {
	// return (o, concept) -> {
	// SmokeSensor sensor = (SmokeSensor) concept;
	// System.out.println("smoke from " + sensor._getIndividualID() + ": " +
	// sensor.getSmoke().getStatus());
	// };
	// }
	//
	// private Observer createGasObserver() {
	// return (o, concept) -> {
	// GasSensor sensor = (GasSensor) concept;
	// String t = sensor.getGas().getType();
	// System.out.println("gas " + t + " from " + sensor._getIndividualID() + ": " +
	// sensor.getGas().getValue());
	// };
	// }

	private Observer createHumidityObserver() { 
		return (o, concept) -> {
			HumiditySensor sensor = (HumiditySensor) concept;
			System.out.println("Humidity   from " + sensor._getIndividualID() + ": "
					+ Double.toString(sensor.getHumidity().getValue()));
		};
	}

	private Observer createLightingObserver() {
		return (o, concept) -> {
			LightingSensor sensor = (LightingSensor) concept;
			System.out.println("Light   from " + sensor._getIndividualID() + ": "
					+ Double.toString(sensor.getLighting().getValue()));
		};
	}

	private Observer createPresenceObserver() {
		return (o, concept) -> {
			PresenceSensor sensor = (PresenceSensor) concept;
			System.out.println("Presence   from " + sensor._getIndividualID() + ": "
					+ Boolean.toString(sensor.getPresence().getStatus()));
		};
	}

	public static void main(String[] args) throws Exception {
		String sib = args.length > 0 ? args[0] : "sib1";
		String addr = args.length > 1 ? args[1] : "15.236.132.74";
		int port = args.length > 2 ? Integer.valueOf(args[2]) : 23000;
//		 Logger.setDebugging(true);
//		 Logger.setDebugLevel(4);
		while (true) {
			try {
				new ConsumerMain(sib, addr, port);
			} catch (KPIModelException | IOException e) {
				e.printStackTrace();
				Thread.sleep(10000);
				System.out.println("RECONNECTING");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
