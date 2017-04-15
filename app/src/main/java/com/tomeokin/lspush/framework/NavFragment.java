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
package com.tomeokin.lspush.framework;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import com.decay.di.ProvideComponent;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.app.logger.AppLogger;
import com.tomeokin.lspush.framework.nav.ActivityResultAdapter;
import com.tomeokin.lspush.framework.nav.NavUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class NavFragment extends Fragment implements EasyPermissions.PermissionCallbacks {
    protected NavUtils navUtils;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        navUtils = new NavUtils(this);
    }

    /**
     * Gets a component for dependency injection by its type.
     */
    protected <C> C component(Class<C> componentType) {
        //noinspection unchecked
        return componentType.cast(((ProvideComponent<C>) getActivity()).component());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!navUtils.dispatchActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public boolean hasPermissions(@NonNull String... perms) {
        return EasyPermissions.hasPermissions(context(), perms);
    }

    public void requestPermissions(@StringRes int rationale, final int requestCode, @NonNull String[] perms) {
        requestPermissions(getString(rationale), requestCode, perms);
    }

    public void requestPermissions(@NonNull String rationale, final int requestCode, @NonNull String[] perms) {
        EasyPermissions.requestPermissions(this, rationale, requestCode, needPermissions(perms));
    }

    public String[] needPermissions(@NonNull String[] permissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : permissions) {
            if (!hasPermissions(perm)) {
                result.add(perm);
            }
        }

        return result.toArray(new String[result.size()]);
    }

    //public boolean hasPermission(String permission) {
    //    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    //        return getContext().checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    //    }
    //    // other
    //    return true;
    //}

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if (perms == null) return;
        AppLogger.of(null).logStub().log("Permissions Granted", Arrays.toString(perms.toArray()));
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            // @formatter:off
            new AppSettingsDialog.Builder(this)
                .setTitle(R.string.settings_dialog_title)
                .setRationale(R.string.rationale_ask_again)
                .setPositiveButton(R.string.setting)
                .setNegativeButton(R.string.cancel)
                .build()
                .show();
            // @formatter:on
            navUtils.registerActivityResult(new ActivityResultAdapter(AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
                @Override
                public void onRequestSuccess(Intent data) {
                    /* no-op */
                }
            });
        }
    }

    public NavFragment self() {
        return this;
    }

    public Context context() {
        return getContext();
    }


}
