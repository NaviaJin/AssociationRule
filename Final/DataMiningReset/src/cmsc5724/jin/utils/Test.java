package cmsc5724.jin.utils;

import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.Map;

import org.junit.Before;

public class Test {

    

    @org.junit.Test
    public void test() throws SQLException {
        String str = "abc";
        String str1 ="def";
        String str2 = str+str1;
        System.out.println(str2);
    }

}
