
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
import ENACTConsumer.model.smoolcore.IGisLocation;

/**
 * This class implements the ontology concept GisLocation
 * including all its properties.
 * @author Genrated via EMF OWL to java transformation
 * @version 1.0
 */
public class GisLocation extends AbstractOntConcept implements IGisLocation, KPProducer, KPConsumer{

    //Not needed.. public static String CLASS_NAMESPACE = "http://com.tecnalia.smool/core/smoolcore#";
  	//Not needed.. public static String CLASS_ID = "GisLocation";
  	public static String CLASS_IRI = "http://com.tecnalia.smool/core/smoolcore#GisLocation"; 
  		
  		
  	/**
    * The Constructor
    */
    public GisLocation() {
    	super();
        init();
	}
    	
    	
	/**
 	* The Constructor
 	* @param id the Actuator identifier
 	*/
	public GisLocation(String id) {
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
    
      
      	// Creates the dataID property
      	String dataIDIRI = "http://com.tecnalia.smool/core/smoolcore#dataID";
      	String dataIDPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > dataIDSlot= new FunctionalDatatypeSlot<String>(String.class);
      	dataIDSlot._setIRI(dataIDIRI);
      	dataIDSlot._setPrefix(dataIDPrefix);
      	dataIDSlot.setRange("xsd:String");
      	this._addProperty(dataIDSlot);
  	  
  	  
      	// Creates the latitude property
      	String latitudeIRI = "http://com.tecnalia.smool/core/smoolcore#latitude";
      	String latitudePrefix = "smoolcore";

      	FunctionalDatatypeSlot < Double > latitudeSlot= new FunctionalDatatypeSlot<Double>(Double.class);
      	latitudeSlot._setIRI(latitudeIRI);
      	latitudeSlot._setPrefix(latitudePrefix);
      	latitudeSlot.setRange("xsd:Double");
      	this._addProperty(latitudeSlot);
  	  
  	  
      	// Creates the longitude property
      	String longitudeIRI = "http://com.tecnalia.smool/core/smoolcore#longitude";
      	String longitudePrefix = "smoolcore";

      	FunctionalDatatypeSlot < Double > longitudeSlot= new FunctionalDatatypeSlot<Double>(Double.class);
      	longitudeSlot._setIRI(longitudeIRI);
      	longitudeSlot._setPrefix(longitudePrefix);
      	longitudeSlot.setRange("xsd:Double");
      	this._addProperty(longitudeSlot);
  	  
  	  
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
 	* Sets the dataID property.
 	* @param dataID String value
 	*/
	public GisLocation setDataID(String dataID) {
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
 	* Sets the latitude property.
 	* @param latitude Double value
 	*/
	public GisLocation setLatitude(Double latitude) {
		this.updateAttribute("latitude",latitude);
		return this;        
	}
		
	 /**
 	* Gets the latitude property.
 	* @return a Double value
 	*/
	public Double getLatitude() {
    	return (Double) this._getFunctionalProperty("latitude").getValue();
	}

 	/**
 	* Sets the longitude property.
 	* @param longitude Double value
 	*/
	public GisLocation setLongitude(Double longitude) {
		this.updateAttribute("longitude",longitude);
		return this;        
	}
		
	 /**
 	* Gets the longitude property.
 	* @return a Double value
 	*/
	public Double getLongitude() {
    	return (Double) this._getFunctionalProperty("longitude").getValue();
	}

 	/**
 	* Sets the timestamp property.
 	* @param timestamp String value
 	*/
	public GisLocation setTimestamp(String timestamp) {
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
