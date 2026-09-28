package com.example.ui.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.GoogleAuthResult
import com.example.ui.theme.UdhaarGreen
import com.example.ui.viewmodel.MobiKhataViewModel
import com.example.util.AppLanguage
import kotlinx.coroutines.delay

data class CountryCode(val name: String, val code: String, val flag: String)

val SUPPORTED_COUNTRIES = listOf(
    CountryCode("Pakistan", "+92", "🇵🇰"),
    CountryCode("Saudi Arabia", "+966", "🇸🇦"),
    CountryCode("United Arab Emirates", "+971", "🇦🇪"),
    CountryCode("United Kingdom", "+44", "🇬🇧"),
    CountryCode("United States", "+1", "🇺🇸"),
    CountryCode("Canada", "+1", "🇨🇦"),
    CountryCode("Qatar", "+974", "🇶🇦"),
    CountryCode("Oman", "+968", "🇴🇲"),
    CountryCode("Kuwait", "+965", "🇰🇼"),
    CountryCode("Bahrain", "+973", "🇧🇭"),
    CountryCode("India", "+91", "🇮🇳")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: MobiKhataViewModel) {
    val context = LocalContext.current
    val activity = context as? Activity
    val currentLang by viewModel.language.collectAsState()

    var selectedAuthTab by remember { mutableStateOf(1) } // Default to Phone OTP (primary for Khata)

    // Email state
    var isSignUp by remember { mutableStateOf(false) }
    var emailName by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var emailPhone by remember { mutableStateOf("") }
    var emailPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Phone state
    var selectedCountry by remember { mutableStateOf(SUPPORTED_COUNTRIES[0]) } // Pakistan +92
    var showCountryPicker by remember { mutableStateOf(false) }
    var phoneNumber by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var enteredOtp by remember { mutableStateOf("") }
    var phoneOwnerName by remember { mutableStateOf("") }
    var otpCountdown by remember { mutableStateOf(60) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var isPhoneSubmitting by remember { mutableStateOf(false) }

    // Email verification state
    val emailVerificationState by viewModel.emailVerificationState.collectAsState()
    var isCheckingVerification by remember { mutableStateOf(false) }
    var verificationStatusMessage by remember { mutableStateOf<String?>(null) }
    var isVerificationStatusError by remember { mutableStateOf(false) }

    // Google Sign-In
    var isGoogleSigningIn by remember { mutableStateOf(false) }
    var googleSignInErrorMessage by remember { mutableStateOf<String?>(null) }

    // Forgot password dialog
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }

    // OTP Countdown timer
    LaunchedEffect(isOtpSent, otpCountdown) {
        if (isOtpSent && otpCountdown > 0) {
            delay(1000)
            otpCountdown -= 1
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Language selector row at the top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable {
                        viewModel.setLanguage(
                            if (currentLang == AppLanguage.URDU) AppLanguage.ENGLISH else AppLanguage.URDU
                        )
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (currentLang == AppLanguage.URDU) "English" else "اردو",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // App Logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Book,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "Mobi Khata",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Digital Khata, POS Billing & Cloud Shop Management",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray
            )

            Spacer(Modifier.height(20.dp))

            if (emailVerificationState != null) {
                // ================= EMAIL VERIFICATION PENDING CARD =================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.MarkEmailRead,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            "Verify Your Email Address",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            "A real verification email has been sent to:",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = emailVerificationState?.email ?: "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                            )
                        }

                        Text(
                            "Please click the verification link in your email to activate your account. You will only be allowed into the workspace once your email is verified.\n\n(Tip: If you do not see the email, please check your Spam or Junk folder.)",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )

                        if (verificationStatusMessage != null) {
                            Surface(
                                color = if (isVerificationStatusError) MaterialTheme.colorScheme.errorContainer else Color(0xFFDCFCE7),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = verificationStatusMessage ?: "",
                                    color = if (isVerificationStatusError) MaterialTheme.colorScheme.error else Color(0xFF166534),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                isCheckingVerification = true
                                verificationStatusMessage = null
                                viewModel.checkEmailVerificationStatus { isVerified, err ->
                                    isCheckingVerification = false
                                    if (isVerified) {
                                        Toast.makeText(context, "Email verified! Opening your workspace...", Toast.LENGTH_SHORT).show()
                                    } else {
                                        isVerificationStatusError = true
                                        verificationStatusMessage = err ?: "Email not verified yet. Please check your inbox and click the link."
                                    }
                                }
                            },
                            enabled = !isCheckingVerification,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isCheckingVerification) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("I Have Verified My Email (Check Status)", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.resendVerificationEmail { success, msg ->
                                    isVerificationStatusError = !success
                                    verificationStatusMessage = msg
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Resend Verification Email")
                        }

                        TextButton(
                            onClick = {
                                viewModel.cancelEmailVerification()
                            }
                        ) {
                            Text("Use a Different Account / Back to Sign In", fontSize = 12.sp)
                        }
                    }
                }
            } else {
            // Google Sign-In Button (Modern Credential Manager & Firebase Auth)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isGoogleSigningIn) {
                        isGoogleSigningIn = true
                        googleSignInErrorMessage = null
                        viewModel.signInWithGoogleViaCredentialManager(context) { result ->
                            isGoogleSigningIn = false
                            when (result) {
                                is GoogleAuthResult.Success -> {
                                    // Successfully authenticated!
                                }
                                is GoogleAuthResult.Cancelled -> {
                                    // User dismissed
                                }
                                is GoogleAuthResult.Error -> {
                                    googleSignInErrorMessage = result.message
                                }
                            }
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isGoogleSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF4285F4)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Connecting to Google...",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                    } else {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = Color.White
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "G",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = Color(0xFF4285F4)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Continue with Google",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            Text(
                                "Instant Google Sign-In",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            if (googleSignInErrorMessage != null) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = googleSignInErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Divider "OR"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
                Text(
                    "  OR CONTINUE WITH  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.SemiBold
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
            }

            Spacer(Modifier.height(16.dp))

            // Tabs for Phone vs Email
            TabRow(
                selectedTabIndex = selectedAuthTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedAuthTab == 1,
                    onClick = {
                        selectedAuthTab = 1
                        phoneError = null
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Mobile (SMS OTP)", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedAuthTab == 0,
                    onClick = {
                        selectedAuthTab = 0
                        emailError = null
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Email", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Spacer(Modifier.height(14.dp))

            // Main Auth Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedAuthTab == 1) {
                        // =================== REAL PHONE OTP AUTH ===================
                        Text(
                            if (!isOtpSent) "Sign In with Mobile Number" else "Verify SMS Code",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (phoneError != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = phoneError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        if (!isOtpSent) {
                            // Step 1: Input Phone Number
                            Text(
                                "An authentic SMS verification code will be sent to your phone number via Firebase.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Country Code Selector
                                Surface(
                                    modifier = Modifier
                                        .weight(0.38f)
                                        .height(56.dp)
                                        .clickable { showCountryPicker = true },
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB)),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${selectedCountry.flag} ${selectedCountry.code}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }

                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text("Mobile Number") },
                                    placeholder = { Text("3001234567") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    modifier = Modifier.weight(0.62f)
                                )
                            }

                            Button(
                                onClick = {
                                    val clean = phoneNumber.filter { it.isDigit() }
                                    if (clean.length < 7) {
                                        phoneError = "Please enter a valid mobile number."
                                        return@Button
                                    }
                                    val fullPhone = "${selectedCountry.code}$clean"
                                    isPhoneSubmitting = true
                                    phoneError = null

                                    if (activity != null) {
                                        viewModel.sendPhoneVerificationCode(
                                            activity = activity,
                                            phoneNumber = fullPhone,
                                            onCodeSent = {
                                                isPhoneSubmitting = false
                                                isOtpSent = true
                                                otpCountdown = 60
                                                Toast.makeText(context, "Verification code sent to $fullPhone", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                isPhoneSubmitting = false
                                                phoneError = err
                                            }
                                        )
                                    } else {
                                        isPhoneSubmitting = false
                                        phoneError = "Could not initialize phone verification."
                                    }
                                },
                                enabled = !isPhoneSubmitting,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (isPhoneSubmitting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Send Verification Code (SMS)", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Step 2: Input Real SMS OTP
                            val fullPhone = "${selectedCountry.code}${phoneNumber.filter { it.isDigit() }}"

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Code sent to $fullPhone", fontSize = 12.sp, color = Color.DarkGray)
                                TextButton(onClick = { isOtpSent = false }) {
                                    Text("Change", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            OutlinedTextField(
                                value = enteredOtp,
                                onValueChange = { enteredOtp = it.filter { ch -> ch.isDigit() }.take(6) },
                                label = { Text("6-Digit SMS Code *") },
                                placeholder = { Text("Enter SMS OTP") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) }
                            )

                            OutlinedTextField(
                                value = phoneOwnerName,
                                onValueChange = { phoneOwnerName = it },
                                label = { Text("Store Owner Name (Optional)") },
                                placeholder = { Text("e.g. Usman Ali") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (otpCountdown > 0) {
                                    Text("Resend code in ${otpCountdown}s", fontSize = 12.sp, color = Color.Gray)
                                } else {
                                    TextButton(onClick = {
                                        if (activity != null) {
                                            viewModel.sendPhoneVerificationCode(
                                                activity = activity,
                                                phoneNumber = fullPhone,
                                                onCodeSent = {
                                                    otpCountdown = 60
                                                    Toast.makeText(context, "New code sent via SMS", Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { phoneError = it }
                                            )
                                        }
                                    }) {
                                        Text("Resend SMS Code", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (enteredOtp.length < 6) {
                                        phoneError = "Please enter the 6-digit verification code received by SMS."
                                        return@Button
                                    }
                                    isPhoneSubmitting = true
                                    phoneError = null

                                    viewModel.verifyPhoneOtpWithFirebase(
                                        enteredOtp = enteredOtp,
                                        phone = fullPhone,
                                        ownerName = phoneOwnerName
                                    ) { success, err ->
                                        isPhoneSubmitting = false
                                        if (!success) {
                                            phoneError = err ?: "Invalid code. Please check SMS and try again."
                                        }
                                    }
                                },
                                enabled = !isPhoneSubmitting && enteredOtp.length >= 6,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                if (isPhoneSubmitting) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Verify & Open Khata", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    } else {
                        // =================== EMAIL AUTH ===================
                        Text(
                            if (isSignUp) "Create New Account" else "Sign In with Email",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        if (emailError != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = emailError ?: "",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        if (isSignUp) {
                            OutlinedTextField(
                                value = emailName,
                                onValueChange = { emailName = it },
                                label = { Text("Full Name / Proprietor Name *") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                            )
                        }

                        OutlinedTextField(
                            value = emailAddress,
                            onValueChange = { emailAddress = it },
                            label = { Text("Email Address *") },
                            placeholder = { Text("store@example.com") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) }
                        )

                        if (isSignUp) {
                            OutlinedTextField(
                                value = emailPhone,
                                onValueChange = { emailPhone = it },
                                label = { Text("Mobile Phone Number") },
                                placeholder = { Text("03001234567") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                            )
                        }

                        OutlinedTextField(
                            value = emailPassword,
                            onValueChange = { emailPassword = it },
                            label = { Text("Password *") },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            }
                        )

                        if (!isSignUp) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    forgotPasswordEmail = emailAddress
                                    showForgotPasswordDialog = true
                                }) {
                                    Text("Forgot Password?", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                emailError = null
                                if (emailAddress.isBlank()) {
                                    emailError = "Please enter your email address"
                                    return@Button
                                }
                                if (!emailAddress.contains("@") || !emailAddress.contains(".")) {
                                    emailError = "Please enter a valid email address"
                                    return@Button
                                }
                                if (emailPassword.isBlank()) {
                                    emailError = "Please enter your password"
                                    return@Button
                                }
                                isSubmitting = true
                                if (isSignUp) {
                                    viewModel.registerWithEmail(emailName, emailAddress, emailPhone, emailPassword) { success, err ->
                                        isSubmitting = false
                                        if (!success) emailError = err
                                    }
                                } else {
                                    viewModel.loginWithEmail(emailAddress, emailPassword) { success, err ->
                                        isSubmitting = false
                                        if (!success) emailError = err
                                    }
                                }
                            },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(if (isSignUp) "Register & Open Khata" else "Sign In to Khata", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }

                        TextButton(
                            onClick = {
                                isSignUp = !isSignUp
                                emailError = null
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(if (isSignUp) "Already have an account? Sign In" else "New store owner? Create Account")
                        }
                    }
                }
            }
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    // Country Code Picker Dialog
    if (showCountryPicker) {
        AlertDialog(
            onDismissRequest = { showCountryPicker = false },
            title = { Text("Select Country") },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(SUPPORTED_COUNTRIES) { c ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCountry = c
                                    showCountryPicker = false
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${c.flag}  ${c.name}", fontWeight = FontWeight.Medium)
                                Text(c.code, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCountryPicker = false }) { Text("Close") }
            }
        )
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        var msg by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter your email address to receive password reset link:", fontSize = 13.sp, color = Color.Gray)
                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (msg != null) {
                        Text(msg ?: "", color = UdhaarGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (forgotPasswordEmail.isNotBlank()) {
                            msg = viewModel.resetPassword(forgotPasswordEmail)
                        }
                    },
                    enabled = forgotPasswordEmail.isNotBlank()
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) { Text("Close") }
            }
        )
    }
}
