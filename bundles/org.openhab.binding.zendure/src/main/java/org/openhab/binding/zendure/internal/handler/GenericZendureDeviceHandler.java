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
package org.openhab.binding.zendure.internal.handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zendure.internal.ZendureBindingConstants.ThingType;
import org.openhab.binding.zendure.internal.config.ZendureThingConfiguration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * The {@link GenericZendureDeviceHandler} is responsible for handling commands,
 * which are sent to one of the channels. It does the "heavy lifting" of
 * connecting to the Solar-Log, getting the data, parsing it and updating the
 * channels.
 *
 * @author Arne Plöse - Initial contribution
 */
@NonNullByDefault
public class GenericZendureDeviceHandler extends BaseBridgeHandler {

    private final Object httpReadWriteLock = new Object();

    private final Logger logger = LoggerFactory.getLogger(GenericZendureDeviceHandler.class);
    private final ZendureThingConfiguration config;
    private final ThingType zendureDeviceType;

    private final ChannelUID batteryPackBypassUID;
    private final ChannelUID batteryPackCapacityUID;
    private final ChannelUID batteryPackDcStateUID;
    private final ChannelUID batteryPackHeatingUID;
    private final ChannelUID batteryPackPowerUID;
    private final ChannelUID batteryPackNumberOfBatteriesUID;
    private final ChannelUID batteryPackPackStateUID;
    private final ChannelUID batteryPackRemaingDischargeTimeUID;
    private final ChannelUID batteryPackStateOfChargeUID;
    private final ChannelUID batteryPackDischargeLevelMaxUID;
    private final ChannelUID batteryPackDischargeLevelMinUID;
    private final ChannelUID batteryPackSocStateUID;
    private final ChannelUID batteryPackCalTimeUID;

    private final ChannelUID deviceIsErrorUID;
    private final ChannelUID deviceRssiUID;
    private final ChannelUID deviceTemperatureUID;
    private final ChannelUID deviceLampSwitchUID;
    private final ChannelUID deviceFanModeUID;
    private final ChannelUID deviceFanSpeedUID;
    private final ChannelUID deviceSmartModeUID;
    private final ChannelUID deviceFaultLevelUID;

    private final ChannelUID pvStateUID;
    private final ChannelUID pvPowerUID;
    private final ChannelUID pvPowerPanel1UID;
    private final ChannelUID pvPowerPanel2UID;
    private final ChannelUID pvPowerPanel3UID;
    private final ChannelUID pvPowerPanel4UID;

    private final ChannelUID inverterMainsAcStateUID;
    private final ChannelUID inverterMainsGridStateUID;
    private final ChannelUID inverterMainsPowerUID;
    private final ChannelUID inverterMainsPowerSetpointUID;
    private final ChannelUID inverterMainsGridStandardUID;
    private final ChannelUID inverterMainsReverseFlowUID;
    private final ChannelUID inverterMainsGridReverseFlowModeUID;
    private final ChannelUID inverterMainsPowerLimitOutUID;
    private final ChannelUID inverterMainsMaxPowerLimitInUID;
    private final ChannelUID inverterMainsPhaseSwitchUID;

    private final ChannelUID inverterOffGridModeUID;
    private final ChannelUID inverterOffGridPowerUID;
    private final ChannelUID acCouplingStateUID;
    private final ChannelUID offGridStateUID;
    private final ChannelUID dryNodeStateUID;

    public GenericZendureDeviceHandler(Bridge bridge, ThingType zendureDeviceType) {
        super(bridge);
        this.zendureDeviceType = zendureDeviceType;
        this.config = getConfigAs(ZendureThingConfiguration.class);

        batteryPackBypassUID = new ChannelUID(getThing().getUID(), Channels.BatteryPack.GROUP_BATTERY_PACK,
                Channels.BatteryPack.BYPASS);
        batteryPackDcStateUID = new ChannelUID(getThing().getUID(), Channels.BatteryPack.GROUP_BATTERY_PACK,
                Channels.BatteryPack.DC_STATE);
        batteryPackRemaingDischargeTimeUID = new ChannelUID(getThing().getUID(),
                Channels.BatteryPack.GROUP_BATTERY_PACK, Channels.BatteryPack.REMAINING_DISCHARGE_TIME);
        batteryPackStateOfChargeUID = new ChannelUID(getThing().getUID(), Channels.BatteryPack.GROUP_BATTERY_PACK,
                Channels.BatteryPack.STATE_OF_CHARGE);
        batteryPackDischargeLevelMaxUID = new ChannelUID(getThing().getUID(), Channels.BatteryPack.GROUP_BATTERY_PACK,
                Channels.BatteryPack.CHARGE_LEVGEL_MAX);
        batteryPackDischargeLevelMinUID = new ChannelUID(getThing().getUID(), Channels.BatteryPack.GROUP_BATTERY_PACK,
                Channels.BatteryPack.DISCHARGE_LEVEL_MIN);
        batteryPackCapacityUID = new ChannelUID(getThing().getUID(), Channels.BatteryPack.GROUP_BATTERY_PACK,
                Channels.BatteryPack.CAPACITY);
        batteryPackHeatingUID = new ChannelUID(getThing().getUID(), Channels.BatteryPack.GROUP_BATTERY_PACK,
                Channels.BatteryPack.HEATING);
        batteryPackPowerUID = new ChannelUID(getThing().getUID(), Channels.BatteryPack.GROUP_BATTERY_PACK,
                Channels.BatteryPack.POWER);
        batteryPackPackStateUID = new ChannelUID(getThing().getUID(), Channels.BatteryPack.GROUP_BATTERY_PACK,
                Channels.BatteryPack.PACK_STATE);
        batteryPackNumberOfBatteriesUID = new ChannelUID(getThing().getUID(), Channels.BatteryPack.GROUP_BATTERY_PACK,
                Channels.BatteryPack.NUMBER_OF_BATTERIES);
        batteryPackSocStateUID = new ChannelUID(getThing().getUID(), Channels.BatteryPack.GROUP_BATTERY_PACK,
                Channels.BatteryPack.SOC_STATE);
        batteryPackCalTimeUID = new ChannelUID(getThing().getUID(), Channels.BatteryPack.GROUP_BATTERY_PACK,
                Channels.BatteryPack.CALIBRATION_TIME);

        deviceTemperatureUID = new ChannelUID(getThing().getUID(), Channels.Device.GROUP_DEVICE,
                Channels.Device.TEMPERATURE);
        deviceRssiUID = new ChannelUID(getThing().getUID(), Channels.Device.GROUP_DEVICE, Channels.Device.WLAN_RSSI);
        deviceIsErrorUID = new ChannelUID(getThing().getUID(), Channels.Device.GROUP_DEVICE, Channels.Device.IS_ERROR);
        deviceSmartModeUID = new ChannelUID(getThing().getUID(), Channels.Device.GROUP_DEVICE,
                Channels.Device.SMART_MODE);
        deviceFanSpeedUID = new ChannelUID(getThing().getUID(), Channels.Device.GROUP_DEVICE,
                Channels.Device.FAN_SPEED);
        deviceFanModeUID = new ChannelUID(getThing().getUID(), Channels.Device.GROUP_DEVICE, Channels.Device.FAN_MODE);
        deviceLampSwitchUID = new ChannelUID(getThing().getUID(), Channels.Device.GROUP_DEVICE,
                Channels.Device.LAMP_SWITCH);
        deviceFaultLevelUID = new ChannelUID(getThing().getUID(), Channels.Device.GROUP_DEVICE,
                Channels.Device.FAULT_LEVEL);

        pvPowerUID = new ChannelUID(getThing().getUID(), Channels.Pv.GROUP_PV, Channels.Pv.POWER);
        pvPowerPanel1UID = new ChannelUID(getThing().getUID(), Channels.Pv.GROUP_PV, Channels.Pv.POWER_PANEL_1);
        pvPowerPanel2UID = new ChannelUID(getThing().getUID(), Channels.Pv.GROUP_PV, Channels.Pv.POWER_PANEL_2);
        pvPowerPanel3UID = new ChannelUID(getThing().getUID(), Channels.Pv.GROUP_PV, Channels.Pv.POWER_PANEL_3);
        pvPowerPanel4UID = new ChannelUID(getThing().getUID(), Channels.Pv.GROUP_PV, Channels.Pv.POWER_PANEL_4);
        pvStateUID = new ChannelUID(getThing().getUID(), Channels.Pv.GROUP_PV, Channels.Pv.STATE);

        inverterMainsAcStateUID = new ChannelUID(getThing().getUID(), Channels.InverterMains.GROUP_INVERTER_MAINS,
                Channels.InverterMains.AC_STATE);
        inverterMainsGridStateUID = new ChannelUID(getThing().getUID(), Channels.InverterMains.GROUP_INVERTER_MAINS,
                Channels.InverterMains.GRID_STATE);
        inverterMainsReverseFlowUID = new ChannelUID(getThing().getUID(), Channels.InverterMains.GROUP_INVERTER_MAINS,
                Channels.InverterMains.REVERSE_FLOW);
        inverterMainsPowerUID = new ChannelUID(getThing().getUID(), Channels.InverterMains.GROUP_INVERTER_MAINS,
                Channels.InverterMains.POWER);

        inverterMainsMaxPowerLimitInUID = new ChannelUID(getThing().getUID(),
                Channels.InverterMains.GROUP_INVERTER_MAINS, Channels.InverterMains.MAX_POWER_LIMIT_IN);
        inverterMainsPowerLimitOutUID = new ChannelUID(getThing().getUID(), Channels.InverterMains.GROUP_INVERTER_MAINS,
                Channels.InverterMains.MAX_POWER_LIMIT_OUT);
        inverterMainsPowerSetpointUID = new ChannelUID(getThing().getUID(), Channels.InverterMains.GROUP_INVERTER_MAINS,
                Channels.InverterMains.POWER_SETPOINT);

        inverterMainsGridReverseFlowModeUID = new ChannelUID(getThing().getUID(),
                Channels.InverterMains.GROUP_INVERTER_MAINS, Channels.InverterMains.GRID_REVERSE_FLOW_MODE);
        inverterMainsGridStandardUID = new ChannelUID(getThing().getUID(), Channels.InverterMains.GROUP_INVERTER_MAINS,
                Channels.InverterMains.GRID_STANDARD);
        inverterMainsPhaseSwitchUID = new ChannelUID(getThing().getUID(), Channels.InverterMains.GROUP_INVERTER_MAINS,
                Channels.InverterMains.PHASE_SWITCH);

        inverterOffGridPowerUID = new ChannelUID(getThing().getUID(), Channels.InverterOffGrid.GROUP_INVERTER_OFF_GRID,
                Channels.InverterOffGrid.POWER);
        inverterOffGridModeUID = new ChannelUID(getThing().getUID(), Channels.InverterOffGrid.GROUP_INVERTER_OFF_GRID,
                Channels.InverterOffGrid.MODE);
        // TODO Where to put this?
        acCouplingStateUID = new ChannelUID(getThing().getUID(), "acCouplingState");
        offGridStateUID = new ChannelUID(getThing().getUID(), "offGridState");
        dryNodeStateUID = new ChannelUID(getThing().getUID(), "dryNodeState");
    }

    private static interface PVState {

        static final int VALUE_STOPPED = 0;
        static final int VALUE_RUNNING = 1;

        final DecimalType STOPPED = DecimalType.ZERO;
        final DecimalType RUNNING = new DecimalType(VALUE_RUNNING);
    }

    private static interface BatteryPackState {

        static final int VALUE_STANDBY = 0;
        static final int VALUE_CHARGING = 1;
        static final int VALUE_DISCHARGING = 2;

        final DecimalType STANDBY = DecimalType.ZERO;
        final DecimalType CHARGING = new DecimalType(VALUE_CHARGING);
        final DecimalType DISCHARGING = new DecimalType(VALUE_DISCHARGING);
    }

    private static interface BatteryPackBypass {

        static final int VALUE_NO = 0;
        static final int VALUE_YES = 1;
        static final int VALUE_VALUE_2 = 2;

        final DecimalType NO = DecimalType.ZERO;
        final DecimalType YES = new DecimalType(VALUE_YES);
        final DecimalType VALUE_2 = new DecimalType(VALUE_VALUE_2);
    }

    private static interface DeviceState {

        static final int VALUE_NO_ERROR_PENDING = 0;
        static final int VALUE_ERROR_PENDING = 1;

        final DecimalType NORMAL_OPERATION = DecimalType.ZERO;
        final DecimalType ERROR = new DecimalType(VALUE_ERROR_PENDING);
    }

    private static interface GridReverseFlowMode {

        static final int VALUE_DISABLED = 0;
        static final int VALUE_ALLOWED = 1;
        static final int VALUE_FORBIDDEN = 2;

        final DecimalType DISABLED = DecimalType.ZERO;
        final DecimalType ALLOWED = new DecimalType(VALUE_ALLOWED);
        final DecimalType FORBIDDEN = new DecimalType(VALUE_FORBIDDEN);
    }

    private static interface GridOffMode {

        static final int VALUE_NORMAL = 0;
        static final int VALUE_ECO = 1;
        static final int VALUE_OFF = 2;

        final DecimalType NORMAL = DecimalType.ZERO;
        final DecimalType ECO = new DecimalType(VALUE_ECO);
        final DecimalType OFF = new DecimalType(VALUE_OFF);
    }

    private static interface GridState {

        static final int VALUE_NOT_CONNECTED = 0;
        static final int VALUE_CONNECTED = 1;

        final DecimalType NOT_CONNECTED = DecimalType.ZERO;
        final DecimalType CONNECTED = new DecimalType(VALUE_CONNECTED);
    }

    private static interface GridStandard {

        static final int VALUE_GERMANY = 0;
        static final int VALUE_FRANCE = 1;
        static final int VALUE_AUSTRIA = 2;
        static final int VALUE_SWIZERLAND = 3;
        static final int VALUE_BELGIUM = 4;

        final DecimalType GERMANY = DecimalType.ZERO;
        final DecimalType FRANCE = new DecimalType(VALUE_FRANCE);
        final DecimalType AUSTRIA = new DecimalType(VALUE_AUSTRIA);
        final DecimalType SWIZERLAND = new DecimalType(VALUE_SWIZERLAND);
        final DecimalType BELGIUM = new DecimalType(VALUE_BELGIUM);
    }

    private static interface ReverseState {

        static final int VALUE_NO = 0;
        static final int VALUE_YES = 1;

        final DecimalType NO = DecimalType.ZERO;
        final DecimalType YES = new DecimalType(VALUE_YES);
    }

    private static interface SocStatus {

        static final int VALUE_NOT_CALIBRATING = 0;
        static final int VALUE_CALIBRATING = 1;

        final DecimalType NOT_CALIBRATING = DecimalType.ZERO;
        final DecimalType CALIBRATING = new DecimalType(VALUE_CALIBRATING);
    }

    private static interface DcStatus {

        static final int VALUE_STOPPED = 0;
        static final int VALUE_BATTERY_INPUT = 1;
        static final int VALUE_BATTERY_OUTPUT = 2;

        final DecimalType STOPPED = DecimalType.ZERO;
        final DecimalType BATTERY_INPUT = new DecimalType(VALUE_BATTERY_INPUT);
        final DecimalType BATTERY_OUTPUT = new DecimalType(VALUE_BATTERY_OUTPUT);
    }

    private static interface AcStatus {

        static final int VALUE_STOPPED = 0;
        static final int VALUE_GRID_CONNECTED = 1;
        static final int VALUE_CHARGING = 2;

        final DecimalType STOPPED = DecimalType.ZERO;
        final DecimalType GRID_CONNECTED = new DecimalType(VALUE_GRID_CONNECTED);
        final DecimalType CHARGING = new DecimalType(VALUE_CHARGING);
    }

    private static interface SocLimit {

        static final int VALUE_NORMAL = 0;
        static final int VALUE_CHARGE_LIMIT_REACHED = 1;
        static final int VALUE_DISCHARGE_LIMIT_REACHED = 2;

        final DecimalType NORMAL = DecimalType.ZERO;
        final DecimalType CHARGE_LIMIT_REACHED = new DecimalType(VALUE_CHARGE_LIMIT_REACHED);
        final DecimalType DISCHARGE_LIMIT_REACHED = new DecimalType(VALUE_DISCHARGE_LIMIT_REACHED);
    }

    private static interface Channels {

        static interface Pv {

            // PV related channels
            public static final String GROUP_PV = "PV";

            public static final String POWER = "Power";
            public static final String POWER_PANEL_1 = "PowerPanel_1";
            public static final String POWER_PANEL_2 = "PowerPanel_2";
            public static final String POWER_PANEL_3 = "PowerPanel_3";
            public static final String POWER_PANEL_4 = "PowerPanel_4";
            public static final String STATE = "State";
        }

        static interface InverterMains {
            // Inverter - Mains related channels
            public static final String GROUP_INVERTER_MAINS = "Inverter_Mains";
            public static final String AC_STATE = "AcState";
            public static final String GRID_STATE = "GridState";
            public static final String POWER = "Power";
            public static final String REVERSE_FLOW = "ReverseFlow";
            public static final String POWER_SETPOINT = "PowerSetpoint";
            public static final String MAX_POWER_LIMIT_IN = "MaxPowerLimitIn";
            public static final String MAX_POWER_LIMIT_OUT = "MaxPowerLimitOut";
            public static final String GRID_STANDARD = "GridStandard";
            public static final String GRID_REVERSE_FLOW_MODE = "GridReverseFlowMode";
            public static final String PHASE_SWITCH = "PhaseSwitch";
        }

        static interface InverterOffGrid {
            // Inverter - Off Grid related channels
            public static final String GROUP_INVERTER_OFF_GRID = "Inverter_OffGrid";
            public static final String POWER = "Power";
            public static final String MODE = "Mode";
        }

        static interface BatteryPack {
            // Battery pack related channels
            public static final String GROUP_BATTERY_PACK = "BatteryPack";
            public static final String BYPASS = "Bypass";
            public static final String DC_STATE = "DcState";
            public static final String HEATING = "Heating";
            public static final String POWER = "Power";
            public static final String CAPACITY = "Capacity";
            public static final String NUMBER_OF_BATTERIES = "NumberOfBatteries";
            public static final String STATE_OF_CHARGE = "StateOfCharge";
            public static final String CHARGE_LEVGEL_MAX = "ChargeLevelMax";
            public static final String DISCHARGE_LEVEL_MIN = "DischargeLevelMin";
            public static final String REMAINING_DISCHARGE_TIME = "RemainingDischargeTime";
            public static final String PACK_STATE = "PackState";
            public static final String SOC_STATE = "SocState";
            public static final String CALIBRATION_TIME = "CalibrationTime";
        }

        static interface Device {
            // Device related channels
            public static final String GROUP_DEVICE = "Device";
            public static final String TEMPERATURE = "Temperature";
            public static final String FAULT_LEVEL = "FaultLevel";
            public static final String LAMP_SWITCH = "LampSwitch";
            public static final String FAN_MODE = "FanMode";
            public static final String FAN_SPEED = "FanSpeed";
            public static final String SMART_MODE = "SmartMode";
            public static final String WLAN_RSSI = "RSSI";
            public static final String IS_ERROR = "IsError";
        }
    }

    private int packNum;
    private double batteryPackCapacity;

    @Nullable
    private ScheduledFuture<?> refreshJob;

    @Override
    public void initialize() {
        logger.debug("Initializing Zendure");
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            logger.debug("Running refresh cycle");
            try {
                synchronized (httpReadWriteLock) {
                    refresh();
                }
                updateStatus(ThingStatus.ONLINE);
                // Very rudimentary Exception differentiation
            } catch (IOException e) {
                logger.warn("{}: Error reading response from Zendure Device", getThing().getUID(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Communication error with the device. Please retry later. \n" + e.getMessage());
            } catch (JsonSyntaxException je) {
                logger.warn("{}: Invalid JSON when refreshing ", getThing().getUID(), je);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Invalid JSON when refreshing. Please retry later. \n" + je.getMessage());
            } catch (Exception e) {
                logger.warn("{}: Error refreshing ", getThing().getUID(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unknown Error with the device. Please retry later. \n" + e.getMessage());
            } catch (Throwable t) {
                logger.warn("{}: Throwable refreshing ", getThing().getUID(), t);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unknown Error with the device. Please retry later. \n" + t.getMessage());
            }
        }, 0, config.refreshInterval < 1 ? 1 : config.refreshInterval, TimeUnit.SECONDS); // Minimum interval is 1 s
    }

    // TODO reader.skipValue sollte nicht mehr vorkommen
    private void refresh() throws Exception {
        logger.debug("Starting refresh handler");
        try {
            URI uri = new URI("http", config.deviceIp, config.jsonReadPropertyPath, null);
            HttpURLConnection httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            try {
                httpConnection.setRequestMethod("GET");
                if (httpConnection.getResponseCode() != 200) {
                    throw new RuntimeException(
                            "Can't get data from device: " + httpConnection.getResponseCode() + " " + httpConnection);
                }

                JsonReader reader = new JsonReader(new InputStreamReader(httpConnection.getInputStream()));
                if (!reader.hasNext()) {
                    throw new RuntimeException("Empty!");
                }
                reader.beginObject();

                String name = reader.nextName();
                if ("timestamp".equals(name)) {
                    reader.skipValue();
                } else {
                    throw new RuntimeException("expected \"timestamp\" but found: \"" + name + '\"');
                }

                name = reader.nextName();
                if ("messageId".equals(name)) {
                    reader.skipValue();
                } else {
                    throw new RuntimeException("expected \"messageId\" but found: \"" + name + '\"');
                }

                name = reader.nextName();
                if ("sn".equals(name)) {
                    final String serialNumber = reader.nextString();
                    if (!config.serialNumber.equals(serialNumber)) {
                        throw new RuntimeException("Wrong SerialNumber: expected \"" + config.serialNumber
                                + "\" but found: \"" + serialNumber + '\"');
                    }
                } else {
                    throw new RuntimeException("expected \"sn\" but found: \"" + name + '\"');
                }

                name = reader.nextName();
                if ("version".equals(name)) {
                    final int version = reader.nextInt();
                    switch (version) {
                        case 2 -> {
                        }
                        case 3 -> {
                        }
                        default ->
                            throw new RuntimeException("Wrong version: expected \"2\" but found: \"" + version + '\"');
                    }
                } else {
                    throw new RuntimeException("expected \"version\" but found: \"" + name + '\"');
                }

                name = reader.nextName();
                if ("product".equals(name)) {
                    final String value = reader.nextString();
                    switch (zendureDeviceType) {
                        case SOLAR_FLOW_800 -> {
                            if (!"solarFlow800".equals(value)) {
                                throw new RuntimeException(
                                        "expected \"product\" == \"solarFlow800\" but found: \"" + value + '\"');
                            }
                        }
                        case SOLAR_FLOW_800_PRO -> {
                            if (!"solarFlow800Pro".equals(value)) {
                                throw new RuntimeException(
                                        "expected \"product\" == \"solarFlow800Pro\" but found: \"" + value + '\"');
                            }
                        }
                        case SOLAR_FLOW_1600AC_PLUS -> {
                            if (!"solarFlow1600AC+".equals(value)) {
                                throw new RuntimeException(
                                        "expected \"product\" == \"solarFlow1600AC+\" but found: \"" + value + '\"');
                            }
                        }
                        default -> throw new RuntimeException("unknown \"product\" found: \"" + value + '\"');
                    }
                } else {
                    throw new RuntimeException("expected \"product\" but found: \"" + name + '\"');
                }

                name = reader.nextName();
                if ("properties".equals(name)) {
                    reader.beginObject();
                    readSolarFlowProperties(reader);
                    reader.endObject();
                } else {
                    throw new RuntimeException("expected \"properties\" but found: \"" + name + '\"');
                }

                batteryPackCapacity = 0.0;

                name = reader.nextName();
                if ("packData".equals(name)) {
                    int i = 0;
                    reader.beginArray();
                    while (reader.hasNext()) {
                        reader.beginObject();
                        readBatteryPackProperties(reader, i);
                        reader.endObject();
                        i++;
                    }
                    reader.endArray();
                    if (i != packNum) {
                        throw new RuntimeException("Number of batteriepacks mismatch! property \"packNum\" = " + packNum
                                + " entries found = " + i);
                    }
                } else {
                    throw new RuntimeException("expected \"packData\" but found: \"" + name + '\"');
                }

                reader.endObject();
            } finally {
                httpConnection.disconnect();
            }

            updateState(batteryPackCapacityUID, new DecimalType(batteryPackCapacity));

            logger.debug("Refresh DONE");
        } catch (URISyntaxException ex) {
            logger.warn("Syntax exception in refresh()", ex);
            throw ex;
        }
    }

    private void readBatteryPackProperties(JsonReader reader, int index) throws IOException {
        final double capacity;
        final String model;
        // "": "CO4EHNCDN234434",
        String name = reader.nextName();
        final String sn;
        if ("sn".equals(name)) {
            sn = reader.nextString();
            // "lifted" from https://github.com/Zendure/Zendure-HA/blob/master/custom_components/zendure_ha/device.py
            switch (sn.charAt(0)) {
                case 'A' -> {
                    if (sn.charAt(3) == '3') {
                        model = "AIO2400";
                        capacity = 2.4;
                    } else {
                        model = "AB1000";
                        capacity = 0.96;
                    }
                }
                case 'B' -> {
                    model = "AB1000S";
                    capacity = 0.96;
                }
                case 'C' -> {
                    model = "AB2000" + (sn.charAt(3) == 'F' ? 'S' : sn.charAt(3) == 'E' ? 'X' : "");
                    capacity = 1.92;
                }
                case 'F' -> {
                    model = "AB3000";
                    capacity = 2.88;
                }
                default -> throw new RuntimeException(
                        "Cant decode serialnumber pleas report error with SN and modelname and capacity: \"" + sn
                                + '\"');
            }
        } else {
            throw new RuntimeException("expected \"sn\" but found: \"" + name + '\"');
        }

        batteryPackCapacity += capacity;

        // "": 300,
        final int packType;
        name = reader.nextName();
        if ("packType".equals(name)) {
            packType = reader.nextInt();
        } else {
            throw new RuntimeException("expected \"packType\" but found: \"" + name + '\"');
        }

        // "": 20,
        name = reader.nextName();
        if ("socLevel".equals(name)) {
            reader.skipValue();
            // updateState(batterySocLevelUID), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"socLevel\" but found: \"" + name + '\"');
        }

        // "": 1,
        name = reader.nextName();
        if ("state".equals(name)) {
            reader.skipValue();
            // updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"state\" but found: \"" + name + '\"');
        }

        // "": 19,
        name = reader.nextName();
        if ("power".equals(name)) {
            reader.skipValue();
            // updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"power\" but found: \"" + name + '\"');
        }

        // "": 2801,
        name = reader.nextName();
        if ("maxTemp".equals(name)) {
            reader.skipValue();
            // updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"maxTemp\" but found: \"" + name + '\"');
        }

        // "": 4800,
        name = reader.nextName();
        if ("totalVol".equals(name)) {
            reader.skipValue();
            // updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"totalVol\" but found: \"" + name + '\"');
        }

        // "": 4,
        name = reader.nextName();
        if ("batcur".equals(name)) {
            reader.skipValue();
            // updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"batcur\" but found: \"" + name + '\"');
        }

        // "": 322,
        name = reader.nextName();
        if ("maxVol".equals(name)) {
            reader.skipValue();
            // updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"maxVol\" but found: \"" + name + '\"');
        }

        // "": 318,
        name = reader.nextName();
        if ("minVol".equals(name)) {
            reader.skipValue();
            // updateState(new ChannelUID(getThing().getUID(), Channels.), new DecimalType(reader.nextInt()));
        } else {
            throw new RuntimeException("expected \"minVol\" but found: \"" + name + '\"');
        }

        // TODO Config
        // "": 4117
        name = reader.nextName();
        if ("softVersion".equals(name)) {
            reader.skipValue();
        } else {
            throw new RuntimeException("expected \"softVersion\" but found: \"" + name + '\"');
        }

        switch (packType) {
            case 70 -> {
                name = reader.nextName();
                if ("heatState".equals(name)) {
                    reader.skipValue();
                } else {
                    throw new RuntimeException("expected \"heatState\" but found: \"" + name + '\"');
                }
            }
            case 240 -> {
                // LOGGER.log(Level.SEVERE, "{0}Battery[{1}] : Unknown packType {2} for sn {3} type {4}.", new
                // Object[]{config.serialNumber, index, packType, sn, model});
            }
            case 300 -> {
                // LOGGER.log(Level.SEVERE, "{0}Battery[{1}] : Unknown packType {2} for sn {3} type {4}.", new
                // Object[]{config.serialNumber, index, packType, sn, model});
            }
            default -> throw new RuntimeException("Unknown packType " + packType + " for sn " + sn);
        }
        if (reader.hasNext()) {
            throw new RuntimeException("Reader has more data! propertyname: " + reader.nextName());
        }
    }

    private void readSolarFlowProperties(JsonReader reader) throws IOException {
        Integer packInputPower = null;
        Integer outputPackPower = null;
        Integer acMode = null;
        Integer outputHomePower = null;
        Integer gridInputPower = null;
        Integer inputLimit = null;
        Integer outputLimit = null;
        do {
            final String name = reader.nextName();
            switch (name) {
                case "heatState" -> {
                    final int value = reader.nextInt();
                    switch (value) {
                        case 0 -> updateState(batteryPackHeatingUID, OnOffType.OFF);
                        case 1 -> updateState(batteryPackHeatingUID, OnOffType.ON);
                        default -> throw new RuntimeException(
                                "Can''t handle \"heatState\" from json unknown value : " + value);
                    }
                }
                // Power out of the battery pack (into inverter?)
                case "packInputPower" -> packInputPower = reader.nextInt();
                // Power into the battery pack (out of hte pv panels?)
                case "outputPackPower" -> outputPackPower = reader.nextInt();
                case "outputHomePower" -> outputHomePower = reader.nextInt();
                case "remainOutTime" ->
                    updateState(batteryPackRemaingDischargeTimeUID, new DecimalType(reader.nextInt()));
                // 0: Standby, 1: Charging, 2: Discharging
                case "packState" -> {
                    final int packState = reader.nextInt();
                    switch (packState) {
                        case BatteryPackState.VALUE_STANDBY ->
                            updateState(batteryPackPackStateUID, BatteryPackState.STANDBY);
                        case BatteryPackState.VALUE_CHARGING ->
                            updateState(batteryPackPackStateUID, BatteryPackState.CHARGING);
                        case BatteryPackState.VALUE_DISCHARGING ->
                            updateState(batteryPackPackStateUID, BatteryPackState.DISCHARGING);
                        default -> throw new RuntimeException("Unkown value for packState " + packState);
                    }
                }
                case "electricLevel" -> updateState(batteryPackStateOfChargeUID, new DecimalType(reader.nextInt()));
                case "gridInputPower" -> gridInputPower = reader.nextInt();
                case "solarInputPower" -> updateState(pvPowerUID, new DecimalType(reader.nextInt()));
                case "solarPower1" -> updateState(pvPowerPanel1UID, new DecimalType(reader.nextInt()));
                case "solarPower2" -> updateState(pvPowerPanel2UID, new DecimalType(reader.nextInt()));
                case "solarPower3" -> updateState(pvPowerPanel3UID, new DecimalType(reader.nextInt()));
                case "solarPower4" -> updateState(pvPowerPanel4UID, new DecimalType(reader.nextInt()));
                // Bypass 0: No, 1: Yes ?TODO APL we will see 2 sporadically ?
                case "pass" -> {
                    final int bypass = reader.nextInt();
                    switch (bypass) {
                        case BatteryPackBypass.VALUE_NO -> updateState(batteryPackBypassUID, BatteryPackBypass.NO);
                        case BatteryPackBypass.VALUE_YES -> updateState(batteryPackBypassUID, BatteryPackBypass.YES);
                        case BatteryPackBypass.VALUE_VALUE_2 ->
                            updateState(batteryPackBypassUID, BatteryPackBypass.VALUE_2);
                        default -> throw new RuntimeException("Unkown value for pass " + bypass);
                    }
                }
                // 0: No, 1: Reverse flow
                case "reverseState" -> {
                    final int reverseState = reader.nextInt();
                    switch (reverseState) {
                        case ReverseState.VALUE_NO -> updateState(inverterMainsReverseFlowUID, ReverseState.NO);
                        case ReverseState.VALUE_YES -> updateState(inverterMainsReverseFlowUID, ReverseState.YES);
                        default -> throw new RuntimeException("Unkown value for reverseState " + reverseState);
                    }
                }
                // 0: No, 1: Calibrating
                case "socStatus" -> {
                    final int socStatus = reader.nextInt();
                    switch (socStatus) {
                        case SocStatus.VALUE_NOT_CALIBRATING ->
                            updateState(batteryPackSocStateUID, SocStatus.NOT_CALIBRATING);
                        case SocStatus.VALUE_CALIBRATING -> updateState(batteryPackSocStateUID, SocStatus.CALIBRATING);
                        default -> throw new RuntimeException("Unkown value for socStatus " + socStatus);
                    }
                }
                case "hyperTmp" -> updateState(deviceTemperatureUID, new DecimalType(reader.nextInt() / 100.0));
                case "gridOffPower" -> updateState(inverterOffGridPowerUID, new DecimalType(reader.nextInt()));
                // 0: Stopped, 1: Battery input, 2: Battery output,
                case "dcStatus" -> {
                    final int dcStatus = reader.nextInt();
                    switch (dcStatus) {
                        case DcStatus.VALUE_STOPPED -> updateState(batteryPackDcStateUID, DcStatus.STOPPED);
                        case DcStatus.VALUE_BATTERY_INPUT -> updateState(batteryPackDcStateUID, DcStatus.BATTERY_INPUT);
                        case DcStatus.VALUE_BATTERY_OUTPUT ->
                            updateState(batteryPackDcStateUID, DcStatus.BATTERY_OUTPUT);
                        default -> throw new RuntimeException("Unkown value for dcStatus " + dcStatus);
                    }
                }
                case "pvStatus" -> {
                    final int pvState = reader.nextInt();
                    switch (pvState) {
                        case PVState.VALUE_STOPPED -> updateState(pvStateUID, PVState.STOPPED);
                        case PVState.VALUE_RUNNING -> updateState(pvStateUID, PVState.RUNNING);
                        default -> throw new RuntimeException("Unkown value for pvStatus " + pvState);
                    }
                }
                // 0: Stopped, 1: Grid-connected operation, 2: Charging operation
                case "acStatus" -> {
                    final int acState = reader.nextInt();
                    switch (acState) {
                        case AcStatus.VALUE_STOPPED -> updateState(inverterMainsAcStateUID, AcStatus.STOPPED);
                        case AcStatus.VALUE_GRID_CONNECTED ->
                            updateState(inverterMainsAcStateUID, AcStatus.GRID_CONNECTED);
                        case AcStatus.VALUE_CHARGING -> updateState(inverterMainsAcStateUID, AcStatus.CHARGING);
                        default -> throw new RuntimeException("Unkown value for acStatus " + acState);
                    }
                }
                // "": 1,
                case "dataReady" -> {
                    final int dataReady = reader.nextInt();
                    if (dataReady != 1) {
                        logger.info("{}: dataReady - expected 0, but was: {}", getThing().getUID(), dataReady);
                    }
                }
                // 0: Not connected, 1: Connected
                case "gridState" -> {
                    final int gridState = reader.nextInt();
                    switch (gridState) {
                        case GridState.VALUE_NOT_CONNECTED ->
                            updateState(inverterMainsGridStateUID, GridState.NOT_CONNECTED);
                        case GridState.VALUE_CONNECTED -> updateState(inverterMainsGridStateUID, GridState.CONNECTED);
                        default -> throw new RuntimeException("Unkown value for gridState " + gridState);
                    }
                }
                case "BatVolt" -> {
                    reader.skipValue();
                    // from HA "BatVolt": ("template", "{{ value / 100 if (value | int) < 32768 else (value |
                    // bitwise_xor(0x8000 | int) - 0x8000 | int) / 100 }}", "V", "voltage"),
                    // updateState(new ChannelUID(getThing().getUID(), Channels.BATTERY_PACK_VOLTAGE), new
                    // DecimalType(reader.nextInt() / 100.0));
                }
                // 0: Normal state, 1: Charge limit reached, 2: Discharge limit reached
                case "socLimit" -> {
                    final int socLimit = reader.nextInt();
                    switch (socLimit) {
                        case SocLimit.VALUE_NORMAL -> updateState(batteryPackSocStateUID, SocLimit.NORMAL);
                        case SocLimit.VALUE_CHARGE_LIMIT_REACHED ->
                            updateState(batteryPackSocStateUID, SocLimit.CHARGE_LIMIT_REACHED);
                        case SocLimit.VALUE_DISCHARGE_LIMIT_REACHED ->
                            updateState(batteryPackSocStateUID, SocLimit.DISCHARGE_LIMIT_REACHED);
                        default -> throw new RuntimeException("Unkown value for socLimit " + socLimit);
                    }
                }
                case "faultLevel" -> updateState(deviceFaultLevelUID, new DecimalType(reader.nextInt()));
                case "acCouplingState" -> updateState(acCouplingStateUID, new DecimalType(reader.nextInt()));
                case "offGridState" -> updateState(offGridStateUID, new DecimalType(reader.nextInt()));
                case "dryNodeState" -> updateState(dryNodeStateUID, new DecimalType(reader.nextInt()));
                case "writeRsp" -> {
                    final int writeRsp = reader.nextInt();
                    if (writeRsp != 0) {
                        logger.warn("{}: writeRsp != 0 : {}", getThing().getUID(), writeRsp);
                    }
                }
                // 0: 0 1: Input, 2: Output
                // ./Zendure-HA/custom_components/zendure_ha/device.py: self.acMode = ZendureSelect(self, "acMode", {1:
                // "input", 2: "output"}, self.entityWrite, 1)
                // ./Zendure-HA/custom_components/zendure_ha/device.py: await self.doCommand({"properties":
                // {"smartMode": 0 if power == 0 else 1, "acMode": 1, "inputLimit": -power}})
                // ./Zendure-HA/custom_components/zendure_ha/device.py: await self.doCommand({"properties":
                // {"smartMode": 0 if power == 0 else 1, "acMode": 2, "outputLimit": power}})
                // ./Zendure-HA/custom_components/zendure_ha/device.py: await self.doCommand({"properties":
                // {"smartMode": 0, "acMode": 2, "outputLimit": 0, "inputLimit": 0}})
                case "acMode" -> acMode = reader.nextInt();
                case "inputLimit" -> inputLimit = reader.nextInt();
                case "outputLimit" -> outputLimit = reader.nextInt();
                case "socSet" -> updateState(batteryPackDischargeLevelMaxUID, new DecimalType(reader.nextInt() / 10.0));
                case "minSoc" -> updateState(batteryPackDischargeLevelMinUID, new DecimalType(reader.nextInt() / 10.0));
                case "gridStandard" -> {
                    final int gridStandard = reader.nextInt();
                    switch (gridStandard) {
                        case GridStandard.VALUE_GERMANY ->
                            updateState(inverterMainsGridStandardUID, GridStandard.GERMANY);
                        case GridStandard.VALUE_FRANCE ->
                            updateState(inverterMainsGridStandardUID, GridStandard.FRANCE);
                        case GridStandard.VALUE_AUSTRIA ->
                            updateState(inverterMainsGridStandardUID, GridStandard.AUSTRIA);
                        case GridStandard.VALUE_SWIZERLAND ->
                            updateState(inverterMainsGridStandardUID, GridStandard.SWIZERLAND);
                        case GridStandard.VALUE_BELGIUM ->
                            updateState(inverterMainsGridStandardUID, GridStandard.BELGIUM);
                        default -> throw new RuntimeException("Unkown value for GridStandard " + gridStandard);
                    }
                }
                // 0: Disabled, 1: Allowed reverse flow, 2: Forbidden reverse flow
                case "gridReverse" -> {
                    final int gridReverse = reader.nextInt();
                    switch (gridReverse) {
                        case GridReverseFlowMode.VALUE_DISABLED ->
                            updateState(inverterMainsGridReverseFlowModeUID, GridReverseFlowMode.DISABLED);
                        case GridReverseFlowMode.VALUE_ALLOWED ->
                            updateState(inverterMainsGridReverseFlowModeUID, GridReverseFlowMode.ALLOWED);
                        case GridReverseFlowMode.VALUE_FORBIDDEN ->
                            updateState(inverterMainsGridReverseFlowModeUID, GridReverseFlowMode.FORBIDDEN);
                        default -> throw new RuntimeException("Unknown value for gridReverse: " + gridReverse);
                    }
                }
                case "inverseMaxPower" -> updateState(inverterMainsPowerLimitOutUID, new DecimalType(reader.nextInt()));
                case "lampSwitch" -> {
                    final int value = reader.nextInt();
                    switch (value) {
                        case 0 -> updateState(deviceLampSwitchUID, OnOffType.OFF);
                        case 1 -> updateState(deviceLampSwitchUID, OnOffType.ON);
                        default -> throw new RuntimeException(
                                "Can''t handle \"heatState\" from json unknown value : " + value);
                    }
                }
                case "gridOffMode" -> {
                    final int value = reader.nextInt();
                    switch (value) {
                        case GridOffMode.VALUE_NORMAL -> updateState(inverterOffGridModeUID, GridOffMode.NORMAL);
                        case GridOffMode.VALUE_ECO -> updateState(inverterOffGridModeUID, GridOffMode.ECO);
                        case GridOffMode.VALUE_OFF -> updateState(inverterOffGridModeUID, GridOffMode.OFF);
                        default -> throw new RuntimeException(
                                "Can''t handle \"gridOffMode\" from json unknown value : " + value);
                    }
                }
                // "": 2, APL: 2 -> MQTT?
                case "IOTState" -> {
                    final int IOTState = reader.nextInt();
                    if (IOTState != 2) {
                        logger.info("{} : IOTState - expected 0, but was: {}", getThing().getUID(), IOTState);
                    }
                }

                case "Fanmode" -> {
                    final int value = reader.nextInt();
                    switch (value) {
                        case 0 -> updateState(deviceFanModeUID, OnOffType.OFF);
                        case 1 -> updateState(deviceFanModeUID, OnOffType.ON);
                        default ->
                            throw new RuntimeException("Can't handle \"heatState\" from json unknown value : " + value);
                    }
                }
                case "Fanspeed" -> updateState(deviceFanSpeedUID, new DecimalType(reader.nextInt()));
                case "bindstate" -> {
                    final int bindstate = reader.nextInt();
                    if (bindstate != 0) {
                        logger.info("{}: bindState - expected 0, but was: {}", getThing().getUID(), bindstate);
                    }
                }
                case "factoryModeState" -> {
                    final int factoryModeState = reader.nextInt();
                    if (factoryModeState != 0) {
                        logger.info("{}: factoryModeState - expected 0, but was: {}", getThing().getUID(),
                                factoryModeState);
                    }
                }
                case "OTAState" -> {
                    final int OTAState = reader.nextInt();
                    if (OTAState != 0) {
                        logger.info("{}: OTAState - expected 0, but was: {}", getThing().getUID(), OTAState);
                    }
                }
                case "LCNState" -> {
                    final int LCNState = reader.nextInt();
                    if (LCNState != 0) {
                        logger.info("{} : LCNState - expected 0, but was: {}", getThing().getUID(), LCNState);
                    }
                }
                case "oldMode" -> {
                    final int oldMode = reader.nextInt();
                    if (oldMode != 0) {
                        logger.info("{}: oldMode - expected 0, but was: {}", getThing().getUID(), oldMode);
                    }
                }
                case "VoltWakeup" -> {
                    final int VoltWakeup = reader.nextInt();
                    if (VoltWakeup != 0) {
                        logger.info("{} : VoltWakeup - expected 0, but was: {}", getThing().getUID(), VoltWakeup);
                    }
                }
                case "ts" -> {
                    final int ts = reader.nextInt();
                }
                case "tsZone" -> {
                    final int tsZone = reader.nextInt();
                }
                case "chargeMaxLimit" ->
                    updateState(inverterMainsMaxPowerLimitInUID, new DecimalType(reader.nextInt()));
                case "smartMode" -> {
                    final int value = reader.nextInt();
                    switch (value) {
                        case 0 -> updateState(deviceSmartModeUID, OnOffType.OFF);
                        case 1 -> updateState(deviceSmartModeUID, OnOffType.ON);
                        default -> throw new RuntimeException(
                                "Can''t handle \"heatState\" from json unknown value : " + value);
                    }
                }
                case "phaseSwitch" -> updateState(inverterMainsPhaseSwitchUID, new DecimalType(reader.nextInt()));
                case "batCalTime" -> updateState(batteryPackCalTimeUID, new DecimalType(reader.nextInt()));
                case "packNum" -> {
                    packNum = reader.nextInt();
                    updateState(batteryPackNumberOfBatteriesUID, new DecimalType(packNum));
                }
                case "rssi" -> updateState(deviceRssiUID, new DecimalType(reader.nextInt()));
                case "is_error" -> {
                    final int is_error = reader.nextInt();
                    if (is_error == 0) {
                        updateState(deviceIsErrorUID, DecimalType.ZERO);
                    } else {
                        updateState(deviceIsErrorUID, new DecimalType(is_error));
                    }
                }
                default -> logger.warn("{}: Found new property: \"{}\": \"{}\"", getThing().getUID(), name,
                        reader.nextString());
            }
        } while (reader.peek() == JsonToken.NAME);

        Objects.requireNonNull(packInputPower, "packInputPower not set");
        Objects.requireNonNull(outputPackPower, "outputPackPower not set");
        if (packInputPower != 0) {
            if (outputPackPower == 0) {
                // Discharging power is positive
                updateState(batteryPackPowerUID, new DecimalType(packInputPower));
            } else {
                throw new RuntimeException("Error: \"packInputPower\" = " + packInputPower
                        + " and \"outputPackPower\" = " + outputPackPower);
            }
        } else {
            // Charging power is negative
            updateState(batteryPackPowerUID, new DecimalType(-outputPackPower));
        }

        Objects.requireNonNull(outputHomePower, "outputHomePower not set");
        Objects.requireNonNull(gridInputPower, "gridInputPower not set");
        if (outputHomePower != 0) {
            if (gridInputPower == 0) {
                // Discharging power or solar power is positive
                updateState(inverterMainsPowerUID, new DecimalType(outputHomePower));
            } else {
                throw new RuntimeException("Error: \"outputHomePower\" = " + outputHomePower
                        + " and \"gridInputPower\" = " + gridInputPower);
            }
        } else {
            // Charging power is negative
            updateState(inverterMainsPowerUID, new DecimalType(-gridInputPower));
        }

        Objects.requireNonNull(acMode, "acMode not set");
        Objects.requireNonNull(inputLimit, "inputLimit not set");
        Objects.requireNonNull(outputLimit, "outputLimit not set");
        switch (acMode) {
            case 0 -> updateState(inverterMainsPowerSetpointUID, DecimalType.ZERO);
            case 1 -> updateState(inverterMainsPowerSetpointUID, new DecimalType(-inputLimit));
            case 2 -> updateState(inverterMainsPowerSetpointUID, new DecimalType(outputLimit));
            case 255 -> {
                // TODO RESET of ACMOde -> Zendure BUG??
                logger.warn("{} : acMode == 255 !!!", getThing().getUID());
                handleCommand(inverterMainsPowerSetpointUID, DecimalType.ZERO);
                updateState(inverterMainsPowerSetpointUID, new DecimalType(0));
            }
            default -> throw new RuntimeException("Unknown value of acMode: " + acMode);
        }
    }

    public void writeJsonofCommand(ChannelUID channelUID, Command command, OutputStream os) throws IOException {
        try (final JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(os))) {
            jsonWriter.beginObject();
            jsonWriter.name("sn");
            jsonWriter.value(config.serialNumber);
            jsonWriter.name("properties");
            jsonWriter.beginObject();
            switch (channelUID.getGroupId()) {
                case Channels.Device.GROUP_DEVICE -> {
                    switch (channelUID.getIdWithoutGroup()) {
                        case Channels.Device.LAMP_SWITCH -> {
                            if (command instanceof DecimalType decimalType) {
                                jsonWriter.name("lampSwitch");
                                final int lampState = decimalType.intValue();
                                switch (lampState) {
                                    case 0 -> jsonWriter.value(0);
                                    case 1 -> jsonWriter.value(1);
                                    default -> throw new RuntimeException(
                                            "Unknow state for channel \"" + channelUID + "\" : " + lampState);
                                }
                            } else {
                                throw new RuntimeException("Unknow command for channel \"" + channelUID + "\" : "
                                        + command + "  " + command.getClass().getCanonicalName());
                            }
                        }
                    }
                }
                case Channels.InverterMains.GROUP_INVERTER_MAINS -> {
                    switch (channelUID.getIdWithoutGroup()) {
                        case Channels.InverterMains.GRID_REVERSE_FLOW_MODE -> {
                            if (command instanceof DecimalType decimalType) {
                                jsonWriter.name("gridReverse");
                                final int gridReverse = decimalType.intValue();
                                switch (gridReverse) {
                                    case 0 -> jsonWriter.value(0);
                                    case 1 -> jsonWriter.value(1);
                                    case 2 -> jsonWriter.value(2);
                                    default -> throw new RuntimeException(
                                            "Unknow state for channel \"" + channelUID + "\" : " + gridReverse);
                                }
                            } else {
                                throw new RuntimeException("Unknow command for channel \"" + channelUID + "\" : "
                                        + command + "  " + command.getClass().getCanonicalName());
                            }
                        }
                        case Channels.InverterMains.GRID_STANDARD -> {
                            if (command instanceof DecimalType decimalType) {
                                jsonWriter.name("gridStandard");
                                final int gridStandard = decimalType.intValue();
                                switch (gridStandard) {
                                    case 0 -> jsonWriter.value(0);
                                    case 1 -> jsonWriter.value(1);
                                    case 2 -> jsonWriter.value(2);
                                    case 3 -> jsonWriter.value(3);
                                    case 4 -> jsonWriter.value(4);
                                    default -> throw new RuntimeException(
                                            "Unknow state for channel \"" + channelUID + "\" : " + gridStandard);
                                }
                            } else {
                                throw new RuntimeException("Unknow command for channel \"" + channelUID + "\" : "
                                        + command + "  " + command.getClass().getCanonicalName());
                            }
                        }
                        case Channels.InverterMains.POWER_SETPOINT -> {
                            final int value = switch (command) {
                                case QuantityType quantityType -> quantityType.intValue();
                                case DecimalType decimalType -> decimalType.intValue();
                                default -> throw new RuntimeException("Unknow command for channel \"" + channelUID
                                        + "\" : " + command + "  " + command.getClass().getCanonicalName());
                            };

                            jsonWriter.name("smartMode");
                            jsonWriter.value(1);
                            jsonWriter.name("acMode");
                            if (value < 0) {
                                // Input to Inverter (Energyflow from inverter to battery pack)
                                // Zendure Zen Cloud sets this to max
                                // SolarFlow 800 max 1200W
                                // SolarFlow 800 Pro max 2000W
                                // but inverseMaxPower can only be set to max 800W in Germany???
                                // TODO possible race condition ??
                                jsonWriter.value(1);
                                jsonWriter.name("inputLimit");
                                jsonWriter.value(-value);
                            } else {
                                // value >= 0
                                // Output from Battery
                                // TODO possible race condition ??
                                jsonWriter.value(2);
                                jsonWriter.name("outputLimit");
                                jsonWriter.value(value);
                            }
                        }
                        case Channels.InverterMains.MAX_POWER_LIMIT_IN -> {
                            final int value = switch (command) {
                                case QuantityType quantityType -> quantityType.intValue();
                                case DecimalType decimalType -> decimalType.intValue();
                                default -> throw new RuntimeException("Unknow command for channel \"" + channelUID
                                        + "\" : " + command + "  " + command.getClass().getCanonicalName());
                            };
                            jsonWriter.name("chargeMaxLimit");
                            jsonWriter.value(value);
                        }
                        case Channels.InverterMains.MAX_POWER_LIMIT_OUT -> {
                            final int value = switch (command) {
                                case QuantityType quantityType -> quantityType.intValue();
                                case DecimalType decimalType -> decimalType.intValue();
                                default -> throw new RuntimeException("Unknow command for channel \"" + channelUID
                                        + "\" : " + command + "  " + command.getClass().getCanonicalName());
                            };
                            jsonWriter.name("inverseMaxPower");
                            jsonWriter.value(value);
                        }

                    }
                }
                case Channels.InverterOffGrid.GROUP_INVERTER_OFF_GRID -> {
                    switch (channelUID.getIdWithoutGroup()) {
                        case Channels.InverterOffGrid.MODE -> {
                            if (command instanceof DecimalType decimalType) {
                                jsonWriter.name("gridOffMode");
                                int gridOffMode = decimalType.intValue();
                                switch (gridOffMode) {
                                    case 0 -> jsonWriter.value(0);
                                    case 1 -> jsonWriter.value(1);
                                    case 2 -> jsonWriter.value(2);
                                    default -> throw new RuntimeException(
                                            "Unknow state for channel \"" + channelUID + "\" : " + gridOffMode);
                                }
                            } else {
                                throw new RuntimeException("Unknow command for channel \"" + channelUID + "\" : "
                                        + command + "  " + command.getClass().getCanonicalName());
                            }
                        }
                    }

                }
                case Channels.BatteryPack.GROUP_BATTERY_PACK -> {
                    switch (channelUID.getIdWithoutGroup()) {
                        case Channels.BatteryPack.DISCHARGE_LEVEL_MIN -> {
                            final float value = switch (command) {
                                case QuantityType quantityType -> quantityType.intValue();
                                case DecimalType decimalType -> decimalType.intValue();
                                default -> throw new RuntimeException("Unknow command for channel \"" + channelUID
                                        + "\" : " + command + "  " + command.getClass().getCanonicalName());
                            };
                            jsonWriter.name("minSoc");
                            jsonWriter.value(Math.round(value * 10));
                        }
                        case Channels.BatteryPack.CHARGE_LEVGEL_MAX -> {
                            final float value = switch (command) {
                                case QuantityType quantityType -> quantityType.intValue();
                                case DecimalType decimalType -> decimalType.intValue();
                                default -> throw new RuntimeException("Unknow command for channel \"" + channelUID
                                        + "\" : " + command + "  " + command.getClass().getCanonicalName());
                            };
                            jsonWriter.name("socSet");
                            jsonWriter.value(Math.round(value * 10));
                        }
                        default -> throw new RuntimeException("Unknow channel \"" + channelUID + "\" : " + command
                                + "  " + command.getClass().getCanonicalName());
                    }

                }
                default -> {
                    logger.warn("Can't handle Command handleCommand({}, {})", channelUID, command);
                }
            }
            jsonWriter.endObject();
            jsonWriter.endObject();
            jsonWriter.flush();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        synchronized (httpReadWriteLock) {
            logger.debug("BEGIN handleCommand( {}, {})", channelUID, command);
            // Ignore Refresh Request? or schedule one?
            if (command == RefreshType.REFRESH) {
                return;
            }
            try {
                URI uri = new URI("http", config.deviceIp, config.jsonWritePropertyPath, null);
                HttpURLConnection httpConnection = (HttpURLConnection) uri.toURL().openConnection();
                logger.trace("OPEND CONNECTION  handleCommand({}, {})", channelUID, command);
                try {
                    httpConnection.setRequestMethod("POST");
                    httpConnection.setRequestProperty("Content-Type", "application/json");
                    httpConnection.setDoOutput(true);

                    writeJsonofCommand(channelUID, command, httpConnection.getOutputStream());

                    if (logger.isTraceEnabled()) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
                        writeJsonofCommand(channelUID, command, baos);
                        logger.trace("{}: Send MSG: {}", getThing().getUID(), baos.toString());
                    }

                    if (httpConnection.getResponseCode() == 200) {
                        logger.debug("SUCCESS handleCommand({}, {})", channelUID, command);
                    } else {
                        logger.warn("ERROR handleCommand({}, {}) code: {}", channelUID, command,
                                httpConnection.getResponseCode());
                        throw new RuntimeException("Can't post data to device: " + httpConnection.getResponseCode()
                                + " " + httpConnection);
                    }
                } finally {
                    httpConnection.disconnect();
                }
            } catch (URISyntaxException | IOException ex) {
                logger.warn("Syntax exception in handleCommand", ex);
            }
        }
    }

    @Override
    public void dispose() {
        logger.info("Close Zendure handler for: {}", config.deviceIp);

        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        logger.info("Zendure handler closed for: {}", config.deviceIp);
    }
}
