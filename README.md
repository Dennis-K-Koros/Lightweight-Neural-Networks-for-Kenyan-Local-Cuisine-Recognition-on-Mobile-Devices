# Lightweight Neural Networks for Kenyan Local Cuisine Recognition on Mobile Devices

<div align="center">

![Project Status](https://img.shields.io/badge/status-active-success.svg)
![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![API Level](https://img.shields.io/badge/API-26%2B-blue.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)

**NutriScan** - Real-time Kenyan cuisine recognition powered by on-device machine learning

[Features](#-features) • [Getting Started](#-getting-started) • [Dataset](#-dataset-and-training) • [Contributing](#-contributing)

</div>

---

## Table of Contents

- [Overview](#-overview)
- [Project Achievement](#-project-summary--achievement)
- [Features](#-features)
- [Technical Specifications](#-core-technical-specifications)
- [Prerequisites](#-prerequisites)
- [Getting Started](#-getting-started)
- [Repository Structure](#️-repository-structure)
- [Dataset and Training](#-dataset-and-training)
- [Usage](#-usage)
- [Future Works](#-future-works-and-contributions)
- [Contributing](#-contributing)
- [License](#-license)
- [Acknowledgments](#-acknowledgments)

---

## Overview

**NutriScan** is a lightweight, mobile-optimized system for real-time recognition of **12 Kenyan local cuisines**. Built with a fine-tuned **MobileNetV2** architecture and optimized via **TensorFlow Lite (TFLite)**, this solution enables fast, **offline inference** on standard Android devices without requiring internet connectivity.

This project addresses the critical need for accurate, accessible dietary assessment in low-resource environments where network connectivity is often unreliable and where global food recognition systems fail to recognize culturally specific dishes.

## Project Summary & Achievement

### Problem Statement
- **Cloud Dependency:** Most food recognition solutions require constant internet connectivity, making them impractical in areas with unreliable networks
- **Cultural Bias:** Global food recognition datasets overwhelmingly focus on Western cuisines, leaving African and specifically Kenyan dishes underrepresented
- **Accessibility:** Limited availability of nutrition tracking tools that understand local dietary patterns

### Our Solution
- ✅ **Offline-First Design:** Complete on-device inference with no internet required
- ✅ **Culturally Relevant:** Trained specifically on Kenyan local cuisines
- ✅ **Lightweight & Fast:** 2.5 MB model with ~350ms inference time
- ✅ **High Accuracy:** Achieved 78% Top-1 accuracy on validation set
- ✅ **Accessible:** Runs on devices with Android 8.0+ and 4GB RAM

## Features

- **12 Kenyan Dish Recognition:** Identifies Bhaji, Chapati, Githeri, Kachumbari, Mandazi, Masala Chips, Matoke, Mukimo, Nyamachoma, Pilau, Sukuma Wiki, and Ugali
- **Real-time Camera Classification:** Instant recognition using your device's camera
- **100% Offline:** No internet connection required after installation
- **Ultra-Fast Inference:** Results in under 350ms
- **Nutritional Information:** View nutritional data for recognized dishes
- **Lightweight:** Only 2.5 MB model size for quick downloads
- **Wide Device Support:** Compatible with Android 8.0+ devices

## Core Technical Specifications

| Component | Detail | Metric |
|:----------|:-------|:-------|
| **Model Architecture** | Transfer-learned MobileNetV2 | Optimized for efficiency |
| **Model Format** | TensorFlow Lite (`.tflite`) | Required for on-device deployment |
| **Final Model Size** | **~2.5 MB** | Ultra-low footprint for fast download |
| **Validation Accuracy** | **78%** (Top-1 Accuracy) | Achieved on independent test set |
| **Inference Time** | **~350 ms** | Confirms real-time capability |
| **Target Dishes** | 12 Classes | Bhaji, Chapati, Githeri, Kachumbari, Mandazi, Masala Chips, Matoke, Mukimo, Nyamachoma, Pilau, Sukuma Wiki, Ugali |
| **Minimum Device Specs** | Android 8.0+ (API 26), 4 GB RAM | Ensures wide accessibility |

##  Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio Dolphin (2021.3.1) or higher**
- **Android SDK** (API Level 33 or later recommended)
- **Java Development Kit (JDK) 11 or higher**
- **Physical Android device** (API 26+ / Android 8.0) or emulator with camera support

## Getting Started

### Installation

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Dennis-K-Koros/Lightweight-Neural-Networks-for-Kenyan-Local-Cuisine-Recognition-on-Mobile-Devices.git
   cd Lightweight-Neural-Networks-for-Kenyan-Local-Cuisine-Recognition-on-Mobile-Devices
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select **File → Open**
   - Navigate to the cloned project directory and click **OK**

3. **Sync Dependencies**
   - Android Studio will automatically download and sync all necessary Gradle and Kotlin dependencies (including the TFLite runtime)
   - Wait for the Gradle build to complete successfully
   - If you encounter any sync issues, try **File → Invalidate Caches / Restart**

4. **Run the Application**

   **Option A: Physical Device (Recommended for camera testing)**
   - Connect your Android device via USB
   - Enable **Developer Options** and **USB Debugging** on your device
   - Select your device from the target device dropdown in Android Studio
   - Click the green **Run** button 

   **Option B: Emulator**
   - Select an Android Virtual Device (AVD) from the target dropdown
   - Ensure the emulator has camera support enabled
   - Click **Run**

5. **Grant Permissions**
   - On first launch, grant camera permissions when prompted
   - The app requires camera access to capture and classify food images

## Repository Structure

```
.
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/              # Kotlin source code for Android app
│   │   │   ├── assets/            # Contains nutriscan_model.tflite
│   │   │   └── res/               # App resources (layouts, strings, images)
│   │   └── AndroidManifest.xml    # App configuration
│   └── build.gradle               # App-level Gradle configuration
├── build.gradle                   # Project-level Gradle configuration
├── LICENSE                        # Project license
└── README.md                      # This file
```

## 🔗 Dataset and Training

### Dataset Source
The model was trained on the **Food13 Dataset**, a curated collection of Kenyan cuisine images.

- **Download Dataset:** [Food13 Dataset on Dropbox](https://www.dropbox.com/scl/fi/hk1llnnv6bpjw153epfxo/Food13.zip?rlkey=o7iq83g4g0xjeif45ibxd9kkb&dl=0)
- **Dataset Size:** 13 categories (12 used in current model)
- **Image Format:** JPEG/PNG images of various resolutions

### Training Pipeline

1. **Data Preprocessing:** Image augmentation, normalization, and split into train/validation/test sets
2. **Transfer Learning:** Fine-tuning MobileNetV2 pre-trained on ImageNet
3. **Optimization:** Model quantization and TFLite conversion
4. **Validation:** Performance evaluation on held-out test set

## Usage

1. **Launch the App** on your Android device
2. **Point Camera** at a Kenyan dish from the supported categories
3. **Capture or Use Real-time Detection** to identify the dish
4. **View Results** including:
   - Dish name 
   - Nutritional information (calories, macronutrients)

### Supported Dishes

| Dish | Description |
|:-----|:------------|
| **Bhaji** | Spiced vegetable fritters |
| **Chapati** | Unleavened flatbread |
| **Githeri** | Boiled maize and beans |
| **Kachumbari** | Fresh tomato and onion salad |
| **Mandazi** | Sweet fried dough |
| **Masala Chips** | Spiced French fries |
| **Matoke** | Cooked plantains |
| **Mukimo** | Mashed potato, peas, and maize mixture |
| **Nyamachoma** | Grilled meat |
| **Pilau** | Spiced rice dish |
| **Sukuma Wiki** | Braised collard greens |
| **Ugali** | Maize flour staple |

## Future Works and Contributions

We welcome contributions from researchers and developers! Here are key areas for expansion:

### High-Priority Enhancements

1. **Multi-Item Recognition**
   - Transition to object detection framework (YOLO, SSD, or EfficientDet)
   - Recognize multiple dishes in a single image
   - Enable mixed-plate analysis for complete meal tracking

2. **Portion Size Estimation**
   - Implement depth estimation for volume calculation
   - Add reference object detection for scale calibration
   - Integrate computer vision techniques for density estimation
   - Move beyond fixed portion sizes for accurate nutrition tracking

3. **Dataset Expansion**
   - Add more images per category (target: 1000+ per class)
   - Include new Kenyan regional dishes
   - Capture variations in preparation styles
   - Add images with different lighting and backgrounds

### Additional Improvements

4. **Model Enhancements**
   - Experiment with newer architectures (EfficientNet, MobileNetV3)
   - Implement ensemble methods for improved accuracy
   - Add confidence-based rejection for ambiguous images

5. **User Experience**
   - Multi-language support (Swahili, Kikuyu, etc.)
   - Meal history tracking
   - Personalized nutrition recommendations
   - Social sharing features

6. **Technical Improvements**
   - iOS version development
   - Backend API for meal logging (optional)
   - Integration with fitness tracking apps

## Contributing

We enthusiastically welcome contributions! Here's how you can help:

### Ways to Contribute

- **Report Bugs:** Open an issue describing the problem
- **Suggest Features:** Share your ideas for improvements
- **Improve Documentation:** Help us make docs clearer
- **Contribute Data:** Share images of Kenyan dishes
- **Submit Code:** Fork, develop, and submit pull requests

### Contribution Process

1. **Fork the Repository**
2. **Create a Feature Branch**
   ```bash
   git checkout -b feature/AmazingFeature
   ```
3. **Commit Your Changes**
   ```bash
   git commit -m 'Add some AmazingFeature'
   ```
4. **Push to Branch**
   ```bash
   git push origin feature/AmazingFeature
   ```
5. **Open a Pull Request**

Please ensure your code follows the existing style and includes appropriate tests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- **Dataset Contributors:** Thanks to all who contributed to the Food13 dataset
- **TensorFlow Team:** For the excellent TFLite framework
- **Android Community:** For comprehensive documentation and support
- **Local Community:** Kenyan nutritionists and food experts who validated dish classifications

---

##  Contact & Support

- **Project Maintainer:** Dennis K. Koros
- **GitHub Issues:** [Report Issues](https://github.com/Dennis-K-Koros/Lightweight-Neural-Networks-for-Kenyan-Local-Cuisine-Recognition-on-Mobile-Devices/issues)
- **Discussions:** [GitHub Discussions](https://github.com/Dennis-K-Koros/Lightweight-Neural-Networks-for-Kenyan-Local-Cuisine-Recognition-on-Mobile-Devices/discussions)

---
