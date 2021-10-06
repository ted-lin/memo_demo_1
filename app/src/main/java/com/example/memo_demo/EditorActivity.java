package com.example.memo_demo;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.example.protos.Data;
import com.google.protobuf.InvalidProtocolBufferException;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import jp.wasabeef.richeditor.RichEditor;

public class EditorActivity extends AppCompatActivity {
    public static final String TAG = "EditorActivity";
    private RichEditor mEditor;
    private Button htmlBtn;
    private Button loadHtmlBtn;
    private Button loadProtoToHtml;
    private Button imageBtn;
    private Button htmlToProtoBtn;
    private TextView textView;
    private TextView lastTextView;
    private String lastString = "";
    private TextHelper mTextHelper;
    private String txtFileName = "a.txt";
    private String htmlFileName = "a.html";
    byte[] save;
    String html;
    private int mFontSize = 5;
    private View.OnClickListener imageInsert = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            getExternalFilesDir(null);
            mEditor.insertImage("file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/s1.png", "", 400, 500);
        }
    };
    private View.OnClickListener protoToHtml = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                Data.EditorMessage editorMessage = Data.EditorMessage.parseFrom(save);
//                editorMessage
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
                return isPlain ? mTextHelper.toHtml(fileContents) : fileContents;
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
            String str = mTextHelper.toPlainTxt(lastString);
            save_file(str, txtFileName);
        }
    };
    private View.OnClickListener loadTxt = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String newString = load_file(txtFileName, true);
            if (newString != "") {
                mEditor.setHtml(newString);
                lastString = newString;
            }
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
//                dmp.diffCleanupSemantic(diff);
//                dmp.diffCleanupEfficiency(diff);
                lastString = text;
                textView.setText(lastString);
                lastTextView.setText(patch.toString());

            }
        });

        textView = findViewById(R.id.hello);
        lastTextView = findViewById(R.id.last);
        findViewById(R.id.btnHtml).setOnClickListener(getHtml);
        findViewById(R.id.btnImg).setOnClickListener(imageInsert);
        findViewById(R.id.btnProto).setOnClickListener(protoToHtml);
        findViewById(R.id.btnToProto).setOnClickListener(htmlToProto);
        mEditor.setPadding(10, 10, 10, 10);
        findViewById(R.id.btnSaveTxt).setOnClickListener(saveTxt);
        findViewById(R.id.btnSaveHtml).setOnClickListener(saveHtml);
        findViewById(R.id.btnLoadTxt).setOnClickListener(loadTxt);
        findViewById(R.id.btnLoadHtml).setOnClickListener(loadHtml);

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
        Data.EditorMessage.newBuilder().setHtml("");
        byte[] b = Data.EditorMessage.newBuilder()
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

    private boolean saveToTxt() {
        return false;
    }



}
