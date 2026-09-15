package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {
    @Query("SELECT * FROM workspaces ORDER BY createdAt ASC")
    fun getAllWorkspaces(): Flow<List<WorkspaceEntity>>

    @Query("SELECT * FROM workspaces WHERE id = :id")
    suspend fun getWorkspaceById(id: Long): WorkspaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkspace(workspace: WorkspaceEntity): Long

    @Update
    suspend fun updateWorkspace(workspace: WorkspaceEntity)

    @Delete
    suspend fun deleteWorkspace(workspace: WorkspaceEntity)
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE workspaceId = :workspaceId ORDER BY name ASC")
    fun getFoldersForWorkspace(workspaceId: Long): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE workspaceId = :workspaceId ORDER BY name ASC")
    suspend fun getFoldersForWorkspaceSync(workspaceId: Long): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: Long): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE workspaceId = :workspaceId ORDER BY updatedAt DESC")
    fun getDocumentsForWorkspace(workspaceId: Long): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE workspaceId = :workspaceId ORDER BY updatedAt DESC")
    suspend fun getDocumentsForWorkspaceSync(workspaceId: Long): List<DocumentEntity>

    @Query("SELECT * FROM documents WHERE id = :id")
    fun getDocumentById(id: Long): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentByIdSync(id: Long): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)
}

@Dao
interface BlockDao {
    @Query("SELECT * FROM blocks WHERE documentId = :documentId ORDER BY orderIndex ASC")
    fun getBlocksForDocument(documentId: Long): Flow<List<BlockEntity>>

    @Query("SELECT * FROM blocks WHERE documentId = :documentId ORDER BY orderIndex ASC")
    suspend fun getBlocksForDocumentSync(documentId: Long): List<BlockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: BlockEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<BlockEntity>)

    @Update
    suspend fun updateBlock(block: BlockEntity)

    @Delete
    suspend fun deleteBlock(block: BlockEntity)

    @Query("DELETE FROM blocks WHERE documentId = :documentId")
    suspend fun deleteBlocksForDocument(documentId: Long)
}
