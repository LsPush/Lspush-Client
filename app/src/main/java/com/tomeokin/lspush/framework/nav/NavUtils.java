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
package com.tomeokin.lspush.framework.nav;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.ArrayMap;

import com.tomeokin.lspush.R;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;

public class NavUtils {
    public static final @IdRes int DEFAULT_LAYOUT_ID = R.id.fragment_container;
    public static final boolean DEFAULT_ADD_TO_BACK_STACK = true;
    public static final int DEFAULT_REQUEST_CODE = 72;

    private FragmentActivity activity;
    private Fragment fragment;
    private @IdRes int containerId = DEFAULT_LAYOUT_ID;
    private boolean fragmentNavDisabled = false;
    private final ArrayMap<Integer, OnActivityResultListener> listeners = new ArrayMap<>();

    public NavUtils(FragmentActivity activity) {
        this.activity = activity;
    }

    public NavUtils(Fragment fragment) {
        this.fragment = fragment;
    }

    public void startActivity(Class<? extends Activity> target) {
        startActivity(target, null);
    }

    public void startActivity(Class<? extends Activity> target, Bundle args) {
        if (activity != null) {
            startActivity(activity, target, args);
        } else {
            startActivity(fragment, target, args);
        }
    }

    public static void startActivity(Activity activity, Class<? extends Activity> target, Bundle args) {
        Intent starter = new Intent(activity, target);
        if (args != null) {
            starter.putExtras(args);
        }
        activity.startActivity(starter);
    }

    public static void startActivity(Fragment fragment, Class<? extends Activity> target, Bundle args) {
        Intent starter = new Intent(fragment.getContext(), target);
        if (args != null) {
            starter.putExtras(args);
        }
        fragment.startActivity(starter);
    }

    public void startActivityForResult(Intent intent, ActivityResultAdapter adapter) {
        if (activity != null) {
            activity.startActivityForResult(intent, adapter.requestCode);
        } else {
            fragment.startActivityForResult(intent, adapter.requestCode);
        }
        registerActivityResult(adapter.requestCode, adapter);
    }

    public void startActivityForResult(Class<? extends Activity> target, ActivityResultAdapter adapter) {
        startActivityForResult(target, null, adapter);
    }

    public void startActivityForResult(Class<? extends Activity> target, Bundle args, ActivityResultAdapter adapter) {
        if (activity != null) {
            startActivityForResult(activity, target, adapter.requestCode, args);
        } else {
            startActivityForResult(fragment, target, adapter.requestCode, args);
        }
        registerActivityResult(adapter.requestCode, adapter);
    }

    public static void startActivityForResult(Activity activity, Class<? extends Activity> target, int requestCode,
        Bundle args) {
        Intent starter = new Intent(activity, target);
        if (args != null) {
            starter.putExtras(args);
        }
        activity.startActivityForResult(starter, requestCode);
    }

    public static void startActivityForResult(Fragment fragment, Class<? extends Activity> target, int requestCode,
        Bundle args) {
        Intent starter = new Intent(fragment.getContext(), target);
        if (args != null) {
            starter.putExtras(args);
        }
        fragment.startActivityForResult(starter, requestCode);
    }

    public void registerActivityResult(@NonNull ActivityResultAdapter adapter) {
        listeners.put(adapter.requestCode, adapter);
    }

    public void registerActivityResult(int requestCode, @NonNull OnActivityResultListener listener) {
        listeners.put(requestCode, listener);
    }

    public boolean dispatchActivityResult(int requestCode, int resultCode, Intent data) {
        if (listeners.size() == 0) return false;

        OnActivityResultListener listener = listeners.get(requestCode);
        if (listener == null) return false;

        if (resultCode == Activity.RESULT_OK) {
            listener.onRequestSuccess(data);
        } else {
            listener.onRequestCancel();
        }

        return true;
    }

    public int getContainerId() {
        return containerId;
    }

    public void setContainerId(@IdRes int containerId) {
        this.containerId = containerId;
    }

    public boolean isFragmentNavDisabled() {
        return fragmentNavDisabled;
    }

    public void setFragmentNavDisabled(boolean fragmentNavDisable) {
        this.fragmentNavDisabled = fragmentNavDisable;
    }

    public void moveTo(Class<? extends Fragment> target) {
        moveTo(target, null, DEFAULT_ADD_TO_BACK_STACK);
    }

    public void moveTo(Class<? extends Fragment> target, Bundle args) {
        moveTo(target, args, DEFAULT_ADD_TO_BACK_STACK);
    }

    public void moveTo(Class<? extends Fragment> target, NavOption option) {
        moveTo(target, null, DEFAULT_ADD_TO_BACK_STACK, option);
    }

    public void moveTo(Class<? extends Fragment> target, boolean addToBackStack) {
        moveTo(target, null, addToBackStack, null);
    }

    public void moveTo(Class<? extends Fragment> target, Bundle args, NavOption option) {
        moveTo(target, args, DEFAULT_ADD_TO_BACK_STACK, option);
    }

    public void moveTo(Class<? extends Fragment> target, Bundle args, boolean addToBackStack) {
        moveTo(target, args, addToBackStack, null);
    }

    public void moveTo(Class<? extends Fragment> target, boolean addToBackStack, @Nullable NavOption option) {
        moveTo(target, null, addToBackStack, option);
    }

    public void moveTo(Class<? extends Fragment> target, Bundle args, boolean addToBackStack,
        @Nullable NavOption option) {
        if (fragmentNavDisabled) return;

        if (activity != null) {
            moveTo(activity, activity.getSupportFragmentManager(), containerId, target, args, addToBackStack, option);
        } else {
            moveTo(fragment.getContext(), fragment.getFragmentManager(), containerId, target, args, addToBackStack,
                option);
        }
    }

    public static void moveTo(Context context, FragmentManager fragmentManager, @IdRes int containerId,
        Class<? extends Fragment> fragment, Bundle args, boolean addToBackStack, @Nullable NavOption option) {

        String tag = null;
        int requestCode;
        int[] anim = null;
        int transit = FragmentTransaction.TRANSIT_UNSET;
        boolean addFirstIntoBackStack = false;
        if (option != null) {
            tag = option.tag;
            requestCode = option.requestCode;
            anim = option.anim;
            transit = option.transition;
            addFirstIntoBackStack = option.addFirstIntoBackStack;
        } else {
            requestCode = DEFAULT_REQUEST_CODE;
        }

        if (tag == null || tag.length() == 0) {
            tag = fragment.getName();
        }

        Fragment current = fragmentManager.findFragmentById(containerId);
        Fragment target = fragmentManager.findFragmentByTag(tag);

        if (target == null) {
            try {
                target = Fragment.instantiate(context, fragment.getName(), args);
            } catch (Exception e) {
                // ignore
            }
            if (target == null) {
                return;
            }

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (transit != FragmentTransaction.TRANSIT_UNSET) {
                transaction.setTransition(transit);
            } else if (anim == null || (anim.length != 2 && anim.length != 4)) {
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
            } else if (anim.length == 2) {
                transaction.setCustomAnimations(anim[0], anim[1]);
            } else {
                transaction.setCustomAnimations(anim[0], anim[1], anim[2], anim[3]);
            }
            if (current == null) {
                transaction.add(containerId, target, tag);
                if (addFirstIntoBackStack) {
                    transaction.addToBackStack(tag);
                }
            } else {
                transaction.replace(containerId, target, tag);
                if (addToBackStack) {
                    transaction.addToBackStack(tag);
                }
            }
            transaction.commit();
        } else {
            if (current == target) {
                return;
            }
            // set result
            Intent intent = new Intent();
            if (args != null) {
                intent.putExtras(args);
            }
            target.onActivityResult(requestCode, Activity.RESULT_OK, intent);
            popToBackStackImmediate(fragmentManager, tag);
        }
    }

    public static void popToBackStackImmediate(FragmentManager fragmentManager, String tag) {
        boolean result = fragmentManager.popBackStackImmediate(tag, 0);
        if (!result) {
            fragmentManager.popBackStackImmediate(0, POP_BACK_STACK_INCLUSIVE);
        }
    }
}
