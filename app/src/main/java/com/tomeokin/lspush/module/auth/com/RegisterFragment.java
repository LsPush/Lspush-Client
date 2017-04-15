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

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.decay.logger.Log;
import com.decay.utillty.FileNameUtils;
import com.decay.utillty.ImageIntentUtils;
import com.decay.utillty.ViewHelper;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.app.logger.AppLogger;
import com.tomeokin.lspush.data.model.AccessBundle;
import com.tomeokin.lspush.data.model.RegisterData;
import com.tomeokin.lspush.data.remote.RxRequestAdapter;
import com.tomeokin.lspush.framework.NavFragment;
import com.tomeokin.lspush.framework.nav.ActivityResultAdapter;
import com.tomeokin.lspush.module.auth.SelectImageDialog;
import com.tomeokin.lspush.module.auth.di.AuthComponent;
import com.tomeokin.lspush.widget.PasswordFilter;
import com.tomeokin.lspush.widget.PasswordUtils;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;

import pub.devrel.easypermissions.AfterPermissionGranted;

public class RegisterFragment extends NavFragment implements SelectImageDialog.OnSelectImageRequest {
    private static final int REQUEST_PICK_IMAGE = 101;
    private static final int REQUEST_TAKE_PHOTO = 102;
    private static final int REQUEST_PERMISSION_PICK_IMAGE = 201;
    private static final int REQUEST_PERMISSION_TAKE_PHOTO = 202;
    public static final String[] DEFAULT_AVATAR = new String[] {
        "http://101.201.65.221/api/resource/download/ele.png", "http://101.201.65.221/api/resource/download/jm.png",
    };

    public static Log log = AppLogger.of("RegisterFragment");
    public static final String ARG_TICKET = "arg.ticket";
    private final TextWatcher textChangeAdapter = new TextWatchAdapter() {
        @Override
        public void afterTextChanged(Editable s) {
            checkTextField();
        }
    };

    private SelectImageDialog selectImageDialog;
    private String ticket;
    private ImageView avatarIv;
    private EditText userNameEditText;
    private EditText passwordEditText;
    private CheckableImageButton passwordToggle;
    private TextView submitRegisterBtn;

    private File takePhotoFile;
    private File pickImageFile;
    private File avatarFile;
    private String avatar;

    @Inject AuthPresenter authPresenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {

        final Bundle args = getArguments();
        if (args != null) {
            ticket = args.getString(ARG_TICKET);
            if (ticket == null) {
                log.logStub().w("Need Ticket to register!");
            } else {
                log.log("ticket: %s", ticket);
            }
        }

        component(AuthComponent.class).inject(this);

        final View view = inflater.inflate(R.layout.fragment_register, container, false);
        avatarIv = (ImageView) view.findViewById(R.id.avatar_iv);
        userNameEditText = (EditText) view.findViewById(R.id.input_userName_editText);
        passwordEditText = (EditText) view.findViewById(R.id.input_password_editText);
        passwordToggle = (CheckableImageButton) view.findViewById(R.id.password_toggle);
        submitRegisterBtn = (TextView) view.findViewById(R.id.submit_register_btn);
        TextView gotoLoginBtn = (TextView) view.findViewById(R.id.goto_login);

        int i = (int) (new Date().getTime() % DEFAULT_AVATAR.length);
        avatar = DEFAULT_AVATAR[i];

        // @formatter:off
        Glide.with(this)
            .load(avatar)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            // when using transform or bitmapTransform, don't use fitCenter() or centerCrop()
            .transform(new CircleTransform(context()))
            .into(avatarIv);
        // @formatter:on

        avatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectImageDialog();
            }
        });
        userNameEditText.addTextChangedListener(textChangeAdapter);
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
        ViewHelper.setViewEnabled(submitRegisterBtn, false);

        submitRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitRegister();
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

    public static Bundle create(String ticket) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TICKET, ticket);
        return bundle;
    }

    public void checkTextField() {
        ViewHelper.setViewEnabled(submitRegisterBtn,
            isUserNameValid() && PasswordUtils.isPasswordValid(passwordEditText));
    }

    public boolean isUserNameValid() {
        final int length = userNameEditText.getText().toString().trim().length();
        return length >= 3 && length <= 12;
    }

    public void showSelectImageDialog() {
        if (selectImageDialog == null) {
            selectImageDialog = new SelectImageDialog(context(), this);
        }
        selectImageDialog.show();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_TAKE_PHOTO)
    @Override
    public void takePhoto() {
        if (!hasPermissions(ImageIntentUtils.PERMISSION_TAKE_PHOTO)) {
            requestPermissions(R.string.take_photo_rationale, REQUEST_PERMISSION_TAKE_PHOTO,
                ImageIntentUtils.PERMISSION_TAKE_PHOTO);
            return;
        }

        takePhotoFile = FileNameUtils.getJPEGFile(getContext());
        Intent intent = ImageIntentUtils.createTakePhotoIntent(takePhotoFile);
        boolean canTakePhoto = intent.resolveActivity(getContext().getPackageManager()) != null;
        if (!canTakePhoto) {
            showErrorMessage(R.string.no_suitable_camera);
            return;
        }

        navUtils.startActivityForResult(intent, new ActivityResultAdapter(REQUEST_TAKE_PHOTO) {
            @Override
            public void onRequestSuccess(Intent data) {
                if (takePhotoFile != null && takePhotoFile.exists()) {
                    cropImage(takePhotoFile);
                } else {
                    showErrorMessage(R.string.could_not_access_image);
                }
            }
        });
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_PICK_IMAGE)
    @Override
    public void pickImage() {
        if (!hasPermissions(ImageIntentUtils.PERMISSION_PICK_IMAGE)) {
            requestPermissions(R.string.pick_image_rationale, REQUEST_PERMISSION_PICK_IMAGE,
                ImageIntentUtils.PERMISSION_PICK_IMAGE);
            return;
        }

        Intent intent = ImageIntentUtils.createSelectImageIntent();
        navUtils.startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image_title)),
            new ActivityResultAdapter(REQUEST_PICK_IMAGE) {
                @Override
                public void onRequestSuccess(Intent data) {
                    File file = ImageIntentUtils.resolveSelectImageData(context(), data);
                    if (file == null) {
                        showErrorMessage(R.string.read_image_failed);
                    } else if (!ImageIntentUtils.isImage(file)) {
                        showErrorMessage(R.string.select_file_not_image);
                    } else {
                        pickImageFile = file;
                        cropImage(pickImageFile);
                    }
                }
            });
    }

    // 根据 uri 裁剪图片
    private void cropImage(@NonNull File file) {
        avatarFile = FileNameUtils.getJPEGFile(context());

        CropImage.activity(Uri.fromFile(file))
            .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
            .setOutputUri(Uri.fromFile(avatarFile))
            .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
            .setMinCropResultSize(450, 450)
            .setRequestedSize(512, 512)
            .setMaxCropResultSize(768, 768)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(getContext(), this);
        navUtils.registerActivityResult(new ActivityResultAdapter(CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            @Override
            public void onRequestSuccess(Intent data) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (avatarFile == null || !avatarFile.exists()) {
                    avatarFile = new File(result.getUri().toString());
                }
                if (avatarFile == null) {
                    showErrorMessage(R.string.could_not_access_image);
                    return;
                }

                log.log("crop-image result uri: %s", avatarFile.getAbsolutePath());
                updateAvatar(avatarFile);
            }
        });
    }

    public void updateAvatar(File file) {
        // @formatter:off
        Glide.with(this)
            .load(file)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            // when using transform or bitmapTransform, don't use fitCenter() or centerCrop()
            .transform(new CircleTransform(context()))
            .into(avatarIv);
        // @formatter:on
    }

    public void showErrorMessage(@StringRes int message) {
        showErrorMessage(getString(message));
    }

    public void showErrorMessage(String message) {
        ((AuthActivity) getActivity()).showErrorMessage(message);
    }

    public void submitRegister() {
        RegisterData registerData = new RegisterData(ticket, userNameEditText.getText().toString(),
            passwordEditText.getText().toString(), avatar);
        authPresenter.register(registerData, avatarFile, new RxRequestAdapter<AccessBundle>(context()) {
            @Override
            public void onRequestSuccess(AccessBundle data) {
                ((AuthActivity) getActivity()).gotoMain();
            }
        });
    }
}
