package ENACTProducer.logic;

import java.io.IOException;
import java.util.Observer;
import java.util.UUID;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.Locale;

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

	private IMqttClient publisher; 

	public TemperatureInformation tempInfo;

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
    		publisher = new MqttClient("tcp://192.168.1.28:1883", publisherId);
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
			publisher.setCallback(this);
            publisher.connect(options);
			System.out.println("Connected to broker");

			
			String myTopic = "/home/A/Input/float/Thermostat_(Room_Temperature)";
			MqttTopic topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);

			myTopic = "/home/A/Input/bool/Smoke_Detector";
			topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);

			myTopic = "/home/A/Input/float/Brightness_Sensor_(Analog)";
			topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);
            
        }catch(Exception e){
            e.printStackTrace();
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
        
		/*if(s.equals("/home/A/Input/float/Thermostat_(Room_Temperature)")){
			
			String message_payload = new String(mqttMessage.getPayload());
			System.out.println("-------------------------------------------------");
			System.out.println("Forwarding to SMOOL" + message_payload);
			System.out.println("-------------------------------------------------");
			String timestamp = Long.toString(System.currentTimeMillis());
			NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
			Number number = format.parse(message_payload);
			double d = number.doubleValue();
			tempInfo.setValue(d).setUnit("ºC").setTimestamp(timestamp);
			SmoolKP.getProducer().updateTemperatureSensor(ProducerMain.tempSensor._getIndividualID(), ProducerMain.name, ProducerMain.vendor, null, null, tempInfo);				
		}*/

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
			System.out.println("receiving ACTUATION order on blinds. Value: " + info.getValue());
			if(info.getValue() != null){
					//byte [] val = new byte[] { (byte)((info.getValue() == 0.0)?1:0) };
					MqttMessage msg=null;
					MqttMessage msg2=null;
					if((info.getValue() == 0.0)){
						msg= new MqttMessage("true".getBytes());
						msg2= new MqttMessage("false".getBytes());
					} 
					if((info.getValue() == 100.0)){
						msg= new MqttMessage("false".getBytes());
						msg2= new MqttMessage("true".getBytes());
					} 
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Forwarding to MQTT"+new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("/home/A/Output/bool/Roller_Shades_1_(Down)", msg2);
							publisher.publish("/home/A/Output/bool/Roller_Shades_2_(Down)", msg2);
							publisher.publish("/home/A/Output/bool/Roller_Shades_3_(Down)", msg2);
							publisher.publish("/home/A/Output/bool/Roller_Shades_4_(Down)", msg2);
							
							publisher.publish("/home/A/Output/bool/Roller_Shades_1_(Up)", msg2);
							publisher.publish("/home/A/Output/bool/Roller_Shades_2_(Up)", msg2);
							publisher.publish("/home/A/Output/bool/Roller_Shades_3_(Up)", msg2);
							publisher.publish("/home/A/Output/bool/Roller_Shades_4_(Up)", msg2);

							try
							{
								Thread.sleep(20);
							}
							catch(InterruptedException ex)
							{
								Thread.currentThread().interrupt();
							}

							publisher.publish("/home/A/Output/bool/Roller_Shades_1_(Up)", msg);
							publisher.publish("/home/A/Output/bool/Roller_Shades_2_(Up)", msg);
							publisher.publish("/home/A/Output/bool/Roller_Shades_3_(Up)", msg);
							publisher.publish("/home/A/Output/bool/Roller_Shades_4_(Up)", msg);
							
						}else {
							System.out.println("publisher.isConnected() = " + publisher.isConnected());
						}
					} catch (MqttPersistenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MqttException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}    
				} 
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
				Thread.sleep(10000);
				System.out.println("RECONNECTING");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
