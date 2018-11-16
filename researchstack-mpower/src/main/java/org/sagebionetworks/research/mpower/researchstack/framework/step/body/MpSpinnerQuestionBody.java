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

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.researchstack.backbone.model.Choice;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.ui.step.body.SingleChoiceQuestionBody;
import org.sagebionetworks.research.mpower.researchstack.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rianhouston on 12/30/17.
 */

public class MpSpinnerQuestionBody<T> extends SingleChoiceQuestionBody {

    public MpSpinnerQuestionBody(Step step, StepResult result) {
        super(step, result);

        if(result != null) {
            currentSelected = result.getResult();
        }

    }

    @Override
    public View getBodyView(int viewType, LayoutInflater inflater, ViewGroup parent) {

        Resources res = parent.getResources();
        LinearLayout view = (LinearLayout)inflater.inflate(R.layout.mp_step_body_spinner, parent, false);

        Spinner spinner = view.findViewById(R.id.spinner);

        List<String> items = new ArrayList<>();
        for(Choice c: choices) {
            items.add(c.getText());
        }


        ArrayAdapter<String> adapter = new ArrayAdapter<>(parent.getContext(), R.layout.mp_step_body_item_spinner, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Choice<T> c = choices[i];
                currentSelected = c.getValue();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        if (currentSelected != null) {
            for (int i = 0; i < choices.length; i++) {
                if (currentSelected.equals(choices[i].getValue())) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }

        LinearLayout.MarginLayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setPadding(0, 0, 0, 0);

        view.setLayoutParams(layoutParams);

        return view;
    }

    protected boolean isValueSelected(Object value) {
        boolean isSelected = currentSelected != null && currentSelected.equals(value);
        return isSelected;
    }
}
