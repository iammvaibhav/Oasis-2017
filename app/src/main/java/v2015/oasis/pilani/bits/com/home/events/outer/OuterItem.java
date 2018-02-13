package v2015.oasis.pilani.bits.com.home.events.outer;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.ramotion.garlandview.header.HeaderDecorator;
import com.ramotion.garlandview.header.HeaderItem;
import com.ramotion.garlandview.inner.InnerLayoutManager;
import com.ramotion.garlandview.inner.InnerRecyclerView;

import java.util.ArrayList;

import v2015.oasis.pilani.bits.com.home.Pair;
import v2015.oasis.pilani.bits.com.home.R;
import v2015.oasis.pilani.bits.com.home.databinding.OuterItemBinding;
import v2015.oasis.pilani.bits.com.home.events.inner.InnerAdapter;
import v2015.oasis.pilani.bits.com.home.events.inner.InnerData;
import v2015.oasis.pilani.bits.com.home.events.onItemClickListener;

public class OuterItem extends HeaderItem {

    private final View mHeader;
    private final View mHeaderAlpha;

    private final InnerRecyclerView mRecyclerView;

    private final View mMiddle;
    private final View mFooter;

    private boolean mIsScrolling;
    private OuterItemBinding binding;

    private Typeface typeface = null;

    private final static float MIDDLE_RATIO_START = 0.7f;
    private final static float MIDDLE_RATIO_MAX = 0.1f;
    private final static float MIDDLE_RATIO_DIFF = MIDDLE_RATIO_START- MIDDLE_RATIO_MAX;

    private final static float FOOTER_RATIO_START = 1.1f;
    private final static float FOOTER_RATIO_MAX = 0.35f;
    private final static float FOOTER_RATIO_DIFF = FOOTER_RATIO_START - FOOTER_RATIO_MAX;

    private final int m10dp;
    private final int m90dp;
    private final int m15dp;

    public OuterItem(OuterItemBinding outerItemBinding, RecyclerView.RecycledViewPool pool, onItemClickListener listener) {
        super(outerItemBinding.getRoot());
        binding = outerItemBinding;

        m10dp = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.dp10);
        m90dp = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.dp90);
        m15dp = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.dp15);

        mHeader = itemView.findViewById(R.id.header);
        mHeaderAlpha = itemView.findViewById(R.id.header_alpha);
        mMiddle = itemView.findViewById(R.id.header_middle);
        mFooter = itemView.findViewById(R.id.header_footer);
        mRecyclerView = itemView.findViewById(R.id.recycler_view);
        mRecyclerView.setRecycledViewPool(pool);
        mRecyclerView.setAdapter(new InnerAdapter(listener));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                mIsScrolling = newState != RecyclerView.SCROLL_STATE_IDLE;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                onItemScrolled(recyclerView, dx, dy);
            }
        });

        mRecyclerView.addItemDecoration(new HeaderDecorator(
                itemView.getContext().getResources().getDimensionPixelSize(R.dimen.inner_item_height),
                itemView.getContext().getResources().getDimensionPixelSize(R.dimen.inner_item_offset)));

    }

    @Override
    public boolean isScrolling() {
        return mIsScrolling;
    }

    @Override
    public InnerRecyclerView getViewGroup() {
        return mRecyclerView;
    }

    @Override
    public View getHeader() {
        return mHeader;
    }

    @Override
    public View getHeaderAlphaView() {
        return mHeaderAlpha;
    }

    void setContent(Pair<OuterData, ArrayList<InnerData>> pageData, int type) {
        final Context context = itemView.getContext();

        if (typeface == null){
            typeface = Typeface.createFromAsset(context.getAssets(), "fonts/bold.otf");
        }

        mRecyclerView.setLayoutManager(new InnerLayoutManager());
        ((InnerAdapter)mRecyclerView.getAdapter()).addData(pageData.getSecond(), pageData.getFirst().getColor(), type);

        String heading = pageData.getFirst().getHeading().toUpperCase();
        binding.heading.setText(heading);
        binding.heading.setTypeface(typeface);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadii(new float[]{m15dp, m15dp, m15dp, m15dp, 0, 0, 0, 0});
        gradientDrawable.setColor(pageData.getFirst().getColor());
        binding.headerMiddle.setBackground(gradientDrawable);
    }

    void clearContent() {
        ((InnerAdapter)mRecyclerView.getAdapter()).clearData();
    }

    private float computeRatio(RecyclerView recyclerView) {
        final View child0 = recyclerView.getChildAt(0);
        final int pos = recyclerView.getChildAdapterPosition(child0);
        if (pos != 0) {
            return 0;
        }

        final int height = child0.getHeight();
        final float y = Math.max(0, child0.getY());
        return y / height;
    }

    private void onItemScrolled(RecyclerView recyclerView, int dx, int dy) {
        final float ratio = computeRatio(recyclerView);

        final float footerRatio = Math.max(0, Math.min(FOOTER_RATIO_START, ratio) - FOOTER_RATIO_DIFF) / FOOTER_RATIO_MAX;
        final float middleRatio = Math.max(0, Math.min(MIDDLE_RATIO_START, ratio) - MIDDLE_RATIO_DIFF) / MIDDLE_RATIO_MAX;

        ViewCompat.setPivotY(mFooter, 0);
        ViewCompat.setScaleY(mFooter, footerRatio);

        final ViewGroup.LayoutParams lp = mMiddle.getLayoutParams();
        lp.height = m90dp - (int)(m10dp * (1f - middleRatio));
        mMiddle.setLayoutParams(lp);
    }
}
