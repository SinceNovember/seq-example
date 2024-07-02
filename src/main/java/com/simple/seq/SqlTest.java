package com.simple.seq;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * .
 *
 * @author SinceNovember
 * @date 2023/4/27
 */
public class SqlTest {

    public void test() {
        Seq<String> seq = c -> {
            c.accept("1");
            System.out.println("222");
            c.accept("2");
        };

//        Seq<Integer> map = seq.map(Integer::valueOf).take(2);
//        Seq<Integer> filter = map.filter(m -> m == 2);
//        List<Integer> integers = filter.toList();
//        System.out.println(integers);

    }

    public  void test2() {
        int[] a = {2};
        a[0] = a[0]--;
        System.out.println(a[0]);
    }

    /**
     * 讲带下划线的字符串转为驼峰字符
     *
     * @param str str
     * @return
     */
//    static String underscoreToCamel(String str) {
//        UnaryOperator<String> capitalize = s -> s.substring(0, 1).toUpperCase() + s.substring(1)
//            .toLowerCase();
//        //利用生成器构造一个方法的流
//        Seq<UnaryOperator<String>> seq = c -> {
//            //yield第一个小写函数
//            c.accept(String::toLowerCase);
//            while (true) {
//                c.accept(capitalize);
//            }
//
//        };
//        List<String> split = Arrays.asList(str.split("_"));
//        //zip 中的f指代的是上一层seq里的c.accept里面的待消费参数，sub 指的是split迭代器中的一个元素
//        //这边相当于把上面UnaryOperator<String>待消费参数应用到split参数中 (String::toLowerCase, split[i]) -> f.apply(sub)
//        return seq.zip(split, (f, sub) -> f.apply(sub)).join("");
//    }

    /**
     * 测试seq方法流转stream流
     *
     * @return
     */
    static void testSeqToStream() {
        Stream<Integer> stream = StreamBuilderUtils.stream(c -> {
            c.accept(0);
            c.accept(1);

            for (int i = 1; i < 5; i++) {
                c.accept(i);
            }
        });
        List<Integer> collect = stream.collect(Collectors.toList());

        //或者
        Seq<Integer> test = c -> {
            c.accept(0);
            c.accept(1);

            for (int i = 1; i < 5; i++) {
                c.accept(i);
            }
        };
//        List<Integer> collect2 = test.stream().collect(Collectors.toList());
//        System.out.println(test.filter(s -> s != 1).toList());
    }

    public static void testIO() {
//        Seq<String> seq = c -> {
//            try (BufferedReader reader = Files.newBufferedReader(Paths.get("file"))) {
//                String s;
//                while ((s = reader.readLine()) != null) {
//                    c.accept(s);
//                }
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        };
//
//        seq.consume(System.out::println);
    }


    public static void main(String[] args) {
        SqlTest sqlTest = new SqlTest();
        sqlTest.test();



    }

}
