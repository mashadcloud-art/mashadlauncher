package com.mashad.launcher.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Data model for live TV, sports fixtures, and regional broadcast cards.
 */
data class LiveProgram(
    val id: String,
    val title: String,
    val channel: String,
    val platform: String,
    val category: String,
    val region: String,
    val time: String = "LIVE NOW",
    val liveScore: String = "",
    val teamA: String? = null,
    val teamB: String? = null,
    val pkg: String = "in.startv.hotstar",
    val deepLink: String = "https://www.hotstar.com/in/sports",
    val xstreamUri: String = "https://www.airtelxstream.in/livetv-channels"
)

/**
 * Mashad Single-Event Live Dock Strip.
 * Displays ONE match or show at a time with real-time score updates,
 * D-Pad remote switcher (Prev/Next), and dual action launch buttons.
 */
@Composable
fun MashadSingleEventDockWidget(
    programs: List<LiveProgram>,
    onWatchPrimary: (LiveProgram) -> Unit,
    onWatchAirtelXstream: (LiveProgram) -> Unit,
    modifier: Modifier = Modifier
) {
    if (programs.isEmpty()) return

    var currentIndex by remember { mutableStateOf(0) }
    val safeIndex = currentIndex.coerceIn(0, programs.lastIndex)
    val item = programs[safeIndex]

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xCC0F172A))
            .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Column: Status, Live Score, Title, Broadcaster info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🔴 ${item.time}  (${safeIndex + 1}/${programs.size})",
                    color = Color(0xFFFB7185),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                if (item.liveScore.isNotEmpty()) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "⚡ ${item.liveScore}",
                        color = Color(0xFF6EE7B7),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = item.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Text(
                text = "${item.channel} • ${item.platform} • ${item.category} (${item.region})",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }

        Spacer(Modifier.width(16.dp))

        // Right Row: Direct Watch, Airtel Xstream Free & Prev/Next controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onWatchPrimary(item) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Watch", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { onWatchAirtelXstream(item) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFECDD3)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Xstream Free", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            IconButton(onClick = {
                currentIndex = if (safeIndex - 1 < 0) programs.lastIndex else safeIndex - 1
            }) {
                Text("‹", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = {
                currentIndex = (safeIndex + 1) % programs.size
            }) {
                Text("›", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/**
 * Launcher Dock Container with Embedded Live Event Widget.
 * Embeds MashadSingleEventDockWidget directly above the app icon dock row.
 */
@Composable
fun LauncherDockWithLiveEvent(
    livePrograms: List<LiveProgram>,
    dockAppTilesContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. EMBEDDED SINGLE EVENT HERO DOCK
        MashadSingleEventDockWidget(
            programs = livePrograms,
            onWatchPrimary = { program ->
                launchOttApp(context, program.deepLink, program.pkg)
            },
            onWatchAirtelXstream = { program ->
                launchOttApp(context, program.xstreamUri, "tv.airtel.xstream.tvapp")
            }
        )

        // 2. LAUNCHER APP ICONS / DOCK ROW
        dockAppTilesContent()
    }
}

/**
 * Helper function to trigger Android TV intent to launch OTT apps.
 */
private fun launchOttApp(context: Context, uriString: String, packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
            setPackage(packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to browser or system chooser
        val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallback)
    }
}
