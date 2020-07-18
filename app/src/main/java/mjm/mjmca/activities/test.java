package mjm.mjmca.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import mjm.mjmca.R;

public class test extends AppCompatActivity {
    EmojiconEditText etEmoji;
    ImageView imgEmojiBtn;
    RelativeLayout rlRootView;
    EmojIconActions emojAction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        etEmoji = (EmojiconEditText) findViewById(R.id.edt_emoji);
        imgEmojiBtn = (ImageView) findViewById(R.id.img_emoji_btn);
        rlRootView = (RelativeLayout) findViewById(R.id.rl_root);
        emojAction = new EmojIconActions(this, rlRootView, etEmoji, imgEmojiBtn);
        emojAction.ShowEmojIcon();
        emojAction.setIconsIds(R.drawable.ic_action_keyboard,R.drawable.smiley);
        emojAction.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
            }
            @Override
            public void onKeyboardClose() {
            }
        });

    }
}
