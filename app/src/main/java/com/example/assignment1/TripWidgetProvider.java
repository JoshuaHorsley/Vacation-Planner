package com.example.assignment1;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.assignment1.activities.MainActivity;
import com.example.assignment1.database.TripDAO;
import com.example.assignment1.model.TripModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        // Get trip data from database
        TripDAO tripDAO = new TripDAO(context);
        tripDAO.open();
        List<TripModel> trips = tripDAO.getAllTrips();
        tripDAO.close();

        // Set count text
        views.setTextViewText(R.id.widget_count, "Total trips: " + trips.size());

        // Find the next upcoming trip, if any
        TripModel upcomingTrip = findUpcomingTrip(trips);

        if (upcomingTrip != null) {
            // Format and display the upcoming trip info
            String tripInfo = String.format("Next trip: %s\nTo: %s\nDeparture: %s",
                    upcomingTrip.getTripName(),
                    upcomingTrip.getDestination(),
                    upcomingTrip.getDepartureDate());
            views.setTextViewText(R.id.widget_trip_info, tripInfo);
        } else {
            // No upcoming trips
            views.setTextViewText(R.id.widget_trip_info, "No upcoming trips");
        }

        // Create a pending intent to open the main activity when the widget is clicked
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_title, pendingIntent);

        // Update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // Helper method to find the next upcoming trip
    private static TripModel findUpcomingTrip(List<TripModel> trips) {
        if (trips == null || trips.isEmpty()) {
            return null;
        }

        TripModel upcomingTrip = null;
        Date today = new Date();
        Date closestDate = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            for (TripModel trip : trips) {
                String departureDateStr = trip.getDepartureDate();
                if (departureDateStr != null && !departureDateStr.isEmpty()) {
                    Date departureDate = dateFormat.parse(departureDateStr);

                    // Skip past trips
                    if (departureDate != null && departureDate.after(today)) {
                        // If this is the first future trip we've found, or it's sooner than our current closest
                        if (closestDate == null || departureDate.before(closestDate)) {
                            closestDate = departureDate;
                            upcomingTrip = trip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Handle parsing errors
            e.printStackTrace();
        }

        return upcomingTrip;
    }

    @Override
    public void onEnabled(Context context) {
        // Called when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Called when the last widget is disabled
    }
}