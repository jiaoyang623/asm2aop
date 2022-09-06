package guru.ioio.asm2aop.demo

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println(load(0, '0', intArrayOf(0)))
    }

    private fun load(a: Int, b: Char, c: IntArray): Int {
        Log.i("MA", "load $a, $b, $c")
        return 1
    }

}
