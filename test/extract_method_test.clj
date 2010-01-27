(ns extract_method_test
  (:use clojure_refactoring.extract_method clojure_refactoring.core clojure.test clojure.contrib.str-utils))

(defn fixture [f]
  (def start-src
       "(defn add [s] (reduce + (map #(Integer. %) s)))")
  (def end-src
       "(defn add-numbers [s] (reduce + (map #(Integer. %) s)))

(defn add [s] (add-numbers s))")
(def extract-string "(reduce + (map #(Integer. %) s))")
(def helper (extract-method start-src
                         extract-string
                         "add-numbers"))
(def f-string (str "(defn add [s a] " extract-string ")"))
(f))


(use-fixtures :once fixture)

(deftest extract_method
  (is (= helper
         end-src)))

(deftest removes_unused_args
  (is (= (extract-method f-string
                       extract-string
                       "add-numbers")
       (str "(defn add-numbers [s] " extract-string ")

(defn add [s a] (add-numbers s))"))))


(deftest uses_variables_from_let
  (is (= (extract-method
          "(defn add [s]
(let [a 1] (+ a 1)))" "(+ a 1)" "add-number")
"(defn add-number [a] (+ a 1))

(defn add [s]
(let [a 1] (add-number a)))")))


(deftest works_in_list_comprehensions
	(is (= (extract-method
                "(defn add [s]
(for [x (re-split #\",\" s)] (Integer. x)))"
"(Integer. x)"
"to-i")
"(defn to-i [x] (Integer. x))

(defn add [s]
(for [x (re-split #\",\" s)] (to-i x)))")
)
(is (= (extract-method
        "(defn add [s]
(for [x (re-split #\",\" s)] (Integer. x)))"
"(re-split #\",\" s)"
"split-string")
"(defn split-string [s] (re-split #\",\" s))

(defn add [s]
(for [x (split-string s)] (Integer. x)))"
)))

(deftest no_duplicated_bindings
  (is (= (extract-method
          "(defn a [s] (if (.contains s \",\") 1 s))"
          "(if (.contains s \",\") 1 s)"
          "b")
"(defn b [s] (if (.contains s \",\") 1 s))\n\n(defn a [s] (b s))")))