package com.hqumath.demo.bean

/*
{
    "code": "200",
    "data": {
        "expires": "86400",
        "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3NTU1NjI0MDMsImlhdCI6MTc1NTQ3NjAwM30.lWAYZhD_ibihae5XP0oYmUKMWiUTQAsK3cb3yfDITzXI4R-pMWJYRW3qi9tDlTKVXYq4vL57vs-OaVRo8Bv-wE2m1-VsrqrmgJuUW9ecXXlIoUpVcUbl262Tcjh5v8ayCZKtFnqflRbzC2AI3GVyI5tGHXHHZbSPPOPdT6QSZJSOpkQlb3hqqHv4oWWWUfZ_1Q452HWgtWmMKzNTDHDQv36KMFIMk62NKlFV5hq9jrkCoiMrm79CFXMbTBO33At96Fh_7BvOxyFpfNdmTpT195wx6AQ0gGKzZpzoaVw-S-iP-kKWFcvezCLn_h1Vp5187kKmfcLfE0lgZUTWrGzk3g",
        "userInfo": {
            "avatar": null,
            "companyName": null,
            "deptName": null,
            "identity": "FLPTCaVUdJTFd7LRtt11/I0JWsM7gfZqALpMUFD/Yqk=",
            "nickname": "guoyadi",
            "phone": "JPZ31Z+P+gy5N8pyZ4hncw==",
            "sex": "0",
            "username": "guoyadi"
        }
    },
    "msg": "操作成功",
    "success": true,
    "timestamp": 1755476003952
}
 */

data class BaseResult<T>(
    val code: String, //错误号 200成功
    val `data`: T,
    val msg: String, //错误原因
    val success: Boolean, //是否成功
    val timestamp: Long //时间戳
)