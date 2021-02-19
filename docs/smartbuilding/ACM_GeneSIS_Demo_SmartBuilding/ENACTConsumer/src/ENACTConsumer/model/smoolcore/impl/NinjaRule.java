
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
import ENACTConsumer.model.smoolcore.INinjaRule;

/**
 * This class implements the ontology concept NinjaRule
 * including all its properties.
 * @author Genrated via EMF OWL to java transformation
 * @version 1.0
 */
public class NinjaRule extends AbstractOntConcept implements INinjaRule, KPProducer, KPConsumer{

    //Not needed.. public static String CLASS_NAMESPACE = "http://com.tecnalia.smool/core/smoolcore#";
  	//Not needed.. public static String CLASS_ID = "NinjaRule";
  	public static String CLASS_IRI = "http://com.tecnalia.smool/core/smoolcore#NinjaRule"; 
  		
  		
  	/**
    * The Constructor
    */
    public NinjaRule() {
    	super();
        init();
	}
    	
    	
	/**
 	* The Constructor
 	* @param id the Actuator identifier
 	*/
	public NinjaRule(String id) {
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
    
      
      	// Creates the ruleID property
      	String ruleIDIRI = "http://com.tecnalia.smool/core/smoolcore#ruleID";
      	String ruleIDPrefix = "smoolcore";

      	FunctionalDatatypeSlot < String > ruleIDSlot= new FunctionalDatatypeSlot<String>(String.class);
      	ruleIDSlot._setIRI(ruleIDIRI);
      	ruleIDSlot._setPrefix(ruleIDPrefix);
      	ruleIDSlot.setRange("xsd:String");
      	this._addProperty(ruleIDSlot);
  	  
  	}
	/*
	* PROPERTIES: GETTERS AND SETTERS
	*/
 	
 	/**
 	* Sets the ruleID property.
 	* @param ruleID String value
 	*/
	public NinjaRule setRuleID(String ruleID) {
		this.updateAttribute("ruleID",ruleID);
		return this;        
	}
		
	 /**
 	* Gets the ruleID property.
 	* @return a String value
 	*/
	public String getRuleID() {
    	return (String) this._getFunctionalProperty("ruleID").getValue();
	}

}
