package com.cliffordlab.amoss.gui.mom

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.gui.surveys.graphs.ListFragment

class MoyoMomActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MoyoMomActivity"
        const val name = "MOYO MOM"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.listactivity)
        val frag = ListFragment()
        val args = Bundle()
        args.putString("activityName", name)
        frag.arguments = args
        initFrag(frag)
    }

    private fun initFrag(frag: ListFragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.add(R.id.contentFrame, frag)
        transaction.commit()
    }
}
