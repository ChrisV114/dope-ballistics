package za.co.dope.ballistics.engine

internal data class DragPoint(
    val mach: Double,
    val coefficient: Double,
)

/**
 * BRL standard-projectile Cd-versus-Mach tables published by JBM Ballistics.
 * Values are linearly interpolated; no projectile coefficient is inferred here.
 */
internal object StandardDragTables {
    const val SOURCE = "BRL G1/G7 Cd tables published by JBM Ballistics (retrieved 2026-08-31)"

    private val g1 =
        points(
            "0:.2629 .05:.2558 .10:.2487 .15:.2413 .20:.2344 .25:.2278 .30:.2214 " +
                ".35:.2155 .40:.2104 .45:.2061 .50:.2032 .55:.2020 .60:.2034 .70:.2165 " +
                ".725:.2230 .75:.2313 .775:.2417 .80:.2546 .825:.2706 .85:.2901 .875:.3136 " +
                ".90:.3415 .925:.3734 .95:.4084 .975:.4448 1:.4805 1.025:.5136 1.05:.5427 " +
                "1.075:.5677 1.10:.5883 1.125:.6053 1.15:.6191 1.20:.6393 1.25:.6518 " +
                "1.30:.6589 1.35:.6621 1.40:.6625 1.45:.6607 1.50:.6573 1.55:.6528 " +
                "1.60:.6474 1.65:.6413 1.70:.6347 1.75:.6280 1.80:.6210 1.85:.6141 " +
                "1.90:.6072 1.95:.6003 2:.5934 2.05:.5867 2.10:.5804 2.15:.5743 " +
                "2.20:.5685 2.25:.5630 2.30:.5577 2.35:.5527 2.40:.5481 2.45:.5438 " +
                "2.50:.5397 2.60:.5325 2.70:.5264 2.80:.5211 2.90:.5168 3:.5133 " +
                "3.10:.5105 3.20:.5084 3.30:.5067 3.40:.5054 3.50:.5040 3.60:.5030 " +
                "3.70:.5022 3.80:.5016 3.90:.5010 4:.5006 4.20:.4998 4.40:.4995 " +
                "4.60:.4992 4.80:.4990 5:.4988",
        )

    private val g7 =
        points(
            "0:.1198 .05:.1197 .10:.1196 .15:.1194 .20:.1193 .25:.1194 .30:.1194 " +
                ".35:.1194 .40:.1193 .45:.1193 .50:.1194 .55:.1193 .60:.1194 .65:.1197 " +
                ".70:.1202 .725:.1207 .75:.1215 .775:.1226 .80:.1242 .825:.1266 " +
                ".85:.1306 .875:.1368 .90:.1464 .925:.1660 .95:.2054 .975:.2993 " +
                "1:.3803 1.025:.4015 1.05:.4043 1.075:.4034 1.10:.4014 1.125:.3987 " +
                "1.15:.3955 1.20:.3884 1.25:.3810 1.30:.3732 1.35:.3657 1.40:.3580 " +
                "1.50:.3440 1.55:.3376 1.60:.3315 1.65:.3260 1.70:.3209 1.75:.3160 " +
                "1.80:.3117 1.85:.3078 1.90:.3042 1.95:.3010 2:.2980 2.05:.2951 " +
                "2.10:.2922 2.15:.2892 2.20:.2864 2.25:.2835 2.30:.2807 2.35:.2779 " +
                "2.40:.2752 2.45:.2725 2.50:.2697 2.55:.2670 2.60:.2643 2.65:.2615 " +
                "2.70:.2588 2.75:.2561 2.80:.2533 2.85:.2506 2.90:.2479 2.95:.2451 " +
                "3:.2424 3.10:.2368 3.20:.2313 3.30:.2258 3.40:.2205 3.50:.2154 " +
                "3.60:.2106 3.70:.2060 3.80:.2017 3.90:.1975 4:.1935 4.20:.1861 " +
                "4.40:.1793 4.60:.1730 4.80:.1672 5:.1618",
        )

    @Suppress("ReturnCount")
    fun coefficient(
        model: DragModel,
        mach: Double,
    ): Double {
        val table = if (model == DragModel.G1) g1 else g7
        if (mach <= table.first().mach) return table.first().coefficient
        if (mach >= table.last().mach) return table.last().coefficient
        val upperIndex =
            table.binarySearch { it.mach.compareTo(mach) }.let {
                if (it >= 0) return table[it].coefficient else -it - 1
            }
        val lower = table[upperIndex - 1]
        val upper = table[upperIndex]
        val fraction = (mach - lower.mach) / (upper.mach - lower.mach)
        return lower.coefficient + fraction * (upper.coefficient - lower.coefficient)
    }

    private fun points(value: String): List<DragPoint> =
        value.split(' ').filter(String::isNotBlank).map { token ->
            val (mach, coefficient) = token.split(':')
            DragPoint(mach.toDouble(), coefficient.toDouble())
        }
}
