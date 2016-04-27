package com.bob.sparktour.hadoops

import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, TableName}

import scala.collection.JavaConversions._

object HBASETour {

  val tableName = "blog"
  val family: Array[String] = Array("article", "author")
  val conf = HBaseConfiguration.create
  conf.set("hbase.zookeeper.quorum", "localhost")
  val conn: Connection = ConnectionFactory.createConnection(conf)

  def main(args: Array[String]) {
    creatTable(tableName, family)
  }

  /**
   *
   * @param tableName
   * @param rowKey
   * @param familyName
   * @param columnName
   * @param value
   */
  def updateTable(tableName: String, rowKey: String, familyName: String,
                  columnName: String, value: String): Unit = {
    val table: Table = conn.getTable(TableName.valueOf(tableName))
    val put = new Put(Bytes.toBytes(rowKey));
    put.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(columnName),
      Bytes.toBytes(value));
    table.put(put);
  }


  /**
   * 查询表中某一列
   * @param tableName
   * @param rowKey
   * @param familyName
   * @param columnName
   */
  def getResultByColumn(tableName: String, rowKey: String, familyName: String, columnName: String): Unit = {
    val table: Table = conn.getTable(TableName.valueOf(tableName))
    val get: Get = new Get(Bytes.toBytes(rowKey))
    get.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(columnName))
    get.setMaxVersions() // we can set the version which we want to get
    val rs: Result = table.get(get)
    rs.listCells().foreach(x => {
      println(s"value is ${Bytes.toString(x.getValueArray)}")
    })
  }

  /**
   * 返回指定表指定rowkey的值
   * @param rowKey
   * @param tableName
   * @return
   */
  def getData(rowKey: String, tableName: String): Result = {
    val get: Get = new Get(Bytes.toBytes(rowKey))
    val table: Table = conn.getTable(TableName.valueOf(tableName))
    val result: Result = table.get(get)
    result.listCells().foreach(x => {
      println(s"value is ${Bytes.toString(x.getValueArray)}")
    })
    result
  }

  /**
   * 遍历查询hbase表
   *
   * @param tableName
   */
  def getDataScan(tableName: String) = {
    val table: Table = conn.getTable(TableName.valueOf(tableName))
    val scan: Scan = new Scan()
    val rs: ResultScanner = table.getScanner(scan)
    rs.foreach(r => {
      r.listCells().foreach(c => {
        println(s"value is ${Bytes.toString(c.getValueArray)}")
      })
    })

  }

  /**
   * 适合知道有多少列族的固定表
   * @param rowKey
   * @param tableName
   * @param column1
   * @param value1
   * @param column2
   * @param value2
   */
  def addData(rowKey: String, tableName: String, column1: Array[String], value1: Array[String],
              column2: Array[String], value2: Array[String]): Unit = {

    val put: Put = new Put(Bytes.toBytes(rowKey))
    val table: Table = conn.getTable(TableName.valueOf(tableName))
    val columnFamilies: Array[HColumnDescriptor] = table.getTableDescriptor.getColumnFamilies
    columnFamilies.foreach(x => {
      val familyName = x.getNameAsString
      familyName match {
        case "article" => {
          column1.zip(value1).foreach(x => {
            put.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(x._1), Bytes.toBytes(x._2))
          })
        }
        case "author" => {
          column2.zip(value2).foreach(x => {
            put.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(x._1), Bytes.toBytes(x._2))
          })
        }
        case _ => {

        }
      }
    })

    table.put(put)
    println("add data is done")
  }


  def createOrOverwrite(admin: Admin, table: HTableDescriptor) = {
    if (admin.tableExists(table.getTableName)) {
      admin.disableTable(table.getTableName)
      admin.deleteTable(table.getTableName)
    }
    admin.createTable(table)
  }

  def creatTable(tableName: String, family: Array[String]) {

    val admine: Admin = conn.getAdmin

    val table: HTableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
    family.foreach(x => table.addFamily(new HColumnDescriptor(x)))

    println("creating table...")
    createOrOverwrite(admine, table)
    println("creating done...")

    println("begining to add data to table")
    val column1: Array[String] = Array("title", "content", "tag")
    val value1: Array[String] = Array("Head First HBase", "HBase is the Hadoop database. Use it when you need random, realtime read/write access to your Big Data.", "Hadoop,HBase,NoSQL")
    val column2 = Array("name", "nickname")
    val value2 = Array("nicholas", "lee")
    addData("rowkey1", tableName, column1, value1, column2, value2)
    println("ended to add data to table")

    println("begin to query")
    getData("rowkey1", tableName)
    println("end to query")

    getDataScan(tableName)

    getResultByColumn(tableName, "rowkey1", "author", "name");

    updateTable(tableName, "rowkey1", "author", "name", "bin");

    getResultByColumn(tableName, "rowkey1", "author", "name");
  }
}