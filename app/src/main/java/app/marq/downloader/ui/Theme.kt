package app.marq.downloader.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object MarqColors {
    val Ink = Color(0xFF12100E)
    val Bg = Color(0xFF171411)
    val Surface = Color(0xFF221E19)
    val Raised = Color(0xFF2C2721)
    val Line = Color(0xFF3D362E)
    val Fg = Color(0xFFF6F1EA)
    val Muted = Color(0xFFA89B8C)
    val Faint = Color(0xFF6F655B)
    val Copper = Color(0xFFE8843A)
    val OnCopper = Color(0xFF1C1008)
}

val DisplayStyle = TextStyle(
    fontFamily = FontFamily.Serif,
    fontWeight = FontWeight.SemiBold,
    fontSize = 36.sp,
    lineHeight = 38.sp,
    color = MarqColors.Fg,
)

@Composable
fun MarqTheme(content: @Composable () -> Unit) {
    // Marq is a dark-only design, regardless of system theme.
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = MarqColors.Copper,
            onPrimary = MarqColors.OnCopper,
            background = MarqColors.Bg,
            onBackground = MarqColors.Fg,
            surface = MarqColors.Surface,
            onSurface = MarqColors.Fg,
            surfaceVariant = MarqColors.Raised,
            outline = MarqColors.Line,
        ),
        content = content,
    )
}
