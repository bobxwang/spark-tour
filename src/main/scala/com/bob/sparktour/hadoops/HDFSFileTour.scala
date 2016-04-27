package com.bob.sparktour.hadoops

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs._

object HDFSFileTour {

  val hdfspath = "hdfs://127.0.0.1:9000"

  def main(args: Array[String]) {

    createHDFSFile("/out/bbb", "fuck 51 test u should know")

    var byte: Array[Byte] = readHDFSFile("/user/hive/warehouse/xp/000000_0")
    var content = new String(byte)
    println(content)

    listAll("/")

    uploadLocalFile2HDFS("/Users/bob/Desktop/abc", "/out/abc")

    byte = readHDFSFile("/out/bbb")
    content = new String(byte)
    println(content)

    listAll("/out")

    deleteHDFSFile("/out/bbb")
  }

  /**
   * upload the local file to hdfs
   * notice the file should be the full path
   * @param localFile
   * @param hdfsFile
   */
  def uploadLocalFile2HDFS(localFile: String, hdfsFile: String) = {

    val config: Configuration = new Configuration()
    config.set("fs.defaultFS", hdfspath)
    val hdfs: FileSystem = FileSystem.get(config)

    val src: Path = new Path(localFile)
    val dst: Path = new Path(hdfsFile)

    hdfs.copyFromLocalFile(src, dst)
    hdfs.close
  }

  /**
   * create a new file in HDFS
   * @param createFilePath should be the full path
   * @param content
   */
  def createHDFSFile(createFilePath: String, content: String) = {

    val config: Configuration = new Configuration()
    config.set("fs.defaultFS", hdfspath)
    val hdfs: FileSystem = FileSystem.get(config)
    val os: FSDataOutputStream = hdfs.create(new Path(createFilePath));
    os.write(content.getBytes("UTF-8"));
    os.close();
    hdfs.close();
  }

  /**
   * delete the hdfs file
   * @param dst should be the full path
   * @return
   */
  def deleteHDFSFile(dst: String): Boolean = {
    val config: Configuration = new Configuration()
    config.set("fs.defaultFS", hdfspath)
    val hdfs: FileSystem = FileSystem.get(config)
    val path: Path = new Path(dst)
    val isDeleted = hdfs.delete(path, true)
    hdfs.close()
    isDeleted
  }

  /**
   * read the hdfs file content
   * @param dst should be the full path
   * @return
   */
  def readHDFSFile(dst: String): Array[Byte] = {
    val config: Configuration = new Configuration()
    config.set("fs.defaultFS", hdfspath)
    val hdfs: FileSystem = FileSystem.get(config)

    val path: Path = new Path(dst)
    if (hdfs.exists(path)) {
      val is: FSDataInputStream = hdfs.open(path)
      val stat: FileStatus = hdfs.getFileStatus(path)
      val buffer = new Array[Byte](stat.getLen.toInt)
      is.readFully(0, buffer)
      is.close()
      hdfs.close()
      buffer
    } else {
      throw new Exception("hdfs file is not exist")
    }
  }

  /**
   * 创建一个目录
   * @param dir should be the full path
   */
  def mkdirInHDFS(dir: String) = {
    val config: Configuration = new Configuration()
    config.set("fs.defaultFS", hdfspath)
    val hdfs: FileSystem = FileSystem.get(config)
    hdfs.mkdirs(new Path(dir))
    hdfs.close()
  }

  /**
   * 删除一个目录
   * @param dir
   */
  def deldirInHDFS(dir: String) = {
    val config: Configuration = new Configuration()
    config.set("fs.defaultFS", hdfspath)
    val hdfs: FileSystem = FileSystem.get(config)
    hdfs.delete(new Path(dir), true)
    hdfs.close()
  }

  def listAll(dir: String) = {
    val config: Configuration = new Configuration()
    config.set("fs.defaultFS", hdfspath)
    val hdfs: FileSystem = FileSystem.get(config)
    val stats = hdfs.listStatus(new Path(dir))
    stats.foreach(f => {
      if (f.isFile) println(s"${f.getPath.toString} is a file")
      if (f.isDirectory) println(s"${f.getPath.toString} is a directory")
      if (f.isSymlink) println(s"${f.getPath.toString} is a symlink")
    })
  }

}
