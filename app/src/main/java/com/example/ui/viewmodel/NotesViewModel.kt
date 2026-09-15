package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppRepository
import com.example.data.local.BlockEntity
import com.example.data.local.BlockType
import com.example.data.local.DocumentEntity
import com.example.data.local.FolderEntity
import com.example.data.local.TableHelper
import com.example.data.local.WorkspaceEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppScreen {
    WORKSPACES,
    FILES,
    EDITOR,
    CALENDAR,
    TAGS,
    SETTINGS
}

enum class AppThemeSetting {
    SYSTEM,
    LIGHT,
    DARK,
    DYNAMIC
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.getDatabase(application))
    private val prefs = application.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    // Current Screen
    private val _currentScreen = MutableStateFlow(AppScreen.FILES)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Workspaces
    val allWorkspaces: StateFlow<List<WorkspaceEntity>> = repository.allWorkspaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeWorkspaceId = MutableStateFlow(prefs.getLong("active_workspace_id", 1L))
    val activeWorkspaceId: StateFlow<Long> = _activeWorkspaceId.asStateFlow()

    val activeWorkspace: StateFlow<WorkspaceEntity?> = combine(allWorkspaces, _activeWorkspaceId) { list, id ->
        list.find { it.id == id } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Folders for active workspace
    @OptIn(ExperimentalCoroutinesApi::class)
    val foldersForActiveWorkspace: StateFlow<List<FolderEntity>> = _activeWorkspaceId
        .flatMapLatest { id ->
            if (id <= 0) flowOf(emptyList()) else repository.getFoldersForWorkspace(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Documents for active workspace
    @OptIn(ExperimentalCoroutinesApi::class)
    val documentsForActiveWorkspace: StateFlow<List<DocumentEntity>> = _activeWorkspaceId
        .flatMapLatest { id ->
            if (id <= 0) flowOf(emptyList()) else repository.getDocumentsForWorkspace(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Document in Editor
    private val _activeDocumentId = MutableStateFlow<Long?>(null)
    val activeDocumentId: StateFlow<Long?> = _activeDocumentId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeDocument: StateFlow<DocumentEntity?> = _activeDocumentId
        .flatMapLatest { docId ->
            if (docId == null) flowOf(null) else repository.getDocument(docId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeDocumentBlocks: StateFlow<List<BlockEntity>> = _activeDocumentId
        .flatMapLatest { docId ->
            if (docId == null) flowOf(emptyList()) else repository.getBlocksForDocument(docId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Editor state: Read vs Write mode
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    // Find / Search in Editor
    private val _editorSearchQuery = MutableStateFlow("")
    val editorSearchQuery: StateFlow<String> = _editorSearchQuery.asStateFlow()

    private val _isSearchVisible = MutableStateFlow(false)
    val isSearchVisible: StateFlow<Boolean> = _isSearchVisible.asStateFlow()

    // Calendar state
    private val _selectedCalendarDate = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis)
    val selectedCalendarDate: StateFlow<Long> = _selectedCalendarDate.asStateFlow()

    // Tags Screen state
    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    // Settings
    private val _themeSetting = MutableStateFlow(
        AppThemeSetting.valueOf(prefs.getString("theme_setting", AppThemeSetting.DYNAMIC.name) ?: AppThemeSetting.DYNAMIC.name)
    )
    val themeSetting: StateFlow<AppThemeSetting> = _themeSetting.asStateFlow()

    private val _dateFormatPattern = MutableStateFlow(
        prefs.getString("date_format_pattern", "dd.MM.yyyy") ?: "dd.MM.yyyy"
    )
    val dateFormatPattern: StateFlow<String> = _dateFormatPattern.asStateFlow()

    private val _storageLocation = MutableStateFlow(
        prefs.getString("storage_location", "Внутренняя память (SQLite DB)") ?: "Внутренняя память (SQLite DB)"
    )
    val storageLocation: StateFlow<String> = _storageLocation.asStateFlow()

    init {
        viewModelScope.launch {
            val initialWsId = repository.checkAndSeedDefaultData()
            if (_activeWorkspaceId.value <= 0 || _activeWorkspaceId.value == 1L) {
                setActiveWorkspace(initialWsId)
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setActiveWorkspace(workspaceId: Long) {
        _activeWorkspaceId.value = workspaceId
        prefs.edit().putLong("active_workspace_id", workspaceId).apply()
    }

    fun openDocumentInEditor(documentId: Long) {
        _activeDocumentId.value = documentId
        _currentScreen.value = AppScreen.EDITOR
    }

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    fun setEditMode(edit: Boolean) {
        _isEditMode.value = edit
    }

    fun toggleSearchVisible() {
        _isSearchVisible.value = !_isSearchVisible.value
        if (!_isSearchVisible.value) {
            _editorSearchQuery.value = ""
        }
    }

    fun setEditorSearchQuery(query: String) {
        _editorSearchQuery.value = query
    }

    fun setSelectedCalendarDate(dateMillis: Long) {
        _selectedCalendarDate.value = dateMillis
    }

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = tag
    }

    fun setThemeSetting(theme: AppThemeSetting) {
        _themeSetting.value = theme
        prefs.edit().putString("theme_setting", theme.name).apply()
    }

    fun setDateFormatPattern(pattern: String) {
        _dateFormatPattern.value = pattern
        prefs.edit().putString("date_format_pattern", pattern).apply()
    }

    fun setStorageLocation(path: String) {
        _storageLocation.value = path
        prefs.edit().putString("storage_location", path).apply()
    }

    fun formatDate(timestamp: Long?): String {
        if (timestamp == null || timestamp <= 0) return "Не указана"
        return try {
            val sdf = SimpleDateFormat(_dateFormatPattern.value, Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }

    // Workspace actions
    fun createWorkspace(name: String, icon: String, colorHex: String) {
        viewModelScope.launch {
            val id = repository.createWorkspace(name, icon, colorHex)
            setActiveWorkspace(id)
        }
    }

    fun renameWorkspace(workspace: WorkspaceEntity, newName: String) {
        viewModelScope.launch {
            repository.updateWorkspace(workspace.copy(name = newName))
        }
    }

    fun deleteWorkspace(workspace: WorkspaceEntity) {
        viewModelScope.launch {
            repository.deleteWorkspace(workspace)
            val list = allWorkspaces.value.filter { it.id != workspace.id }
            if (list.isNotEmpty()) {
                setActiveWorkspace(list.first().id)
            }
        }
    }

    fun cloneWorkspace(workspaceId: Long, newName: String) {
        viewModelScope.launch {
            val newId = repository.cloneWorkspace(workspaceId, newName)
            if (newId > 0) {
                setActiveWorkspace(newId)
            }
        }
    }

    // Folder actions
    fun createFolder(parentId: Long?, name: String, icon: String, colorHex: String) {
        val wsId = _activeWorkspaceId.value
        if (wsId <= 0) return
        viewModelScope.launch {
            repository.createFolder(wsId, parentId, name, icon, colorHex)
        }
    }

    fun updateFolder(folder: FolderEntity) {
        viewModelScope.launch {
            repository.updateFolder(folder)
        }
    }

    fun deleteFolder(folder: FolderEntity) {
        viewModelScope.launch {
            repository.deleteFolder(folder)
        }
    }

    // Document actions
    fun createDocument(folderId: Long?, title: String, icon: String = "📝", colorHex: String = "#3B82F6", calendarDate: Long? = null) {
        val wsId = _activeWorkspaceId.value
        if (wsId <= 0) return
        viewModelScope.launch {
            val id = repository.createDocument(
                workspaceId = wsId,
                folderId = folderId,
                title = title,
                icon = icon,
                colorHex = colorHex,
                calendarDate = calendarDate
            )
            openDocumentInEditor(id)
            _isEditMode.value = true
        }
    }

    fun updateDocument(document: DocumentEntity) {
        viewModelScope.launch {
            repository.updateDocument(document)
        }
    }

    fun updateDocumentColor(colorHex: String) {
        val doc = activeDocument.value ?: return
        viewModelScope.launch {
            repository.updateDocument(doc.copy(colorHex = colorHex))
        }
    }

    fun updateDocumentIcon(icon: String) {
        val doc = activeDocument.value ?: return
        viewModelScope.launch {
            repository.updateDocument(doc.copy(icon = icon))
        }
    }

    fun updateDocumentCalendarDate(calendarDate: Long?) {
        val doc = activeDocument.value ?: return
        viewModelScope.launch {
            repository.updateDocument(doc.copy(calendarDate = calendarDate))
        }
    }

    fun updateDocumentStatusTag(status: String) {
        val doc = activeDocument.value ?: return
        viewModelScope.launch {
            repository.updateDocument(doc.copy(statusTag = status))
        }
    }

    fun addTagToDocument(newTag: String) {
        val doc = activeDocument.value ?: return
        val currentTags = doc.tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
        val cleaned = newTag.trim().removePrefix("#")
        if (cleaned.isNotEmpty() && !currentTags.contains(cleaned)) {
            currentTags.add(cleaned)
            val updatedCsv = currentTags.joinToString(",")
            viewModelScope.launch {
                repository.updateDocument(doc.copy(tagsCsv = updatedCsv))
            }
        }
    }

    fun removeTagFromDocument(tagToRemove: String) {
        val doc = activeDocument.value ?: return
        val currentTags = doc.tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() && it != tagToRemove }
        val updatedCsv = currentTags.joinToString(",")
        viewModelScope.launch {
            repository.updateDocument(doc.copy(tagsCsv = updatedCsv))
        }
    }

    fun deleteDocument(document: DocumentEntity) {
        viewModelScope.launch {
            if (_activeDocumentId.value == document.id) {
                _activeDocumentId.value = null
            }
            repository.deleteDocument(document)
        }
    }

    // Block actions
    fun addBlock(type: BlockType, content: String = "") {
        val docId = _activeDocumentId.value ?: return
        viewModelScope.launch {
            repository.addBlock(docId, type, content)
        }
    }

    fun updateBlock(block: BlockEntity) {
        viewModelScope.launch {
            repository.updateBlock(block)
        }
    }

    fun deleteBlock(block: BlockEntity) {
        viewModelScope.launch {
            repository.deleteBlock(block)
        }
    }

    fun duplicateBlock(block: BlockEntity) {
        viewModelScope.launch {
            repository.duplicateBlock(block)
        }
    }

    fun moveBlockUp(block: BlockEntity) {
        viewModelScope.launch {
            repository.moveBlockUp(block)
        }
    }

    fun moveBlockDown(block: BlockEntity) {
        viewModelScope.launch {
            repository.moveBlockDown(block)
        }
    }

    // Export helpers
    fun getMarkdownExport(): String {
        val doc = activeDocument.value ?: return ""
        val blocks = activeDocumentBlocks.value
        return repository.exportToMarkdown(doc, blocks, ::formatDate)
    }

    fun getPlainTextExport(): String {
        val doc = activeDocument.value ?: return ""
        val blocks = activeDocumentBlocks.value
        return repository.exportToPlainText(doc, blocks, ::formatDate)
    }
}
