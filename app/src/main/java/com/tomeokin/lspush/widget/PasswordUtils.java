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
package com.tomeokin.lspush.widget;

import android.support.design.widget.CheckableImageButton;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;

import java.util.Arrays;

public class PasswordUtils {
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 16;
    public static final String PASSWORD_SPECIAL = ".,;";
    public static final char[] PASSWORD_SPECIAL_SORT = PASSWORD_SPECIAL.toCharArray();

    static {
        Arrays.sort(PASSWORD_SPECIAL_SORT);
    }

    private PasswordUtils() {}

    public static void toggle(CheckableImageButton passwordToggle, EditText passwordEditText) {
        // 保存 EditText 焦点
        final int selection = passwordEditText.getSelectionEnd();
        boolean passwordToggledVisible;
        if (passwordEditText.getTransformationMethod() instanceof PasswordTransformationMethod) {
            passwordEditText.setTransformationMethod(null);
            passwordToggledVisible = true;
        } else {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordToggledVisible = false;
        }
        passwordToggle.setChecked(passwordToggledVisible);
        passwordEditText.setSelection(selection);
    }

    public static boolean isPasswordValid(EditText passwordEditText) {
        final String password = passwordEditText.getText().toString().trim();
        final int length = password.length();
        return length >= PASSWORD_MIN_LENGTH && length <= PASSWORD_MAX_LENGTH && quickFallPasswordStrength(password);
    }

    private static boolean quickFallPasswordStrength(CharSequence password) {
        int result = indexDigest(password) >= 0 ? 1 : 0;
        result += indexLowerLetter(password) >= 0 ? 1 : 0;
        if (result == 2) {
            return true;
        }
        result += indexUpperLetter(password) >= 0 ? 1 : 0;
        if (result >= 2) {
            return true;
        }
        result += indexSpecial(PASSWORD_SPECIAL_SORT, password);
        return result >= 2;
    }

    private static int indexDigest(CharSequence sequence) {
        int length = sequence.length();
        for (int i = 0; i < length; i++) {
            if (Character.isDigit(sequence.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static int indexLowerLetter(CharSequence sequence) {
        int length = sequence.length();
        for (int i = 0; i < length; i++) {
            if (Character.isLowerCase(sequence.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static int indexUpperLetter(CharSequence sequence) {
        int length = sequence.length();
        for (int i = 0; i < length; i++) {
            if (Character.isUpperCase(sequence.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param chars sort characters
     */
    private static int indexSpecial(char[] chars, CharSequence sequence) {
        final int length = sequence.length();
        for (int i = 0; i < length; i++) {
            if (Arrays.binarySearch(chars, sequence.charAt(i)) >= 0) {
                return i;
            }
        }
        return -1;
    }
}
