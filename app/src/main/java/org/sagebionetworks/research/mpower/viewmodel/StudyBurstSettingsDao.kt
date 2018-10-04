package org.sagebionetworks.research.mpower.viewmodel

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.google.gson.reflect.TypeToken
import org.sagebionetworks.bridge.rest.RestUtils
import org.threeten.bp.LocalDateTime
import javax.inject.Inject

open class StudyBurstSettingsDao @Inject constructor(context: Context) {
    val orderKey = "StudyBurstTaskOrder"
    val timestampKey = "StudyBurstTimestamp"

    private val prefs = context.getSharedPreferences("StudyBurstViewModel", Context.MODE_PRIVATE)

    @VisibleForTesting
    open fun setOrderedTasks(sortOrder: List<String>, timestamp: LocalDateTime) {
        val editPrefs = prefs.edit()
        editPrefs.putString(orderKey, RestUtils.GSON.toJson(sortOrder))
        editPrefs.putString(timestampKey, timestamp.toString())
        editPrefs.apply()
    }

    open fun getTaskSortOrder(): List<String> {
        prefs.getString(orderKey, null)?.let {
            return RestUtils.GSON.fromJson(it, object : TypeToken<List<String>>() {}.type)
        } ?: return StudyBurstViewModel.defaultTaskSortOrder
    }

    open fun getTaskSortOrderTimestamp(): LocalDateTime? {
        prefs.getString(timestampKey, null)?.let {
            return LocalDateTime.parse(it)
        } ?: return null
    }
}