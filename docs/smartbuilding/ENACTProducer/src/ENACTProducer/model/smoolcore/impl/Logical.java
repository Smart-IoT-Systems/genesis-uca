
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
import org.smool.kpi.model.smart.slots.FunctionalObjectSlot;
import ENACTProducer.model.smoolcore.ILogical;
import ENACTProducer.model.smoolcore.ILogicalLocation;
import ENACTProducer.model.smoolcore.impl.LogicalLocation;

/**
 * This class implements the ontology concept Logical
 * including all its properties.
 * @author Genrated via EMF OWL to java transformation
 * @version 1.0
 */
public class Logical extends AbstractOntConcept implements ILogical, KPProducer, KPConsumer{

    //Not needed.. public static String CLASS_NAMESPACE = "http://com.tecnalia.smool/core/smoolcore#";
  	//Not needed.. public static String CLASS_ID = "Logical";
  	public static String CLASS_IRI = "http://com.tecnalia.smool/core/smoolcore#Logical"; 
  		
  		
  	/**
    * The Constructor
    */
    public Logical() {
    	super();
        init();
	}
    	
    	
	/**
 	* The Constructor
 	* @param id the Actuator identifier
 	*/
	public Logical(String id) {
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
    
      
      	// Creates the logicalLoc property
      	String logicalLocIRI = "http://com.tecnalia.smool/core/smoolcore#logicalLoc";
      	String logicalLocPrefix = "smoolcore";

      	FunctionalObjectSlot < LogicalLocation > logicalLocSlot= new FunctionalObjectSlot<LogicalLocation>(LogicalLocation.class);
      	logicalLocSlot._setIRI(logicalLocIRI);
      	logicalLocSlot._setPrefix(logicalLocPrefix);
      	
      	this._addProperty(logicalLocSlot);
  	  
  	}
	/*
	* PROPERTIES: GETTERS AND SETTERS
	*/
 	
 	/**
 	* Sets the logicalLoc property.
 	* @param logicalLoc ILogicalLocation value
 	*/
	public Logical setLogicalLoc(ILogicalLocation logicalLoc) {
		this.updateAttribute("logicalLoc",logicalLoc);
		return this;        
	}
		
	 /**
 	* Gets the logicalLoc property.
 	* @return a ILogicalLocation value
 	*/
	public ILogicalLocation getLogicalLoc() {
    	return (ILogicalLocation) this._getFunctionalProperty("logicalLoc").getValue();
	}

}
