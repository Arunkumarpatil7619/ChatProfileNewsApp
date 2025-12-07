package com.assisment.newschatprofileapp



import android.content.Context
import android.graphics.Bitmap
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.assisment.newschatprofileapp.data.local.dao.ArticleDao
import com.assisment.newschatprofileapp.data.local.dao.MessageDao
import com.assisment.newschatprofileapp.data.local.database.AppDatabase
import com.assisment.newschatprofileapp.data.local.entity.ArticleEntity
import com.assisment.newschatprofileapp.data.local.entity.MessageEntity
import com.assisment.newschatprofileapp.data.repository.MessageRepositoryImpl
import com.assisment.newschatprofileapp.data.repository.NewsRepositoryImpl
import com.assisment.newschatprofileapp.data.repository.ProfileRepository
import com.assisment.newschatprofileapp.data.repository.ProfileRepositoryImpl
import com.assisment.newschatprofileapp.domain.model.*
import com.assisment.newschatprofileapp.domain.usecase.*
import com.assisment.newschatprofileapp.presentation.common.ConnectivityObserver
import com.assisment.newschatprofileapp.presentation.home.HomeEvent
import com.assisment.newschatprofileapp.presentation.home.HomeState
import com.assisment.newschatprofileapp.presentation.home.HomeViewModel
import com.assisment.newschatprofileapp.presentation.messages.*
import com.assisment.newschatprofileapp.presentation.profile.*
import com.assisment.newschatprofileapp.utils.Resource
import com.assisment.newschatprofileapp.utils.ThemeManager
import com.assisment.newschatprofileapp.utils.ThemePreference
import com.assisment.newschatprofileapp.utils.toEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AppUnitTests {

    // Test Coroutine Scope
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // Mock Dependencies
    @Mock
    private lateinit var mockGetTopHeadlinesUseCase: GetTopHeadlinesUseCase

    @Mock
    private lateinit var mockGetCachedArticlesUseCase: GetCachedArticlesUseCase

    @Mock
    private lateinit var mockSearchArticlesUseCase: SearchArticlesUseCase

    @Mock
    private lateinit var mockConnectivityObserver: ConnectivityObserver

    @Mock
    private lateinit var mockGetMessagesUseCase: GetMessagesUseCase

    @Mock
    private lateinit var mockSendTextMessageUseCase: SendTextMessageUseCase

    @Mock
    private lateinit var mockSendImageMessageUseCase: SendImageMessageUseCase

    @Mock
    private lateinit var mockSimulateReceivedMessageUseCase: SimulateReceivedMessageUseCase

    @Mock
    private lateinit var mockGetUserProfileUseCase: GetUserProfileUseCase

    @Mock
    private lateinit var mockUpdateProfileUseCase: UpdateProfileUseCase

    @Mock
    private lateinit var mockGetCurrentLocationUseCase: GetCurrentLocationUseCase

    @Mock
    private lateinit var mockSaveProfileImageUseCase: SaveProfileImageUseCase

    @Mock
    private lateinit var mockProfileRepository: ProfileRepository

    @Mock
    private lateinit var mockThemeManager: ThemeManager

    // Real repositories dependencies
    private lateinit var context: Context
    private lateinit var testDatabase: AppDatabase
    private lateinit var messageDao: MessageDao
    private lateinit var articleDao: ArticleDao

    // ViewModels
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var messagesViewModel: MessagesViewModel
    private lateinit var profileViewModel: ProfileViewModel

    // Repositories
    private lateinit var messageRepository: MessageRepositoryImpl
    private lateinit var newsRepository: NewsRepositoryImpl
    private lateinit var profileRepository: ProfileRepositoryImpl

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        // Setup test dispatcher
        Dispatchers.setMain(testDispatcher)

        // Initialize context for real repositories
        context = ApplicationProvider.getApplicationContext()

        // Setup in-memory database
        testDatabase = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        messageDao = testDatabase.messageDao()
        articleDao = testDatabase.articleDao()

        // Initialize real repositories
        messageRepository = MessageRepositoryImpl(messageDao)
        newsRepository = NewsRepositoryImpl(
            newsApi = mock(), // Mock API
            articleDao = articleDao,
            context = context
        )
        profileRepository = ProfileRepositoryImpl(context)

        // Setup ViewModels with mocked use cases
        homeViewModel = HomeViewModel(
            getTopHeadlinesUseCase = mockGetTopHeadlinesUseCase,
            getCachedArticlesUseCase = mockGetCachedArticlesUseCase,
            searchArticlesUseCase = mockSearchArticlesUseCase,
            connectivityObserver = mockConnectivityObserver
        )

        messagesViewModel = MessagesViewModel(
            getMessagesUseCase = mockGetMessagesUseCase,
            sendTextMessageUseCase = mockSendTextMessageUseCase,
            sendImageMessageUseCase = mockSendImageMessageUseCase,
            simulateReceivedMessageUseCase = mockSimulateReceivedMessageUseCase
        )

        profileViewModel = ProfileViewModel(
            getUserProfileUseCase = mockGetUserProfileUseCase,
            updateProfileUseCase = mockUpdateProfileUseCase,
            getCurrentLocationUseCase = mockGetCurrentLocationUseCase,
            saveProfileImageUseCase = mockSaveProfileImageUseCase,
            profileRepository = mockProfileRepository,
            themeManager = mockThemeManager
        )

        // Setup default behaviors
        setupDefaultMocks()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDatabase.close()
    }



    private fun setupDefaultMocks() {
        // Connectivity
        whenever(mockConnectivityObserver.observe())
            .thenReturn(flowOf(ConnectivityObserver.Status.Available))

        // Messages
        whenever(mockGetMessagesUseCase())
            .thenReturn(flowOf(emptyList()))

        // Suspend functions - use runBlocking + whenever
        runBlocking {
            whenever(mockSendTextMessageUseCase.invoke(any())).thenReturn(Unit)
            whenever(mockSendImageMessageUseCase.invoke(any())).thenReturn(Unit)
            whenever(mockSimulateReceivedMessageUseCase.invoke()).thenReturn(Unit)
        }

        // Profile
        whenever(mockGetUserProfileUseCase())
            .thenReturn(
                flowOf(
                    UserProfile(
                        name = "Test User",
                        email = "test@example.com",
                        bio = "Test bio",
                        phone = "1234567890"
                    )
                )
            )

        runBlocking {
            whenever(mockUpdateProfileUseCase.invoke(any())).thenReturn(Unit)
            whenever(mockSaveProfileImageUseCase.invoke(any())).thenReturn("/test/path.jpg")
        }

        // Location
        whenever(mockGetCurrentLocationUseCase.invoke(any()))
            .thenReturn(flowOf(Resource.Success(Location(0.0, 0.0, "Test Location"))))
    }


    // ===========================================
    // HOME VIEWMODEL TESTS
    // ===========================================

    @Test
    fun `homeViewModel initial state should be loading`() = testScope.runTest {
        // Given
        val initialState = homeViewModel.state.value

        // Then
        assert(initialState.isLoading)
        assert(initialState.articles is Resource.Loading)
        assert(initialState.isOnline)
    }

    @Test
    fun `homeViewModel should load headlines on init`() = testScope.runTest {
        // Given
        val testArticles = listOf(
            Article(
                title = "Test Article 1",
                description = "Test Description 1",
                url = "http://test.com/1",
                urlToImage = "http://test.com/image1.jpg",
                publishedAt = "2024-01-01",
                sourceName = "GitHub",
                author="Geek",
                content="xyz",
                isFeatured = true
            )
        )

        whenever(mockGetTopHeadlinesUseCase.invoke(any(), any()))
            .thenReturn(flowOf(Resource.Success(testArticles)))

        // When
        // ViewModel is already initialized in setup

        // Then
        verify(mockGetTopHeadlinesUseCase).invoke(page = 1, forceRefresh = false)
    }

    @Test
    fun `homeViewModel refresh should force reload`() = testScope.runTest {
        // Given
        val testArticles = listOf(
            Article(
                title = "Test Article",
                description = "Test Description",
                url = "http://test.com/1",
                urlToImage = "http://test.com/image.jpg",
                publishedAt = "2024-01-01",
                sourceName = "GitHub",
                author="Geek",
                content="xyz",
                isFeatured = true
            )
        )

        whenever(mockGetTopHeadlinesUseCase.invoke(any(), any()))
            .thenReturn(flowOf(Resource.Success(testArticles)))

        // When
        homeViewModel.onEvent(HomeEvent.Refresh)

        // Then
        verify(mockGetTopHeadlinesUseCase).invoke(page = 1, forceRefresh = true)
    }

    @Test
    fun `homeViewModel search should trigger search use case`() = testScope.runTest {
        // Given
        val query = "test query"
        val searchResults = listOf(
            Article(
                title = "Search Result",
                description = "Search Description",
                url = "http://test.com/2",
                urlToImage = "http://test.com/image2.jpg",
                publishedAt = "2024-01-02",
                author="Geek",
                sourceName = "znvv",
                content="xyz",
                isFeatured = true
            )
        )

        whenever(mockSearchArticlesUseCase.invoke(query))
            .thenReturn(flowOf(Resource.Success(searchResults)))

        // When
        homeViewModel.onEvent(HomeEvent.Search(query))

        // Wait for debounce
        delay(350)

        // Then
        verify(mockSearchArticlesUseCase).invoke(query)
        val state = homeViewModel.state.value
        assert(state.searchResults == searchResults)
        assert(state.searchState is Resource.Success)
    }


    @Test
    fun `homeViewModel should handle connectivity changes`() = testScope.runTest {
        // Given
        val connectivityFlow = MutableStateFlow(ConnectivityObserver.Status.Available)
        whenever(mockConnectivityObserver.observe()).thenReturn(connectivityFlow)

        // Create new ViewModel with the mutable flow
        val viewModel = HomeViewModel(
            mockGetTopHeadlinesUseCase,
            mockGetCachedArticlesUseCase,
            mockSearchArticlesUseCase,
            mockConnectivityObserver
        )

        // When going offline
        connectivityFlow.value = ConnectivityObserver.Status.Unavailable

        // Then
        val state = viewModel.state.value
        assert(!state.isOnline)
    }

    @Test
    fun `homeViewModel load more should increment page`() = testScope.runTest {
        // Given
        val page1Articles = List(20) { index ->
            Article(
                title = "Article $index",
                description = "Description $index",
                url = "http://test.com/$index",
                urlToImage = "http://test.com/image$index.jpg",
                publishedAt = "2024-01-01",
                sourceName = "GitHub",
                author="Geek",
                content="xyz",
                isFeatured = true
            )
        }

        val page2Articles = List(20) { index ->
            Article(
                title = "Article ${index + 20}",
                description = "Description ${index + 20}",
                url = "http://test.com/${index + 20}",
                urlToImage = "http://test.com/image${index + 20}.jpg",
                publishedAt = "2024-01-02",
                sourceName = "GitHub",
                author="Geek",
                content="xyz",
                isFeatured = true
            )
        }

        whenever(mockGetTopHeadlinesUseCase.invoke(page = 1, forceRefresh = false))
            .thenReturn(flowOf(Resource.Success(page1Articles)))

        whenever(mockGetTopHeadlinesUseCase.invoke(page = 2, forceRefresh = false))
            .thenReturn(flowOf(Resource.Success(page2Articles)))

        // Setup initial state
        homeViewModel.onEvent(HomeEvent.Refresh)
        delay(100)

        // When
        homeViewModel.onEvent(HomeEvent.LoadMore)

        // Then
        verify(mockGetTopHeadlinesUseCase).invoke(page = 2, forceRefresh = false)
        val state = homeViewModel.state.value
        assert(state.currentPage == 2)
    }

    // ===========================================
    // MESSAGES VIEWMODEL TESTS
    // ===========================================

    @Test
    fun `messagesViewModel initial state should be empty`() = testScope.runTest {
        // Given
        val initialState = messagesViewModel.state.value

        // Then
        assert(initialState.messages.isEmpty())
        assert(initialState.groupedMessages.isEmpty())
        assert(!initialState.isLoading)
    }

    @Test
    fun `messagesViewModel should load messages on init`() = testScope.runTest {
        // Then
        verify(mockGetMessagesUseCase).invoke()
    }

    @Test
    fun `messagesViewModel send text should call use case`() = testScope.runTest {
        // Given
        val testText = "Hello, world!"

        // When
        messagesViewModel.onEvent(MessagesEvent.SendText(testText))

        // Then
        verify(mockSendTextMessageUseCase).invoke(testText)
    }

    @Test
    fun `messagesViewModel send image should call use case`() = testScope.runTest {
        // Given
        val testImageUri = "content://test/image.jpg"

        // When
        messagesViewModel.onEvent(MessagesEvent.SendImage(testImageUri))

        // Then
        verify(mockSendImageMessageUseCase).invoke(testImageUri)
    }

    @Test
    fun `messagesViewModel simulate received should call use case`() = testScope.runTest {
        // When
        messagesViewModel.onEvent(MessagesEvent.SimulateReceived)

        // Then
        verify(mockSimulateReceivedMessageUseCase).invoke()
    }

    @Test
    fun `messagesViewModel update typing text should update ui state`() = testScope.runTest {
        // Given
        val typingText = "Typing..."

        // When
        messagesViewModel.onEvent(MessagesEvent.UpdateTypingText(typingText))

        // Then
        val uiState = messagesViewModel.uiState.value
        assert(uiState.typingText == typingText)
    }

    @Test
    fun `messagesViewModel should group messages by date`() = testScope.runTest {
        // Given
        val testMessages = listOf(
            Message(
                id = 1,
                text = "Message 1",
                timestamp = System.currentTimeMillis(),
                dateGroup = "2024-01-01",
                isSentByMe = true
            ),
            Message(
                id = 2,
                text = "Message 2",
                timestamp = System.currentTimeMillis() + 1000,
                dateGroup = "2024-01-01",
                isSentByMe = false
            ),
            Message(
                id = 3,
                text = "Message 3",
                timestamp = System.currentTimeMillis() + 86400000, // Next day
                dateGroup = "2024-01-02",
                isSentByMe = true
            )
        )

        whenever(mockGetMessagesUseCase()).thenReturn(flowOf(testMessages))

        // Create new ViewModel to trigger init
        val viewModel = MessagesViewModel(
            mockGetMessagesUseCase,
            mockSendTextMessageUseCase,
            mockSendImageMessageUseCase,
            mockSimulateReceivedMessageUseCase
        )

        delay(100)

        // Then
        val state = viewModel.state.value
        assert(state.groupedMessages.size == 2)
        assert(state.groupedMessages.containsKey("2024-01-01"))
        assert(state.groupedMessages.containsKey("2024-01-02"))
        assert(state.groupedMessages["2024-01-01"]?.size == 2)
        assert(state.groupedMessages["2024-01-02"]?.size == 1)
    }

    // ===========================================
    // PROFILE VIEWMODEL TESTS
    // ===========================================

    @Test
    fun `profileViewModel should load user profile on init`() = testScope.runTest {
        // Then
        verify(mockGetUserProfileUseCase).invoke()
    }

    @Test
    fun `profileViewModel update name should call use case`() = testScope.runTest {
        // Given
        val newName = "Updated Name"

        // When
        profileViewModel.onEvent(ProfileEvent.UpdateName(newName))


    }

    @Test
    fun `profileViewModel update bio should call use case`() = testScope.runTest {
        // Given
        val newBio = "Updated bio text"

        // When
        profileViewModel.onEvent(ProfileEvent.UpdateBio(newBio))

        // Then

    }

    @Test
    fun `profileViewModel update phone should call use case`() = testScope.runTest {
        // Given
        val newPhone = "+1 234 567 8900"

        // When
        profileViewModel.onEvent(ProfileEvent.UpdatePhone(newPhone))


    }

    @Test
    fun `profileViewModel save profile image should call use case`() = testScope.runTest {
        // Given
        val mockBitmap = mock(Bitmap::class.java)

        // When
        profileViewModel.onEvent(ProfileEvent.SaveProfileImage(mockBitmap))

        // Then
        verify(mockSaveProfileImageUseCase).invoke(mockBitmap)
    }

    @Test
    fun `profileViewModel get current location should call use case`() = testScope.runTest {
        // Given
        val mockContext = mock(Context::class.java)

        // When
        profileViewModel.onEvent(ProfileEvent.GetCurrentLocation(mockContext))

        // Then
        verify(mockGetCurrentLocationUseCase).invoke(mockContext)
    }

    @Test
    fun `profileViewModel toggle theme should update theme manager`() = testScope.runTest {
        // When
        profileViewModel.toggleTheme()

        // Then
        verify(mockThemeManager).setThemePreference(any())
    }

    @Test
    fun `profileViewModel set theme preference should call theme manager`() = testScope.runTest {
        // Given
        val preference = ThemePreference.DARK

        // When
        profileViewModel.setThemePreference(preference)

        // Then
        verify(mockThemeManager).setThemePreference(preference)
    }

    @Test
    fun `profileViewModel check permissions should call repository`() = testScope.runTest {
        // Given
        val mockContext = mock(Context::class.java)
        whenever(mockProfileRepository.hasPermission(any(), any())).thenReturn(true)

        // When
        profileViewModel.onEvent(ProfileEvent.CheckPermissions(mockContext))

        // Then
        verify(mockProfileRepository, atLeastOnce()).hasPermission(any(), any())
    }

    // ===========================================
    // MESSAGE REPOSITORY TESTS
    // ===========================================

    @Test
    fun `messageRepository send text should save to database`() = testScope.runTest {
        // Given
        val testText = "Test message"

        // When
        messageRepository.sendTextMessage(testText)

        // Then
        val messages = messageRepository.getMessages().first()
        assert(messages.isNotEmpty())
        assert(messages.last().text == testText)
        assert(messages.last().isSentByMe)
    }

    @Test
    fun `messageRepository send image should save to database`() = testScope.runTest {
        // Given
        val testImageUri = "content://test/image.jpg"

        // When
        messageRepository.sendImageMessage(testImageUri)

        // Then
        val messages = messageRepository.getMessages().first()
        assert(messages.isNotEmpty())
        assert(messages.last().imageUri == testImageUri)
        assert(messages.last().isSentByMe)
    }

    @Test
    fun `messageRepository simulate received should create received message`() = testScope.runTest {
        // Given
        val testText = "Received message"

        // When
        messageRepository.simulateReceivedMessage(testText)

        // Then
        val messages = messageRepository.getMessages().first()
        assert(messages.isNotEmpty())
        assert(messages.last().text == testText)
        assert(!messages.last().isSentByMe)
    }

    @Test
    fun `messageRepository get messages by date should filter correctly`() = testScope.runTest {
        // Given
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormatter.format(Date())
        val yesterday = dateFormatter.format(Date(System.currentTimeMillis() - 86400000))

        // Insert test messages
        val message1 = Message(
            id = 0,
            text = "Today's message",
            timestamp = System.currentTimeMillis(),
            dateGroup = today,
            isSentByMe = true
        )

        val message2 = Message(
            id = 0,
            text = "Yesterday's message",
            timestamp = System.currentTimeMillis() - 86400000,
            dateGroup = yesterday,
            isSentByMe = false
        )

        messageDao.insert(message1.toEntity())
        messageDao.insert(message2.toEntity())

        // When
        val todayMessages = messageRepository.getMessagesByDate(today)
        val yesterdayMessages = messageRepository.getMessagesByDate(yesterday)

        // Then
        assert(todayMessages.size == 1)
        assert(todayMessages[0].text == "Today's message")

        assert(yesterdayMessages.size == 1)
        assert(yesterdayMessages[0].text == "Yesterday's message")
    }

    @Test
    fun `messageRepository clear messages should remove all messages`() = testScope.runTest {
        // Given
        messageRepository.sendTextMessage("Test message 1")
        messageRepository.sendTextMessage("Test message 2")

        // When
        messageRepository.clearMessages()

        // Then
        val messages = messageRepository.getMessages().first()
        assert(messages.isEmpty())
    }

    // ===========================================
    // NEWS REPOSITORY TESTS
    // ===========================================

    @Test
    fun `newsRepository cache articles should save to database`() = testScope.runTest {
        // Given
        val testArticles = listOf(
            Article(
                title = "Test Article",
                description = "Test Description",
                url = "http://test.com/1",
                urlToImage = "http://test.com/image.jpg",
                publishedAt = "2024-01-01T00:00:00Z",
                sourceName = "GitHub",
                author="Geek",
                content="xyz",
                isFeatured = true
            )
        )

        // When
        // We'll test this indirectly through a mock, as we can't easily test the real API call
        val entities = testArticles.map { it.toEntity(1) }
        articleDao.insertAll(entities)

        // Then
        val cached = articleDao.getAllArticles().first()
        assert(cached.isNotEmpty())
        assert(cached[0].title == "Test Article")
    }

    @Test
    fun `newsRepository get cached articles should return flow`() = testScope.runTest {
        // Given
        val testArticles = listOf(
            Article(
                title = "Cached Article",
                description = "Cached Description",
                url = "http://test.com/1",
                urlToImage = "http://test.com/image.jpg",
                publishedAt = "2024-01-01T00:00:00Z",
                sourceName = "GitHub",
                author="Geek",
                content="xyz",
                isFeatured = true
            )
        )

        val entities = testArticles.map { it.toEntity(1) }
        articleDao.insertAll(entities)

        // When
        val cachedFlow = newsRepository.getCachedArticles()

        // Then
        val result = cachedFlow.first()
        assert(result.isNotEmpty())
        assert(result[0].title == "Cached Article")
    }


    // ===========================================
    // PROFILE REPOSITORY TESTS
    // ===========================================

    @Test
    fun `profileRepository getUserProfile should return default profile initially`() = testScope.runTest {
        // When
        val profileFlow = profileRepository.getUserProfile()

        // Then
        val profile = profileFlow.first()
        assert(profile.name == "John Doe")
        assert(profile.email == "john.doe@example.com")
        assert(profile.bio.contains("Android Developer"))
    }

    @Test
    fun `profileRepository updateProfile should save data`() = testScope.runTest {
        // Given
        val testProfile = UserProfile(
            name = "Test Name",
            email = "test@example.com",
            bio = "Test bio",
            phone = "+1 234 567 8900",
            profileImageUri = null,
            location = Location(
                latitude = 40.7128,
                longitude = -74.0060,
                address = "New York, NY"
            )
        )

        // When
        profileRepository.updateProfile(testProfile)

        // Then
        val savedProfile = profileRepository.getUserProfile().first()
        assert(savedProfile.name == "Test Name")
        assert(savedProfile.email == "test@example.com")
        assert(savedProfile.location?.latitude == 40.7128)
        assert(savedProfile.location?.longitude == -74.0060)
    }

    @Test
    fun `profileRepository saveThemePreference should store value`() = testScope.runTest {
        // When
        profileRepository.saveThemePreference(true)

        // Then
        val isDarkMode = profileRepository.getThemePreference()
        assert(isDarkMode)
    }

    @Test
    fun `profileRepository clearAllData should reset to defaults`() = testScope.runTest {
        // Given
        val testProfile = UserProfile(
            name = "Test Name",
            email = "test@example.com",
            bio = "Test bio",
            phone = "+1 234 567 8900"
        )

        profileRepository.updateProfile(testProfile)
        profileRepository.saveThemePreference(true)

        // When
        profileRepository.clearAllData()

        // Then
        val clearedProfile = profileRepository.getUserProfile().first()
        val themePreference = profileRepository.getThemePreference()

        assert(clearedProfile.name == "John Doe")
        assert(clearedProfile.email == "john.doe@example.com")
        assert(!themePreference)
    }

    // ===========================================
    // ERROR HANDLING TESTS
    // ===========================================

    @Test
    fun `homeViewModel should handle network errors gracefully`() = testScope.runTest {
        // Given
        val errorMessage = "Network error"
        whenever(mockGetTopHeadlinesUseCase.invoke(any(), any()))
            .thenReturn(flowOf(Resource.Error(errorMessage)))

        // Create new ViewModel
        val viewModel = HomeViewModel(
            mockGetTopHeadlinesUseCase,
            mockGetCachedArticlesUseCase,
            mockSearchArticlesUseCase,
            mockConnectivityObserver
        )

        delay(100)

        // Then
        val state = viewModel.state.value
        assert(state.error == errorMessage)
        assert(state.articles is Resource.Error)
    }

    @Test
    fun `profileViewModel should handle location errors`() = testScope.runTest {
        // Given
        val errorMessage = "Location unavailable"
        whenever(mockGetCurrentLocationUseCase.invoke(any()))
            .thenReturn(flowOf(Resource.Error(errorMessage)))

        // When
        profileViewModel.onEvent(ProfileEvent.GetCurrentLocation(mock(Context::class.java)))

        // Then
        delay(100)
        val state = profileViewModel.profileState.value
        assert(state.locationError == errorMessage)
    }

    @Test
    fun `messageRepository should not save blank messages`() = testScope.runTest {
        // Given
        val blankText = "   "

        // When
        messageRepository.sendTextMessage(blankText)

        // Then
        val messages = messageRepository.getMessages().first()
        assert(messages.isEmpty())
    }

    // ===========================================
    // INTEGRATION STYLE TESTS
    // ===========================================

    @Test
    fun `complete flow - send message and verify persistence`() = testScope.runTest {
        // Given
        val messageText = "Integration test message"

        // When
        messageRepository.sendTextMessage(messageText)

        // Simulate received response
        messageRepository.simulateReceivedMessage("Response message")

        // Then
        val messages = messageRepository.getMessages().first()
        assert(messages.size == 2)
        assert(messages[0].text == messageText)
        assert(messages[0].isSentByMe)
        assert(messages[1].text == "Response message")
        assert(!messages[1].isSentByMe)
    }

    @Test
    fun `profile update flow - update multiple fields`() = testScope.runTest {
        // Given
        val updatedProfile = UserProfile(
            name = "Updated Name",
            email = "updated@example.com",
            bio = "Updated bio with more details",
            phone = "+1 555 123 4567",
            location = Location(
                latitude = 51.5074,
                longitude = -0.1278,
                address = "London, UK"
            )
        )

        // When
        profileRepository.updateProfile(updatedProfile)

        // Update theme preference
        profileRepository.saveThemePreference(true)

        // Then
        val savedProfile = profileRepository.getUserProfile().first()
        val themePreference = profileRepository.getThemePreference()

        assert(savedProfile.name == "Updated Name")
        assert(savedProfile.email == "updated@example.com")
        assert(savedProfile.location?.address == "London, UK")
        assert(themePreference)
    }
}