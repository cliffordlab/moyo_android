package com.cliffordlab.amoss.gui.environment

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.R


class EnvironmentActivity : AppCompatActivity() {
    companion object Title {
        const val name = "ENVIRONMENT"
        const val TAG = "EnvironmentActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.environment_activity)
        val frag = EnvironmentFragment()
        initFrag(frag)
    }

    private fun initFrag(frag: EnvironmentFragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.contentFrame, frag)
        transaction.commit()
    }
}