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
package com.tomeokin.lspush.module.main.com;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.decay.di.ProvideComponent;
import com.jaeger.library.StatusBarUtil;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.framework.NavActivity;
import com.tomeokin.lspush.framework.nav.NavOption;
import com.tomeokin.lspush.module.app.App;
import com.tomeokin.lspush.module.main.di.MainComponent;
import com.tomeokin.lspush.module.search.com.SearchActivity;
import com.tomeokin.lspush.widget.NoScrollViewPager;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;

public class MainActivity extends NavActivity implements ProvideComponent<MainComponent> {
    private static final String SAVE_INSTANCE_PAGES
        = "com.tomeokin.lspush.module.main.com.MainActivity.save.instance.pages";
    private static final int REQUEST_PERMISSION_READ_PHONE_STATE = 111;
    public static final String[] PERMISSION_READ_PHONE_STATE = new String[] {
        Manifest.permission.READ_PHONE_STATE
    };

    static final NavOption navOption;

    ImageView homeBtn;
    ImageView findBtn;
    ImageView meBtn;
    NoScrollViewPager viewPager;

    Toolbar toolbar;
    ActionBar actionBar;
    TextView toolbarTitle;

    ImageView[] tabs;
    // @formatter:off
    final int[][] TAB_ICONS = new int[][] {
        { R.drawable.home_selected, R.drawable.home_unselected },
        { R.drawable.find_selected, R.drawable.find_unselected },
        { R.drawable.me_selected, R.drawable.me_unselected },
    };
    // @formatter:on
    @IdRes private int selectedTab;
    static final List<Pair<String, Class<Fragment>>> PAGES = new ArrayList<>();

    static {
        navOption = NavOption.newBuilder().transit(FragmentTransaction.TRANSIT_NONE).build();
    }

    private View.OnClickListener onTabSelectAdapter = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            moveTo(v.getId());
        }
    };

    @Override
    public MainComponent component() {
        return ((App) getApplication()).mainComponent(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navUtils.setFragmentNavDisabled(true);

        component().inject(this);

        homeBtn = (ImageView) findViewById(R.id.main_home_btn);
        findBtn = (ImageView) findViewById(R.id.main_find_btn);
        meBtn = (ImageView) findViewById(R.id.main_me_btn);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.statusBarColor), 30);
        }

        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            setToolbarTitle(getTitle());
        }

        addTab(getString(R.string.main_home), HomeFragment.class);
        addTab(getString(R.string.main_find), FindFragment.class);
        addTab(getString(R.string.main_me), MeFragment.class);

        viewPager = (NoScrollViewPager) findViewById(R.id.content_viewpager);
        MainPageAdapter pageAdapter = new MainPageAdapter(context(), getSupportFragmentManager());
        viewPager.setAdapter(pageAdapter);
        viewPager.setPageScrollEnabled(false);

        tabs = new ImageView[] { homeBtn, findBtn, meBtn };
        selectedTab = View.NO_ID;

        homeBtn.setOnClickListener(onTabSelectAdapter);
        findBtn.setOnClickListener(onTabSelectAdapter);
        meBtn.setOnClickListener(onTabSelectAdapter);

        int pageId = R.id.main_home_btn;
        if (savedInstanceState != null) {
            pageId = savedInstanceState.getInt(SAVE_INSTANCE_PAGES, R.id.main_home_btn);
        }
        moveTo(pageId);
    }

    public void setToolbarTitle(CharSequence title) {
        toolbarTitle.setText(title);
    }

    public void setToolbarTitle(@StringRes int title) {
        toolbarTitle.setText(title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SAVE_INSTANCE_PAGES, selectedTab);
        super.onSaveInstanceState(outState);
    }

    public void addTab(String title, Class<? extends Fragment> fragment) {
        //noinspection unchecked
        PAGES.add(Pair.create(title, (Class<Fragment>) fragment));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        requestPhoneState();
    }

    @AfterPermissionGranted(REQUEST_PERMISSION_READ_PHONE_STATE)
    public void requestPhoneState() {
        if (!hasPermissions(PERMISSION_READ_PHONE_STATE)) {
            requestPermissions(getString(R.string.read_phone_state_rationale), REQUEST_PERMISSION_READ_PHONE_STATE,
                PERMISSION_READ_PHONE_STATE);
        }
    }

    @Override
    protected void onDestroy() {
        ((App) getApplication()).releaseMainComponent();
        super.onDestroy();
    }

    void moveTo(@IdRes int tabId) {
        if (selectedTab == tabId) {
            return;
        }
        selectedTab = tabId;

        //Class<? extends Fragment> fragment = pages.get(tabId);
        //navUtils.moveTo(fragment, false, navOption);
        for (int i = 0; i < tabs.length; i++) {
            ImageView iv = tabs[i];
            if (iv.getId() == tabId) {
                viewPager.setCurrentItem(i);
                setToolbarTitle(PAGES.get(i).first);
                iv.setImageResource(TAB_ICONS[i][0]);
            } else {
                iv.setImageResource(TAB_ICONS[i][1]);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_collect) {
            navUtils.startActivity(CollectEditorActivity.class, CollectEditorActivity.create(null));
        } else if (id == R.id.action_search) {
            navUtils.startActivity(SearchActivity.class);
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public static class MainPageAdapter extends FragmentPagerAdapter {
        private final Context context;

        public MainPageAdapter(Context context, FragmentManager fm) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            Class<Fragment> target = PAGES.get(position).second;
            return Fragment.instantiate(context, target.getName());
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return PAGES.get(position).first;
        }

        @Override
        public int getCount() {
            return PAGES.size();
        }
    }
}
