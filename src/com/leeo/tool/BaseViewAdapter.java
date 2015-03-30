package com.leeo.tool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * Created by leeo on 3/4/15.
 */
public abstract class BaseViewAdapter<T> extends BaseAdapter {
    protected List<T> dataList;
    private LayoutInflater inflater;

    public BaseViewAdapter(Context context,List<T> dataList){
        inflater = LayoutInflater.from(context);
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        if(dataList == null ){
            return 0;
        }
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        if(null == dataList) return null;
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseViewHolder baseViewHolder = null;
        if(convertView == null){
            convertView = inflater.inflate(getItemViewId(),null);
            baseViewHolder = initViewHolder(convertView);
            convertView.setTag(baseViewHolder);
        }else{
            baseViewHolder = (BaseViewHolder) convertView.getTag();
        }
        setContentView(position, baseViewHolder,dataList.get(position));
        return convertView;
    }

    /**
     * 获取ConvertView
     * @return
     */
    protected abstract int getItemViewId();
    /**
     * 初始化ViewHolder,把ViewHolder相关的属性进行赋值在这个方法里面进行
     * @param itemView
     * @return
     */
    protected abstract BaseViewHolder initViewHolder(View itemView);
    /**
     * 这里主要对contentview的Item进行处理，相关的事件，逻辑都在这里进行
     * @param position
     * @param baseViewHolder
     */
    protected abstract void setContentView(int position, BaseViewHolder baseViewHolder,T data);

    //持有Item相关的类
    public static class BaseViewHolder{

    }
}
