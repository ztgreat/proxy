package com.proxy.common.util;

import com.google.common.util.concurrent.RateLimiter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 生成编号，基于com.google.common 令牌桶算法
 * <p>
 * 生成编号，编号格式：年月日时分秒+格式化的5递增位数；例如：2018011115373700001、2018011115373701101
 *
 * @author ztgreat
 */
public abstract class NumberGenerate {


    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMddHHmmss");
    private Date date = new Date();
    // 每秒内的自增序列号
    private int sequence = 1;
    private StringBuilder builder = new StringBuilder();

    public abstract int getLimit();

    public abstract String getFormat();

    public abstract RateLimiter getRateLimiter();


    public Long generateId() {
        getRateLimiter().acquire();
        String id = getId();
        id = setPrefix(id);
        return Long.valueOf(setSuffix(id));
    }

    private synchronized String getId() {
        if (sequence > getLimit()) {
            sequence = 1;
        }
        // 清空stringbuffer中生成的编号
        builder.setLength(0);
        long time = System.currentTimeMillis();
        date.setTime(time);
        builder.append(simpleDateFormat.format(date));
        builder.append(String.format(getFormat(), sequence++));
        return builder.toString();
    }

    public String setPrefix(String id) {
        return id;
    }

    public String setSuffix(String id) {
        return id;
    }

}
