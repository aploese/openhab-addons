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
package org.openhab.binding.zendure.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ZendureBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Arne Plöse - Initial contribution
 */
@NonNullByDefault
public class ZendureBindingConstants {

    public static final String BINDING_ID = "zendure";

    public enum ThingType {

        SOLAR_FLOW_800("solarflow-800", true),
        SOLAR_FLOW_800_PRO("solarflow-800-pro", true),
        SOLAR_FLOW_1600AC_PLUS("solarflow-1600-ac-plus", true),
        BATTERY_PACK("battery-pack", false);

        public static @Nullable ThingType find(final ThingTypeUID thingTypeUID) {
            if (SOLAR_FLOW_800.thingTypeUID.equals(thingTypeUID)) {
                return SOLAR_FLOW_800;
            } else if (SOLAR_FLOW_800_PRO.thingTypeUID.equals(thingTypeUID)) {
                return SOLAR_FLOW_800_PRO;
            } else if (SOLAR_FLOW_1600AC_PLUS.thingTypeUID.equals(thingTypeUID)) {
                return SOLAR_FLOW_1600AC_PLUS;
            } else {
                return null;
            }
        }

        public static boolean supportsThingType(final ThingTypeUID thingTypeUID) {
            return find(thingTypeUID) != null;
        }

        public final ThingTypeUID thingTypeUID;
        public final boolean isBridge;

        private ThingType(String thingTypeId, boolean isBridge) {
            thingTypeUID = new ThingTypeUID(BINDING_ID, thingTypeId);
            this.isBridge = isBridge;
        }
    }

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES;

    static {
        HashSet<ThingTypeUID> types = new HashSet<>();
        for (ThingType thingTypes : ThingType.values()) {
            types.add(thingTypes.thingTypeUID);
        }
        SUPPORTED_THING_TYPES = Collections.unmodifiableSet(types);
    }
}

/*
 * http://192.168.1.202/properties/report
 * txt = ["Zendure=2.0.13"]
 * {"timestamp":1775577475,"messageId":196,"sn":"HED4NENCN500223","version":2,"product":"solarFlow1600AC+","properties":
 * {"heatState":0,"packInputPower":0,"outputPackPower":52,"outputHomePower":0,"remainOutTime":328,"packState":1,
 * "electricLevel":41,"gridInputPower":33,"solarInputPower":0,"solarPower1":0,"solarPower2":0,"solarPower3":0,
 * "solarPower4":0,"pass":0,"reverseState":0,"socStatus":0,"hyperTmp":3011,"gridOffPower":-19,"dcStatus":1,"pvStatus":0,
 * "acStatus":2,"dataReady":1,"gridState":1,"BatVolt":4946,"socLimit":0,"faultLevel":0,"acCouplingState":32771,
 * "offGridState":0,"dryNodeState":1,"writeRsp":0,"acMode":1,"inputLimit":31,"outputLimit":0,"socSet":1000,"minSoc":100,
 * "gridStandard":0,"gridReverse":1,"inverseMaxPower":1600,"lampSwitch":1,"gridOffMode":1,"IOTState":2,"Fanmode":1,
 * "Fanspeed":0,"bindstate":0,"factoryModeState":0,"OTAState":0,"LCNState":0,"oldMode":0,"VoltWakeup":0,"ts":1775577340,
 * "tsZone":14,"smartMode":0,"chargeMaxLimit":1600,"phaseSwitch":1,"batCalTime":0,"packNum":1,"rssi":-62,"is_error":0},
 * "packData":[{"sn":"CO4AENCN5000233","packType":300,"socLevel":41,"state":1,"power":9,"maxTemp":2991,"totalVol":4940,
 * "batcur":2,"maxVol":329,"minVol":329,"softVersion":4117}]}
 *
 * Ver Zendure=2.0.11 from avahi-browse -r _http._tcp
 * {"timestamp":1774876080,"messageId":24,"sn":"EOA1NHN3N234970","version":2,"product":"solarFlow800Pro","properties":{
 * "heatState":0,"packInputPower":430,"outputPackPower":0,"outputHomePower":801,"remainOutTime":82,"packState":2,
 * "electricLevel":19,"gridInputPower":0,"solarInputPower":371,"solarPower1":89,"solarPower2":102,"solarPower3":100,
 * "solarPower4":80,"pass":0,"reverseState":0,"socStatus":0,"hyperTmp":3091,"gridOffPower":0,"dcStatus":1,"pvStatus":0,
 * "acStatus":1,"dataReady":1,"gridState":1,"BatVolt":4860,"socLimit":0,"faultLevel":0,"writeRsp":0,"acMode":2,
 * "inputLimit":0,"outputLimit":800,"socSet":1000,"minSoc":100,"gridStandard":0,"gridReverse":1,"inverseMaxPower":800,
 * "lampSwitch":1,"gridOffMode":2,"IOTState":2,"Fanmode":1,"Fanspeed":0,"bindstate":1,"factoryModeState":0,"OTAState":0,
 * "LCNState":0,"oldMode":0,"VoltWakeup":0,"ts":1774874290,"tsZone":14,"smartMode":0,"chargeMaxLimit":1000,"phaseSwitch"
 * :1,"packNum":2,"rssi":-72,"is_error":0},"packData":[{"sn":"CO4EHNCDN234434","packType":300,"socLevel":19,"state":2,
 * "power":296,"maxTemp":2871,"totalVol":4860,"batcur":65475,"maxVol":325,"minVol":323,"softVersion":4123},{"sn":
 * "CO4EHNA9N236231","packType":300,"socLevel":18,"state":2,"power":213,"maxTemp":2831,"totalVol":4850,"batcur":65492,
 * "maxVol":324,"minVol":322,"softVersion":4123}]}
 *
 * Off-Grid Default
 * {"timestamp":1762874855,"messageId":141,"sn":"EOA1NHN3N234970","version":2,"product":"solarFlow800Pro","properties":{
 * "heatState":0,"packInputPower":145,"outputPackPower":0,"outputHomePower":163,"remainOutTime":703,"packState":2,
 * "electricLevel":42,"gridInputPower":0,"solarInputPower":18,"solarPower1":10,"solarPower2":0,"solarPower3":8,
 * "solarPower4":0,"pass":0,"reverseState":0,"socStatus":0,"hyperTmp":3071,"gridOffPower":0,"dcStatus":1,"pvStatus":0,
 * "acStatus":1,"dataReady":1,"gridState":1,"BatVolt":4864,"socLimit":0,"writeRsp":0,"acMode":2,"inputLimit":23,
 * "outputLimit":164,"socSet":960,"minSoc":200,"gridStandard":0,"gridReverse":1,"inverseMaxPower":800,"lampSwitch":0,
 * "gridOffMode":2,"IOTState":2,"Fanmode":1,"Fanspeed":0,"bindstate":0,"factoryModeState":0,"OTAState":0,"LCNState":0,
 * "oldMode":0,"VoltWakeup":0,"ts":1762874449,"tsZone":13,"smartMode":1,"chargeMaxLimit":1000,"packNum":2,"rssi":-60,
 * "is_error":0},"packData":[{"sn":"CO4EHNCDN234434","packType":300,"socLevel":42,"state":2,"power":77,"maxTemp":2961,
 * "totalVol":4860,"batcur":65520,"maxVol":324,"minVol":323,"softVersion":4117},{"sn":"CO4EHNA9N232163","packType":300,
 * "socLevel":42,"state":2,"power":68,"maxTemp":2911,"totalVol":4860,"batcur":65522,"maxVol":324,"minVol":323,
 * "softVersion":4117}]}
 * SmartMode (Eco?)
 * {"timestamp":1762874899,"messageId":142,"sn":"EOA1NHN3N234970","version":2,"product":"solarFlow800Pro","properties":{
 * "heatState":0,"packInputPower":134,"outputPackPower":0,"outputHomePower":152,"remainOutTime":755,"packState":2,
 * "electricLevel":42,"gridInputPower":0,"solarInputPower":18,"solarPower1":10,"solarPower2":0,"solarPower3":8,
 * "solarPower4":0,"pass":0,"reverseState":0,"socStatus":0,"hyperTmp":3061,"gridOffPower":0,"dcStatus":1,"pvStatus":0,
 * "acStatus":1,"dataReady":1,"gridState":1,"BatVolt":4863,"socLimit":0,"writeRsp":0,"acMode":2,"inputLimit":23,
 * "outputLimit":153,"socSet":960,"minSoc":200,"gridStandard":0,"gridReverse":1,"inverseMaxPower":800,"lampSwitch":0,
 * "gridOffMode":1,"IOTState":2,"Fanmode":1,"Fanspeed":0,"bindstate":0,"factoryModeState":0,"OTAState":0,"LCNState":0,
 * "oldMode":0,"VoltWakeup":0,"ts":1762874449,"tsZone":13,"smartMode":1,"chargeMaxLimit":1000,"packNum":2,"rssi":-60,
 * "is_error":0},"packData":[{"sn":"CO4EHNCDN234434","packType":300,"socLevel":42,"state":2,"power":72,"maxTemp":2961,
 * "totalVol":4860,"batcur":65521,"maxVol":324,"minVol":323,"softVersion":4117},{"sn":"CO4EHNA9N232163","packType":300,
 * "socLevel":42,"state":2,"power":68,"maxTemp":2911,"totalVol":4860,"batcur":65522,"maxVol":325,"minVol":324,
 * "softVersion":4117}]}
 * SmartMode (Normal?)
 * {"timestamp":1762874970,"messageId":143,"sn":"EOA1NHN3N234970","version":2,"product":"solarFlow800Pro","properties":{
 * "heatState":0,"packInputPower":139,"outputPackPower":0,"outputHomePower":155,"remainOutTime":658,"packState":2,
 * "electricLevel":42,"gridInputPower":0,"solarInputPower":16,"solarPower1":10,"solarPower2":0,"solarPower3":6,
 * "solarPower4":0,"pass":0,"reverseState":0,"socStatus":0,"hyperTmp":3061,"gridOffPower":0,"dcStatus":1,"pvStatus":0,
 * "acStatus":1,"dataReady":1,"gridState":1,"BatVolt":4861,"socLimit":0,"writeRsp":0,"acMode":2,"inputLimit":23,
 * "outputLimit":156,"socSet":960,"minSoc":200,"gridStandard":0,"gridReverse":1,"inverseMaxPower":800,"lampSwitch":0,
 * "gridOffMode":0,"IOTState":2,"Fanmode":1,"Fanspeed":0,"bindstate":0,"factoryModeState":0,"OTAState":0,"LCNState":0,
 * "oldMode":0,"VoltWakeup":0,"ts":1762874449,"tsZone":13,"smartMode":1,"chargeMaxLimit":1000,"packNum":2,"rssi":-60,
 * "is_error":0},"packData":[{"sn":"CO4EHNCDN234434","packType":300,"socLevel":42,"state":2,"power":77,"maxTemp":2961,
 * "totalVol":4850,"batcur":65520,"maxVol":324,"minVol":323,"softVersion":4117},{"sn":"CO4EHNA9N232163","packType":300,
 * "socLevel":42,"state":2,"power":72,"maxTemp":2911,"totalVol":4860,"batcur":65521,"maxVol":325,"minVol":323,
 * "softVersion":4117}]}
 * Off-Grid Default
 * {"timestamp":1762874999,"messageId":144,"sn":"EOA1NHN3N234970","version":2,"product":"solarFlow800Pro","properties":{
 * "heatState":0,"packInputPower":140,"outputPackPower":0,"outputHomePower":158,"remainOutTime":728,"packState":2,
 * "electricLevel":42,"gridInputPower":0,"solarInputPower":18,"solarPower1":10,"solarPower2":0,"solarPower3":8,
 * "solarPower4":0,"pass":0,"reverseState":0,"socStatus":0,"hyperTmp":3061,"gridOffPower":0,"dcStatus":1,"pvStatus":0,
 * "acStatus":1,"dataReady":1,"gridState":1,"BatVolt":4861,"socLimit":0,"writeRsp":0,"acMode":2,"inputLimit":23,
 * "outputLimit":158,"socSet":960,"minSoc":200,"gridStandard":0,"gridReverse":1,"inverseMaxPower":800,"lampSwitch":0,
 * "gridOffMode":2,"IOTState":2,"Fanmode":1,"Fanspeed":0,"bindstate":0,"factoryModeState":0,"OTAState":0,"LCNState":0,
 * "oldMode":0,"VoltWakeup":0,"ts":1762874449,"tsZone":13,"smartMode":1,"chargeMaxLimit":1000,"packNum":2,"rssi":-60,
 * "is_error":0},"packData":[{"sn":"CO4EHNCDN234434","packType":300,"socLevel":42,"state":2,"power":63,"maxTemp":2961,
 * "totalVol":4850,"batcur":65523,"maxVol":324,"minVol":323,"softVersion":4117},{"sn":"CO4EHNA9N232163","packType":300,
 * "socLevel":42,"state":2,"power":63,"maxTemp":2911,"totalVol":4860,"batcur":65523,"maxVol":324,"minVol":323,
 * "softVersion":4117}]}
 *
 * OnGrid setting
 * {"timestamp":1762875284,"messageId":4,"sn":"WOB1NTMCN030166","version":2,"product":"solarFlow800","properties":{
 * "heatState":0,"packInputPower":0,"outputPackPower":8,"outputHomePower":0,"remainOutTime":59940,"packState":0,
 * "electricLevel":21,"gridInputPower":0,"solarInputPower":8,"solarPower1":4,"solarPower2":4,"pass":0,"reverseState":0,
 * "socStatus":0,"hyperTmp":2911,"dcStatus":0,"pvStatus":1,"acStatus":0,"dataReady":1,"gridState":1,"BatVolt":4954,
 * "socLimit":2,"writeRsp":0,"acMode":1,"inputLimit":0,"outputLimit":0,"socSet":960,"minSoc":200,"gridStandard":0,
 * "gridReverse":1,"inverseMaxPower":800,"lampSwitch":1,"IOTState":2,"factoryModeState":0,"OTAState":0,"LCNState":0,
 * "oldMode":0,"VoltWakeup":0,"ts":1762875280,"bindstate":0,"tsZone":13,"chargeMaxLimit":800,"smartMode":0,"packNum":1,
 * "rssi":-73,"is_error":0},"packData":[{"sn":"CO4EHNA9N234526","packType":70,"socLevel":21,"state":0,"power":0,
 * "maxTemp":2881,"totalVol":4940,"batcur":0,"maxVol":330,"minVol":329,"softVersion":4117,"heatState":0}]}
 * {"timestamp":1762875319,"messageId":5,"sn":"WOB1NTMCN030166","version":2,"product":"solarFlow800","properties":{
 * "heatState":0,"packInputPower":0,"outputPackPower":8,"outputHomePower":0,"remainOutTime":59940,"packState":0,
 * "electricLevel":21,"gridInputPower":0,"solarInputPower":8,"solarPower1":4,"solarPower2":4,"pass":0,"reverseState":0,
 * "socStatus":0,"hyperTmp":2911,"dcStatus":0,"pvStatus":1,"acStatus":0,"dataReady":1,"gridState":1,"BatVolt":4955,
 * "socLimit":2,"writeRsp":0,"acMode":2,"inputLimit":0,"outputLimit":0,"socSet":960,"minSoc":200,"gridStandard":0,
 * "gridReverse":1,"inverseMaxPower":800,"lampSwitch":1,"IOTState":2,"factoryModeState":0,"OTAState":0,"LCNState":0,
 * "oldMode":0,"VoltWakeup":0,"ts":1762875315,"bindstate":0,"tsZone":13,"chargeMaxLimit":800,"smartMode":0,"packNum":1,
 * "rssi":-77,"is_error":0},"packData":[{"sn":"CO4EHNA9N234526","packType":70,"socLevel":21,"state":0,"power":0,
 * "maxTemp":2881,"totalVol":4940,"batcur":0,"maxVol":330,"minVol":329,"softVersion":4117,"heatState":0}]}
 *
 * PV läd Akku + Netz
 * {"timestamp":1762866944,"messageId":140,"sn":"EOA1NHN3N234970","version":2,"product":"solarFlow800Pro","properties":{
 * "heatState":0,"packInputPower":0
 * ,"outputPackPower":106,"outputHomePower":143,"remainOutTime":1602,"packState":1,"electricLevel":46,"gridInputPower":0
 * ,"solarInputPower":249,"solarPower1":81,"solarPower2":76,"solarPower3":92,"solarPower4":0,"pass":0,"reverseState":0,
 * "socStatus":0,"hyperTmp":3091,"gridOffPower":0,"dcStatus":1,"pvStatus":0,"acStatus":1,"dataReady":1,"gridState":1,
 * "BatVolt":4955,"socLimit":0,"writeRsp":0,"acMode":2,"inputLimit":13,"outputLimit":143,"socSet":960,"minSoc":200,
 * "gridStandard":0,"gridReverse":1,"inverseMaxPower":800,"lampSwitch":0,"gridOffMode":2,"IOTState":2,"Fanmode":1,
 * "Fanspeed":0,"bindstate":0,"factoryModeState":0,"OTAState":0,"LCNState":0,"oldMode":0,"VoltWakeup":0,"ts":1762865448,
 * "tsZone":13,"smartMode":1,"chargeMaxLimit":1000,"packNum":2,"rssi":-59,"is_error":0},"packData":[{"sn":
 * "CO4EHNCDN234434","packType":300,"socLevel":47,"state":1,"power":49,"maxTemp":2931,"totalVol":4940,"batcur":10,
 * "maxVol":330,"minVol":329,"softVersion":4117},
 * Akku entläd ins Netz + PV
 * {"timestamp":1762867111,"messageId":3 ,"sn":"WOB1NTMCN030166","version":2,"product":"solarFlow800"
 * ,"properties":{"heatState":0,"packInputPower":12,"outputPackPower":0 ,"outputHomePower":103,"remainOutTime":645
 * ,"packState":2,"electricLevel":21,"gridInputPower":0,"solarInputPower":91,"solarPower1":46,"solarPower2":45,"pass":0,
 * "reverseState":0,"socStatus":0,"hyperTmp":3031,"dcStatus":2,"pvStatus":1,"acStatus":1,"dataReady":1,"gridState":1,
 * "BatVolt":4925,"socLimit":0,"writeRsp":0,"acMode":2,"inputLimit":13,"outputLimit":103,"socSet":960,"minSoc":200,
 * "gridStandard":0,"gridReverse":1,"inverseMaxPower":800,"lampSwitch":1,"IOTState":2,"factoryModeState":0,"OTAState":0,
 * "LCNState":0,"oldMode":0,"VoltWakeup":0,"ts":1762867108,"bindstate":0,"tsZone":13,"chargeMaxLimit":800,"smartMode":1,
 * "packNum":1,"rssi":-75,"is_error":0},"packData":[{"sn":"CO4EHNA9N234526","packType":70,"socLevel":21,"state":2,
 * "power":49,"maxTemp":2901,"totalVol":4910,"batcur":65526,"maxVol":328,"minVol":327,"softVersion":4117,"heatState":0}]
 * }
 *
 * {"sn":"CO4EHNA9N232163","packType":300,"socLevel":46,"state":1,"power":49,"maxTemp":2881,"totalVol":4950,"batcur":10,
 * "maxVol":330,"minVol":329,"softVersion":4117}]}
 * {"timestamp":1762768481,"messageId":1
 * ,"sn":"EOA1NHN3N234970","version":2,"product":"solarFlow800Pro","properties":{"heatState":0,"packInputPower":0,
 * "outputPackPower":94,"outputHomePower":0,"remainOutTime":474,"packState":1,"electricLevel":21,"gridInputPower":0,
 * "solarInputPower":94,"solarPower1":35,"solarPower2":23,"solarPower3":36,"solarPower4":0,"pass":0,"reverseState":0,
 * "socStatus":0,"hyperTmp":2901,"gridOffPower":0,"dcStatus":1,"pvStatus":0,"acStatus":0,"dataReady":1,"gridState":1,
 * "BatVolt":4717,"socLimit":2,"writeRsp":0,"acMode":2,"inputLimit":0,"outputLimit":0,"socSet":960,"minSoc":200,
 * "gridStandard":0,"gridReverse":1,"inverseMaxPower":800,"lampSwitch":0,"gridOffMode":2,"IOTState":2,"Fanmode":1,
 * "Fanspeed":0,"bindstate":0,"factoryModeState":0,"OTAState":0,"LCNState":0,"oldMode":0,"VoltWakeup":0,"ts":1762768248
 * ,"tsZone":13
 * ,"smartMode":0,"chargeMaxLimit":1000,"packNum":2,"rssi":-60,"is_error":0},"packData":[{"sn":"CO4EHNCDN234434",
 * "packType":300,"socLevel":21,"state":1,"power":47,"maxTemp":2821,"totalVol":4700,"batcur":10,"maxVol":316,"minVol":
 * 312,"softVersion":4117},
 * {"sn":"CO4EHNA9N232163","packType":300,"socLevel":20,"state":1,"power":47,"maxTemp":2811,"totalVol":4710,"batcur":10,
 * "maxVol":315,"minVol":310,"softVersion":4117}]}
 * {"timestamp":1762768513,"messageId":2 ,"sn":"WOB1NTMCN030166","version":2,"product":"solarFlow800"
 * ,"properties":{"heatState":0,"packInputPower":0,"outputPackPower":65,"outputHomePower":0,"remainOutTime":78
 * ,"packState":1,"electricLevel":21,"gridInputPower":0,"solarInputPower":65,"solarPower1":31,"solarPower2":34
 * ,"pass":0,"reverseState":0,"socStatus":0,"hyperTmp":2871
 * ,"dcStatus":1,"pvStatus":1,"acStatus":0,"dataReady":1,"gridState":1,"BatVolt":5027,"socLimit":2,"writeRsp":0,"acMode"
 * :2,"inputLimit":0,"outputLimit":0,"socSet":960,"minSoc":200,"gridStandard":0,"gridReverse":1,"inverseMaxPower":800,
 * "lampSwitch":1 ,"IOTState":2
 * ,"factoryModeState":0,"OTAState":0,"LCNState":0,"oldMode":0,"VoltWakeup":0,"ts":1762768508,"bindstate":0,"tsZone":13,
 * "chargeMaxLimit":800,"smartMode":0
 * ,"packNum":1,"rssi":-73,"is_error":0},"packData":[{"sn":"CO4EHNA9N234526","packType":70
 * ,"socLevel":21,"state":1,"power":50,"maxTemp":2831,"totalVol":5010,"batcur":10,"maxVol":334,"minVol":333,
 * "softVersion":4117,"heatState":0}]}
 *
 */
/*
 * Solar Flow 800 PRO
 * http://solarflow-800-pro/properties/report
 *
 * {
 * "timestamp":1762530723,
 * "messageId":5,
 * "sn":"EOA1NHN3N234970",
 * "version":2,
 * "product":"solarFlow800Pro",
 * "properties":
 * {
 * "heatState":0,
 * "packInputPower":0,
 * "outputPackPower":0,
 * "outputHomePower":0,
 * "remainOutTime":59940,
 * "packState":1,
 * "electricLevel":22,
 * "gridInputPower":0,
 * "solarInputPower":0,
 * "solarPower1":0,
 * "solarPower2":0,
 * "solarPower3":0,
 * "solarPower4":0,
 * "pass":0,
 * "reverseState":0,
 * "socStatus":0,
 * "hyperTmp":2841,
 * "gridOffPower":0,
 * "dcStatus":1,
 * "pvStatus":0,
 * "acStatus":0,
 * "dataReady":1,
 * "gridState":1,
 * "BatVolt":4919,
 * "socLimit":0,
 * "writeRsp":0,
 * "acMode":2,
 * "inputLimit":0,
 * "outputLimit":0,
 * "socSet":960,
 * "minSoc":200,
 * "gridStandard":0,
 * "gridReverse":1,
 * "inverseMaxPower":800,
 * "lampSwitch":0,
 * "gridOffMode":2,
 * "IOTState":2,
 * "Fanmode":1,
 * "Fanspeed":0,
 * "bindstate":0,
 * "factoryModeState":0,
 * "OTAState":0,
 * "LCNState":0,
 * "oldMode":0,
 * "VoltWakeup":0,
 * "ts":1762529488,
 * "tsZone":13,
 * "smartMode":0,
 * "chargeMaxLimit":1000,
 * "packNum":2,
 * "rssi":-60,
 * "is_error":0
 * },
 * "packData":
 * [
 * {
 * "sn":"CO4EHNCDN234434",
 * "packType":300,
 * "socLevel":22,
 * "state":1,
 * "power":9,
 * "maxTemp":2841,
 * "totalVol":4910,
 * "batcur":2,
 * "maxVol":328,
 * "minVol":327,
 * "softVersion":4117
 * },
 * {
 * "sn":"CO4EHNA9N232163",
 * "packType":300,
 * "socLevel":22,
 * "state":1,
 * "power":9,
 * "maxTemp":2821,
 * "totalVol":4910,
 * "batcur":2,
 * "maxVol":328,
 * "minVol":326,
 * "softVersion":4117
 * }
 * ]
 * }
 *
 * Solar Flow 800
 * http://solarflow-800/properties/report
 * {
 * "timestamp":1762531064,
 * "messageId":2,
 * "sn":"WOB1NTMCN030166",
 * "version":2,
 * "product":"solarFlow800",
 * "properties":
 * {
 * "heatState":0,
 * "packInputPower":0,
 * "outputPackPower":0,
 * "outputHomePower":0,
 * "remainOutTime":59940,
 * "packState":0,
 * "electricLevel":20,
 * "gridInputPower":0,
 * "solarInputPower":0,
 * "solarPower1":0,
 * "solarPower2":0,
 * "pass":0,
 * "reverseState":0,
 * "socStatus":0,
 * "hyperTmp":2861,
 * "dcStatus":0,
 * "pvStatus":1,
 * "acStatus":0,
 * "dataReady":1,
 * "gridState":1,
 * "BatVolt":4959,
 * "socLimit":2,
 * "writeRsp":0,
 * "acMode":2,
 * "inputLimit":284,
 * "outputLimit":0,
 * "socSet":960,
 * "minSoc":200,
 * "gridStandard":0,
 * "gridReverse":1,
 * "inverseMaxPower":800,
 * "lampSwitch":1,
 * "IOTState":2,
 * "factoryModeState":0,
 * "OTAState":0,
 * "LCNState":0,
 * "oldMode":0,
 * "VoltWakeup":0,
 * "ts":1762531060,
 * "bindstate":0,
 * "tsZone":13,
 * "chargeMaxLimit":800,
 * "smartMode":0,
 * "packNum":1,
 * "rssi":-75,
 * "is_error":0
 * },
 * "packData":
 * [
 * {
 * "sn":"CO4EHNA9N234526",
 * "packType":70,
 * "socLevel":20,
 * "state":0,
 * "power":0,
 * "maxTemp":2831,
 * "totalVol":4940,
 * "batcur":0,
 * "maxVol":330,
 * "minVol":329,
 * "softVersion":4117,
 * "heatState":0
 * }
 * ]
 * }
 */
