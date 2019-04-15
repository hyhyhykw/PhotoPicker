///*
// * Copyright © Yan Zhenjie
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.hy.picker.utils;
//
//import android.content.Context;
//import android.text.TextUtils;
//
//import com.afollestad.materialdialogs.MaterialDialog;
//import com.hy.picker.R;
//import com.picker2.utils.AndroidLifecycleUtils;
//import com.yanzhenjie.permission.AndPermission;
//import com.yanzhenjie.permission.runtime.Permission;
//import com.yanzhenjie.permission.runtime.setting.SettingRequest;
//
//import java.util.List;
//
///**
// * Created by YanZhenjie on 2018/1/1.
// */
//public final class PermissionSetting {
//
//    private final Context mContext;
//
//    public PermissionSetting(Context context) {
//        this.mContext = context;
//    }
//
//    public void showSetting(final List<String> permissions, int requestCode) {
//        List<String> permissionNames = Permission.transformText(mContext, permissions);
//        String message = mContext.getString(R.string.picker_message_permission_always_failed, TextUtils.join("\n", permissionNames));
//
////        final SettingService settingService = AndPermission.permissionSetting(mContext);
//        SettingRequest setting = AndPermission.with(mContext).runtime().setting();
//
//        if (AndroidLifecycleUtils.canLoadImage(mContext)){
//            new MaterialDialog.Builder(mContext)
//                    .cancelable(false)
//                    .title(R.string.picker_title_dialog)
//                    .content(message)
//                    .positiveText(R.string.picker_setting)
//                    .onPositive((dialog, which) -> setting.start(requestCode))
//                    .negativeText(R.string.picker_no)
//                    .show();
//        }
//
//    }
//}
