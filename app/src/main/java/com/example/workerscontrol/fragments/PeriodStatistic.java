package com.example.workerscontrol.fragments;

import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.workerscontrol.R;
import com.example.workerscontrol.data.EventRepository;
import com.example.workerscontrol.data.WorkerRepository;
import com.example.workerscontrol.data.WokerDbContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PeriodStatistic extends Fragment {
    public static final int PERIOD_WEEK = 0;
    public static final int PERIOD_MONTH = 1;
    public static final int PERIOD_YEAR = 2;

    private TextView latenessCount_textView;
    private TextView overtimeCount_textView;
    private TextView latenessAllTime_textView;
    private TextView overtimeAllTime_textView;
    private TextView latenessAverage_textView;
    private TextView overtimeAverage_textView;
    private TextView comeTimeAverage_textView;
    private TextView homeTimeAverage_textView;

    private long workerId;
    private int period;
    private EventRepository eventRepository;
    private WorkerRepository workerRepository;

    public PeriodStatistic() {
    }

    public static PeriodStatistic newInstance(long workerId, int period) {
        PeriodStatistic fragment = new PeriodStatistic();
        Bundle args = new Bundle();
        args.putLong("workerId", workerId);
        args.putInt("period", period);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            workerId = getArguments().getLong("workerId");
            period = getArguments().getInt("period", PERIOD_WEEK);
        }
        eventRepository = new EventRepository(getContext());
        workerRepository = new WorkerRepository(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_week_statistic, container, false);

        latenessCount_textView = view.findViewById(R.id.latenessCount_textView);
        overtimeCount_textView = view.findViewById(R.id.overtimeCount_textView);
        latenessAllTime_textView = view.findViewById(R.id.latenessAllTime_textView);
        overtimeAllTime_textView = view.findViewById(R.id.overtimeAllTime_textView);
        latenessAverage_textView = view.findViewById(R.id.latenessAverage_textView);
        overtimeAverage_textView = view.findViewById(R.id.overtimeAverage_textView);
        comeTimeAverage_textView = view.findViewById(R.id.comeTimeAverage_textView);
        homeTimeAverage_textView = view.findViewById(R.id.homeTimeAverage_textView);

        updateStatistics();

        return view;
    }

    private void updateStatistics() {
        Calendar calendar = Calendar.getInstance();
        String startDate, endDate;

        switch (period) {
            case PERIOD_MONTH:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
                break;

            case PERIOD_YEAR:
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
                endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
                break;

            case PERIOD_WEEK:
            default:
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

                calendar.add(Calendar.DAY_OF_WEEK, 6);
                endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
                break;
        }

        Cursor events = eventRepository.getEventsByWorkerAndDateRange(workerId, startDate, endDate);

        Cursor workerData = workerRepository.getWorkerById(workerId);
        String plannedTimeFrom = "09:00";
        String plannedTimeTo = "18:00";
        
        if (workerData != null && workerData.moveToFirst()) {
            plannedTimeFrom = workerData.getString(workerData.getColumnIndex(WokerDbContract.Worker.COLUMN_TIME_FROM));
            plannedTimeTo = workerData.getString(workerData.getColumnIndex(WokerDbContract.Worker.COLUMN_TIME_TO));
        }

        int latenessCount = 0;
        int overtimeCount = 0;
        long totalLatenessMinutes = 0;
        long totalOvertimeMinutes = 0;
        long totalComeMinutes = 0;
        long totalHomeMinutes = 0;
        int daysWithCome = 0;
        int daysWithHome = 0;

        String currentDate = "";
        String comeTime = "";
        String homeTime = "";

        if (events != null && events.moveToFirst()) {
            do {
                String date = events.getString(events.getColumnIndex(WokerDbContract.Events.COLUMN_DATE));
                String time = events.getString(events.getColumnIndex(WokerDbContract.Events.COLUMN_TIME));
                int type = events.getInt(events.getColumnIndex(WokerDbContract.Events.COLUMN_TYPE));

                if (!date.equals(currentDate)) {
                    if (!currentDate.isEmpty() && !comeTime.isEmpty()) {
                        long diffMinutes = getTimeDifferenceInMinutes(plannedTimeFrom, comeTime);
                        if (diffMinutes > 0) {
                            latenessCount++;
                            totalLatenessMinutes += diffMinutes;
                        }
                        totalComeMinutes += getMinutesSinceMidnight(comeTime);
                        daysWithCome++;
                    }
                    if (!currentDate.isEmpty() && !homeTime.isEmpty()) {
                        long diffMinutes = getTimeDifferenceInMinutes(plannedTimeTo, homeTime);
                        if (diffMinutes > 0) {
                            overtimeCount++;
                            totalOvertimeMinutes += diffMinutes;
                        }
                        totalHomeMinutes += getMinutesSinceMidnight(homeTime);
                        daysWithHome++;
                    }
                    
                    currentDate = date;
                    comeTime = "";
                    homeTime = "";
                }

                if (type == WokerDbContract.Events.EVENT_TO_WORK) {
                    comeTime = time;
                } else if (type == WokerDbContract.Events.EVENT_FROM_WORK) {
                    homeTime = time;
                }
            } while (events.moveToNext());

            if (!comeTime.isEmpty()) {
                long diffMinutes = getTimeDifferenceInMinutes(plannedTimeFrom, comeTime);
                if (diffMinutes > 0) {
                    latenessCount++;
                    totalLatenessMinutes += diffMinutes;
                }
                totalComeMinutes += getMinutesSinceMidnight(comeTime);
                daysWithCome++;
            }
            if (!homeTime.isEmpty()) {
                long diffMinutes = getTimeDifferenceInMinutes(plannedTimeTo, homeTime);
                if (diffMinutes > 0) {
                    overtimeCount++;
                    totalOvertimeMinutes += diffMinutes;
                }
                totalHomeMinutes += getMinutesSinceMidnight(homeTime);
                daysWithHome++;
            }
        }

        latenessCount_textView.setText(String.valueOf(latenessCount));
        overtimeCount_textView.setText(String.valueOf(overtimeCount));
        
        latenessAllTime_textView.setText(formatMinutesToTime(totalLatenessMinutes));
        overtimeAllTime_textView.setText(formatMinutesToTime(totalOvertimeMinutes));
        
        long avgLateness = latenessCount > 0 ? totalLatenessMinutes / latenessCount : 0;
        long avgOvertime = overtimeCount > 0 ? totalOvertimeMinutes / overtimeCount : 0;
        latenessAverage_textView.setText(formatMinutesToTime(avgLateness));
        overtimeAverage_textView.setText(formatMinutesToTime(avgOvertime));
        
        long avgComeTime = daysWithCome > 0 ? totalComeMinutes / daysWithCome : 0;
        long avgHomeTime = daysWithHome > 0 ? totalHomeMinutes / daysWithHome : 0;
        comeTimeAverage_textView.setText(formatMinutesToTime(avgComeTime));
        homeTimeAverage_textView.setText(formatMinutesToTime(avgHomeTime));

        if (events != null) {
            events.close();
        }
        if (workerData != null) {
            workerData.close();
        }
    }

    private long getTimeDifferenceInMinutes(String time1, String time2) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date1 = format.parse(time1);
            Date date2 = format.parse(time2);
            
            return (date2.getTime() - date1.getTime()) / (60 * 1000);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private long getMinutesSinceMidnight(String time) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = format.parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar.get(Calendar.HOUR_OF_DAY) * 60L + calendar.get(Calendar.MINUTE);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String formatMinutesToTime(long minutes) {
        if (minutes == 0) return "00:00";
        long hours = minutes / 60;
        long mins = minutes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", hours, mins);
    }
}