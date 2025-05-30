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
import java.util.Date;
import java.util.Locale;

public class NowStatistic extends Fragment {

    private TextView comeTime_textView;
    private TextView homeTime_textView;
    private TextView latenessText_textView;
    private TextView lateness_textView;
    private TextView overtimeText_textView;
    private TextView overTime_textView;
    private long workerId;
    private EventRepository eventRepository;
    private WorkerRepository workerRepository;

    public NowStatistic() {
    }

    public static NowStatistic newInstance(long workerId) {
        NowStatistic fragment = new NowStatistic();
        Bundle args = new Bundle();
        args.putLong("workerId", workerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            workerId = getArguments().getLong("workerId");
        }
        eventRepository = new EventRepository(getContext());
        workerRepository = new WorkerRepository(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_now_statistic, container, false);
        
        comeTime_textView = view.findViewById(R.id.comeTime_textView);
        homeTime_textView = view.findViewById(R.id.homeTime_textView);
        latenessText_textView = view.findViewById(R.id.latenessText_textView);
        lateness_textView = view.findViewById(R.id.lateness_textView);
        overtimeText_textView = view.findViewById(R.id.overtimeText_textView);
        overTime_textView = view.findViewById(R.id.overTime_textView);

        updateStatistics();
        
        return view;
    }

    private void updateStatistics() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        Cursor events = eventRepository.getEventsByWorkerAndDate(workerId, currentDate);
        
        String comeTime = "--:--";
        String homeTime = "--:--";
        
        if (events != null && events.moveToFirst()) {
            do {
                int eventType = events.getInt(events.getColumnIndex(WokerDbContract.Events.COLUMN_TYPE));
                String time = events.getString(events.getColumnIndex(WokerDbContract.Events.COLUMN_TIME));
                
                if (eventType == WokerDbContract.Events.EVENT_TO_WORK) {
                    comeTime = time;
                } else if (eventType == WokerDbContract.Events.EVENT_FROM_WORK) {
                    homeTime = time;
                }
            } while (events.moveToNext());
        }
        
        comeTime_textView.setText(comeTime);
        homeTime_textView.setText(homeTime);
        
        // Получаем плановое время работы
        Cursor workerData = workerRepository.getWorkerById(workerId);
        if (workerData != null && workerData.moveToFirst()) {
            String plannedTimeFrom = workerData.getString(workerData.getColumnIndex(WokerDbContract.Worker.COLUMN_TIME_FROM));
            String plannedTimeTo = workerData.getString(workerData.getColumnIndex(WokerDbContract.Worker.COLUMN_TIME_TO));
            
            // Расчет опоздания/раннего прихода
            if (!comeTime.equals("--:--")) {
                long diffMinutes = getTimeDifferenceInMinutes(plannedTimeFrom, comeTime);
                if (diffMinutes > 0) {
                    latenessText_textView.setText("Опоздание на:");
                    lateness_textView.setText(formatMinutesToTime(diffMinutes));
                } else if (diffMinutes < 0) {
                    latenessText_textView.setText("Пришел раньше на:");
                    lateness_textView.setText(formatMinutesToTime(Math.abs(diffMinutes)));
                } else {
                    latenessText_textView.setText("Вовремя");
                    lateness_textView.setText("00:00");
                }
            } else {
                lateness_textView.setText("--:--");
            }
            
            // Расчет переработки/раннего ухода
            if (!homeTime.equals("--:--")) {
                long diffMinutes = getTimeDifferenceInMinutes(plannedTimeTo, homeTime);
                if (diffMinutes > 0) {
                    overtimeText_textView.setText("Переработка на:");
                    overTime_textView.setText(formatMinutesToTime(diffMinutes));
                } else if (diffMinutes < 0) {
                    overtimeText_textView.setText("Ушел раньше на:");
                    overTime_textView.setText(formatMinutesToTime(Math.abs(diffMinutes)));
                } else {
                    overtimeText_textView.setText("Вовремя");
                    overTime_textView.setText("00:00");
                }
            } else {
                overTime_textView.setText("--:--");
            }
            
            workerData.close();
        }
        
        if (events != null) {
            events.close();
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
    
    private String formatMinutesToTime(long minutes) {
        long hours = minutes / 60;
        long mins = minutes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", hours, mins);
    }
}