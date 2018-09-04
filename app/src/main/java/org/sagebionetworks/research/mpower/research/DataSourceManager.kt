package org.sagebionetworks.research.mpower.research

class DataSourceManager {

}

// TODO: mdephillips 9/4/18 is this how we want to represent task ids?  why does iOS wrap them in an enum vs a string const?
enum class MpRsdIdentifier: RsdIdentifier {
    TRIGGERS {
        override val identifier = "Triggers"
    },
    SYMPTOMS {
        override val identifier = "Symptoms"
    },
    MEDICATION {
        override val identifier = "Medication"
    },
    STUDY_BURST_COMPLETED {
        override val identifier = "study-burst-task"
    }
}

interface RsdIdentifier {
    val identifier: String
}
