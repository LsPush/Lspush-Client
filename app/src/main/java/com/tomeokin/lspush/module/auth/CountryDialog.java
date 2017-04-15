/*
 * Copyright 2017 LsPush
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tomeokin.lspush.module.auth;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.decay.country.Country;
import com.decay.country.CountryUtils;
import com.decay.recyclerview.decoration.DividerDecoration;
import com.decay.utillty.PinyinUtils;
import com.tomeokin.lspush.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CountryDialog extends Dialog implements TextWatcher, CountryAdapter.OnItemClickListener {
    private EditText searchEditText;
    private CountryAdapter countryAdapter;
    private Collection<Country> countries;
    private PinyinUtils pinyinUtils;
    private String lastSequence;
    private OnCountrySelectedListener listener;

    public CountryDialog(@NonNull Context context, OnCountrySelectedListener listener) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final Window window = getWindow();
        if (window != null) {
            window.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            window.setBackgroundDrawable(new ColorDrawable());
        }

        setContentView(R.layout.dialog_country);

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.x = 0;
        attributes.y = 0;
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.windowAnimations = android.R.style.Animation;
        attributes.gravity = Gravity.TOP | Gravity.LEFT;

        this.listener = listener;
        searchEditText = (EditText) findViewById(R.id.search_editText);
        searchEditText.addTextChangedListener(this);
        TextView cancelBtn = (TextView) findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        RecyclerView countryRv = (RecyclerView) findViewById(R.id.country_rv);
        countryRv.setLayoutManager(new LinearLayoutManager(context));
        DividerDecoration decoration = new DividerDecoration(context, DividerDecoration.VERTICAL);
        decoration.setEndOffset(1);
        countryRv.addItemDecoration(decoration);
        countryAdapter = new CountryAdapter(null, this);
        countryRv.setAdapter(countryAdapter);

        final CountryUtils countryUtils = CountryUtils.getInstance(context);
        countries = countryUtils.getCountries();

        pinyinUtils = PinyinUtils.getInstance(context);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        String sequence = searchEditText.getText().toString().trim();
        if (sequence.equals(lastSequence)) return;

        if (sequence.length() == 0) {
            countryAdapter.setCountryList(new ArrayList<Country>(0));
            return;
        }

        lastSequence = sequence;
        List<Country> filters = new ArrayList<>(8);
        int i = 0, i2 = 0;
        for (Country country : countries) {
            int a = compareCountry(country);
            if (a == 3) {
                filters.add(i, country);
                i++;
            } else if (a == 2) {
                filters.add(i2 + i, country);
                i2++;
            } else if (a == 1) {
                filters.add(country);
            }
        }
        countryAdapter.setCountryList(filters);
    }

    private int compareCountry(Country country) {
        String jianPin = pinyinUtils.getJianpin(country.name);
        String fullPinyin = pinyinUtils.getFullPinyin(country.name);

        // @formatter:off
        if (country.countryCode.equals(lastSequence)
            || country.country.equals(lastSequence)
            || country.name.equals(lastSequence)
            || jianPin.equals(lastSequence)
            || fullPinyin.equals(lastSequence)) {
            return 3;
        }
        if (country.countryCode.startsWith(lastSequence)
            || country.country.startsWith(lastSequence)
            || country.name.startsWith(lastSequence)
            || jianPin.startsWith(lastSequence)
            || fullPinyin.startsWith(lastSequence)) {
            return 2;
        }
        if (country.countryCode.contains(lastSequence)
            || country.country.contains(lastSequence)
            || country.name.contains(lastSequence)
            || jianPin.contains(lastSequence)
            || fullPinyin.contains(lastSequence)) {
            return 1;
        }
        // @formatter:on

        return 0;
    }

    @Override
    public void onItemClick(View view, CountryAdapter adapter, Country country, int position) {
        listener.onCountrySelected(this, country);
    }
}
