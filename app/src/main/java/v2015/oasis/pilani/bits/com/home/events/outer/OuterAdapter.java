package v2015.oasis.pilani.bits.com.home.events.outer;


import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.ramotion.garlandview.TailAdapter;

import java.util.ArrayList;

import v2015.oasis.pilani.bits.com.home.Pair;
import v2015.oasis.pilani.bits.com.home.R;
import v2015.oasis.pilani.bits.com.home.databinding.OuterItemBinding;
import v2015.oasis.pilani.bits.com.home.events.inner.InnerData;
import v2015.oasis.pilani.bits.com.home.events.onItemClickListener;


public class OuterAdapter extends TailAdapter<OuterItem> {

    private final int POOL_SIZE = 16;
    private final RecyclerView.RecycledViewPool mPool;
    private ArrayList<Pair<OuterData, ArrayList<InnerData>>> sortedData;
    private int type;
    private onItemClickListener mListener;

    // type can be 1 - date, 2 - category
    public OuterAdapter(ArrayList<Pair<OuterData, ArrayList<InnerData>>> data, int type, onItemClickListener listener) {
        sortedData = data;
        mPool = new RecyclerView.RecycledViewPool();
        mPool.setMaxRecycledViews(0, POOL_SIZE);
        this.type = type;
        mListener = listener;
    }

    @Override
    public OuterItem onCreateViewHolder(ViewGroup parent, int viewType) {
        final OuterItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), viewType, parent, false);
        return new OuterItem(binding, mPool, mListener);
    }

    @Override
    public void onBindViewHolder(OuterItem holder, int position) {
        holder.setContent(sortedData.get(position), type);
    }

    @Override
    public void onViewRecycled(OuterItem holder) {
        holder.clearContent();
    }

    @Override
    public int getItemCount() {
        return sortedData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.outer_item;
    }

    public void changeDatasetTo(ArrayList<Pair<OuterData, ArrayList<InnerData>>> data, int type){
        sortedData = data;
        this.type = type;
        notifyDataSetChanged();
    }

}
