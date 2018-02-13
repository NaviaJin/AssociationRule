package cmsc5724.jin.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class Utils {

    /**
     * 传入一个item的字符串数组,返回所有可能的itemset,并将所有itemset都存入一个ArrayList
     * 
     * @param str
     * @param al
     */
    public static void comb(String[] str, ArrayList<String> al) {
        int len = str.length;
        int nbits = 1 << len;
        for (int i = 0; i < nbits - 1; ++i) {
            int t;
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < len; j++) {
                t = 1 << j;
                if ((t & i) != 0) { // 与运算，同为1时才会是1
                    // System.out.print(str[j]);
                    // str[j]=str[j].trim();
                    // if(str[j]!=null&&str[j]!=""){
                    // sb.append(str[j]);
                    // }
                    if(str[j]!=""){
                        sb.append("start" + str[j] + "end");
                    }

                }
            }
            // System.out.println();
            if (sb.toString() != null && !"".equals(sb.toString())) {
                al.add(sb.toString());
            }
            // sb.append("~~~~");
        }
        // System.out.println("~~~~");
        // return sb;
    }
    
    /**
     * 传入一个item的字符串数组,返回所有可能的itemset,并将所有itemset都存入一个ArrayList
     * 
     * @param str
     * @param al
     */
    public static void comb(String[] str, HashSet<String> hs,String id) {
        int len = str.length;
        int nbits = 1 << len;
        for (int i = 0; i < nbits - 1; ++i) {
            int t;
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < len; j++) {
                t = 1 << j;
                if ((t & i) != 0) { // 与运算，同为1时才会是1
                    // System.out.print(str[j]);
                    // str[j]=str[j].trim();
                    // if(str[j]!=null&&str[j]!=""){
                    // sb.append(str[j]);
                    // }
                    if(str[j]!=""){
                        sb.append(str[j]);
                    }

                }
            }
            // System.out.println();
            if (sb.toString() != null && !"".equals(sb.toString())) {
                sb.append(id);
                hs.add(sb.toString());
            }
            // sb.append("~~~~");
        }
        // System.out.println("~~~~");
        // return sb;
    }

    /**
     * 存放从数据库中读取出来的数据,key是transaction的Id,value是item,用"start","end"包裹 key=-1,value存放的是transaction的总数
     * 
     * @return
     * @throws SQLException
     */
    public static Map<Integer, String> getData(String mysqlName,String address,String user,String password) {
        try {
            // 与数据库建立连接
            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
            Connection conn = DriverManager.getConnection(address, user,password);
            System.out.println(conn.getClass().getName());
            Statement stmt = conn.createStatement();

            System.err.println("滴~~~~~数据库读取成功~");

            // 从数据库查询数据
            ResultSet rs = stmt.executeQuery("select * from "+mysqlName);
            Map<Integer, String> map = new HashMap<Integer, String>();
            int sum = 0;
            while (rs.next()) {
                sum = sum + 1;

                int transactionCount = 8;
                if (rs.getObject("item8") == null) {
                    transactionCount = 7;
                }
                if (rs.getObject("item7") == null) {
                    transactionCount = 6;
                }
                if (rs.getObject("item6") == null) {
                    transactionCount = 5;
                }
                if (rs.getObject("item5") == null) {
                    transactionCount = 4;
                }
                if (rs.getObject("item4") == null) {
                    transactionCount = 3;
                }
                if (rs.getObject("item3") == null) {
                    transactionCount = 2;
                }
                if (rs.getObject("item2") == null) {
                    transactionCount = 1;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= transactionCount; i++) {
                    sb.append(String.valueOf("start" + rs.getObject("item" + i)) + "end");
                }
                map.put((int) rs.getObject("id"), sb.toString());
            }
            map.put(-1, String.valueOf(sum));
            return map;
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.out.println("读取数据库出错了~囧~");
        }
        return null;
    }

    /**
     * 
     * @param originData 从数据库取得的原始数据
     * @param itemSets 存放item全组合后的数据,key是transaction的Id,value是组合后的可能,也就是itemSet
     * @param sum 总数据条数
     */
    public static void findAllItemSets(Map<Integer, String> originData, Map<Integer, String> itemSets,
            String sum) {
        for (Map.Entry<Integer, String> e : originData.entrySet()) {
            if (e.getKey() == -1) {
                // System.out.println("总共"+e.getValue()+"条数据");
                sum = e.getValue();
            } else {
                String items = e.getValue();
                String item[] = items.split("end");
                for (int i = 0; i < item.length; i++) {
                    item[i] = item[i].substring(5);
                }
                // al里面放的是一个transaction里面的所有组合.
                ArrayList<String> al = new ArrayList<String>();
                Utils.comb(item, al);
                //
                StringBuilder sb = new StringBuilder();
                sb.append("begin");
                for (String string : al) {
                    // System.out.println(string);
                    sb.append(string + "@@@@");
                }
                sb.append("finish");
                // System.out.println(sb.toString());
                itemSets.put(e.getKey(), sb.toString());
            }
        }
    }

    /**
     * 传入一个字符串和特征字符symble,字符串要符合下列规则,则可以吧@@@@和@@@@之间的含有symbol的字符串删掉.
     * 
     * @param itemSets
     * @param symbol
     * @return
     */
    // beginstart3end@@@@start4end@@@@start3endstart4end@@@@start5end@@@@start3endstart5end@@@@start4endstart5end@@@@finish
    public static String deleteUnsupport(String itemSets, String symbol) {
        int lastIndexOf = itemSets.lastIndexOf("finish");
        itemSets = itemSets.substring(5, lastIndexOf);
        String itemSet[] = itemSets.split("@@@@");
        ArrayList<Integer> al = new ArrayList<Integer>();
        for (int i = 0; i < itemSet.length; i++) {
            if (!itemSet[i].contains(symbol)) {
                al.add(i);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("begin");
        for (Integer integer : al) {
            sb.append(itemSet[integer] + "@@@@");
        }
        sb.append("finish");
        itemSets = sb.toString();
        return itemSets;

    }

    public static void checkSupport(Map<Integer, String> originData, Map<Integer, String> itemSets,
            int supportNum, Map<String, Integer> I1) {
        for (Map.Entry<Integer, String> entry : itemSets.entrySet()) {
            // 某个元素组合的所有可能
            int id = entry.getKey();
//            System.out.println("现在遍历的是id为:" + id + "的transaction");
            String itemSets1 = entry.getValue();
            int lastIndexOf = itemSets1.lastIndexOf("finish");
            itemSets1 = itemSets1.substring(5, lastIndexOf);
            // System.out.println("我是Id::"+entry.getKey()+"!!我是所有组合::"+itemSets1);
            // itemSets2:是单个元素组合的数组
            String[] itemSets2 = itemSets1.split("@@@@");
            for (String string : itemSets2) {
//                System.out.println("单个组合::" + string + "~~");
                // itemSet放的是一个transaction中可能的组合,纯的
                String[] itemSet = string.split("nd");
                for (int i = 0; i < itemSet.length; i++) {
                    // itemSet[i] = itemSet[i].substring(5);
//                    System.out.println(itemSet[i] + "!!!!!!!!!!!!");
                }
                String str1 = null;
                String str2 = null;
                String str3 = null;
                String str4 = null;
                String str5 = null;
                String str6 = null;
                String str7 = null;
                String str8 = null;
                if (itemSet.length == 8) {
                    str1 = itemSet[0];
                    str2 = itemSet[1];
                    str3 = itemSet[2];
                    str4 = itemSet[3];
                    str5 = itemSet[4];
                    str6 = itemSet[5];
                    str7 = itemSet[6];
                    str8 = itemSet[7];
                }
                if (itemSet.length == 7) {
                    str1 = itemSet[0];
                    str2 = itemSet[1];
                    str3 = itemSet[2];
                    str4 = itemSet[3];
                    str5 = itemSet[4];
                    str6 = itemSet[5];
                    str7 = itemSet[6];
                }
                if (itemSet.length == 6) {
                    str1 = itemSet[0];
                    str2 = itemSet[1];
                    str3 = itemSet[2];
                    str4 = itemSet[3];
                    str5 = itemSet[4];
                    str6 = itemSet[5];
                }
                if (itemSet.length == 5) {
                    str1 = itemSet[0];
                    str2 = itemSet[1];
                    str3 = itemSet[2];
                    str4 = itemSet[3];
                    str5 = itemSet[4];
                }
                if (itemSet.length == 4) {
                    str1 = itemSet[0];
                    str2 = itemSet[1];
                    str3 = itemSet[2];
                    str4 = itemSet[3];
                }
                if (itemSet.length == 3) {
                    str1 = itemSet[0];
                    str2 = itemSet[1];
                    str3 = itemSet[2];
                }
                if (itemSet.length == 2) {
                    str1 = itemSet[0];
                    str2 = itemSet[1];
                }
                if (itemSet.length == 1) {
                    str1 = itemSet[0];
                }

                int count = 0;
                // 遍历原始数据,查每个itemSet的支持度~
                for (Map.Entry<Integer, String> e : originData.entrySet()) {
                    String transaction = e.getValue();
                    // transactionItem[]装的是一个transaction原始数据中的每个元素
//                    System.out.println(transaction + "~~~!!~~~");
                    if (str8 == null && str7 == null && str6 == null && str5 == null && str4 == null
                            && str3 == null && str2 == null && str1 != null) {
                        if (transaction.contains(str1)) {
                            count = count + 1;
                        }
                    }
                    if (str8 == null && str7 == null && str6 == null && str5 == null && str4 == null
                            && str3 == null && str2 != null && str1 != null) {
                        if (transaction.contains(str1) && transaction.contains(str2)) {
                            count = count + 1;
                        }
                    }
                    if (str8 == null && str7 == null && str6 == null && str5 == null && str4 == null
                            && str3 != null && str2 != null && str1 != null) {
                        if (transaction.contains(str1) && transaction.contains(str2)
                                && transaction.contains(str3)) {
                            count = count + 1;
                        }
                    }
                    if (str8 == null && str7 == null && str6 == null && str5 == null && str4 != null
                            && str3 != null && str2 != null && str1 != null) {
                        if (transaction.contains(str1) && transaction.contains(str2)
                                && transaction.contains(str3) && transaction.contains(str4)) {
                            count = count + 1;
                        }
                    }
                    if (str8 == null && str7 == null && str6 == null && str5 != null && str4 != null
                            && str3 != null && str2 != null && str1 != null) {
                        if (transaction.contains(str1) && transaction.contains(str2)
                                && transaction.contains(str3) && transaction.contains(str4)
                                && transaction.contains(str5)) {
                            count = count + 1;
                        }
                    }
                    if (str8 == null && str7 == null && str6 != null && str5 != null && str4 != null
                            && str3 != null && str2 != null && str1 != null) {
                        if (transaction.contains(str1) && transaction.contains(str2)
                                && transaction.contains(str3) && transaction.contains(str4)
                                && transaction.contains(str5) && transaction.contains(str6)) {
                            count = count + 1;
                        }
                    }
                    if (str8 == null && str7 != null && str6 != null && str5 != null && str4 != null
                            && str3 != null && str2 != null && str1 != null) {
                        if (transaction.contains(str1) && transaction.contains(str2)
                                && transaction.contains(str3) && transaction.contains(str4)
                                && transaction.contains(str5) && transaction.contains(str6)
                                && transaction.contains(str7)) {
                            count = count + 1;
                        }
                    }
                    if (str8 != null && str7 != null && str6 != null && str5 != null && str4 != null
                            && str3 != null && str2 != null && str1 != null) {
                        if (transaction.contains(str1) && transaction.contains(str2)
                                && transaction.contains(str3) && transaction.contains(str4)
                                && transaction.contains(str5) && transaction.contains(str6)
                                && transaction.contains(str7) && transaction.contains(str8)) {
                            count = count + 1;
                        }
                    }
                }
                if (count >= supportNum) {
//                    System.out.println("这个itemSet::" + string + "是被支持的!!!!支持度为" + count);
                    I1.put(string+"~~~~"+entry.getKey()+"id", count);
                } else {
//                    System.out.println("这个itemSet::" + string + "挂了~~~~~`");
                    // TUDO
                    // 要把挂了的存一下,然后去map里把含有这个的删掉对应的id
                    // 根据id从itemSets中查出itemSet1
                    String itemSet1 = itemSets.get(id);
                    // 将itemSet1中含有支持度不够的string的itemSet删掉
                    itemSet1 = Utils.deleteUnsupport(itemSet1, string);
                    itemSets.put(id, itemSet1);
                }

            }
        }
    }
    /**
     * 传入一个严查过support的map,存放的key-value是transaction的id-id对应的元素组合.
     * 返回一个Map
     * key__beginstart3endfinish==>beginstart1endstart2endstart4endfinish
     * value__beginstart3endstart1endstart2endstart4endfinish
     * @param itemSets
     * @return
     */
    public static Map<String, String> combineI1AndI2(Map<Integer, String> itemSets){
        Map<String, String> aR = new HashMap<String, String>();
//      Map<Integer, String> aR1 = new HashMap<Integer, String>();
      //beginstart3end@@@@finish==>beginstart1endstart2endstart4end@@@@finish
      //beginstart3end@@@@start1endstart2endstart4end@@@@finish
      for (Map.Entry<Integer, String> ei : itemSets.entrySet()) {
          String itemSets1 = ei.getValue();
           int lastIndexOf = itemSets1.lastIndexOf("finish");
           String str1 = itemSets1.substring(5, lastIndexOf);
           String item[]= str1.split("@@@@");
           for (int i = 0; i < item.length; i++) {
              for (int j = 0; j < item.length; j++) {
                  if (i==j) {
                      continue;
                  }
                  aR.put("begin"+item[i]+"finish==>begin"+item[j]+"finish","begin"+ item[i]+item[j]+"finish");
//                  aR1.put(ei.getKey(), "begin"+ item[i]+"@@@@"+item[j]+"@@@@finish");
              }
          }
      }
      return aR;
    } 
    /**
     * 名字清晰,无需多言
     * @param originData
     * @param aR
     * @param supportNum
     * @param I1AndI2
     */
    public static void checkSupport1(Map<Integer, String> originData, Map<String, String> aR, int supportNum,
            Map<String, Integer> I1AndI2) {
        for (Map.Entry<String, String> entry : aR.entrySet()) {
            // 某个元素组合的所有可能
            String id = entry.getKey();
//            System.out.println("111现在遍历的是id为:" + id + "的transaction");
            String itemSets1 = entry.getValue();
//            System.out.println(itemSets1);
            int lastIndexOf = itemSets1.lastIndexOf("finish");
            itemSets1 = itemSets1.substring(5, lastIndexOf);
            // System.out.println("我是Id::"+entry.getKey()+"!!我是所有组合::"+itemSets1);
            // itemSets2:是单个元素组合的数组
//            String[] itemSets2 = itemSets1.split("end");
//            // start3endstart1endstart2endstart4end
//            for (int i = 0; i < itemSets2.length; i++) {
//                itemSets2[i] = itemSets2[i].substring(5);
//            }

//            for (String string : itemSets2) {
//                System.out.println("单个组合::" + itemSets1 + "~~");
                // itemSet放的是一个transaction中可能的组合,纯的
              //单个组合::start1endstart1endstart2end~~
                String[] itemSet = itemSets1.split("nd");
//                for (int i = 0; i < itemSet.length; i++) {
//                    itemSet[i] = itemSet[i].substring(5);
//              }
                //[start1e][start1e][start2e]
                //数组去重,优化
//                for (int i = 0; i < itemSet.length; i++) {
//                    // itemSet[i] = itemSet[i].substring(5);
//                    System.out.println(itemSet[i] + "!!!!!!!!!!!!");
//                }
                String str1 = null;
                String str2 = null;
                String str3 = null;
                String str4 = null;
                String str5 = null;
                String str6 = null;
                String str7 = null;
                String str8 = null;
                if (itemSet.length == 8) {
                    str1 = itemSet[0];
                    str2 = itemSet[1];
                    str3 = itemSet[2];
                    str4 = itemSet[3];
                    str5 = itemSet[4];
                    str6 = itemSet[5];
                    str7 = itemSet[6];
                    str8 = itemSet[7];
                }
                if (itemSet.length == 7) {
                    str1 = itemSet[0];
                    str2 = itemSet[1];
                    str3 = itemSet[2];
                    str4 = itemSet[3];
                    str5 = itemSet[4];
                    str6 = itemSet[5];
                    str7 = itemSet[6];
                }
                if (itemSet.length == 6) {
                    str1 = itemSet[0];
                    str2 = itemSet[1];
                    str3 = itemSet[2];
                    str4 = itemSet[3];
                    str5 = itemSet[4];
                    str6 = itemSet[5];
                }
                if (itemSet.length == 5) {
                    str1 = itemSet[0];
                    str2 = itemSet[1];
                    str3 = itemSet[2];
                    str4 = itemSet[3];
                    str5 = itemSet[4];
                }
                if (itemSet.length == 4) {
                    str1 = itemSet[0];
                    str2 = itemSet[1];
                    str3 = itemSet[2];
                    str4 = itemSet[3];
                }
                if (itemSet.length == 3) {
                    str1 = itemSet[0];
                    str2 = itemSet[1];
                    str3 = itemSet[2];
                }
                if (itemSet.length == 2) {
                    str1 = itemSet[0];
                    str2 = itemSet[1];
                }
                if (itemSet.length == 1) {
                    str1 = itemSet[0];
                }

                int count = 0;
                // 遍历原始数据,查每个itemSet的支持度~
                for (Map.Entry<Integer, String> e : originData.entrySet()) {
                    String transaction = e.getValue();
                    // transactionItem[]装的是一个transaction原始数据中的每个元素
//                    System.out.println(transaction + "~~~!!~~~");
                    if (str8 == null && str7 == null && str6 == null && str5 == null && str4 == null
                            && str3 == null && str2 == null && str1 != null) {
                        if (transaction.contains(str1)) {
                            count = count + 1;
                        }
                    }
                    if (str8 == null && str7 == null && str6 == null && str5 == null && str4 == null
                            && str3 == null && str2 != null && str1 != null) {
                        if (transaction.contains(str1) && transaction.contains(str2)) {
                            count = count + 1;
                        }
                    }
                    if (str8 == null && str7 == null && str6 == null && str5 == null && str4 == null
                            && str3 != null && str2 != null && str1 != null) {
                        if (transaction.contains(str1) && transaction.contains(str2)
                                && transaction.contains(str3)) {
                            count = count + 1;
                        }
                    }
                    if (str8 == null && str7 == null && str6 == null && str5 == null && str4 != null
                            && str3 != null && str2 != null && str1 != null) {
                        if (transaction.contains(str1) && transaction.contains(str2)
                                && transaction.contains(str3) && transaction.contains(str4)) {
                            count = count + 1;
                        }
                    }
                    if (str8 == null && str7 == null && str6 == null && str5 != null && str4 != null
                            && str3 != null && str2 != null && str1 != null) {
                        if (transaction.contains(str1) && transaction.contains(str2)
                                && transaction.contains(str3) && transaction.contains(str4)
                                && transaction.contains(str5)) {
                            count = count + 1;
                        }
                    }
                    if (str8 == null && str7 == null && str6 != null && str5 != null && str4 != null
                            && str3 != null && str2 != null && str1 != null) {
                        if (transaction.contains(str1) && transaction.contains(str2)
                                && transaction.contains(str3) && transaction.contains(str4)
                                && transaction.contains(str5) && transaction.contains(str6)) {
                            count = count + 1;
                        }
                    }
                    if (str8 == null && str7 != null && str6 != null && str5 != null && str4 != null
                            && str3 != null && str2 != null && str1 != null) {
                        if (transaction.contains(str1) && transaction.contains(str2)
                                && transaction.contains(str3) && transaction.contains(str4)
                                && transaction.contains(str5) && transaction.contains(str6)
                                && transaction.contains(str7)) {
                            count = count + 1;
                        }
                    }
                    if (str8 != null && str7 != null && str6 != null && str5 != null && str4 != null
                            && str3 != null && str2 != null && str1 != null) {
                        if (transaction.contains(str1) && transaction.contains(str2)
                                && transaction.contains(str3) && transaction.contains(str4)
                                && transaction.contains(str5) && transaction.contains(str6)
                                && transaction.contains(str7) && transaction.contains(str8)) {
                            count = count + 1;
                        }
                    }
                }
                if (count >= supportNum) {
//                    System.out.println("这个itemSetI1AndI2::" + id + "是被支持的!!!!支持度为" + count);
                    I1AndI2.put(id, count);
                } else {
                    System.out.println("这个itemSetI1AndI2::" + id + "挂了~~~~~`");
                    // TUDO
                    // 要把挂了的存一下,然后去map里把含有这个的删掉对应的id
                    // 根据id从itemSets中查出itemSet1
                    // String itemSet1 = aR.get(id);
                    // 将itemSet1中含有支持度不够的string的itemSet删掉
                    // itemSet1 = Utils.deleteUnsupport(itemSet1, string);
                    // aR.put(id, itemSet1);
                }

//            }
        }
    }
    
    /**
     * 计算confidence
     * @param I1AndI2
     * @param I1
     * @return
     */
    public static  Map<String,String> computeConfidence(Map<String,Integer> I1AndI2 ,Map<String,Integer> I1){
        Map<String,String> aRRs = new HashMap<String, String>();
        for (Map.Entry<String, Integer> eI1AndI2: I1AndI2.entrySet()) {
            //beginstart1endfinish==>beginstart1endstart2endfinish!!我是支持::4
            String str1 = eI1AndI2.getKey(); 
            String I1InI1AndI2 = str1.substring(0, str1.indexOf("==>"));
            int lastIndexOf = I1InI1AndI2.lastIndexOf("finish");
            I1InI1AndI2 = I1InI1AndI2.substring(5, lastIndexOf);
            
            int supportI1AndI2 = eI1AndI2.getValue();
            for (Map.Entry<String, Integer> eI1: I1.entrySet()) {
                String str = eI1.getKey();
                if (str.equals(I1InI1AndI2)) {
                    int supportI1 = eI1.getValue();
                    DecimalFormat df=new DecimalFormat("0.00");
                    String confidence = df.format((float)supportI1AndI2/supportI1);
//                    String confidence =String.valueOf(supportI1AndI2)+"/"+String.valueOf(supportI1) ;
                    aRRs.put(eI1AndI2.getKey(), confidence);
                }
            }
        }
        return aRRs;
    }
    /**
     * 为PPT特别准备的结果.
     * @param result
     * @param confidenceDecide
     */
    
    public static void forBeer(Map<String,String> aRRs){
        String products[] = {"beer","bread","butter","milk","potato","onion"};
        for (Map.Entry<String, String> entry : aRRs.entrySet()) {
            //beginstart3endstart4endfinish==>beginstart1endstart4endfinish
//            System.err.println("这条AssociationRule::"+entry.getKey()+"的confidence是::"+entry.getValue());
            //beginstart3endstart4endfinish和beginstart1endstart4endfinish
            String str = entry.getKey();
            str = str.replaceAll("begin", "");
            str = str.replaceAll("finish", "");
            str = str.replaceAll("1", "beer");
            str = str.replaceAll("2", "bread");
            str = str.replaceAll("3", "butter");
            str = str.replaceAll("4", "milk");
            str = str.replaceAll("5", "potato");
            str = str.replaceAll("6", "onion");
            str = str.replaceAll("start", "(");
            str = str.replaceAll("end", ")");
            String[] str1 = str.split("==>");
            System.err.println("您本次购买的商品是::"+str1[0]+". 推荐给您的商品是::"+str1[1]+".此条推荐的confidence是::"+entry.getValue());
        }
    }
    /**
     * 为PPT特别准备的结果.
     * @param result
     * @param confidenceDecide
     */
    
    public static void forBeer(HashSet<String> result,HashSet<String> withoutIdResult){
        String products[] = {"beer","bread","butter","milk","potato","onion"};
        //您本次购买的商品是::start1end. 推荐给您的商品是::start4end~~~~3id.此条推荐的confidence是::0.75
        Iterator<String> it = result.iterator();
        while (it.hasNext()) {
            String string = (String) it.next();
            
            String id = string.substring(string.indexOf("~~~~"), string.indexOf("id"));
            int idStartIndex=string.indexOf("~~~~");
            string = string.replaceAll(id, "");
            
            int confidenceIndex = string.indexOf("fidence");
            String confidence = string.substring(confidenceIndex, string.length());
            string = string.replaceAll(confidence, "");
            
            
            string = string.replaceAll("1", "beer");
            string = string.replaceAll("2", "bread");
            string = string.replaceAll("3", "butter");
            string = string.replaceAll("4", "milk");
            string = string.replaceAll("5", "potato");
            string = string.replaceAll("6", "onion");
            
            string = string.replaceAll("id.", ".此条推荐来自于id为"+id+"的transaction.");
            string = string.replaceAll("此条推荐的con", "此条推荐的con"+confidence);
            string = string.replaceAll("start", "(");
            string = string.replaceAll("end", ")");
            string = string.replaceAll("~~~~", "(");
            string = string.replaceAll("的transaction.", ")的transaction.");
            System.err.println(string);
            int start = string.indexOf("此条推荐来自于id为");
            int end = string.lastIndexOf(")的transaction.此条推荐的con");
            String delete = string.substring(start+11, end);
           
            string =string.replaceFirst(delete, "");
            withoutIdResult.add(string);
        }
       
        
    }
    /**
     * 普通的输出结果
     * @param result
     * @param confidenceDecide
     */
    public static void forDemo(Map<String,String> aRRs){
//        String products[] = {"beer","bread","butter","milk","potato","onion"};
        for (Map.Entry<String, String> entry : aRRs.entrySet()) {
            //beginstart9endfinish==>beginstart1endfinish
            String str = entry.getKey();
            str = str.replaceAll("begin", "");
            str = str.replaceAll("finish", "");
            str = str.replaceAll("start", "(");
            str = str.replaceAll("end", ")");
            String[] str1 = str.split("==>");
            System.err.println("您本次购买的商品是::"+str1[0]+". 推荐给您的商品是::"+str1[1]+".此条推荐的confidence是::"+entry.getValue());
        }
    }
    
    /**
     * 普通的输出结果
     * @param result
     * @param confidenceDecide
     * @throws InterruptedException 
     */
    public static void forDemo(HashSet<String> result,double confidenceDecide,HashSet<String> withoutIdResult) throws InterruptedException{
        Iterator<String> it = result.iterator();
        while (it.hasNext()) {
            //您本次购买的商品是::start12end. 推荐给您的商品是::start16end~~~~946id.此条推荐的confidence是::0.86
            String string = (String) it.next();
            
            String id = string.substring(string.indexOf("~~~~"), string.indexOf("id"));
            int idStartIndex=string.indexOf("~~~~");
            string = string.replaceAll(id, "");
            
            int confidenceIndex = string.indexOf("fidence");
            String confidence = string.substring(confidenceIndex, string.length());
            String confidenceNum = confidence.substring(10);
            string = string.replaceAll(confidence, "");
            
            string = string.replaceAll("id.", ".此条推荐来自于id为"+id+"的transaction.");
            string = string.replaceAll("此条推荐的con", "此条推荐的con"+confidence);
            string = string.replaceAll("start", "(");
            string = string.replaceAll("end", ")");
            string = string.replaceAll("~~~~", "(");
            string = string.replaceAll("的transaction.", ")的transaction.");
            if (Double.parseDouble(confidenceNum)>= confidenceDecide) {
                //您本次购买的商品是::(14). 推荐给您的商品是::(12).此条推荐来自于id为(312)的transaction.此条推荐的confidence是::0.91
                System.err.println(string);
                
               
                
                int end1 = string.indexOf("此条推荐来自于id为");
                int start2 = string.lastIndexOf("此条推荐的con");
                String str1 = string.substring(0, end1);
                String str2 = string.substring(start2);
               
               
                
//                int start = string.indexOf("此条推荐来自于id为");
//                int end = string.lastIndexOf(")的transaction.此条推荐的con");
//                String delete = string.substring(start+11, end);
//               
//                string =string.replaceFirst(delete, "");
                
                
                withoutIdResult.add(str1+str2);
            }
            
           
             
        }
  }
    /**
     * 函数名很清晰了吧
     * @param strs 存放的I1
     * @param result 将合格的结果存放进去
     * @param I1   存放的I
     */
    public static void  computeConfidence(String[] strs, HashSet<String> result , Map<String, Integer> I1){
        for (int i = 0; i < strs.length; i++) {
            String strI1AndId = strs[i];
            String strI1Id = strs[i].substring(strs[i].indexOf("~~~~"));
            int lastIndexOf = strs[i].lastIndexOf("~~~~");
            String strI1 = strs[i].substring(0, lastIndexOf);
            int supportI1 = 0;
            //通过I查I1的support
            for (Map.Entry<String, Integer> entry : I1.entrySet()) {// 这里的I1其实是I
                if (entry.getKey().equals(strI1AndId)) {
                    supportI1 = entry.getValue();
                    break;
                }
            }
            //根据id找到对应的I计算confidence
            for (Map.Entry<String, Integer> entry : I1.entrySet()) {// 这里的I1其实是I
                String strIAndId = entry.getKey();
                String strIId = strIAndId.substring(strIAndId.indexOf("~~~~"));
                int lastIndexOf1 = strIAndId.lastIndexOf("~~~~");
                String strI = strIAndId.substring(0, lastIndexOf1);
                int count = 0 ;
                String[] stri2 = strI1.split("nd");
                for (int k = 0; k < stri2.length; k++) {
                    stri2[k] = stri2[k] + "nd";
                    if (strIAndId.contains(stri2[k])) {
                        count++;
                    }
                }
                
                if (count == stri2.length &&  !strI.equals(strI1)) {
                    int supportI1AndI2 = entry.getValue();
                    DecimalFormat df = new DecimalFormat("0.00");
                    String confidence = df.format((float) supportI1AndI2 / supportI1);
                    String[] stri1 = strI1.split("nd");
                    for (int j = 0; j < stri1.length; j++) {
                        stri1[j] = stri1[j] + "nd";
                        if (strIAndId.contains(stri1[j])) {

                            strIAndId = strIAndId.replaceAll(stri1[j], "");
                        }
                    }
                    // strIAndId代表的是I2
                    // System.out.println("您本次购买的商品是::"+strI1+".
                    // 推荐给您的商品是::"+strIAndId+".此条推荐的confidence是::"+confidence);
                    result.add("您本次购买的商品是::" + strI1 + ". 推荐给您的商品是::" + strIAndId + ".此条推荐的confidence是::"
                            + confidence);

                }
            }
        }
        
    }
    /**
     * 通过传入I找I的全部能分裂出来的I1,参数I1就是I,打错...不想改...hs中装的就是真正的I1
     * @param I1
     * @param hs
     */
    public static void findI1(Map<String, Integer> I1,HashSet<String> hs){
        for (Map.Entry<String, Integer> en : I1.entrySet()) {
            String itemSet = en.getKey();
            // ~~~~5id
            String id = itemSet.substring(itemSet.indexOf("~~~~"));
            // start1endstart2endstart4end
            itemSet = itemSet.substring(0, itemSet.lastIndexOf("~~~~"));
            // int lastIndexOf = itemSet.lastIndexOf("finish");
            // itemSet = itemSet.substring(5, lastIndexOf);
            // String[] availableElement = itemSet.split("nd");
            // String[] availableElement = itemSet.split("nd");

            if (itemSet.length() == 9 || itemSet.length() == 10 || itemSet.length() == 10) {
                itemSet = "";
            }
            if (itemSet == null || "".equals(itemSet)) {
                continue;
            }
            // start1e和start2e和start4e
            String[] availableElement = itemSet.split("nd");
            for (int i = 0; i < availableElement.length; i++) {
                availableElement[i] = availableElement[i] + "nd";
            }

            // [start1end,start2end,start4end]
            if (itemSet != null) {
                Utils.comb(availableElement, hs, id);
            }
        }
    }

    public static String[] stringArrayTrim(String[] str1) {
        int count = 0;
        for (Object o : str1) {
            if (o != null) {
                count = count + 1;
            }
        }
        String[] str = new String[count];
        int m = 0;
        for (int i = 0; i < str1.length; i++) {
            if (str1[i] != null) {
                str[m] = str1[i];
                m = m + 1;
            }
        }
        //
        // for (String string : str) {
        // System.out.println(string);
        // }

        return str;

    }
}
