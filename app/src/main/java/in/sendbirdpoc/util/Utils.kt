package `in`.sendbirdpoc.util

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

object Utils {

    fun  Activity?.addFragment(fragment: Fragment?, frameId: Int?, bundle: Bundle?) {
        (this as AppCompatActivity).supportFragmentManager.inTransaction {
            frameId?.let { frameId ->
                fragment?.let { fragment ->
                    add(frameId, fragment::class.java, bundle, fragment::class.java.simpleName)
                }
            }
            addToBackStack(fragment?.javaClass?.name)
        }
    }



    fun Activity?.replaceFragments(fragment: Fragment?, frameId: Int?, bundle: Bundle?) {
        (this as AppCompatActivity)?.supportFragmentManager?.inTransaction {
            frameId?.let { frameId ->
                fragment?.let { fragment ->
                    replace(frameId, fragment::class.java, bundle, fragment::class.java.simpleName)
                }
            }
            addToBackStack(fragment?.javaClass?.name)
        }
    }


    @SuppressLint("CommitTransaction")
    fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
        beginTransaction()
            .func()
            .commit()
    }


    fun Activity?.replaceFragment(fragment: Fragment?, frameId: Int?, bundle: Bundle?, isAddToBackStack: Boolean = true) {
        val backStateName = fragment?.let { fragment1 -> fragment1::class.java.name }
        val manager: FragmentManager? = (this as AppCompatActivity?)?.supportFragmentManager
        val ft: FragmentTransaction? = manager?.beginTransaction()

        frameId?.let { frame ->
            fragment
                ?.let { fragment1 ->
                    fragment1::class.java
                }
                ?.let { fragmentClass ->
                    ft?.replace(frame, fragmentClass, bundle, backStateName)
                }
        }

        ft?.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        if (isAddToBackStack) {
            ft?.addToBackStack(backStateName)
        }

        ft?.commit()
    }


    fun AppCompatActivity?.removeFragment(fragment: Fragment?) {
        fragment?.let { fragment1 -> this?.supportFragmentManager?.beginTransaction()?.remove(fragment1)?.commit() }
    }

    fun Activity?.removeFragments() {
        (this as AppCompatActivity).supportFragmentManager.fragments.forEachIndexed { index, fragment ->

        }
    }

    fun AppCompatActivity?.removeLastFragment(isCheckBackStackEntryCount: Boolean = true) {
        if (this?.supportFragmentManager?.fragments?.isNotEmpty() == true) {
            val lastFragment = supportFragmentManager.fragments.last()

            if (isCheckBackStackEntryCount) {
                if (supportFragmentManager.backStackEntryCount > 1) {
                    removeFragment(lastFragment)
                }

            } else {
                removeFragment(lastFragment)
            }
        }
    }
}

