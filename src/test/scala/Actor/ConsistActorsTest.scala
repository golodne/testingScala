package Actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, Suite, WordSpecLike}

import scala.concurrent.duration._

class ConsistActorsTest extends TestKit(ActorSystem("MySpec"))
  with WordSpecLike
  with Matchers
  with StopSystemAfterAll {

  " test ConsistActors" must {
    "send to actors " in {
      val endProbe = TestProbe()
      val speedFilterRef = system.actorOf(Props(new SpeedFilter(50, endProbe.ref)))
      val licenseFilterRef = system.actorOf(Props(new LicenseFilter(speedFilterRef)))

      val msg = new Photo("123zx",60)
      licenseFilterRef ! msg
      endProbe.expectMsg(msg)

      licenseFilterRef ! new Photo("", 55)
      endProbe.expectNoMsg(1 seconds)

      //хорошую скорость не пропускам, отфильтровываем
      val msg2 = new Photo("164er", 40)
      speedFilterRef ! msg2
      endProbe.expectNoMsg(1 seconds)
    }
  }
}

case class Photo(license: String, speed: Int)

class LicenseFilter(pipe: ActorRef) extends Actor {
  override def receive: Receive = {
    case msg: Photo =>
      if (!msg.license.isEmpty) pipe ! msg
  }
}

class SpeedFilter(minSpeed: Int, pipe: ActorRef) extends Actor {
  override def receive: Receive = {
    case r: Photo =>
      if (r.speed > minSpeed) pipe ! r
  }
}

trait StopSystemAfterAll extends BeforeAndAfterAll {

    this: TestKit with Suite =>
    override protected def afterAll(): Unit = {
      super.afterAll()
      system.terminate()
    }
  }