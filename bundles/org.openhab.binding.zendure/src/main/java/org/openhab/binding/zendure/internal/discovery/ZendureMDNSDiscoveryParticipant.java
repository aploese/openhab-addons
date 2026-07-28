/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.zendure.internal.discovery;

import java.net.Inet4Address;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zendure.internal.ZendureBindingConstants;
import org.openhab.binding.zendure.internal.ZendureBindingConstants.ThingType;
import org.openhab.binding.zendure.internal.config.ZendureThingConfiguration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class identifies Zendure devices by their mDNS service information.
 *
 * @author Arne Plöse - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class)
public class ZendureMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    public static final String SERVICE_TYPE = "_http._tcp.local.";

    private final Logger logger = LoggerFactory.getLogger(ZendureMDNSDiscoveryParticipant.class);

    @Activate
    public ZendureMDNSDiscoveryParticipant() {
        logger.debug("Activating Zendure mDNS discovery service");
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return ZendureBindingConstants.SUPPORTED_THING_TYPES;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    /**
     * Process updates to Binding Config
     *
     * @param componentContext
     */
    @Modified
    protected void modified(final ComponentContext componentContext) {
        logger.warn("TODO Zendure Binding Configuration modified - not implemented");
    }

    @Override
    public @Nullable DiscoveryResult createResult(final ServiceInfo service) {
        final String serviceName = service.getName();
        final String[] splitted = serviceName.split("-");
        if (splitted.length != 3) {
            logger.warn("Discovered Device: {} only 2 - are allowed in name .", serviceName);
            return null;
        } else {
            if (!"Zendure".equals(splitted[0])) {
                logger.warn("Discovered Device: {} first part must be \"Zendure\".", serviceName);
                return null;
            }
        }
        final String deviceName = splitted[1];
        final String serialNumber = splitted[2];

        final Inet4Address[] inet4Addresses = service.getInet4Addresses();
        if (inet4Addresses.length == 0) {
            logger.warn("Discovered Device: {} has no IP address dropping for now.", serviceName);
            return null;
        }

        ZendureThingConfiguration config = new ZendureThingConfiguration();
        config.deviceIp = inet4Addresses[0].getHostAddress();
        config.serialNumber = serialNumber;
        config.port = service.getPort();

        final String label;
        switch (deviceName) {
            case "solarFlow800" -> {
                label = "SolarFlow 800";
            }
            case "solarFlow800Pro" -> {
                label = "SolarFlow 800 Pro";
            }
            case "solarFlow1600AC+" -> {
                label = "SolarFlow 1600 AC+";
            }
            default -> {
                logger.warn("Device: {} unknown Thing type: {}", serviceName, deviceName);
                return null;
            }
        }

        Map<String, Object> properties = config.getProperties();

        Enumeration<String> value = service.getPropertyNames();
        while (value.hasMoreElements()) {
            final String name = value.nextElement();
            properties.put(name, service.getPropertyString(name));
        }

        ThingUID result = getThingUID(service);
        if (result != null) {
            return DiscoveryResultBuilder.create(result).withProperties(properties).withLabel(label)
                    .withRepresentationProperty("serialNumber").build();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String serviceName = service.getName();
        if (serviceName == null) {
            logger.warn("serviceName is null");
            return null;
        }
        String[] splitted = serviceName.split("-");
        if (splitted.length != 3) {
            logger.warn("Discovered Device: {} only 2 - are allowed in name .", serviceName);
            return null;
        } else {
            if (!"Zendure".equals(splitted[0])) {
                logger.warn("Discovered Device: {} first part must be \"Zendure\".", serviceName);
                return null;
            }
        }
        String deviceName = splitted[1];
        String serialNumber = splitted[2];

        ThingType thingType;
        switch (deviceName) {
            case "solarFlow800" -> {
                thingType = ThingType.SOLAR_FLOW_800;
            }
            case "solarFlow800Pro" -> {
                thingType = ThingType.SOLAR_FLOW_800_PRO;
            }
            case "solarFlow1600AC+" -> {
                thingType = ThingType.SOLAR_FLOW_1600AC_PLUS;
            }
            default -> {
                logger.warn("Device: {} unknown Thing type: {}", serviceName, deviceName);
                return null;
            }
        }
        return new ThingUID(thingType.thingTypeUID, serialNumber);
    }
}
