package com.example.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class AppRepository(private val database: AppDatabase) {
    private val workspaceDao = database.workspaceDao()
    private val folderDao = database.folderDao()
    private val documentDao = database.documentDao()
    private val blockDao = database.blockDao()

    val allWorkspaces: Flow<List<WorkspaceEntity>> = workspaceDao.getAllWorkspaces()

    fun getFoldersForWorkspace(workspaceId: Long): Flow<List<FolderEntity>> {
        return folderDao.getFoldersForWorkspace(workspaceId)
    }

    fun getDocumentsForWorkspace(workspaceId: Long): Flow<List<DocumentEntity>> {
        return documentDao.getDocumentsForWorkspace(workspaceId)
    }

    fun getDocument(documentId: Long): Flow<DocumentEntity?> {
        return documentDao.getDocumentById(documentId)
    }

    fun getBlocksForDocument(documentId: Long): Flow<List<BlockEntity>> {
        return blockDao.getBlocksForDocument(documentId)
    }

    suspend fun createWorkspace(name: String, icon: String, colorHex: String): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        workspaceDao.insertWorkspace(
            WorkspaceEntity(
                name = name,
                icon = icon,
                colorHex = colorHex,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun updateWorkspace(workspace: WorkspaceEntity) = withContext(Dispatchers.IO) {
        workspaceDao.updateWorkspace(workspace.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteWorkspace(workspace: WorkspaceEntity) = withContext(Dispatchers.IO) {
        workspaceDao.deleteWorkspace(workspace)
    }

    suspend fun cloneWorkspace(workspaceId: Long, newName: String): Long = withContext(Dispatchers.IO) {
        val sourceWorkspace = workspaceDao.getWorkspaceById(workspaceId) ?: return@withContext -1L
        val now = System.currentTimeMillis()
        val newWorkspaceId = workspaceDao.insertWorkspace(
            WorkspaceEntity(
                name = newName,
                icon = sourceWorkspace.icon,
                colorHex = sourceWorkspace.colorHex,
                createdAt = now,
                updatedAt = now
            )
        )

        // Clone folders
        val oldFolders = folderDao.getFoldersForWorkspaceSync(workspaceId)
        val oldToNewFolderMap = mutableMapOf<Long, Long>()

        // Copy root folders first, then child folders recursively
        suspend fun copyFolders(parentId: Long?, newParentId: Long?) {
            val children = oldFolders.filter { it.parentId == parentId }
            for (folder in children) {
                val createdFolderId = folderDao.insertFolder(
                    FolderEntity(
                        workspaceId = newWorkspaceId,
                        parentId = newParentId,
                        name = folder.name,
                        icon = folder.icon,
                        colorHex = folder.colorHex,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                oldToNewFolderMap[folder.id] = createdFolderId
                copyFolders(folder.id, createdFolderId)
            }
        }
        copyFolders(null, null)

        // Clone documents
        val oldDocs = documentDao.getDocumentsForWorkspaceSync(workspaceId)
        for (doc in oldDocs) {
            val newFolderId = doc.folderId?.let { oldToNewFolderMap[it] }
            val newDocId = documentDao.insertDocument(
                DocumentEntity(
                    workspaceId = newWorkspaceId,
                    folderId = newFolderId,
                    title = doc.title,
                    icon = doc.icon,
                    colorHex = doc.colorHex,
                    calendarDate = doc.calendarDate,
                    statusTag = doc.statusTag,
                    tagsCsv = doc.tagsCsv,
                    createdAt = now,
                    updatedAt = now
                )
            )

            // Clone blocks
            val oldBlocks = blockDao.getBlocksForDocumentSync(doc.id)
            val newBlocks = oldBlocks.map { block ->
                BlockEntity(
                    documentId = newDocId,
                    orderIndex = block.orderIndex,
                    type = block.type,
                    content = block.content,
                    isChecked = block.isChecked,
                    tableDataJson = block.tableDataJson
                )
            }
            blockDao.insertBlocks(newBlocks)
        }

        newWorkspaceId
    }

    suspend fun createFolder(
        workspaceId: Long,
        parentId: Long?,
        name: String,
        icon: String,
        colorHex: String
    ): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        folderDao.insertFolder(
            FolderEntity(
                workspaceId = workspaceId,
                parentId = parentId,
                name = name,
                icon = icon,
                colorHex = colorHex,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun updateFolder(folder: FolderEntity) = withContext(Dispatchers.IO) {
        folderDao.updateFolder(folder.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteFolder(folder: FolderEntity) = withContext(Dispatchers.IO) {
        // Find subfolders recursively and delete them
        val allFolders = folderDao.getFoldersForWorkspaceSync(folder.workspaceId)
        val toDelete = mutableSetOf<Long>()
        fun collectFolderIds(id: Long) {
            toDelete.add(id)
            val children = allFolders.filter { it.parentId == id }
            children.forEach { collectFolderIds(it.id) }
        }
        collectFolderIds(folder.id)

        // Delete all documents in these folders
        val allDocs = documentDao.getDocumentsForWorkspaceSync(folder.workspaceId)
        for (doc in allDocs) {
            if (doc.folderId in toDelete) {
                documentDao.deleteDocument(doc)
            }
        }
        for (fId in toDelete) {
            val f = folderDao.getFolderById(fId)
            if (f != null) folderDao.deleteFolder(f)
        }
    }

    suspend fun createDocument(
        workspaceId: Long,
        folderId: Long?,
        title: String,
        icon: String = "📝",
        colorHex: String = "#3B82F6",
        calendarDate: Long? = null,
        statusTag: String = "В процессе",
        tagsCsv: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val docId = documentDao.insertDocument(
            DocumentEntity(
                workspaceId = workspaceId,
                folderId = folderId,
                title = title,
                icon = icon,
                colorHex = colorHex,
                calendarDate = calendarDate,
                statusTag = statusTag,
                tagsCsv = tagsCsv,
                createdAt = now,
                updatedAt = now
            )
        )
        // Add an initial paragraph block
        blockDao.insertBlock(
            BlockEntity(
                documentId = docId,
                orderIndex = 0,
                type = BlockType.PARAGRAPH,
                content = ""
            )
        )
        docId
    }

    suspend fun updateDocument(document: DocumentEntity) = withContext(Dispatchers.IO) {
        documentDao.updateDocument(document.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteDocument(document: DocumentEntity) = withContext(Dispatchers.IO) {
        documentDao.deleteDocument(document)
    }

    suspend fun addBlock(
        documentId: Long,
        type: BlockType,
        content: String = "",
        isChecked: Boolean = false,
        tableDataJson: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val existing = blockDao.getBlocksForDocumentSync(documentId)
        val nextOrder = if (existing.isEmpty()) 0 else (existing.maxOf { it.orderIndex } + 1)
        val defaultTable = if (type == BlockType.TABLE && tableDataJson.isBlank()) {
            TableHelper.serializeTable(
                listOf(
                    listOf("Колонка 1", "Колонка 2"),
                    listOf("Данные 1", "Данные 2")
                )
            )
        } else {
            tableDataJson
        }
        val id = blockDao.insertBlock(
            BlockEntity(
                documentId = documentId,
                orderIndex = nextOrder,
                type = type,
                content = content,
                isChecked = isChecked,
                tableDataJson = defaultTable
            )
        )
        // Touch document update time
        documentDao.getDocumentByIdSync(documentId)?.let {
            documentDao.updateDocument(it.copy(updatedAt = System.currentTimeMillis()))
        }
        id
    }

    suspend fun updateBlock(block: BlockEntity) = withContext(Dispatchers.IO) {
        blockDao.updateBlock(block)
        documentDao.getDocumentByIdSync(block.documentId)?.let {
            documentDao.updateDocument(it.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteBlock(block: BlockEntity) = withContext(Dispatchers.IO) {
        blockDao.deleteBlock(block)
        // Re-index remaining blocks
        val remaining = blockDao.getBlocksForDocumentSync(block.documentId)
        remaining.forEachIndexed { index, b ->
            if (b.orderIndex != index) {
                blockDao.updateBlock(b.copy(orderIndex = index))
            }
        }
        documentDao.getDocumentByIdSync(block.documentId)?.let {
            documentDao.updateDocument(it.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun duplicateBlock(block: BlockEntity) = withContext(Dispatchers.IO) {
        val all = blockDao.getBlocksForDocumentSync(block.documentId).sortedBy { it.orderIndex }
        val targetIdx = all.indexOfFirst { it.id == block.id }
        if (targetIdx == -1) return@withContext

        // Increment order of subsequent blocks
        for (i in targetIdx + 1 until all.size) {
            val item = all[i]
            blockDao.updateBlock(item.copy(orderIndex = item.orderIndex + 1))
        }

        // Insert duplicate
        blockDao.insertBlock(
            BlockEntity(
                documentId = block.documentId,
                orderIndex = block.orderIndex + 1,
                type = block.type,
                content = block.content,
                isChecked = block.isChecked,
                tableDataJson = block.tableDataJson
            )
        )
        documentDao.getDocumentByIdSync(block.documentId)?.let {
            documentDao.updateDocument(it.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun moveBlockUp(block: BlockEntity) = withContext(Dispatchers.IO) {
        val all = blockDao.getBlocksForDocumentSync(block.documentId).sortedBy { it.orderIndex }
        val idx = all.indexOfFirst { it.id == block.id }
        if (idx > 0) {
            val prev = all[idx - 1]
            blockDao.updateBlock(prev.copy(orderIndex = block.orderIndex))
            blockDao.updateBlock(block.copy(orderIndex = prev.orderIndex))
            documentDao.getDocumentByIdSync(block.documentId)?.let {
                documentDao.updateDocument(it.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    suspend fun moveBlockDown(block: BlockEntity) = withContext(Dispatchers.IO) {
        val all = blockDao.getBlocksForDocumentSync(block.documentId).sortedBy { it.orderIndex }
        val idx = all.indexOfFirst { it.id == block.id }
        if (idx >= 0 && idx < all.size - 1) {
            val next = all[idx + 1]
            blockDao.updateBlock(next.copy(orderIndex = block.orderIndex))
            blockDao.updateBlock(block.copy(orderIndex = next.orderIndex))
            documentDao.getDocumentByIdSync(block.documentId)?.let {
                documentDao.updateDocument(it.copy(updatedAt = System.currentTimeMillis()))
            }
        }
    }

    suspend fun reorderBlocks(documentId: Long, orderedBlocks: List<BlockEntity>) = withContext(Dispatchers.IO) {
        orderedBlocks.forEachIndexed { index, block ->
            if (block.orderIndex != index) {
                blockDao.updateBlock(block.copy(orderIndex = index))
            }
        }
        documentDao.getDocumentByIdSync(documentId)?.let {
            documentDao.updateDocument(it.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun checkAndSeedDefaultData(): Long = withContext(Dispatchers.IO) {
        val existingWorkspaces = workspaceDao.getWorkspaceById(1)
        if (existingWorkspaces != null) {
            return@withContext 1L
        }

        val now = System.currentTimeMillis()
        val personalWsId = workspaceDao.insertWorkspace(
            WorkspaceEntity(
                name = "Личное",
                icon = "👤",
                colorHex = "#3B82F6",
                createdAt = now,
                updatedAt = now
            )
        )
        workspaceDao.insertWorkspace(
            WorkspaceEntity(
                name = "Работа & Проекты",
                icon = "💼",
                colorHex = "#10B981",
                createdAt = now,
                updatedAt = now
            )
        )

        // Seed folders
        val ideasFolderId = folderDao.insertFolder(
            FolderEntity(
                workspaceId = personalWsId,
                name = "Идеи и Мысли",
                icon = "💡",
                colorHex = "#F59E0B",
                createdAt = now,
                updatedAt = now
            )
        )
        val nestedFolderId = folderDao.insertFolder(
            FolderEntity(
                workspaceId = personalWsId,
                parentId = ideasFolderId,
                name = "Архив идей",
                icon = "📦",
                colorHex = "#8B5CF6",
                createdAt = now,
                updatedAt = now
            )
        )
        folderDao.insertFolder(
            FolderEntity(
                workspaceId = personalWsId,
                name = "Ежедневник",
                icon = "📅",
                colorHex = "#EC4899",
                createdAt = now,
                updatedAt = now
            )
        )

        // Today start of day for calendar note
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayMillis = cal.timeInMillis

        // Seed Welcome document
        val welcomeDocId = documentDao.insertDocument(
            DocumentEntity(
                workspaceId = personalWsId,
                folderId = null,
                title = "Добро пожаловать в Заметки",
                icon = "🚀",
                colorHex = "#3B82F6",
                calendarDate = todayMillis,
                statusTag = "Готово",
                tagsCsv = "руководство,возможности,важное",
                createdAt = now - 3600000,
                updatedAt = now
            )
        )

        val welcomeBlocks = listOf(
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 0,
                type = BlockType.HEADING_1,
                content = "Добро пожаловать в Заметки!"
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 1,
                type = BlockType.PARAGRAPH,
                content = "Это блочный редактор заметок с древовидной организацией папок, календарем, тегами и кастомизацией."
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 2,
                type = BlockType.DIVIDER,
                content = ""
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 3,
                type = BlockType.HEADING_2,
                content = "Ключевые возможности"
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 4,
                type = BlockType.BULLETED_LIST,
                content = "Воркспейсы: переключение, создание, клонирование, переименование"
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 5,
                type = BlockType.BULLETED_LIST,
                content = "Древовидная файловая структура с вложенными папками"
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 6,
                type = BlockType.BULLETED_LIST,
                content = "Блочный редактор: заголовки H1-H3, списки, задачи, таблицы"
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 7,
                type = BlockType.BULLETED_LIST,
                content = "Метаданные в табличном виде и связь с календарем"
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 8,
                type = BlockType.DIVIDER,
                content = ""
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 9,
                type = BlockType.HEADING_2,
                content = "Контрольный список (Todo)"
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 10,
                type = BlockType.TODO,
                content = "Изучить возможности блочного редактора",
                isChecked = true
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 11,
                type = BlockType.TODO,
                content = "Переключить режим Read / Write в правом тулбаре",
                isChecked = false
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 12,
                type = BlockType.TODO,
                content = "Настроить цветовую метку документа (нажатие на цветной кружок)",
                isChecked = false
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 13,
                type = BlockType.DIVIDER,
                content = ""
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 14,
                type = BlockType.HEADING_2,
                content = "Таблица статуса модулей"
            ),
            BlockEntity(
                documentId = welcomeDocId,
                orderIndex = 15,
                type = BlockType.TABLE,
                tableDataJson = TableHelper.serializeTable(
                    listOf(
                        listOf("Компонент", "Статус", "Версия"),
                        listOf("Воркспейсы", "Активен", "1.0"),
                        listOf("Древо файлов", "Активен", "1.0"),
                        listOf("Блоки", "Активен", "1.0"),
                        listOf("Календарь", "Активен", "1.0")
                    )
                )
            )
        )
        blockDao.insertBlocks(welcomeBlocks)

        // Seed note in folder
        val planDocId = documentDao.insertDocument(
            DocumentEntity(
                workspaceId = personalWsId,
                folderId = ideasFolderId,
                title = "План развития проекта",
                icon = "🎯",
                colorHex = "#10B981",
                calendarDate = todayMillis,
                statusTag = "В работе",
                tagsCsv = "проект,планы",
                createdAt = now - 1800000,
                updatedAt = now
            )
        )
        val planBlocks = listOf(
            BlockEntity(
                documentId = planDocId,
                orderIndex = 0,
                type = BlockType.HEADING_1,
                content = "План развития и ключевые цели"
            ),
            BlockEntity(
                documentId = planDocId,
                orderIndex = 1,
                type = BlockType.NUMBERED_LIST,
                content = "Сформировать структуру папок и тегов"
            ),
            BlockEntity(
                documentId = planDocId,
                orderIndex = 2,
                type = BlockType.NUMBERED_LIST,
                content = "Разметить задачи по датам в календаре"
            ),
            BlockEntity(
                documentId = planDocId,
                orderIndex = 3,
                type = BlockType.NUMBERED_LIST,
                content = "Экспортировать документацию в Markdown"
            )
        )
        blockDao.insertBlocks(planBlocks)

        personalWsId
    }

    fun exportToMarkdown(
        document: DocumentEntity,
        blocks: List<BlockEntity>,
        formatDate: (Long) -> String
    ): String {
        val sb = StringBuilder()
        sb.append("# ${document.icon} ${document.title}\n\n")

        // Metadata Table
        sb.append("### Метаданные\n\n")
        sb.append("| Свойство | Значение |\n")
        sb.append("| --- | --- |\n")
        sb.append("| Создано | ${formatDate(document.createdAt)} |\n")
        sb.append("| Обновлено | ${formatDate(document.updatedAt)} |\n")
        if (document.calendarDate != null) {
            sb.append("| Дата в календаре | ${formatDate(document.calendarDate)} |\n")
        }
        sb.append("| Статус | ${document.statusTag} |\n")
        if (document.tagsCsv.isNotBlank()) {
            sb.append("| Теги | ${document.tagsCsv.split(",").joinToString(", ") { "#$it" }} |\n")
        }
        sb.append("\n---\n\n")

        // Blocks
        for (b in blocks.sortedBy { it.orderIndex }) {
            when (b.type) {
                BlockType.HEADING_1 -> sb.append("# ${b.content}\n\n")
                BlockType.HEADING_2 -> sb.append("## ${b.content}\n\n")
                BlockType.HEADING_3 -> sb.append("### ${b.content}\n\n")
                BlockType.PARAGRAPH -> sb.append("${b.content}\n\n")
                BlockType.DIVIDER -> sb.append("\n---\n\n")
                BlockType.BULLETED_LIST -> sb.append("- ${b.content}\n")
                BlockType.NUMBERED_LIST -> sb.append("1. ${b.content}\n")
                BlockType.TODO -> {
                    val mark = if (b.isChecked) "[x]" else "[ ]"
                    sb.append("- $mark ${b.content}\n")
                }
                BlockType.TABLE -> {
                    val table = TableHelper.parseTable(b.tableDataJson)
                    if (table.isNotEmpty()) {
                        val header = table.first()
                        sb.append("| " + header.joinToString(" | ") + " |\n")
                        sb.append("| " + header.map { "---" }.joinToString(" | ") + " |\n")
                        for (row in table.drop(1)) {
                            sb.append("| " + row.joinToString(" | ") + " |\n")
                        }
                        sb.append("\n")
                    }
                }
            }
        }
        return sb.toString()
    }

    fun exportToPlainText(
        document: DocumentEntity,
        blocks: List<BlockEntity>,
        formatDate: (Long) -> String
    ): String {
        val sb = StringBuilder()
        sb.append("${document.icon} ${document.title}\n")
        sb.append("=".repeat(document.title.length + 4)).append("\n\n")

        sb.append("Метаданные:\n")
        sb.append("• Создано: ${formatDate(document.createdAt)}\n")
        sb.append("• Обновлено: ${formatDate(document.updatedAt)}\n")
        if (document.calendarDate != null) {
            sb.append("• Дата в календаре: ${formatDate(document.calendarDate)}\n")
        }
        sb.append("• Статус: ${document.statusTag}\n")
        if (document.tagsCsv.isNotBlank()) {
            sb.append("• Теги: ${document.tagsCsv}\n")
        }
        sb.append("\n------------------------------------\n\n")

        for (b in blocks.sortedBy { it.orderIndex }) {
            when (b.type) {
                BlockType.HEADING_1, BlockType.HEADING_2, BlockType.HEADING_3 -> sb.append("[${b.content}]\n\n")
                BlockType.PARAGRAPH -> sb.append("${b.content}\n\n")
                BlockType.DIVIDER -> sb.append("------------------------------------\n\n")
                BlockType.BULLETED_LIST -> sb.append("• ${b.content}\n")
                BlockType.NUMBERED_LIST -> sb.append("  ${b.content}\n")
                BlockType.TODO -> {
                    val mark = if (b.isChecked) "[✓]" else "[ ]"
                    sb.append("$mark ${b.content}\n")
                }
                BlockType.TABLE -> {
                    val table = TableHelper.parseTable(b.tableDataJson)
                    for (row in table) {
                        sb.append(row.joinToString(" | ")).append("\n")
                    }
                    sb.append("\n")
                }
            }
        }
        return sb.toString()
    }
}
