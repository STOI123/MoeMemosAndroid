package me.mudkip.moememos.data.repository

import DatabaseHelper
import com.skydoves.sandwich.ApiResponse
import genID
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import me.mudkip.moememos.MoeMemosApp.Companion.CONTEXT
import kotlin.coroutines.CoroutineContext
import me.mudkip.moememos.data.api.CreateMemoInput
import me.mudkip.moememos.data.api.DeleteTagInput
import me.mudkip.moememos.data.api.MemosApiService
import me.mudkip.moememos.data.api.PatchMemoInput
import me.mudkip.moememos.data.api.UpdateMemoOrganizerInput
import me.mudkip.moememos.data.api.UpdateTagInput
import me.mudkip.moememos.data.entity.MemoEntity
import me.mudkip.moememos.data.entity.extractTags
import me.mudkip.moememos.data.model.Memo
import me.mudkip.moememos.data.model.MemosRowStatus
import me.mudkip.moememos.data.model.MemosVisibility
import javax.inject.Inject

class MemoRepository @Inject constructor(private val memosApiService: MemosApiService) {
    suspend fun oldLoadMemos(rowStatus: MemosRowStatus? = null): ApiResponse<List<Memo>> = memosApiService.call { api ->
        api.listMemo(rowStatus = rowStatus)
    }
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun loadMemos(limit: Int?): Deferred<Result<List<MemoEntity>?>> = GlobalScope.async(Dispatchers.IO) {
        val realLimit = limit ?: 1
        val res = dbHelper.getAll(realLimit,1, null)
        Result.success(res)
    }

    private val dbHelper = DatabaseHelper(CONTEXT)

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun createMemo(content: String,
                           resourceIdList: List<Long>? = null,
                           visibility: MemosVisibility = MemosVisibility.PRIVATE):
            Deferred<Result<Unit>> = GlobalScope.async(Dispatchers.IO) {
        val resourceID = genID().toString()
        val (extractedTags, updatedContent) = extractTags(content)
        val memo = MemoEntity(
            id = System.currentTimeMillis(),
            content = content,
            resourceList = null,
            createdTs = System.currentTimeMillis(),
            creatorId = 222,
            updatedTs = System.currentTimeMillis(),
            tagList = null
        )
        dbHelper.insertContent(memo, extractedTags)
        Result.success(Unit)
        //ApiResponse<Memo> =
        //memosApiService.call { api ->
        //api.createMemo(CreateMemoInput(content, resourceIdList = resourceIdList, visibility = visibility))
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun getAllLocalMemo(limit: Int? = null, offset: Int? = null, pinned: Boolean? = null, tags: List<String>? = null, visibility: MemosVisibility? = null):
            Deferred<List<MemoEntity>?> = GlobalScope.async(Dispatchers.IO) {
        dbHelper.getAll(limit?:3, offset?:1, tags)
    }

    @OptIn(DelicateCoroutinesApi::class)
    suspend fun getTags(): Deferred<List<String>> = GlobalScope.async(Dispatchers.IO) {
        dbHelper.getTags()
    }
    // ============
    suspend fun oldgetTags(): ApiResponse<List<String>> = memosApiService.call { api ->
        api.getTags()
    }

    suspend fun updateTag(name: String): ApiResponse<String> = memosApiService.call { api ->
        api.updateTag(UpdateTagInput(name))
    }

    suspend fun deleteTag(name: String): ApiResponse<Unit> = memosApiService.call { api ->
        api.deleteTag(DeleteTagInput(name))
    }

    suspend fun updatePinned(memoId: Long, pinned: Boolean): ApiResponse<Memo> = memosApiService.call { api ->
        api.updateMemoOrganizer(memoId, UpdateMemoOrganizerInput(pinned = pinned))
    }

    suspend fun archiveMemo(memoId: Long): ApiResponse<Memo> = memosApiService.call { api ->
        api.patchMemo(memoId, PatchMemoInput(id = memoId, rowStatus = MemosRowStatus.ARCHIVED))
    }

    suspend fun restoreMemo(memoId: Long): ApiResponse<Memo> = memosApiService.call { api ->
        api.patchMemo(memoId, PatchMemoInput(id = memoId, rowStatus = MemosRowStatus.NORMAL))
    }

    suspend fun deleteMemo(memoId: Long): ApiResponse<Unit> = memosApiService.call { api ->
        api.deleteMemo(memoId)
    }

    suspend fun editMemo(memoId: Long, content: String, resourceIdList: List<Long>? = null, visibility: MemosVisibility = MemosVisibility.PRIVATE): ApiResponse<Memo> = memosApiService.call { api ->
        api.patchMemo(memoId, PatchMemoInput(id = memoId, content = content, resourceIdList = resourceIdList, visibility = visibility))
    }

    suspend fun listAllMemo(limit: Int? = null, offset: Int? = null, pinned: Boolean? = null, tag: String? = null, visibility: MemosVisibility? = null): ApiResponse<List<Memo>> = memosApiService.call { api ->
        api.listAllMemo(
            limit = limit,
            offset = offset,
            pinned = pinned,
            tag = tag,
            visibility = visibility
        )
    }
}