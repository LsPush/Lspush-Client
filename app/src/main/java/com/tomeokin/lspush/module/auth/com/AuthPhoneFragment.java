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
package com.tomeokin.lspush.module.auth.com;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.decay.callback.TextWatchAdapter;
import com.decay.country.Country;
import com.decay.country.CountryUtils;
import com.decay.logger.Log;
import com.decay.utillty.ViewHelper;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.app.logger.AppLogger;
import com.tomeokin.lspush.data.model.PhoneData;
import com.tomeokin.lspush.data.remote.RxRequestAdapter;
import com.tomeokin.lspush.framework.NavFragment;
import com.tomeokin.lspush.framework.nav.ActivityResultAdapter;
import com.tomeokin.lspush.module.auth.SMSCaptchaUtils;
import com.tomeokin.lspush.module.auth.SMSRequestCallback;
import com.tomeokin.lspush.module.auth.di.AuthComponent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import javax.inject.Inject;

public class AuthPhoneFragment extends NavFragment implements SMSRequestCallback {
    private Log log = AppLogger.of("AuthPhoneFragment");
    private static final int REQUEST_SELECT_COUNTRY = 100;
    private final TextWatcher textChangeAdapter = new TextWatchAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
            checkTextField();
        }
    };

    private Country country;
    private int[] phoneLengths;
    private CountDownTimer countDownTimer;

    private TextView selectCountryTv;
    private EditText phoneEditText;
    private EditText captchaEditText;
    private TextView obtainCaptchaBtn;
    private TextView submitBtn;

    @Inject AuthPresenter authPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {

        component(AuthComponent.class).inject(this);
        authPresenter.registerSMSEventListener(this);

        final View view = inflater.inflate(R.layout.fragment_auth_phone, container, false);
        selectCountryTv = (TextView) view.findViewById(R.id.select_country_tv);
        phoneEditText = (EditText) view.findViewById(R.id.input_phone_editText);
        captchaEditText = (EditText) view.findViewById(R.id.input_captcha_editText);
        obtainCaptchaBtn = (TextView) view.findViewById(R.id.obtain_captcha_btn);
        submitBtn = (TextView) view.findViewById(R.id.submit_next_btn);
        TextView gotoLoginBtn = (TextView) view.findViewById(R.id.goto_login);

        final CountryUtils countryUtils = CountryUtils.getInstance(context());
        final Country country = countryUtils.getCurrentCountry();
        setCountry(country);
        selectCountryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCountry();
            }
        });

        phoneEditText.addTextChangedListener(textChangeAdapter);
        captchaEditText.addTextChangedListener(textChangeAdapter);
        ViewHelper.setViewEnabled(submitBtn, false);
        ViewHelper.setViewEnabled(obtainCaptchaBtn, false);

        obtainCaptchaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtainCaptcha();
            }
        });
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitCaptcha();
            }
        });
        gotoLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AuthActivity) getActivity()).gotoLogin();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        phoneEditText.removeTextChangedListener(textChangeAdapter);
        captchaEditText.removeTextChangedListener(textChangeAdapter);
        authPresenter.removeSMSEventListener();
        super.onDestroyView();
    }

    public void selectCountry() {
        navUtils.startActivityForResult(CountryActivity.class, new ActivityResultAdapter(REQUEST_SELECT_COUNTRY) {
            @Override
            public void onRequestSuccess(Intent data) {
                Country country = CountryActivity.resolveData(context(), data);
                setCountry(country);
                checkTextField();
            }
        });
    }

    public void setCountry(Country country) {
        if (country == null) return;

        log.log("setCountry %s", country);
        this.country = country;
        String[] phoneLens = country.phoneLength.split("\\|");
        phoneLengths = new int[phoneLens.length];
        for (int i = 0; i < phoneLens.length; i++) {
            phoneLengths[i] = Integer.valueOf(phoneLens[i]);
        }
        Arrays.sort(phoneLengths);
        selectCountryTv.setText(String.format("%s +%s", country.name, country.countryCode));
    }

    public void checkTextField() {
        boolean phoneValid = isPhoneValid();
        ViewHelper.setViewEnabled(obtainCaptchaBtn, phoneValid);
        ViewHelper.setViewEnabled(submitBtn, phoneValid && isCaptchaValid());
    }

    public boolean isPhoneValid() {
        return Arrays.binarySearch(phoneLengths, phoneEditText.getText().length()) >= 0;
    }

    public boolean isCaptchaValid() {
        int length = captchaEditText.getText().length();
        return length >= 4 && length <= 6;
    }

    public void obtainCaptcha() {
        String[] countryCodes = country.countryCode.split("\\|");
        authPresenter.sendSMS(countryCodes[0], phoneEditText.getText().toString());
        obtainCaptchaBtn.setEnabled(false);
        countDownTimer = new CountDownTimer(60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                obtainCaptchaBtn.setText(String.format(Locale.ENGLISH, "%d s", millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                obtainCaptchaBtn.setText(R.string.obtain_captcha);
                obtainCaptchaBtn.setEnabled(true);
            }
        };
        countDownTimer.start();
    }

    public void submitCaptcha() {
        PhoneData phoneData = new PhoneData(phoneEditText.getText().toString(), country.country, country.countryCode,
            captchaEditText.getText().toString());
        authPresenter.checkSMS(phoneData, new RxRequestAdapter<String>(context()) {
            @Override
            public void onRequestSuccess(String data) {
                ((AuthActivity) getActivity()).gotoRegister(data);
            }
        });
    }

    @Override
    public void onGetCountryListResponse(HashMap<String, String> countryList) {}

    @Override
    public void onSendSMSResponse(boolean autoReadCaptcha) {}

    @Override
    public void onCheckCaptchaResponse(String phone, String captcha) {}

    @Override
    public void onSMSRequestFailed(Throwable t, int action) {
        if (action == SMSCaptchaUtils.SEND_CAPTCHA) {
            showErrorMessage("发送短信验证码失败");
        }
    }

    @Override
    public void onNetworkOffline() {
        showErrorMessage(R.string.network_offline);
    }

    public void showErrorMessage(@StringRes int message) {
        showErrorMessage(getString(message));
    }

    public void showErrorMessage(String message) {
        ((AuthActivity) getActivity()).showErrorMessage(message);
    }
}
