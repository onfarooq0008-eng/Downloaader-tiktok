package app.marq.downloader.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarqMark(size: Dp, color: Color = MarqColors.Copper) {
    val a = remember { PathParser().parsePathString("M12 5.5H10.5A5 5 0 0 0 5.5 10.5v11A5 5 0 0 0 10.5 26.5h11a5 5 0 0 0 5-5V20").toPath() }
    val b = remember { PathParser().parsePathString("M18.5 5.5H22a4.5 4.5 0 0 1 4.5 4.5v3.5").toPath() }
    Canvas(Modifier.size(size)) {
        val k = this.size.width / 32f
        scale(k, k, pivot = Offset.Zero) {
            drawPath(a, color, style = Stroke(width = 2.4f, cap = StrokeCap.Round))
            drawPath(b, color.copy(alpha = 0.4f), style = Stroke(width = 2.4f, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun Eyebrow(text: String) {
    Text(text.uppercase(), color = MarqColors.Copper, fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
}

@Composable
fun PrimaryButton(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) MarqColors.Copper else MarqColors.Copper.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = MarqColors.OnCopper, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MarqColors.Raised.copy(alpha = if (enabled) 1f else 0.5f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(PaddingValues(horizontal = 16.dp)),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}
