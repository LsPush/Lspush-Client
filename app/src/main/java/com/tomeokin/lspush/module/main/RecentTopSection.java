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

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.decay.recyclerview.adapter.Section;
import com.tomeokin.lspush.R;

import java.util.Locale;

public class RecentTopSection extends Section {
    private AlertDialog dialog;
    private Context context;
    private int days = 6;
    private View.OnClickListener onDaySelectorClickListener;

    public RecentTopSection(Context context, @NonNull View.OnClickListener onDaySelectorClickListener) {
        super(R.layout.layout_recent_top_header);
        this.context = context;
        this.onDaySelectorClickListener = onDaySelectorClickListener;
    }

    @Override
    public RecyclerView.ViewHolder createSection(View view) {
        return new RecentTopHolder(view, onDaySelectorClickListener);
    }

    @Override
    public void populate(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RecentTopHolder) {
            final RecentTopHolder recentTopHolder = (RecentTopHolder) holder;
            recentTopHolder.daysSelector.setText(String.format(Locale.getDefault(), "最近%d天", days));
        }
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getDays() {
        return days;
    }

    public static class RecentTopHolder extends RecyclerView.ViewHolder {
        TextView titleTv;
        TextView daysSelector;

        public RecentTopHolder(View itemView, View.OnClickListener listener) {
            super(itemView);
            titleTv = (TextView) itemView.findViewById(R.id.title_tv);
            daysSelector = (TextView) itemView.findViewById(R.id.days_selector);

            daysSelector.setOnClickListener(listener);
        }
    }

    public void showDialog(@NonNull final OnDaySelectorDialogListener listener) {
        if (dialog == null) {
            final DaysAdapter adapter = new DaysAdapter(context);
            // @formatter:off
            dialog = new AlertDialog.Builder(context)
                .setSingleChoiceItems(adapter, days / 6 - 1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        listener.onDaySelected(RecentTopSection.this, dialog, (which + 1) * 6);
                    }
                })
                .setCancelable(true).create();
            // @formatter:on
        }
        if (dialog.isShowing()) return;
        dialog.show();
    }

    public static class DaysAdapter extends ArrayAdapter<Integer> {
        static final int layout = R.layout.layout_single_choice_item;
        static final Integer[] data = new Integer[] { 6, 12, 18 };

        public DaysAdapter(@NonNull Context context) {
            super(context, layout, data);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Integer value = getItem(position);

            final View view;
            final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (convertView == null) {
                view = inflater.inflate(layout, parent, false);
            } else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(String.format(Locale.getDefault(), "最近%d天", value));
            return view;
        }
    }

    public interface OnDaySelectorDialogListener {
        void onDaySelected(RecentTopSection section, DialogInterface dialog, int days);
    }
}
