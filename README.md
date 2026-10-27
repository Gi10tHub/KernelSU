 English | Español | 简体中文 | 繁體中文 | 日本語 | 한국어 | Polski | Português (Brasil) | Türkçe | Русский | Tiếng Việt | Indonesia | עברית | हिंदी | Italiano

KernelSU

￼

A kernel-based root solution for Android devices.

￼ ￼ ￼ ￼ ￼

Features

Kernel-based su and root access management.

Module system based on metamodules: Pluggable infrastructure for systemless modifications.

App Profile: Lock up the root power in a cage.

Compatibility state

KernelSU officially supports Android GKI 2.0 devices (kernel 5.10+). Older kernels (4.14+) are also supported, but the kernel will need to be built manually.

With this, WSA, ChromeOS, and container-based Android are all supported.

Currently, the arm64-v8a and x86_64 architectures are supported.

Caution

Recent kernel versions have implemented a breaking change causing KernelSU to fail and potentially trigger a kernel panic on x86_64! Check the website for more info!

Usage

Installation

How to build

Official website

Translation

To help translate KernelSU, we no longer accept translation contributions via Weblate. All translations are now handled using LLMs.

If you would like to add support for a new language, feel free to open a PR. Please note that modifications to existing English and Chinese translations are not accepted.

Discussion

Telegram: @KernelSU

Security

For information on reporting security vulnerabilities in KernelSU, see SECURITY.md.

License

Files under the kernel directory are GPL-2.0-only.

All other parts except the kernel directory are GPL-3.0-or-later.

Credits

Kernel-Assisted Superuser: The KernelSU idea.

Magisk: The powerful root tool.

genuine: APK v2 signature validation.

Diamorphine: Some rootkit skills.


