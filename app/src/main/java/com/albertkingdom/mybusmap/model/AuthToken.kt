package com.albertkingdom.mybusmap.model

import com.google.gson.annotations.SerializedName

data class AuthToken(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("expires_in")
    val expireIn: Int
)
//{
//    "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJER2lKNFE5bFg4WldFajlNNEE2amFVNm9JOGJVQ3RYWGV6OFdZVzh3ZkhrIn0.eyJleHAiOjE2NTc3OTUyOTUsImlhdCI6MTY1NzcwODg5NSwianRpIjoiMTUyMmU5NGMtNTg3ZS00YjgxLWEyNTktZjIwMWJkY2M4MjlhIiwiaXNzIjoiaHR0cHM6Ly90ZHgudHJhbnNwb3J0ZGF0YS50dy9hdXRoL3JlYWxtcy9URFhDb25uZWN0Iiwic3ViIjoiOGQ0MWZjMjctZWZmYi00OTlmLThkODAtYzczM2Q3N2RmYzQyIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiYWxiZXJ0a2luZ2RvbS5kZXZlbG9wLWY0YWQ2MTA0LWQyN2QtNDZhNCIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsic3RhdGlzdGljIiwicHJlbWl1bSIsIm1hYXMiLCJhZHZhbmNlZCIsImhpc3RvcmljYWwiLCJiYXNpYyJdfSwic2NvcGUiOiJwcm9maWxlIGVtYWlsIiwidXNlciI6ImY0NjU2NDBlIn0.JH09UJvEZh3hTbUDttknn7rueLGM4xRGnq8PXrCjw32k1vMHZZbPOKR-odHPbKngC5pbMQ5hpdIRyrIr9H9pqbYkKbs_8qKObyYvxGTMhuUIxLIaa2hM0u73Qw-ByWaFoGoLpSD88jVaEpuOO9JDGntTfZE3i-JqX3hStjJfBc3zsPE-oV8lQebR74EiRkr4etRkQqa-pZkbmBC125QhpB_WrNHl8vCasE3PfHX7mBOivfdirdzT-jlsfWkpQgIJruvSY5tEB1JwRofyyq0Btw0SKvSJ6ylBq33NdlHmEKw7q80xgX6grIjqv_YAFSuHA0Ub-YywPHhHhDZtcXfPRQ",
//    "expires_in": 86400,
//    "refresh_expires_in": 0,
//    "token_type": "Bearer",
//    "not-before-policy": 0,
//    "scope": "profile email"
//}