
/*
 * Generated Smool KP API
 */
package ENACTConsumer.api;

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

import ENACTConsumer.api.util.SmoolKPUtil;
import ENACTConsumer.model.smoolcore.IAlarm;
import ENACTConsumer.model.smoolcore.IBooleanInformation;
import ENACTConsumer.model.smoolcore.IContinuousInformation;
import ENACTConsumer.model.smoolcore.IPhysicalLocation;
import ENACTConsumer.model.smoolcore.IProgrammeInformation;
import ENACTConsumer.model.smoolcore.ISecurity;
import ENACTConsumer.model.smoolcore.impl.Accelerometer;
import ENACTConsumer.model.smoolcore.impl.BlackoutSensor;
import ENACTConsumer.model.smoolcore.impl.BlindPositionActuator;
import ENACTConsumer.model.smoolcore.impl.FloodSensor;
import ENACTConsumer.model.smoolcore.impl.GasSensor;
import ENACTConsumer.model.smoolcore.impl.HVACActuator;
import ENACTConsumer.model.smoolcore.impl.HumiditySensor;
import ENACTConsumer.model.smoolcore.impl.LightRangeActuator;
import ENACTConsumer.model.smoolcore.impl.LightSwitchActuator;
import ENACTConsumer.model.smoolcore.impl.LightingSensor;
import ENACTConsumer.model.smoolcore.impl.MessageReceiveSensor;
import ENACTConsumer.model.smoolcore.impl.NoiseSensor;
import ENACTConsumer.model.smoolcore.impl.PresenceSensor;
import ENACTConsumer.model.smoolcore.impl.SmokeSensor;
import ENACTConsumer.model.smoolcore.impl.TemperatureSensor;
import ENACTConsumer.model.smoolcore.impl.WashingMachineActuator;
import ENACTConsumer.model.smoolcore.IMessage;
import ENACTConsumer.model.smoolcore.ILogicalLocation;


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
	private static String KP_NAME = "ENACTConsumer";

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
		SmoolKP.isConnected = SmoolKP.connectToSIB(name, address, Integer.toString(port), 2000);
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
				kpie.printStackTrace();
				return false;
			} catch (InterruptedException ie) {
				ie.printStackTrace();
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

		public String createBlindPositionActuator(String elemID, String deviceID, String vendor,
				java.util.List<IAlarm> alarms, IContinuousInformation blindPos, IPhysicalLocation physicalLoc)
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
			BlindPositionActuator concept = new BlindPositionActuator();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setBlindPos(blindPos);
			concept.setPhysicalLoc(physicalLoc);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateBlindPositionActuator(String elemID, String deviceID, String vendor,
				java.util.List<IAlarm> alarms, IContinuousInformation blindPos, IPhysicalLocation physicalLoc)
				throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof BlindPositionActuator)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, blindPos, physicalLoc);

			Collection<Object> vals = null;
			((BlindPositionActuator) concept).setDeviceID(deviceID);
			((BlindPositionActuator) concept).setVendor(vendor);
			vals = new ArrayList<Object>(
					((BlindPositionActuator) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((BlindPositionActuator) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((BlindPositionActuator) concept).addAlarms(elem);
				}
			}
			((BlindPositionActuator) concept).setBlindPos(blindPos);
			((BlindPositionActuator) concept).setPhysicalLoc(physicalLoc);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeBlindPositionActuator(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof BlindPositionActuator)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		public String createHVACActuator(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				IPhysicalLocation physicalLoc, IProgrammeInformation programme) throws KPIModelException {

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
			HVACActuator concept = new HVACActuator();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setPhysicalLoc(physicalLoc);
			concept.setProgramme(programme);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateHVACActuator(String elemID, String deviceID, String vendor, java.util.List<IAlarm> alarms,
				IPhysicalLocation physicalLoc, IProgrammeInformation programme) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof HVACActuator)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, physicalLoc, programme);

			Collection<Object> vals = null;
			((HVACActuator) concept).setDeviceID(deviceID);
			((HVACActuator) concept).setVendor(vendor);
			vals = new ArrayList<Object>(((HVACActuator) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((HVACActuator) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((HVACActuator) concept).addAlarms(elem);
				}
			}
			((HVACActuator) concept).setPhysicalLoc(physicalLoc);
			((HVACActuator) concept).setProgramme(programme);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeHVACActuator(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof HVACActuator)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		public String createLightRangeActuator(String elemID, String deviceID, String vendor,
				java.util.List<IAlarm> alarms, IContinuousInformation lightIntensity, IPhysicalLocation physicalLoc)
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
			LightRangeActuator concept = new LightRangeActuator();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setLightIntensity(lightIntensity);
			concept.setPhysicalLoc(physicalLoc);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateLightRangeActuator(String elemID, String deviceID, String vendor,
				java.util.List<IAlarm> alarms, IContinuousInformation lightIntensity, IPhysicalLocation physicalLoc)
				throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof LightRangeActuator)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, lightIntensity, physicalLoc);

			Collection<Object> vals = null;
			((LightRangeActuator) concept).setDeviceID(deviceID);
			((LightRangeActuator) concept).setVendor(vendor);
			vals = new ArrayList<Object>(
					((LightRangeActuator) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((LightRangeActuator) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((LightRangeActuator) concept).addAlarms(elem);
				}
			}
			((LightRangeActuator) concept).setLightIntensity(lightIntensity);
			((LightRangeActuator) concept).setPhysicalLoc(physicalLoc);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeLightRangeActuator(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof LightRangeActuator)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		public String createLightSwitchActuator(String elemID, String deviceID, String vendor,
				java.util.List<IAlarm> alarms, IBooleanInformation bool, IPhysicalLocation physicalLoc)
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
			LightSwitchActuator concept = new LightSwitchActuator();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setBool(bool);
			concept.setPhysicalLoc(physicalLoc);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateLightSwitchActuator(String elemID, String deviceID, String vendor,
				java.util.List<IAlarm> alarms, IBooleanInformation bool, IPhysicalLocation physicalLoc)
				throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof LightSwitchActuator)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, bool, physicalLoc);

			Collection<Object> vals = null;
			((LightSwitchActuator) concept).setDeviceID(deviceID);
			((LightSwitchActuator) concept).setVendor(vendor);
			vals = new ArrayList<Object>(
					((LightSwitchActuator) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((LightSwitchActuator) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((LightSwitchActuator) concept).addAlarms(elem);
				}
			}
			((LightSwitchActuator) concept).setBool(bool);
			((LightSwitchActuator) concept).setPhysicalLoc(physicalLoc);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeLightSwitchActuator(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof LightSwitchActuator)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			dl.getModel().remove(concept);
			dl.getModel().publish();
		}

		public String createWashingMachineActuator(String elemID, String deviceID, String vendor,
				java.util.List<IAlarm> alarms, IBooleanInformation bool, IPhysicalLocation physicalLoc,
				IProgrammeInformation programme) throws KPIModelException {

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
			WashingMachineActuator concept = new WashingMachineActuator();
			concept._setIndividualID(elemID);

			concept.setDeviceID(deviceID);
			concept.setVendor(vendor);
			if (alarms != null) {
				for (IAlarm elem : alarms) {
					concept.addAlarms(elem);
				}
			}
			concept.setBool(bool);
			concept.setPhysicalLoc(physicalLoc);
			concept.setProgramme(programme);

			// Add it to the HashMap
			conceptMap.put(elemID, concept);
			dl.getModel().add(concept);
			dl.getModel().publish();

			return concept._getIndividualID();
		}

		public void updateWashingMachineActuator(String elemID, String deviceID, String vendor,
				java.util.List<IAlarm> alarms, IBooleanInformation bool, IPhysicalLocation physicalLoc,
				IProgrammeInformation programme) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot update the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot update concept. Connection lost. Try to reconnect");
			}
			AbstractOntConcept concept = conceptMap.get(elemID);

			if (concept == null || !(concept instanceof WashingMachineActuator)) {
				throw new KPIModelException("Provided ID does not refer to an existing concept of the selected type.");
			}
			// SECURITY: refresh secutity objects when updating, even if the were not been
			// modified (then the commands for those slots are added)
			persistSecurityConcepts(elemID, deviceID, vendor, alarms, bool, physicalLoc, programme);

			Collection<Object> vals = null;
			((WashingMachineActuator) concept).setDeviceID(deviceID);
			((WashingMachineActuator) concept).setVendor(vendor);
			vals = new ArrayList<Object>(
					((WashingMachineActuator) concept)._getNonFunctionalProperty("alarms").listValues());
			if (vals != null)
				((WashingMachineActuator) concept)._getNonFunctionalProperty("alarms").removeAll(vals);

			if (alarms != null) {
				for (IAlarm elem : alarms) {
					((WashingMachineActuator) concept).addAlarms(elem);
				}
			}
			((WashingMachineActuator) concept).setBool(bool);
			((WashingMachineActuator) concept).setPhysicalLoc(physicalLoc);
			((WashingMachineActuator) concept).setProgramme(programme);

			dl.getModel().add(concept);
			dl.getModel().publish();
		}

		public void removeWashingMachineActuator(String elemID) throws KPIModelException {

			// check that we're connected and model is inserted
			if (SmoolKP.dl.getModel() == null || !SmoolKP.dl.getModel().isConnected()) {
				Logger.error("Cannot remove the concept. Connection lost. Try to reconnect");
				throw new KPIModelException("Cannot remove the concept. Connection lost. Try to reconnect");
			}

			AbstractOntConcept concept = conceptMap.get(elemID);
			if (concept == null || !(concept instanceof WashingMachineActuator)) {
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

		/*
		 * public void persistSecurityConcepts(Object... objects) { for (Object obj :
		 * objects) { Method m; try { m = obj.getClass().getMethod("getSecurityData");
		 * ISecurity sec = (ISecurity) m.invoke(obj); if (sec != null) { String d =
		 * sec.getData() != null ? new String(sec.getData()) : ""; String t =
		 * sec.getType() != null ? new String(sec.getType()) : ""; // d =
		 * Long.toString(System.currentTimeMillis()); // sec.setData(sec.getData()); //
		 * sec.setType(sec.getType()); sec.setData("d1"); sec.setType("t1");
		 * sec.setData(d); sec.setType(t); } } catch (Exception e) { ; } } }
		 */
	}

	private class ConsumerImpl implements Consumer {

		public ConsumerImpl() throws KPIModelException {
			if (!SmoolKP.isConnectedToSIB()) {
				SmoolKP.connectToSIB();
			}
		}

		public void subscribeToAccelerometer(AccelerometerSubscription subscription, String individualID)
				throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(Accelerometer.class, subscription);
			} else {
				dl.getModel().subscribe(Accelerometer.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToAccelerometer(AccelerometerSubscription subscription) throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<Accelerometer> queryAllAccelerometer() throws KPIModelException {
			return dl.getModel().query(Accelerometer.class, TypeAttribute.RDFM3);
		}

		public Accelerometer queryAccelerometer(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(Accelerometer.class, individualID, TypeAttribute.RDFM3);
		}

		public void subscribeToBlackoutSensor(BlackoutSensorSubscription subscription, String individualID)
				throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(BlackoutSensor.class, subscription);
			} else {
				dl.getModel().subscribe(BlackoutSensor.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToBlackoutSensor(BlackoutSensorSubscription subscription) throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<BlackoutSensor> queryAllBlackoutSensor() throws KPIModelException {
			return dl.getModel().query(BlackoutSensor.class, TypeAttribute.RDFM3);
		}

		public BlackoutSensor queryBlackoutSensor(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(BlackoutSensor.class, individualID, TypeAttribute.RDFM3);
		}

		public void subscribeToFloodSensor(FloodSensorSubscription subscription, String individualID)
				throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(FloodSensor.class, subscription);
			} else {
				dl.getModel().subscribe(FloodSensor.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToFloodSensor(FloodSensorSubscription subscription) throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<FloodSensor> queryAllFloodSensor() throws KPIModelException {
			return dl.getModel().query(FloodSensor.class, TypeAttribute.RDFM3);
		}

		public FloodSensor queryFloodSensor(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(FloodSensor.class, individualID, TypeAttribute.RDFM3);
		}

		public void subscribeToGasSensor(GasSensorSubscription subscription, String individualID)
				throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(GasSensor.class, subscription);
			} else {
				dl.getModel().subscribe(GasSensor.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToGasSensor(GasSensorSubscription subscription) throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<GasSensor> queryAllGasSensor() throws KPIModelException {
			return dl.getModel().query(GasSensor.class, TypeAttribute.RDFM3);
		}

		public GasSensor queryGasSensor(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(GasSensor.class, individualID, TypeAttribute.RDFM3);
		}

		public void subscribeToHumiditySensor(HumiditySensorSubscription subscription, String individualID)
				throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(HumiditySensor.class, subscription);
			} else {
				dl.getModel().subscribe(HumiditySensor.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToHumiditySensor(HumiditySensorSubscription subscription) throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<HumiditySensor> queryAllHumiditySensor() throws KPIModelException {
			return dl.getModel().query(HumiditySensor.class, TypeAttribute.RDFM3);
		}

		public HumiditySensor queryHumiditySensor(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(HumiditySensor.class, individualID, TypeAttribute.RDFM3);
		}

		public void subscribeToLightingSensor(LightingSensorSubscription subscription, String individualID)
				throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(LightingSensor.class, subscription);
			} else {
				dl.getModel().subscribe(LightingSensor.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToLightingSensor(LightingSensorSubscription subscription) throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<LightingSensor> queryAllLightingSensor() throws KPIModelException {
			return dl.getModel().query(LightingSensor.class, TypeAttribute.RDFM3);
		}

		public LightingSensor queryLightingSensor(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(LightingSensor.class, individualID, TypeAttribute.RDFM3);
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

		public void subscribeToNoiseSensor(NoiseSensorSubscription subscription, String individualID)
				throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(NoiseSensor.class, subscription);
			} else {
				dl.getModel().subscribe(NoiseSensor.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToNoiseSensor(NoiseSensorSubscription subscription) throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<NoiseSensor> queryAllNoiseSensor() throws KPIModelException {
			return dl.getModel().query(NoiseSensor.class, TypeAttribute.RDFM3);
		}

		public NoiseSensor queryNoiseSensor(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(NoiseSensor.class, individualID, TypeAttribute.RDFM3);
		}

		public void subscribeToPresenceSensor(PresenceSensorSubscription subscription, String individualID)
				throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(PresenceSensor.class, subscription);
			} else {
				dl.getModel().subscribe(PresenceSensor.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToPresenceSensor(PresenceSensorSubscription subscription) throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<PresenceSensor> queryAllPresenceSensor() throws KPIModelException {
			return dl.getModel().query(PresenceSensor.class, TypeAttribute.RDFM3);
		}

		public PresenceSensor queryPresenceSensor(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(PresenceSensor.class, individualID, TypeAttribute.RDFM3);
		}

		public void subscribeToSmokeSensor(SmokeSensorSubscription subscription, String individualID)
				throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(SmokeSensor.class, subscription);
			} else {
				dl.getModel().subscribe(SmokeSensor.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToSmokeSensor(SmokeSensorSubscription subscription) throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<SmokeSensor> queryAllSmokeSensor() throws KPIModelException {
			return dl.getModel().query(SmokeSensor.class, TypeAttribute.RDFM3);
		}

		public SmokeSensor querySmokeSensor(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(SmokeSensor.class, individualID, TypeAttribute.RDFM3);
		}

		public void subscribeToTemperatureSensor(TemperatureSensorSubscription subscription, String individualID)
				throws KPIModelException {
			if (individualID == null || individualID.equals("")) {
				dl.getModel().subscribe(TemperatureSensor.class, subscription);
			} else {
				dl.getModel().subscribe(TemperatureSensor.class, individualID, subscription);
			}
			dl.getModel().publish();
		}

		public void unsubscribeToTemperatureSensor(TemperatureSensorSubscription subscription)
				throws KPIModelException {
			dl.getModel().unsubscribe(subscription);
			dl.getModel().publish();
		}

		public List<TemperatureSensor> queryAllTemperatureSensor() throws KPIModelException {
			return dl.getModel().query(TemperatureSensor.class, TypeAttribute.RDFM3);
		}

		public TemperatureSensor queryTemperatureSensor(String individualID) throws KPIModelException {
			if (individualID == null) {
				return null;
			}
			return dl.getModel().query(TemperatureSensor.class, individualID, TypeAttribute.RDFM3);
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
