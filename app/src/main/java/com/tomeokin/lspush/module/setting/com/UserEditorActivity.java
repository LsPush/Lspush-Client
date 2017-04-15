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
package com.tomeokin.lspush.module.setting.com;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.decay.di.ProvideComponent;
import com.decay.glide.CircleTransform;
import com.decay.logger.Log;
import com.decay.utillty.FileNameUtils;
import com.decay.utillty.ImageIntentUtils;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.app.logger.AppLogger;
import com.tomeokin.lspush.data.internal.CurrentUser;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.data.remote.RxRequestAdapter;
import com.tomeokin.lspush.framework.ToolbarActivity;
import com.tomeokin.lspush.framework.nav.ActivityResultAdapter;
import com.tomeokin.lspush.module.app.App;
import com.tomeokin.lspush.module.auth.SelectImageDialog;
import com.tomeokin.lspush.module.setting.di.SettingComponent;

import java.io.File;

import javax.inject.Inject;

import pub.devrel.easypermissions.AfterPermissionGranted;

public class UserEditorActivity extends ToolbarActivity
    implements ProvideComponent<SettingComponent>, SelectImageDialog.OnSelectImageRequest {
    public static Log log = AppLogger.of("UserEditorActivity");

    private static final int REQUEST_PICK_IMAGE = 301;
    private static final int REQUEST_TAKE_PHOTO = 302;
    private static final int REQUEST_PERMISSION_PICK_IMAGE = 401;
    private static final int REQUEST_PERMISSION_TAKE_PHOTO = 402;

    @Inject SettingPresenter settingPresenter;
    @Inject CurrentUser currentUser;
    User user;

    ImageView userAvatarIv;
    EditText usernameEt;
    EditText userDescEt;

    private SelectImageDialog selectImageDialog;
    private File takePhotoFile;
    private File pickImageFile;
    private File avatarFile;

    @Override
    public SettingComponent component() {
        return ((App) getApplication()).settingComponent(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolbarTitle("修改用户信息");
        setDisplayHomeAsUpEnabled(true);

        component().inject(this);
        setContentLayout(R.layout.activity_user_editor);

        usernameEt = (EditText) findViewById(R.id.username_et);
        userDescEt = (EditText) findViewById(R.id.user_desc_et);
        userAvatarIv = (ImageView) findViewById(R.id.user_avatar_iv);

        user = currentUser.getCurrentUser();

        // @formatter:off
        Glide.with(this)
            .load(user.getAvatar())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            // when using transform or bitmapTransform, don't use fitCenter() or centerCrop()
            .transform(new CircleTransform(context()))
            .into(userAvatarIv);
        // @formatter:on

        usernameEt.setText(user.getUsername());
        userDescEt.setText(user.getDescription());

        userAvatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectImageDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_submit) {
            updateUserInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateUserInfo() {
        user.setUsername(usernameEt.getText().toString());
        user.setDescription(userDescEt.getText().toString());
        settingPresenter.updateUserInfo(user, avatarFile, new RxRequestAdapter<User>(context()) {
            @Override
            public void onRequestSuccess(User data) {
                super.onRequestSuccess(data);
                Toast.makeText(UserEditorActivity.this, "更新用户信息成功", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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

        takePhotoFile = FileNameUtils.getJPEGFile(this);
        Intent intent = ImageIntentUtils.createTakePhotoIntent(takePhotoFile);
        boolean canTakePhoto = intent.resolveActivity(getPackageManager()) != null;
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
            .start(this);
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
            .into(userAvatarIv);
        // @formatter:on
    }

    public void showErrorMessage(@StringRes int message) {
        showErrorMessage(getString(message));
    }

    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
