package com.example.gradeview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.view.GradeView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private int[] grade = {0, 2000, 3500, 4500, 5000};
    private String[] gradeName = {"普通", "白银", "黄金", "铂金", "钻石"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final GradeView gradeView = findViewById(R.id.grade);
        gradeView.setGradeNameAndGradeAndTip(gradeName, grade, 1000);
        findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                int ran = random.nextInt(5000);
                Log.i(TAG, "生成的随机数：" + ran);
                gradeView.setCurrentGrade(ran);
            }
        });
    }
}
