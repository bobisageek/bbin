(ns babashka.bbin.gen-script
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [fipp.edn :as fipp]))

(def bbin-deps (some-> (slurp "deps.edn") edn/read-string :deps))

(def version
  (-> (slurp "deps.edn") edn/read-string
      :aliases :neil :project :version))

(def prelude-template
  (str/triml "
#!/usr/bin/env bb

; :bbin/start
;
; {:coords {:bbin/url \"https://raw.githubusercontent.com/babashka/bbin/v%s/bbin\"}}
;
; :bbin/end

(babashka.deps/add-deps
  '{:deps %s})
"))

(def prelude-str
  (let [lines (-> (with-out-str (fipp/pprint bbin-deps {:width 80})) str/split-lines)]
    (format prelude-template
            version
            (str/join "\n" (cons (first lines) (map #(str "          " %) (rest lines)))))))

(defn gen-script []
  (let [util (slurp "src/babashka/bbin/util.clj")
        scripts (slurp "src/babashka/bbin/scripts.clj")
        cli (slurp "src/babashka/bbin/cli.clj")]
    (spit "bbin" (str/join "\n" [prelude-str
                                 util
                                 scripts
                                 cli]))))
