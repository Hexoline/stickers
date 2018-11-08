/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.example.samplestickerapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StickerPackListAdapter extends RecyclerView.Adapter<StickerPackListItemViewHolder> {
    @NonNull
    private List<StickerPack> stickerPacks;
    @NonNull
    private final OnAddButtonClickedListener onAddButtonClickedListener;
    private int maxNumberOfStickersInARow;
    private ArrayList<Integer> buttonPositions=new ArrayList<>();

    private static final int BUTTON_INTERVAL=10;

    StickerPackListAdapter(@NonNull List<StickerPack> stickerPacks, @NonNull OnAddButtonClickedListener onAddButtonClickedListener) {
        this.stickerPacks = stickerPacks;
        this.onAddButtonClickedListener = onAddButtonClickedListener;

        for(int i=BUTTON_INTERVAL;i<stickerPacks.size();i+=BUTTON_INTERVAL){
            buttonPositions.add(i+buttonPositions.size());
        }
        if(buttonPositions.isEmpty())
            buttonPositions.add(stickerPacks.size());

    }

    @NonNull
    @Override
    public StickerPackListItemViewHolder onCreateViewHolder(@NonNull final ViewGroup viewGroup, final int type) {
    	if(type==2){
            WebView webView=new WebView(viewGroup.getContext());
            webView.loadUrl("https://telegram.space/stickers_info/android?lang="+Locale.getDefault().toString());
            webView.setBackgroundColor(0);
            webView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, viewGroup.getResources().getDimensionPixelSize(R.dimen.web_view_height)));
            webView.setAlpha(0f);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient(){
				@Override
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
					webView.setVisibility(View.INVISIBLE);
				}

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url){
					view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
					return true;
				}
			});
            webView.setWebChromeClient(new WebChromeClient(){
            	boolean didShow=false;
				@Override
				public void onProgressChanged(WebView view, int newProgress){
					if(newProgress==100 && !didShow){
						didShow=true;
						view.animate().alpha(1).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
					}
				}
			});
            return new StickerPackListItemViewHolder(webView);
        }
        final Context context = viewGroup.getContext();
        final LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View stickerPackRow = layoutInflater.inflate(R.layout.sticker_packs_list_item, viewGroup, false);
        return new StickerPackListItemViewHolder(stickerPackRow);
    }

    @Override
    public int getItemViewType(int position){
        if(buttonPositions.contains(position))
            return 2;
        return 1;
    }

    @Override
    public void onBindViewHolder(@NonNull final StickerPackListItemViewHolder viewHolder, int index) {
        if(buttonPositions.contains(index)){
            return;
        }
        int diff=0;
        for(int pos:buttonPositions){
            if(index>pos)
                diff++;
        }
        index-=diff;
        StickerPack pack = stickerPacks.get(index);
        final Context context = viewHolder.publisherView.getContext();
        viewHolder.publisherView.setText(pack.publisher);
        viewHolder.filesizeView.setText(Formatter.formatShortFileSize(context, pack.getTotalSize()));

        viewHolder.titleView.setText(pack.name);
        viewHolder.container.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), StickerPackDetailsActivity.class);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, true);
            intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, pack);
            view.getContext().startActivity(intent);
        });
        viewHolder.imageRowView.removeAllViews();
        //if this sticker pack contains less stickers than the max, then take the smaller size.
		// Grishka: apparently, WhatsApp developers had no idea that layout in Android isn't instant and so the view dimensions aren't available right after you've created the views
        viewHolder.itemView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){
            @Override
            public boolean onPreDraw(){
                viewHolder.itemView.getViewTreeObserver().removeOnPreDrawListener(this);
                int actualNumberOfStickersToShow = Math.min(maxNumberOfStickersInARow, pack.getStickers().size());
                for (int i = 0; i < actualNumberOfStickersToShow; i++) {
                    final SimpleDraweeView rowImage = (SimpleDraweeView) LayoutInflater.from(context).inflate(R.layout.sticker_pack_list_item_image, viewHolder.imageRowView, false);
                    rowImage.setImageURI(StickerPackLoader.getStickerAssetUri(pack.identifier, pack.getStickers().get(i).imageFileName));
                    final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rowImage.getLayoutParams();
                    final int marginBetweenImages = (viewHolder.imageRowView.getMeasuredWidth() - maxNumberOfStickersInARow * viewHolder.imageRowView.getContext().getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size)) / (maxNumberOfStickersInARow - 1) - lp.leftMargin - lp.rightMargin;
                    if (i != actualNumberOfStickersToShow - 1 && marginBetweenImages > 0) { //do not set the margin for the last image
                        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin + marginBetweenImages, lp.bottomMargin);
                        rowImage.setLayoutParams(lp);
                    }
                    viewHolder.imageRowView.addView(rowImage);
                }
                return true;
            }
        });
        setAddButtonAppearance(viewHolder.addButton, pack);
    }

    private void setAddButtonAppearance(ImageView addButton, StickerPack pack) {
        if (pack.getIsWhitelisted()) {
            addButton.setImageResource(R.drawable.sticker_3rdparty_added);
            addButton.setClickable(false);
            addButton.setOnClickListener(null);
            setBackground(addButton, null);
        } else {
            addButton.setImageResource(R.drawable.sticker_3rdparty_add);
            addButton.setOnClickListener(v -> onAddButtonClickedListener.onAddButtonClicked(pack));
            TypedValue outValue = new TypedValue();
            addButton.getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            addButton.setBackgroundResource(outValue.resourceId);
        }
    }

    private void setBackground(View view, Drawable background) {
        if (Build.VERSION.SDK_INT >= 16) {
            view.setBackground(background);
        } else {
            view.setBackgroundDrawable(background);
        }
    }
    @Override
    public int getItemCount() {
        return stickerPacks.size()+buttonPositions.size();
    }

    void setMaxNumberOfStickersInARow(int maxNumberOfStickersInARow) {
        if (this.maxNumberOfStickersInARow != maxNumberOfStickersInARow) {
            this.maxNumberOfStickersInARow = maxNumberOfStickersInARow;
            notifyDataSetChanged();
        }
    }

    public void setStickerPackList(List<StickerPack> stickerPackList) {
        this.stickerPacks = stickerPackList;
    }

    public interface OnAddButtonClickedListener {
        void onAddButtonClicked(StickerPack stickerPack);
    }
}
