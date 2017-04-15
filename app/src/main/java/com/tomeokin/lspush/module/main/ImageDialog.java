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
package com.tomeokin.lspush.module.main;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.tomeokin.lspush.R;

public class ImageDialog implements OnClickListener {
    private Context context;
    private DialogPlus dialog;
    private OnDialogImageConfirmListener onImageConfirmListener;

    private ImageView imgView;
    private String imgUrl;
    private int imgWidth;
    private int imgHeight;

    public ImageDialog(Activity activity, String imageUrl, int width, int height,
        @NonNull OnDialogImageConfirmListener listener) {

        context = activity;
        imgUrl = imageUrl;
        onImageConfirmListener = listener;
        final View content = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        final int maxWidth = content.getWidth() - 50;
        final int maxHeight = content.getHeight() / 2;
        final float radio = optimumRadio(maxWidth, maxHeight, width, height);
        imgWidth = (int) (width * radio);
        imgHeight = (int) (height * radio);

        View view = View.inflate(activity, R.layout.dialog_web_image_confirm, null);
        imgView = (ImageView) view.findViewById(R.id.image_target);

        dialog = DialogPlus.newDialog(activity)
            .setCancelable(true)
            .setContentHolder(new ViewHolder(view))
            .setGravity(Gravity.CENTER)
            .setOnClickListener(this)
            //.setContentWidth(imgWidth)
            //.setContentHeight(imgHeight)
            .create();
    }

    @Override
    public void onClick(DialogPlus dialog, View view) {
        int id = view.getId();
        if (id == R.id.positive_btn) {
            onImageConfirmListener.onImageConfirm(imgUrl, imgWidth, imgHeight);
            dismiss();
        } else if (id == R.id.negative_btn) {
            dismiss();
        }
    }

    public void show() {
        dialog.show();

        //final Uri imgUri = Uri.parse(imgUrl);
        Glide.with(context)
            .load(imgUrl)
            .error(R.drawable.loading_failed)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(imgWidth, imgHeight)
            .fitCenter()
            .into(imgView);
    }

    public void dismiss() {
        Glide.clear(imgView);
        dialog.dismiss();
    }

    public static float optimumRadio(float maxWidth, float maxHeight, float imageWidth, float imageHeight) {
        float radio = 1;
        if (imageWidth > maxWidth || imageHeight > maxHeight) {
            final float radioWidth = maxWidth / imageWidth * 1.0f;
            final float radioHeight = maxHeight / imageHeight * 1.0f;
            radio = (float) Math.floor(Math.min(radioWidth, radioHeight) * 5) / 5;
        }
        return radio;
    }

    public interface OnDialogImageConfirmListener {
        void onImageConfirm(String imgUrl, int width, int height);
    }
}
