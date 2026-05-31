package com.example.myimagelogapp.data.remote

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import retrofit2.HttpException
import java.io.IOException

object ApiErrorMapper {

    fun message(throwable: Throwable): String = when {
        isAuthError(throwable) -> "로그인이 필요합니다."
        throwable is HttpException && throwable.code() in 500..599 ->
            "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."
        throwable is HttpException ->
            "요청 처리에 실패했습니다. (${throwable.code()})"
        throwable is IOException ->
            "네트워크 연결을 확인해 주세요."
        else -> "알 수 없는 오류가 발생했습니다."
    }

    fun isAuthError(throwable: Throwable): Boolean {
        if (throwable is HttpException && throwable.code() in listOf(401, 403)) {
            return true
        }
        if (throwable is JsonEncodingException || throwable is JsonDataException) {
            return true
        }
        val msg = throwable.message.orEmpty()
        return msg.contains("JsonReader.setLenient", ignoreCase = true)
            || msg.contains("Expected BEGIN_OBJECT", ignoreCase = true)
            || msg.contains("Expected BEGIN_ARRAY", ignoreCase = true)
    }
}
