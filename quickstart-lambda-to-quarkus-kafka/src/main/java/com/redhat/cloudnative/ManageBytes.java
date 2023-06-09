package com.redhat.cloudnative;

import java.util.Arrays;

public class ManageBytes {

public static void main(String[] args) {
        System.out.println("I am a Geek");

        String str0 = "\n";
        byte[] byteArr = str0.getBytes();
        // print the byte[] elements
        System.out.println("String to byte array: " + Arrays.toString(byteArr));


        byte[] byteArray = { 'P', 'A', 'N', 'K', 'A', 'J' };
        byte[] byteArray1 = { 80, 65, 78, 75, 65, 74 };
        byte[] byteArray2 = {0, 0, 0, 4, -78, 17, 11, -109, 0, 0, 37, -89, 0, 0, 0, 0, 0, 0, 0, 3, 10, 45, 91, 18, -80, 40, 52, 23, 0, 0, 1, 107, 43, -31, 4, 8, 0, 0, 0, 1, 6, 8, -78, 17, 11, -109, 8, 8, 0, 0, 37, -89, 12, 32, -44, -114, -2, -106, 67, 9, -5, 125, 126, -126, -115, -76, -80, 25, 86, 28, 14, 16, 126, 100, -112, 22, 13, -92, -5, 109};

        String str = new String(byteArray);
        String str1 = new String(byteArray1);
        String str2 = new String(byteArray2);

        System.out.println(str);
        System.out.println(str1);
        System.out.println(str2);

}

}