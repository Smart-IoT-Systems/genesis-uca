
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
import ENACTProducer.model.smoolcore.IEmailMessage;

/**
 * This class implements the ontology concept EmailMessage
 * including all its properties.
 * @author Genrated via EMF OWL to java transformation
 * @version 1.0
 */
public class EmailMessage extends AbstractOntConcept implements IEmailMessage, KPProducer, KPConsumer{

    //Not needed.. public static String CLASS_NAMESPACE = "http://com.tecnalia.smool/core/smoolcore#";
  	//Not needed.. public static String CLASS_ID = "EmailMessage";
  	public static String CLASS_IRI = "http://com.tecnalia.smool/core/smoolcore#EmailMessage"; 
  		
  		
  	/**
    * The Constructor
    */
    public EmailMessage() {
    	super();
        init();
	}
    	
    	
	/**
 	* The Constructor
 	* @param id the Actuator identifier
 	*/
	public EmailMessage(String id) {
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
    
      
      	// Creates the body property
      	String bodyIRI = "http://com.tecnalia.smool/core/smoolcore#body";
      	String bodyPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > bodySlot= new FunctionalDatatypeSlot<String>(String.class);
      	bodySlot._setIRI(bodyIRI);
      	bodySlot._setPrefix(bodyPrefix);
      	bodySlot.setRange("xsd:String");
      	this._addProperty(bodySlot);
  	  
  	  
      	// Creates the dataID property
      	String dataIDIRI = "http://com.tecnalia.smool/core/smoolcore#dataID";
      	String dataIDPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > dataIDSlot= new FunctionalDatatypeSlot<String>(String.class);
      	dataIDSlot._setIRI(dataIDIRI);
      	dataIDSlot._setPrefix(dataIDPrefix);
      	dataIDSlot.setRange("xsd:String");
      	this._addProperty(dataIDSlot);
  	  
  	  
      	// Creates the subject property
      	String subjectIRI = "http://com.tecnalia.smool/core/smoolcore#subject";
      	String subjectPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > subjectSlot= new FunctionalDatatypeSlot<String>(String.class);
      	subjectSlot._setIRI(subjectIRI);
      	subjectSlot._setPrefix(subjectPrefix);
      	subjectSlot.setRange("xsd:String");
      	this._addProperty(subjectSlot);
  	  
  	  
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
 	* Sets the body property.
 	* @param body String value
 	*/
	public EmailMessage setBody(String body) {
		this.updateAttribute("body",body);
		return this;        
	}
		
	 /**
 	* Gets the body property.
 	* @return a String value
 	*/
	public String getBody() {
    	return (String) this._getFunctionalProperty("body").getValue();
	}

 	/**
 	* Sets the dataID property.
 	* @param dataID String value
 	*/
	public EmailMessage setDataID(String dataID) {
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
 	* Sets the subject property.
 	* @param subject String value
 	*/
	public EmailMessage setSubject(String subject) {
		this.updateAttribute("subject",subject);
		return this;        
	}
		
	 /**
 	* Gets the subject property.
 	* @return a String value
 	*/
	public String getSubject() {
    	return (String) this._getFunctionalProperty("subject").getValue();
	}

 	/**
 	* Sets the timestamp property.
 	* @param timestamp String value
 	*/
	public EmailMessage setTimestamp(String timestamp) {
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
