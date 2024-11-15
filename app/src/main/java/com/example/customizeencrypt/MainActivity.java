package com.example.customizeencrypt;

import static com.tencent.trtc.TRTCCloudDef.TRTC_APP_SCENE_LIVE;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    protected static final int REQ_PERMISSION_CODE = 0x1000;
    // 权限个数计数，获取Android系统权限
    protected int mGrantedCount = 0;
    TXCloudVideoView txCloudVideoView;
    TXCloudVideoView remoteView;
    TRTCCloud mTRTCCloud;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        checkPermission();
        txCloudVideoView = findViewById(R.id.videoView);
        remoteView = findViewById(R.id.remote);
        mTRTCCloud = TRTCCloud.sharedInstance(this);
        findViewById(R.id.start).setOnClickListener(v -> {
            String encryptKey = ((EditText) findViewById(R.id.editText)).getText().toString();
            if (TextUtils.isEmpty(encryptKey)) {
                Toast.makeText(MainActivity.this, "请输入加密key", Toast.LENGTH_SHORT).show();
                return;
            }
            TRTCCloudDef.TRTCParams mTRTCParams = new TRTCCloudDef.TRTCParams();
            mTRTCParams.sdkAppId = GenerateTestUserSig.SDKAPPID;
            mTRTCParams.userId = Build.MODEL;
            mTRTCParams.roomId = Integer.parseInt("341212");
            mTRTCParams.userSig = GenerateTestUserSig.genTestUserSig(mTRTCParams.userId);
            mTRTCParams.role = TRTCCloudDef.TRTCRoleAnchor;
            TRTCCloudDef.TRTCAudioVolumeEvaluateParams evaluateParams =
                    new TRTCCloudDef.TRTCAudioVolumeEvaluateParams();
            evaluateParams.enableVadDetection = true;
            evaluateParams.interval = 110;
            mTRTCCloud.enableAudioVolumeEvaluation(true, evaluateParams);

            mTRTCCloud.startLocalPreview(false, txCloudVideoView);
            mTRTCCloud.startLocalAudio(TRTCCloudDef.TRTC_AUDIO_QUALITY_SPEECH);
            mTRTCCloud.enterRoom(mTRTCParams, TRTC_APP_SCENE_LIVE);
            mTRTCCloud.setListener(new TRTCCloudListener() {
                @Override
                public void onUserVoiceVolume(ArrayList<TRTCCloudDef.TRTCVolumeInfo> userVolumes, int totalVolume) {
                    super.onUserVoiceVolume(userVolumes, totalVolume);
                    Log.e("harry", "onUserVoiceVolume"+totalVolume );
                }

                @Override
                public void onUserVideoAvailable(String userId, boolean available) {
                    super.onUserVideoAvailable(userId, available);
                    if (available) {
                        mTRTCCloud.startRemoteView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, remoteView);
                    } else {
                        mTRTCCloud.stopRemoteView(userId);
                    }
                }
            });

         setEncodedDataProcessingListener(encryptKey);
        });
        findViewById(R.id.stop).setOnClickListener(v -> {
            mTRTCCloud.stopLocalAudio();
            mTRTCCloud.stopLocalPreview();
            mTRTCCloud.exitRoom();
        });
    }

    private void setEncodedDataProcessingListener(String encryptKey) {
        try {
            TRTCCustomerCrypt.sharedInstance().encryptKey = encryptKey;
            JSONObject params = new JSONObject().put("listener", TRTCCustomerCrypt.sharedInstance().getEncodedDataProcessingListener());
            JSONObject jsonApi = new JSONObject()
                    .put("api", "setEncodedDataProcessingListener")
                    .put("params", params);
            mTRTCCloud.callExperimentalAPI(jsonApi.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //////////////////////////////////    动态权限申请   ////////////////////////////////////////

    protected boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)) {
                permissions.add(android.Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)) {
                permissions.add(android.Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this,
                        permissions.toArray(new String[0]),
                        REQ_PERMISSION_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION_CODE:
                for (int ret : grantResults) {
                    if (PackageManager.PERMISSION_GRANTED == ret) {
                        mGrantedCount++;
                    }
                }
                if (mGrantedCount == permissions.length) {

                } else {
                    Toast.makeText(this, "用户没有允许需要的权限，加入通话失败", Toast.LENGTH_SHORT).show();
                }
                mGrantedCount = 0;
                break;
            default:
                break;
        }
    }
}