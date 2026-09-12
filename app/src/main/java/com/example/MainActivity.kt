package com.example

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import android.content.Context
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
import android.content.Intent
import android.speech.RecognizerIntent
import android.app.Activity
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.AppTheme
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBackgroundLight
import com.example.ui.theme.WorksheetStudioTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WorksheetData(
    val title: String,
    val intro: String,
    val sections: List<WorksheetSection>
)

@Serializable
data class WorksheetSection(
    val title: String,
    val content: String
)

enum class Level(val code: String, val displayName: String) {
    A1("A1", "A1 • Beginner"),
    A2("A2", "A2 • Elementary"),
    B1("B1", "B1 • Intermediate"),
    B2("B2", "B2 • Upper-Intermediate"),
    C1("C1", "C1 • Advanced"),
    C2("C2", "C2 • Proficiency")
}

enum class Category(val displayName: String) {
    GrammarAndSyntax("Grammar & Syntax"),
    ReadingAndComprehension("Reading Comprehension"),
    VocabularyAndIdioms("Vocabulary & Phonics"),
    ExamWorksheets("Exam Preparation (IELTS / Cambridge)"),
    WritingAndComposition("Writing & Sentence Composition")
}

enum class TemplateType(val displayName: String) {
    Standard("Comprehensive English Mix"),
    FillInTheBlanks("Fill-in-the-Blanks (Cloze Test)"),
    Matching("Vocabulary & Meaning Matching"),
    MultipleChoice("Multiple Choice (Grammar & Context)"),
    TrueFalse("True / False / Not Given"),
    SentenceCorrection("Error Correction & Rewriting"),
    ReadingComprehension("Reading Passage & Questions")
}

class WorksheetViewModel : ViewModel() {
    private val _theme = MutableStateFlow(AppTheme.OBSIDIAN)
    val theme: StateFlow<AppTheme> = _theme

    private val _customizationSettings = MutableStateFlow(
        WorksheetCustomizationSettings(
            font = WorksheetFont.SANS_SERIF,
            accentColor = WorksheetAccentColor.OXFORD_NAVY,
            layoutTemplate = WorksheetLayoutTemplate.MODERN_CARDS,
            teacherName = "Mr. Zaafouri Abdelmalek"
        )
    )
    val customizationSettings: StateFlow<WorksheetCustomizationSettings> = _customizationSettings

    private val _worksheetFont = MutableStateFlow(WorksheetFont.SANS_SERIF)
    val worksheetFont: StateFlow<WorksheetFont> = _worksheetFont

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _level = MutableStateFlow(Level.B1)
    val level: StateFlow<Level> = _level

    private val _category = MutableStateFlow(Category.GrammarAndSyntax)
    val category: StateFlow<Category> = _category

    private val _templateType = MutableStateFlow(TemplateType.FillInTheBlanks)
    val templateType: StateFlow<TemplateType> = _templateType

    private val _prompt = MutableStateFlow("Present Perfect vs Past Simple in daily life conversations")
    val prompt: StateFlow<String> = _prompt

    private val _headerLogoUri = MutableStateFlow<Uri?>(null)
    val headerLogoUri: StateFlow<Uri?> = _headerLogoUri

    private val _referenceImageUri = MutableStateFlow<Uri?>(null)
    val referenceImageUri: StateFlow<Uri?> = _referenceImageUri

    private val _worksheetVariations = MutableStateFlow<List<WorksheetData>>(emptyList())
    val worksheetVariations: StateFlow<List<WorksheetData>> = _worksheetVariations

    private val _selectedVariationIndex = MutableStateFlow(0)
    val selectedVariationIndex: StateFlow<Int> = _selectedVariationIndex

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _studentAnswers = MutableStateFlow("")
    val studentAnswers: StateFlow<String> = _studentAnswers

    private val _aiFeedback = MutableStateFlow<String?>(null)
    val aiFeedback: StateFlow<String?> = _aiFeedback

    private val _isEvaluating = MutableStateFlow(false)
    val isEvaluating: StateFlow<Boolean> = _isEvaluating

    fun setTheme(theme: AppTheme) { _theme.value = theme }

    fun updateCustomizationSettings(settings: WorksheetCustomizationSettings) {
        _customizationSettings.value = settings
        _worksheetFont.value = settings.font
    }

    fun setWorksheetFont(font: WorksheetFont) {
        _worksheetFont.value = font
        _customizationSettings.value = _customizationSettings.value.copy(font = font)
    }

    fun setAccentColor(color: WorksheetAccentColor) {
        _customizationSettings.value = _customizationSettings.value.copy(accentColor = color)
    }

    fun setLayoutTemplate(template: WorksheetLayoutTemplate) {
        _customizationSettings.value = _customizationSettings.value.copy(layoutTemplate = template)
    }

    fun applyStylePreset(preset: StylePreset) {
        _customizationSettings.value = _customizationSettings.value.copy(
            font = preset.font,
            accentColor = preset.accentColor,
            layoutTemplate = preset.layoutTemplate,
            showCandidateHeader = preset.showCandidateHeader,
            showScoreBox = preset.showScoreBox
        )
        _worksheetFont.value = preset.font
    }

    fun toggleWorksheetFont() {
        val next = when (_customizationSettings.value.font) {
            WorksheetFont.SANS_SERIF -> WorksheetFont.SERIF
            WorksheetFont.SERIF -> WorksheetFont.MONOSPACE
            WorksheetFont.MONOSPACE -> WorksheetFont.CURSIVE
            WorksheetFont.CURSIVE -> WorksheetFont.SANS_SERIF
        }
        setWorksheetFont(next)
    }
    fun toggleDarkMode() { _isDarkMode.value = !_isDarkMode.value }
    fun setLevel(level: Level) { _level.value = level }
    fun setCategory(category: Category) { _category.value = category }
    fun setTemplateType(type: TemplateType) { _templateType.value = type }
    fun setPrompt(prompt: String) { _prompt.value = prompt }
    fun selectVariation(index: Int) { _selectedVariationIndex.value = index }
    fun setHeaderLogoUri(uri: Uri?) { _headerLogoUri.value = uri }
    fun setReferenceImageUri(uri: Uri?) { _referenceImageUri.value = uri }
    fun setStudentAnswers(answers: String) { _studentAnswers.value = answers }
    fun resetFeedback() {
        _studentAnswers.value = ""
        _aiFeedback.value = null
    }

    private val _variationCount = MutableStateFlow(3)
    val variationCount: StateFlow<Int> = _variationCount

    fun setVariationCount(count: Int) { _variationCount.value = count }
    fun applySampleTopic(topic: String) { _prompt.value = topic }

    fun generateWorksheet(context: Context, count: Int = _variationCount.value) {
        if (_prompt.value.isBlank()) return
        _isLoading.value = true
        
        val fullPrompt = "You are creating an authentic English Language educational worksheet for the English subject ONLY.\n" +
                "Target CEFR Level: ${_level.value.displayName} (${_level.value.code}).\n" +
                "English Curriculum Domain: ${_category.value.displayName}.\n" +
                "Exercise Format: ${_templateType.value.displayName}.\n" +
                "English Topic / Target Grammar: ${_prompt.value}.\n\n" +
                "Pedagogical Guidelines:\n" +
                "- The worksheet MUST focus strictly and exclusively on the English subject (ESL, EFL, English Language Arts).\n" +
                "- Tailor all vocabulary, grammar complexity, and reading comprehension difficulty precisely to CEFR ${_level.value.code}.\n" +
                "- Include an engaging educational title suitable for an A4 classroom handout.\n" +
                "- Include concise student instructions/intro.\n" +
                "- Provide 3-5 comprehensive exercises strictly adhering to the chosen format (${_templateType.value.displayName}).\n" +
                "- In the final section, ALWAYS include a complete 'Answer Key & Teacher Solutions' section with clear answers and pedagogical explanations."
        
        viewModelScope.launch {
            var base64Image: String? = null
            _referenceImageUri.value?.let { uri ->
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bytes = inputStream.readBytes()
                        base64Image = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val content = generateWorksheetVariations(fullPrompt, base64Image, count)
            try {
                val parsedData = Json { ignoreUnknownKeys = true }.decodeFromString<List<WorksheetData>>(content)
                _worksheetVariations.value = parsedData
                _selectedVariationIndex.value = 0
                _studentAnswers.value = ""
                _aiFeedback.value = null
            } catch (e: Exception) {
                _worksheetVariations.value = listOf(WorksheetData("Error", "Could not parse JSON. Please try again.", emptyList()))
                _selectedVariationIndex.value = 0
            }
            _isLoading.value = false
        }
    }

    fun evaluateStudentAnswers() {
        val currentData = _worksheetVariations.value.getOrNull(_selectedVariationIndex.value) ?: return
        if (_studentAnswers.value.isBlank()) return
        
        _isEvaluating.value = true
        
        val worksheetContext = Json.encodeToString(WorksheetData.serializer(), currentData)
        
        viewModelScope.launch {
            val feedback = evaluateAnswers(worksheetContext, _studentAnswers.value)
            _aiFeedback.value = feedback
            _isEvaluating.value = false
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WorksheetViewModel = viewModel()
            val currentTheme by viewModel.theme.collectAsState()
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            
            WorksheetStudioTheme(themeStyle = currentTheme, isDarkMode = isDarkMode) {
                WorksheetStudioApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorksheetStudioApp(viewModel: WorksheetViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showThemeModal by remember { mutableStateOf(false) }
    
    val pickLogo = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.setHeaderLogoUri(uri)
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "English Worksheet Studio",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Exclusively for English Subject & CEFR",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                Spacer(Modifier.height(12.dp))
                
                // Teacher Profile Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "ZA",
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Mr. Zaafouri Abdelmalek",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "English Instructor",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(Modifier.height(12.dp))
                
                Text(
                    "Themes",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                AppTheme.values().forEach { theme ->
                    NavigationDrawerItem(
                        label = { Text(theme.name.replace("_", " ")) },
                        selected = viewModel.theme.collectAsState().value == theme,
                        onClick = { 
                            viewModel.setTheme(theme)
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            unselectedContainerColor = Color.Transparent,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Spacer(Modifier.height(24.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "Personalization",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                val currentLogo by viewModel.headerLogoUri.collectAsState()
                
                NavigationDrawerItem(
                    label = { Text(if (currentLogo == null) "Upload Logo" else "Change Logo") },
                    selected = false,
                    onClick = { pickLogo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                if (currentLogo != null) {
                    NavigationDrawerItem(
                        label = { Text("Remove Logo") },
                        selected = false,
                        onClick = { viewModel.setHeaderLogoUri(null) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = MaterialTheme.colorScheme.error
                        )
                    )
                }

                Spacer(Modifier.height(24.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(Modifier.height(16.dp))

                Text(
                    "Theme & Visual Styles",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                val customSettings by viewModel.customizationSettings.collectAsState()

                NavigationDrawerItem(
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Worksheet Theme Studio", fontWeight = FontWeight.Bold)
                                Text("${customSettings.layoutTemplate.displayName} • ${customSettings.accentColor.displayName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showThemeModal = true
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    "Worksheet Typography",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )

                val currentFont by viewModel.worksheetFont.collectAsState()
                WorksheetFont.values().forEach { font ->
                    NavigationDrawerItem(
                        label = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${font.displayName} (${font.subtitle})",
                                    fontFamily = font.fontFamily,
                                    fontSize = 12.5.sp
                                )
                                if (currentFont == font) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        selected = currentFont == font,
                        onClick = { viewModel.setWorksheetFont(font) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            unselectedContainerColor = Color.Transparent,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        }
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        var isFocusPrintMode by remember { mutableStateOf(false) }

        if (showThemeModal) {
            val customizationSettings by viewModel.customizationSettings.collectAsState()
            WorksheetThemeCustomizationModal(
                settings = customizationSettings,
                onDismiss = { showThemeModal = false },
                onUpdateSettings = { updated ->
                    viewModel.updateCustomizationSettings(updated)
                }
            )
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (!isFocusPrintMode) {
                    TopAppBar(
                        title = {
                            Column {
                                Text("English Worksheet Studio", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("Mr. Zaafouri Abdelmalek", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                        val isDarkMode by viewModel.isDarkMode.collectAsState()
                        
                        // Prominent Night Mode / Light Mode Toggle Button in TopAppBar
                        FilledTonalButton(
                            onClick = { viewModel.toggleDarkMode() },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isDarkMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (isDarkMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(
                                if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Toggle Night Mode",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (isDarkMode) "Night" else "Light",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(onClick = { isFocusPrintMode = true }) {
                            Icon(
                                Icons.Filled.Visibility,
                                contentDescription = "Focus Print View",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showThemeModal = true }) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = "Worksheet Theme Studio",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                            actionIconContentColor = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isFocusPrintMode) PaddingValues(0.dp) else paddingValues)
            ) {
                if (isFocusPrintMode) {
                    // Fullscreen Focus Print View: Dashboard hidden, A4 Sheet maximized
                    WorksheetPreview(
                        viewModel = viewModel,
                        snackbarHostState = snackbarHostState,
                        onOpenCustomizer = { showThemeModal = true },
                        isFocusPrintMode = true,
                        onToggleFocusPrintMode = { isFocusPrintMode = false },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                } else {
                    val isTablet = maxWidth >= 700.dp
                    // Dashboard is ALWAYS on the LEFT of the screen
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (isTablet) 16.dp else 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(if (isTablet) 16.dp else 10.dp)
                    ) {
                        // Configuration Dashboard ALWAYS on the LEFT
                        Column(
                            modifier = Modifier
                                .weight(if (isTablet) 1.15f else 1.12f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                        ) {
                            GlassAiArea(
                                viewModel = viewModel,
                                isCompact = !isTablet,
                                onOpenCustomizer = { showThemeModal = true }
                            )
                        }
                        // Preview on the RIGHT
                        WorksheetPreview(
                            viewModel = viewModel,
                            snackbarHostState = snackbarHostState,
                            onOpenCustomizer = { showThemeModal = true },
                            isFocusPrintMode = false,
                            onToggleFocusPrintMode = { isFocusPrintMode = true },
                            modifier = Modifier
                                .weight(if (isTablet) 1.25f else 1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassAiArea(
    viewModel: WorksheetViewModel,
    isCompact: Boolean = false,
    onOpenCustomizer: () -> Unit = {}
) {
    val level by viewModel.level.collectAsState()
    val category by viewModel.category.collectAsState()
    val templateType by viewModel.templateType.collectAsState()
    val prompt by viewModel.prompt.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val referenceImageUri by viewModel.referenceImageUri.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val variationCount by viewModel.variationCount.collectAsState()
    val customizationSettings by viewModel.customizationSettings.collectAsState()
    
    val context = LocalContext.current
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            viewModel.setReferenceImageUri(uri)
        }
    }
    
    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.get(0)
            if (!spokenText.isNullOrEmpty()) {
                viewModel.setPrompt(spokenText)
            }
        }
    }

    // Authentic Frosted Glass Gradient Background
    val glassBg = if (isDarkMode) {
        Brush.linearGradient(
            colors = listOf(
                Color(0x38283548),
                Color(0x1F1E293B),
                Color(0x140F172A),
                Color(0x2E1E293B)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xEBFFFFFF),
                Color(0xD9F8FAFC),
                Color(0xE6F1F5F9)
            ),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    // Specular Highlight Rim Border
    val glassBorderBrush = if (isDarkMode) {
        Brush.linearGradient(
            colors = listOf(
                Color(0x70FFFFFF),
                Color(0x22FFFFFF),
                Color(0x10FFFFFF),
                Color(0x40FFFFFF)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xCCFFFFFF),
                Color(0x50CBD5E1),
                Color(0x80FFFFFF)
            )
        )
    }

    val subPanelBg = if (isDarkMode) Color(0x24FFFFFF) else Color(0x80FFFFFF)
    val subPanelBorder = if (isDarkMode) Color(0x33FFFFFF) else Color(0x4D94A3B8)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Ambient chromatic backdrop illumination for authentic depth
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isDarkMode) Color(0x4D6366F1) else Color(0x293B82F6),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.12f, size.height * 0.08f),
                        radius = size.width * 0.7f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isDarkMode) Color(0x40EC4899) else Color(0x1EF43F5E),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.9f, size.height * 0.45f),
                        radius = size.width * 0.6f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isDarkMode) Color(0x3DEAB308) else Color(0x20F59E0B),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.95f),
                        radius = size.width * 0.65f
                    )
                )
            }
            .clip(RoundedCornerShape(24.dp))
            .background(brush = glassBg)
            .border(
                width = 1.4.dp,
                brush = glassBorderBrush,
                shape = RoundedCornerShape(24.dp)
            )
            .animatedBorder(
                borderWidth = if (isLoading) 2.5.dp else 1.2.dp,
                cornerRadius = 24.dp,
                borderColors = if (isLoading) listOf(
                    MaterialTheme.colorScheme.primary,
                    Color(0xFF818CF8),
                    Color(0xFFFFC107),
                    MaterialTheme.colorScheme.primary
                ) else listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                    Color.Transparent,
                    Color(0x33818CF8),
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                ),
                durationMillis = if (isLoading) 1500 else 4800,
                isActive = true
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) 14.dp else 20.dp)
        ) {
            // Header: Grand AI Studio Engine with Glowing Badge & Live Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(if (isCompact) 38.dp else 46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color.White,
                            modifier = Modifier.size(if (isCompact) 20.dp else 24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "English AI Studio",
                            style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            "Gemini 2.5 • CEFR A1–C2 • English Only",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Quick Night Mode Switch inside Dashboard
                    Surface(
                        onClick = { viewModel.toggleDarkMode() },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDarkMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Toggle Night Mode",
                                tint = if (isDarkMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isDarkMode) "Night" else "Light",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (isDarkMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Frosted Status Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDarkMode) Color(0x33FFFFFF) else Color(0x80E2E8F0),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0x4DFFFFFF) else Color(0x66CBD5E1))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (isLoading) Color(0xFFF59E0B) else Color(0xFF10B981))
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = if (isLoading) "GENERATING" else "ENGLISH ONLY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subpanel 1: Curriculum & Format Settings
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(subPanelBg)
                    .border(0.8.dp, subPanelBorder, RoundedCornerShape(16.dp))
                    .padding(if (isCompact) 10.dp else 14.dp)
            ) {
                Text(
                    "CEFR ENGLISH CURRICULUM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (isCompact) {
                    LevelDropdown(level = level, onLevelSelected = { viewModel.setLevel(it) })
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryDropdown(category = category, onCategorySelected = { viewModel.setCategory(it) })
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            LevelDropdown(level = level, onLevelSelected = { viewModel.setLevel(it) })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            CategoryDropdown(category = category, onCategorySelected = { viewModel.setCategory(it) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                TemplateDropdown(templateType = templateType, onTemplateSelected = { viewModel.setTemplateType(it) })
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subpanel 2: Topic & Pedagogical Prompt Instructions (ENLARGED)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(subPanelBg)
                    .border(0.8.dp, subPanelBorder, RoundedCornerShape(16.dp))
                    .padding(if (isCompact) 10.dp else 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ENGLISH TOPIC & TARGET GRAMMAR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        "${prompt.length} chars",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Enlarged Multi-line Text Field
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { viewModel.setPrompt(it) },
                    placeholder = {
                        Text(
                            "Enter English topic, grammar rules, vocabulary theme, or reading text (e.g. Present Perfect vs Past Simple, Phrasal Verbs, Relative Clauses)...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 96.dp, max = 160.dp),
                    maxLines = 6,
                    minLines = 3,
                    trailingIcon = {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak English worksheet topic...")
                                }
                                try {
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }) {
                                Icon(Icons.Default.Mic, contentDescription = "Dictate English Topic", tint = MaterialTheme.colorScheme.primary)
                            }
                            if (prompt.isNotBlank()) {
                                IconButton(onClick = { viewModel.setPrompt("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDarkMode) Color(0x18000000) else Color(0x22FFFFFF),
                        unfocusedContainerColor = if (isDarkMode) Color(0x10000000) else Color(0x15FFFFFF),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (isDarkMode) Color(0x40FFFFFF) else Color(0x4094A3B8)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Inspiration Chips Row
                Text(
                    "English Curricula Inspiration:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                val samplePills = listOf(
                    "Present Perfect vs Past Simple",
                    "Conditionals (0, 1st, 2nd & 3rd)",
                    "Passive Voice in News Reporting",
                    "Phrasal Verbs in Daily Context",
                    "Reading: Modern Technology & Society",
                    "Modals of Deduction (Must/Might/Can't)",
                    "Reported Speech & Indirect Questions",
                    "Common Idioms & Collocations",
                    "Articles (A, An, The & Zero Article)",
                    "IELTS Academic Reading: Science & Innovation"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    samplePills.forEach { pill ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isDarkMode) Color(0x26FFFFFF) else Color(0x59E2E8F0),
                            border = BorderStroke(0.8.dp, if (isDarkMode) Color(0x33FFFFFF) else Color(0x50CBD5E1)),
                            modifier = Modifier.clickable { viewModel.applySampleTopic(pill) }
                        ) {
                            Text(
                                text = pill,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subpanel 3: Style Reference & Scanning
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(subPanelBg)
                    .border(0.8.dp, subPanelBorder, RoundedCornerShape(16.dp))
                    .padding(if (isCompact) 10.dp else 14.dp)
            ) {
                Text(
                    "TEXTBOOK SCAN & STYLE REFERENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (isDarkMode) Color(0x33FFFFFF) else Color(0x6694A3B8))
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Scan", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (referenceImageUri == null) "Scan / Upload Textbook Page" else "Textbook Page Attached",
                            maxLines = 1
                        )
                    }

                    if (referenceImageUri != null) {
                        IconButton(
                            onClick = { viewModel.setReferenceImageUri(null) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove Image", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subpanel 4: Visual Theme & Styling Customizer
            WorksheetThemeDashboardBar(
                settings = customizationSettings,
                onOpenCustomizer = onOpenCustomizer,
                onApplyPreset = { viewModel.applyStylePreset(it) },
                onSelectFont = { viewModel.setWorksheetFont(it) },
                onSelectColor = { viewModel.setAccentColor(it) },
                onSelectTemplate = { viewModel.setLayoutTemplate(it) }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Generation Section (Enlarged Split Action with Variations)
            PremiumSplitButton(
                mainText = "Generate English Worksheets",
                subText = "$variationCount Variations • Complete Answer Key",
                mainIcon = Icons.Default.AutoAwesome,
                isLoading = isLoading,
                enabled = prompt.isNotBlank() && !isLoading,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onMainClick = { viewModel.generateWorksheet(context) },
                modifier = Modifier.fillMaxWidth()
            ) { closeMenu ->
                Text(
                    "Batch Generation Settings",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold
                )
                listOf(1, 3, 5).forEach { count ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    when (count) {
                                        1 -> "1 Focused A4 Sheet"
                                        3 -> "3 Differentiated Variations"
                                        else -> "5 Complete Class Variations"
                                    }
                                )
                                if (variationCount == count) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            viewModel.setVariationCount(count)
                            closeMenu()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LevelDropdown(
    level: Level,
    onLevelSelected: (Level) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = level.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("CEFR Level") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Level.values().forEach { l ->
                DropdownMenuItem(
                    text = { Text(l.displayName, fontWeight = if (l == level) FontWeight.Bold else FontWeight.Normal) },
                    onClick = {
                        onLevelSelected(l)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    category: Category,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = category.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("English Domain") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Category.values().forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.displayName, fontWeight = if (c == category) FontWeight.Bold else FontWeight.Normal) },
                    onClick = {
                        onCategorySelected(c)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemplateDropdown(
    templateType: TemplateType,
    onTemplateSelected: (TemplateType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = templateType.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Exercise Format") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            TemplateType.values().forEach { t ->
                DropdownMenuItem(
                    text = { Text(t.displayName, fontWeight = if (t == templateType) FontWeight.Bold else FontWeight.Normal) },
                    onClick = {
                        onTemplateSelected(t)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun WorksheetPreview(
    viewModel: WorksheetViewModel,
    snackbarHostState: SnackbarHostState? = null,
    onOpenCustomizer: () -> Unit = {},
    isFocusPrintMode: Boolean = false,
    onToggleFocusPrintMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val worksheetVariations by viewModel.worksheetVariations.collectAsState()
    val selectedVariationIndex by viewModel.selectedVariationIndex.collectAsState()
    val headerLogoUri by viewModel.headerLogoUri.collectAsState()
    val level by viewModel.level.collectAsState()
    val category by viewModel.category.collectAsState()
    val currentFont by viewModel.worksheetFont.collectAsState()
    val customSettings by viewModel.customizationSettings.collectAsState()
    var showPrintPreview by remember { mutableStateOf(false) }
    var isPageBreakPreviewMode by remember { mutableStateOf(false) }

    var pendingSavePdfFile by remember { mutableStateOf<java.io.File?>(null) }
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { targetUri ->
        if (targetUri != null && pendingSavePdfFile != null) {
            val success = PdfGenerator.writePdfToUri(context, pendingSavePdfFile!!, targetUri)
            if (success) {
                android.widget.Toast.makeText(context, "Worksheet PDF saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                if (snackbarHostState != null) {
                    coroutineScope.launch {
                        val res = snackbarHostState.showSnackbar(
                            message = "Worksheet saved successfully!",
                            actionLabel = "Open",
                            duration = SnackbarDuration.Long
                        )
                        if (res == SnackbarResult.ActionPerformed) {
                            PdfGenerator.openPdf(context, targetUri)
                        }
                    }
                }
            } else {
                android.widget.Toast.makeText(context, "Failed to write PDF file", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    val onDownloadPdf: () -> Unit = {
        if (worksheetVariations.isNotEmpty()) {
            val data = worksheetVariations[selectedVariationIndex]
            try {
                val pdfFile = PdfGenerator.generatePdf(
                    context,
                    data,
                    headerLogoUri,
                    level.displayName,
                    category.displayName,
                    settings = customSettings
                )
                val result = PdfGenerator.downloadPdfToStorage(context, pdfFile, data.title)
                if (result != null) {
                    android.widget.Toast.makeText(context, "Saved to ${result.displayPath}", android.widget.Toast.LENGTH_SHORT).show()
                    if (snackbarHostState != null) {
                        coroutineScope.launch {
                            val snackbarRes = snackbarHostState.showSnackbar(
                                message = "Downloaded: ${result.fileName}",
                                actionLabel = "Open",
                                duration = SnackbarDuration.Long
                            )
                            if (snackbarRes == SnackbarResult.ActionPerformed) {
                                PdfGenerator.openPdf(context, result.uri)
                            }
                        }
                    }
                } else {
                    android.widget.Toast.makeText(context, "Failed to save PDF to storage", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Download error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isFocusPrintMode) {
                    Button(
                        onClick = onToggleFocusPrintMode,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit Focus Mode", modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Exit Focus", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    if (isFocusPrintMode) "Focus Print View (A4)" else "Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Quick Theme & Font Customizer chip in Preview header
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = customSettings.accentColor.primaryColor.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, customSettings.accentColor.primaryColor.copy(alpha = 0.35f)),
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onOpenCustomizer() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(customSettings.accentColor.primaryColor)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            customSettings.font.displayName,
                            fontFamily = customSettings.font.fontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = customSettings.accentColor.primaryColor
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "• ${customSettings.layoutTemplate.displayName}",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                val isDarkMode by viewModel.isDarkMode.collectAsState()
                IconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = "Toggle Night Mode",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Focus Print View toggle button
                IconButton(
                    onClick = onToggleFocusPrintMode,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (isFocusPrintMode) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (isFocusPrintMode) "Exit Focus Print View" else "Focus Print View",
                        tint = if (isFocusPrintMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { onOpenCustomizer() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = "Worksheet Theme Studio",
                        tint = customSettings.accentColor.primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (worksheetVariations.isNotEmpty()) {
                    CompactSplitButton(
                        text = "Download PDF",
                        icon = Icons.Default.Download,
                        enabled = worksheetVariations.isNotEmpty(),
                        onPrimaryClick = { onDownloadPdf() }
                    ) { closeMenu ->
                        DropdownMenuItem(
                            text = { Text("Save to Downloads (A4)") },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                onDownloadPdf()
                                closeMenu()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Save As... (Choose Folder)") },
                            leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                val data = worksheetVariations[selectedVariationIndex]
                                try {
                                    val pdfFile = PdfGenerator.generatePdf(
                                        context,
                                        data,
                                        headerLogoUri,
                                        level.displayName,
                                        category.displayName,
                                        settings = customSettings
                                    )
                                    pendingSavePdfFile = pdfFile
                                    val sanitized = data.title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(28)
                                    createDocumentLauncher.launch("${sanitized}_A4.pdf")
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                closeMenu()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Share PDF") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                val data = worksheetVariations[selectedVariationIndex]
                                try {
                                    val pdfFile = PdfGenerator.generatePdf(
                                        context,
                                        data,
                                        headerLogoUri,
                                        level.displayName,
                                        category.displayName,
                                        settings = customSettings
                                    )
                                    PdfGenerator.sharePdf(context, pdfFile, data.title)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Export error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                closeMenu()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Print A4 Directly") },
                            leadingIcon = { Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                val data = worksheetVariations[selectedVariationIndex]
                                try {
                                    val pdfFile = PdfGenerator.generatePdf(
                                        context,
                                        data,
                                        headerLogoUri,
                                        level.displayName,
                                        category.displayName,
                                        settings = customSettings
                                    )
                                    PdfGenerator.printPdf(context, pdfFile, data.title)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Print error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                closeMenu()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("A4 Fullscreen Preview") },
                            leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                showPrintPreview = true
                                closeMenu()
                            }
                        )
                    }
                }

                TextButton(
                    onClick = { showPrintPreview = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = "Print Preview", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Print Preview", fontSize = 12.sp)
                }
            }
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (worksheetVariations.size > 1) {
                TabRow(
                    selectedTabIndex = selectedVariationIndex,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    worksheetVariations.forEachIndexed { index, _ ->
                        Tab(
                            selected = selectedVariationIndex == index,
                            onClick = { viewModel.selectVariation(index) },
                            text = { Text("Variation ${index + 1}", fontSize = 12.sp) }
                        )
                    }
                }
            } else {
                Spacer(Modifier.width(8.dp))
            }

            if (worksheetVariations.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Row(modifier = Modifier.padding(3.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (!isPageBreakPreviewMode) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (!isPageBreakPreviewMode) 1.dp else 0.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { isPageBreakPreviewMode = false }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (!isPageBreakPreviewMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Continuous",
                                    fontSize = 11.sp,
                                    fontWeight = if (!isPageBreakPreviewMode) FontWeight.Bold else FontWeight.Normal,
                                    color = if (!isPageBreakPreviewMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isPageBreakPreviewMode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shadowElevation = if (isPageBreakPreviewMode) 1.dp else 0.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { isPageBreakPreviewMode = true }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.ContentCut,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp),
                                    tint = if (isPageBreakPreviewMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Page Breaks",
                                    fontSize = 11.sp,
                                    fontWeight = if (isPageBreakPreviewMode) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isPageBreakPreviewMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isPageBreakPreviewMode && worksheetVariations.isNotEmpty()) {
            val data = worksheetVariations[selectedVariationIndex]
            val pages = remember(data) { WorksheetPaginator.paginate(data) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    pages.forEachIndexed { index, page ->
                        if (index > 0) {
                            PageBreakDivider(
                                previousPage = index,
                                nextPage = index + 1,
                                totalPages = pages.size
                            )
                        }

                        PhysicalA4Sheet(
                            data = data,
                            pageContent = page,
                            headerLogoUri = headerLogoUri,
                            level = level,
                            category = category,
                            settings = customSettings,
                            showMarginGuides = true
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .animateContentSize()
                    .animatedBorder(
                        borderWidth = 1.dp,
                        cornerRadius = 12.dp,
                        borderColors = listOf(
                            customSettings.accentColor.primaryColor.copy(alpha = 0.45f),
                            customSettings.accentColor.primaryColor.copy(alpha = 0.05f),
                            customSettings.accentColor.secondaryColor.copy(alpha = 0.25f),
                            customSettings.accentColor.primaryColor.copy(alpha = 0.45f)
                        ),
                        durationMillis = 6000,
                        isActive = worksheetVariations.isNotEmpty()
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (worksheetVariations.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = "English Worksheet",
                                    tint = customSettings.accentColor.primaryColor.copy(alpha = 0.5f),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "Your generated English A4 worksheet will appear here.",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "CEFR A1–C2 • Grammar • Reading • Vocabulary • Cambridge / IELTS",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        val data = worksheetVariations[selectedVariationIndex]
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            if (headerLogoUri != null) {
                                AsyncImage(
                                    model = headerLogoUri,
                                    contentDescription = "School / Personal Logo",
                                    modifier = Modifier
                                        .height(80.dp)
                                        .weight(1f, fill = false),
                                    alignment = Alignment.TopStart,
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            
                            val shareUrl = "https://worksheet.studio/share/${data.title.hashCode()}"
                            val qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=${android.net.Uri.encode(shareUrl)}"
                            AsyncImage(
                                model = qrUrl,
                                contentDescription = "QR Code for Digital Version",
                                modifier = Modifier.size(80.dp),
                                alignment = Alignment.TopEnd
                            )
                        }

                        // Candidate Header if enabled
                        if (customSettings.showCandidateHeader) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = customSettings.accentColor.tintBackground,
                                border = BorderStroke(1.dp, customSettings.accentColor.primaryColor.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Student: ____________________",
                                        fontFamily = customSettings.font.fontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        "Class: _______",
                                        fontFamily = customSettings.font.fontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        "Date: ____________",
                                        fontFamily = customSettings.font.fontFamily,
                                        fontSize = 11.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    if (customSettings.showScoreBox) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.dp, customSettings.accentColor.primaryColor)
                                        ) {
                                            Text(
                                                "Score: ___ / 20",
                                                fontFamily = customSettings.font.fontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = customSettings.accentColor.primaryColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = data.title,
                            color = customSettings.accentColor.primaryColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = customSettings.font.fontFamily,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = data.intro,
                            color = customSettings.accentColor.secondaryColor,
                            fontSize = 14.sp,
                            fontFamily = customSettings.font.fontFamily,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        data.sections.forEach { section ->
                            Text(
                                text = section.title,
                                color = customSettings.accentColor.primaryColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = customSettings.font.fontFamily,
                                modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
                            )
                            Text(
                                text = section.content,
                                color = Color.Black,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                fontFamily = customSettings.font.fontFamily,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        // Faux illustration / decoration
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .background(customSettings.accentColor.tintBackground, RoundedCornerShape(8.dp))
                                .border(1.dp, customSettings.accentColor.primaryColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Illustration / Notes Area", color = customSettings.accentColor.primaryColor, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        
        // Instant AI Feedback Section
        if (worksheetVariations.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            val studentAnswers by viewModel.studentAnswers.collectAsState()
            val aiFeedback by viewModel.aiFeedback.collectAsState()
            val isEvaluating by viewModel.isEvaluating.collectAsState()
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .animatedBorder(
                        borderWidth = if (isEvaluating) 2.dp else 1.2.dp,
                        cornerRadius = 16.dp,
                        borderColors = if (isEvaluating) listOf(
                            MaterialTheme.colorScheme.primary,
                            Color(0xFFFFB703),
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.primary
                        ) else listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        ),
                        durationMillis = if (isEvaluating) 1500 else 5000,
                        isActive = isEvaluating
                    ),
                colors = CardDefaults.cardColors(containerColor = if (isDarkMode) GlassBackground else GlassBackgroundLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Interactive Student Practice (English)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Enter answers to receive automated English grammar, spelling, and CEFR grading.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (aiFeedback == null) {
                        OutlinedTextField(
                            value = studentAnswers,
                            onValueChange = { viewModel.setStudentAnswers(it) },
                            placeholder = { Text("Type your English answers here (e.g. 1. went 2. had seen 3. is travelling)...") },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.evaluateStudentAnswers() },
                            modifier = Modifier.align(Alignment.End),
                            enabled = studentAnswers.isNotBlank() && !isEvaluating
                        ) {
                            if (isEvaluating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("Get AI English Feedback")
                            }
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("AI English Tutor Feedback", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(aiFeedback!!, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.resetFeedback() }) {
                            Text("Try Again")
                        }
                    }
                }
            }
        }
    }

    if (showPrintPreview && worksheetVariations.isNotEmpty()) {
        val data = worksheetVariations[selectedVariationIndex]
        PrintPreviewModal(
            data = data,
            headerLogoUri = headerLogoUri,
            level = level,
            category = category,
            settings = customSettings,
            onOpenThemeCustomizer = { onOpenCustomizer() },
            onPrint = {
                try {
                    val pdfFile = PdfGenerator.generatePdf(
                        context,
                        data,
                        headerLogoUri,
                        level.displayName,
                        category.displayName,
                        settings = customSettings
                    )
                    PdfGenerator.printPdf(context, pdfFile, data.title)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Print error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            onDownload = {
                onDownloadPdf()
            },
            onShare = {
                try {
                    val pdfFile = PdfGenerator.generatePdf(
                        context,
                        data,
                        headerLogoUri,
                        level.displayName,
                        category.displayName,
                        settings = customSettings
                    )
                    PdfGenerator.sharePdf(context, pdfFile, data.title)
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Export error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showPrintPreview = false }
        )
    }
}
