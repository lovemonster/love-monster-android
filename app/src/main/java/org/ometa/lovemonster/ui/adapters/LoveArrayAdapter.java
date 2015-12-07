package org.ometa.lovemonster.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.ometa.lovemonster.R;
import org.ometa.lovemonster.models.Love;
import org.ometa.lovemonster.models.User;
import org.ometa.lovemonster.ui.activities.UserLoveActivity;
import org.ometa.lovemonster.ui.widget.RoundedRectangleTransformation;

import java.util.List;

/**
 * Created by bschmeckpeper on 11/18/15.
 */
public class LoveArrayAdapter extends ArrayAdapter<Love> {
    public User currentUser;
    public User subjectUser;

    static class ViewHolder {

        private Context context;
        public ImageView ivSenderImage, ivRecipientImage;
        public TextView tvLoverName, tvLoveeName, tvReason, tvEllipsis, tvMessage, tvTimeAgo;

        public ViewHolder(Context context) {
            this.context = context;
        }

        public void populate(final Love love) {
            Picasso.with(context)
                    .load(Uri.parse(love.lover.profileImageUrl))
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.ic_default_user_avatar)
                    .transform(new RoundedRectangleTransformation(10, 1))
                    .into(ivSenderImage);

            Picasso.with(context)
                    .load(Uri.parse(love.lovee.profileImageUrl))
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.ic_default_user_avatar)
                    .transform(new RoundedRectangleTransformation(10, 1))
                    .into(ivRecipientImage);

            tvLoverName.setText(displayNameFor(love.lover));

            final View.OnClickListener loverOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, UserLoveActivity.class);
                    i.putExtra(User.PARCELABLE_KEY, love.lover);
                    context.startActivity(i);
                }
            };
            ivSenderImage.setOnClickListener(loverOnClickListener);
            tvLoverName.setOnClickListener(loverOnClickListener);

            tvLoveeName.setText(displayNameFor(love.lovee));

            final View.OnClickListener loveeOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, UserLoveActivity.class);
                    i.putExtra(User.PARCELABLE_KEY, love.lovee);
                    context.startActivity(i);
                }
            };
            ivRecipientImage.setOnClickListener(loveeOnClickListener);
            tvLoveeName.setOnClickListener(loveeOnClickListener);

            tvTimeAgo.setText(love.timeAgo());
            tvReason.setText(love.reason);

            if (love.hasMessage()) {
                tvMessage.setText(love.message);
                tvMessage.setVisibility(View.VISIBLE);
            } else {
                tvMessage.setText("");
                tvMessage.setVisibility(View.GONE);
            }
        }

        private String displayNameFor(User user) {
            if (user.name != null)
                return user.name;

            return user.username;
        }

        private void highlight(boolean highlightOn) {
            if (highlightOn) {
                tvReason.setText("HIGHLIGHT");
            } else {
                tvReason.setText("REASON");
            }
        }
    }

    public LoveArrayAdapter(Context context, List<Love> loves, User currentUser) {
        super(context, android.R.layout.simple_list_item_1, loves);
        this.currentUser = currentUser;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Love love = getItem(position);
        if (convertView == null) {
            convertView = inflateView(parent);
        }

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.populate(love);
        highlight(convertView, love.lovee.equals(currentUser) || love.lover.equals(currentUser));
        return convertView;
    }

    // method to add a list of loves to the beginning of the array adapter
    public void addAllToBeginning(List<Love> loves) {
        for (Love love : loves) {
            this.insert(love, 0);
        }
        notifyDataSetChanged();
    }

    // method to add a list of loves to the end of the array adapter
    public void addAllToEnd(List<Love> loves) {
        for (Love love : loves) {
            this.add(love);
        }
        notifyDataSetChanged();
    }

    private void highlight(View view, boolean highlightOn) {
        if (highlightOn) {
            view.setBackgroundResource(R.drawable.user_highlight);
        } else {
            view.setBackgroundResource(0);
        }
    }

    private View inflateView(ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.love_list_item, parent, false);

        final ViewHolder viewHolder = new ViewHolder(getContext());
        viewHolder.ivRecipientImage = (ImageView) view.findViewById(R.id.ivRecipientImage);
        viewHolder.ivSenderImage = (ImageView) view.findViewById(R.id.ivSenderImage);
        viewHolder.tvLoveeName = (TextView) view.findViewById(R.id.tvRecipientName);
        viewHolder.tvLoverName = (TextView) view.findViewById(R.id.tvSenderName);
        viewHolder.tvReason = (TextView) view.findViewById(R.id.tvReason);
        viewHolder.tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        viewHolder.tvTimeAgo = (TextView) view.findViewById(R.id.tvTimeAgo);

        view.setTag(viewHolder);

        return view;
    }
}
