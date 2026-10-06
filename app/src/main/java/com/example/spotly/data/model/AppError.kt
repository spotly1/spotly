package com.example.spotly.data.model

// Los repositorios y ViewModels comunican errores; la UI resuelve el texto traducible.
enum class AppError {
    RequiredFields,
    InvalidEmail,
    UsernameTooShort,
    PasswordTooShort,
    PasswordsDoNotMatch,
    Generic,
    UsernameTaken,
    RegistrationFailed,
    WeakPassword,
    InvalidCredentials,
    EmailAlreadyUsed,
    Unauthenticated,
    ProfileNotFound,
    ProfileLoadFailed,
    ProfileUpdateFailed,
    ImageResponseInvalid,
    ImageUploadFailed,
    PostSaveFailed,
    FeedLoadFailed
}
