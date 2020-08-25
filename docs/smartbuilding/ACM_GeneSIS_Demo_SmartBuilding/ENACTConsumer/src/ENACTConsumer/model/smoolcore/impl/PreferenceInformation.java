
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
import ENACTConsumer.model.smoolcore.IPreferenceInformation;
import ENACTConsumer.model.smoolcore.ISecurity;
import ENACTConsumer.model.smoolcore.impl.Security;

/**
 * This class implements the ontology concept PreferenceInformation
 * including all its properties.
 * @author Genrated via EMF OWL to java transformation
 * @version 1.0
 */
public class PreferenceInformation extends AbstractOntConcept implements IPreferenceInformation, KPProducer, KPConsumer{

    //Not needed.. public static String CLASS_NAMESPACE = "http://com.tecnalia.smool/core/smoolcore#";
  	//Not needed.. public static String CLASS_ID = "PreferenceInformation";
  	public static String CLASS_IRI = "http://com.tecnalia.smool/core/smoolcore#PreferenceInformation"; 
  		
  		
  	/**
    * The Constructor
    */
    public PreferenceInformation() {
    	super();
        init();
	}
    	
    	
	/**
 	* The Constructor
 	* @param id the Actuator identifier
 	*/
	public PreferenceInformation(String id) {
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
    
      
      	// Creates the PreferredTemperature property
      	String PreferredTemperatureIRI = "http://com.tecnalia.smool/core/smoolcore#PreferredTemperature";
      	String PreferredTemperaturePrefix = "smoolcore";

      	FunctionalDatatypeSlot < Integer > PreferredTemperatureSlot= new FunctionalDatatypeSlot<Integer>(Integer.class);
      	PreferredTemperatureSlot._setIRI(PreferredTemperatureIRI);
      	PreferredTemperatureSlot._setPrefix(PreferredTemperaturePrefix);
      	PreferredTemperatureSlot.setRange("xsd:Integer");
      	this._addProperty(PreferredTemperatureSlot);
  	  
  	  
      	// Creates the dataID property
      	String dataIDIRI = "http://com.tecnalia.smool/core/smoolcore#dataID";
      	String dataIDPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > dataIDSlot= new FunctionalDatatypeSlot<String>(String.class);
      	dataIDSlot._setIRI(dataIDIRI);
      	dataIDSlot._setPrefix(dataIDPrefix);
      	dataIDSlot.setRange("xsd:String");
      	this._addProperty(dataIDSlot);
  	  
  	  
      	// Creates the disabilityType property
      	String disabilityTypeIRI = "http://com.tecnalia.smool/core/smoolcore#disabilityType";
      	String disabilityTypePrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > disabilityTypeSlot= new FunctionalDatatypeSlot<String>(String.class);
      	disabilityTypeSlot._setIRI(disabilityTypeIRI);
      	disabilityTypeSlot._setPrefix(disabilityTypePrefix);
      	disabilityTypeSlot.setRange("xsd:String");
      	this._addProperty(disabilityTypeSlot);
  	  
  	  
      	// Creates the isSmoker property
      	String isSmokerIRI = "http://com.tecnalia.smool/core/smoolcore#isSmoker";
      	String isSmokerPrefix = "smoolcore";

      	FunctionalDatatypeSlot < Boolean > isSmokerSlot= new FunctionalDatatypeSlot<Boolean>(Boolean.class);
      	isSmokerSlot._setIRI(isSmokerIRI);
      	isSmokerSlot._setPrefix(isSmokerPrefix);
      	isSmokerSlot.setRange("xsd:Boolean");
      	this._addProperty(isSmokerSlot);
  	  
  	  
      	// Creates the timestamp property
      	String timestampIRI = "http://com.tecnalia.smool/core/smoolcore#timestamp";
      	String timestampPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > timestampSlot= new FunctionalDatatypeSlot<String>(String.class);
      	timestampSlot._setIRI(timestampIRI);
      	timestampSlot._setPrefix(timestampPrefix);
      	timestampSlot.setRange("xsd:String");
      	this._addProperty(timestampSlot);
  	  
  	  
      	// Creates the userIDPublic property
      	String userIDPublicIRI = "http://com.tecnalia.smool/core/smoolcore#userIDPublic";
      	String userIDPublicPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > userIDPublicSlot= new FunctionalDatatypeSlot<String>(String.class);
      	userIDPublicSlot._setIRI(userIDPublicIRI);
      	userIDPublicSlot._setPrefix(userIDPublicPrefix);
      	userIDPublicSlot.setRange("xsd:String");
      	this._addProperty(userIDPublicSlot);
  	  
  	  
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
 	* Sets the PreferredTemperature property.
 	* @param PreferredTemperature Integer value
 	*/
	public PreferenceInformation setPreferredTemperature(Integer PreferredTemperature) {
		this.updateAttribute("PreferredTemperature",PreferredTemperature);
		return this;        
	}
		
	 /**
 	* Gets the PreferredTemperature property.
 	* @return a Integer value
 	*/
	public Integer getPreferredTemperature() {
    	return (Integer) this._getFunctionalProperty("PreferredTemperature").getValue();
	}

 	/**
 	* Sets the dataID property.
 	* @param dataID String value
 	*/
	public PreferenceInformation setDataID(String dataID) {
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
 	* Sets the disabilityType property.
 	* @param disabilityType String value
 	*/
	public PreferenceInformation setDisabilityType(String disabilityType) {
		this.updateAttribute("disabilityType",disabilityType);
		return this;        
	}
		
	 /**
 	* Gets the disabilityType property.
 	* @return a String value
 	*/
	public String getDisabilityType() {
    	return (String) this._getFunctionalProperty("disabilityType").getValue();
	}

 	/**
 	* Sets the isSmoker property.
 	* @param isSmoker Boolean value
 	*/
	public PreferenceInformation setIsSmoker(Boolean isSmoker) {
		this.updateAttribute("isSmoker",isSmoker);
		return this;        
	}
		
	 /**
 	* Gets the isSmoker property.
 	* @return a Boolean value
 	*/
	public Boolean getIsSmoker() {
    	return (Boolean) this._getFunctionalProperty("isSmoker").getValue();
	}

 	/**
 	* Sets the timestamp property.
 	* @param timestamp String value
 	*/
	public PreferenceInformation setTimestamp(String timestamp) {
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
 	* Sets the userIDPublic property.
 	* @param userIDPublic String value
 	*/
	public PreferenceInformation setUserIDPublic(String userIDPublic) {
		this.updateAttribute("userIDPublic",userIDPublic);
		return this;        
	}
		
	 /**
 	* Gets the userIDPublic property.
 	* @return a String value
 	*/
	public String getUserIDPublic() {
    	return (String) this._getFunctionalProperty("userIDPublic").getValue();
	}

 	/**
 	* Sets the securityData property.
 	* @param securityData ISecurity value
 	*/
	public PreferenceInformation setSecurityData(ISecurity securityData) {
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
