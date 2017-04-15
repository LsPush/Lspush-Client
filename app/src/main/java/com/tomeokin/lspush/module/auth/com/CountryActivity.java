package com.tomeokin.lspush.module.auth.com;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.decay.anim.AnimationListenerAdapter;
import com.decay.country.Country;
import com.decay.country.CountryUtils;
import com.decay.recyclerview.adapter.SectionAdapter;
import com.decay.recyclerview.callback.HeaderProvider;
import com.decay.recyclerview.callback.OnIndexChangeListener;
import com.decay.recyclerview.decoration.DividerDecoration;
import com.decay.recyclerview.decoration.StickyHeaderDecoration;
import com.decay.recyclerview.index.IndexBar;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.framework.ToolbarActivity;
import com.tomeokin.lspush.module.auth.CountryAdapter;
import com.tomeokin.lspush.module.auth.CountryDialog;
import com.tomeokin.lspush.module.auth.OnCountrySelectedListener;
import com.tomeokin.lspush.module.auth.SearchHeader;
import com.tomeokin.lspush.widget.FitCenterImageSpan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CountryActivity extends ToolbarActivity
    implements OnIndexChangeListener, View.OnClickListener, CountryAdapter.OnItemClickListener, OnCountrySelectedListener {

    public static final String RESULT_COUNTRY = "com.tomeokin.lspush.module.auth.com.CountryActivity.Country";
    public static final String RESULT_COUNTRY_CODE = "com.tomeokin.lspush.module.auth.com.CountryActivity.CountryCode";
    public static final String RESULT_COUNTRY_NAME = "com.tomeokin.lspush.module.auth.com.CountryActivity.CountryName";

    private List<Country> countryList;
    private RecyclerView recyclerView;
    private TextView indexNotice;
    private AlphaAnimation fadeAnim;
    private SectionAdapter sectionAdapter;
    private SpannableString searchSpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_country);

        setDisplayHomeAsUpEnabled(true);
        setToolbarTitle(getString(R.string.select_country));

        final CountryUtils countryUtils = CountryUtils.getInstance(this);
        Collection<Country> countries = countryUtils.getCountries();
        countryList = new ArrayList<>(countries);
        final CountryAdapter adapter = new CountryAdapter(countryList, this); // .subList(9, countryList.size())
        sectionAdapter = new SectionAdapter(adapter);
        sectionAdapter.addHeader(new SearchHeader(this, this));

        recyclerView = (RecyclerView) findViewById(R.id.list_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(sectionAdapter);
        recyclerView.addItemDecoration(new StickyHeaderDecoration(this, new HeaderProvider() {
            @Nullable
            @Override
            public String getHeaderText(int position) {
                if (sectionAdapter.isHeaderPosition(position)) {
                    return null;
                }
                final int index = sectionAdapter.getAdapterPosition(position);
                return adapter.getHeaderText(index);
            }
        }));
        DividerDecoration decoration = new DividerDecoration(this, DividerDecoration.VERTICAL);
        decoration.setEndOffset(1);
        recyclerView.addItemDecoration(decoration);

        IndexBar indexBar = (IndexBar) findViewById(R.id.indexBar);
        List<String> indexTitles = new ArrayList<>();

        String lastItem = null;
        for (Country country : countryList) {
            if (country.group != null && !country.group.equals(lastItem)) {
                lastItem = country.group;
                indexTitles.add(lastItem.substring(0, 1));
            }
        }

        indexBar.setIndexTitles(indexTitles);
        indexBar.setOnIndexChangeListener(this);

        indexNotice = (TextView) findViewById(R.id.indexNotice);
        indexNotice.setVisibility(View.GONE);

        FitCenterImageSpan imageSpan = new FitCenterImageSpan(this, R.drawable.search_big);
        searchSpan = new SpannableString("search");
        searchSpan.setSpan(imageSpan, 0, "search".length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_tv) {
            if (appBar.getVisibility() == View.GONE) {
                return;
            }

            final Dialog dialog = new CountryDialog(this, this);

            final int height = appBar.getHeight();
            TranslateAnimation moveUpAnim = new TranslateAnimation(0, 0, 0, -height);
            moveUpAnim.setDuration(300);
            moveUpAnim.setAnimationListener(new AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    dialog.show();
                    appBar.setVisibility(View.GONE);
                    rootLayout.clearAnimation();
                }
            });
            final TranslateAnimation moveDownAnim = new TranslateAnimation(0, 0, -height, 0);
            moveDownAnim.setDuration(300);
            moveDownAnim.setAnimationListener(new AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    appBar.setVisibility(View.VISIBLE);
                    rootLayout.clearAnimation();
                    rootLayout.offsetTopAndBottom(height);
                    rootLayout.requestLayout();
                }
            });

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    rootLayout.startAnimation(moveDownAnim);
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    rootLayout.startAnimation(moveDownAnim);
                }
            });
            rootLayout.startAnimation(moveUpAnim);
        }
    }

    public void showIndexNotice(CharSequence indexTitle) {
        if (fadeAnim != null && !fadeAnim.hasEnded()) indexNotice.clearAnimation();

        indexNotice.setVisibility(View.VISIBLE);
        indexNotice.setText(indexTitle);
    }

    public void hideIndexNotice() {
        if (indexNotice.getVisibility() != View.VISIBLE) return;

        if (fadeAnim == null) {
            fadeAnim = new AlphaAnimation(1, 0);
            fadeAnim.setDuration(300);
            fadeAnim.setAnimationListener(new AnimationListenerAdapter() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    indexNotice.setVisibility(View.GONE);
                }
            });
            indexNotice.startAnimation(fadeAnim);
        } else if (fadeAnim.hasEnded()) {
            indexNotice.startAnimation(fadeAnim);
        }
    }

    @Override
    public void onIndexSelected(int index, String indexTitle) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (!(layoutManager instanceof LinearLayoutManager)) {
            return;
        }
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

        if (index < 0) {
            showIndexNotice(searchSpan);
            linearLayoutManager.scrollToPositionWithOffset(0, 0);
        } else {
            int i = queryIndexOfIndexTitles(index, indexTitle);
            if (i >= 0) {
                showIndexNotice(indexTitle);
                linearLayoutManager.scrollToPositionWithOffset(i + 1, 0);
            }
        }
    }

    private int queryIndexOfIndexTitles(int index, String indexTitle) {
        for (int i = index; i < countryList.size(); i++) {
            Country country = countryList.get(i);
            if (country.group.startsWith(indexTitle)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onIndexSelectedRelease() {
        hideIndexNotice();
    }

    @Override
    public void onItemClick(View view, CountryAdapter adapter, Country country, int position) {
        setResult(country);
    }

    @Override
    public void onCountrySelected(Dialog dialog, Country country) {
        dialog.dismiss();
        setResult(country);
    }

    public void setResult(Country country) {
        Intent data = new Intent();
        data.putExtra(RESULT_COUNTRY, country.country);
        data.putExtra(RESULT_COUNTRY_CODE, country.countryCode);
        data.putExtra(RESULT_COUNTRY_NAME, country.name);
        setResult(RESULT_OK, data);
        onBackPressed();
    }

    public static Country resolveData(Context context, Intent data) {
        String country = data.getStringExtra(RESULT_COUNTRY);
        if (country != null && country.length() != 0) {
            return CountryUtils.getInstance(context).getCountry(country);
        }
        return null;
    }
}
