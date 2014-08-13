package co.addoil.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by chandominic on 7/8/14.
 */
public class ForecastAdapter extends CursorAdapter {

    public static final String TAG = ForecastAdapter.class.getSimpleName();
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private boolean mUseTodayLayout;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                layoutId = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE_DAY:
                layoutId = R.layout.list_item_forecast;
                break;
        }

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);

        switch (viewType) {
            case VIEW_TYPE_TODAY:
                viewHolder.getIconView().setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
                break;
            case VIEW_TYPE_FUTURE_DAY:
                viewHolder.getIconView().setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
                break;
        }

        // Read date from cursor
        String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        viewHolder.getsDateView().setText(Utility.getFriendlyDayString(context, dateString));

        // Read weather forecast from cursor
        String description = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        // Find TextView and set weather forecast on it
        viewHolder.getDescriptionView().setText(description);
        viewHolder.getIconView().setContentDescription(description);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);
        // Read high temperature from cursor
        float high = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.getHighTempView().setText(Utility.formatTemperature(context, high, isMetric));
        // Read low temperature from cursor
        float low = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.getLowTempView().setText(Utility.formatTemperature(context, low, isMetric));
    }

    public static class ViewHolder {

        private final ImageView sIconView;
        private final TextView sDateView;
        private final TextView sDescriptionView;
        private final TextView sHighTempView;
        private final TextView sLowTempView;

        public ViewHolder(View view) {
            sIconView = (ImageView) view.findViewById(R.id.list_item_icon);
            sDateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            sDescriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            sHighTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            sLowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }

        public ImageView getIconView() {
            return sIconView;
        }

        public TextView getsDateView() {
            return sDateView;
        }

        public TextView getDescriptionView() {
            return sDescriptionView;
        }

        public TextView getHighTempView() {
            return sHighTempView;
        }

        public TextView getLowTempView() {
            return sLowTempView;
        }
    }
}
