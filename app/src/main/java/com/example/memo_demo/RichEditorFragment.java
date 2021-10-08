package com.example.memo_demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.protos.Data;
import com.google.protobuf.InvalidProtocolBufferException;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RichEditorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RichEditorFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RichEditorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment richeditor.
     */
    // TODO: Rename and change types and number of parameters
    public static RichEditorFragment newInstance(String param1, String param2) {
        RichEditorFragment fragment = new RichEditorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_richeditor, container, false);
        mEditor = rootView.findViewById(R.id.editor);
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
//                textView.setText(lastString);
//                lastTextView.setText(patch.toString());

            }
        });
        mEditor.setPadding(10, 10, 10, 10);

        //mEditor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg");
        mEditor.setHtml("");
        imgBtnInit(rootView);
        return rootView;
    }

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

            save_file(v, str, htmlFileName);
        }
    };

    private String load_file(View view, String file_name, boolean isPlain) {
        File docDir = view.getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
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

    private void save_file(View v, String str, String file_name) {
        File docDir = v.getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
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
            save_file(v, str, txtFileName);
        }
    };
    private View.OnClickListener loadTxt = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String newString = load_file(v, txtFileName, true);
            mEditor.setHtml(newString);
            lastString = newString;
        }
    };
    private View.OnClickListener loadHtml = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String newString = load_file(v, htmlFileName, false);
            if (newString != "") {
                mEditor.setHtml(newString);
                lastString = newString;
            }
        }
    };

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }

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


    private void imgBtnInit(View view) {
        view.findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.undo();
            }


        });

        view.findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.redo();
            }
        });

        view.findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setBold();
            }
        });

        view.findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setItalic();
            }
        });

        view.findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setSubscript();
            }
        });

        view.findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setSuperscript();
            }
        });

        view.findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setStrikeThrough();
            }
        });

        view.findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mEditor.focusEditor();
                mEditor.setUnderline();
            }
        });

        view.findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setHeading(1);
            }
        });

        view.findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setHeading(2);
            }
        });

        view.findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setHeading(3);
            }
        });

        view.findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setHeading(4);
            }
        });

        view.findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.focusEditor();
                mEditor.setHeading(5);
            }
        });

        view.findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(6);
            }
        });

        view.findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override
            public void onClick(View v) {
                mEditor.setTextColor(isChanged ? Color.BLACK : Color.RED);
                isChanged = !isChanged;
            }
        });

        view.findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
            private boolean isChanged;

            @Override
            public void onClick(View v) {
                mEditor.setTextBackgroundColor(isChanged ? Color.TRANSPARENT : Color.YELLOW);
                isChanged = !isChanged;
            }
        });

        view.findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setIndent();
            }
        });

        view.findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setOutdent();
            }
        });

        view.findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignLeft();
            }
        });

        view.findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignCenter();
            }
        });

        view.findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignRight();
            }
        });

        view.findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBlockquote();
            }
        });

        view.findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBullets();
            }
        });

        view.findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setNumbers();
            }
        });

        view.findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertImage("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg",
                        "dachshund", 320);
            }
        });

        view.findViewById(R.id.action_insert_youtube).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertYoutubeVideo("https://www.youtube.com/embed/pS5peqApgUA");
            }
        });

        view.findViewById(R.id.action_insert_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertAudio("https://file-examples-com.github.io/uploads/2017/11/file_example_MP3_5MG.mp3");
            }
        });

        view.findViewById(R.id.action_insert_video).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertVideo("https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_10MB.mp4", 360);
            }
        });

        view.findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLinkDialog(v);
            }
        });
        view.findViewById(R.id.action_insert_checkbox).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.insertTodo();
            }
        });
        view.findViewById(R.id.saveImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_file(v, mEditor.getHtml(), htmlFileName);
            }
        });
        view.findViewById(R.id.loadImg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                load_file(view, htmlFileName, false);
            }
        });
    }

    private void showLinkDialog(View v) {
        // TODO need to fix link problem
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
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
}