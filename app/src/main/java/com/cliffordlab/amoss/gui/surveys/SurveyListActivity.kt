package com.cliffordlab.amoss.gui.surveys

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.gui.surveys.graphs.ListFragment

class SurveyListActivity : AppCompatActivity()  {
    companion object Title {
        val name = "SURVEYS"
    }
    // todo add surveys

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
