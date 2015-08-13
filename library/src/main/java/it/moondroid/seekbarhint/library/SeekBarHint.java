package it.moondroid.seekbarhint.library;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

public class SeekBarHint extends SeekBar implements SeekBar.OnSeekBarChangeListener {

    private String mPopupText;
    private int mPopupStyle;
    public static final int POPUP_FIXED = 1;
    public static final int POPUP_FOLLOW = 0;

    private PopupWindow mPopup;
    private TextView mPopupTextView;
    private int mYLocationOffset;

    private OnSeekBarChangeListener mInternalListener;
    private OnSeekBarChangeListener mExternalListener;

    private OnSeekBarHintProgressChangeListener mProgressChangeListener;

    public interface OnSeekBarHintProgressChangeListener {
        public String onHintTextChanged(SeekBarHint seekBarHint, int progress);
    }

    public SeekBarHint (Context context) {
        super(context);
        init(context, null);
    }

    public SeekBarHint (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public SeekBarHint (Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){

        setOnSeekBarChangeListener(this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarHint);

        mYLocationOffset = (int) a.getDimension(R.styleable.SeekBarHint_yOffset, 0);
        mPopupStyle = a.getInt(R.styleable.SeekBarHint_popupStyle, POPUP_FOLLOW);

        a.recycle();
        initHintPopup();
    }

    public void setPopupStyle(int style){
        mPopupStyle = style;
    }

    public int getPopupStyle(){
        return mPopupStyle;
    }

    private void initHintPopup(){
        if (mProgressChangeListener!=null){
            mPopupText = mProgressChangeListener.onHintTextChanged(this, getProgress());
        }

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View undoView = inflater.inflate(R.layout.popup, null);
        mPopupTextView = (TextView)undoView.findViewById(R.id.text);
        mPopupTextView.setText(mPopupText!=null? mPopupText : String.valueOf(getProgress()));

        mPopup = new PopupWindow(undoView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);

        mPopup.setAnimationStyle(R.style.fade_animation);

    }

    private void showPopup(){
        if(mPopupStyle==POPUP_FOLLOW){
            mPopup.showAtLocation(getRootView(), Gravity.LEFT | Gravity.BOTTOM, (int) (this.getX()+(int) getXPosition(this)), (this.getGlobalTop() + mYLocationOffset));
        }
        if (mPopupStyle==POPUP_FIXED){
            mPopup.showAtLocation(getRootView(), Gravity.CENTER | Gravity.BOTTOM, 0, (this.getGlobalTop() + mYLocationOffset));
        }
    }

    private void hidePopup(){
        if(mPopup.isShowing()) {
            mPopup.dismiss();
        }
    }

    public void setHintView(View view){
        //TODO
        //initHintPopup();
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        if (mInternalListener==null){
            mInternalListener = l;
            super.setOnSeekBarChangeListener(l);
        }else {
            mExternalListener = l;
        }
    }

    public void setOnProgressChangeListener(OnSeekBarHintProgressChangeListener l){
        mProgressChangeListener = l;
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        if (mProgressChangeListener!=null){
            mPopupText = mProgressChangeListener.onHintTextChanged(this, getProgress());
        }

        if(mExternalListener !=null){
            mExternalListener.onProgressChanged(seekBar, progress, b);
        }


        mPopupTextView.setText(mPopupText != null ? mPopupText : String.valueOf(progress));

        if(mPopupStyle==POPUP_FOLLOW){
            mPopup.update((int) (this.getX()+(int) getXPosition(seekBar)), (this.getGlobalTop() + mYLocationOffset), -1, -1);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if(mExternalListener !=null){
            mExternalListener.onStartTrackingTouch(seekBar);
        }

        showPopup();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(mExternalListener !=null){
            mExternalListener.onStopTrackingTouch(seekBar);
        }

        hidePopup();
    }


    private float getXPosition(SeekBar seekBar){
        float val = (((float)seekBar.getProgress() * (float)(seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax());
        float offset = seekBar.getThumbOffset();

        ((View)mPopupTextView.getParent()).measure(0, 0);

        int textWidth = ((View)mPopupTextView.getParent()).getMeasuredWidth();
        float textCenter = (textWidth/2.0f);

        float newX = val+offset - textCenter;

        return newX;
    }

    private int getGlobalTop() {
        int[] coords = {0 ,0};
        getLocationInWindow(coords);
        return getRootView().getHeight() - coords[1];
    }
}
