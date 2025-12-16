package org.mccmarion.radio.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.mccmarion.radio.BuildConfig
import org.mccmarion.radio.Config
import org.mccmarion.radio.ui.theme.Primary
import org.mccmarion.radio.ui.theme.Secondary
import java.util.Calendar

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun openEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        context.startActivity(intent)
    }

    fun openPhone(phone: String) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
        context.startActivity(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            "About ${Config.STATION_FULL_NAME}",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mission Section
        SectionCard(title = "Our Mission") {
            Text(
                Config.ABOUT_TEXT,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contact Section
        SectionCard(title = "Contact Us") {
            ContactRow(
                icon = Icons.Default.Email,
                title = "Email",
                value = Config.CONTACT_EMAIL,
                onClick = { openEmail(Config.CONTACT_EMAIL) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ContactRow(
                icon = Icons.Default.Phone,
                title = "Phone",
                value = Config.CONTACT_PHONE,
                onClick = { openPhone(Config.CONTACT_PHONE) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ContactRow(
                icon = Icons.Default.Language,
                title = "Website",
                value = "mccmarion.org",
                onClick = { openUrl(Config.WEBSITE_URL) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Social Links
        SectionCard(title = "Connect With Us") {
            Button(
                onClick = { openUrl(Config.FACEBOOK_URL) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Facebook")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { openUrl(Config.YOUTUBE_URL) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("YouTube")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Donate Section
        SectionCard(title = "Support Our Ministry") {
            Text(
                "Your generous donations help us continue spreading the Gospel through music. Every contribution makes a difference!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { openUrl(Config.DONATE_URL) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Secondary)
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Donate")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LPFM Coming Soon
        LpfmSection()

        Spacer(modifier = Modifier.height(24.dp))

        // App Info
        Text(
            "MCC Radio Android App v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "© ${Calendar.getInstance().get(Calendar.YEAR)} My Community Church",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun ContactRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Primary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LpfmSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Radio,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    "Coming Soon!",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "We're excited to announce that we have a construction permit for an LPFM radio station. Stay tuned for updates on our broadcast launch!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
