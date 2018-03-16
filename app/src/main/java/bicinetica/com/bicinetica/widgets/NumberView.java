package bicinetica.com.bicinetica.widgets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressWarnings("all")
public class NumberView extends TextView {

    private double value = 0;

    public NumberView(Context context) {
        super(context);
    }

    public NumberView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NumberView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public NumberView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setValue(double value) {
        this.value = value;
        setText(value > 0 ? String.format("%.2f", value) : "--");
    }

    public void setValue(long value) {
        this.value = value;
        setText(value > 0 ? String.valueOf(value) : "--");
    }
}
