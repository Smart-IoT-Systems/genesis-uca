package ENACTProducer.logic;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.smool.kpi.model.exception.KPIModelException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ENACTProducer.api.BlindPositionActuatorSubscription;
import ENACTProducer.api.Consumer;
import ENACTProducer.api.Producer;
import ENACTProducer.api.SmoolKP;
import ENACTProducer.model.smoolcore.IContinuousInformation;
import ENACTProducer.model.smoolcore.impl.BlindPositionActuator;
import ENACTProducer.model.smoolcore.impl.ContinuousInformation;
import ENACTProducer.model.smoolcore.impl.GasInformation;
import ENACTProducer.model.smoolcore.impl.GasSensor;
import ENACTProducer.model.smoolcore.impl.LightingInformation;
import ENACTProducer.model.smoolcore.impl.LightingSensor;
import ENACTProducer.model.smoolcore.impl.SecurityAuthorization;
import ENACTProducer.model.smoolcore.impl.SmokeInformation;
import ENACTProducer.model.smoolcore.impl.SmokeSensor;
import ENACTProducer.model.smoolcore.impl.TemperatureInformation;
import ENACTProducer.model.smoolcore.impl.TemperatureSensor;

/**
 * This is a customised Producer of data for the Smart Building use case for Enact.
 * <p>
 * NOTE: although this app is mainly a producer of sensors data, it can receive
 * actuation orders.
 * </p>
 * <p>
 * This app is sending (via SMOOL) the temperature, gas, etc status in the
 * smart building. When an external app controlling this data decides that the
 * values must be re-adapted, an actuation order is sent back to this
 * application, and this one check if security data is valid and then it calls
 * the Smart Building SCADA to tune the values.
 * </p>
 *
 */
public class Customized_ProducerMain_Temp implements MqttCallback {

	public static final String vendor = "Tecnalia";
	public static final String name = "EnactProducer" + System.currentTimeMillis() % 10000;
	
	public static TemperatureSensor tempSensor;
	public static TemperatureInformation tempInfo = new TemperatureInformation(name + "_temp");
	
	LightingSensor lightingSensor;
	LightingInformation lightingInfo;
	
	SmokeSensor smokeSensor;
	SmokeInformation smokeInfo;
	
	GasSensor gasSensor;
	GasInformation gasInfo;	
	
		
	public IMqttClient publisher; 
	
	public final Gson gson = new GsonBuilder().serializeNulls().create();
	
	/**
     * If we need flexible parameters, use this constructor
     * @param sib
     * @param addr
     * @param port
     * @throws Exception
     */
	public Customized_ProducerMain_Temp(String sib, String addr, int port) throws Exception {
		SmoolKP.setKPName(name);
		System.out.println("*** " + name + " ***");
		// ---------------------------CONNECT TO SMOOL---------------------
		// SmoolKP.connect();
		// SmoolKP.connect("sib1", "172.24.5.151", 23000);
		SmoolKP.connect(sib, addr, port);

		// ---------------------------CREATE SENSOR-----------------------------
		tempSensor = new TemperatureSensor(name + "_tempSensor");
		lightingSensor = new LightingSensor(name + "_lightingSensor");
		smokeSensor = new SmokeSensor(name + "smokeSensor");
		gasSensor = new GasSensor(name + "gasSensor");//unused in Home IO
		
		double temp = 25.6789;//Initialisation
		String timestamp = Long.toString(System.currentTimeMillis());
		tempInfo.setValue(temp).setUnit("??C").setTimestamp(timestamp);
		
		
		Producer producer = SmoolKP.getProducer();		
		producer.createTemperatureSensor(Customized_ProducerMain_Temp.tempSensor._getIndividualID(), Customized_ProducerMain_Temp.name, Customized_ProducerMain_Temp.vendor, null, null, tempInfo);
		
		lightingInfo = new LightingInformation();
		double lightValue = 0;
		lightingInfo.setLightValue(lightValue);
		lightingInfo.setTimestamp(timestamp);
		producer.createLightingSensor(lightingSensor._getIndividualID(), name, timestamp, null, lightingInfo, null);
		
		smokeInfo = new SmokeInformation(name + "_smoke");
		boolean smoke = false;
		smokeInfo.setStatus(smoke).setTimestamp(timestamp);
		producer.createSmokeSensor(smokeSensor._getIndividualID(), name, vendor, null, null, smokeInfo);

		//unused in Home IO
		gasInfo = new GasInformation(name + "_CO2");
		double gas = 400;
		gasInfo.setType("CO2").setUnit("ppm").setValue(gas).setTimestamp(timestamp);
		producer.createGasSensor(gasSensor._getIndividualID(), name, vendor, null, gasInfo, null);

		//Create an MQTT client here just for testing purpose to publish any actuation command sent from SMOOL
		initMQTTclient();
		
		// ---------------------------CONSUME ACTION----------------------------------
		Consumer consumer = SmoolKP.getConsumer();
		BlindPositionActuatorSubscription subscription = new BlindPositionActuatorSubscription(createObserver());
		consumer.subscribeToBlindPositionActuator(subscription, null);
	}
	
	/**
	 * Create an MQTT client here just for testing purpose: 
	 * whenever there is an actuation command sent from SMOOL, publish it to MQTT broker 
	 */
	private void initMQTTclient() {
		try{
        	// ---------------------------MQTT Client----------------------------------
    		String publisherId = UUID.randomUUID().toString();
    		publisher = new MqttClient("tcp://localhost:1883", publisherId);
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            publisher.connect(options);
            
        }catch(Exception e){
            throw new RuntimeException("Exception occured in creating MQTT Client");
        }
	}
	
	/**
	FROM APP to SMOOL
	*/
	public void connectionLost(Throwable t) {
        System.out.println("Connection lost!");
        // code to reconnect to the broker would go here if desired
    }

    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
		//we should send it to smool
        System.out.println("-------------------------------------------------");
        System.out.println("| Topic:" + s);
        System.out.println("| Message: " + new String(mqttMessage.getPayload()));
        System.out.println("-------------------------------------------------");

        String timestamp = Long.toString(System.currentTimeMillis());
        
        if(s.equals("/home/A/Output/float/Thermostat_(Room_Temperature)")){
        	tempInfo.setValue(ByteBuffer.wrap(mqttMessage.getPayload()).getDouble()).setUnit("??C").setTimestamp(timestamp);
        	SmoolKP.getProducer().updateTemperatureSensor(tempSensor._getIndividualID(), name, vendor, null, null, tempInfo);
        }
        
        if(s.equals("/home/A/Input/float/Brightness_Sensor_(Analog)")){
        	lightingInfo.setValue(ByteBuffer.wrap(mqttMessage.getPayload()).getDouble()).setTimestamp(timestamp);
        	SmoolKP.getProducer().updateLightingSensor(lightingSensor._getIndividualID(), name, vendor, null, lightingInfo, null);
        }
        
        if(s.equals("/home/A/Input/bool/Smoke_Detector")){//hard code: mqttMessage.getPayload()) != null
        	smokeInfo.setStatus(ByteBuffer.wrap(mqttMessage.getPayload()) != null).setTimestamp(timestamp);
        	SmoolKP.getProducer().updateSmokeSensor(smokeSensor._getIndividualID(), name, vendor, null, null, smokeInfo);
        }
    }

    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        //System.out.println("Pub complete" + new String(token.getMessage().getPayload()));

    }

	/**
	 * Process messages related to actuation orders on blinds
	 */
	private Observer createObserver() {
		return (o, concept) -> {
			
			BlindPositionActuator actuator = (BlindPositionActuator) concept;
			IContinuousInformation info = actuator.getBlindPos();
			
			//Adding extra code here to mimic any calls the SCADA to send actuation orders
			Map<String, Object> record = new HashMap<>();
			
			if (info.getSecurityData() == null) {
				// insecure actuation orders are FORBIDDEN
				System.out.println("CRITICAL: only secure actuation orders are allowed");
			} else {
				// process order
				System.out.println(name + "receiving ACTUATION order on blinds. Value: " + info.getValue() + " and security "
						+ info.getSecurityData().getData());
								
				// check security is ok
				// TODO call secutron or AC agent to verify order
				record.put("id", info._getIndividualID());
				Map<String, String> sec = new HashMap<>();
				sec.put("type", info.getSecurityData().getType());
				sec.put("data", info.getSecurityData().getData());
				record.put("security", sec);
				record.put("timestamp", System.currentTimeMillis());
				sec.put("category", info.getSecurityData().getClass().getSimpleName());
				System.out.println(gson.toJson(record));
				
				//TODO call the SCADA to send the order, here mimic by publishing the commands via MQTT
								
				byte[] payload = String.format("ACTUATION:%04.2f", info.getValue()).getBytes();       
				MqttMessage msg = new MqttMessage(payload);
				
				//MqttMessage msg = new MqttMessage(String.format("T:%04.2f", info.getValue()).getBytes());
				
		        msg.setQos(0);
		        msg.setRetained(true);
		        
		        try {
		        	if(publisher.isConnected()) {
		        		//whenever there is an actuation command sent from SMOOL, publish it to MQTT broker 
		        		publisher.publish("home/A/Output/bool/Roller_Shades_1_(Down)", msg);
		        	}else {
		        		System.out.println("Cannot publish actuation commands because MqttClient is not connected!");
		        	}
				} catch (MqttPersistenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}     
			}
		};
	}

	public static void main(String[] args) throws Exception {
		String sib = args.length > 0 ? args[0] : "sib1";
		String addr = args.length > 1 ? args[1] : "smool.tecnalia.com";
		int port = args.length > 2 ? Integer.valueOf(args[2]) : 80;
		// Logger.setDebugging(true);
		// Logger.setDebugLevel(4);
		
		double temp = 25;
				
		while (true) {
			try {
				new Customized_ProducerMain_Temp(sib, addr, port);
				
				// ---------------------SEND DATA--------------------------------------------
				while (true) {
					Thread.sleep(10000);
					
					temp = temp < 26 ? temp + 0.5 : 22;
					String timestamp = Long.toString(System.currentTimeMillis());
					tempInfo.setValue(temp).setUnit("??C").setTimestamp(timestamp);
					
					SmoolKP.getProducer().updateTemperatureSensor(Customized_ProducerMain_Temp.tempSensor._getIndividualID(), Customized_ProducerMain_Temp.name, Customized_ProducerMain_Temp.vendor, null, null, tempInfo);
					
					System.out.println(Customized_ProducerMain_Temp.tempSensor._getIndividualID() + ": Producing temp  " + Double.toString(temp) + " (and more concepts)");
				}
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
