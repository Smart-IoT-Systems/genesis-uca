
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
import ENACTProducer.model.smoolcore.ILocation3D;

/**
 * This class implements the ontology concept Location3D
 * including all its properties.
 * @author Genrated via EMF OWL to java transformation
 * @version 1.0
 */
public class Location3D extends AbstractOntConcept implements ILocation3D, KPProducer, KPConsumer{

    //Not needed.. public static String CLASS_NAMESPACE = "http://com.tecnalia.smool/core/smoolcore#";
  	//Not needed.. public static String CLASS_ID = "Location3D";
  	public static String CLASS_IRI = "http://com.tecnalia.smool/core/smoolcore#Location3D"; 
  		
  		
  	/**
    * The Constructor
    */
    public Location3D() {
    	super();
        init();
	}
    	
    	
	/**
 	* The Constructor
 	* @param id the Actuator identifier
 	*/
	public Location3D(String id) {
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
  	  
  	  
      	// Creates the timestamp property
      	String timestampIRI = "http://com.tecnalia.smool/core/smoolcore#timestamp";
      	String timestampPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > timestampSlot= new FunctionalDatatypeSlot<String>(String.class);
      	timestampSlot._setIRI(timestampIRI);
      	timestampSlot._setPrefix(timestampPrefix);
      	timestampSlot.setRange("xsd:String");
      	this._addProperty(timestampSlot);
  	  
  	  
      	// Creates the x property
      	String xIRI = "http://com.tecnalia.smool/core/smoolcore#x";
      	String xPrefix = "smoolcore";

      	FunctionalDatatypeSlot < Double > xSlot= new FunctionalDatatypeSlot<Double>(Double.class);
      	xSlot._setIRI(xIRI);
      	xSlot._setPrefix(xPrefix);
      	xSlot.setRange("xsd:Double");
      	this._addProperty(xSlot);
  	  
  	  
      	// Creates the y property
      	String yIRI = "http://com.tecnalia.smool/core/smoolcore#y";
      	String yPrefix = "smoolcore";

      	FunctionalDatatypeSlot < Double > ySlot= new FunctionalDatatypeSlot<Double>(Double.class);
      	ySlot._setIRI(yIRI);
      	ySlot._setPrefix(yPrefix);
      	ySlot.setRange("xsd:Double");
      	this._addProperty(ySlot);
  	  
  	  
      	// Creates the z property
      	String zIRI = "http://com.tecnalia.smool/core/smoolcore#z";
      	String zPrefix = "smoolcore";

      	FunctionalDatatypeSlot < Double > zSlot= new FunctionalDatatypeSlot<Double>(Double.class);
      	zSlot._setIRI(zIRI);
      	zSlot._setPrefix(zPrefix);
      	zSlot.setRange("xsd:Double");
      	this._addProperty(zSlot);
  	  
  	}
	/*
	* PROPERTIES: GETTERS AND SETTERS
	*/
 	
 	/**
 	* Sets the dataID property.
 	* @param dataID String value
 	*/
	public Location3D setDataID(String dataID) {
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
	public Location3D setTimestamp(String timestamp) {
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
 	* Sets the x property.
 	* @param x Double value
 	*/
	public Location3D setX(Double x) {
		this.updateAttribute("x",x);
		return this;        
	}
		
	 /**
 	* Gets the x property.
 	* @return a Double value
 	*/
	public Double getX() {
    	return (Double) this._getFunctionalProperty("x").getValue();
	}

 	/**
 	* Sets the y property.
 	* @param y Double value
 	*/
	public Location3D setY(Double y) {
		this.updateAttribute("y",y);
		return this;        
	}
		
	 /**
 	* Gets the y property.
 	* @return a Double value
 	*/
	public Double getY() {
    	return (Double) this._getFunctionalProperty("y").getValue();
	}

 	/**
 	* Sets the z property.
 	* @param z Double value
 	*/
	public Location3D setZ(Double z) {
		this.updateAttribute("z",z);
		return this;        
	}
		
	 /**
 	* Gets the z property.
 	* @return a Double value
 	*/
	public Double getZ() {
    	return (Double) this._getFunctionalProperty("z").getValue();
	}

}
