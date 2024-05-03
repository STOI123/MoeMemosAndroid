package me.mudkip.moememos.data.entity

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

// 1. obj
// 2. interface+implementation
// 3. 单例模式

@Dao
public interface UserDao {
    //@Query("SELECT * FROM users")
    //fun getAllUsers(): List<User>

    @Insert
    public fun insert(id:String,content: String,tag:String)

    @Insert
    public fun tryInsert(id: String, content: String, tag: String): Int // 返回影响的行数
    //@Delete
    //fun delete(id:String,content: String,tag:String)

}