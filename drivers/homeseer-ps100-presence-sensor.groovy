/**
 *  HomeSeer PS100 mmWave Presence Sensor
 *  Hubitat Elevation driver
 *
 *  A 24 GHz mmWave radar presence sensor (USB-C powered, Z-Wave Plus, always-on).
 *  Reports presence/motion via the Z-Wave Notification (Home Security) and/or
 *  Sensor Binary command classes and exposes the device's configuration
 *  parameters (per-range sensitivity, no-motion timeout, LED behavior/colors)
 *  as driver preferences.
 *
 *  Supports both HomeSeer PS100 hardware revisions:
 *    - PS100 (original, "HS-PS100"): mfr 0x000C, productType 0x0204, productId 0x0002
 *    - PS100 v2:                     mfr 0x000C, productType 0x0204, productId 0x0003
 *  Parameters that only exist on one revision are shown only when that unit is
 *  detected (after the first Configure/Refresh reads the product ID).
 *
 *  Device documentation: https://docs.homeseer.com/products/ps100
 *  Parameter reference:   https://devices.zwave-js.io/?jumpTo=0x000c:0x0204:0x0002
 *
 *  Copyright 2026 Zac Shenker
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

import groovy.transform.Field

@Field static final String DRIVER_VERSION = "1.1.0"

@Field static final String PID_V1 = "0002"   // original PS100 (HS-PS100)
@Field static final String PID_V2 = "0003"   // PS100 v2

/*
 * Z-Wave configuration parameters for the PS100.
 * Sources: HomeSeer spec (https://docs.homeseer.com/products/ps100-specs-z-wave-parameters)
 *          Z-Wave JS device DB (https://devices.zwave-js.io/?jumpTo=0x000c:0x0204:0x0002)
 *
 * Each entry drives both a matching preference input and the configure() routine.
 * The preference setting name is "param<number>" (e.g. settings.param1).
 *
 *   appliesTo       : optional list of product IDs this param exists on (absent = both)
 *   defaultValue    : factory default (when identical across revisions)
 *   defaultByProduct: factory default keyed by product ID (when revisions differ)
 */
@Field static final Map<Integer, Map> CONFIG_PARAMS = [
    1 : [
        title      : "No-motion timeout (seconds)",
        description: "Time with no motion before presence clears. Range 10-3600. Default 10.",
        type       : "number",
        size       : 4,
        defaultValue: 10,
        range      : "10..3600"
    ],
    2 : [
        title      : "Distance report interval (seconds)",
        description: "Original PS100 only. How often the sensor reports measured distance. Range 2-600. Default 10.",
        type       : "number",
        size       : 4,
        defaultValue: 10,
        range      : "2..600",
        appliesTo  : [PID_V1]
    ],
    3 : [
        title      : "Bluetooth radio",
        description: "Only used by the HomeSeer mobile app to read the mmWave sensor. Power-cycle the unit after changing. Default Disabled.",
        type       : "enum",
        size       : 1,
        defaultValue: 0,
        options    : [0: "Disabled", 1: "Enabled"]
    ],
    4 : [
        title      : "Motion LED",
        description: "Flash the front LED red on motion and green on timeout. Default Enabled.",
        type       : "enum",
        size       : 1,
        defaultValue: 1,
        options    : [0: "Disabled", 1: "Enabled"]
    ],
    6 : [
        title      : "Sensitivity: 0-75 cm (0-2.4 ft)",
        description: "0 = range disabled, 1-100 = sensitivity (higher = more sensitive). Default 50.",
        type       : "number", size: 1, defaultValue: 50, range: "0..100"
    ],
    7 : [
        title      : "Sensitivity: 75-150 cm (2.4-4.9 ft)",
        description: "0 = range disabled, 1-100 = sensitivity. Default 50.",
        type       : "number", size: 1, defaultValue: 50, range: "0..100"
    ],
    8 : [
        title      : "Sensitivity: 150-225 cm (4.9-7.3 ft)",
        description: "0 = range disabled, 1-100 = sensitivity. Default: 50 (original) / 0 (v2).",
        type       : "number", size: 1, defaultByProduct: [(PID_V1): 50, (PID_V2): 0], range: "0..100"
    ],
    9 : [
        title      : "Sensitivity: 225-300 cm (7.3-9.8 ft)",
        description: "0 = range disabled, 1-100 = sensitivity. Default: 50 (original) / 0 (v2).",
        type       : "number", size: 1, defaultByProduct: [(PID_V1): 50, (PID_V2): 0], range: "0..100"
    ],
    10: [
        title      : "Sensitivity: 300-375 cm (9.8-12.3 ft)",
        description: "0 = range disabled, 1-100 = sensitivity. Default: 90 (original) / 0 (v2).",
        type       : "number", size: 1, defaultByProduct: [(PID_V1): 90, (PID_V2): 0], range: "0..100"
    ],
    11: [
        title      : "Sensitivity: 375-450 cm (12.3-14.7 ft)",
        description: "0 = range disabled, 1-100 = sensitivity. Default: 90 (original) / 0 (v2).",
        type       : "number", size: 1, defaultByProduct: [(PID_V1): 90, (PID_V2): 0], range: "0..100"
    ],
    12: [
        title      : "Sensitivity: 450-525 cm (14.7-17.2 ft)",
        description: "0 = range disabled, 1-100 = sensitivity. Default: 90 (original) / 0 (v2).",
        type       : "number", size: 1, defaultByProduct: [(PID_V1): 90, (PID_V2): 0], range: "0..100"
    ],
    13: [
        title      : "Sensitivity: 525-600 cm (17.2-19.6 ft)",
        description: "0 = range disabled, 1-100 = sensitivity. Default: 90 (original) / 0 (v2).",
        type       : "number", size: 1, defaultByProduct: [(PID_V1): 90, (PID_V2): 0], range: "0..100"
    ],
    20: [
        title      : "Left LED color",
        description: "PS100 v2 only. Steady color for the left front LED. Default Off.",
        type       : "enum", size: 1, defaultValue: 0, appliesTo: [PID_V2],
        options    : [0: "Off", 1: "Red", 2: "Green", 3: "Blue", 4: "Magenta", 5: "Yellow", 6: "Cyan", 7: "White"]
    ],
    21: [
        title      : "Right LED color",
        description: "PS100 v2 only. Steady color for the right front LED. Default Off.",
        type       : "enum", size: 1, defaultValue: 0, appliesTo: [PID_V2],
        options    : [0: "Off", 1: "Red", 2: "Green", 3: "Blue", 4: "Magenta", 5: "Yellow", 6: "Cyan", 7: "White"]
    ]
]

metadata {
    definition(
        name     : "HomeSeer PS100 mmWave Presence Sensor",
        namespace: "zshenker",
        author   : "Zac Shenker",
        importUrl: "https://raw.githubusercontent.com/zshenker/hubitat-homeseer-ps100/main/drivers/homeseer-ps100-presence-sensor.groovy"
    ) {
        capability "PresenceSensor"
        capability "MotionSensor"
        capability "Sensor"
        capability "Configuration"
        capability "Refresh"

        // HomeSeer = mfr 000C. Two hardware revisions share productType 0204.
        fingerprint mfr: "000C", prod: "0204", deviceId: "0002",
            deviceJoinName: "HomeSeer PS100 mmWave Presence Sensor"
        fingerprint mfr: "000C", prod: "0204", deviceId: "0003",
            deviceJoinName: "HomeSeer PS100 mmWave Presence Sensor (v2)"
    }

    preferences {
        String pid = getDataValue("productId")
        CONFIG_PARAMS.each { num, p ->
            if (!paramApplies(p, pid)) return
            def dflt = paramDefault(p, pid)
            if (p.type == "enum") {
                input name: "param${num}", type: "enum", title: "<b>[${num}] ${p.title}</b>",
                    description: p.description, options: p.options,
                    defaultValue: dflt, required: false
            } else {
                input name: "param${num}", type: "number", title: "<b>[${num}] ${p.title}</b>",
                    description: p.description, defaultValue: dflt,
                    range: p.range, required: false
            }
        }
        input name: "logEnable", type: "bool", title: "<b>Enable debug logging</b>",
            description: "Automatically disables after 30 minutes.", defaultValue: true
        input name: "txtEnable", type: "bool", title: "<b>Enable descriptive (info) logging</b>",
            defaultValue: true
    }
}

// ===================================================================================
//  Lifecycle
// ===================================================================================

void installed() {
    logInfo "Installed - driver v${DRIVER_VERSION}"
    runIn(2, "configure")
}

void updated() {
    logInfo "Preferences saved - driver v${DRIVER_VERSION}"
    log.warn "Debug logging is: ${logEnable == true}"
    log.warn "Description logging is: ${txtEnable != false}"
    if (logEnable) runIn(1800, "logsOff")
    configure()
}

void logsOff() {
    log.warn "Debug logging disabled."
    device.updateSetting("logEnable", [value: "false", type: "bool"])
}

// ===================================================================================
//  Commands
// ===================================================================================

List<String> configure() {
    logInfo "Configuring device..."
    List<String> cmds = []
    String pid = getDataValue("productId")

    CONFIG_PARAMS.each { num, p ->
        if (!paramApplies(p, pid)) return
        def value = settings."param${num}"
        // Only push a value the user has set; otherwise just read the device's
        // current value (avoids stomping factory defaults that differ by revision).
        if (value != null) {
            cmds << secure(zwave.configurationV1.configurationSet(
                parameterNumber: num, size: p.size, scaledConfigurationValue: value as Integer))
        }
        cmds << secure(zwave.configurationV1.configurationGet(parameterNumber: num))
    }

    // Lifeline association so the hub receives unsolicited reports.
    cmds << secure(zwave.associationV2.associationSet(groupingIdentifier: 1, nodeId: [zwaveHubNodeId]))
    cmds << secure(zwave.associationV2.associationGet(groupingIdentifier: 1))

    cmds += refresh()
    return delayBetween(cmds, 500)
}

List<String> refresh() {
    logDebug "Refreshing device state..."
    List<String> cmds = [
        secure(zwave.manufacturerSpecificV2.manufacturerSpecificGet()),
        secure(zwave.versionV2.versionGet()),
        secure(zwave.notificationV8.notificationGet(notificationType: 0x07, event: 0x08)),
        secure(zwave.sensorBinaryV2.sensorBinaryGet(sensorType: 0x0C))
    ]
    return delayBetween(cmds, 500)
}

// ===================================================================================
//  Parse / Z-Wave event handlers
// ===================================================================================

void parse(String description) {
    logDebug "parse: ${description}"
    hubitat.zwave.Command cmd = zwave.parse(description, commandClassVersions)
    if (cmd) {
        zwaveEvent(cmd)
    } else {
        logDebug "Unparsed: ${description}"
    }
}

// Command class versions the device advertises (used by zwave.parse).
@Field static final Map commandClassVersions = [
    0x20: 1,  // Basic
    0x30: 2,  // Sensor Binary
    0x59: 3,  // Association Group Info
    0x70: 1,  // Configuration
    0x71: 8,  // Notification
    0x72: 2,  // Manufacturer Specific
    0x85: 2,  // Association
    0x86: 2,  // Version
    0x9F: 1   // Security 2
]

void zwaveEvent(hubitat.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
    hubitat.zwave.Command encap = cmd.encapsulatedCommand(commandClassVersions)
    if (encap) {
        logDebug "S0 encapsulated: ${encap}"
        zwaveEvent(encap)
    } else {
        logDebug "Unable to decode S0 encapsulated command: ${cmd}"
    }
}

void zwaveEvent(hubitat.zwave.commands.supervisionv1.SupervisionGet cmd) {
    hubitat.zwave.Command encap = cmd.encapsulatedCommand(commandClassVersions)
    if (encap) zwaveEvent(encap)
    // Acknowledge the supervised command.
    sendHubCommand(new hubitat.device.HubAction(secure(zwave.supervisionV1.supervisionReport(
        sessionID: cmd.sessionID, reserved: 0, moreStatusUpdates: false, status: 0xFF, duration: 0)),
        hubitat.device.Protocol.ZWAVE))
}

void zwaveEvent(hubitat.zwave.commands.notificationv8.NotificationReport cmd) {
    logDebug "NotificationReport: ${cmd}"
    if (cmd.notificationType == 0x07) {       // Home Security
        // event 0x07/0x08 = motion detected, 0x00 = idle/no event
        Boolean active = cmd.event in [0x07, 0x08]
        updatePresence(active)
    } else {
        logDebug "Unhandled notification type ${cmd.notificationType}"
    }
}

void zwaveEvent(hubitat.zwave.commands.sensorbinaryv2.SensorBinaryReport cmd) {
    logDebug "SensorBinaryReport: ${cmd}"
    updatePresence(cmd.sensorValue == 0xFF)
}

void zwaveEvent(hubitat.zwave.commands.basicv1.BasicReport cmd) {
    logDebug "BasicReport: ${cmd}"
    updatePresence(cmd.value > 0)
}

void zwaveEvent(hubitat.zwave.commands.basicv1.BasicSet cmd) {
    logDebug "BasicSet: ${cmd}"
    updatePresence(cmd.value > 0)
}

void zwaveEvent(hubitat.zwave.commands.configurationv1.ConfigurationReport cmd) {
    logDebug "ConfigurationReport: param ${cmd.parameterNumber} = ${cmd.scaledConfigurationValue}"
    Map p = CONFIG_PARAMS[cmd.parameterNumber as Integer]
    if (p) {
        device.updateSetting("param${cmd.parameterNumber}",
            [value: cmd.scaledConfigurationValue, type: p.type])
    }
}

void zwaveEvent(hubitat.zwave.commands.associationv2.AssociationReport cmd) {
    logDebug "AssociationReport: group ${cmd.groupingIdentifier} = ${cmd.nodeId}"
}

void zwaveEvent(hubitat.zwave.commands.versionv2.VersionReport cmd) {
    logDebug "VersionReport: ${cmd}"
    String fw = "${cmd.firmware0Version}.${cmd.firmware0SubVersion}"
    updateDataValue("firmwareVersion", fw)
    updateDataValue("protocolVersion", "${cmd.zWaveProtocolVersion}.${cmd.zWaveProtocolSubVersion}")
    logInfo "Firmware version: ${fw}"
}

void zwaveEvent(hubitat.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
    logDebug "ManufacturerSpecificReport: ${cmd}"
    updateDataValue("manufacturer", hubitat.helper.HexUtils.integerToHexString(cmd.manufacturerId, 2))
    updateDataValue("productTypeId", hubitat.helper.HexUtils.integerToHexString(cmd.productTypeId, 2))
    updateDataValue("productId", hubitat.helper.HexUtils.integerToHexString(cmd.productId, 2))
    String pid = hubitat.helper.HexUtils.integerToHexString(cmd.productId, 2)
    logInfo "Identified as ${pid == PID_V1 ? 'PS100 (original)' : pid == PID_V2 ? 'PS100 v2' : "unknown variant (productId ${pid})"}"
}

void zwaveEvent(hubitat.zwave.Command cmd) {
    logDebug "Unhandled Z-Wave command: ${cmd}"
}

// ===================================================================================
//  Helpers
// ===================================================================================

// True if a parameter exists on the detected revision. When the product ID is
// not yet known, all parameters are treated as applicable (safe superset).
private boolean paramApplies(Map p, String pid) {
    return !p.appliesTo || !pid || (pid in p.appliesTo)
}

// Factory default for a parameter, accounting for revision-specific differences.
private Integer paramDefault(Map p, String pid) {
    if (p.defaultByProduct) {
        Integer v = (pid && p.defaultByProduct[pid] != null) ? p.defaultByProduct[pid] : null
        return (v != null) ? v : p.defaultByProduct.values().first()
    }
    return p.defaultValue
}

private void updatePresence(Boolean active) {
    String presence = active ? "present" : "not present"
    String motion = active ? "active" : "inactive"

    if (device.currentValue("presence") != presence) {
        if (txtEnable != false) log.info "${device.displayName} presence is ${presence}"
        sendEvent(name: "presence", value: presence,
            descriptionText: "${device.displayName} presence is ${presence}")
    }
    if (device.currentValue("motion") != motion) {
        if (txtEnable != false) log.info "${device.displayName} motion is ${motion}"
        sendEvent(name: "motion", value: motion,
            descriptionText: "${device.displayName} motion is ${motion}")
    }
}

// Securely encapsulate outgoing commands when the device was paired with S0/S2.
private String secure(hubitat.zwave.Command cmd) {
    return zwaveSecureEncap(cmd)
}

private void logDebug(String msg) { if (logEnable) log.debug "${device.displayName}: ${msg}" }
private void logInfo(String msg)  { if (txtEnable != false) log.info "${device.displayName}: ${msg}" }
