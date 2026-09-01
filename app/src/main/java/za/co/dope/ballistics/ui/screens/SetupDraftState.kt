package za.co.dope.ballistics.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class SetupDraftState {
    var rifleId by mutableStateOf<String?>(null)
    var ammunitionId by mutableStateOf<String?>(null)
    var scopeId by mutableStateOf<String?>(null)
}
