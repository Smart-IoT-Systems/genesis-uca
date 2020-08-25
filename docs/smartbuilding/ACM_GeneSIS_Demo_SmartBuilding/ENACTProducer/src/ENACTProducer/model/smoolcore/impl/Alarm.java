
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
package ENACTProducer.model.smoolcore.impl;
     
import org.smool.kpi.model.smart.AbstractOntConcept;
import org.smool.kpi.model.smart.KPProducer;
import org.smool.kpi.model.smart.KPConsumer;
import org.smool.kpi.model.smart.slots.FunctionalDatatypeSlot;
import ENACTProducer.model.smoolcore.IAlarm;

/**
 * This class implements the ontology concept Alarm
 * including all its properties.
 * @author Genrated via EMF OWL to java transformation
 * @version 1.0
 */
public class Alarm extends AbstractOntConcept implements IAlarm, KPProducer, KPConsumer{

    //Not needed.. public static String CLASS_NAMESPACE = "http://com.tecnalia.smool/core/smoolcore#";
  	//Not needed.. public static String CLASS_ID = "Alarm";
  	public static String CLASS_IRI = "http://com.tecnalia.smool/core/smoolcore#Alarm"; 
  		
  		
  	/**
    * The Constructor
    */
    public Alarm() {
    	super();
        init();
	}
    	
    	
	/**
 	* The Constructor
 	* @param id the Actuator identifier
 	*/
	public Alarm(String id) {
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
    
      
      	// Creates the alarmDescription property
      	String alarmDescriptionIRI = "http://com.tecnalia.smool/core/smoolcore#alarmDescription";
      	String alarmDescriptionPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > alarmDescriptionSlot= new FunctionalDatatypeSlot<String>(String.class);
      	alarmDescriptionSlot._setIRI(alarmDescriptionIRI);
      	alarmDescriptionSlot._setPrefix(alarmDescriptionPrefix);
      	alarmDescriptionSlot.setRange("xsd:String");
      	this._addProperty(alarmDescriptionSlot);
  	  
  	  
      	// Creates the alarmSeverity property
      	String alarmSeverityIRI = "http://com.tecnalia.smool/core/smoolcore#alarmSeverity";
      	String alarmSeverityPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > alarmSeveritySlot= new FunctionalDatatypeSlot<String>(String.class);
      	alarmSeveritySlot._setIRI(alarmSeverityIRI);
      	alarmSeveritySlot._setPrefix(alarmSeverityPrefix);
      	alarmSeveritySlot.setRange("xsd:String");
      	this._addProperty(alarmSeveritySlot);
  	  
  	  
      	// Creates the dataID property
      	String dataIDIRI = "http://com.tecnalia.smool/core/smoolcore#dataID";
      	String dataIDPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > dataIDSlot= new FunctionalDatatypeSlot<String>(String.class);
      	dataIDSlot._setIRI(dataIDIRI);
      	dataIDSlot._setPrefix(dataIDPrefix);
      	dataIDSlot.setRange("xsd:String");
      	this._addProperty(dataIDSlot);
  	  
  	  
      	// Creates the timestamp property
      	String timestampIRI = "http://com.tecnalia.smool/core/smoolcore#timestamp";
      	String timestampPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > timestampSlot= new FunctionalDatatypeSlot<String>(String.class);
      	timestampSlot._setIRI(timestampIRI);
      	timestampSlot._setPrefix(timestampPrefix);
      	timestampSlot.setRange("xsd:String");
      	this._addProperty(timestampSlot);
  	  
  	}
	/*
	* PROPERTIES: GETTERS AND SETTERS
	*/
 	
 	/**
 	* Sets the alarmDescription property.
 	* @param alarmDescription String value
 	*/
	public Alarm setAlarmDescription(String alarmDescription) {
		this.updateAttribute("alarmDescription",alarmDescription);
		return this;        
	}
		
	 /**
 	* Gets the alarmDescription property.
 	* @return a String value
 	*/
	public String getAlarmDescription() {
    	return (String) this._getFunctionalProperty("alarmDescription").getValue();
	}

 	/**
 	* Sets the alarmSeverity property.
 	* @param alarmSeverity String value
 	*/
	public Alarm setAlarmSeverity(String alarmSeverity) {
		this.updateAttribute("alarmSeverity",alarmSeverity);
		return this;        
	}
		
	 /**
 	* Gets the alarmSeverity property.
 	* @return a String value
 	*/
	public String getAlarmSeverity() {
    	return (String) this._getFunctionalProperty("alarmSeverity").getValue();
	}

 	/**
 	* Sets the dataID property.
 	* @param dataID String value
 	*/
	public Alarm setDataID(String dataID) {
		this.updateAttribute("dataID",dataID);
		return this;        
	}
		
	 /**
 	* Gets the dataID property.
 	* @return a String value
 	*/
	public String getDataID() {
    	return (String) this._getFunctionalProperty("dataID").getValue();
	}

 	/**
 	* Sets the timestamp property.
 	* @param timestamp String value
 	*/
	public Alarm setTimestamp(String timestamp) {
		this.updateAttribute("timestamp",timestamp);
		return this;        
	}
		
	 /**
 	* Gets the timestamp property.
 	* @return a String value
 	*/
	public String getTimestamp() {
    	return (String) this._getFunctionalProperty("timestamp").getValue();
	}

}
