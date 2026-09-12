package com.example

import com.example.BuildConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    val mimeType: String,
    val data: String
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

suspend fun generateWorksheetVariations(prompt: String, base64Image: String? = null, numVariations: Int = 3): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty()) return@withContext "[{\"title\": \"Error\", \"intro\": \"API Key is missing.\", \"sections\": []}]"
    
    val parts = mutableListOf<Part>()
    parts.add(Part(text = prompt))
    if (base64Image != null) {
        parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image)))
    }
    
    val request = GenerateContentRequest(
        contents = listOf(Content(
            parts = parts
        )),
        systemInstruction = Content(
            parts = listOf(Part(text = "You are a master English Language Arts & ESL/EFL curriculum designer and educator. You generate worksheets EXCLUSIVELY for the English subject (English grammar, vocabulary, reading comprehension, idioms, syntax, phonics, writing mechanics, and English language exams like Cambridge, IELTS, and TOEFL). All generated content, questions, instructions, and passages MUST be for English language education. Output ONLY a valid JSON array containing exactly $numVariations distinct variations of the English worksheet. Do not use conversational filler or markdown code block formatting (e.g., do not output ```json). Structure: [ { \"title\": \"string\", \"intro\": \"string\", \"sections\": [ { \"title\": \"string\", \"content\": \"string\" } ] } ]. Always make sure the final section contains a comprehensive 'Answer Key & Teacher Solutions' section with correct answers and pedagogical explanations. If a reference image is provided, adapt its typography and formatting conventions into the English worksheets."))
        )
    )
    
    try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "[]"
        // Remove potential markdown code blocks if the model ignores the instruction
        text.replace("```json", "").replace("```", "").trim()
    } catch (e: Exception) {
        "[{\"title\": \"Error\", \"intro\": \"${e.message}\", \"sections\": []}]"
    }
}

suspend fun evaluateAnswers(worksheetContext: String, studentAnswers: String): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isEmpty()) return@withContext "Error: API Key is missing."

    val prompt = "Here is the original English worksheet content:\n$worksheetContext\n\nThe student provided the following answers:\n$studentAnswers\n\nPlease act as a supportive and expert English Language Tutor. Grade the answers, provide constructive feedback on grammar, vocabulary, spelling, and sentence structures, gently point out mistakes, and offer clear explanations to help the student learn."

    val request = GenerateContentRequest(
        contents = listOf(Content(parts = listOf(Part(text = prompt)))),
        systemInstruction = Content(
            parts = listOf(Part(text = "You are an encouraging and expert English Language teacher and AI tutor. Provide instant, constructive feedback on English grammar, vocabulary, spelling, and reading comprehension with helpful explanations."))
        )
    )

    try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No feedback generated."
    } catch (e: Exception) {
        "Error generating feedback: ${e.message}"
    }
}
