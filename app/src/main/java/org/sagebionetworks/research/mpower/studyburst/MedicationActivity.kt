package org.sagebionetworks.research.mpower.studyburst


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.style.UnderlineSpan
import kotlinx.android.synthetic.main.activity_medication.*
import org.slf4j.LoggerFactory

import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.TaskLauncher
import javax.inject.Inject


class MedicationActivity : AppCompatActivity(), Listener {

    private val LOGGER = LoggerFactory.getLogger(MedicationActivity::class.java)

    private val medicationViewModel: MedicationViewModel by lazy {
        ViewModelProviders.of(this).get(MedicationViewModel::class.java)
    }

    @Inject
    lateinit var taskLauncher: TaskLauncher

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        LOGGER.debug("onCreate()")
        setContentView(R.layout.activity_medication)

        medication_recycler.layoutManager = LinearLayoutManager(this)


        val str = getString(R.string.remove_medication)
        val content = SpannableString(str)
        content.setSpan(UnderlineSpan(), 0, str.length, 0)
        medication_message.text = content

        medicationViewModel.getTitle().observe(this, Observer {
            text -> medication_title.setText(text)
        })


        medicationViewModel.getItems().observe(this, Observer {
            items ->
                LOGGER.debug("How many items: " + items?.size)
                medication_recycler.adapter =
                        MedicationAdapter(items ?: ArrayList<MedicationItem>(), this, this)
        })
        medicationViewModel.init()

        medication_back.setOnClickListener { _ -> finish() }

        medication_next.setEnabled(false)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun addSchedule() {
        LOGGER.debug("addSchedule()")
        medicationViewModel.addSchedule()
    }

    override fun enableNext(enable: Boolean) {
        LOGGER.debug("enabledNext(): $enable")
        medication_next.setEnabled(enable)
    }
}