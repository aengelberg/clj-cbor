(ns clj-cbor.codec-test
  "Decoding tests. Test examples are from RFC 7049 Appendix A."
  (:require
    [clojure.test :refer :all]
    [clj-cbor.core :as cbor]
    [clj-cbor.data.model :as data]
    [clj-cbor.test-utils :refer :all]))


(deftest unsigned-integers
  (testing "direct values"
    (check-roundtrip 0 "00")
    (check-roundtrip 1 "01")
    (check-roundtrip 10 "0A")
    (check-roundtrip 23 "17"))
  (testing "uint8"
    (check-roundtrip 24 "1818")
    (check-roundtrip 25 "1819")
    (check-roundtrip 100 "1864")
    (check-roundtrip 255 "18FF"))
  (testing "uint16"
    (check-roundtrip 256 "190100")
    (check-roundtrip 1000 "1903E8")
    (check-roundtrip 65535 "19FFFF"))
  (testing "uint32"
    (check-roundtrip 65536 "1A00010000")
    (check-roundtrip 1000000 "1A000F4240")
    (check-roundtrip 4294967295 "1AFFFFFFFF"))
  (testing "uint64"
    (check-roundtrip 4294967296 "1B0000000100000000")
    (check-roundtrip 1000000000000 "1B000000E8D4A51000")
    (check-roundtrip 18446744073709551615N "1BFFFFFFFFFFFFFFFF")))


(deftest negative-integers
  (testing "direct values"
    (check-roundtrip -1 "20")
    (check-roundtrip -10 "29")
    (check-roundtrip -24 "37"))
  (testing "int8"
    (check-roundtrip -25 "3818")
    (check-roundtrip -100 "3863")
    (check-roundtrip -256 "38FF"))
  (testing "int16"
    (check-roundtrip -257 "390100")
    (check-roundtrip -1000 "3903E7")
    (check-roundtrip -65536 "39FFFF"))
  (testing "int32"
    (check-roundtrip -65537 "3A00010000")
    (check-roundtrip -1000000 "3A000F423F")
    (check-roundtrip -4294967296 "3AFFFFFFFF"))
  (testing "int64"
    (check-roundtrip -4294967297 "3B0000000100000000")
    (check-roundtrip -18446744073709551616 "3BFFFFFFFFFFFFFFFF")))


(deftest byte-strings
  (is (= "40" (encoded-hex (byte-array 0))))
  (is (data/bytes= [] (decode-hex "40")))
  (is (= "4401020304" (encoded-hex (byte-array [1 2 3 4]))))
  (is (data/bytes= [1 2 3 4] (decode-hex "4401020304")))
  (is (data/bytes= [1 2 3 4 5] (decode-hex "5F42010243030405FF"))
      "streaming byte string"))


(deftest text-strings
  (check-roundtrip "" "60")
  (check-roundtrip "a" "6161")
  (check-roundtrip "IETF" "6449455446")
  (check-roundtrip "\"\\" "62225C")
  (check-roundtrip "\u00fc" "62C3BC")
  (check-roundtrip "\u6c34" "63E6B0B4")
  (check-roundtrip "\ud800\udd51" "64F0908591")
  (is (= "streaming" (decode-hex "7F657374726561646D696E67FF"))
      "streaming text string"))


(deftest data-arrays
  (check-roundtrip [] "80")
  (check-roundtrip [1 2 3] "83010203")
  (check-roundtrip [1 [2 3] [4 5]] "8301820203820405")
  (check-roundtrip (range 1 26) "98190102030405060708090A0B0C0D0E0F101112131415161718181819")
  (testing "streaming"
    (is (= [] (decode-hex "9FFF")))
    (is (= [1 [2 3] [4 5]] (decode-hex "9F018202039F0405FFFF")))
    (is (= [1 [2 3] [4 5]] (decode-hex "9F01820203820405FF")))
    (is (= [1 [2 3] [4 5]] (decode-hex "83018202039F0405FF")))
    (is (= [1 [2 3] [4 5]] (decode-hex "83019F0203FF820405")))
    (is (= (range 1 26) (decode-hex "9F0102030405060708090A0B0C0D0E0F101112131415161718181819FF")))))


(deftest data-maps
  (check-roundtrip {} "A0")
  (check-roundtrip {1 2, 3 4} "A201020304")
  (check-roundtrip {"a" 1, "b" [2 3]} "A26161016162820203")
  (check-roundtrip ["a" {"b" "c"}] "826161A161626163")
  (check-roundtrip {"a" "A", "b" "B", "c" "C", "d" "D", "e" "E"}
                   "A56161614161626142616361436164614461656145")
  (testing "streaming"
    (is (= {"a" 1, "b" [2 3]} (decode-hex "BF61610161629F0203FFFF")))
    (is (= ["a" {"b" "c"}] (decode-hex "826161BF61626163FF")))
    (is (= {"Fun" true, "Amt" -2} (decode-hex "BF6346756EF563416D7421FF")))))



#_
(deftest floating-point-numbers
  (testing "special values"
    (check-roundtrip 0.0 "F90000")
    (check-roundtrip -0.0 "F90000")
    (check-roundtrip Float/NaN "F97E00")
    (check-roundtrip Float/POSITIVE_INFINITY "F97C00")
    (check-roundtrip Float/NEGATIVE_INFINITY "F9FC00"))
  (testing "single-precision"
    (check-roundtrip (float 100000.0) "FA47C35000")
    (check-roundtrip (float 3.4028234663852886e+38) "FA7F7FFFFF"))
  (testing "double-precision"
    (check-roundtrip 1.1 "FB3FF199999999999A")
    #_(is (= "FB7E37E43C8800759C" (encoded-hex 1.0e+300)))
    (check-roundtrip -4.1 "FBC010666666666666")))
(deftest floating-point-numbers
  (testing "half-precision"
    (is (instance? Float (decode-hex "F90000")))
    (is (= 0.0 (decode-hex "F90000")))
    (is (= -0.0 (decode-hex "F98000")))
    (is (= 1.0 (decode-hex "F93C00")))
    (is (= 1.5 (decode-hex "F93E00")))
    (is (= 65504.0 (decode-hex "F97BFF")))
    (is (= 5.960464477539063e-8 (decode-hex "F90001")))
    (is (= 0.00006103515625 (decode-hex "F90400")))
    (is (= -4.0 (decode-hex "F9C400")))
    (is (Float/isNaN (decode-hex "F97E00")))
    (is (= Float/POSITIVE_INFINITY (decode-hex "F97C00")))
    (is (= Float/NEGATIVE_INFINITY (decode-hex "F9FC00"))))
  (testing "single-precision"
    (is (instance? Float (decode-hex "FA47C35000")))
    (is (= 100000.0 (decode-hex "FA47C35000")))
    (is (= 3.4028234663852886e+38 (decode-hex "FA7F7FFFFF")))
    (is (Float/isNaN (decode-hex "FA7FC00000")))
    (is (= Float/POSITIVE_INFINITY (decode-hex "FA7F800000")))
    (is (= Float/NEGATIVE_INFINITY (decode-hex "FAFF800000"))))
  (testing "double-precision"
    (is (instance? Double (decode-hex "FB7FF8000000000000")))
    (is (= 1.1 (decode-hex "FB3FF199999999999A")))
    (is (= 1.0e+300 (decode-hex "FB7E37E43C8800759C")))
    (is (= -4.1 (decode-hex "FBC010666666666666")))
    (is (Double/isNaN (decode-hex "FB7FF8000000000000")))
    (is (= Double/POSITIVE_INFINITY (decode-hex "FB7FF0000000000000")))
    (is (= Double/NEGATIVE_INFINITY (decode-hex "FBFFF0000000000000")))))


(deftest simple-values
  (testing "special primitives"
    (check-roundtrip false "F4")
    (check-roundtrip true "F5")
    (check-roundtrip nil "F6")
    (check-roundtrip data/undefined "F7"))
  (testing "generic values"
    (check-roundtrip (data/simple-value 16)  "F0")
    (check-roundtrip (data/simple-value 32)  "F820")
    (check-roundtrip (data/simple-value 255) "F8FF")))