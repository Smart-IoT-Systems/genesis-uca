
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
import ENACTProducer.model.smoolcore.INature;

/**
 * This class implements the ontology concept Nature
 * including all its properties.
 * @author Genrated via EMF OWL to java transformation
 * @version 1.0
 */
public class Nature extends AbstractOntConcept implements INature, KPProducer, KPConsumer{

    //Not needed.. public static String CLASS_NAMESPACE = "http://com.tecnalia.smool/core/smoolcore#";
  	//Not needed.. public static String CLASS_ID = "Nature";
  	public static String CLASS_IRI = "http://com.tecnalia.smool/core/smoolcore#Nature"; 
  		
  		
  	/**
    * The Constructor
    */
    public Nature() {
    	super();
        init();
	}
    	
    	
	/**
 	* The Constructor
 	* @param id the Actuator identifier
 	*/
	public Nature(String id) {
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
    
    }
	/*
	* PROPERTIES: GETTERS AND SETTERS
	*/
 	
}
