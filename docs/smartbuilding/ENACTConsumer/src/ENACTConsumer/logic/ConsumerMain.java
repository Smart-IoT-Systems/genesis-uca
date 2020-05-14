package ENACTConsumer.logic;

import java.io.IOException;
import java.util.Observer;

import org.smool.kpi.model.exception.KPIModelException;

import ENACTConsumer.api.Consumer;
import ENACTConsumer.api.GasSensorSubscription;
import ENACTConsumer.api.SmokeSensorSubscription;
import ENACTConsumer.api.SmoolKP;
import ENACTConsumer.api.TemperatureSensorSubscription;
import ENACTConsumer.model.smoolcore.impl.GasSensor;
import ENACTConsumer.model.smoolcore.impl.SmokeSensor;
import ENACTConsumer.model.smoolcore.impl.TemperatureSensor;

/**
 * Subscribe to data generated from KUBIK smart building.
 * <p>
 * This class should be the skeleton fot the IoT application int the SB use case
 * </p>
 * <p>
 * The app is retrieving temperature and other data. When temp is higher or
 * lower than comfort, an actuation order is sent back to the Smart Building to
 * turn the temperature back to normal.
 * </p>
 *
 */
public class ConsumerMain {
	public static final String name = "EnactConsumer" + System.currentTimeMillis() % 10000;
	public static CustomActuation actuation;

	public ConsumerMain(String sib, String addr, int port) throws Exception {

		SmoolKP.setKPName(name);
		System.out.println("*** " + name + " ***");
		// ------------PREPARE ACTUATION OBJECT--------------------
		actuation = new CustomActuation(name);

		// ---------------------------CONNECT TO SMOOL---------------------
		// SmoolKP.connect();
		// SmoolKP.connect("sib1", "172.24.5.151", 23000);
		SmoolKP.connect(sib, addr, port);

		// ---------------------------SUBSCRIBE TO DATA----------------------
		Consumer consumer = SmoolKP.getConsumer();

		TemperatureSensorSubscription tempSubscription = new TemperatureSensorSubscription(createTemperatureObserver());
		consumer.subscribeToTemperatureSensor(tempSubscription, null);

		SmokeSensorSubscription smokeSubscription = new SmokeSensorSubscription(createSmokeObserver());
		consumer.subscribeToSmokeSensor(smokeSubscription, null);

		GasSensorSubscription gasSubscription = new GasSensorSubscription(createGasObserver());
		consumer.subscribeToGasSensor(gasSubscription, null);

		// -----------ATTACH WATCHDOG instead of SLEEP-------
		// Thread.sleep(Long.MAX_VALUE); // keep application alive.
		SmoolKP.watchdog(5 * 60); // 5 min is the maximum interval that at least one message should arrive

	}

	private Observer createTemperatureObserver() {
		return (o, concept) -> {
			TemperatureSensor sensor = (TemperatureSensor) concept;
			double temp = sensor.getTemperature().getValue();
			System.out.println("temp  from " + sensor._getIndividualID() + ": " + temp);
			if (temp > 22) {
				// launch an actuation order to modify the blinds position
				new Thread(() -> ConsumerMain.actuation.run(temp)).start(); // ConsumerMain.actuation.run();
			}
		};
	}

	private Observer createSmokeObserver() {
		return (o, concept) -> {
			SmokeSensor sensor = (SmokeSensor) concept;
			System.out.println("smoke  from " + sensor._getIndividualID() + ": " + sensor.getSmoke().getStatus());
		};
	}

	private Observer createGasObserver() {
		return (o, concept) -> {
			GasSensor sensor = (GasSensor) concept;
			String t = sensor.getGas().getType();
			System.out.println("gas " + t + "  from " + sensor._getIndividualID() + ": " + sensor.getGas().getValue());
		};
	}

	public static void main(String[] args) throws Exception {
		String sib = args.length > 0 ? args[0] : "sib1";
		String addr = args.length > 1 ? args[1] : "smool.tecnalia.com";
		int port = args.length > 2 ? Integer.valueOf(args[2]) : 80;
		// Logger.setDebugging(true);
		// Logger.setDebugLevel(4);
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
