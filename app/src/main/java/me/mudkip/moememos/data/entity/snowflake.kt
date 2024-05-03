import java.time.Instant

fun genID(): String {
    // 使用当前时间戳
    val timestamp = Instant.now().toEpochMilli()
    // 可以添加一个固定的字符串，以生成一个唯一的标识符
    val prefix = "ID_"
    // 将时间戳和前缀拼接起来，转换为字符串
    return "$prefix$timestamp"
}