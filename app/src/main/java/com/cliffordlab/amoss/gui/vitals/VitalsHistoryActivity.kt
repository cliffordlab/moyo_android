package com.cliffordlab.amoss.gui.vitals

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.adapters.VitalsAdapter
import com.cliffordlab.amoss.gui.mom.MomSymptomsActivity
import com.cliffordlab.amoss.gui.mom.MomVitalsActivity
import com.cliffordlab.amoss.models.BPModel
import com.cliffordlab.amoss.models.SymptomsModel
import com.cliffordlab.amoss.models.VitalItems
import io.realm.Realm

class VitalsHistoryActivity: AppCompatActivity() {
    companion object {
        private const val TAG = "VitalsHistory"
        const val name = "Vitals History"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_vitals_history_list)

        val activityType = intent.getStringExtra("activityType")

        val realm = Realm.getDefaultInstance()

        when (activityType) {
            MomVitalsActivity.name -> {
                val items = mutableListOf<VitalItems>()
                val results = realm.where(BPModel::class.java).findAll()
                if (results.size != 0) {
                    for (result in results) {
                        items.add(VitalItems(result.arm, result.vitals, result.createdAt))
                    }
                    val recyclerView = findViewById<RecyclerView>(R.id.vitalsHistoryRecyclerList)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    val adapter = VitalsAdapter(items)
                    recyclerView.adapter = adapter
                }
            }
            MomSymptomsActivity.name -> {
                val items = mutableListOf<VitalItems>()
                val results = realm.where(SymptomsModel::class.java).findAll()
                if (results.size != 0) {
                    for (result in results) {
                        items.add(VitalItems("Symptoms reported", result.symptoms, result.createdAt))
                    }
                    val recyclerView = findViewById<RecyclerView>(R.id.vitalsHistoryRecyclerList)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    val adapter = VitalsAdapter(items)
                    recyclerView.adapter = adapter
                }
            }
        }

        realm.close()
    }
}
