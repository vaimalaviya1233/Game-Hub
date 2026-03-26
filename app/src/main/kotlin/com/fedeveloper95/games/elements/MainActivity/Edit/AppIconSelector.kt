package com.fedeveloper95.games.elements.MainActivity.Edit

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.fedeveloper95.games.elements.ui.AppIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppIconSelector(
    onDismiss: () -> Unit,
    onAppSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    val sheetState = rememberModalBottomSheetState()

    val installedApps = remember {
        val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        packageManager.queryIntentActivities(intent, 0).mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            val name = resolveInfo.loadLabel(packageManager).toString()
            if (pkg != null) Pair(pkg, name) else null
        }.sortedBy { it.second }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        LazyColumn {
            items(installedApps) { (pkg, name) ->
                ListItem(
                    headlineContent = { Text(name) },
                    leadingContent = {
                        AppIcon(
                            packageName = pkg,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                        )
                    },
                    modifier = Modifier.clickable { onAppSelected(pkg) },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                )
            }
        }
    }
}