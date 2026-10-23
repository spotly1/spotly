package com.example.spotly

import android.content.Context
import androidx.lifecycle.createSavedStateHandle
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
    val connection: ConnectionRepository = AndroidConnectionRepository(context)
    private val database = com.example.spotly.database.SpotlyDatabase.create(context)
    private val auth: AuthRepository = FirebaseAuthRepository(services)
    private val profiles: ProfileRepository = FirebaseProfileRepository(services)
    private val posts: PostRepository = FirestorePostRepository(services, connection)
    private val follows: FollowRepository = FirestoreFollowRepository(services, connection)
    private val feed: FeedRepository = OfflineFeedRepository(posts, follows, auth, connection, database.feedDao())
    private val likes: LikeRepository = FirestoreLikeRepository(services, connection)
    private val notifications: NotificationRepository = FirestoreNotificationRepository(services, connection)
    val localNotifications = com.example.spotly.notifications.LocalNotificationController(context,
        ObserveLocalNotificationsUseCase(notifications), GetCurrentUserIdUseCase(auth),
        MarkNotificationReadUseCase(notifications))
    private val postDetails: PostDetailRepository = CachedPostDetailRepository(
        FirestorePostDetailRepository(services), auth, connection, database.feedDao())
    private val images: ImageRepository = CloudinaryImageRepository(context, services, connection)
    private val locations: LocationRepository = AndroidLocationRepository(context)
    val addressRepository: AddressRepository = AndroidAddressRepository(context)

    private val emailValidator = EmailValidator { android.util.Patterns.EMAIL_ADDRESS.matcher(it).matches() }

    val authFactory = viewModelFactory { initializer {
        AuthViewModel(LoginUseCase(auth, emailValidator), RegisterUseCase(auth, emailValidator),
            ObserveAuthenticationUseCase(auth), LogoutUseCase(auth), GetCurrentUserIdUseCase(auth))
    } }
    val profileFactory = viewModelFactory { initializer {
        ProfileViewModel(GetCurrentProfileUseCase(profiles), UpdateProfileUseCase(profiles, images),
            ObserveUserPostsUseCase(posts), ObserveFollowProfileUseCase(follows))
    } }

    fun followListFactory(uid: String, type: com.example.spotly.domain.model.FollowListType) = viewModelFactory {
        initializer { FollowListViewModel(uid, type, ObserveFollowUsersUseCase(follows)) }
    }

    val userProfileFactory = viewModelFactory {
        initializer {
            UserProfileViewModel(
                GetUserProfileUseCase(profiles),
                ObserveUserPostsUseCase(posts),
                ObserveFollowProfileUseCase(follows),
                SetFollowingUseCase(follows, auth)
            )
        }
    }
    val feedFactory = viewModelFactory { initializer {
        FeedViewModel(ObserveFeedUseCase(feed), ObservePostLikesUseCase(likes), SetPostLikeUseCase(likes, auth),
            ObserveUnreadNotificationsUseCase(notifications))
    } }
    val exploreFactory = viewModelFactory { initializer {
        ExploreViewModel(GetExplorePageUseCase(FirestoreExploreRepository(services, connection), auth),
            ObserveFollowingIdsUseCase(follows, auth), ObservePostLikesUseCase(likes),
            SetPostLikeUseCase(likes, auth), ObserveUnreadNotificationsUseCase(notifications))
    } }
    val notificationsFactory = viewModelFactory { initializer {
        NotificationsViewModel(ObserveNotificationsUseCase(notifications), MarkNotificationReadUseCase(notifications))
    } }
    val searchFactory = viewModelFactory { initializer {
        SearchViewModel(SearchUsersUseCase(FirestoreUserSearchRepository(services)), createSavedStateHandle())
    } }
    fun likeListFactory(postId: String) = viewModelFactory {
        initializer { LikeListViewModel(postId, ObservePostLikeUsersUseCase(likes)) }
    }

    fun postDetailFactory(id: String) = viewModelFactory { initializer {
        PostDetailViewModel(id, GetPostUseCase(postDetails), UpdatePostUseCase(postDetails, auth),
            DeletePostUseCase(postDetails, auth), GetCurrentUserIdUseCase(auth), createSavedStateHandle(),
            ObservePostLikesUseCase(likes), SetPostLikeUseCase(likes, auth))
    } }
    // El caso de publicación conserva el borrador: debe ser una instancia por ViewModel, no global.
    val createPostFactory = viewModelFactory { initializer {
        CreatePostViewModel(PublishPostUseCase(posts, images, auth), GetCurrentLocationUseCase(locations))
    } }
}
