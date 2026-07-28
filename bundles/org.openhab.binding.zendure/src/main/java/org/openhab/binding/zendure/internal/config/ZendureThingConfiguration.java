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
package org.openhab.binding.zendure.internal.config;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The class {@link ZendureThingConfiguration} represents the configuration
 * values available for the Solar-Log Binding.
 *
 * @author Arne Plöse - Initial contribution
 */
@NonNullByDefault
public class ZendureThingConfiguration {

    public static final String CONFIG_DEVICE_IP = "deviceIp";
    public static final String CONFIG_SERIAL_NUMBER = "serialNumber";
    public static final String CONFIG_REFRESH_INTERVAL = "refreshInterval";
    public static final String CONFIG_PORT = "port";

    public static final String PARAM_MDNS_ZENDURE = "Zendure";

    public String deviceIp = "";
    public String serialNumber = "";
    public int refreshInterval = 10;
    public int port = 80;
    public String jsonReadPropertyPath = "/properties/report";
    public String jsonWritePropertyPath = "/properties/write";

    public Map<String, Object> getProperties() {
        final Map<String, Object> result = new TreeMap<>();
        result.put(CONFIG_DEVICE_IP, deviceIp);
        result.put(CONFIG_SERIAL_NUMBER, serialNumber);
        result.put(CONFIG_REFRESH_INTERVAL, refreshInterval);
        result.put(CONFIG_PORT, port);
        return result;
    }
}
