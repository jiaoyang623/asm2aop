package guru.ioio.asm2aop.demo

import android.app.Activity
import android.os.Bundle
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
        if (savedInstanceState == null) {
            return
        }
    }


}

open class BaseBean : Serializable {
    var id: String? = null
}

open class AlphaBean : BaseBean()

open class BetaBean : AlphaBean()