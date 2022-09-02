package guru.ioio.asm2aop

import java.math.BigInteger
import java.security.MessageDigest

class MD5Utils {
    companion object {
        fun md5(str: String) = BigInteger(1, MessageDigest.getInstance("MD5").digest(str.toByteArray()))
            .toString(16).padStart(32, '0')
    }
}
