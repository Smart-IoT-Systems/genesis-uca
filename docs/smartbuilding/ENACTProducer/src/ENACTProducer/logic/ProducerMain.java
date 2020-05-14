package ENACTProducer.logic;

import java.io.IOException;
import java.util.Observer;

import org.smool.kpi.model.exception.KPIModelException;

import org.eclipse.paho.client.mqttv3.*;
import ENACTProducer.api.BlindPositionActuatorSubscription;
import ENACTProducer.api.Consumer;
import ENACTProducer.api.Producer;
import ENACTProducer.api.SmoolKP;
import ENACTProducer.model.smoolcore.IContinuousInformation;
import ENACTProducer.model.smoolcore.impl.BlindPositionActuator;
import ENACTProducer.model.smoolcore.impl.GasInformation;
import ENACTProducer.model.smoolcore.impl.GasSensor;
import ENACTProducer.model.smoolcore.impl.SmokeInformation;
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
public class ProducerMain implements MqttCallback {

	public static final String vendor = "Tecnalia";
	public static final String name = "EnactProducer" + System.currentTimeMillis() % 10000;
	public static TemperatureSensor tempSensor;

	private TemperatureInformation tempInfo;

	public ProducerMain(String sib, String addr, int port) throws Exception {
		SmoolKP.setKPName(name);
		System.out.println("*** " + name + " ***");
		// ---------------------------CONNECT TO SMOOL---------------------
		// SmoolKP.connect();
		// SmoolKP.connect("sib1", "172.24.5.151", 23000);
		SmoolKP.connect(sib, addr, port);

		//Create an MQTT client here
		initMQTTclient();

		// ---------------------------CREATE SENSOR-----------------------------
		Producer producer = SmoolKP.getProducer();
		tempSensor = new TemperatureSensor(name + "tempSensor");
		SmokeSensor smokeSensor = new SmokeSensor(name + "smokeSensor");
		GasSensor gasSensor = new GasSensor(name + "gasSensor");

		// ---------------------------PRODUCE DATA----------------------------------
		String timestamp = Long.toString(System.currentTimeMillis());

		tempInfo = new TemperatureInformation(name + "_temp");
		double temp = 25;
		tempInfo.setValue(temp).setUnit("ºC").setTimestamp(timestamp);

		producer.createTemperatureSensor(tempSensor._getIndividualID(), name, vendor, null, null, tempInfo);

		SmokeInformation smokeInfo = new SmokeInformation(name + "_smoke");
		boolean smoke = false;
		smokeInfo.setStatus(smoke).setTimestamp(timestamp);
		producer.createSmokeSensor(smokeSensor._getIndividualID(), name, vendor, null, null, smokeInfo);

		GasInformation gasInfo = new GasInformation(name + "_CO2");
		double gas = 400;
		gasInfo.setType("CO2").setUnit("ppm").setValue(gas).setTimestamp(timestamp);
		producer.createGasSensor(gasSensor._getIndividualID(), name, vendor, null, gasInfo, null);

		// ---------------------------CONSUME ACTION----------------------------------
		Consumer consumer = SmoolKP.getConsumer();
		BlindPositionActuatorSubscription subscription = new BlindPositionActuatorSubscription(createObserver());
		consumer.subscribeToBlindPositionActuator(subscription, null);

	}


	private void initMQTTclient() {
		try{
        	// ---------------------------MQTT Client----------------------------------
    		String publisherId = UUID.randomUUID().toString();
    		publisher = new MqttClient("tcp://127.0.0.1:1883", publisherId);
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
			publisher.setCallback(this);
            publisher.connect(options);
			System.out.println("Connected to broker");

			// All subscriptions for actuation orders
			String myTopic = "/home/A/Input/float/Thermostat_(Room_Temperature)";
			MqttTopic topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);
            
        }catch(Exception e){
            throw new RuntimeException("Exception occured in creating MQTT Client");
        }
	}

	/**
	FROM APP to SMOOL
	*/
	 @Override
    public void connectionLost(Throwable t) {
        System.out.println("Connection lost!");
        // code to reconnect to the broker would go here if desired
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
		//we should send it to smoll
        System.out.println("-------------------------------------------------");
        System.out.println("| Topic:" + s);
        System.out.println("| Message: " + new String(mqttMessage.getPayload()));
        System.out.println("-------------------------------------------------");
        
		String timestamp = Long.toString(System.currentTimeMillis());
		tempInfo.setValue(temp).setUnit("ºC").setTimestamp(timestamp);
		SmoolKP.getProducer().updateTemperatureSensor(ProducerMain.tempSensor._getIndividualID(), ProducerMain.name, ProducerMain.vendor, null, null, tempInfo);				
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        //System.out.println("Pub complete" + new String(token.getMessage().getPayload()));

    }


	/**
	 * Processs messages related to actuation orders on blinds
	 */
	private Observer createObserver() {
		return (o, concept) -> {
			BlindPositionActuator actuator = (BlindPositionActuator) concept;
			IContinuousInformation info = actuator.getBlindPos();
			System.out.println("receiving ACTUATION order on blinds. Value: " + info.getValue() + " and security "
					+ info.getSecurityData().getData());
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
		String addr = args.length > 1 ? args[1] : "smool.tecnalia.com";
		int port = args.length > 2 ? Integer.valueOf(args[2]) : 80;
		// Logger.setDebugging(true);
		// Logger.setDebugLevel(4);
		while (true) {
			try {
				new ProducerMain(sib, addr, port);
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
