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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.decay.di.ProvideComponent;
import com.decay.logger.Log;
import com.decay.recyclerview.adapter.SectionAdapter;
import com.decay.recyclerview.decoration.DividerDecoration;
import com.decay.recyclerview.listener.EndlessRecyclerOnScrollListener;
import com.decay.recyclerview.section.LoadMoreFooter;
import com.decay.utillty.ClipboardUtils;
import com.decay.utillty.ListUtils;
import com.decay.utillty.ViewHelper;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import com.tomeokin.lspush.R;
import com.tomeokin.lspush.app.logger.AppLogger;
import com.tomeokin.lspush.data.model.Collect;
import com.tomeokin.lspush.data.model.Comment;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.data.remote.RxRequestAdapter;
import com.tomeokin.lspush.framework.WebViewActivity;
import com.tomeokin.lspush.framework.prefer.Prefer;
import com.tomeokin.lspush.module.app.App;
import com.tomeokin.lspush.module.main.CommentAdapter;
import com.tomeokin.lspush.module.main.di.MainComponent;
import com.tomeokin.lspush.module.user.com.UserActivity;

import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class CollectWebViewActivity extends WebViewActivity
        implements ProvideComponent<MainComponent>, CommentAdapter.OnItemClickListener {

    public static final String ARG_COLLECT_ID = "arg.collect.id";
    public static final String ARG_SHOW_COMMENT = "arg.show.comment";
    public static final String REQUEST_DATA_COLLECT_ID
            = "com.tomeokin.lspush.module.main.com.CollectWebViewActivity.collect.id";
    private Log log = AppLogger.of("CollectWebViewActivity");
    private ImageButton backBtn;
    private ImageButton forwardBtn;

    private ImageView favorBtn;
    private TextView favorCountTv;
    private ImageView commentBtn;
    private TextView commentCountTv;
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private EndlessRecyclerOnScrollListener endlessScrollListener;
    private SectionAdapter sectionAdapter;
    private LoadMoreFooter loadMoreFooter;

    private ImageView refreshIv;
    private EditText commentEt;
    private TextView submitTv;

    private int TEXT_COLOR_YES;
    private int TEXT_COLOR_NO;
    private Collect collect;

    PublishSubject<List<Comment>> commentSubject = PublishSubject.create();
    private final int PAGE_COUNT = 20;
    boolean showComment;
    boolean isFirst;
    int fixHeight;
    int wrapHeight = 162;

    @Inject
    HomePresenter homePresenter;
    @Inject
    Prefer prefer;

    @Override
    public MainComponent component() {
        return ((App) getApplication()).mainComponent(this);
    }

    public static Bundle create(Prefer prefer, Collect collect, boolean showComment) {
        prefer.put(String.valueOf(collect.getId()), collect);
        Bundle bundle = new Bundle();
        bundle.putLong(ARG_COLLECT_ID, collect.getId());
        bundle.putBoolean(ARG_SHOW_COMMENT, showComment);
        return bundle;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBottomBar(R.layout.layout_collect_webview_bottom_bar);

        component().inject(this);
        long colId = getIntent().getLongExtra(ARG_COLLECT_ID, -1);
        if (colId >= 0) {
            collect = prefer.get(String.valueOf(colId), Collect.class);
        }
        if (collect == null) {
            log.w("Request collect should not be null");
            log.w("The collect id value is %d", colId);
            finish();
        }
        showComment = getIntent().getBooleanExtra(ARG_SHOW_COMMENT, false);
        isFirst = true;

        setToolbarTitle(collect.getTitle());
        setNavigationIcon(R.drawable.close);
        setDisplayHomeAsUpEnabled(true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveResultAndBack();
            }
        });

        TEXT_COLOR_YES = ContextCompat.getColor(this, R.color.blue_4_whiteout);
        TEXT_COLOR_NO = ContextCompat.getColor(this, R.color.blue_gray_30);

        backBtn = (ImageButton) findViewById(R.id.back_btn);
        forwardBtn = (ImageButton) findViewById(R.id.forward_btn);
        favorBtn = (ImageView) findViewById(R.id.favor_btn);
        favorCountTv = (TextView) findViewById(R.id.favor_count_tv);
        commentBtn = (ImageView) findViewById(R.id.comment_btn);
        commentCountTv = (TextView) findViewById(R.id.comment_count_tv);
        recyclerView = (RecyclerView) findViewById(R.id.comment_rv);
        refreshIv = (ImageView) findViewById(R.id.refresh_iv);
        commentEt = (EditText) findViewById(R.id.comment_et);
        submitTv = (TextView) findViewById(R.id.submit_comment_tv);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x5WebView.goBack();
            }
        });
        forwardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                x5WebView.goForward();
            }
        });
        x5WebView.setWebViewClient(new WebViewClient() {

            /**
             * 防止加载网页时调起系统浏览器
             */
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, String url) {
                webView.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                super.onPageStarted(webView, s, bitmap);
                ViewHelper.setViewEnabled(backBtn, webView.canGoBack());
                ViewHelper.setViewEnabled(forwardBtn, webView.canGoForward());
            }

            @Override
            public void onPageFinished(WebView webView, String s) {
                super.onPageFinished(webView, s);
                ViewHelper.setViewEnabled(backBtn, webView.canGoBack());
                ViewHelper.setViewEnabled(forwardBtn, webView.canGoForward());
            }
        });
        ViewHelper.setViewEnabled(backBtn, false);
        ViewHelper.setViewEnabled(forwardBtn, false);

        favorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFavorBtnClick();
            }
        });
        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCommentBtnClick();
            }
        });
        updateCollectView();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerDecoration decoration = new DividerDecoration(this, DividerDecoration.VERTICAL);
        decoration.setEndOffset(1);
        recyclerView.addItemDecoration(decoration);
        endlessScrollListener = new EndlessRecyclerOnScrollListener() {
            @Override
            public void onLoadMore() {
                loadMoreFooter.setState(LoadMoreFooter.STATE_LOADING);
                sectionAdapter.notifyDataSetChanged();
                loadNextCommentList();
            }
        };
        recyclerView.addOnScrollListener(endlessScrollListener);
        commentAdapter = new CommentAdapter(this);
        sectionAdapter = new SectionAdapter(commentAdapter);
        loadMoreFooter = new LoadMoreFooter(context(), new LoadMoreFooter.OnLoadMoreClickListener() {
            @Override
            public void onLoadMoreClick(LoadMoreFooter footer, int state) {
                if (state == LoadMoreFooter.STATE_ERROR) {
                    footer.setState(LoadMoreFooter.STATE_LOADING);
                    sectionAdapter.notifyDataSetChanged();
                }
            }
        });
        loadMoreFooter.setNoMoreText(getString(R.string.no_comment));
        loadMoreFooter.setState(LoadMoreFooter.STATE_NO_MORE);
        sectionAdapter.addFooter(loadMoreFooter);
        sectionAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(sectionAdapter);

        x5WebView.loadUrl(collect.getUrl());

        refreshIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshCommentList();
            }
        });
        submitTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitComment();
            }
        });

        bottomSheetBehavior.setHideable(false);
        fixHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56,
                getResources().getDisplayMetrics());
        // https://code.google.com/p/android/issues/detail?can=2&q=type%3DDefect%20BottomSheetBehavior&colspec=ID%20Type%20Status%20Owner%20Summary%20Stars&groupby=&sort=-stars&id=234951
        if (Build.VERSION.SDK_INT <= 19) {
            bottomSheetBehavior.setPeekHeight(wrapHeight);
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    //if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    //    bottomSheetBehavior.setPeekHeight(wrapHeight);
                    //}
                    log.logStub().i("StateChanged newState %d peek height: %d", newState, bottomSheetBehavior.getPeekHeight());
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    log.logStub().i("* peek height: %d to %d", bottomSheetBehavior.getPeekHeight(), fixHeight);
                    bottomSheetBehavior.setPeekHeight(fixHeight);
                    //log.i("* peek height: %d", bottomSheetBehavior.getPeekHeight());
                }
            });

            bottomLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
                        log.i("bottomLayout peek height: %d to %d", bottomSheetBehavior.getPeekHeight(), wrapHeight);
                        bottomSheetBehavior.setPeekHeight(wrapHeight);
                        log.i("bottomLayout peek height: %d", bottomSheetBehavior.getPeekHeight());
                    }
                }
            });
        }
        if (showComment) {
            bottomSheetBehavior.setPeekHeight(fixHeight);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        subscribeCommentSubject();
        refreshCommentList();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void refreshCommentList() {
        endlessScrollListener.setEnabled(false);
        sectionAdapter.notifyDataSetChanged();
        homePresenter.getComments(collect.getId(), 0, PAGE_COUNT,
                new RxRequestAdapter<List<Comment>>(context(), false) {
                    @Override
                    public void onRequestSuccess(List<Comment> data) {
                        publishCollectList(true, data);
                    }

                    @Override
                    public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
                        super.onRequestFailed(t, message);
                        sectionAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void loadNextCommentList() {
        int page = commentAdapter.getItemCount() / PAGE_COUNT;
        loadMoreFooter.setState(LoadMoreFooter.STATE_LOADING);
        homePresenter.getComments(collect.getId(), page, PAGE_COUNT,
                new RxRequestAdapter<List<Comment>>(context(), false) {
                    @Override
                    public void onRequestSuccess(List<Comment> data) {
                        publishCollectList(false, data);
                    }

                    @Override
                    public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
                        super.onRequestFailed(t, message);
                        loadMoreFooter.setState(LoadMoreFooter.STATE_ERROR);
                        sectionAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void publishCollectList(boolean update, List<Comment> commentList) {
        if (commentList.size() < PAGE_COUNT) {
            endlessScrollListener.setEnabled(false);
            loadMoreFooter.setState(LoadMoreFooter.STATE_NO_MORE);
            sectionAdapter.notifyDataSetChanged();
        } else {
            endlessScrollListener.setEnabled(true);
            loadMoreFooter.setState(LoadMoreFooter.STATE_HIDE);
            sectionAdapter.notifyDataSetChanged();
        }

        if (update) {
            commentAdapter.setCommentList(commentList);
            sectionAdapter.notifyDataSetChanged();
        } else {
            commentSubject.onNext(commentList);
        }

        collect.setCommentCount(commentList.size());
        updateCollectView();
    }

    public void subscribeCommentSubject() {
        // @formatter:off
        commentSubject
                .subscribeOn(Schedulers.computation())
                .map(new Func1<List<Comment>, List<Comment>>() {
                    @Override
                    public List<Comment> call(List<Comment> comments) {
                        final List<Comment> commentList = commentAdapter.getCommentList();
                        return ListUtils.combineSortList(commentList, comments, Comment.UPDATE_COMPARATOR);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Comment>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(List<Comment> collects) {
                        commentAdapter.setCommentList(collects);
                        sectionAdapter.notifyDataSetChanged();
                    }
                });
        // @formatter:on
    }

    public void onFavorBtnClick() {
        if (Build.VERSION.SDK_INT <= 19) {
            log.i("FavorBtn state: %d peek height: %d to %d", bottomSheetBehavior.getState(),
                    bottomSheetBehavior.getPeekHeight(), wrapHeight);
            int state = bottomSheetBehavior.getState();
            bottomSheetBehavior.setPeekHeight(wrapHeight);
            bottomSheetBehavior.setState(state);
            log.i("FavorBtn peek height: %d", bottomSheetBehavior.getPeekHeight());
        }
        collect.toggleFavor();
        updateCollectView();
        if (collect.isHasFavor()) {
            homePresenter.addFavor(collect.getId(), new RxRequestAdapter<Void>(context(), false) {
                @Override
                public void onRequestSuccess(Void data) {}

                @Override
                public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
                    super.onRequestFailed(t, message);
                    collect.toggleFavor();
                    updateCollectView();
                }
            });
        } else {
            homePresenter.removeFavor(collect.getId(), new RxRequestAdapter<Void>(context(), false) {
                @Override
                public void onRequestSuccess(Void data) {}

                @Override
                public void onRequestFailed(@Nullable Throwable t, @Nullable String message) {
                    super.onRequestFailed(t, message);
                    collect.toggleFavor();
                    updateCollectView();
                }
            });
        }
    }

    public void onCommentBtnClick() {
        if (isBottomBarShowing()) {
            if (Build.VERSION.SDK_INT <= 19) {
                log.i("CommentBtn peek height: %d to %d", bottomSheetBehavior.getPeekHeight(), fixHeight);
                bottomSheetBehavior.setPeekHeight(fixHeight);
            }
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            if (Build.VERSION.SDK_INT <= 19) {
                log.i("CommentBtn peek height: %d to %d", bottomSheetBehavior.getPeekHeight(), wrapHeight);
                bottomSheetBehavior.setPeekHeight(wrapHeight);
            }
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        log.i("CommentBtn peek height: %d", bottomSheetBehavior.getPeekHeight());
    }

    public void submitComment() {
        String comment = commentEt.getText().toString();
        if (TextUtils.isEmpty(comment)) {
            return;
        }
        homePresenter.addComment(collect.getId(), comment, new RxRequestAdapter<Comment>(context(), false) {
            @Override
            public void onRequestSuccess(Comment data) {
                commentEt.setText(null);

                commentAdapter.addItemAt(0, data);
                sectionAdapter.notifyDataSetChanged();

                collect.setCommentCount(commentAdapter.getItemCount());
                updateCollectView();
            }
        });
    }

    @Override
    public void onUserClick(int position, Comment comment, User user) {
        navUtils.startActivity(UserActivity.class, UserActivity.create(prefer, user));
    }

    public void updateCollectView() {
        if (collect.isHasFavor()) {
            favorCountTv.setTextColor(TEXT_COLOR_YES);
            favorBtn.setImageResource(R.drawable.favor_yes);
        } else {
            favorCountTv.setTextColor(TEXT_COLOR_NO);
            favorBtn.setImageResource(R.drawable.favor_no);
        }
        favorCountTv.setText(String.valueOf(collect.getFavorCount()));
        commentCountTv.setText(String.valueOf(collect.getCommentCount()));
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        ViewCompat.setTranslationY(progressBar, verticalOffset);
    }

    @Override
    public void onBackPressed() {
        if (isBottomBarShowing()) {
            bottomSheetBehavior.setPeekHeight(fixHeight);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }

        saveResultAndBack();
    }

    public void saveResultAndBack() {
        prefer.put(String.valueOf(collect.getId()), collect);
        Intent data = new Intent();
        data.putExtra(REQUEST_DATA_COLLECT_ID, collect.getId());
        setResult(Activity.RESULT_OK, data);

        super.onBackPressed();
    }

    public static Collect resolveCollect(Prefer prefer, @NonNull Intent data) {
        long colId = data.getLongExtra(REQUEST_DATA_COLLECT_ID, -1);
        if (colId == -1) return null;
        return prefer.get(String.valueOf(colId), Collect.class);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_collect_webview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_copy_current_link:
                String currentUrl = x5WebView.getUrl();
                ClipboardUtils.setText(this, currentUrl);
                Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_share:
                String text = collect.getTitle() + " " + collect.getUrl();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, text);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "分享到"));
                return true;
            case R.id.action_see_share_user:
                navUtils.startActivity(UserActivity.class, UserActivity.create(prefer, collect.getUser()));
                return true;
            case R.id.action_open_in_browser:
                Intent viewIntent = new Intent();
                viewIntent.setAction(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(x5WebView.getUrl());
                viewIntent.setData(uri);
                startActivity(Intent.createChooser(viewIntent, "选择"));
                return true;
            case R.id.action_refresh:
                x5WebView.reload();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
