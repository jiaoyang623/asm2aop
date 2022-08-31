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


    private fun load(vararg args:Any): IntArray {
        Log.i("MA", "load $args")
        return intArrayOf(9)
    }
}
