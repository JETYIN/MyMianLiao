package com.tjut.mianliao.scan.assist;

import android.app.Activity;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;
import com.tjut.mianliao.scan.Scanner;

public class ScanResultAssist {

    private ScanResultAssist() {}

    public static void handle(Activity activity, Scanner scanner, Result rawResult) {
        ParsedResult result = ResultParser.parseResult(rawResult);

        // It's not possible to extends a enum, check the result for what we need
        // before checking default types provided by ZXing.
        switch (MlParser.check(result)) {
            case USER:
                new UserResultHandler(activity, scanner, result).handle();
                return;
            case FORUM:
                new ForumResultHandler(activity, scanner, result).handle();
                return;
            case NEWS_SOURCE:
                new NewsSourceResultHandler(activity, scanner, result).handle();
                return;
            case USA:
                new UsaHandler(activity, scanner, result).handle();
                return;
            case TICKET:
                new TicketResultHandler(activity, scanner, result).handle();
                return;
            case UNKNOWN:
            default:
                break;
        }

        switch (result.getType()) {
            case URI:
                new UriResultHandler(activity, scanner, result).handle();
                return;
            case EMAIL_ADDRESS:
                new EmailResultHandler(activity, scanner, result).handle();
                return;
            case TEL:
                new TelResultHandler(activity, scanner, result).handle();
                return;
            case ADDRESSBOOK:
            case PRODUCT:
            case WIFI:
            case GEO:
            case SMS:
            case CALENDAR:
            case ISBN:
            default:
                new BaseResultHandler(activity, scanner, result).handle();
                break;
        }
    }

}
