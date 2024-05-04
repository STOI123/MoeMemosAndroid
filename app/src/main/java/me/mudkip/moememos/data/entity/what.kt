package me.mudkip.moememos.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.mudkip.moememos.data.model.MemosRowStatus
import me.mudkip.moememos.data.model.MemosVisibility
import me.mudkip.moememos.data.model.Resource

@Entity(tableName = "content")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val createdTs: Long,
    val updatedTs: Long,
    val creatorId: Long,
    val creatorName: String? = null,
    val content: String,
    var tagList: List<String>?,
    //val visibility: MemosVisibility = MemosVisibility.PRIVATE,
    //val syncStatus: String,
    val resourceList: List<Long>? = null
)


