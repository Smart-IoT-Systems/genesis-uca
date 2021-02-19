
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
import ENACTConsumer.model.smoolcore.ISecurityNonRepudiation;

/**
 * This class implements the ontology concept SecurityNonRepudiation
 * including all its properties.
 * @author Genrated via EMF OWL to java transformation
 * @version 1.0
 */
public class SecurityNonRepudiation extends AbstractOntConcept implements ISecurityNonRepudiation, KPProducer, KPConsumer{

    //Not needed.. public static String CLASS_NAMESPACE = "http://com.tecnalia.smool/core/smoolcore#";
  	//Not needed.. public static String CLASS_ID = "SecurityNonRepudiation";
  	public static String CLASS_IRI = "http://com.tecnalia.smool/core/smoolcore#SecurityNonRepudiation"; 
  		
  		
  	/**
    * The Constructor
    */
    public SecurityNonRepudiation() {
    	super();
        init();
	}
    	
    	
	/**
 	* The Constructor
 	* @param id the Actuator identifier
 	*/
	public SecurityNonRepudiation(String id) {
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
    
      
      	// Creates the data property
      	String dataIRI = "http://com.tecnalia.smool/core/smoolcore#data";
      	String dataPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > dataSlot= new FunctionalDatatypeSlot<String>(String.class);
      	dataSlot._setIRI(dataIRI);
      	dataSlot._setPrefix(dataPrefix);
      	dataSlot.setRange("xsd:String");
      	this._addProperty(dataSlot);
  	  
  	  
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
  	  
  	  
      	// Creates the type property
      	String typeIRI = "http://com.tecnalia.smool/core/smoolcore#type";
      	String typePrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > typeSlot= new FunctionalDatatypeSlot<String>(String.class);
      	typeSlot._setIRI(typeIRI);
      	typeSlot._setPrefix(typePrefix);
      	typeSlot.setRange("xsd:String");
      	this._addProperty(typeSlot);
  	  
  	}
	/*
	* PROPERTIES: GETTERS AND SETTERS
	*/
 	
 	/**
 	* Sets the data property.
 	* @param data String value
 	*/
	public SecurityNonRepudiation setData(String data) {
		this.updateAttribute("data",data);
		return this;        
	}
		
	 /**
 	* Gets the data property.
 	* @return a String value
 	*/
	public String getData() {
    	return (String) this._getFunctionalProperty("data").getValue();
	}

 	/**
 	* Sets the dataID property.
 	* @param dataID String value
 	*/
	public SecurityNonRepudiation setDataID(String dataID) {
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
	public SecurityNonRepudiation setTimestamp(String timestamp) {
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

 	/**
 	* Sets the type property.
 	* @param type String value
 	*/
	public SecurityNonRepudiation setType(String type) {
		this.updateAttribute("type",type);
		return this;        
	}
		
	 /**
 	* Gets the type property.
 	* @return a String value
 	*/
	public String getType() {
    	return (String) this._getFunctionalProperty("type").getValue();
	}

}
