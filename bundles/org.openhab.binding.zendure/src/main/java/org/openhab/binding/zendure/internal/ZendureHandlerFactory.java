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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.zendure.internal.ZendureBindingConstants.ThingType;
import org.openhab.binding.zendure.internal.handler.GenericZendureDeviceHandler;
import org.openhab.binding.zendure.internal.provider.ZendureTranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ZendureHandlerFactory} is responsible for creating things and
 * thing handlers. It is completely boiler-plate and nothing special at all.
 *
 * @author Arne Plöse - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding." + ZendureBindingConstants.BINDING_ID, service = ThingHandlerFactory.class)
public class ZendureHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(ZendureHandlerFactory.class);
    // TODO use??? private HttpClientFactory httpClientFactory;
    private ZendureTranslationProvider translationProvider;

    @Activate
    public ZendureHandlerFactory(/* @Reference HttpClientFactory httpClientFactory, */
            @Reference ZendureTranslationProvider translationProvider) {
        this.translationProvider = translationProvider;
        // TODO USE this??
        // this.httpClientFactory = httpClientFactory;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return ThingType.supportsThingType(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        final ThingType thingType = ThingType.find(thing.getThingTypeUID());
        logger.debug("Create Thing Handler for {}", thingType);
        if (thingType == null) {
            logger.warn("Zendure Handler -> thing type {} not found", thingType);
            return null;
        }
        switch (thingType) {
            case SOLAR_FLOW_800 -> {
                return new GenericZendureDeviceHandler((Bridge) thing, thingType);
            }
            case SOLAR_FLOW_800_PRO -> {
                return new GenericZendureDeviceHandler((Bridge) thing, thingType);
            }
            case SOLAR_FLOW_1600AC_PLUS -> {
                return new GenericZendureDeviceHandler((Bridge) thing, thingType);
            }
            default -> {
                logger.warn("Zendure Handler -> thing type {} not implemented", thingType);
                return null;
            }
        }
    }
}
