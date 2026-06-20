import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.delivce.kikapu.ui.foundation.RetroDefaults
import com.delivce.kikapu.ui.foundation.RetroTheme
import com.delivce.kikapu.ui.foundation.retroBorder

@Composable
fun RetroSurface(
    modifier: Modifier = Modifier,
    backgroundColor: Color = RetroTheme.SurfaceColor,
    shape: Shape = RoundedCornerShape(RetroDefaults.CornerRadius),
    content: @Composable BoxScope.() -> Unit
) {

    val borderColor = RetroTheme.BorderColor
    val shadowColor = RetroTheme.ShadowColor

    Box(
        modifier = modifier
    ) {

        Box(
            Modifier
                .offset(
                    RetroDefaults.ShadowOffset,
                    RetroDefaults.ShadowOffset
                )
                .matchParentSize()
                .background(
                    shadowColor,
                    RoundedCornerShape(
                        RetroDefaults.CornerRadius
                    )
                )
        )

        Box(
            Modifier
                .matchParentSize()
                .background(
                    backgroundColor,
                    RoundedCornerShape(
                        RetroDefaults.CornerRadius
                    )
                )
                .retroBorder(borderColor)
        )

        Box(
            Modifier.padding(
                RetroDefaults.ContentPadding
            )
        ) {
            content()
        }
    }
}