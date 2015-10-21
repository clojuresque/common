;-
; Copyright 2009-2013 © Meikel Brandmeyer.
; All rights reserved.
; 
; Permission is hereby granted, free of charge, to any person obtaining a copy
; of this software and associated documentation files (the "Software"), to deal
; in the Software without restriction, including without limitation the rights
; to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
; copies of the Software, and to permit persons to whom the Software is
; furnished to do so, subject to the following conditions:
; 
; The above copyright notice and this permission notice shall be included in
; all copies or substantial portions of the Software.
; 
; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
; IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
; FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
; AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
; LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
; OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
; THE SOFTWARE.

(ns clojuresque.util
  (:import
    clojure.lang.LineNumberingPushbackReader)
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]))

(defn namespace-of-file
  [file]
  (let [of-interest '#{ns clojure.core/ns in-ns clojure.core/in-ns}
        eof         (Object.)
        input       (LineNumberingPushbackReader. (io/reader file))
        in-seq      (take-while #(not (identical? % eof))
                                (repeatedly #(read input false eof)))
        candidate   (first
                      (drop-while
                        #(or (not (instance? clojure.lang.ISeq %))
                             (not (contains? of-interest (first %))))
                        in-seq))]
    (when candidate
      (second candidate))))

(defn namespaces
  [files]
  (distinct (keep namespace-of-file files)))

(defn safe-require
  [& nspaces]
  (binding [*unchecked-math*     *unchecked-math*
            *warn-on-reflection* *warn-on-reflection*]
    (apply require nspaces)))

(defn resolve-required
  [fully-qualified-sym]
  (let [slash  (.indexOf ^String fully-qualified-sym "/")
        nspace (symbol (subs fully-qualified-sym 0 slash))
        hfn    (symbol (subs fully-qualified-sym (inc slash)))]
    (safe-require nspace)
    (ns-resolve nspace hfn)))

(defmacro deftask
  [task-name & fntail]
  `(let [driver# (fn ~(symbol (str (name task-name) "-task-driver"))
                   ~fntail)]
     (defn ~task-name
       []
       (let [options# (edn/read (LineNumberingPushbackReader. *in*))]
         (driver# options#)))))
