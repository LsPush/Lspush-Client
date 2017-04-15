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
package com.tomeokin.lspush.module.auth;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.tomeokin.lspush.R;

public class SelectImageDialog implements OnClickListener {
    private DialogPlus dialog;
    private OnSelectImageRequest callback;

    public SelectImageDialog(Context context, @NonNull OnSelectImageRequest callback) {
        this.callback = callback;

        TextView header = (TextView) View.inflate(context, R.layout.dialog_header, null);
        header.setText(R.string.select_image_from);
        View view = View.inflate(context, R.layout.dialog_select_image, null);

        dialog = DialogPlus.newDialog(context)
            .setCancelable(true)
            .setHeader(header)
            .setContentHolder(new ViewHolder(view))
            .setOnClickListener(this)
            .setGravity(Gravity.CENTER)
            .setMargin(100, 0, 100, 0)
            .create();
    }

    public void show() {
        dialog.show();
    }

    @Override
    public void onClick(DialogPlus dialog, View view) {
        final int id = view.getId();
        if (id == R.id.take_photo) {
            callback.takePhoto();
        } else if (id == R.id.pick_image) {
            callback.pickImage();
        }
        dialog.dismiss();
    }

    public interface OnSelectImageRequest {
        void takePhoto();

        void pickImage();
    }
}
