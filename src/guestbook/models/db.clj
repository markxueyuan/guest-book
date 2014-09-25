(ns guestbook.models.db
  (:require [clojure.java.jdbc :as sql])
  (:import java.sql.DriverManager
           (java.util Date)))

(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname "//localhost:3306/sq3"
         :user "root"
         :password "othniel"})

(defn table-exists?
  []
  (with-open [db-con (sql/get-connection db)]
    (let [meta (.getMetaData db-con)
          tables (.getTables meta nil nil nil (into-array ["table"]))
          a (atom #{})]
      (while (.next tables)
        (swap! a conj (.getString tables "TABLE_NAME")))
      (get @a "guestbook"))))

(defn create-guestbook-table
  []
  (sql/with-db-connection [database db]
                          (sql/db-do-commands database
                                              (sql/create-table-ddl
                                                :guestbook
                                                [:id "INTEGER PRIMARY KEY AUTO_INCREMENT"]
                                                [:timestamp "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"]
                                                [:name "TEXT"]
                                                [:message "TEXT"]))
                          (sql/db-do-commands database
                                              "CREATE INDEX timestamp_index ON guestbook (timestamp)")))

(defn read-guests
  []
  (sql/with-db-connection
    [db-spec db]
    (sql/query db-spec
               "select * from guestbook order by timestamp desc"
               :result-set-fn
               doall)))

(defn save-message
  [name message]
  (sql/with-db-connection [db-spec db]
      (sql/insert! db-spec :guestbook
                   {:name name
                    :message message
                    :timestamp (Date.)})))