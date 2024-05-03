package me.mudkip.moememos.data.entity

import java.util.regex.Pattern
import java.util.regex.Matcher

fun extractTags(input: String): Pair<List<String>, String> {
    // 正则表达式匹配以 # 开头，后跟一个或多个非空白字符，直到遇到空格
    // 通过使用捕获组 (\\S+) 来确保 # 不被包含在内
    val tagPattern = Pattern.compile("(#)(\\S+)\\b")
    val matcher = tagPattern.matcher(input)

    // 将所有匹配的标签（不包括 #）添加到列表中
    val tags = mutableListOf<String>()
    while (matcher.find()) {
        // 只添加捕获组2，即 # 后面的部分
        tags.add(matcher.group(2))
    }

    // 替换所有匹配的标签（包括 #）为一个空字符串，从而从原始字符串中删除它们
    val updatedString = matcher.replaceAll("")

    return tags to updatedString
}