package com.lwx.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.Message;

import java.net.InetSocketAddress;
import java.util.List;

import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @description: SimpleCanalClientExample
 * @author: liwx
 * @create: 2020-06-21 19:17
 **/
@Component
public class SimpleCanalClient {
    private static Logger logger = LoggerFactory.getLogger(SimpleCanalClient.class);

    //    @Value("${canal.consumer.concurrency}")
//    private int concurrency;
    @PostConstruct
    public void startCanalSubscribe() {
        logger.info("------> startCanalSubscribe");
//        start();
    }

    private void start() {
        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress("172.10.4.21",
                11111), "test2", "canal", "canal");
        int batchSize = 1000;
        int emptyCount = 0;
        try {
            connector.connect();
//            connector.subscribe(".*\\..*");//订阅所有库下面的所有表
            connector.subscribe("lwx_test\\..*");
            connector.rollback();
            int totalEmptyCount = 120;
            while (true) {
                try {

                    Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                    long batchId = message.getId();
                    int size = message.getEntries().size();
                    if (batchId == -1 || size == 0) {
                        emptyCount++;
                        logger.info("empty count : " + emptyCount);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        emptyCount = 0;
                        // System.out.printf("message[batchId=%s,size=%s] \n", batchId, size);
                        printEntry(message.getEntries());
                    }

                    connector.ack(batchId); // 提交确认
                    // connector.rollback(batchId); // 处理失败, 回滚数据
                } catch (Exception e) {
                    logger.info("获取数据失败");

                }
            }

        } finally {
            connector.disconnect();
            logger.info("stop subscribe");
        }
    }

    private static void printEntry(List<Entry> entrys) {
        for (Entry entry : entrys) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN || entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }

            RowChange rowChage = null;
            try {
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }

            EventType eventType = rowChage.getEventType();
//            System.out.println(String.format("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s",
//                    entry.getHeader().getLogfileName(),//
//                    entry.getHeader().getLogfileOffset(),//偏移量
//                    entry.getHeader().getSchemaName(),//库名
//                    entry.getHeader().getTableName(),//表名
//                    eventType));//事件名
            logger.info(String.format("=======> EventType[ %s ] Schema[ %s ] Table[ %s ] ; Logfile[ %s ] Offset[ %s ]",
                    eventType,//事件名
                    entry.getHeader().getSchemaName(),//库名
                    entry.getHeader().getTableName(),//表名
                    entry.getHeader().getLogfileName(),//binlog文件名
                    entry.getHeader().getLogfileOffset()//偏移量
            ));
            for (RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == EventType.DELETE) {
                    printColumn(rowData.getBeforeColumnsList());
                } else if (eventType == EventType.INSERT) {
                    printColumn(rowData.getAfterColumnsList());
                } else {
                    logger.info("-------> before");
                    printColumn(rowData.getBeforeColumnsList());
                    logger.info("-------> after");
                    printColumn(rowData.getAfterColumnsList());
                }
            }
        }
    }

    private static void printColumn(List<Column> columns) {
        logger.info("------>" + columns.size());
        StringBuilder sb = new StringBuilder();
        for (Column column : columns) {
            sb.append("  column:" + " " + column.getName() + ",value:" + column.getValue() + ",update=" + column.getUpdated());
        }
        logger.info("------>" + sb.toString());
    }

}
