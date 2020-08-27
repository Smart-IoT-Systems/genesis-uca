package ENACTProducer.logic;

import ENACTProducer.model.smoolcore.impl.MessageReceiveSensor;

public class ACM_GeneSIS_Demo_Common {

	public static final String vendor = "CNRS";
	public static final String name = "ACM_GeneSIS_Demo";//"EnactProducer" + System.currentTimeMillis() % 10000;
	
	public static MessageReceiveSensor sensor_luminance;
	public static MessageReceiveSensor camera_detection;
	public static MessageReceiveSensor botvacD3_state;
	public static MessageReceiveSensor botvacD3_action;
	public static MessageReceiveSensor botvacD3_is_docked;
	public static MessageReceiveSensor botvacD3_is_scheduled;
	public static MessageReceiveSensor botvacD3_is_charging;
	public static MessageReceiveSensor botvacD3_command;//Actuator but use this MessageReceiveSensor instead
	public static MessageReceiveSensor microphone_record;//Actuator but use this MessageReceiveSensor instead
	public static MessageReceiveSensor microphone_sound;
	public static MessageReceiveSensor microphone_zcr;
	public static MessageReceiveSensor microphone_mfcc;
	public static MessageReceiveSensor microphone_time;
	public static MessageReceiveSensor microphone_replay;//Actuator but use this MessageReceiveSensor instead
	public static MessageReceiveSensor sensors_cec_status;
	public static MessageReceiveSensor sensors_cec_source;
	public static MessageReceiveSensor actuators_cec_source;//Actuator but use this MessageReceiveSensor instead
	public static MessageReceiveSensor actuators_cec_power;//Actuator but use this MessageReceiveSensor instead
	
//	public static MessageSendActuator switch_binary;
//	public static MessageReceiveSensor switch_binary;//Actuator but use this MessageReceiveSensor instead
	
	//IDs
	public static String sensor_luminance_id = name + "_x_sensor_luminance";
	public static String camera_detection_id = name + "_camera_detection";
	public static String botvacD3_state_id = name + "_botvacD3_state";
	public static String botvacD3_action_id = name + "_botvacD3_action";
	public static String botvacD3_is_docked_id = name + "_botvacD3_is_docked";
	public static String botvacD3_is_scheduled_id = name + "_botvacD3_is_scheduled";
	public static String botvacD3_is_charging_id = name + "_botvacD3_is_charging";
	public static String botvacD3_command_id = name + "_botvacD3_command";
	public static String microphone_record_id = name + "_microphone_record";
	public static String microphone_sound_id = name + "_microphone_sound";
	public static String microphone_zcr_id = name + "_microphone_zcr";
	public static String microphone_mfcc_id = name + "_microphone_mfcc";
	public static String microphone_time_id = name + "_microphone_time";
	public static String microphone_replay_id = name + "_microphone_replay";
	public static String sensors_cec_status_id = name + "_sensors_cec_status";
	public static String sensors_cec_source_id = name + "_sensors_cec_source";
	public static String actuators_cec_source_id = name + "_actuators_cec_source";
	public static String actuators_cec_power_id = name + "_actuators_cec_power";
	
	//TOPICS
	public static String sensor_luminance_topic = "enact/sensors/zwave/doors/x/sensor_luminance";
	public static String camera_detection_topic = "enact/sensors/camera/detection";
	public static String botvacD3_state_topic = "enact/sensors/neato/botvacD3/state";
	public static String botvacD3_action_topic = "enact/sensors/neato/botvacD3/action";
	public static String botvacD3_is_docked_topic = "enact/sensors/neato/botvacD3/is-docked";
	public static String botvacD3_is_scheduled_topic = "enact/sensors/neato/botvacD3/is-scheduled";
	public static String botvacD3_is_charging_topic = "enact/sensors/neato/botvacD3/is-charging";
	public static String botvacD3_command_topic = "enact/actuators/neato/botvacD3/command";
	public static String microphone_record_topic = "enact/actuators/microphone/record";
	public static String microphone_sound_topic = "enact/sensors/microphone/sound";
	public static String microphone_zcr_topic = "enact/sensors/microphone/zcr";
	public static String microphone_mfcc_topic = "enact/sensors/microphone/mfcc";
	public static String microphone_time_topic = "enact/sensors/microphone/time";
	public static String microphone_replay_topic = "enact/sensors/microphone/replay";
	public static String sensors_cec_status_topic = "enact/sensors/cec/status";
	public static String sensors_cec_source_topic = "enact/sensors/cec/source";
	public static String actuators_cec_source_topic = "enact/actuators/cec/source";
	public static String actuators_cec_power_topic = "enact/actuators/cec/power";
}
