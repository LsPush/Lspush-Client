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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.decay.country.Country;
import com.decay.recyclerview.callback.HeaderProvider;
import com.tomeokin.lspush.R;

import java.util.ArrayList;
import java.util.List;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.CountryHolder>
    implements HeaderProvider, View.OnClickListener {

    private List<Country> countryList = new ArrayList<>();
    private OnItemClickListener listener;

    public CountryAdapter(List<Country> countries, @NonNull OnItemClickListener listener) {
        setCountryList(countries);
        this.listener = listener;
    }

    public void setCountryList(List<Country> countries) {
        if (countries == null) {
            countryList = new ArrayList<>();
        } else {
            countryList = countries;
        }
        notifyDataSetChanged();
    }

    @Override
    public CountryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_country, parent, false);
        view.setOnClickListener(this);
        return new CountryHolder(view);
    }

    @Override
    public void onBindViewHolder(CountryHolder holder, int position) {
        holder.itemView.setTag(R.id.recycler_item_position, position);
        final Country country = countryList.get(position);
        holder.country.setText(country.name);
        holder.countryCode.setText(String.format("+%s", country.countryCode));
    }

    @Override
    public int getItemCount() {
        return countryList.size();
    }

    @Nullable
    @Override
    public String getHeaderText(int position) {
        return countryList.get(position).group;
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag(R.id.recycler_item_position);
        Country country = countryList.get(position);
        listener.onItemClick(v, this, country, position);
    }

    public static class CountryHolder extends RecyclerView.ViewHolder {
        TextView country;
        TextView countryCode;

        public CountryHolder(View itemView) {
            super(itemView);
            country = (TextView) itemView.findViewById(R.id.country);
            countryCode = (TextView) itemView.findViewById(R.id.countryCode);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, CountryAdapter adapter, Country country, int position);
    }
}
