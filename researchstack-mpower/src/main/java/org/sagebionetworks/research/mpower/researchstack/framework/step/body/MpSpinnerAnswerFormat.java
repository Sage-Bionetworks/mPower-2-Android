/*
 *    Copyright 2017 Sage Bionetworks
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package org.sagebionetworks.research.mpower.researchstack.framework.step.body;

import org.researchstack.backbone.answerformat.ChoiceAnswerFormat;
import org.researchstack.backbone.model.Choice;


/**
 * Created by rianhouston on 11/27/17.
 */

public class MpSpinnerAnswerFormat extends MpChoiceAnswerFormat {

    public MpSpinnerAnswerFormat() {
        super();
    }

    public MpSpinnerAnswerFormat(ChoiceAnswerStyle answerStyle, Choice... choices) {
        super(answerStyle, choices);
    }

    public QuestionType getQuestionType() {
        // For this to work, we must also provide custom code in CrfTaskHelper for this format
        return () -> MpSpinnerQuestionBody.class;
    }
}