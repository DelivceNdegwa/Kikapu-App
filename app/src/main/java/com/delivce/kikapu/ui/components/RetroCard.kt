import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroBorder

@Composable
fun RetroCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = RetroTheme.SurfaceColor,
    borderColor: Color = RetroTheme.BorderColor,
    content: @Composable ColumnScope.() -> Unit
) {

    Column(
        modifier = modifier
            .retroBorder(borderColor=borderColor)
            .background(
                backgroundColor,
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        content = content
    )
}