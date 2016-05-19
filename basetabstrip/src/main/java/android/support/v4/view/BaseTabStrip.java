/*
 * Copyright (C) 2014 The Android Open Source Project
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

package android.support.v4.view;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * BasePagerTabStrip ViewPager������Ӧ�仯Ч��
 *
 * @author Alex
 */
public abstract class BaseTabStrip extends View implements ViewPager.Decor {

    private ViewPager mPager;
    private final PageListener mPageListener = new PageListener();
    private WeakReference<PagerAdapter> mWatchingAdapter;
    private int mLastKnownPosition = 0;
    private float mLastKnownPositionOffset = -1;
    private int mCurrentPager = 0;
    private int mPosition = 0;
    private boolean clickSelectedItem = false;
    private Drawable mTabItemBackground;
    private ArrayList<Drawable> mTabItemBackgrounds = new ArrayList<>();
    private boolean tabClickable;
    private boolean clickSmoothScroll;
    private GestureDetectorCompat mTabGestureDetector;
    private TabOnGestureListener mTabOnGestureListener = new TabOnGestureListener();
    private OnItemClickListener clickListener;
    private ArrayList<OnChangeListener> changeListeners;

    public BaseTabStrip(Context context) {
        this(context, null);
    }

    public BaseTabStrip(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseTabStrip(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setItemClickable(false);
        setClickSmoothScroll(false);
        mTabGestureDetector = new GestureDetectorCompat(context, mTabOnGestureListener);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final ViewParent parent = getParent();
        if (mPager == null && parent != null && parent instanceof ViewPager) {
            bindViewPager((ViewPager) parent);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        bindViewPager(null);
        clearItemBackground();
    }

    /**
     * ����ViewPager
     *
     * @param pager ������ViewPager
     */
    public void bindViewPager(ViewPager pager) {
        PagerAdapter oldAdapter = null;
        PagerAdapter newAdapter = null;
        if (mPager != null) {
            mPager.setInternalPageChangeListener(null);
            mPager.setOnAdapterChangeListener(null);
            oldAdapter = mPager.getAdapter();
        }
        mPager = pager;
        if (mPager != null) {
            mPager.setInternalPageChangeListener(mPageListener);
            mPager.setOnAdapterChangeListener(mPageListener);
            newAdapter = mPager.getAdapter();
        }
        bindPagerAdapter(oldAdapter, newAdapter);
    }

    private void clearItemBackground() {
        for (Drawable drawable : mTabItemBackgrounds) {
            drawable.setCallback(null);
        }
        mTabItemBackgrounds.clear();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!tabClickable) {
            return super.onTouchEvent(ev);
        }
        final boolean tab = mTabGestureDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev) || tab;
    }

    @Override
    @Deprecated
    public boolean performClick() {
        return super.performClick();
    }

    /**
     * ���
     *
     * @param position λ��
     */
    @SuppressWarnings("unused")
    public boolean performClick(int position) {
        return performClick(position, false, true);
    }

    /**
     * ���
     *
     * @param position     λ��
     * @param smoothScroll �Ƿ�ƽ������
     */
    public boolean performClick(int position, boolean smoothScroll, boolean notifyListener) {
        if (getViewPager() != null && position >= 0 && position < getItemCount()) {
            clickSelectedItem = position == mPosition;
            mPosition = position;
            if (!clickSelectedItem) {
                if (!smoothScroll) {
                    mCurrentPager = position;
                    mLastKnownPosition = mCurrentPager;
                    mLastKnownPositionOffset = 0;
                    jumpTo(mCurrentPager);
                    notifyJumpTo(mCurrentPager);
                }
                getViewPager().setCurrentItem(position, smoothScroll);
            }
            if (clickListener != null && notifyListener) {
                clickListener.onItemClick(mPosition);
            }
            return true;
        }
        return false;
    }

    /**
     * ����PagerAdapter
     *
     * @param oldAdapter ��Adapter
     * @param newAdapter ��Adapter
     */
    protected void bindPagerAdapter(PagerAdapter oldAdapter, PagerAdapter newAdapter) {
        if (oldAdapter != null) {
            oldAdapter.unregisterDataSetObserver(mPageListener);
            mWatchingAdapter = null;
        }
        if (newAdapter != null) {
            newAdapter.registerDataSetObserver(mPageListener);
            mWatchingAdapter = new WeakReference<>(newAdapter);
        }
        createItemBackgrounds();
        onBindPagerAdapter();
        checkCurrentItem();
        requestLayout();
        invalidate();
    }

    /**
     * ���������
     */
    protected void createItemBackgrounds() {
        if (mTabItemBackground == null)
            return;
        int count = getItemCount();
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                if (i < mTabItemBackgrounds.size()) {
                    mTabItemBackgrounds.get(i).setState(onCreateDrawableState(0));
                } else {
                    Drawable tag = mTabItemBackground.getConstantState().newDrawable();
                    tag.setCallback(this);
                    mTabItemBackgrounds.add(tag);
                }
            }
        } else {
            for (Drawable drawable : mTabItemBackgrounds) {
                drawable.setState(onCreateDrawableState(0));
            }
        }
    }

    /**
     * ���´��������
     */
    protected void recreateItemBackgrounds() {
        clearItemBackground();
        if (mTabItemBackground == null)
            return;
        int count = getItemCount();
        for (int i = 0; i < count; i++) {
            Drawable tag = mTabItemBackground.getConstantState().newDrawable();
            tag.setCallback(this);
            mTabItemBackgrounds.add(tag);
        }
    }

    /**
     * ��ȡ��ǰѡ�е�����
     *
     * @return ��ǰѡ�е�����
     */
    public int getCurrentItem() {
        return mPager == null ? -1 : mPager.getCurrentItem();
    }

    /**
     * ���ѡ������
     */
    public void checkCurrentItem() {
        final int position = getCurrentItem();
        mPosition = position;
        if (position >= 0 && position != mCurrentPager) {
            mCurrentPager = position;
            mLastKnownPosition = mCurrentPager;
            mLastKnownPositionOffset = 0;
            jumpTo(mCurrentPager);
            notifyJumpTo(mCurrentPager);
        }
    }

    /**
     * ����PagerAdapter
     */
    protected void onBindPagerAdapter() {

    }

    /**
     * �ɴ�����תΪPosition
     *
     * @param x X����
     * @param y Y����
     * @return �����Ӧλ��
     */
    protected int pointToPosition(float x, float y) {
        return 0;
    }

    @Override
    protected void drawableStateChanged() {
        final float downMotionX = mTabOnGestureListener.getDownMotionX();
        final float downMotionY = mTabOnGestureListener.getDownMotionY();
        int position = pointToPosition(downMotionX, downMotionY);
        if (position >= 0 && position < mTabItemBackgrounds.size()) {
            Drawable tag = mTabItemBackgrounds.get(position);
            DrawableCompat.setHotspot(tag, getHotspotX(tag, position, downMotionX, downMotionY),
                    getHotspotY(tag, position, downMotionX, downMotionY));
            if (tag.isStateful()) {
                tag.setState(getDrawableState());
            }
        }
        super.drawableStateChanged();
    }

    /**
     * set hotspot's x location
     *
     * @param background ����ͼ
     * @param position   ͼƬPosition
     * @param motionX    ���λ��X
     * @param motionY    ���λ��Y
     * @return x location
     */
    protected float getHotspotX(Drawable background, int position, float motionX, float motionY) {
        return background.getIntrinsicWidth() * 0.5f;
    }

    /**
     * set hotspot's y location
     *
     * @param background ����ͼ
     * @param position   ͼƬPosition
     * @param motionX    ���λ��X
     * @param motionY    ���λ��Y
     * @return y location
     */
    protected float getHotspotY(Drawable background, int position, float motionX, float motionY) {
        return background.getIntrinsicHeight() * 0.5f;
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        boolean isTag = false;
        for (Drawable tag : mTabItemBackgrounds) {
            if (who == tag) {
                isTag = true;
                break;
            }
        }
        return isTag || super.verifyDrawable(who);
    }

    /**
     * ��ȡTab�����
     *
     * @param position λ��
     * @return ����
     */
    protected Drawable getItemBackground(int position) {
        return position < mTabItemBackgrounds.size() ? mTabItemBackgrounds.get(position) : null;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        BaseTabStripSavedState ss = new BaseTabStripSavedState(superState);
        ss.currentPager = mCurrentPager;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        BaseTabStripSavedState ss = (BaseTabStripSavedState) state;
        performClick(ss.currentPager, false, false);
        super.onRestoreInstanceState(ss.getSuperState());
    }

    static class BaseTabStripSavedState extends BaseSavedState {
        int currentPager;

        BaseTabStripSavedState(Parcelable superState) {
            super(superState);
        }

        private BaseTabStripSavedState(Parcel in) {
            super(in);
            currentPager = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentPager);
        }

        public static final Creator<BaseTabStripSavedState> CREATOR =
                new Creator<BaseTabStripSavedState>() {
                    public BaseTabStripSavedState createFromParcel(Parcel in) {
                        return new BaseTabStripSavedState(in);
                    }

                    public BaseTabStripSavedState[] newArray(int size) {
                        return new BaseTabStripSavedState[size];
                    }
                };
    }

    private class PageListener extends DataSetObserver implements
            ViewPager.OnPageChangeListener, ViewPager.OnAdapterChangeListener {
        private int mScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels) {
            updateView(position, positionOffset, false);
        }

        @Override
        public void onPageSelected(int position) {
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                float offset = mLastKnownPositionOffset >= 0 ? mLastKnownPositionOffset : 0;
                updateView(position, offset, false);
            }
            mPosition = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;
        }

        @Override
        public void onAdapterChanged(PagerAdapter oldAdapter,
                                     PagerAdapter newAdapter) {
            bindPagerAdapter(oldAdapter, newAdapter);
        }

        @Override
        public void onChanged() {
            final float offset = mLastKnownPositionOffset >= 0 ? mLastKnownPositionOffset : 0;
            final int position = getCurrentItem();
            mPosition = position;
            updateView(position, offset, true);
        }
    }

    /**
     * ����View
     *
     * @param position       λ��
     * @param positionOffset λ��ƫ��
     * @param force          �Ƿ�ǿ�Ƹ���
     */
    private void updateView(int position, float positionOffset, boolean force) {
        if (mLastKnownPositionOffset == -1) {
            mLastKnownPositionOffset = positionOffset;
        }
        if (!force && positionOffset == mLastKnownPositionOffset) {
            return;
        }
        float mPositionOffset = positionOffset;
        if (mLastKnownPositionOffset == 0 || mLastKnownPositionOffset == 1)
            if (mPositionOffset > 0.5f)
                mLastKnownPositionOffset = 1;
            else
                mLastKnownPositionOffset = 0;
        int nextPager;
        if (position > mLastKnownPosition) {
            mLastKnownPosition = position - 1;
            if (mLastKnownPositionOffset > mPositionOffset) {
                if (mPositionOffset == 0) {
                    mPositionOffset = 1;
                } else {
                    mLastKnownPosition = position;
                }
                mCurrentPager = mLastKnownPosition;
                nextPager = mLastKnownPosition + 1;
                gotoRight(mCurrentPager, nextPager, mPositionOffset);
                notifyGotoRight(mCurrentPager, nextPager, mPositionOffset);
            } else {
                mCurrentPager = mLastKnownPosition + 1;
                nextPager = mLastKnownPosition;
                gotoLeft(mCurrentPager, nextPager, mPositionOffset);
                notifyGotoLeft(mCurrentPager, nextPager, mPositionOffset);
            }
        } else {
            mLastKnownPosition = position;
            if (mLastKnownPositionOffset > mPositionOffset) {
                mCurrentPager = mLastKnownPosition + 1;
                nextPager = mLastKnownPosition;
                gotoLeft(mCurrentPager, nextPager, mPositionOffset);
                notifyGotoLeft(mCurrentPager, nextPager, mPositionOffset);
            } else {
                mPositionOffset = mPositionOffset == 0 ? 1 : mPositionOffset;
                mCurrentPager = mLastKnownPosition;
                nextPager = mLastKnownPosition + 1;
                gotoRight(mCurrentPager, nextPager, mPositionOffset);
                notifyGotoRight(mCurrentPager, nextPager, mPositionOffset);
            }
        }
        mLastKnownPosition = position;
        mLastKnownPositionOffset = positionOffset;
    }

    /**
     * ֪ͨ��ת��
     *
     * @param current λ��
     */
    private void notifyJumpTo(int current) {
        if (changeListeners == null)
            return;
        for (OnChangeListener listener : changeListeners) {
            listener.jumpTo(current);
        }
    }

    /**
     * ֪ͨ�������
     *
     * @param current ��ǰҳ
     * @param next    Ŀ��ҳ
     * @param offset  ƫ��
     */
    private void notifyGotoLeft(int current, int next, float offset) {
        if (changeListeners == null)
            return;
        for (OnChangeListener listener : changeListeners) {
            listener.gotoLeft(current, next, offset);
        }
    }

    /**
     * ֪ͨ�����ұ�
     *
     * @param current ��ǰҳ
     * @param next    Ŀ��ҳ
     * @param offset  ƫ��
     */
    private void notifyGotoRight(int current, int next, float offset) {
        if (changeListeners == null)
            return;
        for (OnChangeListener listener : changeListeners) {
            listener.gotoRight(current, next, offset);
        }
    }

    /**
     * ֱ����ת��
     *
     * @param current λ��
     */
    protected abstract void jumpTo(int current);

    /**
     * �������
     *
     * @param current ��ǰҳ
     * @param next    Ŀ��ҳ
     * @param offset  ƫ��
     */
    protected abstract void gotoLeft(int current, int next, float offset);

    /**
     * �����ұ�
     *
     * @param current ��ǰҳ
     * @param next    Ŀ��ҳ
     * @param offset  ƫ��
     */
    protected abstract void gotoRight(int current, int next, float offset);

    private class TabOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        private float mDownMotionX = -1;
        private float mDownMotionY = -1;

        @Override
        public boolean onDown(MotionEvent e) {
            mDownMotionX = e.getX();
            mDownMotionY = e.getY();
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return performClick(pointToPosition(e.getX(), e.getY()), clickSmoothScroll, true);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            int position = pointToPosition(e.getX(), e.getY());
            if (position < 0)
                return false;
            if (clickSelectedItem && clickListener != null) {
                clickListener.onSelectedClick(mPosition);
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            int position = pointToPosition(e.getX(), e.getY());
            if (position < 0)
                return false;
            if (clickListener != null) {
                clickListener.onDoubleClick(mPosition);
            }
            return true;
        }

        public float getDownMotionX() {
            return mDownMotionX;
        }

        public float getDownMotionY() {
            return mDownMotionY;
        }
    }

    /**
     * ��ȡViewPager
     *
     * @return ViewPager
     */
    public final ViewPager getViewPager() {
        return mPager;
    }

    /**
     * ��ȡ��������
     *
     * @return ��������
     */
    public final int getItemCount() {
        try {
            return mWatchingAdapter.get().getCount();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * ��ȡ��������
     *
     * @param position ��������
     * @return ��������
     */
    @SuppressWarnings("unused")
    public final CharSequence getItemText(int position) {
        try {
            return mWatchingAdapter.get().getPageTitle(position);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ����Tab�����
     *
     * @param background ����
     */
    public void setItemBackground(Drawable background) {
        if (mTabItemBackground != background) {
            mTabItemBackground = background;
            recreateItemBackgrounds();
            requestLayout();
            invalidate();
        }
    }

    /**
     * ����Tab�����
     *
     * @param background ����
     */
    @SuppressWarnings("unused")
    public void setItemBackground(int background) {
        setItemBackground(ContextCompat.getDrawable(getContext(), background));
    }

    /**
     * ����Tab�Ƿ���Ե��
     *
     * @param clickable �Ƿ���Ե��
     */
    public void setItemClickable(boolean clickable) {
        tabClickable = clickable;
        if (tabClickable) {
            setClickable(true);
        }
    }

    /**
     * Tab�Ƿ���Ե��
     *
     * @return Tab�Ƿ���Ե��
     */
    @SuppressWarnings("unused")
    public boolean isTabClickable() {
        return tabClickable;
    }

    /**
     * �Ƿ���ʱƽ������
     *
     * @return ���ʱ�Ƿ�ƽ������
     */
    @SuppressWarnings("unused")
    public boolean isClickSmoothScroll() {
        return clickSmoothScroll;
    }

    /**
     * ���õ��ʱ�Ƿ�ƽ������
     *
     * @param smooth ���ʱ�Ƿ�ƽ������
     */
    public void setClickSmoothScroll(boolean smooth) {
        clickSmoothScroll = smooth;
    }

    /**
     * ���õ������
     *
     * @param listener ������
     */
    @SuppressWarnings("unused")
    public void setOnItemClickListener(OnItemClickListener listener) {
        clickListener = listener;
    }

    /**
     * �������
     */
    public interface OnItemClickListener {
        /**
         * �������
         *
         * @param position λ��
         */
        void onItemClick(int position);

        /**
         * �����ѡ������
         *
         * @param position λ��
         */
        void onSelectedClick(int position);

        /**
         * ˫������
         *
         * @param position λ��
         */
        void onDoubleClick(int position);
    }

    /**
     * ��ӱ仯������
     *
     * @param listener �仯������
     */
    @SuppressWarnings("unused")
    public void addOnChangeListener(OnChangeListener listener) {
        if (listener == null)
            return;
        if (changeListeners == null)
            changeListeners = new ArrayList<>();
        changeListeners.add(listener);
        listener.jumpTo(mCurrentPager);
    }

    /**
     * �Ƴ��仯������
     *
     * @param listener �仯������
     */
    @SuppressWarnings("unused")
    public void removeOnChangeListener(OnChangeListener listener) {
        if (changeListeners == null)
            return;
        changeListeners.remove(listener);
    }

    /**
     * ��ȡĬ��Tag����
     *
     * @return Ĭ��Tag����
     */
    protected Drawable getDefaultTagBackground() {
        final float density = getResources().getDisplayMetrics().density;
        final GradientDrawable mBackground = new GradientDrawable();
        mBackground.setShape(GradientDrawable.RECTANGLE);
        mBackground.setColor(0xffff4444);
        mBackground.setCornerRadius(10 * density);
        mBackground.setSize((int) (10 * density), (int) (10 * density));
        return mBackground;
    }

    /**
     * �仯����
     */
    public interface OnChangeListener {
        /**
         * ��ת����ǰλ��
         *
         * @param correct ��ǰλ��
         */
        void jumpTo(int correct);

        /**
         * �������
         *
         * @param correct ��ǰλ��
         * @param next    ��Ҫ�ִ�λ��
         * @param offset  �ƶ�����
         */
        void gotoLeft(int correct, int next, float offset);

        /**
         * ���ҹ���
         *
         * @param correct ��ǰλ��
         * @param next    ��Ҫ�ִ�λ��
         * @param offset  �ƶ�����
         */
        void gotoRight(int correct, int next, float offset);
    }

    /**
     * �Ǳ���������Adapter
     */
    public interface ItemTabAdapter {

        /**
         * �Ƿ����ýǱ�
         *
         * @param position Itemλ��
         * @return �Ƿ�����
         */
        boolean isTagEnable(int position);

        /**
         * ��ȡ�Ǳ�ֵ
         *
         * @param position Itemλ��
         * @return �Ǳ�ֵ
         */
        String getTag(int position);
    }
}