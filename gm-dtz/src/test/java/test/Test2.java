package test;

import com.sy.mainland.util.CommonUtil;
import com.sy.mainland.util.SecretUtil;
import com.sy.mainland.util.SecretUtil2;
import com.sy.util.StringUtil;

import java.util.*;


/**
 * Created by pc on 2017/4/20.
 */
public class Test2 {
    public static void main(String[] args) {
//        System.out.println(StringUtil.loadWeekRange(2017,12));
        try {
            String a = "4f8751f5202be94d20c3ef06d1513b8c";
            String a1= SecretUtil2.decrypt(a,"yhnufoYHNIO");
            System.out.println(a1);
//            String a2= SecretUtil.decrypt(a1,null);
//            System.out.println(a2);
//
//            a1= SecretUtil2.encrypt("root",null);
//            System.out.println(a1);
//            a2= SecretUtil2.decrypt(a1,null);
//            System.out.println(a2);

//            Map<Long,Object> map=new HashMap<>();
//
//            map.put(1L,"a");
//            map.put(new Long(1),"b");
//
//            map.put(3000L,"c");
//
//            map.put(3000L,"d");
//            map.put(new Long(300),"e");
//            System.out.println(map);
//            List<Map<String, Object>> subResult=new ArrayList<>();
//            Map<String, Object> map1=new HashMap<>();
//            map1.put("mycount",300);
//            subResult.add(map1);
//            map1=new HashMap<>();
//            map1.put("mycount",500);
//            subResult.add(map1);
//            map1=new HashMap<>();
//            map1.put("mycount",800);
//            subResult.add(map1);
//            Collections.sort(subResult, new Comparator<Map<String, Object>>() {
//                @Override
//                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
//                    return CommonUtil.object2Int(o2.get("mycount"))-CommonUtil.object2Int(o1.get("mycount"));
//                }
//            });
//            System.out.println(subResult);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


}
