package com.example.workerscontrol;

import android.content.Context;
import android.database.Cursor;
import android.widget.Toast;

import com.example.workerscontrol.data.WokerDbContract;
import com.example.workerscontrol.data.WorkerRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class ExcelExportHelper {

    private ExcelExportHelper() {
    }

    public static File exportLegacyXls(Context context,
                                       long workerId,
                                       String workerName,
                                       String periodTitle,
                                       String fileNamePart,
                                       String startDate,
                                       String endDate,
                                       Cursor cursor,
                                       String[] plannedTimes) throws Exception {
        boolean[] workDays = loadWorkDays(context, workerId);
        ArrayList<AttendanceRow> rows = readRows(cursor, plannedTimes, workDays);
        int plannedMinutes = Math.max(0, parseMinutes(plannedTimes[1]) - parseMinutes(plannedTimes[0]));

        int totalWorked = 0;
        int totalLate = 0;
        int totalOvertime = 0;
        int totalUndertime = 0;
        int nonWorkingVisits = 0;
        int completedDays = 0;
        for (AttendanceRow row : rows) {
            if (row.workedMinutes > 0) {
                totalWorked += row.workedMinutes;
                completedDays++;
            }
            totalLate += row.lateMinutes;
            totalOvertime += row.overtimeMinutes;
            totalUndertime += row.undertimeMinutes;
            if (!row.workingDay && row.workedMinutes > 0) {
                nonWorkingVisits++;
            }
        }

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append("<?mso-application progid=\"Excel.Sheet\"?>");
        xml.append("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" ");
        xml.append("xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">");
        xml.append("<Worksheet ss:Name=\"Отчет\"><Table>");

        addSection(xml, "Общие данные");
        addRow(xml, "Сотрудник", workerName);
        addRow(xml, "Период", periodTitle);
        addRow(xml, "Диапазон дат", formatDate(startDate) + " - " + formatDate(endDate));
        addRow(xml, "Плановый график", plannedTimes[0] + " - " + plannedTimes[1]);
        addRow(xml, "План в день", formatDuration(plannedMinutes));
        addEmptyRow(xml);

        addSection(xml, "Статистика");
        addRow(xml, "Дней с полными отметками", String.valueOf(completedDays));
        addRow(xml, "Всего отработано", formatDuration(totalWorked));
        addRow(xml, "Всего опозданий", formatDuration(totalLate));
        addRow(xml, "Всего переработок", formatDuration(totalOvertime));
        addRow(xml, "Всего недоработок", formatDuration(totalUndertime));
        addRow(xml, "Выходов в нерабочие дни", String.valueOf(nonWorkingVisits));
        addEmptyRow(xml);

        addSection(xml, "Детализация посещаемости");
        addHeader(xml, "Дата", "День недели", "Рабочий день", "Приход", "Уход", "Отработано", "Опоздание", "Переработка", "Недоработка");
        for (AttendanceRow row : rows) {
            addCells(xml, formatDate(row.date), row.dayName, row.workingDay ? "Да" : "Нет", row.arrival, row.departure,
                    formatDurationOrDash(row.workedMinutes), formatDuration(row.lateMinutes), formatDuration(row.overtimeMinutes), formatDuration(row.undertimeMinutes));
        }
        addEmptyRow(xml);

        addSection(xml, "Выходы в нерабочие дни");
        addHeader(xml, "Дата", "День недели", "Приход", "Уход", "Отработано");
        for (AttendanceRow row : rows) {
            if (!row.workingDay && row.workedMinutes > 0) {
                addCells(xml, formatDate(row.date), row.dayName, row.arrival, row.departure, formatDuration(row.workedMinutes));
            }
        }
        addEmptyRow(xml);

        addSection(xml, "Переработки");
        addHeader(xml, "Дата", "Отработано", "Переработка", "График переработок");
        for (AttendanceRow row : rows) {
            if (row.overtimeMinutes > 0) {
                addCells(xml, formatDate(row.date), formatDuration(row.workedMinutes), formatDuration(row.overtimeMinutes), bar(row.overtimeMinutes));
            }
        }
        addEmptyRow(xml);

        addSection(xml, "Недоработки");
        addHeader(xml, "Дата", "Отработано", "Недоработка");
        for (AttendanceRow row : rows) {
            if (row.undertimeMinutes > 0) {
                addCells(xml, formatDate(row.date), formatDuration(row.workedMinutes), formatDuration(row.undertimeMinutes));
            }
        }
        addEmptyRow(xml);

        addSection(xml, "График опозданий");
        addHeader(xml, "Дата", "Опоздание", "График");
        for (AttendanceRow row : rows) {
            addCells(xml, formatDate(row.date), formatDuration(row.lateMinutes), row.lateMinutes > 0 ? bar(row.lateMinutes) : "-");
        }
        addEmptyRow(xml);

        addSection(xml, "График переработок");
        addHeader(xml, "Дата", "Переработка", "График");
        for (AttendanceRow row : rows) {
            addCells(xml, formatDate(row.date), formatDuration(row.overtimeMinutes), row.overtimeMinutes > 0 ? bar(row.overtimeMinutes) : "-");
        }

        xml.append("</Table></Worksheet></Workbook>");

        File exportDir = ensureExportDir(context);
        File file = new File(exportDir, "worker_" + workerId + "_" + fileNamePart + "_" + timestamp() + ".xls");
        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(xml.toString().getBytes(StandardCharsets.UTF_8));
            stream.flush();
        }
        Toast.makeText(context, "Файл сохранен: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        return file;
    }

    private static ArrayList<AttendanceRow> readRows(Cursor cursor, String[] plannedTimes, boolean[] workDays) {
        ArrayList<AttendanceRow> rows = new ArrayList<>();
        int plannedStart = parseMinutes(plannedTimes[0]);
        int plannedEnd = parseMinutes(plannedTimes[1]);
        int plannedDuration = Math.max(0, plannedEnd - plannedStart);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String date = getSafe(cursor, WokerDbContract.Events.COLUMN_DATE);
                String arrival = normalizeTime(getSafe(cursor, "arrival_time"));
                String departure = normalizeTime(getSafe(cursor, "departure_time"));
                int worked = calcWorkedMinutes(arrival, departure);
                int late = Math.max(0, parseMinutes(arrival) - plannedStart);
                int overtime = Math.max(0, parseMinutes(departure) - plannedEnd);
                int undertime = worked >= 0 && plannedDuration > worked ? plannedDuration - worked : 0;
                int dayIndex = getDayIndex(date);
                boolean workingDay = dayIndex >= 0 && workDays[dayIndex];
                rows.add(new AttendanceRow(date, dayName(dayIndex), workingDay, arrival, departure, worked, late, overtime, undertime));
            } while (cursor.moveToNext());
        }
        return rows;
    }

    private static boolean[] loadWorkDays(Context context, long workerId) {
        boolean[] result = {true, true, true, true, true, false, false};
        WorkerRepository repository = new WorkerRepository(context);
        Cursor cursor = repository.getWorkerById(workerId);
        if (cursor != null && cursor.moveToFirst()) {
            result[0] = getIntSafe(cursor, WokerDbContract.Worker.COLUMN_MONDAY, 1) == 1;
            result[1] = getIntSafe(cursor, WokerDbContract.Worker.COLUMN_TUESDAY, 1) == 1;
            result[2] = getIntSafe(cursor, WokerDbContract.Worker.COLUMN_WEDNESDAY, 1) == 1;
            result[3] = getIntSafe(cursor, WokerDbContract.Worker.COLUMN_THURSDAY, 1) == 1;
            result[4] = getIntSafe(cursor, WokerDbContract.Worker.COLUMN_FRIDAY, 1) == 1;
            result[5] = getIntSafe(cursor, WokerDbContract.Worker.COLUMN_SATURDAY, 0) == 1;
            result[6] = getIntSafe(cursor, WokerDbContract.Worker.COLUMN_SUNDAY, 0) == 1;
            cursor.close();
        }
        repository.close();
        return result;
    }

    private static String getSafe(Cursor cursor, String col) {
        int idx = cursor.getColumnIndex(col);
        if (idx < 0 || cursor.isNull(idx)) return "";
        return cursor.getString(idx);
    }

    private static int getIntSafe(Cursor cursor, String col, int defaultValue) {
        int idx = cursor.getColumnIndex(col);
        if (idx < 0) {
            idx = cursor.getColumnIndex(col.trim());
        }
        if (idx < 0 || cursor.isNull(idx)) {
            return defaultValue;
        }
        return cursor.getInt(idx);
    }

    private static String normalizeTime(String value) {
        return (value == null || value.trim().isEmpty()) ? "--:--" : value;
    }

    private static int parseMinutes(String time) {
        try {
            if (time == null || "--:--".equals(time)) return -1;
            String[] p = time.split(":");
            if (p.length != 2) return -1;
            return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
        } catch (Exception e) {
            return -1;
        }
    }

    private static int calcWorkedMinutes(String arrival, String departure) {
        int start = parseMinutes(arrival);
        int end = parseMinutes(departure);
        if (start < 0 || end < 0 || end < start) return -1;
        return end - start;
    }

    private static String formatDuration(int minutes) {
        if (minutes <= 0) return "0:00";
        return String.format(Locale.getDefault(), "%d:%02d", minutes / 60, minutes % 60);
    }

    private static String formatDurationOrDash(int minutes) {
        if (minutes < 0) return "--";
        return formatDuration(minutes);
    }

    private static String bar(int minutes) {
        int count = Math.max(1, Math.min(30, minutes / 10));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) sb.append('|');
        return sb.toString();
    }

    private static String formatDate(String source) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(source);
            if (d == null) return source;
            return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(d);
        } catch (Exception e) {
            return source;
        }
    }

    private static int getDayIndex(String source) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(source);
            if (date == null) return -1;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.MONDAY) return 0;
            if (day == Calendar.TUESDAY) return 1;
            if (day == Calendar.WEDNESDAY) return 2;
            if (day == Calendar.THURSDAY) return 3;
            if (day == Calendar.FRIDAY) return 4;
            if (day == Calendar.SATURDAY) return 5;
            if (day == Calendar.SUNDAY) return 6;
            return -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private static String dayName(int index) {
        String[] names = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье"};
        return index >= 0 && index < names.length ? names[index] : "";
    }

    private static void addSection(StringBuilder xml, String title) {
        addCells(xml, title);
    }

    private static void addRow(StringBuilder xml, String title, String value) {
        addCells(xml, title, value);
    }

    private static void addHeader(StringBuilder xml, String... values) {
        addCells(xml, values);
    }

    private static void addCells(StringBuilder xml, String... values) {
        xml.append("<Row>");
        for (String value : values) {
            cell(xml, value);
        }
        xml.append("</Row>");
    }

    private static void addEmptyRow(StringBuilder xml) {
        xml.append("<Row></Row>");
    }

    private static void cell(StringBuilder xml, String value) {
        xml.append("<Cell><Data ss:Type=\"String\">").append(escape(value == null ? "" : value)).append("</Data></Cell>");
    }

    private static String escape(String v) {
        return v.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    private static File ensureExportDir(Context context) {
        File baseDir = context.getExternalFilesDir("documents");
        if (baseDir == null) {
            baseDir = context.getFilesDir();
        }
        File exportDir = new File(baseDir, "exports");
        if (!exportDir.exists() && !exportDir.mkdirs()) {
            throw new IllegalStateException("Cannot create export directory: " + exportDir.getAbsolutePath());
        }
        return exportDir;
    }

    private static String timestamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
    }

    private static class AttendanceRow {
        final String date;
        final String dayName;
        final boolean workingDay;
        final String arrival;
        final String departure;
        final int workedMinutes;
        final int lateMinutes;
        final int overtimeMinutes;
        final int undertimeMinutes;

        AttendanceRow(String date, String dayName, boolean workingDay, String arrival, String departure,
                      int workedMinutes, int lateMinutes, int overtimeMinutes, int undertimeMinutes) {
            this.date = date;
            this.dayName = dayName;
            this.workingDay = workingDay;
            this.arrival = arrival;
            this.departure = departure;
            this.workedMinutes = workedMinutes;
            this.lateMinutes = lateMinutes;
            this.overtimeMinutes = overtimeMinutes;
            this.undertimeMinutes = undertimeMinutes;
        }
    }
}
