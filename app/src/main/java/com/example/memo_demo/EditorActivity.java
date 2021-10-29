package com.example.memo_demo;

import android.annotation.SuppressLint;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import ja.burhanrashid52.photoeditor.*;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;


public class EditorActivity extends AppCompatActivity {

    public static final String TAG = "HyperMemo";
    protected RichEditorX mEditor;
    PhotoEditorView mPhotoEditorView;
    PhotoEditor mPhotoEditor;

    private boolean hiding = false;
    int marginOriginIndex = 0;
    int marginNewIndex = 1;
    int[] margin = {0, 0};

    // color changers
    int textColorIndex = 0;
    int textBgColorIndex = 6;
    int[] textColorSrcId = {R.color.BLACK, R.color.GREEN, R.color.BLUE, R.color.YELLOW,
            R.color.CYAN, R.color.RED, R.color.WHITE};
    int[] textColors = {Color.BLACK, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.CYAN, Color.RED, Color.WHITE};

    // default test url
    private final String mp3Url = "https://file-examples-com.github.io/uploads/2017/11/file_example_MP3_5MG.mp3";
    private final String link_url = "https://www.oppo.com/cn/";
    private final String videoUrl = "https://www.oppo.com/content/dam/oppo/common/mkt/reno-page/renoSeriesBackGroundPc.mp4";
    private final String imgUrl = "https://www.oppo.com/content/dam/oppo/product-asset-library/reno/reno6-cn/reno6/v2/index/assets/kv-phone-purple-0249e2.png.webp";
    protected Uri pasteUri;
    protected String pasteText = "";
    protected MemoFileManager memoFileManager = null;
    protected EditorRequestHandler requestHandler = null;
    protected Dialog dialog = null;
    private String content;
    protected Bitmap bitmap;
    final int EDITOR = 0;
    final int VIEW = 1;
    final int DRAW = 2;
    final int editorModeSize = 3;
    int editorMode = EDITOR;
    protected HashMap<Integer, int[]> visibleTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        helperClassInit();
        editorInit();
        photoEditorInit();
        imgBtnInit();
        mEditor.focusEditor();
        mEditor.setFontSize(100);
    }

    protected void editorInit() {
        mEditor = findViewById(R.id.editorX);
        mEditor.setPadding(10, 10, 10, 10);
    }

    protected void helperClassInit() {
        memoFileManager = new MemoFileManager(this);
        requestHandler = new EditorRequestHandler(this, memoFileManager);
        dialog = new Dialog(this);
    }

    protected void photoEditorInit() {
        mPhotoEditorView = findViewById(R.id.photoEditorView);
        mPhotoEditorView.getSource().setImageResource(R.drawable.white);
        mPhotoEditor = new PhotoEditor.Builder(this, mPhotoEditorView)
                .setPinchTextScalable(true)
                .build();
        mPhotoEditor.setShape(new ShapeBuilder().withShapeColor(Color.RED));
        mPhotoEditor.setOnPhotoEditorListener(new OnPhotoEditorListener() {
            @Override
            public void onEditTextChangeListener(View rootView, String text, int colorCode) {

            }

            @Override
            public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {

            }

            @Override
            public void onRemoveViewListener(ViewType viewType, int numberOfAddedViews) {

            }

            @Override
            public void onStartViewChangeListener(ViewType viewType) {

            }

            @Override
            public void onStopViewChangeListener(ViewType viewType) {
                saveEditingImg();
            }

            @Override
            public void onTouchSourceImage(MotionEvent event) {
            }
        });
    }

    protected byte[] getImgByteArray() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, stream);
            return stream.toByteArray();
        } else {
            return null;
        }
    }

    protected byte[] imgEncoding() {
        byte[] bytes = getImgByteArray();
        if (bytes == null)
            return null;
        byte[] byte64 = Base64.getEncoder().encode(bytes);
        return StringProcessor.imgToByteArray(byte64);
    }

    protected void saveEditingImg() {
        mPhotoEditor.saveAsBitmap(new SaveSettings.Builder().setClearViewsEnabled(false).build(), new OnSaveBitmap() {
            @Override
            public void onBitmapReady(Bitmap saveBitmap) {
                Log.e("PhotoEditor", "Image Saved Successfully");
                bitmap = saveBitmap;
                sendImg();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("PhotoEditor", "Fail to save");
            }
        });
    }

    protected void loadEditingImg(Bitmap newBitmap) {
        mPhotoEditor.clearAllViews();
        mPhotoEditorView.getSource().setImageBitmap(newBitmap);
    }

    protected void updatePasteUri() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = Objects.requireNonNull(clipboardManager).getPrimaryClip();
        ClipData.Item item = Objects.requireNonNull(clip).getItemAt(0);
        pasteUri = item.getUri();

        if (pasteUri != null) {
            ContentResolver cr = getContentResolver();
            if (cr != null) {
                String uriMimeType = cr.getType(pasteUri);
                pasteText = uriMimeType;
            } else {
                pasteText = (String) item.getText();
            }
        } else {
            pasteText = (String) item.getText();
        }
    }


    protected String getPasteText() {
        return pasteText;
    }

    protected void copyToClipBoard(String str) {
        // TODO use it to complete clipboard feature
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("simple text", str);
        if (clipboardManager != null) {
            clipboardManager.setPrimaryClip(clipData);
        }
    }

    private void updateHTMLFromClipBoardData() {
        // TODO use it to complete clipboard feature
        if (pasteUri != null) {
            ContentResolver cr = getContentResolver();
            String uriMimeType = cr.getType(pasteUri);
            if (uriMimeType != null) {
//            if (uriMimeType.equals(MIME_TYPE_CONTACT))
//                Cursor pasteCursor = cr.query(uri, null, null, null, null);
//                if (pasteCursor != null)
//                    if (pasteCursor.moveToFirst())
//                pasteCursor.close();
            }
        }
    }

    private void setContentString(String str) {
        content = str;
    }

    private String getContentString() {
        return content;
    }

    protected void setVisibleTable() {
        visibleTable = new HashMap<>();
        visibleTable.put(R.id.editorX, new int[]{View.VISIBLE, View.VISIBLE, View.GONE});
        visibleTable.put(R.id.action_undo, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_redo, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_bold, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_italic, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_strikethrough, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_underline, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.change_txt_color, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.change_bg_txt_color, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_txt_color, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_bg_color, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_indent, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_outdent, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_align_left, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_align_center, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_align_right, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_blockquote, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_insert_bullets, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_insert_numbers, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_insert_image, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_insert_audio, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_insert_video, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_insert_link, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.action_insert_checkbox, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.saveImg, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.loadImg, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.new_file, new int[]{View.VISIBLE, View.GONE, View.VISIBLE});
        visibleTable.put(R.id.hideImg, new int[]{View.VISIBLE, View.VISIBLE, View.VISIBLE});
        visibleTable.put(R.id.copyFromServer, new int[]{View.VISIBLE, View.GONE, View.GONE});
        visibleTable.put(R.id.pasteFromServer, new int[]{View.VISIBLE, View.GONE, View.GONE});

        visibleTable.put(R.id.photoEditorView, new int[]{View.GONE, View.GONE, View.VISIBLE});
        visibleTable.put(R.id.sync, new int[]{View.GONE, View.GONE, View.VISIBLE});
    }

    private void imgBtnInit() {
        setVisibleTable();
        updateVisibilities();
        initLayout();
        initBtnClickListeners();
    }

    protected void initLayout() {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) findViewById(R.id.editor_view).getLayoutParams();
        margin[marginOriginIndex] = params.topMargin;
    }

    protected void initBtnClickListeners() {
        findViewById(R.id.sync).setOnClickListener(v ->
                ColorPickerDialogBuilder.with(this).setTitle("Choose color")
                        .initialColor(mPhotoEditor.getBrushColor())
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(5)
                        .setOnColorSelectedListener(selectedColor -> {
                        })
                        .setPositiveButton("ok", (dialog, selectedColor, allColors) -> mPhotoEditor.setShape(new ShapeBuilder().withShapeColor(selectedColor))
                        )
                        .setNegativeButton("cancel", (dialog, which) -> {
                        })
                        .build()
                        .show()
        );

        findViewById(R.id.mode_btn).setOnClickListener(v -> {
            mEditor.focusEditor();
            modeChange();
        });
        findViewById(R.id.action_undo).setOnClickListener(v -> {
            mEditor.focusEditor();
            mEditor.undo();
        });

        findViewById(R.id.action_redo).setOnClickListener(v -> {
            mEditor.focusEditor();
            mEditor.redo();
        });

        findViewById(R.id.action_bold).setOnClickListener(v -> {
            mEditor.focusEditor();
            mEditor.setBold();
        });

        findViewById(R.id.action_italic).setOnClickListener(v -> {
            mEditor.focusEditor();
            mEditor.setItalic();
        });

        findViewById(R.id.action_strikethrough).setOnClickListener(v -> {
            mEditor.focusEditor();
            mEditor.setStrikeThrough();
        });

        findViewById(R.id.action_underline).setOnClickListener(v -> {

            mEditor.focusEditor();
            mEditor.setUnderline();
        });

        findViewById(R.id.change_txt_color).setOnClickListener(v -> {
            textColorIndex += 1;
            if (textColorIndex >= textColorSrcId.length) {
                textColorIndex = 0;
            }
            ((ImageButton) findViewById(R.id.change_txt_color)).setImageResource(textColorSrcId[textColorIndex]);
            mEditor.setTextColor(textColors[textColorIndex]);
        });

        findViewById(R.id.change_bg_txt_color).setOnClickListener(v -> {
            textBgColorIndex += 1;
            if (textBgColorIndex >= textColorSrcId.length)
                textBgColorIndex = 0;
            ((ImageButton) findViewById(R.id.change_bg_txt_color)).setImageResource(textColorSrcId[textBgColorIndex]);
            mEditor.setTextBackgroundColor(textColors[textBgColorIndex]);
        });

        findViewById(R.id.action_txt_color).setOnClickListener(v -> mEditor.setTextColor(textColors[textColorIndex]));

        findViewById(R.id.action_bg_color).setOnClickListener(v -> mEditor.setTextBackgroundColor(textColors[textBgColorIndex]));

        findViewById(R.id.action_indent).setOnClickListener(v -> mEditor.setIndent());

        findViewById(R.id.action_outdent).setOnClickListener(v -> mEditor.setOutdent());

        findViewById(R.id.action_align_left).setOnClickListener(v -> mEditor.setAlignLeft());

        findViewById(R.id.action_align_center).setOnClickListener(v -> mEditor.setAlignCenter());

        findViewById(R.id.action_align_right).setOnClickListener(v -> mEditor.setAlignRight());

        findViewById(R.id.action_blockquote).setOnClickListener(v -> mEditor.setBlockquote());

        findViewById(R.id.action_insert_bullets).setOnClickListener(v -> mEditor.setBullets());

        findViewById(R.id.action_insert_numbers).setOnClickListener(v -> mEditor.setNumbers());

        findViewById(R.id.action_insert_image).setOnClickListener(v -> dialog.showImg(imgUrl));

        findViewById(R.id.action_insert_audio).setOnClickListener(v -> mEditor.insertAudio(mp3Url));

        findViewById(R.id.action_insert_video).setOnClickListener(v -> dialog.showVideo(videoUrl));

        findViewById(R.id.action_insert_link).setOnClickListener(v -> dialog.showLink(getContentString(), link_url));

        findViewById(R.id.action_insert_checkbox).setOnClickListener(v -> mEditor.insertTodo());

        findViewById(R.id.saveImg).setOnClickListener(v -> dialog.select((dialogInterface, i) -> {
            if (i == 0) {
                memoFileManager.createFile("text/html", "a.html", true);
            } else {
                memoFileManager.createFile("text/plain", "a.txt", false);
            }
        }, new String[]{"html", "text"}, "choose type"));

        findViewById(R.id.loadImg).setOnClickListener(v -> dialog.select((dialogInterface, i) -> {
            if (i == 0) {
                memoFileManager.openFile("text/html", true);
            } else {
                memoFileManager.openFile("text/plain", false);
            }
        }, new String[]{"html", "text"}, "choose type"));

        findViewById(R.id.new_file).setOnClickListener(view -> dialog.checkBox((dialog, which) -> clearNote(), (dialog, which) -> {
            // DO NOTHING HERE
        }, "New file log", "Clear the editor?"));

        findViewById(R.id.hideImg).setOnClickListener(v -> {
            hiding = !hiding;
            if (hiding) {
                hideMsg();
            } else {
                showMsg();
            }
        });
    }

    private void modeChange() {
        editorMode = (editorMode + 1) % editorModeSize;
        switch (editorMode) {
            case EDITOR:
                editorModeActions();
                break;
            case VIEW:
                viewModeActions();
                break;
            case DRAW:
                drawModeActions();
                break;
        }
        updateVisibilities();
    }

    private void drawModeActions() {
        mPhotoEditorView.setFocusable(true);
        ((ImageButton) (findViewById(R.id.mode_btn))).setImageResource(R.drawable.drawing);
        mPhotoEditor.setBrushDrawingMode(true);
    }

    private void viewModeActions() {
        mEditor.setInputEnabled(false);
        ((ImageButton) (findViewById(R.id.mode_btn))).setImageResource(R.drawable.v);
    }

    private void editorModeActions() {
        mEditor.setInputEnabled(true);
        ((ImageButton) (findViewById(R.id.mode_btn))).setImageResource(R.drawable.e);
    }

    protected void updateVisibilities() {
        Set<Integer> ids = visibleTable.keySet();
        for (int id : ids) {
            int[] visibilities = visibleTable.get(id);
            if (visibilities != null) {
                int visibility = visibilities[editorMode];
                findViewById(id).setVisibility(visibility);
            }
        }
    }

    /* basic */
    public static String mMode;
    public static String mUser;
    private List<Button> mButtons;

    public void hideMsg() {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) findViewById(R.id.editor_view).getLayoutParams();
        ((ImageButton) findViewById(R.id.hideImg)).setImageResource(R.drawable.to_show);
        params.topMargin = margin[marginNewIndex];
        findViewById(R.id.editor_view).setLayoutParams(params);
    }

    public void showMsg() {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) findViewById(R.id.editor_view).getLayoutParams();
        ((ImageButton) findViewById(R.id.hideImg)).setImageResource(R.drawable.to_hide);
        params.topMargin = margin[marginOriginIndex];
        findViewById(R.id.editor_view).setLayoutParams(params);
    }

    public void sendImg() {

    }

    @SuppressLint("SetTextI18n")
    public void init(Intent intent) {


        MemoInfo memoInfo = intent.getParcelableExtra(MainActivity.MEMO_EXTRA);
        if (memoInfo != null) {
            mMode = memoInfo.type == MainActivity.MEMO_HOST ? "Host" : "Client";
            mUser = memoInfo.user;
        }

        Log.e(TAG, mMode + " created");

        TextView title = findViewById(R.id.textViewTitle);
        title.setText("Hi " + mUser + ", you are running as " + mMode + " Mode");

        TextView textView = findViewById(R.id.textViewStatus);
        textView.setMovementMethod(new ScrollingMovementMethod());
        textView.setTextSize(10);
        textView.setText(getPrefix() + "start\n");

        View.OnFocusChangeListener ofcListener = new EditorActivity.MemoFocusChangeListener();
        mEditor.setOnFocusChangeListener(ofcListener);

        findAllButtons();
    }

    protected void findAllButtons() {
        mButtons = new ArrayList<>();
        mButtons.add(findViewById(R.id.write_to));
        mButtons.add(findViewById(R.id.clear));
        mButtons.add(findViewById(R.id.start_relay));
        mButtons.add(findViewById(R.id.stop_relay));
        mButtons.add(findViewById(R.id.show_list));
    }

    protected void setAllButtonView(int v) {
        for (Button btn : mButtons)
            btn.setVisibility(v);
    }


    private class MemoFocusChangeListener implements View.OnFocusChangeListener {
        public void onFocusChange(View v, boolean hasFocus) {
            //log("touch");
            if (v.getId() == R.id.editorX && !hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (null != imm)
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

    /*  utils */
    enum MEMO_SET_TYPE {
        MEMO_TEXT_SET,
        MEMO_TEXT_APPEND
    }

    public void force_sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void log(String message) {
        Log.e(TAG, String.format("%s %s", getPrefix(), message));
    }

    public String getTime() {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
        return now.format(new Date());
    }

    public String getUser() {
        return "[" + mUser + "] ";
    }

    public String getMode() {
        return "[" + mMode + "] ";
    }

    public String getPrefix() {
        return getTime() + getUser() + getMode();
    }

    public void clearText(View v) {
        updateStatusText(getPrefix() + "reset\n", MEMO_SET_TYPE.MEMO_TEXT_SET);
        updateEditText("", MEMO_SET_TYPE.MEMO_TEXT_SET);
        resetStatusTextCursor();
    }

    public void resetStatusTextCursor() {
        TextView tv = findViewById(R.id.textViewStatus);
        tv.scrollTo(0, 0);
    }

    public void updateStatusText(String msg, EditorActivity.MEMO_SET_TYPE type) {
        TextView tv = findViewById(R.id.textViewStatus);
        if (tv == null) {
            log("can't find target to update");
            return;
        }

        synchronized (this) {
            try {
                switch (type) {
                    case MEMO_TEXT_SET:
                        tv.setText(msg);
                        break;
                    case MEMO_TEXT_APPEND:
                        tv.append(msg);
                        break;
                }
                log("update status: " + type + " " + msg);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
                log("index out of bound\n");
                tv.setText("");
                tv.append(msg);
            }
        }
    }

    public void updateEditText(String msg, EditorActivity.MEMO_SET_TYPE type) {
        if (mEditor == null)
            return;

        switch (type) {
            case MEMO_TEXT_SET:
            case MEMO_TEXT_APPEND:
                mEditor.setHtml(msg);
                break;
        }
        log("update editor: " + type + " " + msg);
    }

    public String getStatusText() {
        TextView tv = findViewById(R.id.textViewStatus);
        if (tv == null)
            return "";
        return tv.getText().toString();
    }

    public String getEditText() {
        return mEditor.getHtml();
    }

    private void clearNote() {
        mEditor.setHtml("");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null)
            return;
        Uri uri = data.getData();
        if (uri == null)
            return;
        requestHandler.handleRequestCode(requestCode, uri);
    }
}
