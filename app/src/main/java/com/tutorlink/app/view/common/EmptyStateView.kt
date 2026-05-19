package com.tutorlink.app.view.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tutorlink.app.ui.theme.MainPurple

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            modifier           = Modifier.size(64.dp),
            tint               = Color.LightGray
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text       = title,
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            color      = Color.DarkGray
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = subtitle,
            style     = MaterialTheme.typography.bodyMedium,
            color     = Color.Gray,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAction,
                colors  = ButtonDefaults.buttonColors(containerColor = MainPurple)
            ) { Text(actionLabel) }
        }
    }
}
