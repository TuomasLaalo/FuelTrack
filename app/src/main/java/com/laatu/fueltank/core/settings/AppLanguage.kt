package com.laatu.fueltank.core.settings

enum class AppLanguage(
    val storageValue: String,
    val languageTag: String?,
) {
    SYSTEM("system", null),
    ENGLISH("en", "en"),
    FINNISH("fi", "fi");

    companion object {
        fun fromStorageValue(value: String?): AppLanguage =
            entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}
