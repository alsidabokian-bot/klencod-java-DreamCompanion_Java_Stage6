package com.example.dreamcompanion;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private android.content.SharedPreferences prefs;
    private TextView timeLabel, statusLabel, dreamCountLabel, dreamListLabel, pointsLabel, badgeLabel;
    private Switch reminderSwitch;
    private int reminderHour, reminderMinute;
    private int quietStart, quietEnd;
    private final int bg = Color.rgb(23, 21, 45);
    private final int panel = Color.rgb(39, 35, 68);
    private final int lavender = Color.rgb(190, 177, 255);

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("dream_settings", MODE_PRIVATE);
        reminderHour = prefs.getInt("hour", 10);
        reminderMinute = prefs.getInt("minute", 0);
        quietStart = prefs.getInt("quiet_start", 22);
        quietEnd = prefs.getInt("quiet_end", 7);
        buildScreen();
        askNotificationPermission();
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(bg);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(28), dp(20), dp(28));
        scroll.addView(root);

        TextView title = text("رفيق الأحلام الواعية 🌙", 26, Color.WHITE, true);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());
        TextView subtitle = text("خطوات صغيرة تساعدك على تذكّر أحلامك", 14, 0xFFD0CBE8, false);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = matchWrap(); subParams.topMargin = dp(8);
        root.addView(subtitle, subParams);

        addGap(root, 24);
        LinearLayout card = card(root);
        card.addView(text("تذكيرات فحوصات الواقع", 19, Color.WHITE, true), matchWrap());
        reminderSwitch = new Switch(this);
        reminderSwitch.setText("تفعيل التذكيرات اليومية");
        reminderSwitch.setTextColor(Color.WHITE);
        reminderSwitch.setTextSize(16);
        reminderSwitch.setChecked(prefs.getBoolean("enabled", false));
        LinearLayout.LayoutParams swp = matchWrap(); swp.topMargin = dp(12);
        card.addView(reminderSwitch, swp);
        reminderSwitch.setOnCheckedChangeListener((button, checked) -> {
            prefs.edit().putBoolean("enabled", checked).apply();
            if (checked) { ReminderScheduler.schedule(this); statusLabel.setText("التذكيرات مفعّلة ✓"); }
            else { ReminderScheduler.cancel(this); statusLabel.setText("التذكيرات متوقفة"); }
        });

        Button timeButton = button("اختيار وقت التذكير");
        LinearLayout.LayoutParams bp = matchWrap(); bp.topMargin = dp(14);
        card.addView(timeButton, bp);
        timeLabel = text("الوقت: " + format(reminderHour, reminderMinute), 15, lavender, true);
        timeLabel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tlp = matchWrap(); tlp.topMargin = dp(8);
        card.addView(timeLabel, tlp);
        timeButton.setOnClickListener(v -> new TimePickerDialog(this, (view, h, m) -> {
            reminderHour = h; reminderMinute = m;
            prefs.edit().putInt("hour", h).putInt("minute", m).apply();
            timeLabel.setText("الوقت: " + format(h, m));
            if (reminderSwitch.isChecked()) ReminderScheduler.schedule(this);
        }, reminderHour, reminderMinute, false).show());

        addGap(root, 16);
        LinearLayout quietCard = card(root);
        quietCard.addView(text("فترة الهدوء والنوم", 19, Color.WHITE, true), matchWrap());
        TextView quietInfo = text("لن يرسل التطبيق التذكير خلال هذه الفترة.", 14, 0xFFD0CBE8, false);
        LinearLayout.LayoutParams qip = matchWrap(); qip.topMargin = dp(6); quietCard.addView(quietInfo, qip);
        Button startButton = button("بداية الهدوء: " + format(quietStart, 0));
        Button endButton = button("نهاية الهدوء: " + format(quietEnd, 0));
        LinearLayout.LayoutParams qbp = matchWrap(); qbp.topMargin = dp(12); quietCard.addView(startButton, qbp);
        LinearLayout.LayoutParams qbp2 = matchWrap(); qbp2.topMargin = dp(8); quietCard.addView(endButton, qbp2);
        startButton.setOnClickListener(v -> chooseQuietTime(true, startButton));
        endButton.setOnClickListener(v -> chooseQuietTime(false, endButton));

        addGap(root, 16);
        LinearLayout info = card(root);
        info.addView(text("تذكير لطيف", 18, Color.WHITE, true), matchWrap());
        TextView msg = text("لا تحتاج إلى تقليل ساعات نومك أو إجبار نفسك على الاستيقاظ. اهتم براحتك، واكتب ما تتذكره من أحلامك عند الاستيقاظ.", 15, 0xFFE4E0F4, false);
        LinearLayout.LayoutParams mp = matchWrap(); mp.topMargin = dp(8); info.addView(msg, mp);

        statusLabel = text(prefs.getBoolean("enabled", false) ? "التذكيرات مفعّلة ✓" : "التذكيرات متوقفة", 14, lavender, true);
        statusLabel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams stp = matchWrap(); stp.topMargin = dp(18); root.addView(statusLabel, stp);

        addGap(root, 24);
        LinearLayout journalCard = card(root);
        journalCard.addView(text("دفتر الأحلام 📖", 21, Color.WHITE, true), matchWrap());
        TextView journalHint = text("دوّن ما تتذكره من أحلامك فور الاستيقاظ، حتى لو كانت مجرد تفاصيل صغيرة.", 14, 0xFFD0CBE8, false);
        LinearLayout.LayoutParams jhp = matchWrap(); jhp.topMargin = dp(8); journalCard.addView(journalHint, jhp);
        Button addDreamButton = button("+ إضافة حلم جديد");
        LinearLayout.LayoutParams jbp = matchWrap(); jbp.topMargin = dp(14); journalCard.addView(addDreamButton, jbp);
        addDreamButton.setOnClickListener(v -> showAddDreamDialog());
        dreamCountLabel = text("الأحلام المسجلة: 0", 15, lavender, true);
        LinearLayout.LayoutParams dcp = matchWrap(); dcp.topMargin = dp(14); journalCard.addView(dreamCountLabel, dcp);
        dreamListLabel = text("لم تسجل أي حلم بعد. ابدأ بتدوين أول حلم تتذكره!", 14, 0xFFE4E0F4, false);
        LinearLayout.LayoutParams dlp = matchWrap(); dlp.topMargin = dp(10); journalCard.addView(dreamListLabel, dlp);
        Button clearDreamsButton = button("حذف جميع الأحلام");
        LinearLayout.LayoutParams clearParams = matchWrap(); clearParams.topMargin = dp(12);
        journalCard.addView(clearDreamsButton, clearParams);
        clearDreamsButton.setOnClickListener(v -> confirmClearDreams());

        addGap(root, 16);
        LinearLayout rewardCard = card(root);
        rewardCard.addView(text("رحلة الاستمرار ⭐", 19, Color.WHITE, true), matchWrap());
        pointsLabel = text("نقاطك: 0", 17, lavender, true);
        LinearLayout.LayoutParams plp = matchWrap(); plp.topMargin = dp(8); rewardCard.addView(pointsLabel, plp);
        TextView rewardHint = text("كل حلم تسجله يمنحك 10 نقاط.", 14, 0xFFE4E0F4, false);
        LinearLayout.LayoutParams rhp = matchWrap(); rhp.topMargin = dp(8); rewardCard.addView(rewardHint, rhp);
        badgeLabel = text("الشارات المحققة:", 14, 0xFFE4E0F4, false);
        LinearLayout.LayoutParams blp = matchWrap(); blp.topMargin = dp(8); rewardCard.addView(badgeLabel, blp);
        refreshDreams();
        setContentView(scroll);
    }


    private void showAddDreamDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(20), dp(8), dp(20), 0);
        EditText titleInput = new EditText(this);
        titleInput.setHint("عنوان الحلم (اختياري)");
        titleInput.setSingleLine(true);
        layout.addView(titleInput, matchWrap());
        EditText dreamInput = new EditText(this);
        dreamInput.setHint("اكتب ما تتذكره من الحلم...");
        dreamInput.setMinLines(4);
        dreamInput.setGravity(Gravity.TOP | Gravity.RIGHT);
        dreamInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        LinearLayout.LayoutParams inputParams = matchWrap(); inputParams.topMargin = dp(10);
        layout.addView(dreamInput, inputParams);
        new AlertDialog.Builder(this)
            .setTitle("تدوين حلم جديد")
            .setView(layout)
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حفظ الحلم", (dialog, which) -> {
                String body = dreamInput.getText().toString().trim();
                String title = titleInput.getText().toString().trim();
                if (body.isEmpty()) {
                    Toast.makeText(this, "اكتب بعض التفاصيل قبل الحفظ", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (title.isEmpty()) title = "حلم جديد";
                saveDream(title, body);
            }).show();
    }


    private void confirmClearDreams() {
        new AlertDialog.Builder(this)
            .setTitle("حذف دفتر الأحلام")
            .setMessage("هل تريد حذف جميع الأحلام المسجلة؟ لا يمكن التراجع عن هذا الإجراء.")
            .setNegativeButton("إلغاء", null)
            .setPositiveButton("حذف الكل", (dialog, which) -> {
                prefs.edit().remove("dream_entries").apply();
                refreshDreams();
                Toast.makeText(this, "تم حذف جميع الأحلام", Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    private void saveDream(String title, String body) {
        JSONArray dreams = readDreams();
        JSONObject item = new JSONObject();
        try {
            item.put("title", title);
            item.put("body", body);
            item.put("date", new SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(new Date()));
            dreams.put(item);
            prefs.edit().putString("dream_entries", dreams.toString()).apply();
            refreshDreams();
            Toast.makeText(this, "تم حفظ حلمك وإضافة 10 نقاط ⭐", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Toast.makeText(this, "تعذر حفظ الحلم، حاول مرة أخرى", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONArray readDreams() {
        String raw = prefs.getString("dream_entries", "[]");
        try { return new JSONArray(raw); }
        catch (JSONException e) { return new JSONArray(); }
    }

    private void refreshDreams() {
        if (dreamCountLabel == null) return;
        JSONArray dreams = readDreams();
        int count = dreams.length();
        dreamCountLabel.setText("الأحلام المسجلة: " + count);
        pointsLabel.setText("نقاطك: " + (count * 10) + " ⭐");
        String badges = "الشارات المحققة:\n";
        badges += count >= 1 ? "🏅 شارة البداية ✓\n" : "🔒 شارة البداية — سجّل حلماً واحداً\n";
        badges += count >= 5 ? "🌟 شارة المثابرة ✓\n" : "🔒 شارة المثابرة — سجّل 5 أحلام\n";
        badges += count >= 10 ? "🏆 شارة جامع الأحلام ✓" : "🔒 شارة جامع الأحلام — سجّل 10 أحلام";
        badgeLabel.setText("كل حلم تسجله يمنحك 10 نقاط.\n" + badges);
        if (count == 0) {
            dreamListLabel.setText("لم تسجل أي حلم بعد. ابدأ بتدوين أول حلم تتذكره!");
            return;
        }
        StringBuilder list = new StringBuilder();
        for (int i = count - 1; i >= 0; i--) {
            JSONObject dream = dreams.optJSONObject(i);
            if (dream == null) continue;
            list.append("• ").append(dream.optString("title", "حلم جديد"))
                .append("\n").append(dream.optString("date", ""))
                .append("\n").append(dream.optString("body", ""));
            if (i > 0) list.append("\n\n────────────\n\n");
        }
        dreamListLabel.setText(list.toString());
    }

    private void chooseQuietTime(boolean start, Button target) {
        int current = start ? quietStart : quietEnd;
        new TimePickerDialog(this, (view, h, m) -> {
            if (start) { quietStart = h; prefs.edit().putInt("quiet_start", h).apply(); target.setText("بداية الهدوء: " + format(h, 0)); }
            else { quietEnd = h; prefs.edit().putInt("quiet_end", h).apply(); target.setText("نهاية الهدوء: " + format(h, 0)); }
        }, current, 0, false).show();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 91);
    }
    private LinearLayout card(LinearLayout parent) {
        LinearLayout c = new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16), dp(16), dp(16), dp(16));
        c.setBackgroundColor(panel); LinearLayout.LayoutParams p = matchWrap(); p.topMargin = dp(2); parent.addView(c, p); return c;
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); t.setGravity(Gravity.RIGHT); return t;
    }
    private Button button(String label) {
        Button b = new Button(this); b.setText(label); b.setTextColor(Color.WHITE); b.setAllCaps(false); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF6555A8)); return b;
    }
    private LinearLayout.LayoutParams matchWrap() { return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); }
    private void addGap(LinearLayout root, int height) { View v = new View(this); root.addView(v, new LinearLayout.LayoutParams(1, dp(height))); }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }
    private String format(int h, int m) { return String.format(Locale.getDefault(), "%02d:%02d", h, m); }
}