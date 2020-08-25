
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
package ENACTConsumer.model.smoolcore;
       
import org.smool.kpi.model.smart.IAbstractOntConcept;
import ENACTConsumer.model.smoolcore.IAlarm;
       
import ENACTConsumer.model.smoolcore.ILogicalLocation;
       

/**
 * This class implements interface for the ontology concept LogicalActuator
 * including all its properties.
 * @author Genrated via EMF OWL to java transformation
 * @version 1.0
 */

public interface ILogicalActuator extends IAbstractOntConcept, IActuator, ILogical{

   /*
 	* PROPERTIES: GETTERS AND SETTERS
 	*/
   
 	/**
 	* Sets the deviceID property.
 	* @param deviceID String value
 	*/
 	public ILogicalActuator setDeviceID(String deviceID );

	/**
 	* Gets the deviceID property.
 	* @return a String value
	*/
 	public String getDeviceID();

 	/**
 	* Sets the vendor property.
 	* @param vendor String value
 	*/
 	public ILogicalActuator setVendor(String vendor );

	/**
 	* Gets the vendor property.
 	* @return a String value
	*/
 	public String getVendor();

 	/**
 	* Adds the alarms property.
 	* @param alarms IAlarm value to add
 	*/
 	public void addAlarms(IAlarm alarms );

	/**
 	* Removes the alarms property.
 	* @param alarms IAlarm value to remove
	*/
 	public void removeAlarms(IAlarm alarms );

 	/**
 	* Sets the logicalLoc property.
 	* @param logicalLoc ILogicalLocation value
 	*/
 	public ILogicalActuator setLogicalLoc(ILogicalLocation logicalLoc );

	/**
 	* Gets the logicalLoc property.
 	* @return a ILogicalLocation value
	*/
 	public ILogicalLocation getLogicalLoc();
}
