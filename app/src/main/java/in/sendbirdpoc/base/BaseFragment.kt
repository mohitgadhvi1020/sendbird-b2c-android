package `in`.sendbirdpoc.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import `in`.sendbirdpoc.R
import `in`.sendbirdpoc.data.AppSharedPref
import javax.inject.Inject

open class BaseFragment<DB : ViewDataBinding>(private val layoutId: Int) : Fragment() {

    private lateinit var _binding: DB
    val binding get() = _binding

    @Inject
    lateinit var pref: AppSharedPref

    private var mProgressDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (layoutId != 0) {
            _binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
            binding.lifecycleOwner = viewLifecycleOwner
            return binding.root
        } else {
            throw IllegalArgumentException("Layout resource can't be null")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        setUpListeners()
        performApiCall()
        setUpObservers()
    }

    fun showErrorSnackBar(message: String) {
        val snackBar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        snackBar.setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
        snackBar.view.let {
            /*it.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    android.R.color.holo_red_light
                )
            )*/
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
        snackBar.setBackgroundTint(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
        snackBar.view.let {
            /*it.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    android.R.color.holo_red_light
                )
            )*/
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
            mProgressDialog = Dialog(requireContext()).apply {
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

    open fun setUpViews(){}

    open fun setUpListeners(){}

    open fun performApiCall() {}

    open fun setUpObservers() {}

    fun showToast(title: String) = Toast.makeText(requireContext(), title, Toast.LENGTH_LONG).show()

    fun showShortToast(title: String) =
        Toast.makeText(requireContext(), title, Toast.LENGTH_SHORT).show()
}