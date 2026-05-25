package com.example.proyectopruebaappmusia1.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyectopruebaappmusia1.ui.theme.AccentGreen
import com.example.proyectopruebaappmusia1.ui.theme.CardGreenBg
import com.example.proyectopruebaappmusia1.ui.theme.DarkGreenBg
import com.example.proyectopruebaappmusia1.ui.theme.PrimaryText
import com.example.proyectopruebaappmusia1.ui.theme.SecondaryText

@Composable
fun SettingsSectionTitle(text: String) {
    Text(text, color = PrimaryText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun SettingsChoiceCard(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardGreenBg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SettingIconBubble(icon)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(title, color = PrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = SecondaryText, fontSize = 13.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    Button(
                        onClick = { onOptionSelected(option) },
                        modifier = Modifier.weight(1f).height(42.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) AccentGreen else DarkGreenBg,
                            contentColor = if (isSelected) DarkGreenBg else PrimaryText
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(option, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsActionRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    actionText: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardGreenBg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingIconBubble(icon)
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(title, color = PrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = SecondaryText, fontSize = 13.sp)
            }
            OutlinedButton(
                onClick = onClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(actionText)
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardGreenBg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingIconBubble(icon)
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(title, color = PrimaryText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = SecondaryText, fontSize = 13.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DarkGreenBg,
                    checkedTrackColor = AccentGreen,
                    uncheckedThumbColor = SecondaryText,
                    uncheckedTrackColor = PrimaryText.copy(alpha = 0.14f)
                )
            )
        }
    }
}

@Composable
fun SettingIconBubble(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = AccentGreen.copy(alpha = 0.14f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}
