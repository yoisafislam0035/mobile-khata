package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.DecimalFormat

object CalculatorEngine {
    private val decimalFormat = DecimalFormat("#.##")

    /**
     * Evaluates a mathematical expression containing +, -, *, / or ×, ÷
     * Example: "1000 + 500 - 200" -> 1300.0
     */
    fun evaluate(expression: String): Double? {
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")
            .trim()

        if (sanitized.isEmpty()) return null

        return try {
            val tokens = tokenize(sanitized)
            if (tokens.isEmpty()) return null
            evaluateTokens(tokens)
        } catch (e: Exception) {
            null
        }
    }

    fun formatResult(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            decimalFormat.format(value)
        }
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var currentNumber = StringBuilder()

        for (i in expr.indices) {
            val c = expr[i]
            if (c.isDigit() || c == '.') {
                currentNumber.append(c)
            } else if (c in listOf('+', '-', '*', '/')) {
                if (currentNumber.isNotEmpty()) {
                    tokens.add(currentNumber.toString())
                    currentNumber = StringBuilder()
                } else if (c == '-' && (tokens.isEmpty() || tokens.last() in listOf("+", "-", "*", "/"))) {
                    // Leading or unary negative
                    currentNumber.append(c)
                    continue
                }
                tokens.add(c.toString())
            }
        }
        if (currentNumber.isNotEmpty()) {
            tokens.add(currentNumber.toString())
        }
        return tokens
    }

    private fun evaluateTokens(tokens: List<String>): Double {
        // Step 1: Multiply and Divide
        val intermediate = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token == "*" || token == "/") {
                val prev = intermediate.removeAt(intermediate.size - 1).toDouble()
                val next = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 1.0
                val result = if (token == "*") prev * next else {
                    if (next != 0.0) prev / next else 0.0
                }
                intermediate.add(result.toString())
                i += 2
            } else {
                intermediate.add(token)
                i++
            }
        }

        // Step 2: Add and Subtract
        if (intermediate.isEmpty()) return 0.0
        var total = intermediate[0].toDoubleOrNull() ?: 0.0
        var j = 1
        while (j < intermediate.size) {
            val op = intermediate[j]
            val next = intermediate.getOrNull(j + 1)?.toDoubleOrNull() ?: 0.0
            if (op == "+") {
                total += next
            } else if (op == "-") {
                total -= next
            }
            j += 2
        }
        return total
    }
}

/**
 * Full in-app calculator modal for monetary calculations
 */
@Composable
fun MobiKhataCalculatorDialog(
    initialValue: String = "",
    title: String = "In-App Calculator",
    currency: String = "Rs.",
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, formattedText: String) -> Unit
) {
    var expression by remember {
        mutableStateOf(
            if (initialValue.isBlank() || initialValue == "0") "" else initialValue
        )
    }

    val liveResult = remember(expression) {
        if (expression.isBlank()) null
        else CalculatorEngine.evaluate(expression)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Calculator Screen Display
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (expression.isEmpty()) "0" else expression,
                            fontSize = if (expression.length > 15) 20.sp else 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            textAlign = TextAlign.End
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (liveResult != null) "= $currency ${CalculatorEngine.formatResult(liveResult)}" else "$currency 0",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Keypad Grid
                val buttonRows = listOf(
                    listOf("AC", "÷", "×", "DEL"),
                    listOf("7", "8", "9", "-"),
                    listOf("4", "5", "6", "+"),
                    listOf("1", "2", "3", "="),
                    listOf("0", "00", ".", "DONE")
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    buttonRows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { label ->
                                CalculatorKey(
                                    label = label,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        when (label) {
                                            "AC" -> expression = ""
                                            "DEL" -> {
                                                if (expression.isNotEmpty()) {
                                                    expression = expression.dropLast(1)
                                                }
                                            }
                                            "=" -> {
                                                val res = CalculatorEngine.evaluate(expression)
                                                if (res != null) {
                                                    expression = CalculatorEngine.formatResult(res)
                                                }
                                            }
                                            "DONE" -> {
                                                val res = CalculatorEngine.evaluate(expression)
                                                    ?: expression.toDoubleOrNull()
                                                    ?: 0.0
                                                val formatted = CalculatorEngine.formatResult(res)
                                                onConfirm(res, formatted)
                                            }
                                            "+", "-", "×", "÷" -> {
                                                if (expression.isNotEmpty() && !expression.last().isDigit() && expression.last() != '.') {
                                                    expression = expression.dropLast(1) + label
                                                } else if (expression.isNotEmpty()) {
                                                    expression += " $label "
                                                }
                                            }
                                            else -> {
                                                expression += label
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorKey(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isAction = label in listOf("DONE", "=")
    val isOperator = label in listOf("÷", "×", "-", "+")
    val isClearOrDel = label in listOf("AC", "DEL")

    val containerColor = when {
        label == "DONE" -> MaterialTheme.colorScheme.primary
        label == "=" -> MaterialTheme.colorScheme.secondary
        isOperator -> MaterialTheme.colorScheme.primaryContainer
        isClearOrDel -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        label == "DONE" -> MaterialTheme.colorScheme.onPrimary
        label == "=" -> MaterialTheme.colorScheme.onSecondary
        isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
        isClearOrDel -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = if (!isAction && !isOperator && !isClearOrDel) {
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
        } else null,
        tonalElevation = if (isAction) 4.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (label == "DEL") {
                Icon(
                    Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete",
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            } else if (label == "DONE") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = contentColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "OK",
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor,
                        fontSize = 15.sp
                    )
                }
            } else {
                Text(
                    text = label,
                    fontSize = if (isOperator || label == "=") 20.sp else 16.sp,
                    fontWeight = if (isOperator || isAction) FontWeight.ExtraBold else FontWeight.Bold,
                    color = contentColor
                )
            }
        }
    }
}

/**
 * Standard Amount field that opens the in-app Calculator on click or via icon
 */
@Composable
fun MobiKhataAmountInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "Amount (Rs.) *",
    placeholder: String = "0.00",
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var showCalculator by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it) },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = true,
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCalculator = true },
            leadingIcon = {
                Text(
                    "Rs.",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp)
                )
            },
            trailingIcon = {
                IconButton(onClick = { showCalculator = true }) {
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = "Open Calculator",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp)
            )
        }
    }

    if (showCalculator) {
        MobiKhataCalculatorDialog(
            initialValue = value,
            onDismiss = { showCalculator = false },
            onConfirm = { _, formattedText ->
                onValueChange(formattedText)
                showCalculator = false
            }
        )
    }
}
