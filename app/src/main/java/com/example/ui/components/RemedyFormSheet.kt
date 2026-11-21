package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.RemedyEntity
import com.example.data.model.RemedyConstants
import com.example.data.model.RemedyForm
import com.example.data.model.formatFullDate
import com.example.ui.theme.MonoFontFamily

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RemedyFormSheet(
    existingRemedy: RemedyEntity?,
    onDismiss: () -> Unit,
    onSave: (RemedyEntity) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember(existingRemedy) { mutableStateOf(existingRemedy?.name.orEmpty()) }
    var commonName by remember(existingRemedy) { mutableStateOf(existingRemedy?.commonName.orEmpty()) }
    var potency by remember(existingRemedy) { mutableStateOf(existingRemedy?.potency ?: "30C") }
    var selectedForm by remember(existingRemedy) {
        mutableStateOf(existingRemedy?.remedyForm ?: RemedyForm.DILUTION)
    }
    var brand by remember(existingRemedy) { mutableStateOf(existingRemedy?.brand.orEmpty()) }
    var bottleSize by remember(existingRemedy) {
        mutableStateOf(existingRemedy?.bottleSize ?: RemedyForm.DILUTION.defaultSize)
    }
    var quantity by remember(existingRemedy) { mutableIntStateOf(existingRemedy?.quantity ?: 1) }
    var minStockLevel by remember(existingRemedy) { mutableIntStateOf(existingRemedy?.minStockLevel ?: 2) }
    var batchNumber by remember(existingRemedy) { mutableStateOf(existingRemedy?.batchNumber.orEmpty()) }
    var expiryMillis by remember(existingRemedy) { mutableStateOf(existingRemedy?.expiryDateMillis) }
    var shelfLocation by remember(existingRemedy) { mutableStateOf(existingRemedy?.shelfLocation.orEmpty()) }
    var notes by remember(existingRemedy) { mutableStateOf(existingRemedy?.notes.orEmpty()) }

    var nameError by remember { mutableStateOf(false) }
    var potencyError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (existingRemedy == null) {
                        stringResource(R.string.add_remedy)
                    } else {
                        stringResource(R.string.edit_remedy)
                    },
                    style = MaterialTheme.typography.headlineSmall,
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.cancel),
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = false
                },
                label = { Text("Remedy Name *") },
                placeholder = { Text("e.g., Arnica Montana") },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text("Please enter a remedy name") }
                } else null,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("remedy_name_input"),
            )

            // Quick-fill classic remedy suggestions when adding new
            if (existingRemedy == null && name.length < 3) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Quick Pick Classic Remedies",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        RemedyConstants.classicRemedies.take(6).forEach { (latin, common) ->
                            SuggestionChip(
                                onClick = {
                                    name = latin
                                    commonName = common
                                    nameError = false
                                },
                                label = { Text(latin) },
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = commonName,
                onValueChange = { commonName = it },
                label = { Text("Common / Source Name (optional)") },
                placeholder = { Text("e.g., Mountain Daisy") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // Potency selector + custom field
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Potency *",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    RemedyConstants.commonPotencies.forEach { preset ->
                        FilterChip(
                            selected = potency.equals(preset, ignoreCase = true),
                            onClick = {
                                potency = preset
                                potencyError = false
                            },
                            label = {
                                Text(
                                    text = preset,
                                    fontFamily = MonoFontFamily,
                                )
                            },
                            modifier = Modifier.testTag("potency_chip_$preset"),
                        )
                    }
                }
                OutlinedTextField(
                    value = potency,
                    onValueChange = {
                        potency = it
                        potencyError = false
                    },
                    label = { Text("Custom Potency") },
                    isError = potencyError,
                    supportingText = if (potencyError) {
                        { Text("Please enter a potency (e.g., 30C, 200C, 1M, Q)") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("potency_input"),
                )
            }

            // Form selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Preparation Form",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    RemedyForm.entries.forEach { form ->
                        FilterChip(
                            selected = selectedForm == form,
                            onClick = {
                                selectedForm = form
                                if (existingRemedy == null) {
                                    bottleSize = form.defaultSize
                                }
                            },
                            label = { Text(form.label) },
                            modifier = Modifier.testTag("form_chip_${form.name}"),
                        )
                    }
                }
            }

            // Quantity & Reorder Threshold steppers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StepperBox(
                    label = "Stock Qty",
                    value = quantity,
                    onDecrement = { if (quantity > 0) quantity-- },
                    onIncrement = { quantity++ },
                    tagPrefix = "form_qty",
                    modifier = Modifier.weight(1f),
                )
                StepperBox(
                    label = "Reorder Alert ≤",
                    value = minStockLevel,
                    onDecrement = { if (minStockLevel > 0) minStockLevel-- },
                    onIncrement = { minStockLevel++ },
                    tagPrefix = "form_min",
                    modifier = Modifier.weight(1f),
                )
            }

            // Brand & Bottle size
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Manufacturer / Brand") },
                    placeholder = { Text("e.g., Dr. Reckeweg, SBL, Schwabe") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    RemedyConstants.commonBrands.forEach { b ->
                        SuggestionChip(
                            onClick = { brand = b },
                            label = { Text(b) },
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = bottleSize,
                    onValueChange = { bottleSize = it },
                    label = { Text("Bottle / Pack Size") },
                    placeholder = { Text("e.g., 30 ml, 100 ml, 25 g") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    RemedyConstants.commonSizes.take(6).forEach { sz ->
                        SuggestionChip(
                            onClick = { bottleSize = sz },
                            label = { Text(sz) },
                        )
                    }
                }
            }

            // Batch & Expiry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = batchNumber,
                    onValueChange = { batchNumber = it },
                    label = { Text("Batch No.") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = shelfLocation,
                    onValueChange = { shelfLocation = it },
                    label = { Text("Rack / Shelf") },
                    placeholder = { Text("e.g., Rack A-2") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("expiry_date_button"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = expiryMillis?.let { "Expires: ${formatFullDate(it)}" }
                            ?: "Set Expiry Date",
                    )
                }
                if (expiryMillis != null) {
                    TextButton(
                        onClick = { expiryMillis = null },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Text("Clear")
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Clinical / Storage Notes") },
                placeholder = { Text("e.g., Keep away from camphor & sunlight") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    val cleanName = name.trim()
                    val cleanPotency = potency.trim()
                    nameError = cleanName.isEmpty()
                    potencyError = cleanPotency.isEmpty()
                    if (!nameError && !potencyError) {
                        onSave(
                            RemedyEntity(
                                id = existingRemedy?.id ?: 0L,
                                name = cleanName,
                                commonName = commonName.trim(),
                                potency = cleanPotency.uppercase(),
                                form = selectedForm.name,
                                brand = brand.trim(),
                                bottleSize = bottleSize.trim().ifBlank { selectedForm.defaultSize },
                                quantity = quantity,
                                minStockLevel = minStockLevel,
                                batchNumber = batchNumber.trim(),
                                expiryDateMillis = expiryMillis,
                                shelfLocation = shelfLocation.trim(),
                                notes = notes.trim(),
                                updatedAt = System.currentTimeMillis(),
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_remedy_button"),
            ) {
                Text(
                    text = if (existingRemedy == null) "Add to Stock" else "Save Changes",
                    style = MaterialTheme.typography.titleSmall,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = expiryMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        expiryMillis = pickerState.selectedDateMillis
                        showDatePicker = false
                    },
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun StepperBox(
    label: String,
    value: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    tagPrefix: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        androidx.compose.material3.Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onDecrement,
                    enabled = value > 0,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("${tagPrefix}_dec"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Decrease $label",
                    )
                }
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = MonoFontFamily,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                IconButton(
                    onClick = onIncrement,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("${tagPrefix}_inc"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Increase $label",
                    )
                }
            }
        }
    }
}
