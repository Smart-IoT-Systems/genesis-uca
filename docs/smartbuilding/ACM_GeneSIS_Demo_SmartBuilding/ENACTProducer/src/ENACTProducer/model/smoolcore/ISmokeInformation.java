
/*******************************************************************************
* Copyright (c) 2018 Tecnalia Research and Innovation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
* This file is a result of OWL 2 java transformation using EMF
* 
* Generated by SMOOL SDK Wizard
*******************************************************************************/ 
package ENACTProducer.model.smoolcore;
       
import org.smool.kpi.model.smart.IAbstractOntConcept;
import ENACTProducer.model.smoolcore.ISecurity;
       

/**
 * This class implements interface for the ontology concept SmokeInformation
 * including all its properties.
 * @author Genrated via EMF OWL to java transformation
 * @version 1.0
 */

public interface ISmokeInformation extends IAbstractOntConcept, IBooleanInformation{

   /*
 	* PROPERTIES: GETTERS AND SETTERS
 	*/
   
 	/**
 	* Sets the dataID property.
 	* @param dataID String value
 	*/
 	public ISmokeInformation setDataID(String dataID );

	/**
 	* Gets the dataID property.
 	* @return a String value
	*/
 	public String getDataID();

 	/**
 	* Sets the status property.
 	* @param status Boolean value
 	*/
 	public ISmokeInformation setStatus(Boolean status );

	/**
 	* Gets the status property.
 	* @return a Boolean value
	*/
 	public Boolean getStatus();

 	/**
 	* Sets the timestamp property.
 	* @param timestamp String value
 	*/
 	public ISmokeInformation setTimestamp(String timestamp );

	/**
 	* Gets the timestamp property.
 	* @return a String value
	*/
 	public String getTimestamp();

 	/**
 	* Sets the securityData property.
 	* @param securityData ISecurity value
 	*/
 	public ISmokeInformation setSecurityData(ISecurity securityData );

	/**
 	* Gets the securityData property.
 	* @return a ISecurity value
	*/
 	public ISecurity getSecurityData();
}
