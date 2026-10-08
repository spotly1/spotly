package com.example.spotly

import android.content.Context
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.spotly.data.repository.*
import com.example.spotly.domain.repository.*
import com.example.spotly.network.FirebaseServices
import com.example.spotly.viewmodel.*
import com.example.spotly.domain.usecase.*

/** Punto único de composición: las features reciben contratos, nunca clases de datos. */
class AppContainer(context: Context) {
    private val services = FirebaseServices()
    private val auth: AuthRepository = FirebaseAuthRepository(services)
    private val profiles: ProfileRepository = FirebaseProfileRepository(services)
    private val posts: PostRepository = FirestorePostRepository(services)
    private val images: ImageRepository = CloudinaryImageRepository(context, services)
    private val locations: LocationRepository = AndroidLocationRepository(context)
    val addressRepository: AddressRepository = AndroidAddressRepository(context)

    private val emailValidator = EmailValidator { android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() }

    val authFactory = viewModelFactory { initializer {
        AuthViewModel(LoginUseCase(auth, emailValidator), RegisterUseCase(auth, emailValidator),
            ObserveAuthenticationUseCase(auth), LogoutUseCase(auth), GetCurrentUserIdUseCase(auth))
    } }
    val profileFactory = viewModelFactory { initializer {
        ProfileViewModel(GetCurrentProfileUseCase(profiles), UpdateProfileUseCase(profiles, images),
            ObserveUserPostsUseCase(posts))
    } }

    val userProfileFactory = viewModelFactory {
        initializer {
            UserProfileViewModel(
                GetUserProfileUseCase(profiles),
                ObserveUserPostsUseCase(posts)
            )
        }
    }
    val feedFactory = viewModelFactory { initializer { FeedViewModel(ObserveFeedUseCase(posts)) } }
    // El caso de publicación conserva el borrador: debe ser una instancia por ViewModel, no global.
    val createPostFactory = viewModelFactory { initializer {
        CreatePostViewModel(PublishPostUseCase(posts, images, auth), GetCurrentLocationUseCase(locations))
    } }
}
