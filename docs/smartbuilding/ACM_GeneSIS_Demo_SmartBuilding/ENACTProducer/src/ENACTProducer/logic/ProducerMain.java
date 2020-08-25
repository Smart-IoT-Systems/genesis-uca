package ENACTProducer.logic;

import java.io.IOException;
import java.util.Observer;

import org.smool.kpi.model.exception.KPIModelException;

import ENACTProducer.api.BlindPositionActuatorSubscription;
import ENACTProducer.api.Consumer;
import ENACTProducer.api.Producer;
import ENACTProducer.api.SmoolKP;
import ENACTProducer.model.smoolcore.IContinuousInformation;
import ENACTProducer.model.smoolcore.impl.BlindPositionActuator;
import ENACTProducer.model.smoolcore.impl.GasSensor;
import ENACTProducer.model.smoolcore.impl.SmokeSensor;
import ENACTProducer.model.smoolcore.impl.TemperatureInformation;
import ENACTProducer.model.smoolcore.impl.TemperatureSensor;

/**
 * Producer of data for the Smart Building use case for Enact.
 * <p>
 * NOTE: although this app is mainly a producer of sensors data, it can receive
 * actuation orders.
 * </p>
 * <p>
 * This app is sending (via SMOOL) the temperature, gas, etc stataus in the
 * smart building. When an external app controlling this data decides that the
 * values must be re-adapted, an actuation order is sent back to this
 * appication, and this one check if security data is valid and then it calls
 * the Smart Building SCADA to tune the values.
 * </p>
 *
 */
public class ProducerMain {

	public static final String vendor = "Tecnalia";
	public static final String name = "EnactProducer" + System.currentTimeMillis() % 10000;
	public static TemperatureSensor tempSensor;

	public ProducerMain(String sib, String addr, int port) throws Exception {
		SmoolKP.setKPName(name);
		System.out.println("*** " + name + " ***");
		// ---------------------------CONNECT TO SMOOL---------------------
		// SmoolKP.connect();
		// SmoolKP.connect("sib1", "172.24.5.151", 23000);
		// SmoolKP.connect("sib1", "192.168.1.128", 23000);
		SmoolKP.connect(sib, addr, port);

		// ---------------------------CREATE SENSOR-----------------------------
		Producer producer = SmoolKP.getProducer();
		tempSensor = new TemperatureSensor(name + "tempSensor");
		SmokeSensor smokeSensor = new SmokeSensor(name + "smokeSensor");
		GasSensor gasSensor = new GasSensor(name + "gasSensor");

		// ---------------------------PRODUCE DATA----------------------------------
		String timestamp = Long.toString(System.currentTimeMillis());

		TemperatureInformation tempInfo = new TemperatureInformation(name + "_temp");
		double temp = 20;
		tempInfo.setValue(temp).setUnit("ºC").setTimestamp(timestamp);

		producer.createTemperatureSensor(tempSensor._getIndividualID(), name, vendor, null, null, tempInfo);

		// SmokeInformation smokeInfo = new SmokeInformation(name + "_smoke");
		// boolean smoke = false;
		// smokeInfo.setStatus(smoke).setTimestamp(timestamp);
		// producer.createSmokeSensor(smokeSensor._getIndividualID(), name, vendor,
		// null, null, smokeInfo);
		//
		// GasInformation gasInfo = new GasInformation(name + "_CO2");
		// double gas = 400;
		// gasInfo.setType("CO2").setUnit("ppm").setValue(gas).setTimestamp(timestamp);
		// producer.createGasSensor(gasSensor._getIndividualID(), name, vendor, null,
		// gasInfo, null);

		// ---------------------------CONSUME ACTION----------------------------------
		Consumer consumer = SmoolKP.getConsumer();
		BlindPositionActuatorSubscription subscription = new BlindPositionActuatorSubscription(createObserver());
		consumer.subscribeToBlindPositionActuator(subscription, null);

		// ---------------------SEND DATA--------------------------------------------
		while (true) {
			Thread.sleep(10000);
			timestamp = Long.toString(System.currentTimeMillis());
			temp = temp < 26 ? temp + 0.5 : 22;
			tempInfo.setValue(temp).setTimestamp(timestamp);
			producer.updateTemperatureSensor(tempSensor._getIndividualID(), name, vendor, null, null, tempInfo);

			System.out.println("Producing temp  " + Double.toString(temp) + " (and more concepts)");

			// smoke = !smoke;
			// smokeInfo.setStatus(smoke).setTimestamp(timestamp);
			// producer.updateSmokeSensor(smokeSensor._getIndividualID(), name, vendor,
			// null, null, smokeInfo);
			//
			// gas = gas < 2000 ? gas + 100 : 400;
			// gasInfo.setValue(gas).setTimestamp(timestamp);
			// producer.updateGasSensor(gasSensor._getIndividualID(), name, vendor, null,
			// gasInfo, null);
		}

	}

	/**
	 * Process messages related to actuation orders on blinds
	 */
	private Observer createObserver() {
		
		return (o, concept) -> {
			try {
				BlindPositionActuator actuator = (BlindPositionActuator) concept;
				IContinuousInformation info = actuator.getBlindPos();
				System.out.println("receiving ACTUATION order on blinds. Value: " + info.getValue() + " and security "
						+ info.getSecurityData().getData());
			}catch(Exception e) {
				e.printStackTrace();
			}
		};
		
	}

	boolean test(String data) {
		return true;
	}

	private Observer myObserver() {
		return (o, concept) -> {
			BlindPositionActuator actuator = (BlindPositionActuator) concept;
			IContinuousInformation info = actuator.getBlindPos();
			System.out.println(info.getValue());
		};
	}

	public static void main(String[] args) throws Exception {
		String sib = args.length > 0 ? args[0] : "sib1";
		String addr = args.length > 1 ? args[1] : "15.236.132.74";
		int port = args.length > 2 ? Integer.valueOf(args[2]) : 23000;
		// Logger.setDebugging(true);
		// Logger.setDebugLevel(4);
		while (true) {
			try {
				new ProducerMain(sib, addr, port);
			} catch (KPIModelException | IOException e) {
				e.printStackTrace();
				Thread.sleep(1000);
				System.out.println("RECONNECTING");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
