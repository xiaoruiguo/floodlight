/**
*    Copyright 2012, Big Switch Networks, Inc. 
*    Originally created by David Erickson, Stanford University
* 
*    Licensed under the Apache License, Version 2.0 (the "License"); you may
*    not use this file except in compliance with the License. You may obtain
*    a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*    License for the specific language governing permissions and limitations
*    under the License.
**/

package net.floodlightcontroller.devicemanager.internal;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.VlanVid;

import net.floodlightcontroller.devicemanager.IEntityClass;
import net.floodlightcontroller.devicemanager.SwitchPort;
import net.floodlightcontroller.util.FilterIterator;

/**
 * An iterator for handling device queries
 */
public class DeviceIterator extends FilterIterator<Device> {
    private IEntityClass[] entityClasses;
    
    private MacAddress macAddress;
    private VlanVid vlan;
    private IPv4Address ipv4Address; 
    private DatapathId switchDPID;
    private OFPort switchPort;
    
    /**
     * Construct a new device iterator over the key fields
     * @param subIterator an iterator over the full data structure to scan
     * @param entityClasses the entity classes to search for
     * @param macAddress The MAC address
     * @param vlan the VLAN
     * @param ipv4Address the ipv4 address
     * @param switchDPID the switch DPID
     * @param switchPort the switch port
     */
    public DeviceIterator(Iterator<Device> subIterator, 
                          IEntityClass[] entityClasses,
                          MacAddress macAddress,
                          VlanVid vlan, 
                          IPv4Address ipv4Address, 
                          DatapathId switchDPID,
                          OFPort switchPort) {
        super(subIterator);
        this.entityClasses = entityClasses;
        this.subIterator = subIterator;
        this.macAddress = macAddress;
        this.vlan = vlan;
        this.ipv4Address = ipv4Address;
        this.switchDPID = switchDPID;
        this.switchPort = switchPort;
    }

    @Override
    protected boolean matches(Device value) {
        boolean match;
        if (entityClasses != null) {
            IEntityClass clazz = value.getEntityClass();
            if (clazz == null) return false;

            match = false;
            for (IEntityClass entityClass : entityClasses) {
                if (clazz.equals(entityClass)) {
                    match = true;
                    break;
                }
            }
            if (!match) return false;                
        }
        if (macAddress != null) {
            if (!macAddress.equals(value.getMACAddress()))
                return false;
        }
        if (vlan != null) {
            VlanVid[] vlans = value.getVlanId();
            List<VlanVid> searchableVlanList = Arrays.asList(vlans);
            if (!searchableVlanList.contains(vlan)) {
            	return false;
            }
            /*TODO @Ryan Does the above replace the below?
             * Maybe should change the return of Device to be List/ArrayList instead
            if (Arrays.binarySearch(vlans.vlans, vlan) < 0) 
                return false;
            */
        }
        if (ipv4Address != null) {
            IPv4Address[] ipv4Addresses = value.getIPv4Addresses();
            /*TODO @Ryan same here...
               if (Arrays.binarySearch(ipv4Addresses, ipv4Address) < 0) 
                return false;
             */
            List<IPv4Address> searchableIPv4AddrList = Arrays.asList(ipv4Addresses);
            if (!searchableIPv4AddrList.contains(ipv4Address)) {
            	return false;
            }
        }
        if (switchDPID != null || switchPort != null) {
            SwitchPort[] sps = value.getAttachmentPoints();
            if (sps == null) return false;
            
            match = false;
            for (SwitchPort sp : sps) {
                if (switchDPID != null) {
                    if (!switchDPID.equals(sp.getSwitchDPID()))
                        return false;
                }
                if (switchPort != null) {
                    if (!switchPort.equals(sp.getPort()))
                        return false;
                }
                match = true;
                break;
            }
            if (!match) return false;
        }
        return true;
    }
}
