package guru.ioio.asm2aop.demo

import android.app.Activity
import android.os.Bundle
import android.util.Log

class MainActivity : Activity() {

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
