package utils;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.app.vst.christapp.R;

public class ScoreView extends TextView {
    public int MaximumValue;
    public double Interval;
    public double Value;
    public boolean Absent;
    public int Progress = -1;
    public Handler ValueChangeCallback;

    public ScoreView(Context context) {
        super(context);
        this.init();
    }
    public ScoreView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    private void init() {
        this.setGravity(Gravity.CENTER);
        this.setTextSize(24);
        this.setTypeface(Common.getDigitalFontface(this.getContext())); //Typeface.DEFAULT_BOLD
        this.setTextColor(Color.parseColor("#112233"));

        final ScoreView instance = this;
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Absent == true) {
                    Common.alert(instance.getContext(), "ERROR: Access denied, Student status set as absent!");
                    return;
                }

                LayoutInflater inflater = (LayoutInflater) instance.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.view_group_score, null);
                final PopupWindow window = new PopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
                window.setOutsideTouchable(true);

                final TextView lbl_value = (TextView) layout.findViewById(R.id.lbl_value);
                final SeekBar sb_value = (SeekBar) layout.findViewById(R.id.sb_value);
                lbl_value.setTypeface(Common.getDigitalFontface(lbl_value.getContext())); //Typeface.DEFAULT_BOLD
                sb_value.setMax((int) (MaximumValue / Interval));

                if(Progress > -1) {
                    sb_value.setProgress(Progress);
                    lbl_value.setText(Double.toString(Progress * Interval));
                }
                else {
                    sb_value.setProgress(0);
                    lbl_value.setText("");
                }

                sb_value.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        lbl_value.setText(Double.toString(sb_value.getProgress() * Interval));
                        return false;
                    }
                });
                sb_value.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lbl_value.setText(Double.toString(sb_value.getProgress() * Interval));
                    }
                });
                sb_value.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        Progress = progress;
                        lbl_value.setText(Double.toString(progress * Interval));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                layout.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        instance.setText("");
                        String value = lbl_value.getText().toString();
                        if(value != null && value.trim().length() > 0) {
                            double dValue = 0.00F;
                            dValue = Double.parseDouble(value.trim());
                            instance.setText(value);
                            Value = dValue;
                        }
                        window.dismiss();

                        if(ValueChangeCallback != null) {
                            Message msg = new Message();
                            msg.obj = instance;
                            ValueChangeCallback.sendMessage(msg);
                        }
                    }
                });
                layout.findViewById(R.id.btn_cancel).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        window.dismiss();
                    }
                });
                /*layout.findViewById(R.id.pnl_overlay).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        window.dismiss();
                    }
                });*/

                window.showAtLocation(layout, Gravity.CENTER, 0, 0);
            }
        });
    }
}
