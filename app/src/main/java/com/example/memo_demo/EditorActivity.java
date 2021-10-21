package com.example.memo_demo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;

public class EditorActivity extends AppCompatActivity {

    public static final String TAG = "EditorActivity";
    protected RichEditor mEditor;
    private boolean hidding = false;
    int margin_origin_index = 0;
    int margin_new_index = 1;
    int margin[] = {0, 0};

    // color changers
    int text_color_index = 0;
    int text_bg_color_index = 6;
    int[] text_color_src_id = {R.color.BLACK, R.color.GREEN, R.color.BLUE, R.color.YELLOW,
            R.color.CYAN, R.color.RED, R.color.WHITE};
    int[] text_colors = {Color.BLACK, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.CYAN, Color.RED, Color.WHITE};

    // default test url
    private String mp3_url = "https://file-examples-com.github.io/uploads/2017/11/file_example_MP3_5MG.mp3";
    private String video_url = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_10MB.mp4";
    private String img_url = "https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg";
    private int img_width = 320;
    private int video_width = 360;
    protected Uri pasteUri;
    protected String pasteText;
    protected MemoFileManager memoFileManager = null;
    protected EditorRequestHandler requestHandler = null;
    protected Dialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        memoFileManager = new MemoFileManager(this);
        requestHandler = new EditorRequestHandler(this, memoFileManager);

        mEditor = findViewById(R.id.editor);
        mEditor.setPadding(10, 10, 10, 10);
        mEditor.setHtml("");
        dialog = new Dialog(this);
        imgBtnInit();
    }

    protected void updatePasteUri() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboardManager.getPrimaryClip();
        if (clip != null) {
            ClipData.Item item = clip.getItemAt(0);

            pasteUri = item.getUri();
            pasteText = (String) item.getText();
        }
    }

    protected String getPasteText() {
        return pasteText;
    }

    protected boolean copyToClipBoard(String str) {
        // TODO use it to complete clipboard feature
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("simple text", str);
        clipboardManager.setPrimaryClip(clipData);
        return false;
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


    private void imgBtnInit() {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) findViewById(R.id.editor_view).getLayoutParams();
        margin[margin_origin_index] = params.topMargin;
        findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.undo();
            }


        });

        findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.redo();
            }
        });

        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setBold();
            }
        });

        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setItalic();
            }
        });

        findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setSubscript();
            }
        });

        findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setSuperscript();
            }
        });

        findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setStrikeThrough();
            }
        });

        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mEditor.focusEditor();
                mEditor.setUnderline();
            }
        });

        findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setHeading(1);
            }
        });

        findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setHeading(2);
            }
        });

        findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setHeading(3);
            }
        });

        findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setHeading(4);
            }
        });

        findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setHeading(5);
            }
        });

        findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(6);
            }
        });
        findViewById(R.id.change_txt_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_color_index += 1;
                if (text_color_index >= text_color_src_id.length) {
                    text_color_index = 0;
                }
                ((ImageButton) findViewById(R.id.change_txt_color)).setImageResource(text_color_src_id[text_color_index]);
                mEditor.setTextColor(text_colors[text_color_index]);
            }
        });
        findViewById(R.id.change_bg_txt_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text_bg_color_index += 1;
                if (text_bg_color_index >= text_color_src_id.length)
                    text_bg_color_index = 0;
                ((ImageButton) findViewById(R.id.change_bg_txt_color)).setImageResource(text_color_src_id[text_bg_color_index]);
                mEditor.setTextBackgroundColor(text_colors[text_bg_color_index]);
            }
        });

        findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mEditor.setTextColor(text_colors[text_color_index]);
            }
        });

        findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override
            public void onClick(View v) {
                mEditor.setTextBackgroundColor(text_colors[text_bg_color_index]);
            }
        });

        findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setIndent();
            }
        });

        findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setOutdent();
            }
        });

        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignLeft();
            }
        });

        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignCenter();
            }
        });

        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignRight();
            }
        });

        findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBlockquote();
            }
        });

        findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBullets();
            }
        });

        findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setNumbers();
            }
        });

        findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertImage(img_url, "dachshund", img_width);
            }
        });

        findViewById(R.id.action_insert_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertAudio(mp3_url);
            }
        });

        findViewById(R.id.action_insert_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertVideo(video_url, video_width);
            }
        });

        findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.showLinkDialog();
            }
        });
        findViewById(R.id.action_insert_checkbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertTodo();
            }
        });
        findViewById(R.id.saveImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                memoFileManager.createFile("text/html", "a.html", true);
            }
        });
        findViewById(R.id.saveTxtImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                memoFileManager.createFile("text/plain", "a.txt", false);
            }
        });
        findViewById(R.id.loadImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memoFileManager.openFile("text/html", true);
            }
        });
        findViewById(R.id.loadTxtImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memoFileManager.openFile("text/plain", false);
            }
        });

        findViewById(R.id.new_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.checkDialog("OK", "cancel", "New file log", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clearNote();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // DO NOTHING HERE
                    }
                });
            }
        });

        findViewById(R.id.hideImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidding = !hidding;
                if (hidding) {
                    ((ImageButton) findViewById(R.id.hideImg)).setImageResource(R.drawable.to_show);
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) findViewById(R.id.editor_view).getLayoutParams();
                    params.topMargin = margin[margin_new_index];
                    findViewById(R.id.editor_view).setLayoutParams(params);
                } else {
                    ((ImageButton) findViewById(R.id.hideImg)).setImageResource(R.drawable.to_hide);
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) findViewById(R.id.editor_view).getLayoutParams();
                    params.topMargin = margin[margin_origin_index];
                    findViewById(R.id.editor_view).setLayoutParams(params);
                }
            }
        });
    }

    /* basic */
    public static String mMode;
    public static String mUser;
    private List<Button> mButtons;


    public void init(Intent intent) {
        Log.e(TAG, "created");

        MemoInfo memoInfo = intent.getParcelableExtra(MainActivity.MEMO_EXTRA);
        mMode = memoInfo.type == MainActivity.MEMO_HOST ? "Host" : "Client";
        mUser = memoInfo.user;

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
        mButtons.add((Button) findViewById(R.id.write_to));
        mButtons.add((Button) findViewById(R.id.clear));
        mButtons.add((Button) findViewById(R.id.start_relay));
        mButtons.add((Button) findViewById(R.id.stop_relay));
        mButtons.add((Button) findViewById(R.id.show_list));
    }

    protected void setAllButtonView(int v) {
        for (Button btn : mButtons)
            btn.setVisibility(v);
    }


    private class MemoFocusChangeListener implements View.OnFocusChangeListener {
        public void onFocusChange(View v, boolean hasFocus) {
            log("touch");
            if (v.getId() == R.id.editor && !hasFocus) {
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

    ;

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
        SimpleDateFormat now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
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
                mEditor.setHtml(msg);
                break;
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
        if (mEditor != null)
            return mEditor.getHtml();
        return "";
    }

    private void clearNote() {
        mEditor.setHtml("");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK || data == null)
            return;
        Uri uri = data.getData();
        if (uri == null)
            return;
        requestHandler.handleRequestCode(requestCode, uri);
    }
}
