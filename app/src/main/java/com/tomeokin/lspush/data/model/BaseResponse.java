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
package com.tomeokin.lspush.data.model;

public class BaseResponse {
    public static final int SUCCESS = 0;
    public static final int TOKEN_TIMEOUT = 99;
    public static final String SUCCESS_DESCRIPTION = "success";

    protected int resultCode;
    protected String result;

    public BaseResponse() {
        resultCode = SUCCESS;
        result = SUCCESS_DESCRIPTION;
    }

    public BaseResponse(int resultCode, String result) {
        this.resultCode = resultCode;
        this.result = result;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public boolean isSuccess() {
        return resultCode == SUCCESS;
    }

    public boolean isTokenTimeout() {
        return resultCode == TOKEN_TIMEOUT;
    }

    @Override
    public String toString() {
        return "Response {" +
            "resultCode=" + resultCode +
            ", result='" + result + '\'' +
            '}';
    }
}
