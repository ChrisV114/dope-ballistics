package za.co.dope.ballistics.ui.screens

import android.content.Intent
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import za.co.dope.ballistics.ui.components.DopeSecondaryButton

/** Displays only an owner-selected local image; no third-party product artwork is bundled. */
@Composable
internal fun ProfilePhotoEditor(
    imageUri: String?,
    illustrationType: EquipmentIllustrationType,
    onImageSelected: (String) -> Unit,
) {
    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri ?: return@rememberLauncherForActivityResult
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            onImageSelected(uri.toString())
        }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (imageUri.isNullOrBlank()) {
            EquipmentIllustration(illustrationType, Modifier.fillMaxWidth())
        } else {
            ProfilePhoto(imageUri)
        }
        DopeSecondaryButton(
            label = if (imageUri.isNullOrBlank()) "Choose my photo" else "Change my photo",
            onClick = { launcher.launch(arrayOf("image/*")) },
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Outlined.AddAPhoto,
        )
    }
}

@Composable
internal fun ProfilePhoto(imageUri: String?) {
    if (imageUri.isNullOrBlank()) return
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                adjustViewBounds = true
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = "Owner-selected profile photo"
            }
        },
        update = { view -> view.setImageURI(imageUri.toUri()) },
        modifier = Modifier.fillMaxWidth().height(132.dp),
    )
}
