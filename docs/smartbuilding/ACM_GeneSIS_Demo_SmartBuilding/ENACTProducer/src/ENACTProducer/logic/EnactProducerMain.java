package ENACTProducer.logic;

import java.io.IOException;
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
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.smool.kpi.model.exception.KPIModelException;

import ENACTProducer.api.Consumer;
import ENACTProducer.api.MessageReceiveSensorSubscription;
import ENACTProducer.api.Producer;
import ENACTProducer.api.SmoolKP;
import ENACTProducer.model.smoolcore.IMessage;
import ENACTProducer.model.smoolcore.impl.Message;
import ENACTProducer.model.smoolcore.impl.MessageReceiveSensor;
import ENACTProducer.model.smoolcore.impl.TemperatureInformation;

/**
 * Producer of data for the Smart Building use case for Enact.
 * <p>
 * NOTE: although this app is mainly a producer of sensors data, it can receive
 * actuation orders.
 * </p>
 * <p>
 * This app is sending (via SMOOL) the temperature, gas, etc stataus in the
 * smart building. It is also receiving (via SMOOL) actuation orders, and checking if security data is valid and then it calls
 * the Smart Building SCADA to tune the values.
 * </p>
 *
 */
public class EnactProducerMain implements MqttCallback{

	public static final String vendor = "CNRS";
	public static final String name = "ACM_GeneSIS_Demo";//"EnactProducer" + System.currentTimeMillis() % 10000;
	
	public static MessageReceiveSensor sensor_luminance;
	
	public static MessageReceiveSensor camera_detection;
	
//	public static MessageSendActuator switch_binary;
	public static MessageReceiveSensor switch_binary;//Actuator but use this MessageReceiveSensor instead
	
	private IMqttClient publisher; 
	public TemperatureInformation tempInfo;

	public EnactProducerMain(String sib, String addr, int port) throws Exception {
		SmoolKP.setKPName(name);
		System.out.println("*** " + name + " ***");
		// ---------------------------CONNECT TO SMOOL---------------------
		// SmoolKP.connect();
		// SmoolKP.connect("sib1", "172.24.5.151", 23000);
		// SmoolKP.connect("sib1", "192.168.1.128", 23000);
		SmoolKP.connect(sib, addr, port);
		
		//Create an MQTT client here
		initMQTTclient();


		// ---------------------------CREATE SENSOR-----------------------------
		Producer producer = SmoolKP.getProducer();
		sensor_luminance = new MessageReceiveSensor(name + "_sensor_luminance");
		camera_detection = new MessageReceiveSensor(name + "_camera_detection");
//		switch_binary = new MessageSendActuator(name + "_switch_binary");
		switch_binary = new MessageReceiveSensor(name + "_switch_binary");

		// ---------------------------PRODUCE DATA----------------------------------
		String timestamp = Long.toString(System.currentTimeMillis());

		Message msg = new Message();
		msg.setBody("");
		msg.setTimestamp(timestamp);

		//maybe this is not necessary
		producer.createMessageReceiveSensor(sensor_luminance._getIndividualID(), name, vendor, null, null, null, msg, null);
		producer.createMessageReceiveSensor(camera_detection._getIndividualID(), name, vendor, null, null, null, msg, null);
		

		// ---------------------------CONSUME ACTION----------------------------------
		Consumer consumer = SmoolKP.getConsumer();
		
//		MessageReceiveSensorSubscription sensor_luminance_subscription = new MessageReceiveSensorSubscription();
//		consumer.subscribeToMessageReceiveSensor(sensor_luminance_subscription, sensor_luminance._getIndividualID());
		
		MessageReceiveSensorSubscription subscription_switch_binary = new MessageReceiveSensorSubscription(createObserver_switch_binary());
		consumer.subscribeToMessageReceiveSensor(subscription_switch_binary, switch_binary._getIndividualID());
		
//		LightSwitchActuatorSubscription subscription_switch_binary = new LightSwitchActuatorSubscription(createObserver_switch_binary());
//		consumer.subscribeToLightSwitchActuator(subscription_switch_binary, switch_binary._getIndividualID());		
	}
	
	private void initMQTTclient() throws InterruptedException {
		try{
        	// ---------------------------MQTT Client----------------------------------
    		String publisherId = UUID.randomUUID().toString();
    		//Connect to the MQTT broker that is connected to HomeIO app
    		//publisher = new MqttClient("tcp://192.168.1.28:1883", publisherId);
    		publisher = new MqttClient("tcp://localhost:1883", publisherId);
//    		publisher = new MqttClient("tcp://10.0.0.13:1883", publisherId);
            
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
			publisher.setCallback(this);
            publisher.connect(options);
			System.out.println("Connected to the broker to SMART BUILDING");
			
			String myTopic = "enact/sensors/zwave/doors/x/sensor_luminance";
			MqttTopic topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);

			myTopic = "enact/sensors/camera/detection";
			topic = publisher.getTopic(myTopic);
			publisher.subscribe(myTopic, 0);
			
		}catch(MqttException e){
//        	e.printStackTrace();
			Thread.sleep(5000);
			System.out.println("WAITING for the CONNECTION to the broker to SMART BUILDING...");
			initMQTTclient();
            //throw new RuntimeException("Exception occurred in creating MQTT Client");
        }catch(Exception e) {
        	//Unable to connect to server (32103) - java.net.ConnectException: Connection refused
        	e.printStackTrace();
			System.exit(1);
        }
	}
	
	/**
	FROM APP to SMOOL
	*/
	 @Override
    public void connectionLost(Throwable t) {
        System.out.println("Connection to SMART BUILDING lost!");
        
        // code to reconnect to the broker would go here if desired
        try {
        	System.out.println("RECONNECTING to the broker to SMART BUILDING...");
			initMQTTclient();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
		//Sending sensor data from SMART BUILDING to SMOOL
        
		if(s.equals("enact/sensors/zwave/doors/x/sensor_luminance")){
			
			String message_payload = new String(mqttMessage.getPayload());
			System.out.println("-------------------------------------------------");
			System.out.println("Forwarding to SMOOL: enact/sensors/zwave/doors/x/sensor_luminance = " + message_payload);
			System.out.println("-------------------------------------------------");
			
			String timestamp = Long.toString(System.currentTimeMillis());
			
			Message msg_sensor_luminance = new Message();
			msg_sensor_luminance.setBody(message_payload);
			msg_sensor_luminance.setTimestamp(timestamp);
			SmoolKP.getProducer().updateMessageReceiveSensor(sensor_luminance._getIndividualID(), name, vendor, null, null, null, msg_sensor_luminance, null);
		}
		
		if(s.equals("enact/sensors/camera/detection")){
			
			String message_payload = new String(mqttMessage.getPayload());
			System.out.println("-------------------------------------------------");
			System.out.println("Forwarding to SMOOL: enact/sensors/camera/detection = " + message_payload);
			System.out.println("-------------------------------------------------");
			
			String timestamp = Long.toString(System.currentTimeMillis());
			
			Message msg_camera_detection = new Message();
			msg_camera_detection.setBody(message_payload);
			msg_camera_detection.setTimestamp(timestamp);
			SmoolKP.getProducer().updateMessageReceiveSensor(camera_detection._getIndividualID(), name, vendor, null, null, null, msg_camera_detection, null);
		}

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
//        try {
//			System.out.println("Forwarding to SMOOL completed" + new String(iMqttDeliveryToken.getMessage().getPayload()));
//		} catch (MqttException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    }

    /**
	 * Processs messages related to actuation orders on blinds
	 */
	private Observer createObserver_switch_binary() {
		return (o, concept) -> {
			MessageReceiveSensor actuator_switch_binary = (MessageReceiveSensor) concept;
			IMessage msg_switch_binary = actuator_switch_binary.getMessage();
			
			if(actuator_switch_binary.getDeviceID().equalsIgnoreCase("ACM_GeneSIS_Demo")) {
				System.out.println("receiving Actuator Message order. Value: " + msg_switch_binary.getBody());
			}
			
			if(msg_switch_binary.getBody() != null){
				MqttMessage msg=null;
				
				msg = new MqttMessage(msg_switch_binary.getBody().getBytes());
				
				if(msg!=null){
					msg.setQos(0);
					msg.setRetained(true);
					
					try {
						if(publisher.isConnected()) {
							//whenever there is sensor data update sent from SMOOL server, send it to the Apps via MQTT broker 
							System.out.println("-------------------------------------------------");
							System.out.println("Is Forwarding Actuation Cmd to SMART BUILDING: " + new String(msg.getPayload()));
							System.out.println("-------------------------------------------------");
							publisher.publish("enact/actuators/zwave/switches/X/switch_binary", msg);
							
						}else {
							System.out.println("Cannot send to SMART BUILDING: publisher.isConnected() = " + publisher.isConnected());
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


	public static void main(String[] args) throws Exception {
		String sib = args.length > 0 ? args[0] : "sib1";
		String addr = args.length > 1 ? args[1] : "15.236.132.74";
		int port = args.length > 2 ? Integer.valueOf(args[2]) : 23000;
		// Logger.setDebugging(true);
		// Logger.setDebugLevel(4);
		while (true) {
			try {
				new EnactProducerMain(sib, addr, port);
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
