package guru.ioio.asm2aop.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
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
    }
}

open class BaseBean : Serializable {
    var id: String? = null
}

open class AlphaBean : BaseBean()

open class BetaBean : AlphaBean()