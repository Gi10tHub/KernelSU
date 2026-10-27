package me.weishu.k10ernelsu.data.repository

import me.weishu.k10ernelsu.data.model.Module
import me.weishu.k10ernelsu.data.model.ModuleUpdateInfo

interface ModuleRepository {
    suspend fun getModules(): Result<List<Module>>
    suspend fun checkUpdate(module: Module): Result<ModuleUpdateInfo>
}
