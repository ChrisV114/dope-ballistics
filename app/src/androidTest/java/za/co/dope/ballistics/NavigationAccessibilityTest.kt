package za.co.dope.ballistics

import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class NavigationAccessibilityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun splashToDashboardAndProfilesNavigationIsAccessible() {
        composeRule.onNodeWithContentDescription("Enter DOPE").assertHeightIsAtLeast(48.dp).performClick()
        composeRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeRule.onNodeWithTag("dope_bottom_navigation").assertIsDisplayed()

        composeRule
            .onNodeWithContentDescription("Profiles", useUnmergedTree = true)
            .performClick()
        composeRule.onNodeWithContentDescription("Open Rifle").assertHeightIsAtLeast(48.dp)
    }
}
