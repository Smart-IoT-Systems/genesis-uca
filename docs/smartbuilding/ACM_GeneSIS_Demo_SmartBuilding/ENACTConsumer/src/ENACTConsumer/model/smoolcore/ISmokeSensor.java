
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
       
import ENACTConsumer.model.smoolcore.IPhysicalLocation;
       
import ENACTConsumer.model.smoolcore.ISmokeInformation;
       

/**
 * This class implements interface for the ontology concept SmokeSensor
 * including all its properties.
 * @author Genrated via EMF OWL to java transformation
 * @version 1.0
 */

public interface ISmokeSensor extends IAbstractOntConcept, ITrueFalseSensor{

   /*
 	* PROPERTIES: GETTERS AND SETTERS
 	*/
   
 	/**
 	* Sets the deviceID property.
 	* @param deviceID String value
 	*/
 	public ISmokeSensor setDeviceID(String deviceID );

	/**
 	* Gets the deviceID property.
 	* @return a String value
	*/
 	public String getDeviceID();

 	/**
 	* Sets the vendor property.
 	* @param vendor String value
 	*/
 	public ISmokeSensor setVendor(String vendor );

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
 	* Sets the physicalLoc property.
 	* @param physicalLoc IPhysicalLocation value
 	*/
 	public ISmokeSensor setPhysicalLoc(IPhysicalLocation physicalLoc );

	/**
 	* Gets the physicalLoc property.
 	* @return a IPhysicalLocation value
	*/
 	public IPhysicalLocation getPhysicalLoc();

 	/**
 	* Sets the smoke property.
 	* @param smoke ISmokeInformation value
 	*/
 	public ISmokeSensor setSmoke(ISmokeInformation smoke );

	/**
 	* Gets the smoke property.
 	* @return a ISmokeInformation value
	*/
 	public ISmokeInformation getSmoke();
}
