package com.example.memo_demo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.protos.Data;
import com.google.protobuf.InvalidProtocolBufferException;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;

public class EditorActivity extends AppCompatActivity {
    public static final String TAG = "EditorActivity";
    private static final int WRITE_REQUEST_CODE_HTML = 43;
    private static final int WRITE_REQUEST_CODE = 42;
    private static final int READ_REQUEST_CODE_HTML = 41;
    private static final int READ_REQUEST_CODE = 40;
    private RichEditor mEditor;
    private WebView webView;
    private TextView textView;
    private TextView lastTextView;
    private String lastString = "";
    private String txtFileName = "a.txt";
    private String htmlFileName = "a.html";
    byte[] save;
    String html;
    private int mFontSize = 5;
    int text_color_index = 0;
    int text_bg_color_index = 6;
    int[] text_color_src_id = {R.color.BLACK, R.color.GREEN, R.color.BLUE, R.color.YELLOW,
            R.color.CYAN, R.color.RED, R.color.WHITE};
    int[] text_colors = {Color.BLACK, Color.GREEN, Color.BLUE, Color.YELLOW,
            Color.CYAN, Color.RED, Color.WHITE};
    private View.OnClickListener imageInsert = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            mEditor.getSettings().setAllowContentAccess(true);
            mEditor.getSettings().setAllowFileAccessFromFileURLs(true);
            mEditor.setOnTextChangeListener(null);
//            mEditor.insertImage("file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/s1.png", "", 400, 500);
        }
    };
    private View.OnClickListener protoToHtml = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                Data.EditorMessage editorMessage = Data.EditorMessage.parseFrom(save);
                String html = editorMessage.getHtml();
                Boolean isFullHtml = editorMessage.getIsFullHtml();
                if (isFullHtml) {
                    mEditor.setHtml(html);
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    };
    private View.OnClickListener htmlToProto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String _html = mEditor.getHtml();
            Data.EditorMessage editorMessage = Data.EditorMessage.newBuilder()
                    .setHtml(_html)
                    .setIsFullHtml(true)
                    .build();
            save = editorMessage.toByteArray();
        }
    };
    private View.OnClickListener saveHtml = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String str = lastString;

            save_file(str, htmlFileName);
        }
    };

    private String load_file(String file_name, boolean isPlain) {
        File docDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        String extStorageState = Environment.getExternalStorageState();
        if (extStorageState.equals(Environment.MEDIA_MOUNTED)) {
            File inPutFile = new File(docDir, file_name);
            StringBuilder stringBuilder = new StringBuilder();
            try {
                FileReader fr = new FileReader(inPutFile);
                BufferedReader br = new BufferedReader(fr);
                String line = br.readLine();
                while (line != null) {
                    stringBuilder.append(line).append('\n');
                    line = br.readLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                String fileContents = stringBuilder.toString();
                return isPlain ? TextHelper.toHtml(fileContents) : fileContents;
            }
        } else {
            return "";
        }
    }

    private void save_file(String str, String file_name) {
        File docDir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        String extStorageState = Environment.getExternalStorageState();
        if (extStorageState.equals(Environment.MEDIA_MOUNTED)) {
            File outPutFile = new File(docDir, file_name);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(outPutFile);
                byte[] bytes;
                bytes = str.getBytes();
                fos.write(bytes);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // TODO
        }
    }

    private View.OnClickListener getHtml = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            html = mEditor.getHtml();
            textView.setText(html);
        }
    };
    private View.OnClickListener saveTxt = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String str = TextHelper.toPlainTxt(lastString);
            save_file(str, txtFileName);
        }
    };
    private View.OnClickListener loadTxt = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String newString = load_file(txtFileName, true);
            mEditor.setHtml(newString);
            lastString = newString;
        }
    };
    private View.OnClickListener loadHtml = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String newString = load_file(htmlFileName, false);
            if (newString != "") {
                mEditor.setHtml(newString);
                lastString = newString;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        mEditor = findViewById(R.id.editor);
        mEditor.setEditorHeight(300);
        mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
            DiffMatchPatch dmp = new DiffMatchPatch();
            LinkedList<DiffMatchPatch.Diff> diff;
            LinkedList<DiffMatchPatch.Patch> patch;

            @Override
            public void onTextChange(String text) {
                diff = dmp.diffMain(lastString, text);
                patch = dmp.patchMake(diff);
                lastString = text;

            }
        });

        mEditor.setPadding(10, 10, 10, 10);

        //mEditor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg");
        mEditor.setHtml("");
        imgBtnInit();
    }

    public void recievePatch(byte[] patch) throws InvalidProtocolBufferException {

        Data.EditorMessage editorMessage = Data.EditorMessage.parseFrom(patch);
    }

    public void receiveEditorMsg(byte[] proto) {
        Data.EditorMessage editorMessage;
        try {
            editorMessage = Data.EditorMessage.parseFrom(proto);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return;
        }
        if (editorMessage.getIsFullHtml()) {
            mEditor.setHtml(editorMessage.getHtml());
        } else {
            DiffMatchPatch dmp = new DiffMatchPatch();
            String patchText = editorMessage.getPatchText();
            List<DiffMatchPatch.Patch> patches;
            patches = dmp.patchFromText(patchText);
            this.apply_patch(patches);
        }
    }

    public void apply_patch(List<DiffMatchPatch.Patch> patches) {
        DiffMatchPatch dmp = new DiffMatchPatch();
        Object[] patch_result = dmp.patchApply((LinkedList<DiffMatchPatch.Patch>) patches, mEditor.getHtml());
        if ((Boolean) patch_result[1]) {
            // TODO need to check
            lastString = (String) patch_result[0];
            textView.setText(lastString);
            mEditor.setHtml(lastString);

        } else {
            // TODO apply patch failed
        }

    }

    public void sendPatch(String patchText) {
        byte[] bytes = Data.EditorMessage.newBuilder()
                .setHtml("")
                .setIsFullHtml(false)
                .setPatchText(patchText).build().toByteArray();
        // Todo Send
    }

    private String buildPatchText(String origin, String newStr) {
        DiffMatchPatch dmp = new DiffMatchPatch();
        LinkedList<DiffMatchPatch.Patch> patches;
        patches = dmp.patchMake(origin, newStr);
        String patchText = dmp.patchToText(patches);
        return patchText;
    }


    private void imgBtnInit() {
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
                mEditor.insertImage("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg",
                        "dachshund", 320);
            }
        });

        findViewById(R.id.action_insert_youtube).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertYoutubeVideo("https://www.youtube.com/embed/pS5peqApgUA");
            }
        });

        findViewById(R.id.action_insert_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertAudio("https://file-examples-com.github.io/uploads/2017/11/file_example_MP3_5MG.mp3");
            }
        });

        findViewById(R.id.action_insert_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertVideo("https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_10MB.mp4", 360);
            }
        });

        findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLinkDialog();
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
                createFile("text/html", htmlFileName, true);
            }
        });
        findViewById(R.id.saveTxtImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFile("text/plain", txtFileName, false);
            }
        });
        findViewById(R.id.loadImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFile("text/html", true);
            }
        });
        findViewById(R.id.loadTxtImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFile("text/plain", false);
            }
        });
        findViewById(R.id.new_file).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertCheck();
            }
        });
    }

    private void showLinkDialog() {
        // TODO need to fix link problem
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        View view = getLayoutInflater().inflate(R.layout.dialog_link, null, false);
        final EditText editText = view.findViewById(R.id.edit);
        builder.setView(view);
        builder.setTitle("Insert link");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String link = editText.getText().toString().trim();
                if (TextUtils.isEmpty(link)) {

                    return;
                }
                mEditor.insertLink("https://github.com/wasabeef", link);
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // DO NOTHING HERE
            }
        });

        builder.create().show();
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
        textView.setTextSize(16);
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
    }

    public void updateStatusText(String msg, EditorActivity.MEMO_SET_TYPE type) {
        TextView tv = findViewById(R.id.textViewStatus);
        if (tv == null) {
            log("can't find target to update");
            return;
        }

        switch (type) {
            case MEMO_TEXT_SET:
                tv.setText(msg);
                break;
            case MEMO_TEXT_APPEND:
                tv.append(msg);
                break;
        }
        log("update status: " + type + " " + msg);
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

    private void alertCheck() {
        // TODO need to fix link problem
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);

        View view = getLayoutInflater().inflate(R.layout.dialog_new, null, false);
        builder.setView(view);
        builder.setTitle("New file");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mEditor.setHtml("");
                lastString = "";
//                String link = editText.getText().toString().trim();
//                if (TextUtils.isEmpty(link)) {
//
//                    return;
//                }
//                mEditor.insertLink("https://github.com/wasabeef", link);
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // DO NOTHING HERE
            }
        });

        builder.create().show();
    }

    private void openFile(String mimeType, boolean isHtml) {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        if (isHtml) {
            startActivityForResult(intent, READ_REQUEST_CODE_HTML);
        } else {
            startActivityForResult(intent, READ_REQUEST_CODE);
        }
    }

    private void createFile(String mimeType, String fileName, boolean isHtml) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        if (isHtml) {
            startActivityForResult(intent, WRITE_REQUEST_CODE_HTML);
        } else {
            startActivityForResult(intent, WRITE_REQUEST_CODE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case WRITE_REQUEST_CODE: {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        try {
                            FileOutputStream fos = null;
                            byte[] bytes;
                            fos = (FileOutputStream) getContentResolver().openOutputStream(uri);
                            bytes = mEditor.getHtml().getBytes();
                            fos.write(bytes);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // TODO
                    }
                }
                break;
            }
            case WRITE_REQUEST_CODE_HTML: {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        try {
                            FileOutputStream fos = null;
                            byte[] bytes;
                            fos = (FileOutputStream) getContentResolver().openOutputStream(uri);
                            bytes = TextHelper.toPlainTxt(mEditor.getHtml()).getBytes();
                            fos.write(bytes);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // TODO
                    }
                }
                break;
            }
            case READ_REQUEST_CODE: {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        StringBuilder stringBuilder = new StringBuilder();
                        try {
                            FileInputStream fins = (FileInputStream) getContentResolver().openInputStream(uri);
                            byte[] b = new byte[1024];
                            while (fins.read(b) != -1) {
                                stringBuilder.append(new String(b));
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            String fileContents = stringBuilder.toString();
                            mEditor.setHtml(TextHelper.toHtml(fileContents));
                        }
                    } else {
                        // TODO
                    }
                    break;
                }
            }
            case READ_REQUEST_CODE_HTML: {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        StringBuilder stringBuilder = new StringBuilder();
                        try {
                            FileInputStream fins = (FileInputStream) getContentResolver().openInputStream(uri);
                            byte[] b = new byte[1024];
                            while (fins.read(b) != -1) {
                                stringBuilder.append(new String(b));
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            String fileContents = stringBuilder.toString();
                            mEditor.setHtml(fileContents);
                        }
                    } else {
                        // TODO
                    }
                    break;
                }
            }
        }
    }
}
