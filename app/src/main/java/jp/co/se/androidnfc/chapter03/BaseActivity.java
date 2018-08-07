
package jp.co.se.androidnfc.chapter03;

import jp.co.se.androidnfc.chapter03.R;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends Activity {
    protected NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // NFCを扱うためのインスタンスを取得
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // NFCが搭載されているかチェック
        if (mNfcAdapter != null) {
            // NFC機能が有効になっているかチェック
            if (!mNfcAdapter.isEnabled()) {
                // NFC機能が無効の場合はユーザーへ通知
                Toast.makeText(getApplicationContext(), getString(R.string.error_nfc_disable),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // NFC非搭載の場合はユーザーへ通知
            Toast.makeText(getApplicationContext(), getString(R.string.error_nfc_nosupport),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // メニューアイテムを生成
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_readwrite) {
            finish();
            startActivity(new Intent(this, ReadWriteActivity.class));
        } else if (itemId == R.id.action_beam) {
            finish();
            startActivity(new Intent(this, BeamActivity.class));
        } else if (itemId == R.id.action_settings) {
            // 設定画面を開く
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                // Android.4.1以上であれば直接NFC設定を呼び出すIntentアクションを利用
                startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
            } else {
                // Android.4.0以下はワイヤレス設定を呼び出すIntentアクションを利用
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
