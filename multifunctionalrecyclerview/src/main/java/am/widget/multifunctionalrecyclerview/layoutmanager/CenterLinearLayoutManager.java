/*
 * Copyright (C) 2017 AlexMofer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package am.widget.multifunctionalrecyclerview.layoutmanager;

import android.content.Context;
import android.support.v7.widget.PublicLinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * 剧中线性布局
 * Created by Alex on 2017/11/3.
 */
@SuppressWarnings("all")
public class CenterLinearLayoutManager extends PublicLinearLayoutManager {

    private boolean mCenter = false;

    public CenterLinearLayoutManager(Context context) {
        super(context);
        setSizeUnlimited(true);
    }

    public CenterLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        setSizeUnlimited(true);
    }

    public CenterLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setSizeUnlimited(true);
    }

    @Override
    public void layoutDecorated(View child, int left, int top, int right, int bottom) {
        if (!mCenter) {
            super.layoutDecorated(child, left, top, right, bottom);
            return;
        }
        final int leftDecorationWidth = getLeftDecorationWidth(child);
        final int topDecorationHeight = getTopDecorationHeight(child);
        final int rightDecorationWidth = getRightDecorationWidth(child);
        final int bottomDecorationHeight = getBottomDecorationHeight(child);
        final ViewGroup parent = (ViewGroup) child.getParent();
        final int offset;
        if (getOrientation() == HORIZONTAL) {
            final int contentHeight = parent.getMeasuredHeight() -
                    parent.getPaddingTop() - parent.getPaddingBottom();
            offset = (contentHeight - (bottom - top)) / 2;
            child.layout(left + leftDecorationWidth, top + topDecorationHeight + offset,
                    right - rightDecorationWidth, bottom - bottomDecorationHeight + offset);
        } else {
            final int contentWidth = parent.getMeasuredWidth() -
                    parent.getPaddingLeft() - parent.getPaddingRight();
            offset = (contentWidth - (right - left)) / 2;
            child.layout(left + leftDecorationWidth + offset, top + topDecorationHeight,
                    right - rightDecorationWidth + offset, bottom - bottomDecorationHeight);
        }
    }

    @Override
    public void layoutDecoratedWithMargins(View child, int left, int top, int right, int bottom) {
        if (!mCenter) {
            super.layoutDecoratedWithMargins(child, left, top, right, bottom);
            return;
        }
        final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
        final int leftDecorationWidth = getLeftDecorationWidth(child);
        final int topDecorationHeight = getTopDecorationHeight(child);
        final int rightDecorationWidth = getRightDecorationWidth(child);
        final int bottomDecorationHeight = getBottomDecorationHeight(child);
        final ViewGroup parent = (ViewGroup) child.getParent();
        final int offset;
        if (getOrientation() == HORIZONTAL) {
            final int contentHeight = parent.getMeasuredHeight() -
                    parent.getPaddingTop() - parent.getPaddingBottom();
            offset = (contentHeight - (bottom - top)) / 2;
            child.layout(left + leftDecorationWidth + lp.leftMargin,
                    top + topDecorationHeight + lp.topMargin + offset,
                    right - rightDecorationWidth - lp.rightMargin,
                    bottom - bottomDecorationHeight - lp.bottomMargin + offset);
        } else {
            final int contentWidth = parent.getMeasuredWidth() -
                    parent.getPaddingLeft() - parent.getPaddingRight();
            offset = (contentWidth - (right - left)) / 2;
            child.layout(left + leftDecorationWidth + offset + lp.leftMargin,
                    top + topDecorationHeight + lp.topMargin,
                    right - rightDecorationWidth - lp.rightMargin + offset,
                    bottom - bottomDecorationHeight - lp.bottomMargin);
        }
    }

    /**
     * 判断是否剧中布局
     *
     * @return 是否剧中布局
     */
    public boolean isLayoutInCenter() {
        return mCenter;
    }

    /**
     * 设置布局剧中
     *
     * @param center 剧中
     */
    public void setLayoutInCenter(boolean center) {
        if (mCenter == center)
            return;
        mCenter = center;
        requestLayout();
    }
}