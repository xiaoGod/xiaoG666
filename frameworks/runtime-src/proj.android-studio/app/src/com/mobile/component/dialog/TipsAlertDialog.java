package com.mobile.component.dialog;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.audiGame.teen_patti.R;

import org.cocos2dx.lib.Utils;

@SuppressLint("ValidFragment")
public class TipsAlertDialog extends BaseAlertDialog {

    private TextView mtvTips;
    private String msg;

    public TipsAlertDialog(String msg) {
        this.msg = msg;
    }

    public static TipsAlertDialog newInstance(String msg) {
        TipsAlertDialog dialog = new TipsAlertDialog(msg);
        Bundle bundle = new Bundle();
        dialog.setArguments(bundle);
        return dialog;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void initView(View view, @Nullable Bundle savedInstanceState) {
        super.initView(view, savedInstanceState);

        mtvTips = new TextView(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        mtvTips.setLayoutParams(layoutParams);
        mtvTips.setGravity(Gravity.CENTER);
        mtvTips.setLineSpacing(Utils.dip2px(4), 1f);
        mtvTips.setTextSize(15);
        mtvTips.setTextColor(Utils.getColor(R.color.pp_color_666666));
        mtvTips.setText(msg);

        mContent.addView(mtvTips);

        View divide = view.findViewById(R.id.id_divide);
        divide.setVisibility(View.GONE);
        mtvCancel.setVisibility(View.GONE);

        mtvOK.setTextColor(Utils.getColor(R.color.pp_color_FF7CA0));
    }

    @Override
    public boolean getCancelOutside() {
        return true;
    }

    @Override
    public double getWidthScale() {
        Configuration mConfiguration = this.getResources().getConfiguration(); //获取设置的配置信息
        int ori = mConfiguration.orientation; //获取屏幕方向
        return ori == mConfiguration.ORIENTATION_PORTRAIT ? 0.7 : 0.4;
    }
}
