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
 *  Device documentation: https://docs.homeseer.com/products/ps100
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

@Field static final String DRIVER_VERSION = "1.0.0"

/*
 * Z-Wave configuration parameters for the PS100, per HomeSeer's published spec:
 * https://docs.homeseer.com/products/ps100-specs-z-wave-parameters
 *
 * Each entry drives both a matching preference input and the configure() routine.
 * The preference setting name is "param<number>" (e.g. settings.param1).
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
    3 : [
        title      : "Bluetooth radio",
        description: "Only used by the HomeSeer mobile app to read the mmWave sensor. Default Disabled.",
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
        description: "0 = range disabled, 1-100 = sensitivity. Default 0 (disabled).",
        type       : "number", size: 1, defaultValue: 0, range: "0..100"
    ],
    9 : [
        title      : "Sensitivity: 225-300 cm (7.3-9.8 ft)",
        description: "0 = range disabled, 1-100 = sensitivity. Default 0 (disabled).",
        type       : "number", size: 1, defaultValue: 0, range: "0..100"
    ],
    10: [
        title      : "Sensitivity: 300-375 cm (9.8-12.3 ft)",
        description: "0 = range disabled, 1-100 = sensitivity. Default 0 (disabled).",
        type       : "number", size: 1, defaultValue: 0, range: "0..100"
    ],
    11: [
        title      : "Sensitivity: 375-450 cm (12.3-14.7 ft)",
        description: "0 = range disabled, 1-100 = sensitivity. Default 0 (disabled).",
        type       : "number", size: 1, defaultValue: 0, range: "0..100"
    ],
    12: [
        title      : "Sensitivity: 450-525 cm (14.7-17.2 ft)",
        description: "0 = range disabled, 1-100 = sensitivity. Default 0 (disabled).",
        type       : "number", size: 1, defaultValue: 0, range: "0..100"
    ],
    13: [
        title      : "Sensitivity: 525-600 cm (17.2-19.6 ft)",
        description: "0 = range disabled, 1-100 = sensitivity. Default 0 (disabled).",
        type       : "number", size: 1, defaultValue: 0, range: "0..100"
    ],
    20: [
        title      : "Left LED color",
        description: "Steady color for the left front LED. Default Off.",
        type       : "enum", size: 1, defaultValue: 0,
        options    : [0: "Off", 1: "Red", 2: "Green", 3: "Blue", 4: "Magenta", 5: "Yellow", 6: "Cyan", 7: "White"]
    ],
    21: [
        title      : "Right LED color",
        description: "Steady color for the right front LED. Default Off.",
        type       : "enum", size: 1, defaultValue: 0,
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

        // HomeSeer manufacturer ID is 000C. Product type/ID below match the PS100;
        // adjust if your "Device Details > Data" shows different values.
        fingerprint mfr: "000C", prod: "0203", deviceId: "0001",
            inClusters: "0x5E,0x55,0x9F,0x6C,0x71,0x30,0x85,0x70,0x86,0x72,0x5A,0x73,0x7A,0x59,0x84",
            controllerType: "ZWV", deviceJoinName: "HomeSeer PS100 mmWave Presence Sensor"
    }

    preferences {
        CONFIG_PARAMS.each { num, p ->
            if (p.type == "enum") {
                input name: "param${num}", type: "enum", title: "<b>[${num}] ${p.title}</b>",
                    description: p.description, options: p.options,
                    defaultValue: p.defaultValue, required: false
            } else {
                input name: "param${num}", type: "number", title: "<b>[${num}] ${p.title}</b>",
                    description: p.description, defaultValue: p.defaultValue,
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
    // Seed preference defaults so the first configure() pushes known-good values.
    CONFIG_PARAMS.each { num, p -> device.updateSetting("param${num}", [value: p.defaultValue, type: p.type]) }
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
    logInfo "Configuring device parameters..."
    List<String> cmds = []

    CONFIG_PARAMS.each { num, p ->
        Integer value = (settings."param${num}" != null ? settings."param${num}" : p.defaultValue) as Integer
        cmds << secure(zwave.configurationV1.configurationSet(
            parameterNumber: num, size: p.size, scaledConfigurationValue: value))
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
        secure(zwave.notificationV8.notificationGet(notificationType: 0x07, event: 0x08)),
        secure(zwave.sensorBinaryV2.sensorBinaryGet(sensorType: 0x0C)),
        secure(zwave.versionV2.versionGet()),
        secure(zwave.manufacturerSpecificV2.manufacturerSpecificGet())
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
}

void zwaveEvent(hubitat.zwave.Command cmd) {
    logDebug "Unhandled Z-Wave command: ${cmd}"
}

// ===================================================================================
//  Helpers
// ===================================================================================

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
