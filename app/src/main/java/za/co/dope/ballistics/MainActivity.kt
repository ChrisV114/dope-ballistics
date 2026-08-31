package za.co.dope.ballistics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import za.co.dope.ballistics.data.ProfileRepository
import za.co.dope.ballistics.data.db.DopeDatabase
import za.co.dope.ballistics.ui.navigation.DopeApp

class MainActivity : ComponentActivity() {
    private val profileRepository by lazy { ProfileRepository(DopeDatabase.getInstance(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Color.Transparent.toArgb()),
        )
        setContent {
            DopeApp(profileRepository = profileRepository)
        }
    }
}
