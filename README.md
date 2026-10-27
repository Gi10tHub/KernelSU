# KernelSU for Samsung Devices

A comprehensive setup, patching, and compatibility guide for deploying **KernelSU** (a kernel-based root solution) on Samsung Galaxy devices.

---

## 📱 Compatibility Matrix

| Architecture / Kernel | Status | Requirements for Samsung Devices |
| :--- | :--- | :--- |
| **GKI 2.0 (Android 12+ / Kernel 5.10+)** | Officially Supported | Direct flashing of a compatible GKI `boot.img` without requiring custom kernel source builds. |
| **Legacy / Non-GKI (Kernel 4.14 / 4.19)** | Supported (Manual) | Requires downloading the specific kernel source from [Samsung Open Source](https://opensource.samsung.com/), integrating KernelSU patches, and building manually. |
| **Architectures** | `arm64-v8a` | Compatible with all modern Exynos and Snapdragon Galaxy chipsets. |

---

## ⚠️ Samsung-Specific Considerations

* **Knox Warranty Bit (0x1):** Unlocking the bootloader and flashing custom/patched binaries permanently trips Knox to `0x1`. This permanently disables hardware-backed security features including Samsung Wallet, Secure Folder, Samsung Pass, and Knox Workspace.
* **Knox Guard / KG State:** Ensure **OEM Unlocking** is enabled in Developer Options and verify that your device's KG/RMM state in Download Mode is not `Prenormal` or `Locked` prior to flashing.
* **AVB & dm-verity Enforcement:** Samsung bootloaders strictly verify partition signatures. A patched `vbmeta.img` with verification disabled (`--disable-verity --disable-verification`) must be flashed to prevent signature enforcement bootloops.

---

## 🛠️ Prerequisites

1. An unlocked bootloader on your Samsung device.
2. Official stock firmware matching your device's exact PDA/build version.
3. Flashing software: **Odin** (Windows) or **Heimdall** (Linux/macOS).
4. **KernelSU Manager** application (`.apk`).
5. A TAR packaging tool or terminal command line.

---

## 🚀 Installation & Flashing Steps

### 1. Extract the Stock Boot Image
* Extract the `AP_*.tar.md5` package from your stock firmware.
* Locate `boot.img.lz4` and decompress it to obtain the raw `boot.img`.

### 2. Prepare the KernelSU Image
* **For GKI Devices (Kernel 5.10+):** Replace the stock kernel inside `boot.img` with the prebuilt GKI KernelSU image.
* **For Legacy Devices:** Compile your device-specific kernel source with KernelSU integrated into a final `boot.img`.

### 3. Package Image into TAR Format
Because Samsung uses Download Mode instead of standard Fastboot, wrap the image into an Odin-flashable TAR container:
```bash
tar -cvf boot_ksu.tar boot.img
