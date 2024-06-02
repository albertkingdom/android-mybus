package com.albertkingdom.mybusmap.util

import com.albertkingdom.mybusmap.repository.MyRepository
import io.reactivex.rxjava3.core.Single

class TokenManager(private val repository: MyRepository) {
    private var token: String? = null
    private var tokenExpiryTime: Int = 0

    fun getToken(): Single<String> {
        val currentTime = System.currentTimeMillis()
        return if (token != null && currentTime < tokenExpiryTime) {
            Single.just(token!!)
        } else {
            repository.getTokenRx()
                .doOnSuccess { newToken ->
                    token = newToken.accessToken
                    tokenExpiryTime = newToken.expireIn // 假设token有效期为1小时
                }.map { newToken ->
                    newToken.accessToken
                }
        }
    }
}