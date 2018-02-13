package v2015.oasis.pilani.bits.com.home.selfiecontest;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.marcorei.infinitefire.InfiniteFireArray;
import com.marcorei.infinitefire.InfiniteFireRecyclerViewAdapter;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import v2015.oasis.pilani.bits.com.home.R;

public class RecentImagesView extends Fragment {

    InfiniteFireArray<Selfie> array;
    String userID = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.images_view, container, false);

        userID = getArguments().getString("userID");

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("SelfieContest").child("Selfies");

        final Query query = ref.orderByKey();
        array = new InfiniteFireArray<>(
                Selfie.class,
                query,
                9,
                9,
                false,
                true
        );

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                array.reset();
            }
        });
        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(array, userID, getActivity(), ((SelfieMainPageFragment)getParentFragment()).listener);
        final GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        layoutManager.setReverseLayout(false);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return adapter.getSpanSize(position);
            }
        });
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);
        recyclerView.setHasFixedSize(false);

        array.addOnLoadingStatusListener(new InfiniteFireArray.OnLoadingStatusListener() {
            @Override
            public void onChanged(EventType type) {
                switch (type) {
                    case LoadingContent:
                        adapter.setLoadingMore(true);
                        break;
                    case LoadingNoContent:
                        adapter.setLoadingMore(false);
                        break;
                    case Done:
                        swipeRefreshLayout.setRefreshing(false);
                        adapter.setLoadingMore(false);
                        break;
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy < 0) {
                    return;
                }
                if(layoutManager.findLastVisibleItemPosition() < array.getCount() - 20) {
                    return;
                }
                array.more();
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        array.cleanup();
        super.onDestroyView();
    }

    public static class RecyclerViewAdapter extends InfiniteFireRecyclerViewAdapter<Selfie> {

        public static final int VIEW_TYPE_CONTENT = 1;
        public static final int VIEW_TYPE_FOOTER = 2;
        String userID;
        Context context;
        selfiItemClickListener listener;

        /**
         * This is the view holder for the chat messages.
         */
        public static class LetterHolder extends RecyclerView.ViewHolder {
            public ImageView image;
            public ShineButton shineButton;
            public TextView noOfLikes;

            public LetterHolder(View itemView) {
                super(itemView);
                noOfLikes = (TextView) itemView.findViewById(R.id.noOfLikes);
                image = (ImageView) itemView.findViewById(R.id.image);
                shineButton = (ShineButton) itemView.findViewById(R.id.like);
            }
        }

        /**
         * This is the view holder for the simple header and footer of this example.
         */
        public static class LoadingHolder extends RecyclerView.ViewHolder {
            public ProgressBar progressBar;

            public LoadingHolder(View view) {
                super(view);
                progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
            }
        }

        private boolean loadingMore = false;

        /**
         * @param snapshots data source for this adapter.
         */
        public RecyclerViewAdapter(InfiniteFireArray snapshots, String userID, Context context, selfiItemClickListener listener) {
            super(snapshots, 0, 1);
            this.userID = userID;
            this.context = context;
            this.listener = listener;
        }


        /**
         * @return status of load-more loading procedures
         */
        public boolean isLoadingMore() {
            return loadingMore;
        }

        /**
         * This loading status has nothing to do with firebase real-time functionality.
         * It reflects the loading procedure of the first "fresh" data set after a change to the query.
         *
         * @param loadingMore adjust the status of additional loading procedures.
         */
        public void setLoadingMore(boolean loadingMore) {
            if(loadingMore == this.isLoadingMore()) return;
            this.loadingMore = loadingMore;
            notifyItemChanged(getItemCount() - 1);
        }

        @Override
        public int getItemViewType(int position) {
            if(position == getItemCount() - 1) {
                return VIEW_TYPE_FOOTER;
            }
            return VIEW_TYPE_CONTENT;
        }

        public int getSpanSize(int position) {
            if(position == getItemCount() - 1) {
                return 3;
            }
            return 1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            RecyclerView.ViewHolder viewHolder;
            View view;
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                case VIEW_TYPE_CONTENT:
                    view = inflater.inflate(R.layout.image_item, parent, false);
                    viewHolder = new LetterHolder(view);
                    break;
                case VIEW_TYPE_FOOTER:
                    view = inflater.inflate(R.layout.list_item_loading, parent, false);
                    viewHolder = new LoadingHolder(view);
                    break;
                default: throw new IllegalArgumentException("Unknown type");
            }
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            int viewType = getItemViewType(position);
            switch(viewType) {
                case VIEW_TYPE_CONTENT:
                    Selfie selfie = snapshots.getItem(position - indexOffset).getValue();
                    if(selfie == null) {
                        selfie = new Selfie("-", "-",
                                "http://cumbrianrun.co.uk/wp-content/uploads/2014/02/default-placeholder-300x300.png",
                                "http://cumbrianrun.co.uk/wp-content/uploads/2014/02/default-placeholder-300x300.png",
                                0L, new ArrayList<String>(), "");
                    }
                    LetterHolder contentHolder = (LetterHolder) holder;
                    String noOfLikes = String.valueOf(selfie.getNoOfLikes());
                    boolean liked = false;
                    try {
                        liked = selfie.getLikes().contains(userID);
                    }catch (NullPointerException e){
                        e.printStackTrace();
                    }
                    String image = selfie.getThumbnailUrl();
                    final String selfieID = String.valueOf(selfie.selfieID);

                    final Selfie selfieCopy = new Selfie(selfie.caption, selfie.description, selfie.thumbnailUrl, selfie.fullImageUrl, selfie.noOfLikes, selfie.likes, selfie.selfieID);

                    contentHolder.shineButton.setChecked(liked);
                    contentHolder.noOfLikes.setText(noOfLikes);
                    contentHolder.shineButton.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(View view, boolean checked) {
                            if (checked){
                                if (selfieCopy.likes == null){
                                    ArrayList<String> c = new ArrayList<>();
                                    c.add(userID);
                                    selfieCopy.likes = c;
                                    FirebaseDatabase.getInstance().getReference().child("SelfieContest").child("Selfies").child(selfieID).setValue(selfieCopy).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            FirebaseDatabase.getInstance().getReference().child("SelfieContest").child("Selfies").child(selfieID).child("noOfLikes").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    FirebaseDatabase.getInstance().getReference().child("SelfieContest").child("Selfies").child(selfieID).child("noOfLikes").setValue((((Long)dataSnapshot.getValue()) + 1L) );
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    });
                                }else {
                                    selfieCopy.likes.add(userID);
                                    FirebaseDatabase.getInstance().getReference().child("SelfieContest").child("Selfies").child(selfieID).setValue(selfieCopy).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            FirebaseDatabase.getInstance().getReference().child("SelfieContest").child("Selfies").child(selfieID).child("noOfLikes").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    FirebaseDatabase.getInstance().getReference().child("SelfieContest").child("Selfies").child(selfieID).child("noOfLikes").setValue((((Long)dataSnapshot.getValue()) + 1L) );
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    });
                                }

                            }else {
                                int index = selfieCopy.likes.indexOf(userID);
                                if (index != -1){
                                    selfieCopy.likes.remove(index);
                                    FirebaseDatabase.getInstance().getReference().child("SelfieContest").child("Selfies").child(selfieID).setValue(selfieCopy).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            FirebaseDatabase.getInstance().getReference().child("SelfieContest").child("Selfies").child(selfieID).child("noOfLikes").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    FirebaseDatabase.getInstance().getReference().child("SelfieContest").child("Selfies").child(selfieID).child("noOfLikes").setValue((((Long)dataSnapshot.getValue()) - 1L) );
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    });

                    Picasso.with(context).load(image).fit().centerCrop().into(contentHolder.image);
                    contentHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onItemClick(selfieCopy, 0);
                        }
                    });

                    break;
                case VIEW_TYPE_FOOTER:
                    LoadingHolder footerHolder = (LoadingHolder) holder;
                    footerHolder.progressBar.setVisibility((isLoadingMore()) ? View.VISIBLE : View.GONE);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown type");
            }
        }
    }
}