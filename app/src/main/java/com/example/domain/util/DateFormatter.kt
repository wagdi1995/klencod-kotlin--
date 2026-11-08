package com.example.domain.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatter {

    private val arabicLocale = Locale("ar")

    private val fullArabicFormat = SimpleDateFormat("EEEE، d MMMM yyyy", arabicLocale)
    private val shortDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private val dayMonthFormat = SimpleDateFormat("dd/MM", Locale.US)
    private val timeFormat = SimpleDateFormat("hh:mm a", arabicLocale)

    val ARABIC_MONTH_NAMES = arrayOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    fun formatFullDateArabic(timestamp: Long): String {
        return fullArabicFormat.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        return shortDateFormat.format(Date(timestamp))
    }

    fun formatDayMonth(timestamp: Long): String {
        return dayMonthFormat.format(Date(timestamp))
    }

    fun formatRelative(timestamp: Long): String {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }

        val isToday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val isYesterday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - target.get(Calendar.DAY_OF_YEAR) == 1

        val timeStr = timeFormat.format(Date(timestamp))
            .replace("AM", "ص")
            .replace("PM", "م")

        return when {
            isToday -> "اليوم، $timeStr"
            isYesterday -> "أمس، $timeStr"
            else -> "${dayMonthFormat.format(Date(timestamp))} $timeStr"
        }
    }

    fun getMonthName(monthIndex0to11: Int): String {
        return ARABIC_MONTH_NAMES.getOrElse(monthIndex0to11) { "" }
    }
}
