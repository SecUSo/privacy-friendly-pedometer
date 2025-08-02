# Changelog

<a id="v3.1.2"></a>
## [Pedometer (Privacy Friendly) v3.1.2](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v3.1.2) - 2025-08-02

## What's Changed
* Resolves "' is not escaped and it breaks the build." in it translation by [@FlutterFraz](https://github.com/FlutterFraz) in [#187](https://github.com/SecUSo/privacy-friendly-pedometer/pull/187)
* Updates CI by [@coderPaddyS](https://github.com/coderPaddyS) in [#191](https://github.com/SecUSo/privacy-friendly-pedometer/pull/191)
* Change versionCode to differentiate from last Google Play version by [@coderPaddyS](https://github.com/coderPaddyS) in [#192](https://github.com/SecUSo/privacy-friendly-pedometer/pull/192)


**Full Changelog**: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v3.1.1...v3.1.2

[Changes][v3.1.2]


<a id="v3.1.0"></a>
## [Pedometer (Privacy Friendly) v3.1.0](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v3.1.0) - 2024-01-23

## What's Changed
* Make correct widget size more reliable on API >=31 by [@morckx](https://github.com/morckx) in [#124](https://github.com/SecUSo/privacy-friendly-pedometer/pull/124)
* Translation to Polish language by [@WaldiSt](https://github.com/WaldiSt) in [#67](https://github.com/SecUSo/privacy-friendly-pedometer/pull/67)
* Swedish translation by [@bengan](https://github.com/bengan) in [#92](https://github.com/SecUSo/privacy-friendly-pedometer/pull/92)
* Fix translations by [@morckx](https://github.com/morckx) in [#125](https://github.com/SecUSo/privacy-friendly-pedometer/pull/125)
* Upgrade target api and dependencies by [@morckx](https://github.com/morckx) in [#129](https://github.com/SecUSo/privacy-friendly-pedometer/pull/129)
* Add missing channel id in motivation notifications by [@morckx](https://github.com/morckx) in [#133](https://github.com/SecUSo/privacy-friendly-pedometer/pull/133)
* Fix `ApplicationTest.java` by [@udenr](https://github.com/udenr) in [#136](https://github.com/SecUSo/privacy-friendly-pedometer/pull/136)
* Add `ci.yml` workflow by [@udenr](https://github.com/udenr) in [#137](https://github.com/SecUSo/privacy-friendly-pedometer/pull/137)
* documentation: Added info re: Privacy Friendly Backup to README.md by [@jahway603](https://github.com/jahway603) in [#135](https://github.com/SecUSo/privacy-friendly-pedometer/pull/135)

## New Contributors
* [@WaldiSt](https://github.com/WaldiSt) made their first contribution in [#67](https://github.com/SecUSo/privacy-friendly-pedometer/pull/67)
* [@bengan](https://github.com/bengan) made their first contribution in [#92](https://github.com/SecUSo/privacy-friendly-pedometer/pull/92)
* [@udenr](https://github.com/udenr) made their first contribution in [#136](https://github.com/SecUSo/privacy-friendly-pedometer/pull/136)
* [@jahway603](https://github.com/jahway603) made their first contribution in [#135](https://github.com/SecUSo/privacy-friendly-pedometer/pull/135)

**Full Changelog**: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v3.0.1...v3.1.0

[Changes][v3.1.0]


<a id="v3.0.1"></a>
## [Pedometer (Privacy Friendly) v3.0.1](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v3.0.1) - 2023-02-01

- Fixes/improvements for storing the step counts in DB  (Saving frequency from settings has more effect now)
- Various improvements to CSV exports (date+time, newer APIs)
- Fixed all linting errors (replaced getColumnIndex with getColumnIndexOrThrow)
- Added automated CI test to the project


[Changes][v3.0.1]


<a id="v3.0.0"></a>
## [Pedometer (Privacy Friendly) v3.0.0](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v3.0.0) - 2022-09-06

## What's Changed
* Reworked step detection 
* added permission check and fixed pending intent flags by [@Kamuno](https://github.com/Kamuno) in [#105](https://github.com/SecUSo/privacy-friendly-pedometer/pull/105)
* Added correct icons by [@Kamuno](https://github.com/Kamuno) in [#106](https://github.com/SecUSo/privacy-friendly-pedometer/pull/106)
* added new fastlane info by [@Kamuno](https://github.com/Kamuno) in [#107](https://github.com/SecUSo/privacy-friendly-pedometer/pull/107)
* Fixed many smaller issues

**Full Changelog**: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v2.4...v3.0.0

[Changes][v3.0.0]


<a id="v2.4"></a>
## [Pedometer (Privacy Friendly) v2.4](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v2.4) - 2021-07-12

- Made hardware step detection optional and opt-in since some devices are not compatible
- Added permission for physical activity (necessary from SKD 29+)
- Reduced sensor poll frequency for battery reasons
- Small fixes

[Changes][v2.4]


<a id="v2.3"></a>
## [Pedometer (Privacy Friendly) v2.3](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v2.3) - 2021-07-09

- Updated Service backend to work on newer Android versions
- Dependency updates

[Changes][v2.3]


<a id="v2.2"></a>
## [Pedometer (Privacy Friendly) v2.2](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v2.2) - 2021-07-02

Functionality:
- Added option to export to CSV file
- Added function to show current velocity
- Added function to change number count manually
- Added button for step counting start/stop
- Added widgets
- Added distance measurement function
- Incorporated Backup-API so backups with the Privacy Friendly Backup App can be made.
- Added Joule energy unit option
- Added support for hardware step counter

Other improvements:
- Extended Help section
- Added F-Droid description files
- Added changeable step threshold for counting
- French and German translation
- Updated code dependencies to AndroidX
- Crash fixes, bug fixes, improvements

[Changes][v2.2]


<a id="v1.0.5"></a>
## [Privacy Friendly Pedometer v1.0.5](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v1.0.5) - 2017-03-07

- Minor Bug Fixes
- Manifest refinement


[Changes][v1.0.5]


<a id="v1.0.4"></a>
## [Privacy Friendly Pedometer v1.0.4](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v1.0.4) - 2016-12-23

- Minor Bug Fixes


[Changes][v1.0.4]


<a id="v1.0.3"></a>
## [Privacy Friendly Pedometer v1.0.3](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v1.0.3) - 2016-12-14

- Minor Bug Fixes
- Welcome dialog replaced with tutorial
- Downgrade to Android 6.0.1


[Changes][v1.0.3]


<a id="v.1.0.2"></a>
## [Privacy Friendly Pedometer v1.0.2 (v.1.0.2)](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v.1.0.2) - 2016-11-17

- Design adjustments
- Minor bug fixes


[Changes][v.1.0.2]


<a id="v1.0.1"></a>
## [Privacy Friendly Pedometer v1.0.1](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v1.0.1) - 2016-11-10

Improvement of device support
Improvement of help page


[Changes][v1.0.1]


<a id="v1.0"></a>
## [Privacy Friendly Pedometer v1.0](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v1.0) - 2016-11-04

The Privacy Friendly Pedometer can count your steps in background, provides you an overview about your walked steps and allows you to define custom walking modes and notifications if the achievement of your daily step goal is in danger. The App requires minimal permissions (Run at startup and prevent phone from sleeping). It belongs to the group of Privacy Friendly Apps from the research group SECUSO (Security, Usability and Society) by the Technische Universität Darmstadt, Germany.


[Changes][v1.0]


[v3.1.2]: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v3.1.0...v3.1.2
[v3.1.0]: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v3.0.1...v3.1.0
[v3.0.1]: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v3.0.0...v3.0.1
[v3.0.0]: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v2.4...v3.0.0
[v2.4]: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v2.3...v2.4
[v2.3]: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v2.2...v2.3
[v2.2]: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v1.0.5...v2.2
[v1.0.5]: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v1.0.4...v1.0.5
[v1.0.4]: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v1.0.3...v1.0.4
[v1.0.3]: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v.1.0.2...v1.0.3
[v.1.0.2]: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v1.0.1...v.1.0.2
[v1.0.1]: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v1.0...v1.0.1
[v1.0]: https://github.com/SecUSo/privacy-friendly-pedometer/tree/v1.0

<!-- Generated by https://github.com/rhysd/changelog-from-release v3.9.0 -->
