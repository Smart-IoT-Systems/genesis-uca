
/*******************************************************************************
* Copyright (c) 2018 Tecnalia Research and Innovation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* This file is a result of OWL 2 java transformation using EMF
* Contributors:
*    Enas Ashraf (inas@itida.gov.eg) - creation of level 2 metamodel and transformation to java classes 
*    Adrian Noguero (Tecnalia Research and Innovation - Software Systems Engineering) - creation of level 1 metamodel by creating ...
*******************************************************************************/ 
package ENACTConsumer.model.smoolcore.impl;
     
import org.smool.kpi.model.smart.AbstractOntConcept;
import org.smool.kpi.model.smart.KPProducer;
import org.smool.kpi.model.smart.KPConsumer;
import org.smool.kpi.model.smart.slots.FunctionalDatatypeSlot;
import org.smool.kpi.model.smart.slots.FunctionalObjectSlot;
import org.smool.kpi.model.smart.slots.NonFunctionalObjectSlot;
import ENACTConsumer.model.smoolcore.IHumanDataDevice;
import ENACTConsumer.model.smoolcore.IPreferenceInformation;
import ENACTConsumer.model.smoolcore.impl.PreferenceInformation;
import ENACTConsumer.model.smoolcore.IAlarm;
import ENACTConsumer.model.smoolcore.impl.Alarm;

/**
 * This class implements the ontology concept HumanDataDevice
 * including all its properties.
 * @author Genrated via EMF OWL to java transformation
 * @version 1.0
 */
public class HumanDataDevice extends AbstractOntConcept implements IHumanDataDevice, KPProducer, KPConsumer{

    //Not needed.. public static String CLASS_NAMESPACE = "http://com.tecnalia.smool/core/smoolcore#";
  	//Not needed.. public static String CLASS_ID = "HumanDataDevice";
  	public static String CLASS_IRI = "http://com.tecnalia.smool/core/smoolcore#HumanDataDevice"; 
  		
  		
  	/**
    * The Constructor
    */
    public HumanDataDevice() {
    	super();
        init();
	}
    	
    	
	/**
 	* The Constructor
 	* @param id the Actuator identifier
 	*/
	public HumanDataDevice(String id) {
      	/** Call superclass to establish the identifier */
      	super(id);
      	init();
	}
    
    	
    	
	/**
 	* Inits the fields associated to a ontology concept
 	*/
	public void init() {
      	/** Sets the context of this ontology concept */
      	this._setClassContext("smoolcore", CLASS_IRI);

      	/** Sets the individual context */
      	this._setDefaultIndividualContext();
    
      
      	// Creates the deviceID property
      	String deviceIDIRI = "http://com.tecnalia.smool/core/smoolcore#deviceID";
      	String deviceIDPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > deviceIDSlot= new FunctionalDatatypeSlot<String>(String.class);
      	deviceIDSlot._setIRI(deviceIDIRI);
      	deviceIDSlot._setPrefix(deviceIDPrefix);
      	deviceIDSlot.setRange("xsd:String");
      	this._addProperty(deviceIDSlot);
  	  
  	  
      	// Creates the vendor property
      	String vendorIRI = "http://com.tecnalia.smool/core/smoolcore#vendor";
      	String vendorPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > vendorSlot= new FunctionalDatatypeSlot<String>(String.class);
      	vendorSlot._setIRI(vendorIRI);
      	vendorSlot._setPrefix(vendorPrefix);
      	vendorSlot.setRange("xsd:String");
      	this._addProperty(vendorSlot);
  	  
  	  
      	// Creates the PublicPreferences property
      	String PublicPreferencesIRI = "http://com.tecnalia.smool/core/smoolcore#PublicPreferences";
      	String PublicPreferencesPrefix = "smoolcore";

      	FunctionalObjectSlot < PreferenceInformation > PublicPreferencesSlot= new FunctionalObjectSlot<PreferenceInformation>(PreferenceInformation.class);
      	PublicPreferencesSlot._setIRI(PublicPreferencesIRI);
      	PublicPreferencesSlot._setPrefix(PublicPreferencesPrefix);
      	
      	this._addProperty(PublicPreferencesSlot);
  	  
  	  
      	// Creates the alarms property
      	String alarmsIRI = "http://com.tecnalia.smool/core/smoolcore#alarms";
      	String alarmsPrefix = "smoolcore";

      	NonFunctionalObjectSlot < Alarm > alarmsSlot= new NonFunctionalObjectSlot<Alarm>(Alarm.class);
      	alarmsSlot._setIRI(alarmsIRI);
      	alarmsSlot._setPrefix(alarmsPrefix);
      	
      	this._addProperty(alarmsSlot);
  	  
  	}
	/*
	* PROPERTIES: GETTERS AND SETTERS
	*/
 	
 	/**
 	* Sets the deviceID property.
 	* @param deviceID String value
 	*/
	public HumanDataDevice setDeviceID(String deviceID) {
		this.updateAttribute("deviceID",deviceID);
		return this;        
	}
		
	 /**
 	* Gets the deviceID property.
 	* @return a String value
 	*/
	public String getDeviceID() {
    	return (String) this._getFunctionalProperty("deviceID").getValue();
	}

 	/**
 	* Sets the vendor property.
 	* @param vendor String value
 	*/
	public HumanDataDevice setVendor(String vendor) {
		this.updateAttribute("vendor",vendor);
		return this;        
	}
		
	 /**
 	* Gets the vendor property.
 	* @return a String value
 	*/
	public String getVendor() {
    	return (String) this._getFunctionalProperty("vendor").getValue();
	}

 	/**
 	* Sets the PublicPreferences property.
 	* @param PublicPreferences IPreferenceInformation value
 	*/
	public HumanDataDevice setPublicPreferences(IPreferenceInformation PublicPreferences) {
		this.updateAttribute("PublicPreferences",PublicPreferences);
		return this;        
	}
		
	 /**
 	* Gets the PublicPreferences property.
 	* @return a IPreferenceInformation value
 	*/
	public IPreferenceInformation getPublicPreferences() {
    	return (IPreferenceInformation) this._getFunctionalProperty("PublicPreferences").getValue();
	}

 	/**
 	* Adds the alarms property.
 	* @param alarms IAlarm value to add
 	*/
	public void addAlarms(IAlarm alarms) {
	  
	  if (!this.containsAttribute("alarms",alarms)) {
            this.addAttribute("alarms",alarms);
        }
		
	}
		
	 /**
 	* Removes the alarms property.
 	* @param alarms IAlarm value to remove
 	*/
	public void removeAlarms(IAlarm alarms) {
	  if (this.containsAttribute("alarms",alarms)) {
            this.removeAttribute("alarms",alarms);
        }
    	
	}

}
