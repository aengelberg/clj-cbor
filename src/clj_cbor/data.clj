(ns clj-cbor.data
  "Type definitions and keyword identifiers for CBOR data types.")


(def major-types
  "Vector of major type keywords, indexed by the three-bit values 0-7. (§2.1)"
  [:unsigned-integer
   :negative-integer
   :byte-string
   :text-string
   :data-array
   :data-map
   :tagged-value
   :simple-value])


(def length-information-types
  "Map of keywords designating the types of additional information to the
  numeric value."
  {:uint8      24
   :uint16     25
   :uint32     26
   :uint64     27
   :indefinite 31})


(def simple-information-types
  "Set of keywords designating the types of additional information."
  {:false             20
   :true              21
   :null              22
   :undefined         23
   :simple-value-byte 24
   :float16           25
   :float32           26
   :float64           27
   :break             31})


(def break
  "Value of the break code."
  0xFF)



;; ## Undefined Value

(deftype Undefined
  [_meta]

  Object

  (toString
    [this]
    "undefined")

  (equals
    [this that]
    (boolean (or (identical? this that) (instance? Undefined that))))

  (hashCode
    [this]
    (hash (class this)))


  clojure.lang.IObj

  (meta [this] _meta)

  (withMeta
    [this meta-map]
    (Undefined. meta-map)))


; Remove automatic constructor function.
(ns-unmap *ns* '->Undefined)


(def undefined
  "Base singleton undefined value."
  (Undefined. nil))


(defn undefined*
  "Constructs a new undefined value with metadata about why it has been
  constructed."
  [reason-key & {:as data}]
  (Undefined. {:cbor/cause (assoc data :type reason-key)}))


(defn undefined?
  "Predicate which returns true if `x` is a CBOR undefined value."
  [x]
  (instance? Undefined x))



;; ## Simple Values

(deftype SimpleValue
  [^long n _meta]

  Object

  (toString
    [this]
    (str "simple(" n ")"))

  (equals
    [this that]
    (boolean (or (identical? this that)
                 (and (instance? SimpleValue that)
                      (= n (.n ^SimpleValue that))))))

  (hashCode
    [this]
    (hash [(class this) n]))


  clojure.lang.IObj

  (meta [this] _meta)

  (withMeta
    [this meta-map]
    (SimpleValue. n meta-map)))


; Remove automatic constructor function.
(ns-unmap *ns* '->SimpleValue)


(defn simple-value
  "Constructs a simple type for the given number."
  [^long n]
  (case n
    20 false
    21 true
    22 nil
    23 undefined
    (SimpleValue. n nil)))


(defn boolean?
  [x]
  (or (true? x) (false? x)))


(let [byte-array-class (class (byte-array 0))]
  (defn byte-array?
    [x]
    (instance? byte-array-class x)))