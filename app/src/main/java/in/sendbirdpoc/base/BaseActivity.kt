package `in`.sendbirdpoc.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.snackbar.Snackbar
import `in`.sendbirdpoc.R
import `in`.sendbirdpoc.data.AppSharedPref
import javax.inject.Inject

open class BaseActivity<DB : ViewDataBinding>(private val layoutId: Int) : AppCompatActivity() {

    private lateinit var _binding: DB
    val binding get() = _binding

    @Inject
    lateinit var pref: AppSharedPref

    private var mProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        /*enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
        if (layoutId != 0) {
            _binding = DataBindingUtil.setContentView(this, layoutId)
            binding.lifecycleOwner = this
        } else {
            throw IllegalArgumentException("Layout resource can't be null")
        }

        setUpViews()
        setUpListeners()
        performApiCall()
        setUpObservers()
    }

    fun showToastLong(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    fun showToastShort(message: String) {
        showErrorSnackBar(message)
    }

    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        snackBar.setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        snackBar.view.let {
            it.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                setTextColor(Color.WHITE)
                maxLines = 5
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
        }
        snackBar.show()
    }

    fun showSuccessSnackBar(message: String) {
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        snackBar.setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_green_dark))

        snackBar.view.let {
            it.layoutParams = (snackBar.view.layoutParams as FrameLayout.LayoutParams).apply {
                gravity = Gravity.TOP
            }
            it.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                setTextColor(Color.WHITE)
                maxLines = 5
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }
        }
        snackBar.show()
    }

    open fun showProgressDialog() {
        mProgressDialog?.let {
            if (!it.isShowing) it.show()
        } ?: run {
            mProgressDialog = Dialog(this@BaseActivity).apply {
                window?.let {
                    it.requestFeature(Window.FEATURE_NO_TITLE)
                    it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    it.setDimAmount(0f)
                }
                setContentView(R.layout.progress_dialog)
                setCancelable(false)
                setCanceledOnTouchOutside(false)
            }
            if (mProgressDialog?.isShowing == false) mProgressDialog?.show()
        }
    }

    open fun hideProgressDialog() {
        mProgressDialog?.let {
            if (it.isShowing) it.dismiss()
        }
    }

    open fun setUpViews() {}

    open fun setUpListeners() {}

    open fun performApiCall() {}

    open fun setUpObservers() {}

}