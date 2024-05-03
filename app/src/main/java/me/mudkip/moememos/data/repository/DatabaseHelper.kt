import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import me.mudkip.moememos.data.entity.MemoEntity
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "MyDatabase", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        // 创建内容信息表
        // DB字段兼容问题：字段名字/字段限制
        val createContentTable = """
            CREATE TABLE IF NOT EXISTS content (
                ID INTEGER  PRIMARY KEY,
                content TEXT,
                tagList TEXT,
                createdTs INTEGER ,
                updatedTs INTEGER ,
                syncStatus INTEGER
            );
        """.trimIndent()
        db.execSQL(createContentTable)

        val createResourceTable = """
            CREATE TABLE IF NOT EXISTS resource (
                resourceID INTEGER PRIMARY KEY,
                data BLOB NOT NULL
            );
        """.trimIndent()
        db.execSQL(createResourceTable)
        // 性能
        val createTagTable = """
            CREATE TABLE IF NOT EXISTS tags (
                tag TEXT NOT NULL,
                memoID INTEGER,  
                PRIMARY KEY (memoID, tag)
            );
        """.trimIndent()
        db.execSQL(createTagTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 当数据库版本更新时，可以在这里添加升级逻辑
        db.execSQL("DROP TABLE IF EXISTS content")
        db.execSQL("DROP TABLE IF EXISTS resource")
        onCreate(db)
    }

    fun insertContent(memo: MemoEntity, extractedTags: List<String>) {
        val id1 = memo.id
        val content = memo.content
        //val tag = memo.tag
        val createdTs = memo.createdTs
        val updatedTs = memo.updatedTs
        var insertStr = """
            INSERT INTO content (ID,content,tagList,createdTs,updatedTs) 
            VALUES($id1,"$content",null,"$createdTs","$updatedTs");
        """.trimIndent()
        var multiSQL = ""
        var startInsert = "INSERT INTO tags(tag,memoID) VALUES "
        extractedTags.map{
            if(multiSQL.isNotEmpty())multiSQL+=","
            multiSQL +=
            """
                ("$it",$id1)
            """.trimIndent()
        }
        Log.i("测试", "插入tags语句："+insertStr)
        if(multiSQL.isNotEmpty()) {
            startInsert += multiSQL+";"
        }
        var insertTagStr = """
            INSERT INTO tags (memoID,tag) VALUES
        """.trimIndent()
        var multiStr = ""
        extractedTags.map{
            if(multiStr.isNotEmpty())multiStr+=","
            multiStr +=
             "($id1,\"$it\")".trimMargin()
        }
        try {
            Log.d("测试","插入tag语句:"+insertTagStr+multiStr+";")
            if(multiStr.isNotEmpty())this.writableDatabase.execSQL(insertTagStr+multiStr+";")
            this.writableDatabase.execSQL(insertStr)
            Log.d("测试","插入成功")
        } catch (e:Exception) {
            Log.e("测试","插入错误"+e.toString())
        }
    }

    fun getAll(limit: Int, offset: Int,tags: List<String>?):List<MemoEntity>? {
        Log.d("测试","查询参数"+limit+"=|="+offset)
        var selectContentSQL = """
            SELECT content.ID, content.content, content.createdTs,content.updatedTs%s FROM content
        """.trimIndent()
        var joinStr = ""
        var i = 0
        tags?.map {
            i++
            joinStr += "\nJOIN tags$i ON content.memoID = tags$i.memoID"
        }
        selectContentSQL += " LIMIT $offset,$limit;"
        Log.d("测试","语句："+selectContentSQL)
        if(tags==null || tags.isEmpty())selectContentSQL = selectContentSQL.format("")
        else selectContentSQL = selectContentSQL.format(",")
        try {
            Log.d("测试","查询："+selectContentSQL)
            val cursor: Cursor = this.readableDatabase.rawQuery(selectContentSQL, null)
            Log.d("测试","查询中")
            val res = cursorToList(cursor) // TODO-再次查询，把tags变成列表
            Log.d("测试","查询成功")
            return res
        } catch (e:Exception) {
            Log.e("测试","查询错误"+e.toString())
        }
        Log.e("测试","错误查询测试")
        val res = mutableListOf<MemoEntity>()
        return res
    }

    fun getTags():List<String> {
        val selectContent = """
            SELECT DISTINCT(tag) FROM tags;
        """.trimIndent()
        return try {
            Log.d("测试","getTag前")
            val cursor: Cursor = this.readableDatabase.rawQuery(selectContent, null)
            Log.d("测试","getTag后")
            cursorToStrList(cursor)
        } catch (e :Exception) {
            Log.e("测试","失败tag:"+Log.getStackTraceString(e))
            mutableListOf<String>()
        }
    }
}

fun cursorToStrList(cursor: Cursor): List<String> {
    val resultList = mutableListOf<String>()
    if (cursor.moveToFirst()) {
        do {
            val tags = cursor.getString(cursor.getColumnIndexOrThrow("tag"))
            if(tags!=null) {
                resultList.add(tags)
            }
        } while (cursor.moveToNext())
    }
    Log.d("测试","cursorToStrList查询结果大小是"+resultList.size)
    cursor.close()
    return resultList
}
fun cursorToList(cursor: Cursor): List<MemoEntity> {
    val resultList = mutableListOf<MemoEntity>()
    var intStr = mutableMapOf<Long, MutableList<String>>().withDefault { key ->
        mutableListOf<String>()
    }
    if (cursor.moveToFirst()) {
        do {
            // 假设 "xx" 是你要检索的列名
            Log.d("测试", "没错")
            val id = cursor.getLong(cursor.getColumnIndexOrThrow("ID"))
            Log.d("测试", "updatedTs5没错"+id.toString())
            val content = cursor.getString(cursor.getColumnIndexOrThrow("content"))
            Log.d("测试", "updatedTs4没错"+content)
            val createTs = cursor.getLong(cursor.getColumnIndexOrThrow("createdTs"))
            Log.d("测试", "updatedTs3没错"+createTs.toString())

            val updatedTs = cursor.getLong(cursor.getColumnIndexOrThrow("updatedTs"))
            Log.d("测试", "updatedTs1没错"+updatedTs.toString())
            val tagIndex = cursor.getColumnIndex("tag")
            if(tagIndex>-1) {
                val tag = cursor.getString(tagIndex)
                Log.d("测试", "updatedTs2没错"+tag)
                intStr[id]?.add(tag)
            }
            val dataClass = MemoEntity(id =id, content = content, createdTs = createTs, creatorId = 222,
                tagList = null,resourceList = null, updatedTs = updatedTs, creatorName = null)
            resultList.add(dataClass)
            Log.d("测试", "updatedTs0没错")
        } while (cursor.moveToNext()) // 移动到下一行
    }
    resultList.map{
        it.tagList = intStr[it.id]
    }
    Log.d("测试","cursorToList查询结果大小是"+resultList.size)
    cursor.close()
    return resultList
}