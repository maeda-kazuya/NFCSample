
package jp.co.se.androidnfc.chapter03;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jp.co.se.androidnfc.chapter03.R;

import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class ReadWriteActivity extends BaseActivity {
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readwrite);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
       if (mNfcAdapter != null) {
            // 起動中のアクティビティが優先的にNFCを受け取れるよう設定
            Intent intent = new Intent(this, this.getClass())
                    .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                    intent, 0);
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
       if (mNfcAdapter != null) {
            // アクテイビティが非表示になる際に優先的にNFCを受け取る設定を解除
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // UIDを取得
            byte[] uid = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);

            // UIDを文字列に変換して表示
            TextView tvRead = (TextView) findViewById(R.id.Read);
            tvRead.setText(NfcUtil.bytesToHex(uid));

            Switch swWrite = (Switch) findViewById(R.id.SwitchWrite);
            if (swWrite.isChecked()) {
                // NFCタグ情報を取得
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                if (tag != null) {
                    // 入力したテキストを取得
                    EditText etWrite = (EditText) findViewById(R.id.Write);
                    String ndefMsg = etWrite.getText().toString();
                    if (!TextUtils.isEmpty(ndefMsg)) {
                        // NdefRecordの作成
                        NdefRecord[] ndefRecords = new NdefRecord[] {
                                NdefRecord.createUri(ndefMsg),
                        };
                        // NdefMessageの作成
                        NdefMessage msg = new NdefMessage(ndefRecords);
                        write(tag, msg);
                    } else {
                        // 書き込むデータが存在しない場合はユーザーへ通知
                        Toast.makeText(this, getString(R.string.error_empty_uri),
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }
        }
    }

    /**
     * NFCタグに書き込む.
     * 
     * @param tag
     * @param msg
     */
    private void write(Tag tag, NdefMessage msg) {
        try {
            List<String> techList = Arrays.asList(tag.getTechList());
            // 書き込みを行うタグにNDEFデータが格納されているか確認
            if (techList.contains(Ndef.class.getName())) {
                // NDEFが含まれている場合
                Ndef ndef = Ndef.get(tag);
                try {
                    // そのままNDEFデータ上にNDEFメッセージを書き込む
                    ndef.connect();
                    ndef.writeNdefMessage(msg);
                } catch (IOException e) {
                    throw new RuntimeException(getString(R.string.error_connect), e);
                } catch (FormatException e) {
                    throw new RuntimeException(getString(R.string.error_format), e);
                } finally {
                    try {
                        ndef.close();
                    } catch (IOException e) {
                    }
                }
            } else if (techList.contains(NdefFormatable.class.getName())) {
                // NDEFFormatableが含まれている場合
                NdefFormatable ndeffmt = NdefFormatable.get(tag);
                try {
                    // そのままNDEFにフォーマットしつつNDEFメッセージを書き込む
                    ndeffmt.connect();
                    ndeffmt.format(msg);
                } catch (IOException e) {
                    throw new RuntimeException(getString(R.string.error_connect), e);
                } catch (FormatException e) {
                    throw new RuntimeException(getString(R.string.error_format), e);
                } finally {
                    try {
                        ndeffmt.close();
                    } catch (IOException e) {
                    }
                }
            }
            Toast.makeText(this, getString(R.string.write_success), Toast.LENGTH_SHORT).show();
        } catch (RuntimeException e) {
            Toast.makeText(this, getString(R.string.write_failure), Toast.LENGTH_SHORT).show();
        }
    }
}
