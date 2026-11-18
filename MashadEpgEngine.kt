package com.mashad.launcher.epg

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL

data class EpgProgram(
    val id: String,
    val channel: String,
    val title: String,
    val timeRange: String,
    val desc: String,
    val region: String,
    val category: String,
    val satellite: String,
    val ottName: String,
    val packageName: String,
    val deepLink: String,
    val xstreamUri: String,
    val startTime: Long,
    val endTime: Long,
    val isFeaturedAlert: Boolean
)

class MashadEpgEngine(private val context: Context) {

    private val prefs = context.getSharedPreferences("mashad_epg_prefs", Context.MODE_PRIVATE)
    private val feedUrl = "https://raw.githubusercontent.com/mashadcloud-art/mashadlauncher/main/live_guide.json"

    fun saveUserInterests(interests: Set<String>) {
        prefs.edit().putStringSet("user_interests", interests).apply()
    }

    fun getUserInterests(): Set<String> =
        prefs.getStringSet("user_interests", emptySet()) ?: emptySet()

    suspend fun fetchLiveSchedule(
        regionFilter: String = "All",
        categoryFilter: String = "All",
        platformFilter: String = "All"
    ): List<EpgProgram> = withContext(Dispatchers.IO) {
        val result = mutableListOf<EpgProgram>()
        val savedInterests = getUserInterests()

        try {
            val jsonText = URL(feedUrl).readText()
            val array = JSONArray(jsonText)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val region = obj.getString("region")
                val category = obj.getString("category")
                val ottName = obj.getString("ott_name")

                // Filter by saved user interests if configured
                if (savedInterests.isNotEmpty()) {
                    if (!savedInterests.contains(region) && !savedInterests.contains(category)) {
                        continue
                    }
                }

                // Filter by UI chips
                if (regionFilter != "All" && !region.equals(regionFilter, ignoreCase = true)) continue
                if (categoryFilter != "All" && !category.equals(categoryFilter, ignoreCase = true)) continue
                if (platformFilter != "All" && !ottName.contains(platformFilter, ignoreCase = true)) continue

                result.add(
                    EpgProgram(
                        id = obj.getString("id"),
                        channel = obj.getString("channel"),
                        title = obj.getString("title"),
                        timeRange = obj.getString("time_range"),
                        desc = obj.optString("desc", ""),
                        region = region,
                        category = category,
                        satellite = obj.getString("satellite"),
                        ottName = ottName,
                        packageName = obj.getString("package_name"),
                        deepLink = obj.getString("deep_link"),
                        xstreamUri = obj.optString("xstream_uri", ""),
                        startTime = obj.getLong("start_time"),
                        endTime = obj.getLong("end_time"),
                        isFeaturedAlert = obj.getBoolean("is_featured_alert")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        result
    }

    fun launchPrimaryOtt(program: EpgProgram) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(program.deepLink)).apply {
                setPackage(program.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(program.deepLink)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }

    fun launchAirtelXstream(program: EpgProgram) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(program.xstreamUri)).apply {
                setPackage("tv.airtel.xstream.tvapp")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(program.xstreamUri)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        }
    }
}
