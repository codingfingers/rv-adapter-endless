package com.rockerhieu.rvadapter.endless;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rockerhieu.rvadapter.RecyclerViewAdapterWrapper;

import java.util.concurrent.atomic.AtomicBoolean;

import static android.support.v7.widget.RecyclerView.Adapter;
import static android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * @author rockerhieu on 7/6/15.
 */
public class EndlessRecyclerViewAdapter extends RecyclerViewAdapterWrapper {
    public static final int TYPE_PENDING = 999;
    private final Context context;
    private final int pendingViewResId;
    private AtomicBoolean keepOnAppending;
    private AtomicBoolean dataPending;
    private RequestToLoadMoreListener requestToLoadMoreListener;
    private boolean displayIndicatorOnFirstLoading;
    private boolean displayIndicator;

    public EndlessRecyclerViewAdapter(Context context, Adapter wrapped, RequestToLoadMoreListener requestToLoadMoreListener, @LayoutRes int pendingViewResId, boolean keepOnAppending,
                                      boolean displayIndicator, boolean displayIndicatorOnFirstLoading) {
        super(wrapped);
        this.context = context;
        this.requestToLoadMoreListener = requestToLoadMoreListener;
        this.pendingViewResId = pendingViewResId;
        this.keepOnAppending = new AtomicBoolean(keepOnAppending);
        this.displayIndicatorOnFirstLoading = displayIndicatorOnFirstLoading;
        this.displayIndicator = displayIndicator;
        dataPending = new AtomicBoolean(false);
    }

    public EndlessRecyclerViewAdapter(Context context, Adapter wrapped, RequestToLoadMoreListener requestToLoadMoreListener, boolean displayIndicator, boolean displayIndicatorOnFirstLoading) {
        this(context, wrapped, requestToLoadMoreListener, R.layout.item_loading, true, displayIndicator, displayIndicatorOnFirstLoading);
    }

    public EndlessRecyclerViewAdapter(Context context, Adapter wrapped, RequestToLoadMoreListener requestToLoadMoreListener) {
        this(context, wrapped, requestToLoadMoreListener, R.layout.item_loading, true, false, false);
    }

    private void stopAppending() {
        setKeepOnAppending(false);
    }

    /**
     * Let the adapter know that data is load and ready to view.
     *
     * @param keepOnAppending whether the adapter should request to load more when scrolling to the bottom.
     */
    public void onDataReady(boolean keepOnAppending) {
        dataPending.set(false);
        setKeepOnAppending(keepOnAppending);
    }

    private void setKeepOnAppending(boolean newValue) {
        keepOnAppending.set(newValue);
        getWrappedAdapter().notifyDataSetChanged();
    }

    /**
     *
     */
    public void restartAppending() {
        dataPending.set(false);
        setKeepOnAppending(true);
    }

    private View getPendingView(ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(pendingViewResId, viewGroup, false);
    }

    @Override
    public int getItemCount() {
        int count = super.getItemCount();
        if (displayIndicator) {
            if (displayIndicatorOnFirstLoading) {
                return count + (keepOnAppending.get() ? 1 : 0);
            } else {
                if (count == 0) {
                    return count;
                } else {
                    return count + (keepOnAppending.get() ? 1 : 0);
                }
            }
        } else {
            return count;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getWrappedAdapter().getItemCount() && position != 0) {
            return TYPE_PENDING;
        }
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_PENDING) {
            return new PendingViewHolder(getPendingView(parent));
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_PENDING) {
            if (!dataPending.get()) {
                dataPending.set(true);
                requestToLoadMoreListener.onLoadMoreRequested();
            }
        } else {
            super.onBindViewHolder(holder, position);
        }
    }

    public interface RequestToLoadMoreListener {
        /**
         * The adapter requests to load more data.
         */
        void onLoadMoreRequested();
    }

    static class PendingViewHolder extends ViewHolder {

        public PendingViewHolder(View itemView) {
            super(itemView);
        }
    }
}
