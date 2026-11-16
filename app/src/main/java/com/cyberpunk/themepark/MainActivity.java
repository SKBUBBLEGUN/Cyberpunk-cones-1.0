package com.cyberpunk.themepark;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.database.Cursor;
import android.provider.OpenableColumns;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    private static final int PICK_IMAGE = 2077;
    private LinearLayout gallery;
    private final ArrayList<String> hidden = new ArrayList<>();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(7, 10, 18));
        getWindow().setNavigationBarColor(Color.rgb(7, 10, 18));
        hidden.addAll(getPreferences(0).getStringSet("hidden_icons", new HashSet<>()));
        buildScreen();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size);
        t.setTextColor(color); t.setGravity(Gravity.CENTER); return t;
    }
    private void buildScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(18), dp(16), dp(8));
        root.setBackgroundColor(Color.rgb(10, 12, 20));
        TextView title = text("CYBERPUNK 2077", 25, Color.rgb(0,240,255));
        title.setTypeface(null, Typeface.BOLD);
        root.addView(title);
        TextView subtitle = text("ICON DATABASE  //  PERSONALIZE", 11, Color.rgb(255,45,110));
        root.addView(subtitle);
        Button add = new Button(this); add.setText("+  ADICIONAR IMAGENS");
        add.setTextColor(Color.BLACK); add.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(0,240,255)));
        root.addView(add);
        add.setOnClickListener(v -> {
            Intent pick = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            pick.setType("image/*"); pick.addCategory(Intent.CATEGORY_OPENABLE);
            pick.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(pick, PICK_IMAGE);
        });
        TextView hint = text("Toque em REMOVER para ocultar um ícone incluído ou apagar uma imagem adicionada.", 12, Color.LTGRAY);
        root.addView(hint);
        ScrollView scroll = new ScrollView(this);
        gallery = new LinearLayout(this); gallery.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(gallery);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        refreshGallery();
    }

    private void refreshGallery() {
        if (gallery == null) return;
        gallery.removeAllViews();
        ArrayList<View> row = new ArrayList<>();
        ArrayList<Integer> ids = new ArrayList<>();
        try {
            for (java.lang.reflect.Field field : R.drawable.class.getFields()) {
                String name = field.getName();
                if (name.equals("ic_pack") || hidden.contains(name)) continue;
                int id = field.getInt(null);
                if (name.startsWith("abc_") || name.startsWith("notification_") || name.startsWith("design_")) continue;
                ids.add(id);
            }
        } catch (Exception ignored) {}
        LinearLayout line = null;
        int count = 0;
        for (int id : ids) {
            if (count % 3 == 0) {
                line = new LinearLayout(this); line.setOrientation(LinearLayout.HORIZONTAL);
                gallery.addView(line, new LinearLayout.LayoutParams(-1, dp(142)));
            }
            line.addView(iconCard(getResources().getResourceEntryName(id), id, null),
                new LinearLayout.LayoutParams(0, -1, 1));
            count++;
        }
        File folder = new File(getFilesDir(), "custom_icons");
        File[] custom = folder.listFiles();
        if (custom != null) for (File file : custom) {
            if (!file.isFile()) continue;
            if (count % 3 == 0) {
                line = new LinearLayout(this); line.setOrientation(LinearLayout.HORIZONTAL);
                gallery.addView(line, new LinearLayout.LayoutParams(-1, dp(142)));
            }
            line.addView(iconCard(file.getName(), 0, file), new LinearLayout.LayoutParams(0, -1, 1));
            count++;
        }
        if (count == 0) gallery.addView(text("Nenhuma imagem encontrada.", 15, Color.WHITE));
    }

    private View iconCard(String name, int resource, File file) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL); card.setGravity(Gravity.CENTER);
        card.setPadding(dp(4), dp(5), dp(4), dp(4));
        card.setBackgroundColor(Color.rgb(20, 25, 38));
        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(68), dp(68));
        card.addView(image, ip);
        try {
            if (file != null) image.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            else image.setImageResource(resource);
        } catch (Exception ignored) {}
        TextView label = text(name, 10, Color.WHITE);
        label.setSingleLine(true); label.setEllipsize(android.text.TextUtils.TruncateAt.END);
        card.addView(label);
        Button remove = new Button(this); remove.setText("REMOVER");
        remove.setTextSize(9); remove.setMinHeight(dp(30)); remove.setMinimumHeight(dp(30));
        remove.setPadding(0,0,0,0);
        remove.setTextColor(Color.rgb(255,75,115));
        card.addView(remove, new LinearLayout.LayoutParams(-1, dp(32)));
        remove.setOnClickListener(v -> {
            if (file != null) {
                if (file.delete()) refreshGallery();
                else Toast.makeText(this, "Não foi possível remover.", Toast.LENGTH_SHORT).show();
            } else {
                hidden.add(name);
                getPreferences(0).edit().putStringSet("hidden_icons", new HashSet<>(hidden)).apply();
                refreshGallery();
            }
        });
        return card;
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_IMAGE || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri uri = data.getData();
        try {
            String name = "image_" + System.currentTimeMillis() + ".png";
            File folder = new File(getFilesDir(), "custom_icons");
            if (!folder.exists() && !folder.mkdirs()) throw new IOException("Não foi possível criar a pasta");
            try (InputStream in = getContentResolver().openInputStream(uri);
                 OutputStream out = new FileOutputStream(new File(folder, name))) {
                byte[] buffer = new byte[8192]; int n;
                while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
            }
            refreshGallery();
            Toast.makeText(this, "Imagem adicionada à galeria.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao importar imagem.", Toast.LENGTH_LONG).show();
        }
    }
}