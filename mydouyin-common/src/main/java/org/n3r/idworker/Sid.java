package org.n3r.idworker;

import org.n3r.idworker.strategy.DefaultWorkerIdStrategy;
import org.n3r.idworker.utils.Utils;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Sid是一个分布式ID生成器，基于Twitter的Snowflake算法()实现。
 * 该算法生成的ID是一个64位的整数，包含时间戳、机器ID和序列号等信息。
 * Sid类提供了生成唯一ID的方法，可以用于分布式系统中的唯一标识符生成。
 * ------------------------------------------------------------
 * idworker 是一个分布式 ID 生成器库，主要用于在分布式系统中生成全局唯一的 ID。它的主要特点和作用包括：
 * 唯一性保证：能在分布式环境中生成不重复的 ID，用于数据库主键等场景
 * 高性能：基于改进的雪花算法（Snowflake），能快速生成 ID 而不依赖数据库
 * 时间有序：生成的 ID 大体上按时间递增，便于数据库索引和查询
 * 灵活的格式：提供多种格式的 ID，如纯数字和字母数字混合
 */
@Component
public class Sid {
    private static WorkerIdStrategy workerIdStrategy;
    private static IdWorker idWorker;

    static {
        configure(DefaultWorkerIdStrategy.instance);
    }


    public static synchronized void configure(WorkerIdStrategy custom) {
        if (workerIdStrategy != null) workerIdStrategy.release();
        workerIdStrategy = custom;
        idWorker = new IdWorker(workerIdStrategy.availableWorkerId()) {
            @Override
            public long getEpoch() {
                return Utils.midnightMillis();
            }
        };
    }

    /**
     * 一天最大毫秒86400000，最大占用27比特
     * 27+10+11=48位 最大值281474976710655(15字)，YK0XXHZ827(10字)
     * 6位(YYMMDD)+15位，共21位
     *
     * @return 固定21位数字字符串
     */

    public static String next() {
        long id = idWorker.nextId();
        String yyMMdd = new SimpleDateFormat("yyMMdd").format(new Date());
        return yyMMdd + String.format("%014d", id);
    }


    /**
     * 返回固定16位的字母数字混编的字符串。
     */
    public String nextShort() {
        long id = idWorker.nextId();
        String yyMMdd = new SimpleDateFormat("yyMMdd").format(new Date());
        return yyMMdd + Utils.padLeft(Utils.encode(id), 10, '0');
    }
    
    public static void main(String[] args) {
		String aa = new Sid().nextShort();
		String bb = new Sid().next();

		System.out.println(aa);
		System.out.println(bb);
	}
}
