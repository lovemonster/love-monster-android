package org.ometa.lovemonster.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.ometa.lovemonster.R;
import org.ometa.lovemonster.models.Love;
import org.ometa.lovemonster.models.User;

import java.util.List;

/**
 * Created by bschmeckpeper on 11/18/15.
 */
public class LoveArrayAdapter extends ArrayAdapter<Love> {


    static class VisibilityToggler implements View.OnClickListener {
        View toToggle;

        public VisibilityToggler(View toToggle) {
            this.toToggle = toToggle;
        }

        @Override
        public void onClick(View v) {
            if (toToggle.getVisibility() == View.VISIBLE) {
                toToggle.setVisibility(View.GONE);
            } else {
                toToggle.setVisibility(View.VISIBLE);
            }
        }
    }
    static class ViewHolder {

        public ImageView ivSenderImage, ivRecipientImage;
        public TextView tvSenderName, tvRecipientName, tvReason, tvEllipsis, tvMessage;

        public void populate(Love love) {
            tvSenderName.setText(displayNameFor(love.lover));
            tvRecipientName.setText(displayNameFor(love.lovee));
            tvReason.setText(love.reason);
            if (love.hasMessage()) {
                tvEllipsis.setVisibility(View.VISIBLE);
                tvMessage.setText(love.message);
            } else {
                tvEllipsis.setVisibility(View.GONE);
                tvMessage.setText("");
            }

            //tvMessage.setVisibility(View.GONE);
        }

        private String displayNameFor(User user) {
            if (user.name != null)
                return user.name;

            return user.username;
        }
    }

    public LoveArrayAdapter(Context context, List<Love> loves) {
        super(context, android.R.layout.simple_list_item_1, loves);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Love love = getItem(position);
        if (convertView == null) {
            convertView = inflateView(parent);
        }

        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.populate(love);

        return convertView;
    }

    private View inflateView(ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.love_list_item, parent, false);

        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.ivRecipientImage = (ImageView) view.findViewById(R.id.ivRecipientImage);
        viewHolder.ivSenderImage = (ImageView) view.findViewById(R.id.ivSenderImage);
        viewHolder.tvRecipientName = (TextView) view.findViewById(R.id.tvRecipientName);
        viewHolder.tvSenderName = (TextView) view.findViewById(R.id.tvSenderName);
        viewHolder.tvReason = (TextView) view.findViewById(R.id.tvReason);
        viewHolder.tvEllipsis = (TextView) view.findViewById(R.id.tvElipsis);
        viewHolder.tvMessage = (TextView) view.findViewById(R.id.tvMessage);
        viewHolder.tvEllipsis.setOnClickListener(new VisibilityToggler(viewHolder.tvMessage));

        view.setTag(viewHolder);

        return view;
    }
}
