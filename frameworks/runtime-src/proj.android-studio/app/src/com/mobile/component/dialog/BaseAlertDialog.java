package com.mobile.component.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.audiGame.teen_patti.R;
import com.mobile.component.base.BaseDialog;

import org.cocos2dx.lib.Utils;

public class BaseAlertDialog extends BaseDialog {

    protected TextView mtvTitle;
    protected TextView mtvCancel;
    protected TextView mtvOK;
    protected FrameLayout mContent;

    private CancelListener cancelListener;
    private OKListener okListener;

    protected Dialog mDialog;
    protected Window mWindow;

    protected int mWidthPixels = Utils.getScreenWidth();
    protected int mHeightPixels = Utils.getScreenHeight();

    @Override
    public int getLayoutID() {
        return R.layout.ad_dialog_alert;
    }

    @Override
    public int getStyle() {
        return R.style.ad_DialogTranslucent;
    }

    @Override
    public void initView(View view, @Nullable Bundle savedInstanceState) {
        mDialog = getDialog();
        mWindow = mDialog.getWindow();

        mtvTitle = view.findViewById(R.id.tv_title);
        mContent = view.findViewById(R.id.id_content);
        mtvCancel = view.findViewById(R.id.tv_cancel);
        mtvOK = view.findViewById(R.id.tv_sure);

        mtvCancel.setOnClickListener(view1 -> {
            if (cancelListener != null)
                cancelListener.onCancel();
            dismiss();
        });

        mtvOK.setOnClickListener(view1 -> {
            if (okListener != null)
                okListener.onOk();
            dismiss();
        });
    }

    @Override
    public void onStart() {
        if (mDialog != null) {
            if (null != mWindow) {
                int width = ViewGroup.LayoutParams.WRAP_CONTENT;
                int height = ViewGroup.LayoutParams.WRAP_CONTENT;
                if (getWidthScale() > 0) {
                    if (mWidthPixels > 0)
                        width = (int) (mWidthPixels * getWidthScale());
                }

                if (getHeightScale() > 0) {
                    if (mHeightPixels > 0)
                        height = (int) (mHeightPixels * getHeightScale());
                }

                mWindow.setLayout(width, height);
                mWindow.setDimAmount(0.5f);
            }
            mDialog.setCancelable(getCancelOutside());
            mDialog.setCanceledOnTouchOutside(getCancelOutside());
        }

        super.onStart();
    }

    protected boolean getCancelOutside() {
        return true;
    }

    protected double getWidthScale() {
        return 0.7;
    }

    protected double getHeightScale() {
        return 0;
    }

    public void setCancel(String cancel) {
        if (cancel == null || cancel.isEmpty())
            return;

        mtvCancel.setText(cancel);
    }

    public void setOK(String ok) {
        if (ok == null || ok.isEmpty())
            return;

        mtvOK.setText(ok);
    }

    public void setTitle(String title) {
        if (mtvTitle == null || title.isEmpty())
            return;

        mtvTitle.setText(title);
    }

    public void setCancelListener(CancelListener listener) {
        cancelListener = listener;
    }

    public void setOkListener(OKListener listener) {
        okListener = listener;
    }

    public interface CancelListener {
        void onCancel();
    }

    public interface OKListener {
        void onOk();
    }
}
