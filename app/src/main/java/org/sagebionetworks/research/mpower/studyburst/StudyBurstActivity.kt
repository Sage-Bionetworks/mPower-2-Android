package org.sagebionetworks.research.mpower.studyburst

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import org.slf4j.LoggerFactory

import org.sagebionetworks.research.mpower.R
import kotlinx.android.synthetic.main.activity_study_burst.*
import org.researchstack.backbone.utils.ResUtils
import org.sagebionetworks.research.mpower.R.id.*
import org.sagebionetworks.research.mpower.TaskLauncher
import org.sagebionetworks.research.mpower.viewmodel.StudyBurstViewModel
import org.threeten.bp.Instant
import javax.inject.Inject


class StudyBurstActivity : AppCompatActivity() {
    private val LOGGER = LoggerFactory.getLogger(StudyBurstActivity::class.java)

    private val studyBurstViewModel: StudyBurstViewModel by lazy {
        StudyBurstViewModel.create(this)
    }

    @Inject
    lateinit var taskLauncher: TaskLauncher

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        LOGGER.debug("StudyBurstActivity.onCreate()")
        setContentView(R.layout.activity_study_burst)

        studyBurstRecycler.layoutManager = GridLayoutManager(this, 2)

        studyBurstViewModel.liveData().observe(this, Observer { it?.let {
            // StudyBurstItem actually can't be null but appears Nullable because of the Observer @Nullable annotation
            if (!it.hasStudyBurst) {
                // If we don't have a study burst, this means that we should send the user to a completion task
                // Or, if we don't have any completion tasks, we should probably leave this Activity
                return@Observer
            }

            studyBurstTitle.text = studyBurstTitle(it.dayCount)
            studyBurstMessage.text = studyBurstMessage(it.dayCount, it.missedDayCount)
            expiresText.text = studyExpirationMessage(it.expiresOn)
      }})
//        studyBurstViewModel.getDaysMissed().observe(this, Observer {
//            missed ->
//                var message: String
//                var total: Int? = studyBurstViewModel.getDayCount().value
//                when(missed) {
//                    0 -> message = getString(R.string.study_burst_message_day)
//                    1 -> message = getString(R.string.study_burst_message_days_missed_one, total)
//                    else -> {
//                        message = getString(R.string.study_burst_message_days_missed, total, missed)
//                    }
//
//                }
//                studyBurstMessage.text = message
//        })
//        studyBurstViewModel.getTitle().observe(this, Observer {
//            text -> studyBurstTitle.setText(text)
//        })
//        studyBurstViewModel.getExpires().observe(this, Observer {
//            text ->
//                expiresText.text = getResources().getString(R.string.study_burst_progress_message, text)
//        })
//        studyBurstViewModel.getDayNumber().observe(this, Observer {
//            count ->
//                studyBurstStatusWheel.setDayCount(count ?: 0)
//                studyBurstStatusWheel.setProgress(count ?: 0)
//        })
//        studyBurstViewModel.getDayCount().observe(this, Observer {
//            count -> studyBurstStatusWheel.setMaxProgress(count ?: 0)
//
//        })
//
//        studyBurstViewModel.getItems().observe(this, Observer {
//            items ->
//                LOGGER.debug("How many items: " + items?.size)
//                var completed = items?.count { it.completed } ?: 0
//                LOGGER.debug("Number completed: $completed")
//                studyBurstTopProgressBar.progress = completed
//
//                studyBurstRecycler.adapter = StudyBurstAdapter(items, this)
//
//        })
//        studyBurstViewModel.init()

        studyBurstBack.setOnClickListener { _ -> finish() }
    }

    /**
     * @return The title string is the same regardless of how many days they've missed, if any.
     *         It will vary only by the current day of the study burst
     */
    fun studyBurstTitle(dayCount: Int?): String? {
        val dayCountChecked = dayCount ?: return null
        val stringRes = ResUtils.getStringResourceId(this,
                String.format("study_burst_title_day_%s", dayCountChecked))
        if (stringRes > 0) return getString(stringRes) else return ""
    }

    /**
     * @return The message will vary by the current day of the study burst for no missed days
     *         For all else, the message will be the same for each day of the study burst and will simply
     *         indicate the current day and the number of missed days
     */
    fun studyBurstMessage(dayCount: Int?, missedDayCount: Int): String? {
        val dayCountChecked = dayCount ?: return null
        return when (missedDayCount) {
            // If the user hasn't missed any days, the message will vary by the current day of the study burst
            0 -> {
                val stringRes = ResUtils.getStringResourceId(this,
                        String.format("study_burst_message_day_%s", dayCountChecked))
                if (stringRes > 0) getString(stringRes) else ""
            }
            // The message will be the same for each day of the study burst and will simply
            // indicate the current day and the number of missed days
            1 -> getString(R.string.study_burst_message_days_missed_one, dayCountChecked)
            else -> getString(R.string.study_burst_message_days_missed, dayCountChecked, missedDayCount)
        }
    }

    fun studyExpirationMessage(expiresOn: Instant?): String? {
        val expiresOnChecked = expiresOn ?: return null
        //val formatter =
        return ""
//        let formatter = DateComponentsFormatter()
//        formatter.allowedUnits = [.hour, .minute, .second]
//        formatter.collapsesLargestUnit = false
//        formatter.zeroFormattingBehavior = .pad
//                formatter.allowsFractionalUnits = false
//        formatter.unitsStyle = .positional
//                let timeString = formatter.string(from: Date(), to: expiresOn!)!
//
//        let marker = "%@"
//        let format = Localization.localizedString("PROGRESS_EXPIRES_%@")
//
//        let mutableString = NSMutableString(string: format)
//        let markerRange = mutableString.range(of: marker)
//        mutableString.replaceCharacters(in: markerRange, with: timeString)
//        let boldRange = NSRange(location: markerRange.location, length: (timeString as NSString).length)
//        let attributedString = NSMutableAttributedString(string: mutableString as String)
//        let fullRange = NSRange(location: 0, length: attributedString.length)
//
//        let fontSize: CGFloat = 14
//        let font = self.font ?? UIFont.italicSystemFont(ofSize: fontSize)
//        attributedString.addAttribute(.font, value: font, range: fullRange)
//        if let fontDescriptor = font.fontDescriptor.withSymbolicTraits([.traitItalic, .traitBold]) {
//            let boldFont = UIFont(descriptor: fontDescriptor, size: fontSize)
//            attributedString.addAttribute(.font, value: boldFont, range: boldRange)
//        }
//        self.attributedText = attributedString
//
//        // Fire update in 1 second.
//        let delay = DispatchTime.now() + .seconds(1)
//        DispatchQueue.main.asyncAfter(deadline: delay) {
//            if let delegate = self.delegate, let date = delegate.studyBurstExpiresOn() {
//            self.updateStudyBurstExpirationTime(date)
//        }
//        }
    }
}