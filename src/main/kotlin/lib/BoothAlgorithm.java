package lib;

public class BoothAlgorithm {

    public static int evaluate(int m1, int m2) {

        String m = getbinary(m1);
        String r = getbinary(m2);

        int x = m.length();
        int y = r.length();
        
        // Initialise A
        String A = m;
        for (int i = 0; i <= y; i++) {
            A = A + "0";
        }

        // Initialise S
        String S = get2scomplement(m.substring(1));

        if (m1 <= 0) {
            S = "0" + S;
        } else {
            S = "1" + S;
        }

        for (int i = 0; i <= y; i++) {
            S = S + "0";
        }

        // Initialise P
        String P = "";
        for (int i = 0; i < x; i++) {
            P += "0";
        }
        P = P + r + "0";

        for (int i = 1; i <= y; i++) {
            // The value of P after P + A and right shift for each iteration
            if (P.substring(P.length() - 2).equals("01")) {
                P = binaryaddn(P, A);
                P = shiftright(P);
            }
            // The value of P after P + S and right shift
            else if (P.substring(P.length() - 2).equals("10")) {
                P = binaryaddn(P, S);
                P = shiftright(P);
            }
            // The value of P after right shift
            else {
                P = shiftright(P);
            }
        }

        P = P.substring(0, P.length() - 1);
        if (P.charAt(0) == '0') {
            int i;
            for (i = 0; i < P.length(); i++) {
                // leftmost zeroes are removed to get decimal eqv to avoid redundancy
                if (P.charAt(i) == '0') {       
                    continue;
                }
                break;
            }
            // this means that the string consists of zeroes only.
            if (i == P.length()) { 
                
                return 0;
            }
            // after removing leading zeroes
            return binarytodec(P.substring(i));
        }
        
        else {
            // MSB = 1, means that it is negative
            return binarytodec(get2scomplement(P));
        }

    }

    public static String get2scomplement(String s1) {

        String s2 = "";
        for (int i = 0; i < s1.length(); i++) { // flipping the bits

            if (s1.charAt(i) == '0') {
                s2 += '1';
                continue;
            }
            s2 += '0';
        }

        String s3 = "";
        String carry = "1";
        for (int i = s2.length() - 1; i >= 0; i--) {

            if (s2.charAt(s2.length() - 1) == '0') {
                s2 = s2.substring(0, s2.length() - 1) + "1";
                return s2;
            }

            if (s2.charAt(i) == '1' && carry.equals("1")) {
                s3 = '0' + s3;
                carry = "1";
            } else if (s2.charAt(i) == '0' && carry.equals("1")) {
                s3 = '1' + s3;
                carry = "0";
            } else if (s2.charAt(i) == '0' && carry.equals("0")) {
                s3 = '0' + s3;
                carry = "0";
            } else {
                s3 = '1' + s3;
                carry = "0";
            }

        }

        return s3;

    }

    public static String getbinary(int n) {

        String str1 = "";

        // n is positive
        str1 = Integer.toBinaryString(n);
        str1 = "0" + str1;
        if (n < 0) {
            // n is negative
            str1 = "-" + str1;
            str1 = get2scomplement(str1);
            str1 = "1" + str1;
        }
        return str1;

    }

    public static String shiftright(String str) {

        return str.charAt(0) + str.substring(0, str.length() - 1);

    }

    public static String binaryaddn(String s1, String s2) {

        String res = "";
        String carry = "0";
        for (int i = s1.length() - 1; i >= 0; i--) {

            if (s1.charAt(i) == '1' && s2.charAt(i) == '1' && carry.equals("0")
                    || (s1.charAt(i) == '0' && s2.charAt(i) == '1' && carry.equals("1"))
                    || s1.charAt(i) == '1' && s2.charAt(i) == '0' && carry.equals("1")) {
                res = '0' + res;
                carry = "1";
            } else if (s1.charAt(i) == '1' && s2.charAt(i) == '1' && carry.equals("1")) {
                res = '1' + res;
                carry = "1";
            } else if (s1.charAt(i) == '0' && s2.charAt(i) == '1' && carry.equals("0")
                    || s1.charAt(i) == '1' && s2.charAt(i) == '0' && carry.equals("0")
                    || s1.charAt(i) == '0' && s2.charAt(i) == '0' && carry.equals("1")) {
                res = '1' + res;
                carry = "0";
            } else if (s1.charAt(i) == '0' && s2.charAt(i) == '0' && carry.equals("0")) {
                res = '0' + res;
                carry = "0";
            }

        }
        return res;
    }

    public static int binarytodec(String s) {
        return Integer.parseInt(s, 2);
    }
}
