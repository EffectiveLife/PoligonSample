package com.effectivelife.polygonsample;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by com on 2015-07-14.
 */
public class AreaAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ANIMATED_ITEMS_COUNT = 2;

    private Context context;
    private int lastAnimatedPosition = -1;
    private int itemCount = 0;

    private List<AreaBean> areas = new ArrayList<AreaBean>();

    public AreaAdapter(Context context, List<AreaBean> areas) {
        this.context = context;
        this.areas.addAll(areas);
    }

    public void updateItem(List<AreaBean> items) {
        if(!this.areas.isEmpty()) {
            this.areas.clear();
            this.areas.addAll(items);
            this.notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(context).inflate(R.layout.item_area, parent, false);
        return new AreaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        AreaViewHolder vHolder = (AreaViewHolder) holder;

        vHolder.setItem(areas.get(position));
        vHolder.tvAreaName.setText(areas.get(position).getName());
        /*if(position % 2 == 0) {
            vHolder.tvAreaName.setBackgroundColor(Color.parseColor("#FFE4E1"));
        } else {
            vHolder.tvAreaName.setBackgroundColor(Color.parseColor("#E6E6FA"));
        }*/

    }

    @Override
    public int getItemCount() {
        return areas.size();
    }

//    private void runEnterAnimation(View view, int position)

    public static class AreaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @InjectView(R.id.tvAreaName)
        TextView tvAreaName;

        AreaBean item;

        public AreaViewHolder(View view) {
            super(view);
            ButterKnife.inject(this, view);
            view.setOnClickListener(this);
        }

        public void setItem(AreaBean item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            if(!TextUtils.isEmpty(item.getPoint())) {
                Intent intent = new Intent(v.getContext(), MapActivity.class);
                intent.putExtra(CommercialAreaDBAdapter.AREA_POINT, item.getPoint());
                v.getContext().startActivity(intent);
            }
        }

    }

}
