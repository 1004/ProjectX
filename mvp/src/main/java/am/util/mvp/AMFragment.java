/*
 * Copyright (C) 2015 AlexMofer
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

package am.util.mvp;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment
 * Created by Alex on 2017/3/14.
 */
@SuppressWarnings("unused")
@RequiresApi(11)
public abstract class AMFragment extends Fragment {

    private LocalBroadcastManager mLocalBroadcastManager;// 应用内部广播
    /**
     * 广播接受器
     */
    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onReceiveLocalBroadcast(context, intent);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AMPresenter presenter = getPresenter();
        if (null != presenter) {
            presenter.onCreated(savedInstanceState);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(
                getContentViewLayout(inflater, container, savedInstanceState),
                container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeFragment(savedInstanceState);
        if (isLocalBroadcastEnable()) {
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
            IntentFilter filter = new IntentFilter();
            onAddLocalAction(filter);
            mLocalBroadcastManager.registerReceiver(mLocalBroadcastReceiver, filter);
            onRegisteredLocalBroadcastReceiver();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        final AMPresenter presenter = getPresenter();
        if (null != presenter) {
            presenter.onStarted();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final AMPresenter presenter = getPresenter();
        if (null != presenter) {
            presenter.onResumed();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        final AMPresenter presenter = getPresenter();
        if (null != presenter) {
            presenter.onPaused();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        final AMPresenter presenter = getPresenter();
        if (null != presenter) {
            presenter.onStopped();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        final AMPresenter presenter = getPresenter();
        if (null != presenter) {
            presenter.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onDestroy() {
        if (!isLocalBroadcastUnregistered()) {
            mLocalBroadcastManager.unregisterReceiver(mLocalBroadcastReceiver);
            mLocalBroadcastManager = null;
            onUnregisteredLocalBroadcastReceiver();
        }
        super.onDestroy();
        final AMPresenter presenter = getPresenter();
        if (null != presenter) {
            presenter.onDestroyed();
            presenter.detach();
        }
    }

    /**
     * 获取内容Layout
     *
     * @param inflater           布局容器
     * @param container          根View
     * @param savedInstanceState 保存的状态
     * @return 资源id
     */
    @LayoutRes
    protected abstract int getContentViewLayout(LayoutInflater inflater,
                                                ViewGroup container, Bundle savedInstanceState);

    /**
     * 初始化Fragment
     *
     * @param savedInstanceState 保存的实例数据
     */
    protected abstract void initializeFragment(@Nullable Bundle savedInstanceState);

    /**
     * 获取基础Presenter
     *
     * @return 基础Presenter
     */
    protected AMPresenter getPresenter() {
        return null;
    }

    /**
     * 判断是否开启应用内广播
     * 对于Fragment来说其为非必须选项
     *
     * @return 是否开启
     */
    protected boolean isLocalBroadcastEnable() {
        return false;
    }

    /**
     * 添加本地广播监听意图
     *
     * @param filter 意图过滤器
     */
    protected void onAddLocalAction(IntentFilter filter) {
    }

    /**
     * 接收到本地广播
     *
     * @param context 发起广播的context
     * @param intent  发起广播的意图
     */
    protected void onReceiveLocalBroadcast(Context context, Intent intent) {
    }

    /**
     * 已注册本地广播接收器
     */
    protected void onRegisteredLocalBroadcastReceiver() {
    }

    /**
     * 已取消注册本地广播接收器
     */
    protected void onUnregisteredLocalBroadcastReceiver() {
    }

    /**
     * 判断本地广播接收器是否已取消注册
     *
     * @return 是否已取消注册
     */
    public final boolean isLocalBroadcastUnregistered() {
        return mLocalBroadcastManager == null;
    }

    /**
     * 通过ID查找View
     *
     * @param id  View 的资源ID
     * @param <V> View类型
     * @return 对应资源ID的View
     */
    @SuppressWarnings("unchecked")
    public final <V extends View> V findViewById(int id) {
        if (null == getView()) {
            // 在错误的时机调用
            throw new IllegalStateException("Fragment " + this
                    + " has not created its view yet.");
        }
        return (V) getView().findViewById(id);
    }

    /**
     * 发送广播
     *
     * @param action 广播动作
     */
    public final void sendLocalBroadcast(String action) {
        sendLocalBroadcast(new Intent(action));
    }

    /**
     * 发送广播
     *
     * @param intent 广播意图
     */
    public final void sendLocalBroadcast(Intent intent) {
        if (isLocalBroadcastUnregistered()) {
            return;
        }
        mLocalBroadcastManager.sendBroadcast(intent);
    }
}
