package me.weishu.k10ernelsu.data.repository

import me.weishu.k10ernelsu.data.model.AppInfo

interface SuperUserRepository {
    suspend fun getAppList(): Result<Pair<List<AppInfo>, List<Int>>>
    suspend fun refreshProfiles(currentApps: List<AppInfo>): Result<List<AppInfo>>
}
