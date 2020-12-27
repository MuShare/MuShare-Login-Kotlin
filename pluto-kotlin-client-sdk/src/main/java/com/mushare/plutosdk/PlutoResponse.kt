package com.mushare.plutosdk

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody

class PlutoResponseWithBody<T>(
    status: String,
    error: PlutoResponseErrorData,
    @field:SerializedName("body") private var body: T
) : PlutoResponse(status, error) {
    fun getBody(): T = body
}

open class PlutoResponse(
    @field:SerializedName("status") private var status: String,
    @field:SerializedName("error") private var error: PlutoResponseErrorData
) {
    private val isStatusOK: Boolean
        get() = status == "ok"

    val errorCode: PlutoError
        get() = PlutoError.values().find { it.value == error.code } ?: PlutoError.unknown

    fun analysis(success: () -> Unit, error: (PlutoError) -> Unit) {
        if (isStatusOK) {
            success()
        } else {
            when(errorCode) {
                PlutoError.invalidRefreshToken -> {
                    Pluto.getInstance()?.let {
                        it.data.clear()
                        it.state.value = Pluto.State.invalidRefreshToken
                    }
                }
            }
            error(errorCode)
        }
    }
}

class PlutoResponseErrorData(
    @field:SerializedName("code") var code: Int = PlutoError.badRequest.value
)

class RefreshAuthResponse(
    @field:SerializedName("access_token") var accessToken: String,
    @field:SerializedName("refresh_token") var refreshToken: String
)

class LoginResponse(
    @field:SerializedName("refresh_token") var refreshToken: String,
    @field:SerializedName("access_token") var accessToken: String
)

internal fun parseErrorCodeFromErrorBody(errorBody: ResponseBody?, gson: Gson): PlutoError {
    if (errorBody == null) {
        return PlutoError.badRequest
    }
    val response = gson.fromJson(errorBody.string(), PlutoResponse::class.java)
    return response?.errorCode ?: PlutoError.badRequest
}

enum class PlutoError(val value: Int) {
    unknown(-99999),
    badRequest(-99998),
    parseError(-99997),
    notSignIn(1001),
    mailAlreadyRegister(2001),
    mailNotExist(2002),
    mailNotVerified(2003),
    mailAlreadyVerified(2004),
    userIdNotExist(2005),
    userIdExist(2006),
    bindAlreadyExist(2007),
    bindNotExist(2008),
    passwordNotSet(2009),
    unbindNotAllow(2010),
    sendMailFailure(2011),
    invalidPassword(3001),
    invalidRefreshToken(3002),
    invalidJWTToken(3003),
    invalidGoogleIDToken(3004),
    invalidWeChatCode(3005),
    invalidAvatarFormat(3006),
    jwtTokenExpired(3008),
    invalidAccessToken(3009),
    invalidApplication(3010),
    refreshTokenExpired(3011);
}