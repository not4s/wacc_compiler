package lib

import java.util.Arrays
import kotlin.Array

class boothAlgorithm(multiplicand: Int, multiplier: Int) {

    var br_list = arrayListOf<Int>()
    var qr_list = arrayListOf<Int>()
    var qrn = 4
    var br = Array<Int>(qrn) {r -> r * 0}
    var qr = Array<Int>(qrn) {r -> r * 0}
    var mt = Array<Int>(qrn) {r -> r * 0}
    var sc = qrn

    init {
        for(digit in multiplicand.toString(radix = 2)) {
            br_list.add(digit.digitToInt())
        }

        for(digit in multiplier.toString(radix = 2)) {
            qr_list.add(digit.digitToInt())
        }
        mt = br_list.toTypedArray()
        complement(mt, qrn)
        br_list.reverse()
        qr_list.reverse()
        br = br_list.toTypedArray()
        qr = br_list.toTypedArray()
    }
    

    // function to perform adding in the accumulator
    fun add(ac: Array<Int>, x: Array<Int>): Array<Int> {
        var c = 0

        for(i in 0..x.size) {
 
            // ACC = ACC + BR
            ac[i] = ac[i] + x[i] + c;
            
            // carry the excess bit to the next digit
            if (ac[i] > 1) {
                ac[i] = ac[i] % 2;
                c = 1;
            } else {
                c = 0;
            }
        }
        return ac
    }

    // function to find the number's complement
    fun complement(a: Array<Int>, n: Int): Array<Int> {
        var x = Array<Int>(qrn) {r -> r * 0}
        x[0] = 1
 
        for (i in 0..n) a[i] = (a[i] + 1) % 2
        return add(a, x);
    }

    // function ro perform right shift
    fun rightShift(ac: Array<Int>): Int {
        var temp = ac[0]
 
        for (i in 0..qrn-1) {
            ac[i] = ac[i + 1]
            qr[i] = qr[i + 1]
        }
        qr[qrn - 1] = temp
        return qr[0]
    }

    fun evaluate(): Int {
        var qn = 0;
        var ac = Array<Int>(10) {r -> r * 0}
        var temp = 0;
        var result = ""
 
        while (sc != 0) {
 
            // SECOND CONDITION
            if ((qn + qr[0]) == 1) {
                if (temp == 0) {
 
                    // subtract BR from accumulator
                    ac = add(ac, mt);
                    temp = 1;
                }

                // THIRD CONDITION
                else if (temp == 1) {
                    // add BR to accumulator
                    ac = add(ac, br);
                    temp = 0;
                }
            qn = rightShift(ac);
            }

            // FIRST CONDITION
            else if (qn - qr[0] == 0) {
                qn = rightShift(ac);
            }

            // decrement counter
            sc--;
        }
        for(i in 0..qr.size) {
            result += qr[i]
        }
        return result.toInt(2)
    }
}