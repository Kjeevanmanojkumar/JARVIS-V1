package com.example.model

enum class MemoryCategory(val displayName: String, val code: String) {
    USER_PREFERENCE("User Preference", "PREF"),
    PROJECT("Project", "PROJ"),
    PERSONAL_CONTEXT("Personal Context", "PERS"),
    TASK("Task", "TASK"),
    GENERAL("General", "GEN");

    companion object {
        fun fromString(value: String): MemoryCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) || it.code.equals(value, ignoreCase = true) }
                ?: GENERAL
        }
    }
}
