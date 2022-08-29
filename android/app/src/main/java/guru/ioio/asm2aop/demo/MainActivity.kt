package guru.ioio.asm2aop.demo

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import guru.ioio.asm2aop.demo.databinding.ActivityMainBinding
import java.io.Serializable

class MainActivity : Activity() {
    private val mBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(LayoutInflater.from(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        load(0, '0', 0)
    }

    override fun onResume() {
        super.onResume()
        Log.i("MA", "onResume")
    }


    private fun load(a: Byte, b: Char, c: Short): IntArray {
        Log.i("MA", "load $a, $b, $c")
        return intArrayOf(9)
    }
}

open class BaseBean : Serializable {
    var id: String? = null
}

open class AlphaBean : BaseBean()

open class BetaBean : AlphaBean()