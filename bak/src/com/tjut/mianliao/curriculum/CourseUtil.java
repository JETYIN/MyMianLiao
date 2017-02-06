package com.tjut.mianliao.curriculum;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.SparseArray;

import com.tjut.mianliao.R;
import com.tjut.mianliao.data.Course;
import com.tjut.mianliao.data.Course.Entry;
import com.tjut.mianliao.util.Utils;

/**
 * Helper class to do calculation, string formatting for a course.
 */
public class CourseUtil {

    public static final int MAX_WEEK = 24;

    private static final int ODD_WEEK = 22369621; //0b1010101010101010101010101
    private static final int EVEN_WEEK = 11184810; //0b101010101010101010101010

    private static SparseArray<String> sWeekMap = new SparseArray<String>();
    private static Resources sResources;

    private static int[] sColors;

    private CourseUtil() {}

    public static void init(Context context) {
        if (context != null) {
            sResources = context.getResources();

            TypedArray colors = sResources.obtainTypedArray(R.array.lesson_colors);
            int numColors = colors.length();
            sColors = new int[numColors];
            for (int i = 0; i < numColors; i++) {
                sColors[i] = colors.getColor(i, 0);
            }
            colors.recycle();
        }
    }

    public static boolean containsWeek(int weeks, int week) {
        return ((weeks >> (week - 1)) & 1) == 1;
    }

    public static int addWeek(int weeks, int week) {
        return weeks | (1 << (week - 1));
    }

    public static int removeWeek(int weeks, int week) {
        return weeks & (~(1 << (week - 1)));
    }

    /**
     * Example: 201301 means: 2013-2014-01; 201302 means 2013-2014-02
     */
    public static String getSemesterDesc(int semester) {
        if (semester == 0) {
            return "";
        }
        int term = semester % 100;
        int year = semester / 100;
        return sResources.getString(R.string.course_semester_desc, year, year + 1, term);
    }

    private static String getEvenWeekDesc(int weeks) {
        if ((weeks & EVEN_WEEK) == weeks && ((weeks << 2) & weeks) == ((weeks - 1) & weeks)) {
            int first = 0;
            int last = 0;
            for (int i = 0; weeks > 0; i = i + 2) {
                if (first == 0) {
                    if ((weeks & 2) == 2) {
                        first = i + 2;
                    }
                } else {
                    if (weeks == 2) {
                        last = i + 2;
                        break;
                    }
                }
                weeks >>= 2;
            }
            if (last - first > 2) {
                return sResources.getString(R.string.course_even_week_desc, first, last);
            }
        }
        return null;
    }

    private static String getOddWeekDesc(int weeks) {
        if ((weeks & ODD_WEEK) == weeks && ((weeks << 2) & weeks) == ((weeks - 1) & weeks)) {
            int first = 0;
            int last = 0;
            for (int i = 1; weeks > 0; i = i + 2) {
                if (first == 0) {
                    if ((weeks & 1) == 1) {
                        first = i;
                    }
                } else {
                    if (weeks == 1) {
                        last = i;
                        break;
                    }
                }
                weeks >>= 2;
            }
            if (last - first > 2) {
                return sResources.getString(R.string.course_odd_week_desc, first, last);
            }
        }
        return null;
    }

    public static String getWeekDesc(int weeks) {
        if (weeks == 0) {
            return "";
        }
        if (sWeekMap.get(weeks) != null) {
            return sWeekMap.get(weeks);
        }

        String eoDesc = getOddWeekDesc(weeks);
        if (eoDesc != null) {
            sWeekMap.put(weeks, eoDesc);
            return eoDesc;
        }

        eoDesc = getEvenWeekDesc(weeks);
        if (eoDesc != null) {
            sWeekMap.put(weeks, eoDesc);
            return eoDesc;
        }

        StringBuilder desc = new StringBuilder();
        int count = 1;
        int mark = 1;
        int bound = MAX_WEEK + 1;
        for (int i = 0; i < bound; i++) {
            if ((weeks & mark) != mark) {
                if (mark == 1) {
                    weeks = weeks >> 1;
                    continue;
                }
                weeks = weeks >> count;
                if (desc.length() > 0) {
                    desc.append(", ");
                }
                if (count > 2) {
                    desc.append((i - count + 2) + "~" + i);
                } else {
                    int from = i - count + 2;
                    for (int j = 0; j < count - 1; j++) {
                        if (j > 0) {
                            desc.append(",");
                        }
                        desc.append((from + j));
                    }
                }
                mark = 1;
                count = 1;
                continue;
            } else {
                count++;
                mark = (mark << 1) + 1;
            }
        }
        // Limit the number, so it doesn't eat too much memory
        if (sWeekMap.size() > 30) {
            sWeekMap.remove(sWeekMap.keyAt(0));
        }
        String result = desc.toString();
        sWeekMap.put(weeks, result);
        return sResources.getString(R.string.course_week_desc, result);
    }

    public static String getWeekdayDesc(int weekDay) {
        int weekdayRes = 0;
        switch (weekDay) {
            case 1:
                weekdayRes = R.string.cur_week_mon;
                break;

            case 2:
                weekdayRes = R.string.cur_week_tue;
                break;

            case 3:
                weekdayRes = R.string.cur_week_wed;
                break;

            case 4:
                weekdayRes = R.string.cur_week_thu;
                break;

            case 5:
                weekdayRes = R.string.cur_week_fri;
                break;

            case 6:
                weekdayRes = R.string.cur_week_sat;
                break;

            default:
                weekdayRes = R.string.cur_week_sun;
                break;
        }
        return weekdayRes == 0 ? "" : sResources.getString(
                R.string.course_weekday_desc, sResources.getString(weekdayRes));
    }

    public static String getPeriodDesc(int periodStart, int periodEnd) {
        StringBuilder period = new StringBuilder();
        if (periodStart > 0) {
            period.append(periodStart);
            if (periodEnd > periodStart) {
                period.append("~").append(periodEnd);
            }
        }
        return sResources.getString(R.string.course_period_desc, period.toString());
    }

    public static String getPeriodDesc(Course.Entry entry) {
        if (entry.weekday == 0 || entry.periodStart == 0 || entry.periodEnd == 0) {
            return "";
        }
        return new StringBuilder(getWeekdayDesc(entry.weekday))
                .append(" ").append(getPeriodDesc(entry.periodStart, entry.periodEnd))
                .toString();
    }

    public static String getPlaceDesc(Course.Entry entry) {
        return entry.classroom == null ? "" : entry.classroom;
    }

    public static String getEntryDesc(Course.Entry entry) {
        return sResources.getString(R.string.course_entry_desc,
                getWeekDesc(entry.weeks), getPeriodDesc(entry), getPlaceDesc(entry));
    }

    public static String getCourseDesc(Course course) {
        List<Entry> entries = course.getEntries();
        // Get the first entry description since there always be at least one
        StringBuilder desc = new StringBuilder(getEntryDesc(entries.get(0)));
        for (int i = 1; i < entries.size(); i++) {
            desc.append(Utils.getLineSeparator()).append(getEntryDesc(entries.get(i)));
        }
        return desc.toString();
    }

    public static String getSchedule(Entry entry, int currentWeek) {
        return String.format(Locale.US, "%02d%1d%02d", currentWeek, entry.weekday, entry.periodStart);
    }

    public static int getColorFor(int seed) {
        return sColors[Math.abs(seed % sColors.length)];
    }

    public static boolean isCourseClosed(int weeks, int currentWeek) {
        return (1 << (currentWeek - 1)) > weeks;
    }
}
