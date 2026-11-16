package com.cyberpunk.themepark;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(32, 32, 32, 32);
        layout.setBackgroundColor(Color.rgb(10, 12, 20));
        TextView title = new TextView(this);
        title.setText("CYBERPUNK 2077\nICON PACK");
        title.setTextColor(Color.rgb(0, 240, 255));
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        TextView info = new TextView(this);
        info.setText("\n186 ícones incluídos\n\nAbra Theme Park → Icon → Create new → Iconpack.\nSelecione Cyberpunk 2077 Icon Pack em Third-party icon packs.\nSalve e aplique.");
        info.setTextColor(Color.WHITE);
        info.setTextSize(16);
        info.setGravity(Gravity.CENTER);
        layout.addView(title);
        layout.addView(info);
        setContentView(layout);
    }
}
