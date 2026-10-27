//
// Created by weishu on 2022/12/9.
//

#include <sys/prctl.h>
#include <cstdint>
#include <cstring>
#include <cstdio>
#include <unistd.h>
#include <utility>
#include <android/log.h>
#include <dirent.h>
#include <cstdlib>

#include <unistd.h>
#include <climits>
#include <sys/syscall.h>
#include <cerrno>
#include <string>
#include "ksu.h"
#include "logging.h"

static int fd = -1;
static int get_info_result = INT_MIN;
static int get_info_errno = 0;
static int get_legacy_info_result = INT_MIN;
static int get_legacy_info_errno = 0;
static int legacy_prctl_result = INT_MIN;
static int legacy_prctl_errno = 0;
static int32_t legacy_prctl_version = -1;
static int32_t legacy_prctl_flags = 0;

static inline int scan_driver_fd() {
    const char *kName = "[ksu_driver]";
    DIR *dir = opendir("/proc/self/fd");
    if (!dir) {
        return -1;
    }

    int found = -1;
    struct dirent *de;
    char path[64];
    char target[PATH_MAX];

    while ((de = readdir(dir)) != NULL) {
        if (de->d_name[0] == '.') {
            continue;
        }

        char *endptr = NULL;
        long fd_long = strtol(de->d_name, &endptr, 10);
        if (!de->d_name[0] || *endptr != '\0' || fd_long < 0 || fd_long > INT_MAX) {
            continue;
        }

        snprintf(path, sizeof(path), "/proc/self/fd/%s", de->d_name);
        ssize_t n = readlink(path, target, sizeof(target) - 1);
        if (n < 0) {
            continue;
        }
        target[n] = '\0';

        const char *base = strrchr(target, '/');
        base = base ? base + 1 : target;

        if (strstr(base, kName)) {
            found = (int)fd_long;
            break;
        }
    }

    closedir(dir);
    return found;
}

template<typename... Args>
static int ksuctl(unsigned long op, Args &&... args) {

    if (fd < 0) {
        fd = scan_driver_fd();
    }

    static_assert(sizeof...(Args) <= 1, "ioctl expects at most one extra argument");

    return ioctl(fd, op, std::forward<Args>(args)...);
}

static struct ksu_get_info_cmd g_version {};

static std::string format_query_result(const char *name, int result, int error) {
    char details[160];
    if (result < 0) {
        snprintf(details, sizeof(details), "%s ret=%d errno=%d (%s)", name, result, error,
                 strerror(error));
    } else {
        snprintf(details, sizeof(details), "%s ret=%d", name, result);
    }
    return details;
}

struct ksu_get_info_cmd get_info() {
    if (!g_version.version) {
        errno = 0;
        get_info_result = ksuctl(KSU_IOCTL_GET_INFO, &g_version);
        get_info_errno = errno;
        if (get_info_result < 0) {
            errno = 0;
            get_legacy_info_result = ksuctl(KSU_IOCTL_GET_INFO_LEGACY, &g_version);
            get_legacy_info_errno = errno;
            g_version.uapi_version = 0;
        }
    }
    return g_version;
}

std::pair<int, int> legacy_get_info() {
    int32_t result = 0;
    errno = 0;
    legacy_prctl_result = prctl(
        0xDEADBEEF,
        2,
        &legacy_prctl_version,
        &legacy_prctl_flags,
        &result);
    legacy_prctl_errno = errno;
    return {legacy_prctl_version, legacy_prctl_flags};
}

std::string get_kernel_info_diagnostics() {
    const auto info = get_info();
    if (info.version <= 0 && legacy_prctl_result == INT_MIN) {
        legacy_get_info();
    }

    std::string details = format_query_result("GET_INFO ioctl", get_info_result, get_info_errno);
    if (get_legacy_info_result != INT_MIN) {
        details += "; ";
        details += format_query_result(
            "GET_INFO_LEGACY ioctl",
            get_legacy_info_result,
            get_legacy_info_errno);
    }
    char kernel_status[128];
    snprintf(kernel_status, sizeof(kernel_status),
             "; kernel version=%u flags=0x%08x manager=%s",
             info.version, info.flags,
             (info.flags & KSU_GET_INFO_FLAG_MANAGER) != 0 ? "true" : "false");
    details += kernel_status;
    if (legacy_prctl_result != INT_MIN) {
        details += "; ";
        details += format_query_result(
            "legacy prctl",
            legacy_prctl_result,
            legacy_prctl_errno);
        char legacy_status[96];
        snprintf(legacy_status, sizeof(legacy_status),
                 " version=%d flags=0x%08x",
                 legacy_prctl_version, legacy_prctl_flags);
        details += legacy_status;
    }

    return details;
}

uint32_t get_kernel_uapi_version() {
    auto info = get_info();
    return info.uapi_version;
}

uint32_t get_manager_uapi_version() {
    return KERNEL_SU_UAPI_VERSION;
}

uint32_t get_version() {
    auto info = get_info();
    return info.version;
}

bool get_allow_list(struct ksu_new_get_allow_list_cmd *cmd) {
    return ksuctl(KSU_IOCTL_NEW_GET_ALLOW_LIST, cmd) == 0;
}

bool is_safe_mode() {
    struct ksu_check_safemode_cmd cmd = {};
    ksuctl(KSU_IOCTL_CHECK_SAFEMODE, &cmd);
    return cmd.in_safe_mode;
}

bool is_lkm_mode() {
    auto info = get_info();
    if (info.version > 0) {
        return (info.flags & KSU_GET_INFO_FLAG_LKM) != 0;
    }
    return (legacy_get_info().second & KSU_GET_INFO_FLAG_LKM) != 0;
}

bool is_lkm_bundled() {
    auto info = get_info();
    return (info.flags & KSU_GET_INFO_FLAG_LKM) != 0 &&
           (info.flags & KSU_GET_INFO_FLAG_BUNDLED) != 0;
}

bool is_late_load_mode() {
    auto info = get_info();
    if (info.version > 0) {
        return (info.flags & KSU_GET_INFO_FLAG_LATE_LOAD) != 0;
    }
    return false;
}

bool is_manager() {
    auto info = get_info();
    if (info.version > 0) {
        return (info.flags & KSU_GET_INFO_FLAG_MANAGER) != 0;
    }
    return legacy_get_info().first > 0;
}

bool is_pr_build() {
    auto info = get_info();
    if (info.version > 0) {
        return (info.flags & KSU_GET_INFO_FLAG_PR_BUILD) != 0;
    }
    return false;
}

bool uid_should_umount(int uid) {
    struct ksu_uid_should_umount_cmd cmd = {};
    cmd.uid = uid;
    ksuctl(KSU_IOCTL_UID_SHOULD_UMOUNT, &cmd);
    return cmd.should_umount;
}

bool set_app_profile(const app_profile *profile) {
    struct ksu_set_app_profile_cmd cmd = {};
    cmd.profile = *profile;
    return ksuctl(KSU_IOCTL_SET_APP_PROFILE, &cmd) == 0;
}

int get_app_profile(app_profile *profile) {
    struct ksu_get_app_profile_cmd cmd = {.profile = *profile};
    int ret = ksuctl(KSU_IOCTL_GET_APP_PROFILE, &cmd);
    *profile = cmd.profile;
    return ret;
}

bool set_su_enabled(bool enabled) {
    struct ksu_set_feature_cmd cmd = {};
    cmd.feature_id = KSU_FEATURE_SU_COMPAT;
    cmd.value = enabled ? 1 : 0;
    return ksuctl(KSU_IOCTL_SET_FEATURE, &cmd) == 0;
}

bool is_su_enabled() {
    struct ksu_get_feature_cmd cmd = {};
    cmd.feature_id = KSU_FEATURE_SU_COMPAT;
    if (ksuctl(KSU_IOCTL_GET_FEATURE, &cmd) != 0) {
        return false;
    }
    if (!(cmd.supported & KSU_FEATURE_FLAG_SUPPORTED)) {
        return false;
    }
    return cmd.value != 0;
}

static inline bool get_feature(uint32_t feature_id, uint64_t *out_value, bool *out_supported) {
    struct ksu_get_feature_cmd cmd = {};
    cmd.feature_id = feature_id;
    if (ksuctl(KSU_IOCTL_GET_FEATURE, &cmd) != 0) {
        return false;
    }
    if (out_value) *out_value = cmd.value;
    if (out_supported) *out_supported = cmd.supported & KSU_FEATURE_FLAG_SUPPORTED;
    return true;
}

static inline bool set_feature(uint32_t feature_id, uint64_t value) {
    struct ksu_set_feature_cmd cmd = {};
    cmd.feature_id = feature_id;
    cmd.value = value;
    return ksuctl(KSU_IOCTL_SET_FEATURE, &cmd) == 0;
}

bool set_kernel_umount_enabled(bool enabled) {
    return set_feature(KSU_FEATURE_KERNEL_UMOUNT, enabled ? 1 : 0);
}

bool is_kernel_umount_enabled() {
    uint64_t value = 0;
    bool supported = false;
    if (!get_feature(KSU_FEATURE_KERNEL_UMOUNT, &value, &supported)) {
        return false;
    }
    if (!supported) {
        return false;
    }
    return value != 0;
}

int set_selinux_hide_enabled(bool enabled) {
    if (!set_feature(KSU_FEATURE_SELINUX_HIDE, enabled ? 1 : 0)) {
        return -errno;
    }
    return 0;
}

bool is_selinux_hide_enabled() {
    uint64_t value = 0;
    bool supported = false;
    if (!get_feature(KSU_FEATURE_SELINUX_HIDE, &value, &supported)) {
        return false;
    }
    if (!supported) {
        return false;
    }
    return value != 0;
}
