package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DeviceBrand
import com.example.data.model.DeviceCatalog
import com.example.data.model.DeviceTemplate
import com.example.data.model.PerformanceProfile
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianCardBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceVariant
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCloudPhonesSheet(
    totalFleetCount: Int,
    onDismiss: () -> Unit,
    onCreateBatch: (
        template: DeviceTemplate,
        batchCount: Int,
        namePrefix: String,
        ramGb: Int,
        performanceMode: PerformanceProfile,
        openInMiniDesktop: Boolean
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedBrand by remember { mutableStateOf(DeviceBrand.INFINIX) }
    val brandTemplates = remember(selectedBrand) { DeviceCatalog.forBrand(selectedBrand) }
    var selectedTemplate by remember(selectedBrand) { mutableStateOf(brandTemplates.first()) }
    var batchCount by remember { mutableIntStateOf(2) }
    var selectedRamGb by remember { mutableIntStateOf(selectedTemplate.defaultRamGb) }
    var selectedPerf by remember { mutableStateOf(PerformanceProfile.SMOOTH_120) }
    var namePrefix by remember(selectedBrand) {
        mutableStateOf("${selectedBrand.displayName} Cloud")
    }
    var openInMiniDesktop by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Provision Cloud Phones",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                    Text(
                        text = "Unlimited Fleet ($totalFleetCount active) • Android 14 Update",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Brand Selector: Infinix 14 vs Realme 14
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "1. Device OS (Android 14 Update)",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyberCyan
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DeviceBrand.entries.forEach { brand ->
                        val isSelected = brand == selectedBrand
                        Surface(
                            color = if (isSelected) brand.accentColor.copy(alpha = 0.18f) else ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) brand.accentColor else ObsidianCardBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedBrand = brand
                                    val first = DeviceCatalog.forBrand(brand).first()
                                    selectedTemplate = first
                                    selectedRamGb = first.defaultRamGb
                                    namePrefix = "${brand.displayName} Cloud"
                                }
                                .testTag("brand_${brand.id.lowercase()}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.PhoneAndroid,
                                        contentDescription = null,
                                        tint = brand.accentColor
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = brand.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${brand.osSkin} • ${brand.androidVersion}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = brand.accentColor
                                )
                            }
                        }
                    }
                }
            }

            // Model Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "2. Select ${selectedBrand.displayName} 14 Model",
                    style = MaterialTheme.typography.labelLarge,
                    color = CyberCyan
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(brandTemplates, key = { it.id }) { model ->
                        val selected = model.id == selectedTemplate.id
                        Surface(
                            color = if (selected) ObsidianBg else ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                width = if (selected) 2.dp else 1.dp,
                                color = if (selected) selectedBrand.accentColor else ObsidianCardBorder
                            ),
                            modifier = Modifier
                                .width(196.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedTemplate = model
                                    selectedRamGb = model.defaultRamGb
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = model.modelName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = selectedBrand.accentColor
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = model.chipset,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = selectedBrand.accentColor
                                )
                                Text(
                                    text = model.displaySpec,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Min 1 Max 5 Batch Selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "3. Cloud Phones to Create",
                        style = MaterialTheme.typography.labelLarge,
                        color = CyberCyan
                    )
                    Text(
                        text = "Min 1 • Max 5 per batch",
                        style = MaterialTheme.typography.labelMedium,
                        color = selectedBrand.accentColor
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (DeviceCatalog.MIN_BATCH_COUNT..DeviceCatalog.MAX_BATCH_COUNT).forEach { count ->
                        val active = count == batchCount
                        Surface(
                            color = if (active) selectedBrand.accentColor else ObsidianSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                1.dp,
                                if (active) selectedBrand.accentColor else ObsidianCardBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { batchCount = count }
                                .testTag("batch_count_$count")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) ObsidianBg else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Smoothness & RAM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("RAM Profile", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(8, 12, 16).forEach { ram ->
                            val selected = selectedRamGb == ram
                            Surface(
                                color = if (selected) CyberCyan else ObsidianSurfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedRamGb = ram }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${ram}G",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (selected) ObsidianBg else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Smooth Engine", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        PerformanceProfile.entries.forEach { profile ->
                            val selected = selectedPerf == profile
                            Surface(
                                color = if (selected) selectedBrand.accentColor else ObsidianSurfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedPerf = profile }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${profile.refreshRateHz}Hz",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) ObsidianBg else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = namePrefix,
                onValueChange = { namePrefix = it },
                label = { Text("Device Label Prefix") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Surface(
                color = ObsidianSurfaceVariant,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Launch in Mini Desktop",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            text = "Opens up to 5 floating mini windows smoothly",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = openInMiniDesktop,
                        onCheckedChange = { openInMiniDesktop = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ObsidianBg,
                            checkedTrackColor = selectedBrand.accentColor
                        )
                    )
                }
            }

            Button(
                onClick = {
                    onCreateBatch(
                        selectedTemplate,
                        batchCount,
                        namePrefix,
                        selectedRamGb,
                        selectedPerf,
                        openInMiniDesktop
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = selectedBrand.accentColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("confirm_create_phones_button")
            ) {
                Icon(Icons.Filled.AddCircle, contentDescription = null, tint = ObsidianBg)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Create $batchCount ${selectedBrand.displayName} 14 Phone${if (batchCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ObsidianBg
                )
            }
        }
    }
}
