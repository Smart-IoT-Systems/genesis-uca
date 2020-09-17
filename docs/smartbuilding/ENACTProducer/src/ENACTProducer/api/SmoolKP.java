
/*
 * Generated Smool KP API
 */
package ENACTProducer.api;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.function.Predicate;

import org.smool.kpi.common.Logger;
import org.smool.kpi.connector.SIBDescriptor;
import org.smool.kpi.model.IModelListener;
import org.smool.kpi.model.ModelManager;
import org.smool.kpi.model.exception.KPIModelException;
import org.smool.kpi.model.smart.AbstractOntConcept;
import org.smool.kpi.model.smart.SmartModel;
import org.smool.kpi.ssap.ISIBDiscoveryListener;
import org.smool.kpi.ssap.message.parameter.SSAPMessageRDFParameter.TypeAttribute;

import ENACTProducer.api.util.SmoolKPUtil;
import ENACTProducer.model.smoolcore.IAccelerometerInformation;
import ENACTProducer.model.smoolcore.IAlarm;
import ENACTProducer.model.smoolcore.IBlackoutInformation;
import ENACTProducer.model.smoolcore.IFloodInformation;
import ENACTProducer.model.smoolcore.IGasInformation;
import ENACTProducer.model.smoolcore.IHumidityInformation;
import ENACTProducer.model.smoolcore.ILightingInformation;
import ENACTProducer.model.smoolcore.ILogicalLocation;
import ENACTProducer.model.smoolcore.IMessage;
import ENACTProducer.model.smoolcore.INoiseInformation;
import ENACTProducer.model.smoolcore.IPhysicalLocation;
import ENACTProducer.model.smoolcore.IPresenceInformation;
import ENACTProducer.model.smoolcore.ISecurity;
import ENACTProducer.model.smoolcore.ISmokeInformation;
import ENACTProducer.model.smoolcore.ITemperatureInformation;
import ENACTProducer.model.smoolcore.impl.Accelerometer;
import ENACTProducer.model.smoolcore.impl.BlackoutSensor;
import ENACTProducer.model.smoolcore.impl.BlindPositionActuator;
import ENACTProducer.model.smoolcore.impl.FloodSensor;
import ENACTProducer.model.smoolcore.impl.GasSensor;
import ENACTProducer.model.smoolcore.impl.HVACActuator;
import ENACTProducer.model.smoolcore.impl.HumiditySensor;
import ENACTProducer.model.smoolcore.impl.LightRangeActuator;
import ENACTProducer.model.smoolcore.impl.LightSwitchActuator;
import ENACTProducer.model.smoolcore.impl.LightingSensor;
import ENACTProducer.model.smoolcore.impl.MessageReceiveSensor;
import ENACTProducer.model.smoolcore.impl.NoiseSensor;
import ENACTProducer.model.smoolcore.impl.PresenceSensor;
import ENACTProducer.model.smoolcore.impl.SmokeSensor;
import ENACTProducer.model.smoolcore.impl.TemperatureSensor;

public class SmoolKP {

	/**
	 * Reference to the Consumer interface
	 */
	private static Consumer consumer = null;
	/**
	 * Reference to the Producer interface
	 */
	private static Producer producer = null;

	/**
	 * A HashMap that contains pairs key-value of conceptID-concept to be produced
	 */
	private static HashMap<String, AbstractOntConcept> conceptMap;

	/**
	 * KP name
	 */
	private static String KP_NAME = "ENACTProducer";

	/**
	 * Reference to the object responsible of SIB discovery and model notifications
	 */
	private static DiscoveryListener dl = null;

	/**
	 * Reference to the thread that will stop the discovery
	 */
	private static DiscoveryStopper ds = null;

	/**
	 * Singleton reference
	 */
	private static SmoolKP instance = new SmoolKP();

	/**
	 * Constructor
	 */
	private SmoolKP() {
		conceptMap = new HashMap<String, AbstractOntConcept>();
		dl = this.new DiscoveryListener();
		ds = this.new DiscoveryStopper(dl);
	}

	/**
	 * Check whether the KP is connected to the SIB
	 * 
	 * @return TRUE if connected
	 */
	public static boolean isConnectedToSIB() {
		if (dl.getModel() == null) {
			return false;
		}
		return dl.getModel().isConnected();
	}

	/**
	 * Clean threads when trying to reconnect multiple times. SmoolKP does not clean
	 * resources when reconnecting (example, the TCPIPConnector thread is created
	 * and not disposed every time).
	 */
	private static void clean() {
		lastTimestamp = System.currentTimeMillis();
		try {
			if (dl != null && dl.getModel() != null) {
				// dl.getModel().disconnect();
				dl.getModel().getSIB().destroy();
			}
			if (ds != null) {
				ds.doStop();
			}
			instance = new SmoolKP();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Connect to a SIB with zero configuration.
	 * <p>
	 * If any SIB is available in the network, this method will perform a multicast
	 * ping for discovering existing SIBs, and then connect to the first SIB found
	 * </p>
	 * .
	 * 
	 * @throws IOException
	 */
	public static void connect() throws IOException {
		clean();
		SmoolKP.synchronousSearch(1000, true);
		if (SmoolKP.isSIBfound()) {
			System.out.println("Found a SIB: " + SmoolKP.getDiscoveredSIBs().get(0).getSIBName());
			if (SmoolKP.connectToSIB(1000)) {
				System.out.println("Successfully connected to SIB");
				SmoolKP.isConnected = true;
			} else {
				throw new IOException("Unable to connect to SIB");
			}
		}
	}

	/**
	 * Connect to a SIB
	 * 
	 * @param name
	 *                    the name of the SIB (typical values are "sib", "sib1",
	 *                    etc...)
	 * @param address
	 *                    the IP or hostname where the SIB is running
	 *                    ("192.168.1.5", "sib.acme.com", etc...)
	 * @param port
	 *                    TCP port where the SIB is listening
	 * @throws IOException
	 */
	public static void connect(String name, String address, int port) throws IOException {
		clean();
		SmoolKP.isConnected = SmoolKP.connectToSIB(name, address, Integer.toString(port), 1000);
		if (SmoolKP.isConnected) {
			System.out.println("Successfully connected to SIB");
		} else {
			throw new IOException("Unable to connect to SIB");
		}
	}

	/**
	 * Connect to a specific SIB. This method blocks until the connection process
	 * has ended. This method is equivalent to: connectToSIB(sibName, sibAddress,
	 * sibPort, 0)
	 * 
	 * @param sibName.
	 *            The name of the SIB.
	 * @param sibAddress.
	 *            The IP Address of the SIB
	 * @param sibPort.
	 *            The TCP port.
	 * @return true if the connection was successful.
	 */
	public static boolean connectToSIB(String sibName, String sibAddress, String sibPort) {
		return connectToSIB(sibName, sibAddress, sibPort, 0);
	}

	/**
	 * Connect to a specific SIB. This method blocks until either the connection
	 * process has ended or the timeout expires.
	 * 
	 * @param sibName.
	 *            The name of the SIB.
	 * @param sibAddress.
	 *            The IP Address of the SIB
	 * @param sibPort.
	 *            The TCP port.
	 * @param milliseconds.
	 *            The timeout time lapse in milliseconds.
	 * @return true if the connection was successful.
	 */
	public static boolean connectToSIB(String sibName, String sibAddress, String sibPort, int milliseconds) {

		if (dl.getModel() != null && dl.getModel().isConnected()) {
			Logger.error("Already connected to SIB");
			return false;
		} else if (KP_NAME == null || KP_NAME.equals("")) {
			Logger.error("No KP Name was provided. Cannot connect.");
			return false;
		} else {
			try {

				java.util.Properties p = new java.util.Properties();
				p.setProperty("HOST", sibAddress);
				p.setProperty("PORT", sibPort);
				SIBDescriptor theSIB = new SIBDescriptor(sibName, "TCP/IP", p);

				SmartModel model = ModelManager.getInstance().createModel(KP_NAME, theSIB);
				model.addModelListener(dl); // listen to sib events
				model.setTimeout(10000);
				dl.setModel(model);
				dl.getModel().connect(false);

				dl.setSIBParams(sibAddress, sibPort);

				if (milliseconds > 0) {
					int count = 0;
					int maxCount = (milliseconds / 5) > 0 ? 5 : 1;
					while (!dl.getModel().isConnected() && count++ < maxCount) {
						Thread.sleep(milliseconds / maxCount);
					}
					return dl.getModel().isConnected();
				} else {
					while (!dl.getModel().isConnected()) {
						Thread.sleep(100);
					}
					return true;
				}
			} catch (KPIModelException kpie) {
				Logger.error("Failed to connect to SIB...");
				return false;
			} catch (InterruptedException ie) {
				return dl.getModel().isConnected();
			}
		}

	}

	/**
	 * Method used to connect to the first SIB discovered. The method blocks until
	 * the connection process has finished. This method is equivalent to
	 * connectToSIB(0, 0)
	 * 
	 * @return TRUE if connection was successful
	 */
	public static boolean connectToSIB() {
		return connectToSIB(0, 0);
	}

	/**
	 * Method used to connect to the first SIB discovered. The method blocks until
	 * the connection process has finished or the provided timeout expires.
	 * 
	 * @param milliseconds
	 *                         the timeout value in milliseconds. If milliseconds <=
	 *                         0 no timeout is set.
	 * @return TRUE if connection was successful
	 */
	public static boolean connectToSIB(int milliseconds) {
		return connectToSIB(milliseconds, 0);
	}

	/**
	 * Method used to connect to any SIB discovered. The method blocks until the
	 * connection process has finished or the provided timeout expires.
	 * 
	 * @param milliseconds
	 *                         the timeout value in milliseconds. If milliseconds <=
	 *                         0 no timeout is set.
	 * @param sibNumber
	 *                         the array number of the discovered SIB to connect to.
	 * @return TRUE if connection was successful
	 */
	public static boolean connectToSIB(int milliseconds, int sibNumber) {

		if (dl.getModel() != null && dl.getModel().isConnected()) {
			Logger.error("Already connected to SIB");
			return false;
		} else {
			try {

				Vector<SIBDescriptor> desc = dl.getSIBDescriptors();
				if (sibNumber > desc.size()) {
					Logger.error("No such SIB...");
					return false;
				}

				SIBDescriptor theSIB = desc.get(sibNumber);
				if (theSIB == null) {
					Logger.error("Failed to get the SIB descriptor");
					return false;
				}
				// Angel 05/11/14, DEFAULT_IPADDRESS is not always in properties
				String tempParam = theSIB.getProperties().getProperty("DEFAULT_IPADDRESS");
				if (tempParam == null || tempParam.equals("")) {
					Logger.warn("DEFAULT_IPADDRESS not found!! trying to add ADDRESSES instead");
					theSIB.getProperties().setProperty("DEFAULT_IPADDRESS",
							theSIB.getProperties().getProperty("ADDRESSES"));
				}

				// TODO Angel: (long term) review list of sib properties and remove obsolete or
				// error-prone properties (ADDRESSES and PORT are good, DEFAULT_IPADDRESSis
				// mandatory but it collides with ADDRESSES and HOST is an old property)

				SmartModel model = ModelManager.getInstance().createModel(KP_NAME, theSIB);
				model.addModelListener(dl); // listen to sib events
				model.setTimeout(10000);
				dl.setModel(model);
				dl.getModel().connect(false);

				dl.setSIBParams(theSIB.getProperties().getProperty("DEFAULT_IPADDRESS"),
						theSIB.getProperties().getProperty("PORT"));

				if (milliseconds > 0) {
					int count = 0;
					int maxCount = (milliseconds / 5) > 0 ? 5 : 1;
					while (!dl.getModel().isConnected() && count++ < maxCount) {
						Thread.sleep(milliseconds / maxCount);
					}
					return dl.getModel().isConnected();
				} else {
					while (!dl.getModel().isConnected()) {
						Thread.sleep(100);
					}
					return true;
				}
			} catch (Exception e) {
				Logger.error("Could not connect to SIB. Please try again");
				return false;
			}
		}
	}

	/**
	 * Used to disconnect from SIB
	 * 
	 * @return TRUE if disconnection was successful
	 */
	public static boolean disconnectFromSIB() {

		if (dl.getModel() == null) {
			Logger.error("Model is null, cannot disconnect");
			return false;
		} else if (!dl.getModel().isConnected()) {
			return true;
		}
		try {
			if (SmoolKPUtil.isMachineReacheable(dl.getSIBAddress(), Integer.parseInt(dl.getSIBPort()), "TCP")) {
				// Logger.debug("Disconnecting from SIB...");
				dl.getModel().disconnect();
				Logger.debug("Disconnected successfully from SIB!");
				isConnected = false;
				dl.setSIBParams(null, null);
			}
			return true;
		} catch (KPIModelException e) {
			Logger.error("Could not disconnect to SIB. Please try again");
			return false;
		}
	}

	/**
	 * Used to start searching for SIBs asynchronously (non-blocking). The search
	 * will be stopped when the first SIB is discovered. It is equivalent to
	 * asynchronousSearch(true).
	 */
	public static void asynchronousSearch() {
		asynchronousSearch(true);
	}

	/**
	 * Used to start searching for SIBs asynchronously (non-blocking).
	 * 
	 * @param stopWithFirstSIB
	 *                             true if the search should be stopped when the
	 *                             first SIB is located. False otherwise.
	 */
	public static void asynchronousSearch(boolean stopWithFirstSIB) {
		if (KP_NAME == null || KP_NAME.equals("")) {
			Logger.error("No KPName defined. Cannot start search.");
			return;
		}

		Logger.debug("Looking for SIBs in the surroundings");
		// create a discoveryListener and start looking for SIBs
		if (dl == null) {
			dl = instance.new DiscoveryListener();
		}
		ModelManager.getInstance().addSIBDiscoveryListener(dl);
		dl.discoverSIBs(true);

		if (stopWithFirstSIB) {
			if (ds == null) {
				ds = instance.new DiscoveryStopper(dl);
				ds.start();
			}
		}
	}

	/**
	 * Used to start searching for SIBs synchronously (blocking). The method returns
	 * when the first SIB is located.
	 */
	public static void synchronousSearch() {
		synchronousSearch(0, true);
	}

	/**
	 * Used to start searching for SIBs synchronously (blocking). The method returns
	 * when the first SIB is located or when the timeout expires.
	 * 
	 * @param milliseconds
	 *                         the timeout in milliseconds. If milliseconds <= 0 no
	 *                         timeout is set.
	 */
	public static void synchronousSearch(int milliseconds) {
		synchronousSearch(milliseconds, true);
	}

	/**
	 * Used to start searching for SIBs synchronously (blocking). The method may
	 * return when the first SIB is located or when the timeout expires.
	 * 
	 * @param milliseconds
	 *                             the timeout in milliseconds. If milliseconds <= 0
	 *                             no timeout is set.
	 * @param stopWithFirstSIB
	 *                             true if the search should be stopped when the
	 *                             first SIB is located. False otherwise.
	 */
	public static void synchronousSearch(int milliseconds, boolean stopWithFirstSIB) {
		Logger.debug("Looking for SIBs in the surroundings");
		// create a discoveryListener and start looking for SIBs
		// dl = instance.new DiscoveryListener();
		ModelManager.getInstance().addSIBDiscoveryListener(dl);
		dl.discoverSIBs(true);

		if (stopWithFirstSIB) {
			if (ds == null) {
				ds = instance.new DiscoveryStopper(dl);
			}
			ds.start();
		}

		try {
			if (milliseconds > 0) {
				int count = 0;
				int maxCount = (milliseconds / 5) > 0 ? 5 : 1;
				while (!isSIBfound() && count++ < maxCount) {
					Thread.sleep(milliseconds / maxCount);
				}
			} else {
				while (!isSIBfound() && dl.isLookingForSIBs()) {
					Thread.sleep(100);
				}
			}
		} catch (InterruptedException ie) {
		}

	}

	/**
	 * Used to stop searching for SIBs
	 */
	public static void stopSearch() {
		dl.stopDiscovery();
		if (ds != null) {
			ds.doStop();
			// ds = null;
		}
	}

	/**
	 * Checks if the DiscoveryListener has found a SIB
	 * 
	 * @return TRUE if DiscoveryListener found a SIB
	 */
	public static boolean isSIBfound() {
		return dl.sibDiscovered;
	}

	/**
	 * Returns the discovered SIB list.
	 * 
	 * @return a Vector containing the results.
	 */
	public static Vector<SIBDescriptor> getDiscoveredSIBs() {
		return dl.getSIBDescriptors();
	}

	/**
	 * Returns the singleton Producer instance
	 * 
	 * @return the instance
	 * @throws KPIModelException
	 *                               if an error occurs
	 */
	public static Producer getProducer() throws KPIModelException {
		if (producer == null) {
			producer = instance.new ProducerImpl();
		}
		return producer;
	}

	/**
	 * Returns the singleton Consumer instance
	 * 
	 * @return the instance
	 * @throws KPIModelException
	 *                               if an error occurs
	 */
	public static Consumer getConsumer() throws KPIModelException {
		if (consumer == null) {
			consumer = instance.new ConsumerImpl();
		}
		return consumer;
	}

	/**
	 * Modify the default KP name
	 * 
	 * @param kpName
	 *                   the new KP name.
	 */
	public static void setKPName(String kpName) {
		SmoolKP.KP_NAME = kpName;
	}

	/**
	 * This class is used for the implementation of connection and disconnection
	 * from the SIB, based on a SIB Discovering approach.
	 *
	 */
	private class DiscoveryListener implements ISIBDiscoveryListener, IModelListener {

		/**
		 * Reference to the smart model
		 */
		private SmartModel model = null;

		/**
		 * List to store discovered SIBs
		 */
		private Vector<SIBDescriptor> sdList = new Vector<SIBDescriptor>();

		/**
		 * Flag to control if discovery must be stopped once a SIB is found
		 */
		private boolean stopWhenSIBDiscovered;

		/**
		 * Flag to control if discovery must be stopped once a SIB is found
		 */
		private boolean lookingForSIBs;

		/**
		 * Used to store the IP from the SIB
		 */
		private String SIBAddress = null;

		/**
		 * Used to store the listening Port from the SIB
		 */
		private String SIBPort = null;

		/**
		 * Used to control if a SIB is discovered
		 */
		private boolean sibDiscovered;

		/**
		 * Constructor
		 */
		public DiscoveryListener() {
			this.model = null;
			this.stopWhenSIBDiscovered = false;
			this.sibDiscovered = false;
			this.lookingForSIBs = false;
		}

		/**
		 * Get the value of the lookingForSIBs flag.
		 * 
		 * @return value of the flag.
		 */
		public boolean isLookingForSIBs() {
			return lookingForSIBs;
		}

		/**
		 * Set the address and port params for the SIB.
		 * 
		 * @param sibAddress
		 * @param sibPort
		 */
		public void setSIBParams(String sibAddress, String sibPort) {
			this.SIBAddress = sibAddress;
			this.SIBPort = sibPort;
		}

		/**
		 * Implementation for the method of the interface ISIBDiscoveryListener. This
		 * method is automatically called when a SIB is found, invoked from the
		 * ModelManager class
		 */
		public void SIBDiscovered(SIBDescriptor sd) {

			sdList.add(sd); // simply add it to a list
			sibDiscovered = true;
			// Angel 04/11/14 solve issue #55. Stop thread when a sib has been discovered
			if (stopWhenSIBDiscovered) {
				try {
					ModelManager.getInstance().stopLookForSIB();
				} catch (KPIModelException e) {
					Logger.error("Could not stop looking for more SIBs");
				}
			}

		}

		public void SIBConnectorDiscoveryFinished(String connectorName) {
			// Not used
		}

		/**
		 * Implementation for the method of the interface ISIBDiscoveryListener. This
		 * method is automatically called when it is finished the search of SIBs
		 */
		public void SIBDiscoveryFinished() {
			try {
				// tell the model to stop looking for SIBS
				ModelManager.getInstance().stopLookForSIB();
				if (sdList.size() == 0) {
					Logger.debug("No SIB Descriptors were found. Impossible to connect. ");
					return;
				}
			} catch (Exception e) {
				Logger.error("Could not establish a link with discovered SIB.");
			}
		}

		/**
		 * Gets the Smart Model
		 * 
		 * @return the Smart Model
		 */
		public SmartModel getModel() {
			return model;
		}

		/**
		 * Sets the Smart Model
		 * 
		 * @param model
		 */
		void setModel(SmartModel model) {
			this.model = model;
		}

		/**
		 * Check the value of the sibDiscovered flag and stop discovery if needed.
		 * 
		 * @return the value of the flag.
		 */
		public boolean isSIBDiscovered() {
			if (sibDiscovered && stopWhenSIBDiscovered && lookingForSIBs) {
				stopDiscovery();
			}
			return sibDiscovered;
		}

		/**
		 * Getter for SIBAddress
		 * 
		 * @return SIBAddress
		 */
		public String getSIBAddress() {
			return SIBAddress;
		}

		/**
		 * Getter for SIBPort
		 * 
		 * @return SIBPort
		 */
		public String getSIBPort() {
			return SIBPort;
		}

		/**
		 * Returns the discovered SIB list
		 */
		public Vector<SIBDescriptor> getSIBDescriptors() {
			return sdList;
		}

		/**
		 * Starts the inquiry of the SIBs
		 */
		public void discoverSIBs() {
			discoverSIBs(true);
		}

		/**
		 * Starts the inquiry of the SIBs
		 */
		public void discoverSIBs(boolean stopWhenFound) {
			stopWhenSIBDiscovered = stopWhenFound;
			lookingForSIBs = true;
			sdList.removeAllElements();
			try {
				ModelManager.getInstance().lookForSIB(); // starts looking for SIBs
			} catch (KPIModelException e) {
				Logger.error("Error during SIB discovery");
			}
		}

		public void stopDiscovery() {
			try {
				ModelManager.getInstance().stopLookForSIB();
			} catch (KPIModelException e) {
				e.printStackTrace();
				Logger.error("Error stopping SIB discovery");
			}
			lookingForSIBs = false;
		}

		/**
		 * Implementation of the IModelListener method that is notified when the
		 * SmartModel connects to the SIB
		 */
		public void connected() {
			Logger.debug("KP connected susccesfully!");
		}

		/**
		 * Implementation of the IModelListener method that is notified when the SIB is
		 * disconnected or stopped
		 */
		public void disconnected() {
			Logger.debug("KP disconnected susccesfully!");
			sibDiscovered = false;
			isConnected = false;
			// this.discoverSIBs();
		}

		/**
		 * Implementation of the IModelListener method that is notified when a SIB error
		 * arises, and cannot longer create a SIB
		 */
		public void connectionError(String error) {
			Logger.debug("There was a problem trying to connect to SIB:" + error);
			isConnected = false;
			this.discoverSIBs();

		}

		/**
		 * Implementation of the IModelListener method that is notified when a SIB is
		 * initalized
		 */
		public void initialized() {
			// Not used...
			// Logger.debug("Model initialized and inserted correctly into SIB");
		}

		/**
		 * Implementation of the IModelListener method that is notified when a something
		 * is published in the SIB
		 */
		public void published() {
			Logger.debug("Model has been published correctly");
		}

	}

	private class DiscoveryStopper extends Thread {
		private boolean running;
		private DiscoveryListener dl;

		public DiscoveryStopper(DiscoveryListener dl) {
			running = false;
			this.dl = dl;
		}

		public void doStop() {
			running = false;
			try {
				this.join(5000);
			} catch (InterruptedException e) {
			}
		}

		public void run() {
			boolean done = false;
			running = true;
			while (running && !done) {
				if (dl.isSIBDiscovered()) {
					done = true;
					dl.stopDiscovery();
				} else {
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	private class ProducerImpl implements Producer {

		public ProducerImpl() throws KPIModelException {
			if (!SmoolKP.isConnectedToSIB()) {
				SmoolKP.connectToSIB();
			}
		}

		public String createAccelerometer(String elemID, String deviceID, String vendor,
				IAccelerometerInformation accinfo, java.util.List<IAlarm> alarms, IPhysicalLocation physicalLoc)
				throws KPIModelException {

			if (elemID == null)
				throw new KPIModelException("Concepts must have an ID");

			if (conceptMap.containsKey(elemID)) {
				throw new KPIModelException("Cannot create two concepts with the same ID");
			}

			// check that we're connected
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot create a new concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot create a new concept. Connection lost. Try to reconnect");
			}
			Accelerometer concept = new Accelerometer();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			concept.setAccinfo(accinfo);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setPhysicalLoc(physicalLoc);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateAccelerometer(String elemID, String deviceID, String vendor,
				IAccelerometerInformation accinfo, java.util.List<IAlarm> alarms, IPhysicalLocation physicalLoc)
				throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof Accelerometer)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, accinfo, alarms, physicalLoc);

			Collection<Object> vals = null;
			((Accelerometer) concept).setDeviceID(deviceID);
			((Accelerometer) concept).setVendor(vendor);
			((Accelerometer) concept).setAccinfo(accinfo);
			vals = new ArrayList<Object>(((Accelerometer) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((Accelerometer) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((Accelerometer) concept).addAlarms(elem);
				}
			}
			((Accelerometer) concept).setPhysicalLoc(physicalLoc);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeAccelerometer(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof Accelerometer)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		public String createBlackoutSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				IBlackoutInformation blackout, IPhysicalLocation physicalLoc) throws KPIModelException {

			if (elemID == null)
				throw new KPIModelException("Concepts must have an ID");

			if (conceptMap.containsKey(elemID)) {
				throw new KPIModelException("Cannot create two concepts with the same ID");
			}

			// check that we're connected
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot create a new concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot create a new concept. Connection lost. Try to reconnect");
			}
			BlackoutSensor concept = new BlackoutSensor();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setBlackout(blackout);
			concept.setPhysicalLoc(physicalLoc);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateBlackoutSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				IBlackoutInformation blackout, IPhysicalLocation physicalLoc) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof BlackoutSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, blackout, physicalLoc);

			Collection<Object> vals = null;
			((BlackoutSensor) concept).setDeviceID(deviceID);
			((BlackoutSensor) concept).setVendor(vendor);
			vals = new ArrayList<Object>(((BlackoutSensor) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((BlackoutSensor) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((BlackoutSensor) concept).addAlarms(elem);
				}
			}
			((BlackoutSensor) concept).setBlackout(blackout);
			((BlackoutSensor) concept).setPhysicalLoc(physicalLoc);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeBlackoutSensor(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof BlackoutSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		public String createFloodSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				IFloodInformation flood, IPhysicalLocation physicalLoc) throws KPIModelException {

			if (elemID == null)
				throw new KPIModelException("Concepts must have an ID");

			if (conceptMap.containsKey(elemID)) {
				throw new KPIModelException("Cannot create two concepts with the same ID");
			}

			// check that we're connected
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot create a new concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot create a new concept. Connection lost. Try to reconnect");
			}
			FloodSensor concept = new FloodSensor();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setFlood(flood);
			concept.setPhysicalLoc(physicalLoc);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateFloodSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				IFloodInformation flood, IPhysicalLocation physicalLoc) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof FloodSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, flood, physicalLoc);

			Collection<Object> vals = null;
			((FloodSensor) concept).setDeviceID(deviceID);
			((FloodSensor) concept).setVendor(vendor);
			vals = new ArrayList<Object>(((FloodSensor) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((FloodSensor) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((FloodSensor) concept).addAlarms(elem);
				}
			}
			((FloodSensor) concept).setFlood(flood);
			((FloodSensor) concept).setPhysicalLoc(physicalLoc);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeFloodSensor(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof FloodSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		public String createGasSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				IGasInformation gas, IPhysicalLocation physicalLoc) throws KPIModelException {

			if (elemID == null)
				throw new KPIModelException("Concepts must have an ID");

			if (conceptMap.containsKey(elemID)) {
				throw new KPIModelException("Cannot create two concepts with the same ID");
			}

			// check that we're connected
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot create a new concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot create a new concept. Connection lost. Try to reconnect");
			}
			GasSensor concept = new GasSensor();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setGas(gas);
			concept.setPhysicalLoc(physicalLoc);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateGasSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				IGasInformation gas, IPhysicalLocation physicalLoc) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof GasSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, gas, physicalLoc);

			Collection<Object> vals = null;
			((GasSensor) concept).setDeviceID(deviceID);
			((GasSensor) concept).setVendor(vendor);
			vals = new ArrayList<Object>(((GasSensor) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((GasSensor) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((GasSensor) concept).addAlarms(elem);
				}
			}
			((GasSensor) concept).setGas(gas);
			((GasSensor) concept).setPhysicalLoc(physicalLoc);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeGasSensor(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof GasSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		public String createHumiditySensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				IHumidityInformation humidity, IPhysicalLocation physicalLoc) throws KPIModelException {

			if (elemID == null)
				throw new KPIModelException("Concepts must have an ID");

			if (conceptMap.containsKey(elemID)) {
				throw new KPIModelException("Cannot create two concepts with the same ID");
			}

			// check that we're connected
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot create a new concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot create a new concept. Connection lost. Try to reconnect");
			}
			HumiditySensor concept = new HumiditySensor();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setHumidity(humidity);
			concept.setPhysicalLoc(physicalLoc);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateHumiditySensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				IHumidityInformation humidity, IPhysicalLocation physicalLoc) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof HumiditySensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, humidity, physicalLoc);

			Collection<Object> vals = null;
			((HumiditySensor) concept).setDeviceID(deviceID);
			((HumiditySensor) concept).setVendor(vendor);
			vals = new ArrayList<Object>(((HumiditySensor) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((HumiditySensor) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((HumiditySensor) concept).addAlarms(elem);
				}
			}
			((HumiditySensor) concept).setHumidity(humidity);
			((HumiditySensor) concept).setPhysicalLoc(physicalLoc);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeHumiditySensor(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof HumiditySensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		public String createLightingSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				ILightingInformation lighting, IPhysicalLocation physicalLoc) throws KPIModelException {

			if (elemID == null)
				throw new KPIModelException("Concepts must have an ID");

			if (conceptMap.containsKey(elemID)) {
				throw new KPIModelException("Cannot create two concepts with the same ID");
			}

			// check that we're connected
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot create a new concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot create a new concept. Connection lost. Try to reconnect");
			}
			LightingSensor concept = new LightingSensor();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setLighting(lighting);
			concept.setPhysicalLoc(physicalLoc);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateLightingSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				ILightingInformation lighting, IPhysicalLocation physicalLoc) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof LightingSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, lighting, physicalLoc);

			Collection<Object> vals = null;
			((LightingSensor) concept).setDeviceID(deviceID);
			((LightingSensor) concept).setVendor(vendor);
			vals = new ArrayList<Object>(((LightingSensor) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((LightingSensor) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((LightingSensor) concept).addAlarms(elem);
				}
			}
			((LightingSensor) concept).setLighting(lighting);
			((LightingSensor) concept).setPhysicalLoc(physicalLoc);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeLightingSensor(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof LightingSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		public String createMessageReceiveSensor(String elemID, String deviceID, String vendor,
				java.util.List<IAlarm> alarms, java.util.List<ILogicalLocation> destination,
				ILogicalLocation logicalLoc, IMessage message, ILogicalLocation origin) throws KPIModelException {

			if (elemID == null)
				throw new KPIModelException("Concepts must have an ID");

			if (conceptMap.containsKey(elemID)) {
				throw new KPIModelException("Cannot create two concepts with the same ID");
			}

			// check that we're connected
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot create a new concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot create a new concept. Connection lost. Try to reconnect");
			}
			MessageReceiveSensor concept = new MessageReceiveSensor();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			if (destination != null) {
				for (ILogicalLocation elem : destination) {
					concept.addDestination(elem);
				}
			}
			concept.setLogicalLoc(logicalLoc);
			concept.setMessage(message);
			concept.setOrigin(origin);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateMessageReceiveSensor(String elemID, String deviceID, String vendor,
				java.util.List<IAlarm> alarms, java.util.List<ILogicalLocation> destination,
				ILogicalLocation logicalLoc, IMessage message, ILogicalLocation origin) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof MessageReceiveSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, destination, logicalLoc, message, origin);

			Collection<Object> vals = null;
			((MessageReceiveSensor) concept).setDeviceID(deviceID);
			((MessageReceiveSensor) concept).setVendor(vendor);
			vals = new ArrayList<Object>(
					((MessageReceiveSensor) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((MessageReceiveSensor) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((MessageReceiveSensor) concept).addAlarms(elem);
				}
			}
			vals = new ArrayList<Object>(
					((MessageReceiveSensor) concept)._getNonFunctionalProperty("destination").listValues());
			if (vals != null)
				((MessageReceiveSensor) concept)._getNonFunctionalProperty("destination").removeAll(vals);

			if (destination != null) {
				for (ILogicalLocation elem : destination) {
					((MessageReceiveSensor) concept).addDestination(elem);
				}
			}
			((MessageReceiveSensor) concept).setLogicalLoc(logicalLoc);
			((MessageReceiveSensor) concept).setMessage(message);
			((MessageReceiveSensor) concept).setOrigin(origin);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeMessageReceiveSensor(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof MessageReceiveSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		public String createNoiseSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				INoiseInformation noise, IPhysicalLocation physicalLoc) throws KPIModelException {

			if (elemID == null)
				throw new KPIModelException("Concepts must have an ID");

			if (conceptMap.containsKey(elemID)) {
				throw new KPIModelException("Cannot create two concepts with the same ID");
			}

			// check that we're connected
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot create a new concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot create a new concept. Connection lost. Try to reconnect");
			}
			NoiseSensor concept = new NoiseSensor();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setNoise(noise);
			concept.setPhysicalLoc(physicalLoc);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateNoiseSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				INoiseInformation noise, IPhysicalLocation physicalLoc) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof NoiseSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, noise, physicalLoc);

			Collection<Object> vals = null;
			((NoiseSensor) concept).setDeviceID(deviceID);
			((NoiseSensor) concept).setVendor(vendor);
			vals = new ArrayList<Object>(((NoiseSensor) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((NoiseSensor) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((NoiseSensor) concept).addAlarms(elem);
				}
			}
			((NoiseSensor) concept).setNoise(noise);
			((NoiseSensor) concept).setPhysicalLoc(physicalLoc);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeNoiseSensor(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof NoiseSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		public String createPresenceSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				IPhysicalLocation physicalLoc, IPresenceInformation presence) throws KPIModelException {

			if (elemID == null)
				throw new KPIModelException("Concepts must have an ID");

			if (conceptMap.containsKey(elemID)) {
				throw new KPIModelException("Cannot create two concepts with the same ID");
			}

			// check that we're connected
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot create a new concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot create a new concept. Connection lost. Try to reconnect");
			}
			PresenceSensor concept = new PresenceSensor();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setPhysicalLoc(physicalLoc);
			concept.setPresence(presence);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updatePresenceSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				IPhysicalLocation physicalLoc, IPresenceInformation presence) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof PresenceSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, physicalLoc, presence);

			Collection<Object> vals = null;
			((PresenceSensor) concept).setDeviceID(deviceID);
			((PresenceSensor) concept).setVendor(vendor);
			vals = new ArrayList<Object>(((PresenceSensor) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((PresenceSensor) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((PresenceSensor) concept).addAlarms(elem);
				}
			}
			((PresenceSensor) concept).setPhysicalLoc(physicalLoc);
			((PresenceSensor) concept).setPresence(presence);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removePresenceSensor(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof PresenceSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		public String createSmokeSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				IPhysicalLocation physicalLoc, ISmokeInformation smoke) throws KPIModelException {

			if (elemID == null)
				throw new KPIModelException("Concepts must have an ID");

			if (conceptMap.containsKey(elemID)) {
				throw new KPIModelException("Cannot create two concepts with the same ID");
			}

			// check that we're connected
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot create a new concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot create a new concept. Connection lost. Try to reconnect");
			}
			SmokeSensor concept = new SmokeSensor();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setPhysicalLoc(physicalLoc);
			concept.setSmoke(smoke);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateSmokeSensor(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				IPhysicalLocation physicalLoc, ISmokeInformation smoke) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof SmokeSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, physicalLoc, smoke);

			Collection<Object> vals = null;
			((SmokeSensor) concept).setDeviceID(deviceID);
			((SmokeSensor) concept).setVendor(vendor);
			vals = new ArrayList<Object>(((SmokeSensor) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((SmokeSensor) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((SmokeSensor) concept).addAlarms(elem);
				}
			}
			((SmokeSensor) concept).setPhysicalLoc(physicalLoc);
			((SmokeSensor) concept).setSmoke(smoke);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeSmokeSensor(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof SmokeSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		public String createTemperatureSensor(String elemID, String deviceID, String vendor,
				java.util.List<IAlarm> alarms, IPhysicalLocation physicalLoc, ITemperatureInformation temperature)
				throws KPIModelException {

			if (elemID == null)
				throw new KPIModelException("Concepts must have an ID");

			if (conceptMap.containsKey(elemID)) {
				throw new KPIModelException("Cannot create two concepts with the same ID");
			}

			// check that we're connected
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot create a new concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot create a new concept. Connection lost. Try to reconnect");
			}
			TemperatureSensor concept = new TemperatureSensor();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setPhysicalLoc(physicalLoc);
			concept.setTemperature(temperature);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateTemperatureSensor(String elemID, String deviceID, String vendor,
				java.util.List<IAlarm> alarms, IPhysicalLocation physicalLoc, ITemperatureInformation temperature)
				throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof TemperatureSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, physicalLoc, temperature);

			Collection<Object> vals = null;
			((TemperatureSensor) concept).setDeviceID(deviceID);
			((TemperatureSensor) concept).setVendor(vendor);
			vals = new ArrayList<Object>(
					((TemperatureSensor) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((TemperatureSensor) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((TemperatureSensor) concept).addAlarms(elem);
				}
			}
			((TemperatureSensor) concept).setPhysicalLoc(physicalLoc);
			((TemperatureSensor) concept).setTemperature(temperature);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeTemperatureSensor(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof TemperatureSensor)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		/**
		 * Security concepts are intended to travel along with the KP messages, but if
		 * no changes, the KP treat them as unchanged values and it does not send in the
		 * messages (the KP sends only concepts that have been changed).
		 * 
		 * <p>
		 * This method forces the security data to be sent always (or while the security
		 * parameter in update() is not null).
		 * </p>
		 * 
		 * <p>
		 * Example: once authorization token is retrieved, all the messages should sent
		 * it as header.
		 * </p>
		 */
		public void persistSecurityConcepts(Object... objects) {
			for (Object obj : objects) {
				Method m;
				try {
					m = obj.getClass().getMethod("getSecurityData");
					ISecurity sec = (ISecurity) m.invoke(obj);
					if (sec != null) {
						String d = sec.getData() != null ? new String(sec.getData()) : "";
						String t = sec.getType() != null ? new String(sec.getType()) : "";
						sec.setData(d);
						sec.setType(t);
					}
				} catch (Exception e) {
					;
				}
			}
		}

	}

	private class ConsumerImpl implements Consumer {

		public ConsumerImpl() throws KPIModelException {
			if (!SmoolKP.isConnectedToSIB()) {
				SmoolKP.connectToSIB();
			}
		}

		public void subscribeToBlindPositionActuator(BlindPositionActuatorSubscription subscription,
				String individualID) throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(BlindPositionActuator.class, subscription);
			} else {
				dl.getModel().subscribe(BlindPositionActuator.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToBlindPositionActuator(BlindPositionActuatorSubscription subscription)
				throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<BlindPositionActuator> queryAllBlindPositionActuator() throws KPIModelException {
			return dl.getModel().query(BlindPositionActuator.class, TypeAttribute.RDFM3);
		}

		public BlindPositionActuator queryBlindPositionActuator(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(BlindPositionActuator.class, individualID, TypeAttribute.RDFM3);
		}

		public void subscribeToHVACActuator(HVACActuatorSubscription subscription, String individualID)
				throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(HVACActuator.class, subscription);
			} else {
				dl.getModel().subscribe(HVACActuator.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToHVACActuator(HVACActuatorSubscription subscription) throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<HVACActuator> queryAllHVACActuator() throws KPIModelException {
			return dl.getModel().query(HVACActuator.class, TypeAttribute.RDFM3);
		}

		public HVACActuator queryHVACActuator(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(HVACActuator.class, individualID, TypeAttribute.RDFM3);
		}

		public void subscribeToLightRangeActuator(LightRangeActuatorSubscription subscription, String individualID)
				throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(LightRangeActuator.class, subscription);
			} else {
				dl.getModel().subscribe(LightRangeActuator.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToLightRangeActuator(LightRangeActuatorSubscription subscription)
				throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<LightRangeActuator> queryAllLightRangeActuator() throws KPIModelException {
			return dl.getModel().query(LightRangeActuator.class, TypeAttribute.RDFM3);
		}

		public LightRangeActuator queryLightRangeActuator(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(LightRangeActuator.class, individualID, TypeAttribute.RDFM3);
		}

		public void subscribeToLightSwitchActuator(LightSwitchActuatorSubscription subscription, String individualID)
				throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(LightSwitchActuator.class, subscription);
			} else {
				dl.getModel().subscribe(LightSwitchActuator.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToLightSwitchActuator(LightSwitchActuatorSubscription subscription)
				throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<LightSwitchActuator> queryAllLightSwitchActuator() throws KPIModelException {
			return dl.getModel().query(LightSwitchActuator.class, TypeAttribute.RDFM3);
		}

		public LightSwitchActuator queryLightSwitchActuator(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(LightSwitchActuator.class, individualID, TypeAttribute.RDFM3);
		}

		public void subscribeToMessageReceiveSensor(MessageReceiveSensorSubscription subscription, String individualID)
				throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(MessageReceiveSensor.class, subscription);
			} else {
				dl.getModel().subscribe(MessageReceiveSensor.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToMessageReceiveSensor(MessageReceiveSensorSubscription subscription)
				throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<MessageReceiveSensor> queryAllMessageReceiveSensor() throws KPIModelException {
			return dl.getModel().query(MessageReceiveSensor.class, TypeAttribute.RDFM3);
		}

		public MessageReceiveSensor queryMessageReceiveSensor(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(MessageReceiveSensor.class, individualID, TypeAttribute.RDFM3);
		}
	}

	// -----------------------------------------------------------------------------------------------------------------------------
	// ARF:14/06/19
	// Custom functionality to prevent TCP connection undetected problems in
	// production environments, where the clients are running standalone for days
	// and they, or the server itself, could fail silently
	// -----------------------------------------------------------------------------------------------------------------------------

	public static long lastTimestamp = System.currentTimeMillis();
	public static boolean isConnected;

	/**
	 * Check if connection with smool seems OK. This is a last-resource critical
	 * check for sokets really working because otherwise a transission error could
	 * appear and neither exceptions nor messages are raised in the client. This
	 * problem affects mostly to consumer KPs. The producer should not have this
	 * problem because once a messge is sent, if problem, the exception will be
	 * raised.
	 * <p>
	 * Two checks:
	 * </p>
	 * <ul>
	 * <li>if local network card or cable was disconnected, the OS socket is still
	 * alive ,but, if the client is a producer, there is no way to know if we're
	 * receiving data from producers, since no messages arrives. Therefore a timeout
	 * limit to receive messages is set</li>
	 * <li>if remote network is down the exception in the connector would be thrown,
	 * but if the smool server application is stopped, but the server keeps alive,
	 * only disconnection messages are received in the SmooKP on client, so this
	 * method also checks that boolean value</li>
	 * </ul>
	 * <p>
	 * Please note that the lastTimestamp is updated automatically on any observer
	 * object, so if messages are arriving at good pace, that value is updated
	 * </p>
	 * <p>
	 * Remember than even if implementing socket keep_alive and default OS is 2
	 * hous, internet routers could break connection every 5 mins of idle socket
	 * (and other network elements as well), so do not trust on OS events and
	 * timeouts to replace this feature. Other people has try and fight with those
	 * issues and the best way to know if the KP is really able to receive data is
	 * to test those two problems.
	 * </p>
	 * 
	 * @param timeoutInSeconds
	 *                             The maximun time to be expected that at least a
	 *                             mesage should arrive. if 0 (default), the timeout
	 *                             will be set to 5 minutes. If clients send data
	 *                             more scarcely, a PING message (example,
	 *                             subscribing to message receive nd the producer
	 *                             sending message as a simple PING) should be sent
	 *                             periodically at least for one of the producers.
	 * @throws IOException
	 *                         If the connection is not valid, or it may have
	 *                         undetected problems.
	 */
	public static void checkConnection(long timeoutInSeconds) throws IOException {
		if (!isConnected) {
			throw new IOException(
					"KP IS NOT CONNECTED TO SIB! (maybe the server was restarted). A reconnection should be invoked");
		} else if (System.currentTimeMillis() - lastTimestamp > timeoutInSeconds * 1000) {
			throw new IOException(
					"KP SUBSCRIPTION SEEMS NOT HAVING ANY DATA FOR LONG TIME! (it could be a network problem, ...or not. Anyway reconnection should be invoked)");
		}
	}

	/**
	 * Check if TCP connection is OK
	 * 
	 * @see #checKConnection(timeout) javadoc
	 * @throws IOException
	 */
	public static void checkConnection() throws IOException {
		checkConnection(5 * 60);
	}

	/**
	 * Method useful for consumerKPs and when nrunning in different subnetworks.
	 * 
	 * <p>
	 * Handy ready-to-use watchdog to check if connection to server is OK in
	 * consumers. You should replace the Thread.sleep(max) in the ConsumerMain with
	 * this watchdog. Every 10 seconds checks if the connection to SMOOL server is
	 * still valid.
	 * </p>
	 * 
	 * <p>
	 * Note that you can implement your own custom watchdogs by using only the
	 * checkConnection() methods.Copy and modify the source code of the watchdog.
	 * </p>
	 * 
	 * <p>
	 * Default time to see if any message (either sensor data or ping messages)
	 * arrived is 5 mins
	 * </p>
	 * 
	 * @see #checKConnection(timeout) javadoc
	 * 
	 * @throws IOException
	 */
	public static void watchdog() throws IOException {
		watchdog(5 * 60); // 5 mins
	}

	/**
	 * @see #watchdog()
	 * @param timeoutInSeconds
	 * @throws IOException
	 */
	public static void watchdog(long timeoutInSeconds) throws IOException {
		while (true) {
			try {
				Thread.sleep(10000);
			} catch (Exception e) {
			}
			SmoolKP.checkConnection(timeoutInSeconds);
		}
	}

	/**
	 * ------------------------------------------------------------------------------------------------------------------------------------
	 * 
	 * SECURITY POLICIES CHECKER
	 *
	 * If a SecurityChecker class is found, then, on every subscription, the policy
	 * is checked before piped to the subscription observer. If the policy is not
	 * fulfilled, the observer is not invoked
	 * 
	 * --------------------------------------------------------------------------------------------------------------------------------------/
	 * 
	 * /** Core security controls for SMOOL messages
	 * 
	 * A custom security class is injected (if available) so the same KP can have
	 * different security checks depending on the environment to be deployed
	 */
	private static Predicate<AbstractOntConcept> securityChecker;

	public static boolean checkSecurity(AbstractOntConcept concept) {
		if (securityChecker == null)
			initSecurity();
		return securityChecker.test(concept);
	}

	/**
	 * Init generic security test class for Smool objects The class must implement
	 * java.util.function.Predicate interface to test AbstractConcepts as input
	 */
	public static void initSecurity() {
		try {
			Class cl = Class.forName("org.smool.security.SecurityChecker");
			securityChecker = (Predicate<AbstractOntConcept>) cl.newInstance();
		} catch (Exception e) {
			// no Security class was found (either Standard one or third party security
			// class, use a default class
			securityChecker = o -> true;
		}
	}

}
