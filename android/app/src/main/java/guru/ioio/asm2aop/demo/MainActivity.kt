package guru.ioio.asm2aop.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import guru.ioio.asm2aop.demo.databinding.ActivityMainBinding
import java.io.Serializable

class MainActivity : AppCompatActivity() {
    private val mBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(LayoutInflater.from(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        mBinding.content.setOnClickListener {
            Toast.makeText(this, "iwantofindlambda", Toast.LENGTH_SHORT).show()
        }
//        AopTarget.f0()
    }
}

open class BaseBean : Serializable {
    var id: String? = null
}

open class AlphaBean : BaseBean()

open class BetaBean : AlphaBean()