package com.subhajitrajak.pushcounter.utils

import android.content.Context
import android.text.Layout
import androidx.core.graphics.ColorUtils
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.LayeredComponent
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.DashedShape
import com.patrykandpatrick.vico.core.common.shape.MarkerCorneredShape
import com.subhajitrajak.pushcounter.R

internal fun getMarker(
    context: Context,
    valueFormatter: DefaultCartesianMarker.ValueFormatter =
        DefaultCartesianMarker.ValueFormatter.default(),
    showIndicator: Boolean = true,
): CartesianMarker {
    val primary = context.getColor(R.color.primary)
    val translucentPrimary = ColorUtils.setAlphaComponent(primary, 25)

    val labelBackgroundShape = MarkerCorneredShape(CorneredShape.Corner.Rounded)
    val labelBackground =
        ShapeComponent(
            fill = Fill(translucentPrimary),
            shape = labelBackgroundShape,
            strokeThicknessDp = 1f,
            strokeFill = Fill(primary),
        )
    val label =
        TextComponent(
            color = context.getColor(R.color.black),
            textAlignment = Layout.Alignment.ALIGN_CENTER,
            padding = Insets(horizontalDp = 8f, verticalDp = 4f),
            background = labelBackground,
            minWidth = TextComponent.MinWidth.fixed(valueDp = 40f),
        )
    val indicatorFrontComponent =
        ShapeComponent(Fill(translucentPrimary), CorneredShape.Pill)
    val guideline =
        LineComponent(
            fill = Fill(primary),
            thicknessDp = 1f,
            shape = DashedShape(),
        )
    return DefaultCartesianMarker(
        label = label,
        valueFormatter = valueFormatter,
        indicator =
            if (showIndicator) {
                { color ->
                    LayeredComponent(
                        back = ShapeComponent(Fill(ColorUtils.setAlphaComponent(color, 40)), CorneredShape.Pill),
                        front =
                            LayeredComponent(
                                back = ShapeComponent(Fill(color), CorneredShape.Pill),
                                front = indicatorFrontComponent,
                                padding = Insets(allDp = 5f),
                            ),
                        padding = Insets(allDp = 10f),
                    )
                }
            } else {
                null
            },
        indicatorSizeDp = 36f,
        guideline = guideline,
    )
}