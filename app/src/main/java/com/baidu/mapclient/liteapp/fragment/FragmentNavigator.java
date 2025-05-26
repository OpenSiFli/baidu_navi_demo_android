/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.mapclient.liteapp.fragment;

import java.util.Stack;

import com.baidu.mapclient.liteapp.R;

import android.text.TextUtils;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class FragmentNavigator {

    private FragmentManager fragmentManager = null;
    private FragmentActivity activity = null;
    private Stack<BNBaseFragment> fragmentStack = new Stack<>();

    public FragmentNavigator(FragmentActivity activity) {
        this.activity = activity;
        fragmentManager = activity.getSupportFragmentManager();
    }

    public void jumpTo(String fragmentName) {
        BNBaseFragment fragment = (BNBaseFragment) fragmentManager.findFragmentByTag(fragmentName);
        BNBaseFragment curFragment = null;
        if (fragment == null) {
            if (TextUtils.equals(fragmentName, "BNLightNaviFragment")) {
                fragment = new BNLightNaviFragment();
            } else if (TextUtils.equals(fragmentName, "BNProNaviFragment")) {
                fragment = new BNProNaviFragment();
            } else if (TextUtils.equals(fragmentName, "BNRouteResultFragment")) {
                fragment = new BNRouteResultFragment();
            } else {
                fragment = null;
            }
        }

        if (fragment == null) {
            return;
        }

        if (fragmentStack != null && fragmentStack.size() != 0) {
            curFragment = fragmentStack.peek();
        }
        if (fragmentStack != null) {
            fragmentStack.push(fragment);
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (curFragment != null) {
            fragmentTransaction.replace(R.id.view_container, fragment, fragmentName);
        } else {
            fragmentTransaction.add(R.id.view_container, fragment, fragmentName);
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void goBack() {
        if (fragmentStack != null && fragmentStack.size() != 0) {
            BNBaseFragment fragment = fragmentStack.pop();
            if (fragment != null) {
                fragment.goBack();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if (fragmentStack.size() != 0) {
                    BNBaseFragment lastFragment = fragmentStack.peek();
                    fragmentTransaction.replace(R.id.view_container, lastFragment);
                } else {
                    activity.finish();
                }
                fragmentTransaction.commitAllowingStateLoss();
            }
        }

    }

    public Stack<BNBaseFragment> getFragmentStack() {
        return fragmentStack;
    }
}
