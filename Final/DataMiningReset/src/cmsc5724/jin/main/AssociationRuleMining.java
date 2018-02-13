package cmsc5724.jin.main;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import cmsc5724.jin.utils.Utils;

public class AssociationRuleMining {
    
    /***************************************************************/
    /***WARNING!!!!!!***** 本程序运用反射技术,所需参数*****************/
    /***请勿修改代码********在config.properties中修改*****************/
    /***************************************************************/

    /***************************************************************/
    /******** 使用前请仔细阅读本文档 ***********************************/
    /***************************************************************/

    /**
     * 使用本程序前请安装MySql数据库，端口为3306，数据库名称为datamining。 
     * 请确保打开MySql服务。 
     * 请将用户名设置为root，密码设置为34551255.
     * 请用下方代码创建表。
     *  将网页下载的asso.csv文件导入。(打包好了,test1是PPT的例子,exercise是练习9的习题) 
     * 在项目根目录下建立lib文件夹，如果有就不需要建立了。
     * 将压缩包内的MySql驱动黏贴进lib文件夹。 
     * 右击此驱动选择buildPath. 
     * 在程序开始前指定各种参数.
     * 
     * 然后就可以在主程序运行了,输出结果在控制台上(Console)。
     * 
     * ps：将PPT中的例子导入此表命名为“test1”有惊喜哦！！
     * 
     * ps2:此demo可以处理含有item<=8的transaction,每个item的取值范围(1,999).transactions的数量不限.
     */

    /**
     *  创建表代码
                    CREATE TABLE `transactions` (
                      `id` int(11) NOT NULL,
                      `item1` int(11) DEFAULT NULL,
                      `item2` int(11) DEFAULT NULL,
                      `item3` int(11) DEFAULT NULL,
                      `item4` int(11) DEFAULT NULL,
                      `item5` int(11) DEFAULT NULL,
                      `item6` int(11) DEFAULT NULL,
                      `item7` int(11) DEFAULT NULL,
                      `item8` int(11) DEFAULT NULL,
                      PRIMARY KEY (`id`)
                    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
     */

    /***************************************************************/
    /*********** 文档结束,请在config.properties中修改参数 *************/
    /***************************************************************/

    public static void main(String[] args) throws SQLException {
        long start = System.currentTimeMillis();
        
        /* ↓↓↓↓↓↓↓↓↓↓↓↓需要指定的内容↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ */
        // 指定support和confidence的值
        int supportNum = 0;
        double confidenceDecide = 0;

        // MySql数据库的地址,账户名及密码
        String address = null;
        String user = null;
        String password = null;

        // 所用表的名称"transactions","test1"
        String mySQL = null;
        /* ↑↑↑↑↑↑↑↑↑↑↑↑需要指定的内容↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑ */

        //读取配置文件
        Properties prop = new Properties();

        try {
            prop.load(AssociationRuleMining.class.getClassLoader().getResourceAsStream("config.properties"));

            supportNum = Integer.parseInt(prop.getProperty("supportNum"));
            confidenceDecide = Double.parseDouble(prop.getProperty("confidenceDecide"));
            address = prop.getProperty("address");
            user = prop.getProperty("user");
            password = prop.getProperty("password");
            mySQL = prop.getProperty("mySQL");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("读取配置文件出错啦~~囧~~");
        }

        if ("test1".equals(mySQL)) {
            supportNum = 3;
        }

        // 从数据库取得的原始数据
        // key:value~~value~~
        Map<Integer, String> originData = Utils.getData(mySQL, address, user, password);
        System.out.println();
        System.out.println("准备好了吗?要开始输出被支持的itemsets和合格的推荐了哦~~~");
        System.out.println();
        
        
//        int count1and49 = 0;
//        int count1 = 0;
//        for (Map.Entry<Integer, String> en : originData.entrySet()) {
//            if(en.getValue().contains("start1end")&&en.getValue().contains("start49end")){
//                count1and49++;
//            }
//            if(en.getValue().contains("start1end")){
//               count1++;
//            }
//        }
//        System.out.println(count1and49+":::::"+count1);
       
        // 存放item全组合后的数据,key是transaction的Id,value是组合后的可能,也就是itemSet."@@@@"是每个元素组合的隔断,"start","end"是元素的起始,"begin""finish"是元素组合的所有可能的起始
        // key:beginstart3end@@@@start4end@@@@start3endstart4end@@@@start5end@@@@start3endstart5end@@@@start4endstart5end@@@@finish
        Map<Integer, String> itemSets = new HashMap<Integer, String>();
        String sum = null;
        // 循环每条transaction,存入itemSets.
        Utils.findAllItemSets(originData, itemSets, sum);
        // for (Map.Entry<Integer, String> entry : itemSets.entrySet()){
        // System.out.println("我是Id::"+entry.getKey()+"!!我是组合::"+entry.getValue());
        // }
        sum = originData.get(-1);
        // 检查每个组合的support是否合格,某个id的元素组合
        // key存的是I,value存的是支持度
        Map<String, Integer> I1 = new HashMap<String, Integer>();// 注:这里的I1其实I,实在懒得改了..//exercise I没问题
        Utils.checkSupport(originData, itemSets, supportNum, I1);
        // 上面这行代码运行完后,itemSets里全是support的Itemset
        
        Map<String, Integer> I = new HashMap<String, Integer>();//I是被支持的Itemsets,不含id版的.
        for (Map.Entry<String, Integer> entry : I1.entrySet()) {
//          System.out.println("我是被支持的I::" + entry.getKey() + "!!我是支持数::" + entry.getValue());
          String str = entry.getKey();
          String subStr = str.substring(0, str.indexOf("~~~~"));
          if(!"".equals(subStr)&&subStr!=null){
              I.put(subStr, entry.getValue());
          }
        }
        System.out.println();
        System.err.println("size为1的itemsets来了!!!");
        System.out.println();
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Map.Entry<String, Integer> en: I.entrySet()) {
            if(en.getKey().length()>=9&&en.getKey().length()<=11){
                String str = en.getKey();
                str = str.replaceAll("start", "(");
                str = str.replaceAll("end", ")");
                System.out.println(str+"的支持度::"+en.getValue());
            }
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println();
        System.err.println("size为2的itemsets来了!!!");
        System.out.println();
        
        for (Map.Entry<String, Integer> en: I.entrySet()) {
            if(en.getKey().length()>=18&&en.getKey().length()<=22){
                String str = en.getKey();
                str = str.replaceAll("start", "(");
                str = str.replaceAll("end", ")");
                System.out.println(str+"的支持度::"+en.getValue());
            }
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println();
        System.err.println("size为3的itemsets来了!!!");
        System.out.println();
        
        for (Map.Entry<String, Integer> en: I.entrySet()) {
            if(en.getKey().length()>=27&&en.getKey().length()<=33){
                String str = en.getKey();
                str = str.replaceAll("start", "(");
                str = str.replaceAll("end", ")");
                System.out.println(str+"的支持度::"+en.getValue());
            }
        }
        System.out.println();System.out.println();
        System.err.println("别着急~程序正在运行中...");
//        for (Map.Entry<Integer,String> entry : itemSets.entrySet()) {
//          System.out.println("我是id::" + entry.getValue() + "!!我是剩下的被支持的I::" + entry.getKey());
//      }
//        int I = 0;
//        int I11 =0;
//         for (Map.Entry<String, Integer> entry : I1.entrySet()) {
////             System.out.println("我是被支持的I::" + entry.getKey() + "!!我是支持数::" + entry.getValue());
////             int I = 0;
////             int I11 =0;
//             if(entry.getKey().substring(0,entry.getKey().indexOf("~~~~")).equals("start1endstart49end")||entry.getKey().substring(0,entry.getKey().indexOf("~~~~")).equals("start49endstart1end")){
//                 System.out.println("我是被支持的I::" + entry.getKey() + "!!我是支持数::" + entry.getValue());
//                 I = entry.getValue();
//             }
//             if(entry.getKey().substring(0,entry.getKey().indexOf("~~~~")).equals("start1end")){
//                 System.out.println("我是被支持的I::" + entry.getKey() + "!!我是支持数::" + entry.getValue());
//                 I11 = entry.getValue();
//             }
//         }
//         if(I!=0&&I11!=0){
//             DecimalFormat df = new DecimalFormat("0.000000");
//             String confidence = df.format((float) I / I11);
//             System.err.println("1-->49的confidence为：："+confidence);
//         }

        // start1endstart2endstart4end~~~~5id
        HashSet<String> hs = new HashSet<String>();//start1endstart4end~~~~3id3个id的都在说明这里没问题
        // 寻找I1
        Utils.findI1(I1, hs);
        // 将I1放入String strs[]中.
        Iterator<String> iterator = hs.iterator();
        String strs[] = new String[hs.size()];//start1endstart4end~~~~3id3个id的都在说明这里没问题//exercise这里I1没问题
        int count = 0;
        while (iterator.hasNext()) {
            String string = (String) iterator.next();
//             System.out.println("组合出来的I1::" + string);
            strs[count] = string;
            count++;
        }

//         for (String string : strs) {
//             if(!"".equals(string)){
//                 System.out.println("没有重复的I1::"+string);
//             }
//         }
        HashSet<String> result = new HashSet<String>();//exercise这里检查没问题
        // I::start2endstart4end~~~~4id
        // 计算confidence
        Utils.computeConfidence(strs, result, I1);
//        for (String string : result) {
//            if(!"".equals(string)){
//                System.out.println(string);
//            }
//        }
        HashSet<String> withoutIdResult = new HashSet<String>();
        // 为PPT特别准备了个输出
        if ("test1".equals(mySQL)) {
            Utils.forBeer(result,withoutIdResult);
        } else {
            try {
                Utils.forDemo(result, confidenceDecide,withoutIdResult);
            } catch (InterruptedException e) {

                e.printStackTrace();
                System.out.println("打印出错了");
            }
        }
        System.out.println();
        System.out.println();
        System.out.println("上面的是带Id的完整版");
        System.out.println("下面我要输出不带Id的简洁版了哦~~~");
        
        System.out.println();
        System.out.println();
        System.out.println();
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        Iterator<String> it = withoutIdResult.iterator();
        int efficientAR =0;
        while (it.hasNext()) {
            String string = (String) it.next();
//            String str1 = string.substring(0, string.indexOf("此条推荐来自于id为"));
//            String str2 = string.substring(string.indexOf("此条推荐的conf"));
//            if((str1+str2).contains("()")){
//                continue;
//            }
            System.err.println(string);
            efficientAR++;
        }
        
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        System.out.println();
        if ("test1".equals(mySQL)) {
            System.err.println("The Association Rule的条数为：："+efficientAR+"条。");
        } else {
            System.err.println("confidence大于等于"+confidenceDecide+"的The Association Rule的条数为：："+efficientAR+"条。");
        }
        System.out.println();
        System.err.println("本程序运行完了!!!");
        System.out.println();
           
        System.out.println("本次使用的support值是：："+supportNum);
        System.out.println();
        System.out.println("本次使用的confidence值是：："+confidenceDecide);
        System.out.println();

        System.out.println("本次分析的数据总条数为::" + sum+"条.");
        System.out.println();

        long end = System.currentTimeMillis();
        System.err.println("本程序运行时间为::" + (end - start-4000) + "毫秒.(ps:扣除了主动休眠的4000毫秒)");
        
        System.out.println();
        System.out.println("谢谢观赏!");
        
        

    }

}
