# SmartScan

English | [简体中文](README_zh-CN.md)

A powerful Android QR code/barcode scanning application with support for batch scanning, history management, and data export features.

## 📥 Download & Installation

### Latest Version: v1.0.0

[![GitHub Release](https://img.shields.io/github/v/release/liyongcheng94/SmartScan)](https://github.com/liyongcheng94/SmartScan/releases/tag/v1.0.0)
[![Download APK](https://img.shields.io/badge/Download-APK-green)](https://github.com/liyongcheng94/SmartScan/releases/tag/v1.0.0)
[![License](https://img.shields.io/github/license/liyongcheng94/SmartScan)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)](https://developer.android.com)
[![API Level](https://img.shields.io/badge/API-26%2B-orange)](https://developer.android.com/guide/topics/manifest/uses-sdk-element)

**🚀 Direct Download**: [SmartScan v1.0.0 APK](https://github.com/liyongcheng94/SmartScan/releases/tag/v1.0.0)

### 📱 Installation Instructions

1. Download the latest APK file from the [Releases page](https://github.com/liyongcheng94/SmartScan/releases/tag/v1.0.0)
2. Enable "Unknown Sources" app installation permission on your Android device
3. Open the downloaded APK file to install
4. Grant camera permission when first launched to start using scanning features

### ⚡ Quick Start

After installation, you can immediately experience the following features:

- 📷 Open the app and start scanning QR codes immediately
- 📊 Try batch scanning mode
- 📋 View scanning history
- 📤 Export scan results to Excel files
- 🔒 Use completely offline without privacy concerns

## Features

### 📱 Core Features

- **Real-time Scanning**: Support real-time QR code and barcode recognition
- **Batch Scanning**: Scan multiple codes at once to improve work efficiency
- **History Management**: Automatically save scan records for easy viewing and management
- **Data Export**: Export scan results to Excel files with automatic invoice information parsing
- **Invoice Processing**: Automatically extract invoice number, amount, and date from QR code content

### 🛠️ Technical Features

- **High Precision Recognition**: Advanced image recognition algorithms
- **Fast Response**: Optimized scanning engine with quick recognition speed
- **Smart Data Parsing**: Automatically parse invoice information from scan content
- **Flexible Export Options**: Support exporting today's, all, or latest batch scan results
- **User Friendly**: Clean and intuitive user interface
- **Data Security**: Local storage to protect user privacy
- **Offline Operation**: No network permissions required, completely offline operation, preventing information leaks

## Screenshots

<div align="center">

### Home Interface

<img src="images/首页.jpg" width="300" alt="Home Interface">

### Scan History

<img src="images/扫描历史.jpg" width="300" alt="Scan History">

</div>

## System Requirements

- **Android Version**: Android 8.0 (API Level 26) and above
- **Target Version**: Android 14 (API Level 36)
- **Permission Requirements**:
  - Camera permission (required)
  - Storage permission (for file export)

## Main Functional Modules

### 1. Scanning Function

- Support multiple code types: QR codes, Code128, Code39, EAN-13, etc.
- Real-time preview and auto-focus
- Instant display of scan results

### 2. Batch Scanning

- Continuous scanning mode
- Batch result management
- One-click export of all results

### 3. History Records

- Automatic saving of scan records
- Time-sorted viewing
- Search and filter support
- Detailed record viewing

### 4. Data Export

- Excel format export with intelligent invoice information parsing
- Support exporting today's records, all records, or latest batch
- Automatic extraction of invoice number, amount, and date fields
- Custom file names with timestamp
- Share function support
- Batch export directly from scanning interface

## Technology Stack

- **Development Language**: Java
- **Minimum SDK**: API Level 26 (Android 8.0)
- **Target SDK**: API Level 36 (Android 14)
- **Build Tool**: Gradle Kotlin DSL
- **Dependency Management**: Version Catalog
- **UI Framework**: Android View System + ViewBinding

## Project Structure

```
SmartScan/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/willli/smart_scan/
│   │   │   │   ├── MainActivity.java          # Main interface
│   │   │   │   ├── ScanActivity.java          # Scan interface
│   │   │   │   ├── HistoryActivity.java       # History records
│   │   │   │   └── BatchScanActivity.java     # Batch scanning
│   │   │   ├── res/                           # Resource files
│   │   │   └── AndroidManifest.xml           # App configuration
│   │   ├── androidTest/                       # Integration tests
│   │   └── test/                              # Unit tests
│   └── build.gradle.kts                       # Module build config
├── gradle/
│   └── libs.versions.toml                     # Dependency version management
├── images/                                    # App screenshots
├── build.gradle.kts                          # Project build config
├── settings.gradle.kts                       # Project settings
└── README.md                                 # Project documentation
```

## Getting Started

### 📱 Regular Users

If you just want to use the SmartScan app, simply:

1. Go to the [Releases page](https://github.com/liyongcheng94/SmartScan/releases/tag/v1.0.0) to download the latest APK
2. Install and use it on your Android device

### 👨‍💻 Developers

If you want to participate in development or build it yourself, follow these steps:

#### Environment Setup

1. Android Studio Arctic Fox or higher
2. JDK 11 or higher
3. Android SDK API Level 36

#### Build Steps

1. Clone the project locally

   ```bash
   git clone https://github.com/liyongcheng94/SmartScan.git
   ```

2. Open the project with Android Studio

3. Sync Gradle dependencies

   ```bash
   ./gradlew build
   ```

4. Connect an Android device or start an emulator

5. Click the run button or use command line
   ```bash
   ./gradlew installDebug
   ```

## Permission Requirements

The app requires the following permissions to function properly:

- **CAMERA**: For scanning QR codes/barcodes
- **WRITE_EXTERNAL_STORAGE**: For exporting Excel files (Android 12 and below)
- **READ_EXTERNAL_STORAGE**: For reading files (Android 12 and below)
- **READ_MEDIA_IMAGES/VIDEO/AUDIO**: For accessing media files (Android 13+)
- **MANAGE_EXTERNAL_STORAGE**: For managing external storage (optional)

### 🔒 Privacy Protection

- **No Network Permissions**: The app does not request any network-related permissions and runs completely offline
- **No Data Upload**: All scan data is only saved locally on the device and never uploaded to servers
- **Privacy Security**: Prevents information leaks and protects the privacy of user scan content

## Version Information

- **Current Version**: v1.0.0
- **Version Code**: 1
- **Release Date**: July 26, 2025
- **APK Size**: Approximately 13.3 MB
- **Supported Architectures**: armeabi-v7a, arm64-v8a, x86, x86_64

### Version History

#### v1.0.0 (2025-08-01)

- ✨ First official release
- 📱 Real-time QR code/barcode scanning support
- 📊 Batch scanning functionality with export capability
- 📋 Enhanced scan history management
- 📤 Excel format data export with invoice information parsing
- 🧾 Automatic extraction of invoice number, amount, and date
- 📊 Multiple export options: today's records, all records, or latest batch
- 🔒 Local data storage for privacy protection

## Contributing

Welcome to submit Issues and Pull Requests to help improve the project:

1. Fork the project
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## Contact

If you have any questions or suggestions, please contact us through:

- Project Issues: [GitHub Issues](https://github.com/liyongcheng94/SmartScan/issues)
- Email: 1558139110@qq.com

---

**Made with ❤️ by [liyongcheng94]**
