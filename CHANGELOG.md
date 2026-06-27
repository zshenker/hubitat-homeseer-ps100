# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-06-27

### Added
- Verified Z-Wave fingerprints for **both** PS100 hardware revisions, sourced
  from the Z-Wave JS device database: original PS100 (`000C:0204:0002`) and
  PS100 v2 (`000C:0204:0003`). The driver now auto-assigns on inclusion.
- Automatic variant detection via the Manufacturer Specific product ID, with a
  log line identifying the detected revision.
- Parameter 2 (Distance Report Interval) for the original PS100.

### Changed
- Revision-specific parameters are shown in preferences only for the matching
  unit (param 2 on the original; LED colors 20/21 on v2). Until the product ID
  is known, the full superset is shown.
- Sensitivity defaults (params 8–13) are now displayed per revision.
- `configure()` no longer force-writes factory defaults on install; it only
  pushes preferences you have explicitly set and reads back the rest, so it
  won't stomp the device's own (revision-specific) factory configuration.

### Fixed
- Corrected the fingerprint, which previously used incorrect placeholder IDs
  (`000C:0203:0001`).

## [1.0.0] - 2026-06-27

### Added
- Initial release of the HomeSeer PS100 mmWave Presence Sensor driver.
- `PresenceSensor` and `MotionSensor` reporting via the Z-Wave Notification
  (Home Security) and Sensor Binary command classes, with Basic Set/Report
  fallback.
- All HomeSeer-documented configuration parameters exposed as preferences:
  no-motion timeout (1), Bluetooth (3), motion LED (4), per-range sensitivities
  (6–13), and left/right LED colors (20, 21).
- Lifeline association, `configure()`, and `refresh()` support.
- S0/S2 secure command encapsulation.
- Z-Wave Supervision Get acknowledgement.
- Firmware/manufacturer info read into Device Data.
- Hubitat Package Manager `packageManifest.json` and `repository.json`.
