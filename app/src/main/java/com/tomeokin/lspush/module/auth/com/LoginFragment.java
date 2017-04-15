/*
 * Copyright 2017 TomeOkin
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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CheckableImageButton;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.decay.callback.TextWatchAdapter;
import com.decay.glide.CircleTransform;
import com.decay.utillty.ViewHelper;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.data.model.AccessBundle;
import com.tomeokin.lspush.data.model.LoginData;
import com.tomeokin.lspush.data.remote.RxRequestAdapter;
import com.tomeokin.lspush.framework.NavFragment;
import com.tomeokin.lspush.module.auth.di.AuthComponent;
import com.tomeokin.lspush.widget.PasswordFilter;
import com.tomeokin.lspush.widget.PasswordUtils;

import javax.inject.Inject;

public class LoginFragment extends NavFragment {
    private ImageView avatarIv;
    private EditText phoneEditText;
    private EditText passwordEditText;
    private CheckableImageButton passwordToggle;
    private TextView submitLoginBtn;

    private final TextWatcher textChangeAdapter = new TextWatchAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
            checkTextField();
        }
    };

    @Inject AuthPresenter authPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        component(AuthComponent.class).inject(this);

        final View view = inflater.inflate(R.layout.fragment_login, container, false);
        avatarIv = (ImageView) view.findViewById(R.id.avatar_iv);
        phoneEditText = (EditText) view.findViewById(R.id.input_phone_editText);
        passwordEditText = (EditText) view.findViewById(R.id.input_password_editText);
        passwordToggle = (CheckableImageButton) view.findViewById(R.id.password_toggle);
        submitLoginBtn = (TextView) view.findViewById(R.id.submit_login_btn);
        TextView gotoRegisterBtn = (TextView) view.findViewById(R.id.goto_register);

        Glide.with(this)
            .load(R.drawable.avatar_big)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            // when using transform or bitmapTransform, don't use fitCenter() or centerCrop()
            .transform(new CircleTransform(context()))
            .into(avatarIv);

        phoneEditText.addTextChangedListener(textChangeAdapter);
        passwordEditText.addTextChangedListener(textChangeAdapter);
        final PasswordFilter filter = new PasswordFilter() {
            @Override
            public void onInvalidCharacter(char c) {
                showErrorMessage(getString(R.string.not_support_character, c));
            }
        };
        passwordEditText.setFilters(new InputFilter[] { filter });
        passwordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PasswordUtils.toggle(passwordToggle, passwordEditText);
            }
        });
        ViewHelper.setViewEnabled(submitLoginBtn, false);

        submitLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitLogin();
            }
        });
        gotoRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AuthActivity) getActivity()).gotoPhoneAuth();
            }
        });

        return view;
    }

    public void checkTextField() {
        ViewHelper.setViewEnabled(submitLoginBtn,
            isPhoneValid() && PasswordUtils.isPasswordValid(passwordEditText));
    }

    public boolean isPhoneValid() {
        final int length = passwordEditText.getText().toString().length();
        return length >= 6 && length <= 16;
    }

    public void submitLogin() {
        LoginData loginData = new LoginData(phoneEditText.getText().toString(), passwordEditText.getText().toString());
        authPresenter.login(loginData, new RxRequestAdapter<AccessBundle>(context()) {
            @Override
            public void onRequestSuccess(AccessBundle data) {
                ((AuthActivity) getActivity()).gotoMain();
            }
        });
    }

    public void showErrorMessage(@StringRes int message) {
        showErrorMessage(getString(message));
    }

    public void showErrorMessage(String message) {
        ((AuthActivity) getActivity()).showErrorMessage(message);
    }
}
