package com.example.spotly.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.spotly.core.ui.R
import com.example.spotly.domain.model.AppError

@Composable
fun AppError.localizedMessage(): String = stringResource(
    when (this) {
        AppError.RequiredFields -> R.string.error_required_fields
        AppError.InvalidEmail -> R.string.error_invalid_email
        AppError.UsernameTooShort -> R.string.error_username_too_short
        AppError.PasswordTooShort -> R.string.error_password_too_short
        AppError.PasswordsDoNotMatch -> R.string.error_passwords_do_not_match
        AppError.Generic -> R.string.error_generic
        AppError.UsernameTaken -> R.string.error_username_taken
        AppError.RegistrationFailed -> R.string.error_registration_failed
        AppError.WeakPassword -> R.string.error_weak_password
        AppError.InvalidCredentials -> R.string.error_invalid_credentials
        AppError.EmailAlreadyUsed -> R.string.error_email_already_used
        AppError.Unauthenticated -> R.string.error_unauthenticated
        AppError.ProfileNotFound -> R.string.error_profile_not_found
        AppError.ProfileLoadFailed -> R.string.profile_load_error
        AppError.ProfileUpdateFailed -> R.string.error_profile_update_failed
        AppError.ImageResponseInvalid -> R.string.error_image_response_invalid
        AppError.ImageUploadFailed -> R.string.error_image_upload_failed
        AppError.PostSaveFailed -> R.string.error_post_save
        AppError.FeedLoadFailed -> R.string.error_feed_load
        AppError.LocationUnavailable -> R.string.location_failed
        AppError.AddressUnavailable -> R.string.location_selected
    }
)
