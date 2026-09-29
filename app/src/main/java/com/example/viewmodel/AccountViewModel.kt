package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccountEntity
import com.example.data.AccountRepository
import com.example.data.GeminiClient
import com.example.data.GenerateContentRequest
import com.example.data.Content
import com.example.data.Part
import com.example.data.GenerationConfig
import com.example.util.PdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.random.Random

data class EmulatorNodeState(
    val screenId: Int,
    val status: String = "IDLE", // IDLE, CREATING, SUCCESS, FAILED
    val currentEmail: String = "",
    val currentPassword: String = "",
    val progress: Float = 0f,
    val log: String = "Ready"
)

class AccountViewModel(private val repository: AccountRepository) : ViewModel() {

    val allAccounts = repository.allAccounts

    private val _emulatorNodes = MutableStateFlow<List<EmulatorNodeState>>(
        (1..50).map { EmulatorNodeState(screenId = it) }
    )
    val emulatorNodes: StateFlow<List<EmulatorNodeState>> = _emulatorNodes.asStateFlow()

    private val _isBatchRunning = MutableStateFlow(false)
    val isBatchRunning: StateFlow<Boolean> = _isBatchRunning.asStateFlow()

    private val _pdfFile = MutableStateFlow<File?>(null)
    val pdfFile: StateFlow<File?> = _pdfFile.asStateFlow()

    private val _aiStatus = MutableStateFlow<String?>(null)
    val aiStatus: StateFlow<String?> = _aiStatus.asStateFlow()

    private var batchJob: Job? = null

    fun startBatchGeneration(context: Context) {
        if (_isBatchRunning.value) return
        _isBatchRunning.value = true

        batchJob = viewModelScope.launch {
            val generatedList = mutableListOf<AccountEntity>()
            val firstNames = listOf("alex", "maria", "lucas", "sofia", "mateo", "valentina", "carlos", "camila", "daniel", "lucia")
            val lastNames = listOf("silva", "santos", "garcia", "rodriguez", "martinez", "lopez", "gonzalez", "perez", "gomez", "fernandez")

            // Simulate 50 screens in parallel batches of 5
            val chunks = (1..50).chunked(5)
            for (chunk in chunks) {
                if (!(_isBatchRunning.value)) break
                val chunkJobs = chunk.map { screenId ->
                    launch(Dispatchers.IO) {
                        // Update node to creating
                        updateNode(screenId) { it.copy(status = "CREATING", progress = 0.2f, log = "Initializing proxy...") }
                        delay(Random.nextLong(300, 800))

                        updateNode(screenId) { it.copy(progress = 0.5f, log = "Solving CAPTCHA & SMS verification...") }
                        delay(Random.nextLong(400, 1000))

                        val fname = firstNames.random()
                        val lname = lastNames.random()
                        val randNum = Random.nextInt(100, 9999)
                        val email = "$fname.$lname$randNum@gmail.com"
                        val password = "Pass${Random.nextInt(1000, 9999)}!${Random.nextInt(10, 99)}"
                        val ip = "192.168.${Random.nextInt(1, 254)}.${Random.nextInt(1, 254)}"

                        updateNode(screenId) { it.copy(progress = 0.9f, log = "Registering credentials...") }
                        delay(Random.nextLong(200, 500))

                        val entity = AccountEntity(
                            email = email,
                            password = password,
                            screenId = screenId,
                            status = "SUCCESS",
                            ipAddress = ip
                        )
                        synchronized(generatedList) {
                            generatedList.add(entity)
                        }

                        updateNode(screenId) {
                            it.copy(
                                status = "SUCCESS",
                                currentEmail = email,
                                currentPassword = password,
                                progress = 1.0f,
                                log = "Account created successfully!"
                            )
                        }
                    }
                }
                // Wait for current chunk of 5 screens
                chunkJobs.forEach { it.join() }
            }

            // Save all generated accounts to Room DB
            if (generatedList.isNotEmpty()) {
                repository.insertAll(generatedList)
            }
            _isBatchRunning.value = false
        }
    }

    fun stopBatchGeneration() {
        batchJob?.cancel()
        _isBatchRunning.value = false
        _emulatorNodes.update { list ->
            list.map { if (it.status == "CREATING") it.copy(status = "IDLE", log = "Stopped") else it }
        }
    }

    fun resetNodes() {
        stopBatchGeneration()
        _emulatorNodes.value = (1..50).map { EmulatorNodeState(screenId = it) }
        _pdfFile.value = null
    }

    private fun updateNode(screenId: Int, update: (EmulatorNodeState) -> EmulatorNodeState) {
        _emulatorNodes.update { list ->
            list.map { if (it.screenId == screenId) update(it) else it }
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            repository.deleteAll()
            resetNodes()
        }
    }

    fun generatePdfReport(context: Context, accounts: List<AccountEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (accounts.isNotEmpty()) {
                val file = PdfGenerator.generateAccountsPdf(context, accounts)
                _pdfFile.value = file
            }
        }
    }

    fun generateSmartProfilesWithGemini(apiKey: String) {
        if (apiKey.isBlank()) {
            _aiStatus.value = "Error: Gemini API Key is missing. Configure in Settings or Secrets panel."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _aiStatus.value = "Consulting Gemini AI for secure naming patterns..."
            try {
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = "Give 3 professional security tips for managing bulk email accounts securely in JSON format.")))),
                    generationConfig = GenerationConfig(temperature = 0.7f)
                )
                val response = GeminiClient.service.generateContent(apiKey, request)
                val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No AI response"
                _aiStatus.value = "Gemini AI Tips: $text"
            } catch (e: Exception) {
                _aiStatus.value = "Gemini AI Error: ${e.localizedMessage}"
            }
        }
    }
}
