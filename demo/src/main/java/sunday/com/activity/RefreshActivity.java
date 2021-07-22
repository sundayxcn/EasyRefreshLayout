package sunday.com.activity;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import sunday.com.demo.R;

import com.sunday.easy.EasyRefreshLayout;
import com.sunday.easy.assist.ClassicsFootView;
import com.sunday.easy.assist.OnlyLoadingHeaderView;
import com.sunday.easy.assist.RotateHeaderView;


/**
 * Created by zhongfei.sun on 2018/1/24.
 */

public class RefreshActivity extends Activity{
    private EasyRefreshLayout mRefreshLayout;
    private ListView mListView;
    public static final String[] OP = {
            "RotateHeaderView,下拉看效果",
            "OnlyLoadingHeaderView,下拉看效果",
            "经典FooterView,上拉看效果"};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refresh_layout);
        mRefreshLayout = (EasyRefreshLayout) findViewById(R.id.refresh_layout);
//        mRefreshLayout.setRefreshListener(new RefreshListener() {
//            @Override
//            public void refresh() {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mRefreshLayout.finishRefresh(true);
//                    }
//                },2000);
//            }
//
//            @Override
//            public void loadMore() {
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        mRefreshLayout.finishLoadMore(true);
//                    }
//                },2000);
//            }
//        });
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        mRefreshLayout.setHeadView(new RotateHeaderView(getBaseContext()));
                        break;
                    case 1:
                        mRefreshLayout.setHeadView(new OnlyLoadingHeaderView(getBaseContext()));
                        break;
                    case 2:
                        mRefreshLayout.setFootView(new ClassicsFootView(getBaseContext()));
                        break;
                }
                Toast.makeText(getBaseContext(),"修改成功",Toast.LENGTH_SHORT).show();
            }
        });
        mListView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return OP.length;
            }

            @Override
            public Object getItem(int position) {
                return OP[position];
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if(view == null){
                    view = LayoutInflater.from(getBaseContext()).inflate(R.layout.item_text_setting,null,false);
                    Holder holder = new Holder();
                    holder.textView = (TextView) view.findViewById(R.id.title);
                    view.setTag(holder);
                }
                Holder holder = (Holder) view.getTag();
                holder.textView.setText(OP[position]);
                return view;
            }
        });
        final Button pullFull = (Button) findViewById(R.id.full_pull);
        pullFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRefreshLayout.isSupportFullPull()){
                    mRefreshLayout.setFullPull(false);
                    pullFull.setText("下拉只能滑到headerView显示(加载也可调整)");
                }else{
                    mRefreshLayout.setFullPull(true);
                    pullFull.setText("下拉可以全屏滑动(加载也可调整)");
                }
            }
        });
        final Button pullRate = (Button) findViewById(R.id.pull_rate);
        pullRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mRefreshLayout.getMoveRate() < 1f){
                    mRefreshLayout.setMoveRate(1f);
                    pullRate.setText("android默认滑动速度1f-值越大越快，默认0.3f");
                }else{
                    pullRate.setText("RefreshLayout默认滑动速度0.3f");
                    mRefreshLayout.setMoveRate(0.3f);
                }
            }
        });
    }

    public static class Holder{
        TextView textView;
    }
}
