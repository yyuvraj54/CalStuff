package com.dusht.calstuff.ui.screens.onboarding

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dusht.calstuff.BuildConfig
import com.dusht.calstuff.R
import com.dusht.calstuff.auth.PhoneAuthRepository
import com.dusht.calstuff.login.GoogleSignInUtils
import com.dusht.calstuff.ui.common.CalAuraOutlinedTextField
import com.dusht.calstuff.ui.common.CalOrDivider
import com.dusht.calstuff.vm.LoginViewModel
import kotlinx.coroutines.flow.collectLatest

private const val MIN_DIGITS_CONTINUE = 10

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleEvent(LoginEvent.LegacyActivityResult(result))
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                LoginEffect.NavigateAfterLogin -> onLoginSuccess()
            }
        }
    }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        viewModel.handleEvent(LoginEvent.ErrorConsumed)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoginBackgroundImage()

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        }

        LoginContent(
            state = state,
            onGoogleSignInClick = {
                viewModel.handleEvent(LoginEvent.GoogleSignInClicked)
                GoogleSignInUtils.doGoogleSignIn(
                    context = context,
                    scope = scope,
                    launcher = launcher,
                    onSuccess = { viewModel.onGoogleSignInSuccess() },
                    onError = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onPhoneDigitsChange = { viewModel.handleEvent(LoginEvent.PhoneDigitsChanged(it)) },
            onOtpDigitsChange = { viewModel.handleEvent(LoginEvent.OtpDigitsChanged(it)) },
            onPhoneContinue = {
                val activity = context as? Activity
                if (activity != null) {
                    viewModel.handleEvent(LoginEvent.PhoneContinue(activity))
                }
            },
            onVerifyOtp = { viewModel.handleEvent(LoginEvent.PhoneVerifyOtp) }
        )
    }
}

@Composable
private fun ColumnScope.LoginBackgroundImage(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .weight(1f)
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.login_bg),
            contentDescription = "Login Background",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(
                    RoundedCornerShape(
                        bottomStart = 32.dp,
                        bottomEnd = 32.dp
                    )
                ),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onGoogleSignInClick: () -> Unit,
    onPhoneDigitsChange: (String) -> Unit,
    onOtpDigitsChange: (String) -> Unit,
    onPhoneContinue: () -> Unit,
    onVerifyOtp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasPhoneInput = state.phoneDigits.any { it.isDigit() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(top = 48.dp, bottom = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        WelcomeText()
        DescriptionText()

        if (BuildConfig.IS_STAGING) {
            Text(
                text = stringResource(
                    R.string.login_staging_hint,
                    PhoneAuthRepository.STAGING_TEST_PHONE_DIGITS,
                    PhoneAuthRepository.STAGING_TEST_OTP
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        PhoneNumberBlock(
            state = state,
            onPhoneDigitsChange = onPhoneDigitsChange,
            onOtpDigitsChange = onOtpDigitsChange,
            onPhoneContinue = onPhoneContinue,
            onVerifyOtp = onVerifyOtp
        )

        AnimatedVisibility(
            visible = !hasPhoneInput,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CalOrDivider(text = stringResource(R.string.login_or))
                GoogleSignInButton(onClick = onGoogleSignInClick)
            }
        }
    }
}

@Composable
private fun PhoneNumberBlock(
    state: LoginUiState,
    onPhoneDigitsChange: (String) -> Unit,
    onOtpDigitsChange: (String) -> Unit,
    onPhoneContinue: () -> Unit,
    onVerifyOtp: () -> Unit
) {
    val phoneDigits = state.phoneDigits.filter { it.isDigit() }
    val canSendCode = phoneDigits.length >= MIN_DIGITS_CONTINUE
    val otpReady = state.otpDigits.length == 6

    CalAuraOutlinedTextField(
        value = state.phoneDigits,
        onValueChange = onPhoneDigitsChange,
        label = stringResource(R.string.login_phone_label),
        placeholder = stringResource(R.string.login_phone_placeholder),
        enabled = state.phoneLoginPhase == PhoneLoginPhase.PhoneEntry,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
    )

    AnimatedVisibility(
        visible = state.phoneLoginPhase == PhoneLoginPhase.PhoneEntry && canSendCode
    ) {
        Button(
            onClick = onPhoneContinue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5C6BC0),
                contentColor = Color.White
            )
        ) {
            Text(
                text = stringResource(R.string.login_phone_continue),
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    AnimatedVisibility(visible = state.phoneLoginPhase == PhoneLoginPhase.OtpEntry) {
        Column(
            modifier = Modifier.padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.login_otp_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CalAuraOutlinedTextField(
                value = state.otpDigits,
                onValueChange = onOtpDigitsChange,
                label = stringResource(R.string.login_otp_label),
                placeholder = stringResource(R.string.login_otp_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            AnimatedVisibility(visible = otpReady) {
                Button(
                    onClick = onVerifyOtp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF26A69A),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = stringResource(R.string.login_verify_otp),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeText(
    modifier: Modifier = Modifier,
    text: String = "Welcome Back"
) {
    Text(
        text = text,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
private fun DescriptionText(
    modifier: Modifier = Modifier,
    text: String = "Kickstart your calorie diet with the best plan to reach your goals!"
) {
    Text(
        text = text,
        fontSize = 14.sp,
        color = Color.Gray,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

@Composable
private fun GoogleSignInButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    buttonColor: Color = Color(0xFFFF6B6B),
    buttonText: String = "Sign in with Google"
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.google_btn_icon),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = buttonText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}
