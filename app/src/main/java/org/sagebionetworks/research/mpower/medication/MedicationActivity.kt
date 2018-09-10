package org.sagebionetworks.research.mpower.medication


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.style.UnderlineSpan
import kotlinx.android.synthetic.main.activity_medication.*
import org.slf4j.LoggerFactory

import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.TaskLauncher
import javax.inject.Inject


class MedicationActivity : AppCompatActivity(),
        Listener,
        DaySelectedListener {

    private val LOGGER = LoggerFactory.getLogger(
            MedicationActivity::class.java)

    private val medicationViewModel: MedicationViewModel by lazy {
        ViewModelProviders.of(this).get(
                MedicationViewModel::class.java)
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
                var name: String = medicationViewModel.getTitle().value ?: ""
                medication_recycler.adapter =
                        MedicationAdapter(
                                name,
                                items
                                        ?: ArrayList<MedicationItem>(),
                                this, this)
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

    override fun showAddSchedule(show: Boolean) {
        LOGGER.debug("showNext(): $show")
        medicationViewModel.showAddSchedule(show)
    }

    override fun showDaySelection(name: String, schedule: Schedule) {
        LOGGER.debug("showDaySelection()")
        var days = schedule.days.joinToString(",")
        var dialog = MedicationDayFragment.newInstance(
                schedule.id, name, schedule.time, days)
        dialog.show(supportFragmentManager, "Day select")
    }

    override fun onDaySelected(id: String, days: String) {
        var list = days.split(",")
        var size = list.size
        LOGGER.debug("onDaySelected() $size - $days")
        //medicationViewModel.setSchedule()
        medicationViewModel.setScheduleDays(id, list)
        //medication_recycler.adapter.notifyDataSetChanged()
    }

    override fun setAnytime(schedule: Schedule, anytime: Boolean) {
        schedule.anytime = anytime;
        medicationViewModel.deleteOtherSchedules(schedule)
    }
}