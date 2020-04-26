package com.zenchn.picbrowserlib.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.zenchn.picbrowserlib.R;
import com.zenchn.picbrowserlib.annotation.ImageSourceType;
import com.zenchn.picbrowserlib.pojo.ImageSourceInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author:Hzj
 * @date :2019/9/19/019
 * desc  ：RecyclerView 添加图片adapter
 * record：
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private static final String IMG_SIZE = "?x-oss-process=image/resize,m_fixed,h_300,w_0";

    private List<ImageSourceInfo> mData;
    private Context mContext;

    /***是否启用编辑模式，默认关闭***/
    private boolean mIsEditMode = false;
    /***最大图片数量，默认9***/
    private int mMaxNum = 9;

    public PhotoAdapter(List<ImageSourceInfo> data, Context context) {
        this(data, context, false, 9);
    }

    public PhotoAdapter(List<ImageSourceInfo> data, Context context, boolean isEditMode, int maxCount) {
        this.mData = data;
        this.mContext = context;
        this.mIsEditMode = isEditMode;
        this.mMaxNum = maxCount;
    }

    /**
     * 刷新数据
     *
     * @param newData
     */
    public void setNewData(List<ImageSourceInfo> newData) {
        this.mData = newData == null ? new ArrayList<>() : newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.grid_item_edit_photo, viewGroup, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int i) {
        int position = holder.getAdapterPosition();
        //加载图片
        RequestOptions requestOptions = new RequestOptions()
                .centerCrop()
                .error(R.drawable.photo_default)
                .placeholder(R.drawable.photo_default);

        if (mIsEditMode) {
            /***可编辑模式***/
            if (position >= mMaxNum) {
                //最大数目后的Item隐藏
                holder.convertView.setVisibility(View.GONE);
            } else {
                holder.convertView.setVisibility(View.VISIBLE);
                holder.convertView.setOnClickListener(v -> {
                    if (mOnPhotoEditableItemClickListener != null) {
                        if (position < mData.size()) {
                            //点击图片回调
                            mOnPhotoEditableItemClickListener.onItemClick(v, position, mData.get(position));
                        } else {
                            //新增图片回调
                            mOnPhotoEditableItemClickListener.onAddPhotoClick();
                        }
                    }
                });

                if (position < mData.size()) {
                    //正常的图片显示
                    holder.ibtDel.setVisibility(View.VISIBLE);
                    holder.ibtDel.setOnClickListener(v -> {
                        if (mOnPhotoEditableItemClickListener != null) {
                            //删除图片回调
                            ImageSourceInfo target = mData.get(position);
                            Iterator<ImageSourceInfo> iterator = mData.iterator();
                            int index = 0;
                            while (iterator.hasNext()) {
                                ImageSourceInfo next = iterator.next();
                                if (next == target) {
                                    iterator.remove();
                                    if (getItemCount() == mMaxNum) {
                                        notifyDataSetChanged();
                                    } else {
                                        notifyItemRemoved(index);
                                    }
                                }
                                index++;
                            }
                            mOnPhotoEditableItemClickListener.onPhotoDeleteClick(index, target);
                        }
                    });

                    //代表+号之前的需要正常显示图片
                    ImageSourceInfo imageSourceInfo = mData.get(position);
                    Object houseImg = null;
                    if (imageSourceInfo.getSourceType() == ImageSourceType.URL) {
                        //列表加载缩略图
                        houseImg = (String) imageSourceInfo.getSource() + IMG_SIZE;
                    } else {
                        houseImg = imageSourceInfo.getSource();
                    }
                    Glide
                            .with(mContext)
                            .load(houseImg)
                            .apply(requestOptions)
                            .into(holder.ivPhoto);
                } else {
                    holder.ibtDel.setVisibility(View.GONE);
                    //最后一个显示加号图片
                    Glide
                            .with(mContext)
                            .load(R.drawable.add_pic)
                            .apply(requestOptions)
                            .into(holder.ivPhoto);
                }
            }
        } else {
            /***不可编辑模式，仅查看***/
            holder.ibtDel.setVisibility(View.GONE);

            holder.convertView.setOnClickListener(v -> {
                if (mOnPhotoItemClickListener != null) {
                    //点击图片回调
                    mOnPhotoItemClickListener.onItemClick(v, position, mData.get(position));
                }
            });

            ImageSourceInfo imageSourceInfo = mData.get(position);
            Object houseImg = null;
            if (imageSourceInfo.getSourceType() == ImageSourceType.URL) {
                //列表加载缩略图
                houseImg = (String) imageSourceInfo.getSource() + IMG_SIZE;
            } else {
                houseImg = imageSourceInfo.getSource();
            }
            Glide
                    .with(mContext)
                    .load(houseImg)
                    .apply(requestOptions)
                    .into(holder.ivPhoto);
        }

    }

    @Override
    public int getItemCount() {
        //编辑模式，末尾有+
        int plus = mIsEditMode ? 1 : 0;
        return mData == null ? plus : mData.size() + plus;
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        View convertView;
        /***照片***/
        AppCompatImageView ivPhoto;
        /***删除按钮***/
        ImageButton ibtDel;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            convertView = itemView;
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            ibtDel = itemView.findViewById(R.id.ibt_del);
        }
    }

    public interface OnPhotoItemClickListener {
        /***点击图片***/
        void onItemClick(View view, int position, ImageSourceInfo imageSourceInfo);
    }

    public interface OnPhotoEditableItemClickListener extends OnPhotoItemClickListener {

        /***删除图片***/
        void onPhotoDeleteClick(int position, ImageSourceInfo imageSourceInfo);

        /***添加图片***/
        void onAddPhotoClick();
    }

    private OnPhotoItemClickListener mOnPhotoItemClickListener;

    /**
     * 不可编辑状态调用此方法（查看）
     *
     * @param onPhotoItemClickListener
     */
    public void setOnPhotoItemClickListener(OnPhotoItemClickListener onPhotoItemClickListener) {
        mOnPhotoItemClickListener = onPhotoItemClickListener;
    }

    private OnPhotoEditableItemClickListener mOnPhotoEditableItemClickListener;

    /**
     * 可编辑状态调用此方法监听(添加，删除，查看)
     *
     * @param onPhotoEditItemClickListener
     */
    public void setOnPhotoEditableItemClickListener(OnPhotoEditableItemClickListener onPhotoEditItemClickListener) {
        mOnPhotoEditableItemClickListener = onPhotoEditItemClickListener;
    }
}
