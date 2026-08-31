package za.co.dope.ballistics.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import za.co.dope.ballistics.ui.navigation.DopeGoldenScreen

@PreviewTest
@Preview(
    name = "Samsung S25 portrait equivalent",
    device = "spec:width=360dp,height=780dp,dpi=480",
    showSystemUi = true,
)
@Composable
fun DashboardSamsungS25Preview() {
    DopeGoldenScreen(route = "dashboard")
}

@PreviewTest
@Preview(
    name = "Compact portrait",
    device = "spec:width=360dp,height=780dp,dpi=420",
)
@Composable
fun DashboardCompactPreview() {
    DopeGoldenScreen(route = "dashboard")
}

@PreviewTest
@Preview(
    name = "Landscape",
    device = "spec:width=780dp,height=360dp,dpi=420",
)
@Composable
fun RangeCardLandscapePreview() {
    DopeGoldenScreen(route = "range_card")
}

@PreviewTest
@Preview(
    name = "Large font",
    device = "spec:width=360dp,height=780dp,dpi=420",
    fontScale = 1.3f,
)
@Composable
fun ProfilesLargeFontPreview() {
    DopeGoldenScreen(route = "profiles")
}

@PreviewTest
@Preview(
    name = "Splash",
    device = "spec:width=360dp,height=780dp,dpi=480",
    showSystemUi = true,
)
@Composable
fun SplashSamsungS25Preview() {
    DopeGoldenScreen(route = "splash")
}

@PreviewTest
@Preview(
    name = "Target range choices",
    device = "spec:width=360dp,height=780dp,dpi=480",
)
@Composable
fun TargetRangeChoicesPreview() {
    DopeGoldenScreen(route = "target_range")
}

@PreviewTest
@Preview(
    name = "Environment manual offline",
    device = "spec:width=360dp,height=780dp,dpi=480",
    showSystemUi = true,
)
@Composable
fun EnvironmentManualOfflinePreview() {
    DopeGoldenScreen(route = "environment")
}

@PreviewTest
@Preview(
    name = "Wind wheel",
    device = "spec:width=360dp,height=780dp,dpi=480",
    showSystemUi = true,
)
@Composable
fun WindWheelPreview() {
    DopeGoldenScreen(route = "wind")
}

@PreviewTest
@Preview(
    name = "Session log",
    device = "spec:width=360dp,height=780dp,dpi=480",
    showSystemUi = true,
)
@Composable
fun SessionLogPreview() {
    DopeGoldenScreen(route = "session")
}
