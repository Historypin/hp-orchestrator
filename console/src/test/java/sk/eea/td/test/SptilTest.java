package sk.eea.td.test;

import org.junit.Test;

import java.util.Arrays;

public class SptilTest {

    @Test
    public void splitTest(){
        String filename = "asdasd";
        System.out.println(Arrays.toString(filename.split("\\.", 2)));


    }
}
