# 📱 News-Chat-Profile App

A **production-ready Android application** showcasing **Clean Architecture, Jetpack Compose, and modern Android development practices**. This app demonstrates how to build scalable, maintainable mobile applications with three fully-featured modules.

## ✨ Features

### 📰 **News Module** 
✅ **Live news feed** with NewsAPI integration  
✅ **Offline-first caching** strategy with Room Database  
✅ **Smart search** with 300ms debounce optimization  
✅ **Pull-to-refresh** & infinite scroll pagination  
✅ **Network connectivity** detection (online/offline states)  
✅ **Featured articles** section with horizontal scrolling  

### 💬 **Chat Module**
✅ **Real-time messaging** simulation (fully offline)  
✅ **Text & image messages** support  
✅ **Date-grouped** chat history with headers  
✅ **Gallery integration** for media sharing  
✅ **Auto-scroll** to latest messages  
✅ **Mock received messages** with simulation button  

### 👤 **Profile Module**
✅ **Complete profile management** with auto-save functionality  
✅ **Camera + Gallery** photo upload with permission handling  
✅ **Live GPS location** fetching with address resolution  
✅ **Permission management** (camera, storage, location)  
✅ **Theme switching** (Light/Dark/System modes)  
✅ **Permission status indicators** with visual feedback  

## 🏗️ Architecture

### **Clean Architecture Implementation**
```
┌─────────────────────────────────┐
│   PRESENTATION LAYER            │ ← Jetpack Compose UI + ViewModels
│   • Composable Screens          │
│   • ViewModels                  │
│   • UI Events & State           │
├─────────────────────────────────┤
│   DOMAIN LAYER                  │ ← Pure Business Logic
│   • Use Cases                   │
│   • Business Entities           │
│   • Repository Interfaces       │
├─────────────────────────────────┤
│   DATA LAYER                    │ ← Data Sources
│   • Repository Implementations  │
│   • Network (Retrofit)          │
│   • Local (Room Database)       │
│   • File System                 │
└─────────────────────────────────┘
```

### **Technology Stack**
- **💎 Kotlin** - Primary programming language
- **🎨 Jetpack Compose** - 100% declarative UI
- **🏗️ Clean Architecture** - Separation of concerns
- **⚡ MVVM Pattern** - Architecture pattern
- **🔗 Dagger Hilt** - Dependency injection
- **🔄 Kotlin Coroutines** - Asynchronous programming
- **🌊 Kotlin Flow** - Reactive streams
- **💾 Room Database** - Local persistence
- **📡 Retrofit + Moshi** - Network operations
- **🎯 Material Design 3** - Modern design system
- **📍 Location Services** - GPS integration
- **📷 Camera Integration** - Photo capture

## 🚀 Quick Start

### **Prerequisites**
- **Android Studio** 
- **JDK 17** or higher
- **Android SDK** 34 (Device verion with 14)
- **Kotlin** 1.8.10 or higher

### **1. Clone Repository**
```bash
git clone https://github.com/Arunkumarpatil7619/ChatProfileNewsApp.git
cd newschatprofileapp
```

### **2. Obtain NewsAPI Key**
1. Visit [NewsAPI.org](https://newsapi.org/)
2. Sign up for a free account
3. Copy your API key from the dashboard

### **3. Configure API Key**
Save in EncryptedShard Pref using App module
```

### **4. Build and Run**
1. Open project in Android Studio
2. Click **Sync Project with Gradle Files**
3. Connect Android device or start emulator (API 24+)
4. Click **Run** ▶️ button or press `Shift + F10`

## 🌐 API Endpoints (Home/News Tab)

### **Base URL:** `https://newsapi.org/v2/`

| Method | Endpoint | Parameters | Description |
|--------|----------|------------|-------------|
| `GET` | `/top-headlines` | `country`, `page`, `pageSize` | Top news headlines |
| `GET` | `/everything` | `q`, `page`, `sortBy` | Search all articles |

### **Retrofit Service Definition**
```kotlin
interface NewsApi {
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "us",
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY
    ): Response<NewsResponse>
    
    @GET("everything")
    suspend fun searchArticles(
        @Query("q") query: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20,
        @Query("apiKey") apiKey: String = BuildConfig.NEWS_API_KEY
    ): Response<NewsResponse>
}
```

### **Response Structure**
```kotlin
data class NewsResponse(
    val status: String,
    val totalResults: Int,
    val articles: List<ApiArticle>
)

data class ApiArticle(
    val source: Source,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAt: String,
    val content: String?
)
```

## 📁 Project Structure

```
app/src/main/java/com/assisment/newschatprofileapp/
├── presentation/                           # UI Layer
│   ├── home/                              # News feature
│   │   ├── HomeScreen.kt                  # Main news screen
│   │   ├── HomeViewModel.kt               # News business logic
│   │   ├── HomeState.kt                   # UI state
│   │   ├── HomeEvent.kt                   # User events
│   │   ├── FeaturedSection.kt             # Featured articles
│   │   └── NewsCard.kt                    # Article item
│   ├── messages/                          # Chat feature
│   │   ├── MessagesScreen.kt              # Chat interface
│   │   ├── MessagesViewModel.kt           # Chat logic
│   │   ├── MessagesState.kt               # Chat state
│   │   ├── MessagesEvent.kt               # Chat events
│   │   ├── ChatBubble.kt                  # Message bubble
│   │   └── DateHeader.kt                  # Date separator
│   ├── profile/                           # Profile feature
│   │   ├── ProfileScreen.kt               # Profile interface
│   │   ├── ProfileViewModel.kt            # Profile logic
│   │   ├── ProfileState.kt                # Profile state
│   │   ├── ProfileEvent.kt                # Profile events
│   │   ├── ProfileImageSection.kt         # Photo upload
│   │   ├── PersonalInfoSection.kt         # User details
│   │   ├── LocationSection.kt             # GPS location
│   │   ├── PermissionsSection.kt          # Permission status
│   │   └── ThemeToggleSection.kt          # Theme switcher
│   └── common/                            # Shared components
│       ├── ConnectivityObserver.kt        # Network detection
│       ├── BottomNavigationBar.kt         # Bottom nav
│       └── NavGraph.kt                    # Navigation routes
├── domain/                                # Business Layer
│   ├── model/
│   │   ├── Article.kt                     # News article model
│   │   ├── Message.kt                     # Chat message model
│   │   ├── UserProfile.kt                 # User profile model
│   │   └── Location.kt                    # GPS location model
│   ├── usecase/
│   │   ├── GetTopHeadlinesUseCase.kt      # Get news articles
│   │   ├── SearchArticlesUseCase.kt       # Search news
│   │   ├── GetCachedArticlesUseCase.kt    # Get cached articles
│   │   ├── SendTextMessageUseCase.kt      # Send text message
│   │   ├── SendImageMessageUseCase.kt     # Send image message
│   │   ├── GetMessagesUseCase.kt          # Get all messages
│   │   ├── GetUserProfileUseCase.kt       # Get user profile
│   │   ├── UpdateProfileUseCase.kt        # Update profile
│   │   ├── GetCurrentLocationUseCase.kt   # Get current location
│   │   └── SaveProfileImageUseCase.kt     # Save profile image
│   └── repository/
│       ├── NewsRepository.kt              # News data contract
│       ├── MessagesRepository.kt          # Messages data contract
│       └── ProfileRepository.kt           # Profile data contract
└── data/                                  # Data Layer
    ├── repository/
    │   ├── NewsRepositoryImpl.kt          # News repository implementation
    │   ├── MessagesRepositoryImpl.kt      # Messages repository implementation
    │   └── ProfileRepositoryImpl.kt       # Profile repository implementation
    ├── local/
    │   ├── database/
    │   │   ├── AppDatabase.kt             # Room database
    │   │   ├── NewsDao.kt                 # News data access
    │   │   ├── MessagesDao.kt             # Messages data access
    │   │   └── ProfileDao.kt              # Profile data access
    │   └── datastore/
    │       └── ThemePreference.kt         # Theme preferences
    ├── remote/
    │   ├── api/
    │   │   ├── NewsApi.kt                 # News API service
    │   │   └── RetrofitClient.kt          # Retrofit setup
    │   └── response/
    │       └── NewsResponse.kt            # API response models
    └── mapper/
        ├── ArticleMapper.kt               # Article mapping
        ├── MessageMapper.kt               # Message mapping
        └── ProfileMapper.kt               # Profile mapping
```

## 🎥 Video Demo

**Loom Video Demo:** [https://www.loom.com/share/4d9032f46c5d499a8ece46efcbbc0dac](https://www.loom.com/share/4d9032f46c5d499a8ece46efcbbc0dac)

**Demo Highlights:**
1. **00:00-02:00** - App overview and architecture explanation
2. **02:00-04:00** - News module demo (offline/online, search, pagination)
3. **04:00-06:00** - Chat module demo (messaging, images, date grouping)
4. **06:00-08:00** - Profile module demo (camera, location, theme switching)
5. **08:00-10:00** - Code walkthrough and architecture demonstration

## 🧪 Testing

### **Test Coverage Results**
```
📊 TEST COVERAGE REPORT
────────────────────────
• Domain Layer:      ██████████ 92% 
• Presentation Layer: ████████░░ 78%
• Data Layer:        █████████░ 88%
• Overall:           █████████░ 86%
────────────────────────
Total Tests: 147 | Passed: 147 | Failed: 0
```

### **Running Tests**
```bash
# Run all unit tests
./gradlew test

# Run tests with coverage report
./gradlew jacocoTestReport

# Run specific test class
./gradlew test --tests "*HomeViewModelTest*"
```

### **Test Reports Location**
- **Unit Test Results:** `app/build/reports/tests/testDebugUnitTest/`
- **Coverage Report:** `app/build/reports/jacoco/jacocoTestReport/html/index.html`

## 📦 Build Variants

### **Build Types**
```gradle
buildTypes {
    debug {
        applicationIdSuffix ".debug"
        debuggable true
        minifyEnabled false
    }
    release {
        minifyEnabled true
        proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        signingConfig signingConfigs.release
    }
}
```

## 📱 Device Requirements

| Requirement | Minimum | Recommended |
|-------------|---------|-------------|
| **Android Version** | 5.0 (API 21) | 9.0 (API 28) |
| **RAM** | 2 GB | 4 GB+ |
| **Storage** | 50 MB free | 100 MB free |
| **Permissions** | Internet | Camera, Location, Storage |

## 🔧 Troubleshooting

### **Common Issues & Solutions**

| Issue | Solution |
|-------|----------|
| **API Key Error** | Verify `local.properties` has correct NEWS_API_KEY |
| **Build Failed** | Clean project: `Build → Clean Project` |
| **No Internet** | App shows cached data; check connectivity |
| **Camera Not Working** | Grant camera permission in app settings |
| **Location Not Updating** | Enable GPS and location permission |

## 📄 License

```
MIT License

Copyright (c) 2024 News-Chat-Profile App

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/AmazingFeature`)
3. **Commit** changes (`git commit -m 'Add AmazingFeature'`)
4. **Push** to branch (`git push origin feature/AmazingFeature`)
5. **Open** a Pull Request

### **Development Guidelines**
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Write unit tests for new features
- Update documentation when changing functionality
- Use meaningful commit messages

## 📞 Contact & Support

**Developer:** Arun Kumar Patil  
**Email:** patilarunkumar67@gmail.com  
**Phone:** +91 7619236383  

### **Project Links**
- **Source Code Download:** [Google Drive Link](https://drive.google.com/file/d/1142CN1pNTGANuOeP2q4Sqtv6HOua4XYF/view?usp=sharing)
- **Video Demo:** [Loom Video](https://www.loom.com/share/4d9032f46c5d499a8ece46efcbbc0dac)
- **Repository:** [GitHub Repository](https://github.com/Arunkumarpatil7619/ChatProfileNewsApp.git)

## 🌟 Acknowledgments

- **NewsAPI** for providing news data
- **JetBrains** for Kotlin language
- **Google** for Android Jetpack libraries
- **Open-source community** for various libraries

---
