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

public class Response<T> extends BaseResponse {
    private T data;

    public Response(T data) {
        this.data = data;
    }

    public static <D> Response<D> create(D data) {
        return new Response<>(data);
    }

    public Response(int resultCode, String result) {
        super(resultCode, result);
        this.data = null;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        if (isSuccess()) {
            return "Response{" +
                "data=" + data +
                '}';
        } else {
            return super.toString();
        }
    }
}
