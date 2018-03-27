package com.lightbend.akka.http.sample

import akka.testkit.SocketUtil
import com.github.simplyscala.{ MongoEmbedDatabase, MongodProps }
import org.mongodb.scala.{ MongoClient, MongoDatabase }
import org.scalatest.{ BeforeAndAfterAll, WordSpec }

trait MongoDBSpec extends WordSpec with BeforeAndAfterAll with MongoEmbedDatabase {
  protected val mongoPort = SocketUtil.temporaryLocalPort()

  var mongoProps: MongodProps = null

  protected override def beforeAll(): Unit = {
    mongoProps = mongoStart(mongoPort)
  }

  protected override def afterAll(): Unit = {
    mongoStop(mongoProps)
  }

  protected lazy val testMongoDB: MongoDatabase = MongoClient(s"mongodb://localhost:$mongoPort").getDatabase("akka-http-crud")
}
