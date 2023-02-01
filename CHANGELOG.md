# Changelog

<a name="v3.0.1"></a>
## [Pedometer (Privacy Friendly) v3.0.1](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v3.0.1) - 01 Feb 2023

- Fixes/improvements for storing the step counts in DB  (Saving frequency from settings has more effect now)
- Various improvements to CSV exports (date+time, newer APIs)
- Fixed all linting errors (replaced getColumnIndex with getColumnIndexOrThrow)
- Added automated CI test to the project


[Changes][v3.0.1]


<a name="v3.0.0"></a>
## [Pedometer (Privacy Friendly) v3.0.0](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v3.0.0) - 06 Sep 2022

## What's Changed
* Reworked step detection 
* added permission check and fixed pending intent flags by [@Kamuno](https://github.com/Kamuno) in https://github.com/SecUSo/privacy-friendly-pedometer/pull/105
* Added correct icons by [@Kamuno](https://github.com/Kamuno) in https://github.com/SecUSo/privacy-friendly-pedometer/pull/106
* added new fastlane info by [@Kamuno](https://github.com/Kamuno) in https://github.com/SecUSo/privacy-friendly-pedometer/pull/107
* Fixed many smaller issues

**Full Changelog**: https://github.com/SecUSo/privacy-friendly-pedometer/compare/v2.4...v3.0.0

[Changes][v3.0.0]


<a name="v2.4"></a>
## [Pedometer (Privacy Friendly) v2.4](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v2.4) - 12 Jul 2021

- Made hardware step detection optional and opt-in since some devices are not compatible
- Added permission for physical activity (necessary from SKD 29+)
- Reduced sensor poll frequency for battery reasons
- Small fixes

[Changes][v2.4]


<a name="v2.3"></a>
## [Pedometer (Privacy Friendly) v2.3](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v2.3) - 09 Jul 2021

- Updated Service backend to work on newer Android versions
- Dependency updates

[Changes][v2.3]


<a name="v2.2"></a>
## [Pedometer (Privacy Friendly) v2.2](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v2.2) - 02 Jul 2021

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


<a name="v1.0.5"></a>
## [Privacy Friendly Pedometer v1.0.5](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v1.0.5) - 07 Mar 2017

- Minor Bug Fixes
- Manifest refinement


[Changes][v1.0.5]


<a name="v1.0.4"></a>
## [Privacy Friendly Pedometer v1.0.4](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v1.0.4) - 23 Dec 2016

- Minor Bug Fixes


[Changes][v1.0.4]


<a name="v1.0.3"></a>
## [Privacy Friendly Pedometer v1.0.3](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v1.0.3) - 14 Dec 2016

- Minor Bug Fixes
- Welcome dialog replaced with tutorial
- Downgrade to Android 6.0.1


[Changes][v1.0.3]


<a name="v.1.0.2"></a>
## [Privacy Friendly Pedometer v1.0.2 (v.1.0.2)](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v.1.0.2) - 17 Nov 2016

- Design adjustments
- Minor bug fixes


[Changes][v.1.0.2]


<a name="v1.0.1"></a>
## [Privacy Friendly Pedometer v1.0.1](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v1.0.1) - 10 Nov 2016

Improvement of device support
Improvement of help page


[Changes][v1.0.1]


<a name="v1.0"></a>
## [Privacy Friendly Pedometer v1.0](https://github.com/SecUSo/privacy-friendly-pedometer/releases/tag/v1.0) - 04 Nov 2016

The Privacy Friendly Pedometer can count your steps in background, provides you an overview about your walked steps and allows you to define custom walking modes and notifications if the achievement of your daily step goal is in danger. The App requires minimal permissions (Run at startup and prevent phone from sleeping). It belongs to the group of Privacy Friendly Apps from the research group SECUSO (Security, Usability and Society) by the Technische Universität Darmstadt, Germany.


[Changes][v1.0]


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

<!-- Generated by https://github.com/rhysd/changelog-from-release v3.7.0 -->
