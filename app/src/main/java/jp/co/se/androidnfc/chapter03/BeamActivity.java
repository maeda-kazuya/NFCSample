
package jp.co.se.androidnfc.chapter03;

import java.nio.charset.Charset;
import java.util.Locale;

import jp.co.se.androidnfc.chapter03.R;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class BeamActivity extends BaseActivity {
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beam);

        // AndroidBeamの設定
        if (mNfcAdapter != null) {
            // NDEFメッセージを送信する際に呼ばれるコールバック
            mNfcAdapter.setNdefPushMessageCallback(new CreateNdefMessageCallback() {
                @SuppressLint("NewApi")
                @Override
                public NdefMessage createNdefMessage(NfcEvent event) {
                    // AndroidBeamで送信するメッセージを生成
                    EditText etWrite = (EditText) findViewById(R.id.Write);
                    String beamData = etWrite.getText().toString();
                    NdefMessage msg = new NdefMessage(new NdefRecord[] {
                            createMimeRecord("application/jp.co.se.androidnfc.chapter03",
                                    beamData.getBytes())
                    });
                    return msg;
                }
            }, this);
            // BeamでNDEFメッセージを送信したことを通知するリスナー
            mNfcAdapter.setOnNdefPushCompleteCallback(new OnNdefPushCompleteCallback() {
                @Override
                public void onNdefPushComplete(NfcEvent event) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BeamActivity.this, getString(R.string.beam_success),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }, this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ビームで受信したNDEFメッセージを取得
        Intent intent = getIntent();
        if (intent != null
                && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())
                && (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
            Parcelable[] rawMsgs = intent
                    .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage ndefMessage = (NdefMessage) rawMsgs[0];
            String message = new String(
                    ndefMessage.getRecords()[0].getPayload());
            TextView tvRecieve = (TextView) findViewById(R.id.Recieve);
            tvRecieve.setText(message);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    /**
     * Creates a custom MIME type encapsulated in an NDEF record
     */
    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }
}
