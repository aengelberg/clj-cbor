(ns clj-cbor.tags.time
  "Built-in tag support for the time extensions in RFC 7049. See section
  2.4.1.

  This namespace offers interop with both the older `java.util.Date` class as
  well as the newer `java.time.Instant`. Support for both timestamp-based
  tagged values and the more efficient epoch-based values is included."
  (:require
    [clj-cbor.data.model :as data])
  (:import
    java.time.Instant
    java.time.format.DateTimeFormatter
    (java.util
      Date
      TimeZone)))


;; ## Epoch Formatting

(defn- tagged-epoch-time
  [epoch-millis]
  (data/tagged-value 1
    (if (zero? (mod epoch-millis 1000))
      (long (/ epoch-millis 1000))
      (/ epoch-millis 1000.0))))


(defn format-instant-epoch
  [^Instant value]
  (tagged-epoch-time (.toEpochMilli value)))


(defn format-date-epoch
  [^Date value]
  (tagged-epoch-time (.getTime value)))


(def time-epoch-formatters
  "Map of date-time types to render as tag 1 epoch offsets."
  {Date format-date-epoch
   Instant format-instant-epoch})



;; ## String Formatting

(defn format-instant-string
  [^Instant value]
  (data/tagged-value 0
    (.format DateTimeFormatter/ISO_INSTANT value)))


(defn format-date-string
  [^Date value]
  (format-instant-string (.toInstant value)))


(def time-string-formatters
  "Map of date-time types to render as tag 0 time strings."
  {Date format-date-string
   Instant format-instant-string})



;; ## Instant Parsing

(defn parse-string-instant
  [tag value]
  ;DateTimeFormatter/ISO_INSTANT
  (Instant/parse value))


(defn parse-epoch-instant
  [tag value]
  (Instant/ofEpochMilli (long (* value 1000))))


(def instant-handlers
  "Map of tag handlers to parse date-times as `java.time.Instant` values."
  {0 parse-string-instant
   1 parse-epoch-instant})



;; ## Date Parsing

(defn parse-epoch-date
  [tag value]
  (Date. (long (* value 1000))))


(defn parse-string-date
  [tag value]
  (Date/from (parse-string-instant tag value)))


(def date-handlers
  "Map of tag handlers to parse date-times as `java.util.Date` values."
  {0 parse-string-date
   1 parse-epoch-date})
