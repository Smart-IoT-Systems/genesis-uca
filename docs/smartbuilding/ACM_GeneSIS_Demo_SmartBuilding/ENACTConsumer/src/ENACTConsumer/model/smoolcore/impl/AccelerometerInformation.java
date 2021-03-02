
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
import ENACTConsumer.model.smoolcore.IAccelerometerInformation;
import ENACTConsumer.model.smoolcore.ISecurity;
import ENACTConsumer.model.smoolcore.impl.Security;

/**
 * This class implements the ontology concept AccelerometerInformation
 * including all its properties.
 * @author Genrated via EMF OWL to java transformation
 * @version 1.0
 */
public class AccelerometerInformation extends AbstractOntConcept implements IAccelerometerInformation, KPProducer, KPConsumer{

    //Not needed.. public static String CLASS_NAMESPACE = "http://com.tecnalia.smool/core/smoolcore#";
  	//Not needed.. public static String CLASS_ID = "AccelerometerInformation";
  	public static String CLASS_IRI = "http://com.tecnalia.smool/core/smoolcore#AccelerometerInformation"; 
  		
  		
  	/**
    * The Constructor
    */
    public AccelerometerInformation() {
    	super();
        init();
	}
    	
    	
	/**
 	* The Constructor
 	* @param id the Actuator identifier
 	*/
	public AccelerometerInformation(String id) {
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
    
      
      	// Creates the axisx property
      	String axisxIRI = "http://com.tecnalia.smool/core/smoolcore#axisx";
      	String axisxPrefix = "smoolcore";

      	FunctionalDatatypeSlot < Double > axisxSlot= new FunctionalDatatypeSlot<Double>(Double.class);
      	axisxSlot._setIRI(axisxIRI);
      	axisxSlot._setPrefix(axisxPrefix);
      	axisxSlot.setRange("xsd:Double");
      	this._addProperty(axisxSlot);
  	  
  	  
      	// Creates the axisy property
      	String axisyIRI = "http://com.tecnalia.smool/core/smoolcore#axisy";
      	String axisyPrefix = "smoolcore";

      	FunctionalDatatypeSlot < Double > axisySlot= new FunctionalDatatypeSlot<Double>(Double.class);
      	axisySlot._setIRI(axisyIRI);
      	axisySlot._setPrefix(axisyPrefix);
      	axisySlot.setRange("xsd:Double");
      	this._addProperty(axisySlot);
  	  
  	  
      	// Creates the axisz property
      	String axiszIRI = "http://com.tecnalia.smool/core/smoolcore#axisz";
      	String axiszPrefix = "smoolcore";

      	FunctionalDatatypeSlot < Double > axiszSlot= new FunctionalDatatypeSlot<Double>(Double.class);
      	axiszSlot._setIRI(axiszIRI);
      	axiszSlot._setPrefix(axiszPrefix);
      	axiszSlot.setRange("xsd:Double");
      	this._addProperty(axiszSlot);
  	  
  	  
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
  	  
  	  
      	// Creates the unit property
      	String unitIRI = "http://com.tecnalia.smool/core/smoolcore#unit";
      	String unitPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > unitSlot= new FunctionalDatatypeSlot<String>(String.class);
      	unitSlot._setIRI(unitIRI);
      	unitSlot._setPrefix(unitPrefix);
      	unitSlot.setRange("xsd:String");
      	this._addProperty(unitSlot);
  	  
  	  
      	// Creates the value property
      	String valueIRI = "http://com.tecnalia.smool/core/smoolcore#value";
      	String valuePrefix = "smoolcore";

      	FunctionalDatatypeSlot < Double > valueSlot= new FunctionalDatatypeSlot<Double>(Double.class);
      	valueSlot._setIRI(valueIRI);
      	valueSlot._setPrefix(valuePrefix);
      	valueSlot.setRange("xsd:Double");
      	this._addProperty(valueSlot);
  	  
  	  
      	// Creates the securityData property
      	String securityDataIRI = "http://com.tecnalia.smool/core/smoolcore#securityData";
      	String securityDataPrefix = "smoolcore";

      	FunctionalObjectSlot < Security > securityDataSlot= new FunctionalObjectSlot<Security>(Security.class);
      	securityDataSlot._setIRI(securityDataIRI);
      	securityDataSlot._setPrefix(securityDataPrefix);
      	
      	this._addProperty(securityDataSlot);
  	  
  	}
	/*
	* PROPERTIES: GETTERS AND SETTERS
	*/
 	
 	/**
 	* Sets the axisx property.
 	* @param axisx Double value
 	*/
	public AccelerometerInformation setAxisx(Double axisx) {
		this.updateAttribute("axisx",axisx);
		return this;        
	}
		
	 /**
 	* Gets the axisx property.
 	* @return a Double value
 	*/
	public Double getAxisx() {
    	return (Double) this._getFunctionalProperty("axisx").getValue();
	}

 	/**
 	* Sets the axisy property.
 	* @param axisy Double value
 	*/
	public AccelerometerInformation setAxisy(Double axisy) {
		this.updateAttribute("axisy",axisy);
		return this;        
	}
		
	 /**
 	* Gets the axisy property.
 	* @return a Double value
 	*/
	public Double getAxisy() {
    	return (Double) this._getFunctionalProperty("axisy").getValue();
	}

 	/**
 	* Sets the axisz property.
 	* @param axisz Double value
 	*/
	public AccelerometerInformation setAxisz(Double axisz) {
		this.updateAttribute("axisz",axisz);
		return this;        
	}
		
	 /**
 	* Gets the axisz property.
 	* @return a Double value
 	*/
	public Double getAxisz() {
    	return (Double) this._getFunctionalProperty("axisz").getValue();
	}

 	/**
 	* Sets the dataID property.
 	* @param dataID String value
 	*/
	public AccelerometerInformation setDataID(String dataID) {
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
	public AccelerometerInformation setTimestamp(String timestamp) {
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
 	* Sets the unit property.
 	* @param unit String value
 	*/
	public AccelerometerInformation setUnit(String unit) {
		this.updateAttribute("unit",unit);
		return this;        
	}
		
	 /**
 	* Gets the unit property.
 	* @return a String value
 	*/
	public String getUnit() {
    	return (String) this._getFunctionalProperty("unit").getValue();
	}

 	/**
 	* Sets the value property.
 	* @param value Double value
 	*/
	public AccelerometerInformation setValue(Double value) {
		this.updateAttribute("value",value);
		return this;        
	}
		
	 /**
 	* Gets the value property.
 	* @return a Double value
 	*/
	public Double getValue() {
    	return (Double) this._getFunctionalProperty("value").getValue();
	}

 	/**
 	* Sets the securityData property.
 	* @param securityData ISecurity value
 	*/
	public AccelerometerInformation setSecurityData(ISecurity securityData) {
		this.updateAttribute("securityData",securityData);
		return this;        
	}
		
	 /**
 	* Gets the securityData property.
 	* @return a ISecurity value
 	*/
	public ISecurity getSecurityData() {
    	return (ISecurity) this._getFunctionalProperty("securityData").getValue();
	}

}
