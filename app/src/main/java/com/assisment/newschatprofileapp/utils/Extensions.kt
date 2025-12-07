package com.assisment.newschatprofileapp.utils

import com.assisment.newschatprofileapp.data.local.entity.ArticleEntity
import com.assisment.newschatprofileapp.data.local.entity.MessageEntity
import com.assisment.newschatprofileapp.data.remote.dto.ArticleDto
import com.assisment.newschatprofileapp.domain.model.Article
import com.assisment.newschatprofileapp.domain.model.Message




fun ArticleDto.toArticleEntity(): ArticleEntity {
    return ArticleEntity(
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        sourceName = source.name,
        author = author,
        content = content ,

    )
}


fun Article.toEntity(page: Int = 1): ArticleEntity {
    return ArticleEntity(
        title = this.title,
        description = this.description,
        url = this.url,
        urlToImage = this.urlToImage,
        publishedAt = this.publishedAt,
        sourceName = this.sourceName,
        author = this.author,
        content = this.content,
        isFeatured = this.isFeatured,
        page = page
    )
}

fun ArticleEntity.toArticle(): Article {
    return Article(
        url = url,
        title = title,
        description = description,
        urlToImage = urlToImage,
        publishedAt = publishedAt,
        sourceName = sourceName,
        author = author,
        content = content,
        isFeatured = isFeatured
    )
}


// Message conversions
fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        text = text,
        imageUri = imageUri,
        isSentByMe = isSentByMe,
        timestamp = timestamp,
        dateGroup = dateGroup
    )
}

fun MessageEntity.toMessage(): Message {
    return Message(
        id = id,
        text = text,
        imageUri = imageUri,
        isSentByMe = isSentByMe,
        timestamp = timestamp,
        dateGroup = dateGroup
    )
}

// Date formatting
fun Long.toFormattedTime(): String {
    val date = java.util.Date(this)
    val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return formatter.format(date)
}

fun String.toReadableDate(): String {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault())
        val date = inputFormat.parse(this)
        outputFormat.format(date ?: java.util.Date())
    } catch (e: Exception) {
        this
    }
}


// Extension to check if string is not blank and not empty
fun String?.isNotNullOrBlank(): Boolean {
    return this != null && this.isNotBlank()
}

// Extension to get first letter for avatar
fun String.getInitials(): String {
    return if (this.isNotBlank()) {
        this.split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().toString() }
            .uppercase()
    } else {
        "?"
    }
}

// Extension to capitalize each word
fun String.capitalizeWords(): String {
    return this.split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
}

// Extension to mask email
fun String.maskEmail(): String {
    return if (this.contains("@")) {
        val parts = this.split("@")
        val username = parts[0]
        val domain = parts[1]

        val maskedUsername = if (username.length > 2) {
            "${username.take(3)}***"
        } else {
            "***"
        }

        "$maskedUsername@$domain"
    } else {
        this
    }
}

// Extension to mask phone number
fun String.maskPhone(): String {
    val digits = this.filter { it.isDigit() }
    return if (digits.length >= 4) {
        val lastFour = digits.takeLast(4)
        "***-***-$lastFour"
    } else {
        "***-***-****"
    }
}

// Extension to validate email
fun String.isValidEmail(): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$"
    return this.matches(emailRegex.toRegex())
}

// Extension to validate phone number
fun String.isValidPhone(): Boolean {
    val phoneRegex = "^[+]?[0-9]{10,15}\$"
    return this.matches(phoneRegex.toRegex())
}

