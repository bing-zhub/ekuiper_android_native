package com.github.bing_zhub;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    final static String FILENAME = "kuiperd";

    private static String fileDirectory = null;

    Process currentProcess;

    TextView tvIPAddress;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init(this);

        tvIPAddress =  (TextView) findViewById(R.id.ip_addr);
        tvIPAddress.setText("Local IP address: " + Utils.getIPAddress(true));


        File fileRootDire = new File(fileDirectory);
        if(!fileRootDire.exists()) fileRootDire.mkdir();
        createDirectories();
        copyFiles();
    }

    void createDirectories() {
        List<String> directories = Arrays.asList("data","log",
                "etc","etc/services","etc/services/schemas","etc/services/schemas/google","etc/services/schemas/google/api","etc/sources","etc/connections","etc/mgmt","etc/ops","etc/sinks","etc/multilingual",
                "plugins","plugins/sources","plugins/portable","plugins/wasm","plugins/functions","plugins/sinks");

        for(String dir : directories) {
            String dirPath = fileDirectory+ dir +"/";
            File tmpDir = new File(dirPath);
            if(tmpDir.exists()) continue ;
            try {
                tmpDir.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    void copyFiles() {
        copyFile("kuiperd", "kuiperd");
        copyFile("kuiper.yaml", "etc/kuiper.yaml");
        copyFile("mqtt_source.json", "etc/mqtt_source.json");
        copyFile("mqtt_source.yaml", "etc/mqtt_source.yaml");
    }

    public void copyFile(String resFilename, String relativePath) {
        //文件私有目录路径
        File file = new File(fileDirectory + relativePath);
        if (!file.exists()) {
            try {
                //输入流 获取assets里的文件
                InputStream sourceFile = getResources().getAssets().open(resFilename);
                //输出流
                FileOutputStream destFile =  new FileOutputStream(new File(fileDirectory  +relativePath));
                byte[] b = new byte[sourceFile.available()];
                sourceFile.read(b);
                destFile.write(b);
                //关闭
                sourceFile.close();
                destFile.close();
                Toast.makeText(this, "copy finished!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "file exist!", Toast.LENGTH_SHORT).show();
        }
    }

    public void chmod(View v) {
        try {
            execCommand("chmod 777 "+ fileDirectory + FILENAME);
        } catch (IOException e) {
            Toast.makeText(this, "exec failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void run(View v) {
        try {
            execCommandAsync(fileDirectory + FILENAME);
        } catch (IOException e) {
            Toast.makeText(this, "exec failed", Toast.LENGTH_SHORT).show();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void closeProcess(View v) {
        try {
            if (currentProcess.isAlive()) {
                currentProcess.destroy();
                currentProcess.waitFor();
                Toast.makeText(this, "Process closed with exit value " + currentProcess.exitValue(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Process closed!", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this, "Process cannot be closed!", Toast.LENGTH_SHORT).show();
        }
    }

    public void execCommandAsync(String command) throws IOException{
        Runtime runtime = Runtime.getRuntime();
        currentProcess = runtime.exec(command);
    }


    public void execCommand(String command) throws IOException{
        Runtime runtime = Runtime.getRuntime();
        currentProcess = runtime.exec(command);

        InputStream infoIS = currentProcess.getInputStream(); //获得执行信息
        InputStreamReader infoIsr = new InputStreamReader(infoIS);
        StringBuilder infoSb = new StringBuilder();
        for (int ch; (ch = infoIsr.read()) != -1; ) {
            infoSb.append((char) ch);
        }
        String info = infoSb.toString();
        Toast.makeText(this,  info, Toast.LENGTH_SHORT).show();

        InputStream errIS = currentProcess.getErrorStream(); //获得执行信息
        InputStreamReader errIsr = new InputStreamReader(errIS);
        StringBuilder errSb = new StringBuilder();
        for (int ch; (ch = errIsr.read()) != -1; ) {
            errSb.append((char) ch);
        }
        String err = errSb.toString();
        Toast.makeText(this,  err, Toast.LENGTH_SHORT).show();
    }

    public static void init(Context context) {
        fileDirectory = getFilePath(context) + "/";
    }


    private static String getFilePath(Context context) {
        return "/data/data/com.github.bing_zhub/files";
    }
    
    
    
}
