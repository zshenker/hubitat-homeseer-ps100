# Changelog

All notable changes to this project are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
