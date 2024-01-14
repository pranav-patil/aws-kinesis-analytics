package com.emprovise.machine.analytics;

//import org.apache.flink.table.api.TableEnvironment;
//import org.junit.Test;
//import org.apache.flink.api.common.eventtime.WatermarkStrategy;
//import org.apache.flink.api.common.functions.MapFunction;
//import org.apache.flink.api.java.tuple.Tuple3;
//import org.apache.flink.streaming.api.TimeCharacteristic;
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.table.api.Table;
//import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
//import org.apache.flink.types.Row;
//import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
//import org.apache.flink.api.common.typeinfo.TypeInformation;
//import org.apache.flink.api.java.tuple.Tuple3;
//import org.apache.flink.api.java.tuple.Tuple5;
//import org.apache.flink.api.java.typeutils.RowTypeInfo;
//import org.apache.flink.streaming.api.datastream.DataStream;
//import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.table.api.Table;
//import org.apache.flink.table.api.TableEnvironment;
//import org.apache.flink.table.api.java.StreamTableEnvironment;
//import org.apache.flink.table.runtime.utils.JavaStreamTestData;
//import org.apache.flink.table.runtime.utils.StreamITCase;
//import org.apache.flink.test.util.AbstractTestBase;
//import org.apache.flink.types.Row;
//
//import java.sql.Timestamp;
//import java.time.Duration;
//
//import static org.apache.flink.table.api.Expressions.$;
//
//import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {

//    @Test
//    public void testSelect() throws Exception {
//        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        StreamTableEnvironment tableEnv = TableEnvironment.getTableEnvironment(env);
//        StreamITCase.clear();
//
//        DataStream<Tuple3<Integer, Long, String>> ds = StreamTestData.getSmall3TupleDataSet(env);
//        Table in = tableEnv.fromDataStream(ds, "a,b,c");
//        tableEnv.registerTable("MyTable", in);
//
//        String sqlQuery = "SELECT * FROM MyTable";
//        Table result = tableEnv.sql(sqlQuery);
//
//        DataStream<Row> resultSet = tableEnv.toAppendStream(result, Row.class);
//        resultSet.addSink(new StreamITCase.StringSink<Row>());
//        env.execute();
//
//        List<String> expected = new ArrayList<>();
//        expected.add("1,1,Hi");
//        expected.add("2,2,Hello");
//        expected.add("3,2,Hello world");
//
//        StreamITCase.compareWithList(expected);
//    }

}