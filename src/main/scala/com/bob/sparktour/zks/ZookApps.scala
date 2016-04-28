package com.bob.sparktour.zks

import java.util.concurrent.TimeUnit

import org.apache.curator.framework.recipes.leader.{LeaderSelector, LeaderSelectorListenerAdapter, LeaderSelectorListener}
import org.apache.curator.framework.recipes.locks.InterProcessMutex
import org.apache.curator.framework.{CuratorFrameworkFactory, CuratorFramework}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.utils.CloseableUtils
import org.apache.zookeeper.CreateMode

object ZookApps extends App {

  val PATH = "/example/basic"
  val LOCKPATH = "/example/lock"
  val client = createClient("192.168.2.200:2182")
  client.start()
  if (client.checkExists().forPath(PATH) != null) {
    client.delete().forPath(PATH)
  }
  client.create.creatingParentsIfNeeded.withMode(CreateMode.PERSISTENT).forPath(PATH, "fuck51test".getBytes)

  /* distributed lock */
  val lock: InterProcessMutex = new InterProcessMutex(client, LOCKPATH)
  if (lock.acquire(1000, TimeUnit.SECONDS)) {
    try {
      // do sth
      println("got the lock")
    }
    finally {
      lock.release
    }
  }

  val LeaderPath = "/example/leader"
  val listener = new LeaderSelectorListenerAdapter() {
    override def takeLeadership(curatorFramework: CuratorFramework): Unit = {
      // this callback will get called when you are the leader
      // do whatever leader work you need to and only exit
      // this method when you want to relinquish leadership
      println("now is the leader")
    }
  }

  val selector = new LeaderSelector(client, LeaderPath, listener);
  selector.autoRequeue(); // not required, but this is behavior that you will probably expect
  selector.start();

  CloseableUtils.closeQuietly(client);

  def createClient(zk: String) = {
    val retryPolicy: ExponentialBackoffRetry = new ExponentialBackoffRetry(1000, 3)
    CuratorFrameworkFactory.newClient(zk, retryPolicy)
  }
}